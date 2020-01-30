import java.util.*;

public class GroupFactory {
  private ArtifactListener groupListener;

  public GroupFactory( ArtifactListener gl ) {
    groupListener = gl;
  }

  public static CommonData.GroupList getGroupList( Vector<GroupArtifact> list ){
    CommonData.GroupList gl = new CommonData.GroupList();
    gl.num = list.size();
    gl.groups = new CommonData.GroupObj[gl.num];
    short i = 0;

    for( GroupArtifact g : list ) {
      CommonData.GroupObj go = new CommonData.GroupObj();
      gl.groups[i] = go;
      i++;
      g.groupId = (short)-i;

      go.num = g.group.size();
      go.objs = new short[go.num];
      go.id = g.groupId;
      go.pos.setLocation( g.pos );
      go.dir = g.dir;
      go.bbox[0].setLocation( g.bb[0] );
      go.bbox[1].setLocation( g.bb[1] );
      int j = 0;

      for( Artifact a : g.group ) {
        go.objs[j] = a.groupId;
        j++;
      }      
    }

   return gl;
  }

  public boolean buildGroups( CommonData.GroupList list, EnvModel model ) {
    boolean rc = true;

    for( int i = 0; i < list.num; i++ ) {
      CommonData.GroupObj go = list.groups[i];
      GroupArtifact ga = new GroupArtifact( groupListener );
      ga.pos.setLocation( go.pos );
      ga.dir = go.dir;
      ga.bb[0].setLocation( go.bbox[0] );
      ga.bb[1].setLocation( go.bbox[1] );

      for( short idx : go.objs ) {
        if( idx >= 0 ) {
          Artifact a = model.getArtifact( idx );
          if( a != null ) {
            ga.addArtifact( a );
            a.groupPos.setLocation( a.pos );
            // Adjust original position to original (0) orientation
            Artifact.transform( a.groupPos, -ga.dir, ga.pos, null, a.groupPos );
          } else {
            rc = false;
          }
        } else {
          if( -idx <= list.num ) {
            ga.addArtifact( model.getGroup( -idx - 1 ) );
          } else {
            rc = false;
          }
        }
      }
      ga.updateCache( true );
      model.insertGroup( ga );
    }
    return rc;
  }
}
