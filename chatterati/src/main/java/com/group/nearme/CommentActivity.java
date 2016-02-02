package com.group.nearme;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.BufferType;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.CommentAdapter;
import com.group.nearme.adapter.FeedHashTagAdapter;
import com.group.nearme.adapter.PublicPostAdapter.VideoDownloadTask;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.NonScrollListView;
import com.group.nearme.util.ResizableImageView;
import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class CommentActivity extends Activity {
	private ImageView mBackImg;
	private ParseImageView mGroupImgView, userImage;
	private String mGroupId="",mGroupName="",mGroupImage="",mPostText="",userImageUrl="",updateTime="";
	private ProgressBar progressBar;
	private TextView mGroupNameTxtView;
	private TextView mPostTxtView,comment,sourceUrl;
	private EditText mCommentEditTxt;
	private NonScrollListView mListView;
	private int position;
	private TextView userName,postText,updatedTime,points,replies,imageCaption,linkDes,linkUrl,stypeAbstract;
	private ImageView upVote,downVote,flag,delete,hashTag,shareImage;
	private ResizableImageView postImage;
	public Dialog mDialog;
	String flagValue="";
	List<ParseObject> list;
	boolean likeFlag,unLikeFlag;
	CommentAdapter adapter;
	int post_points=0,comment_count=0;
	PullToRefreshScrollView scrollView;
	AlertDialog mAlertDialog;
	public GPSTracker gpsTracker;
	ParseObject groupObject;
	ArrayList<List<String>> tempLikeList,tempDisLikeList;
	private RelativeLayout bottomLayout;
	FeedHashTagAdapter hashTagFeedAdapter=null;
	FrameLayout postImageFrame;
	Typeface tf;
	ProgressBar videoProgress;
	VideoView videoView;
	FrameLayout sVideoPlayImage,playImage;
	ResizableImageView sVideoBg;
	ProgressBar sVideoProgress;
	RelativeLayout sVideoLayout;
	boolean isFileDownloading=false;
	TextView eventStartEndDate,eventStartEndTime,eventLocationAddress1,eventLocationAddress2;
	TextView eventShortMonth,eventShortDate,eventAreaName;
	RelativeLayout eventLocationRootLayout,eventDateRootLayout;

	TextView listPrice,offerPercentage,offerPrice;
	LinearLayout productPriceRootLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.comment_list_view);
		Utility.getTracker(this, "COMMENT SCREEN");
		initViews();
		
		updateValue();
		setCommentAdapter();
		
		
		
		scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				if(Utility.checkInternetConnectivity(CommentActivity.this))
				{
					ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
					//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
					query1.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
					query1.whereEqualTo(Constants.FEED_TYPE, "Comment");
					//query.orderByAscending("updatedAt");
					query1.include(Constants.USER_ID);
					query1.findInBackground(new FindCallback<ParseObject>() {
						public void done(final List<ParseObject> list2, ParseException e) {
								if (e == null) 
								{
									if(list2.size() > 0)
									{
									ParseObject.unpinAllInBackground(list.get(position).getObjectId(),new DeleteCallback() {
										@Override
										public void done(ParseException arg0) {
											ParseObject.pinAllInBackground(list.get(position).getObjectId(),list2,
													new SaveCallback() {
												@Override
												public void done(ParseException arg0) {
													setCommentAdapter();
												}
												});
										}
									});
									}
									else
									{
										if(scrollView.isRefreshing())
											scrollView.onRefreshComplete();
									}
									
								}
								else
								{
									if(scrollView.isRefreshing())
										scrollView.onRefreshComplete();
									progressBar.setVisibility(View.GONE);
								}
						}
					});
				}
				else
				{
					progressBar.setVisibility(View.GONE);
					if(scrollView.isRefreshing())
						scrollView.onRefreshComplete();
					Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.no_network));
				}
			}
			
			});
	}
	
	
	private void updateValue()
	{
		comment_count=list.get(position).getInt(Constants.COMMENT_COUNT);
		//points.setText(list.get(position).getInt(Constants.POST_POINT)+" Points");
		points.setText(list.get(position).getInt(Constants.POST_POINT)+"");
		mPostTxtView.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mPostText=mCommentEditTxt.getText().toString();
				if(mPostText.isEmpty())
				{
					Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.comment_text_empty));
				}
				else
				{
					InputMethodManager inputManager1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
					inputManager1.hideSoftInputFromWindow( CommentActivity.this.getCurrentFocus().getWindowToken(),
			        InputMethodManager.HIDE_NOT_ALWAYS); 
					
					if (gpsTracker.canGetLocation())
			        {
						if(Utility.checkInternetConnectivity(CommentActivity.this))
						{	
							insertValues();
						}
						else
							Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.no_network));
			        }
			        else
			        {
			            gpsTracker.showSettingsAlert(CommentActivity.this);
			        }
					//insertValues();
				}
			}
		});
		
		if(list.get(position).getList(Constants.LIKE_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			System.out.println("inside like");
			downVote.setEnabled(false);
			upVote.setEnabled(true);
			upVote.setImageResource(R.drawable.up2);
			downVote.setImageResource(R.drawable.down1);
		}
		else if(list.get(position).getList(Constants.DIS_LIKE_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			System.out.println("inside dislike");
			upVote.setEnabled(false);
			downVote.setEnabled(true);
			downVote.setImageResource(R.drawable.down2);
			upVote.setImageResource(R.drawable.up1);
		}
		else
		{
			System.out.println("inside like else");
			downVote.setEnabled(true);
			upVote.setEnabled(true);
			//viewHolder.upVote.setEnabled(true);
			upVote.setImageResource(R.drawable.up1);
			downVote.setImageResource(R.drawable.down1);
		}
		final int point=list.get(position).getInt(Constants.POST_POINT);
		upVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Utility.checkInternetConnectivity(CommentActivity.this))
				{	
				if(tempLikeList.get(0).contains(PreferenceSettings.getMobileNo()))
				{
					//points.setText(point+" Points");
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					downVote.setEnabled(true);
					upVote.setEnabled(true);
					System.out.println("inside if click like ");
					tempLikeList.get(0).remove(PreferenceSettings.getMobileNo());
					updateLike(false,position,points);
				}
				else
				{
					//int newPoint=point+50;
					//points.setText(newPoint+" Points");
					upVote.setImageResource(R.drawable.up2);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(false);
					System.out.println("inside else click like ");
					tempLikeList.get(0).add(PreferenceSettings.getMobileNo());
					updateLike(true,position,points);
				}
				}
				else
					Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.no_network));
			}
		});
		
		downVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Utility.checkInternetConnectivity(CommentActivity.this))
				{	
				if(tempDisLikeList.get(0).contains(PreferenceSettings.getMobileNo()))
				{
					//points.setText(point+" Points");
					System.out.println("inside if click dislike ");
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(true);
					tempDisLikeList.get(0).remove(PreferenceSettings.getMobileNo());
					updateDislike(false,position,points);
					
				}
				else
				{
					//int newPoint=point-50;
					//points.setText(newPoint+" Points");
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down2);
					downVote.setEnabled(true);
					upVote.setEnabled(false);
					tempDisLikeList.get(0).add(PreferenceSettings.getMobileNo());
					System.out.println("inside else click dislike ");
					updateDislike(true,position,points);
				}
			}
			else
				Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.no_network));
			}
		});
		
		
		
		flag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Utility.checkInternetConnectivity(CommentActivity.this))
				{	
				if(list.get(position).getList(Constants.FLAG_ARRAY).contains(PreferenceSettings.getMobileNo()))
				{
					Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.flag_already_done));
					//Toast.makeText(CommentActivity.this, "You already done", Toast.LENGTH_LONG).show();
				}
				else
				{
					showFlagDialog(position);
				}
			}
			else
				Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.no_network));
			}
			
		});
		
		if(list.get(position).get(Constants.MOBILE_NO).toString().equals(PreferenceSettings.getMobileNo())
		  || Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			delete.setVisibility(View.VISIBLE);
			flag.setVisibility(View.GONE);
			delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utility.checkInternetConnectivity(CommentActivity.this))
					{	
					showDeleteAlertDialog(position);
				}
				else
					Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.no_network));
				}
				
			});
		}
		else
		{
			flag.setVisibility(View.VISIBLE);
			delete.setVisibility(View.GONE);
		}
		postImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//callCommentActivity(position);
				postImage.setEnabled(false);
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				{
					if(list.get(position).getString(Constants.SITE_NAME).equals("YouTube"))
					{
						startActivity(new Intent(CommentActivity.this,YoutubePlayerActivity.class).putExtra("title", list.get(position).getString(Constants.IMAGE_CAPTION)).putExtra(Constants.VIDEO_ID, list.get(position).get(Constants.VIDEO_ID).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
					else
					{
						startActivity(new Intent(CommentActivity.this,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
						//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
						
						//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("SVideo"))
				{
					isFileDownloading=true;
					ParseFile file=(ParseFile) list.get(position).get(Constants.POST_VIDEO);
					playImage.setVisibility(View.GONE);
					videoProgress.setVisibility(View.VISIBLE);
					file.getDataInBackground(new GetDataCallback() {
						@Override
						public void done(byte[] arg0, ParseException arg1) {
							isFileDownloading=false;
							videoProgress.setVisibility(View.GONE);
							playImage.setVisibility(View.VISIBLE);
							byte[] videoByte=arg0;
							System.out.println("insid get data");
							// new VideoDownloadTask1(position,progressBar).execute();
							File file = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/Videos");
						    if (!file.exists()) {
						        file.mkdirs();
						    }
						    File file1 = new File(file.getAbsolutePath() + "/" + "SVideo" + ".mp4");
					    	
						    if(file1.exists()){
								file1.delete();
							}
						    
						    String uriSting = (file.getAbsolutePath() + "/" + "SVideo" + ".mp4");
						    try
							{
								long total = 0;
								InputStream input =new ByteArrayInputStream(videoByte);
								OutputStream output = new FileOutputStream(uriSting);
								byte data[] = new byte[videoByte.length];
								int count;
								while ((count = input.read(data)) != -1) {
									 total += count;
								    output.write(data, 0, count);
								}
							}
							catch(Exception e){
								System.out.println("inside video write catch "+e);
							}
						    startActivity(new Intent(CommentActivity.this,VideoViewActivity.class).putExtra("video", uriSting));
							
						}
					}, new ProgressCallback() {
						@Override
						public void done(Integer progress) {
							videoProgress.setProgress(progress);
						}
					});
				}
				else
				{
					Utility.setParseFile(list.get(position).getParseFile(Constants.POST_IMAGE));
					startActivity(new Intent(CommentActivity.this,ImageFullViewActivity.class)
					.putExtra(Constants.GROUP_NAME, mGroupName)
					.putExtra(Constants.POST_IMAGE, list.get(position).getParseFile(Constants.POST_IMAGE).getUrl()));
				}
				
			}
		});
		
		linkUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			   if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					startActivity(new Intent(CommentActivity.this,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.CREDIT_URL).toString()));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			   else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link")
						|| list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				{
					startActivity(new Intent(CommentActivity.this,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			}
		});
		
		sourceUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			   if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					startActivity(new Intent(CommentActivity.this,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			}
		});
		
		
	}
	
	public  void showDeleteAlertDialog(final int position){
		mAlertDialog=new AlertDialog.Builder(this)
		.setMessage("Are you sure you want to delete this post?")
		.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progressBar.setVisibility(View.VISIBLE);
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
				query.whereEqualTo(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID));
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> l1, ParseException e) {
							if (e == null) 
							{
								if(l1.size() > 0)
								{
									l1.get(0).deleteInBackground(new DeleteCallback() {
		      							          public void done(ParseException e) {
			 							                 if (e == null) {
			 							                	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
			 							                	query.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
			 							                	query.findInBackground(new FindCallback<ParseObject>() {
			 							                		public void done(List<ParseObject> l2, ParseException e) {
			 							   							if (e == null) 
			 							   							{
			 							   								if(l2.size() > 0)
			 							   								{
			 							   									ParseObject.deleteAllInBackground(l2, new DeleteCallback() {
			 							   										public void done(ParseException e) {
			 							   											if (e == null) {
			 							   												progressBar.setVisibility(View.GONE);
			 							   											Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.post_delete));
			 							   												GroupPostListActivity.flag=true;
			 							   												finish();
			 							   											}
			 							   									}});
			 							   								}
			 							   								else
			 															progressBar.setVisibility(View.GONE);
			 							   							}
			 							   							else
			 														progressBar.setVisibility(View.GONE);
			 							                		}});
			 							                 }
		      							          }});
								}
								else
									progressBar.setVisibility(View.GONE);
							}
							else
								progressBar.setVisibility(View.GONE);
					}});
			}
		}).setNegativeButton("No", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mAlertDialog.dismiss();
			}
		}).show();
	}
	
	
	 public void setListViewHeightBasedOnChildren(ListView listView) {
	        ListAdapter listAdapter = listView.getAdapter();
	        if (listAdapter == null)
	            return;

	        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
	        int totalHeight = 0;
	        View view = null;
	        for (int i = 0; i < listAdapter.getCount(); i++) {
	            view = listAdapter.getView(i, view, listView);
	            if (i == 0)
	                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

	            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
	            totalHeight += view.getMeasuredHeight();
	        }
	        ViewGroup.LayoutParams params = listView.getLayoutParams();
	        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	        listView.setLayoutParams(params);
	        listView.requestLayout();
	    }
	
	
	private void insertValues()
	{
		//mPostTxtView.setEnabled(enabled)
		GroupPostListActivity.flag=true;
		progressBar.setVisibility(View.VISIBLE);
		post_points=post_points+100;
		comment_count=comment_count+1;
		//points.setText(post_points+" Points");
		points.setText(post_points+"");
		//replies.setText(comment_count+" Replies");
		comment.setText("Comments ("+comment_count+")");
		ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		final ParseObject userObject = new ParseObject(Constants.ACTIVITY_TABLE);
		userObject.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
		userObject.put(Constants.FEED_ID, list.get(position).getObjectId());
		userObject.put(Constants.FEED_TYPE, "Comment");
		userObject.put(Constants.COMMENT_TEXT, mPostText);
		userObject.put(Constants.USER_NAME, PreferenceSettings.getUserName());
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.UP_VOTE, false);
		userObject.put(Constants.DOWN_VOTE, false);
		userObject.put(Constants.FLAG_VALUE, "");
		//userObject.put(Constants.USER_PICTURE, userImageFile);
		userObject.put(Constants.ACTIVITY_LOCATION, point);
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		/*userObject.pinInBackground(list.get(position).getObjectId(),new SaveCallback() {
			@Override
			public void done(ParseException arg0) {
				setCommentAdapter();
			}
		});*/
		
		userObject.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException arg0) {
				userObject.pinInBackground(list.get(position).getObjectId(),new SaveCallback() {
					@Override
					public void done(ParseException arg0) {
						setCommentAdapter();
					}
				});
				//setCommentAdapter();
			
				//setCommentAdapter();
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object1, ParseException e) {
							if (e == null) 
							{
								if(object1!=null)
								{
									object1.increment(Constants.COMMENT_COUNT,1);
									object1.increment(Constants.POST_POINT,100);
									object1.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
									object1.saveInBackground();
									ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
				                	query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
				                	query.getFirstInBackground(new GetCallback<ParseObject>() {
				    					public void done(ParseObject userObj, ParseException e) {
				    							if (e == null) 
				    							{
				    								if(userObj!=null)
				    								{
				    									userObj.increment(Constants.BADGE_POINT, 100);
				    									userObj.saveInBackground();
				                						}
				                					}
				                			}});
								}
							}
					}
				});
			}
		});
		
	}
	
	private void showFlagDialog(final int position)
	{
		mDialog = new Dialog(this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.flag_view);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		final RadioGroup flagGroup=(RadioGroup) mDialog.findViewById(R.id.flag_group);
		Button send=(Button) mDialog.findViewById(R.id.send);
		Button cancel=(Button) mDialog.findViewById(R.id.cancel);
		
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int selectedId = flagGroup.getCheckedRadioButtonId();
				 if(selectedId!=-1){
					 RadioButton radioSexButton = (RadioButton) mDialog.findViewById(selectedId);
					 flagValue=radioSexButton.getText().toString();
				 	if(flagValue.equals(""))
				 		Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.flag_empty));
				 	else
				 	{
				 		mDialog.dismiss();
				 		updateFlagInTable(position);
				 	}
				 }
				 else
					 Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.flag_empty));
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		mDialog.show();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGroupImgView=(ParseImageView) findViewById(R.id.group_image);
		mGroupNameTxtView=(TextView) findViewById(R.id.group_name);
		mListView=(NonScrollListView) findViewById(R.id.listview);
		mPostTxtView=(TextView) findViewById(R.id.post);
		mCommentEditTxt=(EditText) findViewById(R.id.text_box);
		progressBar=(ProgressBar) findViewById(R.id.progressBar1);
		scrollView=(PullToRefreshScrollView) findViewById(R.id.scrollView);
		postImage=(ResizableImageView) findViewById(R.id.post_image);
		userImage=(ParseImageView) findViewById(R.id.user_pic);
		upVote=(ImageView) findViewById(R.id.up_vote);
		downVote=(ImageView) findViewById(R.id.down_vote);
		//share=(ImageView) findViewById(R.id.share);
		flag=(ImageView) findViewById(R.id.flag);
		postText=(TextView) findViewById(R.id.post_text);
		imageCaption=(TextView) findViewById(R.id.image_caption);
		userName=(TextView) findViewById(R.id.user_name);
		updatedTime=(TextView) findViewById(R.id.updated_time);
		points=(TextView) findViewById(R.id.points);
		replies=(TextView) findViewById(R.id.comments_count);
		delete=(ImageView) findViewById(R.id.delete);
		comment=(TextView) findViewById(R.id.comment_title);
		hashTag=(ImageView) findViewById(R.id.hash);
		bottomLayout=(RelativeLayout) findViewById(R.id.layout);
		
		linkDes=(TextView) findViewById(R.id.link_des);
		linkUrl=(TextView) findViewById(R.id.link_url);
		stypeAbstract=(TextView) findViewById(R.id.stype_abstract);
		sourceUrl=(TextView) findViewById(R.id.source_url);
		
		playImage=(FrameLayout) findViewById(R.id.video_play_frame);
		postImageFrame=(FrameLayout) findViewById(R.id.post_image_layout);
		postImageFrame=(FrameLayout) findViewById(R.id.post_image_layout);
		
		shareImage=(ImageView) findViewById(R.id.share);
		videoProgress=(ProgressBar) findViewById(R.id.progressBar);
		
		videoView = (VideoView) findViewById(R.id.videoView);
		sVideoLayout=(RelativeLayout) findViewById(R.id.svideo_layout);
		sVideoPlayImage=(FrameLayout) findViewById(R.id.svideo_play_frame);
		sVideoBg=(ResizableImageView) findViewById(R.id.svideo_bg_image);
		sVideoProgress=(ProgressBar) findViewById(R.id.video_progrss);
		
		eventShortMonth=(TextView) findViewById(R.id.event_start_month);
		eventShortDate=(TextView) findViewById(R.id.event_start_day);
		eventStartEndDate=(TextView) findViewById(R.id.event_start_end_date_txt);
		eventStartEndTime=(TextView) findViewById(R.id.event_start_end_time_txt);
		eventLocationAddress1=(TextView) findViewById(R.id.event_address_line1);
		eventLocationAddress2=(TextView) findViewById(R.id.event_address_line2);
		eventDateRootLayout=(RelativeLayout) findViewById(R.id.event_date_time_root_layout);
		eventLocationRootLayout=(RelativeLayout) findViewById(R.id.event_location_root_layout);
		eventAreaName=(TextView) findViewById(R.id.area_name);


		listPrice=(TextView) findViewById(R.id.actual_price);
		offerPercentage=(TextView) findViewById(R.id.offer_percentage);
		offerPrice=(TextView) findViewById(R.id.offer_price);

		productPriceRootLayout=(LinearLayout) findViewById(R.id.product_price_layout);


		mGroupId=getIntent().getStringExtra(Constants.GROUP_ID);
		mGroupName=getIntent().getStringExtra(Constants.GROUP_NAME);
		mGroupImage=getIntent().getStringExtra(Constants.GROUP_PICTURE);
		userImageUrl=getIntent().getStringExtra(Constants.PROFILE_PICTURE);
		updateTime=getIntent().getStringExtra("updatedtime");
		position=getIntent().getIntExtra("position",0);
		gpsTracker=new GPSTracker(this);
		tempLikeList=new ArrayList<List<String>>();
		tempDisLikeList=new ArrayList<List<String>>();
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		groupObject=Utility.getGroupObject();
		list=Utility.getList();
		View line=(View) findViewById(R.id.line1);
		
		RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		FrameLayout.LayoutParams param1=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,list.get(position).getInt(Constants.IMAGE_HEIGHT));
		param1.leftMargin=10;
		param1.rightMargin=10;
		param.addRule(RelativeLayout.BELOW,stypeAbstract.getId());
		postImageFrame.setLayoutParams(param);
		postImage.setAdjustViewBounds(true);
		
		
		if(groupObject.getString(Constants.GROUP_TYPE).equals("Open"))
			shareImage.setVisibility(View.VISIBLE);
		else
			shareImage.setVisibility(View.GONE);	
		
		
		
		if(groupObject.get(Constants.GROUP_TYPE).toString().equals("Public"))
		{
			userName.setVisibility(View.GONE);
			updatedTime.setTextSize(12);
			updatedTime.setPadding(8, 15, 5, 5);
			line.setPadding(0, 10, 0, 0);
			userImage.setVisibility(View.GONE);
			InputFilter[] FilterArray = new InputFilter[1];
			FilterArray[0] = new InputFilter.LengthFilter(200);
			mCommentEditTxt.setFilters(FilterArray);
		}
		
		if(groupObject.getInt(Constants.WHO_CAN_COMMENT)==1)
			if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				bottomLayout.setVisibility(View.VISIBLE);
			else
				bottomLayout.setVisibility(View.GONE);
		else
			bottomLayout.setVisibility(View.VISIBLE);
		
		mGroupNameTxtView.setText(mGroupName);
		//Picasso.with(this).load(mGroupImage).into(mGroupImgView);
		Picasso.with(this).load(Utility.getList().get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).into(userImage);
		mGroupImgView.setParseFile(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE));
		mGroupImgView.loadInBackground();
		//userImage.setParseFile(Utility.getUserImageFile());
		//userImage.loadInBackground();
		userName.setText(Utility.getList().get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
		updatedTime.setText(updateTime);
		
		if(Utility.getList().get(position).getInt(Constants.COMMENT_COUNT)>0)
			comment.setText("Comments ("+Utility.getList().get(position).get(Constants.COMMENT_COUNT).toString()+")");
		
		
		
		post_points=Utility.getList().get(position).getInt(Constants.POST_POINT);
		
		
		
		if(Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo())
				 || list.get(position).getString(Constants.MOBILE_NO).equals(PreferenceSettings.getMobileNo()))
		{	
			hashTag.setVisibility(View.VISIBLE);
			
			hashTag.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
					query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
					query.include(Constants.GROUP_ID);
					query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
					query.findInBackground(new FindCallback<ParseObject>() {
						public void done(final List<ParseObject> tagList, ParseException e) {
								if (e == null) 
								{
									if(tagList.size() > 0)
									{
										if(list.get(position).getList(Constants.HASH_TAG_ARRAY) !=null)
										{
											List<String> feedList=list.get(position).getList(Constants.HASH_TAG_ARRAY);
											showManageHashTagDialog(tagList,list.get(position).getObjectId(),feedList);
										}
									}
									else
									{
										Utility.showToastMessage(CommentActivity.this, "No tags available for this group");
									}
								}
						}});
					
				}
			});
		}
		else
		{
			hashTag.setVisibility(View.GONE);
		}
		if(list.get(position).getString(Constants.POST_STATUS).equals("Active") && list.get(position).get(Constants.POST_TYPE).toString().equals(Constants.PRODUCT_POST))
		{
			stypeAbstract.setVisibility(View.VISIBLE);
			imageCaption.setVisibility(View.VISIBLE);
			postImage.setVisibility(View.VISIBLE);


			HashMap<String, Object> map=(HashMap<String, Object>) list.get(position).get(Constants.POST_ATTRIBUTE);
			listPrice.setText(map.get(Constants.PRODUCT_ACTUAL_PRICE).toString());
			offerPrice.setText(map.get(Constants.PRODUCT_OFFER_PERCENTAGE).toString());
			offerPrice.setText(map.get(Constants.PRODUCT_OFFER_PRICE).toString());


			postText.setVisibility(View.GONE);
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			linkDes.setVisibility(View.GONE);
			linkUrl.setVisibility(View.GONE);
			eventDateRootLayout.setVisibility(View.GONE);
			shareImage.setVisibility(View.GONE);


			stypeAbstract.setText(list.get(position).getString(Constants.ABSTRACT));
			imageCaption.setText(list.get(position).getString(Constants.IMAGE_CAPTION));
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			postImage.setParseFile(file);
			postImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
			postImage.loadInBackground();

		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Event"))
		{
			eventDateRootLayout.setVisibility(View.VISIBLE);
			eventLocationRootLayout.setVisibility(View.VISIBLE);
			stypeAbstract.setVisibility(View.VISIBLE);
			imageCaption.setVisibility(View.VISIBLE);
			postImage.setVisibility(View.VISIBLE);
			
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			postText.setVisibility(View.GONE);
			linkDes.setVisibility(View.GONE);
			linkUrl.setVisibility(View.GONE);
			
		
			
			stypeAbstract.setText(list.get(position).getString(Constants.ABSTRACT));
			imageCaption.setText(list.get(position).getString(Constants.IMAGE_CAPTION));
			
			HashMap<String, Object> map=(HashMap<String, Object>) list.get(position).get("PostAttribute");
			eventShortMonth.setText(map.get(Constants.SHORT_START_MONTH).toString());
			eventShortDate.setText(map.get(Constants.START_DATE).toString());
			if(map.get(Constants.START_DATE).toString().equals(map.get(Constants.END_DATE).toString())
					&& map.get(Constants.START_MONTH).toString().equals(map.get(Constants.END_MONTH).toString()))
				eventStartEndDate.setText(map.get(Constants.START_MONTH).toString()+" "+map.get(Constants.START_DATE).toString());
			else
				eventStartEndDate.setText(map.get(Constants.START_MONTH).toString()+" "+map.get(Constants.START_DATE).toString()
						+" - "+map.get(Constants.END_MONTH).toString()+" "+map.get(Constants.END_DATE).toString());
			
			eventStartEndTime.setText(map.get(Constants.START_TIME).toString()+" to "+map.get(Constants.END_TIME).toString());
			
			eventAreaName.setText(map.get(Constants.AREA_NAME).toString());
			eventLocationAddress2.setText(map.get(Constants.ADDRESS).toString());
			
			try{
				eventLocationAddress1.setText(map.get(Constants.PLACE_NAME).toString());
			}
			catch(Exception e){
			}
			
			
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			postImage.setParseFile(file);
			postImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
			postImage.loadInBackground();
			
			final Double latitude=(Double) map.get(Constants.LATITUDE);
			final Double longtitude=(Double) map.get(Constants.LONGTITUDE);
			eventLocationRootLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(CommentActivity.this,EventLocationActivity.class)
					.putExtra(Constants.LATITUDE, latitude).putExtra(Constants.LONGTITUDE, longtitude)
					.putExtra("event_name", list.get(position).getString(Constants.IMAGE_CAPTION)));
				}
			});

			
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Image"))
		{
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			stypeAbstract.setVisibility(View.GONE);
			//imageCaption.setPadding(0, 0, 0, 0);
			linkUrl.setVisibility(View.GONE);
			linkDes.setVisibility(View.GONE);
			if(Utility.getList().get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				imageCaption.setVisibility(View.GONE);
			}
			else
			{
				imageCaption.setVisibility(View.VISIBLE);
				imageCaption.setText(Utility.getList().get(position).get(Constants.IMAGE_CAPTION).toString());
			}
			postImage.setVisibility(View.VISIBLE);
			postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) Utility.getList().get(position).get(Constants.POST_IMAGE);
			postImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
			postImage.setParseFile(file);
			postImage.loadInBackground();
			//Picasso.with(this).load(file.getUrl()).into(postImage);
			
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("SVideo"))
		{
			sVideoBg.setVisibility(View.VISIBLE);
			videoView.setVisibility(View.GONE);
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.VISIBLE);
			stypeAbstract.setVisibility(View.GONE);
			linkUrl.setVisibility(View.GONE);
			linkDes.setVisibility(View.GONE);
			if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				imageCaption.setVisibility(View.GONE);
			}
			else
			{
				imageCaption.setVisibility(View.VISIBLE);
				imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
			}
			postImage.setVisibility(View.VISIBLE);
			postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			
			postImage.setParseFile(file);
			postImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
			postImage.loadInBackground();
			
			//Picasso.with(this).load(file.getUrl()).placeholder(getResources().getDrawable(R.drawable.group_image)).into(sVideoBg);
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("GIFVideo"))
		{
			 sVideoBg.setVisibility(View.VISIBLE);
			 videoView.setVisibility(View.GONE);
			sVideoLayout.setVisibility(View.VISIBLE);
			sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			stypeAbstract.setVisibility(View.GONE);
			linkUrl.setVisibility(View.GONE);
			linkDes.setVisibility(View.GONE);
			if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				imageCaption.setVisibility(View.GONE);
			}
			else
			{
				imageCaption.setVisibility(View.VISIBLE);
				imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
			}
			postImage.setVisibility(View.GONE);
			postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			
			file.getDataInBackground();
			
			Picasso.with(this).load(file.getUrl()).placeholder(getResources().getDrawable(R.drawable.group_image)).into(sVideoBg);
			final ViewTreeObserver observer = sVideoBg.getViewTreeObserver();
			observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					sVideoBg.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					int width = sVideoBg.getMeasuredWidth();
					int height = sVideoBg.getMeasuredHeight();
					RelativeLayout.LayoutParams videoParam = new RelativeLayout.LayoutParams(sVideoBg.getWidth(), sVideoBg.getHeight());
					videoParam.leftMargin = 20;
					videoParam.rightMargin = 20;
					//videoView.setBackgroundColor(Color.WHITE);
					videoParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					//videoParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					videoParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					videoParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					videoView.setLayoutParams(videoParam);
				}
			});
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link")||
				list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
		{
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.GONE);
			stypeAbstract.setVisibility(View.GONE);
			linkUrl.setPadding(0, 15, 0, 0);
			linkUrl.setTextSize(15);
			postText.setVisibility(View.GONE);
			
			postImage.setVisibility(View.VISIBLE);
			imageCaption.setVisibility(View.VISIBLE);
			imageCaption.setText(list.get(position).get(Constants.POST_TEXT).toString());
			//viewHolder.postText.setVisibility(View.VISIBLE);
			linkDes.setVisibility(View.VISIBLE);
			linkUrl.setVisibility(View.VISIBLE);
			
			linkDes.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());//+"\n\n"+list.get(position).get(Constants.LINK_URL).toString());
			linkUrl.setText(list.get(position).get(Constants.SITE_NAME).toString());
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			//viewHolder.postImage.setParseFile(file);
			//viewHolder.postImage.loadInBackground();
			//Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(viewHolder.postImage);
			postImage.setParseFile(file);
			postImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
			postImage.loadInBackground();
			
			if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				playImage.setVisibility(View.VISIBLE);
			else
				playImage.setVisibility(View.GONE);
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
		{
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.VISIBLE);
			playImage.setVisibility(View.GONE);
			stypeAbstract.setVisibility(View.VISIBLE);
			//imageCaption.setPadding(0, 10, 0, 0);
			linkUrl.setPadding(0, 0, 0, 0);
			linkUrl.setTextSize(13);
			postText.setVisibility(View.GONE);
			
			postImage.setVisibility(View.VISIBLE);
			imageCaption.setVisibility(View.VISIBLE);
			stypeAbstract.setText(list.get(position).getString(Constants.ABSTRACT));
			imageCaption.setText(list.get(position).getString(Constants.IMAGE_CAPTION));
			linkDes.setVisibility(View.GONE);
			//linkUrl.setVisibility(View.VISIBLE);
			
			 try {
					URL url=new URL(list.get(position).getString(Constants.LINK_URL));
					sourceUrl.setText("Source: "+url.getHost().replace("www.", ""));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			
			if(list.get(position).getString(Constants.CREDIT)==null)
			{
				linkUrl.setVisibility(View.GONE);
				//viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			}
			else
			{
				linkUrl.setVisibility(View.VISIBLE);
				linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			}
			
			//linkDes.setText(list.get(position).getString(Constants.IMAGE_CAPTION));//+"\n\n"+list.get(position).get(Constants.LINK_URL).toString());
			//linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			postImage.setParseFile(file);
			postImage.setPlaceholder(getResources().getDrawable(R.drawable.group_image));
			postImage.loadInBackground();
			linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			//linkUrl.setMovementMethod(LinkMovementMethod.getInstance());
			//linkUrl.setText(addClickablePart("Credit: "+list.get(position).getString(Constants.CREDIT),8,8+list.get(position).getString(Constants.CREDIT).length(),list.get(position).getString(Constants.CREDIT_URL)),BufferType.SPANNABLE);
			
		}
		else
		{
			sVideoLayout.setVisibility(View.GONE);
			sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			stypeAbstract.setVisibility(View.GONE);
			linkDes.setVisibility(View.GONE);
			linkUrl.setVisibility(View.GONE);
			postImage.setVisibility(View.GONE);
			imageCaption.setVisibility(View.GONE);
			postText.setVisibility(View.VISIBLE);
			postText.setText(Utility.getList().get(position).get(Constants.POST_TEXT).toString());
		}
		//list=Utility.getList();
		tempLikeList.add((List<String>) list.get(position).get(Constants.LIKE_ARRAY));
		tempDisLikeList.add((List<String>) list.get(position).get(Constants.DIS_LIKE_ARRAY));
		
		if(list.get(position).get(Constants.MOBILE_NO).toString().equals(PreferenceSettings.getMobileNo()))
		{
			flag.setVisibility(View.GONE);
		}
		else
		{
			flag.setVisibility(View.VISIBLE);
		}
		linkUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					startActivity(new Intent(CommentActivity.this,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.CREDIT_URL).toString()));
					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			}
		});
		
		shareImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utility.sharePost(CommentActivity.this,mGroupName,mGroupId,list.get(position).getObjectId());
			}
		});
		
		sVideoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("GIFVideo"))
				{
					 if(videoView.isPlaying())
					    {
						 	isFileDownloading=false;
					    	sVideoPlayImage.setVisibility(View.VISIBLE);
					    	videoView.stopPlayback();
					    }
					    else
					    {
							isFileDownloading=true;
							ParseFile file=(ParseFile) list.get(position).get(Constants.POST_VIDEO);
							sVideoPlayImage.setVisibility(View.GONE);
							sVideoProgress.setVisibility(View.VISIBLE);
							//videoProgress.setp
							file.getDataInBackground(new GetDataCallback() {
								@Override
								public void done(byte[] arg0, ParseException arg1) {
									isFileDownloading=false;
									sVideoProgress.setVisibility(View.GONE);
									sVideoPlayImage.setVisibility(View.VISIBLE);
									byte[] videoByte=arg0;
									System.out.println("insid get data");
									// new VideoDownloadTask1(position,progressBar).execute();
									File file = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/GIFVideos");
								    if (!file.exists()) {
								        file.mkdirs();
								    }
								    File file1 = new File(file.getAbsolutePath() + "/" + "GIFVideo" + ".mp4");
							    	
								    if(file1.exists()){
										file1.delete();
									}
								    
								    String uriSting = (file.getAbsolutePath() + "/" + "GIFVideo" + ".mp4");
								    try
									{
										long total = 0;
										InputStream input =new ByteArrayInputStream(videoByte);
										OutputStream output = new FileOutputStream(uriSting);
										byte data[] = new byte[videoByte.length];
										int count;
										while ((count = input.read(data)) != -1) {
											 total += count;
										    output.write(data, 0, count);
										}
									}
									catch(Exception e){
										System.out.println("inside video write catch "+e);
									}
								    sVideoPlayImage.setVisibility(View.GONE);
									videoView.setVisibility(View.VISIBLE);
							    	videoView.setVideoPath(uriSting);
								    videoView.requestFocus();
								    videoView.start();
								    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
								    {
								        @Override
								        public void onCompletion(MediaPlayer mp) 
								        {
								        	isFileDownloading=false;
								        	//videoView.start();
								        }
								        
								    });
								    videoView.setOnPreparedListener(new OnPreparedListener() {
							            public void onPrepared(MediaPlayer mp) {
							                sVideoBg.setVisibility(View.GONE);
							                System.out.println("inside setOnPreparedListener");
							                
							                mp.setVolume(0f, 0f);
							                mp.setLooping(true);
							                mp.start();
							                mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
							                    @Override
							                    public void onVideoSizeChanged(MediaPlayer mp, int arg1,
							                            int arg2) {
							                        sVideoProgress.setVisibility(View.GONE);
							                        sVideoPlayImage.setVisibility(View.GONE);
							                        mp.setVolume(0f, 0f);
									                mp.setLooping(true);
									               
							                        mp.start();
							                    }
							                });
							                
							            }
							        });
								    
							}
						}, new ProgressCallback() {
							@Override
							public void done(Integer progress) {
								sVideoProgress.setProgress(progress);
							}
						});
					    }
				}
			}
		});
		
				
	}
	
	
	public void showManageHashTagDialog(List<ParseObject> tagList,final String feedId,final List<String> alreadySelectedTag)
    {
    	mDialog = new Dialog(CommentActivity.this,R.style.customDialogStyle);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.manage_tags);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		
		Button save=(Button) mDialog.findViewById(R.id.save);
		Button cancel=(Button) mDialog.findViewById(R.id.cancel);
		final ListView listview=(ListView) mDialog.findViewById(R.id.listview);
		final ProgressBar progressBar1=(ProgressBar) mDialog.findViewById(R.id.progressBar);
		//listview.setPullToRefreshEnabled(false);
		progressBar1.setVisibility(View.GONE);
		hashTagFeedAdapter=new FeedHashTagAdapter(CommentActivity.this, tagList,alreadySelectedTag);
		listview.setAdapter(hashTagFeedAdapter);
		
		save.setTypeface(tf);
		cancel.setTypeface(tf);
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				
				progressBar.setVisibility(View.VISIBLE);
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, feedId);
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(final ParseObject object, ParseException e) {
							if (e == null) 
							{
								if(object!=null)
								{
									object.put(Constants.HASH_TAG_ARRAY, hashTagFeedAdapter.getSelectedList());
									object.saveInBackground(new SaveCallback() {
										
										@Override
										public void done(ParseException arg0) {
											object.pinInBackground(mGroupId);
											Utility.showToastMessage(CommentActivity.this, getResources().getString(R.string.tag_post_success));
											progressBar.setVisibility(View.GONE);
										}
									});
								}
								else
									progressBar.setVisibility(View.GONE);
							}
							else
								progressBar.setVisibility(View.GONE);
					}});
				final List<String> newlySelectedTag=hashTagFeedAdapter.getSelectedList();
				List<String> unSelectedTag=new ArrayList<String>();
				for(int k=0;k<alreadySelectedTag.size();k++)
				{
					if(newlySelectedTag.contains(alreadySelectedTag.get(k)))
						newlySelectedTag.remove(alreadySelectedTag.get(k));
					else
						unSelectedTag.add(alreadySelectedTag.get(k));
				}
				
				for(int i=0;i<newlySelectedTag.size();i++)
				{
					ParseQuery<ParseObject> tagQuery = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
					tagQuery.whereEqualTo(Constants.OBJECT_ID, newlySelectedTag.get(i));
					tagQuery.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(final ParseObject object, ParseException e) {
								if (e == null) 
								{
									if(object!=null)
									{
										object.increment(Constants.TAG_POST_COUNT,1);
										object.saveInBackground();
									}
								}
						}});
					
					System.out.println("inside insert for loop");
					final int j=i;
					ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
					query1.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
					query1.whereEqualTo(Constants.TAG_ID_STRING, newlySelectedTag.get(j));
					query1.include(Constants.GROUP_ID);
					query1.include(Constants.TAG_ID);
					//query.fromPin(mGroupId);
					query1.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(final ParseObject object, ParseException e) {
								if (e == null) 
								{
									if(object!=null)
									{
										List<String> feedIdList=object.getList(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
										feedIdList.add(feedId);
										object.put(Constants.TAGGED_GROUP_FEED_ID_ARRAY, feedIdList);
										object.saveInBackground();
									}
									else
									{
										ArrayList<String> feedIdList=new ArrayList<String>();
										feedIdList.add(feedId);
										final ParseObject tagObject = new ParseObject(Constants.TAGGED_GROUP_FEED_TABLE);
										tagObject.put(Constants.TAG_GROUP_ID, mGroupId);
										tagObject.put(Constants.TAG_ID_STRING, newlySelectedTag.get(j));
										tagObject.put(Constants.TAGGED_GROUP_FEED_ID_ARRAY, feedIdList);
										tagObject.put(Constants.GROUP_ID, ParseObject.createWithoutData(Constants.GROUP_TABLE, mGroupId));
										tagObject.put(Constants.TAG_ID, ParseObject.createWithoutData(Constants.HASH_TAG_TABLE, newlySelectedTag.get(j)));
										tagObject.saveInBackground();
									}
								}
								else
								{
									ArrayList<String> feedIdList=new ArrayList<String>();
									feedIdList.add(feedId);
									final ParseObject tagObject = new ParseObject(Constants.TAGGED_GROUP_FEED_TABLE);
									tagObject.put(Constants.TAG_GROUP_ID, mGroupId);
									tagObject.put(Constants.TAG_ID_STRING, newlySelectedTag.get(j));
									tagObject.put(Constants.TAGGED_GROUP_FEED_ID_ARRAY, feedIdList);
									tagObject.put(Constants.GROUP_ID, ParseObject.createWithoutData(Constants.GROUP_TABLE, mGroupId));
									tagObject.put(Constants.TAG_ID, ParseObject.createWithoutData(Constants.HASH_TAG_TABLE, newlySelectedTag.get(j)));
									tagObject.saveInBackground();
								}
						}});
				}
				
				
				for(int j=0;j<unSelectedTag.size();j++)
				{
					ParseQuery<ParseObject> tagQuery = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
					tagQuery.whereEqualTo(Constants.OBJECT_ID, unSelectedTag.get(j));
					tagQuery.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(final ParseObject object, ParseException e) {
								if (e == null) 
								{
									if(object!=null)
									{
										object.increment(Constants.TAG_POST_COUNT,-1);
										object.saveInBackground();
									}
								}
						}});
					
					System.out.println("inside remove for loop");
					ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
					query1.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
					query1.whereEqualTo(Constants.TAG_ID_STRING, unSelectedTag.get(j));
					query1.include(Constants.GROUP_ID);
					query1.include(Constants.TAG_ID);
					//query.fromPin(mGroupId);
					query1.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(final ParseObject object, ParseException e) {
								if (e == null) 
								{
									if(object!=null)
									{
										List<String> feedIdList=object.getList(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
										feedIdList.remove(feedId);
										object.put(Constants.TAGGED_GROUP_FEED_ID_ARRAY, feedIdList);
										object.saveInBackground();
									}
								}
								else
								{
									System.out.println("exception in remove e ::: "+e);
								}
						}});
				}
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		mDialog.show();
    }
	

	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2,final String url) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
	    ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
            	startActivity(new Intent(CommentActivity.this,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", url));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
            }
        }, idx1, idx2, 0);
  	    return ssb;
	}
	

	private void updateLike(final boolean isLike,final int position,final TextView points)
	{
		GroupPostListActivity.flag=true;
		//progressBar.setVisibility(View.VISIBLE);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject feedObj, ParseException e) {
					if (e == null) 
					{
						if(feedObj!=null)
						{
							List<String> likeArray=feedObj.getList(Constants.LIKE_ARRAY);
							if(isLike)
							{
								likeArray.add(PreferenceSettings.getMobileNo());
								feedObj.increment(Constants.POST_POINT, 50);
								feedObj.put(Constants.LIKE_ARRAY, likeArray);
							}
							else
							{
								likeArray.remove(PreferenceSettings.getMobileNo());
								feedObj.increment(Constants.POST_POINT, -50);
								feedObj.put(Constants.LIKE_ARRAY, likeArray);
							}
							feedObj.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
							feedObj.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
							                 if (e == null) {
							                	 points.setText(feedObj.getInt(Constants.POST_POINT)+"");
							                	// points.setText(feedObj.getInt(Constants.POST_POINT)+" Points");
							                	}	//}
  							          }});
							
							}
						}
				}
			});
		
		
		final ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
		query1.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
		query1.whereEqualTo(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID));
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list2, ParseException e) {
					if (e == null) 
					{
						if(list2.size() > 0)
						{
							list2.get(0).put(Constants.UP_VOTE, isLike);
							list2.get(0).put(Constants.ACTIVITY_LOCATION, point);
							list2.get(0).saveInBackground();
						}
						else
						{
							ParseObject userObject = new ParseObject(Constants.ACTIVITY_TABLE);
							userObject.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
							userObject.put(Constants.FEED_ID, list.get(position).getObjectId());
							userObject.put(Constants.FEED_TYPE, "Like");
							userObject.put(Constants.COMMENT_TEXT, "");
							userObject.put(Constants.USER_NAME, PreferenceSettings.getUserName());
							userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
							userObject.put(Constants.UP_VOTE, isLike);
							userObject.put(Constants.DOWN_VOTE, false);
							userObject.put(Constants.FLAG_VALUE, "");
							userObject.put(Constants.ACTIVITY_LOCATION, point);
							userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
							userObject.saveInBackground();
						}
						
						
						/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject feedObj, ParseException e) {
									if (e == null) 
									{
										if(feedObj!=null)
										{
											List<String> likeArray=feedObj.getList(Constants.LIKE_ARRAY);
											if(isLike)
											{
												likeArray.add(PreferenceSettings.getMobileNo());
												feedObj.increment(Constants.POST_POINT, 50);
												feedObj.put(Constants.LIKE_ARRAY, likeArray);
											}
											else
											{
												likeArray.remove(PreferenceSettings.getMobileNo());
												feedObj.increment(Constants.POST_POINT, -50);
												feedObj.put(Constants.LIKE_ARRAY, likeArray);
											}
											feedObj.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
											feedObj.saveInBackground(new SaveCallback() {
		      							          public void done(ParseException e) {
			 							                 if (e == null) {*/
			 							                	progressBar.setVisibility(View.GONE);
			 							                	//list.set(position,feedObj);
			 							                	//updateValue();
			 							                		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
			 							                		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			 							                		query.getFirstInBackground(new GetCallback<ParseObject>() {
			 							       					public void done(ParseObject userObj, ParseException e) {
			 							       							if (e == null) 
			 							       							{
			 							       								if(userObj!=null)
			 							       								{
			 							       									if(isLike)
			 							       									userObj.increment(Constants.BADGE_POINT, 50);
			 							                							else
			 							                								userObj.increment(Constants.BADGE_POINT, -50);
			 							                							
			 							       								userObj.saveInBackground(new SaveCallback() {
			 									      							          public void done(ParseException e) {
			 									 							                 if (e == null) {
						 							                							//setAdapter();
						 							                							//mListView.getChildAt(position).getTop();
			 									 							                 }
			 									      							          }});
			 							                						}
			 							                					}
			 							                				}
			 							                			});
			 				/*			                	}	//}
		      							          }});
										
										}
									}
							}
						});*/
					}
				}
		
			});
		
	}
	
	private void updateDislike(final boolean isDisLike,final int position,final TextView points)
	{
		GroupPostListActivity.flag=true;
		//progressBar.setVisibility(View.VISIBLE);
		
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject feedObj, ParseException e) {
					if (e == null) 
					{
						if(feedObj!=null)
						{
							List<String> likeArray=feedObj.getList(Constants.DIS_LIKE_ARRAY);
							if(isDisLike)
							{
								likeArray.add(PreferenceSettings.getMobileNo());
								feedObj.increment(Constants.POST_POINT, -50);
								feedObj.put(Constants.DIS_LIKE_ARRAY, likeArray);
							}
							else
							{
								likeArray.remove(PreferenceSettings.getMobileNo());
								feedObj.increment(Constants.POST_POINT, 50);
								feedObj.put(Constants.DIS_LIKE_ARRAY, likeArray);
							}
							feedObj.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
							feedObj.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
							                 if (e == null) {
							                	 points.setText(feedObj.getInt(Constants.POST_POINT)+"");
							                	 //points.setText(feedObj.getInt(Constants.POST_POINT)+" Points");
							             	}	//}
  							          }});
							
							}
						}
				}
			});
		
		final ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
		query1.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
		query1.whereEqualTo(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID));
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list2, ParseException e) {
					if (e == null) 
					{
						if(list2.size() > 0)
						{
							list2.get(0).put(Constants.DOWN_VOTE, isDisLike);
							list2.get(0).put(Constants.ACTIVITY_LOCATION, point);
							list2.get(0).saveInBackground();
						}
						else
						{
							ParseObject userObject = new ParseObject(Constants.ACTIVITY_TABLE);
							userObject.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
							userObject.put(Constants.FEED_ID, list.get(position).getObjectId());
							userObject.put(Constants.FEED_TYPE, "Like");
							userObject.put(Constants.COMMENT_TEXT, "");
							userObject.put(Constants.USER_NAME, PreferenceSettings.getUserName());
							userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
							userObject.put(Constants.DOWN_VOTE, isDisLike);
							userObject.put(Constants.UP_VOTE, false);
							userObject.put(Constants.FLAG_VALUE, "");
							userObject.put(Constants.ACTIVITY_LOCATION, point);
							userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
							userObject.saveInBackground();
						}
						
						
					/*	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject feedObj, ParseException e) {
									if (e == null) 
									{
										if(feedObj!=null)
										{
											List<String> likeArray=feedObj.getList(Constants.DIS_LIKE_ARRAY);
											if(isDisLike)
											{
												likeArray.add(PreferenceSettings.getMobileNo());
												feedObj.increment(Constants.POST_POINT, -50);
												feedObj.put(Constants.DIS_LIKE_ARRAY, likeArray);
											}
											else
											{
												likeArray.remove(PreferenceSettings.getMobileNo());
												feedObj.increment(Constants.POST_POINT, 50);
												feedObj.put(Constants.DIS_LIKE_ARRAY, likeArray);
											}
											feedObj.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
											feedObj.saveInBackground(new SaveCallback() {
		      							          public void done(ParseException e) {
			 							                 if (e == null) {*/
			 							                	progressBar.setVisibility(View.GONE);
			 							                	//list.set(position,feedObj);
			 							                	//updateValue();
			 							                		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
			 							                		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			 							                		query.getFirstInBackground(new GetCallback<ParseObject>() {
			 														public void done(ParseObject userObj, ParseException e) {
			 																if (e == null) 
			 																{
			 																	if(userObj!=null)
			 																	{
			 																		if(isDisLike)
			 																			userObj.increment(Constants.BADGE_POINT, -50);
			 							                							else
			 							                								userObj.increment(Constants.BADGE_POINT, 50);
			 							                							
			 																		userObj.saveInBackground(new SaveCallback() {
			 									      							          public void done(ParseException e) {
			 									 							                 if (e == null) {
						 							                							//setAdapter();
			 									 							                 }
			 									      							          }});
			 							                						}
			 							                					}
			 							                				}
			 							                			});
			 							        /*        	}	//}
		      							          }});
										
										}
									}
							}
						});*/
					}
				}
		
			});
		
	}
	
	
	private void updateFlagInTable(final int position)
	{
		
		progressBar.setVisibility(View.VISIBLE);
		final ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		ParseObject userObject = new ParseObject(Constants.ACTIVITY_TABLE);
		userObject.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
		userObject.put(Constants.FEED_ID, list.get(position).getObjectId());
		userObject.put(Constants.FEED_TYPE, "Flag");
		userObject.put(Constants.COMMENT_TEXT, "");
		userObject.put(Constants.USER_NAME, PreferenceSettings.getUserName());
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.UP_VOTE, false);
		userObject.put(Constants.DOWN_VOTE, false);
		userObject.put(Constants.FLAG_VALUE, flagValue);
		userObject.put(Constants.ACTIVITY_LOCATION, point);
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject feedObj, ParseException e) {
					if (e == null) 
					{
						if(feedObj!=null)
						{
								List<String> flagArrayList=feedObj.getList(Constants.FLAG_ARRAY);
								flagArrayList.add(PreferenceSettings.getMobileNo());
								feedObj.put(Constants.FLAG_ARRAY, flagArrayList);
								final int flagCount=feedObj.getInt(Constants.FLAG_COUNT);
								//feedObj.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
								if(flagCount == 2)
								{
									feedObj.put(Constants.POST_STATUS, "InActive");
								}
								feedObj.increment(Constants.FLAG_COUNT, 1);
								
								feedObj.increment(Constants.POST_POINT, -200);
								feedObj.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
							                 if (e == null) {
							                	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
												query.whereEqualTo(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString());
												query.getFirstInBackground(new GetCallback<ParseObject>() {
													public void done(ParseObject userObj, ParseException e) {
															if (e == null) 
															{
																if(userObj!=null)
																{
																	userObj.increment(Constants.BADGE_POINT, -200);
																	if(flagCount==2)
																	{
																		int count=userObj.getInt(Constants.POST_FLAG_COUNT);
																		
																		if(count==2)
																		{
																			userObj.put(Constants.SUSPENDED, true);
																			userObj.put(Constants.USER_STATE, "Suspended");
																		}
																	}
																	userObj.increment(Constants.POST_FLAG_COUNT,1);
																	userObj.saveInBackground(new SaveCallback() {
																          public void done(ParseException e) {
																                 if (e == null) {
																					 setAdapter();
																					 progressBar.setVisibility(View.GONE);
																                 }
																          }});
																	
																}
															}
													}
												});
							                 }
							          }});
						}
					}
			}
		});							
	}
	
	private void setAdapter()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list1, ParseException e) {
				System.out.println("feed size :: "+list.size());
					if (e == null) 
					{
						if(list1.size() > 0)
						{
							list=list1;
							position=0;
							updateValue();
							//points.setText(list1.get(0).getInt(Constants.POST_POINT)+" POINTS");
							progressBar.setVisibility(View.GONE);
						}
						else
							progressBar.setVisibility(View.GONE);
					}
					else
						progressBar.setVisibility(View.GONE);
			}
		});
	}
	
	private void setCommentAdapter()
	{
		System.out.println("inside setCommentAdapter");
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
		query.whereEqualTo(Constants.FEED_TYPE, "Comment");
		query.include(Constants.USER_ID);
		query.orderByAscending("createdAt");
		query.fromPin(list.get(position).getObjectId());
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list2, ParseException e) {
					if (e == null) 
					{
						if(list2.size() > 0)
						{
							adapter=new CommentAdapter(CommentActivity.this, list2,groupObject.get(Constants.GROUP_TYPE).toString());
							mListView.setAdapter(adapter);
							mCommentEditTxt.setText("");
							focusOnView();
							progressBar.setVisibility(View.GONE);	
							if(scrollView.isRefreshing())
								scrollView.onRefreshComplete();
							

							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
							query.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
							query.whereEqualTo(Constants.FEED_TYPE, "Comment");
							query.include(Constants.USER_ID);
							query.orderByAscending("createdAt");
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> commentList, ParseException e) {
										if (e == null) 
										{
											if(commentList.size() > list2.size())
											{
											/*ParseObject.pinAllInBackground(list.get(position).getObjectId(),list2,new SaveCallback() {
												@Override
												public void done(ParseException arg0) {
													adapter.setCommentList(list2);
													adapter.notifyDataSetChanged();
												}
											});*/
												
												ParseObject.unpinAllInBackground(list.get(position).getObjectId(),new DeleteCallback() {
													@Override
													public void done(ParseException arg0) {
														ParseObject.pinAllInBackground(list.get(position).getObjectId(),commentList,
																new SaveCallback() {
															@Override
															public void done(ParseException arg0) {
																adapter.setCommentList(commentList);
																adapter.notifyDataSetChanged();
																focusOnView();
															}
															});
													}
												});
											}
											
										}
								}
							});
							
						}
						else
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
							query.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
							query.whereEqualTo(Constants.FEED_TYPE, "Comment");
							query.include(Constants.USER_ID);
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list2, ParseException e) {
										if (e == null) 
										{
											if(list2.size() > 0)
											{
											ParseObject.pinAllInBackground(list.get(position).getObjectId(),list2,new SaveCallback() {
												@Override
												public void done(ParseException arg0) {
													progressBar.setVisibility(View.GONE);
													setCommentAdapter();
												}
											});
											}
											
										}
										else
										{
											progressBar.setVisibility(View.GONE);
											if(scrollView.isRefreshing())
												scrollView.onRefreshComplete();
										}
								}
							});
							
							
						}
					}
					else
					{
						if(scrollView.isRefreshing())
							scrollView.onRefreshComplete();
						progressBar.setVisibility(View.GONE);
					}
			}
		});
	}
	
	private final void focusOnView(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
            	ScrollView myScrollView =  scrollView.getRefreshableView();
            	myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
               }
        });
    }
	@Override
	protected void onResume() {
		super.onResume();
		postImage.setEnabled(true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
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
	
	public class VideoDownloadTask extends AsyncTask<Void, Integer, Boolean>
	{
		int position=0;
		String filePath;
		ProgressBar sVideoProgress;
		ImageView sVideoBg,sVideoPlayImage;
		VideoView videoView;
		public VideoDownloadTask(int position,ProgressBar progressBar,VideoView videoView,ImageView sVideoBg,ImageView playImage)
		{
			this.sVideoProgress=progressBar;
			this.position=position;
			this.videoView=videoView;
			this.sVideoBg=sVideoBg;
			this.sVideoPlayImage=playImage;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			isFileDownloading=true;
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/Videos");
		    if (!file.exists()) {
		        file.mkdirs();
		    }
		    File file1 = new File(file.getAbsolutePath() + "/" + "SVideo" + ".mp4");
	    	
		    if(file1.exists()){
				file1.delete();
			}
		    
		    String uriSting = (file.getAbsolutePath() + "/" + "SVideo" + ".mp4");
		    filePath=uriSting;
		    try
			{
				byte[] videoByte= list.get(position).getParseFile(Constants.POST_VIDEO).getData();
				long total = 0;

				InputStream input =new ByteArrayInputStream(videoByte);
				OutputStream output = new FileOutputStream(uriSting);
				byte data[] = new byte[videoByte.length];
				int count;
				while ((count = input.read(data)) != -1) {
					 total += count;
					//publishProgress((int)((total*100)/videoByte.length));
					 //viewHolder.videoProgress.setProgress((int)((total*100)/videoByte.length));
				    output.write(data, 0, count);
				}
			}
			catch(Exception e){
				//System.out.println("inside video write catch "+e);
				return false;
			}
			

			return true;
		}
		
		/*@Override
		protected void onProgressUpdate(Integer... progress) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(progress[0]);
			progressBar.setMax(100);
			progressBar.setProgress(progress[0]);
		}*/
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			System.out.println("inside on post excute");
			
			//sVideoPlayImage.setImageResource(R.drawable.pause);
			sVideoPlayImage.setVisibility(View.GONE);
			videoView.setVisibility(View.VISIBLE);
	    	videoView.setVideoPath(filePath);
		    videoView.requestFocus();
		   System.out.println("before start play video");
		    videoView.start();
		    
		    
		    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
		    {
		        @Override
		        public void onCompletion(MediaPlayer mp) 
		        {
		        	isFileDownloading=false;
		        	sVideoPlayImage.setImageResource(R.drawable.play_image);
		        	videoView.setVisibility(View.GONE);
		        	sVideoBg.setVisibility(View.VISIBLE);
		        	sVideoPlayImage.setVisibility(View.VISIBLE);
			    	//videoView.setVisibility(View.GONE);
			    	//viewHolder.postImage.setVisibility(View.VISIBLE);
		        }
		        
		    });
		    videoView.setOnPreparedListener(new OnPreparedListener() {
	            public void onPrepared(MediaPlayer mp) {
	                sVideoBg.setVisibility(View.GONE);
	                System.out.println("inside setOnPreparedListener");
	                mp.start();
	                mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
	                    @Override
	                    public void onVideoSizeChanged(MediaPlayer mp, int arg1,
	                            int arg2) {
	                    	System.out.println("inside setOnVideoSizeChangedListener");
	                        // TODO Auto-generated method stub
	                        sVideoProgress.setVisibility(View.GONE);
	                        sVideoPlayImage.setVisibility(View.GONE);
	                        mp.start();
	                    }
	                });
	                
	            }
	        });
		    
		}
		
	}
	
}
