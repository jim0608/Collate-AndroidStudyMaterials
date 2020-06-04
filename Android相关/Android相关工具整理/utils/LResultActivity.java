package com.blackbox.lerist.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.blackbox.lerist.R;
import com.blackbox.lerist.interfaces.TransparentDialog;
import com.heihezi.mrzeng.libmrzeng.utils.ToastUtil;
import com.socks.library.KLog;

public class LResultActivity extends Activity implements TransparentDialog {

    public static final int REQUEST_CODE = 0;
    public static Callback mCallback;
    public static Intent mIntent;
    private LinearLayout linearLayout;


    public static void startActivityForResult(Context context, Class c, Callback resultCallback) {
        startActivityForResult(context, new Intent(context, c), resultCallback);
    }
    static long lastStartTime;

    public static void startActivityForResult(Context context, Intent intent, Callback resultCallback) {
        mCallback = resultCallback;
        mIntent = intent;
        if (System.currentTimeMillis() - lastStartTime < 500) {
            lastStartTime = System.currentTimeMillis();
            return;
        }
        lastStartTime = System.currentTimeMillis();
        Intent i = new Intent(context, LResultActivity.class);
        if (!(context instanceof Activity))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }

    public interface Callback {
        void onSuccess(Intent result);

        void onFailure();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long startTime = System.currentTimeMillis();
        linearLayout = new LinearLayout(this);
        setContentView(linearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - startTime > 1000) {
                    //两秒后运行点击退出
                    finish();
                }
            }
        });
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init();
    }

    private void init() {
        if (mIntent != null) {
//            mIntent.putExtra("RequstClass", LResultActivity.class);
            try {
                startActivityForResult(mIntent, REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                if (mCallback != null) mCallback.onFailure();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        KLog.i("LResultActivity",requestCode + " " + resultCode + " " + data);
        if (requestCode == REQUEST_CODE && resultCode != RESULT_CANCELED) {
            if (mCallback != null) mCallback.onSuccess(data);
        } else {
            if (mCallback != null) mCallback.onFailure();
        }
        finish();
    }

}
