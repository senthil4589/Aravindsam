package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.util.Constants;
import com.group.nearme.util.ResizableImageView;
import com.group.nearme.util.TouchImageView;
import com.group.nearme.util.Utility;
import com.parse.GetDataCallback;

public class ImageFullViewActivity extends Activity{
	ImageView close,share;
	TouchImageView postImage;
	byte[] bitmapdata = null;
	String mGroupName="";
	ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_full_view);
		Utility.getTracker(this, "IMAGE FULL VIEW SCREEN");
		initViews();
		
		mGroupName=getIntent().getStringExtra(Constants.GROUP_NAME);
		String imagePath=getIntent().getStringExtra(Constants.POST_IMAGE);
		
		postImage.setParseFile(Utility.getParseFile());
		postImage.loadInBackground(new GetDataCallback() {
			@Override
			public void done(byte[] arg0, com.parse.ParseException arg1) {
				try {
					bitmapdata=Utility.getParseFile().getData();
				} catch (com.parse.ParseException e) {
				}
				progressBar.setVisibility(View.GONE);
			}
		});
		//Picasso.with(this).load(imagePath).into(postImage);
	
		/*try {
			bitmapdata=getIntent().getByteArrayExtra("byte");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		
*/	}

	private void initViews() {
		close=(ImageView) findViewById(R.id.close);
		share=(ImageView) findViewById(R.id.share);
		postImage=(TouchImageView) findViewById(R.id.post_image);
		progressBar=(ProgressBar) findViewById(R.id.progressBar1);
		
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				shareImage();
			}
		});
	}
	
	@SuppressLint("SdCardPath") private void shareImage()
	{
		//bitmapdata=Utility.getBitmapdata();
		Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata , 0, bitmapdata.length);
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	    File f = new File(Environment.getExternalStorageDirectory() + File.separator + "chatterati_image.jpg");
	    try {
	        f.createNewFile();
	        FileOutputStream fo = new FileOutputStream(f);
	        fo.write(bytes.toByteArray());
	    } catch (IOException e) {                       
	            e.printStackTrace();
	    }
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("image/*");
		sharingIntent.putExtra("path", f.getAbsolutePath());
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Chatterati "+mGroupName+" group");
		sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/chatterati_image.jpg"));
		startActivity(Intent.createChooser(sharingIntent, "Chatterati Sharing..."));
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	 
	 @Override
		protected void onStart() {
			super.onStart();
			GoogleAnalytics.getInstance(this).reportActivityStart(this);
		}

		@Override
		protected void onStop() {
			GoogleAnalytics.getInstance(this).reportActivityStop(this);
			super.onStop();
		}
		
}
