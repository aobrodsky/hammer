import java.io.*;
import javax.swing.*;
import java.util.*;

public class Logger extends Thread {
  Scanner sc;
  JTextArea log;

  private boolean init() {
    try {
      PipedInputStream sink = new PipedInputStream();
      System.setOut( new PrintStream( new PipedOutputStream( sink ), true ) );
      sc = new Scanner( sink );
    } catch ( Exception e ) {
      log.append( "Logger died: " + e.toString() + "\n" );
      log.setCaretPosition( log.getDocument().getLength() );
      return false;
    }
    return true;
  }

  public Logger( JTextArea ta ) {
    log = ta;
    init();
  }

  public void run() {
    for( ;; ) {
      try {
        log.append( sc.nextLine() + "\n");
        log.setCaretPosition( log.getDocument().getLength() );
      } catch ( Exception e ) {
        if( !init() ) {
          break;
        }
      }
    }
  }
}
