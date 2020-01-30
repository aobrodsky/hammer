import java.util.*;

public class LibraryObject extends EnvModel {
  private String name;

  public LibraryObject( String nm ) {
    name = nm;
  }

  public LibraryObject( String nm, GroupArtifact g ) {
    name = nm;
    getObjs( g );
  }

  private void getObjs( GroupArtifact g ) {
    Vector<Artifact> gv = g.getArtifacts();
    for( Artifact a : gv ) {
      if( a instanceof GroupArtifact ) {
        getObjs( (GroupArtifact)a );
        insertGroup( (GroupArtifact)a );
      } else {
        insertArtifact( a );
      }
    }
    insertGroup( g );
  }

  public String getName() {
    return name;
  }

  public GroupArtifact getArtifact() {
    return groups.lastElement();
  }
}

