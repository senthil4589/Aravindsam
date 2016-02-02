package com.group.nearme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
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
import com.group.nearme.objects.GroupInfo;
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

public class GroupVisibiltyActivity extends Activity{
	private ImageView mBackImg;
	//private ImageView transparentImageView;
	private Button mNext;
	private SeekBar mVisibilityRadius;
	private GoogleMap googleMap;
	private GPSTracker gpsTracker;
	private Double mLatitude,mLongtitude;
	private MapFragment mMapFragment;
	private LatLng mLatLng;
	private int mVisibilityRadiusValue=500;
	private ProgressBar mProgressBar;
	private Marker marker;
	Circle mapCircle;
	TextView text;
	public static Activity activity;
	private RelativeLayout visibilityRangeLayout;
	private LinearLayout visibilityLabelLayout;
	private RadioGroup visibilityRadioGroup;
	private RadioButton nearbyRadioBtn,cityRadioBtn;
	 public GroupInfo groupInfo=new GroupInfo();
	 ArrayList<String> list=new ArrayList<String>();
	 boolean isCitySelected;
	 Dialog mDialog;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_group_location);
		Utility.getTracker(this, "CREATE GROUP - GROUP VISIBILITY SCREEN");
		initViews();
		//showMap();
		groupInfo=Utility.getGroupInfo();
		activity=this;
		if(groupInfo.getGroupType().equals(Constants.PRIVATE_GROUP))
		{
			mVisibilityRadiusValue=50;
			mVisibilityRadius.setProgress(50);
			 text.setText("" + 50);
			 text.setX(70);
		}
		else
		{
			mVisibilityRadiusValue=500;
			mVisibilityRadius.setProgress(500);
			 text.setText("" + 500);
			 text.setX(520);
		}
		
		if(groupInfo.getRangeList().size()==1 && groupInfo.getRangeList().get(0).equals(Constants.NEARBY_GROUP_VISIBILTY))
		{
			cityRadioBtn.setEnabled(false);
		}
		else
		{
			cityRadioBtn.setEnabled(true);
		}
		
		mVisibilityRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		    {
		       @TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@SuppressLint("NewApi")
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
		
		visibilityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            // checkedId is the RadioButton selected
	            RadioButton rb=(RadioButton)findViewById(checkedId);
	            
	           if(rb.getText().toString().equals("City"))
	           {
	        	   visibilityLabelLayout.setVisibility(View.GONE);
	        	   visibilityRangeLayout.setVisibility(View.GONE);
	           }
	           else
	           {
	        	   mVisibilityRadiusValue=500;
	        	   visibilityLabelLayout.setVisibility(View.VISIBLE);
	        	   visibilityRangeLayout.setVisibility(View.VISIBLE);
	           }
	        }
	    });
	}
	
	
	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mVisibilityRadius=(SeekBar) findViewById(R.id.visibility_radius);
		mNext=(Button) findViewById(R.id.next);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		text=(TextView) findViewById(R.id.text);
		
		mNext.setText("CREATE");
		
		visibilityLabelLayout=(LinearLayout) findViewById(R.id.visibility_label);
		visibilityRangeLayout=(RelativeLayout) findViewById(R.id.points);
		
		visibilityRadioGroup=(RadioGroup) findViewById(R.id.group_visibility_radio_group);
		
		nearbyRadioBtn=(RadioButton) findViewById(R.id.nearby);
		cityRadioBtn=(RadioButton) findViewById(R.id.city);
		
		mNext.setTypeface(Utility.getTypeface());
		nearbyRadioBtn.setTypeface(Utility.getTypeface());
		cityRadioBtn.setTypeface(Utility.getTypeface());
		
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
				insertValues();
			}
		});
		
		
	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void showMap() {
		mMapFragment =
               (MapFragment) getFragmentManager().findFragmentById(R.id.map);
       googleMap = mMapFragment.getMap();
       if(googleMap!=null)
       {
       googleMap.setMyLocationEnabled(true);
     //  if (gpsTracker.canGetLocation())
      // {
    	   mLatitude=gpsTracker.getLatitude();
    	   mLongtitude=gpsTracker.getLongitude();
    	   mLatLng=new LatLng(mLatitude, mLongtitude);
    	   googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
    	  // marker= googleMap.addMarker(
    			  // new MarkerOptions().position(mLatLng));
    	   googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15.5));
    	   googleMap.getUiSettings().setZoomControlsEnabled(false);
    	   createCircle(mLatLng, mVisibilityRadiusValue);
     
       googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition arg0) {
    	        mLatLng  =  googleMap.getCameraPosition().target;
    	        // Update your Marker's position to the center of the Map.
    	        //marker.setPosition(mLatLng);
    	        mapCircle.setCenter(mLatLng);
    	        //mapCircle.setRadius(mVisibilityRadiusValue);
    	        //mapCircle.setStrokeColor(0x10000000);
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
        	   googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15.5));
        	   mapCircle.setCenter(mLatLng);
   	           mapCircle.setRadius(500);
   	           mVisibilityRadius.setProgress(500);
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
	
	private void insertValues()
	{
		
		mNext.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);
		list.add(PreferenceSettings.getMobileNo());
		
		
		 RadioButton rb=(RadioButton)findViewById(visibilityRadioGroup.getCheckedRadioButtonId());
		 
		 if(rb.getText().toString().equals("City"))
		 {
			 isCitySelected=true;
			 mVisibilityRadiusValue=500;
		 }
		
		final ParseObject userObject = new ParseObject(Constants.GROUP_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.COUNTRY_NAME, PreferenceSettings.getCountry());
		userObject.put(Constants.GROUP_NAME, groupInfo.getGroupName());
		userObject.put(Constants.GROUP_DESCRIPTION, groupInfo.getGroupDes());
		userObject.put(Constants.GROUP_TYPE, groupInfo.getGroupType());
		userObject.put(Constants.GROUP_LOCATION, groupInfo.getLocationPoint());
		userObject.put(Constants.VISIBILITY_RADIUS, mVisibilityRadiusValue);
		userObject.put(Constants.MEMBER_COUNT, 1);
		userObject.put(Constants.MEMBERSHIP_APPROVAL, groupInfo.isMembershipApproval());
		userObject.put(Constants.MEMBER_INVITATION, groupInfo.isMemberInvitation());
		userObject.put(Constants.GROUP_PICTURE, groupInfo.getGroupLargeImage());
		userObject.put(Constants.THUMBNAIL_PICTURE, groupInfo.getGroupThumbnailImage());
		userObject.put(Constants.JOB_SCHEDULED, false);
		userObject.put(Constants.JOB_HOURS, 0);
		userObject.put(Constants.GROUP_MEMBERS, list);
		userObject.put(Constants.ADDITIONAL_INFO_REQUIRED, false);
		userObject.put(Constants.INFO_REQUIRED, "");
		userObject.put(Constants.LATEST_POST, "");
		userObject.put(Constants.GROUP_STATUS, "Active");
		userObject.put(Constants.SECRET_CODE, "");
		userObject.put(Constants.SECRET_STATUS, false);
		userObject.put(Constants.WHO_CAN_POST, groupInfo.getWhoCanPost());
		userObject.put(Constants.WHO_CAN_COMMENT, groupInfo.getWhoCanComment());
		userObject.put(Constants.POST_APPROVAL, groupInfo.isPostapproval());
		userObject.put(Constants.GROUP_VISIBLE_TILL_DATE, groupInfo.getGroupVisibleTilDate());
		userObject.put(Constants.ADMIN_ARRAY, list);
		userObject.put(Constants.VISIBILITY, Constants.NEARBY_GROUP_VISIBILTY);
		userObject.put(Constants.GROUP_CATEGORY, groupInfo.getGroupCategory());
		userObject.put(Constants.GROUP_PURPOSE,groupInfo.getGroupPurpose() );
		userObject.put(Constants.GROUP_TAG_ARRAY, groupInfo.getTagsList());

		if(groupInfo.isIfBusinessGroup())
		{
			HashMap<String, Object> groupObject=new HashMap<String,Object>();
			groupObject.put(Constants.BUSINESS_NAME,groupInfo.getBusinessName());
			groupObject.put(Constants.BUSINESS_ADDRESS,groupInfo.getBusinessAddress());
			groupObject.put(Constants.BUSINESS_BRANCH,groupInfo.getBusinessBranch());
			groupObject.put(Constants.BUSINESS_PHONE_NUMBER,groupInfo.getBusinessPhoneNo());
			groupObject.put(Constants.LATITUDE,groupInfo.getBusinessLocationPoint().getLatitude());
			groupObject.put(Constants.LONGTITUDE,groupInfo.getBusinessLocationPoint().getLongitude());
			userObject.put(Constants.GROUP_ATTRIBUTE,groupObject);
		}
		
		userObject.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	 System.out.println("inside group info inserted success");
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
								 							                	mNext.setEnabled(true);
								 							                	 MyGroupListActivity.flag1=true;
								 							                	mProgressBar.setVisibility(View.GONE);
								 							                	Utility.showToastMessage(GroupVisibiltyActivity.this, getResources().getString(R.string.create_group_success));
					             												/*Intent intent = new Intent(GroupVisibiltyActivity.this,TabGroupPostActivity.class);
					             												if(isCitySelected)
					             													intent.putExtra(Constants.CITY_GROUP_VISIBILTY, true);*/
					             												Utility.setGroupObject(userObject);
					             												Intent intent = new Intent(GroupVisibiltyActivity.this,TopicListActivity.class);
					             												startActivity(intent);
					             												ChooseGroupPurposeActivityNew.activity.finish();
					             												ChooseGroupTypeActivity.activity.finish();
					             												CreateGroupProfile.activity.finish();
					             												CreateGroupSettingsActivity.activity.finish();
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
	                	 System.out.println("inside exception group info inserted success"+e);
	                	 mProgressBar.setVisibility(View.GONE);
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
