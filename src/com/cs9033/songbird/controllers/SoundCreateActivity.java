package com.cs9033.songbird.controllers;

import java.io.File;

import com.cs9033.songbird.models.FilingSys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.cs9033.songbird.R;
import com.cs9033.songbird.controllers.Logout.OnLogoutSuccessful;
import com.cs9033.songbird.controllers.ProfileActivity;
import com.cs9033.songbird.controllers.ViewFilesActivity;
import com.cs9033.songbird.foreignMidiLibrary.MidiFile;
import com.cs9033.songbird.foreignMidiLibrary.MidiTrack;
import com.cs9033.songbird.foreignMidiLibrary.event.NoteOff;
import com.cs9033.songbird.foreignMidiLibrary.event.NoteOn;
import com.cs9033.songbird.foreignMidiLibrary.event.meta.Tempo;
import com.cs9033.songbird.models.DrawerListOptions;
import com.cs9033.songbird.models.SongBirdPreferences;

public class SoundCreateActivity extends FragmentActivity implements
		OnLogoutSuccessful {

	private static final int REQUEST_CODE = 100;
	private static final int REQUEST_CODE2 = 101;
	private final static String DEBUG = "SoundCreateActivity";
	private static final int AUTH_CODE = 1;

	String TAG = "SoundCreateActivity";
	// TextView editMidi;
	MidiTrack tempoTrack;
	MidiTrack noteTrack;
	int latestPointInTrack; // Keeps track of the latest point in the track so
							// ->|
							// we know where to put the next notes <- V
	boolean firstNote;
	LinearLayout llayout;
	String setFile;
	HorizontalScrollView hsv;
	private DrawerLayout createDrawer;
	private ListView listDrawer;
	private DrawerListAdapter createListDrawerAdapter;
	private ActionBarDrawerToggle toggler;
	private Menu menu;
	private SharedPreferences createPref;
	private SharedPreferences.Editor createEdit;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_create);
		createPref = getSharedPreferences(
				SongBirdPreferences.SongsCreated.CREATEPREF, MODE_PRIVATE);
		createEdit = createPref.edit();
		if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH
				|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
						MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
						-1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
			Log.d(DEBUG, "logged in");
		} else {
			startActivityForResult(new Intent(this,
					AuthenticationActivity.class),
					SongBirdPreferences.REQUEST_CODE_CREATE_AUTH);
			// finish();
		}

		/*
		 * get actionBar
		 */
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			Log.d(DEBUG, "action bar not null");
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
			actionBar.setTitle(DrawerListOptions.CREATE);
		} else {
			Log.d(DEBUG, "action bar is null");
		}
		/*
		 * get drawer
		 */
		createDrawer = (DrawerLayout) findViewById(R.id.drawer_layout_create);
		listDrawer = (ListView) findViewById(R.id.left_drawer_create);
		createListDrawerAdapter = new DrawerListAdapter(this, 1);
		listDrawer.setAdapter(createListDrawerAdapter);
		listDrawer.setBackgroundColor(Color.parseColor("#FFFFFF"));
		// if (createDrawer.isDrawerOpen(listDrawer)) {
		// View view = listDrawer.getChildAt(1);
		// if (view == null) {
		// Log.d(DEBUG, "view is null");
		// } else {
		// Log.d(DEBUG, "view is not null");
		// view.setBackgroundColor(Color.parseColor("#ADD8E6"));
		// }
		// }
		listDrawer.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(DEBUG, "OnItemClick listner for drawer list");
				createDrawer.closeDrawer(listDrawer);
				TextView textView = (TextView) view
						.findViewById(R.id.textDrawerList);
				String name = textView.getText().toString();
				Log.d(DEBUG, "name of clicked item->" + name);
				Intent intent;
				if (name.equals(DrawerListOptions.UPLOAD)) {
					intent = new Intent(SoundCreateActivity.this,
							UplaodActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.LISTEN)) {
					intent = new Intent(SoundCreateActivity.this,
							MainActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.AUTHENTICATION)) {
					intent = new Intent(SoundCreateActivity.this,
							AuthenticationActivity.class);
					startActivity(intent);

				} else if (name.equals(DrawerListOptions.CREATE)) {

				} else if (name.equals(DrawerListOptions.PROFILE)) {
					intent = new Intent(SoundCreateActivity.this,
							ProfileActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.LOGOUT)) {
					if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
						new Logout(SoundCreateActivity.this).logoutFromGoogle();
					} else if (getSharedPreferences(
							SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
						Log.d(DEBUG, "in facebook signout");
						new Logout(SoundCreateActivity.this)
								.logoutFromFacebook();
					}
					Log.d(DEBUG, "after logout");
				}
			}
		});
		/*
		 * toggler
		 */
		toggler = new ActionBarDrawerToggle(this, createDrawer, R.string.open,
				R.string.close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				Log.d(DEBUG, "drawer closed");
				getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
				getActionBar().setTitle(DrawerListOptions.CREATE);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				Log.d(DEBUG, "drawer open");
				// View view = listDrawer.getChildAt(1);
				// if (view == null) {
				// Log.d(DEBUG, "view is null");
				// } else {
				// Log.d(DEBUG, "view is not null");
				// view.setBackgroundColor(Color.parseColor("#ADD8E6"));
				// }
				getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
				getActionBar().setTitle(R.string.app_name);
			}

		};

		/*
		 * set drawer listner to toggler
		 */
		createDrawer.setDrawerListener(toggler);
		// editMidi = (TextView) findViewById(R.id.textView1);
		tempoTrack = new MidiTrack();
		noteTrack = new MidiTrack();
		firstNote = true;
		llayout = (LinearLayout) findViewById(R.id.linearlayout);
		llayout.setDividerPadding(1);
		llayout.setPadding(50, 0, 0, 0);
		llayout.setFocusable(false);
		latestPointInTrack = 0;
		setFile = null;

		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map

		Tempo t = new Tempo();
		t.setBpm(120);

		tempoTrack.insertEvent(t);
		findViewById(R.id.imageView2).setScaleX(5f);
		findViewById(R.id.imageView1).bringToFront();
		findViewById(R.id.imageView2).bringToFront();
		// 2b. Track 1 will have some notes in it
		// for(int i = 21; i < 128; i++) {
		//
		// int channel = 0, pitch = i, velocity = 100;
		// NoteOn on = new NoteOn(i*480, channel, pitch, velocity);
		// NoteOff off = new NoteOff(i*480 + 120, channel, pitch, 0);
		//
		// noteTrack.insertEvent(on);
		// noteTrack.insertEvent(off);
		// }

	}

	public void onCNote(View view) {
		Log.v(TAG, "IN onCNote");
		MediaPlayer mp = MediaPlayer.create(this, R.raw.c3);
		Log.v(TAG, "IN onCNote post media player instantiation");
		mp.start();
		Log.v(TAG, "IN onCNote post start");
		int channel = 0, pitch = 60, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 480, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);

		// editMidi.append("C4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-30f);
		imageView.setAdjustViewBounds(true);
		imageView.setFocusable(true);
		llayout.addView(imageView);
	}

	public void onCSharpNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.csharp3);
		mp.start();
		int channel = 0, pitch = 61, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("CS4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-30f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onDNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.d3);
		mp.start();
		int channel = 0, pitch = 62, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("D4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-36f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onDSharpNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.dsharp3);
		mp.start();
		int channel = 0, pitch = 63, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("DS4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-36f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onENote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.e3);
		mp.start();
		int channel = 0, pitch = 64, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("E4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-42f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onFNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.f3);
		mp.start();
		int channel = 0, pitch = 65, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("F4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-49f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onFSharpNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.fsharp3);
		mp.start();
		int channel = 0, pitch = 66, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("DS4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-49f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onGNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.g3);
		mp.start();
		int channel = 0, pitch = 67, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("G4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-54f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onGSharpNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.gsharp3);
		mp.start();
		int channel = 0, pitch = 68, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("GS4 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-54f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onANote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.a4);
		mp.start();
		int channel = 0, pitch = 69, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("A5 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-61f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onASharpNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.asharp4);
		mp.start();
		int channel = 0, pitch = 70, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("AS5 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-61f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onBNote(View view) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.b4);
		mp.start();
		int channel = 0, pitch = 71, velocity = 100;
		NoteOn on = new NoteOn(noteTrack.getLengthInTicks(), channel, pitch,
				velocity);
		NoteOff off = new NoteOff(noteTrack.getLengthInTicks() + 120, channel,
				pitch, 0);

		noteTrack.insertEvent(on);
		noteTrack.insertEvent(off);
		// editMidi.append("B5 ");

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.quarter_note);
		imageView.setScaleX(.15f);
		imageView.setScaleY(.15f);
		imageView.setX(2f);
		imageView.setY(-67f);
		imageView.setAdjustViewBounds(true);
		llayout.addView(imageView);
	}

	public void onSaveMIDI(View view) {

		Log.v(TAG, "inonSAVEMIDI");
		Intent intent = new Intent(this, ProfileActivity.class);
		Log.v(TAG, "Intent Instantiated");
		startActivityForResult(intent, REQUEST_CODE2);
	}

	public void onLoadMIDI(View view) {
		Intent intent = new Intent(this, ViewFilesActivity.class);
		startActivityForResult(intent, REQUEST_CODE);
	}

	public void onPlayMIDI(View view) {
		FilingSys.confirmFileStruct();
		createEdit
				.putInt(SongBirdPreferences.SongsCreated.KEY_SONGS_CREATED,
						createPref
								.getInt(SongBirdPreferences.SongsCreated.KEY_SONGS_CREATED,
										0) + 1);
		createEdit.commit();
		Log.v(TAG, "onPlayMIDI: Function Start");
		MidiTrack SavedTempoTrack = tempoTrack;
		MidiTrack SavedNoteTrack = noteTrack;
		MediaPlayer player = new MediaPlayer();
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(noteTrack);

		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

		// 4. Write the MIDI data to a file
		File output;
		if (setFile == null) {
			Log.v(TAG, "set file null");
			Long tsLong = System.currentTimeMillis() / 1000;
			String ts = tsLong.toString();
			output = new File(Environment.getExternalStorageDirectory()
					.toString() + "/Songbird/MIDI", ts + ".mid");
		} else {
			Log.v(TAG, "set file no null");
			output = new File(Environment.getExternalStorageDirectory()
					.toString() + "/Songbird/MIDI", setFile);
		}
		try {
			midi.writeToFile(output);
		} catch (IOException e) {
			Log.e(getClass().toString(), e.getMessage(), e);
		}
		try {
			player.setDataSource(output.getAbsolutePath());
			player.prepare();
		} catch (Exception e) {
			Log.e(getClass().toString(), e.getMessage(), e);
		}
		player.start();

		try {
			midi = new MidiFile(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(getClass().toString(), e.getMessage(), e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// tempoTrack = new MidiTrack();
		// noteTrack = new MidiTrack();
		noteTrack = midi.getTracks().get(1);
		tempoTrack = midi.getTracks().get(0);
		latestPointInTrack = 0;
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map

		Tempo t = new Tempo();
		t.setBpm(120);
		Log.v(TAG, "onPlayMIDI: Function End");
	}

	public void onDeleteNote(View view) {
		if (llayout.getChildCount() > 0) {
			llayout.removeViewAt(llayout.getChildCount() - 1);
			noteTrack.removeEvent(noteTrack.getEvents().last());
			noteTrack.removeEvent(noteTrack.getEvents().last());
		}
		Log.v(TAG, "onDeleteNote: Post Double Delete");
		// noteTrack.
	}

	// public void onChangeInstrument(View view){
	// Log.v(TAG, "onChangeInstrument: Function Begining");
	// noteTrack.insertEvent(new InstrumentName(10, 10, "violin"));
	// noteTrack.
	// Log.v(TAG, "onChangeInstrument: Function End");
	// }

	/*
	 * menu item selection
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(DEBUG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		MenuItem item = menu.findItem(R.id.name_account);
		if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
				|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
						MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
						-1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
			item.setTitle(getSharedPreferences(
					SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE).getString(
					SongBirdPreferences.Auth.KEY_NAME, "your name goes here"));
		}
		return true;

	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(DEBUG, "onOptionsItemSelected");
		int id = item.getItemId();

		if (id == android.R.id.home) {
			Log.d(DEBUG, "home button is clicked");
			if (createDrawer.isDrawerOpen(listDrawer)) {
				Log.d(DEBUG, "clicked when drawer is open");
				createDrawer.closeDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
			} else {
				Log.d(DEBUG, "clicked when drawer is closed");
				createDrawer.openDrawer(listDrawer);
			}

		} else if (id == R.id.name_account) {
			startActivity(new Intent(this, ProfileActivity.class));
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * changing the default function of back pressed
	 */
	@Override
	public void onBackPressed() {
		createDrawer.openDrawer(listDrawer);
	}

	/*
	 * overriding the onActivityResult
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			setFile = new String(data.getStringExtra("filename"));
		} else if (requestCode == SongBirdPreferences.REQUEST_CODE_CREATE_AUTH) {
			if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
					MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
					|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
				Log.d(DEBUG, "logged in");
				createListDrawerAdapter = new DrawerListAdapter(this, 1);
				listDrawer.setAdapter(createListDrawerAdapter);
				MenuItem item = menu.findItem(R.id.name_account);
				item.setTitle(getSharedPreferences(
						SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE)
						.getString(SongBirdPreferences.Auth.KEY_NAME,
								"SIGNIN/SIGNUP"));
			} else {
				startActivity(new Intent(this, MainActivity.class));
				finish();
			}
		}
	}

	@Override
	public void onCleardFields(boolean cleared) {
		if (cleared) {
			startActivity(new Intent(SoundCreateActivity.this,
					MainActivity.class));
			finish();
		}

	}
}
