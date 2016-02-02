package com.group.nearme;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.group.nearme.onoffswitch.SwitchButton;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AppSettingsActivity extends Activity{
	private SwitchButton mPushNotificationToggle,mSoundNotificationToggle;
	private ImageView mBackImg;
	private Button saveBtn;
	private ProgressBar progressBar;
	Tracker tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_settings);
		tracker=Utility.getTracker(this, "APP SETTINGS SCREEN");
		initViews();
		
		/*ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupFeedNew");
		query.whereEqualTo(Constants.CREDIT, "Jus'Trufs");
	
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (object != null) 
					{
						System.out.println("inside object");
						HashMap<String, String> map=(HashMap<String, String>) object.get("PostAttribute");
						System.out.println("map object ::: "+map);
						
						System.out.println("start date ::: "+map.get("start"));
						System.out.println("end date ::: "+map.get("end"));
						System.out.println("address ::: "+map.get("address"));
						System.out.println("name ::: "+map.get("name"));
					}
			}
		});*/
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				updateTable();
			}
		});
	}

	private void initViews() {
		saveBtn=(Button) findViewById(R.id.update);
		mBackImg=(ImageView) findViewById(R.id.back);
		mPushNotificationToggle=(SwitchButton) findViewById(R.id.push_notification_toggle);
		mSoundNotificationToggle=(SwitchButton) findViewById(R.id.sound_notification_toggle);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		mPushNotificationToggle.setChecked(PreferenceSettings.getPushStatus());
		mSoundNotificationToggle.setChecked(PreferenceSettings.getSoundStatus());
		
	}

	private void updateTable()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (object != null) 
					{
						object.put(Constants.PUSH_NOTIFICATION, mPushNotificationToggle.isChecked());
						object.put(Constants.SOUND_NOTIFICATION, mSoundNotificationToggle.isChecked());
						object.saveInBackground();
						object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
						PreferenceSettings.setPushStatus(mPushNotificationToggle.isChecked());
						PreferenceSettings.setSoundStatus(mSoundNotificationToggle.isChecked());
						 Utility.showToastMessage(AppSettingsActivity.this, getResources().getString(R.string.push_notification_settings_update_success));
						progressBar.setVisibility(View.GONE);
					}
					else
						progressBar.setVisibility(View.GONE);
			}
			});
		
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
