package com.cs9033.songbird.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.cs9033.songbird.models.RequestParams;
import com.cs9033.songbird.models.SongBirdPreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class AuthAsyncTask extends AsyncTask<RequestParams, Void, Integer> {
	Context context;

	@Override
	protected void onPreExecute() {

		super.onPreExecute();
	}

	public AuthAsyncTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(RequestParams... params) {
		URL url = null;

		HttpURLConnection urlConnection = null;
		int id = 0;
		try {
			Log.d("url",params[0].getURI()+"?"+params[0].getEncodedParams());
			if (params[0].getMethod() == "GET")
				url = new URL(params[0].getURI() + "?"
						+ params[0].getEncodedParams());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);

			urlConnection.connect();
			InputStream in = urlConnection.getInputStream();
			id = Integer.parseInt(new BufferedReader(new InputStreamReader(in))
					.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();
		}
		return id;
	}

	@Override
	protected void onPostExecute(Integer result) {
		SharedPreferences pref=context.getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,Context.MODE_PRIVATE);
		SharedPreferences.Editor edit=pref.edit();
		edit.putInt(SongBirdPreferences.Auth.KEY_PERSON_ID, result);
		edit.commit();
	}

}
