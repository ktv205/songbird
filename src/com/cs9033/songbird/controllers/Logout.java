package com.cs9033.songbird.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cs9033.songbird.models.SongBirdPreferences;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

public class Logout implements ConnectionCallbacks, OnConnectionFailedListener {
	private final static String DEBUG = "Logout";
	GoogleApiClient mGoogleApiClient;
	int id;
	Session session;
	Context context;
	private SharedPreferences pref;
	private SharedPreferences.Editor edit;
	OnLogoutSuccessful obj;
	public interface OnLogoutSuccessful {
		public void onCleardFields(boolean cleared);
	}
	public Logout(Context context) {
		this.context = context;
		pref = context.getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				Context.MODE_PRIVATE);
		edit = pref.edit();
	}

	public void logoutFromFacebook() {
		Log.d(DEBUG, "in facebook signout");
		if (Session.getActiveSession() == null) {
			Log.d(DEBUG, "Session.getActiveSession() is null");
			clearSharedPref();
		} else {
			Log.d(DEBUG, "Session.getActiveSession() is null");
			session = Session.getActiveSession();
			if (session != null) {
				Log.d(DEBUG, "session is active");
				session.closeAndClearTokenInformation();
				clearSharedPref();
			} else {
				Log.d(DEBUG, "session is not active");
			}
		}

	}

	public void logoutFromGoogle() {
		Log.d(DEBUG, "in logout google");
		mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		mGoogleApiClient.connect();
		// Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		// mGoogleApiClient.disconnect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d(DEBUG, "connection failed");

	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(DEBUG, "in connected");
		Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		mGoogleApiClient.disconnect();
		clearSharedPref();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.d(DEBUG, "connection suspended");

	}

	public void clearSharedPref() {
		edit.putInt(SongBirdPreferences.Auth.KEY_AUTH, -1);
		edit.putString(SongBirdPreferences.Auth.KEY_NAME, "name");
		edit.putString(SongBirdPreferences.Auth.KEY_EMAIL, "email");
		edit.putInt(SongBirdPreferences.Auth.KEY_PERSON_ID, -1);
		SongBirdPreferences.flagForName=0;
		edit.commit();
		Toast.makeText(context, "you are logged out", Toast.LENGTH_LONG).show();
		Log.d(DEBUG, "cleared the fields");
		obj=(OnLogoutSuccessful)context;
		obj.onCleardFields(true);
	}

}
