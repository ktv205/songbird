package com.cs9033.songbird.models;

import java.io.File;
import java.io.IOException;

import com.cs9033.songbird.controllers.MainActivity;
import com.cs9033.songbird.foreignMidiLibrary.MidiFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FilingSys {
	private static String APP_FILE_NAME = "Songbird";
	private static String RECORDINGS_FILE_NAME = "Recordings";
	private static String MIDI_FILE_NAME = "MIDI";
	private static String MIDI_EXTENSION = ".mid";
	private static String MP3_EXTENSION = ".mp3";

	public static File parentDir = new File(
			Environment.getExternalStorageDirectory() + "/" + APP_FILE_NAME);
	public static File recordingDir = new File(
			Environment.getExternalStorageDirectory() + "/" + APP_FILE_NAME
					+ "/" + RECORDINGS_FILE_NAME);
	public static File midiDir = new File(
			Environment.getExternalStorageDirectory() + "/" + APP_FILE_NAME
					+ "/" + MIDI_FILE_NAME);

	public static void createFile(String path, String extension, MidiFile midi) {
		confirmFileStruct();

	}

	public static void confirmFileStruct() {
		if (!parentDir.exists() || !parentDir.isDirectory())
			parentDir.mkdir();
		if (!recordingDir.exists() || !parentDir.isDirectory())
			recordingDir.mkdir();
		if (!midiDir.exists() || !midiDir.isDirectory())
			midiDir.mkdir();
	}

	public static void displayFiles(final Activity context,
			LinearLayout llayout, String path) {
		TextView textView;
		LinearLayout subLayout;
		File f = new File(path);
		File file[] = f.listFiles();
		if (file != null) {
			Log.d("Files", "Size: " + file.length);
			for (int i = 0; i < file.length; i++) {
				subLayout = new LinearLayout(context);
				subLayout.setOrientation(LinearLayout.HORIZONTAL);
				final Button button2 = new Button(context);
				button2.setText(file[i].getName());
				button2.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						intentyness((String) button2.getText(), context);
					}
				});
				final Button button = new Button(context);
				button.setText("Listen to");
				Log.d("Files", "FileName:" + file[i].getName());
				button.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						MediaPlayer player = new MediaPlayer();
						File output;
						output = new File(Environment
								.getExternalStorageDirectory().toString()
								+ "/Songbird/MIDI", (String) button2.getText());
						try {
							player.setDataSource(output.getAbsolutePath());
							player.prepare();
						} catch (Exception e) {
							Log.e(getClass().toString(), e.getMessage(), e);
						}
						player.start();
					}
				});

				subLayout.addView(button);
				subLayout.addView(button2);
				llayout.addView(subLayout);
			}
		} else {
			textView = new TextView(context);
			textView.setText("No Files Available for Loading...");
			llayout.addView(textView);
		}
	}

	public static void intentyness(String filename, Activity activity) {
		// Intent intent = new Intent(activity, MainActivity.class);
		// intent.putExtra("filename", filename);
		// activity.setResult(Activity.RESULT_OK, intent);
		// activity.finish();
	}
}
