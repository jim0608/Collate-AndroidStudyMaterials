package com.blackbox.lerist.mvp.presenter;

import android.content.Context;

import com.blackbox.lerist.mvp.view.IView;


/**
 * Created by Lerist on 2017/01/23 0023.
 */

public abstract class LPresenter<E extends IView> implements IPresenter {
    private final E mView;
    private boolean isRelease;

    public LPresenter(E view) {
        this.mView = view;
    }

    public E getView() {
        return mView;
    }

    public Context getContext() {
        if (mView == null) return null;
        return mView.getActivity();
    }

    public boolean isRelease() {
        return mView == null || mView.getActivity() == null || isRelease;
    }

    @Override
    public void onCreate() {
        isRelease = false;
    }

    @Override
    public void onFinish() {
        isRelease = true;
    }

    @Override
    public void onDestroy() {
        isRelease = true;
    }

    @Override
    public void onRelease() {
        isRelease = true;
    }
}
