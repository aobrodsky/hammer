import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class ArenaInfoPanel extends JToolBar implements ChangeListener {
  private static final String LENGTH_KEY = "ASL_LENGTH";
  private static final String WIDTH_KEY = "ASL_WIDTH";
  private JSpinner arenaWidth;
  private JSpinner arenaLength;
  private PercentSpinner arenaZoom;
  private Env env;

  public ArenaInfoPanel( Env e, Arena zoomListener ) {
    env = e;

    Dimension ad = e.getArenaSize();
    arenaLength = new NumberSpinner( ad.width, 0, 9999, 50, LENGTH_KEY );
    arenaWidth = new NumberSpinner( ad.height, 0, 9999, 50, WIDTH_KEY );
    arenaLength.addChangeListener( this );
    arenaWidth.addChangeListener( this );

    arenaZoom = new PercentSpinner( 1, 0, 9, null );
    arenaZoom.addChangeListener( zoomListener );
    zoomListener.setZoomSpinner( arenaZoom );
    add( new JLabel( "Zoom:" ) );
    add( arenaZoom );
    add( new JLabel( " Arena " ) );
    add( new JLabel( "L:" ) );
    add( arenaLength );
    add( new JLabel( "W:" ) );
    add( arenaWidth );
  }

  public void setArenaSize( Dimension d ) {
    arenaWidth.setValue( d.height );
    arenaLength.setValue( d.width );
  }

  public void stateChanged( ChangeEvent e ) {
    NumberSpinner s = (NumberSpinner) e.getSource();
    Dimension dim = env.getArenaSize();
    int val = (int) s.getValue();

    switch( s.getKey() ) {
    case LENGTH_KEY:
      dim.width = val;
      break;
    case WIDTH_KEY:
      dim.height = val;
      break;
    }
    env.setArenaSize( dim );
  }
}

