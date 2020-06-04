package com.blackbox.lerist.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lerist on 2016/3/19, 0019.
 */
public class NetdataLoader {
    private static OkHttpClient okHttpClient = new OkHttpClient();

    public final static class Loader {
        private String uri;
        private Context context;
        private long startDelay;

        public interface LoadCallback {
            void onSuccess(String result);

            void onError(Throwable ex);
        }

        public Loader(Context context) {
            this.context = context;
        }

        public Loader load(String uri) {
            this.uri = uri;
            return this;
        }

        public Loader startDelay(long startDelay) {
            this.startDelay = startDelay;
            return this;
        }

        public void into(final TextView textView) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Request request = new Request.Builder().url(uri).build();
                        Response response = okHttpClient.newCall(request).execute();
                        final String result = response.body().string();
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!StringUtils.isEmpty(result)) {
                                    textView.setText(result.trim());
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    final RequestParams params = new RequestParams(uri);
//                    x.http().post(params, new Callback.CommonCallback<String>() {
//                        @Override
//                        public void onSuccess(final String result) {
//                            ((Activity) context).runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    String str = "";
//                                    if (!StringUtils.isEmpty(result)) {
//                                        str = result;
//                                        if (result.contains("<html>") && result.contains("</html>")) {
//                                            str = "";
//                                        }
//                                    }
//                                    try {
//                                        textView.setText(str.trim());
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onError(Throwable ex, boolean isOnCallback) {
//
//                        }
//
//                        @Override
//                        public void onCancelled(CancelledException cex) {
//
//                        }
//
//                        @Override
//                        public void onFinished() {
//
//                        }
//                    });
//
//                }
//            }, startDelay);
        }

        public void into(final LoadCallback loadCallback) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder().url(uri).build();
                                final Response response = okHttpClient.newCall(request).execute();
                                final String result = response.body().string();
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            loadCallback.onSuccess(result.trim());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    loadCallback.onError(e);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }).start();
//                    RequestParams params = new RequestParams(uri);
//                    x.http().get(params, new Callback.CommonCallback<String>() {
//                        @Override
//                        public void onSuccess(final String result) {
//                            KLog.i(result);
//                            if (!StringUtils.isEmpty(result)) {
//                                if (result.contains("<html>") && result.contains("</html>")) return;
//                            }
//                            try {
//                                loadCallback.onSuccess(result.trim());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable ex, boolean isOnCallback) {
//                            try {
//                                loadCallback.onError(ex);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(CancelledException cex) {
//
//                        }
//
//                        @Override
//                        public void onFinished() {
//
//                        }
//                    });
                }
            }, startDelay);
        }
    }

    public static Loader with(Context context) {
        return new Loader(context);
    }
}
