package com.example.tempcw.cz_bd_demo;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by TempCw on 2016/8/3.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
    }
}
