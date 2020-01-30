import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class LogPanel extends JScrollPane {
  private JTextArea log = new JTextArea( "Log Area\n", 10, 20 );

  public LogPanel() {
    super();
    setViewportView( log );

    log.setLineWrap( true );
    setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
    new Logger( log ).start();
    System.out.println( "Logger enabled" );
  }
}
