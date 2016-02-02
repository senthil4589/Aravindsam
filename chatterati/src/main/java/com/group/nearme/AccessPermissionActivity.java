package com.group.nearme;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.onoffswitch.SwitchButton;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class AccessPermissionActivity extends Activity
{
	private ImageView mBackImg;
	private Button mNext;
	private EditText secretCodeEditTxt,additionalInfoEditTxt;
	private ProgressBar mProgressBar;
	ScrollView scrollView;
    String mGroupName="",groupDes="",mMobileNo="",mGroupType="",mGroupId="";
    private TextView secretCodeTxt,additionalInfoTxt;
    ParseObject groupObject;
    LinearLayout secretCodeLayout,additionalInfoLayout;
    SwitchButton secretCodeToggle,additionalInfoToggle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.access_permission);
		Utility.getTracker(this, "PRVATE GROUP ACCESS PERMISSION SCREEN");
		initViews();
		
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		
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

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mNext=(Button) findViewById(R.id.next);
		secretCodeEditTxt=(EditText) findViewById(R.id.secret_code);
		additionalInfoEditTxt=(EditText) findViewById(R.id.additional_info);
		scrollView=(ScrollView) findViewById(R.id.scroll_view);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		secretCodeTxt=(TextView) findViewById(R.id.secret_code_txt);
		additionalInfoTxt=(TextView) findViewById(R.id.additional_info_txt);
		secretCodeLayout=(LinearLayout) findViewById(R.id.secret_code_layout);
		additionalInfoLayout=(LinearLayout) findViewById(R.id.additional_info_layout);
		secretCodeToggle=(SwitchButton) findViewById(R.id.secret_code_toggle);
		additionalInfoToggle=(SwitchButton) findViewById(R.id.additional_info_toggle);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNext.setTypeface(tf);
		
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
				if(PreferenceSettings.getMobileNo().equals(mMobileNo))
				{
					updateValues();
				}
				else
				{
					finish();
				}
			}
		});
	}
	
	private void updateValues()
	{
		final String secretCode=secretCodeEditTxt.getText().toString();
		final String additionalInfo=additionalInfoEditTxt.getText().toString();
		boolean flag1 = false,flag2=false;
		
		if(secretCodeLayout.getVisibility()==View.VISIBLE)
			flag1=true;
		else
			flag2=true;
		
		if(flag1)
		{
			if(secretCode.isEmpty())
				Utility.showToastMessage(AccessPermissionActivity.this, getResources().getString(R.string.secret_code_error));
			else
			{
				 mProgressBar.setVisibility(View.VISIBLE);
				groupObject.put(Constants.SECRET_STATUS, true);
				groupObject.put(Constants.SECRET_CODE, secretCode);
				groupObject.put(Constants.ADDITIONAL_INFO_REQUIRED, false);
				groupObject.put(Constants.INFO_REQUIRED, "");
				groupObject.saveInBackground(new SaveCallback() {
			          public void done(ParseException e) {
			                 if (e == null) {
			                	 mProgressBar.setVisibility(View.GONE);
			                	 NearByGroupListActivity.flag2=true;
			                	 Utility.showToastMessage(AccessPermissionActivity.this, getResources().getString(R.string.secret_code_update_success));
			                	 Utility.setGroupObject(groupObject);
			                	 finish();
			                 }
			          }});
			}
		}
		else
		{
			if(additionalInfo.isEmpty())
				Utility.showToastMessage(AccessPermissionActivity.this, getResources().getString(R.string.additional_info_error));
			else
			{
				mProgressBar.setVisibility(View.VISIBLE);
				groupObject.put(Constants.SECRET_STATUS, false);
				groupObject.put(Constants.SECRET_CODE, "");
				groupObject.put(Constants.ADDITIONAL_INFO_REQUIRED, true);
				groupObject.put(Constants.INFO_REQUIRED, additionalInfo);
				groupObject.saveInBackground(new SaveCallback() {
			          public void done(ParseException e) {
			                 if (e == null) {
			                	 mProgressBar.setVisibility(View.GONE);
			                	 Utility.showToastMessage(AccessPermissionActivity.this, getResources().getString(R.string.additional_info_update_success));
			                	 Utility.setGroupObject(groupObject);
			                	 finish();
			                 }
			          }});
			}
		}
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
