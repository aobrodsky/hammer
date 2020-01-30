import java.util.*;

public class UndoManager {
  private Stack<UndoAction> undoStack = new Stack<UndoAction>();
  private Stack<UndoAction> redoStack = new Stack<UndoAction>();
  private UndoListener listener;
  private boolean inUndo = false;
  private boolean enabled = true;

  private enum ActionType {
    CREATE,
    DELETE,
    CHANGE,
    GROUP,
    UNGROUP
  };

  public interface UndoListener {
    public void redoAvailable( boolean b );
    public void undoAvailable( boolean b );
  }

  private class UndoAction {
    public Artifact artifact;
    public ArtifactCheckPoint checkPoint;
    public ActionType typ;

    public UndoAction( Artifact a, ArtifactCheckPoint acp, ActionType t ) {
      artifact = a;
      checkPoint = acp;
      typ = t;
    }
  }

  public UndoManager( UndoListener l ) {
    listener = l;
  }

  private void pushAction( Artifact a, ArtifactCheckPoint acp, ActionType t ) {
    if( enabled ) {
      undoStack.push( new UndoAction( a, acp, t ) );
      if( ( listener != null ) && ( undoStack.size() < 2 ) ) {
        listener.undoAvailable( true );
      }
        
      if( !inUndo && !redoStack.empty() ) {
        redoStack.clear();
        if( listener != null ) {
          listener.redoAvailable( false );
        }
      }
    }
  }

  public void pushCreate( Artifact a ) {
    pushAction( a, null, ActionType.CREATE );
  }

  public void pushGroup( Artifact a ) {
    pushAction( a, null, ActionType.GROUP );
  }

  public void pushDelete( Artifact a ) {
    pushAction( a, null, ActionType.DELETE );
  }

  public void pushUngroup( Artifact a ) {
    pushAction( a, null, ActionType.UNGROUP );
  }

  public void pushChange( ArtifactCheckPoint acp  ) {
    if( acp.useful() ) {
      pushAction( acp.artifact, acp, ActionType.CHANGE );
    }
  }

  public boolean hasUedo() {
    return !undoStack.empty();
  }

  public boolean hasRedo() {
    return !redoStack.empty();
  }

  public boolean redo() {
    if( enabled && pop( redoStack ) ) {
      if( listener != null ) {
        if(  undoStack.empty() ) {
          listener.undoAvailable( false );
        }
        if( undoStack.size() < 2 ) {
          listener.redoAvailable( true );
        }
      }
      return true;
    }
    return false;
  }

  public boolean undo() {
    if( enabled && pop( undoStack ) ) {
      redoStack.push( undoStack.pop() );
      if( listener != null ) {
        if(  undoStack.empty() ) {
          listener.undoAvailable( false );
        }
        if( redoStack.size() < 2 ) {
          listener.redoAvailable( true );
        }
      }
      return true;
    }
    return false;
  }

  private boolean pop( Stack<UndoAction> stk ) {
    if( stk.empty() ) {
      return false;
    }

    UndoAction u = stk.pop();

    inUndo = true;
    switch( u.typ ) {
    case CREATE:
      u.artifact.delete();
      break;
    case GROUP:
      u.artifact.remove();
      break;
    case DELETE:
      u.artifact.insert();
      break;
    case UNGROUP:
      u.artifact.insert();
      break;
    case CHANGE:
      u.artifact.revert( u.checkPoint );
      break;
    }
    inUndo = false;

    return true;
  }

  public void setEnabled( boolean b ) {
    enabled = b;
  }

  private void printStack( Stack<UndoAction> stk ) {
    for( UndoAction u : stk ) {
      System.out.print( " " + u.typ );
    }
    System.out.println( "" );
  }
}
