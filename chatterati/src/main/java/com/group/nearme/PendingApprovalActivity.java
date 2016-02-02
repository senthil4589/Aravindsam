package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.PendingApprovalAdapter;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.util.Constants;

import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PendingApprovalActivity extends Activity{
	private ImageView mBackImg;
	private PullToRefreshListView listView;
	private PendingApprovalAdapter adapter;
	private ProgressBar progressBar;
	public GPSTracker gpsTracker;
	TextView title,txt;
	//ParseObject groupObject;
	//List<String> memberList;
	private String groupId="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pending_approval_listview);
		Utility.getTracker(this, "GROUP INFO - PENDING MEMBERS SCREEN");
		initViews();
		
		groupId=getIntent().getStringExtra(Constants.GROUP_ID);
	
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				setAdapter();
			}
			});
		setAdapter();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		listView=(PullToRefreshListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		txt=(TextView) findViewById(R.id.txt);
		gpsTracker = new GPSTracker(this);
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
	
	private void setAdapter()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, groupId);
		query.whereEqualTo(Constants.POST_TYPE, "Invitation");
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.include(Constants.USER_ID);
		query.orderByDescending("updatedAt");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							txt.setVisibility(View.GONE);
							adapter=new PendingApprovalAdapter(PendingApprovalActivity.this, list,progressBar);
							listView.setAdapter(adapter);
							progressBar.setVisibility(View.GONE);
							if(listView.isRefreshing())
								listView.onRefreshComplete();
						}
						else
						{
							progressBar.setVisibility(View.GONE);
							txt.setText("No Pending Members");
							txt.setVisibility(View.VISIBLE);
							if(listView.isRefreshing())
								listView.onRefreshComplete();
						}
					}
					else
					{
						progressBar.setVisibility(View.GONE);
						if(listView.isRefreshing())
							listView.onRefreshComplete();
					}
					
			}
		});
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
