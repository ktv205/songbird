package com.cs9033.songbird.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.cs9033.songbird.R;
import com.cs9033.songbird.controllers.Logout.OnLogoutSuccessful;
import com.cs9033.songbird.models.DrawerListOptions;
import com.cs9033.songbird.models.RequestParams;
import com.cs9033.songbird.models.SongBirdPreferences;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ProfileActivity extends FragmentActivity implements
		OnLogoutSuccessful {
	String TAG = "ProfileActivity";
	private Menu menu;
	private TextView nameText;
	private String PREF_SONGS_FOUND = "8881";
	private String PREF_SONGS_CREATED = "8882";
	private String PREF_SONGS_SHARED = "8883";
	private String PREF_BADGE_1 = "8884";
	private String PREF_BADGE_2 = "8885";
	private String PREF_BADGE_3 = "8886";
	private String PREF_BADGE_4 = "8887";
	private String PREF_BADGE_1b = "8888";
	private String PREF_BADGE_2b = "8889";
	private String PREF_BADGE_3b = "8890";
	private String PREF_BADGE_4b = "8891";
	private DrawerLayout profileDrawer;
	private ListView listDrawer;
	private final static String DEBUG = "ProfileActivity";
	private static final int AUTH_CODE = 1;
	private DrawerListAdapter profileListDrawerAdapter;
	private ActionBarDrawerToggle toggler;
	private ImageView profilePic;
	private int imageWidth;
	private int imageHeight;
	private int focusFlag = 0;
	private ActionBar actionBar;
	private SharedPreferences uploadPref, createPref, foundPref;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uploadPref = getSharedPreferences(
				SongBirdPreferences.SongsUploaded.UPLOADPREF, MODE_PRIVATE);
		createPref = getSharedPreferences(
				SongBirdPreferences.SongsCreated.CREATEPREF, MODE_PRIVATE);
		foundPref = getSharedPreferences(
				SongBirdPreferences.SongsFound.FOUNDPREF, MODE_PRIVATE);
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putBoolean(PREF_BADGE_1, true).commit();

		Log.v(TAG, "IN ONCREATE");
		setContentView(R.layout.activity_profile);
		profilePic = (ImageView) findViewById(R.id.imageView1);

		if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH
				|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
						MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
						-1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
			Log.d(DEBUG, "logged in");

		} else {
			startActivityForResult(new Intent(this,
					AuthenticationActivity.class),
					SongBirdPreferences.REQUEST_CODE_PROFILE_AUTH);
		}
		Log.v(TAG, "Layout set");
		/*
		 * get actionBar
		 */
		initializeActionBar();

		/*
		 * get drawer
		 */
		profileDrawer = (DrawerLayout) findViewById(R.id.drawer_layout_profile);
		listDrawer = (ListView) findViewById(R.id.left_drawer_profile);
		profileListDrawerAdapter = new DrawerListAdapter(this, 3);
		listDrawer.setAdapter(profileListDrawerAdapter);
		listDrawer.setBackgroundColor(Color.parseColor("#FFFFFF"));
		listDrawer.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(DEBUG, "OnItemClick listner for drawer list");
				profileDrawer.closeDrawer(listDrawer);
				TextView textView = (TextView) view
						.findViewById(R.id.textDrawerList);
				String name = textView.getText().toString();
				Log.d(DEBUG, "name of clicked item->" + name);
				Intent intent;
				if (name.equals(DrawerListOptions.UPLOAD)) {
					intent = new Intent(ProfileActivity.this,
							UplaodActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.LISTEN)) {
					intent = new Intent(ProfileActivity.this,
							MainActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.AUTHENTICATION)) {
					intent = new Intent(ProfileActivity.this,
							AuthenticationActivity.class);
					startActivity(intent);
				} else if (name.equals(DrawerListOptions.CREATE)) {
					intent = new Intent(ProfileActivity.this,
							SoundCreateActivity.class);
					startActivity(intent);
					finish();
				} else if (name.equals(DrawerListOptions.LOGOUT)) {
					if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
						new Logout(ProfileActivity.this).logoutFromGoogle();
					} else if (getSharedPreferences(
							SongBirdPreferences.Auth.AUTHPREF,
							Context.MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
						Log.d(DEBUG, "in facebook signout");
						new Logout(ProfileActivity.this).logoutFromFacebook();
					}

				}
			}
		});
		/*
		 * toggler
		 */
		toggler = new ActionBarDrawerToggle(this, profileDrawer, R.string.open,
				R.string.close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				Log.d(DEBUG, "drawer closed");
				getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
				getActionBar().setTitle(DrawerListOptions.PROFILE);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				Log.d(DEBUG, "drawer open");
				// View view = listDrawer.getChildAt(3);
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
		profileDrawer.setDrawerListener(toggler);

		int songsFound = PreferenceManager.getDefaultSharedPreferences(this)
				.getInt(PREF_SONGS_FOUND, 0);
		int songsCreated = PreferenceManager.getDefaultSharedPreferences(this)
				.getInt(PREF_SONGS_CREATED, 0);
		int songsShared = PreferenceManager.getDefaultSharedPreferences(this)
				.getInt(PREF_SONGS_SHARED, 0);
		TextView text1 = (TextView) findViewById(R.id.textView4);
		text1.append(Integer.toString(foundPref.getInt(
				SongBirdPreferences.SongsFound.KEY_SONGS_FOUND, 0)));
		TextView text2 = (TextView) findViewById(R.id.textView5);
		text2.append(Integer.toString(createPref.getInt(
				SongBirdPreferences.SongsCreated.KEY_SONGS_CREATED, 0)));
		TextView text3 = (TextView) findViewById(R.id.textView6);
		text3.append(Integer.toString(uploadPref.getInt(
				SongBirdPreferences.SongsUploaded.KEY_SONGS, 0)));
		RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.relativelyViewedView2);
		rlayout.canScrollVertically(1);

		ImageView tv = new ImageView(this);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PREF_BADGE_1, false)) {
			tv.setImageResource(R.drawable.badge_1b);
		} else
			tv.setImageResource(R.drawable.badge_1);
		tv.setX(0f);
		tv.setY(150f);
		rlayout.addView(tv);
		tv = new ImageView(this);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PREF_BADGE_2, false)) {
			tv.setImageResource(R.drawable.badge_2b);
		} else
			tv.setImageResource(R.drawable.badge_2);
		tv.setX(380f);
		tv.setY(150f);
		rlayout.addView(tv);
		tv = new ImageView(this);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PREF_BADGE_3, false)) {
			tv.setImageResource(R.drawable.badges_3b);
		} else
			tv.setImageResource(R.drawable.badges_3);
		tv.setX(0f);
		tv.setY(550f);
		rlayout.addView(tv);
		tv = new ImageView(this);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PREF_BADGE_4, false)) {
			tv.setImageResource(R.drawable.badge_4b);
		} else
			tv.setImageResource(R.drawable.badge_4);
		tv.setX(380f);
		tv.setY(550f);
		rlayout.addView(tv);

	}

	/*
	 * initialize actionbar
	 */
	@SuppressLint("NewApi")
	private void initializeActionBar() {
		actionBar = getActionBar();
		if (actionBar == null) {
			Log.d(DEBUG, "action bar null");
		} else {
			Log.d(DEBUG, "action bar not null");
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
			actionBar.setTitle("listen");
		}

	}

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
			if (profileDrawer.isDrawerOpen(listDrawer)) {
				Log.d(DEBUG, "clicked when drawer is open");
				profileDrawer.closeDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.drawer_shadow);
			} else {
				Log.d(DEBUG, "clicked when drawer is closed");
				profileDrawer.openDrawer(listDrawer);
				// getActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
			}

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		nameText = (TextView) findViewById(R.id.textView1);

		nameText.setText(getSharedPreferences(
				SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE).getString(
				SongBirdPreferences.Auth.KEY_NAME, "your name goes here"));

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		LinearLayout linear = (LinearLayout) findViewById(R.id.linear1);
		int linearWidth = linear.getWidth();
		Log.d(DEBUG,
				"in onwindowFocusChanged width of linear layout width is->"
						+ linearWidth);
		int textHeight = nameText.getHeight();
		Log.d(DEBUG, "in onwindowFocusChanged height of textview height is->"
				+ textHeight);
		int textWidth = nameText.getWidth();
		Log.d(DEBUG, "in onWindowFocusChanged width of textview width is->"
				+ textWidth);
		imageWidth = linearWidth - textWidth;
		Log.d(DEBUG, "imageWidth->" + imageWidth);
		imageHeight = textHeight;
		Log.d(DEBUG, "imageHeight->" + imageHeight);
		int totalWidth = imageWidth + textWidth;
		Log.d(DEBUG, "totalWidth->" + totalWidth);
		RequestParams requestParams = new RequestParams();
		requestParams.setURI(getSharedPreferences(
				SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE).getString(
				SongBirdPreferences.Auth.KEY_PICTURE, ""));
		if (focusFlag == 0) {
			if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
					MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
				requestParams
						.setURI("https://graph.facebook.com/"
								+ getSharedPreferences(
										SongBirdPreferences.Auth.AUTHPREF,
										MODE_PRIVATE)
										.getString(
												SongBirdPreferences.Auth.KEY_FACEBOOKID,
												"") + "/picture?type=small");
			} else {
				requestParams.setURI(getSharedPreferences(
						SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE)
						.getString(SongBirdPreferences.Auth.KEY_PICTURE, ""));
			}
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/SongBird/profile_pic.jpg");
			if (file.exists() && file.isFile()) {
				Bitmap bitmap = BitmapFactory
						.decodeFile(file.getAbsolutePath());
				readImageDimensions(bitmap);
			} else {
				new GetImageAsyncTask().execute(requestParams);
				focusFlag = 1;
			}
		}
	}

	public void readImageDimensions(Bitmap bitmap) {
		profilePic.setImageBitmap(getResizedBitmap(bitmap, imageHeight,
				imageWidth));
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

	@Override
	public void onBackPressed() {
		profileDrawer.openDrawer(listDrawer);
	}

	@Override
	protected void onActivityResult(int REQUEST_CODE, int RESPONSE_CODE,
			Intent data) {
		if (REQUEST_CODE == SongBirdPreferences.REQUEST_CODE_PROFILE_AUTH) {
			if (getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
					MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH
					|| getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
							MODE_PRIVATE).getInt(
							SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.FACEBOOK_AUTH) {
				Log.d(DEBUG, "logged in");
				TextView text = (TextView) findViewById(R.id.textView1);
				text.setText(getSharedPreferences(
						SongBirdPreferences.Auth.AUTHPREF, MODE_PRIVATE)
						.getString(SongBirdPreferences.Auth.KEY_NAME,
								"your name goes here"));
				profileListDrawerAdapter = new DrawerListAdapter(this, 3);
				listDrawer.setAdapter(profileListDrawerAdapter);
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

	public class GetImageAsyncTask extends
			AsyncTask<RequestParams, Void, Bitmap> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ProfileActivity.this);
			dialog.setTitle("loading");
			dialog.show();
		}

		@Override
		protected Bitmap doInBackground(RequestParams... params) {
			Log.d(DEBUG, "url->" + params[0].getURI());
			URL url = null;
			try {
				url = new URL(params[0].getURI());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HttpsURLConnection con = null;
			try {
				con = (HttpsURLConnection) url.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Bitmap bitmap;
			try {
				Log.d(DEBUG, "in try");
				bitmap = BitmapFactory.decodeStream(con.getInputStream());

				return bitmap;
			} catch (IOException e) {
				Log.d(DEBUG, "in catch");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			dialog.dismiss();
			readImageDimensions(result);

		}
	}

	@Override
	public void onCleardFields(boolean cleared) {
		Log.d(TAG, "onClearedFields");
		startActivity(new Intent(ProfileActivity.this, MainActivity.class));
		finish();

	}

}
