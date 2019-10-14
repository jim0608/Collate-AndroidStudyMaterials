package com.blackbox.lerist.utils;

import android.content.Context;
import android.media.AudioManager;

import com.socks.library.KLog;

/**
 * Created by Lerist on 2017/08/30 0030.
 * <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
 */

public class LAudioManagerUtils {
    /**
     * 增大音量
     *
     * @param context
     * @param streamType 目标流 @see #AudioManager.STREAM_MUSIC
     */
    public static void volumeUp(Context context, int streamType) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(streamType);
        int currentVolume = mAudioManager.getStreamVolume(streamType) + 1;
        if (currentVolume > maxVolume) {
            currentVolume = maxVolume;
        }
        mAudioManager.setStreamVolume(streamType, currentVolume, 1);
    }

    /**
     * 音量
     *
     * @param context
     * @param streamType 目标流 @see #AudioManager.STREAM_MUSIC
     */
    public static void volumeDown(Context context, int streamType) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(streamType);
        int currentVolume = mAudioManager.getStreamVolume(streamType) - 1;
        if (currentVolume > maxVolume) {
            currentVolume = maxVolume;
        }

        if (currentVolume < 0) currentVolume = 0;

        mAudioManager.setStreamVolume(streamType, currentVolume, 1);
    }

    /**
     * 设置音量
     *
     * @param context
     * @param streamType 目标流 @see #AudioManager.STREAM_MUSIC
     * @param volume     音量
     */
    public static void setVolume(Context context, int streamType, int volume) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            int maxVolume = mAudioManager.getStreamMaxVolume(streamType);
            int currentVolume = volume;
            if (currentVolume > maxVolume) {
                currentVolume = maxVolume;
            }
            mAudioManager.setStreamVolume(streamType, currentVolume, 1);
        }
    }

    /**
     * 设置最大音量
     *
     * @param context
     * @param streamType
     */
    public static void setMaxVolume(Context context, int streamType) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = 9;
        if (mAudioManager != null)
            maxVolume = mAudioManager.getStreamMaxVolume(streamType);
        setVolume(context, streamType, maxVolume);
    }

    /**
     * 静音
     *
     * @param context
     * @param streamType
     */
    public static void soundOff(Context context, int streamType) {
        setVolume(context, streamType, 0);
    }

    /**
     * 恢复声音(最大音量的一半)
     *
     * @param context
     * @param streamType
     */
    public static void soundOn(Context context, int streamType) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = 9;
        if (mAudioManager != null)
            maxVolume = mAudioManager.getStreamMaxVolume(streamType);
        setVolume(context, streamType, maxVolume / 2);
    }

    public static void setBluetoothScoOn(Context context, boolean on) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            if (on) {
                mAudioManager.startBluetoothSco();
            } else {
                mAudioManager.stopBluetoothSco();
            }
            mAudioManager.setBluetoothScoOn(on);
            mAudioManager.setSpeakerphoneOn(!on);

            KLog.e("isBluetoothScoOn: " + mAudioManager.isBluetoothScoOn() + " , isSpeakerphoneOn" + mAudioManager.isSpeakerphoneOn());
        }
    }
}
