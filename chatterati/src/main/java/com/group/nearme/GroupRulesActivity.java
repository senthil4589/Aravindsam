package com.group.nearme;

import java.util.ArrayList;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class GroupRulesActivity extends Activity
{
	ImageView mBackImgView;
	EditText mGroupRulesEditTxt;
	Button mEditBtn;
	ParseObject groupObject;
	ParseImageView mGroupImage;
	ParseFile mGroupImageFile=null;
	ProgressBar progressBar;
	String groupRules="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_rules);
		Utility.getTracker(this, "GROUP RULES SCREEN");
		initViews();
		
		groupRules=getIntent().getStringExtra("Rules");
		mGroupRulesEditTxt.setText(groupRules);
		mGroupRulesEditTxt.setSelection(mGroupRulesEditTxt.getText().length());
		
		mEditBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String rules=mGroupRulesEditTxt.getText().toString();
					mEditBtn.setEnabled(false);
					progressBar.setVisibility(View.VISIBLE);
					GPSTracker gpsTracker=new GPSTracker(GroupRulesActivity.this);
					boolean flag=getIntent().getBooleanExtra("isFirstTime", false);
					if(flag)
					{
						
						
						ArrayList<String> list=new ArrayList<String>();
						ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
						final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
						userObject.put(Constants.GROUP_ID, groupObject.getObjectId());
						userObject.put(Constants.POST_TEXT, rules);
						 userObject.put(Constants.POST_TYPE, "Rule");
						 userObject.put(Constants.COMMENT_COUNT, 0);
						 userObject.put(Constants.FLAG_COUNT, 0);
							userObject.put(Constants.IMAGE_CAPTION, "");
							userObject.put(Constants.LIKE_ARRAY, list);
							userObject.put(Constants.DIS_LIKE_ARRAY, list);
							userObject.put(Constants.POST_POINT, 100);
							userObject.put(Constants.POST_STATUS, "InActive");
							userObject.put(Constants.FLAG_ARRAY, list);
							userObject.put(Constants.FEED_LOCATION, point);
							userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
							userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
							userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
							userObject.put(Constants.HASH_TAG_ARRAY, list);
							userObject.saveInBackground(new SaveCallback() {
								@Override
								public void done(ParseException arg0) {
									mEditBtn.setEnabled(true);
									progressBar.setVisibility(View.GONE);
									userObject.pinInBackground(groupObject.getObjectId());
									Utility.showToastMessage(GroupRulesActivity.this, "Group rules updated successfully");
									finish();
								}
							});
					}
					else
					{
						final ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
						String objectId=getIntent().getStringExtra(Constants.OBJECT_ID);
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.OBJECT_ID, objectId);
						//query.fromPin(groupObject.getObjectId());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject object, ParseException e) {
									if (object != null) 
									{
										
										object.put(Constants.POST_TEXT, rules);
										object.put(Constants.FEED_LOCATION, point);
										object.saveInBackground(new SaveCallback() {
											@Override
											public void done(ParseException arg0) {
												mEditBtn.setEnabled(true);
												progressBar.setVisibility(View.GONE);
												object.pinInBackground(groupObject.getObjectId());
												Utility.showToastMessage(GroupRulesActivity.this, "Group rules updated successfully");
												finish();
											}
										});
									}
									else
									{
										mEditBtn.setEnabled(true);
										progressBar.setVisibility(View.GONE);
										Utility.showToastMessage(GroupRulesActivity.this, getResources().getString(R.string.server_issue));
										finish();
									}
							}});
						
					}
				
			}
		});
	}

	private void initViews() {
		
		mBackImgView=(ImageView) findViewById(R.id.back);
		mGroupImage=(ParseImageView) findViewById(R.id.group_image);
		mGroupRulesEditTxt=(EditText) findViewById(R.id.group_rules_edittxt);
		mEditBtn=(Button) findViewById(R.id.save);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		mBackImgView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mEditBtn.setTypeface(Utility.getTypeface());
		mGroupRulesEditTxt.setTypeface(Utility.getTypeface());
		
		groupObject=Utility.getGroupObject();

		if(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE)==null)
			mGroupImageFile=groupObject.getParseFile(Constants.GROUP_PICTURE);
		else
			mGroupImageFile=groupObject.getParseFile(Constants.THUMBNAIL_PICTURE);
		
		
		mGroupImage.setParseFile(mGroupImageFile);
		mGroupImage.loadInBackground(new GetDataCallback() {
			@Override
			public void done(byte[] arg0, ParseException arg1) {
				
			}
		});
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			mEditBtn.setVisibility(View.VISIBLE);
			
		}
		else
		{
			mEditBtn.setVisibility(View.GONE);
			mGroupRulesEditTxt.setKeyListener(null);
			mGroupRulesEditTxt.setBackgroundResource(0);
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
