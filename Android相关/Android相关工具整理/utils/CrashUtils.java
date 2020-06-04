package com.blackbox.lerist.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.support.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <pre>
 *     desc  : 崩溃相关工具类
 * </pre>
 */
public class CrashUtils implements UncaughtExceptionHandler {

    private static CrashUtils mInstance = new CrashUtils();
    private UncaughtExceptionHandler mDefaultHandler;
    private boolean mInitialized;
    private static String dir;
    private String versionName;
    private int versionCode;
    private Context mContext;
    private Class<?> mRestartActivity;
    private long mRestartTime = 1000; //默认1s后重启应用
    private OnUncaughtExceptionListener onUncaughtExceptionListener;

    public interface OnUncaughtExceptionListener {
        void onUncaughtException(Thread thread, Throwable throwable);
    }

    private CrashUtils() {
    }

    /**
     * 获取单例
     * <p>在Application中初始化{@code CrashUtils.getInstance().init(this);}</p>
     *
     * @return 单例
     */
    public static CrashUtils getInstance() {
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @return {@code true}: 成功<br>{@code false}: 失败
     */
    public boolean init(Context context) {
        if (mInitialized) return true;
        this.mContext = context.getApplicationContext();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = context.getExternalCacheDir().getPath();
        } else {
            dir = context.getCacheDir().getPath();
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        return mInitialized = true;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String fullPath = dir + File.separator + "crash_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(new Date(System.currentTimeMillis())) + ".txt";
        if (!FileUtils.createOrExistsFile(fullPath)) return;
        StringBuilder sb = new StringBuilder();
        sb.append(getCrashHead());
        Writer writer = new StringWriter();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
            Throwable cause = throwable.getCause();
            while (cause != null) {
                cause.printStackTrace(pw);
                cause = cause.getCause();
            }
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        sb.append(writer.toString());
        writeFileFromString(fullPath, sb.toString(), false);

        if (onUncaughtExceptionListener != null) {
            try {
                onUncaughtExceptionListener.onUncaughtException(thread, throwable);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }


        if (!isRestartApp()) {
            //交给默认UncaughtExceptionHandler处理异常, 系统处理异常时会阻塞线程, 直到用户点击"已停止Dialog"按钮, 并杀死进程
            if (mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(thread, throwable);
            }
        } else {
            //需要重启APP时不交给系统处理异常(因为大部分系统会在点击"已停止Dialog"按钮时自动杀死进程), 直接退出App, mRestartTime时间后重启APP
            Intent intent = new Intent(mContext.getApplicationContext(), mRestartActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            AlarmManager mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            //AlarmManager重启应用，使用PendingIntent
            PendingIntent restartIntent = PendingIntent.getActivity(
                    mContext.getApplicationContext(), 0, intent, intent.getFlags());
            mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + mRestartTime,
                    restartIntent); // 重启应用
            //杀死App进程
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    private boolean isRestartApp() {
        return mRestartActivity != null;
    }

    /**
     * 设置崩溃重启
     * <p>
     * 注意: 设置崩溃重启后未捕获异常发生时不会调用 DefaultUncaughtExceptionHandler.uncaughtException()
     * </>
     *
     * @param restartActivity 重启目标Activity
     * @param restartTimems   多少ms后重启
     * @return
     */
    public CrashUtils setCarshRestartApp(@Nullable Class<?> restartActivity, long restartTimems) {
        this.mRestartActivity = restartActivity;
        this.mRestartTime = restartTimems;
        return this;
    }

    /**
     * 设置未捕获异常发生时listener,
     *
     * @param onUncaughtExceptionListener
     * @return
     */
    public CrashUtils setOnUncaughtExceptionListener(OnUncaughtExceptionListener onUncaughtExceptionListener) {
        this.onUncaughtExceptionListener = onUncaughtExceptionListener;
        return this;
    }

    /**
     * 获取崩溃头
     *
     * @return 崩溃头
     */
    private StringBuilder getCrashHead() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n************* Crash Log Head ****************");
        sb.append("\nDevice Manufacturer: ").append(Build.MANUFACTURER);// 设备厂商
        sb.append("\nDevice Model       : ").append(Build.MODEL);// 设备型号
        sb.append("\nAndroid Version    : ").append(Build.VERSION.RELEASE);// 系统版本
        sb.append("\nAndroid SDK        : ").append(Build.VERSION.SDK_INT);// SDK版本
        sb.append("\nApp VersionName    : ").append(versionName);
        sb.append("\nApp VersionCode    : ").append(versionCode);
        sb.append("\n************* Crash Log Head ****************\n\n");
        return sb;
    }

    private boolean writeFileFromString(String filePath, String content, boolean append) {
        return writeFileFromString(new File(filePath), content, append);
    }

    private boolean writeFileFromString(File file, String content, boolean append) {
        if (file == null || content == null) return false;
        if (!createOrExistsFile(file)) return false;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean createOrExistsFile(File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean createOrExistsDir(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

}
