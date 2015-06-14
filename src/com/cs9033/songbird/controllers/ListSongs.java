package com.cs9033.songbird.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;



import com.cs9033.songbird.R;
import com.cs9033.songbird.models.SongBirdPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ListSongs extends Activity {
	private static final String TAG = "ListSongs";
	private String mFileName;
	
	//private SongList v = new SongList(this);
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		String path = mFileName + "/SongBird/Recordings/";
		ArrayList<String> songs = getSongs(0);
		setContentView(R.layout.song_list_view);
		ListView sl = (ListView)findViewById(R.id.recfiles);
		SongAdapter sMidi = new SongAdapter(this,songs.toArray(),path);
		sl.setAdapter(sMidi);
		
		
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		path = mFileName + "/SongBird/MIDI/";
		ListView s2 = (ListView)findViewById(R.id.midifiles);
		songs = getSongs(1);
		SongAdapter sRec = new SongAdapter(this,songs.toArray(),path);
		s2.setAdapter(sRec);
	}

	//the entire View
/*	class SongList extends ListView{

		public SongList(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		
	}*/
	
	//Play button inside the view
	class SongAdapter extends ArrayAdapter<String>{
		private String[] mFiles;
		private Context context;
		private String path;
		private MediaPlayer mp;
		public SongAdapter(Context context, Object[] files, String path) {
			super(context,R.layout.song_view);
			this.context = context;
			mFiles = Arrays.copyOf(files, files.length, String[].class);
			this.path = path;
			if(mp == null)
				mp = new MediaPlayer();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View view = null;
			LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(convertView == null){
				view = inflater.inflate(R.layout.song_view, parent, false);
				TextView textView = (TextView)view.findViewById(R.id.song_name);
				final String data = mFiles[position];
				textView.setText(data);
				Button play = (Button) view.findViewById(R.id.play_song);
				play.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String flag = (String) v.getTag();
						try {
							
							if(flag.equals("Play"))
							{
								Log.d("Play",path+data);
								if(mp == null)
									mp = new MediaPlayer();
								mp.reset();
								mp.setDataSource(path+data);
								mp.prepare();
								mp.start();
								v.setTag("Pause");
							}
							else{
								mp.stop();
								mp.release();
								mp = null;
								v.setTag("Play");
							}
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				});
				Button select = (Button) view.findViewById(R.id.select_song);
				select.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						i.putExtra("SONGPATH", path+data);
						i.putExtra("SONGTITLE",data);
						setResult(SongBirdPreferences.RESPONSE_CODE_LIST_UPLOAD,i);
						finish();
					}
				});
			}else{
				view = convertView;
			}
			return view;
		}
		
		@Override
		public int getCount() {
			return (mFiles != null) ? mFiles.length : 0;
		}

		@Override
		public String getItem(int idx) {
			return (mFiles != null) ? mFiles[idx] : null;
		}

		@Override
		public long getItemId(int position) {
			return  position;
		}

		@Override
		public boolean hasStableIds(){
			return true;
		}

		@Override
		public int getItemViewType(int pos){
			return IGNORE_ITEM_VIEW_TYPE;
		}

		@Override
		public int getViewTypeCount(){
			return 1;
		}
	}
	private ArrayList<String> getSongs(int flag){
		ArrayList<String> songList = new ArrayList<String>();
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/SongBird/";
        File dir = new File(mFileName);
        if(!(dir.exists() && dir.isDirectory())){
        	return songList;
        }
        if(flag == 0){
	        mFileName += "Recordings/";
	        dir = new File(mFileName);
	        if(dir.exists() && dir.isDirectory()){
	        	String[] files = dir.list();
	        	for(int i = 0; i< files.length;i++){
	        		Log.d(TAG,files[i]);
	        		songList.add(files[i]);
	        	}
	        }
        }
        else
        {
	        mFileName = mFileName + "MIDI/";
	        dir = new File(mFileName);
	        if(dir.exists() && dir.isDirectory()){
	        	String[] files = dir.list();
	        	for(int i = 0; i< files.length;i++){
	        		Log.d(TAG,files[i]);
	        		songList.add(files[i]);
	        	}
	        }
        }
        return songList;
	}
	
}
