import java.awt.*;
import javax.swing.*;

public abstract class RobotArtifact extends Artifact {
  public static final RobotFactory factory = new RobotFactory();

  protected boolean offEdge = false;
  protected boolean supported = false;
  protected boolean remoteUpdate = false;
  protected boolean syncPosWithState = true;
  protected boolean stateChanged = false;
  protected boolean fixed = false;
  protected Point resetPosition = new Point();
  protected double resetDirection = 0;

  public RobotArtifact( RobotArtifact t ) {
    super( t );
  }

  public RobotArtifact( Point posn, Point orig, double dirn, Point [] v, 
                         short type, int col, ArtifactListener al ) {
    super( posn, orig, dirn, v, type, col, al );
  }

  public abstract boolean updateFromState();
  public abstract Polygon getContactVerts();
  public abstract CommonData.EnvUserAction getAction();
  public abstract CommonData.RobotState getState();
  public abstract RobotArtifact deepCopy();
  public abstract Point getBottomLeft();
  public abstract JComponent getActionPanel();
  public abstract String toString();

  public RobotArtifact newRobot( Point p, ArtifactListener al ) {
    RobotArtifact r = deepCopy();
    r.listener = al;
    r.pos.setLocation( p );
    return r;
  }

  public synchronized boolean setLocation( Point p ) {
    Point cur = new Point( pos );
    if( super.setLocation( p ) ) {
      if( fixed && ( listener != null ) && !listener.checkLocation( this ) ) {
        pos.setLocation( cur );
        listener.artifactChanged( this, null );  // Do not checkpoint thymio
      } else {
        return true;
      }
    }
    return false;
  }

  public synchronized void setOutOfBounds( boolean b ) {
    if( offEdge != b ) {
      offEdge = b;
      if( listener != null ) {
        listener.artifactChanged( this, null );
      }
    }
  }

  public boolean isRemoteUpdate() {
    return remoteUpdate;
  }

  public synchronized void startModification( Point p ) {
    if( p == null ) {
      p = pos;
    }
    checkPoint = new ArtifactCheckPoint( this );
    prevMove = new Point( p );
    syncPosWithState = false;
  }

  public synchronized void endModification() {
    syncPosWithState = true;
    if( listener != null ) {
      stateChanged = checkPoint.useful();
      listener.artifactChanged( this, null );
      stateChanged = false;
    }
  }

  public boolean stateChanged() {
    return stateChanged;
  }

  public void setReset( Point rpos, double rdir ) {
    resetPosition.setLocation( rpos );
    resetDirection = rdir;
  }

  public void setReset() {
    resetPosition.setLocation( pos );
    resetDirection = dir;
  }

  public void reset() {
    setLocation( resetPosition );
    setDir( resetDirection );
  }

  public boolean fixRobot() {
    insert();
    prevMove = null;
    fixed = true;
    return true; 
  }

  public short getId() {
    return id;
  }

  public void setId( short nid ) {
    id = nid;
  }

  public void changeId( short nid ) {
    remove();
    setId( nid );
    insert();
  }

  public boolean isObstacle() {
    return true;
  }

  public void setSupported( boolean b ) {
    supported = b;
  }

  public boolean isSupported() {
    return supported;
  }
}
