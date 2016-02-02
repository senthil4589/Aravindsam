package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.group.nearme.MemberListActivity;
import com.group.nearme.MyGroupListActivity;
import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.NewGroupInfoActivity;
import com.group.nearme.PendingApprovalActivity;
import com.group.nearme.PendingInvitationActivity;
import com.group.nearme.R;
import com.group.nearme.adapter.PendingApprovalAdapter.ViewHolder;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class PendingInvitesAdapter extends BaseAdapter {

	ViewHolder viewHolder;
	LayoutInflater inflater;
	PendingInvitationActivity activity;
	List<ParseObject> list;
	private ProgressBar progressBar;
	private ParseFile mPostImgFile,userImage=null;
	int memberCount,userPoints;
	List<String> joinRequestList;
	ParseGeoPoint point;
	Location groupLocation=new Location("GroupLocation");
	Location currentLocation=new Location("CurrentLocation");
	AlertDialog mAlertDialog;
	public PendingInvitesAdapter(PendingInvitationActivity activity,List<ParseObject> list,ProgressBar progressBar1)
	{
		this.activity=activity;
		this.list=list;
		this.progressBar=progressBar1;
		inflater = activity.getLayoutInflater();
		point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	
	@Override
	public View getView(final int position, View view, ViewGroup parent) {
			view = inflater.inflate(R.layout.pending_invites, null);
			viewHolder = new ViewHolder();
			viewHolder.groupName=(TextView) view.findViewById(R.id.group_name);
			viewHolder.image=(ImageView) view.findViewById(R.id.user_pic);
			viewHolder.memberCount=(TextView) view.findViewById(R.id.member_count);
			viewHolder.acceptBtn=(ImageView) view.findViewById(R.id.accept);
			viewHolder.rejectBtn=(ImageView) view.findViewById(R.id.reject);

			Picasso.with(activity).load(list.get(position).getParseFile(Constants.GROUP_PICTURE).getUrl()).into(viewHolder.image);
			viewHolder.groupName.setText(list.get(position).get(Constants.GROUP_NAME).toString());
			
			viewHolder.memberCount.setText(String.valueOf(list.get(position).getInt(Constants.MEMBER_COUNT)));
			/*if(list.get(position).getInt(Constants.MEMBER_COUNT) == 1)
			{
				viewHolder.memberCount.setText(list.get(position).getInt(Constants.MEMBER_COUNT)+" Member");
			}
			else if(list.get(position).getInt(Constants.MEMBER_COUNT) > 1)
			{
				viewHolder.memberCount.setText(list.get(position).getInt(Constants.MEMBER_COUNT)+" Members");
			}*/
			
			
			viewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					viewHolder.acceptBtn.setEnabled(false);
					viewHolder.rejectBtn.setEnabled(false);
					acceptInvitation(position);
					
					/*if(list.get(position).getString(Constants.GROUP_TYPE).equals("Secret"))
					{
						acceptInvitation(position);
					}
					else
					{
					groupLocation.setLatitude(list.get(position).getParseGeoPoint(Constants.GROUP_LOCATION).getLatitude());
					groupLocation.setLongitude(list.get(position).getParseGeoPoint(Constants.GROUP_LOCATION).getLongitude());
					
					currentLocation.setLatitude(activity.gpsTracker.getLatitude());
					currentLocation.setLongitude(activity.gpsTracker.getLongitude());
					float distance = currentLocation.distanceTo(groupLocation);
					if(distance <= 500)
					{
						acceptInvitation(position);
					}
					else
					{
						mAlertDialog=new AlertDialog.Builder(activity)
						.setMessage("Your join request could not be completed. Please try again when you are closer to the place the group was created.")
						.setPositiveButton("Ok", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mAlertDialog.dismiss();
							}
						}).show();
						//Toast.makeText(activity, "You join request could not be completed. Please try again when you are closer to the place the group was created.", Toast.LENGTH_LONG).show();
					}
				
					}*/
				}

			});
			
			viewHolder.rejectBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					viewHolder.acceptBtn.setEnabled(false);
					viewHolder.rejectBtn.setEnabled(false);
					rejectJoinInvitation();
				}

				private void rejectJoinInvitation() {
					progressBar.setVisibility(View.VISIBLE);
					
					ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.INVITATION_TABLE);
					query1.whereEqualTo(Constants.GROUP_ID, list.get(position).getObjectId());
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
							object1.put(Constants.GROUP_ID, list.get(position).getObjectId());
							object1.put(Constants.INVITATION_ACCEPT, false);
							object1.put(Constants.INVITATION_TYPE, "Invites");
							object1.saveInBackground();
							progressBar.setVisibility(View.GONE);
 							NearByGroupListActivity.flag2=true;
					        	MyGroupListActivity.flag1=true;
					        	list.remove(position);
					        	notifyDataSetChanged();
						}
						else
							progressBar.setVisibility(View.GONE);
					}});
					
					
					
				}
			});
		return view;
	}
	protected class ViewHolder
	{
		protected TextView groupName,memberCount;
		protected ImageView acceptBtn,rejectBtn,image;
		
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
		userObject.put(Constants.GROUP_ID, list.get(position).getObjectId());
		userObject.put(Constants.POST_TEXT, latestPost);
		userObject.put(Constants.POST_TYPE, "Member");
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.POST_POINT, 0);
		userObject.put(Constants.LIKE_ARRAY, list1);
		userObject.put(Constants.DIS_LIKE_ARRAY, list1);
		userObject.put(Constants.FLAG_ARRAY, list1);
		userObject.put(Constants.POST_IMAGE, list.get(position).getParseFile(Constants.GROUP_PICTURE));
		userObject.put(Constants.IMAGE_CAPTION, "");
		userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();
		userObject.pinInBackground(list.get(position).getObjectId());
		
		ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
 		memberObject.put(Constants.MEMBER_NO,  PreferenceSettings.getMobileNo());
 		memberObject.put(Constants.GROUP_ID, list.get(position).getObjectId());
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
		query1.whereEqualTo(Constants.GROUP_ID, list.get(position).getObjectId());
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
		object1.put(Constants.GROUP_ID, list.get(position).getObjectId());
		object1.put(Constants.INVITATION_ACCEPT, true);
		object1.put(Constants.INVITATION_TYPE, "Invites");
		object1.saveInBackground();
		
	
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
 		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
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
					             								myGroupList.add(list.get(position).getObjectId());
					             								object.put(Constants.MY_GROUP_ARRAY, myGroupList);
					             								joinRequestList=object.getList(Constants.GROUP_INVITATION);
					             								if(joinRequestList.contains(list.get(position).getObjectId()))
					             								{
					             									joinRequestList.remove(list.get(position).getObjectId());
					             									object.put(Constants.GROUP_INVITATION, joinRequestList);
					             									removeJoinRequest(list.get(position).getObjectId());
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
							             							        	list.remove(position);
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
