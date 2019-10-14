package com.blackbox.lerist.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class LOverrideActivity extends FragmentActivity {

    private static Callback mCallback;
    public static LOverrideActivity instance;

    public static void startActivity(Context context, final Callback resultCallback) {
        mCallback = resultCallback;
        Intent i = new Intent(context, LOverrideActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public interface Callback {
        void onCreate(Activity activity, Bundle savedInstanceState);

        void onRestart(Activity activity);

        void onStart(Activity activity);

        void onResume(Activity activity);

        void onPause(Activity activity);

        void onStop(Activity activity);

        void onDestroy(Activity activity);

        void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if (mCallback != null)
            mCallback.onCreate(this, savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mCallback != null)
            mCallback.onRestart(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCallback != null)
            mCallback.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCallback != null)
            mCallback.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCallback != null)
            mCallback.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCallback != null)
            mCallback.onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCallback != null)
            mCallback.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCallback != null)
            mCallback.onActivityResult(this, requestCode, resultCode, data);
    }
}
