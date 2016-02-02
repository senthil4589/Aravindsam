package com.group.nearme;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.ResizableImageView;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class ShareLinkActivity extends Activity{
	EditText linkCaption;
	TextView cancelTxtView,postTxtView,linkDes,linkUrl;
	ResizableImageView linkImageView;
	ProgressBar progressBar;
	private int imgWidth,imgHeight;
	byte[] imgByte;
	private ParseFile mPostImgFile,mPostImageThumbnail;
	String linkTitle="",linkImage="",linkImagePath="",link="", mGroupId="",siteName="";
	ArrayList<String> list=new ArrayList<String>();
	GPSTracker gpsTracker;
	boolean isShare;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.share_link);
		
		Utility.getTracker(this, "LINK PREVIEW WITH CAPTION SCREEN");
		initViews();
		
		//( new ParseURL() ).execute(new String[]{link});
		
		cancelTxtView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		postTxtView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				insertLinkValues();
			}
		});
	}

	private void initViews() {
		linkCaption=(EditText) findViewById(R.id.image_caption);
		cancelTxtView=(TextView) findViewById(R.id.cancel);
		postTxtView=(TextView) findViewById(R.id.post);
		linkDes=(TextView) findViewById(R.id.link_des);
		linkUrl=(TextView) findViewById(R.id.link_url);
		linkImageView=(ResizableImageView) findViewById(R.id.post_image);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		link=getIntent().getStringExtra(Constants.LINK_URL);
		siteName=getIntent().getStringExtra(Constants.SITE_NAME);
		linkTitle=getIntent().getStringExtra("link_title");
		mGroupId=getIntent().getStringExtra(Constants.GROUP_ID);
		imgWidth=getIntent().getIntExtra(Constants.IMAGE_WIDTH, 300);
		imgHeight=getIntent().getIntExtra(Constants.IMAGE_HEIGHT, 300);
		isShare=getIntent().getBooleanExtra("isShare", false);
		
		gpsTracker=new GPSTracker(this);
		if (!gpsTracker.canGetLocation())
        {
			 gpsTracker.showSettingsAlert(ShareLinkActivity.this);
        }
		
		linkDes.setText(linkTitle);
		linkUrl.setText(siteName);
		if(isShare)
			linkImageView.setParseFile(ShareInActivity.mPostImageThumbnail);
		else
		{
			if(GroupPostListActivity.mPostImgFile1 !=null)
				linkImageView.setParseFile(GroupPostListActivity.mPostImgFile1);
			else
				linkImageView.setParseFile(TextPostActivity.mPostImgFile1);
		}
			
		linkImageView.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
		linkImageView.loadInBackground();
	}
	
	
	
	
	private void insertLinkValues()
	{
		
		postTxtView.setEnabled(false);
		String caption=linkCaption.getText().toString();
		IBinder token = linkCaption.getWindowToken();
		( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
		ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.GROUP_ID, mGroupId);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.POST_TEXT, caption);
		System.out.println("video id :: "+Utility.extractYTId(link));
		String id=Utility.extractYTId(link);
		if(id!=null)
		{
			userObject.put(Constants.POST_TYPE, "Video");
			userObject.put(Constants.VIDEO_ID, id);
		}
		else
			userObject.put(Constants.POST_TYPE, "Link");
		if(isShare)
		{
			userObject.put(Constants.POST_IMAGE, ShareInActivity.mPostImgFile);
			userObject.put(Constants.THUMBNAIL_PICTURE, ShareInActivity.mPostImageThumbnail);
		}
		else
		{
			if(GroupPostListActivity.mPostImgFile1!=null){
				userObject.put(Constants.POST_IMAGE, GroupPostListActivity.mPostImgFile1);
				userObject.put(Constants.THUMBNAIL_PICTURE, GroupPostListActivity.mPostImgFile1);
			}
			else{
				userObject.put(Constants.POST_IMAGE, TextPostActivity.mPostImgFile1);
				userObject.put(Constants.THUMBNAIL_PICTURE, TextPostActivity.mPostImgFile1);
			}
			
		}
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.POST_POINT, 100);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.IMAGE_CAPTION, linkTitle);
		userObject.put(Constants.LIKE_ARRAY, list);
		userObject.put(Constants.DIS_LIKE_ARRAY, list);
		userObject.put(Constants.FLAG_ARRAY, list);
		userObject.put(Constants.FEED_LOCATION, point);
		if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
			userObject.put(Constants.POST_STATUS, "Pending");
		else
			userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.IMAGE_HEIGHT, imgHeight);
		userObject.put(Constants.IMAGE_WIDTH, imgWidth);
		userObject.put(Constants.LINK_URL, link);
		userObject.put(Constants.SITE_NAME, siteName);
		
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.HASH_TAG_ARRAY, list);
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
	
		
		userObject.pinInBackground(mGroupId,new SaveCallback() {
           @Override
           public void done(ParseException e) {
               if(e == null) {
               	progressBar.setVisibility(View.GONE);
               	
               }
           }});
		userObject.saveInBackground(new SaveCallback() {
		@Override
		public void done(ParseException arg0) {
			postTxtView.setEnabled(true);
			System.out.println("after text post");
			progressBar.setVisibility(View.GONE);
			if(isShare)
        	{
				if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
            		Utility.showToastMessage(ShareLinkActivity.this, getResources().getString(R.string.post_approval_text));
				else
					Utility.showToastMessage(ShareLinkActivity.this, "Posted successfully");
				ShareInActivity.activity.finish();
				finish();
        	}
			else
			{
				if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
            		Utility.showToastMessage(ShareLinkActivity.this, getResources().getString(R.string.post_approval_text));
				
				if(GroupPostListActivity.mPostImgFile1!=null){
					GroupPostListActivity.flag=true;
					finish();
				}
				else{
					TopicListActivity.flag=true;
					TextPostActivity.activity.finish();
					finish();	
				}
						
				
			}
			
		}
	});
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object1, ParseException e) {
					if (e == null) 
					{
						if(object1!=null)
						{
							object1.increment(Constants.BADGE_POINT, 100);
							object1.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
						                 if (e == null) {
						                	 System.out
												.println("before setadapter");
             							//setAdapter();
						                 }
							          }});
						}
					}
				}
			});
	
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
