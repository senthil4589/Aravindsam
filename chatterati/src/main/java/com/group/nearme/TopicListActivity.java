package com.group.nearme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView.BufferType;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.PublicPostAdapter;
import com.group.nearme.adapter.TopicListFeedAdapter;
import com.group.nearme.fadingheader.FadingActionBarHelper;
import com.group.nearme.floatbutton.ButtonFloat;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.ScrollListView;
import com.group.nearme.util.ScrollListView.OnBottomReachedListener;
import com.group.nearme.util.Utility;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class TopicListActivity extends Activity
{
	ScrollListView mFeedListView;
	//private ImageView mBackImgView;//,
	ImageView mGroupImgView;
	//private TextView mGroupNameTxtView;
	private ParseObject groupObject;
	private String mGroupId="",mGroupName="",mGroupImage="",mMobileNo="";
	private List<ParseObject> mTopicList=new ArrayList<ParseObject>();
	List<ParseObject> objectList;//=new ArrayList<ParseObject>();
	private ProgressBar progressBar;
	ArrayList<String> inactiveList=new ArrayList<String>();
	public GPSTracker gpsTracker;
	public TopicListFeedAdapter feedAdapter;
	public Button newPostBtn;
	public int localCount,serverCount;
	public boolean isFirstTime=true;
	ButtonFloat floatButton;
	public Dialog mDialog;
	public static boolean flag;
	ImageView mediaImage;
	FrameLayout refreshImage;
	TextView memberCount;
	RelativeLayout memberLayout;
	public static TopicListActivity activity;
	 boolean isCitySelected;
	LinearLayout businessLayout;
	ImageView callImage,locationImage;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
	/*	ImageView image=new ImageView(this);
        image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,500));
        image.setImageResource(R.drawable.theri);
        image.setScaleType(ScaleType.FIT_XY);*/
        
		FadingActionBarHelper helper = new FadingActionBarHelper()
        .actionBarBackground(R.drawable.ab_background)
       .headerLayout(R.layout.header)
        .contentLayout(R.layout.activity_listview)
   .headerOverlayLayout(R.layout.header_overlay);
    setContentView(helper.createView(this));
    
    helper.initActionBar(this);
    
		//setContentView(R.layout.topic_feed_listview);
		Utility.getTracker(this, "TOPIC LIST SCREEN");
		initViews();
		activity=this;
		/*root=(SwipeRefreshLayout) findViewById(R.id.root_layout);
		//root.setEnabled(false);
		root.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				root.setRefreshing(true);
				setTagAdapter();
					
			}
			
		});
*//*	mFeedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
	        @Override
	        public void onScrollStateChanged(AbsListView view, int scrollState) {

	        }

	        @Override
	        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	        	int topRowVerticalPosition = (mFeedListView == null || mFeedListView.getChildCount() == 0) ? 0 : mFeedListView.getChildAt(0).getTop();
	            root.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
	        }
	    });
*/		
/*		root.post(new Runnable() {
            @Override
            public void run() {
            	root.setRefreshing(true);

            	setTagAdapter();
            }
        }
);*/
		
		/*mFeedListView.setOnScrollListener(new OnScrollListener() {
		    @Override
		    public void onScrollStateChanged(AbsListView view, int scrollState) {
		    }

		    @Override
		    public void onScroll(AbsListView view, int firstVisibleItem,
		                int visibleItemCount, int totalItemCount) {

		       final int lastItem = firstVisibleItem + visibleItemCount;
		       if(lastItem == totalItemCount) {
		           //load more data
		    	   System.out.println("sroll bottom reached ----------------============");
		    	   
		    	   getNext100Records();
		    	   
		       }
		       
		       int position = firstVisibleItem+visibleItemCount;
		        int limit = totalItemCount - mOffset;

		        // Check if bottom has been reached
		        if (position >= limit && totalItemCount > 0) {
		            if (mListener != null ) {
		                mListener.onBottomReached();
		            }
		        }
		    }
		});
		
*/		
		mFeedListView.setOnBottomReachedListener(new OnBottomReachedListener() {
			
			@Override
			public void onBottomReached() {
				// TODO Auto-generated method stub
				 System.out.println("sroll bottom reached ----------------============");
				getNext100Records();
				
			}
			
			
			
		});
		
		
		//com.group.nearme.util.PullToRefreshListView refresh=(com.group.nearme.util.PullToRefreshListView) findViewById(R.id.refresh_listview);

		
		/*refresh.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
			System.out.println("inside pullto refresh");
			}
		});
		
*/		
		
		
		getActionBar().setHomeButtonEnabled(true);
		
		/*mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(TopicListActivity.this))
				{
					progressBar.setVisibility(View.VISIBLE);
					getTagFromParse();
				}
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(TopicListActivity.this, getResources().getString(R.string.no_network));
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
				onCreate(null);
				//setFeedAdapter();
			}
		});
		
		
		
	}

	private void setTagAdapter() {
		mTopicList=new ArrayList<ParseObject>();
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.whereNotEqualTo(Constants.TAG_STATUS, "InActive");
		query.include(Constants.GROUP_ID);
		query.orderByDescending(Constants.TAG_RANK);
		query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							progressBar.setVisibility(View.GONE);
							mTopicList=list;
							int count=list.size();
							ArrayList<ParseObject> indexList=new ArrayList<ParseObject>();
							for(int i=0;i<count;i++)
							{
								if(mTopicList.get(i).getInt(Constants.TAG_POST_COUNT)==0)
								{
									System.out.println("index :: "+i);
									indexList.add(mTopicList.get(i));
								}
								
							}
							System.out.println("index List :: "+indexList);
							for(int j=0;j<indexList.size();j++)
							{
								mTopicList.remove(indexList.get(j));
							}
							
							setFeedAdapter();
							
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
							query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
							query.whereNotEqualTo(Constants.TAG_STATUS, "InActive");
							query.include(Constants.GROUP_ID);
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											if(list.size() > 0)
											{
												ParseObject.unpinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,
										                  new DeleteCallback() {
										                       @Override
										                    public void done(ParseException e) {
										                    	   if(e == null) {
										                    	   ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,list);
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
	
	private void getTagFromParse()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.whereNotEqualTo(Constants.TAG_STATUS, "InActive");
		query.include(Constants.GROUP_ID);
		//query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							 ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,list,new SaveCallback() {
								@Override
								public void done(ParseException arg0) {
									progressBar.setVisibility(View.GONE);
									setTagAdapter();
								}
							});
						}
						else
						{
							progressBar.setVisibility(View.GONE);
							//if(root.isRefreshing())
								//root.setRefreshing(false);
							setFeedAdapter();
						}
					}
					else
					{
						progressBar.setVisibility(View.GONE);
						//if(root.isRefreshing())
						//	root.setRefreshing(false);
						setFeedAdapter();
					}
			}});
		
	}

	private void initViews() {
		//mBackImgView=(ImageView)findViewById(R.id.back);
		//mGroupImgView=(ImageView) findViewById(R.id.group_image);
		mFeedListView=(ScrollListView) findViewById(android.R.id.list);
		//mGroupNameTxtView=(TextView) findViewById(R.id.group_name);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		if(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE)==null)
			mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		else
			mGroupImage=groupObject.getParseFile(Constants.THUMBNAIL_PICTURE).getUrl();
		setTitle(mGroupName);
		//mGroupNameTxtView.setText(mGroupName);
		mGroupImgView=(ImageView) findViewById(R.id.image_header);
		newPostBtn=(Button) findViewById(R.id.new_post);
		floatButton=(ButtonFloat) findViewById(R.id.buttonFloat);
		
		refreshImage=(FrameLayout) findViewById(R.id.refresh);
		mediaImage=(ImageView) findViewById(R.id.media);
		memberCount=(TextView) findViewById(R.id.member_count);
		memberLayout=(RelativeLayout) findViewById(R.id.group_member_layout);

		callImage=(ImageView) findViewById(R.id.call_image);
		locationImage=(ImageView) findViewById(R.id.location_image);
		businessLayout=(LinearLayout) findViewById(R.id.business_group_layout);
		try
		{
			if(groupObject.getString(Constants.GROUP_PURPOSE).equals("Business Groups")) {
				businessLayout.setVisibility(View.VISIBLE);
				final HashMap<String, Object> groupAttribute=(HashMap<String, Object>) groupObject.get(Constants.GROUP_ATTRIBUTE);
				locationImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						double lat = (double) groupAttribute.get(Constants.LATITUDE);
						double lng = (double) groupAttribute.get(Constants.LONGTITUDE);
						startActivity(new Intent(activity, EventLocationActivity.class)
								.putExtra(Constants.LATITUDE, lat).putExtra(Constants.LONGTITUDE, lng)
								.putExtra("event_name", groupAttribute.get(Constants.BUSINESS_NAME).toString()));

					}
				});

				callImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String no = "tel:" + groupAttribute.get(Constants.BUSINESS_PHONE_NUMBER).toString();
						Uri number = Uri.parse(no);
						Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
						startActivity(callIntent);

					}
				});



			}
			else {
				businessLayout.setVisibility(View.GONE);
			}
		}
		catch (Exception e){
			businessLayout.setVisibility(View.GONE);
		}

		
		memberCount.setText(groupObject.getInt(Constants.MEMBER_COUNT)+"");
		
		refreshImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshImage.setVisibility(View.INVISIBLE);
				refresh();
				//onCreate(null);
				//setTagAdapter();
			}
		});
		
		memberLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(TopicListActivity.this,MemberListActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		mediaImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent= new Intent(TopicListActivity.this, MediaPostActivity.class);
		        intent.putExtra(Constants.GROUP_NAME, mGroupName);
		        intent.putExtra(Constants.GROUP_PICTURE, mGroupImage);
		        intent.putExtra(Constants.GROUP_ID, mGroupId);
		        intent.putExtra("from", true);
		        startActivity(intent);
			}
		});
		
		if(groupObject.getInt(Constants.WHO_CAN_POST)==1 )
		{
			if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				floatButton.setVisibility(View.VISIBLE);
			else
				floatButton.setVisibility(View.GONE);
		}
		else
			floatButton.setVisibility(View.VISIBLE);
		
		
		
		floatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				uploadImage();
				overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.slide_out_to_bottom);
			}
		});
		//Picasso.with(this).load(mGroupImage).into(mGroupImgView);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;
		int h1=widthPixels/3;
		int height = 2*h1;
		mGroupImgView.setLayoutParams(new RelativeLayout.LayoutParams(widthPixels,height));
		mGroupImgView.setAdjustViewBounds(true);
		mGroupImgView.setScaleType(ScaleType.CENTER_CROP);
		System.out.println("width :::: height"+widthPixels+"    "+height);
		Picasso.with(this).load(mGroupImage).into(mGroupImgView);
		
		
		setTagAdapter();
		/*mBackImgView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});*/
		inactiveList.add("InActive");
		inactiveList.add("Inactive");
		gpsTracker=new GPSTracker(this);
		
		if (!gpsTracker.canGetLocation())
        {
			gpsTracker.showSettingsAlert(this);
        }
		
		/*isCitySelected=getIntent().getBooleanExtra(Constants.CITY_GROUP_VISIBILTY, false);
		if(isCitySelected)
			showAlert();*/
		
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

	
	
	private void refresh()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.whereNotEqualTo(Constants.TAG_STATUS, "InActive");
		query.include(Constants.GROUP_ID);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							ParseObject.unpinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,
					                  new DeleteCallback() {
					                       @Override
					                    public void done(ParseException e) {
					                    	   if(e == null) {
					                    	   ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,list);
					                    	   
					                    	   
					                    	   } 
					                       }
								 });
						}
					}
			}});
		
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query1.whereEqualTo(Constants.GROUP_ID, mGroupId);
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			query1.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
			query1.whereEqualTo(Constants.POST_STATUS, "Active");
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query1.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
			try
			{query1.orderByDescending(Constants.FEED_UPDATED_TIME);}
			catch(Exception e){}
		
		query1.include(Constants.USER_ID);
		query1.setLimit(100);
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> feedList, ParseException e) {
					if (e == null) 
					{
						if(feedList.size() > 0)
						{
							serverCount=feedList.size();
							 ParseObject.unpinAllInBackground(mGroupId,
				                  new DeleteCallback() {
				                       @Override
				                    public void done(ParseException e) {
				                    	   if(e == null) {
				                    		   
				                    	   ParseObject.pinAllInBackground(mGroupId,feedList);
				                    	 /*  if(isFirstTime)
					                    	  {
					                    		 // isFirstTime=false;
					                    	   if(serverCount > localCount)
					                    	   {
					                    		   newPostBtn.setVisibility(View.VISIBLE);
					                    	   }
					                    	   else
					                    	   {
					                    		   newPostBtn.setVisibility(View.GONE);  
					                    	   }
					                    	   
					                    	   }*/
					                    	onCreate(null);
				                    	   } 
				                       }
							 });
							
						}
						else
							onCreate(null);
					}
					else
						onCreate(null);
			}});
	
		
	}
	
	public void uploadImage()
    {
    	mDialog = new Dialog(this,R.style.customDialogStyle);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.choose_media);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		mDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
		windowManager.width = WindowManager.LayoutParams.MATCH_PARENT;
		windowManager.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowManager.gravity = Gravity.BOTTOM;
		mDialog.getWindow().setAttributes(windowManager);

		/*TextView cameraImage=(TextView) mDialog.findViewById(R.id.take_photo);
		TextView galleryImage=(TextView) mDialog.findViewById(R.id.from_gallery);
		TextView video=(TextView) mDialog.findViewById(R.id.video);
		TextView gif=(TextView) mDialog.findViewById(R.id.gif);*/
		
		RelativeLayout camera=(RelativeLayout) mDialog.findViewById(R.id.camera_layout);
		RelativeLayout gallery=(RelativeLayout) mDialog.findViewById(R.id.gallery_layout);
		RelativeLayout textPost=(RelativeLayout) mDialog.findViewById(R.id.text_post_layout);
		RelativeLayout video=(RelativeLayout) mDialog.findViewById(R.id.video_layout);
		RelativeLayout gif=(RelativeLayout) mDialog.findViewById(R.id.gif_layout);
		RelativeLayout product=(RelativeLayout) mDialog.findViewById(R.id.product_layout);
		View line=(View) mDialog.findViewById(R.id.product_line);
		try {
			if (groupObject.getString(Constants.GROUP_PURPOSE).equals("Business Groups")) {
				product.setVisibility(View.VISIBLE);
				line.setVisibility(View.VISIBLE);
			} else {
				product.setVisibility(View.GONE);
				line.setVisibility(View.GONE);
			}
		}
		catch (Exception e){
			product.setVisibility(View.GONE);
			line.setVisibility(View.GONE);
		}
		
		if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		{
			camera.setVisibility(View.GONE);
			gallery.setVisibility(View.GONE);
			video.setVisibility(View.GONE);
			gif.setVisibility(View.GONE);
		}
		else
		{
			camera.setVisibility(View.VISIBLE);
			gallery.setVisibility(View.VISIBLE);
			video.setVisibility(View.VISIBLE);
			gif.setVisibility(View.VISIBLE);
			textPost.setVisibility(View.VISIBLE);
		}
		
		
			mDialog.show();
			
			camera.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(TopicListActivity.this, PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("share", false).putExtra("flag", true).putExtra("type", "CameraImage"));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					mDialog.dismiss();
				}
			});
			
			gallery.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(TopicListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("share", false).putExtra("flag", false).putExtra("type", "GalleryImage"));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					mDialog.dismiss();
				}
			});

			
			textPost.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(TopicListActivity.this,TextPostActivity.class));
					mDialog.dismiss();
				}
			});

			video.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(TopicListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("flag", false).putExtra("type", "Video"));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					mDialog.dismiss();
				}
			});
			gif.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(TopicListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("flag", false).putExtra("type", "GIF"));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					mDialog.dismiss();
				}
			});

			product.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(TopicListActivity.this,ProductComposeActivity.class));
					mDialog.dismiss();
				}
			});

    }
	
	
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.activity_menu, menu);
	        return true;
	    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    int id = item.getItemId();

	    if (id == android.R.id.home) {
	        onBackPressed();
	    }
	   /* else  if (id == R.id.media) {
	    	Intent intent= new Intent(this, MediaPostActivity.class);
	        intent.putExtra(Constants.GROUP_NAME, mGroupName);
	        intent.putExtra(Constants.GROUP_PICTURE, mGroupImage);
	        intent.putExtra(Constants.GROUP_ID, mGroupId);
	        intent.putExtra("from", true);
	        startActivity(intent);
	    }
*/	    else  if (id == R.id.settings) {
	    	startActivity(new Intent(TopicListActivity.this,NewGroupInfoActivity.class));
	    	overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	    }

	    return super.onOptionsItemSelected(item);	
	 }
	
	public void setFeedAdapter() {
		System.out.println("inside fedd adapter");
		
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
				query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
				query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.include(Constants.USER_ID);
		query.setLimit(1000);
		try
		{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
		catch(Exception e){}
		query.fromPin(mGroupId);
		
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						System.out.println("feed list ::: "+list.size());
						//System.out.println("inside local storage   :: "+groupObject.get(Constants.GROUP_NAME).toString());	
						objectList=list;
						if(list.size() > 0)
						{
							//System.out.println("index1 ::: "+list.get(0).getDate(Constants.FEED_UPDATED_TIME));
							//System.out.println("index1 ::: "+list.get(7).getDate(Constants.FEED_UPDATED_TIME));
							//System.out.println("index1 ::: "+list.get(8).getDate(Constants.FEED_UPDATED_TIME));
							localCount=list.size();
							feedAdapter=new TopicListFeedAdapter(mTopicList,true,TopicListActivity.this,null,TopicListActivity.this,list,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mFeedListView.setAdapter(feedAdapter);
							progressBar.setVisibility(View.GONE);
							
							refreshImage.setVisibility(View.VISIBLE);
							//if(mFeedListView.isRefreshing())
								//mFeedListView.onRefreshComplete();
							
							//if(root.isRefreshing())
								//root.setRefreshing(false);
							if(isFirstTime)
							{
								isFirstTime=false;
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
							query.whereEqualTo(Constants.GROUP_ID, mGroupId);
							
							if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
							{
								query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
							}
							else
								query.whereEqualTo(Constants.POST_STATUS, "Active");
							//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
							//{
								query.whereNotEqualTo(Constants.POST_TYPE, "Member");
							//}
							query.include(Constants.USER_ID);
							//query.orderByDescending("updatedAt");
							query.setLimit(100);
							query.orderByDescending(Constants.FEED_UPDATED_TIME);
							
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> feedList, ParseException e) {
										if (e == null) 
										{
											if(feedList.size() > 0)
											{
												if(!feedList.get(0).getDate(Constants.FEED_UPDATED_TIME).equals(list.get(0).getDate(Constants.FEED_UPDATED_TIME)))		
												{
															ParseObject.unpinAllInBackground(mGroupId,
											                  new DeleteCallback() {
											                       @Override
											                    public void done(ParseException e) {
											                    	   if(e == null) {
											                    	   ParseObject.pinAllInBackground(mGroupId,feedList);
											                    	   newPostBtn.setVisibility(View.VISIBLE);
											                    	   } 
											                       }
														 });			
															
														}						
											}
										}
								}});
							}
							
						}
						else
						{
							getDataFromServer();
						}
					}
					else
					{
						//getDataFromServer();
						getParseData();
						System.out.println("feed exception ::: "+e);
						//progressBar.setVisibility(View.GONE);
					}
			}
		});
	}
	
	private void getParseData()
	{
		System.out.println("inside getParseData");
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
			query.whereEqualTo(Constants.POST_STATUS, "Active");
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
		query.include(Constants.USER_ID);
		query.setLimit(100);
		query.orderByDescending(Constants.FEED_UPDATED_TIME);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list1, ParseException e) {
					if (e == null) 
					{
						if(list1.size() > 0)
						{
							feedAdapter=new TopicListFeedAdapter(mTopicList,true,TopicListActivity.this,null,TopicListActivity.this,list1,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());;
							mFeedListView.setAdapter(feedAdapter);
							progressBar.setVisibility(View.GONE);
							refreshImage.setVisibility(View.VISIBLE);
							//if(mFeedListView.isRefreshing())
								//mFeedListView.onRefreshComplete();
							//if(root.isRefreshing())
								//root.setRefreshing(false);
						}
						else
						{
							//if(root.isRefreshing())
								//root.setRefreshing(false);
						}
					}
					else
					{
						//if(root.isRefreshing())
							//root.setRefreshing(false);
					}
			}});
			}
	
	private void getDataFromServer1()
	{
		System.out.println("inside getDataFromServer ");
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
			query.whereEqualTo(Constants.POST_STATUS, "Active");
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
		query.include(Constants.USER_ID);
		query.setLimit(100);
		query.orderByDescending(Constants.FEED_UPDATED_TIME);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							ParseObject.pinAllInBackground(mGroupId,list,new SaveCallback() {
					            @Override
					            public void done(ParseException e) {
					                if(e == null) {
					                	setFeedAdapter();
					                }
					                }});
						}
						else
						{
							//if(mTopicList.size() > 0)
							//{
								feedAdapter=new TopicListFeedAdapter(mTopicList,true,TopicListActivity.this,null,TopicListActivity.this,list,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());
								mFeedListView.setAdapter(feedAdapter);
							//}
							progressBar.setVisibility(View.GONE);

						}
					}
			}});
	}
	
	private void getDataFromServer()
	{
		isFirstTime=false;
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
			query.whereEqualTo(Constants.POST_STATUS, "Active");
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
		query.include(Constants.USER_ID);
		query.setLimit(20);
		query.orderByDescending(Constants.FEED_UPDATED_TIME);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							objectList= list;
							ParseObject.pinAllInBackground(mGroupId,list,new SaveCallback() {
					            @Override
					            public void done(ParseException e) {
					                if(e == null) {
					                	//setAdapter();
					                	//showFirstTwenty();
					                	setFeedAdapter();
					                	
					                }}});
						}
						else
						{
							progressBar.setVisibility(View.GONE);
							feedAdapter=new TopicListFeedAdapter(mTopicList,true,TopicListActivity.this,null,TopicListActivity.this,list,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mFeedListView.setAdapter(feedAdapter);
						}
					}
			}});
		
		progressBar.setVisibility(View.GONE);

	}
	
	public void getNext100Records()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
			query.whereEqualTo(Constants.POST_STATUS, "Active");
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
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
							ParseObject.pinAllInBackground(mGroupId,remainingList,new SaveCallback() {
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
	
	@Override
	protected void onResume() {
		super.onResume();
		if(flag)
		{
			flag=false;
			onCreate(null);
		}
	}
}
