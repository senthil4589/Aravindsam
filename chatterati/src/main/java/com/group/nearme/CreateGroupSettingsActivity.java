package com.group.nearme;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.model.LatLng;
import com.group.nearme.objects.GroupInfo;
import com.group.nearme.onoffswitch.SwitchButton;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.DateTimePicker;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class CreateGroupSettingsActivity extends Activity
{
	private Button mSaveBtn;
	private ImageView mBackImg;
	private int whoCanPostFlag=0,whoCanCommentFlag=0;
	private ProgressBar mProgressbar;
	private ImageView datePickerImgView;
	private ArrayList<Object> dateTimeList;
	private TextView dateTxtView,monthTxtView,yearTxtView,timeTxtView;
	private Map<String, String> monthMap;
	private RelativeLayout datePickerLayout,calendarLayout;
	private SwitchButton mGroupVisibilityToggle,mCalendarVisibilityToggle;
	Date groupVisibleTillDate=new Date();
	Date ActualDate=new Date();
	boolean flag,flag1;
	
	private ParseGeoPoint mLocationPoint;
	private Double mLatitude,mLongtitude;
	private GPSTracker gpsTracker;
    private RelativeLayout mMembershipApprovalLayout;
    LinearLayout privateGroupPermissionLayout;
    private SwitchButton postApprovalToggle;
    private RelativeLayout postApprovalLayout;
    public GroupInfo groupInfo=new GroupInfo();
    public static Activity activity;
    private SwitchButton whoCanPostToggle,whoCanCommentToggle,membershipApprovalToggle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_group_settings);
		Utility.getTracker(this, "CREATE GROUP - SETTINGS FOR PRIVATE AND OPEN");
		initViews();
		activity=this;
		groupInfo=Utility.getGroupInfo();
		
		if(groupInfo.getGroupType().equals(Constants.PRIVATE_GROUP))
		{
			System.out.println("inside private ");
			privateGroupPermissionLayout.setVisibility(View.VISIBLE);
		}
		else if(groupInfo.getGroupType().equals(Constants.OPEN_GROUP))
		{
			privateGroupPermissionLayout.setVisibility(View.GONE);
		}
		
		membershipApprovalToggle.setChecked(false);
		whoCanCommentToggle.setChecked(false);
		whoCanPostToggle.setChecked(false);
		postApprovalToggle.setChecked(true);
		
		mGroupVisibilityToggle.setChecked(true);
		
		datePickerLayout.setVisibility(View.VISIBLE);
		
		/*mWhoCanPostGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            RadioButton rb=(RadioButton)findViewById(checkedId);
	            whoCanPost=rb.getText().toString();
	            if(whoCanPost.equals("Any member"))
	        		postApprovalLayout.setVisibility(View.VISIBLE);
	        	else
	        		postApprovalLayout.setVisibility(View.GONE);
	           
	        }
	    });
		mWhoCanCommentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            RadioButton rb=(RadioButton)findViewById(checkedId);
	            whoCanComment=rb.getText().toString();
	        }
	    });*/
		
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
		
		
		
		calendarLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openCalendarControl();
			}
		});
		
		mGroupVisibilityToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
	            @Override
	            public void onCheckedChanged(CompoundButton buttonView,
	                    boolean isChecked) {
	               
	            	if(isChecked)
	            	{
	            		datePickerLayout.setVisibility(View.VISIBLE);
 	            	}
	            	else
	            	{
	            		datePickerLayout.setVisibility(View.GONE);
	            		calendarLayout.setVisibility(View.GONE);
	            	}
	            }

	        });
		
		mCalendarVisibilityToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
	            @Override
	            public void onCheckedChanged(CompoundButton buttonView,
	                    boolean isChecked) {
	            	if(isChecked) 
	            	{
	            			openCalendarControl();
	            	}
	            	else
	            	{
	            		calendarLayout.setVisibility(View.GONE);
	            	}
	            }

	        });
	
	}
	
	private void openCalendarControl()
	{
		dateTimeList = DateTimePicker.dateTimePicker(CreateGroupSettingsActivity.this,ActualDate);
		final AlertDialog alertDialog = (AlertDialog) dateTimeList.get(0);
		alertDialog.show();
		final DatePicker datePicker = (DatePicker) dateTimeList.get(1);
		final TimePicker timePicker = (TimePicker) dateTimeList.get(2);
		
		Button doneBtn = (Button) dateTimeList.get(3);
		Button cancelBtn = (Button) dateTimeList.get(4);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!flag1)
					 mCalendarVisibilityToggle.setChecked(false);
				alertDialog.dismiss();
			}
		});
		
		doneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flag1=true;
				alertDialog.dismiss();
				try {
					calendarLayout.setVisibility(View.VISIBLE);
					String monthName = monthMap.get(datePicker.getMonth() + "");
					String year = datePicker.getYear() + "";
					String day = checkDigit(datePicker.getDayOfMonth()) + "";
					
					int hours = timePicker.getCurrentHour();//.toString();
					int minutes = timePicker.getCurrentMinute();//.toString();
					
					 String timeSet = "";
				        if (hours > 12) {
				            hours -= 12;
				            timeSet = "PM";
				        } else if (hours == 0) {
				            hours += 12;
				            timeSet = "AM";
				        } else if (hours == 12)
				            timeSet = "PM";
				        else
				            timeSet = "AM";
				        String min = "";
				        if (minutes < 10)
				        min = "0" + minutes ;
				        else
				        min = String.valueOf(minutes);
				
					monthTxtView.setText(monthName);
					yearTxtView.setText(year);
					dateTxtView.setText(day);
					
					timeTxtView.setText(hours + ":" + min+timeSet);
					
					
					
					groupVisibleTillDate=getDateFromDatePicket(datePicker,timePicker);
					
				}
				catch (Exception e) {
					System.out.println("inside catch date-----------------");
				}
			}});
		
		alertDialog.setOnCancelListener(new OnCancelListener() {

		    public void onCancel(DialogInterface dialog) {
		       if(!flag && !flag1)     
		       {
		    	   mCalendarVisibilityToggle.setChecked(false);
		       }
		    }
		});
	}
	
	
	public static java.util.Date getDateFromDatePicket(DatePicker datePicker,TimePicker time){
	    int day = datePicker.getDayOfMonth();
	    int month = datePicker.getMonth();
	    int year =  datePicker.getYear();

	    Calendar calendar = Calendar.getInstance();
	    calendar.set(year, month, day,time.getCurrentHour(),time.getCurrentMinute());

	    return calendar.getTime();
	}
	
	 private String getTimeState(int hours, int mins) {
         
	        String timeSet = "";
	        if (hours > 12) {
	            hours -= 12;
	            timeSet = "PM";
	        } else if (hours == 0) {
	            hours += 12;
	            timeSet = "AM";
	        } else if (hours == 12)
	            timeSet = "PM";
	        else
	            timeSet = "AM";
	 
	         
	        /*   String minutes = "";
	        if (mins < 10)
	            minutes = "0" + mins;
	        else
	            minutes = String.valueOf(mins);
	 
	     // Append in a StringBuilder
	         String aTime = new StringBuilder().append(hours).append(':')
	                .append(minutes).append(" ").append(timeSet).toString();
	 
	          output.setText(aTime);*/
	        return timeSet;
	    }

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mSaveBtn=(Button) findViewById(R.id.save);
		mProgressbar=(ProgressBar) findViewById(R.id.progressBar);
		
		mMembershipApprovalLayout=(RelativeLayout) findViewById(R.id.membership_approval_layout);
		whoCanPostToggle=(SwitchButton) findViewById(R.id.who_can_post_toggle);
		whoCanCommentToggle=(SwitchButton) findViewById(R.id.who_can_comment_toggle);
		membershipApprovalToggle=(SwitchButton) findViewById(R.id.membership_approval_toggle);
		privateGroupPermissionLayout=(LinearLayout) findViewById(R.id.private_access_permission_layout);
		
		datePickerImgView=(ImageView) findViewById(R.id.date_picker);
		dateTxtView=(TextView) findViewById(R.id.date);
		monthTxtView=(TextView) findViewById(R.id.month);
		yearTxtView=(TextView) findViewById(R.id.year);
		timeTxtView=(TextView) findViewById(R.id.time);
		datePickerLayout=(RelativeLayout) findViewById(R.id.datePickerLayout);
		calendarLayout=(RelativeLayout) findViewById(R.id.calendar_layout);
		mGroupVisibilityToggle=(SwitchButton) findViewById(R.id.group_visibility_toggle);
		mCalendarVisibilityToggle=(SwitchButton) findViewById(R.id.calendar_toggle);
		postApprovalToggle=(SwitchButton) findViewById(R.id.post_approval_toggle);
		postApprovalLayout=(RelativeLayout) findViewById(R.id.post_approval_layout);
		
		mSaveBtn.setTypeface(Utility.getTypeface());
		mSaveBtn.setText("NEXT");
		groupVisibleTillDate=Utility.getFutureDate();
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		
		
		monthMap = new HashMap<String, String>();
		monthMap.put("0", "Jan");
		monthMap.put("1", "Feb");
		monthMap.put("2", "Mar");
		monthMap.put("3", "Apr");
		monthMap.put("4", "May");
		monthMap.put("5", "Jun");
		monthMap.put("6", "Jul");
		monthMap.put("7", "Aug");
		monthMap.put("8", "Sep");
		monthMap.put("9", "Oct");
		monthMap.put("10", "Nov");
		monthMap.put("11", "Dec");
		
	}
	
	public String checkDigit(int number)
    {
        return number<=9?"0"+number:String.valueOf(number);
    }
	
	private void insert()
	{
		
		mSaveBtn.setEnabled(true);
		mProgressbar.setVisibility(View.VISIBLE);
		mLocationPoint=new ParseGeoPoint(mLatitude,mLongtitude);
		
		
		if(groupInfo.getGroupType().equals(Constants.PRIVATE_GROUP))
		{
			groupInfo.setMembershipApproval(membershipApprovalToggle.isChecked());
			groupInfo.setMemberInvitation(true);
		}
		groupInfo.setWhoCanComment(whoCanCommentFlag);
		groupInfo.setWhoCanPost(whoCanPostFlag);
		
		
		groupInfo.setLocationPoint(mLocationPoint);
		
		if(postApprovalLayout.getVisibility()==View.VISIBLE)
			groupInfo.setPostapproval(postApprovalToggle.isChecked());
		else
			groupInfo.setPostapproval(false);
		if(mGroupVisibilityToggle.isChecked())
			groupInfo.setGroupVisibleTilDate(groupVisibleTillDate);
		else
			groupInfo.setGroupVisibleTilDate(Utility.getPastDate());
		
		Utility.setGroupInfo(groupInfo);
		
			Intent i=new Intent(CreateGroupSettingsActivity.this,GroupVisibiltyActivity.class);
			//i.putExtra("GroupInfo", groupInfo);
			startActivity(i);
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
		
		/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, objectId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
		public void done(final ParseObject groupObject, ParseException e) {
		if (e == null) 
			{
			if(groupObject!=null)
			{
							
				groupObject.put(Constants.GROUP_LOCATION, mLocationPoint);
				groupObject.put(Constants.VISIBILITY_RADIUS, mVisibilityRadius);
				if(mGroupType.equals("Private"))
				{
					groupObject.put(Constants.MEMBERSHIP_APPROVAL, mOnlyAdminApproveRadioBtn.isChecked());
					groupObject.put(Constants.MEMBER_INVITATION, true);
				}
				groupObject.put(Constants.GROUP_STATUS, "Active");
				groupObject.put(Constants.WHO_CAN_POST, whoCanPostFlag);
				groupObject.put(Constants.WHO_CAN_COMMENT, whoCanCommentFlag);
				if(postApprovalLayout.getVisibility()==View.VISIBLE)
					groupObject.put(Constants.POST_APPROVAL, postApprovalToggle.isChecked());
				else
					groupObject.put(Constants.POST_APPROVAL, false);
				if(mGroupVisibilityToggle.isChecked())
					groupObject.put(Constants.GROUP_VISIBLE_TILL_DATE, groupVisibleTillDate);
				else
					groupObject.put(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getPastDate());
				groupObject.put(Constants.VISIBILITY, "Nearby");
				groupObject.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	 groupObject.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
	                	 
	                	 ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
		             		memberObject.put(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
		             		memberObject.put(Constants.GROUP_ID, groupObject.getObjectId());
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
	             												myGroupList.add(groupObject.getObjectId());
	             												object.put(Constants.MY_GROUP_ARRAY, myGroupList);
	             												object.increment(Constants.BADGE_POINT, 1000);
	             												object.saveInBackground(new SaveCallback() {
							      							          public void done(ParseException e) {
								 							                 if (e == null) {
								 							                	 MyGroupListActivity.flag1=true;
								 							                	mProgressbar.setVisibility(View.GONE);
								 							                	Utility.showToastMessage(CreateGroupSettingsActivity.this, getResources().getString(R.string.create_group_success));
					             												Intent intent = new Intent(CreateGroupSettingsActivity.this,TabGroupPostActivity.class);
					             												Utility.setGroupObject(groupObject);
					             												startActivity(intent);
					             												GroupVisibiltyActivity.activity.finish();
					             												finish();
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
	}
}});
*/}
	
	
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
