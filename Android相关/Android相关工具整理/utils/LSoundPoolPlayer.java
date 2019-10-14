package com.blackbox.lerist.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


import java.util.HashMap;

/**
 * Created by Lerist on 2016/5/1, 0001.
 */
public class LSoundPoolPlayer {

    private static SoundPool sp;
    private static HashMap<Integer, Integer> sounds;

    public LSoundPoolPlayer(Context context, int... rawId) {
        // 实例化SoundPool播放器
        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                LLog.i("onLoadComplete: " + sampleId);
            }
        });
        sounds = new HashMap<>();
        for (int i : rawId) {
            sounds.put(i, sp.load(context, i, 1));
        }
    }

    public void play(int... rawId) {
        if (sp == null || sounds.isEmpty()) {
            return;
        }
        if (rawId == null || rawId.length < 1) {
            for (Integer key : sounds.keySet()) {
                Integer integer = sounds.get(key);
                if (integer != null)
                    sp.play(integer, 1.0f, 1.0f, 0, 0, 1.0f);
            }
            return;
        }
        for (int i : rawId) {
            Integer integer = sounds.get(i);
            if (integer != null)
                sp.play(integer, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void play(int rawId, int loop) {
        if (sp == null || sounds.isEmpty()) {
            return;
        }
        sp.play(sounds.get(rawId), 1.0f, 1.0f, 0, loop, 1.0f);
    }

    public void stop(int rawId) {
        sp.stop(sounds.get(rawId));
    }

    public void stopAll() {
        for (Integer key : sounds.keySet()) {
            try {
                stop(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
