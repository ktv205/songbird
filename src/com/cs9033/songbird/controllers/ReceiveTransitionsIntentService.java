package com.cs9033.songbird.controllers;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ReceiveTransitionsIntentService extends IntentService {

	public ReceiveTransitionsIntentService() {
		super("name");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("here in on handle intent", "sdsd");
		if (LocationClient.hasError(intent)) {
			// Get the error code with a static method
			int errorCode = LocationClient.getErrorCode(intent);
			// Log the error
			Log.e("ReceiveTransitionsIntentService",
					"Location Services error: " + Integer.toString(errorCode));
			/*
			 * You can also send the error code to an Activity or Fragment with
			 * a broadcast Intent
			 */
			/*
			 * If there's no error, get the transition type and the IDs of the
			 * geofence or geofences that triggered the transition
			 */
		} else {
			// Get the type of transition (entry or exit)
			int transitionType = LocationClient.getGeofenceTransition(intent);
			// Test that a valid transition was reported
			if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)) {
				List<Geofence> triggerList = LocationClient
						.getTriggeringGeofences(intent);

				String[] triggerIds = new String[triggerList.size()];

				for (int i = 0; i < triggerIds.length; i++) {
					triggerIds[i] = triggerList.get(i).getRequestId();
					Log.d("trigger ids", triggerIds[i] + "");
					Log.d("transition type", transitionType + "");
					Intent intent1 = new Intent();
					intent1.setAction("something");
					intent1.putExtra("id", triggerIds[i]);
					intent1.putExtra("Transition", 1);
					getApplicationContext().sendBroadcast(intent1);

				}
			} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
				List<Geofence> triggerList = LocationClient
						.getTriggeringGeofences(intent);

				String[] triggerIds = new String[triggerList.size()];

				for (int i = 0; i < triggerIds.length; i++) {
					triggerIds[i] = triggerList.get(i).getRequestId();
					Log.d("trigger ids", triggerIds[i] + "");
					Log.d("transition type", transitionType + "");
					Intent intent1 = new Intent();
					intent1.setAction("something");
					intent1.putExtra("id", triggerIds[i]);
					intent1.putExtra("Transition", 0);
					getApplicationContext().sendBroadcast(intent1);
				}

			}

		}
	}
}
