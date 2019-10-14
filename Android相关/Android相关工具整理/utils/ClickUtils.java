package com.blackbox.lerist.utils;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Lerist on 2017/03/02 0002.
 */

public class ClickUtils {
    static long lastClickTime;

    public static boolean isDoubleClick() {
        if (System.currentTimeMillis() - lastClickTime < 500) {
            lastClickTime = 0;
            return true;
        }
        lastClickTime = System.currentTimeMillis();
        return false;
    }


    public interface OnRepeatedlyClickListener {
        /**
         * 正在点击
         *
         * @param repeatCount 已点击次数
         */
        void onClick(int repeatCount);

        /**
         * 单次点击
         */
        void onSingleClick();

        /**
         * 持续单击了指定次数
         */
        void onEnter();

        /**
         * 不满足点击次数 (小于或大于)
         */
        void onCancel(int repeatCount);
    }

    static Handler repeatedlyClickHandler;
    static final int REPEATEDLYCLICK_CLICK = 0;
    static final int REPEATEDLYCLICK_ENTER = 1;
    static final int REPEATEDLYCLICK_CANCEL = 2;
    static Object lastTag;

    /**
     * 连续点击
     *
     * @param tag                       触发标识, 用于绑定本次监听
     * @param repeatedlyNum             指定触发条件(连续点击次数)
     * @param onRepeatedlyClickListener 触发回调
     */
    public static void repeatedlyClick(Object tag, final int repeatedlyNum, final OnRepeatedlyClickListener onRepeatedlyClickListener) {
        if (onRepeatedlyClickListener == null || repeatedlyNum < 0) return;

        if (lastTag != tag) {
            repeatedlyClickHandler = null;
            lastTag = tag;
        }

        if (repeatedlyClickHandler == null) {
            repeatedlyClickHandler = new Handler() {
                final int timeout = 400;
                int repeatCount = 0;

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case REPEATEDLYCLICK_CLICK:
                            repeatCount++;
                            removeMessages(REPEATEDLYCLICK_CANCEL);
                            removeMessages(REPEATEDLYCLICK_ENTER);
                            onRepeatedlyClickListener.onClick(repeatCount);
                            if (repeatCount == repeatedlyNum) {
                                sendEmptyMessageDelayed(REPEATEDLYCLICK_ENTER, timeout);
                            } else {
                                sendEmptyMessageDelayed(REPEATEDLYCLICK_CANCEL, timeout);
                            }
                            break;
                        case REPEATEDLYCLICK_ENTER:
                            lastTag = null;
                            repeatedlyClickHandler = null;
                            onRepeatedlyClickListener.onEnter();
                            break;
                        case REPEATEDLYCLICK_CANCEL:
                            lastTag = null;
                            repeatedlyClickHandler = null;
                            if (repeatCount == 1) {
                                onRepeatedlyClickListener.onSingleClick();
                            }
                            onRepeatedlyClickListener.onCancel(repeatCount);
                            break;
                    }
                }
            };
        }

        repeatedlyClickHandler.sendEmptyMessage(REPEATEDLYCLICK_CLICK);
    }

    /**
     * 连续点击大于一定次数
     *
     * @param tag                       触发标识, 用于绑定本次监听
     * @param repeatedlyNum             指定触发条件(连续点击次数)
     * @param onRepeatedlyClickListener 触发回调
     */
    public static void fastClick(Object tag, final int repeatedlyNum, final OnRepeatedlyClickListener onRepeatedlyClickListener) {
        if (onRepeatedlyClickListener == null || repeatedlyNum < 0) return;

        if (lastTag != tag) {
            repeatedlyClickHandler = null;
            lastTag = tag;
        }

        if (repeatedlyClickHandler == null) {
            repeatedlyClickHandler = new Handler() {
                final int timeout = 400;
                int repeatCount = 0;

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case REPEATEDLYCLICK_CLICK:
                            repeatCount++;
                            removeMessages(REPEATEDLYCLICK_CANCEL);
                            removeMessages(REPEATEDLYCLICK_ENTER);
                            onRepeatedlyClickListener.onClick(repeatCount);
                            if (repeatCount >= repeatedlyNum) {
                                sendEmptyMessageDelayed(REPEATEDLYCLICK_ENTER, timeout);
                            } else {
                                sendEmptyMessageDelayed(REPEATEDLYCLICK_CANCEL, timeout);
                            }
                            break;
                        case REPEATEDLYCLICK_ENTER:
                            lastTag = null;
                            repeatedlyClickHandler = null;
                            onRepeatedlyClickListener.onEnter();
                            break;
                        case REPEATEDLYCLICK_CANCEL:
                            lastTag = null;
                            repeatedlyClickHandler = null;
                            if (repeatCount == 1) {
                                onRepeatedlyClickListener.onSingleClick();
                            }
                            onRepeatedlyClickListener.onCancel(repeatCount);
                            break;
                    }
                }
            };
        }

        repeatedlyClickHandler.sendEmptyMessage(REPEATEDLYCLICK_CLICK);
    }
}
