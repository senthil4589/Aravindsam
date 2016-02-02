package com.group.nearme;

import android.app.Activity;
import android.app.Notification.Action;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.util.Utility;

public class PolicyGuideLineActivity extends Activity{
	
	Button ok;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.policy_guidelines);
		Utility.getTracker(this, "POLICY AND GUIDELINES SCREEN");
		initViews();
	}

	private void initViews() {
		ok=(Button) findViewById(R.id.ok);
		
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
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
