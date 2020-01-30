import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

public class LibraryDialog extends JDialog {
  private final static int iconDim = 50;

  private Library library;
  JTabbedPane collection = new JTabbedPane();
  
  private class PictureList extends JList<String> {
    private Hashtable<String,Icon> labelMap = new Hashtable<String,Icon>();

    private class IconTextListRenderer extends DefaultListCellRenderer {
      //Font font = new Font("helvitica", Font.BOLD, 24);

      public Component getListCellRendererComponent( JList list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus ) {

        JLabel label = (JLabel) super.getListCellRendererComponent( list, value,
                                              index, isSelected, cellHasFocus );
        label.setIcon( labelMap.get( (String)value) );
        label.setHorizontalTextPosition( JLabel.RIGHT );
        // label.setFont(font);
        return label;
      }
    }

    public PictureList() {
      setCellRenderer( new IconTextListRenderer() );
    }

    public void addMap( String name, Icon icn ) {
      labelMap.put( name, icn );
    }
  }
  public LibraryDialog( Library lib ) {
    library = lib;
    setContentPane( collection );
  }

  public String inputNewLibraryName() {
    String msg = "Enter new library name";
    String title = "Library Name";
    return JOptionPane.showInputDialog( null, msg, title,
                                       JOptionPane.PLAIN_MESSAGE );
  }

  public String selectLibraryAndName() {
    Object [] o = new Object[4];  
    JList<String> list = new JList<String>( library.getLibraryNames() );
    list.setSelectedValue( library.getCurName(), true );   

    o[0] = new JLabel( "Select a Library:" );
    o[1] = list;
    o[2] = new JLabel( "Enter the Artifact's Name:" );
    String name = JOptionPane.showInputDialog( null, o, "Artifact Identity",
                                               JOptionPane.PLAIN_MESSAGE );

    if( name != null ) {
       library.selectLibrary( list.getSelectedValue() );
    }
    return name;
  }

  private Icon computeIcon( LibraryObject o ) {
    GroupArtifact a = o.getArtifact();
    int width = a.bb[1].x - a.bb[0].x + 1;
    int height = a.bb[1].y - a.bb[0].y + 1;
    int max = width > height ? width : height;
    a.setLocation( new Point( max / 2 + 1, max / 2 + 1 ) );
    a.updateCache( true );

    double scale = 1;
    if( max > iconDim ) {
      scale = (double)iconDim / max;
    }

    BufferedImage bi = new BufferedImage( iconDim, iconDim,
                                          BufferedImage.TYPE_INT_ARGB );
    Graphics2D g = bi.createGraphics();
    g.translate( 0, iconDim );
    g.scale( scale,-scale );

    for( Artifact oa : o.getArtifacts() ) {
      oa.paintArtifact( g, true );
    }
    g.dispose();
          
    return new ImageIcon( bi );
  }

  private void updateDialog() {
    String [] libs = library.getLibraryNames();
    for( String name : libs ) {
      if( library.isModified( name ) ) {
        int idx = collection.indexOfTab( name );
        PictureList list = new PictureList();
        JScrollPane scroll = new JScrollPane( list );
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        if( idx < 0 ) {
          collection.addTab( name, scroll );
          idx = collection.indexOfTab( name );
        } else {
          collection.setComponentAt( idx, scroll );
        }

        Collection<LibraryObject> objs = library.getLibraryArtifacts( name );
        Vector<String> model = new Vector<String>();
        for( LibraryObject o : objs ) {
          String on = o.getName();
          model.add( on );
          list.addMap( on, computeIcon( o ) );
        }
        list.setListData( model );

        if( list.getModel().getSize() > 0 ) {
          list.setSelectedIndex( 0 );
        }
      }
    }      
  }

  public GroupArtifact selectArtifact() {
    Object [] o = new Object[2];  

    if( library.isModified() ) { 
      updateDialog();
    }
    o[0] = new JLabel( "Select artifact:" );
    o[1] = collection;
    int rc = JOptionPane.showOptionDialog( null, o, "Artifact Library",
                                               JOptionPane.OK_CANCEL_OPTION, 
                                               JOptionPane.PLAIN_MESSAGE,
                                               null, null, null );

    if( rc == JOptionPane.OK_OPTION ) {
      int idx = collection.getSelectedIndex();
      String lib = collection.getTitleAt( idx );
      JScrollPane sp = (JScrollPane) collection.getComponentAt( idx );
      JViewport vp = sp.getViewport(); 
      PictureList pl = (PictureList) vp.getView();
      library.selectLibrary( lib );
      return library.getArtifact( pl.getSelectedValue() );
    }
    return null;
  }
}
