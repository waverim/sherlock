package com.waverim.sherlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Map extends Activity implements OnGetRoutePlanResultListener{

	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	
	private LocationClient mLocationClient = null;
	private BDLocationListener location_listener = new MyLocationListener();
	
	private boolean start_point = true;
	private boolean end_point = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(location_listener);
        
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(500000);//设置发起定位请求的间隔时间
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);
        
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        if (mLocationClient != null && mLocationClient.isStarted())
        	mLocationClient.requestLocation();
        else 
        	Log.d("LocSDK4", "locClient is null or not started");
        
        LatLng pt1 = new LatLng(30.517397,114.427464);  
        LatLng pt2 = new LatLng(30.517405,114.425668); 
        LatLng pt3 = new LatLng(30.517132,114.425227);  
        LatLng pt4 = new LatLng(30.516592,114.423085);  
        List<LatLng> pts = new ArrayList<LatLng>();  
        pts.add(pt1);
        pts.add(pt2); 
        pts.add(pt3);
        pts.add(pt4); 
        
		for (int i = 0; i < pts.size()-1; i++) {
			RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
			mSearch.setOnGetRoutePlanResultListener(Map.this);
			PlanNode stNode = PlanNode.withLocation(pts.get(i));
			PlanNode enNode = PlanNode.withLocation(pts.get(i+1));
			mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
			
			if (i == 0) start_point = false;
			if (i == pts.size()-1) end_point = true;
		}
		
	}
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.i("lat", Double.toString(location.getLatitude()));
			Log.i("lng", Double.toString(location.getLongitude()));
			
			//中心点
			LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
			MapStatus mMapStatus = new MapStatus.Builder()
	            .target(point)
	            .build();
			MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			mBaiduMap.setMapStatus(mMapStatusUpdate);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        mMapView.onDestroy(); 
        mLocationClient.stop();
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        mMapView.onResume();  
    }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        mMapView.onPause();  
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
    		return;
    	}  
    	if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {  
	        return;  
	    }  
	    if (result.error == SearchResult.ERRORNO.NO_ERROR) {  
	    	DrivingRouteLine route = result.getRouteLines().get(0);
	    	DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
	    	overlay.setData(route);
	    	overlay.addToMap();
	    	overlay.zoomToSpan();
	    }  
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {}
	
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
        	Log.i("start_point", Boolean.toString(start_point));
        	if (start_point == true)
        		return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        	else
        		return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
        	Log.i("end_point", Boolean.toString(end_point));
        	if (end_point == true)
        		return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
        	else
        		return null;
        }
    }
}
