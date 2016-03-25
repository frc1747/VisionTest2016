package test;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

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
		double counter = 0;
		while (true) {
			if (networkTable.isConnected()) {
				Object[] direction = processImage(counter);
				// System.out.println(direction);
				networkTable.putString("ShootDirection", (String) direction[0]);
				networkTable.putNumber("ShootRads", (double) direction[1]);
				// networkTable.putNumber("ShootDistance", (double)
				// direction[2]);
				counter++;
			}
		}
	}

	public Object[] processImage(double counter) {
		Mat src = null;
		try {
			URL url = new URL("http://axis-camera.local/axis-cgi/jpg/image.cgi");
			URLConnection uc = url.openConnection();
			InputStream imageStream = uc.getInputStream();
			BufferedImage image = ImageIO.read(imageStream);
			imageStream.close();
			src = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			src.put(0, 0, pixels);
			image = null;
		} catch (MalformedURLException e) {
			System.err.println("Bad URL");
			return new Object[] { "unknown", 0.0 };
		} catch (IOException e) {
			System.err.println("No Camera");
			return new Object[] { "unknown", 0.0 };
		}
		// Look for blue
		// Mat hsv = new Mat();
		// Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		Mat ranged = new Mat();
		Scalar lowerBound = new Scalar(0, 200, 0);// was 0,210,0
		Scalar upperBound = new Scalar(50, 255, 50);// was 40,255,40
		Core.inRange(src, lowerBound, upperBound, ranged);
		// blur image
		// Imgproc.medianBlur(ranged, ranged, 15);
		// Scalar upperThresh = new Scalar(255);
		// Scalar lowerThresh = new Scalar(200);
		// Core.inRange(ranged, lowerThresh, upperThresh, ranged);
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
			System.err.println("Goal not found");
			Utils.show(src, 10);
			contoured.release();
			ranged.release();
			src.release();
			pointList.clear();
			contourHierarchy.release();
			goal.release();
			return new Object[] { "unknown", 0.0 };
		}
		// System.out.println("AREA: " + maxArea);

		Rect rec = Imgproc.boundingRect(goal);
		double y = rec.br().y + rec.height / 2.0;
		y = -((2 * (y / src.height())) - 1);
		double distance = (TOP_TARGET_HEIGHT - TOP_CAMERA_HEIGHT)
				/ Math.tan((y * VERTICAL_FOV / 2.0 + CAMERA_ANGLE) * Math.PI / 180.0);
				// System.out.println("DISTANCE: " + distance);

		// Logic is inverted for how you move it
		Point center = new Point((rec.tl().x + rec.br().x) / 2.0, (rec.tl().y + rec.br().y) / 2.0),
				topLeft = new Point(149.5, 117), bottomRight = new Point(164.5, 139),
				hitboxCenter = new Point((topLeft.x + bottomRight.x) / 2.0, (topLeft.y + bottomRight.y) / 2.0);
		double angle = Math.acos(Math.abs(Math.sqrt(Math.pow(center.x - hitboxCenter.x, 2) + Math.pow(center.y, 2))
				/ Math.sqrt(Math.pow(hitboxCenter.x, 2) + Math.pow(hitboxCenter.y, 2))));
		System.out.println(angle);
		// double boxDistance = (hitboxCenter.x - center.x);
		// System.out.println(boxDistance);

		Imgproc.circle(src, center, 5, new Scalar(255, 0, 0));
		Imgproc.rectangle(src, topLeft, bottomRight, new Scalar(0, 0, 255));
		Imgproc.rectangle(src, rec.tl(), rec.br(), new Scalar(0, 255, 255));
		Utils.show(src, 10);
		String direction = "unknown";
		if (center.y > bottomRight.y) {
			direction = "forward";
		} else if (center.y < topLeft.y) {
			direction = "backward";
		} else if (center.x < topLeft.x) {
			direction = "left";
		} else if (center.x > bottomRight.x) {
			direction = "right";
		} else {
			direction = "shoot";
			if (counter % 20 == 0) {
				Imgcodecs.imwrite((".\\VisionLogs\\" + +System.currentTimeMillis() + ".jpg"), src);
			}
		}
		contoured.release();
		ranged.release();
		src.release();
		System.out.println(direction);
		return new Object[] { direction, angle };
	}
}
