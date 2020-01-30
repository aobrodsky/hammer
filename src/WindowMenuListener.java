import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class WindowMenuListener extends AbstractAction {
  JDialog logWindow;
  JCheckBoxMenuItem logCheckBoxItem;

  public WindowMenuListener( JFrame frame ) {
    logWindow = new JDialog( frame, false );
    logWindow.setLayout( new BorderLayout() );

    JComponent log = new LogPanel();
    logWindow.add( log, BorderLayout.CENTER );
    logWindow.setLocationRelativeTo( null );

    logWindow.  addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) { 
        if( logCheckBoxItem != null ) {
          logCheckBoxItem.setState( false );
        }
      }
    });
  }

  private void changeVisible( JDialog w, JCheckBoxMenuItem item ) {
    boolean visible = !w.isVisible();
    if( visible ) {
      w.pack();
    }
    w.setVisible( visible );
    item.setSelected( visible );
  }

  public void actionPerformed( ActionEvent e ) {
    String desc = e.getActionCommand();
    boolean visible;

    switch( desc ) {
    case MenuBarView.LOG_PANEL:
      logCheckBoxItem = (JCheckBoxMenuItem) e.getSource();
      changeVisible( logWindow, logCheckBoxItem );
      break;
    default:
      System.out.println( "Oops: Unknown window menu choice" );
    }
  }
}
