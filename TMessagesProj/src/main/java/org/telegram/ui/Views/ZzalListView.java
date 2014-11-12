package org.telegram.ui.Views;

import android.app.Activity;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.teamjihu.DownloadZzalTask;
import com.teamjihu.S3Util;

import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.ChatActivity;

/**
 * Created by soohwanpark on 2014-11-07.
 */
public class ZzalListView {

    private Activity parentActivity;
    private ChatActivity chatActivity = null;
    private View zzalLayout = null;

    private int screenWidth;
    private int dp200;

    private boolean isShown = false;

    private WebView webviewZzal;

    FrameLayout topLoadingBar;
    ImageView imgLoading;

    public void setContainerView(Activity activity, View containerView, ChatActivity _chatActivity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;

        dp200 = (int)S3Util.dp2px(activity, 200);

        parentActivity = activity;
        chatActivity = _chatActivity;

        zzalLayout = containerView.findViewById(R.id.zzal_layout);
        S3Util.setViewMarginLeft(zzalLayout, screenWidth, false);

        topLoadingBar = (FrameLayout)containerView.findViewById(R.id.topLoadingBar);
        imgLoading = (ImageView)containerView.findViewById(R.id.imgLoading);

        webviewZzal = (WebView)containerView.findViewById(R.id.webview_zzal);
        webviewZzal.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webviewZzal.getSettings().setJavaScriptEnabled(true);
        webviewZzal.setWebViewClient(new WebViewClientClass());
        webviewZzal.setWebChromeClient(new MyWebChromeClient());
        webviewZzal.addJavascriptInterface(new JsInterface(), "native");
    }

    public boolean isShown() {
        return isShown;
    }

    public void showZzalListView(boolean bShow) {
        isShown = bShow;
        if(bShow) {
            S3Util.swipeView(zzalLayout, screenWidth - dp200, null, 300);
            webviewZzal.loadUrl("http://2runzzal.com/themegram");
        } else {
            S3Util.swipeView(zzalLayout, screenWidth, null, 300);
        }
        chatActivity.changeSwipeBackEnabled(!bShow);
    }

    public class JsInterface {
        public JsInterface() { }
        @JavascriptInterface
        public void sendZzal(String url) {
            webviewZzal.post(new Runnable() {
                public void run() {
                    showZzalListView(false);
                }
            });

            DownloadZzalTask task = new DownloadZzalTask(chatActivity);
            task.execute(url);
        }
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return false;
        }
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return false;
        }
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return false;
        }
        Runnable hideLoadingBar = new Runnable() {
            @Override
            public void run() {
                topLoadingBar.setVisibility(View.INVISIBLE);
            }
        };
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if(zzalLayout == null || topLoadingBar == null || imgLoading == null)
                return;
            if(newProgress >= 0) {
                topLoadingBar.setVisibility(View.VISIBLE);
                imgLoading.getLayoutParams().width = (int) (zzalLayout.getWidth() * (double)newProgress/100);
                imgLoading.requestLayout();
            }
            if(newProgress >= 100)
                (new Handler()).postDelayed(hideLoadingBar, 1000);
        }
    }
}
