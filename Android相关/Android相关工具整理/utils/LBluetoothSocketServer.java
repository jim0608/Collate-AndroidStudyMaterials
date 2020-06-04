package com.blackbox.lerist.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Lerist on 2017/08/26 0028.
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 */

public class LBluetoothSocketServer {
    private static final String NAME = "BluetoothServer";//部分蓝牙设备最大仅支持20个字符
    private final Context context;
    private UUID mUUID;
    private ExecutorService mExecutorService;
    private final List<BluetoothSocket> mConnectedSockets;
    private boolean isRuning;
    private BluetoothServerSocket serverSocket;
    private OnSocketServerListener onSocketServerListener;
    private int maxClientCount = -1;//最大连接数, -1为无限制
    private boolean isStarted;
    private Future<?> acceptFuture;
    private final BluetoothAdapter mBluetoothAdapter;

    public interface OnSocketServerListener {
        void onStartServer();

        void onUnstartServer(String message);

        void onAccepted(BluetoothSocket socket);

        void onRejected(BluetoothSocket socket);

        void onReceivedMsg(BluetoothSocket socket, String msg);

        void onSentMsg(BluetoothSocket targetSocket, String msg);

        void onUnsentMsg(BluetoothSocket targetSocket, String msg);

        void onDisconnected(BluetoothSocket socket);

        void onStopServer();
    }

    public LBluetoothSocketServer(Context context, UUID uuid) {
        this.context = context;
        this.mUUID = uuid;
        mConnectedSockets = Collections.synchronizedList(new ArrayList<BluetoothSocket>());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public UUID getUUID() {
        return mUUID;
    }

    public void setUUID(UUID uuid) {
        this.mUUID = uuid;
    }

    public int getMaxClientCount() {
        return maxClientCount;
    }

    /**
     * 设置最大连接数
     *
     * @param maxClientCount
     */
    public void setMaxClientCount(int maxClientCount) {
        this.maxClientCount = maxClientCount;
    }

    public void setOnSocketServerListener(OnSocketServerListener onSocketServerListener) {
        this.onSocketServerListener = onSocketServerListener;
    }

    /**
     * 启动服务(异步)
     */
    public void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncStartServer();
            }
        }).start();
    }

    /**
     * 重启服务(异步)
     */
    public void restartServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                forceStopServer();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                syncStartServer();
            }
        }).start();
    }

    /**
     * 停止服务(异步)
     */
    public void stopServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncStopServer();
            }
        }).start();
    }

    /**
     * 启动服务(同步)
     */
    public synchronized void syncStartServer() {
        if (isRuning) {
            return;
        }
        isRuning = true;
        try {
            if (acceptFuture != null) {
                try {
                    acceptFuture.cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mExecutorService != null) {
                try {
                    mExecutorService.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                }
                serverSocket = null;
            }
            isStarted = false;
            if (mBluetoothAdapter == null) {
                throw new Exception("the device doesn't support bluetooth.");
            }
            long startCheckTime = System.currentTimeMillis();
            //开启蓝牙(最多8s)
            while (!mBluetoothAdapter.isEnabled() && System.currentTimeMillis() - startCheckTime < 8000) {
                mBluetoothAdapter.enable();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!mBluetoothAdapter.isEnabled()) {
                throw new Exception("bluetooth is off.");
            }

            //打开本机的蓝牙发现功能（默认打开120秒，可以将时间最多延长至300秒）
            try {
                Intent discoverableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(
                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                context.startActivity(discoverableIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }

            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, mUUID);
            mExecutorService = Executors.newCachedThreadPool();
            mConnectedSockets.clear();
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onStartServer();
            } catch (Exception e) {
                e.printStackTrace();
            }

            acceptFuture = mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        isStarted = true;
                        //服务已启动
                        while (isRuning) {
                            if (serverSocket == null) {
                                syncStopServer();
                                break;
                            }
                            BluetoothSocket socket = null;
                            try {
                                socket = serverSocket.accept();
                            } catch (Exception e) {
                                //                    e.printStackTrace();
                                continue;
                            }
                            if (socket == null) continue;
                            //限制连接数
                            if (maxClientCount == -1 || mConnectedSockets.size() < maxClientCount) {
                                mConnectedSockets.add(socket);
                                try {
                                    if (onSocketServerListener != null)
                                        onSocketServerListener.onAccepted(socket);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mExecutorService.execute(new ClientMsgReceiver(socket));
                            } else {
                                try {
                                    if (onSocketServerListener != null)
                                        onSocketServerListener.onRejected(socket);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    socket.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            isRuning = false;
            isStarted = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                serverSocket = null;
            }
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onUnstartServer(e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 停止服务(同步)
     */
    public synchronized void syncStopServer() {
//        if (!isRuning) return;
        isRuning = false;
        isStarted = false;
        forceStopServer();
        try {
            if (onSocketServerListener != null)
                onSocketServerListener.onStopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 强制停止服务(不会触发回调)
     */
    private void forceStopServer() {
        try {
            if (mConnectedSockets != null) {
                for (int i = 0; i < mConnectedSockets.size(); i++) {
                    disconnect(mConnectedSockets.get(i));
                }
                mConnectedSockets.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (acceptFuture != null) {
            try {
                acceptFuture.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mExecutorService != null) {
            try {
                mExecutorService.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
    }

    /**
     * 服务是否正在运行
     *
     * @return
     */
    public boolean isRuning() {
        return isRuning && isStarted;
    }

    /**
     * 客户端消息接收器
     */
    class ClientMsgReceiver implements Runnable {

        private final BluetoothSocket socket;
        private BufferedReader in;

        public ClientMsgReceiver(BluetoothSocket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (isRuning) {
                    if (isConnected(socket)) {
                        String msg;
                        if ((msg = in.readLine()) != null) {
                            //当客户端发送的信息为：exit时，关闭连接
                            if (msg.equals("exit")) {
                                throw new Exception("exit.");
                            } else {
                                onReceivedMsg(socket, msg);
                            }
                        }
                    } else {
                        throw new Exception("disconnected.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mConnectedSockets.remove(socket);
                try {
                    in.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    socket.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    if (onSocketServerListener != null)
                        onSocketServerListener.onDisconnected(socket);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public List<BluetoothSocket> getConnectedSockets() {
        return mConnectedSockets;
    }

    /**
     * 断开客户端连接
     *
     * @param socket
     * @return
     */
    public boolean disconnect(BluetoothSocket socket) {
        try {
            socket.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    Object lock = new Object();

    private void onReceivedMsg(BluetoothSocket socket, String msg) {
        Log.i(NAME, msg);
        synchronized (lock) {
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onReceivedMsg(socket, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 向客户端发送字符串消息
     *
     * @param targetSocket
     * @param msg
     * @return
     */
    public boolean sendMsg(BluetoothSocket targetSocket, String msg) {
        Log.i(NAME, msg);
        return sendBytes(targetSocket, (msg + "\n").getBytes());
    }

    /**
     * 向客户端发送字节数据
     *
     * @param targetSocket
     * @param bytes
     * @return
     */
    public boolean sendBytes(BluetoothSocket targetSocket, byte[] bytes) {
        try {
            if (targetSocket == null) return false;

            DataOutputStream pout = new DataOutputStream(targetSocket.getOutputStream());
            pout.write(bytes);
            pout.flush();
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onSentMsg(targetSocket, new String(bytes));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //注意, 不能关闭流
//          pout.close();
            return true;
        } catch (Exception e) {

            e.printStackTrace();
        }
        try {
            if (onSocketServerListener != null)
                onSocketServerListener.onUnsentMsg(targetSocket, new String(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 判断该客户端是否已连接
     *
     * @param socket
     * @return
     */
    public boolean isConnected(BluetoothSocket socket) {
        try {
            if (socket == null) return false;
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                return socket.isConnected();
            } else {
                try {
                    Field fld = BluetoothSocket.class.getDeclaredField("mClosed");
                    fld.setAccessible(true);
                    return fld.getBoolean(socket);
                } catch (Exception e) {
                }
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    /**
     * 获取本机蓝牙设备地址
     *
     * @param context
     * @return
     */
    public String getHostAddress(Context context) {
        String macAddress = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        return macAddress;
    }

    boolean isStartHeart;
    boolean isHeartStarting;

    /**
     * 启动心跳
     */
    public synchronized void startHeart(final BluetoothSocket socket, String heart, final long timeout) {
        final String msg = heart;
        new Thread(new Runnable() {
            @Override
            public void run() {
                isStartHeart = true;
                while (isStartHeart) {
                    isHeartStarting = true;
                    if (isConnected(socket)) {
                        boolean isSended = sendMsg(socket, msg);
                    } else {
                        if (onSocketServerListener != null)
                            onSocketServerListener.onDisconnected(socket);
                        return;
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
}
