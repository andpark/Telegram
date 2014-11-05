package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.teamjihu.DownloadZzalTask;
import com.teamjihu.EmoticonManager;
import com.teamjihu.ThemeManager;

import org.telegram.android.LocaleController;
import org.telegram.android.PhoneThemeShopEmoji;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.ui.Views.ActionBar.BaseFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by soohwanpark on 2014-10-22.
 */
public class ZzalActivity extends BaseFragment {
    ThemeManager themeManager;
    LayoutInflater INFLATER;

    ChatActivity chatActivity;
    WebView webviewZzal;

    public ZzalActivity( ChatActivity _chatActivity ) {
        chatActivity = _chatActivity;
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.zzal_layout, container, false);
            this.hideActionBar();
            init();
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private void init() {
        webviewZzal = (WebView)((LinearLayout)fragmentView).findViewById(R.id.webview_zzal);
        webviewZzal.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webviewZzal.getSettings().setJavaScriptEnabled(true);
        webviewZzal.setWebViewClient(new WebViewClientClass());
        webviewZzal.loadUrl("http://2runzzal.com/themegram");

        webviewZzal.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final WebView webview = (WebView) v;
                final WebView.HitTestResult result = webview.getHitTestResult();
                if (result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
                    new DownloadZzalTask(chatActivity).execute(result.getExtra());
                }
                return false;
            }
        });
    }

    private Handler mClickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        this.showActionBar();
    }
}