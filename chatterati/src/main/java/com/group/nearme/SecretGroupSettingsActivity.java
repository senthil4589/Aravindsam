package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.objects.GroupInfo;
import com.group.nearme.onoffswitch.SwitchButton;
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

public class SecretGroupSettingsActivity extends Activity{
	private Button mSaveBtn;
	private ImageView mBackImg;
	private int whoCanPostFlag=0,whoCanCommentFlag=0;
	private ProgressBar mProgressbar;
	boolean flag,flag1;
	
	private ParseGeoPoint mLocationPoint;
	private Double mLatitude,mLongtitude;
	private GPSTracker gpsTracker;
	
	private ArrayList<String> list=new ArrayList<String>();
	
    private SwitchButton postApprovalToggle,whoCanPostToggle,whoCanCommentToggle;
    private RelativeLayout postApprovalLayout;
    public GroupInfo groupInfo=new GroupInfo();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.secret_group_settings);
		Utility.getTracker(this, "CREATE GROUP - SETTINGS FOR SECRET");
		initViews();
		groupInfo=Utility.getGroupInfo();	
		
		whoCanPostToggle.setChecked(false);
		whoCanCommentToggle.setChecked(false);
		postApprovalLayout.setVisibility(View.VISIBLE);
		
		whoCanPostToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
	            @Override
	            public void onCheckedChanged(CompoundButton buttonView,
	                    boolean isChecked) {
	               
	            	if(isChecked)
	            	{
	            		whoCanPostFlag=1;
	            		postApprovalLayout.setVisibility(View.GONE);
	            	}
	            	else
	            	{
	            		whoCanPostFlag=0;
	            		postApprovalLayout.setVisibility(View.VISIBLE);
	            	}
	            }
		});
		
		whoCanCommentToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
	            @Override
	            public void onCheckedChanged(CompoundButton buttonView,
	                    boolean isChecked) {
	               
	            	if(isChecked)
	            	{
	            		whoCanCommentFlag=1;
	            	}
	            	else
	            	{
	            		whoCanCommentFlag=0;
	            	}
	            }
		});
	    
		mSaveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				insert();
			}
		});
}
		private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mSaveBtn=(Button) findViewById(R.id.save);
		mProgressbar=(ProgressBar) findViewById(R.id.progressBar);
		
		whoCanPostToggle=(SwitchButton) findViewById(R.id.who_can_post_toggle);
		whoCanCommentToggle=(SwitchButton) findViewById(R.id.who_can_comment_toggle);
				
		postApprovalToggle=(SwitchButton) findViewById(R.id.post_approval_toggle);
		postApprovalLayout=(RelativeLayout) findViewById(R.id.post_approval_layout);
		
		mSaveBtn.setTypeface(Utility.getTypeface());
		mSaveBtn.setText("CREATE");
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
	}
	
	public String checkDigit(int number)
    {
        return number<=9?"0"+number:String.valueOf(number);
    }
	
	private void insert()
	{
		mSaveBtn.setEnabled(false);
		mProgressbar.setVisibility(View.VISIBLE);
		mLocationPoint=new ParseGeoPoint(mLatitude,mLongtitude);
		list.add(PreferenceSettings.getMobileNo());
		final ParseObject userObject = new ParseObject(Constants.GROUP_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.COUNTRY_NAME, PreferenceSettings.getCountry());
		userObject.put(Constants.GROUP_NAME, groupInfo.getGroupName());
		userObject.put(Constants.GROUP_DESCRIPTION, groupInfo.getGroupDes());
		userObject.put(Constants.GROUP_TYPE, groupInfo.getGroupType());
		userObject.put(Constants.GROUP_LOCATION, mLocationPoint);
		userObject.put(Constants.VISIBILITY_RADIUS, 0);
		userObject.put(Constants.MEMBER_COUNT, 1);
		userObject.put(Constants.MEMBERSHIP_APPROVAL, false);
		userObject.put(Constants.MEMBER_INVITATION, false);
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
		userObject.put(Constants.WHO_CAN_POST, whoCanPostFlag);
		userObject.put(Constants.WHO_CAN_COMMENT, whoCanCommentFlag);
		if(postApprovalLayout.getVisibility()==View.VISIBLE)
			userObject.put(Constants.POST_APPROVAL, postApprovalToggle.isChecked());
		else
			userObject.put(Constants.POST_APPROVAL, false);
		userObject.put(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getPastDate());
		userObject.put(Constants.ADMIN_ARRAY, list);
		userObject.put(Constants.VISIBILITY, Constants.NEARBY_GROUP_VISIBILTY);
		userObject.put(Constants.GROUP_CATEGORY, groupInfo.getGroupCategory());
		userObject.put(Constants.GROUP_PURPOSE,groupInfo.getGroupPurpose() );
		userObject.put(Constants.GROUP_TAG_ARRAY, groupInfo.getTagsList());
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
								 							                	mProgressbar.setVisibility(View.GONE);
								 							                	Utility.showToastMessage(SecretGroupSettingsActivity.this, getResources().getString(R.string.create_group_success));
					             												//Intent intent = new Intent(SecretGroupSettingsActivity.this,TabGroupPostActivity.class);
					             												Utility.setGroupObject(userObject);
					             												Intent intent = new Intent(SecretGroupSettingsActivity.this,TopicListActivity.class);
					             												startActivity(intent);
					             												ChooseGroupPurposeActivityNew.activity.finish();
					             												ChooseGroupTypeActivity.activity.finish();
					             												CreateGroupProfile.activity.finish();
					             												finish();
					             												//CreateGroupSettingsActivity.activity.finish();
								 							                }
								      							        }});
	             												
	             											}
	             											else
	             												mProgressbar.setVisibility(View.GONE);
	             										}
	             										else
	             											mProgressbar.setVisibility(View.GONE);
	             								}
	             							});
	             		

	                 } else {
	                	 mProgressbar.setVisibility(View.GONE);
	                 }
	               }
	            });
			
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		gpsTracker = new GPSTracker(this);
		  if (gpsTracker.canGetLocation())
	       {
			  mLatitude=gpsTracker.getLatitude();
		  	   mLongtitude=gpsTracker.getLongitude();
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
