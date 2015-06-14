package com.cs9033.songbird.controllers;

import java.io.File;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cs9033.songbird.R;
import com.cs9033.songbird.controllers.Logout.OnLogoutSuccessful;
import com.cs9033.songbird.models.DrawerListOptions;
import com.cs9033.songbird.models.RequestParams;
import com.cs9033.songbird.models.SongBirdPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class UplaodActivity extends FragmentActivity implements
		OnConnectionFailedListener, ConnectionCallbacks, OnMapClickListener,
		OnMapLongClickListener, OnLogoutSuccessful {
	/*
	 * progress dialog field
	 */
	public ProgressDialog progressDialog;
	private Menu menu;

	/*
	 * some constants
	 */
	private final static String DEBUG = "UploadActivity";
	protected static final int REQUEST_CODE = 0;
	protected static final int AUTH_CODE = 1;
	/*
	 * drawer fields
	 */
	private DrawerLayout uploadDrawer;
	private ListView listDrawer;
	private DrawerListAdapter uploadListDrawerAdapter;
	private ActionBarDrawerToggle toggler;
	/*
	 * map related fields
	 */
	private GoogleMap mMap;
	private SupportMapFragment fragment;
	private LocationClient mClient;
	private Circle circle;
	private Marker marker;
	/*
	 * ui fields
	 */
	private TextView seekbarValue;
	private Button uploadButton;
	/*
	 * file related
	 */
	private String filepath;
	private String filetitle;
	private SharedPreferences uploadPref;
	private SharedPreferences.Editor songsEdit;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_upload);
		uploadPref = getSharedPreferences(
				SongBirdPreferences.SongsUploaded.UPLOADPREF, MODE_PRIVATE);
		songsEdit = uploadPref.edit();
		if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
				|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
						MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
						-1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
			Log.d(DEBUG, "logged in");
		} else {
			Log.d(DEBUG, "not logged in and calling authenticaion Activity");
			startActivityForResult(new Intent(this,
					AuthenticationActivity.class),
					SongBirdPreferences.REQUEST_CODE_UPLOAD_AUTH);
		}
		/*
		 * check for internet
		 */
		if (isConnected()) {
			Log.d(DEBUG, "connected to internet");
		} else {
			Log.d(DEBUG, "connected to internet");
		}
		/*
		 * get actionBar
		 */
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			Log.d(DEBUG, "action bar not null");
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
			actionBar.setTitle(DrawerListOptions.UPLOAD);
		} else {
			Log.d(DEBUG, "action bar is null");
		}
		/*
		 * create Mapfragment and map object and client object
		 */
		fragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mMap = fragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		mClient = new LocationClient(this, this, this);
		/*
		 * get drawer
		 */
		uploadDrawer = (DrawerLayout) findViewById(R.id.drawer_layout_upload);
		listDrawer = (ListView) findViewById(R.id.left_drawer_upload);
		uploadListDrawerAdapter = new DrawerListAdapter(this, 2);
		listDrawer.setAdapter(uploadListDrawerAdapter);
		listDrawer.setBackgroundColor(Color.parseColor("#FFFFFF"));
		if (uploadDrawer.isDrawerOpen(listDrawer)) {
			View view = listDrawer.getChildAt(2);
			if (view == null) {
				Log.d(DEBUG, "view is null");
			} else {
				Log.d(DEBUG, "view is not null");
				view.setBackgroundColor(Color.parseColor("#ADD8E6"));
			}
		}
		listDrawer.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(DEBUG, "OnItemClick listner for drawer list");
				uploadDrawer.closeDrawer(listDrawer);
				TextView textView = (TextView) view
						.findViewById(R.id.textDrawerList);
				String name = textView.getText().toString();
				Log.d(DEBUG, "name of clicked item->" + name);
				Intent intent;
				if (name.equals(DrawerListOptions.UPLOAD)) {

				} else if (name.equals(DrawerListOptions.LISTEN)) {
					intent = new Intent(UplaodActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.AUTHENTICATION)) {
					intent = new Intent(UplaodActivity.this,
							AuthenticationActivity.class);
					startActivity(intent);
				} else if (name.equals(DrawerListOptions.CREATE)) {
					intent = new Intent(UplaodActivity.this,
							SoundCreateActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.PROFILE)) {
					intent = new Intent(UplaodActivity.this,
							ProfileActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.LOGOUT)) {
					if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
						new Logout(UplaodActivity.this).logoutFromGoogle();
					} else if (getSharedPreferences(
							SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
						Log.d(DEBUG, "in facebook signout");
						new Logout(UplaodActivity.this).logoutFromFacebook();
					}
					Log.d(DEBUG, "after logout");

				}
			}

		});
		/*
		 * toggler
		 */
		toggler = new ActionBarDrawerToggle(this, uploadDrawer, R.string.open,
				R.string.close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				Log.d(DEBUG, "drawer closed");
				getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
				getActionBar().setTitle(DrawerListOptions.UPLOAD);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				Log.d(DEBUG, "drawer open");
				View view = listDrawer.getChildAt(2);
				if (view == null) {
					Log.d(DEBUG, "view is null");
				} else {
					Log.d(DEBUG, "view is not null");
					view.setBackgroundColor(Color.parseColor("#ADD8E6"));
				}
				getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
				getActionBar().setTitle(R.string.app_name);
			}

		};

		/*
		 * set drawer listner to toggler
		 */
		uploadDrawer.setDrawerListener(toggler);
		/*
		 * textview for seekbar value creation
		 */
		seekbarValue = (TextView) findViewById(R.id.seekbar_value);
		/*
		 * seekbar creation and listner
		 */
		SeekBar seek = (SeekBar) findViewById(R.id.seekbar_upload);
		seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				seekbarValue.setText(progress + 10 + "");
				Log.d(DEBUG, "onProgressChanges->" + progress);
				if (circle != null) {
					circle.remove();
					marker.remove();
				}
				createACircle();
			}

		});
		/*
		 * upload button
		 */
		uploadButton = (Button) findViewById(R.id.button_upload);
		uploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(UplaodActivity.this,
						ListSongs.class),
						SongBirdPreferences.REQUEST_CODE_UPLOAD_LIST);

				// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				// intent.setType("audio/mpeg");
				// intent.addCategory(Intent.CATEGORY_OPENABLE);
				// startActivityForResult(intent, REQUEST_CODE);

			}
		});
		/*
		 * submit button
		 */
		Button submitButton = (Button) findViewById(R.id.submit_upload);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (validateSubmit()) {
					getSongId();
				} else {
					Toast.makeText(UplaodActivity.this,
							"add a file or record a sound", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		/*
		 * record button
		 */
		Button recordButton = (Button) findViewById(R.id.button_record);
		recordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UplaodActivity.this,
						RecorderActivity.class);
				startActivity(intent);

			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		mClient.connect();
		Log.d(DEBUG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(DEBUG, "onResume called");
	}
  
	@Override
	protected void onStop() {
		super.onStop();
		mClient.disconnect();
		if (circle != null) {
			circle.remove();
			marker.remove();
		}
		Log.d(DEBUG, "onStop");
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
	 * menu item selection
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(DEBUG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		MenuItem item = menu.findItem(R.id.name_account);
		if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
				|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
						MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
						-1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
			item.setTitle(getSharedPreferences(
					SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE).getString(
					SongBirdPreferences.Auth.KEY_NAME, "your name goes here"));
		}
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(DEBUG, "onOptionsItemSelected");
		int id = item.getItemId();

		if (id == android.R.id.home) {
			Log.d(DEBUG, "home button is clicked");
			if (uploadDrawer.isDrawerOpen(listDrawer)) {
				Log.d(DEBUG, "clicked when drawer is open");
				uploadDrawer.closeDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
			} else {
				Log.d(DEBUG, "clicked when drawer is closed");
				uploadDrawer.openDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
			}

		} else if (id == R.id.name_account) {
			startActivity(new Intent(this, ProfileActivity.class));
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * location client call back methods
	 */
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(DEBUG, "onConnected");
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mClient
				.getLastLocation().getLatitude(), mClient.getLastLocation()
				.getLongitude()), 20));
		createACircle();

	}

	@Override
	public void onDisconnected() {

	}

	/*
	 * on map click call back methods
	 */
	@Override
	public void onMapLongClick(LatLng arg0) {

	}

	@Override
	public void onMapClick(LatLng arg0) {

	}

	/*
	 * creating a circle
	 */
	public void createACircle() {
		CircleOptions circleOptions;
		circleOptions = new CircleOptions()
				.center(new LatLng(mClient.getLastLocation().getLatitude(),
						mClient.getLastLocation().getLongitude()))
				.radius(Double.parseDouble(seekbarValue.getText().toString()))
				.fillColor(0x7F00FF00);
		circle = mMap.addCircle(circleOptions);
		marker = mMap.addMarker(new MarkerOptions().position(new LatLng(mClient
				.getLastLocation().getLatitude(), mClient.getLastLocation()
				.getLongitude())));
	}

	/*
	 * validating fields
	 */
	public boolean validateSubmit() {
		if (filepath == null || filetitle == null) {
			return false;
		} else {
			return true;
		}

	}

	/*
	 * adding data to database and gettting the id
	 */
	public void getSongId() {
		RequestParams requestParams = new RequestParams();
		requestParams.setMethod("GET");
		requestParams.setURI("http://" + SongBirdPreferences.ipAdd
				+ "/songbird/beta/insertsong.php");
		requestParams.setParam("song_name", filetitle);
		requestParams.setParam("lat",
				String.valueOf(mClient.getLastLocation().getLatitude()));
		requestParams.setParam("lng",
				String.valueOf(mClient.getLastLocation().getLongitude()));
		requestParams.setParam("radius", seekbarValue.getText().toString());
		requestParams.setParam("creator", String.valueOf(getSharedPreferences(
				SongBirdPreferences.Auth.AUTHPREF, Context.MODE_PRIVATE)
				.getInt(SongBirdPreferences.Auth.KEY_PERSON_ID, -1)));
		new InsertSongAsyncTask().execute(requestParams);
	}

	@Override
	public void onBackPressed() {
		uploadDrawer.openDrawer(listDrawer);
	}

	// /*
	// * adding file to s3
	// */
	// public void addFileToS3(final int id) {
	// final AmazonS3Client client = new AmazonS3Client(
	// new BasicAWSCredentials("AKIAJ4YX333DR4DM4DUQ",
	// "kScJ0H1axGW3ztuuuzBJdEYmRunOEYFCz45m4r0l"));
	// final File filetoupload = new File(filepath);
	// Thread thread = new Thread() {
	// public void run() {
	// PutObjectRequest por = new PutObjectRequest("songbirdsongs",
	// String.valueOf(id), filetoupload);
	// client.putObject(por);
	// }
	// };
	// thread.start();
	// }

	/*
	 * onActivityresult
	 */
	@Override
	protected void onActivityResult(int REQUEST_CODE, int RESPONSE_CODE,
			Intent data) {
		if (REQUEST_CODE == SongBirdPreferences.REQUEST_CODE_UPLOAD_LIST) {
			if (RESPONSE_CODE == SongBirdPreferences.RESPONSE_CODE_LIST_UPLOAD) {
				filepath = data.getExtras().getString("SONGPATH");
				filetitle = data.getExtras().getString("SONGTITLE");
				Toast.makeText(this, "click submit to upload your selection",
						Toast.LENGTH_SHORT).show();

			}
		} else if (REQUEST_CODE == SongBirdPreferences.REQUEST_CODE_UPLOAD_AUTH) {
			if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
					MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
					|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
				Log.d(DEBUG, "logged in");
				uploadListDrawerAdapter = new DrawerListAdapter(this, 2);
				listDrawer.setAdapter(uploadListDrawerAdapter);
				MenuItem item = menu.findItem(R.id.name_account);
				item.setTitle(getSharedPreferences(
						SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE)
						.getString(SongBirdPreferences.Auth.KEY_NAME,
								"SIGNIN/SIGNUP"));

			} else {
				startActivity(new Intent(this, MainActivity.class));
				finish();
			}
		}
		// Log.d(DEBUG, "onActivityResult");
		// if (arg0 == REQUEST_CODE) {
		// Log.d(DEBUG, "onActivityResult,request code passed");
		// if (arg1 == RESULT_OK) {
		// Log.d(DEBUG, "onActivityResult,result code passed");
		// Uri uri = arg2.getData();
		// Log.d(DEBUG, "cotent-uri->" + uri.toString());
		// List<String> list = getFilePathFromUri(uri);
		// filepath = list.get(0);
		// filetitle = list.get(1);
		// Log.d(DEBUG, "file path of the selected->" + filetitle);
		// }
		// } else if (arg0 == AUTH_CODE) {
		// Log.d(DEBUG, "From Authentication activity to onActivityResult");
		// }
	}

	// @SuppressLint("NewApi")
	// public List<String> getFilePathFromUri(Uri contentURI) {
	// List<String> list = new ArrayList<String>();
	// String wholeID = DocumentsContract.getDocumentId(contentURI);
	// Log.d("wholeID", wholeID);
	// String id = wholeID.split(":")[1];
	// String[] column = { MediaStore.Audio.Media.DATA,
	// MediaStore.Audio.Media.DISPLAY_NAME };
	//
	// // where id is equal to
	// String sel = MediaStore.Audio.Media._ID + "=?";
	//
	// Cursor cursor = getContentResolver().query(
	// MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, column, sel,
	// new String[] { id }, null);
	//
	// String filePath = "";
	// String title = "";
	//
	// int columnIndexPath = cursor.getColumnIndex(column[0]);
	// int columnIndexTitle = cursor
	// .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
	// Log.d("cursor", cursor.getCount() + "");
	// if (cursor.moveToFirst()) {
	// filePath = cursor.getString(columnIndexPath);
	// title = cursor.getString(columnIndexTitle);
	// Log.d("string title", title);
	// Log.d("string path", filePath);
	// }
	// list.add(filePath);
	// list.add(title);
	// return list;
	// }

	public class InsertSongAsyncTask extends
			AsyncTask<RequestParams, Void, Integer> {
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(UplaodActivity.this);
			progressDialog.setTitle("Loading");
			progressDialog.setMessage("Inserting data, please wait");
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(RequestParams... params) {

			return new ConnectionControl().getId(params[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d(DEBUG, "id from first async task->" + result);
			new S3AsyncTask().execute(result);
		}
	}

	class S3AsyncTask extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			final AmazonS3Client client = new AmazonS3Client(
					new BasicAWSCredentials("AKIAJ4YX333DR4DM4DUQ",
							"kScJ0H1axGW3ztuuuzBJdEYmRunOEYFCz45m4r0l"));
			final File filetoupload = new File(filepath);
			PutObjectRequest por = new PutObjectRequest("songbirdsongs",
					String.valueOf(params[0]), filetoupload);
			client.putObject(por);
			return params[0];
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d(DEBUG, "id from second async task->" + result);
			RequestParams requestParams = new RequestParams();
			requestParams.setURI("http://" + SongBirdPreferences.ipAdd
					+ "/songbird/beta/downloadfroms3.php");
			requestParams.setMethod("GET");
			requestParams.setParam("id", String.valueOf(result));
			if (filetitle.substring(filetitle.length() - 4).equals(".mid"))
				requestParams.setParam("midflag", "x");
			else
				requestParams.setParam("midflag", "");
			new SongDownloadAsyncTask().execute(requestParams);
		}
	}

	class SongDownloadAsyncTask extends AsyncTask<RequestParams, Void, Integer> {

		@Override
		protected Integer doInBackground(RequestParams... params) {
			return new ConnectionControl().getId(params[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d(DEBUG, "id from third async task->" + result);
			progressDialog.cancel();
			songsEdit.putInt(SongBirdPreferences.SongsUploaded.KEY_SONGS,
					1+uploadPref.getInt(
							SongBirdPreferences.SongsUploaded.KEY_SONGS, 0));
			songsEdit.commit();
			Toast.makeText(UplaodActivity.this, "your sound is uploaded",
					Toast.LENGTH_SHORT).show();
			

		}

	}

	@Override
	public void onCleardFields(boolean cleared) {
		if (cleared) {
			startActivity(new Intent(UplaodActivity.this, MainActivity.class));
			finish();
		}

	}

}
