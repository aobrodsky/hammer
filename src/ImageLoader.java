import java.io.*;
import java.net.*;
import java.awt.image.*;
import javax.imageio.*;

public class ImageLoader {
  public static BufferedImage load( String s ) {
    try {
      return ImageIO.read( Hammer.class.getResource( s ));
    } catch ( Exception e ) { }

    try {
      return ImageIO.read(
                Hammer.class.getClassLoader().getResourceAsStream( s ) );
    } catch ( Exception e ) { }

    try {
      return ImageIO.read( new URL( Hammer.codeBase(), s ) );
    } catch ( Exception e ) { }

    try {
      return ImageIO.read( new File( s ) );
    } catch ( Exception e ) { }

    return null;
  }

  public static BufferedImage load( String s, Class c ) {
    try {
      return ImageIO.read( c.getResource( s ) );
    } catch ( Exception e ) { }

    try {
      return ImageIO.read( c.getClassLoader().getResourceAsStream( s ) );
    } catch ( Exception e ) { }

    return null;
  }
}
