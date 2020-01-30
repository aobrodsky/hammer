import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CollapsePanel extends JPanel {
  private JPanel body;
  private JButton toggle;
  private String title;

  public CollapsePanel( String ttl, JPanel bdy ) {
    super();
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

    title = ttl;
    toggle = new JButton( "+ " + title );
    toggle.addActionListener( new ToggleListener() );
    toggle.setOpaque( false );
    toggle.setBorder( BorderFactory.createEmptyBorder() );
    toggle.setBorderPainted( false );

    JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    panel.add( toggle );
    add( panel );

    if( bdy != null ) {
      body = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      body.add( bdy );
      add( body );
      body.setVisible( false );
    }
  }

  public void setBody( JPanel p ) {
    boolean expanded = false;

    if( body != null ) {
      expanded = body.isVisible();
      remove( body );
    }
   
    if( p != null ) {
      body = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      body.add( p );
      add( body );
      body.setVisible( expanded );
    }
  }

  private class ToggleListener implements ActionListener {
    public void actionPerformed( ActionEvent e ) {
      JButton b = (JButton) e.getSource();
      if( body != null ) {
        boolean expanded = !body.isVisible();
        if( expanded ) {
          b.setText( "- " + title );
        } else {
          b.setText( "+ " + title );
        }

        body.setVisible( expanded );
        revalidate();     
        repaint();     
      }
    }
  }
}
