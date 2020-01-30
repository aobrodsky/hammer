import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.geom.AffineTransform;

public class Arena extends JComponent implements Task, ChangeListener, 
                                                       Printable {
  static private final Color tableTop = new Color( 255, 220, 150 );

  private Env env;
  private Task.Type curTask = Task.Type.NORMAL;
  private double userScale = 1.0;
  private PercentSpinner zoomSpinner;
  private final AffineTransform viewTransform = new AffineTransform();

  public Arena( Env e ) {
    env = e;

    ArenaMouseListener aml = new ArenaMouseListener( env, this );
    addMouseListener( aml );
    addMouseMotionListener( aml );
    addKeyHandlers();
  }

  public JScrollPane getScrollPane() {
    JScrollPane sp = new JScrollPane( this );
    sp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
    sp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
    sp.setBorder( LineBorder.createBlackLineBorder() );
    return sp;
  }

  public Dimension getPreferredSize() {
    Dimension arena = env.getArenaSize();
    if( arena.width > 0 ) {
      double scaleX = viewTransform.getScaleX();
      double scaleY = viewTransform.getScaleY();
      if( ( scaleX != 0 ) && ( scaleY != 0 ) ) {
        arena.width = (int)( scaleX * arena.width );
        arena.height = (int)( Math.abs( scaleY ) * arena.height );
      }
      return arena;
    } else {
      return new Dimension( 2000, 1000 );
    }
  }

  public void paint( Graphics g ) {
    super.paint( g );

    if( userScale != 1.0 ) {
      ((Graphics2D)g).scale( userScale, userScale );
    }
      
    paintArena( g );
  }

  public int print( Graphics g, PageFormat pf, int pg ) throws PrinterException{
    if( pg > 0 ) { /* We have only one page, and 'page' is zero-based */
      return NO_SUCH_PAGE;
    }

    Graphics2D g2d = (Graphics2D) g;
    // pf.setPaper( new Paper() );
    pf.setOrientation( pf.LANDSCAPE );
    g2d.translate( pf.getImageableX(), pf.getImageableY() );
    Dimension arena = env.getArenaSize();
    double xs = pf.getImageableHeight() / arena.height;
    double ys = pf.getImageableWidth() / arena.width;
    double scale = xs < ys ? xs : ys;
    g2d.scale( scale, scale );

    paintArena( g );

    return PAGE_EXISTS;
  }

  public Point translateToGraphicsCoords( Point p ) {
    Point q = new Point();
    try {
      viewTransform.transform( p, q );
    } catch ( Exception e ) {
      System.out.println( "Ooops, should not happen" );
    }
    return q;
  }

  public Point translateToEnvCoords( Point p ) {
    Point q = new Point();
    try {
      viewTransform.inverseTransform( p, q );
    } catch ( Exception e ) {
      System.out.println( "Ooops, should not happen" );
    }
    return q;
  }

  private void paintObjects( Graphics2D g, boolean obst ) {
    for( Artifact a : env.getModel().getArtifacts() ) {
      if( obst == a.isObstacle() ) {
        a.paintArtifact( g, true );
      }
    }
  }

  public void paintArena( Graphics g1 ) {
    Graphics2D g = (Graphics2D)g1;
    Dimension arena = env.getArenaSize();

    g.translate( 0, arena.height );  // Move the origin to the lower left
    g.scale( 1, -1 );               // Flip the sign of the coordinate system

    AffineTransform gaf = g.getTransform();
    viewTransform.setToIdentity();
    viewTransform.scale( gaf.getScaleX(), gaf.getScaleY() );
    viewTransform.translate( 0, -arena.height );

    g.setColor( tableTop );
    g.fillRect( 0, 0, arena.width, arena.height );

    paintObjects( g, false );
    for( RobotArtifact t : env.getModel().getRobots() ) {
      t.paintArtifact( g, true );
    }
    paintObjects( g, true );

    Artifact cur = env.getCurArtifact();
    if( cur != null ) {
      cur.paintHandles( g );
      cur.paintArtifact( g, false );
    }
  }

  public void setTask( Task.Type t ) {
    curTask = t;
    switch( t ) {
    case START_BLOCK:
    case START_PATH:
    case START_POLY:
    case START_ROBOT:
    case NEXT_POINT:
    case DRAG_ARTIFACT:
    case PLACE_LIBOBJ:
      setCursor( new Cursor( Cursor.CROSSHAIR_CURSOR ) );
      break;
    case NORMAL:
    default:
      setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }
  }

  public Type getTask() {
    return curTask;
  }

  public void stateChanged( ChangeEvent e ) {
    zoomSpinner = (PercentSpinner) e.getSource();
    userScale = (double) zoomSpinner.getValue();
    revalidate();
    repaint();
  }

  public void setZoomSpinner( PercentSpinner zs ) {
    zoomSpinner = zs;
  }

  public void changeZoom( double z ) {
    userScale += z;
    if( ( userScale <= 0 ) || ( userScale > 10.0 ) ) {
      userScale -= z;
    } else if( zoomSpinner != null ) {
      zoomSpinner.setValue( userScale );
    }
  }

  private class EscapeHandler extends AbstractAction {
    public static final String ESCAPE = "Escape";

    public void actionPerformed( ActionEvent e ) {
      if( getTask() != Task.Type.NORMAL ) {
        setTask( Task.Type.NORMAL );
        env.selectArtifact( null, false );
      }
    }
  }

  private class EnterHandler extends AbstractAction {
    public static final String ENTER = "Enter";
    public Arena arena;
  
    public EnterHandler( Arena a ) {
      arena = a;
    }

    public void actionPerformed( ActionEvent e ) {
      Point p = getMousePosition();
      mouseAction( MouseEvent.MOUSE_PRESSED, 1, p.x, p.y );
      mouseAction( MouseEvent.MOUSE_CLICKED, 1, p.x, p.y );
      mouseAction( MouseEvent.MOUSE_PRESSED, 1, p.x, p.y );
      mouseAction( MouseEvent.MOUSE_CLICKED, 2, p.x, p.y );
    }

    private void mouseAction( final int e, final int c, final int x, 
                                                        final int y ) {
      SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          dispatchEvent( new MouseEvent( arena, e, System.currentTimeMillis(),
                                         InputEvent.BUTTON1_MASK, x, y, c,
                                         false ) );
        }
      } );
    }
  }

  private class ArrowHandler extends AbstractAction {
    public static final String LEFT = "Left";
    public static final String RIGHT = "Right";
    public static final String UP = "Up";
    public static final String DOWN = "Down";
    private String command;
 
    public ArrowHandler( String cmd ) {
      command = cmd;
    }

    public void actionPerformed( ActionEvent e ) {
      Artifact a = env.getCurArtifact();
      if( a == null ) {
        return; 
      }

      Point p = a.getLocation();
      a.startModification( p );
      switch( command ) {
      case LEFT:
        p.x--;
        break;
      case RIGHT:
        p.x++;
        break;
      case UP:
        p.y++;
        break;
      case DOWN:
        p.y--;
        break;
      }
      a.setLocation( p );
      a.endModification();
    }
  }

  private void addKeyHandlers() {
    KeyStroke k = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 );
    EscapeHandler eh = new EscapeHandler();

    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    InputMap im = getInputMap( JComponent.WHEN_FOCUSED );
    ActionMap am = getActionMap();

    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ), ArrowHandler.LEFT );
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ), ArrowHandler.RIGHT);
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ), ArrowHandler.UP );
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ), ArrowHandler.DOWN );
    am.put( ArrowHandler.LEFT, new ArrowHandler( ArrowHandler.LEFT ) );
    am.put( ArrowHandler.RIGHT, new ArrowHandler( ArrowHandler.RIGHT ) );
    am.put( ArrowHandler.UP, new ArrowHandler( ArrowHandler.UP ) );
    am.put( ArrowHandler.DOWN, new ArrowHandler( ArrowHandler.DOWN ) );

    im = getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
    im.put( k, EscapeHandler.ESCAPE);
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), EnterHandler.ENTER);

    am.put( EscapeHandler.ESCAPE, eh );
    am.put( EnterHandler.ENTER, new EnterHandler( this ) );
  }
}
