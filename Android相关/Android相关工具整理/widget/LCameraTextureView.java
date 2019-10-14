package com.blackbox.lerist.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Lerist on 2016/1/25, 0025.
 * <uses-permission android:name="android.permission.CAMERA" />
 * <uses-feature
 * android:name="android.hardware.camera"
 * android:required="true" />
 */
public class LCameraTextureView extends TextureView implements TextureView.SurfaceTextureListener, Camera.AutoFocusCallback {
    private Camera mCamera;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceTextureListener surfaceTextureListener;
    private SurfaceTexture mSurface;
    private boolean isAutoStartPerview = true;
    private Camera.PreviewCallback mPreviewCallback;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Camera.FaceDetectionListener mFaceDetectionListener;
    private Camera.Parameters mParameters;
    private boolean isPreviewing;
    private boolean isStoped;
    private Camera.OnZoomChangeListener mOnZoomChangeListener;
    private int mSmoothZoomValue = -1;
    private int mZoom = -1;
    private Handler handler;
    private static ConcurrentLinkedQueue<Runnable> mTempActions = new ConcurrentLinkedQueue<>();
    private int mPictureWidth;
    private int mPictureHeight;

    public LCameraTextureView(Context context) {
        this(context, null);
    }

    public LCameraTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LCameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LCameraTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        HandlerThread handlerThread = new HandlerThread("camera");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        //执行缓存队列
                        if (mTempActions != null) {
                            while (!mTempActions.isEmpty()) {
                                Runnable runnable = mTempActions.poll();
                                runnable.run();
                            }
                        }
                        break;
                }
            }
        };

        //设置透明
        setAlpha(0);
        animate().alpha(1).setDuration(1000).start();

        //设置控件透明
        setOpaque(false);

        setSurfaceTextureListener(this);

//        this.setScaleY(-1);

    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        mSurface = surface;
        if (surfaceTextureListener != null)
            surfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);

        //执行缓存队列
        handler.sendEmptyMessage(1);

        if (isAutoStartPerview) {
            startPreview();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (surfaceTextureListener != null)
            surfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
//        //解决java.lang.RuntimeException: Camera is being used after Camera.release() was called异常
//        setSurfaceTextureListener(null);
        stopPreview();
        if (surfaceTextureListener != null) {
            surfaceTextureListener.onSurfaceTextureDestroyed(surface);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (surfaceTextureListener != null)
            surfaceTextureListener.onSurfaceTextureUpdated(surface);

    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public void setListener(SurfaceTextureListener surfaceTextureListener) {
        this.surfaceTextureListener = surfaceTextureListener;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean startPreview() {
        handler.removeMessages(0);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSurface == null) {
                    //mSurface创建后再执行startPreview()
                    mTempActions.clear();
                    mTempActions.offer(new Runnable() {
                        @Override
                        public void run() {
                            startPreview();
                        }
                    });
                    return;
                }

                if (mSurface == null) return;

                if (isPreviewing) {
                    return;
                }
                isPreviewing = true;
                isStoped = false;
                try {
                    WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                    Display display = windowManager.getDefaultDisplay();
                    mCamera = Camera.open(cameraId);

                    //TODO:这里会报错
                    /*for (Camera.Size size : mCamera.getParameters().getSupportedPreviewSizes()) {
//                        KLog.e(size.width + " , " + size.height);
                    }*/


//                    int rotation = 0;
////                            if (getWidth() < getHeight()) {
////                                rotation = 90;
////                            }
//                    switch (display.getRotation()) {
//                        case Surface.ROTATION_0:
//                            rotation = 90;
//                            break;
//                        case Surface.ROTATION_90:
//                            rotation = 0;
//                            break;
//                        case Surface.ROTATION_180:
//                            rotation = 0;
//                            break;
//                        case Surface.ROTATION_270:
//                            rotation = 180;
//                            break;
//                    }
//                    mCamera.setDisplayOrientation(rotation);
                    Camera.CameraInfo info =
                            new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraId, info);
                    int rotation = windowManager.getDefaultDisplay()
                            .getRotation();
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
                    try {
                        mCamera.setDisplayOrientation(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (mParameters != null) mCamera.setParameters(mParameters);
                        if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                            Camera.Parameters parameters = mCamera.getParameters();
                            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
                            mCamera.setParameters(parameters);
                        }
                        if (mPictureWidth != 0 && mPictureHeight != 0) {
                            Camera.Parameters parameters = mCamera.getParameters();
                            parameters.setPictureSize(mPictureWidth, mPictureHeight);
                            mCamera.setParameters(parameters);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mZoom != -1) {
                        setZoom(mZoom);
                    }
                    mCamera.setFaceDetectionListener(mFaceDetectionListener);
                    mCamera.setZoomChangeListener(mOnZoomChangeListener);
                    mCamera.setPreviewCallback(mPreviewCallback);
                    mCamera.setPreviewTexture(mSurface);
                    mCamera.startPreview();
//                    mCamera.autoFocus(LCameraTextureView.this);
                    if (mSmoothZoomValue != -1) {
                        startSmoothZoom(mSmoothZoomValue);
                    }
                } catch (Exception e) {
                    isPreviewing = false;
                    e.printStackTrace();
                    if (e.getMessage().contains("Fail to connect to camera service")
                            || e.getMessage().contains("set display orientation failed")) {
                        //重新打开
                        if (!isStoped) {
                            startPreview();
                        }
                    }
                }
            }
        }, 100);
        return mSurface != null;
    }

    public void stopPreview() {
        handler.removeMessages(0);
        handler.post(new Runnable() {
            @Override
            public void run() {
                isStoped = true;
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewCallback(null);
                        mCamera.stopPreview();
                        mCamera.release(); // 释放相机
                        mCamera = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isPreviewing = false;
            }
        });
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

    /**
     * @param shutter  the callback for image capture moment, or null
     * @param raw      the callback for raw (uncompressed) image data, or null
     * @param postview callback with postview image data, may be null
     * @param jpeg     the callback for JPEG image data, or null
     */
    public void takePicture(@Nullable Camera.ShutterCallback shutter, @Nullable Camera.PictureCallback raw,
                            @Nullable final Camera.PictureCallback postview, @Nullable Camera.PictureCallback jpeg) {
        if (mCamera != null) {
            try {
                mCamera.takePicture(shutter, raw, new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        stopPreview();
                        if (postview != null) {
                            postview.onPictureTaken(data, camera);
                        }
                    }
                }, jpeg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback cb) {
        mPreviewCallback = cb;
    }

    public void setPreviewSize(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(width, height);
            mCamera.setParameters(parameters);
        }
    }

    public void setPictureSize(int width, int height) {
        mPictureWidth = width;
        mPictureHeight = height;
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(width, height);
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 设置焦距
     *
     * @param value [0,android.hardware.Camera.Parameters.getMaxZoom()]
     */
    public void setZoom(int value) {
        mZoom = value;
        if (mCamera == null) {
            return;
        }

        try {
            if (mCamera.getParameters().isZoomSupported()) {
                int maxZoom = mCamera.getParameters().getMaxZoom();
                if (value > maxZoom) {
                    value = maxZoom;
                }
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setZoom(value);
                mCamera.setParameters(parameters);
            } else {
                Log.e(this.getClass().getSimpleName(), "Device zoom no supported.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getZoom() {
        return mZoom;
    }

    /**
     * 获取设备支持的最大缩放值
     *
     * @return
     */
    public int getMaxZoom() {
        if (mCamera == null) {
            Log.e(this.getClass().getSimpleName(), "mCamera == null.");
            return -2;
        }
        if (mCamera.getParameters().isZoomSupported()) {
            return mCamera.getParameters().getMaxZoom();
        }
        return -1;
    }

    public void setParameters(Camera.Parameters parameters) {
        mParameters = parameters;
        if (mCamera != null) {
            mCamera.setParameters(mParameters);
        }
    }

    public Camera.Parameters getParameters() {
        if (mCamera != null) {
            return mCamera.getParameters();
        }
        return mParameters;
    }

    public void setFaceDetectionListener(Camera.FaceDetectionListener listener) {
        mFaceDetectionListener = listener;
    }

    public void startFaceDetection() {
        if (mCamera == null || mFaceDetectionListener == null) return;
        try {
            Camera.Parameters params = mCamera.getParameters();

            // start face detection only *after* preview has started
            if (params.getMaxNumDetectedFaces() > 0) {
                // camera supports face detection, so can start it:
                mCamera.startFaceDetection();
                Log.i("startFaceDetection", "开始人脸检测");
            } else {
                Log.e("startFaceDetection", "该设备不支持人脸检测");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopFaceDetection() {
        if (mCamera == null || mFaceDetectionListener == null) return;
        try {
            mCamera.stopFaceDetection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对焦(直到对焦成功)
     */
    public void autoFocus() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    try {
                        mCamera.autoFocus(LCameraTextureView.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (!success && camera != null) {
            //继续对焦
            try {
                camera.autoFocus(LCameraTextureView.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始调焦
     *
     * @param value [0,android.hardware.Camera.Parameters.getMaxZoom()]
     */
    public void startSmoothZoom(int value) {
        if (mCamera != null) {
            if (mCamera.getParameters().isSmoothZoomSupported()) {
                int maxZoom = mCamera.getParameters().getMaxZoom();
                if (value < 0) {
                    value = 0;
                }
                if (value > maxZoom) {
                    value = maxZoom;
                }
                mCamera.startSmoothZoom(value);
            } else {
                Log.e(this.getClass().getSimpleName(), "Device smooth zoom no supported.");
            }

        } else {
            mSmoothZoomValue = value;
        }
    }

    /**
     * 停止调焦
     */
    public void stopSmoothZoom() {
        if (mCamera != null) {
            if (mCamera.getParameters().isSmoothZoomSupported()) {
                mCamera.stopSmoothZoom();
            } else {
                Log.e(this.getClass().getSimpleName(), "Device smooth zoom no supported.");
            }
        }
    }

    /**
     * 设置调焦回调
     *
     * @param listener
     */
    public void setZoomChangeListener(Camera.OnZoomChangeListener listener) {
        this.mOnZoomChangeListener = listener;
    }

    public void clear() {
//        try {
//            Canvas canvas = lockCanvas();
//            // 清空 Canvas 画黑色
//            canvas.drawColor(Color.BLACK);
//            // 解锁 Canvas，并渲染当前的图像
//            unlockCanvasAndPost(canvas);
////        if (mSurface == null) return;
////        mSurface.updateTexImage();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
