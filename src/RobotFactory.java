import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.*;

public class RobotFactory implements ItemListener {
  // Add new robots here
  private static final RobotArtifact [] initRobots = { new ThymioArtifact() };
       
  private RobotArtifact defaultRobot;
  private ArtifactListener robotListener;
  private final DefaultListModel<RobotArtifact> robots = 
          new DefaultListModel<RobotArtifact>();

  private class RobotListModel implements ComboBoxModel<RobotArtifact> {
    private RobotArtifact selected = defaultRobot;

    public void	addListDataListener( ListDataListener l ) {
      robots.addListDataListener( l );
    }

    public RobotArtifact getElementAt( int index ) {
      return robots.getElementAt( index );
    }

    public int getSize() {
      return robots.getSize();
    }

    public void removeListDataListener( ListDataListener l ) {
      removeListDataListener( l );
    }

    public Object getSelectedItem() {
      return selected;
    }

    public void setSelectedItem( Object item ) {
      selected = (RobotArtifact) item;
    }
  }

  public RobotFactory() {
    for( RobotArtifact r : initRobots ) {
      add( r );
    }
  }

  private RobotArtifact findRobot( short typ ) {
    Enumeration<RobotArtifact> e = robots.elements();
    while( e.hasMoreElements() ) {
      RobotArtifact r = e.nextElement();
      if( r.typ == typ ) {
        return r;
      }
    }
    return null;
  }

  public void setRobotListener( ArtifactListener rl ) {
    robotListener = rl;
  }

  public RobotArtifact getDefaultRobot() {
    return defaultRobot;
  }

  public void setDefaultRobotType( short typ ) {
    defaultRobot = findRobot( typ );
  }

  public RobotArtifact makeRobot( RobotArtifact r, Point p ) {
    if( r != null ) {
      return r.newRobot( p, robotListener );
    }
    return null;
  }

  public RobotArtifact newRobot( Point p ) {
    return makeRobot( defaultRobot, p );
  }

  public RobotArtifact newRobot( short typ, Point p ) {
    return makeRobot( findRobot( typ ), p );
  }

  public void add( RobotArtifact r ) {
    robots.addElement( r );
    if( robots.size() == 1 ) {
      defaultRobot = r;
    }
  }

  public ComboBoxModel<RobotArtifact> getRobotsModel() {
    return new RobotListModel();
  }

  public void itemStateChanged( ItemEvent e ) {
    if( e.getStateChange() == e.SELECTED ) {
      defaultRobot = (RobotArtifact) e.getItem();
    }
  }

  public void setSupportedModels( short [] models ) {
    Enumeration<RobotArtifact> e = robots.elements();
    while( e.hasMoreElements() ) {
      RobotArtifact r = e.nextElement();
      r.setSupported( false );
      for( short typ : models ) {
        if( r.typ == typ ) {
          r.setSupported( true );
          break;
        }
      }
    }
  }
}
