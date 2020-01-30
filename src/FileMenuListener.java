import java.io.*;
import java.net.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.*;
import java.util.jar.*;

public class FileMenuListener extends AbstractAction {
  final java.awt.FileDialog fileChooser = new java.awt.FileDialog((Frame)null);
  final java.awt.FileDialog jarChooser = new java.awt.FileDialog((Frame)null);
  private EnvIO envIO;
  private Arena arena;
  private File lastDir = new File( AppPrefs.appPrefs.getLastDir() ); 
  private String lastFileName = "Untitled";
  private boolean quitting = false;

  final static String saveCurrent = "Save current arena?";
  final static String retryMsg = "Retry?";
 
  private class jarFilter implements FilenameFilter {
    public boolean accept( File dir, String name ) {
      return name.endsWith( ".jar" );
    }
  }

  private class hmrFilter implements FilenameFilter {
    public boolean accept( File dir, String name ) {
      return name.endsWith( ".hmr" );
    }
  }

  public FileMenuListener( Env e, Arena a ) {
    envIO = e.getEnvIO();
    arena = a;
    fileChooser.setFilenameFilter( new hmrFilter() );
    fileChooser.setFile( "*.hmr" );
    jarChooser.setFilenameFilter( new jarFilter() );
    jarChooser.setFile( "*.jar" );
    jarChooser.setMode( java.awt.FileDialog.LOAD );
  }

  public void actionPerformed( ActionEvent e ) {
    String desc = e.getActionCommand();

    switch( desc ) {
    case MenuBarView.PRINT:
      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPrintable( arena );
      job.setJobName( lastFileName );
      if( job.printDialog() ) {
        try {
          job.print();
        } catch ( PrinterException ex ) {
          System.out.println( "Print failed" );
        }
      }
      break;
    case MenuBarView.ROBOT:
      openJar();
      break;
    case MenuBarView.OPEN:
      if( saveFirst() ) {
        chooseFile( desc );
      }
      break;
    case MenuBarView.QUIT:
      if( quitting ) {
        break;
      }
      quitting = true;
    case MenuBarView.NEW:
      if( saveFirst() ) {
        if( desc.equals( MenuBarView.NEW ) ) {
          envIO.reset();
          HammerController.setCurrentFileNameInTitle( null );
        } else {
          System.exit( 0 );
        }
      }
      break;
    case MenuBarView.SAVE:
      if( envIO.saveFile() ) {
        break;
      } // fall through
    case MenuBarView.SAVEAS:
      chooseFile( desc );
      break;
    default:
      System.out.println( "Oops: Unknown file menu choice" );
    }
  }

  private boolean saveFirst() {
    boolean proceed = true;
    if( envIO.isModified() ) {
      int ok = JOptionPane.showConfirmDialog( null, saveCurrent, saveCurrent,
                                             JOptionPane.YES_NO_CANCEL_OPTION );
      if( ok == JOptionPane.YES_OPTION ) {
        proceed = envIO.saveFile() || chooseFile( MenuBarView.SAVE );
      }  else if( ok == JOptionPane.CANCEL_OPTION ) {
        proceed = false;
        quitting = false;
      }
    }
    return proceed;
  }

  private boolean chooseFile( String desc ) {
    fileChooser.setDirectory( lastDir.getAbsolutePath() );
    fileChooser.setTitle( desc );
    if( desc.equals( MenuBarView.OPEN ) ) {
      fileChooser.setMode( java.awt.FileDialog.LOAD );
    } else {
      fileChooser.setMode( java.awt.FileDialog.SAVE );
    }
    fileChooser.setVisible( true );

    boolean success = false;
    File [] select = fileChooser.getFiles();
    if( select.length > 0 ) {
      File file = select[0];
      lastDir = new File( fileChooser.getDirectory() );
      switch( desc ) {
      case MenuBarView.OPEN:
        success = envIO.loadFile( file );
        break;
      case MenuBarView.SAVE:
        success = envIO.saveFile( file );
        break;
      case MenuBarView.SAVEAS:
        success = envIO.saveFile( file );
        break;
      }

      if( !success ) {
        String msg ="Aborting because '" + desc + "' failed: " +
                    envIO.getLastErr();
        String title = desc + "failed!";
        JOptionPane.showMessageDialog( null, msg, title,
                                       JOptionPane.ERROR_MESSAGE );
      } else {
        lastFileName = file.getName();
        AppPrefs.appPrefs.setLastDir( lastDir.getPath() );
        HammerController.setCurrentFileNameInTitle( lastFileName );
      }
    }
    return success;
  }

  static String user_info = "Select JAR file containing robot description";

  private void openJar() {
    if( user_info != null ) {
      JOptionPane.showMessageDialog( null, user_info, "Note", 
                                     JOptionPane.INFORMATION_MESSAGE );
      user_info = null;
    }

    String reason = null;
    jarChooser.setVisible( true );
    File [] select = jarChooser.getFiles();
    if( select.length > 0 ) {
      try {
        URL [] url = { select[0].toURI().toURL() };
        URLClassLoader ucl = new URLClassLoader( url );
        URL u = new URL("jar", "", url[0] + "!/");
        JarURLConnection uc = (JarURLConnection)u.openConnection();
        Attributes attr = uc.getMainAttributes();
        if( attr != null ) {
          String name = attr.getValue( Attributes.Name.MAIN_CLASS );
          Class<RobotArtifact> c = (Class<RobotArtifact>)ucl.loadClass( name );
          RobotArtifact.factory.add( c.newInstance() );
          return;
        } else {
          reason = "Main class not set in JAR file.";
        }
      } catch ( Exception e ) {
        reason = e.getMessage();
      }

      String title = "Could not load robot JAR file!";
      JOptionPane.showMessageDialog( null, reason, title,
                                         JOptionPane.ERROR_MESSAGE );
    }
  }
}
