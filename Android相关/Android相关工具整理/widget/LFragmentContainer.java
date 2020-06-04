package com.blackbox.lerist.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.blackbox.lerist.utils.Lerist;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lerist on 2016/2/29, 0029.
 */
public class LFragmentContainer extends FrameLayout {

    private FragmentManager fragmentManager;
    private int enterAnimation;
    private int exitAnimation;
    private int popEnterAnimation;
    private int popExitAnimarion;
    private boolean isReplace;
    private boolean enableBackStack;
    private ArrayList<String> titles;
    private ArrayList<String> tags;
    private static final String TAG = "LFragmentContainer";

    public interface OnFragmentChangedListener {
        void onFragmentChangedBefore(Fragment currentVisiblefragment, int index);

        void onFragmentChanged(Fragment currentVisibleFragment, int index);
    }

    private ArrayList<Fragment> fragments;
    private ArrayList<Fragment> addedFragments = new ArrayList<>();
    private ArrayList<OnFragmentChangedListener> onFragmentChangedListeners = new ArrayList<>();
    private int currentVisibleFragmentIndex = -1;
    private int transitionStyle = -1;
    private int transition = -1;

    public LFragmentContainer(Context context) {
        this(context, null);
    }

    public LFragmentContainer(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LFragmentContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LFragmentContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void onStart() {
//        if (isReplace) {
//            if (getCurrentVisibleFragment() != null) {
//                try {
//                    getCurrentVisibleFragment().onStart();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            if (ListUtils.isNotEmpty(fragments)) {
//                for (Fragment fragment : fragments) {
//                    try {
//                        fragment.onStart();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    public void onResume() {
//        if (isReplace) {
//            if (getCurrentVisibleFragment() != null) {
//                try {
//                    getCurrentVisibleFragment().onResume();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            if (ListUtils.isNotEmpty(fragments)) {
//                for (Fragment fragment : fragments) {
//                    try {
//                        fragment.onResume();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    public void onPause() {
//        if (isReplace) {
//            if (getCurrentVisibleFragment() != null) {
//                try {
//                    getCurrentVisibleFragment().onPause();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            if (ListUtils.isNotEmpty(fragments)) {
//                for (Fragment fragment : fragments) {
//                    try {
//                        fragment.onPause();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    public void onStop() {
//        if (isReplace) {
//            if (getCurrentVisibleFragment() != null) {
//                try {
//                    getCurrentVisibleFragment().onStop();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            if (ListUtils.isNotEmpty(fragments)) {
//                for (Fragment fragment : fragments) {
//                    try {
//                        fragment.onStop();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    public void onDestroy() {
//        if (isReplace) {
//            if (getCurrentVisibleFragment() != null) {
//                try {
//                    getCurrentVisibleFragment().onDestroy();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            if (ListUtils.isNotEmpty(fragments)) {
//                for (Fragment fragment : fragments) {
//                    try {
//                        fragment.onDestroy();
//                        fragmentManager.beginTransaction().remove(fragment).commit();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    private void init() {
        if (getId() < 0)
            setId(Lerist.generateViewId());

        if (getContext() instanceof FragmentActivity) {
            fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
        } else
            KLog.e("未获取到FragmentManager, 你需要调用setFragmentManager方法");

        fragments = new ArrayList<>();
        titles = new ArrayList<String>();
        tags = new ArrayList<String>();
    }

    public LFragmentContainer setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        return this;
    }

    public LFragmentContainer addOnFragmentChangedListener(OnFragmentChangedListener onFragmentChangedListener) {
        this.onFragmentChangedListeners.add(onFragmentChangedListener);
        return this;
    }

    public LFragmentContainer addFragment(Fragment fragment) {
        if (isReplace == true) {
            throw new IllegalArgumentException("isReplace value is true, please call addFragment(String tag, Fragment fragment)");
        }
        return addFragment(fragment.getClass().getName(), fragment);
    }

    public LFragmentContainer addFragment(String tag, Fragment fragment) {
        return addFragment(tag, fragment, tag);
    }

    public LFragmentContainer addFragment(String tag, Fragment fragment, String title) {
        if (fragments.contains(fragment)) return this;

        fragments.add(fragment);
        tags.add(tag);
        titles.add(title);
        return this;
    }

    public LFragmentContainer enableBackStack(boolean enableBackStack) {
        this.enableBackStack = enableBackStack;
        return this;
    }

    public int getCurrentVisibleFragmentIndex() {
        return currentVisibleFragmentIndex;
    }

    public Fragment getCurrentVisibleFragment() {
        if (currentVisibleFragmentIndex == -1) return null;
        try {
            return fragments.get(currentVisibleFragmentIndex);
        } catch (Exception e) {
            KLog.e(e.getMessage());
        }
        return null;
    }

    public Fragment getFragment(int index) {
        try {
            return fragments.get(index);
        } catch (Exception e) {
            KLog.e(e.getMessage());
        }
        return null;
    }


    public ArrayList<Fragment> getFragments() {
        return fragments;
    }

    public String getCurrentTitle() {
        if (currentVisibleFragmentIndex == -1) return null;
        return getTitle(getCurrentVisibleFragmentIndex());
    }

    public String getTitle(int index) {
        try {
            return titles.get(index);
        } catch (Exception e) {
            KLog.e(e.getMessage());
        }
        return null;
    }

    public void setVisibleFragmentIndex(int index) {
        for (OnFragmentChangedListener onFragmentChangedListener : onFragmentChangedListeners) {
            if (onFragmentChangedListener != null)
                onFragmentChangedListener.onFragmentChangedBefore(getCurrentVisibleFragment(), getCurrentVisibleFragmentIndex());
        }

        if (getCurrentVisibleFragmentIndex() == index) {
            return;
        }

        if (show(index)) {

        }
    }

    public void setVisibleFragment(Fragment fragment) {
        setVisibleFragmentIndex(fragments.indexOf(fragment));
    }

    public LFragmentContainer setReplaceMode(boolean isReplace) {
        this.isReplace = isReplace;
        return this;
    }

    public LFragmentContainer setTransitionStyle(int transitionStyle) {
        this.transitionStyle = transitionStyle;
        return this;
    }

    public LFragmentContainer setTransition(int transition) {
        this.transition = transition;
        return this;
    }

    public LFragmentContainer setCustomAnimations(@AnimRes int enter,
                                                  @AnimRes int exit) {
        return setCustomAnimations(enter, exit, 0, 0);
    }

    public LFragmentContainer setCustomAnimations(@AnimRes int enter,
                                                  @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        //进栈动画
        this.enterAnimation = enter;
        this.exitAnimation = exit;
        //出栈动画
        this.popEnterAnimation = popEnter;
        this.popExitAnimarion = popExit;
        return this;
    }

    public int getCount() {
        return this.fragments.size();
    }

    private synchronized boolean show(int pageIndex) {
        if (fragmentManager == null) {
            KLog.e("fragmentManager is null.");
            return false;
        }

        //即将显示的Fragment
        Fragment willVisibleFragment = getFragment(pageIndex);
        if (willVisibleFragment == null) {
            KLog.e("willVisibleFragment is null.");
            return false;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (transitionStyle != -1) transaction.setTransitionStyle(transitionStyle);
        if (transition != -1) transaction.setTransition(transition);
        transaction.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimarion);

        if (isReplace == false) {


            List<Fragment> addedFragments = fragmentManager.getFragments();
            for (int i = 0; i < addedFragments.size(); i++) {
                //隐藏其他不需要显示的Fragment
                Fragment fragment = addedFragments.get(i);
                if (fragment != null && fragment != willVisibleFragment && fragments.contains(fragment)) {
                    transaction.hide(fragment);
                }
            }

            if (!addedFragments.contains(willVisibleFragment)) {
                transaction.add(getId(), willVisibleFragment, tags.get(pageIndex));
                if (enableBackStack) {
                    transaction.addToBackStack(tags.get(pageIndex));
                }
            }

            try {
                transaction.show(willVisibleFragment).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                transaction.replace(getId(), willVisibleFragment).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        this.currentVisibleFragmentIndex = pageIndex;
        for (OnFragmentChangedListener onFragmentChangedListener : onFragmentChangedListeners) {
            if (onFragmentChangedListener != null)
                try {
                    onFragmentChangedListener.onFragmentChanged(willVisibleFragment, fragments.indexOf(willVisibleFragment));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        onFragmentChangedListeners.clear();
        //移除Fragment
        List<Fragment> addedFragments = fragmentManager.getFragments();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < addedFragments.size(); i++) {
            Fragment fragment = addedFragments.get(i);
            if (fragment != null && fragments.contains(fragment)) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragments.clear();
        addedFragments.clear();
        super.onDetachedFromWindow();
    }
}
