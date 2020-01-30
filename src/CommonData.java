import java.awt.*; 
import java.util.*;

public class CommonData {
  public final static short THYSIM_PROTOCOL_VERSION = 4;
  public final static short THYSIM_PROTOCOL_MULTIBOT_SUPPORT = 3;

  public final static int NUM_WHEELS = 2;
  public final static int LEFT_WHEEL = 0;
  public final static int RIGHT_WHEEL = 1;
  public final static int NUM_PROX_HORIZ = 7;
  public final static int NUM_PROX_GRND = 2;
  public final static int NUM_BUTTONS = 5;
  public final static int AMB_LIGHT = 0;
  public final static int MAX_LIGHT = 1023;
  public final static int ARC_REP_GRAN = 5; // represent arcs by 1 line / 5 degs
                               // corners   front arc degs   rep by 5 deg lines
  public final static int NUM_VERTICES = (4 + ((45 - (-45)) / ARC_REP_GRAN));
  public final static double MAX_SPEED = ( 200.0 / 1000000.0 ); // mm per usec 
  public final static int MAX_SETTING = 500;  // max motor setting
  public final static double SPEED_CONV = ( MAX_SPEED / MAX_SETTING );
  public final static int MAX_PROX_DIST = 100; /* mm */
  public final static int PROX_MAX_VAL = 5000;
  public final static int PROX_C_PARAM = 2500;
  public final static int NUM_CIRCLE_LEDS = 8;
  public final static int NUM_PROX_LEDS = 8; // should be 7, but firmware uses 8

  public final static short THYMIO_ERROR_NONE = 0;
  public final static short THYMIO_ERROR_BAD_ID = 1;
  public final static short THYMIO_ERROR_ALLOC_FAILED = 2;
  public final static short THYMIO_ERROR_OBJ_OUT_OF_BOUNDS = 3;
  public final static short THYMIO_ERROR_OUT_OF_BOUNDS = 4;
  public final static short THYMIO_ERROR_HEARTBEAT = 5;
  public final static short THYMIO_ERROR_BAD_PASSWORD = 6;
  public final static short THYMIO_ERROR_ID = 7;
  public final static short THYMIO_ERROR_INVALID_MODEL = 8;

  public final static int THYMIO_BUTTON_IDX_BACK = 0;
  public final static int THYMIO_BUTTON_IDX_LEFT = 1;
  public final static int THYMIO_BUTTON_IDX_CENTER = 2;
  public final static int THYMIO_BUTTON_IDX_FWD = 3;
  public final static int THYMIO_BUTTON_IDX_RIGHT = 4;

  public final static int THYMIO_NOISE_IDX_LEFT_MOTOR_BIAS = 0;
  public final static int THYMIO_NOISE_IDX_RIGHT_MOTOR_BIAS = 1;
  public final static int THYMIO_NOISE_IDX_LEFT_MOTOR_VAR = 2;
  public final static int THYMIO_NOISE_IDX_RIGHT_MOTOR_VAR = 3;
  public final static int THYMIO_NOISE_IDX_GROUND_SENSOR_BIAS = 4;
  public final static int THYMIO_NOISE_IDX_GROUND_SENSOR_VAR = 5;
  public final static int THYMIO_NOISE_IDX_HORIZ_SENSOR_BIAS = 6;
  public final static int THYMIO_NOISE_IDX_HORIZ_SENSOR_VAR = 7;
  public final static int THYMIO_NOISE_SIZE = 8;

  public final static short MODEL_THYMIO_II = 0;

  public static class RobotState {
    public short ver = THYSIM_PROTOCOL_VERSION;
    public short id;
    public short error;
    public short model;

    public MsgCom.FieldFmt [] fmt = baseFmt;

    public RobotState() { }

    public RobotState( RobotState rs ) {
      rs.copyTo( this );
    }
  
    public boolean equals( RobotState rs ) {
      return ( rs.ver == ver ) && ( rs.error == error ) && ( rs.id == id ) && 
             ( rs.model == model );
    }
 
    protected void copyArray( short [] dst, short [] src ) {
      System.arraycopy( src, 0, dst, 0, src.length );
    }

    public void copyTo( RobotState rs ) {
      rs.ver = ver;
      rs.id = id;
      rs.error = error;
      rs.model = model;
    }

    public static final MsgCom.FieldFmt [] baseFmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "ver", RobotState.class, "ver", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "id", RobotState.class, "id", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "error", RobotState.class, "error", 
                       1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "model", RobotState.class, "model", 
                       1 )
    };
  };

  public static class ThymioState extends RobotState {
    public short [] target = new short[NUM_WHEELS];
    public short [] speed = new short[NUM_WHEELS];
    public Point position = new Point();
    public double direction;
    public short [] button_state = new short[NUM_BUTTONS];
    public short tap;
    public short [] circle_leds = new short[NUM_CIRCLE_LEDS];
    public short [] leds_top = new short[3];
    public short [] leds_bot_left = new short[3];
    public short [] leds_bot_right = new short[3];
    public short [] leds_prox = new short[NUM_PROX_LEDS];
    public short [] leds_prox_grnd = new short[2];
    public short [] leds_buttons = new short[4];
    public short leds_rc;
    public short [] leds_temp = new short[2];
    public short leds_sound;

    /* private information, never recv */
    public double [] noise = new double[THYMIO_NOISE_SIZE];

    public ThymioState() {
      super.fmt = this.fmt;
    }

    public ThymioState( ThymioState ts ) {
      super.fmt = this.fmt;
      ts.copyTo( this );
    }
  
    public boolean equals( ThymioState ts ) {
      return super.equals( ts ) &&
             Arrays.equals( ts.target, target ) &&
             Arrays.equals( ts.speed, speed ) &&
             ts.position.equals( position ) && 
             ( ts.direction == direction ) &&
             Arrays.equals( ts.button_state, button_state ) &&
             ( ts.tap == tap ) && 
             Arrays.equals( ts.circle_leds, circle_leds ) &&
             Arrays.equals( ts.leds_top, leds_top ) &&
             Arrays.equals( ts.leds_bot_left, leds_bot_left ) &&
             Arrays.equals( ts.leds_bot_right, leds_bot_right ) &&
             Arrays.equals( ts.leds_prox, leds_prox ) &&
             Arrays.equals( ts.leds_prox_grnd, leds_prox_grnd ) &&
             Arrays.equals( ts.leds_buttons, leds_buttons ) &&
             ( ts.leds_rc == leds_rc ) && 
             Arrays.equals( ts.leds_temp, leds_temp ) &&
             ( ts.leds_sound == leds_sound );
    }
 
    public void copyTo( ThymioState ts ) {
      super.copyTo( ts );
      copyArray( ts.target, target );
      copyArray( ts.speed, speed );
      ts.position.setLocation( position );
      ts.direction = direction;
      copyArray( ts.button_state, button_state );
      ts.tap = tap;
      copyArray( ts.circle_leds, circle_leds );
      copyArray( ts.circle_leds, circle_leds );
      copyArray( ts.leds_top, leds_top );
      copyArray( ts.leds_bot_left, leds_bot_left );
      copyArray( ts.leds_bot_right, leds_bot_right );
      copyArray( ts.leds_prox, leds_prox );
      copyArray( ts.leds_prox_grnd, leds_prox_grnd );
      copyArray( ts.leds_buttons, leds_buttons );
      ts.leds_rc = leds_rc;
      copyArray( ts.leds_temp, leds_temp );
      ts.leds_sound = leds_sound;
    }

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "ver", ThymioState.class, "ver", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "id", ThymioState.class, "id", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "error", ThymioState.class, "error", 
                       1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "model", ThymioState.class, "model", 
                       1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "target", ThymioState.class, 
                       "target", NUM_WHEELS ),
      MsgCom.addField( MsgCom.Type.SINT16, "speed", ThymioState.class, 
                       "speed", NUM_WHEELS ),
      MsgCom.addField( MsgCom.Type.VECT, "position", ThymioState.class, 
                       "position", 1 ),
      MsgCom.addField( MsgCom.Type.ANGLE, "direction", ThymioState.class, 
                       "direction", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "buttons", ThymioState.class, 
                       "button_state", NUM_BUTTONS ),
      MsgCom.addField( MsgCom.Type.SINT16, "tap", ThymioState.class, "tap", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_circle", ThymioState.class, 
                       "circle_leds", NUM_CIRCLE_LEDS ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_top", ThymioState.class, 
                       "leds_top", 3 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_bot_left", ThymioState.class, 
                       "leds_bot_left", 3 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_bot_right", ThymioState.class, 
                       "leds_bot_right", 3 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_prox", ThymioState.class, 
                       "leds_prox", NUM_PROX_LEDS),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_prox_grnd", ThymioState.class, 
                       "leds_prox_grnd", 2 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_buttons", ThymioState.class, 
                       "leds_buttons", 4 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_rc", ThymioState.class, 
                       "leds_rc", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_temp", ThymioState.class, 
                       "leds_temp", 2 ),
      MsgCom.addField( MsgCom.Type.SINT16, "leds_sound", ThymioState.class, 
                       "leds_sound", 1 )
    };
  };

  public final static short DATA_TYP_ENV = 0;
  public final static short DATA_TYP_MOV_BOT = 1;
  public final static short DATA_TYP_INP = 2;
  public final static short DATA_TYP_HB = 3; /* Heart beat */
  public final static short DATA_TYP_MOD_OBJ = 4;
  public final static short DATA_TYP_DEL_OBJ = 5;
  public final static short DATA_TYP_DEL_BOT = 6;
  public final static short DATA_TYP_MOD_ARENA = 7;

  public static class EnvPolygon {
    public int      num;
    public Point [] verts;

    public EnvPolygon() { }

    public EnvPolygon( Point [] v ) {
      num = v.length;
      verts = new Point[num];
      for( int i = 0; i < num; i++ ) {
        verts[i] = new Point( v[i] );
      }
    }

    public EnvPolygon( EnvPolygon p ) {
      num = p.num;
      verts = new Point[num];
      for( int i = 0; i < num; i++ ) {
        verts[i] = new Point( p.verts[i] );
      }
    }
 
    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.UINT32, "num", EnvPolygon.class, "num", 1 ),
      MsgCom.addField( MsgCom.Type.VECT, "verts", EnvPolygon.class, "verts", -1)
    };
  };

  public final static short ROBOT_TYP_THYMIO = (short) 0x8000;

  public final static short OBJ_TYP_PROTOGROUP = -2;
  public final static short OBJ_TYP_GROUP = -1;
  public final static short OBJ_TYP_BLOCK = 0;
  public final static short OBJ_TYP_PATH = 1;
  public final static short OBJ_TYP_POLYGON_BLOCK = 2;
  public final static short OBJ_TYP_POLYGON_MARK = 3;

  public static class EnvObject extends EnvConfiguration {
    public short         clr;
    public Point         org = new Point();
    public int           num;
    public EnvPolygon [] polys;

    public EnvObject() { }

    public EnvObject( EnvObject o ) {
      typ = o.typ;
      clr = o.clr;
      org.setLocation( o.org );
      pos.setLocation( o.pos );
      dir = o.dir;
      num = o.num;
      polys = new EnvPolygon[num];
      for( int i = 0; i < num; i++ ) {
        polys[i] = new EnvPolygon( o.polys[i] );
      }
    }

    public static final MsgCom.FieldFmt [] id_fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "id", EnvObject.class, "id", 1 )
    };

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "id", EnvObject.class, "id", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "typ", EnvObject.class, "typ", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "clr", EnvObject.class, "clr", 1 ),
      MsgCom.addField( MsgCom.Type.VECT, "org", EnvObject.class, "org", 1 ),
      MsgCom.addField( MsgCom.Type.VECT, "pos", EnvObject.class, "pos", 1 ),
      MsgCom.addField( MsgCom.Type.ANGLE, "dir", EnvObject.class, "dir", 1 ),
      MsgCom.addField( MsgCom.Type.UINT32, "num", EnvObject.class, "num", 1 ),
      MsgCom.addField( MsgCom.Type.STRUCT, "polys", EnvObject.class, "polys", 
                       -1, EnvPolygon.fmt )
    };
  };

  public static class EnvNew {
    static public final int DEF_LENGTH = 2000;
    static public final int DEF_WIDTH = 1000;
    static public final Point defaultPos = new Point( 100, DEF_WIDTH / 2 );

    public int                 length = DEF_LENGTH;
    public int                 width = DEF_WIDTH;
    public int                 num;
    public EnvObject []        objs = new EnvObject[1];
    public int                 num_bots;
    public EnvConfiguration [] robots = new EnvConfiguration[1];

    // Depracated fields
    public Point         pos = new Point( defaultPos );
    public double        dir = 0.0;

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.UINT32, "length", EnvNew.class, "length", 1),
      MsgCom.addField( MsgCom.Type.UINT32, "width", EnvNew.class, "width", 1 ),
      MsgCom.addField( MsgCom.Type.UINT32, "num", EnvNew.class, "num", 1 ),
      MsgCom.addField( MsgCom.Type.STRUCT, "objs", EnvNew.class, "objs", -1, 
                       EnvObject.fmt ),
      MsgCom.addField( MsgCom.Type.UINT32, "num_bots", EnvNew.class, "num_bots",
                       1 ),
      MsgCom.addField( MsgCom.Type.STRUCT, "robots", EnvNew.class, "robots",
                       -1, EnvConfiguration.fmt ),
      // Depracated fields
      MsgCom.addField( MsgCom.Type.VECT, "pos", EnvNew.class, "pos", 1, true ),
      MsgCom.addField( MsgCom.Type.ANGLE, "dir", EnvNew.class, "dir", 1, true )
    };

    public static final MsgCom.FieldFmt [] dim_fmt = {
      MsgCom.addField( MsgCom.Type.UINT32, "length", EnvNew.class, "length", 1),
      MsgCom.addField( MsgCom.Type.UINT32, "width", EnvNew.class, "width", 1 )
    };
  };

  public static class EnvConfiguration {
    public short         id;
    public Point         pos = new Point();
    public double        dir = 0.0;
    public short         typ;

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "id", EnvConfiguration.class, "id", 
                       1 ),
      MsgCom.addField( MsgCom.Type.VECT, "pos", EnvConfiguration.class, "pos", 
                       1 ),
      MsgCom.addField( MsgCom.Type.ANGLE, "dir", EnvConfiguration.class, "dir",
                       1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "typ", EnvConfiguration.class, "typ",
                       1 ),
    };
  };

  public abstract static class EnvUserAction {
    public short         id;
    public MsgCom.FieldFmt [] fmt;
  };

  public static class EnvThymioInput extends EnvUserAction {
    public short []      buttons = new short[NUM_BUTTONS];
    public short         tap = 0;
    public double []     noise; 

    public EnvThymioInput() {
      super.fmt = this.fmt;
    }

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "id", EnvThymioInput.class, "id", 1),
      MsgCom.addField( MsgCom.Type.SINT16, "buttons", EnvThymioInput.class, 
                       "buttons", NUM_BUTTONS ),
      MsgCom.addField( MsgCom.Type.SINT16, "tap", EnvThymioInput.class, "tap", 
                       1 ),
      MsgCom.addField( MsgCom.Type.ANGLE, "noise", EnvThymioInput.class,"noise",
                       THYMIO_NOISE_SIZE ),
    };
  };

  public static class EnvHdr {
    public short typ;
    public short ver = THYSIM_PROTOCOL_VERSION;
    public short id;

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "typ", EnvHdr.class, "typ", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "ver", EnvHdr.class, "ver", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "id", EnvHdr.class, "id", 1 )
    };
  };

  public static class GroupList {
    public int ver = 1;
    public int num;
    public GroupObj [] groups;

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.UINT32, "group_ver", GroupList.class, "ver",
                       1 ),
      MsgCom.addField( MsgCom.Type.UINT32, "num_groups", GroupList.class, "num",
                       1 ),
      MsgCom.addField( MsgCom.Type.STRUCT, "groups", GroupList.class, "groups",
                       -1, GroupObj.fmt )
    };
  } 
  
  public static class GroupObj {
    public int id;
    public Point [] bbox = { new Point(), new Point() };
    public Point pos = new Point();
    public double dir = 0.0;
    public int num;
    public short [] objs;

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.UINT32, "gid", GroupObj.class, "id", 1),
      MsgCom.addField( MsgCom.Type.VECT, "bbox", GroupObj.class, "bbox", 2 ),
      MsgCom.addField( MsgCom.Type.VECT, "pos", GroupObj.class, "pos", 1 ),
      MsgCom.addField( MsgCom.Type.ANGLE, "dir", GroupObj.class, "dir", 1 ),
      MsgCom.addField( MsgCom.Type.UINT32, "num", GroupObj.class, "num", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "objs", GroupObj.class, "objs", -1 )
    };
  }

  public static class Hello {
    public short         ver;
    public short         id;
    public short         bots;
    public short         num_models;
    public short []      models;

    public static final MsgCom.FieldFmt [] fmt = {
      MsgCom.addField( MsgCom.Type.SINT16, "ver", Hello.class, "ver", 1),
      MsgCom.addField( MsgCom.Type.SINT16, "id", Hello.class, "id", 1),
      MsgCom.addField( MsgCom.Type.SINT16, "bots", Hello.class, "bots", 1),
      MsgCom.addField( MsgCom.Type.SINT16, "num_models", Hello.class, 
                       "num_models", 1 ),
      MsgCom.addField( MsgCom.Type.SINT16, "models", Hello.class, "models", -1 )
    };
  };
}

