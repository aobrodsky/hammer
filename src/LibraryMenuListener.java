import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LibraryMenuListener extends AbstractAction {
  final java.awt.FileDialog fileChooser = new java.awt.FileDialog((Frame)null);
  private Env env;
  private File lastLibDir = new File( AppPrefs.appPrefs.getLastLibDir() ); 
  final static String retryMsg = "Retry?";
  private LibraryManager libMgr;

  private class lbrFilter implements FilenameFilter {
    public boolean accept( File dir, String name ) {
      return name.endsWith( ".lbr" );
    }
  }

  public LibraryMenuListener( Env e, LibraryManager lm ) {
    env = e;
    libMgr = lm;
    fileChooser.setFilenameFilter( new lbrFilter() );
    fileChooser.setFile( "*.lbr" );
  }

  public void actionPerformed( ActionEvent e ) {
    String desc = e.getActionCommand();

    switch( desc ) {
    case MenuBarView.LIB_SELECT:
      // Select artifact
      if( libMgr.selectArtifact() ) {
      }
      break;
    case MenuBarView.LIB_ADD:
      Artifact a = env.getCurArtifact();
      if( ( a != null ) && !( a instanceof RobotArtifact ) ) {
        libMgr.addArtifact( a );
      }
      break;
    case MenuBarView.LIB_NEW:
    case MenuBarView.LIB_LOAD:
      if( chooseFile( desc ) ) {
        env.selectArtifact( null, false );
      }
      break;
    default:
      System.out.println( "Oops: Unknown file menu choice" );
    }
  }

  private boolean chooseFile( String desc ) {
    fileChooser.setDirectory( lastLibDir.getAbsolutePath() );
    fileChooser.setTitle( desc );
    if( desc.equals( MenuBarView.LIB_LOAD ) ) {
      fileChooser.setMode( java.awt.FileDialog.LOAD );
    } else {
      fileChooser.setMode( java.awt.FileDialog.SAVE );
    }
    fileChooser.setVisible( true );

    boolean success = false;
    File [] select = fileChooser.getFiles();
    if( select.length > 0 ) {
      File file = select[0];
      lastLibDir = new File( fileChooser.getDirectory() );
      switch( desc ) {
      case MenuBarView.LIB_LOAD:
        success = libMgr.loadLibrary( file );
        break;
      case MenuBarView.LIB_NEW:
        success = libMgr.newLibrary( file );
        break;
      }

      if( !success ) {
        EnvIO eio = env.getEnvIO();
        String msg ="Aborting because '" + desc + "' failed: " +
                    eio.getLastErr();
        String title = desc + "failed!";
        JOptionPane.showMessageDialog( null, msg, title,
                                       JOptionPane.ERROR_MESSAGE );
      } else {
        AppPrefs.appPrefs.setLastLibDir( lastLibDir.getPath() );
      }
    }
    return success;
  }
}
