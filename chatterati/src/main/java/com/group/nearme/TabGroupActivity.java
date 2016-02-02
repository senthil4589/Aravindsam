package com.group.nearme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.group.nearme.adapter.MenuAdapter;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.RefreshCallback;
import com.parse.SaveCallback;

public class TabGroupActivity extends TabActivity {
	 // TabSpec Names
    private static final String NEAR_BY_GROUP = "NEARBY";
    private static final String MY_GROUP = "MY GROUPS";
    private static final String CITY_GROUP = "EXPLORE";
	private ImageView mMenuImg,mBackImg,mUserBadge,notification;
	private RelativeLayout mMenuBarLayout,mSearchLayout;
	private Dialog mDialog;
	ListView mListView;
	DrawerLayout mDrawerLayout;
	ActionBarDrawerToggle mDrawerToggle;
	public static MenuAdapter mMenuAdapter;
	String [] mMenuItemArray;
	public static EditText searchEditTxt;
	public static ImageView search1;
	public static TabHost tabHost;
	public static ImageView mSearchImg;
	
	String regid;
	String SENDER_ID;
	GoogleCloudMessaging gcm;
	public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    ImageLoader imageLoader;
	ArrayList<String> idList=new ArrayList<>();
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tab_group_view);
		Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
		
		/*Window window = getWindow();
		//window.addFlags(WindowManager.LayoutParams.FLAG);
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.setStatusBarColor(Color.parseColor("#0298e2"));
*/
		
		mDrawerLayout.setDrawerShadow(null, GravityCompat.START);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mDrawerLayout.setFocusableInTouchMode(false);
		mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.ic_drawer,R.string.drawer_open,	R.string.drawer_close) {
			public void onDrawerClosed(View view) {
			}
			public void onDrawerOpened(View drawerView) {
			
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mListView.setAdapter(mMenuAdapter);
		 tabHost = getTabHost();
		idList=getIntent().getStringArrayListExtra("pending_invites_id");
		 	
		 	Intent i1 = new Intent(this, MyGroupListActivity.class);
			i1.putStringArrayListExtra("pending_invites_id",idList);
	        setupTab1(i1, MY_GROUP);
	        
		 	Intent i2 = new Intent(this, NearByGroupListActivity.class);
	        setupTab2(i2, NEAR_BY_GROUP);
	      
	        Intent i3 = new Intent(this, CityGroupsActivity.class);
	        setupTab3(i3, CITY_GROUP);
	      
	        tabHost.setCurrentTab(0);

	        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
	        {
	            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	           
	            if(i==0)
	        	  tv.setTextColor(Color.WHITE);
	            else
	        	  tv.setTextColor(Color.parseColor("#A4D7FA"));
	        }
	        
	        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
	            @Override
	            public void onTabChanged(String id) {
	                int tab = tabHost.getCurrentTab();
	                
	                for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
	    	        {
	    	            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	    	            if(i==tab)
	    	            	 tv.setTextColor(Color.WHITE); 
	    	            else
	    	            	tv.setTextColor(Color.parseColor("#A4D7FA"));
	    	        }
	            }
	        });
	        
	        mListView.setOnItemClickListener(new ListView.OnItemClickListener() 
			{
				public void onItemClick(AdapterView<?> arg0, View view, int position,
						long arg3) 
				{
					System.out.println("selected menu :: "+mMenuItemArray[position]);
					if(mMenuItemArray[position].equals("Create Group"))
					{
						mDrawerLayout.closeDrawer(mListView);
						startActivity(new Intent(TabGroupActivity.this,ChooseGroupPurposeActivityNew.class));
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
					else if(mMenuItemArray[position].equals("My Profile"))
					{
						mDrawerLayout.closeDrawer(mListView);
						startActivity(new Intent(TabGroupActivity.this,MyProfleActivity.class));
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						 
					}
					else if(mMenuItemArray[position].equals("Terms of Service"))
					{
						openWebView("Terms of Service",Constants.TERMS_URL);
					}
					else if(mMenuItemArray[position].equals("Privacy Policy"))
					{
						openWebView("Privacy Policy",Constants.PRIVACY_URL);
					}
					else if(mMenuItemArray[position].equals("Policies and Guidelines"))
					{
						openWebView("Policies and Guidelines",Constants.RULES_URL);
					}
					else if(mMenuItemArray[position].equals("Contact Us"))
					{
						startActivity(new Intent(TabGroupActivity.this,ContactUsActivity.class));
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
					else if(mMenuItemArray[position].equals("Home"))
					{
						mDrawerLayout.closeDrawer(mListView);
					}
					else if(mMenuItemArray[position].equals("Pending Invites"))
					{
						mDrawerLayout.closeDrawer(mListView);
						startActivity(new Intent(TabGroupActivity.this,PendingInvitationActivity.class));
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
					else if(mMenuItemArray[position].equals("Settings"))
					{
						mDrawerLayout.closeDrawer(mListView);
						startActivity(new Intent(TabGroupActivity.this,AppSettingsActivity.class));
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
				}

				private void openWebView(String title, String url) {
					startActivity(new Intent(TabGroupActivity.this,WebViewActivity.class).putExtra("title", title).putExtra("url", url));
					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			});
	        
	        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
			query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject userObject, ParseException e) {
							if(userObject!=null)
							{
								
								Utility.setUserImageFile(userObject.getParseFile(Constants.THUMBNAIL_PICTURE));
								mMenuAdapter.notifyDataSetChanged();
								List<String> myGroupIdList=userObject.getList(Constants.MY_GROUP_ARRAY);
								if(myGroupIdList.size() > 0)
								{
									 tabHost.setCurrentTab(0);
								}
								else
								{
									tabHost.setCurrentTab(2);
								}
							}
							else
							{
								 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
									query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
									query.fromLocalDatastore();
									query.getFirstInBackground(new GetCallback<ParseObject>() {
										public void done(ParseObject userObject, ParseException e) {
													if(userObject!=null)
													{
														Utility.setUserImageFile(userObject.getParseFile(Constants.THUMBNAIL_PICTURE));
														mMenuAdapter.notifyDataSetChanged();
													}
												}});
							}
						
					
				}});
	        
	}
	

	private void setupTab1(Intent i, final String tag) {
	    View tabview = createTabView(tabHost.getContext(), tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(i);
	    tabHost.addTab(setContent);
	}
	
	private void setupTab2(Intent i, final String tag) {
	    View tabview = createTabView(tabHost.getContext(), tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(i);
	    tabHost.addTab(setContent);
	}
	private void setupTab3(Intent i, final String tag) {
	    View tabview = createTabView(tabHost.getContext(), tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(i);
	    tabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {
	    View view = LayoutInflater.from(context).inflate(R.layout.apptheme_tab_indicator_holo, null);
	    TextView tv = (TextView) view.findViewById(android.R.id.title);
	    tv.setText(text);
	    return view;
	}

	@SuppressWarnings("deprecation")
	private void initViews() {
		mMenuImg=(ImageView) findViewById(R.id.menu);
		mSearchImg=(ImageView) findViewById(R.id.search);
		notification=(ImageView) findViewById(R.id.notification);
		mUserBadge=(ImageView) findViewById(R.id.user_score);
		mBackImg=(ImageView) findViewById(R.id.back);
		mMenuBarLayout=(RelativeLayout) findViewById(R.id.menu_bar);
		mSearchLayout=(RelativeLayout) findViewById(R.id.search_layout);
		searchEditTxt=(EditText) findViewById(R.id.search_box);
		//search1=(ImageView) findViewById(R.id.search1);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mListView = (ListView) findViewById(R.id.listview);
		mMenuItemArray = getResources().getStringArray(R.array.menu_item_array);
		mMenuAdapter=new MenuAdapter(this,mMenuItemArray);
		mMenuImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//showMenuDialog();
				if (mDrawerLayout.isDrawerOpen(mListView)) {
		            mDrawerLayout.closeDrawer(mListView);
		          }else {
		        	  mDrawerLayout.openDrawer(mListView);
		        }
			}
		});
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEditTxt.setText("");
				mSearchLayout.setVisibility(View.GONE);
				mMenuBarLayout.setVisibility(View.VISIBLE);
			}
		});
		notification.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(TabGroupActivity.this,NotificationListActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		mSearchImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				mSearchLayout.setVisibility(View.VISIBLE);
				mMenuBarLayout.setVisibility(View.GONE);
			}
		});
		
		mUserBadge.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showUserBadgeDialog();
				startActivity(new Intent(TabGroupActivity.this,PolicyGuideLineActivity.class));
				//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				
			}
		});
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
		//System.out.println("status ::: "+PreferenceSettings.getPushStatus());
		//if(PreferenceSettings.getPushStatus())
		//{
		//SENDER_ID=getResources().getString(R.string.gcm_sender_id);
		
		PushService.setDefaultPushCallback(this, TabGroupActivity.class);
		ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
		parseInstallation.put(Constants.MOBILE_NO,PreferenceSettings.getMobileNo());
		parseInstallation.saveInBackground();
		
	}
	
	class RegisterBackground extends AsyncTask<String,String,String>{
		@Override
		protected String doInBackground(String... arg0) {
			String msg = "";
			try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(TabGroupActivity.this);
                }
                regid = gcm.register(SENDER_ID);
                System.out.println("token ::: "+regid);
			}
			catch(Exception e){
				System.out.println("inside catch ;:: "+e);
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			System.out.println("inside onpostexecute");
			ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
			parseInstallation.put(Constants.MOBILE_NO,PreferenceSettings.getMobileNo());
			parseInstallation.saveInBackground();
		}
	}
	
	private void showUserBadgeDialog()
	{
		mDialog = new Dialog(this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.user_badge);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		//Button ok=(Button)mDialog.findViewById(R.id.ok);
		NetworkImageView image=(NetworkImageView) mDialog.findViewById(R.id.user_image);
		TextView name=(TextView) mDialog.findViewById(R.id.user_name);
		
		image.setImageUrl(PreferenceSettings.getProfilePic(), imageLoader);
		imageLoader.get((PreferenceSettings.getProfilePic()), ImageLoader.getImageListener(
				image, R.drawable.launcher_icon, R.drawable.group_image));
		name.setText(PreferenceSettings.getUserName());
		
		/*ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDialog.dismiss();
			}
		});
*/		
		mDialog.show();
	}
	
	@Override
	public void onBackPressed() {
		if(mSearchLayout.getVisibility()==android.view.View.VISIBLE)
		{
			mSearchLayout.setVisibility(View.GONE);
			mMenuBarLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			super.onBackPressed();
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
