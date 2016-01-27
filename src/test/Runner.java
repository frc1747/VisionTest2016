package test;

import org.opencv.core.Core;

public class Runner {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new Test1().test(true);
        new Test2().test();
        new Test3().test();
    }
}
