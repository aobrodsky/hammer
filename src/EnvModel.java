import java.awt.*;
import java.util.*;
import java.io.*;

public class EnvModel extends CommonData.EnvNew {
  private ArtifactCollection artifactCollection = new ArtifactCollection();
  private RobotCollection robotCollection = new RobotCollection();
  protected final Vector<GroupArtifact> groups = new Vector<GroupArtifact>();

  private class ArtifactCollection extends AbstractCollection<Artifact> {
    public int size() {
      return num;
    }

    public Iterator<Artifact> iterator() {
      return new ArtifactIterator();
    }
  }

  private class RobotCollection extends AbstractCollection<RobotArtifact> {
    public int size() {
      return num;
    }

    public Iterator<RobotArtifact> iterator() {
      return new RobotIterator();
    }
  }

  private class ArtifactIterator implements Iterator<Artifact> {
    int idx = 0;

    public boolean hasNext() {
      return idx < num;
    }

    public Artifact next() {
      idx++;
      return (Artifact) objs[idx - 1];
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

  private class RobotIterator implements Iterator<RobotArtifact> {
    int idx = 0;

    public boolean hasNext() {
      return idx < num_bots;
    }

    public RobotArtifact next() {
      idx++;
      return (RobotArtifact) robots[idx - 1];
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

  public EnvModel() { }

  public EnvModel( int len, int wid ) {
    length = len;
    width = wid;
  }

  public Collection<Artifact> getArtifacts() {
    return artifactCollection;
  }

  public Collection<RobotArtifact> getRobots() {
    return robotCollection;
  }

  public Collection<GroupArtifact> getGroups() {
    return groups;
  }

  public boolean writeModel( PrintStream p ) {
    boolean rc = MsgCom.writeData( p, this, this.fmt );
    if( rc ) {
      for( short i = 0; i < num; i++ ) {
        Artifact a = (Artifact)objs[i];
        a.groupId = i;
      }

      CommonData.GroupList g = GroupFactory.getGroupList( groups );
      rc = MsgCom.writeData( p, g, g.fmt );
    }

    if( rc ) {
      for( RobotArtifact r : getRobots() ) {
        CommonData.EnvUserAction act = r.getAction();
        if( !MsgCom.writeData( p, act, act.fmt ) ) {
          rc = false;
          break;
        }
      }
    }
    return rc;
  }

  public boolean writeModel( PrintStream p, CommonData.EnvNew env, 
                                 Vector<GroupArtifact> gv ) {
    return false;
  }

  public boolean sendModel( PrintStream s ) {
    return MsgCom.writeData( s, this, fmt );
  }

  public Dimension getDimension() {
    return new Dimension( length, width );
  }

  public boolean setDimension( Dimension d ) {
    if( ( length != d.width ) || ( width != d.height ) ) {
      length = d.width;
      width = d.height;
      return true;
    }
    return false;
  }

  public short nextRobotId() {
    short id = 0;
    for( int i = 0; i < num_bots; i++ ) {
      if( robots[i].id > id ) {
        id = robots[i].id;
      }
    }
    id++;
    return id;
  }   

  public short getMinRobotId() {
    short min = 1;
    if( num_bots > 0 ) {
      for( int i = 0; i < num_bots; i++ ) {
        if( min > robots[i].id ) {
          min = robots[i].id;
        }
      }
    }
    return min;
  }

  public void deleteArtifact( Artifact a ) {
    if( a != null ) {
      for( int i = 0; i < num; i++ ) {
        if( objs[i] == a ) {
          objs[i] = objs[num - 1];
          objs[num - 1] = null;
          num--;
          break;
        }
      }
    }
  }

  public void deleteRobot( RobotArtifact a ) {
    if( a != null ) {
      for( int i = 0; i < num_bots; i++ ) {
        if( robots[i] == a ) {
          robots[i] = robots[num_bots - 1];
          robots[num_bots - 1] = null;
          num_bots--;
          break;
        }
      }
    }
  }

  public void deleteGroup( GroupArtifact g ) {
    groups.remove( g );
  }

  public Artifact getArtifact( short idx ) {
    if( ( idx >= 0 ) && ( idx < num ) ) {
      return (Artifact) objs[idx];
    }
    return null;
  }

  public GroupArtifact getGroup( int idx ) {
    if( ( idx >= 0 ) && ( idx < groups.size() ) ) {
      return groups.elementAt( idx );
    }
    return null;
  }

  public RobotArtifact getRobot( short id ) {
    for( int i = 0; i < num_bots; i++ ) {
      if( robots[i].id == id ) {
        return (RobotArtifact) robots[i];
      }
    }
    return null;
  }

  public boolean insertArtifact( Artifact a ) {
    synchronized( this ) {
      for( int i = 0; i < num ; i++ ) {
        if( a == objs[i] ) {
          return false;
        }
      }

      if( num >= objs.length ) {
        objs = Arrays.copyOf( objs, 2 * num );
      }
      objs[num] = a;
      num++;
    }
    return true;
  }

  public boolean insertRobot( RobotArtifact a ) {
    synchronized( this ) {
      for( int i = 0; i < num_bots ; i++ ) {
        if( a == robots[i] ) {
          return false;
        }
      }

      if( num_bots >= robots.length ) {
        robots = Arrays.copyOf( robots, 2 * num_bots );
      }
      robots[num_bots] = a;
      num_bots++;
    }
    return true;
  }

  public void insertGroup( GroupArtifact g ) {
    groups.add( g );
  }

  public int getNumArtifacts() {
    return num;
  }

  public int getNumRobots() {
    return num_bots;
  }
}
