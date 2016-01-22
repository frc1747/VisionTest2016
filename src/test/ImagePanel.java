package test;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel(BufferedImage image) {
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
    
    public void setImage(BufferedImage image){
    	this.image=image;
    	invalidate();
    }
}