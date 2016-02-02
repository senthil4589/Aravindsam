package com.group.nearme;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.VideoView;

import com.embedly.api.Api;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.group.nearme.adapter.PostQueryAdapter;
import com.group.nearme.adapter.PublicPostAdapter;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class GroupPostListActivity extends YouTubeBaseActivity {
	Api api = new Api("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2", "eea3a0754b2146698c346dddc5adf262");
	HashMap<String, Object> linkParams;
	
	public PullToRefreshListView mListView;
	private PublicPostAdapter mAdapter;
	private TextView mPostTxtView;
	private ImageView mPostImgView;
	private EditText mPostEditTxt;
	private String mPostText="",mImageCaption="";
	private String mGroupId="",mGroupName="",mGroupImage="";
	private ArrayList<String> list=new ArrayList<String>();
	private ProgressBar progressBar;
	public Dialog mDialog;
	public static boolean flag=false,flag1;
	public int position=0; 
	ParseObject groupObject;
	public GPSTracker gpsTracker;
	ParseGeoPoint point;
	TextView spannableText;
	RelativeLayout createLayout,bottomLayout;
	Button newPostBtn;
	int localCount,serverCount;
	PostQueryAdapter  postsQueryAdapter;
	ArrayList<ParseObject> objectList=new ArrayList<ParseObject>();
	String linkTitle="",linkImage="",linkImagePath;
	private int imgWidth,imgHeight;
	byte[] imgByte;
	private ParseFile mPostImgFile,mPostImageThumbnail;
	String siteName="";
	public static ParseFile mPostImgFile1;
	ArrayList<String> inactiveList=new ArrayList<String>();
	
	RelativeLayout welcomePostLayout;
	//TextView groupDescription;
	//ParseImageView groupImage;
	//boolean isCitySelected=false;
	boolean isFirstTime=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list_view);
		Utility.getTracker(this, "GROUP FEED SCREEN");
		initViews();
	
		mPostTxtView.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mPostText=mPostEditTxt.getText().toString();
				
				if(mPostText.isEmpty())
				{
					 Utility.showToastMessage(GroupPostListActivity.this, getResources().getString(R.string.post_text_empty));
				}
				else
				{
					InputMethodManager inputManager1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
					inputManager1.hideSoftInputFromWindow( GroupPostListActivity.this.getCurrentFocus().getWindowToken(),
			        InputMethodManager.HIDE_NOT_ALWAYS); 
					if (gpsTracker.canGetLocation())
			        {
						if(Utility.checkInternetConnectivity(GroupPostListActivity.this))
						{
							 if(Patterns.WEB_URL.matcher(mPostText).matches())
							 {
								 progressBar.setVisibility(View.VISIBLE);
								 mPostTxtView.setEnabled(false);
								( new ParseURL() ).execute(new String[]{mPostText});
							 }
							 else
							 {
								 System.out.println("not valid url");
								 insertValues();
							 }
						}
						else
							Utility.showToastMessage(GroupPostListActivity.this, getResources().getString(R.string.no_network));
			        }
			        else
			        {
			            gpsTracker.showSettingsAlert(GroupPostListActivity.this);
			        }
					
				}
			}
		});
		
		mPostImgView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Utility.checkInternetConnectivity(GroupPostListActivity.this))
				{	
					uploadImage();
				}
				else
					Utility.showToastMessage(GroupPostListActivity.this, getResources().getString(R.string.no_network));
				
			}
		});
		
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(GroupPostListActivity.this))
				{
					//slideToTop(welcomePostLayout);
					//welcomePostLayout.setVisibility(View.GONE);
					 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
	              		query.whereEqualTo(Constants.OBJECT_ID, groupObject.getObjectId());
	              		query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object, ParseException e) {
									if (object!=null) 
									{
										Utility.setGroupObject(object);
										groupObject=Utility.getGroupObject();
										
										mGroupId=groupObject.getObjectId();
										mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
										mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
										
										if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
										{
											mPostImgView.setVisibility(View.GONE);
											InputFilter[] FilterArray = new InputFilter[1];
											FilterArray[0] = new InputFilter.LengthFilter(200);
											mPostEditTxt.setFilters(FilterArray);
										}
										setAdapter();
									}
							}
	              		});
					
					
				}
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(GroupPostListActivity.this, getResources().getString(R.string.no_network));
				}
			}
			
			});
		newPostBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//slideToTop(welcomePostLayout);
				//welcomePostLayout.setVisibility(View.GONE);
				newPostBtn.setVisibility(View.GONE);
				setAdapter();
			}
		});
		setAdapter();
		mListView.setOnScrollListener(new OnScrollListener(){
		    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		    	
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
	}
	
	public void slideToTop(RelativeLayout view){
		TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
		animate.setDuration(500);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.GONE);
		}
	
	public void uploadImage()
    {
    	mDialog = new Dialog(this,R.style.customDialogStyle);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.choose_image_video);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		TextView cameraImage=(TextView) mDialog.findViewById(R.id.take_photo);
		TextView galleryImage=(TextView) mDialog.findViewById(R.id.from_gallery);
		TextView video=(TextView) mDialog.findViewById(R.id.video);
		TextView gif=(TextView) mDialog.findViewById(R.id.gif);
		
		cameraImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(GroupPostListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("share", false).putExtra("flag", true).putExtra("type", "CameraImage"));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				mDialog.dismiss();
			}
		});
		galleryImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(GroupPostListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("flag", false).putExtra("type", "GalleryImage"));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				mDialog.dismiss();
			}
		});
		video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(GroupPostListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("flag", false).putExtra("type", "Video"));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				mDialog.dismiss();
			}
		});
		gif.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(GroupPostListActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("flag", false).putExtra("type", "GIF"));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				mDialog.dismiss();
			}
		});
		mDialog.show();
    }
	
	
	private void insertValues()
	{
		System.out.println("inside insertvalues");
		progressBar.setVisibility(View.VISIBLE);
		mPostTxtView.setEnabled(false);
		ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.GROUP_ID, mGroupId);
		userObject.put(Constants.POST_TEXT, mPostText);
		/*if(userImage!=null)
			userObject.put(Constants.MEMBER_IMAGE, userImage);*/
		 userObject.put(Constants.POST_TYPE, "Text");
		 userObject.put(Constants.COMMENT_COUNT, 0);
		 userObject.put(Constants.FLAG_COUNT, 0);
			userObject.put(Constants.IMAGE_CAPTION, mImageCaption);
			userObject.put(Constants.LIKE_ARRAY, list);
			userObject.put(Constants.DIS_LIKE_ARRAY, list);
			userObject.put(Constants.POST_POINT, 100);
			if(groupObject.getBoolean(Constants.POST_APPROVAL) && !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				userObject.put(Constants.POST_STATUS, "Pending");
			else
				userObject.put(Constants.POST_STATUS, "Active");
			userObject.put(Constants.FLAG_ARRAY, list);
			userObject.put(Constants.FEED_LOCATION, point);
			//userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
			userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
			userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
			userObject.put(Constants.HASH_TAG_ARRAY, list);
			System.out.println("befroe text post");
			
			
			userObject.pinInBackground(mGroupId,new SaveCallback() {
	            @Override
	            public void done(ParseException e) {
	                if(e == null) {
	                	progressBar.setVisibility(View.GONE);
	                	if(groupObject.getBoolean(Constants.POST_APPROVAL) && !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
	                		Utility.showToastMessage(GroupPostListActivity.this, getResources().getString(R.string.post_approval_text));
	                	setAdapter();
	                }
	            }});
			userObject.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException arg0) {
				System.out.println("after text post");
				progressBar.setVisibility(View.GONE);
				
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
				query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object1, ParseException e) {
							if (e == null) 
							{
								if(object1!=null)
								{
									object1.increment(Constants.BADGE_POINT, 100);
									object1.saveInBackground(new SaveCallback() {
									          public void done(ParseException e) {
								                 if (e == null) {
								                	 System.out
														.println("before setadapter");
		             							//setAdapter();
								                 }
									          }});
								}
							}
						}
					});
			
				
			}
		});
		}				
	
	public void setAdapter() {
		mPostTxtView.setEnabled(true);
		if(groupObject.getInt(Constants.WHO_CAN_POST)==1 )
		{
			if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				bottomLayout.setVisibility(View.VISIBLE);
			else
				bottomLayout.setVisibility(View.GONE);
		}
		else
			bottomLayout.setVisibility(View.VISIBLE);
		
		createLayout.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		mPostEditTxt.setText("");
		mPostText="";
		System.out.println("before get feed table ::: "+Utility.getCurrentDate());
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
				query.whereNotContainedIn(Constants.POST_STATUS, inactiveList);
		}
		else
				query.whereEqualTo(Constants.POST_STATUS, "Active");
		
		query.include(Constants.USER_ID);
		query.setLimit(100);
		try
		{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
		catch(Exception e){}
		query.fromPin(mGroupId);
		
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						//System.out.println("inside local storage   :: "+groupObject.get(Constants.GROUP_NAME).toString());	
						if(list.size() > 0)
						{
							/*if(list.size()==1 && list.get(position).get(Constants.POST_TYPE).toString().equals("Member"))
							{
								
							}*/
							System.out.println("after get feed table ::: "+Utility.getCurrentDate());
							localCount=list.size();
							
							mAdapter=new PublicPostAdapter(GroupPostListActivity.this,list,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mListView.setAdapter(mAdapter);
							progressBar.setVisibility(View.GONE);
							if(mListView.isRefreshing())
								mListView.onRefreshComplete();
							
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
							query.findInBackground(new FindCallback<ParseObject>() {
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
									                    	  if(isFirstTime)
									                    	  {
									                    		  isFirstTime=false;
									                    	   if(serverCount > localCount)
									                    	   {
									                    		   newPostBtn.setVisibility(View.VISIBLE);
									                    	   }
									                    	   else
									                    	   {
									                    		   newPostBtn.setVisibility(View.GONE);  
									                    	   }
									                    	   
									                    	   }
									                    	   } 
									                       }
												 });
												
											}
										}
								}});
							
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
							mAdapter=new PublicPostAdapter(GroupPostListActivity.this,list1,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mListView.setAdapter(mAdapter);
							progressBar.setVisibility(View.GONE);
							if(mListView.isRefreshing())
								mListView.onRefreshComplete();

						}
					}
			}});
			}
	
	private void getDataFromServer()
	{
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
							objectList=(ArrayList<ParseObject>) list;
							ParseObject.pinAllInBackground(mGroupId,list,new SaveCallback() {
					            @Override
					            public void done(ParseException e) {
					                if(e == null) {
					                	//setAdapter();
					                	showFirstTwenty();
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
										query.setSkip(20);
										query.setLimit(80);
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
													                	mAdapter.setList(objectList);
													                	mAdapter.notifyDataSetChanged();
													                }
													            }});
														}
													}
											}});
					                	
					                	
					                }}});
						}
						else
						{
							if(mListView.isRefreshing())
								mListView.onRefreshComplete();

							progressBar.setVisibility(View.GONE);
							createLayout.setVisibility(View.VISIBLE);
							//spannableText.setText(getResources().getString(R.string.after_create_group_system_post));
							spannableText.setMovementMethod(LinkMovementMethod.getInstance());
							spannableText.setText(addClickablePart(getResources().getString(R.string.after_create_group_system_post),63,76), BufferType.SPANNABLE);
						}
					}
			}});
		
		progressBar.setVisibility(View.GONE);

	}
	
	private class ParseURL extends AsyncTask<String, Void, Bitmap> {
		
	     @Override
	     protected Bitmap doInBackground(String... strings) {
	    
	         try {
	        	 linkParams=new HashMap<String, Object>();
	        	 linkParams.put("url",strings[0]);
	        	 JSONArray oembed = api.oembed(linkParams);
	        	 
	        	 linkImage = oembed.getJSONObject(0).getString("thumbnail_url");
	        	 linkTitle=oembed.getJSONObject(0).getString("title");
	        	 siteName=oembed.getJSONObject(0).getString("provider_name");
	        	 
	        	 if(siteName.isEmpty())
	        	 {
	        		 URL url=new URL(strings[0]);
					 siteName=url.getHost();
	        	 }
	        	 
	        	 if(!linkImage.isEmpty())
	        	 {
	        		 Bitmap bitmap=getImageBitmapFromUrl(new URL(linkImage));
                	 return bitmap;
	        	 }
	             }
	         catch(Exception t) {
	        	System.out.println("exception  :: "+t);
	           return null;
	         }
	 
	         return null;
	     }
	 
	     @Override
	     protected void onPostExecute(Bitmap bitmap) {
	         super.onPostExecute(bitmap);
	         
	       if(bitmap!=null)
	       {
	    	   if(!linkTitle.isEmpty() && !linkImage.isEmpty())
	    	   {
	    		   ByteArrayOutputStream stream = new ByteArrayOutputStream();
	   			   bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
	               imgByte = stream.toByteArray();
	               mPostImgFile1= new ParseFile("Image.png", imgByte);
	              // mPostImgFile.saveInBackground();
	               imgWidth=bitmap.getWidth();
	   			   imgHeight=bitmap.getHeight();
	   			   mPostImgFile1.saveInBackground(new SaveCallback() {
	  	          public void done(ParseException e) {
	  	                 if (e == null) {
	  	                	// insertLinkValues();
	  	                	 progressBar.setVisibility(View.GONE);
	  	                	mPostTxtView.setEnabled(true);
	  	                	startActivity(new Intent(GroupPostListActivity.this,ShareLinkActivity.class).putExtra("isShare", false).putExtra(Constants.GROUP_ID, mGroupId)
	  	                			.putExtra("link_title",linkTitle).putExtra(Constants.LINK_URL,mPostText).putExtra(Constants.SITE_NAME,siteName)
	  	                			.putExtra(Constants.IMAGE_WIDTH,imgWidth).putExtra(Constants.IMAGE_HEIGHT,imgHeight));
	  	                	overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	  	                 }
	  	          }});
	    		   
	    	   }
	    	   else
	    	   {
	    		   insertValues();
	    	   }
	    	   
	       }
	       else
    	   {
    		   insertValues();
    	   }
	     }
	}
	
	private String saveImageFromUrl()
	{
		try
		{   
		  URL url = new URL(linkImage);
		  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		  urlConnection.setRequestMethod("GET");
		  urlConnection.setDoOutput(true);                   
		  urlConnection.connect();                  
		  File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();
		  String filename="downloadedFile.png";   
		  Log.i("Local filename:",""+filename);
		  File file = new File(SDCardRoot,filename);
		  if(file.createNewFile())
		  {
		    file.createNewFile();
		  }                 
		  FileOutputStream fileOutput = new FileOutputStream(file);
		  InputStream inputStream = urlConnection.getInputStream();
		  int totalSize = urlConnection.getContentLength();
		  int downloadedSize = 0;   
		  byte[] buffer = new byte[1024];
		  int bufferLength = 0;
		  while ( (bufferLength = inputStream.read(buffer)) > 0 ) 
		  {                 
		    fileOutput.write(buffer, 0, bufferLength);                  
		    downloadedSize += bufferLength;                 
		    Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize) ;
		  }             
		  fileOutput.close();
		  if(downloadedSize==totalSize) linkImagePath=file.getPath();    
		} 
		catch (MalformedURLException e) 
		{
		  e.printStackTrace();
		} 
		catch (IOException e)
		{
			linkImagePath=null;
		  e.printStackTrace();
		}
		Log.i("filepath:"," "+linkImagePath) ;
		return linkImagePath;
	}
	
	 private void generateThumbnail(Bitmap bitmap)
	    {
	    	//Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,200,200);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mPostImageThumbnail = new ParseFile("ThumbImage.png", img);
			mPostImageThumbnail.saveInBackground();
	    }

	 public Bitmap getImageBitmapFromUrl(URL url)
	  { 
	    Bitmap bm = null; 
	    try { 
	      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	      if(conn.getResponseCode() != 200)
	      {
	        return bm;
	      }
	      conn.connect();
	      InputStream is = conn.getInputStream();

	      BufferedInputStream bis = new BufferedInputStream(is); 
	      try
	      {
	        bm = BitmapFactory.decodeStream(bis); 
	      }
	      catch(OutOfMemoryError ex)
	      {
	        bm = null;
	      }
	      bis.close(); 
	      is.close(); 
	    } catch (Exception e) {}
	    
	      return bm; 
	  }
	  
	
	private void showFirstTwenty()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		//if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		//{
			query.whereNotEqualTo(Constants.POST_TYPE, "Member");
		//}
		
		if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
			query.whereNotEqualTo(Constants.POST_STATUS, "InActive");
		else
			query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.include(Constants.USER_ID);
		query.setLimit(100);
		try
		{query.orderByDescending(Constants.FEED_UPDATED_TIME);}
		catch(Exception e){}
		query.fromPin(mGroupId);
		
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						//System.out.println("inside local storage   :: "+groupObject.get(Constants.GROUP_NAME).toString());	
						if(list.size() > 0)
						{
							mAdapter=new PublicPostAdapter(GroupPostListActivity.this,list,progressBar,mGroupId,mGroupName,mGroupImage,groupObject.get(Constants.GROUP_TYPE).toString());
							mListView.setAdapter(mAdapter);
							progressBar.setVisibility(View.GONE);
							if(mListView.isRefreshing())
								mListView.onRefreshComplete();
						}
					}
					else
					{
						System.out.println("get first twenty exception ::: "+e);
					}
			}});
	}
	
	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
	    ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
            	startActivity(new Intent(GroupPostListActivity.this,ContactListActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
            }
        }, idx1, idx2, 0);
  	    return ssb;
	}
	
	
	private void initViews() {
		mListView=(PullToRefreshListView) findViewById(R.id.feed_list_view);
		mPostTxtView=(TextView) findViewById(R.id.post);
		mPostImgView=(ImageView) findViewById(R.id.camera);
		mPostEditTxt=(EditText) findViewById(R.id.text_box);
		gpsTracker=new GPSTracker(GroupPostListActivity.this);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		spannableText=(TextView) findViewById(R.id.spannable_text);
		createLayout=(RelativeLayout) findViewById(R.id.create_layout);
		bottomLayout=(RelativeLayout) findViewById(R.id.layout);
		newPostBtn=(Button) findViewById(R.id.new_post);
		
		//groupImage=(ParseImageView) findViewById(R.id.group_image);
		//groupDescription=(TextView) findViewById(R.id.group_des);
		//welcomePostLayout=(RelativeLayout) findViewById(R.id.welcome_post_layout);
		
		groupObject=Utility.getGroupObject();
		
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		
		//groupImage.setParseFile(groupObject.getParseFile(Constants.GROUP_PICTURE));
		//groupImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
		//groupImage.loadInBackground();
		
		//groupDescription.setText(groupObject.get(Constants.GROUP_DESCRIPTION).toString());
		
		if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		{
			mPostImgView.setVisibility(View.GONE);
			InputFilter[] FilterArray = new InputFilter[1];
			FilterArray[0] = new InputFilter.LengthFilter(200);
			mPostEditTxt.setFilters(FilterArray);
		}
		
		inactiveList.add("InActive");
		inactiveList.add("Inactive");
	}
	
		
	 @Override
	protected void onResume() {
		super.onResume();
		if(flag)
		{
			groupObject=Utility.getGroupObject();
			
			mGroupId=groupObject.getObjectId();
			mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
			mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
			
			if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
			{
				mPostImgView.setVisibility(View.GONE);
				InputFilter[] FilterArray = new InputFilter[1];
				FilterArray[0] = new InputFilter.LengthFilter(200);
				mPostEditTxt.setFilters(FilterArray);
			}
			
			flag=false;
			setAdapter();
		}
		if(flag1)
		{
			TabGroupPostActivity.mGroupNameTxtView.setText(Utility.getGroupObject().get(Constants.GROUP_NAME).toString());
			Picasso.with(this).load(Utility.getGroupObject().getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).into(TabGroupPostActivity.mGroupImgView);
		}
	}
	 
	 @Override
	public void onBackPressed() {
		super.onBackPressed();
		//MyGroupListActivity.flag1=true;
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
