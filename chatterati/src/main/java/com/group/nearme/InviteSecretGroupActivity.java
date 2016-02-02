package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.services.SendInvitationIndia;
import com.group.nearme.services.SendInvitationUS;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class InviteSecretGroupActivity extends Activity{
	LinearLayout layout;
	Button add,send;
	private TextView title; //mPincode
	private Spinner mCountrySpinner;
	String mCountry="",mMobileNo="";
	EditText mobileNo;
	ProgressBar progressBar;
	private ImageView mBackImg;
	private String mGroupId="",mGroupName="",mGroupType="";
	Typeface tf;
	ParseObject groupObject;
	ArrayList<String> list,list1,list2;
	String indiaNumber="",usNumber="";
	private GPSTracker gpsTracker;
	List<String> memberList;
	ParseGeoPoint mLocationPoint;
	boolean isAlreadyMember;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.invite_secret_group);
		Utility.getTracker(this, "INVITE MEMBER - ENTER MOBILE NO SCREEN");
		initViews();
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		memberList=groupObject.getList(Constants.GROUP_MEMBERS);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.country_code_array, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mCountrySpinner.setAdapter(adapter);
		inviteMembers();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		layout=(LinearLayout) findViewById(R.id.layout);
		mobileNo=(EditText) findViewById(R.id.mobile_no);
		add=(Button) findViewById(R.id.add);
		send=(Button) findViewById(R.id.send);
		//mPincode=(TextView) findViewById(R.id.pin);
		mCountrySpinner = (Spinner) findViewById(R.id.pincode_spinner);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		title=(TextView) findViewById(R.id.title);
		gpsTracker = new GPSTracker(this);
		tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		add.setTypeface(tf);
		send.setTypeface(tf);
		mobileNo.setTypeface(tf);
		
		list1=new ArrayList<String>();
		list2=new ArrayList<String>();
		list=new ArrayList<String>();
		
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
	
	private void inviteMembers()
	{
		add.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mMobileNo=mobileNo.getText().toString();
				 mCountry = mCountrySpinner.getSelectedItem().toString();
				
				 String no="";
				 no=mCountry+mMobileNo;
				 if(mMobileNo.isEmpty())
				 {
					 Toast toast = Toast.makeText(InviteSecretGroupActivity.this, getResources().getString(R.string.mobile_no_empty), Toast.LENGTH_LONG);
					 toast.setGravity(Gravity.CENTER, 0, 0);
					 toast.show();
				 }
				 else if(mMobileNo.length()!=10)
				 {
					 Toast toast = Toast.makeText(InviteSecretGroupActivity.this, getResources().getString(R.string.mobile_no_invalid), Toast.LENGTH_LONG);
					 toast.setGravity(Gravity.CENTER, 0, 0);
					 toast.show();
				 }
				 else
				 {
					System.out.println("inside add click");
					if(no.equals(groupObject.get(Constants.MOBILE_NO).toString()) || memberList.contains(no))
					{
						isAlreadyMember=true;
					}
					else
					{
					if(mCountry.equals("+91"))
					{
						String str="+91"+mMobileNo;
						list1.add(str);
						if(indiaNumber.isEmpty())
						{
							indiaNumber=str;
						}
						else
						{
							indiaNumber=indiaNumber+","+str;
						}
						
					}
					else
					{
						String str="+1"+mMobileNo;
						list2.add(str);
						if(usNumber.isEmpty())
						{
							usNumber=str;
						}
						else
						{
							usNumber=usNumber+","+str;
						}
					}
					}
						RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
						RelativeLayout relativeLayout=new RelativeLayout(InviteSecretGroupActivity.this);
						relativeLayout.setLayoutParams(param);
						relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
						RelativeLayout.LayoutParams param1=new RelativeLayout.LayoutParams(mobileNo.getWidth(),mobileNo.getHeight());
						//param1.topMargin=dpToPx(5);
						param1.rightMargin=dpToPx(10);
						param1.leftMargin=dpToPx(5);
						param1.addRule(RelativeLayout.CENTER_HORIZONTAL);
						TextView txt=new TextView(InviteSecretGroupActivity.this);
						txt.setLayoutParams(param1);
						//txt.setGravity(Gravity.CENTER);
						//txt.setBackgroundResource(R.drawable.mobile_no_bg_curve);
						txt.setTextColor(Color.BLACK);
						txt.setTypeface(tf, Typeface.NORMAL);
						txt.setTextSize(16);
						txt.setText(mMobileNo);
						//txt.setId(2);
						txt.setGravity(Gravity.CENTER);
						relativeLayout.addView(txt);
						
						RelativeLayout.LayoutParams param2=new RelativeLayout.LayoutParams(mCountrySpinner.getWidth(),mobileNo.getHeight());
						//param2.topMargin=dpToPx(5);
						param2.addRule(RelativeLayout.LEFT_OF,txt.getId());
						TextView txt1=new TextView(InviteSecretGroupActivity.this);
						txt1.setLayoutParams(param2);
						//txt1.setGravity(Gravity.CENTER);
						//txt1.setBackgroundResource(R.drawable.mobile_no_bg_curve);
						txt1.setTextColor(Color.BLACK);
						txt1.setTypeface(tf, Typeface.NORMAL);
						txt1.setTextSize(16);
						txt1.setGravity(Gravity.CENTER);
						/*if(mCountry.equals("India"))
							txt1.setText("+91");
						else
							txt1.setText("+1");*/
						txt1.setText(mCountry);
						relativeLayout.addView(txt1);
						RelativeLayout.LayoutParams param3=new RelativeLayout.LayoutParams(add.getWidth(),mobileNo.getHeight());
						param3.leftMargin=dpToPx(10);
						param3.addRule(RelativeLayout.RIGHT_OF,txt.getId());
						TextView temp=new TextView(InviteSecretGroupActivity.this);
						temp.setLayoutParams(param3);
						temp.setVisibility(View.INVISIBLE);
						relativeLayout.addView(temp);
						layout.addView(relativeLayout);
						mobileNo.setText("");
					
				}
			}
		});
		
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("inside onclick send :: "+list1.size()+"  "+list2.size());
				if(list1.size()== 0 && list2.size()==0)
				{
					if(isAlreadyMember)
					{
						Utility.showToastMessage(InviteSecretGroupActivity.this, getResources().getString(R.string.invitation_sent_success));
						finish();
					}
					else
						Utility.showToastMessage(InviteSecretGroupActivity.this, getResources().getString(R.string.invite_members_empty_no_send));
				}
				else 
				{
					progressBar.setVisibility(View.VISIBLE);
					if(list1.size() > 0 || list2.size() > 0)
					{
						if(list1.size() > 0)
						{
							list.addAll(list1);
								new SendInvitationIndia(PreferenceSettings.getUserName(),mGroupName, mGroupId, indiaNumber).execute();
						}
						
						if(list2.size() > 0)
						{
							list.addAll(list2);
							new SendInvitationUS(PreferenceSettings.getUserName(),mGroupName, mGroupId, usNumber).execute();
							//Toast.makeText(InviteSecretGroupActivity.this, "Invitation Sent Successfully", Toast.LENGTH_LONG).show();
							//finish();
						}
						/*else
						{
							Toast.makeText(InviteSecretGroupActivity.this, "Invitation Sent Successfully", Toast.LENGTH_LONG).show();
							finish();
						}*/
						 mLocationPoint=new ParseGeoPoint(gpsTracker.getLatitude(),gpsTracker.getLongitude());
	                	 System.out.println("list :: "+list);
	                	for(int j=0;j<list.size();j++)
	                	{
	                		final int i=j;
	                	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
	             		query.whereEqualTo(Constants.TO_USER, list.get(i));
	             		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
	                	query.getFirstInBackground(new GetCallback<ParseObject>() {
	              			public void done(ParseObject object, ParseException e) {
	              					if(object!=null)
	                				{
	              						object.put(Constants.INVITATION_STATUS, "Active");
	              						object.put(Constants.INVITATION_LOCATION, mLocationPoint);
	              						object.saveInBackground();
	                				}
	              					else
	              					{
	              						ParseObject userObject = new ParseObject(Constants.INVITATION_TABLE);
	              						userObject.put(Constants.TO_USER, list.get(i));
	              						userObject.put(Constants.FROM_USER, PreferenceSettings.getMobileNo());
	              						userObject.put(Constants.GROUP_ID, mGroupId);
	              						userObject.put(Constants.INVITATION_STATUS, "Active");
	              						userObject.put(Constants.INVITATION_LOCATION, mLocationPoint);
	              						userObject.saveInBackground();
	              					}
	              			}});
	                	
	                	if(j==list.size()-1)
	                	{
	                		progressBar.setVisibility(View.GONE);
	                		Utility.showToastMessage(InviteSecretGroupActivity.this, getResources().getString(R.string.invitation_sent_success));
		                	 finish();
	                	}
	                }
	          
					}
					
				}
			}
		});
	}
	

	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
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
