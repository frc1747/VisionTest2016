package test;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;

import javax.swing.JFrame;

import org.opencv.core.*;
import org.opencv.videoio.*;
import org.opencv.imgcodecs.*;

public class Test1 {

	HashMap<Integer,JFrame> frames;
	
	public Test1() {
		frames = new HashMap<Integer,JFrame>();
	}
	
	public BufferedImage convert(Mat image) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if(image.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage img = new BufferedImage(image.cols(),image.rows(),type);
		image.get(0, 0,((DataBufferByte)img.getRaster().getDataBuffer()).getData());
		return img;
	}

    public JFrame show(BufferedImage image, int index) {
    	JFrame frame;
    	if(frames.containsKey(index)) {
    		frame = frames.get(index);
    	} else {
    		frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new ImagePanel(image));
    	}
        frame.pack();
        frame.setVisible(true);
        return frame;
    }	
    
    public void close(int index) {
    	if(frames.containsKey(index)) {
    		frames.get(index).dispose();
    	}
    }
    
	public void test() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture vcap = new VideoCapture(0);
		if(vcap == null) {
			System.out.println("VCAP is NULL");
			System.exit(0);
		}
		Mat image = new Mat();
		int cnt = 100;
		while(cnt-- > 0) {
			vcap.read(image);
			show(convert(image),0);
			
		}
	}
	
	public static void main(String[] args) {
		new Test1().test();
	}
	
}
