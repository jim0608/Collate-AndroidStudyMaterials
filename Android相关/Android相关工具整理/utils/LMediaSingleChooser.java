package com.blackbox.lerist.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.socks.library.KLog;

public class LMediaSingleChooser {

    public interface Callback {
        void onSuccess(String filePath);

        void onFailure();
    }

    public static void selectVideo(final Context context, final Callback callback) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra("return-data", true);

        LResultActivity.startActivityForResult(context, intent, new LResultActivity.Callback() {
            @Override
            public void onSuccess(Intent result) {
                Uri uri = result.getData();
                String path = FileUtils.getPath(context, uri);
                KLog.i(path);
                callback.onSuccess(path);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });

    }
    public static void selectAudio(final Context context, final Callback callback) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.putExtra("return-data", true);
//        onClickStartActivity.setAction(Intent.ACTION_GET_CONTENT);
//        onClickStartActivity.setType("image/*");
//        onClickStartActivity.putExtra("return-data", true);

        LResultActivity.startActivityForResult(context, intent, new LResultActivity.Callback() {
            @Override
            public void onSuccess(Intent result) {
                Uri uri = result.getData();
                String path = FileUtils.getPath(context, uri);
                KLog.i(path);
                callback.onSuccess(path);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });

    }
}
