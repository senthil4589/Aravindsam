package com.group.nearme;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.MemberListActivity.MemberAdapter;
import com.group.nearme.settings.DatabaseHelper;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.NotificationObject;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class NotificationListActivity extends Activity
{
	PullToRefreshListView listView;
	ProgressBar progressBar;
	ParseObject groupObject;
	ArrayList<NotificationObject> notificationList;
	LayoutInflater inflater;
	ImageView mBackImg;
	NotificationAdapter mAdapter;
	//public static boolean flag;
	ImageLoader imageLoader;
	TextView text;
	boolean flag;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notification_listview);
		Utility.getTracker(this,"NOTIFICATION LIST SCREEN");
		initViews();
		listView.setPullToRefreshEnabled(false);
		
		/*listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				setAdapter();
			}
			});*/
		
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) 
			{
				System.out.println("type :: "+notificationList.get(position-1).getType());
				position=position-1;
				try
				{
				if(notificationList.get(position).getType().equals("Post") || notificationList.get(position).getType().equals("Comment") || notificationList.get(position).getType().equals("Flag"))
				{
					callCommentActivity(position);
					//startActivity(new Intent(NotificationListActivity.this,GroupProfileActivity.class).putExtra("flag", true).putExtra("fromPendingInvitation", true));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(notificationList.get(position).getType().equals("Invitation"))
				{
					startActivity(new Intent(NotificationListActivity.this,PendingInvitationActivity.class));
					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(notificationList.get(position).getType().equals("JoinRequest"))
				{
					startActivity(new Intent(NotificationListActivity.this,PendingApprovalActivity.class).putExtra(Constants.GROUP_ID, notificationList.get(position).getGroupId()));
					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(notificationList.get(position).getType().equals("JoinRequestApprove"))
				{
					//Utility.setGroupObject(groupList.get(position-1));
					//startActivity(new Intent(NotificationListActivity.this,TabGroupPostActivity.class));
					//overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					callPostListActivity(position);
				}
				
				}
				catch(Exception e)
				{
					
				}
				
			}
		});
		
	}
	
	private void callPostListActivity(final int position)
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query1.whereEqualTo(Constants.OBJECT_ID, notificationList.get(position).getGroupId());
		query1.whereEqualTo(Constants.GROUP_STATUS, "Active");
		query1.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final  ParseObject groupoObject, ParseException e) {
				if(groupoObject!=null)
				{
					progressBar.setVisibility(View.GONE);
					Utility.setGroupObject(groupoObject);
					startActivity(new Intent(NotificationListActivity.this,TabGroupPostActivity.class));
					overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else
					progressBar.setVisibility(View.GONE);
			}});
	}
	
	private void callCommentActivity(final int position)
	{
		System.out.println("gid and fid "+notificationList.get(position).getGroupId()+"   "+notificationList.get(position).getFeedId());
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, notificationList.get(position).getGroupId());
		query.fromPin(Constants.MY_GROUP_LOCAL_DATA_STORE);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
		public void done(final  ParseObject groupoObject, ParseException e) {
			if(groupoObject!=null)
			{
				System.out.println("inside group from local");
				Utility.setGroupObject(groupoObject);
				ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
				query1.whereEqualTo(Constants.OBJECT_ID, notificationList.get(position).getFeedId());
				query1.include(Constants.USER_ID);
				query1.fromPin(notificationList.get(position).getGroupId());
				query1.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if(object!=null)
					{
						List<ParseObject> list=new ArrayList<ParseObject>();
						list.add(object);
						Utility.setList(list);	
						progressBar.setVisibility(View.GONE);
						final java.util.Date f1=list.get(0).getCreatedAt();
						Intent intent=new Intent(NotificationListActivity.this,CommentActivity.class);
						intent.putExtra(Constants.GROUP_NAME, groupoObject.get(Constants.GROUP_NAME).toString());
						intent.putExtra(Constants.GROUP_PICTURE,  groupoObject.getParseFile(Constants.THUMBNAIL_PICTURE).getUrl());
						intent.putExtra(Constants.GROUP_ID, groupoObject.getObjectId());
						intent.putExtra(Constants.PROFILE_PICTURE, list.get(0).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl());
						intent.putExtra("updatedtime", Utility.getTimeAgo(f1.getTime()));
						intent.putExtra("position", 0);
						startActivity(intent);
						overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);

					}
					else
					{
						//progressBar.setVisibility(View.GONE);
						System.out.println("before get feed table "+Utility.getCurrentDate());
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						//query.whereEqualTo(Constants.OBJECT_ID, notificationList.get(position).getFeedId());
						try {
							query.get(notificationList.get(position).getFeedId());
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						query.include(Constants.USER_ID);
						query.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(final  ParseObject object, ParseException e) {
							if(object!=null)
							{
								object.pinInBackground(notificationList.get(position).getGroupId(),new SaveCallback() {
						            @Override
						            public void done(ParseException e) {
						                if(e == null) {
						                	System.out.println("after get feed table "+Utility.getCurrentDate());
						                	progressBar.setVisibility(View.GONE);
						                	callCommentActivity(position);
						                }
						                }});
								
							}
							else
							{
								System.out.println("object null "+Utility.getCurrentDate());
								progressBar.setVisibility(View.GONE);
							}
						}});

					}
				}});
				
			}
			else
			{
				System.out.println("inside group from local");
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, notificationList.get(position).getGroupId());
				query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(final  ParseObject groupoObject, ParseException e) {
					if(groupoObject!=null)
					{
						groupoObject.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE,new SaveCallback() {
				            @Override
				            public void done(ParseException e) {
				                if(e == null) {
				                	progressBar.setVisibility(View.GONE);
				                	callCommentActivity(position);
				                	/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
									query.whereEqualTo(Constants.OBJECT_ID, notificationList.get(position).getFeedId());
									query.getFirstInBackground(new GetCallback<ParseObject>() {
									public void done(final  ParseObject object, ParseException e) {
										if(groupoObject!=null)
										{
											object.pinInBackground(Constants.FEED_LOCAL_DATA_STORE,new SaveCallback() {
									            @Override
									            public void done(ParseException e) {
									                if(e == null) {
									                	progressBar.setVisibility(View.GONE);
									                	callCommentActivity(position);
									                }
									                }});
											
										}
										else
											progressBar.setVisibility(View.GONE);
									}});
				                	*/
				                }
				                }});
						
					}
					else
						progressBar.setVisibility(View.GONE);
				}});
				
				System.out.println("inside lese 1");
				
			}
		}});
	}
	
	/*@Override
	public void onNewIntent(Intent newIntent) {
	    this.setIntent(newIntent);
System.out.println("inside onNewIntent");
	    flag=getIntent().getBooleanExtra("flag", false);        
	}*/

	private void initViews() {
		flag=getIntent().getBooleanExtra("flag", false);
		System.out.println("after receive flag "+flag);
		listView=(PullToRefreshListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		mBackImg=(ImageView) findViewById(R.id.back);
		inflater = getLayoutInflater();
		text=(TextView) findViewById(R.id.txt);
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
		mBackImg.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(flag)
			{
				  	Intent i = new Intent(NotificationListActivity.this, TabGroupActivity.class);
				    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    startActivity(i);
			}
			else
			{
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
			
			
		}
	});
	
	setAdapter();
}

	private void setAdapter() {
		DatabaseHelper db = new DatabaseHelper(this);
		notificationList=db.getAllNotification();
		text.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		/*if(listView.isRefreshing())
		{
			System.out.println("before complete");
			listView.onRefreshComplete();
		}*/
		if(notificationList.size() > 0)
		{
			mAdapter=new NotificationAdapter();
			listView.setAdapter(mAdapter);
			//if(listView.isRefreshing())
			//	listView.onRefreshComplete();
		}
		else
		{
			text.setVisibility(View.VISIBLE);
		}
		
	}
	
	
	
	class NotificationAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			return notificationList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint({ "ViewHolder", "SimpleDateFormat" }) @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.notification_list_item, parent, false);
			
			NetworkImageView image=(NetworkImageView) convertView.findViewById(R.id.notification_image);
			TextView message=(TextView) convertView.findViewById(R.id.notification_text);
			TextView time=(TextView) convertView.findViewById(R.id.updated_time);
			
			
			image.setImageUrl(notificationList.get(position).getImage(), imageLoader);
			imageLoader.get(notificationList.get(position).getImage(), ImageLoader.getImageListener(
					image, R.drawable.group_image,0));
			System.out.println("real date string "+notificationList.get(position).getTime());
			Date date=Utility.getCurrentDate();
			//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			//String dateString = notificationList.get(position).getTime().replace("Z", "GMT+00:00");
			//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
			try {
				 date = dateFormat.parse(notificationList.get(position).getTime());
			   // date = format.parse(notificationList.get(position).getTime());
			    System.out.println("Date ->" + date);
			} catch (Exception e) {
				//System.out.println("tiem string "+notificationList.get(position).getTime());
				System.out.println("inside catch "+e);
			}
			time.setText(Utility.getTimeAgo(date.getTime()));
			message.setText(notificationList.get(position).getText());
			
			
			//Picasso.with(NotificationListActivity.this).load("").placeholder(R.drawable.group_image).into(image);
			return convertView;
		}
	}
	
	@Override
    public void onBackPressed() {
		System.out.println("inside onBackPressed"+flag);
		if(flag)
		{
			Intent i = new Intent(this, TabGroupActivity.class);
		    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(i);
		}
        
    super.onBackPressed();
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
