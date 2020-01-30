import java.awt.*;

public class ProtoGroup extends GroupArtifact {
  private static ProtoGroup protoGroup;
  private static ArtifactListener groupListener;

  public static ProtoGroup newGroup( ArtifactListener gl ) {
    if( protoGroup == null ) {
      groupListener = gl;
      protoGroup = new ProtoGroup();
    }
    return protoGroup;
  }

  public static ProtoGroup getGroup() {
    return protoGroup;
  }

  private ProtoGroup() {
    super( groupListener );
    typ = CommonData.OBJ_TYP_PROTOGROUP;
  }

  private ProtoGroup( ProtoGroup pg ) {
    super( groupListener );
    typ = CommonData.OBJ_TYP_PROTOGROUP;
    unbound = pg.unbound;
    setArtifacts( pg.group );
  }

  public synchronized void paintHandles( Graphics2D g ) {
    for( Artifact a : group ) {
      a.paintHandles( g );
    }
  }

  public void insert() {
    if( this != protoGroup ) {
      super.insert();
      protoGroup.setArtifacts( group );
      remove();
    }
  }

  public void delete() {
    if( this == protoGroup ) {
      ProtoGroup pg = new ProtoGroup( this );
      clear();
      pg.delete();
    } else {
      super.delete();
    }   
  }
  
  public Artifact getRoot( Artifact a ) {
    while( ( a.parent != null ) && ( a.parent != this ) ) {
      a = a.parent;
    }
    return a;
  }

  public GroupArtifact makeGroup() {
    GroupArtifact g = null;

    if( size() > 1 ) {
      g = new GroupArtifact( group, groupListener );
      clear();
      g.group();
    }
    return g;
  }

  public Artifact removeArtifactFromGroup( Artifact a ) {
    if( removeArtifact( a ) ) {
      switch( size() ) {
      case 0: 
        return null;
      case 1: 
        a = group.remove( 0 );
        a.parent = null;
        return a;
      default:
        return this;
      }
    }
    return a;
  }        

  public Artifact addArtifact( Artifact a, Artifact prev ) {
    addArtifact( a );
    if( prev != this ) {
      addArtifact( prev );
    } 
    return this;
  }

  public boolean isGroup() {
    return this != protoGroup;
  }
}
