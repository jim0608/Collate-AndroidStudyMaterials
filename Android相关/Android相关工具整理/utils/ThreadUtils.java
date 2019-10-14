package com.blackbox.lerist.utils;

import android.os.Looper;

/**
 * Created by Lerist on 2016/9/27, 0027.
 */

public class ThreadUtils {
    public static boolean isMainThread(Thread thread) {
        boolean b = Looper.getMainLooper().getThread() == thread;
        thread = null;
        return b;
    }

    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
