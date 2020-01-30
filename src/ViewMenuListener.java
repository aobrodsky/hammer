import java.awt.event.*;
import javax.swing.*;

public class ViewMenuListener extends AbstractAction {
  private Arena arena;
  private MenuBarView menuBar;
  private ConnectionPanel connectionPanel;
 
  public ViewMenuListener( MenuBarView m, Arena a, ConnectionPanel cp ) {
    connectionPanel = cp;
    arena = a;
    menuBar = m;
  }

  public void actionPerformed( ActionEvent e ) {
    String desc = e.getActionCommand();

    switch( desc ) {
    case MenuBarView.ZOOM_IN:
      arena.changeZoom( 0.01 );
      break;  
    case MenuBarView.ZOOM_OUT:
      arena.changeZoom( -0.01 );
      break;
    case MenuBarView.RECONNECT:
      connectionPanel.resetConnection();
      break;
    default:
      System.out.println( "Oops: Unknown view menu choice" );
    }
  }
}
