package com.group.nearme;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class MemberListActivity extends Activity {
	ListView listView;
	ProgressBar progressBar;
	ParseObject groupObject;
	List<ParseObject> memberList;
	List<ParseObject> searchList;
	LayoutInflater inflater;
	ImageView mBackImg;
	EditText searchEditTxt;
	MemberAdapter mAdapter;
	public static boolean flag;
	ImageLoader imageLoader;
	int skipCount=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.member_listview);
		Utility.getTracker(this, "GROUP INFO - GROUP MEMBERS SCREEN");
		initViews();
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) 
			{
				Utility.setParseFile(memberList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
				//parseImageFile=userList.get(position).getParseFile(Constants.PROFILE_PICTURE);
				startActivity(new Intent(MemberListActivity.this,MemberProfileActivity.class)
				.putExtra("isFromFeed", false)
				.putExtra(Constants.USER_NAME, memberList.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
				.putExtra(Constants.MOBILE_NO, memberList.get(position).get(Constants.MEMBER_NO).toString())
				//.putExtra("byte", userList.get(position).getParseFile(Constants.PROFILE_PICTURE).getData())
				.putExtra(Constants.PROFILE_PICTURE, memberList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
				//Utility.setBitmapdata(memberList.get(position).getParseFile(Constants.MEMBER_IMAGE).getData());
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		searchEditTxt.addTextChangedListener(new TextWatcher() {
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		    		 mAdapter.filter(String.valueOf(cs));
		    }
		     
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		        // TODO Auto-generated method stub
		         
		    }
		     
		    @Override
		    public void afterTextChanged(Editable arg0) {
		        // TODO Auto-generated method stub                         
		    }
		});
	}

	private void initViews() {
		listView=(ListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		mBackImg=(ImageView) findViewById(R.id.back);
		searchEditTxt=(EditText) findViewById(R.id.search_box);
		inflater = getLayoutInflater();
		mAdapter=new MemberAdapter();
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
	mBackImg.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
			overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
		}
	});
	
	setAdapter();
}	
	
	class MemberAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			System.out.println("inside getcount ::: "+memberList.size());
			return memberList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.member_list_item, parent, false);
			
			if(position==memberList.size()-1)
			{
				getNext100Records();
			}
			
			ParseImageView image=(ParseImageView) convertView.findViewById(R.id.member_image);
			TextView name=(TextView) convertView.findViewById(R.id.member_name);
			TextView admin=(TextView) convertView.findViewById(R.id.admin);
			
			if(groupObject.getList(Constants.ADMIN_ARRAY).contains(memberList.get(position).get(Constants.MEMBER_NO).toString()))
				admin.setVisibility(View.VISIBLE);
			else
				admin.setVisibility(View.GONE);
			
			/*name.setText(memberList.get(position).get(Constants.MEMBER_NAME).toString());
			
			
			image.setParseFile(memberList.get(position).getParseFile(Constants.MEMBER_IMAGE));
			image.setPlaceholder(MemberListActivity.this.getResources().getDrawable(R.drawable.group_image));
			image.loadInBackground();*/
			
			ParseObject userObject = memberList.get(position).getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
				image.setParseFile(memberList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
				image.setPlaceholder(MemberListActivity.this.getResources().getDrawable(R.drawable.group_image));
				image.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			name.setText(memberList.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
			//image.setImageUrl(memberList.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl(), imageLoader);
			//imageLoader.get((memberList.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()), ImageLoader.getImageListener(
				//	image, R.drawable.group_image,0));
			//Picasso.with(MemberListActivity.this).load(userList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).placeholder(R.drawable.group_image).into(image);
			return convertView;
		}
		
		// Filter Class
		public void filter(String cs) {
			//nearGroupList.get(position).get(Constants.GROUP_NAME).toString()
			cs =  cs.toLowerCase(Locale.getDefault());
			memberList.clear();
			if (cs.length() == 0) {
				memberList.addAll(searchList);
			}
			else
			{
				for (int i=0;i<searchList.size();i++)
				{
					if (searchList.get(i).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME).toLowerCase(Locale.getDefault()).contains(cs))
					{
						memberList.add(searchList.get(i));
						
					}
				}
			}
			notifyDataSetChanged();
		}
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(flag)
		{
			flag=false;
			setAdapter();
		}
		
	}
	
	private void setAdapter()
	{
		groupObject=Utility.getGroupObject();
		ArrayList<String> memberNoList=(ArrayList<String>) groupObject.get(Constants.GROUP_MEMBERS);
		//ArrayList<String> adminArray=(ArrayList<String>) groupObject.get(Constants.ADMIN_ARRAY);
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereContainedIn(Constants.MEMBER_NO, memberNoList);
		query.whereEqualTo(Constants.MEMBER_STATUS, "Active");
		query.include(Constants.USER_ID);
		query.whereEqualTo(Constants.GROUP_ID, groupObject.getObjectId());
		query.orderByAscending("createdAt");
		query.setLimit(100);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						memberList=list;
						skipCount=memberList.size();
						searchList=new ArrayList<ParseObject>();
						searchList.addAll(memberList);
						listView.setAdapter(mAdapter);
						progressBar.setVisibility(View.GONE);
					}
			}});
		
		
		/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
     	query.whereContainedIn(Constants.MOBILE_NO, memberNoList);
     	//query.fromLocalDatastore();
     	query.findInBackground(new FindCallback<ParseObject>() {
     		public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							userList=list;
							searchList=new ArrayList<ParseObject>();
							searchList.addAll(userList);
							listView.setAdapter(mAdapter);
							progressBar.setVisibility(View.GONE);
						}
						else
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
					     	query.findInBackground(new FindCallback<ParseObject>() {
					     		public void done(List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											if(list.size() > 0)
											{
												ParseObject.pinAllInBackground(Constants.USER_LOCAL_DATA_STORE,list,new SaveCallback() {
													@Override
													public void done(ParseException arg0) {
														progressBar.setVisibility(View.GONE);
														setAdapter();
													}
												});
											}
										}
										else
											progressBar.setVisibility(View.GONE);
					     		}});
							progressBar.setVisibility(View.GONE);
						}
					}
     		}});
     	*/
	}
	
	private void getNext100Records()
	{
		ArrayList<String> memberNoList=(ArrayList<String>) groupObject.get(Constants.GROUP_MEMBERS);
		//ArrayList<String> adminArray=(ArrayList<String>) groupObject.get(Constants.ADMIN_ARRAY);
		//progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereContainedIn(Constants.MEMBER_NO, memberNoList);
		query.whereEqualTo(Constants.MEMBER_STATUS, "Active");
		query.include(Constants.USER_ID);
		query.whereEqualTo(Constants.GROUP_ID, groupObject.getObjectId());
		query.orderByAscending("createdAt");
		query.setSkip(skipCount);
		query.setLimit(100);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
						memberList.addAll(list);
						skipCount=memberList.size();
						searchList=new ArrayList<ParseObject>();
						searchList.addAll(memberList);
						//listView.setAdapter(mAdapter);
						mAdapter.notifyDataSetChanged();
						progressBar.setVisibility(View.GONE);
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
