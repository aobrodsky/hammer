import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;


public abstract class Artifact extends CommonData.EnvObject {
  static protected short idCounter = 100;

  static protected final int defObstacleColor = 256; // Approx grey scale / 1024
  static protected final Color darkGreen = new Color( 0, 134, 50 ); // Forest
  static protected final Color pitchBlack = new Color( 0, 0, 0 );
  static private final Point [] filler = { new Point( 0, -1 ), 
                                           new Point( -1, -1 ), 
                                           new Point( -1, 0 ), 
                                           new Point( 0, 0 ) };

  public final Point []  bb = { new Point(), new Point() };       
  public final Point []  rot_handle = { new Point(), new Point() };
  public final Rectangle rect_bb = new Rectangle();
  public Polygon         bb_cache;
  public Vector<Polygon> poly_cache; 
  public boolean         curPointValid = true;
  public short           groupId;

  protected static final Point zero = new Point( 0, 0 );
  protected ArtifactListener listener = null;
  protected Point curHandle = null;
  protected Point prevMove = null;
  protected ArtifactCheckPoint checkPoint = null;
  protected GroupArtifact parent;
  protected final Point groupPos = new Point( 0, 0 );

  protected static final int MIN_SIZE = 5;
  protected static final int HDL_RAD = 5;
  protected static final int HDL_SIZE = HDL_RAD * 2;

  public Artifact( ArtifactListener al ) {
    listener = al;
  }

  public Artifact( Artifact a ) {
    super( a );
    listener = a.listener;
    computeBoundingBox();
  }

  public Artifact( CommonData.EnvObject o, ArtifactListener al ) {
    listener = al;

    pos = o.pos;
    org = o.org;
    dir = o.dir;
    typ = o.typ;
    clr = o.clr;
    num = o.num;
    polys = o.polys;
 
    if( clr <= 0 ) {
      clr = defObstacleColor;
    }
   
    computeBoundingBox();
  }

  public Artifact( Point posn, Point orig, double dirn, Point [] v, 
                         short type, int col, ArtifactListener al ) {
    listener = al;
    pos.setLocation( posn );
    org.setLocation( orig );
    dir = dirn;
    typ = type;
    clr = (short)col;
    num = 1;
    polys = new CommonData.EnvPolygon[1];
    polys[0] = new CommonData.EnvPolygon();
    polys[0].num = v.length;
    polys[0].verts = v;

    computeBoundingBox();
  }

  public synchronized void addPolygon( Point [] v ) {
    if( num <= polys.length ) {
      polys = Arrays.copyOf( polys, num * 2 );
    }

    polys[num] = new CommonData.EnvPolygon();
    polys[num].num = v.length;
    polys[num].verts = v;
    num++;
  }

  public synchronized void computeBoundingBox() {
    bb[0].setLocation( polys[0].verts[0] );
    bb[1].setLocation( polys[0].verts[0] );
    for( int i = 0; i < num; i++ ) {
      CommonData.EnvPolygon p = polys[i];
      for( int j = 0; j < p.num; j++ ) {
        if( bb[0].x > p.verts[j].x ) {
          bb[0].x = p.verts[j].x;
        }
        if( bb[0].y > p.verts[j].y ) {
          bb[0].y = p.verts[j].y;
        }
        if( bb[1].x < p.verts[j].x ) {
          bb[1].x = p.verts[j].x;
        }
        if( bb[1].y < p.verts[j].y ) {
          bb[1].y = p.verts[j].y;
        }
      }
    }
  }

  public synchronized int maxPolygonSize() {
    int n = 0;
    for( int i = 0; i < num; i++ ) {
      if( n < polys[i].num ) {
        n = polys[i].num;
      }
    }
    return n;
  }
  
  public synchronized void paintHandles( Graphics2D g ) {
    updateCache( false );

    g.setColor( Color.RED );
    for( Polygon pc : poly_cache ) {
      for( int j = 0; j < pc.npoints; j++ ) {
        g.fillOval( pc.xpoints[j] - HDL_RAD, pc.ypoints[j] - HDL_RAD, HDL_SIZE,
                    HDL_SIZE );
      }
    }

    if( bb[0].distance( bb[1] ) > 0 ) {
      g.fillOval( rot_handle[0].x - HDL_RAD, rot_handle[0].y - HDL_RAD, 
                  HDL_SIZE, HDL_SIZE );
      g.setColor( Color.BLACK );
      g.drawPolygon( bb_cache.xpoints, bb_cache.ypoints, bb_cache.npoints );
      g.drawLine( rot_handle[0].x, rot_handle[0].y, rot_handle[1].x, 
                  rot_handle[1].y );
    }

    if( ( curHandle != null ) && ( curHandle != rot_handle[0] ) ) {
      Point pt = new Point();
      g.setColor( Color.BLUE );
      transform( curHandle, dir, org, pos, pt );
      g.fillOval( pt.x - HDL_RAD, pt.y - HDL_RAD, HDL_SIZE, HDL_SIZE );
    }
  }

  public synchronized void updateCache( boolean force ) {
    if( force || ( poly_cache == null ) ) {
      poly_cache = new Vector<Polygon>();
      Point src = new Point();
      Point dst = new Point();
      Point tr = null;

      for( int i = 0; i < num; i++ ) {
        CommonData.EnvPolygon p = polys[i];
  
        Polygon q = new Polygon();
        poly_cache.add( q );

        for( int j = 0; j < p.num; j++ ) {
          transform( p.verts[j], dir, org, pos, dst );
          q.addPoint( dst.x, dst.y );
          if( tr == null ) {
            rect_bb.setLocation( dst );
            tr = new Point( dst );
          } else {
            if( dst.x < rect_bb.x ) {
              rect_bb.x = dst.x;
            } else if( dst.x > tr.x ) {
              tr.x = dst.x;
            }
 
            if( dst.y < rect_bb.y ) {
              rect_bb.y = dst.y;
            } else if( dst.y > tr.y ) {
              tr.y = dst.y;
            }
          }
        }
      }

      rect_bb.setSize(  tr.x - rect_bb.x, tr.y - rect_bb.y );
      updateBBCache();
    }
  }

  protected synchronized void updateBBCache() {
    Point src = new Point();
    Point dst = new Point();

    bb_cache = new Polygon();
    src.setLocation( bb[0] );
    transform( src, dir, org, pos, dst );
    bb_cache.addPoint( dst.x, dst.y );
    src.y = bb[1].y;
    transform( src, dir, org, pos, dst );
    bb_cache.addPoint( dst.x, dst.y );
    src.x = bb[1].x;
    transform( src, dir, org, pos, dst );
    bb_cache.addPoint( dst.x, dst.y );
    src.y = bb[0].y;
    transform( src, dir, org, pos, dst );
    bb_cache.addPoint( dst.x, dst.y );

    src.x = ( bb[0].x + bb[1].x ) / 2;
    src.y = bb[1].y + 20;
    transform( src, dir, org, pos, rot_handle[0] );
    src.y = bb[1].y;
    transform( src, dir, org, pos, rot_handle[1] );
  }

  protected synchronized void paintArtifact( Graphics2D g, boolean fill ) {
    updateCache( false );

    for( Polygon pc : poly_cache ) {
      if( fill ) {
        g.fillPolygon( pc.xpoints, pc.ypoints, pc.npoints );
      } else {
        g.drawPolygon( pc.xpoints, pc.ypoints, pc.npoints );
      }
    } 
  }

  public synchronized static void transform( Point in, double dir, Point org, 
                                             Point pos, Point out ) {
    out.setLocation( in );
    if( org != null ) {
      out.translate( -org.x, -org.y );
    }

    if( dir != 0 ) {
      double c = Math.cos( dir );
      double s = Math.sin( dir );
      out.setLocation( out.x * c + out.y * s, -out.x * s + out.y * c );
    }

    if( org != null ) {
      out.translate( org.x, org.y );
    }

    if( pos != null ) {
      out.translate( pos.x, pos.y );
    }
  }   

  private int normalizeDir( int deg ) {
    if( deg < 0 ) {
      deg += ( ( deg  / -360 ) + 1 ) * 360;
    } 
    return deg % 360;
  }

  public synchronized int getDir() {
    return normalizeDir( (int) Math.round( Math.toDegrees( dir ) ) );
  }

  public synchronized int getColour() {
    return clr; 
  }

  public synchronized boolean setColour( int c ) {
    if( clr != c )  {
      clr = (short) c;
      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
      return true;
    } 

    return false;
  }

  public synchronized boolean setLocation( Point p ) {
    if( !p.equals( pos ) ) {
      pos.setLocation( p );
      updateCache( true );

      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
      return true;
    } 
    return false;
  }

  public synchronized Point getLocation() {
    return new Point( pos );
  }

  public synchronized Dimension getSize() {
    return new Dimension( bb[1].x - bb[0].x, bb[1].y - bb[0].y );
  }

  public synchronized void movePoint( Point p ) {
    CommonData.EnvPolygon poly = polys[0];
    if( !p.equals( poly.verts[poly.num - 2] ) ) {
      poly.verts[poly.num - 1].setLocation( p );
      updateCache( true );
  
      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
    }
  }

  public synchronized void addPoint() {
    if( curPointValid ) {
      CommonData.EnvPolygon p = polys[0];
      if( p.verts[p.num - 1].distance( p.verts[p.num - 2] ) >= 20 ) {
        if( p.num >= p.verts.length ) {
          p.verts = Arrays.copyOf( p.verts, p.num * 2 );
        }
        curHandle = new Point( p.verts[p.num - 1] );
        p.verts[p.num] = curHandle;
        p.num++;
      }
    }
  }

  protected void relativize() {
    // Override when needed.
  }

  public synchronized Artifact addCopy() {
    Artifact a = this.deepCopy();

    if( listener != null ) {
      listener.artifactCompleted( a );
    }
    return a;
  }

  public synchronized boolean lastPoint() {
    boolean last = curPointValid && ( polys[0].num > getMinPoints() );
    if( last ) {
      polys[0].num--; // hack because double click generates two events.
      polys[0].verts[polys[0].num] = null;

      relativize();

      curHandle = null;
      if( listener != null ) {
        listener.artifactCompleted( this );
      }
    }
    return last;
  }

  public synchronized void startModification( Point p ) {
    prevMove = new Point( p );
    checkPoint = new ArtifactCheckPoint( this );
  }

  public synchronized void endModification() {
    if( checkPoint != null ) {
      if( listener != null ) {
        listener.artifactChanged( this, checkPoint );
      }
    }
    checkPoint = null;
  }

  public synchronized boolean moveArtifact( Point p ) {
    boolean rc;
    if( prevMove == null ) {
      prevMove = new Point( p );
      rc = setLocation( p );
    } else {
      prevMove.setLocation( pos.x + p.x - prevMove.x,
                            pos.y + p.y - prevMove.y );
      rc = setLocation( prevMove );
      prevMove.setLocation( p );
    }
    return rc;
  }

 protected Point recomputeCentroid() {
    Point pt = new Point();
    computeBoundingBox();

    pt.x = ( bb[0].x + bb[1].x ) / 2;
    pt.y = ( bb[0].y + bb[1].y ) / 2;
 
    for( int i = 0; i < num; i++ ) {
      for( int j = 0; j < polys[i].num; j++ ) {
        polys[i].verts[j].translate( -pt.x, -pt.y );
      } 
    }

    for( int i = 0; i < 2; i++ ) {
      bb[i].translate( -pt.x, -pt.y );
    }

    return pt;
  }

  public synchronized boolean setDirDeg( int deg ) {
    return setDir( Math.toRadians( deg ) );
  }

  public synchronized boolean setDir( double d ) {
    if( dir == d )  {
      return false;
    } 

    dir = d;
    updateCache( true );

    if( listener != null ) {
      listener.artifactChanged( this, null );
    }

    return true;
  }

  public synchronized boolean setHandle( Point hdl ) {
    if( hdl == rot_handle[0] ) {
      curHandle = hdl;
    } else {
      for( int i = 0; i < num && ( curHandle == null ); i++ ) {
        for( int j = 0; j < polys[i].num; j++ ) {
          if( hdl == polys[i].verts[j] ) {
            curHandle = hdl;  
            prevMove = new Point( hdl );
            break;
          }
        }
      }
    }
    return curHandle == hdl;
  }

  public synchronized boolean selectHandle( Point p ) {
    curHandle = null;

    if( p != null ) {
      if( p.distance( rot_handle[0] ) < 10 ) {
        curHandle = rot_handle[0];
      } else {
        Point pt = new Point();

        for( int i = 0; i < num && ( curHandle == null ); i++ ) {
          for( int j = 0; j < polys[i].num; j++ ) {
            Point q = polys[i].verts[j];

            transform( q, dir, org, pos, pt );
            if( p.distance( pt ) <= HDL_SIZE ) { 
              curHandle = q;
              break;
            }
          }
        }
        prevMove = new Point( pt );
      }
    }

    if( listener != null ) {
      listener.artifactChanged( this, null );
    }
    return curHandle != null;
  }

  public synchronized Point getHandleLocation() {
    if( ( curHandle != null ) && ( curHandle != rot_handle[0] ) ) {
      Point pt = new Point();
      transform( curHandle, dir, org, pos, pt );
      return pt;
    }
    return null;
  }

  public abstract boolean moveHandle( Point p );

  public synchronized boolean contains( Point p ) {
    for( Polygon q : poly_cache ) {
      if( q.contains( p ) ) {
        return true;
      }
    }
    return false;
  }

  public synchronized boolean intersects( Artifact a ) {
    if( !rect_bb.intersects( a.rect_bb ) ) {
      return false;
    }

    Point s = new Point();
    Point t = new Point();
    Point u = new Point();
    Point v = new Point();

    for( Polygon p : a.poly_cache ) {
      int m = p.npoints;
      for( Polygon q : poly_cache ) {
        int n = q.npoints;
        for( int i = 0; i < m; ) {
          s.setLocation( p.xpoints[i], p.ypoints[i] );
          i++;
          t.setLocation( p.xpoints[i % m], p.ypoints[i % m] );

          for( int j = 0; j < n; ) {
            u.setLocation( q.xpoints[j], q.ypoints[j] );
            j++;
            v.setLocation( q.xpoints[j % n], q.ypoints[j % n] );

            if( segsCross( s, t, u, v ) ) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  protected double det( int a, int b, int c, int d ) {
    return ( a * d ) - ( b * c );
  }

  protected boolean segsCross( Point s, Point t, Point u, Point v ) {
    double d = det( t.x - s.x, t.y - s.y, u.x - v.x, u.y - v.y );
    double a = det( u.x - s.x, u.y - s.y, u.x - v.x, u.y - v.y ) / d;
    double b = det( t.x - s.x, t.y - s.y, u.x - s.x, u.y - s.y ) / d;

    return !( ( d == 0 ) || ( a < 0 ) || ( b < 0 ) || ( a > 1 ) || ( b > 1 ) );
  }

  public abstract Artifact deepCopy();

  public void revert( ArtifactCheckPoint cp ) {
    curHandle = cp.curHandle;
    ArtifactCheckPoint acp = new ArtifactCheckPoint( this );
    cp.revertFromCheckPoint( this );
    if( ( curHandle != null ) && !curHandle.equals( acp.handlePos ) ) {
      curHandle.translate( pos.x - acp.pos.x, pos.y - acp.pos.y );
      recomputeCentroid();
    }

    updateCache( true );
    if( listener != null ) {
      listener.artifactChanged( this, acp );
    }
  }

  public void insert() {
    if( listener != null ) {
      listener.artifactCompleted( this );
    }
  }

  public void delete() {
    remove();
  }

  public void remove() {
    if( listener != null ) {
      listener.artifactRemoved( this );
    }
  }

  public Artifact convertToPolygon() {
    return this;
  }

  public int getMinPoints() {
    return 1;
  }

  public void sanitize() {
    if( polys[0].num < getMinPoints() ) {
      polys[0] = new CommonData.EnvPolygon( filler );
    }
  }

  public abstract boolean isObstacle();

  public boolean inGroup() {
    return parent != null && parent.isGroup();
  }

  protected static short nextId() {
    return idCounter++;
  }
}
