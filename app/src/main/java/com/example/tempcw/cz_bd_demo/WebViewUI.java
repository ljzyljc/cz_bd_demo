package com.example.tempcw.cz_bd_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * Created by TempCw on 2016/8/4.
 */
public class WebViewUI extends Activity{
    private WebView webView;
    private Button left;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        left= (Button) findViewById(R.id.button);
        left.setVisibility(View.GONE);
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//允许js弹出窗口
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
    //    webView.loadUrl("https://zhfw.cyga.gov.cn/");
        webView.loadUrl("file:///android_asset/demo/index.html");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });


        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()){
                    webView.goBack();
                }
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  //表示按返回键

                        //  时的操作
                        webView.goBack();   //后退

                        //webview.goForward();//前进
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
        webView.addJavascriptInterface(new JavaScriptClass(),"map");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView.canGoBack()){
            left.setVisibility(View.VISIBLE);
        }
    }
    class JavaScriptClass{
        @JavascriptInterface
        public void JsAndroid(){
            Intent intent=new Intent(WebViewUI.this,MainActivity.class);
            startActivity(intent);
        }
    }
}
