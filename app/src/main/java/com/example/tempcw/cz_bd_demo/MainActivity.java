package com.example.tempcw.cz_bd_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.example.tempcw.cz_bd_demo.map.BPoiPointSearch;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BPoiPointSearch.OnBSearchCompleteListener
        {
    /**
     * 翻页按钮
     */
    private TextView btnPrePage, btnNextPage;
    /**
     * 是否首次定位的标志位
     */
    private boolean isFirstLoc = true;// 是否首次定位
    /**
     * 当前检索的索引值
     */
    private int currentSelect = 4;// 判断当前所选的类型
    /**
     * 百度地图搜索帮助实例
     */
    /**
     * 列表数据源
     */
    private ArrayList<Object> poiDataSource = new ArrayList<Object>();
    private BPoiPointSearch mBaiduSearcher;
            private String currentCity = "";
    private MapView mMapView=null;
    private BaiduMap mBaiduMap;
    protected MapStatus.Builder mBuilder;
    private LocationClient mLoactionClient=null;
    private BDLocationListener mLocationListener;
    private MapStatusUpdate mapStatusUpdate;
    /**
     * 警务部门的折叠按钮 展开收起按钮
     */
    @Bind(R.id.rb_police_dept_switch)
    RadioButton rbPoliceDeptSwitch;
    @Bind(R.id.radio_group)
    RadioGroup radio_group;
    @Bind(R.id.bottom_relative)
    View bottomRelative;
    @Bind(R.id.police_dept_wrap)
    View policeDeptWrap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map_main);                 //a2h8ixF5yvXuxfHXaKQjHGDkfGo77XNF  百度地图key
        ButterKnife.bind(this);
        setupView();
        mMapView= (MapView) findViewById(R.id.MapView);
        initMapView();
        mLoactionClient=new LocationClient(this);
        mLocationListener=new MyBaidduLoacation();
        mLoactionClient.registerLocationListener(mLocationListener);
        initLocation();
        mBaiduSearcher = BPoiPointSearch.getInstance(mMapView, this);
        mBaiduSearcher.setOnSearchCompleteListener(this);
      //  mLoactionClient.start();

    }

    private void setupView() {
        View bottomView = getLayoutInflater().inflate(R.layout.listview_bottom, null);
        bottomView.setEnabled(false);
        bottomView.setClickable(false);
        btnPrePage = (TextView) bottomView.findViewById(R.id.last_page);
        btnNextPage = (TextView) bottomView.findViewById(R.id.next_page);
        // 普通按钮的监听
        findViewById(R.id.btn_loc_req).setOnClickListener(this);
        findViewById(R.id.img_full_screen).setOnClickListener(this);
        findViewById(R.id.img_enlarge).setOnClickListener(this);
        findViewById(R.id.img_narrow).setOnClickListener(this);
        btnPrePage.setOnClickListener(this);
        btnNextPage.setOnClickListener(this);

        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_vehicle_administration:
                        currentSelect = 4;
                        break;
                    case R.id.rb_fueling_station:
                        currentSelect = 5;
                        break;
                    case R.id.rb_bus_station:
                        currentSelect = 6;
                        break;
                    case R.id.rb_bank:
                        currentSelect = 7;
                        break;
                    case R.id.rb_hotel:
                        currentSelect = 8;
                        break;
                    case R.id.rb_toilet:
                        currentSelect = 9;
                        break;
                    case R.id.rb_supermarket:
                        currentSelect = 10;
                        break;
                }
                policeDeptWrap.setVisibility(View.GONE); //设置警务部门,派出所等不可见
                rbPoliceDeptSwitch.setSelected(false);
                searchPois(false);
            }
        });
    }
    private void searchPois(boolean isDrag) {
        mBaiduSearcher.searchInBounds(currentSelect, isDrag);
    }


    public void initMapView(){
        mBaiduMap=mMapView.getMap();
        mMapView.showZoomControls(false);
        mBuilder=new MapStatus.Builder(mBaiduMap.getMapStatus());
        mBuilder.zoom(15);

        mBaiduMap.setMyLocationEnabled(true);

        mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mBuilder.build());
        mBaiduMap.setMaxAndMinZoomLevel(16, 12);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                requestLocClick();
            }
        });
        // 滑动监听，在此处搜索poi（兴趣点）
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                searchPois(true);
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onPause();
        if (mLoactionClient != null) {
            mLoactionClient.stop();
        }
        mBaiduSearcher.recycle();
        mLocationListener = null;
        mBaiduMap.setMyLocationEnabled(false);
        mLoactionClient = null;
        mMapView.onDestroy();
        ButterKnife.unbind(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理

    }


    private void initLocation(){
//        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
//        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
//        int span=1000;
//        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
//        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
//        option.setOpenGps(true);//可选，默认false,设置是否使用gps
//        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
//        mLoactionClient.setLocOption(option);

        LocationClientOption option = new LocationClientOption();
        option.setAddrType("all");//all表示返回所有的数据
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);
        // option.setPoiNumber(10);
        mLoactionClient.setLocOption(option);
    }
    /**
     * 手动触发一次定位请求
     */
    public void requestLocClick() {
        isFirstLoc = true;
        if (!mLoactionClient.isStarted())
            mLoactionClient.start();
        mLoactionClient.requestLocation();
        Toast.makeText(MainActivity.this,"正在定位",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_loc_req:// 定位
                requestLocClick();
                break;
//            case R.id.img_full_screen:// 全屏
//                bottomRelative.setVisibility(bottomRelative.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//                break;
            case R.id.img_enlarge:// 放大
                mMapView.getMap().animateMapStatus(MapStatusUpdateFactory.zoomIn());
                break;
            case R.id.img_narrow:// 缩小
                mMapView.getMap().animateMapStatus(MapStatusUpdateFactory.zoomOut());
                break;
//            case R.id.last_page:// 上一页
//                searchPois(--pageIndex);
//                break;
//            case R.id.next_page:// 下一页
//                searchPois(++pageIndex);
//                break;
            default:
                break;
        }
    }

    @Override
    public void onBSearchStart() {

    }

    @Override
    public void onBSearchSuccess(PoiResult result) {
        List<PoiInfo> data = result.getAllPoi();
        if (data != null && !data.isEmpty()) {
            poiDataSource.addAll(data);
        }
        notifyListStatusChange(result.getCurrentPageNum(), result.getTotalPageNum(), data);
    }

    @Override
    public void onBSearchFailure() {

    }

    @Override
    public void onBSearchFinish() {

    }

    @Override
    public void bpoiCloseTitleBar() {

    }

    private void notifyListStatusChange(int currentPageNum, int totalPageNum, List<? extends Object> data) {
        Log.e("flo", currentPageNum + "||||" + totalPageNum);
        if (currentPageNum == 0) {
            btnPrePage.setClickable(false);
            btnPrePage.setTextColor(getResources().getColor(R.color.light_gray));
        } else {
            btnPrePage.setClickable(true);
            btnPrePage.setTextColor(getResources().getColor(R.color.white));
        }

        if (currentPageNum >= totalPageNum - 1 || data.size() < 10) {
            btnNextPage.setClickable(false);
            btnNextPage.setTextColor(getResources().getColor(R.color.light_gray));
        } else {
            btnNextPage.setClickable(true);
            btnNextPage.setTextColor(getResources().getColor(R.color.white));
        }
    }



    public class MyBaidduLoacation implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null)
                return;
            currentCity = bdLocation.getCity();
            if (!TextUtils.isEmpty(currentCity)) {
                mBaiduSearcher.setCurrentCity(currentCity);
            }
            Log.i("jc", "经度" + bdLocation.getLatitude() + "纬度" + bdLocation.getLongitude());
            // 此处设置开发者获取到的方向信息，顺时针0-360
            // 此处设置开发者获取到的方向信息，顺时针0-360
            MyLocationData locData = new MyLocationData.Builder().accuracy(bdLocation.getRadius())
                    .direction(100).latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);

            }
        }
    }
}