import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This is a personal extension of JPanel. I just did this so that the panel includes an image
 * @author Sam
 */
public class myJPanel extends JPanel{
    
    static final long serialVersionUID = 201606281539L;
    
    //The image to include
    public BufferedImage image;
    
    //Constructor
    myJPanel(boolean b, BufferedImage img)
    {
        super(b);
        image = img;
    }

		//The painting of the panel should also redraw the image
    @Override public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

}
