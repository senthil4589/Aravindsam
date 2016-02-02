package com.group.nearme;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Utility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class StarRatingActivity extends Activity{
	private ImageView mBackImg;
	private NetworkImageView userImage;
	private TextView userName;
	private ImageLoader imageLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_badge);
		Utility.getTracker(this, "STAR RATING SCREEN");
		initViews();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
		
		
		userImage=(NetworkImageView) findViewById(R.id.user_image);
		userName=(TextView) findViewById(R.id.user_name);
		
		userImage.setImageUrl(PreferenceSettings.getProfilePic(), imageLoader);
		imageLoader.get((PreferenceSettings.getProfilePic()), ImageLoader.getImageListener(
				userImage, R.drawable.group_image, 0));
		userName.setText(PreferenceSettings.getUserName());
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
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
