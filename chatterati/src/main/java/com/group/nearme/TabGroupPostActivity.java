package com.group.nearme;

import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

public class TabGroupPostActivity extends TabActivity {
	 // TabSpec Names
	private static final String FEED_POST = "FEED";
    private static final String HOT_POST = "MEDIA";
    private static final String HASH_TAG = "TOPICS";
    private static final String PUBLIC_POST = "TEXT  ONLY  ANONYMOUS  POSTING";
    //private static final String TREDING_POST = "TREDING";
   
	public ImageView mBackImg;//,mGroupImgView;
	public static ParseImageView mGroupImgView;
	public static RelativeLayout mGroupInfoLayout;
	private String mGroupId="",mGroupName="",mGroupImage="",mMobileNo="";
	public static TextView mGroupNameTxtView;
	TabHost tabHost;
	ParseObject groupObject;
	TabSpec Spec1;
	 boolean isCitySelected;
	 Dialog mDialog;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tab_group_post);
		Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
		
		 tabHost = getTabHost();
		 
		 if(!groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		 {
	        Intent intent1 = new Intent(this, GroupPostListActivity.class);
	        intent1.putExtra(Constants.GROUP_NAME, mGroupName);
	        intent1.putExtra(Constants.GROUP_PICTURE, mGroupImage);
	        intent1.putExtra(Constants.GROUP_ID, mGroupId);
	        setupTab1(intent1, FEED_POST);
	      
	        Intent intent2 = new Intent(this, HashTagActivity.class);
	        intent2.putExtra(Constants.GROUP_NAME, mGroupName);
	        intent2.putExtra(Constants.GROUP_PICTURE, mGroupImage);
	        intent2.putExtra(Constants.GROUP_ID, mGroupId);
	        intent2.putExtra("from", false);
	        setupTab2(intent2, HASH_TAG);
	        
	        Intent intent3 = new Intent(this, MediaPostActivity.class);
	        intent3.putExtra(Constants.GROUP_NAME, mGroupName);
	        intent3.putExtra(Constants.GROUP_PICTURE, mGroupImage);
	        intent3.putExtra(Constants.GROUP_ID, mGroupId);
	        intent3.putExtra("from", false);
	        setupTab3(intent3, HOT_POST);
	        
		 }
		 else
		 {
			 	Intent intent1 = new Intent(this, GroupPostListActivity.class);
		        intent1.putExtra(Constants.GROUP_NAME, mGroupName);
		        intent1.putExtra(Constants.GROUP_PICTURE, mGroupImage);
		        intent1.putExtra(Constants.GROUP_ID, mGroupId);
		        setupTab1(intent1, PUBLIC_POST);
		 }
		 try
		 {
		      if(Utility.getGroupObject().get(Constants.GROUP_ATTRIBUTE)!=null)
		      {
		    	  HashMap<String, Object> map=(HashMap<String, Object>) Utility.getGroupObject().get(Constants.GROUP_ATTRIBUTE);
				 int index=(Integer) map.get(Constants.INSIDE_GROUP_DEFAULT_TAB);
		    	  tabHost.setCurrentTab(index-1);
		      }
		 }
		 catch(Exception e){
			 
		 }
	       
		 for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
	        {
	            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	           
	          if(i==0)
	          {
	        	  tv.setTextColor(Color.WHITE);
	        	 
	        	  //tv.setAlpha((float) 0.5); 
	          }
	          else
	          {
	        	  tv.setTextColor(Color.parseColor("#A4D7FA"));
	        	  //tv.setAlpha(255);
	          }
	        }
		 
		 if(tabHost.getTabWidget().getChildCount()==1)
		 {
			 tabHost.getTabWidget().getChildTabViewAt(0).setBackgroundDrawable(null);
		 }
	        
	        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
	            @Override
	            public void onTabChanged(String id) {
	                int tab = tabHost.getCurrentTab();
	                for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
	    	        {
	    	            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	    	          if(i==tab)
	    	          {
	    	        	  
	    	        	  tv.setTextColor(Color.WHITE);
	    	        	 // tv.setAlpha((float) 0.5);
	    	          }
	    	          else
	    	          {
	    	        	  tv.setTextColor(Color.parseColor("#A4D7FA"));
	    	        	 // tv.setAlpha(255);
	    	          }
	    	        }
	            }
	        });
	        
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

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGroupImgView=(ParseImageView) findViewById(R.id.group_image);
		mGroupNameTxtView=(TextView) findViewById(R.id.group_name);
		mGroupInfoLayout=(RelativeLayout) findViewById(R.id.group_info_layout);
	
		groupObject=Utility.getGroupObject();
		
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		if(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE)==null)
			mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		else
			mGroupImage=groupObject.getParseFile(Constants.THUMBNAIL_PICTURE).getUrl();
		
		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
				query.whereEqualTo(Constants.GROUP_ID, mGroupId);
				query.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object, ParseException e) {
							if (e == null) 
							{
								if(object!=null)
								{
									object.put(Constants.UNREAD_MESSAGES, 0);
									object.saveInBackground();
								}
							}
					}});
				MyGroupListActivity.flag1=true;*/
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
				
			}
		});
		
		mGroupInfoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(TabGroupPostActivity.this,NewGroupInfoActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		mGroupNameTxtView.setText(mGroupName);
		Picasso.with(this).load(mGroupImage).into(mGroupImgView);
		
		isCitySelected=getIntent().getBooleanExtra(Constants.CITY_GROUP_VISIBILTY, false);
		if(isCitySelected)
			showAlert();
		
	}
	private void showAlert()
	{
		mDialog = new Dialog(this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.two_btn_dialog);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		Button yes=(Button) mDialog.findViewById(R.id.yes);
		Button no=(Button) mDialog.findViewById(R.id.no);
		TextView message=(TextView) mDialog.findViewById(R.id.msg);
		
		message.setText(getResources().getString(R.string.city_group_alert));
		
			no.setVisibility(View.GONE);
			yes.setText("OK");
		
		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		
		
		mDialog.show();
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
