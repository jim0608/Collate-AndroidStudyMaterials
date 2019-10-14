package com.blackbox.lerist.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lerist on 2016/6/18, 0018.
 */

public class LAskPermissions extends Activity {


    private static final int PERMISSION_REQUEST_CODE = 100;
    private static RequestPermissionCallback mRequestPermissionCallback;
    private static String[] mPermissions;

    public interface RequestPermissionCallback {
        void granted();

        void denied(List<String> deniedPermissions);
    }

    public static void requestPermissions(Context context, String permission, RequestPermissionCallback permissionCallback) {
        requestPermissions(context, new String[]{permission}, permissionCallback);
    }

    public static void requestPermissions(Context context, String[] permissions, RequestPermissionCallback permissionCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (permissionCallback != null)
                permissionCallback.denied(Arrays.asList(permissions));
            return;
        }
        ArrayList<String> prePermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                prePermissions.add(permission);
            }
        }
        if (prePermissions.isEmpty()) {
            //已全部授权
            if (permissionCallback != null)
                permissionCallback.denied(prePermissions);
            return;
        }
        mRequestPermissionCallback = permissionCallback;
        mPermissions = permissions;
        Intent i = new Intent(context, LAskPermissions.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestPermissions(mPermissions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions(String... permissions) throws Exception {
        if (permissions == null) {
            throw new NullPointerException("permission == null");
        }
        ArrayList<String> prePermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(LAskPermissions.this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                prePermissions.add(permission);
            }
        }
        if (prePermissions.isEmpty()) {
            finish();
            //已全部授权
            if (mRequestPermissionCallback != null) {
                mRequestPermissionCallback.granted();
            }
        } else {
            //申请权限
            ActivityCompat.requestPermissions(this, prePermissions.toArray(new String[]{}),
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        finish();
        if (requestCode == PERMISSION_REQUEST_CODE) {
            ArrayList<String> grantedPermissions = new ArrayList<>();
            ArrayList<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i]);
                } else {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (deniedPermissions.isEmpty()) {
                // Permission Granted
                if (mRequestPermissionCallback != null) {
                    mRequestPermissionCallback.granted();
                }
            } else {
                // Permission Denied
                if (mRequestPermissionCallback != null) {
                    mRequestPermissionCallback.denied(deniedPermissions);
                }

            }
        }
    }
}
