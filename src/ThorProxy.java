import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.nio.channels.*;

public class ThorProxy extends Thread {
  private SocketChannel front;
  private SocketChannel back;
  private ServerSocketChannel servSock;
  private String thorHost;
  private int thorPort;
  private boolean dead = false;
  private Vector<ThorProxyListener> listeners = new Vector<ThorProxyListener>();
  private ByteBuffer headerBuffer = ByteBuffer.allocate( 6 );
  private ByteBuffer payloadBuffer = ByteBuffer.allocate( 65536 );
  private ByteBuffer dataQueue = ByteBuffer.allocate( 65536 + 6 );
  private int packetsRecvd = 0;
  private int packetsSent = 0;

  private void accepted() {
    for( ThorProxyListener l : listeners ) {
      l.accepted( this );
    }
  }

  private void connected() {
    for( ThorProxyListener l : listeners ) {
      l.connected( this );
    }
  }

  private void disconnected() {
    for( ThorProxyListener l : listeners ) {
      l.disconnected( this );
    }
  }

  private void outOfBandPacket( short source, byte [] data ) {
    packetsRecvd++;
    for( ThorProxyListener l : listeners ) {
      l.outOfBandPacket( this, source, data );
    }
  }

  public ThorProxy( String host, int local, int remote ) throws IOException {
    thorHost = host;
    thorPort = remote;
    servSock = ServerSocketChannel.open();
    servSock.bind( new InetSocketAddress( "localhost", local ) );
    headerBuffer.order( ByteOrder.LITTLE_ENDIAN );
    dataQueue.order( ByteOrder.LITTLE_ENDIAN );
  }

  public void addListener( ThorProxyListener l ) {
    listeners.add( l );
  }

  
  //  Background servSock task.
  public void run() {
    while( !dead ) {
      boolean established = false;
      try {
        System.out.println( "Accepting connections" );
        front = servSock.accept();
        front.setOption( StandardSocketOptions.SO_KEEPALIVE, true );
        front.setOption( StandardSocketOptions.TCP_NODELAY, true );
        accepted();

        back = SocketChannel.open();
        back.connect( new InetSocketAddress( thorHost, thorPort ) );
        established = true;
        back.setOption( StandardSocketOptions.SO_KEEPALIVE, true );
        back.setOption( StandardSocketOptions.TCP_NODELAY, true );

        Scanner in = new Scanner( back.socket().getInputStream() );
        connected();

        front.configureBlocking( false );
        back.configureBlocking( false );
        Selector sel = Selector.open();
        SelectionKey fkey = front.register( sel, SelectionKey.OP_READ, null );
        SelectionKey bkey = back.register( sel, SelectionKey.OP_READ, null );
  
        boolean finished = false;
        while( !finished && front.isConnected() && back.isConnected() ) { 
          if( sel.select() > 0 ) {
            for( SelectionKey s : sel.selectedKeys() ) {
              if( ( s == bkey ) && !moveData( back, front ) ) {
                finished = true;
                break;
              } else if( ( s == fkey ) && !moveData( front, back ) ) {
                finished = true;
                break;
              }
            }
            sel.selectedKeys().clear();
          }
          checkAndSend();
        }
        System.out.println( "Connection to Thor closed" );
      } catch ( UnresolvedAddressException ex ) {
        System.out.println( "Unknown server: " + thorHost );
        System.out.println( "Select another server." );
        close();
        break;
      } catch ( ConnectException ex ) {
        if( established ) {
          System.out.println( "Connection to Thor closed" );
        } else {
          System.out.println( "Connection to Thor refused" );
        }

        if( !established || dead ) {
          close();
        }
        break;
      } catch ( Exception ex ) {
        if( dead ) {
          break;
        } else {
          System.out.println( "Connection closed, retrying"  );
        } 
      }
      disconnect();
    }
  }

  private boolean readBuffer( SocketChannel src, ByteBuffer b ) 
                              throws IOException {
    while( b.hasRemaining() ) {
      if( src.read( b ) < 0 ) {
        return false;
      }
    }
    return true;
  }

  private boolean sendBuffer( SocketChannel dst, ByteBuffer b )
                              throws IOException {
    while( b.hasRemaining() ) {
      if( dst.write( b ) < 0 ) {
        return false;
      }
    }
    return true;
  }
    
  private boolean moveData( SocketChannel src, SocketChannel dst ) 
                            throws IOException {
    headerBuffer.clear();
    if( !readBuffer( src, headerBuffer ) ) {
      return false;
    }
    headerBuffer.flip();
    int length = headerBuffer.getShort() & 0xffff;
    int source = headerBuffer.getShort() & 0xffff;
    int type = headerBuffer.getShort() & 0xffff;

    payloadBuffer.clear();
    payloadBuffer.limit( length );
    if( !readBuffer( src, payloadBuffer ) ) {
      return false;
    }
    payloadBuffer.flip();

    if( type == 0xffff ) {
      byte [] payload = new byte[length];
      payloadBuffer.get( payload, 0, length );
      outOfBandPacket( (short)source, payload );
      return true;
    } 

    headerBuffer.rewind();
    return sendBuffer( dst, headerBuffer ) && sendBuffer( dst, payloadBuffer );
  }

  public void close() {
    try {
      dead = true;
      interrupt();
      if( servSock != null ) {
        servSock.close();
      }
      if( front != null ) {
        disconnect();
      }
    } catch ( Exception e ) { }
    servSock = null;
  }
 
  private void disconnect() {
    try {
      if( front != null ) {
        front.close();
      }
    } catch ( Exception e ) { }
    front = null;

    try {
      if( back != null ) {
        back.close();
      }
    } catch ( Exception e ) { }
    back = null;
    disconnected();
  }

  private boolean checkAndSend() throws IOException {
    boolean rc = true;
    synchronized( dataQueue ) {
      if( dataQueue.position() > 0 ) {
        dataQueue.flip();
        rc = sendBuffer( back, dataQueue );
        dataQueue.clear();
        packetsSent++;
      }
    }
    return true;
  }

  public void sendData( byte [] pkt, int length ) {
    synchronized( dataQueue ) {
      dataQueue.putShort( (short)length );
      dataQueue.putShort( (short)0 ); // packet source is a client
      dataQueue.putShort( (short)0xffff ); // VM ignores INVALID typ
      dataQueue.put( pkt );
    }
  }
  
  public int getNumberOfPacketsSent() {
    return packetsSent;
  }

  public int getNumberOfPacketsRecvd() {
    return packetsRecvd;
  }
}
