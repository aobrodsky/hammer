import java.net.*;
import javax.swing.*;

public class Hammer {
  private static JFrame mainWindow;

  static {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty( "apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS" );
  }

  public static void main( String s[] ) {
    try {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    } catch ( Exception e ) { }

    mainWindow = new HammerController();
  }

  public static URL codeBase() {
    if( mainWindow != null ) {
      // return mainWindow.getCodeBase();
    }
    return null;
  }
}
