package com.blackbox.lerist.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.blackbox.lerist.R;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Lerist on 2015/3/7, 0007.
 */
public class Lerist {
    private static Context mContext;

    public static void Toast(Context context, String toastStr) {
        Toast.makeText(context, toastStr, Toast.LENGTH_SHORT).show();
        context = null;
    }

    public static void Log(Class c, String info) {
        String TAG = c.getSimpleName();
        Log(TAG, info);
    }

    public static void Log(String tag, String info) {
        String TAG = tag;
        Log.i(TAG, info);
    }

    public static void Log(String info) {
        String TAG = "Lerist.";
        Log(TAG, info);
    }

    public static void with(Context context) {
        mContext = context;
    }

    public static int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        if (context == null) return 0;
        final float scale = context.getResources().getDisplayMetrics().density;
        context = null;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        context = null;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param fontScale
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param spValue
     * @param fontScale
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获得一个随机值
     *
     * @param n
     * @return
     */
    public static int getRandom(int n) {
        return new Random().nextInt(n);
    }

    /**
     * 设置View的margin值
     *
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * 获取应用名
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pid) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context = null;

        return processName;
    }

    public static double findMax(Object... doubles) {
        double maxValue = (double) doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if ((double) doubles[i] > maxValue) {
                maxValue = (double) doubles[i];
            }
        }

        return maxValue;
    }

    public static double findMin(Object... doubles) {
        double minValue = (double) doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if ((double) doubles[i] < minValue) {
                minValue = (double) doubles[i];
            }
        }

        return minValue;
    }

    public interface OnCompareHandler<C extends Object> {
        /**
         * @param obj1
         * @param obj2
         * @return [0]:obj1==obj2 , [>0]:obj1>obj2, [<0]:obj1<obj2
         */
        int onCompare(C obj1, C obj2);
    }

    /**
     * 容器中是否包含某一值
     *
     * @param container
     * @param value
     * @param onCompareHandler 判断子对象是否相等逻辑
     * @param <E>
     * @return
     */
    public static <E extends Object> boolean isContains(List<E> container, E value, OnCompareHandler<E> onCompareHandler) {
        if (container == null || container.isEmpty()) return false;

        for (E e : container) {
            boolean isEqual = onCompareHandler.onCompare(e, value) == 0;
            if (isEqual) return true;
        }
        return false;
    }

    /**
     * ListView工具箱
     */
    public static class ListViewUtils {
        /**
         * 计算并设置listView高度,解决嵌入ScrollView内容显示不完全
         *
         * @param listView
         */
        public static void setListViewHeightBasedOnChildren(ListView listView) {
//          获取ListView对应的Adapter
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
//               pre-condition
                return;
            }

            int totalHeight = 0;
            for (int i = 0, len = listAdapter.getCount(); i < len; i++) { //listAdapter.getCount()返回数据项的数目
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0); //计算子项View 的宽高
                totalHeight += listItem.getMeasuredHeight(); //统计所有子项的总高度
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//          listView.getDividerHeight()获取子项间分隔符占用的高度
//          params.height最后得到整个ListView完整显示需要的高度
            listView.setLayoutParams(params);
        }
    }

    /**
     * 获取当前时间
     *
     * @param format
     * @return
     */
    public static String getCurrentTime(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
    }

    /**
     * 获取程序签名
     *
     * @param context
     * @return
     */
    public static String getSign(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> iter = apps.iterator();
        while (iter.hasNext()) {
            PackageInfo packageinfo = iter.next();
            String packageName = packageinfo.packageName;
            if (packageName.equals(context.getPackageName())) {
                return packageinfo.signatures[0].toCharsString();
            }
        }
        context = null;
        return null;
    }

    /**
     * 获取Toolbar高度
     *
     * @param context
     * @return
     */
    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        context = null;

        return toolbarHeight;
    }

    /**
     * 获取年龄
     *
     * @param birthday (20150909|2015-09-09)
     * @return
     */
    public static String getAge(String birthday) {
        return getAge(birthday, null);
    }

    public static String getAge(String birthday, String postfix) {
        if (birthday == null || birthday.isEmpty() || birthday.length() < 4) {
            return "";
        }
        String yearStr = birthday.substring(0, 4);
        try {
            int year = Integer.parseInt(yearStr);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (currentYear >= year) {
                return (currentYear - year) + "" + (postfix == null ? "" : postfix);
            } else {
                return "";
            }
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        context = null;
        return result;
    }

    /**
     * 隐藏软键盘
     *
     * @param activity
     * @return 隐藏之前, 软键盘是否显示
     */
    public static boolean hideSoftKeyboard(Activity activity) {
        boolean isShowed = false;
        try {
            if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (activity.getCurrentFocus() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    isShowed = inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        activity = null;
        return isShowed;
    }

    public static void showSoftKeyboard(Activity activity) {
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(activity.getWindow().getDecorView(), InputMethodManager.SHOW_FORCED);
            }
        }
        activity = null;
    }

    public static void loadImage(final Activity activity, final String url, final ImageView imageView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = new URL(url).openConnection().getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                    // bitmap.recycle();
                    inputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 将float[]数组转换为OpenGL ES所需的FloatBuffer
     */
    public static FloatBuffer toFloatBuffer(float[] arr) {
        FloatBuffer mBuffer;
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }

    /**
     * 设置相机显示方向
     *
     * @param activity
     * @param cameraId 前置 or 后置
     * @param camera
     */
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {

        Camera.CameraInfo info =
                new Camera.CameraInfo();

        Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 主线程运行
     *
     * @param context
     * @param runnable
     */
    public static void runOnMainThread(@Nullable Context context, Runnable runnable) {
        try {
            new Handler(Looper.getMainLooper()).post(runnable);
//            ((Activity) context).runOnUiThread(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context = null;

    }

    /**
     * 判断Activity是否为全屏显示
     *
     * @param activity
     * @return
     */
    public static boolean isActivityFullscreen(Activity activity) {
        int flags = activity.getWindow().getAttributes().flags;
        activity = null;

        if ((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        AtomicInteger sNextGeneratedId = new AtomicInteger(1);
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * 获取View高度
     *
     * @param v
     * @return
     */
    public static int getViewHeight(View v) {

        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        int width = layoutParams.width;
        int height = layoutParams.height;
        try {
            Method m = v.getClass().getDeclaredMethod("onMeasure", int.class,
                    int.class);
            m.setAccessible(true);

            if (width == ViewGroup.LayoutParams.WRAP_CONTENT
                    && height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED));
            } else if (width == ViewGroup.LayoutParams.MATCH_PARENT
                    && height == ViewGroup.LayoutParams.MATCH_PARENT) {
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST));
            } else if (width == ViewGroup.LayoutParams.WRAP_CONTENT
                    && height == ViewGroup.LayoutParams.MATCH_PARENT) {
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST));
            } else {
                //width == ViewGroup.LayoutParams.MATCH_PARENT
                // && height == ViewGroup.LayoutParams.WRAP_CONTENT
                // || other
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v.getMeasuredHeight();
    }

    /**
     * 获取View宽度
     *
     * @param v
     * @return
     */
    public static int getViewWidth(View v) {

        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        int width = layoutParams.width;
        int height = layoutParams.height;
        try {
            Method m = v.getClass().getDeclaredMethod("onMeasure", int.class,
                    int.class);
            m.setAccessible(true);

            if (width == ViewGroup.LayoutParams.MATCH_PARENT
                    && height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED));
            }

            if (width == ViewGroup.LayoutParams.WRAP_CONTENT
                    && height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED));
            }

            if (width == ViewGroup.LayoutParams.MATCH_PARENT
                    && height == ViewGroup.LayoutParams.MATCH_PARENT) {
                m.invoke(v, View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(
                        ((View) v.getParent()).getMeasuredWidth(),
                        View.MeasureSpec.AT_MOST));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v.getMeasuredWidth();
    }

    public static View getViewParent(View v) {
        ViewParent parent = v.getParent();
        return (View) parent;
    }

    /**
     * 为密码输入框指定显示/隐藏出发按钮
     *
     * @param showHideBtn      事件触发按钮
     * @param passwordEditText 密码输入框
     */
    public static void setPasswordShowHideTouchListener(View showHideBtn, final EditText passwordEditText) {
        showHideBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_SCROLL:
                    case MotionEvent.ACTION_UP:
                        // * InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT
                        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                        break;
                    default:
                        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        break;
                }
                //保持光标在末尾
                Editable editable = passwordEditText.getText();
                Selection.setSelection(editable, editable.length());
                return false;
            }
        });
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }


    /**
     * 获得屏幕高度, 不包括底部虚拟按键高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕实际物理尺寸高度，包括虚拟功能键高度
     */
    public static int getScreenRealHeight(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getBottomStatusHeight(Context context) {
//        int totalHeight = getScreenRealHeight(context);
//
//        int contentHeight = getScreenHeight(context);
//
//        return totalHeight  - contentHeight;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 标题栏高度
     *
     * @return
     */
    public static int getTitleHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前进程名
     *
     * @param context
     * @return
     */
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }
}
