package com.cs9033.songbird.models;

public class SongBirdPreferences {
	public final static String ipAdd="54.174.223.185";
	public static int flagForName=0;
	public final static int REQUEST_CODE_LISTEN_AUTH=0;
	public final static int REQUEST_CODE_CREATE_AUTH=1;
	public final static int REQUEST_CODE_PROFILE_AUTH=2;
	public final static int REQUEST_CODE_UPLOAD_AUTH=3;
	public final static int RESPONSE_CODE_AUTH=4;
	public final static int REQUEST_CODE_UPLOAD_LIST=5;
	public final static int RESPONSE_CODE_LIST_UPLOAD=6;
	public final static int REQUEST_CODE_CREATE_VIEW=7;
	public final static int RESPONSE_CODE_VIEW_CREATE=8;
     public static abstract class PutExtrasCodes{
    	 public static final String KEY_GEOFENCES_MAIN_FILTER="send geofences to filter activity from main";
     }
	public static abstract class Auth{
		public static final String AUTHPREF="AUTH_SHAREDPREFERENCES";
		public static final String KEY_AUTH="AUTH?";
		public static final int  FACEBOOK_AUTH=1;
		public static final int GOOGLE_AUTH=0;
		public static final String KEY_NAME="ACCOUTHOLDER_NAME";
		public static final String KEY_EMAIL="ACCOUNTHOLDER_EMAIL";	
		public static final String KEY_PERSON_ID="ACCOUNTHOLDER_DATABASE_ID";
		public static final String KEY_PICTURE="PROFILE_PICTURE_LINK";
		public static final String KEY_FACEBOOKID="FACEBOOK_ID";
	}
	public static abstract class SongDetails{
		public static final String SONGPREF="SONG_PREFERENCES";
	   public static final String KEY_SONG_ID="SONG_DATABASE_ID";
	   public static final String KEY_SONG_EXTENTION="SONG_EXTENSION";
	   public static final String KEY_SONG_PATH="SONG_PATH";
	   public static final String KEY_CURRENT_SONG_ID="CURRENT_SONG_ID";
	}
	public static abstract class SongsUploaded{
		public static final String UPLOADPREF="name of the shared pref for uplaod songs";
		public static final String KEY_SONGS="no.of.songs";
		public static final String KEY_PERSON_ID="person id in db SongsUploaded";
	}
	public static abstract class SongsCreated{
		public static final String CREATEPREF="name of the shared pref for create songs";
		public static final String KEY_SONGS_CREATED="no.of.songs created";
		public static final String KEY_PERSON_ID="person id in db Songs Created";
	}
	public static abstract class SongsFound{
		public static final String FOUNDPREF="name of the shared pref for create songs";
		public static final String KEY_SONGS_FOUND="no.of.songs created";
		public static final String KEY_PERSON_ID="person id in db songs found";
	}
	public static abstract class RateSongs{
		public static final String RATEPREF="rating the songs";
	}

}
