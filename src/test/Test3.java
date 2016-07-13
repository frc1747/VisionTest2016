package test;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

public class Test3 {
	
    public void test() {
        VideoCapture vcap = new VideoCapture();
        vcap.open("http://10.17.47.16/mjpg/video.mjpg");
        vcap.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
        vcap.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);
        //boolean opened = vcap.open("http://10.17.47.16/mjpg/video.mjpg");
        System.out.println(vcap.isOpened());
        while (vcap.isOpened()) {
            Mat result = new Mat();
            vcap.read(result);
            Utils.show(result, 3);
        }
    }
}
