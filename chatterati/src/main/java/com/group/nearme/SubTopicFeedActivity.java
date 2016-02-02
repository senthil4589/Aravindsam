package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.SubTopicFeedAdapter;
import com.group.nearme.adapter.TopicListFeedAdapter;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class SubTopicFeedActivity extends Activity{

	private PullToRefreshListView mListView;
	private ImageView mBackImg;
	private ProgressBar progressBar;
	private TextView title;
	ArrayList<String> feedIdList;
	private String mGroupName="",mGroupType="",mGroupImage="",mGroupId="",hashTagName="";
	public SubTopicFeedAdapter mAdapter;
	public GPSTracker gpsTracker;
	private ParseObject groupObject;
	private String subTopicId="";
	List<ParseObject> objectList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hash_tag_feed_listview);
		Utility.getTracker(this, "GROUP HASHTAG RESULT SCREEN");
		initViews();
		
		mListView.setOnScrollListener(new OnScrollListener(){
		    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		        // TODO Auto-generated method stub
		      }
		      public void onScrollStateChanged(AbsListView view, int scrollState) {
		    	  try
			    	{
			    	  if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
			    		  System.out.println("inside scroll change");
			    		  mAdapter.isScrolled=true;
			    		  VideoView video=(VideoView) view.findViewById(R.id.videoView);
			    		  FrameLayout play=(FrameLayout) view.findViewById(R.id.svideo_play_frame);
				    	  
				    	  if(video.isPlaying())
				    	  {
				    		  play.setVisibility(View.VISIBLE);
				    		  System.out.println("inside onScrollStateChanged before video stop");
				    		  video.stopPlayback();
				    	  }
				    	
			          }
			    	}
			    	catch(Exception e){
			    		
			    	}
			      }
		    });
		
		
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(SubTopicFeedActivity.this))
				{
					getFeedFromServer(true);
				}
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(SubTopicFeedActivity.this, getResources().getString(R.string.no_network));
				}
			}
			
			});
		
	}

	private void initViews() {
		mListView=(PullToRefreshListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		mBackImg=(ImageView) findViewById(R.id.back);
		title=(TextView) findViewById(R.id.title);
		
		hashTagName =getIntent().getStringExtra("hash_tags");
		subTopicId =getIntent().getStringExtra("id");
		feedIdList=getIntent().getStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
		
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.getString(Constants.GROUP_NAME);
		mGroupType=groupObject.getString(Constants.GROUP_TYPE);
		mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		
		title.setText(hashTagName);
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		
		gpsTracker=new GPSTracker(this);
		
		if (!gpsTracker.canGetLocation())
        {
			gpsTracker.showSettingsAlert(this);
        }
		
		setAdapter();
		
	}

	public void setAdapter() {
		ArrayList<String> list=new ArrayList<String>();
		list.add("InActive");
		list.add("Inactive");
		System.out.println("feed id list :: "+feedIdList);
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereContainedIn(Constants.OBJECT_ID, feedIdList);
		query.whereNotContainedIn(Constants.POST_STATUS, list);
		query.include(Constants.USER_ID);
		query.setLimit(1000);
		try
		{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
		catch(Exception e){}
		query.fromPin(subTopicId);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						objectList=list;
						if(list.size() > 0)
						{
							mAdapter=new SubTopicFeedAdapter(SubTopicFeedActivity.this,list,progressBar,mGroupId,mGroupName,mGroupImage,mGroupType);
							mListView.setAdapter(mAdapter);
							progressBar.setVisibility(View.GONE);
							if(mListView.isRefreshing())
								mListView.onRefreshComplete();
						}
						else
						{
							System.out.println("inside else list size 0 tag feed");
							getFeedFromServer(false);
						}
					}
					else
						getFeedFromServer(false);
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
		
		private void getFeedFromServer(final boolean isRefresh)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
			query.whereContainedIn(Constants.OBJECT_ID, feedIdList);
			//query.whereNotContainedIn(Constants.POST_STATUS, feedInActiveList);
			query.whereEqualTo(Constants.POST_STATUS, "Active");
			query.include(Constants.USER_ID);
			query.setLimit(100);
			try
			{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
			catch(Exception e){}
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(final List<ParseObject> list, ParseException e) {
						if (e == null) 
						{
							System.out.println("inside setAdapter()"+list.size());
							if(list.size() > 0)
							{
								progressBar.setVisibility(View.GONE);
								if(isRefresh)
								{
									if(mListView.isRefreshing())
										mListView.onRefreshComplete();
									ParseObject.unpinAllInBackground(subTopicId,
							                  new DeleteCallback() {
							                       @Override
							                    public void done(ParseException e) {
							                    	   if(e == null) {
							                    		   ParseObject.pinAllInBackground(subTopicId,list,new SaveCallback() {
							   					            @Override
							   					            public void done(ParseException e) {
							   					                if(e == null) {
							   					                	setAdapter();
							   					                }
							   					            }});
							                    	   }
							                       }});
								}
								else
								{
									ParseObject.pinAllInBackground(subTopicId,list,new SaveCallback() {
							            @Override
							            public void done(ParseException e) {
							                if(e == null) {
							                	setAdapter();
							                }
							            }});
								}
						        
							}
							else
							{
								progressBar.setVisibility(View.GONE);
								if(mListView.isRefreshing())
									mListView.onRefreshComplete();
								Utility.showToastMessage(SubTopicFeedActivity.this, "No post available for this tag");
							}
						}
						else
						{
							progressBar.setVisibility(View.GONE);
							if(mListView.isRefreshing())
								mListView.onRefreshComplete();
							Utility.showToastMessage(SubTopicFeedActivity.this, "No post available for this tag");
						}
				}});
			
		}
		
		
		public void getNext100Records()
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
			query.whereContainedIn(Constants.OBJECT_ID, feedIdList);
			query.whereEqualTo(Constants.POST_STATUS, "Active");
			query.include(Constants.USER_ID);
			query.setSkip(objectList.size());
			query.setLimit(100);
			query.orderByDescending(Constants.FEED_UPDATED_TIME);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(final List<ParseObject> remainingList, ParseException e) {
						if (e == null) 
						{
							if(remainingList.size() > 0)
							{
								ParseObject.pinAllInBackground(subTopicId,remainingList,new SaveCallback() {
						            @Override
						            public void done(ParseException e) {
						                if(e == null) {
						                	objectList.addAll(remainingList);
						                	mAdapter.setList(objectList);
						                	mAdapter.notifyDataSetChanged();
						                }
						            }});
							}
						}
				}});
	    	
	    	
		}
		

		
}
