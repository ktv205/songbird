package com.cs9033.songbird.controllers;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class ProfilePicService extends IntentService {
	private final static String DEBUG = "ProfilePicService";

	public ProfilePicService() {
		super("name");
		Log.d(DEBUG, "in the profilepicservice constructor");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String uri = intent.getExtras().getString("URI");
		URL url = null;
		try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpsURLConnection con = null;
		try {
			con = (HttpsURLConnection) url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// File file=new File(this.getFilesDir(), "profilepic");
		InputStream is = null;
		try {
			is = con.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isExternalStorageWritable()) {
			Log.d(DEBUG, "isExternalStorageWritable is writable");
			// File file=new
			// File(Environment.getExternalStorageDirectory()+"/SongBird",
			// "ProfilePic");
			File fileName=null;
			if(new File(Environment.getExternalStorageDirectory()+"/SongBird").exists()){
				Log.d(DEBUG,"Directory SongBird  exist");
				  fileName = new File(Environment.getExternalStorageDirectory()
						+ "/SongBird", "profile_pic.jpg");
			}else{
				Log.d(DEBUG,"Directory SongBird does not exist");
				boolean created=new File(Environment.getExternalStorageDirectory(), "SongBird").mkdirs();
				if(created){
					Log.d(DEBUG,"directory created");
					fileName = new File(Environment.getExternalStorageDirectory()
							+ "/SongBird", "profile_pic.jpg");
				}else{
					Log.d(DEBUG,"directory not created");
				}
				
			}
			

			InputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(con.getInputStream(),
						10240);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte buffer[] = new byte[1024];
			int dataSize;
			int loadedSize = 0;
			try {
				while ((dataSize = inputStream.read(buffer)) != -1) {
					loadedSize += dataSize;
					outputStream.write(buffer, 0, dataSize);

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}
}
