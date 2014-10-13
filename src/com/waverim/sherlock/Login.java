package com.waverim.sherlock;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.widget.Toast;
import android.os.Build;

public class Login extends Activity {
	
	private FragmentManager fragmentManager;
	private LoginFragment login_fragment = new LoginFragment();
	private RegisterFragment register_fragment = new RegisterFragment();
	
	private int frag_state = 0; //0 -> login; 1 -> register
	
	private static String baseUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
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
            
            Button btn_signin = (Button) rootView.findViewById(R.id.btn_signin);
            btn_signin.setOnClickListener(new OnClickListener () {
  
	  			@Override
	  			public void onClick(View v) {
	  				new signinClick().execute();
	  			}
          	
            });
            
            return rootView;
        }
    }
    
    public static class RegisterFragment extends Fragment {
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_register, container, false);
        }
    }
    
    private static class signinClick extends AsyncTask<String, Void, String> {
		protected String doInBackground(String...arg0) {
			HttpClient httpclient = new DefaultHttpClient();
			String uri = baseUrl + "/Service.svc/User/Logon?username=aaa&password=bbb";
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
			
		}
	 }
}
