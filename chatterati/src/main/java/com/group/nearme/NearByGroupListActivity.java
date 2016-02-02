package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.NearByMeAdapter;
import com.group.nearme.floatbutton.ButtonFloat;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;

public class NearByGroupListActivity extends Activity {
	private PullToRefreshListView mListView;
	private NearByMeAdapter mAdapter;
	public GPSTracker gpsTracker;
	Double mLatitude,mLongtitude;
	List<String> myGroupIdList,mInvitationList;
	List<ParseObject> mNearByGroupList;
	ProgressBar progressBar;
	AlertDialog mAlertDialog;
	boolean flag;
	public static boolean flag2;
	RelativeLayout createLayout;
	Location currentLocation;
	List<ParseObject> finalNearByList;
	Dialog mDialog;
	TextView spannableText;
	ButtonFloat floatButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.near_by_me_list_view);
		Utility.getTracker(this, "NEAR BY GROUPS");
		initViews();
	
		TabGroupActivity.searchEditTxt.addTextChangedListener(new TextWatcher() {
		     
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		        // When user changed the Text
		    	if(flag)
		        		mAdapter.filter(String.valueOf(cs));
		    }
		     
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		    }
		     
		    @Override
		    public void afterTextChanged(Editable arg0) {
		    }
		});
		
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(NearByGroupListActivity.this))
					getDatafromParse();
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(NearByGroupListActivity.this, getResources().getString(R.string.no_network));
				}
			}
			});
		
		
		mListView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) 
			{
				if(finalNearByList.size()> 0)
				{
				Utility.setGroupObject(finalNearByList.get(position-1));
				startActivity(new Intent(NearByGroupListActivity.this,GroupProfileActivity.class).putExtra("flag", true)
						.putStringArrayListExtra("invitation", (ArrayList<String>) mInvitationList).putExtra("fromPendingInvitation", false));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				
			}
		});
	}

	private void initViews() {
		mListView=(PullToRefreshListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		//lastUpdate=(TextView) findViewById(R.id.last_update);
		//refreshImg=(ImageView) findViewById(R.id.refresh);
		createLayout=(RelativeLayout) findViewById(R.id.create_layout);
		finalNearByList=new ArrayList<ParseObject>();
		spannableText=(TextView) findViewById(R.id.spannable_text);
		
		floatButton=(ButtonFloat) findViewById(R.id.buttonFloat);
		
		
		floatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(NearByGroupListActivity.this,ChooseGroupPurposeActivityNew.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		if(Utility.checkInternetConnectivity(this))
		{
		gpsTracker = new GPSTracker(this);
		 if (gpsTracker.canGetLocation())
	        {
	        	mLatitude=gpsTracker.getLatitude();
	        	mLongtitude=gpsTracker.getLongitude();
	        	
	        	//String str=gpsTracker.getGeocoderAddress(this);
	        	System.out.println("country name in nearby ::: "
	        			+gpsTracker.getCountryCode(this));
	        	 getDatafromParse();
	        }
	        else
	        {
	        	flag2=true;
	            //gpsTracker.showSettingsAlert(this);
	        }
		}
		else
		{
			//Utility.showOkDilaog(this, "Please check your network connection");
			//Toast.makeText(this, "Please check your network connection", Toast.LENGTH_LONG).show();
			//Utility.showToastMessage(this, getResources().getString(R.string.no_network));
		}
		
	}
	
	public void getDatafromParse()
	{
		flag2=false;
		gpsTracker = new GPSTracker(this);
		/*if (gpsTracker.canGetLocation())
        {
			System.out.println("inside ifffgfh");
        	mLatitude=gpsTracker.getLatitude();
        	mLongtitude=gpsTracker.getLongitude();
        	//getDatafromParse();
        }*/
		if (!gpsTracker.canGetLocation())
	        {
	        	flag2=true;
	            gpsTracker.showSettingsAlert(this);
	        }
		else
		{
			System.out.println("inside ifffgfh");
        	
       if(!gpsTracker.flag)
       {
    	   progressBar.setVisibility(View.GONE);
   		if(mListView.isRefreshing())
   			mListView.onRefreshComplete();
    	   Toast.makeText(NearByGroupListActivity.this, "Unable to get your location", Toast.LENGTH_LONG).show();
       }
       else
       {
    	   mLatitude=gpsTracker.getLatitude();
       	mLongtitude=gpsTracker.getLongitude();
		createLayout.setVisibility(View.GONE);
		finalNearByList=new ArrayList<ParseObject>();
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject userObject, ParseException e) {
						if (e == null) 
						{
							if(userObject!=null)
							{
								System.out.println("after get user table :: "+Utility.getCurrentDate());
								
								/*}
								else if(userObject.get(Constants.USER_STATE).toString().equals(Constants.SUSPENDED))
								{
								mAlertDialog=new AlertDialog.Builder(NearByGroupListActivity.this)
								.setMessage(getResources().getString(R.string.user_suspended))
								.setPositiveButton("Ok", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
										Intent intent = new Intent(Intent.ACTION_MAIN);
										intent.addCategory(Intent.CATEGORY_HOME);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									}
								}).show();
							}
							else
							{*/
							mInvitationList=userObject.getList(Constants.GROUP_INVITATION);
							myGroupIdList=userObject.getList(Constants.MY_GROUP_ARRAY);
							
							System.out.println("before get group table :: "+Utility.getCurrentDate());
							
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
							query.whereNotContainedIn(Constants.OBJECT_ID, myGroupIdList);
							query.whereEqualTo(Constants.GROUP_TYPE, "Global Open");
							query.whereEqualTo(Constants.GROUP_STATUS, "Active");
							query.whereGreaterThan(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getCurrentUTCDate());
							query.orderByDescending("updatedAt");
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(List<ParseObject> gloabalOpenList, ParseException e) {
										if (e == null) 
										{
											if(gloabalOpenList.size() > 0)
											{
												for(int j=0;j<gloabalOpenList.size();j++)
												{
													finalNearByList.add(gloabalOpenList.get(j));
													
													if(j==gloabalOpenList.size()-1)
													{
														if(finalNearByList.size() > 0)	
														{
															setAdapter();
														}
													}
												}

												
												getNearByGroups(false);
											}
											else
											{
												getNearByGroups(true);
											}
										}
										else
										{
											showToast();
										}
								}});
							
						}
						else
						{
							showToast();
						}
					}
					else
					{
						showToast();
					}
			}
		});
		}
		}
	}
	
	private void getNearByGroups(final boolean flag)
	{
		ArrayList<String> groupType=new ArrayList<String>();
		groupType.add("Secret");
		groupType.add("Global Open");
		final ParseGeoPoint point = new ParseGeoPoint(mLatitude, mLongtitude);
		currentLocation=new Location("CurrentLocation");
		currentLocation.setLatitude(mLatitude);
		currentLocation.setLongitude(mLongtitude);
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_TABLE);
//		/query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query1.whereNotContainedIn(Constants.OBJECT_ID, myGroupIdList);
		query1.whereEqualTo(Constants.VISIBILITY, "Nearby");
		query1.whereEqualTo(Constants.GROUP_LOCATION, point);
		query1.whereWithinMiles(Constants.GROUP_LOCATION, point,1.242742);
		query1.whereEqualTo(Constants.GROUP_STATUS, "Active");
		query1.whereNotContainedIn(Constants.GROUP_TYPE, groupType);
		//query1.whereNotEqualTo(Constants.GROUP_TYPE, "Global Open");
		query1.whereGreaterThan(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getCurrentUTCDate());
		query1.orderByDescending("updatedAt");
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							System.out.println("after get group table :: "+Utility.getCurrentDate());
							mNearByGroupList=list;
							for(int i=0;i<mNearByGroupList.size();i++)
							{
								
								Location groupLocation=new Location("GroupLocation");
								groupLocation.setLatitude(mNearByGroupList.get(i).getParseGeoPoint(Constants.GROUP_LOCATION).getLatitude());
								groupLocation.setLongitude(mNearByGroupList.get(i).getParseGeoPoint(Constants.GROUP_LOCATION).getLongitude());
								float distance = currentLocation.distanceTo(groupLocation);
								if(distance <= mNearByGroupList.get(i).getInt(Constants.VISIBILITY_RADIUS))
								{
									finalNearByList.add(mNearByGroupList.get(i));
								}
								
								if(i==mNearByGroupList.size()-1)
								{
									if(finalNearByList.size() > 0)	
									{
										if(flag)
										{
											setAdapter();
										}
										else
										{
											mAdapter.setList(finalNearByList);
											mAdapter.notifyDataSetChanged();
										}
										
									}
									else
									{
										if(flag)
											showCreateGroupOption();
									}
								}
							}
							
						}
						else
						{
							if(flag)
								showCreateGroupOption();
						}
					}
					else
					{
						if(flag)
							showToast();
					}
			}
		});
	}
	
	private void showToast()
	{
		progressBar.setVisibility(View.GONE);
		if(mListView.isRefreshing())
			mListView.onRefreshComplete();
		Toast.makeText(NearByGroupListActivity.this, getResources().getString(R.string.server_issue), Toast.LENGTH_LONG).show();
		//Toast toast = Toast.makeText(this, "Can't able to connect now. Please try after some time", Toast.LENGTH_LONG);
		//toast.setGravity(Gravity.BOTTOM, 0, 0);
		//toast.show();
	}
	
	
	private void showCreateGroupOption()
	{
		mListView.setAdapter(null);
		if(mListView.isRefreshing())
			mListView.onRefreshComplete();
		createLayout.setVisibility(View.VISIBLE);
		//mListView.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		spannableText.setMovementMethod(LinkMovementMethod.getInstance());
		spannableText.setText(addClickablePart(getResources().getString(R.string.near_by_system_post),69,83), BufferType.SPANNABLE);
	}
	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
	    ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
            	startActivity(new Intent(NearByGroupListActivity.this,ChooseGroupPurposeActivityNew.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
            }
        }, idx1, idx2, 0);
  	    return ssb;
	}
	

	
	
	private void setAdapter()
	{
		//mInvitationList.addAll(PreferenceSettings.getGroupInvitationList());
		mAdapter=new NearByMeAdapter(NearByGroupListActivity.this,finalNearByList,myGroupIdList,mInvitationList,progressBar);
		mListView.setAdapter(mAdapter);
		flag=true;
		//lastUpdate.setText("Last Updated "+Utility.date());
		progressBar.setVisibility(View.GONE);
		if(mListView.isRefreshing())
			mListView.onRefreshComplete();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		/*double mLatitudeOld=gpsTracker.getLatitude();
		double mLongtitudeOld=gpsTracker.getLongitude();
		currentLocation=new Location("CurrentLocation");
		currentLocation.setLatitude(mLatitudeOld);
		currentLocation.setLongitude(mLongtitudeOld);
		
		Location newLocation=new Location("NewLocation");
		
		
		gpsTracker = new GPSTracker(this);
		if (gpsTracker.canGetLocation())
        {
        	mLatitude=gpsTracker.getLatitude();
        	mLongtitude=gpsTracker.getLongitude();
        	
        	newLocation.setLatitude(mLatitude);
        	newLocation.setLongitude(mLongtitude);
        }
		 else
	        {
	            gpsTracker.showSettingsAlert(this);
	        }
		float distance = currentLocation.distanceTo(newLocation);
		if(distance >=1000)
		{
			getDatafromParse();
		}
		
*/		
		if(flag2)
		{
			getDatafromParse();
			/*flag2=false;
			gpsTracker = new GPSTracker(this);
			if (gpsTracker.canGetLocation())
	        {
				System.out.println("inside ifffgfh");
	        	mLatitude=gpsTracker.getLatitude();
	        	mLongtitude=gpsTracker.getLongitude();
	        	getDatafromParse();
	        }
			 else
		        {
		        	flag2=true;
		            gpsTracker.showSettingsAlert(this);
		        }*/
		}
	}
	
	@Override
	public void onBackPressed() {
        	  showExitAlertDialog();
	}
	public  void showExitAlertDialog(){
		mDialog = new Dialog(NearByGroupListActivity.this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.two_btn_dialog);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		Button yes=(Button) mDialog.findViewById(R.id.yes);
		Button no=(Button) mDialog.findViewById(R.id.no);
		TextView message=(TextView) mDialog.findViewById(R.id.msg);
		
		message.setText("Are you sure you want to exit?");
		
		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		mDialog.show();

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
