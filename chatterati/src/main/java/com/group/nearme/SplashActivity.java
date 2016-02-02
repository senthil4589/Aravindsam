package com.group.nearme;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity{
	ArrayList<String> idList=new ArrayList<>();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		Utility.getTracker(this, "SPLASH SCREEN");
		ParseAnalytics.trackAppOpened(getIntent());
		if(PreferenceSettings.getLoginStatus())
			callTabGroupActivity();
		else
			callWelcomeGroupActivity();
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
		
	
	private void callTabGroupActivity() {
		/*Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

			}
		}, 3000);
*/
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
		query.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());
		query.whereEqualTo(Constants.INVITATION_STATUS, "Active");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null) {
					if (list.size() > 0) {
						for(int i=0;i<list.size();i++)
						{
							idList.add(list.get(i).get(Constants.GROUP_ID).toString());
						}
						startActivity(new Intent(SplashActivity.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id",idList));
						finish();
					}
					else {
						startActivity(new Intent(SplashActivity.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id",idList));
						finish();
					}
				}
				else{
					startActivity(new Intent(SplashActivity.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id",idList));
					finish();
				}
			}
		});
	}
	private void callWelcomeGroupActivity()
	{
		Handler handler=new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(SplashActivity.this,WelcomeActivity1.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				finish();
			}
		},2000);
	}
}
