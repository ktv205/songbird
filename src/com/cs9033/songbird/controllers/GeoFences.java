package com.cs9033.songbird.controllers;

import java.util.ArrayList;
import java.util.List;

import com.cs9033.songbird.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GeoFences extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener, OnAddGeofencesResultListener {
	LocationClient mLocationClient;
	private static final long SECONDS_PER_HOUR = 60;
	private static final long MILLISECONDS_PER_SECOND = 1000;
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_TIME = GEOFENCE_EXPIRATION_IN_HOURS
			* SECONDS_PER_HOUR * MILLISECONDS_PER_SECOND;
	private EditText mLatitude1;
	private EditText mLongitude1;
	private EditText mRadius1;
	private EditText mLatitude2;
	private EditText mLongitude2;
	private EditText mRadius2;
	private SimpleGeofence mUIGeofence1;
	private SimpleGeofence mUIGeofence2;
	List<Geofence> mGeofenceList;
	private PendingIntent mGeofenceRequestIntent;
	public enum REQUEST_TYPE {
		ADD
	};
	private boolean mInProgress;
	private SimpleGeofenceStore mGeofenceStorage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fields_geofence);
		mInProgress = false;
		mLocationClient = new LocationClient(this, this, this);
		mGeofenceStorage = new SimpleGeofenceStore(this);
		mLatitude1 = (EditText) findViewById(R.id.latitude1_fields);
		mLongitude1 = (EditText) findViewById(R.id.longitude1_fields);
		mRadius1 = (EditText) findViewById(R.id.radius1_fields);
		mLatitude2 = (EditText) findViewById(R.id.latitude2_fields);
		mLongitude2 = (EditText) findViewById(R.id.longitude2_fields);
		mRadius2 = (EditText) findViewById(R.id.radius2_fields);
		Button button = (Button) findViewById(R.id.button_fields);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGeofences();
				addGeofences();

			}
		});
		mGeofenceList = new ArrayList<Geofence>();
	}

	public void addGeofences() {
		if (!mInProgress) {
			mInProgress = true;
			mLocationClient.connect();
		} else {
		}
	}

	public void createGeofences() {
		mUIGeofence1 = new SimpleGeofence("10", Double.valueOf(mLatitude1
				.getText().toString()), Double.valueOf(mLongitude1.getText()
				.toString()), Float.valueOf(mRadius1.getText().toString()),
				GEOFENCE_EXPIRATION_TIME,
				Geofence.GEOFENCE_TRANSITION_ENTER);
		mGeofenceStorage.setGeofence("1", mUIGeofence1);
		mUIGeofence2 = new SimpleGeofence("2", Double.valueOf(mLatitude2
				.getText().toString()), Double.valueOf(mLongitude2.getText()
				.toString()), Float.valueOf(mRadius2.getText().toString()),
				GEOFENCE_EXPIRATION_TIME,
				Geofence.GEOFENCE_TRANSITION_ENTER
						| Geofence.GEOFENCE_TRANSITION_EXIT);
		mGeofenceStorage.setGeofence(String.valueOf(2), mUIGeofence2);
		mGeofenceList.add(mUIGeofence1.toGeofence());
		mGeofenceList.add(mUIGeofence2.toGeofence());
	}

	private PendingIntent getTransitionPendingIntent() {
		Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
		return PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
		mGeofenceRequestIntent = getTransitionPendingIntent();
		mLocationClient.addGeofences(mGeofenceList, mGeofenceRequestIntent,
				this);
	}

	@Override
	public void onDisconnected() {
		mInProgress = false;
		mLocationClient = null;

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	public class SimpleGeofence {
		private String mId;
		private double mLatitude;
		private double mLongitude;
		private float mRadius;
		private long mExpirationDuration;
		private int mTransitionType;

		public SimpleGeofence(String geofenceId, double latitude,
				double longitude, float radius, long expiration, int transition) {
			this.mId = geofenceId;
			this.mLatitude = latitude;
			this.mLongitude = longitude;
			this.mRadius = radius;
			this.mExpirationDuration = expiration;
			this.mTransitionType = transition;
		}

		public String getmId() {
			return mId;
		}

		public void setmId(String mId) {
			this.mId = mId;
		}

		public double getmLatitude() {
			return mLatitude;
		}

		public void setmLatitude(double mLatitude) {
			this.mLatitude = mLatitude;
		}

		public double getmLongitude() {
			return mLongitude;
		}

		public void setmLongitude(double mLongitude) {
			this.mLongitude = mLongitude;
		}

		public float getmRadius() {
			return mRadius;
		}

		public void setmRadius(float mRadius) {
			this.mRadius = mRadius;
		}

		public long getmExpirationDuration() {
			return mExpirationDuration;
		}

		public void setmExpirationDuration(long mExpirationDuration) {
			this.mExpirationDuration = mExpirationDuration;
		}

		public int getmTransitionType() {
			return mTransitionType;
		}

		public void setmTransitionType(int mTransitionType) {
			this.mTransitionType = mTransitionType;
		}

		public Geofence toGeofence() {
			return new Geofence.Builder()
					.setRequestId(getmId())
					.setTransitionTypes(mTransitionType)
					.setCircularRegion(getmLatitude(), getmLongitude(),
							getmRadius())
					.setExpirationDuration(mExpirationDuration).build();
		}
	}

	public class SimpleGeofenceStore {
		public static final String KEY_LATITUDE = "com.example.android.geofence.KEY_LATITUDE";
		public static final String KEY_LONGITUDE = "com.example.android.geofence.KEY_LONGITUDE";
		public static final String KEY_RADIUS = "com.example.android.geofence.KEY_RADIUS";
		public static final String KEY_EXPIRATION_DURATION = "com.example.android.geofence.KEY_EXPIRATION_DURATION";
		public static final String KEY_TRANSITION_TYPE = "com.example.android.geofence.KEY_TRANSITION_TYPE";
		public static final String KEY_PREFIX = "com.example.android.geofence.KEY";

		public String getKeyLatitude() {
			return KEY_LATITUDE;
		}

		public String getKeyLongitude() {
			return KEY_LONGITUDE;
		}

		public String getKeyRadius() {
			return KEY_RADIUS;
		}

		public String getKeyExpirationDuration() {
			return KEY_EXPIRATION_DURATION;
		}

		public String getKeyTransitionType() {
			return KEY_TRANSITION_TYPE;
		}

		public String getKeyPrefix() {
			return KEY_PREFIX;
		}
		public static final long INVALID_LONG_VALUE = -999l;
		public static final float INVALID_FLOAT_VALUE = -999.0f;
		public static final int INVALID_INT_VALUE = -999;
		private final SharedPreferences mPrefs;
		private static final String SHARED_PREFERENCES = "SharedPreferences";
		public SimpleGeofenceStore(Context context) {
			mPrefs = context.getSharedPreferences(SHARED_PREFERENCES,
					Context.MODE_PRIVATE);
		}

		public SimpleGeofence getGeoFence(String id) {
			double lat = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE),
					INVALID_FLOAT_VALUE);
			double lng = mPrefs
					.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE),
							INVALID_FLOAT_VALUE);
			float radius = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_RADIUS),
					INVALID_FLOAT_VALUE);
			long expirationDuration = mPrefs.getLong(
					getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
					INVALID_LONG_VALUE);
			int transitionType = mPrefs.getInt(
					getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
					INVALID_INT_VALUE);
			if (lat != INVALID_FLOAT_VALUE && lng != INVALID_FLOAT_VALUE
					&& radius != INVALID_FLOAT_VALUE
					&& expirationDuration != INVALID_LONG_VALUE
					&& transitionType != INVALID_INT_VALUE) {
				return new SimpleGeofence(id, lat, lng, radius,
						expirationDuration, transitionType);
			}
			return null;

		}

		public void setGeofence(String id, SimpleGeofence geofence) {
			SharedPreferences.Editor prefs = mPrefs.edit();
			prefs.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE),
					(float) geofence.getmLatitude());
			prefs.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE),
					(float) geofence.getmLongitude());
			prefs.putFloat(getGeofenceFieldKey(id, KEY_RADIUS),
					geofence.getmRadius());
			prefs.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
					geofence.getmExpirationDuration());
			prefs.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
					geofence.getmTransitionType());
			prefs.commit();
		}
		public void clearGeofence(String id) {
			SharedPreferences.Editor prefs = mPrefs.edit();
			prefs.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
			prefs.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
			prefs.remove(getGeofenceFieldKey(id, KEY_RADIUS));
			prefs.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
			prefs.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
			prefs.commit();
		}

		private String getGeofenceFieldKey(String id, String fieldName) {
			return KEY_PREFIX + "_" + id + "_" + fieldName;
		}

	}

	@Override
	public void onAddGeofencesResult(int arg0, String[] arg1) {
		Log.d("onAddGeofencesResult", "result");
		if (LocationStatusCodes.SUCCESS == arg0) {
			Log.d("success", arg0 + "");
		} else {
		}
		mInProgress = false;
		mLocationClient.disconnect();

	}
}
