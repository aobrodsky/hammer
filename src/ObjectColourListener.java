import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ObjectColourListener implements ChangeListener, ActionListener {
  public static final String OBSTACLE = "OCL_OBSTACLE";
  public static final String MARK = "OCL_MARK";
  private Env env;
  private boolean enabled = true;

  public ObjectColourListener( Env e ) {
    env = e;
  }

  public void stateChanged( ChangeEvent e ) {
    if( enabled ) {
      JSlider s = (JSlider) e.getSource();
      int val = (int) s.getValue();
      if( val == 0 ) {
        val++;
      } else if( val == 1024 ) {
        val--;
      }
      Artifact a = env.getCurArtifact();
      a.startModification( a.getLocation() );
      a.setColour( val );
      a.endModification();
    }
  }

  public void setEnable( boolean b ) {
    enabled = b;
  }

  public void actionPerformed( ActionEvent e ) {
    if( enabled ) {
      PolygonArtifact a = (PolygonArtifact)env.getCurArtifact();
      boolean obs = a.isObstacle();

      switch( e.getActionCommand() ) {
      case OBSTACLE:
        obs = true;
        break;
      case MARK:
        obs = false;
        break;
      } 
      a.startModification( a.getLocation() );
      a.setObstacle( obs );
      a.endModification();
    }
  }
}
