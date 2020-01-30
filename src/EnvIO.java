import java.awt.*;
import java.util.*;
import java.io.*;

public class EnvIO {
  private File file;
  private Env env;
  private String lastErr = null;
  private ArtifactListener artifactListener;
  private GroupFactory groupFactory;

  public EnvIO( Env e, ArtifactListener listener, ArtifactListener glistener ) {
    env = e;
    artifactListener = listener;
    groupFactory = new GroupFactory( glistener );
  }

  public void reset() {
    lastErr = null;
    file = null;
    env.setModel( new EnvModel() );
  }

  public boolean readComponent( Scanner s, EnvModel model ) {
    CommonData.EnvNew nenv = new CommonData.EnvNew();

    if( MsgCom.readData( s, nenv, nenv.fmt ) ) {
      model.setDimension( new Dimension( nenv.length, nenv.width ) );
      for( int i = 0; i < nenv.num; i++ ) {
        Artifact a = null;

        switch( nenv.objs[i].typ ) {
        case CommonData.OBJ_TYP_BLOCK: 
          a = new BlockArtifact( nenv.objs[i], artifactListener );
          break;
        case CommonData.OBJ_TYP_PATH: 
          a = new PathArtifact( nenv.objs[i], artifactListener );
          break;
        case CommonData.OBJ_TYP_POLYGON_BLOCK: 
        case CommonData.OBJ_TYP_POLYGON_MARK: 
          a = new PolygonArtifact( nenv.objs[i], artifactListener );
          break;
        }
  
        if( a != null ) {
          a.sanitize();
          a.updateCache( true );
          model.insertArtifact( a );
        }
      }

      short id = 1;
      for( int i = 0; i < nenv.num_bots; i++ ) {
        RobotArtifact r = RobotArtifact.factory.newRobot( nenv.robots[i].typ,
                                                          nenv.robots[i].pos );
        if( r == null ) {
          System.out.format( "Unknow robot typ 0x%x.  Using default.\n", 
                              nenv.robots[i].typ );
          r = RobotArtifact.factory.newRobot( nenv.robots[i].pos );
        }

        if( r != null ) {
          r.setId( id );
          r.setDir( nenv.robots[i].dir );
          r.setReset( nenv.robots[i].pos, nenv.robots[i].dir );
          model.insertRobot( r );
          id++;
        } else {
          System.out.println( "No robot models loaded!" );
          break;
        }
      }

      if( s.hasNext() ) {
        CommonData.GroupList gls = new CommonData.GroupList();
        if( MsgCom.readData( s, gls, gls.fmt ) ) {
          groupFactory.buildGroups( gls, model );
        } else {
          return false;
        }
      }

      if( s.hasNext() ) {
        for( RobotArtifact r : model.getRobots() ) {
          CommonData.EnvUserAction act = r.getAction();
          if( !MsgCom.readData( s, act, act.fmt ) ) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  public boolean loadFile( File f ) {
    try {
      Scanner s = new Scanner( f );
      EnvModel model = new EnvModel();
      if( readComponent( s, model ) ) {
        file = f;
        env.setModel( model );
        lastErr = null;
      } else {
        lastErr = "Read error";
      }
      s.close();
    } catch ( FileNotFoundException e ) { 
      lastErr = e.toString();
    }

    return lastErr == null;
  }

  public boolean saveFile() {
    if( file == null ) {
      return false;
    }
    
    return saveFile( file );
  }

  public boolean saveFile( File f ) {
    boolean rc = false;
    EnvModel model = env.getModel();

    try {
      PrintStream p = new PrintStream( f );
      rc = model.writeModel( p );
      p.close();

      if( rc ) {
        file = f;
        env.setModified( false );

        for( RobotArtifact r : model.getRobots() ) {
          r.setReset();
        }
      } else {
        lastErr = "Write error";
      }

    } catch ( FileNotFoundException e ) { 
      lastErr = e.toString();
    }
    return rc;
  }

  public String getLastErr() {
    return lastErr;
  }

  public boolean isModified() {
    return env.isModified();
  }
}
