import java.awt.*;
import javax.swing.event.*;

public class ObjectSpinnerListener implements ChangeListener {
  public static final String X_KEY = "OSL_X";
  public static final String Y_KEY = "OSL_Y";
  public static final String HEIGHT_KEY = "OSL_HEIGHT";
  public static final String WIDTH_KEY = "OSL_WIDTH";
  public static final String DIR_KEY = "OSL_DIR";
  public static final String X_HANDLE_KEY = "OSL_X_HANDLE";
  public static final String Y_HANDLE_KEY = "OSL_Y_HANDLE";
  private Env env;
  private boolean enabled = true;

  public ObjectSpinnerListener( Env e ) {
    env = e;
  }

  public void stateChanged( ChangeEvent e ) {
    if( enabled ) {
      Artifact a = env.getCurArtifact();
      NumberSpinner s = (NumberSpinner) e.getSource();
      int val = (int) s.getValue();
      Point loc;
      Dimension dim;

      switch( s.getKey() ) {
      case X_KEY:
        loc = a.getLocation();
        a.startModification( loc );
        loc.x = val;
        a.moveArtifact( loc );
        a.endModification();
        break;
      case Y_KEY:
        loc = a.getLocation();
        a.startModification( loc );
        loc.y = val;
        a.moveArtifact( loc );
        a.endModification();
        break;
      case WIDTH_KEY:
        dim = a.getSize();
        dim.width = val;
        a.startModification( a.getLocation() );
        ((BlockArtifact)a).resizeBlock( dim );
        a.endModification();
        break;
      case HEIGHT_KEY:
        dim = a.getSize();
        dim.height = val;
        a.startModification( a.getLocation() );
        ((BlockArtifact)a).resizeBlock( dim );
        a.endModification();
        break;
      case DIR_KEY:
        a.startModification( a.getLocation() );
        a.setDirDeg( val );
        a.endModification();
        break;
      case X_HANDLE_KEY:
        loc = a.getHandleLocation();
        if( loc != null ) {
          a.startModification( loc );
          loc.x = val;
          a.moveHandle( loc );
          a.endModification();
        }
        break;
      case Y_HANDLE_KEY:
        loc = a.getHandleLocation();
        if( loc != null ) {
          a.startModification( loc );
          loc.y = val;
          a.moveHandle( loc );
          a.endModification();
        }
        break;
      }
    }
  }

  public void setEnable( boolean b ) {
    enabled = b;
  }
}

