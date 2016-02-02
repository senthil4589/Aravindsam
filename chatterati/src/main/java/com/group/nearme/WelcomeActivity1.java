package com.group.nearme;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class WelcomeActivity1 extends Activity implements OnClickListener{
	//private Button mGetStartTxtView;
	private ViewPager pager;
	private LayoutInflater inflater;
	private ImageView dot1,dot2,dot3,dot4,dot5;
	private static final String ACCOUNT_SID = "AC867b012600c2cea93a1ec999eb88870d";
	private static final String AUTH_TOKEN = "1f65e4a8e3c80cfaeb88f356112dbfef";
	private static final String FROM="+18708980344";
	//private TextView mPincode;
	private Spinner mCountrySpinner;
	private EditText mMobileNoEditTxt;
	private ProgressBar mProgressBar;
	private String mMobileNo="",mCountry="",mOTPCode="1234",pinCode="+91";
	Button goBtn;
	ArrayList<String> mInvitationList=new ArrayList<String>();
	private AuthCallback authCallback;
	ArrayList<String> idList=new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome1);
		Utility.getTracker(this, "MOBILE NO SIGN UP SCREEN");
		initViews();
	
		pager.setAdapter(new TextPagerAdapter());
		pager.setCurrentItem(0);


		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if(position==0)
				{
					dot1.setBackgroundColor(Color.BLACK);
					dot2.setBackgroundColor(Color.WHITE);
					dot3.setBackgroundColor(Color.WHITE);
					dot4.setBackgroundColor(Color.WHITE);
					dot5.setBackgroundColor(Color.WHITE);
					//mGetStartTxtView.setVisibility(View.GONE);
				}
				else if(position==1)
				{
					dot1.setBackgroundColor(Color.WHITE);
					dot2.setBackgroundColor(Color.BLACK);
					dot3.setBackgroundColor(Color.WHITE);
					dot4.setBackgroundColor(Color.WHITE);
					dot5.setBackgroundColor(Color.WHITE);
					//mGetStartTxtView.setVisibility(View.GONE);
				}
				else if(position==2)
				{
					dot1.setBackgroundColor(Color.WHITE);
					dot2.setBackgroundColor(Color.WHITE);
					dot3.setBackgroundColor(Color.BLACK);
					dot4.setBackgroundColor(Color.WHITE);
					dot5.setBackgroundColor(Color.WHITE);
					//mGetStartTxtView.setVisibility(View.GONE);

				}
				else if(position==3)
				{
					dot1.setBackgroundColor(Color.WHITE);
					dot2.setBackgroundColor(Color.WHITE);
					dot3.setBackgroundColor(Color.WHITE);
					dot4.setBackgroundColor(Color.BLACK);
					dot5.setBackgroundColor(Color.WHITE);
					//mGetStartTxtView.setVisibility(View.GONE);
				}
				else
				{
					/*dot1.setBackgroundColor(Color.WHITE);
					dot2.setBackgroundColor(Color.WHITE);
					dot3.setBackgroundColor(Color.WHITE);
					dot4.setBackgroundColor(Color.WHITE);
					dot5.setBackgroundColor(Color.BLACK);*/
					//dot1.setVisibility(visibility);
					//mGetStartTxtView.setVisibility(View.GONE);
				}

				if(position==4)
				{
					dot1.setVisibility(View.GONE);
					dot2.setVisibility(View.GONE);
					dot3.setVisibility(View.GONE);
					dot4.setVisibility(View.GONE);
					//dot5.setVisibility(View.GONE);
				}
				else
				{
					dot1.setVisibility(View.VISIBLE);
					dot2.setVisibility(View.VISIBLE);
					dot3.setVisibility(View.VISIBLE);
					dot4.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		dot1.setOnClickListener(this);
		dot2.setOnClickListener(this);
		dot3.setOnClickListener(this);
		dot4.setOnClickListener(this);
		dot5.setOnClickListener(this);
		//mGetStartTxtView.setOnClickListener(this);

		authCallback = new AuthCallback() {
			@Override
			public void success(DigitsSession session, String phoneNumber) {
				// Do something with the session
				System.out.println("phone number "+phoneNumber);
				System.out.println("getmobil : token : id "+session.getPhoneNumber());
				//new SendOTPAsynTask().execute();
				mMobileNo=phoneNumber;
				mProgressBar.setVisibility(View.VISIBLE);
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
				query.whereEqualTo(Constants.MOBILE_NO, phoneNumber);
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object, ParseException e) {
						mProgressBar.setVisibility(View.GONE);
						if (e == null) {
							if (object != null) {
								object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
								try {
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
									ArrayList<String> list1 = (ArrayList<String>) object.get(Constants.GROUP_INVITATION);
									PreferenceSettings.setGroupInvitationList(list1);
									//mProgressBar.setVisibility(View.GONE);

									ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
									query.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());
									query.whereEqualTo(Constants.INVITATION_STATUS, "Active");
									query.findInBackground(new FindCallback<ParseObject>() {
										public void done(List<ParseObject> list, ParseException e) {
											if (e == null) {
												if (list.size() > 0) {
													for (int i = 0; i < list.size(); i++) {
														idList.add(list.get(i).get(Constants.GROUP_ID).toString());

													}
													startActivity(new Intent(WelcomeActivity1.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id", idList).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
													//overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
													finish();
												}
												else
												{
													startActivity(new Intent(WelcomeActivity1.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id", idList).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
													//overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
													finish();
												}
											}
											else
											{
												startActivity(new Intent(WelcomeActivity1.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id", idList).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
												//overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
												finish();
											}
										}
									});

								}
								catch (Exception e1){
									Toast.makeText(WelcomeActivity1.this, "Please close and try again", Toast.LENGTH_SHORT).show();
								}
							} else {
								//mProgressBar.setVisibility(View.GONE);
								startActivity(new Intent(WelcomeActivity1.this, AboutYouActivity.class).putExtra(Constants.COUNTRY_NAME, "").putExtra(Constants.MOBILE_NO, mMobileNo));
								//overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
							}
						} else {
							//mProgressBar.setVisibility(View.GONE);
							startActivity(new Intent(WelcomeActivity1.this, AboutYouActivity.class).putExtra(Constants.COUNTRY_NAME, "").putExtra(Constants.MOBILE_NO, mMobileNo));
							//overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
						}
					}
				});

			}

			@Override
			public void failure(DigitsException exception) {
				// Do something on failure
			}
		};

	}

	private void initViews() {
		//mGetStartTxtView=(Button) findViewById(R.id.get_started);
		pager=(ViewPager) findViewById(R.id.pager);
		dot1=(ImageView) findViewById(R.id.dot1);
		dot2=(ImageView) findViewById(R.id.dot2);
		dot3=(ImageView) findViewById(R.id.dot3);
		dot4=(ImageView) findViewById(R.id.dot4);
		dot5=(ImageView) findViewById(R.id.dot5);
		inflater=getLayoutInflater();


	}
	

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
	}

	class TextPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 5;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			View imageLayout = null;
			
			if(position==4)
			{
				dot1.setVisibility(View.GONE);
				dot2.setVisibility(View.GONE);
				dot3.setVisibility(View.GONE);
				dot4.setVisibility(View.GONE);
				dot5.setVisibility(View.GONE);
				
				imageLayout = inflater.inflate(R.layout.digits, view,false);
				final DigitsAuthButton digitsButton = (DigitsAuthButton) imageLayout.findViewById(R.id.auth_button);
				mProgressBar=(ProgressBar) imageLayout.findViewById(R.id.progressBar);
				digitsButton.setCallback(authCallback);

				digitsButton.setText("GET STARTED");
				digitsButton.setBackground(getResources().getDrawable(R.drawable.submit_bg));
				digitsButton.setTextColor(Color.WHITE);
				digitsButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						digitsButton.setEnabled(false);
						Digits.authenticate(authCallback, R.style.CustomDigitsTheme);
						//finish();
					}
				});

				/*mMobileNoEditTxt=(EditText) imageLayout.findViewById(R.id.mobile_no);
				goBtn=(Button) imageLayout.findViewById(R.id.next);
				//mPincode=(TextView) imageLayout.findViewById(R.id.pin);
				mCountrySpinner = (Spinner) imageLayout.findViewById(R.id.pincode_spinner);
				mProgressBar=(ProgressBar) imageLayout.findViewById(R.id.progressBar);
				Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf");
				Typeface tf1 = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
				mMobileNoEditTxt.setTypeface(tf1);
				goBtn.setTypeface(tf);
				goBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						 mMobileNo=mMobileNoEditTxt.getText().toString();
						 mCountry = mCountrySpinner.getSelectedItem().toString();
						 
						if(mMobileNo.isEmpty())
						{
							Utility.showToastMessage(WelcomeActivity1.this, getResources().getString(R.string.mobile_no_empty));
							//Toast.makeText(WelcomeActivity1.this, getResources().getString(R.string.mobile_no_empty), Toast.LENGTH_LONG).show();
						}
						else if(!Utility.checkInternetConnectivity(WelcomeActivity1.this))
						{
							Utility.showOkDilaog(WelcomeActivity1.this, getResources().getString(R.string.mobile_no_empty));
						}
						else if(mMobileNo.length()==10)
						{
							mMobileNo=mCountry+mMobileNo;
							mProgressBar.setVisibility(View.VISIBLE);
							new SendOTPAsynTask().execute();
						}
						else
						{
							Utility.showToastMessage(WelcomeActivity1.this, getResources().getString(R.string.mobile_no_invalid));
							//Toast.makeText(WelcomeActivity1.this, "Mobile number should be 10 characters", Toast.LENGTH_LONG).show();
						}
					}
				});
				
				
				// Create an ArrayAdapter using the string array and a default spinner layout
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(WelcomeActivity1.this,
				        R.array.country_code_array, R.layout.new_simple_spinner_item);
				// Specify the layout to use when the list of choices appears
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// Apply the adapter to the spinner
				mCountrySpinner.setAdapter(adapter);
				mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			        public void onItemSelected(AdapterView<?> arg0, View arg1,
			                int position, long arg3) {
			        	if(position==0)
			        		pinCode="+91";
			        	else if(position==1)
			        		pinCode="+1";

			        }
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
			    });
				*/
			}
			
			else
			{
				dot1.setVisibility(View.VISIBLE);
				dot2.setVisibility(View.VISIBLE);
				dot3.setVisibility(View.VISIBLE);
				dot4.setVisibility(View.VISIBLE);
				//dot5.setVisibility(View.VISIBLE);
				imageLayout = inflater.inflate(R.layout.pager_item, view,false);
				ImageView image=(ImageView) imageLayout.findViewById(R.id.image);
				//LinearLayout root=(LinearLayout) imageLayout.findViewById(R.id.root);
				//LayoutParams param=new LayoutParams(getSreenWidth(),getScreenHeight());
				//image.setLayoutParams(param);
				//image.setScaleType(ScaleType.FIT_XY);
				if(position==0)
				{
					//root.setBackgroundColor(Color.parseColor("#4ABECE"));
					image.setImageResource(R.drawable.new_w1);
					//Picasso.with(WelcomeActivity1.this).load(R.drawable.w1).resize(getSreenWidth(), getScreenHeight()).into(image);
				}
				else if(position==1)				{
					//root.setBackgroundColor(Color.parseColor("#FFAA31"));
					image.setImageResource(R.drawable.new_w2);
					//Picasso.with(WelcomeActivity1.this).load(R.drawable.w2).resize(getSreenWidth(), getScreenHeight()).into(image);
				}
				else if(position==2)
				{
					//root.setBackgroundColor(Color.parseColor("#5AC663"));
					image.setImageResource(R.drawable.new_w3);
					//Picasso.with(WelcomeActivity1.this).load(R.drawable.w3).resize(getSreenWidth(), getScreenHeight()).into(image);
				}
				else
				{
					//root.setBackgroundColor(Color.parseColor("#8C3DDE"));
					image.setImageResource(R.drawable.new_w4);
					//Picasso.with(WelcomeActivity1.this).load(R.drawable.w4).resize(getSreenWidth(), getScreenHeight()).into(image);
				}
			}
			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}
		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}
		@Override
		public Parcelable saveState() {
			return null;
		}
		@Override
		public void startUpdate(View container) {
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dot1:
			pager.setCurrentItem(0);
			break;
		case R.id.dot2:
			pager.setCurrentItem(1);
			break;
		case R.id.dot3:
			pager.setCurrentItem(2);
			break;
		case R.id.dot4:
			pager.setCurrentItem(3);
			break;
		case R.id.dot5:
			pager.setCurrentItem(4);
			break;
		/*case R.id.get_started:
			startActivity(new Intent(WelcomeActivity1.this,GetMobileNoActivity.class));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			break;*/
		default:
			break;
		}
		
	}
	
	class SendOTPAsynTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params) {
			int randomPIN = (int)(Math.random()*9000)+1000;
			mOTPCode=String.valueOf(randomPIN);
			//mOTPCode="1234";
			if(mCountry.equals("+1"))
			{
			String url="https://api.twilio.com/2010-04-01/Accounts/"+ACCOUNT_SID+"/SMS/Messages.json";
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			String basicAuth = "Basic " + new String(Base64.encode((ACCOUNT_SID+":"+AUTH_TOKEN).getBytes(),Base64.NO_WRAP ));
			httppost.setHeader("Authorization", basicAuth);
			//httppost.addHeader("UserName", ACCOUNT_SID);
			//httppost.addHeader("Password", AUTH_TOKEN);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("From", FROM));
			nameValuePairs.add(new BasicNameValuePair("To", mMobileNo));
			nameValuePairs.add(new BasicNameValuePair("Body", "Your mobile verification code from Chatterati App is "+mOTPCode+". Do not disclose or share."));
			
			 
			    try {
			    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					StatusLine statusLine = response.getStatusLine();
					System.out.println("result ::: "+statusLine.getStatusCode());
					return true;
				}
				 catch (Exception e) {
					 System.out.println("exception :: "+e);
					 return false;
				 }
			}
			else
			{
				System.out.println("inside elseeee");
				//String url1="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo=+919710577519&SenderID=CHATTR&CDMAHeader=CHATTR&Message=Your%20mobile%20verification%20code%20from%20Chatterati%20App%20is%201234.%20Do%20not%20disclose%20or%20share.";
				String url ="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo="+mMobileNo+"&SenderID=CHATTR&CDMAHeader=CHATTR&Message=Your mobile verification code from Chatterati App is "+mOTPCode+". Do not disclose or share.";
				//String url="http://bhashsms.com/api/sendmsg.php?user=9445163340&pass=9ca58b4&sender=AJVISH&phone="+mMobileNo+"&text=One time password from Chatterati to verify your mobile is "+mOTPCode+"&priority=ndnd&stype=normal";
				HttpClient httpclient = new DefaultHttpClient();
				    try {
				    	 URI uri = new URI(url.replace(" ", "%20"));
						HttpResponse response = httpclient.execute(new HttpGet(uri));
						StatusLine statusLine = response.getStatusLine();
						System.out.println("result ::: "+statusLine.getStatusCode());
						return true;
					}
					 catch (Exception e) {
						 return false;
					 }
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.USER_TABLE);
			query1.whereEqualTo(Constants.MOBILE_NO, mMobileNo);
			query1.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
						if (e == null) 
						{
							if(object!=null)
							{
								object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
							}
						}
				}});
			
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MOBILE_VERIFICATION_TABLE);
			query.whereEqualTo(Constants.MOBILE_NO, mMobileNo);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> totemList, ParseException e) {
						if (e == null) 
						{
							if(totemList.size() > 0) // already exist so update 
							{
								totemList.get(0).put(Constants.VERIFICATION_CODE, mOTPCode);
								totemList.get(0).saveInBackground();
							}
							else
							{
								String country_name="";
								if(mCountry.equals("+91"))
									country_name="India";
								else if(mCountry.equals("+1"))
									country_name="US";
								ParseObject mobileVerificationObject = new ParseObject(Constants.MOBILE_VERIFICATION_TABLE);
								mobileVerificationObject.put(Constants.MOBILE_NO, mMobileNo);
								//mobileVerificationObject.put(Constants.COUNTRY_NAME, country_name);
								mobileVerificationObject.put(Constants.VERIFICATION_CODE, mOTPCode);
								mobileVerificationObject.saveInBackground();
							}
							String country_name="";
							if(mCountry.equals("+91"))
								country_name="India";
							else if(mCountry.equals("+1"))
								country_name="US";
							
							mProgressBar.setVisibility(View.GONE);
							startActivity(new Intent(WelcomeActivity1.this,VerifyMobileNoActivity.class).putExtra(Constants.COUNTRY_NAME, country_name).putExtra(Constants.MOBILE_NO, mMobileNo));
							overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
						else
						{
							Utility.showToastMessage(WelcomeActivity1.this, getResources().getString(R.string.server_issue));
						}
				}
			});
			
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


