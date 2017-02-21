package com.example.tempcw.cz_bd_demo.map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.tempcw.cz_bd_demo.R;
import com.example.tempcw.cz_bd_demo.map.overlayutil.DrivingRouteOverlay;
import com.example.tempcw.cz_bd_demo.map.overlayutil.OverlayManager;
import com.example.tempcw.cz_bd_demo.map.overlayutil.TransitRouteOverlay;
import com.example.tempcw.cz_bd_demo.map.overlayutil.WalkingRouteOverlay;


import java.util.ArrayList;

/**
 * 路径规划
 *
 * @author hxz
 */
public class CalRouteActivity extends AbstractMapActivity implements BaiduMap.OnMapClickListener, OnGetRoutePlanResultListener {
    private MapView mapView;
    private Button btn_bus, btn_car, btn_walk;
    private ImageView btn_zoom_in, btn_zoom_out;
    private ImageView btn_loc, btn_del;

    private Button btn_pre, btn_next, btn_back;
    private LinearLayout ll_Detail, ll_Node;
    private TextView tvPlanT, tvPd1, tvPd2, tvPd3, tvDetail;

    private CheckBox btn_layout, btn_traff;
    private Intent localIntent;
    // MKRoute route = null;// 保存驾车/步行路线数据的变量，供浏览节点时使用
    // private MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    int searchType = -1;// 记录搜索的类型，区分驾车/步行和公交
    private String sCity, eCity;
    private double sLot, sLat, eLot, eLat;
    int nodeIndex = -1;// 节点索引,供浏览节点时使用
    // TransitOverlay transitOverlay = null;// 保存公交路线图层数据的变量，供浏览节点时使用
    // private MapController controller;
    // private PopupOverlay pop = null;// 弹出泡泡图层，浏览节点时使用
    private ProgressBar pb = null;
    private RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    private TextView popupText = null;// 泡泡view
    private final OnClickListener clickLinstener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_by_bus:
                    calRouteExe(v);
                    break;
                case R.id.btn_by_car:
                    calRouteExe(v);
                    break;
                case R.id.btn_by_walk:
                    calRouteExe(v);
                    break;
                case R.id.btn_zin:
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
                    break;
                case R.id.btn_zout:
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
                    break;
                case R.id.btn_traff:
                    mBaiduMap.setTrafficEnabled(((CheckBox) v).isChecked());
                    break;
                case R.id.btn_lay:
                    if (((CheckBox) v).isChecked()) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    } else {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    }
                    break;
                case R.id.btn_del:
                    DeleteOverLay();
                    break;
                case R.id.btn_pre:
                    showNodeInfo(v);
                    break;
                case R.id.btn_next:
                    showNodeInfo(v);
                    break;
                case R.id.btn_back:
                    CalRouteActivity.this.finish();
                    break;
                case R.id.tv_plandetail:
                    if (route != null)
                        redirectToDetail();
                    break;
                case R.id.btn_loc:
                    if (null != routeOverlay)
                        routeOverlay.zoomToSpan();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cal_route);
        findView();
        setListener();
        initData();
        initMapView();
        setTitle("路径导航");
        calRouteExe(btn_walk);
    }

    protected void DeleteOverLay() {
        if (routeOverlay == null) {
            hideDetailLayout(true);
            return;
        }
        mBaiduMap.clear();
        hideDetailLayout(true);
        changeBtnState(-1);
    }

    private void hideDetailLayout(boolean b) {
        if (b) {
            ll_Detail.setVisibility(View.GONE);
            ll_Node.setVisibility(View.GONE);
        } else {
            ll_Detail.setVisibility(View.VISIBLE);
            ll_Node.setVisibility(View.VISIBLE);
        }
    }

    private void setListener() {
        btn_bus.setOnClickListener(clickLinstener);
        btn_car.setOnClickListener(clickLinstener);
        btn_walk.setOnClickListener(clickLinstener);
        btn_zoom_in.setOnClickListener(clickLinstener);
        btn_zoom_out.setOnClickListener(clickLinstener);
        btn_traff.setOnClickListener(clickLinstener);
        btn_layout.setOnClickListener(clickLinstener);
        btn_del.setOnClickListener(clickLinstener);
        btn_loc.setOnClickListener(clickLinstener);
        btn_pre.setOnClickListener(clickLinstener);
        btn_next.setOnClickListener(clickLinstener);
        btn_back.setOnClickListener(clickLinstener);
        tvDetail.setOnClickListener(clickLinstener);
    }

    private void initData() {
        localIntent = getIntent();
        sCity = localIntent.getStringExtra("local_city");
        eCity = localIntent.getStringExtra("destination_city");
        sLot = localIntent.getDoubleExtra("local_Longtitude", 0);
        sLat = localIntent.getDoubleExtra("local_Latitude", 0);
        eLot = localIntent.getDoubleExtra("destination_Longtitude", 0);
        eLat = localIntent.getDoubleExtra("destination_Latitude", 0);
    }

    private void initMapView() {
        mapView.showZoomControls(false);
        mBaiduMap = mapView.getMap();
        mBuilder = new MapStatus.Builder(mBaiduMap.getMapStatus());
        if (sLot > 0 && sLat > 0) {
            mBuilder.target(new LatLng(sLat, sLot));
        }
        mBuilder.zoom(currentZoomLevel);
        mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mBuilder.build());
        mBaiduMap.setMapStatus(mapStatusUpdate);
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                calRouteExe(btn_walk);// 默认路径规划为步行
            }
        });
        mBaiduMap.setOnMapClickListener(this);
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }

    private void findView() {
        mapView = (MapView) findViewById(R.id.mv_mapView);
        btn_zoom_in = (ImageView) findViewById(R.id.btn_zin);
        btn_zoom_out = (ImageView) findViewById(R.id.btn_zout);
        btn_bus = (Button) findViewById(R.id.btn_by_bus);
        btn_car = (Button) findViewById(R.id.btn_by_car);
        btn_walk = (Button) findViewById(R.id.btn_by_walk);
        btn_traff = (CheckBox) findViewById(R.id.btn_traff);
        btn_layout = (CheckBox) findViewById(R.id.btn_lay);
        btn_del = (ImageView) findViewById(R.id.btn_del);
        btn_loc = (ImageView) findViewById(R.id.btn_loc);
        btn_pre = (Button) findViewById(R.id.btn_pre);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_back = (Button) findViewById(R.id.btn_back);
        ll_Detail = (LinearLayout) findViewById(R.id.ll_detail);
        ll_Node = (LinearLayout) findViewById(R.id.ll_node);
        tvPlanT = (TextView) findViewById(R.id.tv_plan);
        tvPd1 = (TextView) findViewById(R.id.tv_plan_d1);
        tvPd2 = (TextView) findViewById(R.id.tv_plan_d2);
        tvPd3 = (TextView) findViewById(R.id.tv_plan_d3);
        tvDetail = (TextView) findViewById(R.id.tv_plandetail);
        pb = (ProgressBar) findViewById(R.id.pb_progress);
        pb.setVisibility(View.GONE);
    }

    private void calRouteExe(View v) {
        if (sLat <= 0 || sLot <= 0 || eLot <= 0 || eLat <= 0) {
           // UiUtil.toast(this, getString(R.string.cal_rout_fail));
        }
        PlanNode stNode = PlanNode.withLocation(new LatLng(sLat, sLot));
        PlanNode enNode = PlanNode.withLocation(new LatLng(eLat, eLot));
        int flag = -2;
        pb.setProgress(0);
        pb.setVisibility(View.VISIBLE);
        route = null;
        btn_pre.setVisibility(View.INVISIBLE);
        btn_next.setVisibility(View.INVISIBLE);
        mBaiduMap.clear();
        switch (v.getId()) {
            case R.id.btn_by_bus:
                changeBtnState(0);
                mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city(sCity).to(enNode).city(eCity));
                break;
            case R.id.btn_by_car:
                changeBtnState(1);
                mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
                break;
            case R.id.btn_by_walk:
                changeBtnState(2);
                mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
                break;
        }
    }

    private void changeBtnState(int i) {
        switch (i) {
            case 0:
                btn_bus.setEnabled(false);
                btn_car.setEnabled(true);
                btn_walk.setEnabled(true);
                break;
            case 1:
                btn_bus.setEnabled(true);
                btn_car.setEnabled(false);
                btn_walk.setEnabled(true);
                break;
            case 2:
                btn_bus.setEnabled(true);
                btn_car.setEnabled(true);
                btn_walk.setEnabled(false);
                break;
            default:
                btn_bus.setEnabled(true);
                btn_car.setEnabled(true);
                btn_walk.setEnabled(true);
                break;
        }
    }

    private void redirectToDetail() {
        Intent dIntent = new Intent(CalRouteActivity.this, DetailActivity.class);
        Bundle dBundle = new Bundle();
        dBundle.putInt("type", searchType);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < route.getAllStep().size(); i++) {
            if (route.getAllStep().get(i) instanceof WalkingRouteLine.WalkingStep) {
                list.add(((WalkingRouteLine.WalkingStep) route.getAllStep().get(i)).getInstructions());
            } else if (route.getAllStep().get(i) instanceof TransitRouteLine.TransitStep) {
                list.add(((TransitRouteLine.TransitStep) route.getAllStep().get(i)).getInstructions());
            } else if (route.getAllStep().get(i) instanceof DrivingRouteLine.DrivingStep) {
                list.add(((DrivingRouteLine.DrivingStep) route.getAllStep().get(i)).getInstructions());
            }
        }
        if (list.size() <= 0) {
            return;
        }
        dBundle.putStringArrayList("data", list);
        dIntent.putExtras(dBundle);
        startActivity(dIntent);
    }

    private void showNodeInfo(View v) {
        if (route == null || route.getAllStep() == null) {
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.btn_pre) {
            return;
        }
        // 设置节点索引
        if (v.getId() == R.id.btn_next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }
        } else if (v.getId() == R.id.btn_pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }
        // 获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }
        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        // 移动节点至中心
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(CalRouteActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(getResources().getColor(R.color.light_white));
        popupText.setText(nodeTitle);
        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }

    @Override
    protected MapView getMapView() {
        return mapView;
    }

    private String secToTime(int t) {
        Log.d("flo", t + "");
        String min = "分钟";
        String hour = "小时";
        if (t / 60 == 0 && t % 60 != 0) {
            return 1 + min;
        } else if (t / 3600 == 0) {
            return (t / 60 + (t % 60 > 30 ? 1 : 0)) + min;
        } else {
            return t / 3600 + hour + t % 60 + min;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mBaiduMap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    protected void onDestroy() {
        if (mapView != null)
            mSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        pb.setVisibility(View.GONE);
        // 起点或终点有歧义，需要选择具体的城市列表或地址列表
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            hideDetailLayout(false);
            tvPlanT.setText(R.string.walk_plan);
            tvPd1.setText(secToTime(result.getRouteLines().get(0).getDuration()));
            tvPd2.setText(result.getRouteLines().get(0).getDistance() / 1000 + "公里");
            tvPd3.setText("打车" + (result.getTaxiInfo() == null ? 0 : result.getTaxiInfo().getTotalPrice()) + "元");
            searchType = 2;
            nodeIndex = -1;
            btn_pre.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
        pb.setVisibility(View.GONE);
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            hideDetailLayout(false);
            tvPlanT.setText(R.string.bus_plan);
            tvPd1.setText(secToTime(result.getRouteLines().get(0).getDuration()));
            tvPd2.setText(result.getRouteLines().get(0).getDistance() / 1000 + "公里");
            tvPd3.setText("打车" + (result.getTaxiInfo() == null ? 0 : result.getTaxiInfo().getTotalPrice()) + "元");
            searchType = 1;
            nodeIndex = -1;
            btn_pre.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        pb.setVisibility(View.GONE);
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            hideDetailLayout(false);
            tvPlanT.setText(R.string.drive_plan);
            tvPd1.setText(secToTime(result.getRouteLines().get(0).getDuration()));
            tvPd2.setText(result.getRouteLines().get(0).getDistance() / 1000 + "公里");
            tvPd3.setText("打车" + (result.getTaxiInfo() == null ? 0 : result.getTaxiInfo().getTotalPrice()) + "元");
            searchType = 0;
            nodeIndex = -1;
            btn_pre.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    // 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onRouteNodeClick(int i) {
            return onMyRouteNodeClick(i);
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onRouteNodeClick(int i) {
            return onMyRouteNodeClick(i);
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onRouteNodeClick(int i) {
            return onMyRouteNodeClick(i);
        }
    }

    private Boolean onMyRouteNodeClick(int i) {
        nodeIndex = i;
        if (nodeIndex < 0 || nodeIndex > route.getAllStep().size()) {
            return true;
        }
        // 获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }
        if (nodeLocation == null || nodeTitle == null) {
            return true;
        }
        // 移动节点至中心
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(CalRouteActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(getResources().getColor(R.color.light_white));
        popupText.setText(nodeTitle);
        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
        return true;
    }
}
