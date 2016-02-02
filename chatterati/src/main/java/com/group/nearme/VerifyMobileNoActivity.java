package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.WelcomeActivity1.SendOTPAsynTask;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class VerifyMobileNoActivity extends Activity {
	private Button mNextTxtView;
	private EditText mOTPEditTxt;
	private ProgressBar mProgressBar;
	private String mMobileNo="",mCountryName="";
	private TextView notGetOtp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_verification);
		Utility.getTracker(this, "OTP SCREEN FOR MOBILE NO VERIFICATION");
		initViews();
		mNextTxtView.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				final String code=mOTPEditTxt.getText().toString();
				  if(!code.isEmpty()){
					mProgressBar.setVisibility(View.VISIBLE);
					IBinder token = mOTPEditTxt.getWindowToken();
					( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MOBILE_VERIFICATION_TABLE);
		            query.whereEqualTo(Constants.MOBILE_NO, mMobileNo);
		            query.findInBackground(new FindCallback<ParseObject>() {
		                public void done(List<ParseObject> list, ParseException e) {
		                    if (e == null) {
		                        if(list.size() > 0)
		                        {
		                        	String str=(String) list.get(0).get(Constants.VERIFICATION_CODE);
		                        	if(str.equals(code))
		                        	{
		                        		
		                        		
		                        		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		    							query.whereEqualTo(Constants.MOBILE_NO, mMobileNo);
		    							query.getFirstInBackground(new GetCallback<ParseObject>() {
		    								public void done(ParseObject object, ParseException e) {
		    										if (e == null) 
		    										{
		    											if(object!=null)
		    											{
		    												object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
		    												PreferenceSettings.setUserName(object.get(Constants.USER_NAME).toString());
		    												PreferenceSettings.setUserID(object.getObjectId());
		    												PreferenceSettings.setCountry(object.get(Constants.COUNTRY_NAME).toString());
		    												PreferenceSettings.setMobileNo(object.get(Constants.MOBILE_NO).toString());
		    												PreferenceSettings.setGender(object.get(Constants.GENDER).toString());
		    												PreferenceSettings.setProfilePic(object.getParseFile(Constants.PROFILE_PICTURE).getUrl());
		    												PreferenceSettings.setUserState(object.get(Constants.USER_STATE).toString());
		    												PreferenceSettings.setLoginStatus(true);
		    												PreferenceSettings.setPushStatus(object.getBoolean(Constants.PUSH_NOTIFICATION));
		    												PreferenceSettings.setSoundStatus(object.getBoolean(Constants.SOUND_NOTIFICATION));
		    												PreferenceSettings.setNameChangeCount(object.getInt(Constants.NAME_CHANGE_COUNT));
		    												PreferenceSettings.setUserScore(object.getInt(Constants.BADGE_POINT));
		    												@SuppressWarnings("unchecked")
		    												ArrayList<String> list1=(ArrayList<String>) object.get(Constants.GROUP_INVITATION);
		    												PreferenceSettings.setGroupInvitationList(list1);
		    												mProgressBar.setVisibility(View.GONE);
		    												startActivity(new Intent(VerifyMobileNoActivity.this,TabGroupActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
		    												overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
		    												finish();
		    											}
		    											else
		    											{
		    												mProgressBar.setVisibility(View.GONE);
		    												startActivity(new Intent(VerifyMobileNoActivity.this,AboutYouActivity.class).putExtra(Constants.COUNTRY_NAME, mCountryName).putExtra(Constants.MOBILE_NO, mMobileNo));
		    				            					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
		    											}
		    										}
		    										else
		    										{
		    											mProgressBar.setVisibility(View.GONE);
		    											startActivity(new Intent(VerifyMobileNoActivity.this,AboutYouActivity.class).putExtra(Constants.COUNTRY_NAME, mCountryName).putExtra(Constants.MOBILE_NO, mMobileNo));
	    				            					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
		    										}
		    								}
		    				           });
		                        		
		                        		
		                        		
		                        	}
		                        	else
		                        	{
		                        		mProgressBar.setVisibility(View.GONE);
		                        		Utility.showToastMessage(VerifyMobileNoActivity.this, getResources().getString(R.string.verification_code_incorrect));
		                        		//Toast.makeText(VerifyMobileNoActivity.this, "You are entered wrong code", Toast.LENGTH_LONG).show();
		                        	}
		        				}
		                        else
		                        {
		                        	Utility.showToastMessage(VerifyMobileNoActivity.this, getResources().getString(R.string.server_issue));
		                        	mProgressBar.setVisibility(View.GONE);
		                        }
            				}
		                    else
	                        {
	                        	Utility.showToastMessage(VerifyMobileNoActivity.this, getResources().getString(R.string.server_issue));
	                        	mProgressBar.setVisibility(View.GONE);
	                        }
		                }
            		});
				  }
				  else
				  {
                      	Utility.showToastMessage(VerifyMobileNoActivity.this, getResources().getString(R.string.verification_code_empty));
					  //Toast.makeText(VerifyMobileNoActivity.this, "Please enter your OTP", Toast.LENGTH_LONG).show();
				  }
				
			}
		});
		
	}

	private void initViews() {
		mNextTxtView=(Button) findViewById(R.id.next);
		mOTPEditTxt=(EditText) findViewById(R.id.security_code);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		notGetOtp=(TextView) findViewById(R.id.not_get_otp);
		mMobileNo=getIntent().getStringExtra(Constants.MOBILE_NO);
		mCountryName=getIntent().getStringExtra(Constants.COUNTRY_NAME);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf");
		Typeface tf1 = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNextTxtView.setTypeface(tf1);
		mOTPEditTxt.setTypeface(tf);
		
		notGetOtp.setPaintFlags(notGetOtp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		
		notGetOtp.setOnClickListener(new View.OnClickListener() {
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
