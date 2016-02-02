package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.PendingApprovalAdapter;
import com.group.nearme.adapter.PendingInvitesAdapter;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;

import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PendingInvitationActivity extends Activity{
	private ImageView mBackImg;
	private PullToRefreshListView listView;
	private PendingInvitesAdapter adapter;
	private ProgressBar progressBar;
	List<ParseObject> invitationList;
	ArrayList<String> idList;
	public GPSTracker gpsTracker;
	TextView title,txt;
	private List<ParseObject> groupObjectList=new ArrayList<ParseObject>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pending_approval_listview);
		Utility.getTracker(this, "PENDING INVITES SCREEN");
		initViews();
		
		
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				getDataFromParse();
			}
			});
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) 
			{
				Utility.setGroupObject(groupObjectList.get(position-1));
				startActivity(new Intent(PendingInvitationActivity.this,GroupProfileActivity.class).putExtra("flag", true).putExtra("fromPendingInvitation", true));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		getDataFromParse();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		listView=(PullToRefreshListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		idList=new ArrayList<String>();
		gpsTracker = new GPSTracker(this);
		title=(TextView) findViewById(R.id.title);
		txt=(TextView) findViewById(R.id.txt);
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		title.setText("Pending Invites");
	}
	
	private void getDataFromParse()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
		query.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());
		query.whereEqualTo(Constants.INVITATION_STATUS, "Active");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null)
					{
						if(list.size() > 0)
						{
							invitationList=list;
							for(int i=0;i<invitationList.size();i++)
							{
								idList.add(invitationList.get(i).get(Constants.GROUP_ID).toString());
							}

							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
							query.whereContainedIn(Constants.OBJECT_ID, idList);
							query.whereEqualTo(Constants.GROUP_STATUS, "Active");
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(List<ParseObject> list, ParseException e) {
										if (e == null)
										{
											if(list.size() > 0)
											{
												groupObjectList=list;
												adapter=new PendingInvitesAdapter(PendingInvitationActivity.this,list,progressBar);
												listView.setAdapter(adapter);
												progressBar.setVisibility(View.GONE);
												if(listView.isRefreshing())
													listView.onRefreshComplete();
											}
										}
								}});
						}
						else
						{
							progressBar.setVisibility(View.GONE);
							txt.setVisibility(View.VISIBLE);
						}
					}
					else
						progressBar.setVisibility(View.GONE);

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
