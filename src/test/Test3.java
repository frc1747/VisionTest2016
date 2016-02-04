package test;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Test3 {
    public void test() {
        VideoCapture vcap = new VideoCapture();
        boolean opened = vcap.open("http://trackfield.webcam.oregonstate.edu/mjpg/video.mjpg");
        System.out.println(opened);
        while (opened) {
            Mat result = new Mat();
            vcap.read(result);
            Utils.show(result, 3);
        }
    }
}
