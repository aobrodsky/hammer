import java.awt.*;
import java.awt.geom.*;

public class PolygonArtifact extends PhysicalArtifact {
  static private final Color polygonColor = darkGreen;
  static private final Point [] placeHolder = { new Point(), new Point() };
  private int curHandleIdx = 0;
  private int curHandlePoly = 0;

  static private final Point2D gradStart = new Point2D.Float(0, 0);
  static private final Point2D gradEnd = new Point2D.Float(5, 5);
  static private final float [] gradFract = {0.1f, 1.0f};
  static private final Color [] gradColors = { polygonColor, polygonColor };


  public PolygonArtifact( PolygonArtifact p ) {
    super( p );
  }

  public PolygonArtifact( CommonData.EnvObject o, ArtifactListener al ) {
    super( o, al );
  }

  public PolygonArtifact( Point p, ArtifactListener al ) {
    super( zero, zero, 0, placeHolder, CommonData.OBJ_TYP_POLYGON_BLOCK, 
           defObstacleColor, al );

    Point [] v = { new Point( p ), new Point( p ) };
    polys[0].verts = v;

    computeBoundingBox();
  }

  public synchronized boolean moveHandle( Point p ) {
    if( ( curHandle != null ) && ( prevMove != null ) && !p.equals(prevMove) ) {
      if( curHandle == rot_handle[0] ) {
        dir = Math.atan2( p.x - pos.x, p.y - pos.y  );
      } else {
        Point pt = new Point( p.x - prevMove.x, p.y - prevMove.y );
        transform( pt, -dir, null, null, pt );
        curHandle.translate( pt.x, pt.y );
        if( !checkValidPolygon( curHandle ) ) {
          curHandle.translate( -pt.x, -pt.y );
          return false;
        }
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

  public synchronized void paintArtifact( Graphics2D g, boolean fill ) {
    boolean obs = isObstacle();

    if( !curPointValid ) {
      g.setColor( Color.RED );
    } else if( obs ) {
      g.setColor( polygonColor );
    } else {
      int c = clr / 4;  // translate thymio 1024 colour to 256 grey scale
      g.setColor( new Color( c, c, c ) );
    }
    super.paintArtifact( g, fill );

    if( fill && obs && ( clr != defObstacleColor ) ) {
      Stroke os = g.getStroke();
      g.setStroke( new BasicStroke( 4 ) );
      int c = clr / 4;  // translate thymio 1024 colour to 256 grey scale
      g.setColor( new Color( c, c, c  ) );
      super.paintArtifact( g, false );
      g.setStroke( os );
    }
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

    // Ensure vertices are in CW order
    int n = 0;
    for( int i = 0; i < p.num; i++ ) {
      n += ( p.verts[(i + 1) % p.num].x - p.verts[i].x ) *
           ( p.verts[(i + 1) % p.num].y - p.verts[i].y );
    }

    // Reverse if not
    if( n < 0 ) {
      n = p.num / 2;
      for( int i = 0; i < n; i++ ) {
        Point q = p.verts[i];
        p.verts[i] = p.verts[p.num - 1 - i];
        p.verts[p.num - 1 - i] = q;
      }
    }

    updateCache( true );
  }

  public void movePoint( Point p ) {
    CommonData.EnvPolygon poly = polys[0];
    if( !p.equals( poly.verts[poly.num - 2] ) ) {
      poly.verts[poly.num - 1].setLocation( p );
      
      curPointValid = checkValidPolygon( 0, poly.num - 1 );
      updateCache( true );

      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
    }
  }

  private boolean checkValidPolygon( Point hdl ) {
    if( ( curHandleIdx < polys[curHandlePoly].num ) &&
        ( curHandle == polys[curHandlePoly].verts[curHandleIdx] ) ) {
      return checkValidPolygon( curHandlePoly, curHandleIdx );
    }

    for( int i = 0; i < num; i++ ) {
      for( int j = 0; j < polys[i].num; j++ ) {
        if( curHandle == polys[i].verts[j] ) {
          curHandlePoly = i;
          curHandleIdx = j;
          return checkValidPolygon( i, j );
        }
      }
    }
    return true;
  }

  private boolean checkValidPolygon( int pidx, int vidx ) {
    int n = polys[pidx].num;
    if( n < 4 ) {
      return true;
    }
 
    Point t = polys[pidx].verts[(vidx - 1 + n ) % n]; 
    Point u = polys[pidx].verts[vidx];
    Point v = polys[pidx].verts[( vidx + 1 ) % n];

    for( int i = 0; i < n; i++ ) {
      Point a = polys[pidx].verts[i];
      Point b = polys[pidx].verts[( i + 1 ) % n];

      if( ( a == u ) || ( b == u ) ) {
        continue;
      } else if( ( b != t ) && segsCross( a, b, t, u ) ) {
        return false;
      } else if( ( a != v ) && segsCross( a, b, u, v ) ) {
        return false;
      }
    }

    return true;
  }

  public Artifact deepCopy() {
    return new PolygonArtifact( this );
  }

  public int getMinPoints() {
    return 3;
  }

  public void setObstacle( boolean obs ) {
    if( obs ) {
      typ = CommonData.OBJ_TYP_POLYGON_BLOCK;
    } else {
      typ = CommonData.OBJ_TYP_POLYGON_MARK;
    }
  }

  public boolean isObstacle() {
    return typ == CommonData.OBJ_TYP_POLYGON_BLOCK;
  }
}
