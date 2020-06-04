package com.blackbox.lerist.utils;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Lerist on 2016/3/12, 0012.
 */
public class LTimerUtils {
    public interface OnCountDownListener {
        void start(long beginTime);

        void runing(long timeOut);

        void stop(long stopTime);

        void end(long endTime);
    }

    private static class Inner {
        private static LTimerUtils instance = new LTimerUtils();
    }

    public static LTimerUtils getInstance() {
        return Inner.instance;
    }

    public static LTimerUtils getNewInstance() {
        return new LTimerUtils();
    }

    Handler countdownHandler;

    public void countDown(final long count, final OnCountDownListener onCountDownListener) {
        countDown(count, 1000, onCountDownListener);
    }

    /**
     * 倒计时
     *
     * @param count               倒计时时长(ms) -1: 无限计时
     * @param step                倒计时步长(ms)
     * @param onCountDownListener callback
     */
    public void countDown(final long count, final long step, final OnCountDownListener onCountDownListener) {
        countdownHandler = new Handler() {
            long beginTime = 0;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0: //start
                        beginTime = System.currentTimeMillis();
                        onCountDownListener.start(beginTime);
                        countdownHandler.sendEmptyMessage(1);
                        break;
                    case 1: //runing
                        long timeOut = Math.abs(beginTime - System.currentTimeMillis());
                        if (count == -1) {
                            onCountDownListener.runing(timeOut);
                            countdownHandler.sendEmptyMessageDelayed(1, step);
                        } else {
                            if (timeOut < count && timeOut >= 0) {
                                onCountDownListener.runing(count - timeOut);
                                countdownHandler.sendEmptyMessageDelayed(1, step);
                            } else {
                                countdownHandler.sendEmptyMessage(3);
                            }
                        }
                        break;
                    case 2://stop
                        onCountDownListener.stop(System.currentTimeMillis());
                        break;
                    case 3: //end
                        onCountDownListener.end(System.currentTimeMillis());
                        break;
                }
            }
        };
        start();
    }

    public void start() {
        if (countdownHandler == null) return;
        countdownHandler.removeMessages(0);
        countdownHandler.sendEmptyMessage(0);
    }

    public void stop() {
        if (countdownHandler == null) return;
        countdownHandler.removeMessages(0);
        countdownHandler.removeMessages(1);
        countdownHandler.sendEmptyMessage(2);
    }
}
