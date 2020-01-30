import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class ConnectionPanel extends JToolBar {
  private Env env;
  private HostField thorHost;
  private JSpinner studioPort;
  private JSpinner thorPort;
  private JLabel status;
  private JTextField packetsSent;
  private JTextField packetsRecvd;
  private UpdateHandler commHandler;
  private Vector<JComponent> connectControls = new Vector<JComponent>();
  private ThorProxy proxy;
  private JButton retryButton;
  private FieldGridLayout layout;

  private static final Color FOREST_GREEN = new Color( 34, 139, 34 );
  private class HostFieldListener implements ItemListener {
    public void  itemStateChanged( ItemEvent e ) {
      if( e.getStateChange() == e.SELECTED ) {
        runThorProxy();
      }
    }
  }

  private class UIProxyHandler implements ThorProxyListener  {
    int countDown = -1;
    int sent;
    int recvd;

    public void accepted( ThorProxy tp ) {
      status.setForeground( Color.BLACK );
      status.setText( "Attempting to connect to server" );
      System.out.println( "Accepted connection from Aseba Studio" );
    }

    public void connected( ThorProxy tp ) {
      countDown = 3;
      status.setForeground( FOREST_GREEN );
      status.setText( "Connected to server" );
      System.out.println( "Connected to Thor server" );
      AppPrefs.appPrefs.setLastProxyPort( (int) studioPort.getValue() );
      AppPrefs.appPrefs.setLastRemotePort( (int) thorPort.getValue() );
    }

    public void disconnected( ThorProxy tp ) {
      countDown = -1;
      status.setForeground( Color.RED );
      status.setText( "Disconnected" );
    } 

    public void outOfBandPacket( ThorProxy tp, short source, byte [] data ) {
      int s = tp.getNumberOfPacketsSent();
      int r = tp.getNumberOfPacketsRecvd();

      if( s != sent ) {
        sent = s;
        packetsSent.setText( "" + sent );
      }

      if( r != recvd ) {
        recvd = r;
        packetsRecvd.setText( "" + recvd );
      }

      countDown--;
      if( countDown == 0 ) {
        // Assume connection was successful (usually true)
        thorHost.rememberHost();
      }
    }
  }
 

  public ConnectionPanel( Env e ) {
    env = e;
    commHandler = new UpdateHandler( env );

    add( new JLabel( " Server:" ) );
    thorHost = new HostField();
    thorHost.addItemListener( new HostFieldListener() );
    add( thorHost );
    retryButton = new JButton( new ImageIcon(ImageLoader.load("repeat.png")) );
    retryButton.setFocusable( false );
    retryButton.setToolTipText( "Restart connection" );
    retryButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        runThorProxy();
      }
    } );
    add( retryButton );

    add( new JLabel( " " ) );
    status = new JLabel( "Not Connected" );
    add( status );

    JPanel panel = new PrefPanel( "Connection Preferences" );

    JPanel subpanel = new JPanel();
    layout = new FieldGridLayout( subpanel );
    subpanel.setLayout( layout );

    studioPort = addPort( subpanel, AppPrefs.appPrefs.getLastProxyPort(),
                                "Aseba Studio Port:" );
    thorPort = addPort( subpanel, AppPrefs.appPrefs.getLastRemotePort(),
                                "Thor Server Port:" );

    packetsSent = addStatField( subpanel, "Packets sent:" );
    packetsRecvd = addStatField( subpanel, "Packets received:" );

    panel.add( subpanel );

    add( new Separator() );
    JButton b = new JButton(new ImageIcon( ImageLoader.load("net_prefs.png")) );
    b.setFocusable( false );
    b.setToolTipText( "Network Preferences" );
    b.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        panel.setVisible( !panel.isVisible() );
      }
    } );
    add( b );
   
    runThorProxy();
  }

  JTextField addStatField( JPanel panel, String label ) {
    layout.addLabel( new JLabel( label ) );

    JTextField tf = new JTextField( "     0" );
    tf.setEditable( false );
    layout.addField( tf );
    return tf;
  }

  JSpinner addPort( JPanel panel, int port, String label ) {
    layout.addLabel( new JLabel( label ) );
    JSpinner spin = new NumberSpinner( port, 1, 65535, 60, null );
    layout.addField( spin );
    return spin;
  }

  private void runThorProxy() {
    try {
      if( proxy != null ) {
        proxy.close();
        proxy.join();
        proxy = null;
      }

      proxy = new ThorProxy( thorHost.getHost(),
                           (int) studioPort.getValue(), 
                           (int) thorPort.getValue() );  
      proxy.addListener( new UIProxyHandler() );
      proxy.addListener( commHandler );
      proxy.start();
    } catch ( Exception ex ) { 
      if( proxy != null ) {
        proxy.close();
      }
      proxy = null;
      System.out.println( "Could not run proxy " + ex );
    }
  }

  private void connectControlsSetEnabled( boolean b ) {
    for( JComponent c : connectControls ) {
      c.setEnabled( b );
    }
  }

  public UpdateHandler getCommunicator() {
    return commHandler;
  }

  public void resetConnection() {
    retryButton.doClick();
  }
}
