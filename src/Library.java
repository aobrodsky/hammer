import java.util.*;
import java.io.*;

public class Library {
  private boolean modified = false;
  private Shelf current;
  private Hashtable<String,Shelf> collection = new Hashtable<String,Shelf>();

  private class Shelf {
    private boolean modified = true;
    private String name;
    private File file;
    private Hashtable<String,LibraryObject> objects = 
                                          new Hashtable<String,LibraryObject>();
  
    public Shelf( String nm, File f ) {
      name = nm;
      file = f;
    }

    public GroupArtifact getArtifact( String name ) {
      LibraryObject o = objects.get( name );
      if( o != null ) {
        return o.getArtifact();
      }
      return null;
    }

    public boolean add( LibraryObject o ) {
      String name = o.getName();
      boolean rc = !objects.containsKey( name );
      if( rc ) {
        modified = true;
        objects.put( name, o );
      }
      return rc;
    }

    public Collection<LibraryObject> getArtifacts() {
      return objects.values();
    }

    public boolean isModified() {
      return modified;
    }

    public void clearModified() {
      modified = false;
    }
  }

  public boolean newLibrary( String name, File f ) { 
    boolean rc = !collection.containsKey( name );
    if( rc ) {
      current = new Shelf( name, f );
      collection.put( name, current );
      modified = true;
    }
    return rc;
  }

  public void add( LibraryObject l ) {
    if( current !=null ) {
      modified = true;
      current.add( l );
    }
  }

  public File getCurFile() {
    if( current != null ) {
      return current.file;
    }
    return null;
  }

  public String getCurName() {
    if( current != null ) {
      return current.name;
    }
    return null;
  }

  public GroupArtifact getArtifact( String name ) {
    if( ( current != null ) && ( name != null ) ) {
      return current.getArtifact( name );
    }
    return null;
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified( boolean m ) {
    modified = m;
  }

  public String [] getLibraryNames() {
    return collection.keySet().toArray( new String[0] );
  }

  public void selectLibrary( String key ) {
    Shelf s = collection.get( key );
    if( s != null ) {
      current = s;
    }
  }

  public boolean isEmpty() {
    return collection.isEmpty();
  }

  public Collection<LibraryObject> getLibraryArtifacts( String name ) {
    Shelf s = collection.get( name );
    if( s != null ) {
      return s.getArtifacts();
    }
    return null;
  }

  public boolean isModified( String name ) {
    Shelf s = collection.get( name );
    if( s != null ) {
      return s.isModified();
    }
    return false;
  }

  public void clearModified( String name ) {
    Shelf s = collection.get( name );
    if( s != null ) {
      s.clearModified();
    }
  }
}

