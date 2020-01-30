import java.awt.*;
import java.util.*;


public class GroupArtifact extends Artifact {
  protected Vector<Artifact> group = new Vector<Artifact>();
  protected boolean unbound = false;

  public GroupArtifact( ArtifactListener al ) {
    super( al );
    typ = CommonData.OBJ_TYP_GROUP;
  }

  public GroupArtifact( Vector<Artifact> list, ArtifactListener al ) {
    super( al );
    typ = CommonData.OBJ_TYP_GROUP;
    setArtifacts( list );
  }

  public GroupArtifact( GroupArtifact grp ) {
    super( grp.listener );
    typ = CommonData.OBJ_TYP_GROUP;
    unbound = true;

    for( Artifact a : grp.group ) {
      addArtifact( a.deepCopy() );
    }
  }

  
  public synchronized void setArtifacts( Vector<Artifact> list ) {
    clear();
    group = new Vector<Artifact>( list );
    for( Artifact a : group ) {
      a.parent = this;
    }
  }

  public synchronized Vector<Artifact> getArtifacts() {
    return group;
  }

  public synchronized void addArtifact( Artifact a ) {
    group.add( a );
    a.parent = this;
  }

  public synchronized boolean removeArtifact( Artifact a ) {
    if( group.remove( a ) ) {
      a.parent = null;
      return true;
    }
    return false;
  }

  public synchronized int size() {
    return group.size();
  }

  public synchronized void group() {
    updateCache( true );
    computeBoundingBox();
    pos.setLocation( ( bb[0].x + bb[1].x ) / 2, ( bb[0].y + bb[1].y ) / 2 ); 
    bb[0].translate( -pos.x, -pos.y );
    bb[1].translate( -pos.x, -pos.y );
    for( Artifact a : group ) {
      a.groupPos.setLocation( a.pos );
    }
    updateCache( true );
  }

  public synchronized void clear() {
    for( Artifact a : group ) {
      if( a.parent == this ) {
        a.parent = null;
      }
    }
    group.clear();
  }

  public synchronized void computeBoundingBox() {
    if( !group.isEmpty() ) {
      bb[0].setLocation( Integer.MAX_VALUE, Integer.MAX_VALUE );
      bb[1].setLocation( Integer.MIN_VALUE, Integer.MIN_VALUE );

      for( PhysicalArtifact a : getPhysicalArtifacts() ) {
        for( Polygon p : a.poly_cache ) {
          for( int i = 0; i < p.npoints; i++ ) {
            if( bb[0].x > p.xpoints[i] ) {
              bb[0].x = p.xpoints[i];
            }
            if( bb[1].x < p.xpoints[i] ) {
              bb[1].x = p.xpoints[i];
            }
            if( bb[0].y > p.ypoints[i] ) {
              bb[0].y = p.ypoints[i];
            }
            if( bb[1].y < p.ypoints[i] ) {
              bb[1].y = p.ypoints[i];
            }
          }
        }
      }
    }
  }

  private Collection<PhysicalArtifact> traverse( GroupArtifact g, 
                                            Vector<PhysicalArtifact> list ) {
    for( Artifact a : g.group ) {
      if( a instanceof GroupArtifact ) {
        traverse( (GroupArtifact) a, list );
      } else {
        list.add( (PhysicalArtifact) a );
      }
    }
    return list;
  } 

  public Collection<PhysicalArtifact> getPhysicalArtifacts() {
    return traverse( this, new Vector<PhysicalArtifact>() );
  }

  public synchronized void paintArtifact( Graphics2D g, boolean fill ) { }

  public synchronized void paintHandles( Graphics2D g ) {
    updateCache( false );

    if( bb[0].distance( bb[1] ) > 0 ) {
      g.setColor( Color.RED );
      g.fillOval( rot_handle[0].x - HDL_RAD, rot_handle[0].y - HDL_RAD, 
                  HDL_SIZE, HDL_SIZE );
      g.setColor( Color.BLACK );
      g.drawPolygon( bb_cache.xpoints, bb_cache.ypoints, bb_cache.npoints );
      g.drawLine( rot_handle[0].x, rot_handle[0].y, rot_handle[1].x, 
                  rot_handle[1].y );
    }
  }

  public boolean moveHandle( Point p ) {
    if( ( curHandle == rot_handle[0] ) && ( prevMove != null ) &&
        !p.equals( prevMove ) ) {
      prevMove.setLocation( p );
      return setDir( Math.atan2( p.x - pos.x, p.y - pos.y  ) );
    }
    return false;
  }

  public synchronized void updateCache( boolean force ) {
    for( Artifact a : group ) {
      a.updateCache( force );
    }

    if( force ) {
      updateBBCache();
    }
  }

  public synchronized boolean setLocation( Point p ) {
    if( !p.equals( pos ) ) {
      p.translate( -pos.x, -pos.y );
      pos.translate( p.x, p.y );
      for( Artifact a : group ) {
        Point q = a.getLocation();
        q.translate( p.x, p.y );
        a.setLocation( q );
        a.groupPos.translate( p.x, p.y );
      }
      updateCache( true );
      return true;
    } 
    return false;
  }

  public synchronized boolean setDir( double d ) {
    if( dir == d )  {
      return false;
    } 

    d -= dir;
    dir += d;
    for( Artifact a : group ) {
      Point q = new Point( a.groupPos );
      transform( q, dir, pos, null, q );
      a.setDir( a.dir + d );
      a.setLocation( q );
    }

    updateCache( true );

    return true;
  }

  public synchronized boolean contains( Point p ) {
    for( Artifact a : group ) {
      if( a.contains( p ) ) {
        return true;
      }
    }
    return false;
  }

  public synchronized boolean intersects( Artifact a ) {
    for( Artifact g : group ) {
      if( g.intersects( a ) ) {
        return true;
      }
    }
    return false;
  }

  public Artifact deepCopy() {
    return new GroupArtifact( this );
  }

  public void revert( ArtifactCheckPoint cp ) {
    curHandle = cp.curHandle;
    ArtifactCheckPoint acp = new ArtifactCheckPoint( this );

    if( cp.dir != dir ) {
      setDir( cp.dir );
    } else if( !pos.equals( cp.pos ) ) {
      setLocation( cp.pos );
    }
    
    updateCache( true );
    if( listener != null ) {
      listener.artifactChanged( this, acp );
    }
  }

  public boolean isObstacle() {
    return false;
  }

  public void insert() {
    for( Artifact a : group ) {
      a.parent = this;
      if( unbound ) {
        a.insert();
      }
    }

    ProtoGroup pg = ProtoGroup.getGroup();
    pg.clear();

    super.insert();
    unbound = false;
  }
  
  public void delete() {
    unbound = true;
    for( Artifact a : group ) {
      a.delete();
    }
    remove();
  }
    
  public void remove() {
    if( !unbound ) {
      ProtoGroup.getGroup().setArtifacts( group );
    }
    super.remove();
  }

  public boolean isGroup() {
    return true;
  }

  public boolean isUnbound() {
    return unbound;
  }

  public boolean fixGroup() {
    insert();
    prevMove = null;
    return true;
  }
}
