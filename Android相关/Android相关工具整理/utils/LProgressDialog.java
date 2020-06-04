package com.blackbox.lerist.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.blackbox.lerist.R;


/**
 * Created by Lerist on 2015/3/12, 0012.
 */
public class LProgressDialog {

    private static AlertDialog mAlertDialog;
    private static Context mContext;
    private static boolean isCancelable;
    private static boolean isTouchOutsideCancelable;
    private static Handler mHandler;

    public static void show(Context context, String msg) {
        show(context, msg, null, null);
    }

    public static void show(final Context context, final String msg, final DialogInterface.OnDismissListener onDismissListener) {
        show(context, msg, onDismissListener, null);
    }

    public static void show(final Context context, final String msg, final DialogInterface.OnCancelListener onCancelListener) {
        show(context, msg, null, onCancelListener);
    }

    public static void show(final Context context, final String msg, final DialogInterface.OnDismissListener onDismissListener, final DialogInterface.OnCancelListener onCancelListener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mAlertDialog == null || mContext != context) {
                        try {
                            if (mAlertDialog != null) mAlertDialog.dismiss();
                        } catch (Exception e) {
                        }
                        View contentView = View.inflate(context, R.layout.layout_progress_dialog, null);
                        contentView.setFocusable(true);
                        contentView.setFocusableInTouchMode(true);

                        if (!TextUtils.isEmpty(msg)) {
                            TextView tv_text = (TextView) contentView.findViewById(R.id.l_progress_dialog_tv_text);
                            if (tv_text != null) {
                                tv_text.setVisibility(View.VISIBLE);
                                tv_text.setText(msg);
                            }
                        }
                        mAlertDialog = new AlertDialog.Builder(context).create();
                        mAlertDialog.setCanceledOnTouchOutside(true);
                        mAlertDialog.show();
                        mAlertDialog.getWindow()
                                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                        mAlertDialog.getWindow()
                                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE);
//            contentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if(hasFocus) {
//                        mAlertDialog.getWindow().setSoftInputMode(
//                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                        // show imm
//                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
//                                Context.INPUT_METHOD_SERVICE);
//                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
//                                InputMethodManager.HIDE_IMPLICIT_ONLY);
//                    }
//                }
//            });
                        mAlertDialog.getWindow().setBackgroundDrawable(
                                new ColorDrawable(Color.TRANSPARENT));
                        mAlertDialog.getWindow().setContentView(contentView);

//                        mAlertDialog.setCanceledOnTouchOutside(isTouchOutsideCancelable);
//                        mAlertDialog.setCancelable(isCancelable);
                        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (onDismissListener != null) onDismissListener.onDismiss(dialog);
                            }
                        });
                        mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (onCancelListener != null) onCancelListener.onCancel(dialog);
                            }
                        });
                        isTouchOutsideCancelable = false;
                        isCancelable = false;
                    } else {
                        mAlertDialog.show();
                    }
                    mContext = context;
                } catch (Throwable e) {

                }
            }
        };
        if (ThreadUtils.isMainThread(Thread.currentThread())) {
            runnable.run();
        } else {
            if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(runnable);
        }
    }

    public static Boolean isRunninging(){
        if (mAlertDialog != null) {
            return true;
        }
        return false;
    }

    public static void dismiss() {
        if (mAlertDialog != null) {
            try {
                if (ThreadUtils.isMainThread(Thread.currentThread())) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                } else {
                    if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());
                    mHandler.removeMessages(0);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAlertDialog != null)
                                mAlertDialog.dismiss();
                            mAlertDialog = null;
                        }
                    });
                }
            } catch (Exception e) {
                mAlertDialog = null;
                e.printStackTrace();
            }
        }
    }
    /**
     * 设置是否可取消
     *
     * @param b
     */
    public static void setCancelable(boolean b) {
        isCancelable = b;
        if (mAlertDialog != null) {
            mAlertDialog.setCancelable(b);
            isCancelable = false;
        }
    }

    /**
     * 设置触摸空白区域取消dialog
     *
     * @param isTouchOutsideCancelable
     */
    public static void setIsTouchOutsideCancelable(boolean isTouchOutsideCancelable) {
        LProgressDialog.isTouchOutsideCancelable = isTouchOutsideCancelable;
        if (mAlertDialog != null) {
            mAlertDialog.setCanceledOnTouchOutside(isTouchOutsideCancelable);
            LProgressDialog.isTouchOutsideCancelable = false;
        }
    }
}
