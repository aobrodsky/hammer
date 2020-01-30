import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class EditMenuListener extends AbstractAction {
  private Env env;
  private MenuBarView menuBar;
  private Artifact clipBoard;
  private UndoManager undoManager;
 
  public EditMenuListener( MenuBarView m, Env e ) {
    env = e;
    menuBar = m;
    undoManager = new UndoManager( new UndoUpdater() );
    env.addArtifactListener( new UndoHandler() );
  }

  public void actionPerformed( ActionEvent e ) {
    String desc = e.getActionCommand();
    Artifact a = env.getCurArtifact();

    switch( desc ) {
    case MenuBarView.UNDO:
      undoManager.undo();
      break;
    case MenuBarView.REDO:
      undoManager.redo();
      break;
    case MenuBarView.COPY:
      if( ( a != null ) && !( a instanceof RobotArtifact ) ) {
        clipBoard = a.deepCopy();
        menuBar.setEditMenuItemEnabled( MenuBarView.PASTE, true );
      }
      break;
    case MenuBarView.PASTE:
      if( clipBoard != null ) {
        Point p = clipBoard.getLocation();
        p.x += 10;
        p.y += 10;
        clipBoard.setLocation( p );
        clipBoard.addCopy();
      }
      break;
    case MenuBarView.DELETE:
      if( a != null ) {
        a.delete();
      }
      break;
    case MenuBarView.SELECTALL:
      env.selectAll();
      break;
    case MenuBarView.GROUP:
      env.group();
      break;
    case MenuBarView.UNGROUP:
      env.ungroup();
      break;
    case MenuBarView.CONVERT:
        if( ( a != null ) && !( a instanceof PolygonArtifact ) ) {
          int ok = JOptionPane.showConfirmDialog( null, 
                                         "Are you sure? This cannot be undone.",
                                         "Are you sure?",
                                         JOptionPane.YES_NO_OPTION );
          if( ok == JOptionPane.YES_OPTION ) {
            undoManager.setEnabled( false );
            a = a.convertToPolygon();
            env.setCurArtifact( a );
            undoManager.setEnabled( true );
          }
        }
      break;
    case MenuBarView.RESET:
      env.resetRobotPosition();
      break;
    default:
      System.out.println( "Oops: Unknown edit menu choice" );
    }
  }

  private class UndoUpdater implements UndoManager.UndoListener {
    public void redoAvailable( boolean b ) {
      menuBar.setEditMenuItemEnabled( MenuBarView.REDO, b );
    }

    public void undoAvailable( boolean b ) {
      menuBar.setEditMenuItemEnabled( MenuBarView.UNDO, b );
    }
  }

  private class UndoHandler implements ArtifactListener {
    public void artifactChanged( Artifact a, ArtifactCheckPoint acp ) {
      if( acp != null ) {
        undoManager.pushChange( acp );
      }
    }

    public void artifactCompleted( Artifact a ) {
      if( !a.inGroup() ) {
        if( (a instanceof GroupArtifact) && !((GroupArtifact)a).isUnbound() ) {
          undoManager.pushGroup( a );
        } else {
          undoManager.pushCreate( a );
        }
      }
    }

    public void artifactRemoved( Artifact a ) {
      if( !a.inGroup() ) {
        if( (a instanceof GroupArtifact) && !((GroupArtifact)a).isUnbound() ) {
          undoManager.pushUngroup( a );
        } else {
          undoManager.pushDelete( a );
        }
      }
    }

    public boolean checkLocation( Artifact a ) {
      return true;
    }
  }
}
