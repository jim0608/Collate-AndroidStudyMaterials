package com.blackbox.lerist.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import java.util.Arrays;

/**
 * Created by Lerist on 2016/1/5, 0005.
 */
public class LOrientationUtils implements SensorEventListener {
    private final Context context;
    private final OrientationListener orientationListener;
    private Handler handler = new Handler();
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    private float[] orientationValues = new float[3];
    private SensorManager sensorManager;
    public static Orientation orientation;
    private float OFFSET = 0;

    public static LOrientationUtils getNewInstance(Context context, OrientationListener orientationListener) {
        LOrientationUtils LOrientationUtils = new LOrientationUtils(context, orientationListener);
        return LOrientationUtils;
    }

    public interface OrientationListener {
        void onOrientationChanged(Orientation orientation);
    }

    public LOrientationUtils(Context context, OrientationListener orientationListener) {
        this.context = context;
        this.orientationListener = orientationListener;
    }

    private Runnable updateThread = new Runnable() {
        @Override
        public void run() {
            if (orientation != null)
                orientationListener.onOrientationChanged(orientation);

//            handler.postDelayed(updateThread, 20);
        }
    };

    /**
     * 请求方向
     */
    public void requestOrientation() {
        if (sensorManager != null) return;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

//        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);

//        handler.postDelayed(updateThread, 20);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldValues = event.values;
                break;
            case Sensor.TYPE_ORIENTATION:
                orientationValues = event.values;
                break;
        }
        float[] values = orientationValues;

//        float[] values = new float[3];
//        float[] R = new float[9];
//         SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
//         SensorManager.getOrientation(R, values);
        // 经过一次数据格式的转换，转换为度
//        values[0] = (float) Math.toDegrees(values[0]);
//        KLog.i(values[0]);
//        values[0] = normalizeDegree(values[0] * -0.1f);
//        KLog.i(values[0]);
//        values[1] = (float) Math.toDegrees(values[1]);
//        values[2] = (float) Math.toDegrees(values[2]);

        if (orientation == null)
            orientation = new Orientation();

        orientation.setValues(values);
        //z 方向角 values[0]
        //x 俯仰角 values[1]
        //y 翻转角 values[2]

//        //俯仰角[-180,180] , 转为[0,360]
//        values[1] = values[1] < 0 ? 360 + values[1] : values[1];

        if (Math.abs(orientation.getAxisX() - values[1]) > OFFSET)
            orientation.setAxisX(values[1]);
        if (Math.abs(orientation.getAxisY() - values[2]) > OFFSET)
            orientation.setAxisY(values[2]);
        if (Math.abs(orientation.getAxisZ() - values[0]) > OFFSET)
            orientation.setAxisZ(values[0]);

        orientationListener.onOrientationChanged(orientation);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 停止请求
     */
    public void stopRequest() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
    }

    // 调整方向传感器获取的值
    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    public static class Orientation {

        public void set(Orientation orientation) {
            if (orientation == null) {
                this.axisX = 0;
                this.axisY = 0;
                this.axisZ = 0;
                this.values = null;
                return;
            }
            this.axisX = orientation.axisX;
            this.axisY = orientation.axisY;
            this.axisZ = orientation.axisZ;
            this.values = orientation.values;
        }

        private float[] values; //原始数据

        /**
         * azimuth, rotation around the -Z axis, i.e. the opposite direction of Z axis
         */
        private float axisZ;

        /**
         * pitch, rotation around the -X axis, i.e the opposite direction of X axis.
         */
        private float axisX;

        /**
         * roll, rotation around the Y axis.
         */
        private float axisY;

        public float[] getValues() {
            return values;
        }

        public void setValues(float[] values) {
            this.values = values;
        }

        public float getAxisX() {
            return axisX;
        }

        public void setAxisX(float axisX) {
            this.axisX = axisX;
        }

        public float getAxisY() {
            return axisY;
        }

        public void setAxisY(float axisY) {
            this.axisY = axisY;
        }

        public float getAxisZ() {
            return axisZ;
        }

        public void setAxisZ(float axisZ) {
            this.axisZ = axisZ;
        }

        @Override
        public String toString() {
            return "Orientation{" +
                    "values=" + Arrays.toString(values) +
                    ", axisZ=" + axisZ +
                    ", axisX=" + axisX +
                    ", axisY=" + axisY +
                    '}';
        }
    }
}
