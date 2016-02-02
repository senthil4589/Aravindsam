package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class GroupLocationActivity extends Activity{
	private ImageView mBackImg;
	private Button mNext;
	private SeekBar mVisibilityRadius;
	private GoogleMap googleMap;
	//private GPSTracker gpsTracker;
	private Double mLatitude,mLongtitude;
	private MapFragment mMapFragment;
	private LatLng mLatLng;
	private int mVisibilityRadiusValue=50;
	private ProgressBar mProgressBar;
	private Marker marker;
	Circle mapCircle;
	private ParseGeoPoint mLocationPoint;
	ParseObject groupObject;
	private String mGroupId="",mGroupName="",mMobileNo="",mGroupType="";
	RelativeLayout points;
	LinearLayout visibiltyLabel;
	TextView text,moveMapText,visibilityText;
	 List<String> adminList=new ArrayList<String>();
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_location);
		Utility.getTracker(this, "GROUP INFO - GROUP LOCATION SCREEN");
		initViews();
		
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		
		adminList=groupObject.getList(Constants.ADMIN_ARRAY);
		
		mVisibilityRadiusValue=groupObject.getInt(Constants.VISIBILITY_RADIUS);
		mLocationPoint=groupObject.getParseGeoPoint(Constants.GROUP_LOCATION);
		mLatitude=mLocationPoint.getLatitude();
		mLongtitude=mLocationPoint.getLongitude();
		mLatLng=new LatLng(mLatitude, mLongtitude);
		mVisibilityRadius.setProgress(mVisibilityRadiusValue);

		if(adminList.contains(PreferenceSettings.getMobileNo()))
		{
			//mNext.setEnabled(true);
			//googleMap.getUiSettings().setAllGesturesEnabled(true);
			points.setVisibility(View.VISIBLE);
			mVisibilityRadius.setVisibility(View.VISIBLE);
			visibiltyLabel.setVisibility(View.VISIBLE);
			mNext.setVisibility(View.VISIBLE);
			//visibilityText.setVisibility(View.GONE);
			//moveMapText.setVisibility(View.VISIBLE);
			moveMapText.setText("Move map to pin your group at the right spot");
		}
		else
		{
			//googleMap.getUiSettings().setAllGesturesEnabled(false);
			//mNext.setEnabled(false);
			points.setVisibility(View.GONE);
			mVisibilityRadius.setVisibility(View.GONE);
			visibiltyLabel.setVisibility(View.GONE);
			text.setVisibility(View.GONE);
			mNext.setVisibility(View.GONE);
			//visibilityText.setVisibility(View.VISIBLE);
			//moveMapText.setVisibility(View.GONE);
			moveMapText.setText("Visibility Radius : "+mVisibilityRadiusValue+" meters");
		}
		
		
		showMap();
		
		//mVisibilityRadius.setProgress(50);
		System.out.println("visibility value :: "+mVisibilityRadiusValue);
		if(mVisibilityRadiusValue ==0)
		{
		 text.setText("" + 50);
		 text.setX(70);
		}
		else
		{
			 text.setText("" + mVisibilityRadiusValue);
			 text.setX(mVisibilityRadiusValue);
		}
		mVisibilityRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
	    {
	       public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	       {
	         if(progress <= 50)
	        	 mVisibilityRadiusValue=progress+20;
	         else
	        	 mVisibilityRadiusValue=progress;
	         
	         mapCircle.setCenter(mLatLng);
    	        mapCircle.setRadius(progress);
    	        
    	        
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
		points=(RelativeLayout) findViewById(R.id.points);
		visibiltyLabel=(LinearLayout) findViewById(R.id.visibility_label);
		text=(TextView) findViewById(R.id.text);
		moveMapText=(TextView) findViewById(R.id.move_map);
		visibilityText=(TextView) findViewById(R.id.map_radius);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNext.setTypeface(tf);
		mNext.setText("DONE");
	//	gpsTracker = new GPSTracker(this);
		
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
				if(adminList.contains(PreferenceSettings.getMobileNo()))
				{
					insert();
				}
				else
				{
					finish();
				}
			}
		});
		
		//mVisibilityRadiusValue=mVisibilityRadius.getProgress();
		
	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void showMap() {
		mMapFragment =
               (MapFragment) getFragmentManager().findFragmentById(R.id.map);
       googleMap = mMapFragment.getMap();
       if(googleMap!=null)
       {
       googleMap.getUiSettings().setAllGesturesEnabled(false);
       googleMap.setMyLocationEnabled(false);
     
    	   googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
    	  // marker= googleMap.addMarker(
    			   //new MarkerOptions().position(mLatLng));
    	   googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15.5));
    	   googleMap.getUiSettings().setZoomControlsEnabled(false);
    	   createCircle(mLatLng, mVisibilityRadiusValue);
       }
    	   /* }
       else
       {
           gpsTracker.showSettingsAlert(this);
       }
       
       googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
           @Override
           public void onMapClick(LatLng point) {
        	   mLatLng=point;
        	   marker.setPosition(mLatLng);
        	   mapCircle.setCenter(mLatLng);
        	   mapCircle.setRadius(mVisibilityRadiusValue);
           }
       });
      googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition arg0) {
    	        mLatLng  =  googleMap.getCameraPosition().target;
    	        // Update your Marker's position to the center of the Map.
    	        marker.setPosition(mLatLng);
    	        mapCircle.setCenter(mLatLng);
    	        mapCircle.setRadius(mVisibilityRadiusValue);
    	        mapCircle.setStrokeColor(0x10000000);
			}
    	});
       
       googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
           @Override
           public boolean onMyLocationButtonClick() {
        	   mLatitude=gpsTracker.getLatitude();
        	   mLongtitude=gpsTracker.getLongitude();
        	   mLatLng=new LatLng(mLatitude, mLongtitude);
        	   googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
        	   marker.setPosition(mLatLng);
        	   googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        	   mapCircle.setCenter(mLatLng);
   	           mapCircle.setRadius(25);
   	           mVisibilityRadius.setProgress(50);
        	   return true;
           }
       });*/
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
	
	
	private void insert()
	{
		mProgressBar.setVisibility(View.VISIBLE);
		mNext.setEnabled(false);
		//groupObject.put(Constants.GROUP_LOCATION, mLocationPoint);
		groupObject.put(Constants.VISIBILITY_RADIUS, mVisibilityRadiusValue);
		groupObject.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	 mProgressBar.setVisibility(View.GONE);
	                	 NearByGroupListActivity.flag2=true;
	                	 Utility.showToastMessage(GroupLocationActivity.this, getResources().getString(R.string.group_location_update_success));
	                	 finish();
	                 }
	          }});
		
		/*mLocationPoint=new ParseGeoPoint(mLatLng.latitude,mLatLng.longitude);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.whereEqualTo(Constants.GROUP_NAME, mGroupName);
		query.whereNotEqualTo(Constants.OBJECT_ID, mGroupId);
		query.whereEqualTo(Constants.GROUP_LOCATION, mLocationPoint);
		query.whereWithinMiles(Constants.GROUP_LOCATION, mLocationPoint,0.310);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> totemList, ParseException e) {
					if (e == null) 
					{
						
						if(totemList.size() == 0) // already exist 
						{
							groupObject.put(Constants.GROUP_LOCATION, mLocationPoint);
							groupObject.put(Constants.VISIBILITY_RADIUS, mVisibilityRadiusValue);
							groupObject.saveInBackground(new SaveCallback() {
						          public void done(ParseException e) {
						                 if (e == null) {
						                	 mProgressBar.setVisibility(View.GONE);
						                	 Toast.makeText(GroupLocationActivity.this, "Group location updated successfully", Toast.LENGTH_LONG).show();
						                	 finish();
						                 }
						          }});
						}
						else 
						{
								mProgressBar.setVisibility(View.GONE);
								Toast.makeText(GroupLocationActivity.this, "In this location group name already exist. Please choose some other location", Toast.LENGTH_LONG).show();
						}
					}
					else
						mProgressBar.setVisibility(View.GONE);
			}});*/
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
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