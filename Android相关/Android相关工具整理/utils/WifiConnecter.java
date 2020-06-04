package com.blackbox.lerist.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lerist on 2017/10/09 0009.
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/> //6.0+
 * <uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS"/>
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 */

public class WifiConnecter {

    public static final String TAG = "WifiConnecter";

    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    private final Context mContext;
    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;
    private OnScanListener mOnScanListener;
    private boolean isScaning;
    private Handler mWifiListRefreshHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try {
                refreshWifiList();
            } catch (Exception e) {
                e.printStackTrace();
                if (mOnScanListener != null) mOnScanListener.onScanError(e.getMessage());
            }
            if (isScaning)
                sendEmptyMessageDelayed(0, mScanIntervalMillis);
        }
    };
    private OnConnectListener mOnConnectListener;
    private long mScanIntervalMillis = 1000;
    private WifiDevice mConnectingWifiDevice;

    private void refreshWifiList() {
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        List<WifiDevice> wifiDevices = null;
        if (scanResults != null) {
            wifiDevices = new ArrayList<>();
            for (ScanResult scanResult : scanResults) {
                WifiDevice wifiDevice = new WifiDevice(scanResult);
                WifiInfo connectionInfo = getConnectionInfo();
                if (connectionInfo != null) {
                    if (connectionInfo.getSSID().equals(scanResult.SSID)) {
                        wifiDevice.state = WifiDevice.STATE_CONNECTED;
                    } else {
                        wifiDevice.state = WifiDevice.STATE_NORMAL;
                        if (mStateChangedDevice != null) {
                            if (mStateChangedDevice.SSID.equals(/*"\"" + */scanResult.SSID /*+ "\""*/)) {
                                wifiDevice.state = mStateChangedDevice.state;
                            }
                        }
                    }
                }
                wifiDevice.isConfigured = isConfigured(scanResult.SSID) != null;
                //排序, 顺序依次为: 已连接, 已保存, 其他
                if (wifiDevice.state == WifiDevice.STATE_CONNECTED) {
                    wifiDevices.add(0, wifiDevice);
                } else if (wifiDevice.isConfigured) {
                    wifiDevices.add(wifiDevices.isEmpty() ? 0 : 1, wifiDevice);
                } else {
                    wifiDevices.add(wifiDevice);
                }
            }
        }

        if (mOnScanListener != null) mOnScanListener.onScan(wifiDevices);
    }

    public interface OnConnectListener {
        void onStartConnection(WifiDevice wifiDevice);

        void onConnecting(WifiInfo wifiInfo);

        void onConnected(WifiInfo wifiInfo);

        void onConnectionFailed(WifiDevice wifiDevice, int connectWifiResult);

        void onError(String msg);
    }

    public interface OnScanListener {
        void onStartScan();

        void onStopScan();

        void onScan(@Nullable List<WifiDevice> wifiDevices);

        void onScanError(String msg);

    }

    public WifiConnecter(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public boolean isWifiOpend() {
        return mWifiManager.isWifiEnabled();
    }

    public void openWifi() {
        if (!isWifiOpend()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public void closeWifi() {
        if (isWifiOpend()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    LLog.e("WIFI_STATE_CHANGED_ACTION");
                    handleWifiStateChanged(intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    LLog.e("SUPPLICANT_STATE_CHANGED_ACTION");
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -100);
                    LLog.e("密码认证错误：" + error);
                    if (error == WifiManager.ERROR_AUTHENTICATING) {
                        //wifi密码认证错误！
                        if (mConnectingWifiDevice != null && mStateChangedDevice != null && mOnConnectListener != null) {
                            mStateChangedDevice = new WifiDevice();
                            mStateChangedDevice.BSSID = mConnectingWifiDevice.BSSID;
                            mStateChangedDevice.SSID = mConnectingWifiDevice.SSID;
                            mStateChangedDevice.state = WifiDevice.STATE_PASSWORD_ERROR;
                            mOnConnectListener.onConnectionFailed(mStateChangedDevice, WifiDevice.STATE_PASSWORD_ERROR);
                            mOnConnectListener = null;
                        }
                    }
                    handleStateChanged(networkInfo, wifiInfo);
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    LLog.e("NETWORK_STATE_CHANGED_ACTION");
                    NetworkInfo networkInfo1 = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    WifiInfo wifiInfo1 = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    handleStateChanged(networkInfo1, wifiInfo1);
                    break;
            }
        }
    };


    private WifiDevice mStateChangedDevice;

    private void handleStateChanged(NetworkInfo info, WifiInfo wifiInfo) {
//        if (connectWifiResult == WifiManager.ERROR_AUTHENTICATING) {
//            //密码错误
//            if (mConnectingWifiDevice != null) {
//                mStateChangedDevice = new WifiDevice();
//                mStateChangedDevice.BSSID = mConnectingWifiDevice.BSSID;
//                mStateChangedDevice.SSID = mConnectingWifiDevice.SSID;
//                mStateChangedDevice.state = WifiDevice.STATE_PASSWORD_ERROR;
//            }
//            if (mOnConnectListener != null)
//                mOnConnectListener.onConnectionFailed(mStateChangedDevice, connectWifiResult);
//            mOnConnectListener = null;
//            mStateChangedDevice = null;
//            return;
//        }

        if (info == null /*|| wifiInfo == null*/) {
            Log.e(TAG, "NetworkInfo:" + info + " , " + "wifiInfo: " + wifiInfo);
            return;
        }
        LLog.e("NetworkInfo:" + info + " , " + "wifiInfo: " + wifiInfo);
//        if (wifiInfo != null) {
//            LLog.e(wifiInfo.getSupplicantState());
//            switch (wifiInfo.getSupplicantState()) {
//                case DISCONNECTED:
//                    break;
//                case UNINITIALIZED:
//            }
//        }

        NetworkInfo.DetailedState networkInfoState = info.getDetailedState();
        switch (networkInfoState) {
            case CONNECTING:
                //正在连接...
                if (mConnectingWifiDevice != null) {
                    mStateChangedDevice = new WifiDevice();
                    mStateChangedDevice.BSSID = mConnectingWifiDevice.BSSID;
                    mStateChangedDevice.SSID = mConnectingWifiDevice.SSID;
                    mStateChangedDevice.state = WifiDevice.STATE_CONNECTING;
                }
                if (mOnConnectListener != null)
                    mOnConnectListener.onConnecting(wifiInfo);
                break;
            case OBTAINING_IPADDR:
                //正在获取IP地址

                break;
            case CONNECTED:
                //已连接
                if (wifiInfo != null) {
                    mStateChangedDevice = new WifiDevice();
                    mStateChangedDevice.BSSID = wifiInfo.getBSSID();
                    mStateChangedDevice.SSID = wifiInfo.getSSID() != null ? wifiInfo.getSSID().replaceAll("\"", "") : null;
                    mStateChangedDevice.state = WifiDevice.STATE_CONNECTED;
                }
                if (mOnConnectListener != null)
                    mOnConnectListener.onConnected(wifiInfo);
                mOnConnectListener = null;
                break;
            case DISCONNECTING:
                mStateChangedDevice = null;
                break;
            case DISCONNECTED:
                //密码错误
//                if (mConnectingWifiDevice != null && mStateChangedDevice != null && mOnConnectListener != null) {
//                    mStateChangedDevice = new WifiDevice();
//                    mStateChangedDevice.BSSID = mConnectingWifiDevice.BSSID;
//                    mStateChangedDevice.SSID = mConnectingWifiDevice.SSID;
//                    mStateChangedDevice.state = WifiDevice.STATE_PASSWORD_ERROR;
//                    mOnConnectListener.onConnectionFailed(mStateChangedDevice, WifiDevice.STATE_PASSWORD_ERROR);
//                    mOnConnectListener = null;
//                }
                mStateChangedDevice = null;

                break;
            case SUSPENDED:
                mStateChangedDevice = null;
                break;
            case FAILED:
                //连接失败
                mStateChangedDevice = null;
                break;
        }
    }

    private void handleWifiStateChanged(int wifiState) {
        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
                //wifi已关闭
                Log.e("WIFI状态", "wifiState:WIFI_STATE_DISABLED");
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                //正在关闭wifi
                Log.e("WIFI状态", "wifiState:WIFI_STATE_DISABLING");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                //wifi已打开
                Log.e("WIFI状态", "wifiState:WIFI_STATE_ENABLED");
                if (isScaning) {
                    scanWifi();
                }
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                //正在打开wifi
                Log.e("WIFI状态", "wifiState:WIFI_STATE_ENABLING");
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                //未知状态
                Log.e("WIFI状态", "wifiState:WIFI_STATE_UNKNOWN");
                break;
        }
    }

    public WifiInfo getConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    public final void toggleGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));//0-wifi,1-brightness,2-sync,3-gps,4-bluetooth
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 位置信息是否打开
     *
     * @return
     */
    private boolean isLocationEnabled() {
        boolean isLocationEnabled = false;
        try {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) /*|| locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)*/;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isLocationEnabled;
    }

    public void startScan(long intervalMillis, OnScanListener onScanListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isLocationEnabled()) {
//            toggleGPS(mContext);
                try {
                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(settingsIntent);
                    Toast.makeText(mContext, "请开启[位置信息], 以获取WIFI列表", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        this.mScanIntervalMillis = intervalMillis;
        this.mOnScanListener = null;
        stopScan();
        this.mOnScanListener = onScanListener;

        try {
            mContext.registerReceiver(mWifiStateReceiver, mIntentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isScaning = true;
        try {
            if (mOnScanListener != null) mOnScanListener.onStartScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanWifi();
        //开始刷新扫描到的设备
        mWifiListRefreshHandler.sendEmptyMessage(0);
    }

    private void scanWifi() {
        mWifiManager.startScan();
    }

    public void stopScan() {
        isScaning = false;
        mWifiListRefreshHandler.removeMessages(0);
        try {
            this.mContext.unregisterReceiver(this.mWifiStateReceiver);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        if (mOnScanListener != null) mOnScanListener.onStopScan();
        mOnScanListener = null;
    }

    public void connect(final WifiDevice device, String password, final OnConnectListener onConnectListener) {
        this.mOnConnectListener = onConnectListener;
        this.mConnectingWifiDevice = device;
        if (mOnConnectListener != null)
            mOnConnectListener.onStartConnection(device);

        WifiConfiguration configuredNetwork = isConfigured(device.SSID);
        if (configuredNetwork != null) {
//记住过的密码这里传递了null，所以这边密码对比总是返回false，导致每次切换WIFI密码被忘记（更新）。
//            if (!isPasswordChanged(configuredNetwork, password)) {
            //已配置过, 并且密码未改变, 直接连接
            try {
                mWifiManager.enableNetwork(configuredNetwork.networkId, true);
            } catch (Exception e) {
                e.printStackTrace();
                if (mOnConnectListener != null)
                    mOnConnectListener.onError(e.getMessage());
                mOnConnectListener = null;
            }
//            } else {
//                //已配置过, 并且密码已改变, 更新配置后连接
//                WifiConfiguration configuration = createWifiConfiguration(device.SSID, device.BSSID, password, TextUtils.isEmpty(password) ? 1 : 3);
//                int networkId = updateNetwork(configuration);
//                if (networkId != -1) {
//                    mWifiManager.enableNetwork(networkId, true);
//                } else {
//                    if (mOnConnectListener != null)
//                        mOnConnectListener.onError("networkId(" + networkId + ") is invalid.");
//                    mOnConnectListener = null;
//                }
//            }
        } else {
            //未配置过, 配置后连接
            WifiConfiguration configuration = createWifiConfiguration(device.SSID, device.BSSID, password, TextUtils.isEmpty(password) ? 1 : 3);
            int networkId = addNetwork(configuration);
            if (networkId != -1) {
                mWifiManager.enableNetwork(networkId, true);
            } else {
                if (mOnConnectListener != null)
                    mOnConnectListener.onError("networkId(" + networkId + ") is invalid.");
                mOnConnectListener = null;
            }
        }
    }

    /**
     * Wifi密码是否改变
     *
     * @param wifiConfiguration
     * @param password
     * @return
     */
    private boolean isPasswordChanged(WifiConfiguration wifiConfiguration, String password) {
        String preSharedKey = wifiConfiguration.preSharedKey;
        if (password == null) {
            password = "";
        }
        if (preSharedKey == null && TextUtils.isEmpty(password)) {
            //无密码, 并且未改变
            return false;
        }
        return !("\"" + password + "\"").equals(preSharedKey);
    }

    /**
     * 断开该网络
     *
     * @param netId
     */
    public void disableNetwork(int netId) {
        mWifiManager.disableNetwork(netId);
    }

    /**
     * 移除该网络
     * <p>
     * 注: 仅支持移除有自身应用添加的网络
     * </p>
     *
     * @param netId
     */
    public void removeNetwork(int netId) {
        mWifiManager.removeNetwork(netId);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mWifiManager.disconnect();
    }

    /**
     * 创建wifi配置
     *
     * @param SSID
     * @param password
     * @param type     1:WIFICIPHER_NOPASS, 2:WIFICIPHER_WEP, 3:WIFICIPHER_WPA
     * @return
     */
    public WifiConfiguration createWifiConfiguration(String SSID, String BSSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        config.BSSID = BSSID;

        WifiConfiguration tempConfig = isConfigured(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 添加网络配置
     *
     * @param config
     * @return networkId
     */
    public int addNetwork(WifiConfiguration config) {
        return mWifiManager.addNetwork(config);
    }

    /**
     * 更新网络配置
     *
     * @param config
     * @return networkId
     */
    public int updateNetwork(WifiConfiguration config) {
        return mWifiManager.updateNetwork(config);
    }

    /**
     * 该wifi是否已配置过
     *
     * @param SSID
     * @return
     */
    public WifiConfiguration isConfigured(String SSID) {
        try {
            if (SSID == null) {
                return null;
            }
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            if (existingConfigs != null) {
                for (WifiConfiguration existingConfig : existingConfigs) {
                    if (existingConfig.SSID != null && (existingConfig.SSID.equals("\"" + SSID + "\""))) {//WifiConfiguration中的SSID带""
                        return existingConfig;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class WifiDevice {
        /**
         * The network name.
         */
        public String SSID;

        /**
         * The address of the access point.
         */
        public String BSSID;

        /**
         * Describes the authentication, key management, and encryption schemes
         * supported by the access point.
         */
        public String capabilities;
        /**
         * The detected signal level in dBm, also known as the RSSI.
         * <p>
         * <p>Use {@link WifiManager#calculateSignalLevel} to convert this number into
         * an absolute signal level which can be displayed to a user.
         * Level>-50           信号最强4格
         * -50<Level<-65  信号3格
         * -65<Level<-75  信号2格
         * -75<Level<-90  信号1格
         * -90<Level          信号0格
         */
        public int level;
        /**
         * The primary 20 MHz frequency (in MHz) of the channel over which the client is communicating
         * with the access point.
         */
        public int frequency;

        /**
         * AP Channel bandwidth is 20 MHZ
         */
        public static final int CHANNEL_WIDTH_20MHZ = 0;
        /**
         * AP Channel bandwidth is 40 MHZ
         */
        public static final int CHANNEL_WIDTH_40MHZ = 1;
        /**
         * AP Channel bandwidth is 80 MHZ
         */
        public static final int CHANNEL_WIDTH_80MHZ = 2;
        /**
         * AP Channel bandwidth is 160 MHZ
         */
        public static final int CHANNEL_WIDTH_160MHZ = 3;
        /**
         * AP Channel bandwidth is 160 MHZ, but 80MHZ + 80MHZ
         */
        public static final int CHANNEL_WIDTH_80MHZ_PLUS_MHZ = 4;

        /**
         * AP Channel bandwidth; one of {@link #CHANNEL_WIDTH_20MHZ}, {@link #CHANNEL_WIDTH_40MHZ},
         * {@link #CHANNEL_WIDTH_80MHZ}, {@link #CHANNEL_WIDTH_160MHZ}
         * or {@link #CHANNEL_WIDTH_80MHZ_PLUS_MHZ}.
         */
        public int channelWidth;

        /**
         * Not used if the AP bandwidth is 20 MHz
         * If the AP use 40, 80 or 160 MHz, this is the center frequency (in MHz)
         * if the AP use 80 + 80 MHz, this is the center frequency of the first segment (in MHz)
         */
        public int centerFreq0;

        /**
         * Only used if the AP bandwidth is 80 + 80 MHz
         * if the AP use 80 + 80 MHz, this is the center frequency of the second segment (in MHz)
         */
        public int centerFreq1;

        /**
         * timestamp in microseconds (since boot) when
         * this result was last seen.
         */
        public long timestamp;

        public boolean is80211mcResponder;

        public boolean isPasspointNetwork;

        /**
         * Indicates venue name (such as 'San Francisco Airport') published by access point; only
         * available on passpoint network and if published by access point.
         */
        public CharSequence venueName;

        /**
         * Indicates passpoint operator name published by access point.
         */
        public CharSequence operatorFriendlyName;

        /**
         * 是否已配置过
         */
        public boolean isConfigured;

        /**
         * 连接状态
         */
        public int state;

        public static final String WIFI_AUTH_OPEN = "";
        public static final String WIFI_AUTH_ROAM = "[ESS]";
        public static final int STATE_NORMAL = 0;
        public static final int STATE_CONNECTING = 1;
        public static final int STATE_CONNECTED = 2;
        public static final int STATE_PASSWORD_ERROR = 3;


        public WifiDevice() {
        }

        public WifiDevice(ScanResult scanResult) {
            set(scanResult);
        }

        public void set(ScanResult scanResult) {
            SSID = scanResult.SSID;
            BSSID = scanResult.BSSID;
            frequency = scanResult.frequency;
            level = scanResult.level;
            capabilities = scanResult.capabilities;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                timestamp = scanResult.timestamp;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                centerFreq0 = scanResult.centerFreq0;
                centerFreq1 = scanResult.centerFreq1;
                channelWidth = scanResult.channelWidth;
                operatorFriendlyName = scanResult.operatorFriendlyName;
                venueName = scanResult.venueName;
            }
        }

        public boolean isNeedPassword() {
            return !(capabilities != null && (capabilities.equals(WIFI_AUTH_OPEN) || capabilities.equals(WIFI_AUTH_ROAM)));
        }
    }
}
