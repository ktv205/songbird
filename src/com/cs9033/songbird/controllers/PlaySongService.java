package com.cs9033.songbird.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.cs9033.songbird.R;
import com.cs9033.songbird.models.Song;
import com.cs9033.songbird.models.SongBirdPreferences;
import com.google.android.gms.internal.mp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmManagerClient.OnErrorListener;
import android.drm.DrmManagerClient.OnInfoListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PlaySongService extends Service implements OnCompletionListener,
		OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener,
		android.media.MediaPlayer.OnErrorListener,
		android.media.MediaPlayer.OnInfoListener {

	public static final String ACTION_PAUSE = "com.example.songbird2.ACTION_PAUSE";
	public static final String ACTION_PLAY = "com.example.songbird2.ACTION_PLAY";

	private static final String TAG = "PlaySongService";
	private HashMap<Integer, MediaPlayer> mMediaPlayers;
	private ArrayList<Song> songs;
	private ArrayList<File> files;
	static boolean alive = false;
	private final IBinder mBinder = new SongServiceBinder();
	private static final int MSG_REGISTER_CLIENT = 1;
	private static final int MSG_UNREGISTER_CLIENT = 2;
	private static final int MSG_FROM_SERVICE = 3;
	private static final int MSG_FROM_ACTIVITY = 4;
	private static final String link = "http://" + SongBirdPreferences.ipAdd+"/";
	// Notification id
	private static final int NOTIFICATION_ID = 1;

	@SuppressLint("UseSparseArrays")
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "In Service");
		mMediaPlayers = new HashMap<Integer, MediaPlayer>();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Service started...");
		//initNotification();
		alive = true;
		startSongs(intent);
		return START_NOT_STICKY;
	}

	private void startSongs(Intent intent){
		songs = intent.getExtras().getParcelableArrayList("songlist");
		Log.i(TAG,songs.size()+"");
		for (Song s : songs) {
			Log.i(TAG, s.getSongName());
			if (!mMediaPlayers.containsKey(s.getId())) {
				Log.v(TAG, "Adding Media Player");
				
				//mMediaPlayers.put(s.getId(), new MediaPlayer());
				try {
					Log.v(TAG,link+s.getSong_link());
					//mMediaPlayers.get(s.getId())
					MediaPlayer mp = new MediaPlayer();
					if(s.getSongName().contains(".mid")){
						BackgroundThread bt = new BackgroundThread(s.getId(), 
								link+s.getSong_link());
						bt.start();
					}
					else{
						mp.setDataSource(link+s.getSong_link());
						Log.d(TAG,s.getState()+"");
						if (!s.getState()){
							Log.i(TAG, s.getSong_link());
							//mMediaPlayers.get(s.getId()).setOnPreparedListener(this);
							//mMediaPlayers.get(s.getId()).prepareAsync();
							mp.setOnPreparedListener(this);
							mp.setOnCompletionListener(this);
							mp.prepareAsync();
							mMediaPlayers.put(s.getId(), mp);
						}
					}
				} catch (IllegalArgumentException | SecurityException
						| IllegalStateException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager) getSystemService(ns);
		long when = System.currentTimeMillis();
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Intent serviceIntent = new Intent(this, PlaySongService.class);
		serviceIntent.setAction(ACTION_PAUSE);
		PendingIntent actionIntent = PendingIntent.getService(
				getApplicationContext(), 0, serviceIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle("Song is Playing")
				.setSmallIcon(R.drawable.ic_launcher).setWhen(when)
				.setContentIntent(contentIntent)
				.addAction(R.drawable.pause, "Action Button", actionIntent)
				.build();
		// notification.flags = Notification.FLAG_ONGOING_EVENT;
		nm.notify(NOTIFICATION_ID, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		//startSongs(intent);
		return mBinder;
	}
	@Override
	public boolean onUnbind(Intent intent){
		return false;
	}

	@Override
	public void onDestroy() {
		if (mMediaPlayers != null) {
			for (MediaPlayer m : mMediaPlayers.values()) {
				if (m.isPlaying())
					m.stop();
				m.release();
			}
		}
		mMediaPlayers = null;
		alive = false;
		super.onDestroy();
	}

	private void playSong(MediaPlayer mp) {
		if (!mp.isPlaying()){
			Log.i("Play Song", "asdf");
			mp.start();
		}
	}

	private void stopSong(MediaPlayer mp) {
		if (mp.isPlaying())
			mp.stop();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			Toast.makeText(this, "Media error: Not a valid streaming source",
					Toast.LENGTH_LONG).show();
			break;
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Toast.makeText(this, "Media error: Server not responding",
					Toast.LENGTH_SHORT).show();
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Toast.makeText(this, "Media error: Unkown", Toast.LENGTH_SHORT)
					.show();
			break;
		default:
			Log.i("Error",what+","+extra);
		}
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i("PlaySong",mp.getTrackInfo()[0].toString());
		playSong(mp);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.v(TAG,"Called after completion");
		mp.stop();
	}

	public void filterSong(int song_id) {
		for (Song s : songs) {
			if (s.getId() == song_id) {
				s.setState(!s.getState());
				if (s.getState() && mMediaPlayers.get(song_id).isPlaying()){
					Log.v(TAG, "filtering "+ s.getSongName());
					mMediaPlayers.get(song_id).pause();
				}
				else if (mMediaPlayers.get(song_id).getCurrentPosition() <
						mMediaPlayers.get(song_id).getDuration()){
					try{
						Log.d(TAG, "started after pause");
						mMediaPlayers.get(song_id).start();
					}
					catch(IllegalStateException e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void pauseAllSongs() {
		for (MediaPlayer m : mMediaPlayers.values()) {
			if (m.isPlaying()) {
				m.pause();
			}
		}
	}

	public class SongServiceBinder extends Binder {
		public PlaySongService getService() {
			return PlaySongService.this;
		}
	}
	private class BackgroundThread extends Thread{
		private int song_id;
		private String link;
		public BackgroundThread(int sId, String sLink){
			song_id = sId;
			link = sLink;
		}
		@Override
		public void run() {
			File root = Environment.getExternalStorageDirectory();              
            File dir = new File (root.getAbsolutePath() + "/SongBird/Downloads/");
            if(dir.exists()==false) {
                    dir.mkdirs();
               }
            String fName = song_id+".mid";
            try{
            	URL url = new URL(link);
            	HttpURLConnection con = (HttpURLConnection)url.openConnection();
            	con.connect();
            	InputStream in = con.getInputStream();
				OutputStream out = new FileOutputStream(dir+fName);
				byte[] data = new byte[1024];
				int count;
				while((count = in.read(data))!= -1){
					
					out.write(data,0,count);
				}
				out.flush();
				out.close();
				in.close();
	            MediaPlayer mp = new MediaPlayer();
	            mp.setDataSource(dir+fName);
	            mp.prepare();
				mp.setOnCompletionListener(PlaySongService.this);
	            mMediaPlayers.put(song_id, mp);
	            playSong(mp);
            }catch(Exception e){
            	e.printStackTrace();
            }
		}
	}
}
