package test;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import edu.wpi.first.wpilibj.networktables.*;

public class Test4 {

	// Constants for known variables
	// the height to the top of the target in first stronghold is 97 inches
	public static final int TOP_TARGET_HEIGHT = 97;
	// the physical height of the camera lens
	// TODO: Verify this
	public static final int TOP_CAMERA_HEIGHT = 32;

	public static final double VERTICAL_FOV = 35.25;
	public static final double HORIZONTAL_FOV = 47;
	// TODO: Verify this
	public static final double CAMERA_ANGLE = 10;

	public void test() {
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1747-frc.local");
		NetworkTable networkTable = NetworkTable.getTable("imageProcessing");
		while (true) {
			String direction = processImage();
			System.out.println(direction);
			networkTable.putString("ShootDirection", direction);
		}
	}

	public String processImage() {
		Mat src = null;
		try {
			URL url = new URL("http://10.17.47.11/axis-cgi/jpg/image.cgi");
			URLConnection uc = url.openConnection();
			BufferedImage image = ImageIO.read(uc.getInputStream());
			src = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			src.put(0, 0, pixels);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Look for blue
		// Mat hsv = new Mat();
		// Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		Mat ranged = new Mat();
		Scalar lowerBound = new Scalar(0, 235, 0);
		Scalar upperBound = new Scalar(20, 255, 20);
		Core.inRange(src, lowerBound, upperBound, ranged);
		// blur image
		// Imgproc.medianBlur(ranged, ranged, 15);
		// Scalar upperThresh = new Scalar(255);
		// Scalar lowerThresh = new Scalar(200);
		// Core.inRange(ranged, lowerThresh, upperThresh, ranged);
		Utils.show(ranged, 10);
		// Look for rectangles
		Mat contoured = ranged.clone();
		ArrayList<MatOfPoint> pointList = new ArrayList<MatOfPoint>();
		Mat contourHierarchy = new Mat();
		Imgproc.findContours(contoured, pointList, contourHierarchy, Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);
		// Find the biggest rectangle
		double maxArea = -1;
		boolean foundGoal = false;
		MatOfPoint goal = new MatOfPoint();

		for (MatOfPoint testContour : pointList) {
			double area = Imgproc.contourArea(testContour);
			if (maxArea < area) {
				foundGoal = true;
				maxArea = area;
				goal.release();
				goal = testContour;
			} else {
				testContour.release();
			}
		}
		if (!foundGoal) {
			System.out.println("Goal not found");
			contoured.release();
			ranged.release();
			src.release();
			pointList.clear();
			contourHierarchy.release();
			goal.release();
			return "unkown";
		}
		// System.out.println("AREA: " + maxArea);

		Rect rec = Imgproc.boundingRect(goal);
		double y = rec.br().y + rec.height / 2.0;
		y = -((2 * (y / src.height())) - 1);
		double distance = (TOP_TARGET_HEIGHT - TOP_CAMERA_HEIGHT)
				/ Math.tan((y * VERTICAL_FOV / 2.0 + CAMERA_ANGLE) * Math.PI / 180.0);
		System.out.println("DISTANCE: " + distance);
		double centerX = (rec.tl().x + rec.br().x) / 2.0;
		double centerY = (rec.tl().y + rec.br().y) / 2.0;
		String direction = "unknown";
		if (centerX < src.width() / 3.0) {
			direction = "left";
		} else if (centerX > 2.0 * src.width() / 3.0) {
			direction = "right";
		} else if (centerY < src.height() / 3.0) {
			direction = "backward";
		} else if (centerY > 2.0 * src.height() / 3.0) {
			direction = "forward";
		} else {
			direction = "shoot";
		}
		contoured.release();
		ranged.release();
		src.release();
		return direction;
	}
}
