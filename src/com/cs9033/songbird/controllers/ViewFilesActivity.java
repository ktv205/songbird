package com.cs9033.songbird.controllers;


import com.cs9033.songbird.R;
import com.cs9033.songbird.models.FilingSys;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ViewFilesActivity extends Activity{
	
	String TAG = "ViewFilesActivity";
	TextView textView;
	LinearLayout viewFilesLayout;
	LinearLayout subLayout;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,  "In onCreate");
		setContentView(R.layout.activity_view_files);
		viewFilesLayout = (LinearLayout)findViewById(R.id.viewFilesLinear);
		FilingSys.confirmFileStruct();
		String path = Environment.getExternalStorageDirectory().toString()+"/Songbird/MIDI/";
		Log.d("Files", "Path: " + path);
		FilingSys.displayFiles(this, viewFilesLayout, path);	
	}
}
