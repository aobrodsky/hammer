import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class RobotActionPanel extends JPanel {
  private Component curControl;
  Env env;

  public RobotActionPanel( Env e ) {
    super();
    env = e;
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
    setBorder( LineBorder.createBlackLineBorder() );
  }

  public void add( JComponent c ) {
    RobotControl rc = (RobotControl) c;
    rc.init( env );
    super.add( c );
  }

  public boolean setActionPanel( short typ ) {
    if( curControl != null ) {
      RobotControl rc = (RobotControl) curControl;
      if( rc.getType() == typ ) {
        return true;
      }
      curControl.setVisible( false );
      curControl = null;
    } 

    for( Component c : getComponents() ) {
      if( c instanceof RobotControl ) {
        RobotControl rc = (RobotControl) c;

        if( rc.getType() == typ ) {
          c.setVisible( true );
          curControl = c;
          break;
        }
      }
    }
    revalidate();     
    repaint();     

    return curControl != null;
  }

  public void setTarget( short id ) {
    RobotControl rc = (RobotControl) curControl;
    if( rc != null ) {
      rc.setTarget( id );
    }
  }

  public void setTargetAll() {
    RobotControl rc = (RobotControl) curControl;
    if( rc != null ) {
      rc.setTargetAll();
    }
  }
}
