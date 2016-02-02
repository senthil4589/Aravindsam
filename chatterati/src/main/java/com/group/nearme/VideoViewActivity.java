package com.group.nearme;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.util.Utility;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoViewActivity extends Activity
{
	private VideoView videoView;
	private ImageView mBackImg;
	private TextView titleTxtView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_view);
		Utility.getTracker(this, "VIDEO FULL VIEW SCREEN");
		initViews();
		
		String videoUrl=getIntent().getStringExtra("video");
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		/*try
		{
			byte[] videoByte=getIntent().getByteArrayExtra("video");
			
			InputStream input =new ByteArrayInputStream(videoByte);
			OutputStream output = new FileOutputStream(getFilename());
			byte data[] = new byte[videoByte.length];
			int count;
			while ((count = input.read(data)) != -1) {
			    output.write(data, 0, count);
			}
		}
		catch(Exception e){
			
		}*/
		videoView.setVideoPath(videoUrl);
		videoView.setMediaController(new MediaController(this));
		videoView.requestFocus();
		videoView.start();
		
		  videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
		    {
		        @Override
		        public void onCompletion(MediaPlayer mp) 
		        {
		        	finish();
		        }
		    });
		
		
		
	}
	
	public static String getFilename() {
	    File file = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/Videos");
	    if (!file.exists()) {
	        file.mkdirs();
	    }
	    File file1 = new File(file.getAbsolutePath() + "/" + "SVideo" + ".mp4");
    	
	    if(file1.exists()){
			file1.delete();
		}
	    
	    String uriSting = (file.getAbsolutePath() + "/" + "SVideo" + ".mp4");
	    return uriSting;
	 
	}


	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		titleTxtView=(TextView) findViewById(R.id.title);
		videoView=(VideoView) findViewById(R.id.video_view);
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
