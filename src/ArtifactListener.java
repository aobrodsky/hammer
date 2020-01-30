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

public interface ArtifactListener {
  public void artifactChanged( Artifact a, ArtifactCheckPoint acp );
  public void artifactCompleted( Artifact a );
  public void artifactRemoved( Artifact a );
  public boolean checkLocation( Artifact a );
}
