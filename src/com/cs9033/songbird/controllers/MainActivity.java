package com.cs9033.songbird.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs9033.songbird.R;
import com.cs9033.songbird.controllers.Logout.OnLogoutSuccessful;
import com.cs9033.songbird.models.DrawerListOptions;
import com.cs9033.songbird.models.GeofenceModel;
import com.cs9033.songbird.models.RequestParams;
import com.cs9033.songbird.models.SimpleGeofence;
import com.cs9033.songbird.models.Song;
import com.cs9033.songbird.models.SongBirdPreferences;
import com.cs9033.songbird.models.SongDetailsModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
ConnectionCallbacks, OnConnectionFailedListener,
OnMapLongClickListener, OnAddGeofencesResultListener,
OnPreparedListener, OnLogoutSuccessful {
	/*
	 * for Log statements
	 */
	private final static String DEBUG = "MainActivity";
	/*
	 * progress Dialog
	 */
	public ProgressDialog progressDialog;
	/*
	 * ActionBar field
	 */
	private ActionBar actionBar;
	/*
	 * Menu instance
	 */
	private Menu menu;

	/*
	 * related to drawer
	 */
	private DrawerLayout mainDrawer;
	private ListView listDrawer;
	private DrawerListAdapter mainListDrawerAdapter;
	private ActionBarDrawerToggle toggle;
	/*
	 * related to maps
	 */
	private GoogleMap mMap;
	private SupportMapFragment fragment;
	private LocationClient mapClient;
	private MediaPlayer mPlayer;
	private Marker lastMarker = null;
	// private SongDetailsAsyncTask songDetails;
	private ArrayList<GeofenceModel> geofenceList = new ArrayList<GeofenceModel>();
	/*
	 * related to geo fences
	 */
	private static final long SECONDS_PER_HOUR = 60;
	private static final long MILLISECONDS_PER_SECOND = 1000;
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_TIME = GEOFENCE_EXPIRATION_IN_HOURS
			* SECONDS_PER_HOUR * MILLISECONDS_PER_SECOND;
	List<Geofence> mGeofenceList;
	private PendingIntent mGeofenceRequestIntent;
	private List<Song> songs;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(DEBUG, "oncreate");

		/*
		 * checking for the network and starting songDetails AsyncTask
		 */
		if (isConnected()) {
			Log.d(DEBUG, "connected to network");
			// songDetails = new SongDetailsAsyncTask();
			
		} else {
			Log.d(DEBUG, "no network connection");
			Toast.makeText(this, "no network connection", Toast.LENGTH_SHORT)
			.show();
		}
		/*
		 * getting action bar
		 */
		initializeActionBar();

		/*
		 * Map support fragment
		 */
		initializeMapFields();

		/*
		 * getting drawer
		 */
		initializeDrawerFields();

		/*
		 * toggler the icon on the top
		 */
		initializeToggler();

		/*
		 * media player play button
		 */
		initializePlayButton();

	}

	@SuppressLint("NewApi")
	private void initializeActionBar() {
		actionBar = getActionBar();
		if (actionBar == null) {
			Log.d(DEBUG, "action bar null");
		} else {
			Log.d(DEBUG, "action bar not null");
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
			actionBar.setTitle("listen");
		}

	}

	private void initializeMapFields() {
		fragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mMap = fragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		mapClient = new LocationClient(this, this, this);
	}

	private void initializeDrawerFields() {
		listDrawer = (ListView) findViewById(R.id.left_drawer_main);
		mainDrawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
		listDrawer.setBackgroundColor(Color.parseColor("#FFFFFF"));
		mainListDrawerAdapter = new DrawerListAdapter(this, 0);
		listDrawer.setAdapter(mainListDrawerAdapter);
		listDrawer.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(DEBUG, "OnItemClick listner for drawer list");
				mainDrawer.closeDrawer(listDrawer);
				TextView textView = (TextView) view
						.findViewById(R.id.textDrawerList);
				String name = textView.getText().toString();
				Log.d(DEBUG, "name of clicked item->" + name);
				Intent intent;
				if (name.equals(DrawerListOptions.UPLOAD)) {
					intent = new Intent(MainActivity.this, UplaodActivity.class);
					startActivity(intent);
					// finish();
				} else if (name.equals(DrawerListOptions.LISTEN)) {

				} else if (name.equals(DrawerListOptions.AUTHENTICATION)) {
					intent = new Intent(MainActivity.this,
							AuthenticationActivity.class);
					startActivityForResult(intent,
							SongBirdPreferences.REQUEST_CODE_LISTEN_AUTH);
				} else if (name.equals(DrawerListOptions.CREATE)) {
					intent = new Intent(MainActivity.this,
							SoundCreateActivity.class);
					startActivity(intent);
					// finish();
				} else if (name.equals(DrawerListOptions.PROFILE)) {
					intent = new Intent(MainActivity.this,
							ProfileActivity.class);
					startActivity(intent);
					// finish();
				} else if (name.equals(DrawerListOptions.LOGOUT)) {
					if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
									SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
						new Logout(MainActivity.this).logoutFromGoogle();
					} else if (getSharedPreferences(
							SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
									SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
						Log.d(DEBUG, "in facebook signout");
						new Logout(MainActivity.this).logoutFromFacebook();
					}
					Log.d(DEBUG, "after logout");
				}

			}
		});
		mainDrawer.setDrawerListener(toggle);
	}

	private void initializeToggler() {
		toggle = new ActionBarDrawerToggle(this, mainDrawer, R.string.open,
				R.string.close) {
			@SuppressLint("NewApi")
			public void onDrawerClosed(View view) {
				getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
				getActionBar().setTitle(DrawerListOptions.LISTEN);
			}

			@SuppressLint("NewApi")
			public void onDrawerOpened(View drawerView) {
				getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
				getActionBar().setTitle(R.string.app_name);
			}
		};

		toggle.setDrawerIndicatorEnabled(true);
	}

	private void initializePlayButton() {
		ImageButton imageButton = (ImageButton) findViewById(R.id.playMain);
		imageButton.setImageResource(R.drawable.play);
		imageButton.setTag("play");
		mPlayer = new MediaPlayer();

		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(DEBUG, "onClick");
				if (v.getTag().equals("play")) {
					Log.d(DEBUG, "playButton clicked");
					v.setTag("pause");
					((ImageButton) v).setImageResource(R.drawable.pause);
					RequestParams params = new RequestParams();
					// params.setURI("http://" + SongBirdPreferences.ipAdd
					// + "/songbird/beta/Song.php");
					params.setURI("http://" + SongBirdPreferences.ipAdd
							+ "/songbird/beta/CurrentGeoFences.php");
					if(mapClient.isConnected()){
					params.setParam("lat", String.valueOf(mapClient
							.getLastLocation().getLatitude()));
					params.setParam("lng", String.valueOf(mapClient
							.getLastLocation().getLongitude()));
					}else{
						mapClient.connect();
						
						
					}
					params.setMethod("GET");
					// params.setParam(
					// "song_id",
					// String.valueOf(getSharedPreferences(
					// SongBirdPreferences.SongDetails.SONGPREF,
					// MODE_PRIVATE)
					// .getInt(SongBirdPreferences.SongDetails.KEY_CURRENT_SONG_ID,
					// -1)));
					new SongList().execute(params);
					// songDetails = new SongDetailsAsyncTask();
					// songDetails.execute(params);

				} else {
					Log.d(DEBUG, "pauseButton clicked");
					v.setTag("play");
					((ImageButton) v).setImageResource(R.drawable.play);
					Intent i = new Intent(MainActivity.this,PlaySongService.class);
					stopService(i);
				}
			}
		});
	}

	private RequestParams getRequestParams() {
		RequestParams requestParams = new RequestParams();
		requestParams.setURI("http://" + SongBirdPreferences.ipAdd
				+ "/songbird/beta/GeoFences.php");
		return (requestParams);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}
	@Override
	protected void onStop() {
		super.onStop();
		mapClient.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(DEBUG, "onResume");
		if (menu != null) {
			MenuItem item = menu.findItem(R.id.name_account);
			if (item != null)
				changeName(item);
		}
		/*
		 * getting action bar
		 */
		initializeActionBar();

		/*
		 * Map support fragment
		 */
		initializeMapFields();

		/*
		 * getting drawer
		 */
		initializeDrawerFields();

		/*
		 * toggler the icon on the top
		 */
		initializeToggler();

		/*
		 * media player play button
		 */
		if(isConnected()){
			new GetGeofencesAsyncTask().execute(getRequestParams());
		}

	}

	public void changeName(MenuItem item) {
		Log.d(DEBUG, "in ChangeName");
		Log.d(DEBUG,
				"auth pref->"
						+ getSharedPreferences(
								SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE)
								.getInt(SongBirdPreferences.Auth.KEY_AUTH, -1));

		if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
				|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
						MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
								-1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
			item.setTitle(getSharedPreferences(
					SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE).getString(
							SongBirdPreferences.Auth.KEY_NAME, "SIGNIN/SIGNUP"));
		} else {
			item.setTitle("SIGNIN/SIGNUP");
		}
	}

	/*
	 * Menu methods
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		MenuItem item = menu.findItem(R.id.name_account);
		changeName(item);

		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.geofences) {
			Intent intent = new Intent(this, GeoFences.class);
			startActivity(intent);

		} else if (id == R.id.name_account) {
			if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
					MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
					|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							MODE_PRIVATE).getInt(
									SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
				startActivity(new Intent(this, ProfileActivity.class));
				finish();
			} else {
				startActivityForResult(new Intent(this,
						AuthenticationActivity.class),
						SongBirdPreferences.REQUEST_CODE_PROFILE_AUTH);
			}
		}
		if (id == android.R.id.home) {
			if (mainDrawer.isDrawerOpen(listDrawer)) {
				mainDrawer.closeDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
			} else {
				mainDrawer.openDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * overriding onBackPressed Activity
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	/*
	 * onactivityresult
	 */
	@Override
	protected void onActivityResult(int REQUEST_CODE, int RESPONSE_CODE,
			Intent data) {
		if (REQUEST_CODE == SongBirdPreferences.REQUEST_CODE_LISTEN_AUTH) {
			Log.d(DEBUG, "onActivityResult from auth to Listen");
			mainListDrawerAdapter = new DrawerListAdapter(MainActivity.this, 0);
			listDrawer.setAdapter(mainListDrawerAdapter);
			MenuItem item = menu.findItem(R.id.name_account);
			changeName(item);
		} else if (REQUEST_CODE == SongBirdPreferences.REQUEST_CODE_PROFILE_AUTH) {
			Log.d(DEBUG, "onActivityResult to profile from listen activity");
			startActivity(new Intent(this, ProfileActivity.class));
			finish();
		}
	}

	/*
	 * location client and map methods from google
	 */
	@Override
	public void onConnected(Bundle arg0) {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				mapClient.getLastLocation().getLatitude(), mapClient
				.getLastLocation().getLongitude()), 15));
		drawGeoFences(geofenceList);
		mGeofenceRequestIntent = getTransitionPendingIntent();
		createGeoFences();
		mapClient.addGeofences(mGeofenceList, mGeofenceRequestIntent, this);

	}

	private void createGeoFences() {
		mGeofenceList = new ArrayList<Geofence>();

		for (int i = 0; i < geofenceList.size(); i++) {
			// Log.d(DEBUG, "in creating geofences");
			mGeofenceList.add((new SimpleGeofence(String.valueOf(geofenceList
					.get(i).getId()), geofenceList.get(i).getLatitude(),
					geofenceList.get(i).getLongitude(), geofenceList.get(i)
					.getRadius(), GEOFENCE_EXPIRATION_TIME,
					Geofence.GEOFENCE_TRANSITION_ENTER
					| Geofence.GEOFENCE_TRANSITION_EXIT)).toGeofence());

		}

	}

	@Override
	public void onDisconnected() {

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		if (lastMarker != null) {
			lastMarker.remove();
		}
		lastMarker = mMap.addMarker(new MarkerOptions().position(arg0).title(
				"there"));

	}

	/*
	 * onAddGeofences
	 */
	@Override
	public void onAddGeofencesResult(int arg0, String[] arg1) {
		Log.d(DEBUG, "GeofencesAdded");
	}

	/*
	 * drawing geofences on the map fragment
	 */
	private void drawGeoFences(ArrayList<GeofenceModel> list) {
		Log.d(DEBUG, "in draw geofences");
		for (int i = 0; i < list.size(); i++) {
			CircleOptions circleOptions = new CircleOptions()
			.center(new LatLng(list.get(i).getLatitude(), list.get(i)
					.getLongitude())).radius(list.get(i).getRadius())
					.fillColor(list.get(i).getColor()); // In meters
			mMap.addCircle(circleOptions);
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(list.get(i).getLatitude(), list.get(i)
							.getLongitude())).title(list.get(i).getSongName()));
		}
	}
     
	/*
	 * pending intent to be sent to the service
	 */
	private PendingIntent getTransitionPendingIntent() {
		Log.d(DEBUG, "getTransitionPendingIntent");
		Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
		return PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/*
	 * internet connection check method
	 */
	public boolean isConnected() {
		Log.d(DEBUG, "isConnected method");
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * async task for getting geofence objects
	 */
	class GetGeofencesAsyncTask extends
	AsyncTask<RequestParams, Void, ArrayList<GeofenceModel>> {
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setTitle("loading");
			progressDialog.setMessage("we are loading all the geofences");
			progressDialog.show();
		}

		@Override
		protected ArrayList<GeofenceModel> doInBackground(
				RequestParams... params) {
			Log.d(DEBUG, "do in back ground");
			return new ConnectionControl().getGeofences(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<GeofenceModel> result) {
			progressDialog.cancel();
			geofenceList = result;
			if(geofenceList.size()==0){
				geofenceList.add(new GeofenceModel(0, "default", 0.0, 0.0, 1));
				Toast.makeText(MainActivity.this, "No geofences Found", Toast.LENGTH_SHORT).show();
			}
			Log.d("onPostExecute", result.get(0).getSongName());
			mapClient.connect();

		}
	}

	/*
	 * Async task to get the song in a geolocation(stream the song)
	 */
	// public class SongDetailsAsyncTask extends
	// AsyncTask<RequestParams, Void, SongDetailsModel> {
	//
	// @Override
	// protected SongDetailsModel doInBackground(RequestParams... params) {
	// // TODO Auto-generated method stub
	//
	// return new ConnectionControl().getSongDetails(params[0]);
	// }
	//
	// @Override
	// protected void onPostExecute(SongDetailsModel result) {
	// try {
	// mPlayer.reset();
	// Log.d("SelectedSong", result.getSong_link() + ".mp3");
	// mPlayer.setDataSource(result.getSong_link() + ".mp3");
	// mPlayer.setOnPreparedListener(MainActivity.this);
	// mPlayer.prepare();
	//
	// } catch (IllegalArgumentException e) {
	// e.printStackTrace();
	// } catch (SecurityException e) {
	// e.printStackTrace();
	// } catch (IllegalStateException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// super.onPostExecute(result);
	// }
	//
	// }

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();

	}
	@Override
	public void onDestroy(){
		if(PlaySongService.alive){
			Intent intent = new Intent(this,PlaySongService.class);
			stopService(intent);
		}
		super.onDestroy();
	}
	@Override
	public void onCleardFields(boolean cleared) {
		Log.d(DEBUG, "onClearedFields");
		if (cleared) {
			Log.d(DEBUG, "onClearedFields,cleared");
			if (menu != null) {
				Log.d(DEBUG, "onClearedFields,menu!=null");
				MenuItem item = menu.findItem(R.id.name_account);
				if (item != null) {
					Log.d(DEBUG, "onClearedFields,menuItem!=null");
					changeName(item);
				}
			}
			initializeDrawerFields();

		}

	}

	private class SongList extends
	AsyncTask<RequestParams, Void, ArrayList<Song>> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.show();
		}

		@Override
		protected ArrayList<Song> doInBackground(RequestParams... params) {
			Log.d(DEBUG, "doInBackground");
			ArrayList<Song> songs = new ArrayList<>();
			HttpURLConnection urlConnection = null;
			URL url = null;
			int id = 0;
			try {
				url = new URL(params[0].getURI() + "?"
						+ params[0].getEncodedParams());
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d(DEBUG,params[0].getURI()+"?"+params[0].getEncodedParams());
			try {
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("GET");

				urlConnection.connect();
				InputStream in = urlConnection.getInputStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Log.d(DEBUG,"stringBuilder"+sb.toString());
				JSONArray jsArr = new JSONArray(sb.toString());
				for (int i = 0; i < jsArr.length(); i++) {
					JSONObject jsObj = jsArr.getJSONObject(i);
					songs.add(new Song(jsObj.getInt("id"), jsObj
							.getString("song_name"), jsObj
							.getString("song_link"), jsObj.getString("name")));
				}
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			} finally {
				urlConnection.disconnect();
			}
			return songs;
		}

		@Override
		protected void onPostExecute(ArrayList<Song> result) {
			dialog.dismiss();
			Log.d(DEBUG, "onPostExecue");
			songs = result;
			Intent intent=new Intent(MainActivity.this,PlaySongService.class);
			intent.putParcelableArrayListExtra("songlist", (ArrayList<? extends Parcelable>) songs);
			if(!PlaySongService.alive)
				startService(intent);
			intent=new Intent(MainActivity.this,FilterActivity.class);
			intent.putParcelableArrayListExtra("songs", (ArrayList<? extends Parcelable>) songs);
			intent.putParcelableArrayListExtra(SongBirdPreferences.PutExtrasCodes.KEY_GEOFENCES_MAIN_FILTER, geofenceList);
			startActivity(intent);
		}
	}

}
