package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.TopicListAdapter;
import com.group.nearme.adapter.TopicListFeedAdapter;
import com.group.nearme.fadingheader.FadingActionBarHelper;
import com.group.nearme.floatbutton.ButtonFloat;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
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
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class SubTopicListActivity extends Activity
{
	private ListView mListView;
	private ImageView mTopicImgView;//mBackImgView
	private TextView mGroupNameTxtView;
	private ParseObject groupObject;
	private String mGroupId="",mGroupName="",mGroupImage="",mTagId="",mTagName="",mTagImage="";
	private List<ParseObject> mSubTopicList=new ArrayList<ParseObject>();
	private ProgressBar progressBar;
	public GPSTracker gpsTracker;
	public TopicListFeedAdapter feedAdapter;
	ArrayList<String> topicFeedIdList=new ArrayList<String>();
	ButtonFloat floatButton;
	FrameLayout refreshImage;
	ArrayList<String> feedInActiveList=new ArrayList<String>();
	
	public Button newPostBtn;
	public int localCount,serverCount;
	public boolean isFirstTime=true;
	List<ParseObject> objectList;//=new ArrayList<ParseObject>();
	public static SubTopicListActivity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.sub_topic_listview);
		FadingActionBarHelper helper = new FadingActionBarHelper()
        .actionBarBackground(R.drawable.ab_background)
       .headerLayout(R.layout.header)
        .contentLayout(R.layout.activity_listview)
   .headerOverlayLayout(R.layout.sub_topic_headerlay);
    setContentView(helper.createView(this));
    
    helper.initActionBar(this);


		Utility.getTracker(this, "SUB TOPIC LIST SCREEN");
		initViews();
		activity=this;
		getActionBar().setHomeButtonEnabled(true);
		getSubTopics();
		//mListView.
		/*mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(SubTopicListActivity.this))
				{
					//progressBar.setVisibility(View.VISIBLE);
					//getTagFromParse();
				}
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(SubTopicListActivity.this, getResources().getString(R.string.no_network));
				}
			}});
		*/
		
		newPostBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//slideToTop(welcomePostLayout);
				//welcomePostLayout.setVisibility(View.GONE);
				isFirstTime=false;
				newPostBtn.setVisibility(View.GONE);
				//refresh();
				//setFeedAdapter();
				//setAdapter();
				onCreate(null);
			}
		});
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    int id = item.getItemId();

	    if (id == android.R.id.home) {
	        onBackPressed();
	    }

	    return super.onOptionsItemSelected(item);	
	 }


	private void getSubTopics() {
		mSubTopicList=new ArrayList<ParseObject>();
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.SUB_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.whereEqualTo(Constants.TAG_ID_STRING, mTagId);
		query.whereNotEqualTo(Constants.SUB_TAG_STATUS, "InActive");
		//query.include(Constants.GROUP_ID);
		query.orderByDescending(Constants.SUB_TAG_RANK);
		query.fromPin(mGroupId+Constants.SUB_TAG_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							System.out.println("inside getSubTopics");
							progressBar.setVisibility(View.GONE);
							mSubTopicList=list;
							int count=list.size();
							ArrayList<ParseObject> indexList=new ArrayList<ParseObject>();
							for(int i=0;i<count;i++)
							{
								if(mSubTopicList.get(i).getInt(Constants.SUB_TAG_POST_COUNT)==0)
								{
									System.out.println("index :: "+i);
									indexList.add(mSubTopicList.get(i));
								}
								
							}
							System.out.println("index List :: "+indexList);
							for(int j=0;j<indexList.size();j++)
							{
								mSubTopicList.remove(indexList.get(j));
							}
							setAdapter();

							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.SUB_TAG_TABLE);
							query.whereEqualTo(Constants.TAG_ID_STRING, mTagId);
							query.whereNotEqualTo(Constants.SUB_TAG_STATUS, "InActive");
							//query.include(Constants.GROUP_ID);
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											if(list.size() > 0)
											{
												ParseObject.unpinAllInBackground(mGroupId+Constants.SUB_TAG_LOCAL_DATA_STORE,
										                  new DeleteCallback() {
										                       @Override
										                    public void done(ParseException e) {
										                    	   if(e == null) {
										                    	   ParseObject.pinAllInBackground(mGroupId+Constants.SUB_TAG_LOCAL_DATA_STORE,list);
										                    	   } 
										                       }
													 });
											}
										}
								}});
						
						}
						else
						{
							getTagFromParse();
						}
					}
					else
					{
						getTagFromParse();
					}
			}});
							
		
	}
	
	public void setAdapter()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereContainedIn(Constants.OBJECT_ID, topicFeedIdList);
		//query.whereNotContainedIn(Constants.POST_STATUS, feedInActiveList);
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.include(Constants.USER_ID);
		query.setLimit(1000);
		try
		{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
		catch(Exception e){}
		query.fromPin(mTagId);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						System.out.println("inside setAdapter()"+list.size());
						if(list.size() > 0)
						{
							objectList=list;
							feedAdapter=new TopicListFeedAdapter(mSubTopicList,false,null,SubTopicListActivity.this,SubTopicListActivity.this,list,progressBar,mGroupId,mGroupName,mTagImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mListView.setAdapter(feedAdapter);
							progressBar.setVisibility(View.GONE);
							refreshImage.setVisibility(View.VISIBLE);
							//if(mListView.isRefreshing())
								//mListView.onRefreshComplete();
							
							if(isFirstTime)
							{
								isFirstTime=false;
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
							query.whereContainedIn(Constants.OBJECT_ID, topicFeedIdList);
							//query.whereNotContainedIn(Constants.POST_STATUS, feedInActiveList);
							query.whereEqualTo(Constants.POST_STATUS, "Active");
							query.include(Constants.USER_ID);
							query.setLimit(100);
							try
							{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
							catch(Exception e1){}
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list1, ParseException e) {
										if (e == null) 
										{
											System.out.println("inside setAdapter()"+list1.size());
											
										if(list1.size() > 0)
										{
											//if(!(list.get(0).getDate(Constants.FEED_UPDATED_TIME).
												//	equals(list1.get(0).getDate(Constants.FEED_UPDATED_TIME))))
											if(!list1.get(0).getDate(Constants.FEED_UPDATED_TIME).equals(list.get(0).getDate(Constants.FEED_UPDATED_TIME)))		
											{
														ParseObject.unpinAllInBackground(mTagId,
										                  new DeleteCallback() {
										                       @Override
										                    public void done(ParseException e) {
										                    	   if(e == null) {
										                    	   ParseObject.pinAllInBackground(mTagId,list1);
										                    	   newPostBtn.setVisibility(View.VISIBLE);
										                    	   } 
										                       }
													 });			
														
													}
										}	
											/*if(list1.size() > 0)
											{
												ParseObject.pinAllInBackground(mTagId,list1,new SaveCallback() {
										            @Override
										            public void done(ParseException e) {
										                if(e == null) {
										                	setAdapter();
										                }
										            }});
										        
											}*/
										}
								}});
						
							}
						}
						else
						{
							getFeedFromServer(topicFeedIdList,false);
						}
						
					}
					else
					{
						getFeedFromServer(topicFeedIdList,false);
						//feedAdapter=new TopicListFeedAdapter(mSubTopicList,false,null,SubTopicListActivity.this,SubTopicListActivity.this,list,progressBar,mGroupId,mGroupName,mTagImage,groupObject.get(Constants.GROUP_TYPE).toString());
						//mListView.setAdapter(feedAdapter);
					}
			}});
		
	}
	private void getFeedFromServer(ArrayList<String> feedIdList,final boolean isRefresh)
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
							if(isRefresh)
							{
								ParseObject.unpinAllInBackground(mTagId,
						                  new DeleteCallback() {
						                       @Override
						                    public void done(ParseException e) {
						                    	   if(e == null) {
						                    		   ParseObject.pinAllInBackground(mTagId,list,new SaveCallback() {
						   					            @Override
						   					            public void done(ParseException e) {
						   					                if(e == null) {
						   					                	//setAdapter();
						   					                	onCreate(null);
						   					                }
						   					            }});
						                    	   }
						                       }});
							}
							else
							{
								ParseObject.pinAllInBackground(mTagId,list,new SaveCallback() {
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
							feedAdapter=new TopicListFeedAdapter(mSubTopicList,false,null,SubTopicListActivity.this,SubTopicListActivity.this,list,progressBar,mGroupId,mGroupName,mTagImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mListView.setAdapter(feedAdapter);
						}
					}
					else
					{
						feedAdapter=new TopicListFeedAdapter(mSubTopicList,false,null,SubTopicListActivity.this,SubTopicListActivity.this,list,progressBar,mGroupId,mGroupName,mTagImage,groupObject.get(Constants.GROUP_TYPE).toString());
						mListView.setAdapter(feedAdapter);
					}
			}});
		
	}
	
	
	private void getTagFromParse()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.SUB_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.whereEqualTo(Constants.TAG_ID_STRING, mTagId);
		query.whereNotEqualTo(Constants.SUB_TAG_STATUS, "InActive");
		//query.include(Constants.GROUP_ID);
		//query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							 ParseObject.pinAllInBackground(mGroupId+Constants.SUB_TAG_LOCAL_DATA_STORE,list,new SaveCallback() {
								@Override
								public void done(ParseException arg0) {
									progressBar.setVisibility(View.GONE);
									getSubTopics();
								}
							});
						}
						else
						{
							setAdapter();
							progressBar.setVisibility(View.GONE);
						}
					}
					else
					{
						setAdapter();
						progressBar.setVisibility(View.GONE);
					}
			}});
		
	}
	
	private void refreshTopicsFeed()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.TAG_ID_STRING,mTagId);
		
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (object != null) 
					{
						progressBar.setVisibility(View.GONE);
						 topicFeedIdList=new ArrayList<String>();
						 topicFeedIdList=(ArrayList<String>) object.get(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
						 onCreate(null);
					}
					else
					{
						progressBar.setVisibility(View.GONE);
						
					}
			}});
	}

	private void initViews() {
		//mBackImgView=(ImageView)findViewById(R.id.back);
		mListView=(ListView) findViewById(android.R.id.list);
		//mGroupNameTxtView=(TextView) findViewById(R.id.topic_name);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		
		mTagId=getIntent().getStringExtra(Constants.TAG_ID_STRING);
		mTagName=getIntent().getStringExtra(Constants.TAG_NAME);
		mTagImage=getIntent().getStringExtra(Constants.TAG_IMAGE);
		topicFeedIdList=getIntent().getStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
		System.out.println("mTagImage :: "+mTagImage);
		if(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE)==null)
			mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		else
			mGroupImage=groupObject.getParseFile(Constants.THUMBNAIL_PICTURE).getUrl();
		
		//mGroupNameTxtView.setText(mTagName);
		mTopicImgView=(ImageView) findViewById(R.id.image_header);
		floatButton=(ButtonFloat) findViewById(R.id.buttonFloat);
		floatButton.setVisibility(View.GONE);
		newPostBtn=(Button) findViewById(R.id.new_post);
		
		refreshImage=(FrameLayout) findViewById(R.id.refresh);
		
		refreshImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshImage.setVisibility(View.INVISIBLE);
				//refreshTopicsFeed();
				getFeedFromServer(topicFeedIdList, true);
			}
		});
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;
		int h1=widthPixels/3;
		int height = 2*h1;
		mTopicImgView.setLayoutParams(new RelativeLayout.LayoutParams(widthPixels,height));
		mTopicImgView.setAdjustViewBounds(true);
		mTopicImgView.setScaleType(ScaleType.CENTER_CROP);
		System.out.println("width :::: height"+widthPixels+"    "+height);
		if(mTagImage.equals("NoImage"))
			Picasso.with(this).load(mGroupImage).into(mTopicImgView);
		else
			Picasso.with(this).load(mTagImage).into(mTopicImgView);

		setTitle(mTagName);
		gpsTracker=new GPSTracker(this);
		
		if (!gpsTracker.canGetLocation())
        {
			gpsTracker.showSettingsAlert(this);
        }
		
		
		feedInActiveList.add("InActive");
		feedInActiveList.add("Inactive");
		
		
	/*	
		mBackImgView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});*/
		
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
	
	public void getNext100Records()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereContainedIn(Constants.OBJECT_ID, topicFeedIdList);
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
							ParseObject.pinAllInBackground(mTagId,remainingList,new SaveCallback() {
					            @Override
					            public void done(ParseException e) {
					                if(e == null) {
					                	objectList.addAll(remainingList);
					                	feedAdapter.setList(objectList);
					                	feedAdapter.notifyDataSetChanged();
					                }
					            }});
						}
					}
			}});
    	
    	
	}
	


}
