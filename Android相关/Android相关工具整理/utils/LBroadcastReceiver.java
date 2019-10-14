package com.blackbox.lerist.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Lerist on 2017/09/07 0007.
 * 广播接收器
 */

public class LBroadcastReceiver {

    private final Context context;
    private BroadcastReceiver broadcastReceiver;

    public interface OnReceiveListener {
        void onReceive(Context context, Intent intent);
    }

    public LBroadcastReceiver(Context context) {
        this.context = context;
    }

    public void receiver(String[] actions, final OnReceiveListener onReceiveListener) {
        if (actions == null || onReceiveListener == null) return;
        crashreceiver();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (onReceiveListener != null) onReceiveListener.onReceive(context, intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        try {
            context.registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        broadcastReceiver = null;
    }

    public void crashreceiver() {
        try {
            if (broadcastReceiver != null) context.unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
