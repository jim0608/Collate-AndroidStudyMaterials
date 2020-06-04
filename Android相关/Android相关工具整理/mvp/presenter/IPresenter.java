package com.blackbox.lerist.mvp.presenter;

import android.content.Intent;

public interface IPresenter {
    void init();

    void onCreate();

    void onCreated();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onFinish();

    void onDestroy();

    void onRelease();

    void onActivityResult(int requestCode, int resultCode, Intent data);

}
