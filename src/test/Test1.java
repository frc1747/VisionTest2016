package test;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.Scanner;

public class Test1 {

	@SuppressWarnings("unused")
	public void test(boolean video) {
		Scanner scan = new Scanner(System.in);
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
				scan.nextLine();
			}
		} else {
			src = Imgcodecs.imread("frame.png");
			processImage(src);
		}
		scan.close();
	}

	public void processImage(Mat src) {
		// Look for blue
		// Mat hsv = new Mat();
		// Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		Mat ranged = new Mat();
		Scalar lowerBound = new Scalar(150, 180, 0);
		Scalar upperBound = new Scalar(255, 255, 30);
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
		Rect rec = Imgproc.boundingRect(goal);
		Point center = new Point((rec.tl().x + rec.br().x) / 2.0, (rec.tl().y + rec.br().y) / 2.0),
				topLeft = new Point(100, 100), bottomRight = new Point(300, 200);
		
		Imgproc.circle(coloredBlurred, center, 5, new Scalar(255, 0, 0));
		Imgproc.rectangle(coloredBlurred, topLeft, bottomRight, new Scalar(0, 0, 255));
		
		Utils.show(coloredBlurred, 0);
		coloredBlurred.release();
		boxPoints.release();
		goal2f.release();
		approxCurve.release();
		contoured.release();
		ranged.release();
		src.release();
	}

}