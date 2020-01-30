import javax.swing.*;

public class PercentSpinner extends JSpinner {
  private String idKey;

  public PercentSpinner( double init, double min, double max, String key ) {
    super( new SpinnerNumberModel( init, min, max, 0.01 ) ); 
    idKey = key;
    setEditor( new JSpinner.NumberEditor( this, "###%" ) ); 
    setValue( init );
  }

  public String getKey() {
    return idKey;
  }
}
