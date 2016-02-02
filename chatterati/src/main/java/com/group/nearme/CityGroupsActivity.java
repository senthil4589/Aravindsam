package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.CityGroupsAdapter;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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

public class CityGroupsActivity extends Activity{
	private PullToRefreshListView mListView;
	private CityGroupsAdapter mAdapter;
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
		Utility.getTracker(this, "EXPLORE GROUPS SCREEN");
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
				if(Utility.checkInternetConnectivity(CityGroupsActivity.this))
					getDatafromParse();
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(CityGroupsActivity.this, getResources().getString(R.string.no_network));
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
				startActivity(new Intent(CityGroupsActivity.this,GroupProfileActivity.class).putExtra("flag", true)
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
				startActivity(new Intent(CityGroupsActivity.this,ChooseGroupPurposeActivityNew.class));
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
	        	 getDatafromParse();
	        }
	        else
	        {
	        	flag2=true;
	            gpsTracker.showSettingsAlert(this);
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
		if (!gpsTracker.canGetLocation())
        {
			
        	flag2=true;
            gpsTracker.showSettingsAlert(this);
        }
		 else
	        {
			 if(!gpsTracker.flag)
		       {
		    	   progressBar.setVisibility(View.GONE);
		   		if(mListView.isRefreshing())
		   			mListView.onRefreshComplete();
		    	   Toast.makeText(CityGroupsActivity.this, "Unable to get your location", Toast.LENGTH_LONG).show();
		       }
		       else
		       {
		    				 
			 System.out.println("inside ifffgfh");
	        	mLatitude=gpsTracker.getLatitude();
	        	mLongtitude=gpsTracker.getLongitude();
	        	//getDatafromParse();
	        
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
								mInvitationList=userObject.getList(Constants.GROUP_INVITATION);
								myGroupIdList=userObject.getList(Constants.MY_GROUP_ARRAY);
								getCityGroups();
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
	
	private void getCityGroups()
	{
		
		final ParseGeoPoint point = new ParseGeoPoint(mLatitude, mLongtitude);
		currentLocation=new Location("CurrentLocation");
		currentLocation.setLatitude(mLatitude);
		currentLocation.setLongitude(mLongtitude);
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query1.whereNotContainedIn(Constants.OBJECT_ID, myGroupIdList);
		query1.whereEqualTo(Constants.VISIBILITY, "City");
		query1.whereEqualTo(Constants.GROUP_LOCATION, point);
		query1.whereWithinMiles(Constants.GROUP_LOCATION, point,31.0686);
		query1.whereEqualTo(Constants.GROUP_STATUS, "Active");
		query1.whereNotEqualTo(Constants.GROUP_TYPE, "Secret");
		query1.whereGreaterThan(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getCurrentUTCDate());
		query1.orderByDescending(Constants.GROUP_LOCATION);
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							mNearByGroupList=list;
							finalNearByList=list;
							setAdapter();
							getCountryGroups();
						}
						else
						{
							getCountryGroups();
							//showCreateGroupOption();
						}
					}
					else
					{
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
		Toast.makeText(CityGroupsActivity.this, getResources().getString(R.string.server_issue), Toast.LENGTH_LONG).show();
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
		spannableText.setText(addClickablePart(getResources().getString(R.string.city_group_system_post),62,76), BufferType.SPANNABLE);
	}
	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
	    ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
            	startActivity(new Intent(CityGroupsActivity.this,ChooseGroupPurposeActivityNew.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
            }
        }, idx1, idx2, 0);
  	    return ssb;
	}
	

	
	
	private void setAdapter()
	{
		//mInvitationList.addAll(PreferenceSettings.getGroupInvitationList());
		mAdapter=new CityGroupsAdapter(CityGroupsActivity.this,finalNearByList,myGroupIdList,mInvitationList,progressBar);
		mListView.setAdapter(mAdapter);
		flag=true;
		//lastUpdate.setText("Last Updated "+Utility.date());
		progressBar.setVisibility(View.GONE);
		if(mListView.isRefreshing())
			mListView.onRefreshComplete();
	}
	private void getCountryGroups()
	{
		System.out.println("insdie getCountryGroups");
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query1.whereNotContainedIn(Constants.OBJECT_ID, myGroupIdList);
		query1.whereEqualTo(Constants.VISIBILITY, gpsTracker.getCountryCode(this));
		query1.whereEqualTo(Constants.GROUP_STATUS, "Active");
		query1.whereNotEqualTo(Constants.GROUP_TYPE, "Secret");
		query1.whereGreaterThan(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getCurrentUTCDate());
		query1.orderByDescending(Constants.GROUP_LOCATION);
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							System.out.println("coutrynlist size ::: "+list.get(0).getObjectId());
							System.out.println("NearByList size ::: "+finalNearByList);
							if(finalNearByList.size()==0)
							{
								finalNearByList.addAll(list);
								setAdapter();
							}
							else
							{
								finalNearByList.addAll(list);
								mAdapter.setList(finalNearByList);
								mAdapter.notifyDataSetChanged();
							}
							
						}
						else
						{
							if(finalNearByList.size()==0){
									showCreateGroupOption();
							}
						}
					}
					else
					{
						System.out.println("inside get coutry groups exception ::: "+e);
					}
			}
		});
	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(flag2)
		{
			getDatafromParse();
		}
	}
	
	@Override
	public void onBackPressed() {
        	  showExitAlertDialog();
	}
	public  void showExitAlertDialog(){
		mDialog = new Dialog(CityGroupsActivity.this);
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
