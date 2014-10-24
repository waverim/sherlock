package com.waverim.sherlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Map extends Activity implements OnGetRoutePlanResultListener{
	
	private String baseUrl = new BaseUrl().GetBaseUrl();

	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	
	private LocationClient mLocationClient = null;
	private BDLocationListener location_listener = new MyLocationListener();
	
	private DrivingRouteOverlay overlay;
	
	private Timer timer;
	
	private ArrayList<User> user = new ArrayList<User>();
	
	private int photonum = 3;
	
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
        option.setScanSpan(500000);
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);
        
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        if (mLocationClient != null && mLocationClient.isStarted())
        	mLocationClient.requestLocation();
        else 
        	Log.d("LocSDK4", "locClient is null or not started");
        
        //页面载入获取数据
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override  
            public void run() {  
            	new getCoordinate().execute();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 5000);
        
        
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
        timer.cancel();
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
	    	overlay = new MyDrivingRouteOverlay(mBaiduMap);
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
        	return BitmapDescriptorFactory.fromResource(R.drawable.blank);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
        	switch (photonum) {
        	case 0: return BitmapDescriptorFactory.fromResource(R.drawable.avatar1);
        	case 1: return BitmapDescriptorFactory.fromResource(R.drawable.avatar2);
        	case 2: return BitmapDescriptorFactory.fromResource(R.drawable.avatar3);
        	case 3: return BitmapDescriptorFactory.fromResource(R.drawable.avatar4);
        	default: return BitmapDescriptorFactory.fromResource(R.drawable.avatar1);
        	}
        	
        }
    }
	
	private class getCoordinate extends AsyncTask<String, Void, String> {
		protected String doInBackground(String...url) {
			HttpClient httpclient = new DefaultHttpClient();
			String uri = baseUrl + "GetCoordinate";
			
			HttpGet request = new HttpGet(uri);
			Log.i("request", request.getURI().toString());
			String result = null;
			try{
				HttpResponse response = httpclient.execute(request);
				result = EntityUtils.toString(response.getEntity());
				Log.i("result", result);
			}catch(Exception e){
				Log.e("orderlist", "Error in http connection "+e.toString());
			}
			
			return result;
		}
		
		protected void onPostExecute(String result) {
			JSONArray jsonArray;
			try {
				jsonArray = new JSONArray(result);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					
					int user_num = -1;
					for (int j = 0; j < user.size(); j++) {
						if (user.get(i).getUserId() == jsonObject.getInt("UserId")) {
							user_num = j;
						}
					}
					
					photonum = jsonObject.getInt("PhotoName");
					
					if (user_num == -1) {
						user.add(new User(
									jsonObject.getInt("UserId"),
									jsonObject.getInt("PhotoName"),
									jsonObject.getInt("Id"),
									jsonObject.getDouble("Latitude"),
									jsonObject.getDouble("Longitude"),
									jsonObject.getString("sharetime")
								));

						RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
						mSearch.setOnGetRoutePlanResultListener(Map.this);
						PlanNode stNode = PlanNode.withLocation(new LatLng(jsonObject.getDouble("Latitude"), jsonObject.getDouble("Longitude")));
						PlanNode enNode = PlanNode.withLocation(new LatLng(jsonObject.getDouble("Latitude"), jsonObject.getDouble("Longitude")));
						mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
						
					} else {
						RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
						mSearch.setOnGetRoutePlanResultListener(Map.this);
						PlanNode stNode = PlanNode.withLocation(new LatLng(user.get(user_num).getLat(), user.get(user_num).getLng()));
						PlanNode enNode = PlanNode.withLocation(new LatLng(jsonObject.getDouble("Latitude"), jsonObject.getDouble("Longitude")));
						mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
						
						user_num = -1;
					}
				}

			} catch (JSONException e) {
				Log.e("map", "Error in http connection "+e.toString());
			}		
		}
	 }
}
