package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.HashTagAdapter;
import com.group.nearme.adapter.HashTagMemberAdapter;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class HashTagActivity extends Activity{
	HashTagAdapter adapter;
	PullToRefreshListView listView;
	private String mGroupId="",mGroupName="",mGroupImage;
	ProgressBar progressBar;
	List<ParseObject> hashTagList=new ArrayList<ParseObject>();
	TextView spannableText;
	RelativeLayout createLayout;
	Button mAddBtn,mEditBtn,mFindPostBtn;
	LinearLayout mAddEditLayout;
	ParseObject groupObject;
	Dialog mDialog;
	AlertDialog mAlertDialog;
	GPSTracker gpsTracker;
	Typeface tf;
	ArrayList<String> tagNameList=new ArrayList<String>();
	HashTagMemberAdapter memberAdapter;
	ArrayList<String> typeList=new ArrayList<String>();
	RelativeLayout topLayout;
	TextView title;
	boolean fromTopic;
	ImageView mBackImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hash_tag_listview);
		Utility.getTracker(this, "GROUP HASHTAG LISTING SCREEN");
		initViews();
		
		mAddBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddDialog();
			}
		});
		
		mEditBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(adapter.getSelectedList().size()==0)
				{
					Utility.showToastMessage(HashTagActivity.this, getResources().getString(R.string.tag_nothing_edit));
				}
				else if(adapter.getSelectedList().size() > 1)
				{
					Utility.showToastMessage(HashTagActivity.this, getResources().getString(R.string.tag_multi_edit));
				}
				else
				{
					showEditDialog(adapter.getSelectedList().get(0));
				}
				
			}
		});
		
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(Utility.checkInternetConnectivity(HashTagActivity.this))
					 refresh();
				else
				{
					progressBar.setVisibility(View.GONE);
					if(listView.isRefreshing())
						listView.onRefreshComplete();
					Utility.showToastMessage(HashTagActivity.this, getResources().getString(R.string.no_network));
				}
			}
			
			});
		
		mFindPostBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(adapter.getSelectedTagIDList().size()==0)
				{
					Utility.showToastMessage(HashTagActivity.this, "Please select any tag to find post");
				}
				else
				{
					callHashTagFeedActivity();
				}
			}
		});
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) 
			{
				if(!groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				{
					TextView text=(TextView) view.findViewById(R.id.tag);
					String str=text.getText().toString();
					System.out.println("selected tag :: "+str);
					if(str.equals("# My Posts"))
					{
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.GROUP_ID, mGroupId);
						query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
						query.whereEqualTo(Constants.POST_STATUS, "Active");
						query.whereNotContainedIn(Constants.POST_TYPE, typeList);
						query.include(Constants.USER_ID);
						query.fromPin(mGroupId);
						query.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> list, ParseException e) {
									if (e == null) 
									{
										if(list.size() > 0)
										{
											ArrayList<String> feedIdList=new ArrayList<String>();
											for(int i=0;i<list.size();i++)
											{
												feedIdList.add(list.get(i).getObjectId());
											}
											
											Intent i=new Intent(HashTagActivity.this,HashTagFeedActivity.class);
											i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY,feedIdList);
											i.putExtra("hash_tags", "My Posts");
											startActivity(i);
											overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
										}
										else
										{
											Utility.showToastMessage(HashTagActivity.this, "No post available for slected tag");
										}
									}
							}});
					}
					else if(str.equals("# Group Rules"))
					{
						callGroupRulesActivity();
					}
					else
					{
						//System.out.println("slected tag from hashlist:: "+hashTagList.get(position-2).getString(Constants.TAG_NAME));
						callHashTagFeedActivity1(hashTagList.get(position-3).getObjectId(),hashTagList.get(position-3).getString(Constants.TAG_NAME));
					}
				
				}
				else
				{
					TextView text=(TextView) view.findViewById(R.id.tag_name);
					String str=text.getText().toString();
					System.out.println("selected tag :: "+str);
					if(str.equals("# My Posts"))
					{
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.GROUP_ID, mGroupId);
						query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
						query.whereEqualTo(Constants.POST_STATUS, "Active");
						query.whereNotContainedIn(Constants.POST_TYPE, typeList);
						query.include(Constants.USER_ID);
						query.fromPin(mGroupId);
						query.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> list, ParseException e) {
									if (e == null) 
									{
										if(list.size() > 0)
										{
											ArrayList<String> feedIdList=new ArrayList<String>();
											for(int i=0;i<list.size();i++)
											{
												feedIdList.add(list.get(i).getObjectId());
											}
											
											Intent i=new Intent(HashTagActivity.this,HashTagFeedActivity.class);
											i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY,feedIdList);
											i.putExtra("hash_tags", "My Posts");
											startActivity(i);
											overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
										}
										else
										{
											Utility.showToastMessage(HashTagActivity.this, "No post available for slected tag");
										}
									}
							}});
					}
					else if(str.equals("# Pending Posts"))
					{
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.GROUP_ID, mGroupId);
						query.whereEqualTo(Constants.POST_STATUS, "Pending");
						//query.whereEqualTo(Constants.POST_TYPE, "Pending");
						query.include(Constants.USER_ID);
						query.fromPin(mGroupId);
						query.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> list, ParseException e) {
									if (e == null) 
									{
										if(list.size() > 0)
										{
											ArrayList<String> feedIdList=new ArrayList<String>();
											for(int i=0;i<list.size();i++)
											{
												feedIdList.add(list.get(i).getObjectId());
											}
											
											Intent i=new Intent(HashTagActivity.this,HashTagFeedActivity.class);
											i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY,feedIdList);
											i.putExtra("hash_tags", "Pending Posts");
											startActivity(i);
											overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
										}
										else
										{
											Utility.showToastMessage(HashTagActivity.this, "No Pending Posts");
										}
									}
							}});
					}
					else if(str.equals("# Group Rules"))
					{
						callGroupRulesActivity();
					}
					else
					{
						
					}
					
				}
			}
		});
		
		
	}
	
	private void callGroupRulesActivity()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		//query.whereEqualTo(Constants.POST_STATUS, "InActive");
		query.whereEqualTo(Constants.POST_TYPE, "Rule");
		//query.fromPin(mGroupId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (object != null) 
					{
						progressBar.setVisibility(View.GONE);
						String str=object.getString(Constants.POST_TEXT);
						Intent i=new Intent(HashTagActivity.this,GroupRulesActivity.class);
						i.putExtra("Rules", str);
						i.putExtra("isFirstTime", false);
						i.putExtra(Constants.OBJECT_ID, object.getObjectId());
						startActivity(i);
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);

					}
					else
					{
						progressBar.setVisibility(View.GONE);
						String str=getResources().getString(R.string.group_rules);
						Intent i=new Intent(HashTagActivity.this,GroupRulesActivity.class);
						i.putExtra("Rules", str);
						i.putExtra("isFirstTime", true);
						startActivity(i);
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);

					}
			}
		});

		
	}
	
	private void callHashTagFeedActivity1(final String tagId,final String tagname)
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.TAG_ID_STRING, tagId);
		query.fromPin(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
						if(object!=null)
						{
							progressBar.setVisibility(View.GONE);
							List<String> feedIdList=object.getList(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
							
							if(feedIdList.size() > 0)
							{
								Intent i=new Intent(HashTagActivity.this,HashTagFeedActivity.class);
								i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY, (ArrayList<String>) feedIdList);
								i.putExtra("hash_tags", tagname);
								startActivity(i);
								overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
							}
							else
							{
								Utility.showToastMessage(HashTagActivity.this, "No post available for slected tag");
							}
							
							
							ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
							query1.whereEqualTo(Constants.TAG_ID_STRING, tagId);
							query1.getFirstInBackground(new GetCallback<ParseObject>() {
								public void done(final ParseObject object, ParseException e) {
											if(object!=null)
											{
												object.unpinInBackground(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE,
										                  new DeleteCallback() {
										                       @Override
										                    public void done(ParseException e) {
										                    	   if(e == null) {
										                    		   object.pinInBackground(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE,new SaveCallback() {
										   								@Override
										   								public void done(ParseException arg0) {
										   								}
										   							});
										                    	   }
										                       }
												 });
												
											}
										
								}});
						}
						else
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
							query.whereEqualTo(Constants.TAG_ID_STRING, tagId);
							query.getFirstInBackground(new GetCallback<ParseObject>() {
								public void done(ParseObject object, ParseException e) {
											if(object!=null)
											{
												object.pinInBackground(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE,new SaveCallback() {
														@Override
														public void done(ParseException arg0) {
															callHashTagFeedActivity1(tagId,tagname);
														}
													});
											}
											else
											{
												progressBar.setVisibility(View.GONE);
												Utility.showToastMessage(HashTagActivity.this, "No post available for slected tag");
											}
										}
								});
							
						}
			}});
	}
	
	private void callHashTagFeedActivity()
	{
		mFindPostBtn.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
		query.whereContainedIn(Constants.TAG_ID_STRING, adapter.getSelectedTagIDList());
		query.fromPin(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						//mFindPostBtn.setEnabled(true);
						if(list.size() > 0)
						{
							progressBar.setVisibility(View.GONE);
							System.out.println("find post tag size :: "+list.size());
							ArrayList<String> feedIdList=new ArrayList<String>();
							String str="";
							for(int i=0;i<adapter.getSelectedList().size();i++)
							{
								if(str.isEmpty())
									str=adapter.getSelectedList().get(i);
								else
									str=str+","+adapter.getSelectedList().get(i);
							}
							
							for(int j=0;j<list.size();j++)
							{
								List<String> id=list.get(j).getList(Constants.TAGGED_GROUP_FEED_ID_ARRAY);
								feedIdList.addAll(id);
							}
							
							ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
							query1.whereContainedIn(Constants.TAG_ID_STRING, adapter.getSelectedTagIDList());
							//query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
							query1.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											if(list.size() > 0)
											{
												 ParseObject.unpinAllInBackground(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE,
										                  new DeleteCallback() {
										                       @Override
										                    public void done(ParseException e) {
										                    	   if(e == null) {
										                    		   ParseObject.pinAllInBackground(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE,list,new SaveCallback() {
										   								@Override
										   								public void done(ParseException arg0) {
										   								}
										   							});
										                    	   }
										                       }
												 });
												
											}
										}
								}});
							
							
							mFindPostBtn.setEnabled(true);
							
							if(feedIdList.size() > 0)
							{
								try
								{
								Intent i=new Intent(HashTagActivity.this,HashTagFeedActivity.class);
								i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY, feedIdList);
								i.putExtra("hash_tags", str);
								startActivity(i);
								overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
								}
								catch(Exception e1){}
							}
							else
							{
								if(adapter.getSelectedTagIDList().size() >1)
									Utility.showToastMessage(HashTagActivity.this, "No post available for slected tags");
								else
									Utility.showToastMessage(HashTagActivity.this, "No post available for slected tag");
							}
							
							
							
						}
						else
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.TAGGED_GROUP_FEED_TABLE);
							query.whereContainedIn(Constants.TAG_ID_STRING, adapter.getSelectedTagIDList());
							
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											//mFindPostBtn.setEnabled(true);
											if(list.size() > 0)
											{
												 ParseObject.pinAllInBackground(mGroupId+Constants.TAGGED_GROUP_FEED_TABLE,list,new SaveCallback() {
														@Override
														public void done(ParseException arg0) {
															callHashTagFeedActivity();
														}
													});
											}
											else
											{
												mFindPostBtn.setEnabled(true);
												progressBar.setVisibility(View.GONE);
												if(adapter.getSelectedTagIDList().size() >1)
													Utility.showToastMessage(HashTagActivity.this, "No post available for slected tags");
												else
													Utility.showToastMessage(HashTagActivity.this, "No post available for slected tag");
											}
										}
								}});
							
						}
					}
					else
					{
						progressBar.setVisibility(View.GONE);
					}
			}});
	}
	
	private void refresh()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.include(Constants.GROUP_ID);
		//query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						mFindPostBtn.setEnabled(true);
						if(list.size() > 0)
						{
							
							if(listView.isRefreshing())
								listView.onRefreshComplete();
							 ParseObject.unpinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,
					                  new DeleteCallback() {
					                       @Override
					                    public void done(ParseException e) {
					                    	   if(e == null) {
					                    		   ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,list,new SaveCallback() {
					   								@Override
					   								public void done(ParseException arg0) {
					   									setAdapter();
					   								}
					   							});
					                    	   }
					                       }
							 });
							
							
						}
						else
						{
							setAdapter();
							/*//mEditBtn.setEnabled(false);
							//progressBar.setVisibility(View.GONE);
							//mFindPostBtn.setVisibility(View.GONE);
							//createLayout.setVisibility(View.VISIBLE);
							
							//spannableText.setText(getResources().getString(R.string.no_tags));
							if(listView.isRefreshing())
								listView.onRefreshComplete();*/
						}
					}
			}});
		
		
		
	}

	private void initViews() {
		
		tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
	
		listView=(PullToRefreshListView) findViewById(R.id.hashtag_listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		spannableText=(TextView) findViewById(R.id.spannable_text);
		createLayout=(RelativeLayout) findViewById(R.id.create_layout);
		mAddBtn=(Button) findViewById(R.id.add_hashtag);
		mEditBtn=(Button) findViewById(R.id.edit_hashtag);
		mFindPostBtn=(Button) findViewById(R.id.find_post);
		mAddEditLayout=(LinearLayout) findViewById(R.id.add_edit_layout);
		
		topLayout=(RelativeLayout) findViewById(R.id.top);
		title=(TextView) findViewById(R.id.title);
		mBackImage=(ImageView) findViewById(R.id.back);
		groupObject=Utility.getGroupObject();
		
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		
		fromTopic=getIntent().getBooleanExtra("from", false);
		
		title.setText(mGroupName);
		
		if(fromTopic)
			topLayout.setVisibility(View.VISIBLE);
		else
			topLayout.setVisibility(View.GONE);
		
		mEditBtn.setTypeface(tf);
		mFindPostBtn.setTypeface(tf);
		mAddBtn.setTypeface(tf);
		
		
		if(!groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			mAddEditLayout.setVisibility(View.GONE);
			mFindPostBtn.setVisibility(View.GONE);
		}
		
		gpsTracker=new GPSTracker(HashTagActivity.this);
		
		if (!gpsTracker.canGetLocation())
        {
			 gpsTracker.showSettingsAlert(this);
        }
		mFindPostBtn.setEnabled(false);
		
		typeList.add("Member");
		typeList.add("Invitation");
		typeList.add("Pending");
		setAdapter();
		
		mBackImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void setAdapter() {
		tagNameList=new ArrayList<String>();
		progressBar.setVisibility(View.VISIBLE);
		createLayout.setVisibility(View.GONE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
		query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
		query.include(Constants.GROUP_ID);
		query.orderByDescending("updatedAt");
		query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						mFindPostBtn.setEnabled(true);
						
						if(list.size() > 0)
						{
							
							hashTagList=list;
							if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
							{
								mAddEditLayout.setVisibility(View.VISIBLE);
								mFindPostBtn.setVisibility(View.VISIBLE);
								mEditBtn.setEnabled(true);
								adapter=new HashTagAdapter(HashTagActivity.this, hashTagList);
								listView.setAdapter(adapter);
								for(int i=0;i<list.size();i++)
								{
									tagNameList.add(list.get(i).getString(Constants.TAG_NAME).toLowerCase());
								}
							}
							else
							{
								mAddEditLayout.setVisibility(View.GONE);
								mFindPostBtn.setVisibility(View.GONE);
								memberAdapter=new HashTagMemberAdapter(HashTagActivity.this, hashTagList);
								listView.setAdapter(memberAdapter);
							}
							
							//ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE, list);
							progressBar.setVisibility(View.GONE);
							if(listView.isRefreshing())
								listView.onRefreshComplete();
							
						}
						else
						{
							
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
							query.whereEqualTo(Constants.TAG_GROUP_ID, mGroupId);
							query.include(Constants.GROUP_ID);
							//query.fromPin(mGroupId+Constants.TAG_LOCAL_DATA_STORE);
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											mFindPostBtn.setEnabled(true);
											if(list.size() > 0)
											{
												 ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,list,new SaveCallback() {
													@Override
													public void done(ParseException arg0) {
														setAdapter();
													}
												});
											}
											else
											{
												
												if(groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
												{
													mAddEditLayout.setVisibility(View.VISIBLE);
													//mFindPostBtn.setVisibility(View.VISIBLE);
													mEditBtn.setEnabled(false);
													adapter=new HashTagAdapter(HashTagActivity.this, hashTagList);
													listView.setAdapter(adapter);
												}
												else
												{
													mAddEditLayout.setVisibility(View.GONE);
													mFindPostBtn.setVisibility(View.GONE);
													memberAdapter=new HashTagMemberAdapter(HashTagActivity.this, hashTagList);
													listView.setAdapter(memberAdapter);
												}
												
												//ParseObject.pinAllInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE, list);
												progressBar.setVisibility(View.GONE);
												if(listView.isRefreshing())
													listView.onRefreshComplete();
												/*mEditBtn.setEnabled(false);
												progressBar.setVisibility(View.GONE);
												mFindPostBtn.setVisibility(View.GONE);
												createLayout.setVisibility(View.VISIBLE);
												spannableText.setText(getResources().getString(R.string.no_tags));
												if(listView.isRefreshing())
													listView.onRefreshComplete();*/
											}
										}
								}});
							
						}
					}
					else
						progressBar.setVisibility(View.GONE);
			}});
		
	}
	
	public void showAddDialog()
    {
    	mDialog = new Dialog(this,R.style.customDialogStyle);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.add_hash_tag);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		
		Button save=(Button) mDialog.findViewById(R.id.save);
		Button cancel=(Button) mDialog.findViewById(R.id.cancel);
		final EditText tagName=(EditText) mDialog.findViewById(R.id.tag_name);
		
		save.setTypeface(tf);
		cancel.setTypeface(tf);
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IBinder token = tagName.getWindowToken();
				( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
				boolean flag=false;
				String str=tagName.getText().toString().trim().toLowerCase();
				String str1=tagName.getText().toString().trim();
				final String[] tagNameOriginal=str1.split(",");
				String duplicateName="";
				final String[] tagNameArray=str.split(",");
				
				for(int i=0;i<tagNameArray.length;i++)
				{
					String tagName=tagNameArray[i].trim();
					if(tagNameList.contains(tagName))
					{
						duplicateName=tagNameArray[i].trim();
						flag=true;
						break;
					}
				}
				if(flag)
				{
					Utility.showToastMessage(HashTagActivity.this, duplicateName+" "+getResources().getString(R.string.tag_name_exist));
				}
				else
				{
					mDialog.dismiss();
					for(int i=0;i<tagNameOriginal.length;i++)
					{
						final int j=i;
							progressBar.setVisibility(View.VISIBLE);
							ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
							final ParseObject tagObject = new ParseObject(Constants.HASH_TAG_TABLE);
							tagObject.put(Constants.TAG_GROUP_ID, mGroupId);
							tagObject.put(Constants.TAG_NAME, tagNameOriginal[i].trim());
							tagObject.put(Constants.SYSTEM_TAG, false);
							tagObject.put(Constants.TAG_CREATED_LOCATION, point);
							tagObject.put(Constants.TAGGED_GROUP_FEED_ID_ARRAY, new ArrayList<String>());
							tagObject.put(Constants.GROUP_ID, ParseObject.createWithoutData(Constants.GROUP_TABLE, mGroupId));
							tagObject.saveInBackground(new SaveCallback() {
								@Override
								public void done(ParseException arg0) {
									tagObject.pinInBackground(mGroupId+Constants.TAG_LOCAL_DATA_STORE,new SaveCallback() {
										@Override
										public void done(ParseException arg0) {
											if(j==tagNameOriginal.length-1)
											{
												refresh();
											}
										}
									});
								
								}
							});
					}
				}
				
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IBinder token = tagName.getWindowToken();
				( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
				mDialog.dismiss();
			}
		});
		mDialog.show();
    }
	
	public void showEditDialog(final String name)
    {
    	mDialog = new Dialog(this,R.style.customDialogStyle);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.edit_hash_tag);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		
		Button save=(Button) mDialog.findViewById(R.id.save);
		Button cancel=(Button) mDialog.findViewById(R.id.cancel);
		
		ImageView delete=(ImageView) mDialog.findViewById(R.id.delete);
		
		final EditText tagName=(EditText) mDialog.findViewById(R.id.tag_name);
		tagName.setText(name);
		tagName.setSelection(tagName.length());
		save.setTypeface(tf);
		cancel.setTypeface(tf);
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String str=tagName.getText().toString().trim().toLowerCase();
				String str1=name.toLowerCase();
				IBinder token = tagName.getWindowToken();
				( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
				
				 if(str.equals(str1))
				{
					Utility.showToastMessage(HashTagActivity.this, "Tag name changed successfully");
					mDialog.dismiss();
				}
				 else if(tagNameList.contains(str))
				{
					Utility.showToastMessage(HashTagActivity.this, str+" "+getResources().getString(R.string.tag_name_exist));
				}
				else
				{
					int index=tagNameList.indexOf(str1);
					String tagId=hashTagList.get(index).getObjectId();
					progressBar.setVisibility(View.VISIBLE);
					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
					query.whereEqualTo(Constants.OBJECT_ID, tagId);
					query.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(ParseObject object, ParseException e) {
								if (e == null) 
								{
									if(object!=null)
									{
										object.put(Constants.TAG_NAME, tagName.getText().toString());
										object.saveInBackground(new SaveCallback() {
											@Override
											public void done(ParseException arg0) {
												mDialog.dismiss();
												setAdapter();
											}
										});
									}
									else
									{
										progressBar.setVisibility(View.GONE);
										mDialog.dismiss();
									}
								}
								else
								{
									progressBar.setVisibility(View.GONE);
									mDialog.dismiss();
								}
						}});
				}
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IBinder token = tagName.getWindowToken();
				( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
				mDialog.dismiss();
			}
		});
		
		delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				mAlertDialog=new AlertDialog.Builder(HashTagActivity.this)
				.setMessage("Are you sure you want to delete this tag?")
				.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						IBinder token = tagName.getWindowToken();
						( ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
						mDialog.dismiss();
						mAlertDialog.dismiss();
				String str1=name.toLowerCase();
				int index=tagNameList.indexOf(str1);
				String tagId=hashTagList.get(index).getObjectId();
				progressBar.setVisibility(View.VISIBLE);
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.HASH_TAG_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, tagId);
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object, ParseException e) {
							if (e == null) 
							{
								if(object!=null)
								{
									
									object.deleteInBackground(new DeleteCallback() {
										@Override
										public void done(ParseException arg0) {
											setAdapter();
										}
									});
								}
								else
								{
									progressBar.setVisibility(View.GONE);
									//mAlertDialog.dismiss();
								}
							}
							else
							{
								progressBar.setVisibility(View.GONE);
								//mAlertDialog.dismiss();
							}
					}});
				
			}
					
			
			}).setNegativeButton("No", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mAlertDialog.dismiss();
				}
			}).show();
				
				
			}
		});
		
		mDialog.show();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		//setAdapter();
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
