import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class ThymioActionPanel extends JPanel implements RobotControl, 
                                                         ActionListener {
  private ThymioPrefsPanel thymioPrefsPanel = new ThymioPrefsPanel();
  private JLabel target = new JLabel( "Robot: All" );
  private final Vector<JComponent> thymioButtons = new Vector<JComponent>();

  public ThymioActionPanel() {
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
  }

  public void init( Env e ) {
    MouseListener ml = new ThymioButtonListener( e );

    JPanel panel = new JPanel( new FlowLayout() );
    panel.add( target );
    add( panel );

    JToolBar bar = new JToolBar();
    bar.setFloatable( false );
    bar.add( makeButton( "blank.png", null, null ) );
    bar.add( makeButton( "up_arrow.png", ThymioButtonListener.FWD, ml ) );
    bar.add( makeButton( "blank.png", null, null ) );
    add( bar );

    bar = new JToolBar();
    bar.setFloatable( false );
    bar.add( makeButton( "left_arrow.png", ThymioButtonListener.LEFT, ml ) );
    bar.add( makeButton("center_button.png", ThymioButtonListener.CENTER, ml) );
    bar.add( makeButton(  "right_arrow.png", ThymioButtonListener.RIGHT, ml ) );
    add( bar );

    bar = new JToolBar();
    bar.setFloatable( false );
    JButton b = makeButton(  "prefs_button.png", "Thymio Preferences", null );
    bar.add( b );
    bar.add( makeButton(  "down_arrow.png", ThymioButtonListener.BACK, ml ) );
    bar.add( makeButton( "tap.png", ThymioButtonListener.TAP, ml ) );
    add( bar );
  
    thymioPrefsPanel.init( e );
    b.addActionListener( this );
  }

  private JButton makeButton( String icn, String cmd, MouseListener m ) { 
    JButton b = new JButton( new ImageIcon( ImageLoader.load( icn ) ) );
    if( cmd != null ) {
      if( m != null ) {
        b.addMouseListener( m );
      }
      b.setActionCommand( cmd );
      b.setToolTipText( cmd );
    } else {
      b.setEnabled( false );
    }
    b.setFocusable( false );
    thymioButtons.add( b );
    return b;
  }

  public void setTarget( short id ) {
    target.setText( "Robot: " + id );
    thymioPrefsPanel.setTarget( id );
  }

  public void setTargetAll() {
    target.setText( "Robot: all" );
    thymioPrefsPanel.setTargetAll();
  }

  public short getType() {
    return CommonData.ROBOT_TYP_THYMIO;
  }

  public void actionPerformed( ActionEvent e ) {
    thymioPrefsPanel.setVisible( !thymioPrefsPanel.isVisible() );
  }

}
