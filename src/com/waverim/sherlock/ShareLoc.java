package com.waverim.sherlock;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class ShareLoc extends Activity {
	
	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	private LocationClientOption option = new LocationClientOption();
	
	private double lat = 0.0;
	private double lng = 0.0;
	
	private String baseUrl = new BaseUrl().GetBaseUrl();
	
	private String user_id = "";
	
	private Boolean isSharing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_loc);
		
		Intent intent = getIntent();
		user_id = intent.getExtras().getString("user_id");
		
		Button btn_share_once = (Button) findViewById(R.id.btn_share_once);
		final Button btn_share_more = (Button) findViewById(R.id.btn_share_more);
		Button btn_map = (Button) findViewById(R.id.btn_map);
		
		mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener( myListener );
        
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);//设置发起定位请求的间隔时间
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);

		//分享一次
		btn_share_once.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				option.setScanSpan(999999900);
				mLocationClient.setLocOption(option);
				mLocationClient.start();
				if (mLocationClient != null && mLocationClient.isStarted())
		        	mLocationClient.requestLocation();
		        else 
		        	Log.d("LocSDK4", "locClient is null or not started");
			}
		});
		
		//连续分享
		btn_share_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isSharing) {
					btn_share_more.setText("停止分享");
					isSharing = true;
					
					option.setScanSpan(5000);
					mLocationClient.setLocOption(option);
					mLocationClient.start();
					if (mLocationClient != null && mLocationClient.isStarted())
			        	mLocationClient.requestLocation();
			        else 
			        	Log.d("LocSDK4", "locClient is null or not started");
				} else {
					btn_share_more.setText("连续分享");
					isSharing = false;
					
					mLocationClient.stop();
				}
			}
		});
		
		//好友地图
		btn_map.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(ShareLoc.this, Map.class);
				startActivity(intent);
			}
		});
	}
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(final BDLocation location) {
			if (!(lat == location.getLatitude() && lng == location.getLongitude())) {
				new Thread(new Runnable() {  
		            @Override  
		            public void run() {  
		            	HttpClient httpclient = new DefaultHttpClient();
		    			String uri = baseUrl + "InsertCoordinate?userid=" + user_id + "&longitude=" + location.getLongitude() + "&latitude=" + location.getLatitude();
		    			HttpGet request = new HttpGet(uri);
		    			Log.i("request", request.getURI().toString());
		    			try{
		    				HttpResponse response = httpclient.execute(request);
		    				String result = EntityUtils.toString(response.getEntity());
		    				Log.i("response 2", result);
		    			}catch(Exception e){
		    				Log.e("orderlist", "Error in http connection "+e.toString());
		    			}
		            }  
		        }).start();

				lat = location.getLatitude();
				lng = location.getLongitude();
			} 
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.share_loc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	
	 public void onDestroy() {  
		 super.onDestroy();  
		 mLocationClient.stop();
	 }
}
