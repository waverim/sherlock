package com.waverim.sherlock;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Build;

public class Login extends Activity {
	
	private FragmentManager fragmentManager;
	private LoginFragment login_fragment = new LoginFragment();
	private RegisterFragment register_fragment = new RegisterFragment();
	
	private int frag_state = 0; //0 -> login; 1 -> register
	
	private static String baseUrl = new BaseUrl().GetBaseUrl();
	
	private static Activity activity = null;
	
	private static int image_num;
	
	private static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        activity = this;
        
        fragmentManager = getFragmentManager();
    	fragmentManager.beginTransaction().add(R.id.content_frame, login_fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.action_login_register:
	    		if (frag_state == 0) {
	    			fragmentManager.beginTransaction().replace(R.id.content_frame, register_fragment).commit();
	    			frag_state = 1;
	    		} else if (frag_state == 1) {
	    			fragmentManager.beginTransaction().replace(R.id.content_frame, login_fragment).commit();
	    			frag_state = 0;
	    		}
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
    	}
    }
    
    public static class LoginFragment extends Fragment {
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
            
            final EditText username = (EditText) rootView.findViewById(R.id.login_username);
            final EditText password = (EditText) rootView.findViewById(R.id.login_password);
            
            Button btn_signin = (Button) rootView.findViewById(R.id.btn_signin);
            btn_signin.setOnClickListener(new OnClickListener () {
	  			@Override
	  			public void onClick(View v) {
	  				new signinClick(getActivity()).execute(
	  						"login", 
	  						username.getText().toString(), 
	  						password.getText().toString()
	  				);
	  			}
            });
            
            return rootView;
        }
    }
    
    public static class RegisterFragment extends Fragment {
    	@SuppressLint("NewApi") @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_register, container, false);
            
            final EditText username = (EditText) rootView.findViewById(R.id.register_username);
            final EditText password = (EditText) rootView.findViewById(R.id.register_password);
            final EditText nickname = (EditText) rootView.findViewById(R.id.register_nickname);
            final EditText phonenum = (EditText) rootView.findViewById(R.id.register_phonenum);
            
            final ArrayList<ImageView> avatar = new ArrayList<ImageView>();
            final ImageView avatar1 = (ImageView) rootView.findViewById(R.id.register_avatar1);
            final ImageView avatar2 = (ImageView) rootView.findViewById(R.id.register_avatar2);
            final ImageView avatar3 = (ImageView) rootView.findViewById(R.id.register_avatar3);
            final ImageView avatar4 = (ImageView) rootView.findViewById(R.id.register_avatar4);
            avatar.add(avatar1);
            avatar.add(avatar2);
            avatar.add(avatar3);
            avatar.add(avatar4);
            for (int i = 0; i < avatar.size(); i++) {
            	final int j = i;
            	avatar.get(i).setImageAlpha(100);
            	
            	avatar.get(i).setOnClickListener(new OnClickListener () {
    				public void onClick(View view) {
    					for (int k = 0; k < avatar.size(); k++) {
    						avatar.get(k).setImageAlpha(100);
    					}
    					avatar.get(j).setImageAlpha(255);
    					image_num = j;
    				}
                });
            }
            
            Button btn_register = (Button) rootView.findViewById(R.id.btn_register);
            btn_register.setOnClickListener(new OnClickListener () {
	  			@Override
	  			public void onClick(View v) {
	  				new signinClick(getActivity()).execute(
	  						"register", 
	  						username.getText().toString(), 
	  						password.getText().toString(),
	  						nickname.getText().toString(),
	  						phonenum.getText().toString(),
	  						Integer.toString(image_num)
	  				);
	  			}
            });
            
            return rootView;
        }
    }
    
    private static class signinClick extends AsyncTask<String, Void, String> {
    	Context mContext;
		public signinClick (Context context){
			mContext = context;
		}
		
		protected void onPreExecute () {
			progress = ProgressDialog.show(mContext, "", "验证中，请稍后……", true);
		}
		
		protected String doInBackground(String...url) {
			HttpClient httpclient = new DefaultHttpClient();
			String uri = "";
			if (url[0] == "login") {
				uri = baseUrl + "Logon?username=" + url[1] + "&password=" + url[2];
			} else if (url[0] == "register") {
				uri = baseUrl + "Register?username=" + url[1] + "&password=" + url[2] + "&nickname=" + url[3] + "&phonenum=" + url[4] + "&photonum=" + url[5];
			}
			
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
			progress.dismiss();
			
			JSONObject jsonObject;
			try {
				jsonObject = (JSONObject) new JSONTokener(result).nextValue();
				
				if (jsonObject.getBoolean("isSuccess")) {
					Bundle data = new Bundle();
					data.putString("user_id", jsonObject.getString("Id"));

					Intent intent = new Intent(mContext, ShareLoc.class);
					intent.putExtras(data);
					mContext.startActivity(intent);
					
					activity.finish();
				} else {
					Toast.makeText(mContext, jsonObject.getString("msg"), 3000).show();
				}
			} catch (JSONException e) {
				Log.e("signin", "Error in http connection "+e.toString());
			}			
		}
	 }
}
