import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;

public class ThymioArtifact extends RobotArtifact {
  // Thymio Physical Measurements, relative to center hole (0,0) 
 //  All measurements in mm and degress 
 //  Body 
  static private final Point thymioOrg = new Point( 0, 0 );
  static private final Point backLeft = new Point( -55, -30 );
  static private final Point frontLeft = new Point( -55, 55 );
  static private final Point backRight = new Point( 55, -30 );
  static private final Point frontRight = new Point( 55, 55 );
  static private final double frontArcRadius = 80;
  static private final double frontArcStart = Math.toRadians( -45 );
  static private final double frontArcEnd = Math.toRadians( 45 );

  static private final int degPerSeg = 10;  // rep. arcs by 1 line per 5 degs 
  static private final int numThymioVerts = 4 + ( (45 - -45) / degPerSeg ) - 1;
  static private final Point thymioVerts [] = new Point[numThymioVerts];
  static private final int thymioX [] = new int[numThymioVerts];
  static private final int thymioY [] = new int[numThymioVerts];

  static private final Point left_wheel_pos = new Point( -47, 0 ); 
  static private final Point right_wheel_pos = new Point( 47, 0 );
  static private final Point front_pivot_pos = new Point( 0, 60 );
  static private final int numContactVerts = 3;
  static private final Point thymioContactVerts [] = new Point[numContactVerts];

  // LED Locations
  static private final int [] horizProxDeg = { -40, -20, 0,  20, 40 };
  static private final int horizProxRadius = 80;
  static private final Point [] horizProxRear = { new Point( 30, -30 ),
                                                  new Point( -30, -30 ) };
  static private final Point [] horizProxLEDs = new Point[7];
  static private final int [] botProxDeg = { -7, 7 };
  static private final int botProxRadius = 77;
  static private final Point [] botProxLEDs = { new Point(), new Point() };

  static private final Point circleCenter = new Point( 0, 45 );
  static private final int circleRadius = 25;
  static private final Point [] circleElem = { new Point( -7, -2 ),
                                               new Point( -7, 2 ),  
                                               new Point( 7, 2 ), 
                                               new Point( 7, -2 ) };
  static private final Point [][] circleElems = new Point[8][4];
  static private final Point [] buttonElem = { new Point( -5, -2 ),
                                               new Point( -5, 2 ),  
                                               new Point( 5, 2 ), 
                                               new Point( 5, -2 ) };
  static private final int buttonRadius = 10;
  static private final Point [][] buttonElems = new Point[4][4];
  static private final Point [] bottomLED = { new Point( -5, -20 ),
                                               new Point( -5, 20 ),  
                                               new Point( 5, 20 ), 
                                               new Point( 5, -20 ) };
  static private final Point [][] bottomLEDElems = new Point[2][4];
  static private final Point [] botLEDPos = { new Point( -50, 30 ),
                                              new Point( 50, 30 ) };
  static private final Point tempLED = new Point( -55, 10 );
  static private final Point soundLED = new Point( 55, 10 );
  static private final Point rcLED = new Point( 55, 50 );

  private static Image sadFace;
  private final static int faceWidth = 50;
  private final static int faceHeight = 50;
  static private final Point [] placeHolder = { new Point(), new Point() };
  static private AffineTransform fontTransform = 
                                      AffineTransform.getScaleInstance( 1, -1 );
  private static boolean showDetails = true;

  private CommonData.ThymioState thymioState = new CommonData.ThymioState();
  private CommonData.ThymioState prevState = new CommonData.ThymioState();

  static {
    int i = 0;

    thymioVerts[i++] = frontRight;
    thymioVerts[i++] = backRight;
    thymioVerts[i++] = backLeft;
    thymioVerts[i++] = frontLeft;

    double inc = Math.toRadians( degPerSeg );
    for( double d = frontArcStart + inc; d < frontArcEnd; d += inc ) {
      thymioVerts[i++] = new Point( (int) (frontArcRadius * Math.sin( d ) ), 
                                    (int) (frontArcRadius * Math.cos( d ) ) );
    }
 
    i = 0;
    thymioContactVerts[i++] = left_wheel_pos;
    thymioContactVerts[i++] = right_wheel_pos;
    thymioContactVerts[i++] = front_pivot_pos;

    for( i = 0; i < 5; i++ ) {
      Point p = new Point( 0, horizProxRadius );
      transform( p, Math.toRadians( horizProxDeg[i] ), null, null, p );
      horizProxLEDs[i] = p;
    }
    horizProxLEDs[5] = horizProxRear[0];
    horizProxLEDs[6] = horizProxRear[1];

    for( i = 0; i < 2; i++ ) {
      botProxLEDs[i].setLocation( 0, botProxRadius );
      transform( botProxLEDs[i], Math.toRadians( botProxDeg[i] ), null, null, 
                 botProxLEDs[i] );
    }

    for( i = 0; i < 4; i++ ) {
      circleElem[i].translate( 0, circleRadius );
    }

    for( i = 0; i < 8; i++ ) {
      double d = Math.toRadians( i * ( 360 / 8 ) );
      for( int j = 0; j < 4; j++ ) {
        circleElems[i][j] = new Point( circleElem[j] );
        transform( circleElems[i][j], d, null, circleCenter, circleElems[i][j]);
      }
    }

    for( i = 0; i < 4; i++ ) {
      buttonElem[i].translate( 0, buttonRadius );
    }

    for( i = 0; i < 4; i++ ) {
      double d = Math.toRadians( i * ( 360 / 4 ) );
      for( int j = 0; j < 4; j++ ) {
        buttonElems[i][j] = new Point( buttonElem[j] );
        transform( buttonElems[i][j], d, null, circleCenter, buttonElems[i][j]);
      }
    }

    for( i = 0; i < 2; i++ ) {
      for( int j = 0; j < 4; j++ ) {
        bottomLEDElems[i][j] = new Point( bottomLED[j] );
        transform( bottomLEDElems[i][j], 0, null, botLEDPos[i], 
                   bottomLEDElems[i][j] );
      }
    }
  }

  public ThymioArtifact( ThymioArtifact t ) {
    super( t );
    offEdge = t.offEdge;
    t.thymioState.copyTo( thymioState );
    updateCache( true );
  }

  public ThymioArtifact( Point posn, CommonData.ThymioState ts, 
                         ArtifactListener al ) {
    super( new Point( posn ), zero, 0, thymioVerts, CommonData.ROBOT_TYP_THYMIO,
           -1, al );
    addPolygon( thymioContactVerts );
    computeBoundingBox();
    thymioState = ts;
    updateCache( true );
  }

  public ThymioArtifact( Point posn, ArtifactListener al ) {
    this( posn, new CommonData.ThymioState(), al );
  }

  public ThymioArtifact() {
    this( new Point( 0, 0 ), null );
  }

  private static int scaleColour( int c ) {
    c *= 8;
    if( c >= 256 ) {
      c = 255;
    }
    return c;
  }

  private void paintBottomLEDs( Graphics2D g ) {
    if( !showDetails ) {
      return;
    }

    Point p = new Point();
    for( int i = 0; i < 7; i++ ) {
      int j = i > 2 ? i + 1 : i; // two LEDs on sensor 2
      int a = scaleColour( thymioState.leds_prox[j] );
      g.setColor( new Color( 255, 0, 0, a ) );

      transform( horizProxLEDs[i], dir, org, pos, p );
      g.fillOval( p.x - 5, p.y - 5, 10, 10 );
    }

    for( int i = 0; i < 2; i++ ) {
      int a = scaleColour( thymioState.leds_prox_grnd[i] );
      g.setColor( new Color( 255, 0, 0, a ) );

      transform( botProxLEDs[i], dir, org, pos, p );
      g.fillOval( p.x - 5, p.y - 5, 10, 10 );
    }

    if( ( thymioState.leds_temp[0] + thymioState.leds_temp[1] ) > 0 ) {
      int r = scaleColour( thymioState.leds_temp[0] );
      int b = scaleColour( thymioState.leds_temp[1] );
      g.setColor( new Color( r, 0, b, ( r + b ) / 2 ) );
      transform( tempLED, dir, org, pos, p );
      g.fillOval( p.x - 5, p.y - 5, 10, 10 );
      
    }

    if( thymioState.leds_sound > 0 ) {
      int a = scaleColour( thymioState.leds_sound );
      g.setColor( new Color( 0, 0, 255, a ) );
      transform( soundLED, dir, org, pos, p );
      g.fillOval( p.x - 5, p.y - 5, 10, 10 );
    }
      
    if( thymioState.leds_rc > 0 ) {
      int a = scaleColour( thymioState.leds_rc );
      g.setColor( new Color( 255, 0, 0, a ) );
      transform( rcLED, dir, org, pos, p );
      g.fillOval( p.x - 5, p.y - 5, 10, 10 );
    }
  }

  private void paintTopLEDs( Graphics2D g ) {
    if( !showDetails ) {
      return;
    }

    int [] x = new int[4];
    int [] y = new int[4];
    Point p = new Point();

    for( int i = 0; i < 8; i++ ) {
      for( int j = 0; j < 4; j++ ) {
        transform( circleElems[i][j], dir, org, pos, p );
        x[j] = p.x;
        y[j] = p.y;
      }

      int a = scaleColour( thymioState.circle_leds[i] );
      g.setColor( new Color( 255, 200, 0, a ) );
      g.fillPolygon( x, y, 4 );
      g.setColor( Color.BLACK );
      g.drawPolygon( x, y, 4 );
    }

    for( int i = 0; i < 4; i++ ) {
      for( int j = 0; j < 4; j++ ) {
        transform( buttonElems[i][j], dir, org, pos, p );
        x[j] = p.x;
        y[j] = p.y;
      }

      int a = scaleColour( thymioState.leds_buttons[i] );
      g.setColor( new Color( 255, 200, 0, a ) );
      g.fillPolygon( x, y, 4 );
      g.setColor( Color.BLACK );
      g.drawPolygon( x, y, 4 );
    }

    short [] [] rgb = { thymioState.leds_bot_left, thymioState.leds_bot_right };
    for( int i = 0; i < 2; i++ ) {
      for( int j = 0; j < 4; j++ ) {
        transform( bottomLEDElems[i][j], dir, org, pos, p );
        x[j] = p.x;
        y[j] = p.y;
      }

      Color c = makeColour( rgb[i] );
      if( c != null ) {
        g.setColor( c );
        g.fillPolygon( x, y, 4 );
      }
    }
  }

  private Color makeColour( short [] rgb ) {
    int a = rgb[0] + rgb[1] + rgb[2];
    if( a > 0 ) {
      a = ( a + 2 ) / 3;
      return new Color( rgb[0], rgb[1], rgb[2], a );
    }
    return null;
  }

  public synchronized void paintArtifact( Graphics2D g, boolean fill ) {
    updateCache( fill );

    if( !fill ) {
      return;
    }

    Point pt = new Point();
    Polygon p = poly_cache.elementAt( 0 );

    paintBottomLEDs( g );
    g.setColor( Color.LIGHT_GRAY );
    g.fillPolygon( p.xpoints, p.ypoints, p.npoints );
    Color c = makeColour( thymioState.leds_top );
    if( c != null ) {
      g.setColor( c );
      g.fillPolygon( p.xpoints, p.ypoints, p.npoints );
    }

    if( offEdge ) {
      if( sadFace == null ) {
        try {
          sadFace = ImageLoader.load( "sadface.png", ThymioArtifact.class );
        } catch ( Exception ex ) { }
      }
      g.drawImage( sadFace, pos.x - ( faceWidth / 2 ),
                   pos.y - ( faceHeight / 2 ), faceWidth, faceWidth, null );
      g.setColor( Color.RED );
    } else {
      g.setColor( Color.BLACK );
      Font f = g.getFont();
      g.setFont( f.deriveFont( Font.BOLD, fontTransform ) );
      FontMetrics metrics = g.getFontMetrics();
      String id = "" + getState().id;
      int shift =  metrics.stringWidth( id ) / 2;
      g.drawString( id , pos.x - shift, pos.y );
      g.setFont( f );
    }
    g.drawPolygon( p.xpoints, p.ypoints, p.npoints );
    paintTopLEDs( g );
  }

  public synchronized void paintHandles( Graphics2D g ) {
    updateCache( false );
    g.setColor( Color.RED );
    g.fillOval( rot_handle[0].x - HDL_RAD, rot_handle[0].y - HDL_RAD,
                HDL_SIZE, HDL_SIZE );
    g.setColor( Color.BLACK );
    g.drawPolygon( bb_cache.xpoints, bb_cache.ypoints, bb_cache.npoints );
    g.drawLine( rot_handle[0].x, rot_handle[0].y, rot_handle[1].x,
                rot_handle[1].y );
  }

  public synchronized boolean selectHandle( Point p ) {
    curHandle = null;

    if( p != null ) {
      if( p.distance( rot_handle[0] ) < 10 ) {
        curHandle = rot_handle[0];
      } 
    }

    if( listener != null ) {
      listener.artifactChanged( this, null );
    }
    return curHandle != null;
  }

  public synchronized boolean moveHandle( Point p ) {  
    if( ( curHandle != null ) && ( prevMove != null ) && !p.equals(prevMove) ) {
      dir = Math.atan2( p.x - pos.x, p.y - pos.y  );
      prevMove.setLocation( p );
      updateCache( true );
      if( ( listener != null ) ) {
        listener.artifactChanged( this, null );
      }
      return true;
    }
    return false;
  }

  public synchronized Polygon getContactVerts() {
    return poly_cache.elementAt( 1 );
  }

  public RobotArtifact deepCopy() {
    return new ThymioArtifact( this );
  }

  public synchronized boolean updateFromState() {
    boolean b = false;

    if( syncPosWithState ) {
      remoteUpdate = true;
      b = setLocation( thymioState.position );
      b = setDir( thymioState.direction ) || b;
      if( !b && ( listener != null ) && !thymioState.equals( prevState ) ) {
        listener.artifactChanged( this, null );
        b = true;
      }
      thymioState.copyTo( prevState );
      remoteUpdate = false;
    }
    return b;
  }

  public CommonData.ThymioState getState() {
    return thymioState;
  }

  public Point getBottomLeft() {
    return new Point( backLeft );
  }

  public void setId( short nid ) {
    this.id = nid;
    thymioState.id = nid;
  }

  public CommonData.EnvUserAction getAction() {
    CommonData.EnvThymioInput inp = new CommonData.EnvThymioInput();
    inp.id = getId();
    inp.buttons = thymioState.button_state;
    inp.tap = thymioState.tap;
    inp.noise = thymioState.noise;
    return inp;
  }

  public void setButtonState( short [] bs ) {
    System.arraycopy( bs, 0, thymioState.button_state, 0, 
                      CommonData.NUM_BUTTONS );
  }
  
  public void setTap( boolean b ) {
    thymioState.tap = (short) ( b ? 1 : 0 );
  }

  public JComponent getActionPanel() {
    return new ThymioActionPanel();
  }

  public double [] getNoise() {
    return thymioState.noise;   
  }

  public void setNoise( double [] noise ) {
    System.arraycopy( noise, 0, thymioState.noise, 0, noise.length );
  }

  public String toString() {
    return "Thymio II";
  }

  public static void showDetailsEnabled( boolean b ) {
    showDetails = b;
  }
}
