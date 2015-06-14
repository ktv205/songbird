package com.cs9033.songbird.controllers;

import java.util.Arrays;

import com.cs9033.songbird.R;
import com.cs9033.songbird.models.RequestParams;
import com.cs9033.songbird.models.SongBirdPreferences;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("NewApi")
public class FacebookAuthFragment extends Fragment implements GraphUserCallback {
    
	private static final String DEBUG = "FacebookAuthFragment";
	private static final String TAG="FacebookAuthFragment";
	private UiLifecycleHelper uiHelper;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.fragment_facebook, container, false);
		 LoginButton authButton = (LoginButton) view.findViewById(R.id.fb_auth_button);
		 authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes","email"));
		 authButton.setFragment(this);

		return view;
	}
	@SuppressWarnings("deprecation")
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		Log.d("MainFragment","onSessionStateChange");
		if (state.isOpened()) {
			Log.d("MainFragment","onSessionStateChange in opened");
	    	Request.executeMeRequestAsync(session, this);
	        Log.i(TAG, "Logged in...");
	    } else if (state.isClosed()) {
	    	Log.d("MainFragment","onSessionStateChange in closed");
	        Log.i(TAG, "Logged out...");
	    }
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(DEBUG,"onActivtyCreated");
	}
	private Session.StatusCallback callback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			// TODO Auto-generated method stub
			
		}
	};
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	    Session session = Session.getActiveSession();
	    Log.d("MainFragment","OnResume");
	    if(session==null){
	    	  Log.d("MainFragment","OnResume session null");
	    }
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	        Log.d("MainFragment","OnResume if passed");
	    }else{
	    	Log.d("MainFragment","OnResume session not opened or closed");
	    }
	    
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	@Override
	public void onCompleted(GraphUser user, Response response) {
		String name=user.getFirstName()+" "+user.getLastName();
		String email=user.getProperty("email").toString();
		String facebookId=user.getId();
		int auth=1;
		RequestParams params=new RequestParams();
		params.setMethod("GET");
		params.setURI("http://"+ SongBirdPreferences.ipAdd+"/songbird/beta/Registration.php");
		params.setParam("auth", String.valueOf(auth));
		params.setParam("name", name);
		params.setParam("email", email);
		 Intent service=new Intent(getActivity(), ProfilePicService.class);
		 service.putExtra("URI","https://graph.facebook.com/"+facebookId+"/picture?type=large");
		 getActivity().startService(service);
		AuthAsyncTask async=new AuthAsyncTask(getActivity());
		async.execute(params);
		storeInSharedPreferences(name,email,auth,facebookId);
		Intent intent=new Intent();
		getActivity().setResult(SongBirdPreferences.RESPONSE_CODE_AUTH);
		getActivity().finish();
	}
	public void storeInSharedPreferences(String name,String email,int auth,String id){
		SharedPreferences pref=getActivity().getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,Context.MODE_PRIVATE);
		SharedPreferences.Editor edit=pref.edit();
	    edit.putInt(SongBirdPreferences.Auth.KEY_AUTH, SongBirdPreferences.Auth.FACEBOOK_AUTH);
	    edit.putString(SongBirdPreferences.Auth.KEY_NAME,name);
	    edit.putString(SongBirdPreferences.Auth.KEY_EMAIL,email);
	    edit.putString(SongBirdPreferences.Auth.KEY_FACEBOOKID, id);
	    edit.commit();
	}
}
