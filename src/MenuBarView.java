import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class MenuBarView extends JMenuBar {
  final static String SAVE = "Save";
  final static String SAVEAS = "Save As";
  final static String OPEN = "Open";
  final static String NEW = "New";
  final static String RESET = "Reset Arena";
  final static String ROBOT = "Load Robot";
  final static String PRINT = "Print";
  final static String QUIT = "Quit";

  final static String UNDO = "Undo";
  final static String REDO = "Redo";
  final static String COPY = "Copy";
  final static String PASTE = "Paste";
  final static String DELETE = "Delete";
  final static String SELECTALL = "Select All";
  final static String GROUP = "Group";
  final static String UNGROUP = "Ungroup";
  final static String CONVERT = "Convert to Polygon";

  final static String ZOOM_IN = "Zoom In";
  final static String ZOOM_OUT = "Zoom Out";
  final static String RECONNECT = "Reset Connection";

  final static String LIB_SELECT = "Select from Library";
  final static String LIB_ADD = "Add to Library";
  final static String LIB_NEW = "Create Library";
  final static String LIB_LOAD = "Load Library";

  public final static String INSERT_BLOCK = "Block";
  public final static String INSERT_PATH = "Path";
  public final static String INSERT_POLYGON = "Polygon";
  public final static String INSERT_ROBOT = "Robot";

  public final static String LOG_PANEL = "Log";

  private final static int menuShortcutKeyMask = 
                           Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  private JMenu fileMenu = new JMenu( "File" );
  private JMenu editMenu = new JMenu( "Edit" );
  private JMenu insertMenu = new JMenu( "Insert" );
  private JMenu viewMenu = new JMenu( "View" );
  private JMenu libMenu = new JMenu( "Library" );
  private JMenu winMenu = new JMenu( "Window" );
  private Env env;
  protected JMenuItem quitMenuItem;

  public MenuBarView( Env e ) {
    env = e;

    makeMenuItem( fileMenu, NEW, KeyEvent.VK_N, 0, true );
    makeMenuItem( fileMenu, OPEN, KeyEvent.VK_O, 0, true );
    makeMenuItem( fileMenu, SAVE, KeyEvent.VK_S, 0, true );
    makeMenuItem(fileMenu, SAVEAS, KeyEvent.VK_S, ActionEvent.SHIFT_MASK, true);
    fileMenu.addSeparator();
    makeMenuItem( fileMenu, ROBOT, 0, 0, true );
    fileMenu.addSeparator();
    makeMenuItem( fileMenu, PRINT, KeyEvent.VK_P, 0, true );
    fileMenu.addSeparator();
    quitMenuItem = makeMenuItem( fileMenu, QUIT, KeyEvent.VK_Q, 0, true );
    add( fileMenu );

    makeMenuItem( editMenu, UNDO, KeyEvent.VK_Z, 0, false );
    makeMenuItem( editMenu, REDO, KeyEvent.VK_Y, 0, false );
    editMenu.addSeparator();
    makeMenuItem( editMenu, COPY, KeyEvent.VK_C, 0, true );
    makeMenuItem( editMenu, PASTE, KeyEvent.VK_V, 0, false );
    JMenuItem del = makeMenuItem( editMenu, DELETE, KeyEvent.VK_X, 0, true );
    editMenu.addSeparator();
    makeMenuItem( editMenu, SELECTALL, KeyEvent.VK_A, 0, true );
    editMenu.addSeparator();
    makeMenuItem( editMenu, GROUP, KeyEvent.VK_G, ActionEvent.ALT_MASK, false );
    makeMenuItem( editMenu, UNGROUP, KeyEvent.VK_G, 
                  ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK, false );
    editMenu.addSeparator();
    makeMenuItem( editMenu, CONVERT, 0, 0, false );
    editMenu.addSeparator();
    makeMenuItem( editMenu, RESET, KeyEvent.VK_R, 0, true );
    add( editMenu );

    InputMap im = del.getInputMap( WHEN_IN_FOCUSED_WINDOW );
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), DELETE );
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_BACK_SPACE, 0 ), DELETE );
    del.getActionMap().put( DELETE, new MenuItemAction( del ) );

    makeMenuItem( insertMenu, INSERT_BLOCK, KeyEvent.VK_B, 0, true );
    makeMenuItem( insertMenu, INSERT_PATH, KeyEvent.VK_L, 0, true );
    makeMenuItem( insertMenu, INSERT_POLYGON, KeyEvent.VK_G, 0, true );
    makeMenuItem( insertMenu, INSERT_ROBOT, KeyEvent.VK_T, 0, true );
    add( insertMenu );

    makeMenuItem( viewMenu, RECONNECT, KeyEvent.VK_R, ActionEvent.SHIFT_MASK, 
                  true );
    viewMenu.addSeparator();
    JMenuItem zoom = makeMenuItem(viewMenu, ZOOM_IN, KeyEvent.VK_PLUS, 0, true);
    makeMenuItem( viewMenu, ZOOM_OUT, KeyEvent.VK_MINUS, 0, true );
    add( viewMenu );
    
    im = zoom.getInputMap( WHEN_IN_FOCUSED_WINDOW );
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_EQUALS, menuShortcutKeyMask ), 
            ZOOM_IN );
    im.put( KeyStroke.getKeyStroke( KeyEvent.VK_EQUALS, 
                      ActionEvent.SHIFT_MASK + menuShortcutKeyMask ), ZOOM_IN );
    zoom.getActionMap().put( ZOOM_IN, new MenuItemAction( zoom ) );

    makeMenuItem( libMenu, LIB_SELECT, 0, 0, false );
    makeMenuItem( libMenu, LIB_ADD, 0, 0, false );
    libMenu.addSeparator();
    makeMenuItem( libMenu, LIB_NEW, 0, 0, true );
    makeMenuItem( libMenu, LIB_LOAD, 0, 0, true );
    add( libMenu );

    makeCheckMenuItem( winMenu, LOG_PANEL, KeyEvent.VK_1, 0, true );
    add( winMenu );
  }

  private JMenuItem makeCheckMenuItem( JMenu m, String cmd, int mn, int mod, 
                                  boolean e ) {
    JMenuItem i = new JCheckBoxMenuItem( cmd, false );
    m.add( i );
    i.setMnemonic( mn );
    i.setActionCommand( cmd );
    i.setEnabled( e );
    if( mn != 0 ) {
      i.setAccelerator( KeyStroke.getKeyStroke(mn, menuShortcutKeyMask + mod) );
    }
    return i;
  }

  private JMenuItem makeMenuItem( JMenu m, String cmd, int mn, int mod, 
                                  boolean e ) {
    JMenuItem i = new JMenuItem( cmd, mn );
    m.add( i );
    i.setActionCommand( cmd );
    i.setEnabled( e );
    if( mn != 0 ) {
      i.setAccelerator( KeyStroke.getKeyStroke(mn, menuShortcutKeyMask + mod) );
    }
    return i;
  }

  public void addMenuListener( JMenu menu, ActionListener a ) {
    for( Component c : menu.getMenuComponents() ) {
      if( c instanceof JMenuItem ) {
        JMenuItem m = (JMenuItem) c;
        m.addActionListener( a );
      }
    }
  }

  public void addEditMenuListener( ActionListener a ) {
    addMenuListener( editMenu, a );
  }

  public void addInsertMenuListener( ActionListener a ) {
    addMenuListener( insertMenu, a );
  }

  public void addViewMenuListener( ActionListener a ) {
    addMenuListener( viewMenu, a );
  }

  public void addFileMenuListener( ActionListener a ) {
    addMenuListener( fileMenu, a );
  }
  
  public void addLibraryMenuListener( ActionListener a ) {
    addMenuListener( libMenu, a );
  }
  
  public void addWindowMenuListener( ActionListener a ) {
    addMenuListener( winMenu, a );
  }
  
  public void setMenuItemEnabled( JMenu menu, String cmd, boolean b ) {
    for( Component c : menu.getMenuComponents() ) {
      if( c instanceof JMenuItem ) {
        JMenuItem m = (JMenuItem) c;
        if( cmd.equals( m.getActionCommand() ) ) {
          m.setEnabled( b );
          break;
        }
      }
    }
  }

  public void setFileMenuItemEnabled( String cmd, boolean b ) {
    setMenuItemEnabled( fileMenu, cmd, b );
  }

  public void setEditMenuItemEnabled( String cmd, boolean b ) {
    setMenuItemEnabled( editMenu, cmd, b );
  }

  public void setLibraryMenuItemEnabled( String cmd, boolean b ) {
    setMenuItemEnabled( libMenu, cmd, b );
  }

  public void doQuit() {
    quitMenuItem.doClick();
  }

  private class MenuItemAction extends AbstractAction {
    private JMenuItem menuItem;

    public MenuItemAction( JMenuItem item ) {
      menuItem = item;
    }

    public void actionPerformed( ActionEvent e ) {
      menuItem.doClick();
    }
  }
}
