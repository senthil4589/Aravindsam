package com.group.nearme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class MemberProfileActivity extends Activity {
	TextView memberName,memberJoinDate,additionalInfo,additionalInfoLabel;
	ImageView mBackImg,upload;
	ParseImageView memberImage;
	RelativeLayout imageBgLayout;
	Button removeBtn,makeAdminBtn;
	ProgressBar progressBar,progressBar1;
	ParseObject groupObject,memberObject;
	//int position;
	String mMobileNo="",mGroupId="";
	String userMobileNo,userName,userImageUrl,memberImageUrl="";
	boolean isFromFeed;
	FrameLayout imageRootLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.member_profile);
		Utility.getTracker(this, "GROUP INFO - MEMBER PROFILE SCREEN");
		initViews();
		
		if(isFromFeed || !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()) || groupObject.getList(Constants.ADMIN_ARRAY).contains(userMobileNo))
		{
			makeAdminBtn.setVisibility(View.GONE);
			removeBtn.setVisibility(View.GONE);
		}
		
		removeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				removeBtn.setEnabled(false);
				removeFromGroup();
				
			}
		});
		makeAdminBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeAdminBtn.setEnabled(false);
				progressBar.setVisibility(View.VISIBLE);
				MemberListActivity.flag=true;
				List<String> adminList=(List<String>) groupObject.get(Constants.ADMIN_ARRAY);
				//if(adminList.contains(MemberListActivity.userList.get(position).get(Constants.MOBILE_NO).toString()))
				adminList.add(userMobileNo);
				groupObject.saveInBackground(new SaveCallback() {
			          public void done(ParseException e) {
			                 if (e == null) {
			                	 Utility.setGroupObject(groupObject);
			                	 memberObject.put(Constants.GROUP_ADMIN, true);
			                	 memberObject.saveInBackground(new SaveCallback() {
			   			          public void done(ParseException e) {
						                 if (e == null) {
						                	 progressBar.setVisibility(View.GONE);
						                	 Utility.showToastMessage(MemberProfileActivity.this, getResources().getString(R.string.make_admin_done));
						                	 GroupPostListActivity.flag=true;
						                	 NearByGroupListActivity.flag2=true;
	   										 MyGroupListActivity.flag1=true;
						                	 finish();
						                 }
			   			          }});
			                 }
			          }});
				
				
			}
		});
	}

	private void initViews() {
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		progressBar1=(ProgressBar) findViewById(R.id.progressBar1);
		mBackImg=(ImageView) findViewById(R.id.back);
		memberImage=(ParseImageView) findViewById(R.id.member_image);
		memberName=(TextView) findViewById(R.id.member_name);
		memberJoinDate=(TextView) findViewById(R.id.joined_date);
		additionalInfo=(TextView) findViewById(R.id.additional_info);
		additionalInfoLabel=(TextView) findViewById(R.id.additional_info_label);
		imageBgLayout=(RelativeLayout) findViewById(R.id.layout);
		removeBtn=(Button) findViewById(R.id.remove);
		makeAdminBtn=(Button) findViewById(R.id.make_admin);
		//upload=(ImageView) findViewById(R.id.upload);
		imageRootLayout=(FrameLayout) findViewById(R.id.image_root_layout);

		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		removeBtn.setTypeface(tf);
		makeAdminBtn.setTypeface(tf);
		
//		/position=getIntent().getIntExtra("position",0);
		isFromFeed=getIntent().getBooleanExtra("isFromFeed", false);
		userName=getIntent().getStringExtra(Constants.USER_NAME);
		userMobileNo=getIntent().getStringExtra(Constants.MOBILE_NO);
		userImageUrl=getIntent().getStringExtra(Constants.PROFILE_PICTURE);
		
		groupObject=Utility.getGroupObject();
		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		mGroupId=groupObject.getObjectId();
		
		memberName.setText(userName);
		//System.out.println("no :: "+MemberListActivity.userList.get(position).get(Constants.MOBILE_NO).toString()+"  "+mGroupId);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
		query.whereEqualTo(Constants.MEMBER_NO, userMobileNo);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						System.out.println("inside e==null");
						memberObject=object;
						String additional_info=object.get(Constants.ADDITIONAL_INFO_PROVIDED).toString();
						if(additional_info.isEmpty() || additional_info.equals("No Information Available"))
						{
							additionalInfo.setVisibility(View.GONE);
							//additionalInfoLabel.setVisibility(View.GONE);
						}
						else
						{
							//additionalInfoLabel.setVisibility(View.VISIBLE);
							additionalInfo.setText(additional_info);
						}
						
						DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
						String str=format.format(object.getDate(Constants.JOIN_DATE));
						
						memberJoinDate.setText("Joined :  "+str);
						
					}
			}});
		
		memberImage.setParseFile(Utility.getParseFile());
		memberImageUrl=Utility.getParseFile().getUrl();
		memberImage.loadInBackground(new GetDataCallback() {
			@Override
			public void done(byte[] arg0, ParseException arg1) {
				progressBar1.setVisibility(View.GONE);
			}
		});
	
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		imageRootLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imageRootLayout.setEnabled(false);
				try {
					startActivity(new Intent(MemberProfileActivity.this,ImageFullViewActivity.class)
					.putExtra(Constants.GROUP_NAME, groupObject.get(Constants.GROUP_NAME).toString())
					.putExtra(Constants.POST_IMAGE, memberImageUrl));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}				
			}
		});
		
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		imageRootLayout.setEnabled(true);
	}
	
	private void removeFromGroup()
	{
		MemberListActivity.flag=true;
		progressBar.setVisibility(View.VISIBLE);
		if(memberObject!=null)
		{
			memberObject.put(Constants.MEMBER_STATUS, "InActive");
			memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
			memberObject.put(Constants.EXIT_GROUP, true);
			memberObject.put(Constants.EXITED_BY, "Admin");
			memberObject.saveInBackground();
			
		}
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, userMobileNo);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (e == null) 
				{
							List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
								if(myGroupList.contains(mGroupId))
									myGroupList.remove(mGroupId);
								object.put(Constants.MY_GROUP_ARRAY, myGroupList);
								object.increment(Constants.BADGE_POINT, -100);
								object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
								object.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
							                 if (e == null) {
							                	 groupObject.increment(Constants.MEMBER_COUNT, -1);
							                	 List<String> myGroupList=groupObject.getList(Constants.GROUP_MEMBERS);
							                	if(myGroupList.contains(userMobileNo))
													myGroupList.remove(userMobileNo);
							                	groupObject.put(Constants.GROUP_MEMBERS, myGroupList);
							                	groupObject.saveInBackground(new SaveCallback() {
				  							          public void done(ParseException e) {
			 							                 if (e == null) {
			 							                	 Utility.setGroupObject(groupObject);
			 							                	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
			 							                	query.whereEqualTo(Constants.MOBILE_NO, userMobileNo);
			 							                	query.whereEqualTo(Constants.GROUP_ID, mGroupId);
			 							                	
			 							                	query.findInBackground(new FindCallback<ParseObject>() {
			 							      				public void done(List<ParseObject> list, ParseException e) {
			 							      						if (e == null) 
			 							      						{
			 							      							if(list.size() > 0)
			 							      							{
			 							      								for(int i=0;i<list.size();i++)
			 							      								{
			 							      									list.get(i).put(Constants.POST_STATUS, "InActive");
			 							      								}
			 							      								ParseObject.pinAllInBackground(mGroupId,list);
			 							      								ParseObject.saveAllInBackground(list, new SaveCallback() {
		 							   										public void done(ParseException e) {
		 							   											if (e == null) {
		 							   											progressBar.setVisibility(View.GONE);
		 							   											NearByGroupListActivity.flag2=true;
		 							   											MyGroupListActivity.flag1=true;
		 							   											GroupPostListActivity.flag=true;
		 			 					            							finish();
		 							   											}
		 							   										}});
			 							      							}
			 							      						}
			 							      				}
			 							      			});
			 							                 }
			 							                 else
			 							                	progressBar.setVisibility(View.GONE);
				  							          }});
							                	
							                 }
							                else
 							                progressBar.setVisibility(View.GONE);
							          }});
						}
						else
							progressBar.setVisibility(View.GONE);
				
					}
			
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
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
