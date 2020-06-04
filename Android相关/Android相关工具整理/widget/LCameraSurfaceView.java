#（Android的实现自定义相机录制视频）[https://juejin.im/post/5d68eaf16fb9a06adb7ff80e]

# LCameraSurfaceView：自定义的相机
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.blackbox.lerist.utils.LOrientationUtils;


/**
 * Created by Lerist on 2016/1/25, 0025.
 * <uses-permission android:name="android.permission.CAMERA"/>
 * <uses-feature android:name="android.hardware.camera" />
 * <uses-feature android:name="android.hardware.camera.autofocus" />
 */
public class LCameraSurfaceView extends SurfaceView implements Camera.AutoFocusCallback, LOrientationUtils.OrientationListener {
    private final Context mContext;
    private Camera mCamera;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceHolder.Callback callback;
    private SurfaceHolder mSurfaceHolder;
    private boolean isPreviewing;
    private boolean isAutoStartPerview = true;
    private LOrientationUtils orientationUtils;
    private Camera.PreviewCallback mPreviewCallback;

    public LCameraSurfaceView(Context context) {
        this(context, null);
    }

    public LCameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LCameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LCameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }


    private void init() {
        orientationUtils = new LOrientationUtils(mContext, this);
        //默认启用自动对焦
        setAutoFocus(true);

        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                mSurfaceHolder = holder;
                if (isAutoStartPerview) {
                    startPreview();
                }
                if (callback != null) callback.surfaceCreated(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (callback != null) callback.surfaceChanged(holder, format, width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopPreview();
                if (callback != null) callback.surfaceDestroyed(holder);
            }
        });

    }

    private void initCamera() {
        if (mSurfaceHolder == null) return;
        try {
            mCamera = Camera.open(cameraId);
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            int rotation = 0;
//                            if (getWidth() < getHeight()) {
//                                rotation = 90;
//                            }
            switch (display.getRotation()) {
                case Surface.ROTATION_0:
                    rotation = 90;
                    break;
                case Surface.ROTATION_90:
                    rotation = 0;
                    break;
                case Surface.ROTATION_180:
                    rotation = 0;
                    break;
                case Surface.ROTATION_270:
                    rotation = 180;
                    break;
            }
            mCamera.setDisplayOrientation(rotation);
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            mCamera = null;
        }
    }

    private void releaseCamera() {
        //停止传感器
        orientationUtils.stopRequest();

        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.autoFocus(null);
                mCamera.setPreviewDisplay(null);
                mCamera.release(); // 释放相机
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPreview() {
        if (isPreviewing) return;

        isPreviewing = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initCamera();
                    mCamera.startPreview();
                    mCamera.autoFocus(LCameraSurfaceView.this);
                } catch (Exception e) {
                    isPreviewing = false;
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

    public void stopPreview() {
        if (mCamera == null) return;
        isPreviewing = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    releaseCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public boolean isAutoStartPerview() {
        return isAutoStartPerview;
    }

    /**
     * 在相机初始化后(surfaceCreated)自动开始预览
     *
     * @param autoStartPerview
     */
    public void setAutoStartPerview(boolean autoStartPerview) {
        isAutoStartPerview = autoStartPerview;
    }

    public void setAutoFocus(boolean autoFocus) {
        if (autoFocus) {
            orientationUtils.requestOrientation();
        } else {
            orientationUtils.stopRequest();
        }
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public void zoom(int value, int maxValue) {
        if (mCamera != null && value >= 0 && value <= maxValue) {
            Camera.Parameters parameters = mCamera.getParameters();
            int maxZoom = parameters.getMaxZoom();
            mCamera.startSmoothZoom((int) (((value * 1.0) / maxValue) * maxZoom));
        }
    }

    public void setCallback(SurfaceHolder.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
//        自动连续聚焦 (@弃用 使用了传感器)
//        if (mCamera != null) mCamera.autoFocus(LCameraSurfaceView.this);
    }

    LOrientationUtils.Orientation mOrientation = new LOrientationUtils.Orientation();

    @Override
    public void onOrientationChanged(LOrientationUtils.Orientation orientation) {
        int offset = 10;//各方向变化10度则对焦
        if ( Math.abs(mOrientation.getAxisX() - orientation.getAxisX()) > offset
                || Math.abs(mOrientation.getAxisY() - orientation.getAxisY()) > offset
                || Math.abs(mOrientation.getAxisZ() - orientation.getAxisZ()) > offset) {
            //对焦
            try {
                if (mCamera != null) {
                    mCamera.autoFocus(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mOrientation.set(orientation);
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback cb) {
        mPreviewCallback = cb;
    }
}
