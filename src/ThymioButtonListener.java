import java.awt.event.*;
import javax.swing.*;

public class ThymioButtonListener extends MouseAdapter {
  public static final String FWD = "Forward";
  public static final String BACK = "Back";
  public static final String LEFT = "Left";
  public static final String RIGHT = "Right";
  public static final String CENTER = "Center";
  public static final String TAP = "Tap";

  private static final String [] buttons = new String[CommonData.NUM_BUTTONS];

  static {
    // Order: { BACK, LEFT, CENTER, FWD, RIGHT }
    buttons[CommonData.THYMIO_BUTTON_IDX_BACK] = BACK;
    buttons[CommonData.THYMIO_BUTTON_IDX_LEFT] = LEFT;
    buttons[CommonData.THYMIO_BUTTON_IDX_CENTER] = CENTER;
    buttons[CommonData.THYMIO_BUTTON_IDX_FWD] = FWD;
    buttons[CommonData.THYMIO_BUTTON_IDX_RIGHT] = RIGHT;
  }
    
  private final short [] butn_state = new short[CommonData.NUM_BUTTONS];
  private Env env;

  ThymioButtonListener( Env e ) {
    env = e;
  }

  private void toggleButton( JButton b, boolean on ) {
    String cmd = b.getActionCommand();

    switch( cmd ) {
    case TAP:
        setTap( on );
      break;
    default:
      for( int i = 0; i < 5; i++ ) {
        if( buttons[i].equals( cmd ) ) {
          butn_state[i] = (short) (on ? 1 : 0);
          setButtonState( butn_state );
          break;
        }
      }
      break;
    }
  }

  public void mousePressed( MouseEvent e ) {
    toggleButton( (JButton)e.getSource(), true );
  }

  public void mouseReleased( MouseEvent e ) {
    toggleButton( (JButton)e.getSource(), false );
  }

  public void setButtonState( ThymioArtifact t, short [] bs ) {
    t.setButtonState( bs );
    env.userAction( t );
  }

  public void setButtonState( short [] bs ) {
    Artifact a = env.getCurArtifact();
    if( ( a != null ) && ( a instanceof ThymioArtifact ) ) {
      setButtonState( (ThymioArtifact) a, bs );
    } else {
      for( RobotArtifact t : env.getModel().getRobots() ) {
        setButtonState( (ThymioArtifact) t, bs );
      }
    }
  }

  public void setTap( ThymioArtifact t, boolean b ) {
    t.setTap( b );
    env.userAction( t );
  }

  public void setTap( boolean b ) {
    Artifact a = env.getCurArtifact();
    if( ( a != null ) && ( a instanceof ThymioArtifact ) ) {
      setTap( (ThymioArtifact) a, b );
    } else {
      for( RobotArtifact t : env.getModel().getRobots() ) {
        setTap( (ThymioArtifact) t, b );
      }
    }
  }
}


