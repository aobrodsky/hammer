import java.util.prefs.Preferences;

public class AppPrefs {
  private Preferences prefs = Preferences.userRoot().node( "HammerApp" );
  private static String HOSTLIST_ID = "HostList";
  private static String HOSTIDX_ID = "HostIdx";
  private static String USEPROXY_ID = "UseProxy";
  private static String LASTPORT_ID = "LastPort";
  private static String LASTDIR_ID = "LastDir";
  private static String LASTID_ID = "LastID";
  private static String LASTPASS_ID = "LastPasswd";
  private static String LASTPROXYPORT_ID = "LastProxyPort";
  private static String LASTREMOTEPORT_ID = "LastRemotePort";
  private static String LASTLIBDIR_ID = "LastLibDir";

  private static String defHosts = "exa.cs.dal.ca,localhost";
  private static String defDir = ".";
  private static String defLibDir = ".";
  private static int    defHostIdx = 0;
  private static int    defPort = 33334;
  private static int    defProxyPort = 33333;
  private static int    defRemotePort = 34333;
  private static String defPasswd = "0,0";
  private static String defID = "1";
  private static boolean defUseProxy = false;

  private String hostList;
  private int    hostIdx;
  private int    lastPort;
  private int    lastProxyPort;
  private int    lastRemotePort;
  private String lastID;
  private String lastPasswd;
  private String lastDir;
  private String lastLibDir;
  private boolean useProxy;
  
  public final static AppPrefs appPrefs = new AppPrefs();

  public AppPrefs() { }

  public String [] getHostList() {
    hostList = prefs.get( HOSTLIST_ID, defHosts );
    return hostList.split( "," );
  }

  public void setHostList( String [] hosts ) {
    String h = null;
    for( String s : hosts ) {
      if( h == null ) {
        h = s;
      } else {
        h = h + "," + s;
      }
    }
    if( ( h != null ) && !h.equals( hostList ) ) {
      hostList = h;
      prefs.put( HOSTLIST_ID, h );
    }
  }

  public int getHostIdx() {
    hostIdx = prefs.getInt( HOSTIDX_ID, defHostIdx );
    return hostIdx;
  }

  public void setHostIdx( int idx ) {
    if( idx != hostIdx ) {
      hostIdx = idx;
      prefs.putInt( HOSTIDX_ID, idx );
    }
  }

  public int getLastPort() {
    lastPort = prefs.getInt( LASTPORT_ID, defPort );
    return lastPort;
  }

  public int getLastProxyPort() {
    lastProxyPort = prefs.getInt( LASTPROXYPORT_ID, defProxyPort );
    return lastProxyPort;
  }

  public int getLastRemotePort() {
    lastRemotePort = prefs.getInt( LASTREMOTEPORT_ID, defRemotePort );
    return lastRemotePort;
  }

  public void setLastPort( int port ) {
    if( port != lastPort ) {
      lastPort = port;
      prefs.putInt( LASTPORT_ID, port );
    }
  }

  public void setLastProxyPort( int port ) {
    if( port != lastProxyPort ) {
      lastProxyPort = port;
      prefs.putInt( LASTPROXYPORT_ID, port );
    }
  }

  public void setLastRemotePort( int port ) {
    if( port != lastRemotePort ) {
      lastRemotePort = port;
      prefs.putInt( LASTREMOTEPORT_ID, port );
    }
  }

  public String getLastID() {
    lastID = prefs.get( LASTID_ID, defID );
    return lastID;
  }

  public void setLastID( String id ) {
    if( !id.equals( lastID ) ) {
      lastID = id;
      prefs.put( LASTID_ID, id );
    }
  }

  public String [] getLastPasswd() {
    lastPasswd = prefs.get( LASTPASS_ID, defPasswd );
    return lastPasswd.split( "," );
  }

  public void setLastPasswd( String pw0, String pw1 ) {
    String pw = pw0 + "," + pw1;
    if( !pw.equals( lastPasswd ) ) {
      lastPasswd = pw;
      prefs.put( LASTPASS_ID, lastPasswd );
    }
  }

  public String getLastDir() {
    lastDir = prefs.get( LASTDIR_ID, defDir );
    return lastDir;
  }

  public void setLastDir( String dir ) {
    if( ( dir != null )  && !dir.equals( lastDir ) ) {
      lastDir = dir;
      prefs.put( LASTDIR_ID, lastDir );
    }
  }

  public String getLastLibDir() {
    lastLibDir = prefs.get( LASTLIBDIR_ID, defLibDir );
    return lastLibDir;
  }

  public void setLastLibDir( String dir ) {
    if( ( dir != null )  && !dir.equals( lastLibDir ) ) {
      lastLibDir = dir;
      prefs.put( LASTLIBDIR_ID, lastLibDir );
    }
  }

  public boolean getUseProxy() {
    useProxy = prefs.getBoolean( USEPROXY_ID, defUseProxy );
    return useProxy;
  }
}
