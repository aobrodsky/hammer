import java.awt.*;
import javax.swing.*;

public class FieldGridLayout extends GridBagLayout {
  private JPanel parent;
  private final GridBagConstraints constraints = new GridBagConstraints();

  public FieldGridLayout( JPanel p ) {
    super();
    parent = p;
    constraints.weightx = 1.0;
    constraints.anchor = GridBagConstraints.WEST;
  }

  public JLabel addLabel( JLabel label ) {
    constraints.gridwidth = 1;
    JLabel fluff = new JLabel( "" );
    setConstraints( fluff, constraints );
    parent.add( fluff );
    setConstraints( label, constraints );
    parent.add( label );
    return label;
  }

  public JComponent addField( JComponent field ) {
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    setConstraints( field, constraints );
    parent.add( field );
    return field;
  }
}
