import java.io.*;

public class ThorProxyOutputStream extends PrintStream  {
  private ByteArrayOutputStream bufferStream; 
  private ThorProxy thorProxy;

  public ThorProxyOutputStream( ThorProxy tp, ByteArrayOutputStream bs ) {
    super( bs );
    bufferStream = bs;
    thorProxy = tp;
  }

  public void close() {
    super.close();

    if( bufferStream != null ) {
      thorProxy.sendData( bufferStream.toByteArray(), bufferStream.size() );
      bufferStream.reset();
      bufferStream = null;
    }
  }
}
