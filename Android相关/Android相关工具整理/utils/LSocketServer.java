package com.blackbox.lerist.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lerist on 2017/03/28 0028.
 */

public class LSocketServer {
    private final int mPort;
    private ExecutorService mExecutorService;
    private final List<Socket> mConnectedSockets;
    private boolean isRuning;
    private ServerSocket serverSocket;
    private OnSocketServerListener onSocketServerListener;
    private int maxClientCount = -1;//最大连接数, -1为无限制

    public interface OnSocketServerListener {
        void onStartServer();

        void onUnstartServer(String message);

        void onAccepted(Socket socket);

        void onRejected(Socket socket);

        void onReceivedMsg(Socket socket, String msg);

        void onSentMsg(Socket targetSocket, String msg);

        void onUnsentMsg(Socket targetSocket, String msg);

        void onDisconnected(Socket socket);

        void onStopServer();
    }

    public LSocketServer(int port) {
        this.mPort = port;
        mConnectedSockets = Collections.synchronizedList(new ArrayList<Socket>());
    }

    public int getMaxClientCount() {
        return maxClientCount;
    }

    public void setMaxClientCount(int maxClientCount) {
        this.maxClientCount = maxClientCount;
    }

    public void setOnSocketServerListener(OnSocketServerListener onSocketServerListener) {
        this.onSocketServerListener = onSocketServerListener;
    }

    public void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncStartServer();
            }
        }).start();
    }

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

    public void stopServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncStopServer();
            }
        }).start();
    }

    public synchronized void syncStartServer() {
        if (isRuning) {
            return;
        }
        isRuning = true;

        try {
            serverSocket = new ServerSocket(mPort);
            mExecutorService = Executors.newCachedThreadPool();
            mConnectedSockets.clear();
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onStartServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //服务已启动
            while (isRuning) {
                if (serverSocket.isClosed()) {
                    syncStopServer();
                    break;
                }
                Socket socket = null;
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
            isRuning = false;
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onUnstartServer(e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public synchronized void syncStopServer() {
//        if (!isRuning) return;
        isRuning = false;
        forceStopServer();
        try {
            if (onSocketServerListener != null)
                onSocketServerListener.onStopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if (mExecutorService != null)
                mExecutorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientMsgReceiver implements Runnable {

        private final Socket socket;
        private BufferedReader in;

        public ClientMsgReceiver(Socket socket) {
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

    public List<Socket> getConnectedSockets() {
        return mConnectedSockets;
    }

    public boolean disconnect(Socket socket) {
        try {
            socket.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    Object lock = new Object();

    private void onReceivedMsg(Socket socket, String msg) {
        synchronized (lock) {
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onReceivedMsg(socket, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean sendMsg(Socket targetSocket, String msg) {
        try {
            PrintWriter pout = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(targetSocket.getOutputStream())), true);
            pout.println(msg);
            try {
                if (onSocketServerListener != null)
                    onSocketServerListener.onSentMsg(targetSocket, msg);
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
                onSocketServerListener.onUnsentMsg(targetSocket, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isConnected(Socket socket) {
        try {
            if (socket == null) return false;
            if (socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                socket.sendUrgentData(0xFF);
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}
