package com.blackbox.lerist.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Lerist on 2017/01/19 0019.
 */

public class LVideoView extends VideoView {
    private OnPlayListener mOnPlayListener;

    public interface OnPlayListener {
        void onStart();

//        void onPlaying(long currentTime, long totalTime);

//        void onResume();

        void onPause();

//      void onError(Object obj);
//      void onCompletion();
    }

    public LVideoView(Context context) {
        super(context);
    }

    public LVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //视频全屏
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.mOnPlayListener = onPlayListener;
    }

    @Override
    public void start() {
        super.start();
        if (mOnPlayListener != null) mOnPlayListener.onStart();
    }

    @Override
    public void pause() {
        super.pause();
        if (mOnPlayListener != null) mOnPlayListener.onPause();
    }


}
