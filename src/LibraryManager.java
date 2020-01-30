import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.AffineTransform;

public class LibraryManager {
  private Env env;
  private Library library = new Library();
  private LibraryDialog libraryDialog = new LibraryDialog( library );

  public LibraryManager( Env e ) {
    env = e;
  }

  public boolean loadLibrary( File f ) {
    boolean rc = true;
    try {
      Scanner s = new Scanner( f );
      library.newLibrary( s.nextLine().trim(), f );
      while( s.hasNext() ) {
        LibraryObject lo = new LibraryObject( s.nextLine().trim() );
        EnvIO eio = env.getEnvIO();
        if( eio.readComponent( s, lo ) ) { 
          library.add( lo ); 
        } else {
          rc = false;
        }
        s.nextLine();
      }
      s.close();
    } catch ( FileNotFoundException e ) { 
      rc = false;
    }
    return rc;
  }

  public boolean newLibrary( File f ) {
    boolean rc = true;
    try {
      String name = libraryDialog.inputNewLibraryName();
      if( ( name != null ) && library.newLibrary( name, f ) ) {
        PrintStream p = new PrintStream( f );
        p.println( name );
        p.close();
      } else {
        rc = false;
      }
    } catch ( FileNotFoundException e ) { 
      rc = false;
    }
    return rc;
  }

  public boolean addArtifact( Artifact a ) {
    GroupArtifact g;
    if( a instanceof GroupArtifact ) {
      g = new GroupArtifact( (GroupArtifact)a );
    } else {
      g = new GroupArtifact( (ArtifactListener)null );
      a = a.deepCopy();
      g.addArtifact( a );
    }
    g.group();

    boolean rc = false;
    String name = libraryDialog.selectLibraryAndName();
    if( name != null ) {
      File f = library.getCurFile();
      LibraryObject l = new LibraryObject( name, g );
      try {
        PrintStream p = new PrintStream( new FileOutputStream( f, true ) ); 
        p.println( name );
        rc = l.writeModel( p );
        p.close();
        library.add( l );
      } catch ( FileNotFoundException e ) { 
        rc = false;
      }
    }
    return rc;
  }

  public boolean selectArtifact() {
    GroupArtifact g = libraryDialog.selectArtifact();
 
    if( g != null ) {
      env.newLibraryObject( g );
    }
    return g != null;
  }

  public boolean isEmpty() {
    return library.isEmpty();
  }
}
