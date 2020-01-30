import java.io.*;
import java.util.*;

public class UpdateHandler implements ThorProxyListener  {
  private Env env;
  private ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
  private CommonData.RobotState curSource = new CommonData.RobotState();
  private ThorProxy thorProxy;
  private RecvState recvState = RecvState.RECV_HELLO;

  private enum RecvState {
    RECV_HELLO,
    RECV_SOURCE_ID,
    RECV_STATE,
    CLOSED
  };

  public UpdateHandler( Env e ) {
    env = e;
  }

  public boolean sendEnvironment() {
    EnvModel e = env.getModel();
    return sendToRobot( CommonData.DATA_TYP_ENV, (short)-1, e, e.fmt );
  }

  public void sendChangeArena( EnvModel e ) {
    sendToRobot( CommonData.DATA_TYP_MOD_ARENA, (short)-1, e, e.dim_fmt );
  }

  public void sendChangeObject( PhysicalArtifact p ) {
    sendToRobot( CommonData.DATA_TYP_MOD_OBJ, p.id, p, p.fmt );
  }

  public void sendDeleteObject( PhysicalArtifact p ) {
    sendToRobot( CommonData.DATA_TYP_DEL_OBJ, p.id, p, p.id_fmt );
  }

  public void sendDeleteRobot( RobotArtifact t ) {
    sendModRobot( t, CommonData.DATA_TYP_DEL_BOT );
  }

  public void sendMoveRobot( RobotArtifact t ) {
    sendModRobot( t, CommonData.DATA_TYP_MOV_BOT );
  }

  private void sendModRobot( RobotArtifact t, short typ ) {
    if( thorProxy != null ) {
      CommonData.EnvConfiguration mov = new CommonData.EnvConfiguration();
      mov.id = t.getId();
      mov.pos.setLocation( t.getLocation() );
      mov.dir = Math.toRadians( t.getDir() );
      mov.typ = t.typ;
      sendToRobot( typ, mov.id, mov, mov.fmt );
    }
  }

  public void sendUserActionToRobot( RobotArtifact t ) {
    if( thorProxy != null ) {
      CommonData.EnvUserAction ua = t.getAction();
      sendToRobot( CommonData.DATA_TYP_INP, ua.id, ua, ua.fmt );
    }
  }

  private boolean sendToRobot( short typ, short id, Object obj, 
                               MsgCom.FieldFmt [] fmt ){
    if( thorProxy != null ) {
      synchronized ( bufferStream ) {
        PrintStream out = new ThorProxyOutputStream( thorProxy, bufferStream );
        CommonData.EnvHdr hdr = new CommonData.EnvHdr();
        hdr.typ = typ;
        hdr.id = id;
        MsgCom.writeData( out, hdr, hdr.fmt );
        MsgCom.writeData( out, obj, fmt );
        out.close();
      }
      return true;
    }
    return false;
  }

  public void accepted( ThorProxy tp ) { }

  public void disconnected( ThorProxy tp ) {
    thorProxy = null;
    RobotArtifact.factory.setSupportedModels( new short[0] ); // no models 
  }

  public void connected( ThorProxy tp ) { 
    thorProxy = tp;
    recvState = RecvState.RECV_HELLO;
  }

  private RecvState handShake( ThorProxy tp, Scanner in ) {
    CommonData.Hello h = new CommonData.Hello();
    if( !MsgCom.readData( in, h, h.fmt ) ) {
      System.out.println( "Garbled hello.  Continuing at own risk." );
    } else {
      env.updateRobotIDs( h.id );
      RobotArtifact.factory.setSupportedModels( h.models );
    }
      
    if( !sendEnvironment() ) {
      System.out.println("Error: Could not send Environment information");
      tp.close();
    }
    return RecvState.RECV_SOURCE_ID;
  }

  private RecvState recvSource( ThorProxy tp, Scanner in ) {
    if( !MsgCom.readData( in, curSource, curSource.fmt ) ) {
      tp.close();
      System.out.println( "Error: Failure to receive a robot state");
      return RecvState.CLOSED;
    } else if( curSource.error == CommonData.THYMIO_ERROR_ID ) { 
      return RecvState.RECV_STATE;
    } else if( curSource.error == CommonData.THYMIO_ERROR_INVALID_MODEL ) { 
      System.out.println( "Robot model not supported by simulator" );
    } else if( curSource.error == CommonData.THYMIO_ERROR_ALLOC_FAILED ) { 
      System.out.println( "Simulator is out of memory" );
    }
    return RecvState.RECV_SOURCE_ID;
  }

  boolean updated = false;
  private RecvState recvRobotState( ThorProxy tp, Scanner in ) {
    RobotArtifact t = (RobotArtifact) env.getRobotArtifact( curSource.id );
    CommonData.RobotState rs;
    if( t == null ) { 
      System.out.println( "Received update for unknown bot: " + curSource.id );
      rs = curSource;
    } else if( t.typ != curSource.model ) {
      System.out.println( "Received update for incorrect model, ignoring:" +
                           curSource.id );
      System.out.format( "Expecting model %x, but reeceived %x\n", t.typ,
                          curSource.model );
      rs = curSource;
    } else {
      rs = t.getState();
    }
    
    if( !MsgCom.readData( in, rs, rs.fmt ) ) {
      if( rs != curSource ) { /* only close if we do not expect problems */
        tp.close();
        System.out.println( "Error: Failure to receive a robot state");
        return RecvState.CLOSED;
      }
    } else if( t != null ) {
      boolean ob_err = rs.error == CommonData.THYMIO_ERROR_OUT_OF_BOUNDS;
      t.setOutOfBounds( ob_err );
      updated = t.updateFromState() || updated;
    }
    return RecvState.RECV_SOURCE_ID;
  }

  public void outOfBandPacket( ThorProxy tp, short source, byte [] data ) { 
    Scanner in = new Scanner( new String( data ) );
    while( in.hasNext() ) {
      switch( recvState ) {
      case RECV_HELLO:
        recvState = handShake( tp, in );
        break;
      case RECV_SOURCE_ID:
        recvState = recvSource( tp, in );
        break;
      case RECV_STATE:
        recvState = recvRobotState( tp, in );
        break;
      }
    }

    if( updated ) {
      env.updateComplete();
      updated = false;
    }
  }
}
