package com.group.nearme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.util.Utility;

public class CreateGroupActivity extends Activity implements OnClickListener{
	private RelativeLayout mPublicGroup,mPrivateGroup,mSecretGroup;
	private ImageView mBackImg;
	//Button create1,create2,create3;
	Button createBtn;
	public static Activity activity;
	RadioButton openGroupRadioBtn,privateGroupRadioBtn,secretGroupRadioBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_group);
		//Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
		mPublicGroup.setOnClickListener(this);
		mPrivateGroup.setOnClickListener(this);
		mSecretGroup.setOnClickListener(this);
		createBtn.setOnClickListener(this);
		//create1.setOnClickListener(this);
		//create2.setOnClickListener(this);
		//create3.setOnClickListener(this);
		mBackImg.setOnClickListener(this);
		activity=this;
		
		openGroupRadioBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					//openGroupRadioBtn.setChecked(false);
					privateGroupRadioBtn.setChecked(false);
					secretGroupRadioBtn.setChecked(false);
				}
			}
		});
		

		privateGroupRadioBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					openGroupRadioBtn.setChecked(false);
					//privateGroupRadioBtn.setChecked(false);
					secretGroupRadioBtn.setChecked(false);
				}
			}
		});
		

		secretGroupRadioBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					openGroupRadioBtn.setChecked(false);
					privateGroupRadioBtn.setChecked(false);
					//secretGroupRadioBtn.setChecked(false);
				}
			}
		});
		
	
}

	private void initViews() {
		mPublicGroup=(RelativeLayout) findViewById(R.id.public_group_layout);
		mPrivateGroup=(RelativeLayout) findViewById(R.id.private_group_layout);
		mSecretGroup=(RelativeLayout) findViewById(R.id.secret_group_layout);
		openGroupRadioBtn=(RadioButton) findViewById(R.id.open_radio_button);
		privateGroupRadioBtn=(RadioButton) findViewById(R.id.private_radio_button);
		secretGroupRadioBtn=(RadioButton) findViewById(R.id.secret_radio_button);
		mBackImg=(ImageView) findViewById(R.id.back);
		createBtn=(Button) findViewById(R.id.go);
		//create1=(Button) findViewById(R.id.create1);
		//create2=(Button) findViewById(R.id.create2);
		//create3=(Button) findViewById(R.id.create3);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		createBtn.setTypeface(tf);
		//create1.setTypeface(tf);
		//create2.setTypeface(tf);
		//create3.setTypeface(tf);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.public_group_layout:
			openGroupRadioBtn.setChecked(true);
			privateGroupRadioBtn.setChecked(false);
			secretGroupRadioBtn.setChecked(false);
			break;
			
		case R.id.private_group_layout:
			openGroupRadioBtn.setChecked(false);
			privateGroupRadioBtn.setChecked(true);
			secretGroupRadioBtn.setChecked(false);
			break;
			
		case R.id.secret_group_layout:
			openGroupRadioBtn.setChecked(false);
			privateGroupRadioBtn.setChecked(false);
			secretGroupRadioBtn.setChecked(true);
			break;
		case R.id.go:
			if(openGroupRadioBtn.isChecked())
			{
				startActivity(new Intent(CreateGroupActivity.this,CreatePublicGroupActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
			else if(privateGroupRadioBtn.isChecked())
			{
				startActivity(new Intent(CreateGroupActivity.this,CreatePrivateGroupActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
			else if(secretGroupRadioBtn.isChecked())
			{
				startActivity(new Intent(CreateGroupActivity.this,CreateSecretGroupActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
			else
			{
				Utility.showToastMessage(CreateGroupActivity.this, getResources().getString(R.string.create_group_empty_selection));
			}
			break;
		case R.id.back:
			finish();
			overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );

		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
	}
	
	@Override 
	public void finish() { 
	super.finish(); 
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
