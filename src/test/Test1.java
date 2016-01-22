package test;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import org.opencv.core.*;
import org.opencv.videoio.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

public class Test1 {

	HashMap<Integer, JFrame> frames;

	public Test1() {
		frames = new HashMap<Integer, JFrame>();
	}

	public BufferedImage convert(Mat image) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (image.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage img = new BufferedImage(image.cols(), image.rows(), type);
		image.get(0, 0, ((DataBufferByte) img.getRaster().getDataBuffer()).getData());
		return img;
	}

	public JFrame show(BufferedImage image, int index) {
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

	public void close(int index) {
		if (frames.containsKey(index)) {
			frames.get(index).dispose();
		}
	}

	@SuppressWarnings("unused")
	public void test(boolean video) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat src = new Mat();
		if (video) {
			VideoCapture vcap = new VideoCapture(0);
			if (vcap == null) {
				System.out.println("VCAP is NULL");
				System.exit(0);
			}
			while (true) {
				vcap.read(src);
				processImage(src);
			}
		} else {
			src = Imgcodecs.imread("frame.png");
			processImage(src);
		}
	}

	public void processImage(Mat src) {
		// Look for red
		Mat ranged = new Mat();
		Scalar lowerBound = new Scalar(40, 40, 170);
		Scalar upperBound = new Scalar(120, 120, 255);
		Core.inRange(src, lowerBound, upperBound, ranged);
		// Look for rectangles
		Mat contoured = ranged.clone();
		ArrayList<MatOfPoint> pointList = new ArrayList<MatOfPoint>();
		Mat contourHierarchy = new Mat();
		Imgproc.findContours(contoured, pointList, contourHierarchy, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);
		// Find the biggest rectangle
		double maxArea = -1;
		MatOfPoint goal = new MatOfPoint();
		for (MatOfPoint testContour : pointList) {
			double area = Imgproc.contourArea(testContour);
			if (maxArea < area) {
				maxArea = area;
				goal.release();
				goal = testContour;
			} else {
				testContour.release();
			}
		}
		if (goal == null) {
			System.out.println("Goal not found");
			contoured.release();
			ranged.release();
			src.release();
			return;
		}
		// Color it
		Scalar color = new Scalar(0, 255, 0);
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		MatOfPoint2f goal2f = new MatOfPoint2f(goal.toArray());
		Imgproc.approxPolyDP(goal2f, approxCurve, Imgproc.arcLength(goal2f, true) * .04, true);
		MatOfPoint boxPoints = new MatOfPoint(approxCurve.toArray());
		Rect rect = Imgproc.boundingRect(boxPoints);
		Mat coloredBlurred = new Mat();
		Imgproc.cvtColor(ranged, coloredBlurred, Imgproc.COLOR_GRAY2BGR);
		Imgproc.rectangle(coloredBlurred, new Point(rect.x, rect.y),
				new Point(rect.x + rect.width, rect.y + rect.height), color, 10);
		// Find the top and bottom line
		ArrayList<MatOfPoint> test = new ArrayList<MatOfPoint>();
		test.add(goal);
		Imgproc.drawContours(coloredBlurred, test, 0, color, 10);
		int x1 = rect.x + rect.width / 3;
		int y1 = rect.y + rect.height - 20;
		double[] pixel;
		do {
			y1 -= 1;
			pixel = coloredBlurred.get(y1, x1);
		} while (pixel[0] + pixel[1] + pixel[2] < 75 && y1 > 0);
		//Top of box
		System.out.print((rect.y + rect.height) - y1 + "\t");
		int x2 = rect.x + rect.width * 2 / 3;
		int y2 = rect.y + rect.height - 20;
		do {
			y2 -= 1;
			pixel = coloredBlurred.get(y2, x2);
		} while (pixel[0] + pixel[1] + pixel[2] < 75 && y2 > 0);
		//Bottom of box
		System.out.println((rect.y + rect.height) - y2);
		//Slope
		System.out.println(((double) y2 - y1) / ((double) x2 - x1));
		Imgproc.line(coloredBlurred, new Point(x1, y1), new Point(x1, rect.y + rect.height), color, 10);
		Imgproc.line(coloredBlurred, new Point(x2, y2), new Point(x2, rect.y + rect.height), color, 10);
		show(convert(coloredBlurred), 0);
		coloredBlurred.release();
		boxPoints.release();
		goal2f.release();
		approxCurve.release();
		contoured.release();
		ranged.release();
		src.release();
	}

	public static void main(String[] args) {
		new Test1().test(true);
	}
}