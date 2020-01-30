import java.awt.*;
import java.util.*;

public class PathArtifact extends PhysicalArtifact {
  static private final Color pathColor = pitchBlack;
  static private final int PATH_WIDTH = 9; // mm on each side of PATH
  static private final int PATH_COLOUR = 1;
  static private final Point [] placeHolder = { new Point(), new Point() };

  public PathArtifact( PathArtifact p ) {
    super( p );
  }

  public PathArtifact( CommonData.EnvObject o, ArtifactListener al ) {
    super( o, al );
  }

  public PathArtifact( Point p, ArtifactListener al ) {
    super(zero, zero, 0, placeHolder, CommonData.OBJ_TYP_PATH, PATH_COLOUR, al);

    Point [] v = { new Point( p ), new Point( p.x + 1, p.y + 1 ) };
    polys[0].verts = v;
  }

  protected void relativize() {
    CommonData.EnvPolygon p = polys[0];

    computeBoundingBox();
    pos.x = ( bb[0].x + bb[1].x ) / 2;
    pos.y = ( bb[0].y + bb[1].y ) / 2;
    dir = 0;

    for( int i = 0; i < p.num; i++ ) {
      p.verts[i].x -= pos.x;
      p.verts[i].y -= pos.y;
    }

    for( int i = 0; i < 2; i++ ) {
      bb[i].x -= pos.x;
      bb[i].y -= pos.y;
    }

    updateCache( true );
  }

  public boolean moveHandle( Point p ) {
    if( ( curHandle != null ) && ( prevMove != null ) && !p.equals(prevMove) ) {
      if( curHandle == rot_handle[0] ) {
        dir = Math.atan2( p.x - pos.x, p.y - pos.y  );
      } else {
        Point pt = new Point( p.x - prevMove.x, p.y - prevMove.y );
        transform( pt, -dir, null, null, pt );
        curHandle.translate( pt.x, pt.y );
        pt = recomputeCentroid();
        transform( pt, dir, null, null, pt );
        pos.translate( pt.x, pt.y );
      }

      prevMove.setLocation( p );
      updateCache( true );
      if( ( listener != null ) ) {
        listener.artifactChanged( this, null );
      }
      return true;
    }
    return false;
  }

  public boolean moveHandleOld( Point p ) {
    if( ( curHandle != null ) && ( prevMove != null ) && !p.equals(prevMove) ) {
      curHandle.setLocation( p );
      prevMove.setLocation( p );
      updateCache( true );
      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
      return true;
    } 
    return false;
  }

  public synchronized void paintHandles( Graphics2D g ) {
    updateCache( false );
    Point p = new Point();

    for( int i = 0; i < num; i++ ) {
      for( int j = 0; j < polys[i].num; j++ ) {
        if( curHandle == polys[i].verts[j] ) {
          g.setColor( darkGreen );
        } else {
          g.setColor( Color.RED );
        }
        transform( polys[i].verts[j], dir, org, pos, p );
        g.fillOval( p.x - HDL_RAD, p.y - HDL_RAD, HDL_SIZE, HDL_SIZE );
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
  }

  public synchronized void updateCache( boolean force ) {
    if( force || ( poly_cache == null ) ) {
      poly_cache = new Vector<Polygon>();
      Point src = new Point();
      Point dst = new Point();

      for( int i = 0; i < num; i++ ) {
        CommonData.EnvPolygon p = polys[i];

        for( int j = 0; j < p.num - 1; j++ ) {
          Polygon q = new Polygon();
          poly_cache.add( q );
          Point [] v = p.verts;
          int dx = v[j + 1].x - v[j].x;
          int dy = v[j + 1].y - v[j].y;
          double seg_dir = Math.atan2( dy, dx );
          int dist = (int)Math.sqrt( dx * dx + dy * dy ) + PATH_WIDTH;
          int [] x = { dist, dist, 0, 0 };
          int [] y = { PATH_WIDTH, -PATH_WIDTH, -PATH_WIDTH, PATH_WIDTH };

          for( int k = 0; k < x.length; k++ ) {
            src.setLocation( x[k], y[k] );
            transform( src, -seg_dir, org, v[j], dst );
            transform( dst, dir, org, pos, dst );
            q.addPoint( dst.x, dst.y );
          }
        }
      }

      updateBBCache();
    }
  }

  public synchronized void paintArtifact( Graphics2D g, boolean fill ) {
    int c = clr / 4;  // translate thymio 1024 colour to 256 grey scale
    g.setColor( new Color( c, c, c ) );
    super.paintArtifact( g, fill );
  }

  public Artifact deepCopy() {
    return new PathArtifact( this );
  }

  public Artifact convertToPolygon() {
    CommonData.EnvObject o = new CommonData.EnvObject( this );

    o.typ = CommonData.OBJ_TYP_POLYGON_MARK;
    o.num = 0;
    for( int i = 0; i < num; i++ ) {
      o.num += polys[i].num - 1;
    }
    o.polys = new CommonData.EnvPolygon[o.num];

    int n = 0;
    for( int i = 0; i < num; i++ ) {
      CommonData.EnvPolygon p = polys[i];

      for( int j = 0; j < p.num - 1; j++ ) {
        Point [] v = p.verts;
        int dx = v[j + 1].x - v[j].x;
        int dy = v[j + 1].y - v[j].y;
        double seg_dir = Math.atan2( dy, dx );
        int dist = (int)Math.sqrt( dx * dx + dy * dy ) + PATH_WIDTH;
        int [] x = { dist, dist, 0, 0 };
        int [] y = { PATH_WIDTH, -PATH_WIDTH, -PATH_WIDTH, PATH_WIDTH };

        o.polys[n] = new CommonData.EnvPolygon();
        o.polys[n].num = x.length;
        o.polys[n].verts = new Point[x.length];
  
        for( int k = 0; k < x.length; k++ ) {
          o.polys[n].verts[k] = new Point( x[k], y[k] );
          transform( o.polys[n].verts[k], -seg_dir, org, v[j], 
                     o.polys[n].verts[k] );
        }
        n++;
      }
    }

    remove();

    Artifact pa = new PolygonArtifact( o, listener );
    pa.insert();
    return pa;
  }

  public int getMinPoints() {
    return 2;
  }

  public boolean isObstacle() {
    return false;
  }
}
