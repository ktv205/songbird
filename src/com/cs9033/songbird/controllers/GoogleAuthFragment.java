package com.cs9033.songbird.controllers;

import com.cs9033.songbird.R;
import com.cs9033.songbird.models.RequestParams;
import com.cs9033.songbird.models.SongBirdPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class GoogleAuthFragment extends Fragment implements
		ConnectionCallbacks, OnConnectionFailedListener {
	// private static final String TAG = "GoogleAuthFragment";
	private static final String DEBUG = "GoogleAuthFragment";

	/* Request code used to invoke sign in user interactions. */
	public static final int RC_SIGN_IN = 0;
	private GoogleApiClient mGoogleApiClient;
	/*
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;
	private boolean mSignInClicked;
	Button button;
	private ConnectionResult mConnectionResult;
	View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_google, container, false);
		mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(DEBUG, "onActivtyCreated");
		view.findViewById(R.id.google_auth_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!mGoogleApiClient.isConnecting()) {
							mSignInClicked = true;
							resolveSignInError();
						}

					}
				});

	}

	@Override
	public void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				getActivity().startIntentSenderForResult(
						mConnectionResult.getResolution().getIntentSender(),
						RC_SIGN_IN, null, 0, 0, 0);
			} catch (IntentSender.SendIntentException e) {
				// The intent was canceled before it was sent. Return to the
				// default
				// state and attempt to connect to get an updated
				// ConnectionResult.
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			mConnectionResult = result;
			if (mConnectionResult != null) {
			}

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		mSignInClicked = false;
		SharedPreferences pref = getActivity().getSharedPreferences("AUTH",
				FragmentActivity.MODE_PRIVATE);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt("AUTH?", 2);
		edit.commit();
		Person currentPerson = Plus.PeopleApi
				.getCurrentPerson(mGoogleApiClient);
		String name = currentPerson.getDisplayName();
		int auth = 0;
		String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
		Image picture = currentPerson.getImage();

		Log.d(DEBUG, "string picture->" + picture.getUrl());
		 Intent service=new Intent(getActivity(), ProfilePicService.class);
		 service.putExtra("URI", picture.getUrl());
		 getActivity().startService(service);
		RequestParams params = new RequestParams();
		params.setMethod("GET");
		params.setURI("http://" + SongBirdPreferences.ipAdd
				+ "/songbird/beta/Registration.php");
		params.setParam("auth", String.valueOf(auth));
		params.setParam("name", name);
		params.setParam("email", email);
		AuthAsyncTask async = new AuthAsyncTask(getActivity());
		async.execute(params);
		storeInSharedPreferences(name, email, auth, picture.getUrl());
		getActivity().setResult(SongBirdPreferences.RESPONSE_CODE_AUTH);
		getActivity().finish();
	}

	@Override
	public void onConnectionSuspended(int arg0) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("GoogleAuth", "onActivityResult out side");
		if (requestCode == RC_SIGN_IN) {
			Log.d("GoogleAuth", "onActivityResult RC_SIGN_IN");
			if (resultCode != Activity.RESULT_OK) {
				Log.d("GoogleAuth",
						"onActivityResult resultCode != Activity.RESULT_OK");
				mSignInClicked = false;
			}
			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				Log.d("GoogleAuth", "!mGoogleApiClient.isConnecting()");
				mGoogleApiClient.connect();
			}
		}
	}

	public void storeInSharedPreferences(String name, String email, int auth,
			String picture) {
		SharedPreferences pref = getActivity().getSharedPreferences(
				SongBirdPreferences.Auth.AUTHPREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(SongBirdPreferences.Auth.KEY_AUTH,
				SongBirdPreferences.Auth.GOOGLE_AUTH);
		edit.putString(SongBirdPreferences.Auth.KEY_NAME, name);
		edit.putString(SongBirdPreferences.Auth.KEY_EMAIL, email);
		edit.putString(SongBirdPreferences.Auth.KEY_PICTURE, picture);
		edit.commit();
	}

}
