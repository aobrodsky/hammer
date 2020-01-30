import java.awt.*;

public abstract class PhysicalArtifact extends Artifact {
  static protected short idCounter = 100;

  public PhysicalArtifact( ArtifactListener al ) {
    super( al );
    id = nextId();
  }

  public PhysicalArtifact( Artifact a ) {
    super( a );
    id = nextId();
  }

  public PhysicalArtifact( CommonData.EnvObject o, ArtifactListener al ) {
    super( o, al );
    id = nextId();
  }

  public PhysicalArtifact( Point posn, Point orig, double dirn, Point [] v, 
                         short type, int col, ArtifactListener al ) {
    super( posn, orig, dirn, v, type, col, al );
    id = nextId();
  }

  protected static short nextId() {
    return idCounter++;
  }
}
