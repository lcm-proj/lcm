

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Tries to reproduce a bug related to a translate and rotate operation on a
 * GraphcisContext. If successful, you will see the String "Hello World" not
 * only painted on the left side but also on the right edge of the panel.
 * <p>
 * 
 * @author Achim Westermann
 * 
 */
public class AffineTransformBug extends JPanel {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 700262412146835398L;

  /**
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    String title = "Hello World";

    int titleStartX = 20;
    int titleStartY = this.getHeight() / 2;

    // store former transform for later restore:
    AffineTransform tr = g2d.getTransform();
    AffineTransform at = g2d.getDeviceConfiguration().getDefaultTransform();
    at.translate(titleStartX, titleStartY);
    g2d.setTransform(at);
    g2d.drawString(title, 0, 0);
    g2d.setTransform(tr);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame(AffineTransform.class.getName());
    frame.getContentPane().add(new AffineTransformBug());
    frame.setSize(new Dimension(400, 400));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    System.out.println("Going to resize the window to xy in 1 seconds...");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    frame.setSize(new Dimension(433, 400));
    System.out.println("Resize done!");
    System.out.println("Going to resize the window to xy in 1 seconds...");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    frame.setSize(new Dimension(450, 400));
    System.out.println("Resize done!");
  }

}
