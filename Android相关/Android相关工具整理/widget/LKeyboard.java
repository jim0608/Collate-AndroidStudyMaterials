package com.blackbox.lerist.widget;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blackbox.lerist.utils.LLog;
import com.blackbox.lerist.utils.Lerist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Lerist on 2017/11/20 0020.
 * 自定义软键盘
 */

public abstract class LKeyboard {

    private static final List<LKeyboard> mShowedKeyboards = Collections.synchronizedList(new ArrayList<LKeyboard>());
    private final ViewGroup rootView;
    private final Activity activity;
    private EditText editView;
    private final View keyboardView;
    private boolean isShowing;
    private TextWatcher textWatcher;
    private int offsetWindow;
    private boolean isWithoutNavigationbar = true;
    private final Handler handler;

    public LKeyboard(Activity activity) {
        this.activity = activity;
        rootView = (ViewGroup) (activity.getWindow().getDecorView());
        keyboardView = buildKeyboardView();
        handler = new Handler(Looper.getMainLooper());
    }

    public Activity getActivity() {
        return activity;
    }

    public abstract @NonNull
    View buildKeyboardView();

    public CharSequence getInputText() {
        if (editView != null) {
            return editView.getText();
        }
        return "";
    }

    public void inputText(CharSequence input) {
        if (editView == null) return;
        //光标位置
        int selectionStart = editView.getSelectionStart();
        if (selectionStart <= -1) return;
        Editable editable = editView.getText();
        editable.insert(selectionStart, input);
//        StringBuffer sb = new StringBuffer(editView.getText().toString());
//        //从光标位置插入输入字符
//        sb.insert(selectionStart, input);
//        editView.setText("" + sb.toString());
    }

    public void backspace() {
        if (editView == null) return;
        //光标位置
        int selectionStart = editView.getSelectionStart();
        if (selectionStart <= 0) return;
        Editable editable = editView.getText();
        editable.delete(selectionStart - 1, selectionStart);
//        StringBuffer sb = new StringBuffer(editView.getText().toString());
//        //从光标位置回退
//        sb.deleteCharAt(selectionStart = selectionStart - 1);
//        editView.setText("" + sb.toString());
    }

    public void clearInput() {
        if (editView == null) return;
        editView.setText("");
    }

    public void show(final EditText editView) {
        show(editView, null);
    }

    public void show(final EditText editView, final Bundle args) {
        this.editView = editView;
        if (!isShowing && editView != null) {
            int showDelay = 0;
            if (Lerist.hideSoftKeyboard(activity)) {
                //等待软键盘完全隐藏后再显示
                showDelay = 500;
            }
            isShowing = true;
            mShowedKeyboards.remove(this);
            //隐藏已打开的键盘
            ArrayList<LKeyboard> showedKeyboards = new ArrayList<>(mShowedKeyboards);
            for (int i = 0; i < showedKeyboards.size(); i++) {
                showedKeyboards.get(i).hide();
            }
            showedKeyboards.clear();
            mShowedKeyboards.add(this);
            handler.removeMessages(0);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (editView != null) {
                        onShow(editView, args);
                    } else {
                        isShowing = false;
                    }
                }
            }, showDelay);
        }
    }

    private TextWatcher mSectionTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            String s1 = s + "," + start + "," + count + "," + after;
            LLog.e(s1);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String s1 = s + "," + start + "," + before + "," + count;
            if (editView == null) return;
            //光标位置
//            int selectionEnd = editView.getSelectionEnd();
//            LLog.e(s1 + " , " + selectionEnd);
//            if (selectionEnd == -1) return;
//            try {
//                Selection.setSelection(((EditText) editView).getText(), selectionEnd + (count - before));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            LLog.e(s);
            if (editView == null) return;
            //光标位置
            int selectionStart = editView.getSelectionStart();
            if (selectionStart == -1) return;
//            Selection.setSelection(((EditText) editView).getText(), selectionStart + s.length()-);
        }
    };

    protected void onShow(TextView editView, Bundle args) {
        editView.removeTextChangedListener(mSectionTextWatcher);
        editView.addTextChangedListener(mSectionTextWatcher);
//        if (editView instanceof EditText && editView.getText().length() > 0)
//            Selection.setSelection(((EditText) editView).getText(), editView.getText().length());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        int navigationbarHeight = isWithoutNavigationbar ? Lerist.getBottomStatusHeight(getActivity()) : 0;
        //位于虚拟按键上
        layoutParams.setMargins(0, 0, 0, navigationbarHeight);
        View contentView = rootView.getChildAt(0);
        rootView.addView(keyboardView, layoutParams);
        keyboardView.measure(View.MeasureSpec.makeMeasureSpec(
                ((View) keyboardView.getParent()).getMeasuredWidth(),
                View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED));
        int keyboardHeight = keyboardView.getMeasuredHeight();
        int[] etLocation = new int[2];
        editView.getLocationInWindow(etLocation);
        int editViewBottom = etLocation[1] + editView.getMeasuredHeight();
        int gap = rootView.getBottom() - editViewBottom - navigationbarHeight;
        offsetWindow = 0;
        if (gap < keyboardHeight) {
            offsetWindow = keyboardHeight - gap;
        }
        contentView.animate().translationY(-offsetWindow).setStartDelay(0).setDuration(200).start();
    }

    public void hide() {
        if (rootView != null && keyboardView != null && isShowing) {
            handler.removeMessages(0);
            rootView.removeView(keyboardView);
            isShowing = false;
            mShowedKeyboards.remove(this);
            View contentView = rootView.getChildAt(0);
            contentView.animate().translationY(0).setStartDelay(0).setDuration(200).start();
            offsetWindow = 0;
            if (editView != null) {
//                editView.clearFocus();
//                editView.setOnClickListener(null);
            }
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public boolean isWord(String text) {
        return text.matches("[a-zA-Z]");
    }

    public void bindEditView(@NonNull final EditText editView) {
        bindEditView(editView, null);
    }

    public void bindEditView(@NonNull final EditText editView, @Nullable final Bundle args) {
        //保存原有输入类型, 用于unbind还原
        final int inputType = editView.getInputType();
        editView.setTag(editView.getId(), inputType);
        editView.setLongClickable(false);
        editView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        editView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                int action = event.getAction();
                Layout layout = editView.getLayout();
                int line = 0;
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        line = layout.getLineForVertical(editView.getScrollY() + (int) event.getY());
                        int off = layout.getOffsetForHorizontal(line, (int) event.getX() - editView.getPaddingLeft());
                        Selection.setSelection(editView.getEditableText(), off);
                        return false;
//                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
//                        line = layout.getLineForVertical(getScrollY()+(int)event.getY());
//                        int curOff = layout.getOffsetForHorizontal(line, (int)event.getX());
//                        Selection.setSelection(getEditableText(), off, curOff);
                        int inType = editView.getInputType(); // backup the input type
                        //禁止弹出输入法
                        editView.setInputType(InputType.TYPE_NULL); // disable soft input
                        editView.onTouchEvent(event); // call native handler
                        //还原输入类型
                        editView.setInputType(inType); // restore input type
                        show(editView, args);
                        return true;
                }
                return false;
            }
        });

        editView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    show(editView, args);
                } else {
                    hide();
                }
            }
        });
    }

    public void unbindEditView(@NonNull final TextView editView) {
        //允许弹出输入法
        Object srcInputType = editView.getTag(editView.getId());
        if (srcInputType != null) {
            //还原绑定前的输入类型
            editView.setInputType((Integer) srcInputType);
        } else {
            //设置默认输入类型
            editView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        editView.setOnFocusChangeListener(null);
        if (textWatcher != null)
            editView.removeTextChangedListener(textWatcher);
    }

    /**
     * 是否处于Navigationbar 上面
     *
     * @param isWithoutNavigationbar
     */
    public void setIsWithoutNavigationbar(boolean isWithoutNavigationbar) {
        this.isWithoutNavigationbar = isWithoutNavigationbar;
    }
}
