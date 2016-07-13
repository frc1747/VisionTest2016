package test;

import org.opencv.core.Core;

public class Runner {

	static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_ffmpeg2411_64");
	}
	
    public static void main(String[] args) {
//        new Test1().test(false);
//        new Test2().test();
//      new Test3().test();
        new Test4().test();
//        new Test5().test();
    }
}
