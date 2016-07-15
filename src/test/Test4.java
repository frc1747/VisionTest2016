package test;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class Test4 {

	
	public static final int IMAGE_WIDTH = 320;
	public static final int IMAGE_HEIGHT = 240;

	public static final double VERTICAL_FOV = 35.25;
	public static final double HORIZONTAL_FOV = 47;
	// TODO: Verify this
	public static final double CAMERA_ANGLE = 60.0; //calculated 4/13/16 -JH
	public static final double PIXEL_ANGLE = CAMERA_ANGLE/320.0;
	
	public static final double FOCAL_LENGTH_PIXELS = (0.5 * IMAGE_WIDTH / Math.tan(HORIZONTAL_FOV)/2);
	//public static final double ROBOT_CENTER_ANGLE = Math.atan(12/13*Math.tan())fix if needed - Carl

	private static final String GAME_STATE = "GameState";
	private String gameState;

	public VideoCapture vcap = new VideoCapture();
    
	public void test() {
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("10.17.47.2");
		NetworkTable networkTable = NetworkTable.getTable("imageProcessing");
		double counter = 0;
		String url = "http://10.17.47.16/mjpg/video.mjpg";

	    vcap.open(url);
	    System.out.println(vcap.isOpened());
	    
		while (vcap.isOpened()) {
			if (networkTable.isConnected()) {
				Object[] direction = processImage(counter);
				// System.out.println(direction);
				networkTable.putString("ShootDirection", (String) direction[0]);
				networkTable.putNumber("ShootRads", (double) direction[1]);
				networkTable.putNumber("GyroAngle", (double) direction[2]);
				gameState = networkTable.getString(GAME_STATE);
				// networkTable.putNumber("ShootDistance", (double)
				// direction[2]);
				counter++;
				try {
					//Thread.sleep((long) (1 / 25.0 * 1000));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}

	public Object[] processImage(double counter) {
		Mat src = new Mat();
		try {
			/*
			//URL url = new URL("http://axis-camera.local/axis-cgi/jpg/image.cgi");
			URL url = new URL("http://10.17.47.16/axis-cgi/jpg/image.cgi");
			URLConnection uc = url.openConnection();
			InputStream imageStream = uc.getInputStream();
			BufferedImage image = ImageIO.read(imageStream);
			imageStream.close();
			src = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			src.put(0, 0, pixels);
			image = null;*/
			//System.out.println(vcap.grab());
			//System.out.println(vcap.retrieve(src));
			vcap.read(src);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//System.out.println(src.channels());
		if(src.empty()) {
			//System.out.println();
			return new Object[] { "unknown", 0.0,0.0 };
		}
		/*catch (MalformedURLException e) {
			System.err.println("Bad URL"); 
			return new Object[] { "unknown", 0.0 };
		} catch (IOException e) {
			System.err.println("No Camera");
			return new Object[] { "unknown", 0.0, 0.0 };
		}*/
		// Look for blue
		// Mat hsv = new Mat();
		// Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		Mat ranged = new Mat();
		Scalar lowerBound = new Scalar(0, 140, 0);// was 0,210,0
		Scalar upperBound = new Scalar(50, 255, 50);// was 40,255,40
		//if(src.channels() > 1) {
			Core.inRange(src, lowerBound, upperBound, ranged);
		//}
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
			//Utils.show(src, 10);
			contoured.release();
			ranged.release();
			src.release();
			pointList.clear();
			contourHierarchy.release();
			goal.release();
			return new Object[] { "unknown", 0.0,0.0 };
		}
		// System.out.println("AREA: " + maxArea);

		Rect rec = Imgproc.boundingRect(goal);
		double y = rec.br().y + rec.height / 2.0;
		y = -((2 * (y / src.height())) - 1);
				// System.out.println("DISTANCE: " + distance);

		// Moving hitbox to right moves shot to left (Increasing x)
		// Moving hitbox down moves shot up (Increasing y)
		Point center = new Point((rec.tl().x + rec.br().x) / 2.0, (rec.tl().y + rec.br().y) / 2.0),
				topLeft = new Point(161, 106.5), bottomRight = new Point(168, 122), // Old Values topLeft = new Point(151.5, 117), bottomRight = new Point(165.5, 139),
				hitboxCenter = new Point((topLeft.x + bottomRight.x) / 2.0, (topLeft.y + bottomRight.y) / 2.0);
		double angle = Math.acos(Math.abs(Math.sqrt(Math.pow(center.x - hitboxCenter.x, 2) + Math.pow(center.y, 2))
				/ Math.sqrt(Math.pow(hitboxCenter.x, 2) + Math.pow(hitboxCenter.y, 2))));
		//double gyroAngle = (CAMERA_ANGLE/320)*Math.abs(hitboxCenter.x - center.x);
		double gyroAngle = Math.toDegrees(Math.atan((hitboxCenter.x - center.x)/FOCAL_LENGTH_PIXELS));
		System.out.println(angle);
		// double boxDistance = (hitboxCenter.x - center.x);
		// System.out.println(boxDistance);

		Core.circle(src, center, 5, new Scalar(255, 0, 0));
		Core.rectangle(src, topLeft, bottomRight, new Scalar(0, 0, 255));
		Core.rectangle(src, rec.tl(), rec.br(), new Scalar(0, 255, 255));
		if(gameState.equals("auton") || gameState.equals("teleop") || counter < 100) {
			Utils.show(src, 10);
		}
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
				//Imgcodecs.imwrite((".\\VisionLogs\\" + +System.currentTimeMillis() + ".jpg"), src);
			}
		}
		contoured.release();
		ranged.release();
		src.release();
		System.out.println(direction);
		System.out.println(gyroAngle);
		return new Object[] { direction, angle, gyroAngle };
		
	}
}
