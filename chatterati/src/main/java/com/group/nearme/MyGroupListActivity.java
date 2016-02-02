package com.group.nearme;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.GroupItemAdapter;
import com.group.nearme.adapter.PublicPostAdapter;
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
import com.parse.ParseQuery.CachePolicy;
import com.parse.SaveCallback;

public class MyGroupListActivity extends Activity {
	private PullToRefreshListView mListView;
	private GroupItemAdapter mAdapter;
	ProgressBar progressBar;
	List<ParseObject> groupList;
	List<ParseObject> memberList=new ArrayList<ParseObject>();
	HashMap<String,Date> dateList;
	boolean flag;
	public static boolean flag1;
	AlertDialog mAlertDialog;
	Dialog mDialog;
	//ImageView refreshImg;
	TextView spannableText;
	RelativeLayout createLayout;
	ButtonFloat floatButton;
	int mLastFirstVisibleItem;
	boolean mIsScrollingUp;
	ArrayList<String> pendingInvitesIDList=new ArrayList<>();
	public GPSTracker gpsTracker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_item_list_view);
		Utility.getTracker(this, "MY GROUPS SCREEN");
		initViews();
		/*if (Build.VERSION.SDK_INT >= 21) {
		    Window window = getWindow();
		    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		    window.setStatusBarColor(Color.BLUE);
		}*/
		
		//mListView.setShowViewWhileRefreshing(false);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) 
			{
				if(pendingInvitesIDList.contains(groupList.get(position-1).getObjectId()))
				{
					Utility.setGroupObject(groupList.get(position-1));
					startActivity(new Intent(MyGroupListActivity.this,GroupProfileActivity.class).putExtra("flag", true).putExtra("fromPendingInvitation", true));
					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else {
					view.findViewById(R.id.unread_count).setVisibility(View.GONE);

					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
					query.whereEqualTo(Constants.GROUP_ID, groupList.get(position - 1).getObjectId());
					query.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
					query.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(ParseObject object, ParseException e) {
							if (e == null) {
								if (object != null) {
									object.put(Constants.UNREAD_MESSAGES, 0);
									object.saveInBackground();
								}
							}
						}
					});
					Utility.setGroupObject(groupList.get(position - 1));

					try {
						HashMap<String, Object> groupAttribute = (HashMap<String, Object>) groupList.get(position - 1).get(Constants.GROUP_ATTRIBUTE);
						int index = (Integer) groupAttribute.get(Constants.INSIDE_GROUP_DEFAULT_TAB);

						if (index == 4) {
							startActivity(new Intent(MyGroupListActivity.this, TopicListActivity.class));
							overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
						} else {
							startActivity(new Intent(MyGroupListActivity.this, TopicListActivity.class));
							overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
						}
					} catch (Exception e) {
						startActivity(new Intent(MyGroupListActivity.this, TopicListActivity.class));
						overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
					}
				}
				//startActivity(new Intent(MyGroupListActivity.this,TabGroupPostActivity.class));
				//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(MyGroupListActivity.this)) {

					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
					query.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());
					query.whereEqualTo(Constants.INVITATION_STATUS, "Active");
					query.findInBackground(new FindCallback<ParseObject>() {
						public void done(List<ParseObject> list, ParseException e) {
							pendingInvitesIDList=new ArrayList<String>();
							if (e == null) {
								if (list.size() > 0) {

									for (int i = 0; i < list.size(); i++) {
										pendingInvitesIDList.add(list.get(i).get(Constants.GROUP_ID).toString());
									}
									getDatafromParse();
								}
								else {
									getDatafromParse();
								}
							}
							else {
								getDatafromParse();
							}
						}});
					}
				else
				{
					progressBar.setVisibility(View.GONE);
					if(mListView.isRefreshing())
						mListView.onRefreshComplete();
					Utility.showToastMessage(MyGroupListActivity.this, getResources().getString(R.string.no_network));
				}

			}
			});
		
		TabGroupActivity.searchEditTxt.addTextChangedListener(new TextWatcher() {
		     
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		        // When user changed the Text
		    	if(flag)
		    		 mAdapter.filter(String.valueOf(cs));
		    }
		     
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		    }
		     
		    @Override
		    public void afterTextChanged(Editable arg0) {
		    }
		});
		if(Utility.checkInternetConnectivity(this))
		{
			getDatafromParse();
		}
		else
		{
			getDatafromParse();
			Utility.showOkDilaog(this, "Please check your network connection");
		}
		
		
		/*mListView.setOnScrollListener(new ListView.OnScrollListener(){
		    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		      // TODO Auto-generated method stub
		    }
		    public void onScrollStateChanged(AbsListView view, int scrollState) {
		      // TODO Auto-generated method stub
		      final ListView lw = mListView.getRefreshableView();

		       if(scrollState == 0) 
		      Log.i("a", "scrolling stopped...");


		        if (view.getId() == lw.getId()) {
		        final int currentFirstVisibleItem = lw.getFirstVisiblePosition();
		         if (currentFirstVisibleItem > mLastFirstVisibleItem) {
		            mIsScrollingUp = false;
		            Log.i("a", "scrolling down...");
		        } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
		            mIsScrollingUp = true;
		            Log.i("a", "scrolling up...");
		        }

		        mLastFirstVisibleItem = currentFirstVisibleItem;
		    } 
		    }
		  });
*/		
	/*	mListView.setOnTouchListener(new View.OnTouchListener() {
		    float initialY, finalY;
		    boolean isScrollingUp;

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        int action = MotionEventCompat.getActionMasked(event);

		        switch(action) {
		            case (MotionEvent.ACTION_DOWN):
		                initialY = event.getY();
		            case (MotionEvent.ACTION_UP):
		                finalY = event.getY();

		                if (initialY < finalY) {
		                    Log.d("", "Scrolling up");
		                    isScrollingUp = true;
		                } else if (initialY > finalY) {
		                    Log.d("", "Scrolling down");
		                    isScrollingUp = false;
		                }
		            default:
		        }

		        if (isScrollingUp) {
		            // do animation for scrolling up
		        	System.out.println("inside scroll up");
		        } else {
		            // do animation for scrolling down
		        	System.out.println("inside scroll down");
		        }

		        return false; // has to be false, or it will freeze the listView
		    }
		});
*/


	}

	private void initViews() {
		mListView=(PullToRefreshListView) findViewById(R.id.mygroups_listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		dateList=new HashMap<String,Date>();
		spannableText=(TextView) findViewById(R.id.spannable_text);
		createLayout=(RelativeLayout) findViewById(R.id.create_layout);
		floatButton=(ButtonFloat) findViewById(R.id.buttonFloat);

		gpsTracker=new GPSTracker(this);
		
		pendingInvitesIDList=getIntent().getStringArrayListExtra("pending_invites_id");

		floatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MyGroupListActivity.this, ChooseGroupPurposeActivityNew.class));
				overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
			}
		});
	}
	
	private void getDatafromParse()
	{
		createLayout.setVisibility(View.GONE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.fromPin(Constants.USER_LOCAL_DATA_STORE);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject userObject, ParseException e) {
				if (userObject != null) {
					//Utility.setUserImageFile(userObject.getParseFile(Constants.THUMBNAIL_PICTURE));
					if (userObject.get(Constants.USER_STATE).toString().equals(Constants.BANNED)) {
						mAlertDialog = new AlertDialog.Builder(MyGroupListActivity.this)
								.setMessage(getResources().getString(R.string.user_blocked))
								.setPositiveButton("Ok", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
										Intent intent = new Intent(Intent.ACTION_MAIN);
										intent.addCategory(Intent.CATEGORY_HOME);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									}
								}).show();
					} else if (userObject.get(Constants.USER_STATE).toString().equals(Constants.SUSPENDED)) {
						mAlertDialog = new AlertDialog.Builder(MyGroupListActivity.this)
								.setMessage(getResources().getString(R.string.user_suspended))
								.setPositiveButton("Ok", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
										Intent intent = new Intent(Intent.ACTION_MAIN);
										intent.addCategory(Intent.CATEGORY_HOME);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									}
								}).show();
					} else {

						List<String> myGroupIdList = userObject.getList(Constants.MY_GROUP_ARRAY);
						try {
							if (pendingInvitesIDList.size() > 0) {
								myGroupIdList.addAll(pendingInvitesIDList);
							}
						}
						catch (Exception exce){
							pendingInvitesIDList=new ArrayList<String>();
						}
						getDataFromLocalStore(myGroupIdList);
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
						query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject userObject, ParseException e) {
								if (userObject != null) {
									ParseObject.unpinAllInBackground(Constants.USER_LOCAL_DATA_STORE,
											new DeleteCallback() {
												@Override
												public void done(ParseException e) {
													if (e == null) {
														// loadAllGroupFeedInBackground(list);
														userObject.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
													}
												}
											});

								}
							}
						});


					}
				} else {
					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
					//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
					query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
					//query.fromLocalDatastore();
					query.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(ParseObject userObject, ParseException e) {
							if (userObject != null) {
								userObject.pinInBackground(Constants.USER_LOCAL_DATA_STORE, new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											getDatafromParse();
										}
									}
								});

							}
						}
					});
				}
			}
		});
	}
	
	private void getDataFromLocalStore(final List<String> myGroupIdList)
	{
		//final List<String> myGroupIdList=userObject.getList(Constants.MY_GROUP_ARRAY);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereContainedIn(Constants.OBJECT_ID, myGroupIdList);
		query.whereEqualTo(Constants.GROUP_STATUS, "Active");
		query.orderByDescending("updatedAt");
		query.fromPin(Constants.MY_GROUP_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
				if (e == null) {
					groupList = list;
					if (groupList.size() > 0) {

						setAdapter();
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
						//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
						query.whereContainedIn(Constants.GROUP_ID, myGroupIdList);
						query.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
						query.whereEqualTo(Constants.MEMBER_STATUS, "Active");
						query.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> list, ParseException e) {
								if (e == null) {
									memberList = list;
									if (memberList.size() > 0) {
										//setAdapter();
										mAdapter.setMemberList(memberList);
										mAdapter.notifyDataSetChanged();
									}
								}
							}
						});
						ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_TABLE);
						//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
						query1.whereContainedIn(Constants.OBJECT_ID, myGroupIdList);
						query1.whereEqualTo(Constants.GROUP_STATUS, "Active");
						query1.orderByDescending("updatedAt");
						//query.fromLocalDatastore();
						query1.findInBackground(new FindCallback<ParseObject>() {
							public void done(final List<ParseObject> list, ParseException e) {
								if (e == null) {
									ParseObject.unpinAllInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE,
											new DeleteCallback() {
												@Override
												public void done(ParseException e) {
													if (e == null) {
														// loadAllGroupFeedInBackground(list);
														ParseObject.pinAllInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE, list);
													}
												}
											});

								}
							}
						});


					} else {
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
						//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
						query.whereContainedIn(Constants.OBJECT_ID, myGroupIdList);
						query.whereEqualTo(Constants.GROUP_STATUS, "Active");
						query.orderByDescending("updatedAt");
						//query.fromLocalDatastore();
						query.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> list, ParseException e) {
								if (e == null) {
									groupList = list;
									if (groupList.size() > 0) {
										ParseObject.pinAllInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE, list, new SaveCallback() {
											@Override
											public void done(ParseException e) {
												if (e == null) {
													getDataFromLocalStore(myGroupIdList);
												}
											}
										});

									} else {
										if (mListView.isRefreshing())
											mListView.onRefreshComplete();
										createLayout.setVisibility(View.VISIBLE);
										progressBar.setVisibility(View.GONE);
										spannableText.setMovementMethod(LinkMovementMethod.getInstance());
										spannableText.setText(addClickablePart(getResources().getString(R.string.my_group_system_post), 97, 110), BufferType.SPANNABLE);
									}
								}
							}
						});


					}
				} else
					progressBar.setVisibility(View.GONE);
			}
		});
	}
	
	private void loadAllGroupFeedInBackground(final List<ParseObject> myGroupList)
	{
		if(myGroupList.size() > 0)
		{
			for(int i=0;i<myGroupList.size();i++)
			{
				final int j=i;
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
			//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
			query.whereEqualTo(Constants.GROUP_ID, myGroupList.get(i).getObjectId());
			if(myGroupList.get(i).get(Constants.GROUP_TYPE).toString().equals("Public"))
			{
				query.whereNotEqualTo(Constants.POST_TYPE, "Member");
			}
			query.whereEqualTo(Constants.POST_STATUS, "Active");
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(final List<ParseObject> feedList, ParseException e) {
						if (e == null) 
						{
							if(feedList.size() > 0)
							{
								ParseObject.unpinAllInBackground(myGroupList.get(j).getObjectId(),
						                  new DeleteCallback() {
						                       @Override
						                    public void done(ParseException e) {
						                    	   if(e == null) {
															
						                    	   ParseObject.pinAllInBackground(myGroupList.get(j).getObjectId(),feedList);
						                    	   }
						                       }
								});
							
						             
							}
						}
				}});
			}
		}
			
	}
	
	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2) {
		System.out.println("inside addClickablePart method");
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
	    ssb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				System.out.println("inside onlick spanable text");
				TabGroupActivity.tabHost.setCurrentTab(1);
				//startActivity(new Intent(MyGroupListActivity.this,CreateGroupActivity.class));
				//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		}, idx1, idx2, 0);
  	    return ssb;
	}
	

	
	private void setAdapter()
	{
		if(pendingInvitesIDList.size() > 0) {
			int index = 0;
			for (int j = 0; j < groupList.size(); j++) {
				if (pendingInvitesIDList.contains(groupList.get(j).getObjectId())) {

					ParseObject obj = groupList.get(j);
					groupList.remove(j);
					groupList.add(index, obj);
					index++;
				}
			}
		}
		mAdapter=new GroupItemAdapter(MyGroupListActivity.this,groupList,memberList,pendingInvitesIDList,progressBar);
		mListView.setAdapter(mAdapter);
		flag=true;
		//lastUpdate.setText("Last Updated "+Utility.date());
		progressBar.setVisibility(View.GONE);
		if(mListView.isRefreshing())
			mListView.onRefreshComplete();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(flag1) {
			flag1 = false;
			getDatafromParse();
		}
	}

	@Override
	public void onBackPressed() {
        	  showExitAlertDialog();
	}
	public  void showExitAlertDialog(){

		mDialog = new Dialog(MyGroupListActivity.this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.two_btn_dialog);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);

		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		Button yes=(Button) mDialog.findViewById(R.id.yes);
		Button no=(Button) mDialog.findViewById(R.id.no);
		TextView message=(TextView) mDialog.findViewById(R.id.msg);

		message.setText("Are you sure you want to exit?");

		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		mDialog.show();
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
