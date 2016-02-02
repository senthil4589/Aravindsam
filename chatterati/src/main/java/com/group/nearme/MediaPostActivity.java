package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.MediaGridAdapter;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class MediaPostActivity extends Activity{
	MediaGridAdapter adapter;
	PullToRefreshGridView gridView;
	private String mGroupId="",mGroupName="",mGroupImage;
	ProgressBar progressBar;
	List<ParseObject> mediaList=new ArrayList<ParseObject>();
	TextView spannableText;
	RelativeLayout createLayout;
	ArrayList<String> postTypeList=new ArrayList<String>();
	RelativeLayout topLayout;
	TextView title;
	boolean fromTopic;
	ImageView mBackImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.grid_view);
		Utility.getTracker(this, "GROUP MEDIA SCREEN");
		intiViews();
		
		gridView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, final int position,
					long arg3) 
			{
				Utility.setList(mediaList);	
				
				final java.util.Date f1=mediaList.get(position).getUpdatedAt();
				Intent intent=new Intent(MediaPostActivity.this,CommentActivity.class);
				intent.putExtra(Constants.GROUP_NAME, mGroupName);
				intent.putExtra(Constants.GROUP_PICTURE, mGroupImage);
				intent.putExtra(Constants.GROUP_ID, mGroupId);
				intent.putExtra(Constants.PROFILE_PICTURE, mediaList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl());
				intent.putExtra("updatedtime", Utility.getTimeAgo(f1.getTime()));
				intent.putExtra("position", position);
				startActivity(intent);
			//	overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);

			}
		});
		
		gridView.setOnRefreshListener(new OnRefreshListener<GridView>() {
			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				if(Utility.checkInternetConnectivity(MediaPostActivity.this))
					refresh();
				else
				{
					progressBar.setVisibility(View.GONE);
					if(gridView.isRefreshing())
						gridView.onRefreshComplete();
					Utility.showToastMessage(MediaPostActivity.this, getResources().getString(R.string.no_network));
				}
				
			}
			});

	}
	private void intiViews() {
		gridView=(PullToRefreshGridView) findViewById(R.id.gridview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		spannableText=(TextView) findViewById(R.id.spannable_text);
		createLayout=(RelativeLayout) findViewById(R.id.create_layout);
		topLayout=(RelativeLayout) findViewById(R.id.top);
		title=(TextView) findViewById(R.id.title);
		mBackImage=(ImageView) findViewById(R.id.back);
		
		mGroupId=getIntent().getStringExtra(Constants.GROUP_ID);
		mGroupName=getIntent().getStringExtra(Constants.GROUP_NAME);
		mGroupImage=getIntent().getStringExtra(Constants.GROUP_PICTURE);
		fromTopic=getIntent().getBooleanExtra("from", false);
		
		title.setText(mGroupName);
		
		if(fromTopic)
			topLayout.setVisibility(View.VISIBLE);
		else
			topLayout.setVisibility(View.GONE);
		
		postTypeList.add("Image");
		postTypeList.add("Stype");
		postTypeList.add("Link");
		postTypeList.add("Video");
		postTypeList.add("SVideo");
		postTypeList.add("GIFVideo");
		postTypeList.add("Event");
		setAdapter();
		
		mBackImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	private void setAdapter()
	                    {
		progressBar.setVisibility(View.VISIBLE);
		createLayout.setVisibility(View.GONE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.whereContainedIn(Constants.POST_TYPE,postTypeList);
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		try
		{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
		catch(Exception e){}
		query.setLimit(1000);
		query.fromPin(Constants.MEDIA+mGroupId);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							mediaList=list;
							adapter=new MediaGridAdapter(MediaPostActivity.this, list);
							gridView.setAdapter(adapter);
							progressBar.setVisibility(View.GONE);
							if(gridView.isRefreshing())
								gridView.onRefreshComplete();
						}
						else
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
							query.whereEqualTo(Constants.GROUP_ID, mGroupId);
							query.whereEqualTo(Constants.POST_STATUS, "Active");
							query.whereContainedIn(Constants.POST_TYPE,postTypeList);
							//query.orderByDescending("updatedAt");
							query.setLimit(100);
							query.orderByDescending(Constants.FEED_UPDATED_TIME);
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> feedList, ParseException e) {
										if (e == null) 
										{
											if(feedList.size() > 0)
											{
												 ParseObject.pinAllInBackground(Constants.MEDIA+mGroupId,feedList, new SaveCallback() {
													@Override
													public void done(ParseException arg0) {
														setAdapter();
													}
												});
											}
											else
											{
												if(gridView.isRefreshing())
													gridView.onRefreshComplete();

												progressBar.setVisibility(View.GONE);
												createLayout.setVisibility(View.VISIBLE);
												spannableText.setText(getResources().getString(R.string.photos_system_post));

											}
										}
										else
										{
											showToast();
										}
								}});
							
							
							
														//spannableText.setMovementMethod(LinkMovementMethod.getInstance());
							//spannableText.setText(addClickablePart(getResources().getString(R.string.my_group_system_post),98,111), BufferType.SPANNABLE);
						}
					}
					else
					{
						showToast();
					}
			}
		});
		
	}
	
	private void refresh()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.whereContainedIn(Constants.POST_TYPE,postTypeList);
		query.setLimit(100);
		query.orderByDescending(Constants.FEED_UPDATED_TIME);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> feedList, ParseException e) {
					if (e == null) 
					{
						if(feedList.size() > 0)
						{
							 ParseObject.unpinAllInBackground(Constants.MEDIA+mGroupId,
				                  new DeleteCallback() {
				                       @Override
				                    public void done(ParseException e) {
				                    	   if(e == null) {
				                    	   ParseObject.pinAllInBackground(Constants.MEDIA+mGroupId,feedList);
				                    	   setAdapter();
				                    	   }
				                       }
							 });
							
						}
					}
			}});
	}
	private void showToast()
	{
		progressBar.setVisibility(View.GONE);
		if(gridView.isRefreshing())
			gridView.onRefreshComplete();
		Toast.makeText(this, getResources().getString(R.string.server_issue), Toast.LENGTH_LONG).show();
		//Toast toast = Toast.makeText(this, "Can't able to connect now. Please try after some time", Toast.LENGTH_LONG);
		//toast.setGravity(Gravity.BOTTOM, 0, 0);
		//toast.show();
	}
	
	public void getNext100Records()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.whereContainedIn(Constants.POST_TYPE,postTypeList);
		query.setSkip(mediaList.size());
		query.setLimit(100);
		query.orderByDescending(Constants.FEED_UPDATED_TIME);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> remainingList, ParseException e) {
					if (e == null) 
					{
						if(remainingList.size() > 0)
						{
							ParseObject.pinAllInBackground(Constants.MEDIA+mGroupId,remainingList,new SaveCallback() {
					            @Override
					            public void done(ParseException e) {
					                if(e == null) {
										mediaList.addAll(remainingList);
					                	adapter.setList(mediaList);
					                	adapter.notifyDataSetChanged();
					                }
					            }});
						}
					}
			}});
			
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