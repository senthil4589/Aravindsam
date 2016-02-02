package com.group.nearme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.onoffswitch.SwitchButton;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.DateTimePicker;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class GroupSettingsActivity extends Activity
{
	private String whoCanPost="",whoCanComment="";
	private Button mSaveBtn;
	private ImageView mBackImg;
	private int whoCanPostFlag=0,whoCanCommentFlag=0;
	private ParseObject groupObject;
	private ProgressBar mProgressbar;
	private ArrayList<Object> dateTimeList;
	private TextView dateTxtView,monthTxtView,yearTxtView,timeTxtView;
	private Map<String, String> monthMap;
	private RelativeLayout datePickerLayout,calendarLayout;
	private SwitchButton mGroupVisibilityToggle,mCalendarVisibilityToggle;
	Date groupVisibleTillDate=new Date();
	Date ActualDate=new Date();
	boolean flag,flag1;
	
	private LinearLayout secretCodeLayout,additionalInfoLayout,privateGroupSettingsLayout;
    private SwitchButton secretCodeToggle,additionalInfoToggle;
    private EditText secretCodeEditTxt,additionalInfoEditTxt;
    private String mGroupName="",groupDes="",mMobileNo="",mGroupType="",mGroupId="";
   // private RadioGroup mMemberApprovalRadioGroup;
    //private RadioButton mAnyMemberApproveRadioBtn,mOnlyAdminApproveRadioBtn;
    private SwitchButton postApprovalToggle;
    private RelativeLayout postApprovalLayout;
    private LinearLayout additionalSettingsLayout;
    
    private SwitchButton whoCanPostToggle,whoCanCommentToggle,membershipApprovalToggle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_settings);
		Utility.getTracker(this,"GROUP INFO - GROUP SETTINGS SCREEN");
		initViews();
		
		groupObject=Utility.getGroupObject();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		// for private groups only
		if(mGroupType.equals("Private"))
		{
			System.out.println("inside private ");
			privateGroupSettingsLayout.setVisibility(View.VISIBLE);
			additionalSettingsLayout.setVisibility(View.VISIBLE);
			updatePrivateGroupSettings();
		}
		
		else if(mGroupType.equals("Open"))
		{
			privateGroupSettingsLayout.setVisibility(View.GONE);
			additionalSettingsLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			additionalSettingsLayout.setVisibility(View.GONE);
		}
		
		postApprovalToggle.setChecked(groupObject.getBoolean(Constants.POST_APPROVAL));
		
		whoCanPostFlag=groupObject.getInt(Constants.WHO_CAN_POST);
		whoCanCommentFlag=groupObject.getInt(Constants.WHO_CAN_COMMENT);
		
		if(groupObject.getInt(Constants.WHO_CAN_POST)==0)
		{
			whoCanPostToggle.setChecked(false);
			postApprovalLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			whoCanPostToggle.setChecked(true);
			postApprovalLayout.setVisibility(View.GONE);
		}
		
		if(groupObject.getInt(Constants.WHO_CAN_COMMENT)==0)
			whoCanCommentToggle.setChecked(false);
		else
			whoCanCommentToggle.setChecked(true);
		
		Date d=groupObject.getDate(Constants.GROUP_VISIBLE_TILL_DATE);
		
		ActualDate=d;
		
		//timeTxtView.setText(cal.get(cal.get(Calendar.HOUR)) + ":" + cal.get(Calendar.MINUTE));
		
		int status=d.compareTo(new Date());
		System.out.println("Date :: "+status);
		if(status > 0)
		{
			
			mGroupVisibilityToggle.setChecked(true);
			datePickerLayout.setVisibility(View.VISIBLE);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(Utility.getFutureDate());
			String year1 = (String) android.text.format.DateFormat.format("yyyy", Utility.getFutureDate()); //2013
			String year = (String) android.text.format.DateFormat.format("yyyy", d); //2013
			System.out.println("year && Yaer1 "+year+"  "+year1);
			if(!year.equals(year1))
			{
				System.out.println("inside if----------------");
				flag=true;
				mCalendarVisibilityToggle.setChecked(true);
				calendarLayout.setVisibility(View.VISIBLE);
			//System.out.println("month :::"+""+cal.get(Calendar.MONTH));
			String month = (String) android.text.format.DateFormat.format("MMM", d);
			
			String day = (String) android.text.format.DateFormat.format("dd", d); //20
			String hours = (String) android.text.format.DateFormat.format("hh", d); //20
			String minutes = (String) android.text.format.DateFormat.format("mm", d); //20
			String state = (String) android.text.format.DateFormat.format("aa", d); //20
			
			
			
			monthTxtView.setText(month);
			yearTxtView.setText(year);
			dateTxtView.setText(day);
			
			timeTxtView.setText(hours+":"+minutes+state.toUpperCase());
			}
			else
			{
				System.out.println("inside else----------------");
			}
		}
		else
		{
			mGroupVisibilityToggle.setChecked(false);
			datePickerLayout.setVisibility(View.GONE);
		}
		
	/*	mWhoCanPostGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            // checkedId is the RadioButton selected
	            RadioButton rb=(RadioButton)findViewById(checkedId);
	            whoCanPost=rb.getText().toString();
	            
	        	if(whoCanPost.equals("Any member"))
	        	{
	        		whoCanPostFlag=0;
	        		postApprovalLayout.setVisibility(View.VISIBLE);
	        	}
	        	else
	        	{
	        		whoCanPostFlag=1;
	        		postApprovalLayout.setVisibility(View.GONE);
	        	}
	        }
	    });
		mWhoCanCommentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            // checkedId is the RadioButton selected
	            RadioButton rb=(RadioButton)findViewById(checkedId);
	            whoCanComment=rb.getText().toString();
	            

				if(whoCanComment.equals("Any member"))
					whoCanCommentFlag=0;
				else
					whoCanCommentFlag=1;
	           
	        }
	    });
		
*/		whoCanPostToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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
				
				
				
				
				
				// for private groups
				if(mGroupType.equals("Private"))
				{
				final String secretCode=secretCodeEditTxt.getText().toString();
				final String additionalInfo=additionalInfoEditTxt.getText().toString();
				boolean flag1 = false;
				
				if(secretCodeLayout.getVisibility()==View.VISIBLE)
					flag1=true;
				if(flag1)
				{
					if(secretCode.isEmpty())
						Utility.showToastMessage(GroupSettingsActivity.this, getResources().getString(R.string.secret_code_error));
					else
						updateSettings(flag1,secretCode,additionalInfo);
				}
				else if(additionalInfoLayout.getVisibility()==View.VISIBLE)
				{
					if(additionalInfo.isEmpty())
						Utility.showToastMessage(GroupSettingsActivity.this, getResources().getString(R.string.additional_info_error));
					else
						updateSettings(flag1,secretCode,additionalInfo);
				}
				else
				{
					updateSettings(false,"","");
				}
				}
				else
				{
					updateSettings(false,"","");
				}
				
			}
		});
		
		
		
		calendarLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 //showDialog(DATE_PICKER_ID);
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
	            		if(flag)
	            			calendarLayout.setVisibility(View.VISIBLE);
	            	}
	            	else
	            	{
	            		calendarLayout.setVisibility(View.GONE);
	            		System.out.println("inside mGroupVisibilityToggle off ");
	            		datePickerLayout.setVisibility(View.INVISIBLE);
	            		
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
	            		if(!flag)
	            			openCalendarControl();
	            		else
	            			calendarLayout.setVisibility(View.VISIBLE);
	            	}
	            	else
	            	{
	            		calendarLayout.setVisibility(View.GONE);
	            	}
	            }

	        });
	
	}
	
	private void updateSettings(final boolean flag,final String secretCode,final String additionalInfo)
	{
		mSaveBtn.setEnabled(false);
		mProgressbar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, groupObject.getObjectId());
		query.fromPin(Constants.MY_GROUP_LOCAL_DATA_STORE);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							object.put(Constants.WHO_CAN_POST, whoCanPostFlag);
							object.put(Constants.WHO_CAN_COMMENT, whoCanCommentFlag);
							if(postApprovalLayout.getVisibility()==View.VISIBLE)
								object.put(Constants.POST_APPROVAL, postApprovalToggle.isChecked());
							else
								object.put(Constants.POST_APPROVAL, false);
							if(mGroupVisibilityToggle.isChecked())
								object.put(Constants.GROUP_VISIBLE_TILL_DATE, groupVisibleTillDate);
							else
								object.put(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getPastDate());
							
							if(mGroupType.equals("Private"))
							{
								object.put(Constants.MEMBERSHIP_APPROVAL, membershipApprovalToggle.isChecked());
								if(flag)
								{
									object.put(Constants.SECRET_STATUS, true);
									object.put(Constants.SECRET_CODE, secretCode);
									object.put(Constants.ADDITIONAL_INFO_REQUIRED, false);
									object.put(Constants.INFO_REQUIRED, "");
								}
								else if(additionalInfoLayout.getVisibility()==View.VISIBLE)
								{
									object.put(Constants.SECRET_STATUS, false);
									object.put(Constants.SECRET_CODE, "");
									object.put(Constants.ADDITIONAL_INFO_REQUIRED, true);
									object.put(Constants.INFO_REQUIRED, additionalInfo);
								}
							}
							
							object.saveInBackground(new SaveCallback() {
								
								@Override
								public void done(ParseException arg0) {
									mProgressbar.setVisibility(View.GONE);
									if(mGroupType.equals("Private"))
										 NearByGroupListActivity.flag2=true;
									GroupPostListActivity.flag=true;
									Utility.setGroupObject(object);
									Utility.showToastMessage(GroupSettingsActivity.this, getResources().getString(R.string.group_settings_saved));
									finish();
								}
							});
						}
					}
			}});
	}
	
	// only for private groups
	private void updatePrivateGroupSettings()
	{
		
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		
		boolean membershipApproval=groupObject.getBoolean(Constants.MEMBERSHIP_APPROVAL);
		membershipApprovalToggle.setChecked(membershipApproval);
		
		secretCodeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if(isChecked)
		        {
					secretCodeLayout.setVisibility(View.VISIBLE);
					//secretCodeTxt.setBackgroundResource(R.drawable.txt_view_down);
					//additionalInfoTxt.setBackgroundResource(R.drawable.txt_view_up);
					additionalInfoLayout.setVisibility(View.GONE);
					additionalInfoToggle.setChecked(false);
				}
				else
				{
					//secretCodeTxt.setBackgroundResource(R.drawable.txt_view_up);
					secretCodeLayout.setVisibility(View.GONE);
					additionalInfoLayout.setVisibility(View.VISIBLE);
					additionalInfoToggle.setChecked(true);
					//additionalInfoTxt.setBackgroundResource(R.drawable.txt_view_down);
				}
				
			}
		});
		
		additionalInfoToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if(isChecked)
		        {
					additionalInfoLayout.setVisibility(View.VISIBLE);
					//additionalInfoTxt.setBackgroundResource(R.drawable.txt_view_down);
					//secretCodeTxt.setBackgroundResource(R.drawable.txt_view_up);
					secretCodeLayout.setVisibility(View.GONE);
					secretCodeToggle.setChecked(false);
					
				}
				else
				{
					//additionalInfoTxt.setBackgroundResource(R.drawable.txt_view_up);
					additionalInfoLayout.setVisibility(View.GONE);
					secretCodeLayout.setVisibility(View.VISIBLE);
					secretCodeToggle.setChecked(true);
					//secretCodeTxt.setBackgroundResource(R.drawable.txt_view_down);
				}
				
			}
		});
		
		if(groupObject.getBoolean(Constants.SECRET_STATUS))
		{
			//secretCodeTxt.setBackgroundResource(R.drawable.txt_view_up);
			secretCodeLayout.setVisibility(View.VISIBLE);
			additionalInfoLayout.setVisibility(View.GONE);
			//additionalInfoTxt.setBackgroundResource(R.drawable.txt_view_down);
			secretCodeEditTxt.setText(groupObject.get(Constants.SECRET_CODE).toString());
			secretCodeToggle.setChecked(true);
			additionalInfoToggle.setChecked(false);
		}
		else if(groupObject.getBoolean(Constants.ADDITIONAL_INFO_REQUIRED))
		{
			//additionalInfoTxt.setBackgroundResource(R.drawable.txt_view_up);
			additionalInfoLayout.setVisibility(View.VISIBLE);
			secretCodeLayout.setVisibility(View.GONE);
			//secretCodeTxt.setBackgroundResource(R.drawable.txt_view_down);
			additionalInfoEditTxt.setText(groupObject.get(Constants.INFO_REQUIRED).toString());
			secretCodeToggle.setChecked(false);
			additionalInfoToggle.setChecked(true);
		}
		else
		{
			secretCodeToggle.setChecked(false);
			additionalInfoToggle.setChecked(false);
			additionalInfoLayout.setVisibility(View.GONE);
			secretCodeLayout.setVisibility(View.GONE);
		}
	}
	
	private void openCalendarControl()
	{
		dateTimeList = DateTimePicker.dateTimePicker(GroupSettingsActivity.this,ActualDate);
		final AlertDialog alertDialog = (AlertDialog) dateTimeList.get(0);
		alertDialog.show();
		final DatePicker datePicker = (DatePicker) dateTimeList.get(1);
		final TimePicker timePicker = (TimePicker) dateTimeList.get(2);
		
		Button doneBtn = (Button) dateTimeList.get(3);
		Button cancelBtn = (Button) dateTimeList.get(4);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("Flag :: "+flag);
				if(!flag)
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
	 
	        return timeSet;
	    }

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mSaveBtn=(Button) findViewById(R.id.save);
		mProgressbar=(ProgressBar) findViewById(R.id.progressBar);
		
		whoCanPostToggle=(SwitchButton) findViewById(R.id.who_can_post_toggle);
		whoCanCommentToggle=(SwitchButton) findViewById(R.id.who_can_comment_toggle);
		membershipApprovalToggle=(SwitchButton) findViewById(R.id.membership_approval_toggle);
		
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
		// for private groups
		secretCodeEditTxt=(EditText) findViewById(R.id.secret_code);
		additionalInfoEditTxt=(EditText) findViewById(R.id.additional_info);
		
		privateGroupSettingsLayout=(LinearLayout) findViewById(R.id.private_access_permission_layout);
		secretCodeLayout=(LinearLayout) findViewById(R.id.secret_code_layout);
		additionalInfoLayout=(LinearLayout) findViewById(R.id.additional_info_layout);
		secretCodeToggle=(SwitchButton) findViewById(R.id.secret_code_toggle);
		additionalInfoToggle=(SwitchButton) findViewById(R.id.additional_info_toggle);
		//mMemberApprovalRadioGroup=(RadioGroup) findViewById(R.id.membership_approval);
		//mAnyMemberApproveRadioBtn=(RadioButton) findViewById(R.id.any_member_approve);
		//mOnlyAdminApproveRadioBtn=(RadioButton) findViewById(R.id.only_admin_approve);
		additionalSettingsLayout=(LinearLayout) findViewById(R.id.additional_settings_layout);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		//mAnyMemberPostRadioBtn.setTypeface(tf);
		//mOnlyAdminPostRadioBtn.setTypeface(tf);
		//mAnyMemberCommentRadioBtn.setTypeface(tf);
		//mOnlyAdminCommentRadioBtn.setTypeface(tf);
		//mOnlyAdminApproveRadioBtn.setTypeface(tf);
		//mAnyMemberApproveRadioBtn.setTypeface(tf);
		
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
