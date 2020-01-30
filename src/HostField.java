import javax.swing.*;

public class HostField extends JComboBox<String> {
  public HostField() {
    super( AppPrefs.appPrefs.getHostList() );
    setSelectedIndex( AppPrefs.appPrefs.getHostIdx() );
    setEditable( true );
  }

  public void rememberHost() {
    String h = (String) getSelectedItem();
    int count = getItemCount();
    String [] hosts = new String[count + 1];

    for( int i = 0; i < count; i++ ) {
      hosts[i] = (String) getItemAt( i );
      if( h.equals( hosts[i] ) ) {
        AppPrefs.appPrefs.setHostIdx( getSelectedIndex() );
        return; 
      }
    }
    addItem( h );
    hosts[count] = h;

    AppPrefs.appPrefs.setHostList( hosts );
    AppPrefs.appPrefs.setHostIdx( getSelectedIndex() );
  }

  public String getHost() {
    return (String) getSelectedItem();
  }
}
