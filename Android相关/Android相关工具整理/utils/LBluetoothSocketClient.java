package com.blackbox.lerist.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Lerist on 2017/03/03 0003.
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 */

public class LBluetoothSocketClient {
    private static final String TAG = "LBluetoothSocketClient";
    private final Context mContext;
    private BluetoothSocket mSocket;
    private BluetoothDevice mServer;
    private String mServerAddress;
    private UUID mServerUUID;
    private OnSocketListener onSocketListener;
    private List<OnSocketListener> onSocketListeners = Collections.synchronizedList(new ArrayList<OnSocketListener>());
    private boolean isDataReceiving;
    private BufferedReader reader;
    private DataOutputStream printer;
    private int discoveryTimeout = 15000;//the timeout value, in milliseconds, or zero for no discovery
    private final BluetoothAdapter mBluetoothAdapter;

    public interface OnSocketListener {


        void onStartDiscovery(boolean isSuccess);

        void onDiscoveredDevice(BluetoothDevice device);

        void onUndiscoveredDevice(String targetAddress);

        void onConnected();

        void onUnconnected(String errmsg);

        void onReceivedMsg(String msg);

        void onDisconnected();

    }

    public LBluetoothSocketClient(Context context) {
        this.mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getDiscoveryTimeout() {
        return discoveryTimeout;
    }

    /**
     * 设置扫描蓝牙设备超时时长
     *
     * @param discoveryTimeout the timeout value, in milliseconds, or zero for no discovery
     */
    public void setDiscoveryTimeout(int discoveryTimeout) {
        this.discoveryTimeout = discoveryTimeout;
    }

    public void connectServer(final String serverAddress, final UUID serverUUID) {
        this.mServerAddress = serverAddress;
        this.mServerUUID = serverUUID;
        new Thread(new Runnable() {
            @Override
            public void run() {
                scan(mContext, serverAddress);
            }
        }).start();
    }

    private void scan(final Context context, final String targetAddress) {
        try {
            if (targetAddress == null) {
                Log.e(TAG, "scan(): targetAddress == null");
                throw new Exception("scan(): targetAddress == null");
            }
            if (mBluetoothAdapter == null) {
                throw new Exception("the device doesn't support bluetooth.");
            }

            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            //重已绑定的列表获取设备
            if (bondedDevices != null) {
                for (final BluetoothDevice device : bondedDevices) {
                    if (targetAddress.equals(device.getAddress())||targetAddress.equals(device.getName())) {
                        //目标设备已绑定， 直接连接
                        try {
                            if (onSocketListener != null)
                                onSocketListener.onDiscoveredDevice(device);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        for (int i = 0; i < onSocketListeners.size(); i++) {
                            OnSocketListener socketCallback = onSocketListeners.get(i);
                            try {
                                socketCallback.onDiscoveredDevice(device);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        //连接设备
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                connect(device, mServerUUID);
                            }
                        }).start();
                        return;
                    }
                }
            }
            //未绑定， 则重新扫描绑定连接
            final Handler handler = new Handler(Looper.getMainLooper());
            final BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.e("onReceive", intent.toString());
                    String action = intent.getAction();
                    // 发现设备
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // 从Intent中获取设备对象
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (targetAddress.equals(device.getAddress())) {
                            //找到目标设备
                            handler.removeMessages(0);
                            try {
                                context.unregisterReceiver(this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                if (onSocketListener != null)
                                    onSocketListener.onDiscoveredDevice(device);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            for (int i = 0; i < onSocketListeners.size(); i++) {
                                OnSocketListener socketCallback = onSocketListeners.get(i);
                                try {
                                    socketCallback.onDiscoveredDevice(device);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                            //连接设备
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    connect(device, mServerUUID);
                                }
                            }).start();
                        }
                    }
                }
            };
            try {
                context.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } catch (Exception e) {
                e.printStackTrace();
            }
            long startCheckTime = System.currentTimeMillis();
            //开启蓝牙(最多5s/discoveryTimeout)
            while (!mBluetoothAdapter.isEnabled() && System.currentTimeMillis() - startCheckTime < Math.max(5000, discoveryTimeout)) {
                mBluetoothAdapter.enable();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //开始扫描蓝牙设备
            boolean startDiscovery = mBluetoothAdapter.startDiscovery();
            Log.i(TAG, "startDiscovery is " + startDiscovery);
            try {
                if (onSocketListener != null)
                    onSocketListener.onStartDiscovery(startDiscovery);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (int i = 0; i < onSocketListeners.size(); i++) {
                OnSocketListener socketCallback = onSocketListeners.get(i);
                try {
                    socketCallback.onStartDiscovery(startDiscovery);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            //扫描超时后停止扫描
            handler.removeMessages(0);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //未发现目标设备
                    //停止扫描
                    mBluetoothAdapter.cancelDiscovery();
                    try {
                        //取消广播接收
                        context.unregisterReceiver(receiver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        if (onSocketListener != null)
                            onSocketListener.onUndiscoveredDevice(targetAddress);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    for (int i = 0; i < onSocketListeners.size(); i++) {
                        OnSocketListener socketCallback = onSocketListeners.get(i);
                        try {
                            socketCallback.onUndiscoveredDevice(targetAddress);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }, discoveryTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (onSocketListener != null)
                    onSocketListener.onUnconnected(e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (int i = 0; i < onSocketListeners.size(); i++) {
                OnSocketListener socketCallback = onSocketListeners.get(i);
                try {
                    socketCallback.onUnconnected(e.getMessage());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void connect(BluetoothDevice device, UUID serverUUID) {
        this.mServer = device;
        this.mServerUUID = serverUUID;

        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            mSocket = device.createRfcommSocketToServiceRecord(mServerUUID);
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
            String errmsg = e.getMessage();
            if (mSocket != null) {
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            try {
                if (onSocketListener != null) onSocketListener.onUnconnected(errmsg);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            for (int i = 0; i < onSocketListeners.size(); i++) {
                OnSocketListener socketCallback = onSocketListeners.get(i);
                try {
                    socketCallback.onUnconnected(errmsg);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return;
        }
        try {
            reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            printer = new DataOutputStream(mSocket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //开始接收服务器发来的数据
                    isDataReceiving = true;
                    while (isDataReceiving) {
                        //接收从客户端发送过来的数据
                        if (!isConnected()) {
                            isDataReceiving = false;
                            throw new Exception("disconnect.");
                        }
                        String str = "";
                        if ((str = reader.readLine()) != null) {
                            try {
                                if (onSocketListener != null)
                                    onSocketListener.onReceivedMsg(str);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < onSocketListeners.size(); i++) {
                                OnSocketListener socketCallback = onSocketListeners.get(i);
                                try {
                                    socketCallback.onReceivedMsg(str);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

//                            if ("exit".equals(str)) {
//                                isDataReceiving = false;
//                                try {
//                                    if (onSocketListener != null) onSocketListener.onDisconnected();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                for (int i = 0; i < onSocketListeners.size(); i++) {
//                                    OnSocketListener socketCallback = onSocketListeners.get(i);
//                                    try {
//                                        socketCallback.onDisconnected();
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (onSocketListener != null) onSocketListener.onDisconnected();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    for (int i = 0; i < onSocketListeners.size(); i++) {
                        OnSocketListener socketCallback = onSocketListeners.get(i);
                        try {
                            socketCallback.onDisconnected();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }).start();
        try {
            if (onSocketListener != null) onSocketListener.onConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < onSocketListeners.size(); i++) {
            OnSocketListener socketCallback = onSocketListeners.get(i);
            try {
                socketCallback.onConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void reconnect() {
        connectServer(mServerAddress, mServerUUID);
    }

    public void disconnectServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectSync();
            }
        }).start();
    }

    public void disconnectSync() {
//        isDataReceiving = false;
        stopHeart();
//        sendText("exit");
        if (mSocket == null) return;
        try {
            mSocket.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mSocket.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket = null;
        closeIO(printer, reader);
    }

    public OutputStream getOutputStream() {
        if (mSocket == null) {
            return null;
        }


        try {
            return mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getInputStream() {
        if (mSocket == null) {
            return null;
        }

        try {
            return mSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BluetoothDevice getServer() {
        return mServer;
    }

    public String getServerAddress() {
        return mServerAddress;
    }

    public UUID getServerUUID() {
        return mServerUUID;
    }

    public void sendText(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncSendText(text);
            }
        }).start();
    }

    public void sendBytes(final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncSendBytes(bytes);
            }
        }).start();
    }

    public boolean syncSendText(String text) {
        Log.d(TAG, "syncSendText:" + text);
        return syncSendBytes((text + "\n").getBytes());
    }

    public boolean syncSendBytes(byte[] bytes) {
        if (bytes == null) return false;

        if (mSocket == null) {
            Log.e(TAG, "Socket is null.");
            return false;
        }

        if (!mSocket.isConnected()) {
            Log.e(TAG, "Socket is disconnected.");
            return false;
        }

        boolean isSended = false;
        try {
            printer.write(bytes);
//            printer.println();
            printer.flush();
//            printer.println(URLEncoder.encode(text,"utf-8"));  // 写一个UTF-8的信息
//            printer.flush();
            isSended = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return isSended;
    }

    public void setOnSocketListener(OnSocketListener onSocketListener) {
        this.onSocketListener = onSocketListener;
    }

    public void addOnSocketListener(OnSocketListener onSocketListener) {
        synchronized (lock) {
            this.onSocketListeners.add(onSocketListener);
        }
    }

    Object lock = new Object();

    public void removeOnSocketListener(OnSocketListener onSocketListener) {
        synchronized (lock) {
            this.onSocketListeners.remove(onSocketListener);
        }
    }

    boolean isStartHeart;
    boolean isHeartStarting;

    /**
     * 启动心跳
     */
    public synchronized void startHeart(String heart, final long timeout) {
        if (isHeartStarting) return;
        isStartHeart = true;
        final String msg = heart;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStartHeart) {
                    isHeartStarting = true;
                    if (isConnected()) {
                        boolean isSended = syncSendText(msg);
                    } else {
                        if (onSocketListener != null) onSocketListener.onDisconnected();
                        //重新连接
                        reconnect();
                    }

                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isHeartStarting = false;
            }
        }).start();
    }

    public synchronized void stopHeart() {
        isStartHeart = false;
    }

    public boolean isConnected() {
        try {
            if (mSocket == null) return false;
            if (mSocket.isConnected()) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
