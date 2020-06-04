package com.blackbox.lerist.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.view.View;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Lerist on 2015/8/14, 0014.
 */
public class LToast {

    private static boolean isEnabled = true;
    private static Toast toast;

    /**
     * Show the view or text notification for a short period of time.  This time
     * could be user-definable.  This is the default.
     */
    public static final int LENGTH_SHORT = 0;

    /**
     * Show the view or text notification for a long period of time.  This time
     * could be user-definable.
     */
    public static final int LENGTH_LONG = 1;

    @IntDef({LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    private static ToastTextInterceptor mToastTextInterceptor;

    public static void setToastTextInterceptor(ToastTextInterceptor toastTextInterceptor) {
        mToastTextInterceptor = toastTextInterceptor;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setEnabled(boolean isEnabled) {
        LToast.isEnabled = isEnabled;
    }

    public static void show(Context context, int resId) {
        show(context, context.getString(resId));
    }

    public static void show(final Context context, final String msg) {
        show(context, msg, LENGTH_SHORT);
    }

    public static void show(final Context context, final String msg, @Duration final int duration) {
        show(context, msg, duration, -1);
    }

    public static void show(final Context context, final String msg, @Duration final int duration, final int gravity) {
        if (!isEnabled) {
            return;
        }
        if (context == null) return;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (toast == null) {
                        toast = Toast.makeText(context.getApplicationContext(), "", duration);
                    }
                    if (mToastTextInterceptor != null) {
                        toast.setText(mToastTextInterceptor.onIntercepted(msg));
                    } else {
                        toast.setText(msg);
                    }
                    toast.setDuration(duration);
                    if (gravity != -1) {
                        toast.setGravity(gravity, 0, 0);
                    }
                    toast.show();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        if (isMainThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }


    public static void show(final Context context, final View view, @Duration final int duration) {
        show(context, view, duration, -1, 0, 0);
    }

    public static void show(final Context context, final View view, @Duration final int duration, final int gravity, final int xOffset, final int yOffset) {
        if (!isEnabled) {
            return;
        }
        if (context == null) return;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (toast == null) {
                        toast = Toast.makeText(context.getApplicationContext(), "", duration);
                    }

                    toast.setView(view);
                    toast.setDuration(duration);
                    if (gravity != -1) {
                        toast.setGravity(gravity, xOffset, yOffset);
                    }
                    toast.show();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        if (isMainThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    static boolean isMainThread() {
        boolean b = Looper.getMainLooper().getThread() == Thread.currentThread();
        return b;
    }

    public interface ToastTextInterceptor {
        String onIntercepted(String srcText);
    }
}
