package com.cs9033.songbird.controllers;

import com.cs9033.songbird.R;
import com.cs9033.songbird.models.DrawerListOptions;
import com.cs9033.songbird.models.SongBirdPreferences;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnWindowFocusChangeListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerListAdapter extends BaseAdapter {
	private Context context;
	public final String create = "create";
	private final static String DEBUG = "DrawerListAdapter";
	private String[] options;
	private int pos;

	public DrawerListAdapter(Context context, int pos) {
		this.context = context;
		this.pos = pos;
		/*
		 * changing the sign in to logout
		 */
		if (context.getSharedPreferences(SongBirdPreferences.Auth.AUTHPREF,
				Context.MODE_PRIVATE).getInt(SongBirdPreferences.Auth.KEY_AUTH,
				-1) == SongBirdPreferences.Auth.FACEBOOK_AUTH
				|| context
						.getSharedPreferences(
								SongBirdPreferences.Auth.AUTHPREF,
								Context.MODE_PRIVATE).getInt(
								SongBirdPreferences.Auth.KEY_AUTH, -1) == SongBirdPreferences.Auth.GOOGLE_AUTH) {
			Log.d(DEBUG,
					"already logged in so the options menu item should change to logout");
			options = DrawerListOptions.getOptionsAuth();
		} else {
			Log.d(DEBUG, "not logged in so name should be signin/signup");
			options = DrawerListOptions.getOptionsNoAuth();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return options.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return options[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public class MyHolder {
		TextView testTextView;

		public MyHolder(View view) {
			testTextView = (TextView) view
					.findViewById(R.id.textDrawerList);
		}
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		MyHolder myHolder;
		// Log.i("getView","here");
		if (view == null) {
			// Log.i("getView","here");
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.drawer_list_contents, parent,
					false);
			myHolder = new MyHolder(view);
			view.setTag(myHolder);
		} else {
			myHolder = (MyHolder) view.getTag();
		}
		if (pos == position) {
			view.setBackgroundColor(Color.parseColor("#ADD8E6"));
		}
		myHolder.testTextView.setText(options[position]);
		
		Bitmap bitmap=null;
		if(position==0){
			bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
		}else if(position==1){
			bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.create);
		}else if(position==2){
			bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.upload);
		}else if(position==3){
			bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.profile);
		}else if(position==4){
			bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.signup);
		}
		Bitmap resized=getResizedBitmap(bitmap, 37, 37);
		Drawable d=new BitmapDrawable(context.getResources(),resized);
myHolder.testTextView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
		if(myHolder.testTextView!=null){
			Log.d(DEBUG,"height of textview->"+myHolder.testTextView.getHeight()+"");
		}

		return view;
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

}
