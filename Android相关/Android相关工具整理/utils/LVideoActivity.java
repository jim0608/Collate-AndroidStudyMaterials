package com.blackbox.lerist.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.MediaController;

import com.blackbox.lerist.R;
import com.blackbox.lerist.activity.LActivity;
import com.blackbox.lerist.widget.LVideoView;
import com.socks.library.KLog;


public class LVideoActivity extends LActivity {
    private static OnPlayListener mOnPlayListener;
    private MediaController mediaController;
    private String url;
    LVideoView videoView;
    private static LVideoActivity instance;
    public MediaPlayer mMediaPlayer;
    private boolean isCompletion;
    private boolean isStart;
    private int state;
    public static final int STATE_NONE = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_COMPLETION = 4;

    public interface OnPlayListener {
        void onStart();

        void onPlaying(long currentTime, long totalTime);

        void onResume();

        void onPause();

        void onError(Object obj);

        void onCompletion();
    }

    public static void play(Context context, String url, final OnPlayListener onPlayListener) {
        play(context, url, false, onPlayListener);
    }

    public static void play(Context context, String url, boolean autoFinish, final OnPlayListener onPlayListener) {
        Intent intent = new Intent(context, LVideoActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("autoFinish", autoFinish);
        mOnPlayListener = onPlayListener;
        context.startActivity(intent);
    }

    public static void pause() {
        if (instance != null) {
            try {
                instance.setState(STATE_PAUSE);
                instance.mMediaPlayer.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void resume() {
        if (instance != null) {
            try {
                instance.setState(STATE_PLAYING);
                instance.mMediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isCompletion() {
        if (instance != null) {
            try {
                return instance.isCompletion;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isStart() {
        if (instance != null) {
            try {
                return instance.isStart;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isPlaying() {
        if (instance != null) {
            try {
                return instance.mMediaPlayer.isPlaying();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getCurrentPlayUrl() {
        if (instance != null) {
            try {
                return instance.url;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static int getCurrentPlayState() {
        if (instance != null) {
            try {
                return instance.getState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return STATE_NONE;
    }

    public synchronized static void close() {
        if (instance != null) {
            instance.finish();
        }
        mOnPlayListener = null;
        instance = null;
    }

    Handler mPlayHanlder = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mOnPlayListener != null && isPlaying()) {
                mOnPlayListener.onPlaying(0, 0);
//                if (mMediaPlayer != null) {
//                    try {
//                        mOnPlayListener.onPlaying(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
            }
            mPlayHanlder.sendEmptyMessageDelayed(0, 1000);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_video);
        videoView = find(R.id.a_video_player);
        doIntent(getIntent());
    }

    private void doIntent(Intent intent) {
        isStart = false;
        isCompletion = false;
        this.url = intent.getStringExtra("url");
        final boolean autoFinish = getIntent().getBooleanExtra("autoFinish", false);
        if (TextUtils.isEmpty(this.url)) {
            finish();
            return;
        }
        this.mediaController = new MediaController(this);
        Log.i("LVideoActivity", "video path : " + url);
        setState(STATE_LOADING);
        this.videoView.setVideoPath(url);
        this.videoView.setMediaController(this.mediaController);
        videoView.setOnPlayListener(new LVideoView.OnPlayListener() {
            @Override
            public void onStart() {
                setState(STATE_PLAYING);
            }

            @Override
            public void onPause() {
                setState(STATE_PAUSE);
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                KLog.e("onError");
                setState(STATE_NONE);
                isStart = false;
                isCompletion = false;
                url = null;
                //移除onPlaying回调
                mPlayHanlder.removeMessages(0);
                if (mOnPlayListener != null) mOnPlayListener.onError(what);
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                KLog.e("onPrepared");
                setState(STATE_PLAYING);
                mMediaPlayer = mp;
                isStart = true;
                if (mOnPlayListener != null) mOnPlayListener.onStart();
                //启动onPlaying回调
                mPlayHanlder.sendEmptyMessage(0);
            }
        });
        this.videoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                KLog.e("onCompletion");
                setState(STATE_COMPLETION);
                isCompletion = true;
                url = null;
                LVideoActivity.this.videoView.stopPlayback();
                if (autoFinish) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            LVideoActivity.this.setResult(-1);
                            LVideoActivity.this.finish();
                        }
                    }, 2000);
                }
                //移除onPlaying回调
                mPlayHanlder.removeMessages(0);
                if (mOnPlayListener != null) mOnPlayListener.onCompletion();
            }
        });
        this.mediaController.setMediaPlayer(this.videoView);
        this.videoView.requestFocus();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    protected void onResume() {
        super.onResume();
        try {
            this.videoView.start();
            setState(STATE_PLAYING);
            mPlayHanlder.sendEmptyMessageDelayed(0, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        doIntent(intent);
    }

    @Override
    public void finish() {
        //必须在此处释放
        mPlayHanlder.removeMessages(0);
        mOnPlayListener = null;
        instance = null;
        url = null;
        super.finish();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
