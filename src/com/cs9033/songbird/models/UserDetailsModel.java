package com.cs9033.songbird.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDetailsModel implements Parcelable {
	private String name;
	private String email;
	private int authProvider;

	public UserDetailsModel(String name, String email, int authProvider) {
		this.name = name;
		this.email = email;
		this.authProvider = authProvider;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAuthProvider() {
		return authProvider;
	}

	public void setAuthProvider(int authProvider) {
		this.authProvider = authProvider;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public static final Parcelable.Creator<UserDetailsModel> CREATOR=new Creator<UserDetailsModel>() {
		
		@Override
		public UserDetailsModel[] newArray(int size) {
			// TODO Auto-generated method stub
			return new UserDetailsModel[size];
		}
		
		@Override
		public UserDetailsModel createFromParcel(Parcel source) {
			
			return new UserDetailsModel(source);
		}
	};
     public UserDetailsModel(Parcel source) {
		
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(email);
		dest.writeInt(authProvider);

	}

}
