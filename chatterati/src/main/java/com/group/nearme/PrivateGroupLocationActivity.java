package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class PrivateGroupLocationActivity extends Activity{
	private ImageView mBackImg;
	//private ImageView transparentImageView;
	private Button mNext;
	private SeekBar mVisibilityRadius;
	private GoogleMap googleMap;
	private GPSTracker gpsTracker;
	private Double mLatitude,mLongtitude;
	private MapFragment mMapFragment;
	private LatLng mLatLng;
	private int mVisibilityRadiusValue=50;
	private ProgressBar mProgressBar;
	private Marker marker;
	Circle mapCircle;
//	public static Activity activity;
	//ScrollView scrollView;
	String mGroupName="",groupDes="";
	//private boolean memberApproval;
	private ParseGeoPoint mLocationPoint;
	private ArrayList<String> list=new ArrayList<String>();
	private ArrayList<String> list1=new ArrayList<String>();
	TextView text;
	public static Activity activity;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_location);
		Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
		//showMap();
		activity=this;
		 
		//mVisibilityRadiusValue=mVisibilityRadius.getProgress()+20;
		 mVisibilityRadius.setProgress(50);
		 text.setText("" + 50);
		 text.setX(70);
		
		 mVisibilityRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		    {
		       @SuppressLint("NewApi")
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		       {
		         if(progress <= 50)
		        	 mVisibilityRadiusValue=progress+20;
		         else
		        	 mVisibilityRadiusValue=progress;
		         
		         mapCircle.setCenter(mLatLng);
	    	        mapCircle.setRadius(progress);
	    	        
	    	      // TextView _testText=new TextView(PrivateGroupLocationActivity.this);
	    	        int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
	    	        text.setText("" + mVisibilityRadiusValue);
	    	        text.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
		       }

		      public void onStartTrackingTouch(SeekBar seekBar) {
		    	
		      }

		      public void onStopTrackingTouch(SeekBar seekBar) {}
		    });
	}
	
	
	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mVisibilityRadius=(SeekBar) findViewById(R.id.visibility_radius);
		mNext=(Button) findViewById(R.id.next);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		//transparentImageView=(ImageView) findViewById(R.id.mapImgView);
		mGroupName=getIntent().getStringExtra(Constants.GROUP_NAME);
		groupDes=getIntent().getStringExtra(Constants.GROUP_DESCRIPTION);
		//memberApproval=getIntent().getBooleanExtra(Constants.MEMBERSHIP_APPROVAL, true);
		text=(TextView) findViewById(R.id.text);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNext.setTypeface(tf);
		//scrollView=(ScrollView) findViewById(R.id.scroll_view);
		//gpsTracker = new GPSTracker(this);
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		
		mNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//insert();
				goToGroupSettings();
			}
		});
		
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void showMap() {
		mMapFragment =
               (MapFragment) getFragmentManager().findFragmentById(R.id.map);
       googleMap = mMapFragment.getMap();
       googleMap.setMyLocationEnabled(true);
       if(googleMap!=null)
       {
       //if (gpsTracker.canGetLocation())
      // {
    	   mLatitude=gpsTracker.getLatitude();
    	   mLongtitude=gpsTracker.getLongitude();
    	   mLatLng=new LatLng(mLatitude, mLongtitude);
    	   googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
    	 //  marker= googleMap.addMarker(
    			 //  new MarkerOptions().position(mLatLng));
    	   googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) 16.5));
    	   googleMap.getUiSettings().setZoomControlsEnabled(false);
    	   createCircle(mLatLng, mVisibilityRadiusValue);
     
       googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition arg0) {
    	        mLatLng  =  googleMap.getCameraPosition().target;
    	        mapCircle.setCenter(mLatLng);
			}
    	});
       
       googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
           @Override
           public boolean onMyLocationButtonClick() {
        	   mLatitude=gpsTracker.getLatitude();
        	   mLongtitude=gpsTracker.getLongitude();
        	   mLatLng=new LatLng(mLatitude, mLongtitude);
        	   googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
        	  // marker.setPosition(mLatLng);
        	   googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) 16.5));
        	   mapCircle.setCenter(mLatLng);
   	           mapCircle.setRadius(50);
   	        mVisibilityRadiusValue=50;
   	     text.setText("" + 50);
		// text.setX(70);
		
   	           mVisibilityRadius.setProgress(50);
        	   return true;
           }
       });
       }
	}
	
	private void createCircle(LatLng latlng,int mts)
	{
		  CircleOptions circleOptions = new CircleOptions()
          .center(mLatLng)   //set center
          .radius(mts)   //set radius in meters
          .fillColor(0x5503A9F4) //default
          .strokeColor(0x10000000)
          .strokeWidth(2);
          mapCircle= googleMap.addCircle(circleOptions);
	}
	
	private void goToGroupSettings()
	{
		Intent i=new Intent(this,CreateGroupSettingsActivity.class);
		i.putExtra(Constants.GROUP_NAME, mGroupName);
		i.putExtra(Constants.GROUP_DESCRIPTION, groupDes);
		i.putExtra(Constants.VISIBILITY_RADIUS, mVisibilityRadiusValue);
		//i.putExtra(Constants.MEMBERSHIP_APPROVAL, memberApproval);
		i.putExtra(Constants.GROUP_TYPE, "Private");
		startActivity(i);
		overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	}
	
	
	private void insert()
	{
		mNext.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);
		mLocationPoint=new ParseGeoPoint(mLatLng.latitude,mLatLng.longitude);
		list.add(PreferenceSettings.getMobileNo());
		final ParseObject userObject = new ParseObject(Constants.GROUP_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.COUNTRY_NAME, PreferenceSettings.getCountry());
		userObject.put(Constants.GROUP_NAME, mGroupName);
		userObject.put(Constants.GROUP_PICTURE, CreatePrivateGroupActivity.mGroupImageFile);
		userObject.put(Constants.THUMBNAIL_PICTURE, CreatePrivateGroupActivity.mGroupImageTumbnailFile);
		userObject.put(Constants.GROUP_DESCRIPTION, groupDes);
		userObject.put(Constants.GROUP_TYPE, "Private");
		userObject.put(Constants.GROUP_LOCATION, mLocationPoint);
		userObject.put(Constants.VISIBILITY_RADIUS, mVisibilityRadiusValue);
		userObject.put(Constants.MEMBER_COUNT, 1);
		userObject.put(Constants.NEWS_FEED_COUNT, 0);
		userObject.put(Constants.MEMBER_INVITATION, true);
		userObject.put(Constants.MEMBERSHIP_APPROVAL, false);
		userObject.put(Constants.JOB_SCHEDULED, false);
		userObject.put(Constants.JOB_HOURS, 0);
		userObject.put(Constants.GROUP_MEMBERS, list);
		userObject.put(Constants.ADDITIONAL_INFO_REQUIRED, false);
		userObject.put(Constants.INFO_REQUIRED, "");
		userObject.put(Constants.GREEN_CHANNEL, list1);
		userObject.put(Constants.LATEST_POST, "");
		userObject.put(Constants.GROUP_STATUS, "Active");
		userObject.put(Constants.SECRET_CODE, "");
		userObject.put(Constants.SECRET_STATUS, false);
		userObject.put(Constants.WHO_CAN_POST, 0);
		userObject.put(Constants.WHO_CAN_COMMENT, 0);
		userObject.put(Constants.ADMIN_ARRAY, list);
		userObject.put(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getFutureDate());
		
		userObject.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	 userObject.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
	                	 
	                	 ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
		             		memberObject.put(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
		             		memberObject.put(Constants.GROUP_ID, userObject.getObjectId());
		             		memberObject.put(Constants.ADDITIONAL_INFO_PROVIDED, "");
		             		memberObject.put(Constants.JOIN_DATE, Utility.getCurrentDate());
		             		memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
		             		memberObject.put(Constants.EXIT_GROUP, false);
		             		memberObject.put(Constants.EXITED_BY, "");
		             		memberObject.put(Constants.MEMBER_STATUS, "Active");
		             		//memberObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		             		//memberObject.put(Constants.MEMBER_IMAGE, Utility.getUserImageFile());
		             		memberObject.put(Constants.GROUP_ADMIN, true);
		             		memberObject.put(Constants.UNREAD_MESSAGES, 0);
		             		memberObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		             		memberObject.saveInBackground();
	             		
	             							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
	             							query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	             							query.getFirstInBackground(new GetCallback<ParseObject>() {
	             								public void done(ParseObject object, ParseException e) {
	             										if (e == null) 
	             										{
	             											if(object!=null)
	             											{
	             												List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
	             												myGroupList.add(userObject.getObjectId());
	             												object.put(Constants.MY_GROUP_ARRAY, myGroupList);
	             												object.increment(Constants.BADGE_POINT, 1000);
	             												object.saveInBackground(new SaveCallback() {
							      							          public void done(ParseException e) {
								 							                 if (e == null) {
								 							                	 MyGroupListActivity.flag1=true;
								 							                	mProgressBar.setVisibility(View.GONE);
								 							                	Utility.showToastMessage(PrivateGroupLocationActivity.this, getResources().getString(R.string.create_group_success));
					             												Intent intent = new Intent(PrivateGroupLocationActivity.this,TabGroupPostActivity.class);
					             												Utility.setGroupObject(userObject);
					             												startActivity(intent);
					             												CreateGroupActivity.activity.finish();
					             												CreatePrivateGroupActivity.activity.finish();
					             												finish();
								 							                }
								      							        }});
	             												
	             											}
	             											else
	             												mProgressBar.setVisibility(View.GONE);
	             										}
	             										else
	             											mProgressBar.setVisibility(View.GONE);
	             								}
	             							});
	             		

	                 } else {
	                	 mProgressBar.setVisibility(View.GONE);
	                 }
	               }
	            });
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		gpsTracker = new GPSTracker(this);
		  if (gpsTracker.canGetLocation())
	       {
			  showMap();
	       }
		  else
	       {
	           gpsTracker.showSettingsAlert(this);
	       }
		  
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