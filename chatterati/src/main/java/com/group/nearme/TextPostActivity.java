package com.group.nearme;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.embedly.api.Api;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class TextPostActivity extends Activity{

	ImageView postImg,cancelImg;
	EditText textEditBox;
	ProgressBar progressBar;
	String mPostText="";
	public GPSTracker gpsTracker;
	private ArrayList<String> list=new ArrayList<String>();
	ParseObject groupObject;
	private String mGroupId="",mGroupName="",mGroupImage="";
	
	String linkTitle="",linkImage="",linkImagePath;
	private int imgWidth,imgHeight;
	byte[] imgByte;
	String siteName="";
	Api api = new Api("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2", "eea3a0754b2146698c346dddc5adf262");
	HashMap<String, Object> linkParams;
	public static ParseFile mPostImgFile1;
	public static Activity activity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.text_post);
		Utility.getTracker(this, "TEXT POST SCREEN");
		initViews();
		activity=this;
		
		postImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPostText=textEditBox.getText().toString();
				
				if(mPostText.isEmpty())
				{
					 Utility.showToastMessage(TextPostActivity.this, getResources().getString(R.string.post_text_empty));
				}
				else
				{
					InputMethodManager inputManager1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
					inputManager1.hideSoftInputFromWindow( TextPostActivity.this.getCurrentFocus().getWindowToken(),
			        InputMethodManager.HIDE_NOT_ALWAYS); 
					if (gpsTracker.canGetLocation())
			        {
						if(Utility.checkInternetConnectivity(TextPostActivity.this))
						{
							 if(Patterns.WEB_URL.matcher(mPostText).matches())
							 {
								 progressBar.setVisibility(View.VISIBLE);
								 textEditBox.setEnabled(false);
								( new ParseURL() ).execute(new String[]{mPostText});
							 }
							 else
							 {
								 System.out.println("not valid url");
								 insertValues();
							 }
						}
						else
							Utility.showToastMessage(TextPostActivity.this, getResources().getString(R.string.no_network));
			        }
			        else
			        {
			            gpsTracker.showSettingsAlert(TextPostActivity.this);
			        }
					
				}
				
			}
		});
		
		cancelImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void initViews() {
		postImg=(ImageView) findViewById(R.id.post);
		cancelImg=(ImageView) findViewById(R.id.cancel);
		textEditBox=(EditText) findViewById(R.id.text_box);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		gpsTracker=new GPSTracker(this);
		
		groupObject=Utility.getGroupObject();
		
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		
	}
	
	
	private void insertValues()
	{
		System.out.println("inside insertvalues");
		progressBar.setVisibility(View.VISIBLE);
		textEditBox.setEnabled(false);
		ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.GROUP_ID, mGroupId);
		userObject.put(Constants.POST_TEXT, mPostText);
		/*if(userImage!=null)
			userObject.put(Constants.MEMBER_IMAGE, userImage);*/
		 userObject.put(Constants.POST_TYPE, "Text");
		 userObject.put(Constants.COMMENT_COUNT, 0);
		 userObject.put(Constants.FLAG_COUNT, 0);
			userObject.put(Constants.IMAGE_CAPTION, "");
			userObject.put(Constants.LIKE_ARRAY, list);
			userObject.put(Constants.DIS_LIKE_ARRAY, list);
			userObject.put(Constants.POST_POINT, 100);
			if(groupObject.getBoolean(Constants.POST_APPROVAL) && !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				userObject.put(Constants.POST_STATUS, "Pending");
			else
				userObject.put(Constants.POST_STATUS, "Active");
			userObject.put(Constants.FLAG_ARRAY, list);
			userObject.put(Constants.FEED_LOCATION, point);
			//userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
			userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
			userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
			userObject.put(Constants.HASH_TAG_ARRAY, list);
			System.out.println("befroe text post");
			
			
			userObject.pinInBackground(mGroupId,new SaveCallback() {
	            @Override
	            public void done(ParseException e) {
	                if(e == null) {
	                	progressBar.setVisibility(View.GONE);
	                	if(groupObject.getBoolean(Constants.POST_APPROVAL) && !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
	                		Utility.showToastMessage(TextPostActivity.this, getResources().getString(R.string.post_approval_text));
	                	//setAdapter();
	                	TopicListActivity.flag=true;
	                	finish();
	                }
	            }});
			userObject.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException arg0) {
				System.out.println("after text post");
				progressBar.setVisibility(View.GONE);
				
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
		});
	}	
	
	private class ParseURL extends AsyncTask<String, Void, Bitmap> {
		
	     @Override
	     protected Bitmap doInBackground(String... strings) {
	    
	         try {
	        	 linkParams=new HashMap<String, Object>();
	        	 linkParams.put("url",strings[0]);
	        	 JSONArray oembed = api.oembed(linkParams);
	        	 
	        	 linkImage = oembed.getJSONObject(0).getString("thumbnail_url");
	        	 linkTitle=oembed.getJSONObject(0).getString("title");
	        	 siteName=oembed.getJSONObject(0).getString("provider_name");
	        	 
	        	 if(siteName.isEmpty())
	        	 {
	        		 URL url=new URL(strings[0]);
					 siteName=url.getHost();
	        	 }
	        	 
	        	 if(!linkImage.isEmpty())
	        	 {
	        		 Bitmap bitmap=getImageBitmapFromUrl(new URL(linkImage));
               	 return bitmap;
	        	 }
	             }
	         catch(Exception t) {
	        	System.out.println("exception  :: "+t);
	           return null;
	         }
	 
	         return null;
	     }
	 
	     @Override
	     protected void onPostExecute(Bitmap bitmap) {
	         super.onPostExecute(bitmap);
	         
	       if(bitmap!=null)
	       {
	    	   if(!linkTitle.isEmpty() && !linkImage.isEmpty())
	    	   {
	    		   ByteArrayOutputStream stream = new ByteArrayOutputStream();
	   			   bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
	               imgByte = stream.toByteArray();
	               mPostImgFile1= new ParseFile("Image.png", imgByte);
	              // mPostImgFile.saveInBackground();
	               imgWidth=bitmap.getWidth();
	   			   imgHeight=bitmap.getHeight();
	   			   mPostImgFile1.saveInBackground(new SaveCallback() {
	  	          public void done(ParseException e) {
	  	                 if (e == null) {
	  	                	// insertLinkValues();
	  	                	 progressBar.setVisibility(View.GONE);
	  	                	textEditBox.setEnabled(true);
	  	                	startActivity(new Intent(TextPostActivity.this,ShareLinkActivity.class).putExtra("isShare", false).putExtra(Constants.GROUP_ID, mGroupId)
	  	                			.putExtra("link_title",linkTitle).putExtra(Constants.LINK_URL,mPostText).putExtra(Constants.SITE_NAME,siteName)
	  	                			.putExtra(Constants.IMAGE_WIDTH,imgWidth).putExtra(Constants.IMAGE_HEIGHT,imgHeight));
	  	                	overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	  	                 }
	  	          }});
	    		   
	    	   }
	    	   else
	    	   {
	    		   insertValues();
	    	   }
	    	   
	       }
	       else
   	   {
   		   insertValues();
   	   }
	     }
	}
	
	 public Bitmap getImageBitmapFromUrl(URL url)
	  { 
	    Bitmap bm = null; 
	    try { 
	      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	      if(conn.getResponseCode() != 200)
	      {
	        return bm;
	      }
	      conn.connect();
	      InputStream is = conn.getInputStream();

	      BufferedInputStream bis = new BufferedInputStream(is); 
	      try
	      {
	        bm = BitmapFactory.decodeStream(bis); 
	      }
	      catch(OutOfMemoryError ex)
	      {
	        bm = null;
	      }
	      bis.close(); 
	      is.close(); 
	    } catch (Exception e) {}
	    
	      return bm; 
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
