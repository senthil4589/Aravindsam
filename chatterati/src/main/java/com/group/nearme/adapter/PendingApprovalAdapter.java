package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.group.nearme.GroupPostListActivity;
import com.group.nearme.MyGroupListActivity;
import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.PendingApprovalActivity;
import com.group.nearme.R;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class PendingApprovalAdapter extends BaseAdapter {

	ViewHolder viewHolder;
	LayoutInflater inflater;
	PendingApprovalActivity activity;
	List<ParseObject> list;//,userList;
	private ProgressBar progressBar;
	int memberCount,userPoints;
	ParseGeoPoint point;
	public PendingApprovalAdapter(PendingApprovalActivity activity,List<ParseObject> list,ProgressBar progressBar1)
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
			view = inflater.inflate(R.layout.join_invitation, null);
			viewHolder = new ViewHolder();
			viewHolder.joinRequest=(TextView) view.findViewById(R.id.join_invitation_txt);
			viewHolder.name=(TextView) view.findViewById(R.id.name);
			viewHolder.image=(ParseImageView) view.findViewById(R.id.user_pic);
			viewHolder.time=(TextView) view.findViewById(R.id.updated_time);
			viewHolder.line=(View) view.findViewById(R.id.line1);
			viewHolder.additional_info=(TextView) view.findViewById(R.id.additional_info);
			final ImageView acceptBtn=(ImageView) view.findViewById(R.id.accept);
			final ImageView rejectBtn=(ImageView) view.findViewById(R.id.reject);
			
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.image);
			//viewHolder.name.setText(list.get(position).get(Constants.MEMBER_NAME).toString());
			
			ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.image.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.image.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.name.setText(list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
			final java.util.Date f=list.get(position).getCreatedAt();
			viewHolder.time.setText(Utility.getTimeAgo(f.getTime()));
			if(list.get(position).get(Constants.POST_TEXT).toString().equals("No Information Available") || list.get(position).get(Constants.POST_TEXT).toString().isEmpty())
			{
				viewHolder.line.setVisibility(View.GONE);
				viewHolder.additional_info.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.line.setVisibility(View.VISIBLE);
				viewHolder.additional_info.setVisibility(View.VISIBLE);
				viewHolder.additional_info.setText(list.get(position).get(Constants.POST_TEXT).toString());
			}
			
			acceptBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					acceptBtn.setEnabled(false);
					rejectBtn.setEnabled(false);
					acceptJoinInvitation();
					
				}

				private void acceptJoinInvitation() {
					progressBar.setVisibility(View.VISIBLE);
					
					final String latestPost=list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME)+" - "+activity.getResources().getString(R.string.new_member);
					 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
	              		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).get(Constants.GROUP_ID).toString());
	              		query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object, ParseException e) {
									if (object!=null) 
									{
										List<String> adminList=object.getList(Constants.ADMIN_ARRAY);
										if(!object.getBoolean(Constants.MEMBERSHIP_APPROVAL) || adminList.contains(PreferenceSettings.getMobileNo()))
										{
											
						             		
											ParseObject object1 = new ParseObject(Constants.INVITATION_ACTIVITY_TABLE);
											object1.put(Constants.BY_USER, PreferenceSettings.getMobileNo());
											object1.put(Constants.TO_USER, list.get(position).get(Constants.MOBILE_NO).toString());
											object1.put(Constants.ACTIVITY_LOCATION, point);
											object1.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
											object1.put(Constants.INVITATION_ACCEPT, true);
											object1.put(Constants.INVITATION_TYPE, "Request");
											object1.saveInBackground();
											object.increment(Constants.MEMBER_COUNT, 1);
											memberCount=object.getInt(Constants.MEMBER_COUNT);
											object.put(Constants.LATEST_POST,latestPost);
											ArrayList<String> memberNoList=(ArrayList<String>) object.get(Constants.GROUP_MEMBERS);
				 							memberNoList.add(list.get(position).get(Constants.MOBILE_NO).toString());
				 							object.put(Constants.GROUP_MEMBERS, memberNoList);
											object.saveInBackground(new SaveCallback() {
         							        public void done(ParseException e) {
         							          if (e == null) {
         							        	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
         										query.whereEqualTo(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString());
         										query.getFirstInBackground(new GetCallback<ParseObject>() {
         											public void done(ParseObject object, ParseException e) {
         													if (e == null) 
         													{
         														if(object!=null)
         														{
         															ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
         										             		memberObject.put(Constants.MEMBER_NO,  list.get(position).get(Constants.MOBILE_NO).toString());
         										             		memberObject.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
         										             		memberObject.put(Constants.ADDITIONAL_INFO_PROVIDED, list.get(position).get(Constants.POST_TEXT).toString());
         										             		memberObject.put(Constants.JOIN_DATE, Utility.getCurrentDate());
         										             		memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
         										             		memberObject.put(Constants.EXIT_GROUP, false);
         										             		memberObject.put(Constants.EXITED_BY, "");
         										             		memberObject.put(Constants.MEMBER_STATUS, "Active");
         										             		//memberObject.put(Constants.MEMBER_NAME, list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
         										             		//memberObject.put(Constants.MEMBER_IMAGE, list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
         										             		memberObject.put(Constants.GROUP_ADMIN, false);
         										             		memberObject.put(Constants.UNREAD_MESSAGES, 0);
         										             		memberObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, object.getObjectId()));
         										             		memberObject.saveInBackground();
         															List<String> idList=object.getList(Constants.GROUP_INVITATION);
         															idList.remove(list.get(position).get(Constants.GROUP_ID).toString());
         															object.put(Constants.GROUP_INVITATION, idList);
         															List<String> groupArray=object.getList(Constants.MY_GROUP_ARRAY);
         															groupArray.add(list.get(position).get(Constants.GROUP_ID).toString());
         															object.put(Constants.MY_GROUP_ARRAY, groupArray);
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
         															PreferenceSettings.setGroupInvitationList(idList);
         															object.saveInBackground(new SaveCallback() {
         														          public void done(ParseException e) {
         														                 if (e == null) {
														            					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
														            					query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
														            					query.getFirstInBackground(new GetCallback<ParseObject>() {
														            					public void done(ParseObject object, ParseException e) {
														            					if(object!=null)
														            					{
														            						object.put(Constants.POST_TEXT, latestPost);
														            						object.put(Constants.POST_TYPE, "Member");
														            						object.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
														            						object.pinInBackground(list.get(position).get(Constants.GROUP_ID).toString());
														            						object.saveInBackground(new SaveCallback() {
												             							        public void done(ParseException e) {
													             							          if (e == null) {
													             							        		//progressBar.setVisibility(View.GONE);
													             							        		removeFromInvitation(position);
													             							        		/* NearByGroupListActivity.flag2=true;
													             							        		MyGroupListActivity.flag1=true;
													             							        		//activity.setAdapter();
													             							        		list.remove(position);
													             							        		Utility.setInvitationList(list);
													             							        		removeFromInvitation(position);
													             							        		notifyDataSetChanged();*/
																							            	//Toast.makeText(activity, "Join request accepted", Toast.LENGTH_SHORT).show();
													             							          }}});
														            					}
														            				}});
									             							        }
									             							     }});
										            							
										              						}
										              					}
										              			}
										              		});
									                 }
									          }});
									}
									else
									{
										progressBar.setVisibility(View.GONE);
						            	Toast.makeText(activity, "You Don't have permission to accept ", Toast.LENGTH_LONG).show();		
									}
								}
								
							}
						});
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
					ParseObject object1 = new ParseObject(Constants.INVITATION_ACTIVITY_TABLE);
					object1.put(Constants.BY_USER, PreferenceSettings.getMobileNo());
					object1.put(Constants.TO_USER, list.get(position).get(Constants.MOBILE_NO).toString());
					object1.put(Constants.ACTIVITY_LOCATION, point);
					object1.put(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
					object1.put(Constants.INVITATION_ACCEPT, false);
					object1.put(Constants.INVITATION_TYPE, "Request");
					object1.saveInBackground();
					 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
	              		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).get(Constants.GROUP_ID).toString());
	              		query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object, ParseException e) {
									if (object!=null) 
									{
										if(!object.getBoolean(Constants.MEMBERSHIP_APPROVAL) || object.get(Constants.MOBILE_NO).toString().equals(PreferenceSettings.getMobileNo()))
										{
											object.increment(Constants.NEWS_FEED_COUNT, -1);
	             							//list.get(0).put(Constants.LATEST_POST,request);
											object.saveInBackground(new SaveCallback() {
          							        public void done(ParseException e) {
          							          if (e == null) {
          							        	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
          										query.whereEqualTo(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString());
          										query.getFirstInBackground(new GetCallback<ParseObject>() {
          											public void done(ParseObject object, ParseException e) {
          													if (e == null) 
          													{
          														if(object!=null)
          														{
          															List<String> idList=object.getList(Constants.GROUP_INVITATION);
          															idList.remove(list.get(position).get(Constants.GROUP_ID).toString());
          															object.put(Constants.GROUP_INVITATION, idList);
          															PreferenceSettings.setGroupInvitationList(idList);
          															object.saveInBackground(new SaveCallback() {
          														          public void done(ParseException e) {
          														                 if (e == null) {
														            					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
														            					query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
														            					
														            					query.getFirstInBackground(new GetCallback<ParseObject>() {
														            					public void done(ParseObject object, ParseException e) {
														            					if(object!=null)
														            					{
														            						object.deleteInBackground(new DeleteCallback() {
												             							        public void done(ParseException e) {
													             							          if (e == null) {
													             							        		progressBar.setVisibility(View.GONE);
													             							        		 NearByGroupListActivity.flag2=true;
													             							        		 GroupPostListActivity.flag=true;
													             							        		//activity.setAdapter();
													             							        		list.remove(position);
													             							        		//Utility.setInvitationList(list);
													             							        		notifyDataSetChanged();
													             							        		Toast.makeText(activity, "Join rquest rejected", Toast.LENGTH_SHORT).show();
													             							          }}});
														            					}
														            				}});
									             							        }
									             							     }});
										            							
										              						}
										              					}
										              			}
										              		});
									                 }
									          }});
									}
									else
									{
										progressBar.setVisibility(View.GONE);
						            	Toast.makeText(activity, "You Don't have permission to reject ", Toast.LENGTH_LONG).show();		
									}
								}
								
							}
						});
				}
			});
		
		
		return view;
	}
	protected class ViewHolder
	{
		protected TextView name,time,additional_info,joinRequest;
		protected ParseImageView image;
		protected View line;
		
	}
	
	private void removeFromInvitation(final int position)
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
		query.whereEqualTo(Constants.TO_USER, list.get(position).get(Constants.MOBILE_NO).toString());
		query.whereEqualTo(Constants.INVITATION_STATUS, "Active");
		query.getFirstInBackground(new GetCallback<ParseObject>() {
		public void done(ParseObject object, ParseException e) {
			if(object!=null)
			{
				object.put(Constants.INVITATION_STATUS, "InActive");
				object.saveInBackground();
				progressBar.setVisibility(View.GONE);
				 NearByGroupListActivity.flag2=true;
	        		MyGroupListActivity.flag1=true;
	        		 GroupPostListActivity.flag=true;
	        		//activity.setAdapter();
	        		list.remove(position);
	        		//Utility.setInvitationList(list);
	        		//removeFromInvitation(position);
	        		notifyDataSetChanged();
			}
			else
			{
				progressBar.setVisibility(View.GONE);
				 NearByGroupListActivity.flag2=true;
	        		MyGroupListActivity.flag1=true;
	        		 GroupPostListActivity.flag=true;
	        		//activity.setAdapter();
	        		list.remove(position);
	        		//Utility.setInvitationList(list);
	        		//removeFromInvitation(position);
	        		notifyDataSetChanged();
			}
		}});
	}
}
