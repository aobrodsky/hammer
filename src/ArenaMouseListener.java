import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ArenaMouseListener extends MouseAdapter {
  private Env env;
  private Arena arena;

  public ArenaMouseListener( Env e, Arena a ) {
    env = e;
    arena = a;
  }

  public void mouseMoved( MouseEvent e ) {
    Artifact a = env.getCurArtifact();
    if( a == null ) {
      return;
    }
    
    Task t = (Task) e.getSource();
    Point p = arena.translateToEnvCoords( e.getPoint() );
  
    switch( t.getTask() ) {
    case START_BLOCK:
    case PLACE_LIBOBJ:
    case START_ROBOT:
      a.moveArtifact( p );
      break;
    case NEXT_POINT:
      a.movePoint( p );
      break;
    }
  }

  public void mouseClicked( MouseEvent e ) {
    Task t = (Task) e.getSource();
    Point p = arena.translateToEnvCoords( e.getPoint() );

    switch( t.getTask() ) {
    case START_BLOCK:
      BlockArtifact b = (BlockArtifact) env.getCurArtifact();
      if( b.fixBlock() ) {
        t.setTask( Task.Type.NORMAL );
      }
      break;
    case START_ROBOT:
      RobotArtifact r = (RobotArtifact) env.getCurArtifact();
      if( r.fixRobot() ) {
        t.setTask( Task.Type.NORMAL );
      }
      break;
    case PLACE_LIBOBJ:
      GroupArtifact g = (GroupArtifact) env.getCurArtifact();
      if( g.fixGroup() ) {
        t.setTask( Task.Type.NORMAL );
      }
      break;
    case NEXT_POINT:
      if( e.getClickCount() > 1 ) {
        Artifact a = env.getCurArtifact();
        if( a.lastPoint() ) {
          t.setTask( Task.Type.NORMAL );
        }
      }
      break;
    }
  }

  public void mouseReleased( MouseEvent e ) {
    Task t = (Task) e.getSource();
    switch( t.getTask() ) {
    case DRAG_ARTIFACT:
    case DRAG_HANDLE:
      t.setTask( Task.Type.NORMAL );
      Artifact a = env.getCurArtifact();
      a.endModification();
      break;
    }
  }

  public void mouseDragged( MouseEvent e ) {
    Artifact a = env.getCurArtifact();
    Task t = (Task) e.getSource();
    Point p = arena.translateToEnvCoords( e.getPoint() );
    switch( t.getTask() ) {
    case NEXT_POINT:
      a.movePoint( p );
      break;
    case DRAG_HANDLE:
      a.moveHandle( p );
      break;
    case DRAG_ARTIFACT:
      a.moveArtifact( p );
      break;
    }
  }

  public void mousePressed( MouseEvent e ) {
    JComponent c = (JComponent) e.getSource();
    Task t = (Task) c;
    Point p = arena.translateToEnvCoords( e.getPoint() );
    Artifact a = env.getCurArtifact();
    switch( t.getTask() ) {
    case START_PATH:
      env.newPath( p );
      t.setTask( Task.Type.NEXT_POINT );
      break;
    case START_POLY:
      env.newPolygon( p );
      t.setTask( Task.Type.NEXT_POINT );
      break;
    case NEXT_POINT:
      a.addPoint();
      break;
    case NORMAL:
      if( ( a != null ) && a.selectHandle( p ) ) {
        a.startModification( p );
        t.setTask( Task.Type.DRAG_HANDLE );
      } else if( env.selectArtifact( p, e.isShiftDown() ) ) {
        a = env.getCurArtifact();
        a.startModification( p );
        t.setTask( Task.Type.DRAG_ARTIFACT );
      } 
      break;
    }
    c.requestFocus();
  }
}
