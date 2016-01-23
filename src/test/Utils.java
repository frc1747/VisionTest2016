package test;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;

import javax.swing.JFrame;

import org.opencv.core.Mat;

public class Utils {
	private static HashMap<Integer, JFrame> frames = new HashMap<Integer, JFrame>();

	public static BufferedImage convert(Mat image) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (image.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage img = new BufferedImage(image.cols(), image.rows(), type);
		image.get(0, 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData());
		return img;
	}

	public static JFrame show(Mat image, int index) {
		return show(convert(image), index);
	}

	public static JFrame show(BufferedImage image, int index) {
		JFrame frame;
		if (frames.containsKey(index)) {
			frame = frames.get(index);
		} else {
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frames.put(index, frame);
		}
		frame.add(new ImagePanel(image));
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	public static void close(int index) {
		if (frames.containsKey(index)) {
			frames.get(index).dispose();
		}
	}
}