import java.awt.*;

public class BlockArtifact extends PhysicalArtifact {
  // Duplo blocks (2x2) are 31.8mm wide
  static public final Dimension blockDim = new Dimension( 223, 32 ); 
  static private final Color blockColor = darkGreen;
  static private final Point [] placeHolder = { new Point(), new Point(), 
                                                new Point(), new Point() };

  public BlockArtifact( BlockArtifact b ) {
    super( b );
  }

  public BlockArtifact( CommonData.EnvObject o, ArtifactListener al ) {
    super( o, al );
  }

  public BlockArtifact( Point posn, double dirn, ArtifactListener al ) {
    this( posn, dirn, blockDim, defObstacleColor, al );
  }

  public BlockArtifact( Point posn, double dirn, Dimension dim, int col, 
                        ArtifactListener al ) {
    super( posn, zero, dirn, placeHolder, CommonData.OBJ_TYP_BLOCK, col, al );

    int w = dim.width / 2;
    int h = dim.height / 2;
    Point [] v = { new Point( -w, -h ), new Point( -w, dim.height - h ),
                       new Point( dim.width - w, dim.height - h ),
                       new Point( dim.width - w, -h ) };
    polys[0].verts = v;

    computeBoundingBox();
    updateCache( true );
  }

  public boolean fixBlock() {
    boolean ok = true;

    if( listener != null ) {
      ok = listener.checkLocation( this );
    }

    if( ok ) {
      if( listener != null ) {
        listener.artifactCompleted( this );
      }
      prevMove = null;
    }
    return ok;
  }


  public boolean resizeBlock( Dimension dim ) {
    int w = bb[1].x - bb[0].x;
    int h = bb[1].y - bb[0].y;
    boolean changed = false;

    if( dim.width < MIN_SIZE ) {
      dim.width = MIN_SIZE;
      changed = true;
    } else if( dim.height < MIN_SIZE ) {
      dim.height = MIN_SIZE;
      changed = true;
    }

    if( dim.width != w ) {
      w = -dim.width / 2;
      polys[0].verts[0].x = w;
      polys[0].verts[1].x = w;
      bb[0].x = w;
      w += dim.width;
      polys[0].verts[2].x = w;
      polys[0].verts[3].x = w;
      bb[1].x = w;
      changed = true;
    }

    if( dim.height != h ) {
      h = -dim.height / 2;
      polys[0].verts[0].y = h;
      polys[0].verts[3].y = h;
      bb[0].y = h;
      h += dim.height;
      polys[0].verts[1].y = h;
      polys[0].verts[2].y = h;
      bb[1].y = h;
      changed = true;
    }

    if( changed ) {
      updateCache( true );
      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
    }
    return changed;
  }

  private boolean resizeBlockFromHandle( Point hdl ) {
    Point np = new Point( -pos.x, -pos.y );
    Point p = new Point();
    transform( hdl, 0, null, np, p );
    transform( p, -dir, null, null, p );
    int idx;
    int h, w;

    if( ( Math.abs( p.x ) < MIN_SIZE ) || ( Math.abs( p.y ) < MIN_SIZE ) ) {
      return false;
    } else if( ( p.x < org.x ) && ( p.y < org.y ) ) {
      w = bb[1].x - p.x;
      h = bb[1].y - p.y;
      idx = 2;
    } else if( ( p.x > org.x ) && ( p.y > org.y ) ) {
      w = p.x - bb[0].x;
      h = p.y - bb[0].y;
      idx = 0;
    } else if( ( p.x < org.x ) && ( p.y > org.y ) ) {
      w = bb[1].x - p.x;
      h = p.y - bb[0].y;
      idx = 3;
    } else {
      w = p.x - bb[0].x;
      h = bb[1].y - p.y;
      idx = 1;
    }

    bb[0].x = -w / 2;
    bb[0].y = -h / 2;
    bb[1].x = w - w / 2;
    bb[1].y = h -h / 2;

    polys[0].verts[0].x = bb[0].x;
    polys[0].verts[0].y = bb[0].y;
    polys[0].verts[1].x = bb[0].x;
    polys[0].verts[1].y = bb[1].y;
    polys[0].verts[2].x = bb[1].x;
    polys[0].verts[2].y = bb[1].y;
    polys[0].verts[3].x = bb[1].x;
    polys[0].verts[3].y = bb[0].y;

    Polygon q = poly_cache.elementAt( 0 );
    transform( polys[0].verts[idx], dir, null, pos, p );
    pos.x += q.xpoints[idx] - p.x;
    pos.y += q.ypoints[idx] - p.y;

    return true;
  }

  public boolean moveHandle( Point p ) {
    if( ( curHandle != null ) && ( prevMove != null ) && !p.equals(prevMove) ) {
      boolean update = false;

      if( curHandle == rot_handle[0] ) {
        dir = Math.atan2( p.x - pos.x, p.y - pos.y  );
        update = true;
      } else {
        update = resizeBlockFromHandle( p );
      }

      prevMove.setLocation( p );
      updateCache( update );
      if( ( listener != null ) && update ) {
        listener.artifactChanged( this, null );
      }
      return true;
    }
    return false;
  }

  public synchronized void paintArtifact( Graphics2D g, boolean fill ) {
    if( !fill && ( listener != null ) && !listener.checkLocation( this ) ) {
      g.setColor( Color.RED );
    } else {
      g.setColor( blockColor );
    }
    super.paintArtifact( g, fill );

    if( fill && ( clr != defObstacleColor ) ) {
      Stroke os = g.getStroke();
      g.setStroke( new BasicStroke( 4 ) );
      int c = clr / 4;  // translate thymio 1024 colour to 256 grey scale
      g.setColor( new Color( c, c, c  ) );
      super.paintArtifact( g, false );
      g.setStroke( os );
    }
  }

  public Artifact deepCopy() {
    return new BlockArtifact( this );
  }

  public void revert( ArtifactCheckPoint cp ) {
    ArtifactCheckPoint acp = new ArtifactCheckPoint( this );
    cp.revertFromCheckPoint( this );

    polys[0].verts[0].x = bb[0].x;
    polys[0].verts[0].y = bb[0].y;
    polys[0].verts[1].x = bb[0].x;
    polys[0].verts[1].y = bb[1].y;
    polys[0].verts[2].x = bb[1].x;
    polys[0].verts[2].y = bb[1].y;
    polys[0].verts[3].x = bb[1].x;
    polys[0].verts[3].y = bb[0].y;

    updateCache( true );
    if( listener != null ) {
      listener.artifactChanged( this, acp );
    }
  }

  public Artifact convertToPolygon() {
    this.typ = CommonData.OBJ_TYP_POLYGON_BLOCK;
    Artifact p = new PolygonArtifact( this, listener );
    remove();
    p.insert();
    return p;
  }

  public boolean isObstacle() {
    return true;
  }
}
