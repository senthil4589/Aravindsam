package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.group.nearme.MyGroupListActivity;
import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.R;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;


public class GroupItemAdapter extends BaseAdapter
{
	ViewHolder viewHolder;
	LayoutInflater inflater;
	Activity activity;
	List<ParseObject> groupList,searchList,memberList;
	ImageLoader imageLoader;
	int count;
	boolean flag;

	ArrayList<String> pendingInvitesIDList;
	private ProgressBar progressBar;
	int memberCount,userPoints;
	List<String> joinRequestList;
	ParseGeoPoint point;

	public GroupItemAdapter(MyGroupListActivity activity,List<ParseObject> list,List<ParseObject> list1,ArrayList<String> pendingInvitesIDList1,ProgressBar progressBar1)
	{
		this.activity=activity;
		this.groupList=list;
		this.memberList=list1;
		this.pendingInvitesIDList=pendingInvitesIDList1;
		this.progressBar=progressBar1;
		this.searchList=new ArrayList<ParseObject>();
		this.searchList.addAll(groupList);
		inflater = activity.getLayoutInflater();
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
		if(activity.gpsTracker.canGetLocation())
			point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		else
			point =new ParseGeoPoint(0.0,0.0);
	}
	
	@Override
	public int getCount() {
		return groupList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		//if (view == null) {

		if(pendingInvitesIDList.contains(groupList.get(position).getObjectId()))
		{
			view = inflater.inflate(R.layout.pending_invites_main_page, null);
			viewHolder = new ViewHolder();
			viewHolder.groupName=(TextView) view.findViewById(R.id.group_name);
			viewHolder.image=(ImageView) view.findViewById(R.id.user_pic);
			viewHolder.totalMember=(TextView) view.findViewById(R.id.member_count);
			final ImageView acceptBtn=(ImageView) view.findViewById(R.id.accept);
			final ImageView rejectBtn=(ImageView) view.findViewById(R.id.reject);

			Picasso.with(activity).load(groupList.get(position).getParseFile(Constants.GROUP_PICTURE).getUrl()).into(viewHolder.image);
			viewHolder.groupName.setText(groupList.get(position).get(Constants.GROUP_NAME).toString());

			viewHolder.totalMember.setText(String.valueOf(groupList.get(position).getInt(Constants.MEMBER_COUNT)));

			acceptBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					acceptBtn.setEnabled(false);
					rejectBtn.setEnabled(false);
					acceptInvitation(position);
				}

			});

			rejectBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					acceptBtn.setEnabled(false);
					rejectBtn.setEnabled(false);
					rejectJoinInvitation();
				}

				private void rejectJoinInvitation() {
					progressBar.setVisibility(View.VISIBLE);

					ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.INVITATION_TABLE);
					query1.whereEqualTo(Constants.GROUP_ID, groupList.get(position).getObjectId());
					query1.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());
					query1.whereEqualTo(Constants.INVITATION_STATUS, "Active");
					query1.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(ParseObject object, ParseException e) {
							if(object!=null)
							{
								object.put(Constants.INVITATION_STATUS, "InActive");
								object.saveInBackground();
								ParseObject object1 = new ParseObject(Constants.INVITATION_ACTIVITY_TABLE);
								object1.put(Constants.BY_USER, PreferenceSettings.getMobileNo());
								object1.put(Constants.TO_USER, PreferenceSettings.getMobileNo());
								object1.put(Constants.ACTIVITY_LOCATION, point);
								object1.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
								object1.put(Constants.INVITATION_ACCEPT, false);
								object1.put(Constants.INVITATION_TYPE, "Invites");
								object1.saveInBackground();
								progressBar.setVisibility(View.GONE);
								NearByGroupListActivity.flag2=true;
								MyGroupListActivity.flag1=true;
								pendingInvitesIDList.remove(groupList.get(position).getObjectId());
								groupList.remove(position);
								notifyDataSetChanged();
							}
							else
								progressBar.setVisibility(View.GONE);
						}});



				}
			});
		}
		else {
			view = inflater.inflate(R.layout.my_group_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.groupImage = (ParseImageView) view.findViewById(R.id.group_image);
			//viewHolder.groupTypeImage=(ImageView) view.findViewById(R.id.group_type_image);
			viewHolder.groupName = (TextView) view.findViewById(R.id.group_name);
			viewHolder.groupType = (TextView) view.findViewById(R.id.group_type);
			//viewHolder.groupTypeImage=(ImageView) view.findViewById(R.id.group_type);
			viewHolder.admin = (TextView) view.findViewById(R.id.admin);
			viewHolder.unreadCount = (TextView) view.findViewById(R.id.unread_count);
			viewHolder.totalMember = (TextView) view.findViewById(R.id.total_member);

			//view.setTag(viewHolder);
			//}else {
			//	viewHolder = (ViewHolder) view.getTag();
			//}
			viewHolder.groupName.setText(groupList.get(position).get(Constants.GROUP_NAME).toString());
			//ParseFile file=(ParseFile) groupList.get(position).get(Constants.GROUP_PICTURE);
			//Picasso.with(activity).load(file.getUrl()).placeholder(R.drawable.group_image).into(viewHolder.groupImage);

			for (int i = 0; i < memberList.size(); i++) {
				if (memberList.get(i).get(Constants.GROUP_ID).toString().equals(groupList.get(position).getObjectId())) {
				/*//viewHolder.unreadCount.setVisibility(View.VISIBLE);
				if(memberList.get(i).getInt(Constants.UNREAD_MESSAGES) > 0)
				{
					count=memberList.get(i).getInt(Constants.UNREAD_MESSAGES);
					//viewHolder.unreadCount.setVisibility(View.VISIBLE);
					//String count=String.valueOf(count);
					//viewHolder.unreadCount.setText(count);
				}
				else
				{
					viewHolder.unreadCount.setVisibility(View.GONE);
				}*/
					count = memberList.get(i).getInt(Constants.UNREAD_MESSAGES);
					break;
				}
			}

			if (count > 0) {
				viewHolder.unreadCount.setVisibility(View.VISIBLE);
				viewHolder.unreadCount.setText(String.valueOf(count));
				count = 0;
			} else {
				viewHolder.unreadCount.setVisibility(View.GONE);
			}


			if (groupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Public")) {
				viewHolder.groupType.setText("CHATTER");
			} else {
				viewHolder.groupType.setText(groupList.get(position).get(Constants.GROUP_TYPE).toString().toUpperCase());
			}


			if (groupList.get(position).getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
				viewHolder.admin.setVisibility(View.VISIBLE);
			else
				viewHolder.admin.setVisibility(View.GONE);


			viewHolder.totalMember.setText(groupList.get(position).get(Constants.MEMBER_COUNT).toString());
			if (groupList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE) == null)
				Picasso.with(activity).load(groupList.get(position).getParseFile(Constants.GROUP_PICTURE).getUrl()).placeholder(R.drawable.group_image).into(viewHolder.groupImage);
			else
				Picasso.with(activity).load(groupList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).placeholder(R.drawable.group_image).into(viewHolder.groupImage);
			//viewHolder.groupImage.setImageUrl(groupList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl(), imageLoader);
			//imageLoader.get((groupList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()), ImageLoader.getImageListener(
			//		viewHolder.groupImage, R.drawable.group_image, 0));
		}
		return view;
	}

	protected class ViewHolder
	{
		protected TextView groupName,totalMember,admin,unreadCount,groupType;
		//protected ImageView groupTypeImage;
		protected ParseImageView groupImage;
		protected ImageView acceptBtn,rejectBtn,image;
		//ImageView groupTypeImage;
	}
	

	// Filter Class
	public void filter(String cs) {
		//nearGroupList.get(position).get(Constants.GROUP_NAME).toString()
		cs =  cs.toLowerCase(Locale.getDefault());
		groupList.clear();
		if (cs.length() == 0) {
			groupList.addAll(searchList);
		}
		else
		{
			for (int i=0;i<searchList.size();i++)
			{
				if (searchList.get(i).get(Constants.GROUP_NAME).toString().toLowerCase(Locale.getDefault()).contains(cs))
				{
					groupList.add(searchList.get(i));
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public void setMemberList(List<ParseObject> list)
	{
		this.memberList=list;
	}

	private void acceptInvitation(final int position)
	{
		progressBar.setVisibility(View.VISIBLE);

		final String latestPost=PreferenceSettings.getUserName()+" - "+activity.getResources().getString(R.string.new_member);
		ArrayList<String> list1=new ArrayList<String>();
		ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		/*userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		if(userImage!=null)
			userObject.put(Constants.MEMBER_IMAGE, userImage);*/
		userObject.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
		userObject.put(Constants.POST_TEXT, latestPost);
		userObject.put(Constants.POST_TYPE, "Member");
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.POST_POINT, 0);
		userObject.put(Constants.LIKE_ARRAY, list1);
		userObject.put(Constants.DIS_LIKE_ARRAY, list1);
		userObject.put(Constants.FLAG_ARRAY, list1);
		userObject.put(Constants.POST_IMAGE, groupList.get(position).getParseFile(Constants.GROUP_PICTURE));
		userObject.put(Constants.IMAGE_CAPTION, "");
		userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();
		userObject.pinInBackground(groupList.get(position).getObjectId());

		ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
		memberObject.put(Constants.MEMBER_NO,  PreferenceSettings.getMobileNo());
		memberObject.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
		memberObject.put(Constants.ADDITIONAL_INFO_PROVIDED, "");
		memberObject.put(Constants.JOIN_DATE, Utility.getCurrentDate());
		memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
		memberObject.put(Constants.EXIT_GROUP, false);
		memberObject.put(Constants.EXITED_BY, "");
		memberObject.put(Constants.MEMBER_STATUS, "Active");
		//memberObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		//memberObject.put(Constants.MEMBER_IMAGE, Utility.getUserImageFile());
		memberObject.put(Constants.GROUP_ADMIN, false);
		memberObject.put(Constants.UNREAD_MESSAGES, 0);
		memberObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		memberObject.saveInBackground();

		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.INVITATION_TABLE);
		query1.whereEqualTo(Constants.GROUP_ID, groupList.get(position).getObjectId());
		query1.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());

		query1.whereEqualTo(Constants.INVITATION_STATUS, "Active");
		query1.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if(object!=null)
				{
					object.put(Constants.INVITATION_STATUS, "InActive");
					object.saveInBackground();
				}
			}});


		ParseObject object1 = new ParseObject(Constants.INVITATION_ACTIVITY_TABLE);
		object1.put(Constants.BY_USER, PreferenceSettings.getMobileNo());
		object1.put(Constants.TO_USER, PreferenceSettings.getMobileNo());
		object1.put(Constants.ACTIVITY_LOCATION, point);
		object1.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
		object1.put(Constants.INVITATION_ACCEPT, true);
		object1.put(Constants.INVITATION_TYPE, "Invites");
		object1.saveInBackground();


		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, groupList.get(position).getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (e == null)
				{
					if(object!=null)
					{
						//object.pinInBackground("MyGroup");
						object.increment(Constants.MEMBER_COUNT, 1);
						object.increment(Constants.NEWS_FEED_COUNT, 1);
						object.put(Constants.LATEST_POST,latestPost);
						memberCount=object.getInt(Constants.MEMBER_COUNT);
						ArrayList<String> memberNoList=(ArrayList<String>) object.get(Constants.GROUP_MEMBERS);
						memberNoList.add(PreferenceSettings.getMobileNo());
						object.put(Constants.GROUP_MEMBERS, memberNoList);
						object.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
						object.saveInBackground(new SaveCallback() {
							public void done(ParseException e) {
								if (e == null) {

									ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
									query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
									query.getFirstInBackground(new GetCallback<ParseObject>() {
										public void done(ParseObject object, ParseException e) {
											if (e == null)
											{
												if(object!=null)
												{
													List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
													myGroupList.add(groupList.get(position).getObjectId());
													object.put(Constants.MY_GROUP_ARRAY, myGroupList);
													joinRequestList=object.getList(Constants.GROUP_INVITATION);
													if(joinRequestList.contains(groupList.get(position).getObjectId()))
													{
														joinRequestList.remove(groupList.get(position).getObjectId());
														object.put(Constants.GROUP_INVITATION, joinRequestList);
														removeJoinRequest(groupList.get(position).getObjectId());
													}

													if(PreferenceSettings.isJoinFirst())
													{
														userPoints=1000;
														PreferenceSettings.setJoinStatus(false);
													}
													else
													{
														userPoints=100;
													}

													if(memberCount==10)
														userPoints=userPoints+1000;
													else if(memberCount==50)
														userPoints=userPoints+2000;
													object.increment(Constants.BADGE_POINT, userPoints);
													object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
													object.saveInBackground(new SaveCallback() {
														public void done(ParseException e) {
															if (e == null) {
																//Toast.makeText(activity, "You joined successfully", Toast.LENGTH_LONG).show();
																progressBar.setVisibility(View.GONE);
																NearByGroupListActivity.flag2=true;
																MyGroupListActivity.flag1=true;
																//groupList.remove(position);
																pendingInvitesIDList.remove(groupList.get(position).getObjectId());
																notifyDataSetChanged();

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

	private void removeJoinRequest(String groupId)
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.whereEqualTo(Constants.GROUP_ID, groupId);
		query.whereEqualTo(Constants.POST_TYPE, "Invitation");
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if(object!=null)
				{
					object.put(Constants.POST_STATUS, "InActive");
					object.saveInBackground();
				}
			}
		});
	}
}
