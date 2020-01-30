import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class PrefPanel extends JPanel {
  public PrefPanel( String title ) {
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
    setBorder( BorderFactory.createCompoundBorder( 
                          BorderFactory.createLineBorder( Color.BLACK ),
                          BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );

    JPanel flow = new JPanel();
    flow.setLayout( new BoxLayout( flow, BoxLayout.X_AXIS ) );
    super.add( flow );

    flow.add( new JLabel( "<html><b><u>" + title ) );
    flow.add( Box.createHorizontalGlue() );
    JToolBar t = new JToolBar();
    t.setFloatable( false );
    flow.add( t );
    JButton b = new JButton( "X" );
    t.add( b );

    b.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent e ) {
        setVisible( false );
      }
    } );

    super.setVisible( false );
    HammerController.addRightPanel( this );
  }

  @Override
  public Component add( Component c ) {
    JPanel flow = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    flow.add( c );
    super.add( flow );
    return c;
  }

  @Override 
  public void setVisible( boolean visible ) {
    super.setVisible( visible );
    HammerController.revalidateRightPanel();
  }
}
