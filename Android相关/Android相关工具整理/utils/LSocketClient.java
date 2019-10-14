package com.blackbox.lerist.utils;


import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lerist on 2017/03/03 0003.
 */

public class LSocketClient {
    private static final String TAG = "LSocketClient";
    private Socket mSocket;
    private String mServerIP;
    private int mServerPort;
    private OnSocketListener onSocketListener;
    private List<OnSocketListener> onSocketListeners = Collections.synchronizedList(new ArrayList<OnSocketListener>());
    private boolean isDataReceiving;
    private BufferedReader reader;
    private DataOutputStream printer;
    private int connectTimeout = 0;//the timeout value, in milliseconds, or zero for no timeout
    private boolean isConnecting;

    public interface OnSocketListener {
        void onConnected();

        void onUnconnected(String errmsg);

        void onReceivedMsg(String msg);

        void onDisconnected();

    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置连接超时时长
     *
     * @param connectTimeout the timeout value, in milliseconds, or zero for no timeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void connectServer(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectSync(ip, port);
            }
        }).start();
    }

    public void connectSync(String ip, int port) {
        if (isConnecting) return;

        isConnecting = true;
        this.mServerIP = ip;
        this.mServerPort = port;
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long startConnectTime = -1;
        try {
            mSocket = new Socket();
            startConnectTime = System.currentTimeMillis();
            mSocket.connect(new InetSocketAddress(ip, port), connectTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            String errmsg = e.getMessage();
            if (startConnectTime != -1 && connectTimeout != 0 && System.currentTimeMillis() - startConnectTime >= connectTimeout) {
                //连接超时
                errmsg = "Connect Timeout.";
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
            printer = new DataOutputStream(mSocket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    //开始接收服务器发来的数据
                    isDataReceiving = true;
                    while (isDataReceiving) {
                        //接收从客户端发送过来的数据
                        if (!isConnected() || reader == null) {
                            isDataReceiving = false;
                            throw new Exception("disconnect.");
                        }
                        String str = "";
                        if ((str = reader.readLine()) != null) {
                            try {
                                if (onSocketListener != null) onSocketListener.onReceivedMsg(str);
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
                        //防止onDisconnected在onConnected之前调用
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                    }
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
        isConnecting = false;
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

    public boolean isConnecting() {
        return isConnecting;
    }

    public void reconnect() {
        connectServer(mServerIP, mServerPort);
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
            if (!mSocket.isInputShutdown())
                mSocket.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (!mSocket.isOutputShutdown())
                mSocket.shutdownOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!mSocket.isClosed())
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

        if (mSocket.isOutputShutdown()) {
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

        if (mSocket.isInputShutdown()) {
            return null;
        }

        try {
            return mSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getServerIP() {
        return mServerIP;
    }

    public int getServerPort() {
        return mServerPort;
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

        if (mSocket.isClosed()) {
            Log.e(TAG, "Socket is closed.");
            return false;
        }

        if (!mSocket.isConnected()) {
            Log.e(TAG, "Socket is disconnected.");
            return false;
        }

        if (mSocket.isOutputShutdown()) {
            Log.e(TAG, "Socket is output shutdown.");
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
            if (mSocket.isConnected() && !mSocket.isClosed() && !mSocket.isInputShutdown() && !mSocket.isOutputShutdown()) {
                mSocket.sendUrgentData(0xFF);
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
