import java.awt.event.*;
import javax.swing.*;

public class ToolBarPanel extends JToolBar {
  public final static String BLOCK_BUTTON = "Add a block";
  public final static String PATH_BUTTON = "Add a path";
  public final static String POLYGON_BUTTON = "Add a polygon block or mark";
  public final static String ROBOT_BUTTON = "Add a robot";

  public ToolBarPanel( ActionListener a ) {
    addButton( BLOCK_BUTTON, "block.png", a );
    addButton( PATH_BUTTON, "path.png", a );
    addButton( POLYGON_BUTTON, "polygon.png", a );
    addButton( ROBOT_BUTTON, "robot.png", a );

    RobotField rf = new RobotField( RobotArtifact.factory.getRobotsModel() );
    rf.addItemListener( RobotArtifact.factory );
    add( rf );
    
    setFloatable( false );
  }

  private void addButton( String cmd, String file, ActionListener a ) {
    JButton b = new JButton( new ImageIcon( ImageLoader.load( file ) ) );
    b.setToolTipText( cmd );
    b.setActionCommand( cmd );
    b.addActionListener( a );
    add( b );
  }
}

