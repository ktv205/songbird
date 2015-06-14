package com.cs9033.songbird.controllers;

import com.cs9033.songbird.R;
import com.cs9033.songbird.models.SongBirdPreferences;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent resultIntent = new Intent(context, MainActivity.class);
		
		PendingIntent resultPendingIntent =
		    PendingIntent.getActivity(
		    context,
		    0,
		    resultIntent,
		    PendingIntent.FLAG_UPDATE_CURRENT
		);
		Log.d("broadCast reciever","received");
		String content = null;
		if(intent.getExtras().getInt("Transition")==0){
		  content="you exited a geofence";
		}else if(intent.getExtras().getInt("Transition")==1){
			content="you entered a geofence";
			SharedPreferences pref=context.getSharedPreferences(SongBirdPreferences.SongDetails.SONGPREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor edit=pref.edit();
			edit.putInt(SongBirdPreferences.SongDetails.KEY_CURRENT_SONG_ID, Integer.valueOf(intent.getExtras().getString("id")));
			edit.commit();
		}
		Builder noti = new Notification.Builder(context)
        .setContentTitle(content)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentText("the geo fence is"+intent.getExtras().getString("id"))
        .setDefaults( Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS)
        .setContentIntent(resultPendingIntent)
        .setAutoCancel(true);
		
		NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1,noti.build());
	}
	

}
