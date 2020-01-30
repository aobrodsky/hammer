import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class ThymioPrefsPanel extends PrefPanel implements RobotControl, 
                                                         ChangeListener,
                                                         ItemListener {
  Env env;
  final private JSpinner [] spinners = new JSpinner[8];
  private FieldGridLayout layout;
  private boolean listenerEnabled = true;

  public ThymioPrefsPanel() {
    super( "Thymio Preferences" );
  }

  public void init( Env e ) {
    env = e;

    add( new JLabel( "Motor and Sensor Noise" ) );
    
    JPanel panel = new JPanel();
    layout = new FieldGridLayout( panel );
    panel.setLayout( layout );

    makeField( panel, "Left Motor Bias (\u00B110%):", 0, -1, 1, 
               true, CommonData.THYMIO_NOISE_IDX_LEFT_MOTOR_BIAS );
    makeField( panel, "Right Motor Bias (\u00B110%):", 0, -1, 1, 
               true, CommonData.THYMIO_NOISE_IDX_RIGHT_MOTOR_BIAS );
    makeField( panel, "Left Motor Var. (<10%):", 0, 0, 1,
               true, CommonData.THYMIO_NOISE_IDX_LEFT_MOTOR_VAR );
    makeField( panel, "Right Motor Var. (<10%):", 0, 0, 1,
               true, CommonData.THYMIO_NOISE_IDX_RIGHT_MOTOR_VAR );
    makeField( panel, "Ground Sensor Bias (\u00B150):", 0, -1000, 1000, 
               false, CommonData.THYMIO_NOISE_IDX_GROUND_SENSOR_BIAS );
    makeField( panel, "Ground Sensor Var. (<5):", 0, 0, 100, 
               false, CommonData.THYMIO_NOISE_IDX_GROUND_SENSOR_VAR );
    makeField( panel, "Horizontal Sensor Bias (\u00B1300):", 0, -1000, 1000, 
               false, CommonData.THYMIO_NOISE_IDX_HORIZ_SENSOR_BIAS );
    makeField( panel, "Horizontal Sensor Var. (<20):", 0, 0, 500, 
               false, CommonData.THYMIO_NOISE_IDX_HORIZ_SENSOR_VAR );
    add( panel );

    panel = new JPanel();
    panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
    JPanel subpanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    JCheckBox cb = new JCheckBox( "<html>Show buttons and LEDs" + 
                                  "<br>Uncheck this to improve performance.", 
                                  true );
    cb.addItemListener( this );
    subpanel.add( cb );
    panel.add( subpanel );
    add( new CollapsePanel( "Advanced", panel ) );
  }

  private void makeField( JPanel panel, String title, double init, double min, 
                          double max, boolean prop, int pos ) {
    layout.addLabel( new JLabel( title ) );
    if( prop ) {
      spinners[pos] = new PercentSpinner( init, min, max, null );
    } else {
      spinners[pos] = new JSpinner( new SpinnerNumberModel(init, min, max, 1) );
    }
    spinners[pos].addChangeListener( this );
    layout.addField( spinners[pos] );
  }

  public void setTarget( short id ) {
    ThymioArtifact t = (ThymioArtifact) env.getRobotArtifact( id );
    double [] noise = t.getNoise();
    listenerEnabled = false;
    for( int i = 0; i < spinners.length; i++ ) {
      spinners[i].setEnabled( true );
      spinners[i].setValue( noise[i] );
    }
    listenerEnabled = true;
  }

  public void setTargetAll() {
    for( JSpinner s : spinners ) {
      s.setEnabled( false );
    }
  }

  public short getType() {
    return CommonData.ROBOT_TYP_THYMIO;
  }

  public void stateChanged( ChangeEvent e ) {
    if( listenerEnabled ) {
      double [] noise = new double[spinners.length];
      for( int i = 0; i < noise.length; i++ ) {
        noise[i] = (double) spinners[i].getValue();
      }
    
      ThymioArtifact t = (ThymioArtifact) env.getCurArtifact();
      t.setNoise( noise );
      env.userAction( t );
    }
  }

  public void itemStateChanged( ItemEvent e ) {
    ThymioArtifact.showDetailsEnabled(e.getStateChange() == ItemEvent.SELECTED);
    env.updateComplete();
  }
}
