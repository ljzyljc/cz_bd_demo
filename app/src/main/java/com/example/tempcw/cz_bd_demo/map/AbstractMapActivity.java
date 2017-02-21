package com.example.tempcw.cz_bd_demo.map;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapView;

public abstract class AbstractMapActivity extends Activity {

    private MapView mMapView;
    protected BaiduMap mBaiduMap;
    protected MapStatusUpdate mapStatusUpdate;
    protected MapStatus.Builder mBuilder;
    protected float currentZoomLevel = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView == null) {
            mMapView = getMapView();
        }
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
        mMapView = null;
    }

    protected abstract MapView getMapView();
}
