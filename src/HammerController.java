import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class HammerController extends JFrame {
  private Env env;
  private Arena arena;
  private ConnectionPanel connectPanel;
  private ArenaInfoPanel arenaInfoPanel;
  private ObjectPanel objectPanel;
  private MenuBarView menuBarView;
  private LibraryManager libManager;
  private JPanel rightPanel;

  private static HammerController mainWindow;
  private static String currFileName;
  private static boolean fileModified = false;
  private static RobotActionPanel robotActionPanel;

  private class EnvHandler implements EnvListener {
    public boolean enabled = false;
  
    public void updateAll() {
      if( enabled ) {
        arenaInfoPanel.setArenaSize( env.getArenaSize() );
        updateArena();
      }
    }

    public void updateArena() {
      if( enabled ) {
        Artifact a = env.getCurArtifact();
        if( a != null ) {
          objectPanel.setObjectProperties( a );
        }
        arena.repaint();
      }
    }

    public void modelChanged( EnvModel e ) {
     UpdateHandler comm = connectPanel.getCommunicator();   
     comm.sendChangeArena( e );
    }

    public void stateChanged( Artifact a, StateChange typ ) {
      if( enabled ) {
        UpdateHandler comm = connectPanel.getCommunicator();
        if( comm != null ) {
          switch( typ ) {
          case USER_ACTION:
            comm.sendUserActionToRobot( (RobotArtifact) a );
            break;
          case POSITION:
            comm.sendMoveRobot( (RobotArtifact) a );
            break;
          case OBJECT:
            comm.sendChangeObject( (PhysicalArtifact) a );
            break;
          case DELETE:
            if( a instanceof RobotArtifact ) {
              comm.sendDeleteRobot( (RobotArtifact) a );
            } else {
              comm.sendDeleteObject( (PhysicalArtifact) a );
            }
            break;
          case ENVIRONMENT:
            comm.sendEnvironment();
            break;
          }
        }
      }
    }

    public void objectSelected( Artifact a ) {
      if( !enabled ) {
        return;
      }

      objectPanel.selectObjectPanel( a );

      if( a instanceof RobotArtifact ) {
        RobotArtifact t = (RobotArtifact) a;
        if( !robotActionPanel.setActionPanel( t.typ  ) ) {
          robotActionPanel.add( t.getActionPanel() );
          robotActionPanel.setActionPanel( t.typ  );
        }
        robotActionPanel.setTarget( t.getId() );
      } else {
        robotActionPanel.setTargetAll();
      }

      boolean cnvt = ( a instanceof BlockArtifact ) || 
                     ( a instanceof PathArtifact );
      boolean group = ( a instanceof ProtoGroup );
      boolean ungroup = ( a instanceof GroupArtifact );
      boolean lib = !libManager.isEmpty();
      boolean addable = !( a instanceof RobotArtifact ) && lib;

      menuBarView.setEditMenuItemEnabled( MenuBarView.CONVERT, cnvt );
      menuBarView.setEditMenuItemEnabled( MenuBarView.GROUP, group );
      menuBarView.setEditMenuItemEnabled( MenuBarView.UNGROUP, ungroup );
      menuBarView.setLibraryMenuItemEnabled( MenuBarView.LIB_ADD, addable );
      menuBarView.setLibraryMenuItemEnabled( MenuBarView.LIB_SELECT, lib );
      arena.repaint();
    }

    public void objectInstantiatedFromLibrary( Artifact a ) {
      if( enabled ) {
        arena.setTask( Task.Type.PLACE_LIBOBJ );
      }
    }

    public void setEnabled( boolean b ) {
      enabled = b;
      if( b ) {
        objectSelected( env.getCurArtifact() );
      }
    }
  }

  private class MenuButtonListener implements ActionListener {
    public void actionPerformed( ActionEvent e ) {
      env.selectArtifact( null, false );
      switch( e.getActionCommand() ) {
      case ToolBarPanel.BLOCK_BUTTON:
      case MenuBarView.INSERT_BLOCK:
        arena.setTask( Task.Type.START_BLOCK );
        env.newBlock();
        break;
      case ToolBarPanel.PATH_BUTTON:
      case MenuBarView.INSERT_PATH:
        arena.setTask( Task.Type.START_PATH );
        break;
      case ToolBarPanel.POLYGON_BUTTON:
      case MenuBarView.INSERT_POLYGON:
        arena.setTask( Task.Type.START_POLY );
        break;
      case ToolBarPanel.ROBOT_BUTTON:
      case MenuBarView.INSERT_ROBOT:
        arena.setTask( Task.Type.START_ROBOT );
        env.newRobot();
        break;
      }
    }
  }

  private class TopPanel extends JPanel {
    public TopPanel( MenuButtonListener mbl ) {
      setLayout( new BorderLayout() );
      JPanel panel = new JPanel( new BorderLayout() );
      JPanel subpanel = new JPanel( new WrapLayout( FlowLayout.LEFT ) );
      panel.add( subpanel, BorderLayout.NORTH );

      ToolBarPanel tbp = new ToolBarPanel( mbl );
      subpanel.add( tbp );

      arenaInfoPanel = new ArenaInfoPanel( env, arena );
      subpanel.add( arenaInfoPanel );

      connectPanel = new ConnectionPanel( env );
      subpanel.add( connectPanel );

      objectPanel = new ObjectPanel( env );
      panel.add( objectPanel, BorderLayout.SOUTH );

      robotActionPanel = new RobotActionPanel( env );
      add( robotActionPanel, BorderLayout.EAST );
      add( panel, BorderLayout.CENTER );
    } 
  }


  public HammerController() {
    super( "Hammer" );

    mainWindow = this;

    JPanel panel = new JPanel( new BorderLayout() );
    rightPanel = new JPanel();
    rightPanel.setLayout( new BoxLayout( rightPanel, BoxLayout.Y_AXIS ) );
    panel.add( rightPanel, BorderLayout.NORTH );
    JScrollPane jsp = new JScrollPane( panel );
    
    EnvHandler el = new EnvHandler();
    env = new Env( el );

    addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) { 
        menuBarView.doQuit();
      }
    });

    panel = new JPanel( new BorderLayout() );
    arena = new Arena( env );
    panel.add( arena.getScrollPane(), BorderLayout.CENTER );
    panel.add( jsp, BorderLayout.EAST );

    MenuButtonListener mbl = new MenuButtonListener();
    panel.add( new TopPanel( mbl ), BorderLayout.NORTH );

    add( panel, BorderLayout.CENTER );

    Dimension dim = arena.getPreferredSize();
    setSize( dim.height + 400, dim.height + 40 );
    pack();

    libManager = new LibraryManager( env );
    constructMainMenu( mbl );

    el.setEnabled( true );
    setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    setVisible( true );
  }

  private void constructMainMenu( MenuButtonListener mbl ) {
    menuBarView = new MenuBarView( env );
    setJMenuBar( menuBarView );
    menuBarView.addInsertMenuListener( mbl );
    menuBarView.addFileMenuListener( new FileMenuListener( env, arena ) );
    menuBarView.addLibraryMenuListener(new LibraryMenuListener(env,libManager));
    menuBarView.addEditMenuListener( new EditMenuListener( menuBarView, env ) );
    menuBarView.addViewMenuListener( new ViewMenuListener( menuBarView, arena, 
                                                           connectPanel ) );
    menuBarView.addWindowMenuListener( new WindowMenuListener( this ) );
  }

  public static void setCurrentFileNameInTitle( String s ) {
    if( s == null ) {
      fileModified = false;
    }
    currFileName = s;
    setFileTitle();
  }

  public static void setCurrentFileModified( boolean b ) {
    fileModified = b;
    setFileTitle();
  }

  private static void setFileTitle() {
    if( mainWindow != null ) {
        String s = "Hammer";
        if( currFileName != null ) {
          s += " (" + currFileName + ")";
        }
        if( fileModified ) {
          s += " [modified]";
        }
        mainWindow.setTitle( s );
    }
  }

  public static void addRightPanel( JComponent c ) {
    mainWindow.rightPanel.add( c );
  }

  public static void revalidateRightPanel() {
    mainWindow.revalidate();
    mainWindow.repaint();
  }
}
