package com.cs9033.songbird.models;

import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;

public class GeofenceModel implements Parcelable {

	private int id;
	private String song_name;
	private double latitude, longitude;
	private float radius;
	private int color;
	
	public static final Parcelable.Creator<GeofenceModel> CREATOR = new Parcelable.Creator<GeofenceModel>() {
		public GeofenceModel createFromParcel(Parcel p){
			return new GeofenceModel(p);
		}

		@Override
		public GeofenceModel[] newArray(int size) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	public GeofenceModel(Parcel p) {
		id = p.readInt();
		song_name = p.readString();
		latitude = p.readDouble();
		longitude = p.readDouble();
		radius = p.readFloat();
		color = p.readInt();
	}
	public GeofenceModel(int id,String name,double lat, double lng, float rad) {
		this.id = id;
		song_name = name;
		latitude = lat;
		longitude = lng;
		radius = rad;
		Random r = new Random();
		color = (255<<24)|(r.nextInt(256)<<16)|(r.nextInt(256)<<8)|(r.nextInt(256));
	}
	public int getId() {
		return id;
	}
	public String getSongName() {
		return song_name;
	}
	public double getLatitude(){
		return latitude;
	}
	public double getLongitude(){
		return longitude;
	}
	public float getRadius(){
		return radius;
	}
	@Override
	public int describeContents() {
		// Do not implement
		return 0;
	}
	public int getColor( ){
		return color;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(song_name);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeFloat(radius);
		dest.writeInt(color);
	}

}
