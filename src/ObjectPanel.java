import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

public class ObjectPanel extends JPanel {
  private Env env;
  private JTextField robotID;
  private JButton changeId;
  private RobotField robotModel;
  private JSpinner objectHeight;
  private JSpinner objectWidth;
  private JSpinner objectX;
  private JSpinner objectY;
  private JSpinner objectDir;
  private JSpinner handleX;
  private JSpinner handleY;
  private JSlider objColour;
  private JSpinner objColourSpinner;
  private JRadioButton obstacleButton;
  private JRadioButton markButton;
  private ObjectSpinnerListener objectListener;
  private ObjectColourListener objectColourListener;

  private final Vector<JComponent> blockPane = new Vector<JComponent>();
  private final Vector<JComponent> pathPane = new Vector<JComponent>();
  private final Vector<JComponent> polygonPane = new Vector<JComponent>();
  private final Vector<JComponent> groupPane = new Vector<JComponent>();
  private final Vector<JComponent> robotPane = new Vector<JComponent>();
  private Vector<JComponent> curPane = null;
  private Dimension minSize;

  public ObjectPanel( Env e ) {
    super( new WrapLayout( FlowLayout.LEFT ) );
    setBorder( LineBorder.createBlackLineBorder() );

    env = e;

    robotID = new JTextField( 3 );
    robotID.setEditable( false );

    objectX = new NumberSpinner( 0, 0, 10000, 50, ObjectSpinnerListener.X_KEY );
    objectY = new NumberSpinner( 0, 0, 10000, 50, ObjectSpinnerListener.Y_KEY );
    objectHeight = new NumberSpinner( 0, 0, 10000, 50, 
                                      ObjectSpinnerListener.HEIGHT_KEY );
    objectWidth = new NumberSpinner( 0, 0, 10000, 50, 
                                     ObjectSpinnerListener.WIDTH_KEY );
    objectDir = new NumberSpinner( 0, 0, 360, 45, 
                                    ObjectSpinnerListener.DIR_KEY );
    handleX = new NumberSpinner( 0, 0, 10000, 50, 
                                 ObjectSpinnerListener.X_HANDLE_KEY );
    handleY = new NumberSpinner( 0, 0, 10000, 50, 
                                 ObjectSpinnerListener.Y_HANDLE_KEY );
    objColour = new GrayScaleSlider( 0, 1024, 1 );
    objColourSpinner = new NumberSpinner( 0, 0, 1023, 50, null );
    obstacleButton = new JRadioButton( "Obstacle", true );
    markButton = new JRadioButton( "Mark", false );
 
    objectListener = new ObjectSpinnerListener( env );
    objectColourListener = new ObjectColourListener( env );

    objectX.addChangeListener( objectListener );
    objectY.addChangeListener( objectListener );
    objectDir.addChangeListener( objectListener );
    objectHeight.addChangeListener( objectListener );
    objectWidth.addChangeListener( objectListener );
    handleX.addChangeListener( objectListener );
    handleY.addChangeListener( objectListener );
    objColour.addChangeListener( objectColourListener );
    objColour.setPaintLabels( true );
    objColour.setPaintTicks( true );

    objColourSpinner.addChangeListener( new ChangeListener() {
      public void stateChanged( ChangeEvent e ) {
        objColour.setValue( (int) objColourSpinner.getValue() ); 
      }
    } );
    
    objColour.addChangeListener( new ChangeListener() {
      public void stateChanged( ChangeEvent e ) {
        objColourSpinner.setValue( (int) objColour.getValue() ); 
      }
    } );

    obstacleButton.addActionListener( objectColourListener );
    markButton.addActionListener( objectColourListener );

    obstacleButton.setActionCommand( ObjectColourListener.OBSTACLE );
    markButton.setActionCommand( ObjectColourListener.MARK );

    JToolBar subpanel = addRobotPanel();
    robotPane.add( subpanel );

    subpanel = addPositionPanel();
    blockPane.add( subpanel );
    pathPane.add( subpanel );
    polygonPane.add( subpanel );
    groupPane.add( subpanel );
    robotPane.add( subpanel );

    subpanel = addDimensionPanel();
    blockPane.add( subpanel );

    subpanel = addHandlePanel();
    pathPane.add( subpanel );
    polygonPane.add( subpanel );

    subpanel = addObstacleMarkPanel();
    polygonPane.add( subpanel );

    subpanel = addColourPanel();
    blockPane.add( subpanel );
    pathPane.add( subpanel );
    polygonPane.add( subpanel );

    subpanel.setVisible( true );
    minSize = getPreferredSize();
    subpanel.setVisible( false );

    setObjectPanel( null );
  }

  private class ChangeModelListener implements ItemListener {
    public void itemStateChanged( ItemEvent e ) {
      if( e.getStateChange() == e.SELECTED ) {
        RobotArtifact r = ( RobotArtifact) e.getItem();
        Artifact a = env.getCurArtifact();
        if( r.typ != a.typ ) {
          env.changeRobotModel( r.typ );
        }
      }
    }
  }

  private class ChangeIDListener implements ActionListener {
    public void actionPerformed( ActionEvent e ) {
       Object [] o = new Object[2];
       JComboBox<Short> list = new JComboBox<Short>( env.getFreeIDs() );
       list.setEditable( false );
  
       o[0] = new JLabel( "Select new ID:" );
       o[1] = list;

       int rc = JOptionPane.showOptionDialog( null, o, "Choose ID",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.PLAIN_MESSAGE,
                                               null, null, null );

      if( rc == JOptionPane.OK_OPTION ) {
        Short s = (Short)list.getSelectedItem();
        RobotArtifact t = (RobotArtifact)env.getCurArtifact();
        t.changeId( s.shortValue() );
      }
    }
  }


  private JToolBar addRobotPanel() {
    JToolBar subpanel = new JToolBar(); 
    subpanel.setVisible( false );
    add( subpanel );
    subpanel.add( new JLabel( "Robot ID:" ) );
    subpanel.add( robotID );
    changeId = new JButton( "Change ID" );
    changeId.addActionListener( new ChangeIDListener() );
    subpanel.add( changeId );
    robotModel = new RobotField( RobotArtifact.factory.getRobotsModel() );
    robotModel.addItemListener( new ChangeModelListener() );
    subpanel.add( robotModel );
    return subpanel;
  }

  private JToolBar addPositionPanel() {
    JToolBar subpanel = new JToolBar(); 
    subpanel.setVisible( false );
    add( subpanel );
    subpanel.add( new JLabel( "Position: " ) );
    subpanel.add( new JLabel( "X" ) );
    subpanel.add( objectX );
    subpanel.add( new JLabel( " Y" ) );
    subpanel.add( objectY );
    subpanel.add( new JLabel( " Dir" ) );
    subpanel.add( objectDir );
    return subpanel;
  }

  private JToolBar addDimensionPanel() {
    JToolBar subpanel = new JToolBar(); 
    subpanel.setVisible( false );
    add( subpanel );
    subpanel.add( new JLabel( "Dimensions: " ) );
    subpanel.add( new JLabel( "L" ) );
    subpanel.add( objectWidth );
    subpanel.add( new JLabel( " W" ) );
    subpanel.add( objectHeight );
    return subpanel;
  }

  private JToolBar addHandlePanel() {
    JToolBar subpanel = new JToolBar(); 
    subpanel.setVisible( false );
    add( subpanel );
    subpanel.add( new JLabel( "Handle: " ) );
    subpanel.add( new JLabel( "X" ) );
    subpanel.add( handleX );
    subpanel.add( new JLabel( " Y" ) );
    subpanel.add( handleY );
    subpanel.setVisible( false );
    return subpanel;
  }

  private JToolBar addObstacleMarkPanel() {
    JToolBar subpanel = new JToolBar(); 
    subpanel.setVisible( false );
    add( subpanel );
    ButtonGroup bg = new ButtonGroup();
    bg.add( obstacleButton );
    bg.add( markButton );
    subpanel.add( obstacleButton );
    subpanel.add( markButton );
    return subpanel;
  }

  class GrayScaleSlider extends JSlider {
    public GrayScaleSlider( int start, int end, int inc ) {
      super( start, end, inc );
      setOpaque( false );
    }

    public void paintComponent( Graphics g ){
      Graphics2D g2 = (Graphics2D) g;
      int h = this.getHeight();
      int w = this.getWidth();
      float x = (float) this.getX();
      float y = (float) this.getY();

      g2.setPaint( new GradientPaint( x, y, Color.DARK_GRAY, x + w * 1 / 2, y, 
                                      Color.WHITE, false ) );
      g2.fillRoundRect( 10, h / 4, w - 20, h / 4, 12, 12 );
      super.paintComponent( g );
    }
  }

  private JToolBar addColourPanel() {
    JToolBar subpanel = new JToolBar(); 
    subpanel.setVisible( false );
    add( subpanel );
    subpanel.add( new JLabel( "Colour:" ) );
    subpanel.add( objColour );
    subpanel.add( objColourSpinner );
    return subpanel;
  }

  public void setObjectProperties( Artifact a ) {
    Point p = a.getLocation();
    objectListener.setEnable( false );
    objectX.setValue( p.x );
    objectY.setValue( p.y );
    objectDir.setValue( a.getDir() );

    if( a instanceof RobotArtifact ) {
      RobotArtifact t = (RobotArtifact)a;
      robotID.setText( "" + t.getId() ); 
      robotModel.setType( t.typ );
    } else {
      Dimension dim = a.getSize();
      if( dim != null ) {
        objectHeight.setValue( dim.height );
        objectWidth.setValue( dim.width );
      }

      p = a.getHandleLocation();
      boolean selected = p != null;
      if( selected ) {
        handleX.setValue( p.x );
        handleY.setValue( p.y );
      }
      handleX.setEnabled( selected );
      handleY.setEnabled( selected );

      int c = a.getColour();
      objectColourListener.setEnable( false );
      objColour.setValue( c );
      objectColourListener.setEnable( true );
    }
    objectListener.setEnable( true );
  }

  public void selectObjectPanel( Artifact o ) {
    if( o instanceof PolygonArtifact ) {
      setObjectPanel( polygonPane );
      objectListener.setEnable( false );
      objectX.setValue( o.pos.x );
      objectY.setValue( o.pos.y );
      objectDir.setValue( (int) Math.round( Math.toDegrees( o.dir ) ) );
      objectListener.setEnable( true );

      objectColourListener.setEnable( false );
      objColour.setValue( o.clr );
      objectColourListener.setEnable( true );
      objColour.setEnabled( true );
        
      obstacleButton.setSelected( o.typ == CommonData.OBJ_TYP_POLYGON_BLOCK );
      markButton.setSelected( o.typ == CommonData.OBJ_TYP_POLYGON_MARK );
    } else if( o instanceof RobotArtifact ) {
      setObjectPanel( robotPane );
      objectListener.setEnable( false );
      RobotArtifact t = (RobotArtifact)o;
      robotID.setText( "" + t.getId() ); 
      objectX.setValue( o.pos.x );
      objectY.setValue( o.pos.y );
      objectDir.setValue( (int) Math.round( Math.toDegrees( o.dir ) ) );
      objectListener.setEnable( true );
    } else if( o instanceof BlockArtifact ) {
      setObjectPanel( blockPane );
      objectListener.setEnable( false );
      objectX.setValue( o.pos.x );
      objectY.setValue( o.pos.y );
      objectDir.setValue( (int) Math.round( Math.toDegrees( o.dir ) ) );
      objectListener.setEnable( true );
    } else if( o instanceof PathArtifact ) {
      setObjectPanel( pathPane );
      objectColourListener.setEnable( false );
      objColour.setValue( o.clr );
      objectColourListener.setEnable( true );
      objColour.setEnabled( true );
    } else if( o instanceof GroupArtifact ) {
      setObjectPanel( groupPane );
    } else {
      setObjectPanel( null );
    }
  }
  
  private void setPaneVisible( Vector<JComponent> p, boolean b ) {
    for( JComponent c : p ) {
      c.setVisible( b );
    }
  }

  private void setObjectPanel( Vector<JComponent> p ) {
    if( curPane != null ) {
      setPaneVisible( curPane, false );
    }
    curPane = p;
    if( curPane != null ) {
      setPaneVisible( curPane, true );
    }

    revalidate();     
    repaint();     
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension dim = super.getPreferredSize();
    if( ( minSize != null ) && ( dim.height < minSize.height ) ) {
      dim.height = minSize.height;
    } 
    return dim;
  }
}
