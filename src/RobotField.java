import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class RobotField extends JComboBox<RobotArtifact> {
   private class RobotModelNameRenderer extends DefaultListCellRenderer {
      public Component getListCellRendererComponent( JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus ) {

      JLabel label = (JLabel) super.getListCellRendererComponent( list, value,
                                            index, isSelected, cellHasFocus );
      RobotArtifact r = (RobotArtifact) value;
      if( !r.isSupported() ) {
        label.setForeground( Color.RED );
      }
      return label;
    }
  }


  public RobotField( ComboBoxModel<RobotArtifact> model ) {
    super( model );
    setEditable( false );
    setRenderer( new RobotModelNameRenderer() );

    Dimension d = getPreferredSize();
    d.width = 150;
    setMaximumSize( d );

    if( model.getSize() < 2 ) {
      setVisible( false );
    }
  }

  public RobotArtifact getSelectedRobot() {
    return (RobotArtifact) getSelectedItem();
  }

  public void setType( short typ ) {
    RobotArtifact s = (RobotArtifact) getSelectedItem();
    if( s.typ != typ ) {
      ComboBoxModel<RobotArtifact> model = getModel();
      int size = model.getSize();
      for( int i = 0; i < size; i++ ) {
        RobotArtifact r = model.getElementAt( i );
        if( r.typ == typ ) {
          setSelectedIndex( i );
          repaint();
          break;
        }
      }
    }
  }

  public void contentsChanged( ListDataEvent e ) {
    super.contentsChanged( e );
    if( getModel().getSize() == 2 ) {
      setVisible( true );
    }
  }

  public void intervalAdded( ListDataEvent e ) {
    super.intervalAdded( e );
    if( getModel().getSize() == 2 ) {
      setVisible( true );
    }
  }
}
