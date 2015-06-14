package com.cs9033.songbird.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.util.JsonReader;
import android.util.Log;

import com.cs9033.songbird.models.*;

public class ConnectionControl {
	private final static String TAG = "ConnectionControl";

	@SuppressLint("NewApi")
	public ArrayList<GeofenceModel> getGeofences(RequestParams params) {
		ArrayList<GeofenceModel> geoFences = new ArrayList<GeofenceModel>();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(params.getURI());
		HttpResponse response;
		try {
			response = httpClient.execute(httpPost);
			InputStream is = response.getEntity().getContent();
			@SuppressWarnings("resource")
			JsonReader jsReader = new JsonReader(new InputStreamReader(is,
					"UTF-8"));
			jsReader.beginArray();
			while (jsReader.hasNext()) {
				// Log.d("JsonReader", "in while jsReader.hasNext()");
				jsReader.beginObject();
				int id = 0;
				String lat = null, lng = null, rad = null, song_name = null;
				while (jsReader.hasNext()) {
					String name = jsReader.nextName();
					// Log.d("name", name);
					if (name.equals("id")) {
						id = jsReader.nextInt();
					} else if (name.equals("lat")) {
						lat = jsReader.nextString();
					} else if (name.equals("lng")) {
						lng = jsReader.nextString();
					} else if (name.equals("radius")) {
						rad = jsReader.nextString();
					} else if (name.equals("song_name")) {
						song_name = jsReader.nextString();
					} else {
						jsReader.skipValue();
					}
				}
				jsReader.endObject();
				geoFences
						.add(new GeofenceModel(id, song_name, Double
								.valueOf(lat), Double.valueOf(lng), Float
								.valueOf(rad)));
			}
			jsReader.endArray();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		return geoFences;
	}

	public int getId(RequestParams params) {

		URL url = null;
		HttpURLConnection urlConnection = null;
		int id = 0;
		try {
			Log.d("url", params.getURI() + "?" + params.getEncodedParams());
			if (params.getMethod() == "GET")
				url = new URL(params.getURI() + "?" + params.getEncodedParams());
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

			// InputStreamReader testReader = new InputStreamReader(
			// urlConnection.getInputStream());
			// BufferedReader testBuffer = new BufferedReader(testReader);
			// String testLine = testBuffer.readLine();
			// StringBuilder testBuilder = new StringBuilder();
			// while (testLine != null) {
			// testBuilder.append(testLine);
			// testLine = testBuffer.readLine();
			// }
			// Log.d("testBuffer", testBuilder.toString());
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

	public SongDetailsModel getSongDetails(RequestParams params) {
		HttpURLConnection urlConnection = null;
		SongDetailsModel s = null;
		try {
			Log.d("AsyncTask",
					params.getURI() + "?" + params.getEncodedParams());
			URL url = new URL(params.getURI() + "?" + params.getEncodedParams());
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);
			urlConnection.connect();

			InputStream in = urlConnection.getInputStream();

			@SuppressWarnings("resource")
			JsonReader jsReader = new JsonReader(new InputStreamReader(in,
					"UTF-8"));
			jsReader.beginArray();
			while (jsReader.hasNext()) {
				jsReader.beginObject();
				int id = 0;
				String song_name = null, link = null, creator = null;
				while (jsReader.hasNext()) {
					String name = jsReader.nextName();
					if (name.equals("id")) {
						id = jsReader.nextInt();
					} else if (name.equals("song_name")) {
						song_name = jsReader.nextString();
					} else if (name.equals("song_link")) {
						link = jsReader.nextString();
					} else if (name.equals("creator_name")) {
						creator = jsReader.nextString();
					} else {
						jsReader.skipValue();
					}
				}
				jsReader.endObject();
				s = new SongDetailsModel(id, song_name, link, creator);
			}
			jsReader.endArray();

			return s;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			urlConnection.disconnect();
		}

	}
}
