package com.example.tempcw.cz_bd_demo.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.tempcw.cz_bd_demo.R;
import com.example.tempcw.cz_bd_demo.map.overlayutil.PoiOverlay;

/**
 * 百度poi查询类
 *
 * @author huangxz
 */
public class BPoiPointSearch implements OnGetPoiSearchResultListener {

    private static BPoiPointSearch instance;

    private static PoiSearch mPoiSearch = null;

    private MapView mMapView;

    public String[] keyMap;

    private Context mContext;

    private boolean isDrag = false;

    private OnBSearchCompleteListener mSearchListener = null;

    private static final int ROWOFPAGE = 10;

    private InfoWindow mInfoWindow;
    private String currentCity = "吉林市";

    /**
     * 获取search类的单例
     */
    public static BPoiPointSearch getInstance(MapView mapView, Context context) {
        synchronized (BPoiPointSearch.class) {
            if (instance == null) {
                instance = new BPoiPointSearch(mapView, context);
            }
            return instance;
        }
    }

    public void recycle() {
        instance = null;
        if (mPoiSearch != null) {
            mPoiSearch.destroy();
        }
        System.gc();
    }

    /**
     * 屏幕范围内的搜索,结果会缩放到所有点平均散布到地图的效果
     */
    public void searchInBounds(int index, int page) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //获取屏幕上面的左上角 右下角 转化为地图上面的坐标
        builder.include(mMapView.getMap().getProjection().fromScreenLocation(new Point(mMapView.getLeft(), mMapView.getTop())));
        builder.include(mMapView.getMap().getProjection().fromScreenLocation(new Point(mMapView.getRight(), mMapView.getBottom())));
        mPoiSearch.searchInBound(new PoiBoundSearchOption().keyword(keyMap[index]).bound(builder.build()).pageNum(page).pageCapacity(ROWOFPAGE));
        mSearchListener.onBSearchStart();
    }

    public void searchInBounds(int index) {
        searchInBounds(index, 0);
    }

    /**
     * 拖动时候的屏幕范围内搜索，此时不会自动缩放
     */
    public void searchInBounds(int index, boolean drag) {
        searchInBounds(index);
        isDrag = true;
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult arg0) {
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(mContext, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (mSearchListener != null) {
                mSearchListener.onBSearchSuccess(result);
            }
            mMapView.getMap().clear();
            PoiOverlay overlay = new MyPoiOverlay(mMapView.getMap());
            mMapView.getMap().setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            if (!isDrag) {
                overlay.zoomToSpan();
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(mContext, strInfo, Toast.LENGTH_LONG).show();
        }
    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            mSearchListener.bpoiCloseTitleBar();
            showCallOut(getPoiResult().getAllPoi().get(index));
            return true;
        }
    }

    /**
     * 弹出详细窗口
     *
     * @param info
     */
    public void showCallOut(final PoiInfo info) {
        View pop = LayoutInflater.from(mContext).inflate(R.layout.cheguansuo_callout, null);
        TextView name = (TextView) pop.findViewById(R.id.tv_cgs_name);
        TextView address = (TextView) pop.findViewById(R.id.tv_cgs_address);
        name.setText(info.name);
        address.setText(info.address);
        pop.findViewById(R.id.btn_navi_to).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mSearchListener.bpoiCloseTitleBar();
                MyLocationData data = mMapView.getMap().getLocationData();
                Intent intent = new Intent(mContext, CalRouteActivity.class);
                intent.putExtra("destination_city", info.city);// 目的地城市
                intent.putExtra("destination_Latitude", info.location.latitude);
                intent.putExtra("destination_Longtitude", info.location.longitude);
                intent.putExtra("local_city", currentCity);// 当前城市
                intent.putExtra("local_Latitude", data.latitude);
                intent.putExtra("local_Longtitude", data.longitude);
                mContext.startActivity(intent);
            }
        });
        mInfoWindow = new InfoWindow(pop, info.location, -20);
        mMapView.getMap().showInfoWindow(mInfoWindow);
    }

    private BPoiPointSearch() {
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
    }

    private BPoiPointSearch(MapView m, Context mContext) {
        this();
        mMapView = m;
        mMapView.getMap().setOnMapClickListener(new OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                mSearchListener.bpoiCloseTitleBar();
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0) {
                mMapView.getMap().hideInfoWindow();
            }
        });
        this.mContext = mContext;
        keyMap = mContext.getResources().getStringArray(R.array.search_item);
    }

    public void setOnSearchCompleteListener(OnBSearchCompleteListener l) {
        this.mSearchListener = l;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    /**
     * 百度搜索结果回调接口
     *
     * @author huangxz
     */
    public interface OnBSearchCompleteListener {
        void onBSearchStart();

        void onBSearchSuccess(PoiResult result);

        void onBSearchFailure();

        void onBSearchFinish();

        void bpoiCloseTitleBar();
    }
}
