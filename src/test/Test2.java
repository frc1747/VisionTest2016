package test;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Test2 {

	public void test() {
		Mat orig = Imgcodecs.imread("sub0.jpg");
		Mat goal = Imgcodecs.imread("sub1.jpg");
		Mat result = new Mat();
		Core.subtract(goal, orig, result);
		Utils.show(result, 2);
	}

}
