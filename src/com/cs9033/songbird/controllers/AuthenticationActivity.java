package com.cs9033.songbird.controllers;

import com.cs9033.songbird.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class AuthenticationActivity extends FragmentActivity {
	private static final String DEBUG="AuthenticationActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_authentication);
		Log.d(DEBUG,"in onCreate method");
	}
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		Log.d("MainActivity","onActivityResult");
		if (arg0 == GoogleAuthFragment.RC_SIGN_IN) {
			Log.d(DEBUG,"in onActivityResult from googleAuth");
			GoogleAuthFragment fragment = (GoogleAuthFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.google);
            fragment.onActivityResult(arg0, arg1, arg2);
        } else {
        	Log.d(DEBUG,"in onActivityResult from some where else");
            super.onActivityResult(arg0, arg1, arg2);
        }	
	}
}
