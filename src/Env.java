import java.awt.*;
import java.util.*;

public class Env {
  private EnvModel model = new EnvModel();
  private EnvListener envListener;
  private EnvIO envIO;
  private Artifact curObj = null;
  private boolean modified = false;
  private ArtifactListener artifactListener = new ArtifactHandler();
  private final ProtoGroup protoGroup;
  private final Vector<ArtifactListener> artifactListeners = 
                                                 new Vector<ArtifactListener>();
  private final Vector<Short> deletedIDs = new Vector<Short>();

  private class RobotHandler implements ArtifactListener {
    public void artifactChanged( Artifact a, ArtifactCheckPoint acp ) { 
      RobotArtifact t = (RobotArtifact) a;

      if( t.stateChanged() ) {
        if( envListener != null ) {
          envListener.stateChanged( t, EnvListener.StateChange.POSITION );
        }
      } else if( !t.isRemoteUpdate() && ( envListener != null ) ) {
        envListener.updateArena();
      }
    }

    public void artifactCompleted( Artifact a ) {
      RobotArtifact t = (RobotArtifact) a;

      t.setReset();
      model.insertRobot( t );
      deletedIDs.remove( new Short( t.getId() ) );
      curObj = t;

      if( envListener != null ) {
        envListener.updateArena();
        envListener.objectSelected( t );
        envListener.stateChanged( t, EnvListener.StateChange.POSITION );
      }
    }

    public void artifactRemoved( Artifact a ) { 
      RobotArtifact t = (RobotArtifact) a;

      model.deleteRobot( t );
      deletedIDs.add( t.getId() );
      curObj = null;

      if( envListener != null ) {
        envListener.updateArena();
        envListener.objectSelected( null );
        envListener.stateChanged( t, EnvListener.StateChange.DELETE );
      }
    }

    public boolean checkLocation( Artifact a ) {
      Polygon p = ((RobotArtifact)a).getContactVerts();

      for( int i = 0; i < p.npoints; i++ ) {
        if( ( p.xpoints[i] < 0 ) || ( p.xpoints[i] > model.length ) || 
            ( p.ypoints[i] < 0 ) || ( p.ypoints[i] > model.width ) ) {
          return false;
        }
      }

      for( RobotArtifact t : model.getRobots() ) {
        if( ( t != a ) && a.intersects( t ) ) {
          return false;
        }
      }

      return true; 
    }
  }

  private class ArtifactHandler implements ArtifactListener {
    public void artifactChanged( Artifact a, ArtifactCheckPoint acp ) { 
      setModified( true );

      if( envListener != null ) {
        envListener.updateArena();
        if( ( acp != null ) && ( a instanceof PhysicalArtifact ) && 
              acp.useful() ) {
          envListener.stateChanged( a, EnvListener.StateChange.OBJECT );
        }
      }

      for( ArtifactListener al : artifactListeners ) {
        al.artifactChanged( a, acp );
      }
    }

    public void artifactCompleted( Artifact a ) { 
      setModified( true );

      model.insertArtifact( a );
      curObj = null;

      if( envListener != null ) {
        envListener.updateArena();
        envListener.objectSelected( null );
        if( a instanceof PhysicalArtifact ) {
          envListener.stateChanged( a, EnvListener.StateChange.OBJECT );
        }
      }

      for( ArtifactListener al : artifactListeners ) {
        al.artifactCompleted( a );
      }
    }

    public void artifactRemoved( Artifact a ) { 
      setModified( true );

      model.deleteArtifact( a );
      curObj = null;

      if( envListener != null ) {
        envListener.updateArena();
        envListener.objectSelected( null );
        if( a instanceof PhysicalArtifact ) {
          envListener.stateChanged( a, EnvListener.StateChange.DELETE );
        }
      }

      for( ArtifactListener al : artifactListeners ) {
        al.artifactRemoved( a );
      }
    }

    public boolean checkLocation( Artifact a ) { 
      boolean rc = true;

      for( ArtifactListener al : artifactListeners ) {
        rc = rc && al.checkLocation( a );
      }

      for( RobotArtifact t : model.getRobots() ) {
        rc = rc && !a.intersects( t );
        if( !rc ) {
          break;
        }
      }

      return rc;
    }
  }

  private class GroupListener implements ArtifactListener {
    public void artifactChanged( Artifact a, ArtifactCheckPoint acp ) { 
      GroupArtifact g = (GroupArtifact)a;
      setModified( true );

      if( envListener != null ) {
        envListener.updateArena();
        if( ( acp != null ) && acp.useful() ) {
          for( PhysicalArtifact p : g.getPhysicalArtifacts() ) {
            envListener.stateChanged( p, EnvListener.StateChange.OBJECT );
          }
        }
      }

      for( ArtifactListener al : artifactListeners ) {
        al.artifactChanged( a, acp );
      }
    }

    public void artifactCompleted( Artifact a ) { 
      GroupArtifact g = (GroupArtifact)a;
      setModified( true );

      if( g != protoGroup ) {
        model.insertGroup( g );
      }
      curObj = g;

      if( envListener != null ) {
        envListener.objectSelected( g );
        envListener.updateArena();
        for( PhysicalArtifact p : g.getPhysicalArtifacts() ) {
          envListener.stateChanged( p, EnvListener.StateChange.OBJECT );
        }
      }

      for( ArtifactListener al : artifactListeners ) {
        al.artifactCompleted( g );
      }
    }

    public void artifactRemoved( Artifact a ) { 
      GroupArtifact g = (GroupArtifact)a;
      setModified( true );

      model.deleteGroup( g );
      curObj = protoGroup;

      if( envListener != null ) {
        envListener.objectSelected( curObj );
        envListener.updateArena();
        for( PhysicalArtifact p : g.getPhysicalArtifacts() ) {
          envListener.stateChanged( p, EnvListener.StateChange.DELETE );
        }
      }

      for( ArtifactListener al : artifactListeners ) {
        al.artifactRemoved( g );
      }
    }

    public boolean checkLocation( Artifact a ) {
      return true;
    }
  }

  public Env( EnvListener el ) {
    envListener = el;
    RobotArtifact.factory.setRobotListener( new RobotHandler() );
    ArtifactListener gl = new GroupListener();
    envIO = new EnvIO( this, artifactListener, gl );
    protoGroup = ProtoGroup.newGroup( gl );
    makeDefaultRobot();
  }

  public EnvModel getModel() {
    return model;
  }

  public void setModel( EnvModel mod ) {
    short id = model.getMinRobotId();

    model = mod;

    if( model.getNumRobots() == 0 ) {
      makeDefaultRobot();
    } else {
      updateRobotIDs( id );
    }

    curObj = null;
    setModified( false );

    if( envListener != null ) {
      envListener.updateAll();
      envListener.objectSelected( null );
      envListener.stateChanged( null, EnvListener.StateChange.ENVIRONMENT );
    } 

    // send out the noise settings 
    for( RobotArtifact r : model.getRobots() ) {
      userAction( r );
    }
  }

  private void makeDefaultRobot() {
    RobotArtifact r = RobotArtifact.factory.newRobot( model.pos );
    if( r != null ) {
      r.setDir( model.dir );
      r.setId( (short)1 ); // default id
      r.fixRobot();
    } else {
      System.out.println( "Could not Create default robot" );
    }
  }

  public Dimension getArenaSize() {
    return new Dimension( model.length, model.width );
  }

  public boolean setArenaSize( Dimension d ) {
    if( ( model.length == d.width ) && ( model.width == d.height ) ) {
      return false;
    } else {
      model.length = d.width;
      model.width = d.height;
      if( envListener != null ) {
        envListener.updateArena();
        envListener.modelChanged( model );
      }
      // Should we check if any objects are out of the model now?
      return true;
    }
  }

  public void updateComplete() {
    if( envListener != null ) {
      envListener.updateArena();
    }
  }

  public void newRobot() {
    RobotArtifact r = RobotArtifact.factory.getDefaultRobot();
    Point p = r.getBottomLeft();
    p.setLocation( model.length - p.x, model.width - p.y );
    r = RobotArtifact.factory.newRobot( p );
    curObj = r;
    r.setId( model.nextRobotId() );

    if( envListener != null ) {
      envListener.updateArena();
    }
  }

  public void changeRobotModel( short typ ) {
    RobotArtifact r = (RobotArtifact) curObj;
    short id = r.getId();
    double dir = r.dir;
    Point pos = r.getLocation();
    r.delete();

    r = RobotArtifact.factory.newRobot( typ, pos );
    curObj = r;
    r.setId( id );
    r.setDir( dir );
    r.fixRobot();

    if( envListener != null ) {
      envListener.updateArena();
    }
  }

  public void newBlock() {
    Point p = new Point( model.length + BlockArtifact.blockDim.width, 
                         model.width + BlockArtifact.blockDim.height );
    curObj = new BlockArtifact( p, 0, artifactListener );

    if( envListener != null ) {
      envListener.updateArena();
    }
  }

  public void newPolygon( Point p ) {
    curObj = new PolygonArtifact( p, artifactListener );
  }

  public void newPath( Point p ) {
    curObj = new PathArtifact( p, artifactListener );
  }

  public void newLibraryObject( GroupArtifact g ) {
    g = new GroupArtifact( g );
    g.group();
    curObj = g;
    if( envListener != null ) {
      envListener.objectInstantiatedFromLibrary( g );
      envListener.updateArena();
    }
  }

  public boolean isModified() {
    return modified;
  }

  public void userAction( RobotArtifact r ) {
    if( envListener != null ) {
      envListener.stateChanged( r, EnvListener.StateChange.USER_ACTION );
    }
  }

  public void selectAll() {
    Collection<Artifact> artifacts = model.getArtifacts();
    if( artifacts.size() < 1 ) {
      return;
    }

    protoGroup.clear();
    for( Artifact a : artifacts ) {
      if( !a.inGroup() ) {
        protoGroup.addArtifact( a );
      }
    }

    for( GroupArtifact g : model.getGroups() ) {
      if( !g.inGroup() ) {
        protoGroup.addArtifact( g );
      }
    }

    curObj = protoGroup;

    if( envListener != null ) {
      envListener.objectSelected( curObj );
    }
  }

  public boolean selectArtifact( Point p, boolean shift ) {
    Artifact prev = curObj;
    curObj = null;

    if( p != null ) {
      for( RobotArtifact t : model.getRobots() ) {
        if( t.contains( p ) ) {
          curObj = t;
          protoGroup.clear();
          break;
        }
      }

     if( curObj == null ) {
        for( Artifact a : model.getArtifacts() ) {
          if( a.contains( p ) ) {
            a = protoGroup.getRoot( a );

            if( shift ) {
              curObj = protoGroup.removeArtifactFromGroup( a );
              if( curObj == a ) {
                if( ( prev == null ) || ( prev instanceof RobotArtifact ) ) {
                  curObj = a;
                } else {
                  curObj = protoGroup.addArtifact( a, prev );
                } 
              }
            } else if( a.parent != null ) {
              curObj = a.parent;
            } else { 
              curObj = a;
              protoGroup.clear();
            }
            break;
          }   
        } 
      }
    }

    if( curObj == null ) {
      protoGroup.clear();
    }

    if( envListener != null ) {
      envListener.objectSelected( curObj );
    }
    return curObj != null;
  }

  public RobotArtifact getRobotArtifact( short id ) {
    return model.getRobot( id );
  }

  public boolean setCurArtifact( Artifact a ) {
    for( Artifact b : model.getArtifacts() ) {
      if( a == b ) {
        curObj = a;
        break;
      }
    }

    if( a != curObj ) {
      Collection<GroupArtifact> groups = model.getGroups();
      if( a instanceof ProtoGroup ) {
        curObj = a;
      } else if( ( a instanceof GroupArtifact ) && groups.contains(a) ) {
        curObj = a;
      }
    }
        
    if( envListener != null ) {
      envListener.objectSelected( curObj );
    }
    return a == curObj;
  }

  public Artifact getCurArtifact() {
    return curObj;
  }

  public void addArtifactListener( ArtifactListener al ) {
    artifactListeners.add( al );
  }

  public void group() {
    GroupArtifact g = protoGroup.makeGroup();
    if( g != null ) {
      g.insert(); 
      curObj = g;

      if( envListener != null ) {
        envListener.objectSelected( curObj );
      }
    }
  }

  public void ungroup() {
    if( curObj instanceof GroupArtifact ) {
      GroupArtifact g = (GroupArtifact)curObj;
      curObj = null;
      g.remove();
    
      if( envListener != null ) {
        envListener.objectSelected( curObj );
      }
    }
  }

  public void resetRobotPosition() {
    for( RobotArtifact t : model.getRobots() ) {
      t.startModification( null );
      t.reset();
      t.endModification();
    }
  }

  public void setModified( boolean b ) {
    if( b != modified ) {
      HammerController.setCurrentFileModified( b );
      modified = b;
    }
  }

  public void updateRobotIDs( short id ) {
    short diff = (short) ( id - model.getMinRobotId() );
    for( RobotArtifact t : model.getRobots() ) {
      t.setId( (short) ( t.getId() + diff ) );
    }

    if( envListener != null ) {
      envListener.updateArena();
    }
  }

  public Vector<Short> getFreeIDs() {
    Vector<Short> vid = new Vector<Short>( deletedIDs );

    short id = model.nextRobotId();
    if( !vid.contains( id ) ) {
      vid.add( id );
    }
    return vid;
  }

  public EnvIO getEnvIO() {
    return envIO;
  }
}
