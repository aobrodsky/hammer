import java.io.*;
import java.net.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.*;

public class ArtifactCheckPoint extends CommonData.EnvObject {
  public final Point [] bb = { new Point(), new Point() };
  public final Point handlePos = new Point();
  public Point curHandle;
  public Artifact artifact;

  public ArtifactCheckPoint( Artifact a ) {
    artifact = a;
    pos.setLocation( a.pos );
    org.setLocation( a.org );
    typ = a.typ;
    dir = a.dir;
    clr = a.clr;
    bb[0].setLocation( a.bb[0] );
    bb[1].setLocation( a.bb[1] );
    curHandle = a.curHandle;
    if( curHandle != null ) {
      handlePos.setLocation( curHandle );
    }
  }

  public void revertFromCheckPoint() {
    revertFromCheckPoint( artifact );
  }

  public void revertFromCheckPoint( Artifact a ) {
    a.pos.setLocation( pos );
    a.org.setLocation( org );
    a.typ = typ;
    a.dir = dir;
    a.clr = clr;
    a.bb[0].setLocation( bb[0] );
    a.bb[1].setLocation( bb[1] );
    a.curHandle = curHandle;
    if( curHandle != null ) {
      curHandle.setLocation( handlePos );
    }
  }

  public boolean useful() {
    return !pos.equals( artifact.pos ) || 
           !org.equals( artifact.org ) ||
           ( typ != artifact.typ ) || 
           ( dir != artifact.dir ) || 
           ( clr != artifact.clr ) || 
           !bb[0].equals( artifact.bb[0] ) || 
           !bb[1].equals( artifact.bb[1] ) ||
           ( curHandle != artifact.curHandle ) ||
           ( ( curHandle != null ) && !handlePos.equals( curHandle ) );
  }
}
