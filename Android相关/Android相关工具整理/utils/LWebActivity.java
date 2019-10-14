package com.blackbox.lerist.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.blackbox.lerist.R;
import com.blackbox.lerist.activity.LActivity;


public class LWebActivity extends LActivity {
    private boolean isLoaded;
    private OnClickListener onClickListener = new OnClickListener() {
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.web_btn_goback) {
                LWebActivity.this.webView.goBack();
            } else if (i == R.id.web_btn_goforward) {
                LWebActivity.this.webView.goForward();
            } else if (i != R.id.web_btn_close_or_refresh) {
            } else {
                if (LWebActivity.this.isLoaded) {
                    LWebActivity.this.webView.reload();
                } else {
                    LWebActivity.this.webView.stopLoading();
                }
            }
        }
    };
    private String titleStr;
    private String uri;
    private WebView webView;

    public static void open(Context context, String title, String uri) {
        Intent intent = new Intent(context, LWebActivity.class);
        intent.putExtra("Title", title);
        intent.putExtra("Uri", uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            this.titleStr = intent.getStringExtra("Title");
            this.uri = intent.getStringExtra("Uri");
        }
    }

    private void initView() {
        if (this.titleStr != null) {
            setTitle(this.titleStr);
        }
        final ImageView btn_goback = (ImageView) findViewById(R.id.web_btn_goback);
        final ImageView btn_goforward = (ImageView) findViewById(R.id.web_btn_goforward);
        final ImageView btn_closeOrRefresh = (ImageView) findViewById(R.id.web_btn_close_or_refresh);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.web_progressbar);
        btn_goback.setOnClickListener(this.onClickListener);
        btn_goforward.setOnClickListener(this.onClickListener);
        btn_closeOrRefresh.setOnClickListener(this.onClickListener);
        find(R.id.btn_menu).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LWebActivity.this.showMenu(v);
            }
        });
        this.webView = (WebView) findViewById(R.id.web_webview);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setSupportZoom(true);
        this.webView.getSettings().setBuiltInZoomControls(true);
        this.webView.loadUrl(this.uri);
        this.webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LWebActivity.this.webView.loadUrl(url);
                return true;
            }
        });
        this.webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                String webTitle = view.getTitle();
                if (webTitle != null) {
                    LWebActivity.this.setTitle(webTitle);
                }
                if (newProgress == 100) {
                    progressBar.setVisibility(8);
                    LWebActivity.this.isLoaded = true;
                    btn_closeOrRefresh.setImageResource(R.mipmap.webbrowser_refresh);
                } else {
                    progressBar.setVisibility(0);
                    progressBar.setProgress(newProgress);
                    LWebActivity.this.isLoaded = false;
                    btn_closeOrRefresh.setImageResource(R.mipmap.webbrowser_stop);
                }
                if (LWebActivity.this.webView.canGoBack()) {
                    btn_goback.setImageResource(R.mipmap.webbrowser_goback);
                } else {
                    btn_goback.setImageResource(R.mipmap.webbrowser_goback_disabled);
                }
                if (LWebActivity.this.webView.canGoForward()) {
                    btn_goforward.setImageResource(R.mipmap.webbrowser_goforward);
                } else {
                    btn_goforward.setImageResource(R.mipmap.webbrowser_goforward_disabled);
                }
            }
        });
    }

    private void showMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this.context, v);
        popupMenu.getMenu().add(0, 0, 0, "刷新");
        popupMenu.getMenu().add(0, 1, 1, "在浏览器中打开");
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case 0:
                            //刷新
                            webView.reload();
                            break;
                        case 1:
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(Uri.parse(LWebActivity.this.uri));
                            LWebActivity.this.startActivity(intent);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public void onBackPressed() {
        super.onBackPressed();
        ViewGroup parent = (ViewGroup) this.webView.getParent();
        if (parent != null) {
            parent.removeView(this.webView);
        }
        this.webView.removeAllViews();
        this.webView.destroy();
        finish();
    }
}
