package com.blackbox.lerist.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.socks.library.KLog;

/**
 * Created by Lerist on 2016/4/4, 0004.
 */

public class LMediaPlayer implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private static final int STATE_START = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;
    private static final int STATE_STOP = 4;
    private static final int STATE_FINISH = 5;
    private Context mContext;

    public MediaPlayer mMediaPlayer;
    private boolean isPlaying;
    private OnPlayListener onPlayListener;
    private Handler handler;
    private String mUri;
    private long startTime;
    private TelephonyManager telephonyManager;
    private boolean isCallAction = false;
    private int mDuration;
    private boolean isPrepareing = false;
    private boolean isCancelPrepare;

    private volatile static LMediaPlayer mediaPlayer;
    private AudioManager mAudioManager;

    public static LMediaPlayer getInstance(Context context) {
        if (mediaPlayer == null) {
            synchronized (LMediaPlayer.class) {
                if (mediaPlayer == null) {
                    mediaPlayer = new LMediaPlayer(context);
                }
            }
        }
        return mediaPlayer;
    }

    public LMediaPlayer() {
        this(null);
    }

    public LMediaPlayer(@Nullable Context context) {

        if (context != null) {
            this.mContext = context.getApplicationContext();
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//                    telephonyManager.listen(new PhoneStateListener() {
//                        @Override
//                        public void onCallStateChanged(int state, String incomingNumber) {
//                            super.onCallStateChanged(state, incomingNumber);
//                            switch (state) {
//                                //无任务状态
//                                case TelephonyManager.CALL_STATE_IDLE:
//                                    if (isCallAction && !isPlaying()) {
//                                        play();
//                                    }
//                                    isCallAction = false;
//                                    break;
//                                //电话进来
//                                case TelephonyManager.CALL_STATE_RINGING:
//                                    if (isPlaying()) {
//                                        pause();
//                                        isCallAction = true;
//                                    }
//                                    break;
//                                //接听电话
//                                case TelephonyManager.CALL_STATE_OFFHOOK:
//                                    if (isPlaying()) {
//                                        pause();
//                                        isCallAction = true;
//                                    }
//                                    break;
//                            }
//                        }
//                    }, PhoneStateListener.LISTEN_CALL_STATE);
//                }
//            });
        }

        handler = new Handler(mContext != null ? mContext.getMainLooper() : Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case STATE_START:
                        isPrepareing = false;
                        isPlaying = true;
                        if (startTime == 0) startTime = System.currentTimeMillis();

                        if (onPlayListener != null) onPlayListener.onPlay();
                        handler.removeMessages(STATE_PLAYING);
                        handler.sendEmptyMessage(STATE_PLAYING);
                        break;
                    case STATE_PLAYING:
                        if (mMediaPlayer != null) {
                            try {
                                if (mMediaPlayer.isPlaying()) {
                                    isPrepareing = false;
                                    isPlaying = true;
                                    if (onPlayListener != null)
                                        onPlayListener.onPlaying(mDuration, mMediaPlayer.getCurrentPosition());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessageDelayed(STATE_PLAYING, 1000);
                        }
                        break;
                    case STATE_PAUSE:
                        isPlaying = false;
                        isPrepareing = false;
                        handler.removeMessages(STATE_PLAYING);
                        if (onPlayListener != null) onPlayListener.onPause();
                        break;
                    case STATE_STOP:
                        isPlaying = false;
                        isPrepareing = false;
                        mUri = null;
                        startTime = 0;
                        handler.removeMessages(STATE_PLAYING);
                        if (onPlayListener != null) onPlayListener.onStop();
                        break;
                    case STATE_FINISH:
                        mAudioManager.abandonAudioFocus(LMediaPlayer.this);
                        isPlaying = false;
                        isPrepareing = false;
                        mUri = null;
                        startTime = 0;
                        handler.removeMessages(STATE_PLAYING);
                        if (mMediaPlayer != null) {
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                        }
                        if (onPlayListener != null) onPlayListener.onFinish();
                        break;
                }
            }
        };
    }

    private void initPlayer() {
        mDuration = 0;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    public void play() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            handler.sendEmptyMessage(STATE_START);
        } else {
            handler.sendEmptyMessage(STATE_FINISH);
        }
    }

    public void playUri(final String uri) {
        isCancelPrepare = false;

        if (StringUtils.isEmpty(uri)) return;

        if (!uri.equals(mUri)) {
            KLog.i("playUri: "+uri);
            initPlayer();
            try {
                mDuration = 0;
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(uri);
                isPrepareing = true;
                mMediaPlayer.prepareAsync();//prepare之后自动播放
                mUri = uri;
            } catch (Exception e) {
                mUri = null;
                e.printStackTrace();
            }
        } else {
            if (isPrepareing == true) {
                //正在准备中, 则取消准备完成后播放
                isCancelPrepare = true;
                KLog.i("CancelPrepare...");
                handler.sendEmptyMessage(STATE_FINISH);
            } else {
                play();
                KLog.i("continue play:"+uri);
            }
        }
    }


    public void pause() {
        KLog.i("pause...");

        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            handler.sendEmptyMessage(STATE_PAUSE);
        } else {
            handler.sendEmptyMessage(STATE_FINISH);
        }
    }

    public void stop() {
        KLog.i("stop...");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
            handler.sendEmptyMessage(STATE_STOP);
        } else {
            handler.sendEmptyMessage(STATE_STOP);
        }
    }

    public int getDuration() {
        return mDuration;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPrepareing() {
        return isPrepareing;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mDuration = mp.getDuration();
        isPrepareing = false;
        //取消播放
        if (isCancelPrepare == true) {
            isCancelPrepare = false;
            handler.sendEmptyMessage(STATE_FINISH);
            return;
        }
        //准备好后自动播放(缓冲网络资源)
        mp.start();
        isPlaying = true;
        handler.sendEmptyMessage(STATE_START);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        KLog.e("error!");
        //发生错误, 通常在prepare过程中
        //必须重置
        mp.reset();
        handler.sendEmptyMessage(STATE_FINISH);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        KLog.i("finish.");
        //播放结束
        handler.sendEmptyMessage(STATE_FINISH);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (onPlayListener != null) {
//            isPrepareing = (!mMediaPlayer.isPlaying()) && percent < 100;
            onPlayListener.onBuffering(percent);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mContext = null;
        telephonyManager = null;
        mUri = null;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        try {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    KLog.i("AUDIOFOCUS_GAIN");
                    // resume playback
                    if (mMediaPlayer == null) return;

                    if (isCallAction == false) {
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                    }
                    if (isCallAction && !mMediaPlayer.isPlaying()) {
                        play();
                    }
                    isCallAction = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    KLog.i("AUDIOFOCUS_LOSS");
                    // Lost focus for an unbounded amount of time: stop playback and release media player
                    if (mMediaPlayer == null) return;

    //                if (mMediaPlayer.isPlaying()) stop();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    KLog.i("AUDIOFOCUS_LOSS_TRANSIENT");
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because playback
                    // is likely to resume
                    isCallAction = false;
                    if (mMediaPlayer == null) return;
                    if (mMediaPlayer.isPlaying()) {
                        pause();
                        isCallAction = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    KLog.i("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    isCallAction = false;

                    if (mMediaPlayer == null) return;
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface OnPlayListener {
        void onPlay();

        void onPlaying(int duration, int currentPosition);

        void onPause();

        void onBuffering(int percent);

        void onStop();

        void onFinish();
    }
}
