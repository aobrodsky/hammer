import java.awt.*;
import javax.swing.*;

public class NumberSpinner extends JSpinner {
  private String idKey;

  private static class SpinnerCircularNumberModel extends SpinnerNumberModel {
    SpinnerCircularNumberModel( int init, int min, int max, int step ) {
      super( init, min, max, step );
    }
 
    public Object getNextValue() {
      Object o = super.getNextValue();
      if( o == null ) {
        o = getMinimum();
        setValue( o );
      }
      return o;
    }

    public Object getPreviousValue() {
      Object o = super.getPreviousValue();
      if( o == null ) {
        o = getMaximum();
        setValue( o );
      }
      return o;
    }
  }

  public NumberSpinner( int init, int min, int max, int width, String key ) {
    super( new SpinnerCircularNumberModel( init + 1, min, max, 1 ) ); 
    idKey = key;
    JSpinner.NumberEditor e = (JSpinner.NumberEditor) getEditor();
    Dimension d = e.getPreferredSize();
    e.setPreferredSize( new Dimension( width, d.height ) );
    e.getFormat().setGroupingUsed( false );
    setValue( init );
    
  }

  public String getKey() {
    return idKey;
  }
}
