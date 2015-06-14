package com.cs9033.songbird.controllers;

import com.cs9033.songbird.R;

import android.app.Activity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

public class RecorderActivity extends Activity {
	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = null;

	private RecordButton mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private PlayButton mPlayButton = null;
	private MediaPlayer mPlayer = null;
	private final static String TAG = "RecorderActivity";

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
		Log.i(TAG, "started recording");
	}

	private void stopRecording() {
		try{
			mRecorder.stop();
			mRecorder.release();
			}
		catch(Exception e){
			Log.d(TAG, "Touch event too fast");
		}
		Log.i(TAG, "stopped recording");
		mRecorder = null;
	}

	class RecordButton extends ImageButton {
		boolean mStartRecording = true;
        
		OnFocusChangeListener clicker = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					Log.d(TAG,"RecordButton hasFocus");
					setImageResource(R.drawable.record);
					onRecord(hasFocus);
				} else {
					Log.d(TAG,"RecordButton noFocus");
					setImageResource(R.drawable.recording);
					onRecord(hasFocus);
				}

			}
		};
		OnTouchListener listner = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
					Log.d("TouchTest", "Touch down");
					setImageResource(R.drawable.recording);
					onRecord(true);
				} else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
					Log.d("TouchTest", "Touch up");
					setImageResource(R.drawable.record_default);
					onRecord(false);
				}
				return true;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);
			setImageResource(R.drawable.record_default);
			setOnTouchListener(listner);
		}
	}

	class PlayButton extends ImageButton {
		boolean mStartPlaying = true;

		OnClickListener clicker = new OnClickListener() {

			public void onClick(View v) {
				Log.d(TAG, "in onclick");
				onPlay(mStartPlaying);
				if (mStartPlaying) {
					setImageResource(R.drawable.stop_new);
				} else {
					setImageResource(R.drawable.play);
				}
				mStartPlaying = !mStartPlaying;
			}
		};
		

		public PlayButton(Context ctx) {
			super(ctx);
			setImageResource(R.drawable.play);
			setOnClickListener(clicker);
		}
	}

	public RecorderActivity() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/SongBird/Recordings/";
		File dir = new File(mFileName);
		if (!(dir.exists() && dir.isDirectory())) {
			dir.mkdirs();
		}
		String name = "rec_" + System.currentTimeMillis();
		mFileName += name;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d(TAG, "OnCreate");
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		mRecordButton = new RecordButton(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0);
		lp.gravity = Gravity.CENTER;
		ll.addView(mRecordButton, lp);
		mPlayButton = new PlayButton(this);
		lp.gravity = Gravity.CENTER_HORIZONTAL;
		ll.addView(mPlayButton, lp);
		setContentView(ll);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
