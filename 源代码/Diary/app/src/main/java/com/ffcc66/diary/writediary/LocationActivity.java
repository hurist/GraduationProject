package com.ffcc66.diary.writediary;

import android.graphics.Bitmap;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.amap.api.location.AMapLocationClient;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;

import butterknife.BindView;

public class LocationActivity extends BaseActivity {

    @BindView(R.id.webview) WebView webView;

    private AMapLocationClient locationClient = null;

    @Override
    public int initLayout() {
        return R.layout.activity_location;
    }

    @Override
    public void initView() {
        //加载URL
        webView.loadUrl("file:///android_asset/location.html");
//设置webView参数和WebViewClient
        WebSettings webSettings = webView.getSettings();
// 允许webview执行javaScript脚本
        webSettings.setJavaScriptEnabled(true);


        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            // 处理javascript中的alert
            public boolean onJsAlert(WebView view, String url, String message,
                                     final JsResult result) {
                return true;
            };

            // 处理javascript中的confirm
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final JsResult result) {
                return true;
            };

            // 处理定位权限请求
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
            @Override
            // 设置网页加载的进度条
            public void onProgressChanged(WebView view, int newProgress) {
                LocationActivity.this.getWindow().setFeatureInt(
                        Window.FEATURE_PROGRESS, newProgress * 100);
                super.onProgressChanged(view, newProgress);
            }

            // 设置应用程序的标题title
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

    }

    @Override
    public void initData() {
        locationClient = new AMapLocationClient(getApplicationContext());
        locationClient.startAssistantLocation(webView);

    }

    @Override
    protected void onDestroy() {
        locationClient.stopAssistantLocation();
        super.onDestroy();
    }
}
