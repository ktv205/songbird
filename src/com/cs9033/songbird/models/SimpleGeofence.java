package com.cs9033.songbird.models;

import com.google.android.gms.location.Geofence;

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
		//Log.d(DEBUG, "Geofence->toGeofence");
		return new Geofence.Builder()
				.setRequestId(getmId())
				.setTransitionTypes(mTransitionType)
				.setCircularRegion(getmLatitude(), getmLongitude(),
						getmRadius())
				.setExpirationDuration(mExpirationDuration).build();
	}
}

