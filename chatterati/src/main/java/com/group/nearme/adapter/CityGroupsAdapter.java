package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.group.nearme.CityGroupsActivity;
import com.group.nearme.MyGroupListActivity;
import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.R;
import com.group.nearme.TabGroupPostActivity;
import com.group.nearme.TopicListActivity;
import com.group.nearme.adapter.NearByMeAdapter.ViewHolder;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class CityGroupsAdapter extends BaseAdapter
{
	ViewHolder viewHolder;
	LayoutInflater inflater;
	CityGroupsActivity activity;
	static boolean flag;
	static int joinPosition;
	List<ParseObject> nearGroupList,searchList;
	boolean flag1;
	ProgressBar progressBar;
	public Dialog mDialog;
	String request="";
	List<String> mInvitationList,myGroupList;
	int memberCount,userPoints;
	ImageLoader imageLoader;
	
	public CityGroupsAdapter(CityGroupsActivity activity,List<ParseObject> list1,List<String> list2,List<String> mInvitationList1,ProgressBar progressBar1)
	{
		this.activity=activity;
		this.nearGroupList=list1;
		this.myGroupList=list2;
		this.mInvitationList=mInvitationList1;
		this.progressBar=progressBar1;
		inflater = activity.getLayoutInflater();
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
		
		this.searchList=new ArrayList<ParseObject>();
		this.searchList.addAll(nearGroupList);
				
	}
	
	@Override
	public int getCount() {
		return nearGroupList.size();
	}


	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressLint({ "ResourceAsColor", "DefaultLocale" }) @Override
	public View getView(final int position, View view, ViewGroup parent) {
		if (view == null) {
			view = inflater.inflate(R.layout.near_by_me_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.groupImage=(ParseImageView) view.findViewById(R.id.group_image);
			//viewHolder.openPublicType=(TextView) view.findViewById(R.id.open_public_type);
			viewHolder.groupName=(TextView) view.findViewById(R.id.group_name);
			//viewHolder.groupDes=(TextView) view.findViewById(R.id.group_des);
			viewHolder.totalMembers=(TextView) view.findViewById(R.id.total_member);
			//viewHolder.groupType=(ImageView) view.findViewById(R.id.group_type);
			viewHolder.groupType=(TextView) view.findViewById(R.id.group_type);
			//viewHolder.invitationStatus=(TextView) view.findViewById(R.id.invitation_status);
			
			viewHolder.joinFrame=(FrameLayout) view.findViewById(R.id.join_frame);
			view.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) view.getTag();
		}
		final Button joinBtn=(Button) view.findViewById(R.id.join);
		
		viewHolder.groupName.setText(nearGroupList.get(position).get(Constants.GROUP_NAME).toString());
		viewHolder.totalMembers.setText(String.valueOf(nearGroupList.get(position).getInt(Constants.MEMBER_COUNT)));
		if(nearGroupList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE)!=null)
		Picasso.with(activity).load(nearGroupList.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).placeholder(R.drawable.group_image).into(viewHolder.groupImage);
		
		if(nearGroupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Public"))
		{
			viewHolder.groupType.setText("CHATTER");
		}
		else
		{
			viewHolder.groupType.setText(nearGroupList.get(position).get(Constants.GROUP_TYPE).toString().toUpperCase());
		}
		
		joinBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//progressBar.setVisibility(View.VISIBLE);
				//viewHolder.joinBtn.setClickable(false);
				joinBtn.setEnabled(false);
				updateGroupTable(position);
			}
		});
		if(nearGroupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Private") && mInvitationList.contains(nearGroupList.get(position).getObjectId()))
		{
			joinBtn.setEnabled(false);
			joinBtn.setText("PENDING");
			joinBtn.setBackgroundColor(activity.getResources().getColor(R.color.pending_color));
			
		}
		else
		{
			joinBtn.setEnabled(true);
			joinBtn.setText("JOIN");
			joinBtn.setBackgroundColor(activity.getResources().getColor(R.color.join_color));
		}
		
		return view;
	}

	private void updateGroupTable(final int position) {
	
	 if(nearGroupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Private"))
		{
			if(nearGroupList.get(position).getInt(Constants.JOB_HOURS) > 0)
			{
				insertValues(position);
			}
			else if(nearGroupList.get(position).getBoolean(Constants.ADDITIONAL_INFO_REQUIRED))
			{
				
				mDialog = new Dialog(activity);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);     
				mDialog.setContentView(R.layout.ask_additional_info);
				mDialog.setCancelable(true);
				mDialog.setCanceledOnTouchOutside(true);
				WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
				windowManager.gravity = Gravity.CENTER;
				TextView text=(TextView) mDialog.findViewById(R.id.additional_info_text);
				final EditText box=(EditText) mDialog.findViewById(R.id.additional_info_box);
				text.setText("Enter "+nearGroupList.get(position).get(Constants.INFO_REQUIRED).toString());

				Button send=(Button) mDialog.findViewById(R.id.send);
				Button cancel=(Button) mDialog.findViewById(R.id.cancel);
				send.setOnClickListener(new View.OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						
						final String text=box.getText().toString();
						if(text.isEmpty())
						{/*
							Toast toast = Toast.makeText(activity, "Please enter additional info to join this group", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();*/
							//Toast.makeText(activity, "Please enter the additional info", Toast.LENGTH_LONG).show();
							Utility.showToastMessage(activity, activity.getResources().getString(R.string.additional_info_empty));
						}
						else
						{
							mDialog.dismiss();
							String str=nearGroupList.get(position).get(Constants.INFO_REQUIRED).toString()+" - "+text;
							sendInvitation(str,position);	
						}
					}
				});
				
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v){
						IBinder token = box.getWindowToken();
						( ( InputMethodManager ) activity.getSystemService( activity.INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
						mDialog.dismiss();
					}
				});
				
				mDialog.show();
			}
			else if(nearGroupList.get(position).getBoolean(Constants.SECRET_STATUS))
			{
				mDialog = new Dialog(activity);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);     
				mDialog.setContentView(R.layout.ask_additional_info);
				mDialog.setCancelable(true);
				mDialog.setCanceledOnTouchOutside(true);
				WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
				windowManager.gravity = Gravity.CENTER;
				TextView text=(TextView) mDialog.findViewById(R.id.additional_info_text);
				final EditText box=(EditText) mDialog.findViewById(R.id.additional_info_box);
				text.setText("Enter Invitation Code");

				Button send=(Button) mDialog.findViewById(R.id.send);
				Button cancel=(Button) mDialog.findViewById(R.id.cancel);
				send.setOnClickListener(new View.OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						
						final String text=box.getText().toString();
						if(text.isEmpty())
						{
							Utility.showToastMessage(activity, activity.getResources().getString(R.string.ask_secret_code));	
							//Toast toast = Toast.makeText(activity, "Please enter invitation code", Toast.LENGTH_LONG);
							//toast.setGravity(Gravity.CENTER, 0, 0);
							//toast.show();
							//Toast.makeText(activity, "Please enter invitation code", Toast.LENGTH_LONG).show();
						}
						else if(text.equals(nearGroupList.get(position).get(Constants.SECRET_CODE).toString()))
						{
							mDialog.dismiss();
							insertValues(position);
						}
						else
						{
							box.setText("");
							Utility.showToastMessage(activity, activity.getResources().getString(R.string.invalid_secret_code));
							//Toast toast = Toast.makeText(activity, "Invalid invitation code", Toast.LENGTH_LONG);
							//toast.setGravity(Gravity.CENTER, 0, 0);
							//toast.show();
							//Toast.makeText(activity, "Invalid invitation code", Toast.LENGTH_LONG).show();
						}
					}
				});
				
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						IBinder token = box.getWindowToken();
						( ( InputMethodManager ) activity.getSystemService( activity.INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
						mDialog.dismiss();
					}
				});
				
				mDialog.show();
			}
			else
			{
				sendInvitation("",position);	
			}
		}
	 else
	 {
		 insertValues(position);
	 }
	}
	
	@SuppressLint("NewApi")
	private void sendInvitation(final String additionalInfo,final int position)
	{
		progressBar.setVisibility(View.VISIBLE);
		ArrayList<String> list1=new ArrayList<String>();
		ParseGeoPoint point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		//userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		//userObject.put(Constants.MEMBER_IMAGE, mProfileImgFile);
		userObject.put(Constants.GROUP_ID, nearGroupList.get(position).getObjectId());
		userObject.put(Constants.POST_TEXT, additionalInfo);
		userObject.put(Constants.POST_TYPE, "Invitation");
		//userObject.put(Constants.UP_VOTE_COUNT, 0);
		//userObject.put(Constants.DOWN_VOTE_COUNT, 0);
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.POST_POINT, 0);
		userObject.put(Constants.LIKE_ARRAY, list1);
		userObject.put(Constants.DIS_LIKE_ARRAY, list1);
		userObject.put(Constants.FLAG_ARRAY, list1);
		//userObject.put(Constants.POST_IMAGE, mProfileImgFile);
		userObject.put(Constants.IMAGE_CAPTION, "");
		userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();
		//userObject.pinInBackground(nearGroupList.get(position).getObjectId());
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							List<String> list2=object.getList(Constants.GROUP_INVITATION);
							list2.add(nearGroupList.get(position).getObjectId());
							object.put(Constants.GROUP_INVITATION, list2);
							PreferenceSettings.setGroupInvitationList(list2);
							object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
							object.saveInBackground(new SaveCallback() {
						          public void done(ParseException e) {
						                 if (e == null) {
						                	 progressBar.setVisibility(View.GONE);
		            							//Toast.makeText(activity, "Join request sent", Toast.LENGTH_LONG).show();
		            							activity.getDatafromParse();
		            							flag=false;
						                 }
						          }});
						
						}
						else
							progressBar.setVisibility(View.GONE);
					}
					else
						progressBar.setVisibility(View.GONE);
			}
		});
		
		
	}
	
	private void insertValues(final int position)
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseGeoPoint point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		final String latestPost=PreferenceSettings.getUserName()+" - "+activity.getResources().getString(R.string.new_member);
		ArrayList<String> list1=new ArrayList<String>();
		System.out.println("before insert feed table ::: "+Utility.getCurrentDate());
		ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		//userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		//userObject.put(Constants.MEMBER_IMAGE, mProfileImgFile);
		userObject.put(Constants.GROUP_ID, nearGroupList.get(position).getObjectId());
		userObject.put(Constants.POST_TEXT, latestPost);
		userObject.put(Constants.POST_TYPE, "Member");
		//userObject.put(Constants.UP_VOTE_COUNT, 0);
		//userObject.put(Constants.DOWN_VOTE_COUNT, 0);
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.POST_POINT, 0);
		userObject.put(Constants.LIKE_ARRAY, list1);
		userObject.put(Constants.DIS_LIKE_ARRAY, list1);
		userObject.put(Constants.FLAG_ARRAY, list1);
		//userObject.put(Constants.POST_IMAGE, mProfileImgFile);
		userObject.put(Constants.IMAGE_CAPTION, "");
		userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();
		userObject.pinInBackground(nearGroupList.get(position).getObjectId());
		System.out.println("before insert member table ::: "+Utility.getCurrentDate());
		ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
 		memberObject.put(Constants.MEMBER_NO,  PreferenceSettings.getMobileNo());
 		memberObject.put(Constants.GROUP_ID, nearGroupList.get(position).getObjectId());
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
	   
 		System.out.println("before call group table ::: "+Utility.getCurrentDate());
		
	    
	    ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
 		query.whereEqualTo(Constants.OBJECT_ID, nearGroupList.get(position).getObjectId());
 		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
  						{
							System.out.println("after get group table ::: "+Utility.getCurrentDate());
							object.increment(Constants.MEMBER_COUNT, 1);
							
 							memberCount=object.getInt(Constants.MEMBER_COUNT);
 							ArrayList<String> memberNoList=(ArrayList<String>) object.get(Constants.GROUP_MEMBERS);
 							memberNoList.add(PreferenceSettings.getMobileNo());
 							object.put(Constants.GROUP_MEMBERS, memberNoList);
 							System.out.println("before call user table ::: "+Utility.getCurrentDate());
 							object.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
 							Utility.setGroupObject(object);
 							ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.USER_TABLE);
 					 		query1.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
 					 		query1.getFirstInBackground(new GetCallback<ParseObject>() {
 								public void done(ParseObject object, ParseException e) {
 										if (e == null) 
 										{
 											if(object!=null)
 					  						{
 												System.out.println("after get user table ::: "+Utility.getCurrentDate());
 					 								List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
 					 									myGroupList.add(nearGroupList.get(position).getObjectId());
 					 									object.put(Constants.MY_GROUP_ARRAY, myGroupList);
 					 								if(PreferenceSettings.isJoinFirst())
 					 								{
 					 									userPoints=1000;
 					 									//list.get(0).increment(Constants.BADGE_POINT, 1000);
 					 									PreferenceSettings.setJoinStatus(false);
 					 								}
 					 								else
 					 								{
 					 									userPoints=100;
 					 									//list.get(0).increment(Constants.BADGE_POINT, 100);
 					 								}
 					 								
 					 								if(memberCount==10)
 					 									userPoints=userPoints+1000;
 					 								else if(memberCount==50)
 					 									userPoints=userPoints+2000;
 					 								object.increment(Constants.BADGE_POINT, userPoints);
 					 								object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
 					 								System.out.println("before update user table ::: "+Utility.getCurrentDate());
 					 								object.saveInBackground();
 					 								
 					 								 final ParseObject groupObject=object;
 								                	 
 								                	 flag1=false;
 						     							flag=false;
 						     							activity.getDatafromParse();
 						     							MyGroupListActivity.flag1=true;
 						     							//Toast.makeText(activity, "You joined successfully", Toast.LENGTH_LONG).show();
 						     							progressBar.setVisibility(View.GONE);
 						     							//Utility.setGroupObject(groupObject);
 						     							//activity.startActivity(new Intent(activity,TabGroupPostActivity.class));
 						     							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
 						     							activity.startActivity(new Intent(activity,TopicListActivity.class));
 					 								/*new SaveCallback() {
 					 							          public void done(ParseException e) {
 					 							                 if (e == null) {
 					 							                	System.out.println("after update user table ::: "+Utility.getCurrentDate());
 					 							                	flag1=false;
 							             							flag=false;
 							             							activity.getDatafromParse();
 							             							MyGroupListActivity.flag1=true;
 							             							//Toast.makeText(activity, "You joined successfully", Toast.LENGTH_LONG).show();
 							             							progressBar.setVisibility(View.GONE);
 							             							Utility.setGroupObject(groupObject);
 							             							activity.startActivity(new Intent(activity,TabGroupPostActivity.class));
 							             							activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
 							             							//activity.finish();
 					 							                 }
 					 							          }});*/
 					 							}
 					     					}
 					     			}
 					     		});
 							
 							object.saveInBackground(new SaveCallback() {
						          public void done(ParseException e) {
					                 if (e == null) {
					                	/* final ParseObject groupObject=object;
					                	 
					                	 flag1=false;
			     							flag=false;
			     							activity.getDatafromParse();
			     							MyGroupListActivity.flag1=true;
			     							//Toast.makeText(activity, "You joined successfully", Toast.LENGTH_LONG).show();
			     							progressBar.setVisibility(View.GONE);
			     							Utility.setGroupObject(groupObject);
			     							activity.startActivity(new Intent(activity,TabGroupPostActivity.class));
			     							activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					                	 
					                	 System.out.println("after update group table ::: "+Utility.getCurrentDate());
					                	 System.out.println("before call user table ::: "+Utility.getCurrentDate());*/
					                	
					                 }
						          }});
 							
 							
 						}
 					}
 			}
 		});
 		
 		
	
	}

	protected class ViewHolder
	{
		protected TextView groupName,totalMembers,groupType;
		//ImageView groupType;//,openPublicType;//groupDes,invitationStatus;
		//protected ImageView groupTypeImage;
		protected ParseImageView groupImage;
		//protected RelativeLayout rootLayout,joinLayout;
		//Button joinBtn;
		FrameLayout joinFrame;
	}
	
	public static void setFlag(boolean value,int position)
	{
		flag=value;
		joinPosition=position;
	}
	
	// Filter Class
	public void filter(String cs) {
		//nearGroupList.get(position).get(Constants.GROUP_NAME).toString()
		cs =  cs.toLowerCase(Locale.getDefault());
		nearGroupList.clear();
		if (cs.length() == 0) {
			nearGroupList.addAll(searchList);
		}
		else
		{
			for (int i=0;i<searchList.size();i++)
			{
				if (searchList.get(i).get(Constants.GROUP_NAME).toString().toLowerCase(Locale.getDefault()).contains(cs))
				{
					nearGroupList.add(searchList.get(i));
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public void setList(List<ParseObject> nearGroupList1)
	{
		this.nearGroupList=nearGroupList1;
		this.searchList=new ArrayList<ParseObject>();
		this.searchList.addAll(nearGroupList);
	}

}
