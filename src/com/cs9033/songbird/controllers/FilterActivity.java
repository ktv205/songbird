package com.cs9033.songbird.controllers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.zip.Inflater;

import com.cs9033.songbird.R;
import com.cs9033.songbird.controllers.PlaySongService.SongServiceBinder;
import com.cs9033.songbird.models.GeofenceModel;
import com.cs9033.songbird.models.Song;
import com.cs9033.songbird.models.SongBirdPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

public class FilterActivity extends FragmentActivity implements
ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = "FilterActivity";
	private static final String DEBUG="FilterActivity";
	private LinearLayout filterList;
	private ArrayList<Song> songs;
	private PlaySongService songService;
	private boolean mBound;
	private SharedPreferences foundPref;
	private SharedPreferences.Editor foundEdit;
	private SharedPreferences ratePref;
	private SharedPreferences.Editor rateEdit;
    private ArrayList<GeofenceModel> geofenceList;
    private GoogleMap mMap;
	private SupportMapFragment fragment;
	private LocationClient mapClient;
	private MediaPlayer mPlayer;
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		initializeMapFields();
		foundPref = getSharedPreferences(
				SongBirdPreferences.SongsFound.FOUNDPREF, MODE_PRIVATE);
		foundEdit = foundPref.edit();
		ratePref = getSharedPreferences(SongBirdPreferences.RateSongs.RATEPREF,
				MODE_PRIVATE);
		rateEdit = ratePref.edit();

		Log.i(TAG, "Created");
		filterList = (LinearLayout) findViewById(R.id.filter_list);
		songs = getIntent().getExtras().getParcelableArrayList("songs");
		geofenceList=getIntent().getExtras().getParcelableArrayList(SongBirdPreferences.PutExtrasCodes.KEY_GEOFENCES_MAIN_FILTER);
		LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LayoutInflater mInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(songs.size()==0){
			Toast.makeText(this, "no songs found", Toast.LENGTH_SHORT).show();
		}
		int i=0;
		for (final Song s : songs) {
			View v = mInflater.inflate(R.layout.song_rate_view, null, false);
			CheckBox cb = (CheckBox) v.findViewById(R.id.song_check);
			RatingBar ratingBar=(RatingBar)v.findViewById(R.id.rate_bar);
			Button button=(Button)v.findViewById(R.id.share_song);
			final double lat=geofenceList.get(i).getLatitude();
			final double lng=geofenceList.get(i).getLongitude();
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent2 = new Intent(); intent2.setAction(Intent.ACTION_SEND);
					intent2.setType("text/plain");
					intent2.putExtra(Intent.EXTRA_TEXT, "found a song "+s.getSongName()+" at http://www.google.com/maps/place/"+lat+","+
					                                              lng);  
					startActivity(Intent.createChooser(intent2, "Share via"));
					
				}
			});
			ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
				
				@Override
				public void onRatingChanged(RatingBar ratingBar, float rating,
						boolean fromUser) {
					rateEdit.putFloat(s.getSongName(), rating);
					rateEdit.commit();
					
				}
			});
			ratingBar.setRating(ratePref.getFloat(s.getSongName(),0));
			if (songs.size() > 1) {
				cb.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mBound) {
							Log.i(TAG, "Song filtered");
							songService.filterSong(s.getId());
						} else {
							Intent i = new Intent(FilterActivity.this,
									PlaySongService.class);
							bindService(i, mConnection,
									Context.BIND_ABOVE_CLIENT);
						}

					}
				});
			}
			TextView tv = (TextView) v.findViewById(R.id.song_rate_name);
			tv.setText(s.getSongName());
			tv.setTextColor(geofenceList.get(i).getColor());
			filterList.addView(v);
			i++;
		}
		foundEdit.putInt(
				SongBirdPreferences.SongsFound.KEY_SONGS_FOUND,
				songs.size()
						+ foundPref.getInt(
								SongBirdPreferences.SongsFound.KEY_SONGS_FOUND,
								0));
		foundEdit.commit();
		mBound = false;
	}
	private void initializeMapFields() {
		fragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.filter_map);
		mMap = fragment.getMap();
		mMap.setMyLocationEnabled(true);
		mapClient = new LocationClient(this, this, this);
		mapClient.connect();
	}
	@Override
	protected void onStart() {
		super.onStart();
		Intent i = new Intent(this, PlaySongService.class);
		i.putParcelableArrayListExtra("songlist",
				(ArrayList<? extends Parcelable>) songs);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		Log.i(TAG, "service started");
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SongServiceBinder binder = (SongServiceBinder) service;
			songService = binder.getService();
			mBound = true;
		}
	};

	protected void onDestroy() {
		if (mBound && PlaySongService.alive) {
			this.unbindService(mConnection);
			// songService.unbindService(mConnection);
			mBound = false;
		}
		super.onDestroy();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				mapClient.getLastLocation().getLatitude(), mapClient
				.getLastLocation().getLongitude()), 18));
		drawGeoFences(geofenceList);
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
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
}
