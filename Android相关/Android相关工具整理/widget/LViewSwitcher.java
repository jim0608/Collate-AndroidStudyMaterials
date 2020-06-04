package com.blackbox.lerist.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;

/**
 * Created by Lerist on 2017/11/06 0006.
 * ViewSwitcher 支持任意个子View切换, 默认ViewSwitcher仅支持两个
 */

public class LViewSwitcher extends ViewAnimator {
    private ViewFactory mFactory;

    public LViewSwitcher(Context context) {
        super(context);
    }

    public LViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return LViewSwitcher.class.getName();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    /**
     * 获取下一个显示的View
     *
     * @return
     */
    public View getNextView() {
        return getChildAt(getNextViewIndex());
    }

    public int getNextViewIndex() {
        if (getChildCount() == 0) {
            return 0;
        }
        int which = (getDisplayedChild() + 1) % getChildCount();
        return which;
    }

    public void setFactory(int childCount, ViewFactory factory) {
        mFactory = factory;
        for (int i = 0; i < childCount; i++) {
            obtainView();
        }
    }

    private View obtainView() {
        View child = mFactory.makeView();
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        addView(child, lp);
        return child;
    }

    public void reset() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt != null)
                childAt.setVisibility(GONE);
        }
    }

    /**
     * Creates views in a ViewSwitcher.
     */
    public interface ViewFactory {
        /**
         * Creates a new {@link android.view.View} to be added in a
         * {@link android.widget.ViewSwitcher}.
         *
         * @return a {@link android.view.View}
         */
        View makeView();
    }
}
