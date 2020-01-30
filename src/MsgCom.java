import java.awt.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class MsgCom {
  public static class Type {
    final static int SINT16 = 0;
    final static int UINT32 = 1;
    final static int VECT = 2;
    final static int COORD = 3;
    final static int ANGLE = 4;
    final static int BYTE = 5;
    final static int STRUCT = 6;
  }

  final static String EOS = "EOS";
  final static String EOT = "EOT";
  final static String FLD_SEP = ";";

  public static class FieldFmt {
    int   type;
    String fld_id;
    Field  fld;
    int    num_elems;
    FieldFmt [] st_fmt;
    boolean deprecated;

    public FieldFmt( int typ, String fid, Class<?> cls, String fname, int num,
                     boolean dep ) throws NoSuchFieldException {
      assert( typ != Type.STRUCT );
      type = typ;
      fld_id = fid;
      fld = cls.getField( fname );
      num_elems = num;
      deprecated = dep;
    }

    public FieldFmt( int typ, String fid, Class<?> cls, String fname, int num,
                     FieldFmt [] fmt, boolean dep) throws NoSuchFieldException {
      assert( typ == Type.STRUCT );
      type = typ;
      fld_id = fid;
      fld = cls.getField( fname );
      num_elems = num;
      st_fmt = fmt;
      deprecated = dep;
    }
  }

  public static FieldFmt addField( int typ, String fid, Class<?> cls, 
                                   String fname, int num, boolean dep ) {
    try {
      return new FieldFmt( typ, fid, cls, fname, num, dep );
    } catch ( Exception e ) {
      e.printStackTrace();
      System.exit( 1 );
      return null;
    }
  }

  public static FieldFmt addField( int typ, String fid, Class<?> cls, 
                                   String fname, int num ) {
    return addField( typ, fid, cls, fname, num, false );
  }

  public static FieldFmt addField( int typ, String fid, Class<?> cls, 
                                   String fname, int num, FieldFmt [] fmt,
                                   boolean dep ) {
    try {
      return new FieldFmt( typ, fid, cls, fname, num, fmt, dep );
    } catch ( Exception e ) {
      e.printStackTrace();
      System.exit( 1 );
      return null;
    }
  }

  public static FieldFmt addField( int typ, String fid, Class<?> cls, 
                                   String fname, int num, FieldFmt [] fmt ) {
    return addField( typ, fid, cls, fname, num, fmt, false );
  }

  private static void flushTokens( Scanner s ) {
    while( s.hasNext() && !EOT.equals( s.next() ) );
  }

  private static boolean decodeMessage( Scanner s, Object dst, FieldFmt [] fmt){
    try {
      for( String tok = s.next(); !tok.equals( EOS ); tok = s.next() ) {
        int i;
  
        for( i = 0; i < fmt.length; i++ ) {
          if( tok.equals( fmt[i].fld_id ) ) {
            break;
          }
        }
  
        if( i == fmt.length ) {
          flushTokens( s );
          return false;
        }
  
        int num = fmt[i].num_elems;
        Object arr = null;
        if( fmt[i].num_elems < 0 ) {
          num = fmt[i - 1].fld.getInt( dst );
          arr = Array.newInstance(fmt[i].fld.getType().getComponentType(), num);
          fmt[i].fld.set( dst, arr );
        } else if( num > 1 ) {
          arr = fmt[i].fld.get( dst );
        }

        switch( fmt[i].type ) {
        case Type.SINT16:
          if( arr == null ) {
            fmt[i].fld.setShort( dst, s.nextShort() );
          } else {
            for( int j = 0; j < num; j++ ) {
              Array.set( arr, j, s.nextShort() );
            }
          }
          break;
        case Type.UINT32:
        case Type.COORD:
          if( arr == null ) {
            fmt[i].fld.setInt( dst, s.nextInt() );
          } else {
            for( int j = 0; j < num; j++ ) {
              Array.set( arr, j, s.nextInt() );
            }
          }
          break;
        case Type.VECT:
          if( arr == null ) {
            fmt[i].fld.set( dst, new Point( s.nextInt(), s.nextInt() ) );
          } else {
            for( int j = 0; j < num; j++ ) {
              Array.set( arr, j, new Point( s.nextInt(), s.nextInt() ) );
            }
          }
          break;
        case Type.ANGLE:
          if( arr == null ) {
            fmt[i].fld.setDouble( dst, s.nextDouble() );
          } else {
            for( int j = 0; j < num; j++ ) {
              Array.set( arr, j, s.nextDouble() );
            }
          }
          break;
        case Type.BYTE:
          if( arr == null ) {
            fmt[i].fld.setByte( dst, s.nextByte() );
          } else {
            for( int j = 0; j < num; j++ ) {
              Array.set( arr, j, s.nextByte() );
            }
          }
          break;
        case Type.STRUCT:
          if( arr == null ) {
            Object o = fmt[i].fld.get( dst );
            if( o == null ) {
              o = fmt[i].fld.getType().newInstance();
              fmt[i].fld.set( dst, o );
            }

            if( !decodeMessage( s, o, fmt[i].st_fmt ) ) {
              return false;
            }
          } else {
            for( int j = 0; j < num; j++ ) {
              Object o = Array.get( arr, j );
              if( o == null ) {
                o = fmt[i].fld.getType().getComponentType().newInstance();
                Array.set( arr, j, o );
              }
        
              if( !decodeMessage( s, o, fmt[i].st_fmt ) ) {
                return false;
              }
            }
          }
          break;
        } 
 
        if( !FLD_SEP.equals( s.next() ) ) {
          flushTokens( s );
          return false;
        }
      }
    } catch ( NoSuchElementException | IllegalStateException e ) {
      flushTokens( s );
      return false;
    } catch (  IllegalAccessException | InstantiationException e ) {
      e.printStackTrace();
      System.exit( 1 );
    }

    return true;  
  }

  public static boolean readData( Scanner s, Object dst, FieldFmt [] fmt ) {
     if( !decodeMessage( s, dst, fmt ) ) {
      return false;
    } else if( !EOT.equals( s.next() ) ) {
      flushTokens( s );
      return false;
    }
    return true;
  }

  private static boolean encodeMessage( PrintStream s, Object src, 
                                        FieldFmt [] fmt ) {
    try {
      for( int i = 0; i < fmt.length; i++ ) {
        if( fmt[i].deprecated ) {
          continue;
        }

        s.print( fmt[i].fld_id + " " );

        Object arr = null;
        int num = fmt[i].num_elems;
        if( num != 1 ) {
          if( num < 0 ) {
            num = fmt[i - 1].fld.getInt( src );
          }

          arr = fmt[i].fld.get( src );
          if( arr == null ) {
            arr = Array.newInstance( fmt[i].fld.getType().getComponentType(), 
                                     num );
            fmt[i].fld.set( src, arr );
          }
        } 

        switch( fmt[i].type ) {
        case Type.SINT16:
          if( arr == null ) {
            s.print( fmt[i].fld.getShort( src ) + " " );
          } else {
            for( int j = 0; j < num; j++ ) {
              s.print( Array.getShort( arr, j ) + " " );
            }
          }
          break;
        case Type.UINT32:
        case Type.COORD:
          if( arr == null ) {
            s.print( fmt[i].fld.getInt( src ) + " " );
          } else {
            for( int j = 0; j < num; j++ ) {
              s.print( Array.getInt( arr, j ) + " " );
            }
          }
          break;
        case Type.VECT:
          if( arr == null ) {
            Point p = (Point) fmt[i].fld.get( src );
            s.print( p.x + " " + p.y + " " );
          } else {
            for( int j = 0; j < num; j++ ) {
              Point p = (Point) Array.get( arr, j );
              s.print( p.x + " " + p.y + " " );
            }
          }
          break;
        case Type.ANGLE:
          if( arr == null ) {
            s.print( fmt[i].fld.getDouble( src ) + " " );
          } else {
            for( int j = 0; j < num; j++ ) {
              s.print( Array.getDouble( arr, j ) + " " );
            }
          }
          break;
        case Type.BYTE:
          if( arr == null ) {
            s.print( fmt[i].fld.getByte( src ) + " " );
          } else {
            for( int j = 0; j < num; j++ ) {
              s.print( Array.getByte( arr, j ) + " " );
            }
          }
          break;
        case Type.STRUCT:
          if( arr == null ) {
            Object o = fmt[i].fld.get( src );
            if( !encodeMessage( s, o, fmt[i].st_fmt ) ) {
              return false;
            }
          } else {
            for( int j = 0; j < num; j++ ) {
              Object o = Array.get( arr, j );
              if( !encodeMessage( s, o, fmt[i].st_fmt ) ) {
                return false;
              }
            }
          }
          break;
        } 
  
        s.print( FLD_SEP + " " );
      } 
      s.print( EOS + " " );
    } catch (  IllegalAccessException e ) {
      e.printStackTrace();
      System.exit( 1 );
    }

    return true;  
  }

  public static boolean writeData( PrintStream s, Object src, 
                                     FieldFmt [] fmt ) {

    if( encodeMessage( s, src, fmt ) ) {
      s.println( EOT );
      return true;
    }
    return false;
  }
}

