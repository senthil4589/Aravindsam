package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.android.volley.toolbox.ImageLoader;
import com.group.nearme.CommentActivity;
import com.group.nearme.GroupPostListActivity;
import com.group.nearme.MemberProfileActivity;
import com.group.nearme.MyGroupListActivity;
import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.R;
import com.group.nearme.adapter.PublicPostAdapter.ViewHolder;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.ResizableImageView;
import com.group.nearme.util.Utility;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class PostQueryAdapter extends ParseQueryAdapter {
	ViewHolder viewHolder;
	LayoutInflater inflater;
	Context activity;
	private ProgressBar progressBar;
//	public Dialog mDialog;
	//AlertDialog mAlertDialog;
//	String flagValue="";
//	boolean likeFlag,unLikeFlag;
//	private String mGroupId="",mGroupName="",mGroupImage="";
	ImageLoader imageLoader;
	ParseGeoPoint point;
	int memberCount,userPoints;
	String groupType="";
	ArrayList<List<String>> tempLikeList,tempDisLikeList;
	
	
	
    @SuppressWarnings("unchecked")
	public PostQueryAdapter(Context context, PostQueryAdapter.QueryFactory<ParseObject> queryFactory) {
    	
        super(context, queryFactory, 0);
    }

    @Override
    public View getItemView(final ParseObject currentObject, View view, ViewGroup parent) {
    	activity=getContext();
    	if(currentObject.get(Constants.POST_TYPE).toString().equals("Member"))
		{
			view = View.inflate(getContext(), R.layout.feed_info, null);
			viewHolder = new ViewHolder();
			viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
			viewHolder.postText=(TextView) view.findViewById(R.id.post_text);
			viewHolder.userName=(TextView) view.findViewById(R.id.user_name);
			viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
			viewHolder.updatedTime.setTextSize(12);
			
			
		//viewHolder.userName.setText(list.get(position).get(Constants.MEMBER_NAME)+" joined this group");
		//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
		
		//ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
		
		if( currentObject!=null ){
		viewHolder.userImage.setParseFile(currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
		viewHolder.userImage.loadInBackground();
		//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
		viewHolder.userName.setText(currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME)+" - "+activity.getResources().getString(R.string.new_member));
		}
		
		viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utility.setParseFile(currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
				activity.startActivity(new Intent(activity,MemberProfileActivity.class)
				.putExtra("isFromFeed", true)
				.putExtra(Constants.USER_NAME, currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
				.putExtra(Constants.MOBILE_NO, currentObject.get(Constants.MOBILE_NO).toString())
				.putExtra(Constants.PROFILE_PICTURE, currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
				//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		//viewHolder.postText.setText(list.get(position).get(Constants.POST_TEXT).toString());
		final java.util.Date f=currentObject.getCreatedAt();
		viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
			
		}
		else if(currentObject.get(Constants.POST_TYPE).toString().equals("Invitation"))
		{
			view =  View.inflate(getContext(), R.layout.join_invitation, null);
					//inflater.inflate(R.layout.join_invitation, null);
			viewHolder = new ViewHolder();
			viewHolder.joinRequest=(TextView) view.findViewById(R.id.join_invitation_txt);
			viewHolder.name=(TextView) view.findViewById(R.id.name);
			viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
			viewHolder.time=(TextView) view.findViewById(R.id.updated_time);
			viewHolder.line=(View) view.findViewById(R.id.line1);
			viewHolder.additional_info=(TextView) view.findViewById(R.id.additional_info);
			final ImageView acceptBtn=(ImageView) view.findViewById(R.id.accept);
			final ImageView rejectBtn=(ImageView) view.findViewById(R.id.reject);
			
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			//viewHolder.name.setText(list.get(position).get(Constants.MEMBER_NAME).toString());
			
			//ParseObject userObject=ParseObject.create(Constants.USER_TABLE); 
			ParseObject userObject = currentObject.getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.userImage.setParseFile(currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.userImage.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.name.setText(currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
			
			final java.util.Date f=currentObject.getCreatedAt();
			viewHolder.time.setText(Utility.getTimeAgo(f.getTime()));
			viewHolder.time.setTextSize(12);
			if(currentObject.get(Constants.POST_TEXT).toString().equals("No Information Available") || currentObject.get(Constants.POST_TEXT).toString().isEmpty())
			{
				viewHolder.line.setVisibility(View.GONE);
				viewHolder.additional_info.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.line.setVisibility(View.VISIBLE);
				viewHolder.additional_info.setVisibility(View.VISIBLE);
				viewHolder.additional_info.setText(currentObject.get(Constants.POST_TEXT).toString());
			}
			acceptBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utility.checkInternetConnectivity(activity))
					{
						acceptBtn.setEnabled(false);
						rejectBtn.setEnabled(false);
						acceptJoinInvitation();
					}
					//else
						//Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				}

				private void acceptJoinInvitation() {
					progressBar.setVisibility(View.VISIBLE);
					
					
					final String latestPost=currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME)+" - "+activity.getResources().getString(R.string.new_member);
					 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
	              		query.whereEqualTo(Constants.OBJECT_ID, currentObject.get(Constants.GROUP_ID).toString());
	              		query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object, ParseException e) {
									if (object!=null) 
									{
										List<String> adminList=object.getList(Constants.ADMIN_ARRAY);
										if(!object.getBoolean(Constants.MEMBERSHIP_APPROVAL) || adminList.contains(PreferenceSettings.getMobileNo()))
										{
											
						             		
											ParseObject object1 = new ParseObject(Constants.INVITATION_ACTIVITY_TABLE);
											object1.put(Constants.BY_USER, PreferenceSettings.getMobileNo());
											object1.put(Constants.TO_USER, currentObject.get(Constants.MOBILE_NO).toString());
											object1.put(Constants.ACTIVITY_LOCATION, point);
											object1.put(Constants.GROUP_ID, currentObject.get(Constants.GROUP_ID).toString());
											object1.put(Constants.INVITATION_ACCEPT, true);
											object1.put(Constants.INVITATION_TYPE, "Request");
											object1.saveInBackground();
											
											object.increment(Constants.MEMBER_COUNT, 1);
											memberCount=object.getInt(Constants.MEMBER_COUNT);
											object.put(Constants.LATEST_POST,latestPost);
											ArrayList<String> memberNoList=(ArrayList<String>) object.get(Constants.GROUP_MEMBERS);
				 							memberNoList.add(currentObject.get(Constants.MOBILE_NO).toString());
				 							object.put(Constants.GROUP_MEMBERS, memberNoList);
											object.saveInBackground(new SaveCallback() {
         							        public void done(ParseException e) {
         							          if (e == null) {
         							        	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
         										query.whereEqualTo(Constants.MOBILE_NO, currentObject.get(Constants.MOBILE_NO).toString());
         										query.getFirstInBackground(new GetCallback<ParseObject>() {
         											public void done(ParseObject object, ParseException e) {
         													if (e == null) 
         													{
         														if(object!=null)
         														{
         															
         															ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
         										             		memberObject.put(Constants.MEMBER_NO,  currentObject.get(Constants.MOBILE_NO).toString());
         										             		memberObject.put(Constants.GROUP_ID, currentObject.get(Constants.GROUP_ID).toString());
         										             		memberObject.put(Constants.ADDITIONAL_INFO_PROVIDED, currentObject.get(Constants.POST_TEXT).toString());
         										             		memberObject.put(Constants.JOIN_DATE, Utility.getCurrentDate());
         										             		memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
         										             		memberObject.put(Constants.EXIT_GROUP, false);
         										             		memberObject.put(Constants.EXITED_BY, "");
         										             		memberObject.put(Constants.MEMBER_STATUS, "Active");
         										             		//memberObject.put(Constants.MEMBER_NAME, currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
         										             		//memberObject.put(Constants.MEMBER_IMAGE, currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
         										             		memberObject.put(Constants.GROUP_ADMIN, false);
         										             		memberObject.put(Constants.UNREAD_MESSAGES, 0);
         										             		memberObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, object.getObjectId()));
         										             		memberObject.saveInBackground();
         															List<String> idList=object.getList(Constants.GROUP_INVITATION);
         															idList.remove(currentObject.get(Constants.GROUP_ID).toString());
         															object.put(Constants.GROUP_INVITATION, idList);
         															List<String> groupArray=object.getList(Constants.MY_GROUP_ARRAY);
         															groupArray.add(currentObject.get(Constants.GROUP_ID).toString());
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
         															PreferenceSettings.setGroupInvitationList(idList);
         															object.saveInBackground(new SaveCallback() {
         														          public void done(ParseException e) {
         														                 if (e == null) {
														            					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
														            					query.whereEqualTo(Constants.OBJECT_ID, currentObject.getObjectId());
														            					query.getFirstInBackground(new GetCallback<ParseObject>() {
														            					public void done(ParseObject object, ParseException e) {
														            					if(object!=null)
														            					{
														            						object.put(Constants.POST_TEXT, latestPost);
														            						object.put(Constants.POST_TYPE, "Member");
														            						object.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
														            						object.pinInBackground(currentObject.get(Constants.GROUP_ID).toString());
														            						object.saveInBackground(new SaveCallback() {
												             							        public void done(ParseException e) {
													             							          if (e == null) {
													             							        	/* List<ParseObject> invitationList=Utility.getInvitationList();
													             							        		for(int i=0;i<invitationList.size();i++)
													             							        		{
													             							        			if(invitationList.get(i).getObjectId().equals(list.get(position).getObjectId()))
													             							        			{
													             							        				invitationList.remove(i);
													             							        				Utility.setInvitationList(invitationList);
													             							        				break;
													             							        			}
													             							        		}*/
													             							        	// removeFromInvitation(position);
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
					if(Utility.checkInternetConnectivity(activity))
					{
						acceptBtn.setEnabled(false);
						rejectBtn.setEnabled(false);
						rejectJoinInvitation();
					}
					//else
					//	Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				}

				private void rejectJoinInvitation() {
					progressBar.setVisibility(View.VISIBLE);
					
					ParseObject object1 = new ParseObject(Constants.INVITATION_ACTIVITY_TABLE);
					object1.put(Constants.BY_USER, PreferenceSettings.getMobileNo());
					object1.put(Constants.TO_USER, currentObject.get(Constants.MOBILE_NO).toString());
					object1.put(Constants.ACTIVITY_LOCATION, point);
					object1.put(Constants.GROUP_ID, currentObject.get(Constants.GROUP_ID).toString());
					object1.put(Constants.INVITATION_ACCEPT, false);
					object1.put(Constants.INVITATION_TYPE, "Request");
					object1.saveInBackground();
					 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
	              		query.whereEqualTo(Constants.OBJECT_ID, currentObject.get(Constants.GROUP_ID).toString());
	              		query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object, ParseException e) {
									if (object!=null) 
									{
										List<String> adminList=object.getList(Constants.ADMIN_ARRAY);
										if(!object.getBoolean(Constants.MEMBERSHIP_APPROVAL) || adminList.contains(PreferenceSettings.getMobileNo()))
										{
											object.increment(Constants.NEWS_FEED_COUNT, -1);
	             							//list.get(0).put(Constants.LATEST_POST,request);
											object.saveInBackground(new SaveCallback() {
          							        public void done(ParseException e) {
          							          if (e == null) {
          							        	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
          										query.whereEqualTo(Constants.MOBILE_NO, currentObject.get(Constants.MOBILE_NO));
          										query.getFirstInBackground(new GetCallback<ParseObject>() {
          											public void done(ParseObject object, ParseException e) {
          													if (e == null) 
          													{
          														if(object!=null)
          														{
          															List<String> idList=object.getList(Constants.GROUP_INVITATION);
          															idList.remove(currentObject.get(Constants.GROUP_ID).toString());
          															object.put(Constants.GROUP_INVITATION, idList);
          															PreferenceSettings.setGroupInvitationList(idList);
          															object.saveInBackground(new SaveCallback() {
          														          public void done(ParseException e) {
          														                 if (e == null) {
														            					ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
														            					query.whereEqualTo(Constants.OBJECT_ID, currentObject.getObjectId());
														            					query.getFirstInBackground(new GetCallback<ParseObject>() {
														            					public void done(ParseObject object, ParseException e) {
														            					if(object!=null)
														            					{
														            						object.unpinInBackground(currentObject.get(Constants.GROUP_ID).toString());
														            						object.deleteInBackground(new DeleteCallback() {
												             							        public void done(ParseException e) {
													             							          if (e == null) {
													             							        	 NearByGroupListActivity.flag2=true;
													             										 MyGroupListActivity.flag1=true;
													             							        		progressBar.setVisibility(View.GONE);
													             							        		//activity.setAdapter();
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
		}
		else
		{
		//if (view == null) {
			view = View.inflate(getContext(), R.layout.public_post_list_item, null);
					//inflater.inflate(R.layout.public_post_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.postImage=(ResizableImageView) view.findViewById(R.id.post_image);
			viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
			final ImageView upVote=(ImageView) view.findViewById(R.id.up_vote);
			final ImageView downVote=(ImageView) view.findViewById(R.id.down_vote);
			//viewHolder.share=(ImageView) view.findViewById(R.id.share);
			viewHolder.flag=(ImageView) view.findViewById(R.id.flag);
			viewHolder.delete=(ImageView) view.findViewById(R.id.delete);
			viewHolder.postText=(TextView) view.findViewById(R.id.post_text);
			viewHolder.imageCaption=(TextView) view.findViewById(R.id.image_caption);
			viewHolder.userName=(TextView) view.findViewById(R.id.user_name);
			viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
			viewHolder.points=(TextView) view.findViewById(R.id.points);
			viewHolder.replies=(TextView) view.findViewById(R.id.replies);
			viewHolder.comment_count=(TextView) view.findViewById(R.id.comments_count);
			viewHolder.upVoteFrame=(FrameLayout) view.findViewById(R.id.upvote_frame);
			viewHolder.downVoteFrame=(FrameLayout) view.findViewById(R.id.downvote_frame);
			View line=(View) view.findViewById(R.id.line1);
			
			LayoutParams param=new LayoutParams(LayoutParams.MATCH_PARENT,currentObject.getInt(Constants.IMAGE_HEIGHT));
			param.leftMargin=10;
			param.rightMargin=10;
			param.addRule(RelativeLayout.BELOW,viewHolder.imageCaption.getId());
			viewHolder.postImage.setLayoutParams(param);
			viewHolder.postImage.setAdjustViewBounds(true);
			
			//ParseObject userObject=ParseObject.create(Constants.USER_TABLE); 
			ParseObject userObject = currentObject.getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.userImage.setParseFile(currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.userImage.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.userName.setText(currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
			viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Utility.setParseFile(currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
					activity.startActivity(new Intent(activity,MemberProfileActivity.class)
					.putExtra("isFromFeed", true)
					.putExtra(Constants.USER_NAME, currentObject.getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
					.putExtra(Constants.MOBILE_NO, currentObject.get(Constants.MOBILE_NO).toString())
					.putExtra(Constants.PROFILE_PICTURE, currentObject.getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			});
			
			
			
			if(groupType.equals("Public"))
			{
				viewHolder.userName.setVisibility(View.GONE);
				viewHolder.userImage.setVisibility(View.GONE);
				//line.setVisibility(View.GONE);
				line.setPadding(0, 10, 0, 0);
				viewHolder.updatedTime.setPadding(8, 15, 5, 5);
				viewHolder.updatedTime.setTextSize(12);
				viewHolder.postText.setAutoLinkMask(0);
			}
			else
			{
				viewHolder.userName.setVisibility(View.VISIBLE);
				viewHolder.userImage.setVisibility(View.VISIBLE);
				//line.setVisibility(View.VISIBLE);
				line.setPadding(0, 0, 0, 0);
				viewHolder.updatedTime.setPadding(0, 0, 0, 0);
				viewHolder.updatedTime.setTextSize(12);
				viewHolder.postText.setAutoLinkMask(Linkify.WEB_URLS);
			}
			
		if(currentObject.get(Constants.MOBILE_NO).toString().equals(PreferenceSettings.getMobileNo()))
		{
			viewHolder.delete.setVisibility(View.VISIBLE);
			viewHolder.flag.setVisibility(View.GONE);
			
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utility.checkInternetConnectivity(activity))
					{	
					//showDeleteAlertDialog(position);
				}
				//else
					//Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				}
			});
		}
		else
		{
			viewHolder.flag.setVisibility(View.VISIBLE);
			viewHolder.delete.setVisibility(View.GONE);
		}
		
		if(currentObject.get(Constants.POST_TEXT).toString().equals(""))
		{
			if(currentObject.get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				viewHolder.imageCaption.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.imageCaption.setVisibility(View.VISIBLE);
				viewHolder.imageCaption.setText(currentObject.get(Constants.IMAGE_CAPTION).toString());
			}
			viewHolder.postImage.setVisibility(View.VISIBLE);
			viewHolder.postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) currentObject.get(Constants.POST_IMAGE);
			//viewHolder.postImage.setParseFile(file);
			//viewHolder.postImage.loadInBackground();
			Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(viewHolder.postImage);
			//viewHolder.postImage.setParseFile(file);
			//viewHolder.postImage.loadInBackground();
			//viewHolder.postImage.setImageUrl(file.getUrl(), imageLoader);
			/*imageLoader.get(file.getUrl(), ImageLoader.getImageListener(
					viewHolder.postImage, R.drawable.group_image, R.drawable.group_image));*/
		}
		else
		{
			viewHolder.postImage.setVisibility(View.GONE);
			viewHolder.imageCaption.setVisibility(View.GONE);
			viewHolder.postText.setVisibility(View.VISIBLE);
			viewHolder.postText.setText(currentObject.get(Constants.POST_TEXT).toString());
			
		}
		
		final java.util.Date f=currentObject.getCreatedAt();
		if(f!=null)
			viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
		System.out.println("point :: "+currentObject.getInt(Constants.POST_POINT));
		viewHolder.points.setText(currentObject.getInt(Constants.POST_POINT)+" Points");
		viewHolder.comment_count.setText(currentObject.getInt(Constants.COMMENT_COUNT)+" Replies");
		
		if(currentObject.getList(Constants.LIKE_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			System.out.println("inside like");
			downVote.setEnabled(false);
			upVote.setEnabled(true);
			upVote.setImageResource(R.drawable.up2);
			downVote.setImageResource(R.drawable.down1);
		}
		else if(currentObject.getList(Constants.DIS_LIKE_ARRAY).contains(PreferenceSettings.getMobileNo()))
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
		/*
		upVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(Utility.checkInternetConnectivity(activity))
				{	
             	//downVote.setClickable(false);
				if(tempLikeList.get(position).contains(PreferenceSettings.getMobileNo()))
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					downVote.setEnabled(true);
					upVote.setEnabled(true);
					
					System.out.println("inside if click like ");
					//progressBar.setVisibility(View.VISIBLE);
					//List<String> l=list.get(position).getList(Constants.LIKE_ARRAY);
					tempLikeList.get(position).remove(PreferenceSettings.getMobileNo());
					//list.get(position).put(Constants.LIKE_ARRAY, l);
					//list.get(position).increment(Constants.POST_POINT, -50);
					//int c=list.get(position).getInt(Constants.POST_POINT)-50;
					//viewHolder.points.setText(String.valueOf(c)+" Points");
					//notifyDataSetChanged();
					list.get(position).pinInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							//progressBar.setVisibility(View.GONE);
							notifyDataSetChanged();
						}
					});
					updateLike(false,position);
				}
				else
				{
					upVote.setImageResource(R.drawable.up2);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(false);
					System.out.println("inside else click like ");
					//List<String> l=list.get(position).getList(Constants.LIKE_ARRAY);
					tempLikeList.get(position).add(PreferenceSettings.getMobileNo());
					//list.get(position).put(Constants.LIKE_ARRAY, l);
					//int c=list.get(position).getInt(Constants.POST_POINT)+50;
				//	viewHolder.points.setText(String.valueOf(c)+" Points");
					//list.get(position).increment(Constants.POST_POINT, 50);
					//notifyDataSetChanged();
					list.get(position).pinInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							//progressBar.setVisibility(View.GONE);
							notifyDataSetChanged();
						}
					});
					updateLike(true,position);
				}
				}
				else
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				
			}
		});
		if(list.get(position).getList(Constants.DIS_LIKE_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			System.out.println("inside dislike if");
			viewHolder.upVote.setClickable(false);
			viewHolder.downVote.setClickable(true);
			viewHolder.downVote.setImageResource(R.drawable.down2);
		}
		else
		{
			System.out.println("inside dislike else");
			viewHolder.upVote.setClickable(true);
			viewHolder.downVote.setClickable(true);
			//viewHolder.downVote.setEnabled(true);
			viewHolder.downVote.setImageResource(R.drawable.down1);
		}
		
		downVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//upVote.setClickable(false);
             //	downVote.setClickable(false);
				//System.out.println("inside dislike ");
				//progressBar.setVisibility(View.VISIBLE);
				if(Utility.checkInternetConnectivity(activity))
				{	
				if(tempDisLikeList.get(position).contains(PreferenceSettings.getMobileNo()))
				{
					System.out.println("inside if click dislike ");
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(true);
					//System.out.println("inside if click like ");
					//progressBar.setVisibility(View.VISIBLE);
					//List<String> l=list.get(position).getList(Constants.DIS_LIKE_ARRAY);
					tempDisLikeList.get(position).remove(PreferenceSettings.getMobileNo());
					//list.get(position).put(Constants.DIS_LIKE_ARRAY, l);
					//list.get(position).increment(Constants.POST_POINT, 50);
					//int c=list.get(position).getInt(Constants.POST_POINT)+50;
					//viewHolder.points.setText(String.valueOf(c)+" Points");
					
					
				
					System.out.println("inside dislike if");
					//viewHolder.upVote.setEnabled(true);
					//viewHolder.downVote.setImageResource(R.drawable.down1);
					//List<String> l=list.get(position).getList(Constants.DIS_LIKE_ARRAY);
					//l.remove(PreferenceSettings.getMobileNo());
					//list.get(position).put(Constants.DIS_LIKE_ARRAY, l);
					
					System.out.println("before down vote point :: "+list.get(position).get(Constants.POST_POINT));
				//	list.get(position).increment(Constants.POST_POINT, 50);
					
					System.out.println("before down vote point :: "+list.get(position).get(Constants.POST_POINT));
					
					list.get(position).saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							progressBar.setVisibility(View.GONE);
							notifyDataSetChanged();
						}
					});
				//	System.out.println("inside dislike if");
					updateDislike(false,position);
					
				}
				else
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down2);
					downVote.setEnabled(true);
					upVote.setEnabled(false);
				
					//System.out.println("inside if click like ");
					//progressBar.setVisibility(View.VISIBLE);
					//List<String> l=list.get(position).getList(Constants.DIS_LIKE_ARRAY);
					tempDisLikeList.get(position).add(PreferenceSettings.getMobileNo());
					//list.get(position).put(Constants.DIS_LIKE_ARRAY, l);
					//list.get(position).increment(Constants.POST_POINT, -50);
					//int c=list.get(position).getInt(Constants.POST_POINT)-50;
					//viewHolder.points.setText(String.valueOf(c)+" Points");
					
					System.out.println("inside else click dislike ");
					
					System.out.println("inside dislike else");
					//List<String> l=list.get(position).getList(Constants.DIS_LIKE_ARRAY);
					//l.add(PreferenceSettings.getMobileNo());
					//list.get(position).put(Constants.DIS_LIKE_ARRAY, l);
					System.out.println("before down vote point :: "+list.get(position).get(Constants.POST_POINT));
					//list.get(position).increment(Constants.POST_POINT, -50);
					System.out.println("after down vote point :: "+list.get(position).get(Constants.POST_POINT));
					//notifyDataSetChanged();
					list.get(position).saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							progressBar.setVisibility(View.GONE);
							notifyDataSetChanged();
						}
					});
					updateDislike(true,position);
				}
			}
			else
				Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
			}
		});
		
		
		viewHolder.flag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Utility.checkInternetConnectivity(activity))
				{	
				if(currentObject.getList(Constants.FLAG_ARRAY).contains(PreferenceSettings.getMobileNo()))
				{
					//viewHolder.flag.setEnabled(false);
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.flag_already_done));
				}
				else
				{
					showFlagDialog(position);
					//viewHolder.flag.setEnabled(false);
				}
			}
			else
				Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				
			}
		});
		
		viewHolder.postText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				callCommentActivity(position);
			}
		});
		
		viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				callCommentActivity(position);
				//activity.startActivity(new Intent(activity,ImageFullViewActivity.class).putExtra(Constants.POST_IMAGE, list.get(position).getParseFile(Constants.POST_IMAGE).getUrl()));
			}
		});
		
		
		viewHolder.replies.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callCommentActivity(position);
			}
		});
		*/
		
		}//end of else
    	super.getItemView(currentObject, view, parent);
		return view;
	}
	
	/*public  void showDeleteAlertDialog(final int position){
		mAlertDialog=new AlertDialog.Builder(activity)
		.setMessage("Are you sure you want to delete this post?")
		.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//progressBar.setVisibility(View.VISIBLE);
			
				
				Utility.showToastMessage(activity, activity.getResources().getString(R.string.post_delete));
				final ParseObject object=list.get(position);
				
				list.remove(position);
				notifyDataSetChanged();
				
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, object.getObjectId());
				query.whereEqualTo(Constants.GROUP_ID, object.get(Constants.GROUP_ID));
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
			 							                	query.whereEqualTo(Constants.FEED_ID, object.getObjectId());
			 							                	query.findInBackground(new FindCallback<ParseObject>() {
			 							                		public void done(List<ParseObject> l2, ParseException e) {
			 							   							if (e == null) 
			 							   							{
			 							   								System.out
																				.println("inside delete activity ::"+l2.size());
			 							   								if(l2.size() > 0)
			 							   								{
			 							   									ParseObject.deleteAllInBackground(l2, new DeleteCallback() {
			 							   										public void done(ParseException e) {
			 							   											if (e == null) {
			 							   												progressBar.setVisibility(View.GONE);
			 							   												//Toast.makeText(activity, "Post deleted successfully", Toast.LENGTH_SHORT).show();
			 							   												//activity.setAdapter();
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
	
	
	private void callCommentActivity(int position)
	{
		System.out.println("inside post adapter ");
		String userImageUrl=null;
		for(int i=0;i<userList.size();i++)
		{
			if(userList.get(i).get(Constants.MOBILE_NO).toString().equals(list.get(position).get(Constants.MOBILE_NO).toString()))
			{
				userImageUrl=userList.get(i).getParseFile(Constants.PROFILE_PICTURE).getUrl();
				//Picasso.with(activity).load(userList.get(i).getParseFile(Constants.PROFILE_PICTURE).getUrl()).into(viewHolder.userImage);
				break;
			}
		}
		Utility.setList(list);				
		final java.util.Date f1=list.get(position).getCreatedAt();
		Intent intent=new Intent(activity,CommentActivity.class);
		intent.putExtra(Constants.GROUP_NAME, mGroupName);
		intent.putExtra(Constants.GROUP_PICTURE, mGroupImage);
		intent.putExtra(Constants.GROUP_ID, mGroupId);
		intent.putExtra(Constants.PROFILE_PICTURE, list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl());
		intent.putExtra("updatedtime", Utility.getTimeAgo(f1.getTime()));
		intent.putExtra("position", position);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
		
	}
	
	private void showFlagDialog(final int position)
	{
		mDialog = new Dialog(activity);
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
				 		Utility.showToastMessage(activity, activity.getResources().getString(R.string.flag_empty));
				 	else
				 	{
				 		mDialog.dismiss();
				 		updateFlagInTable(position);
				 	}
				 }
				 else
					 Utility.showToastMessage(activity, activity.getResources().getString(R.string.flag_empty));
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
*/
	protected class ViewHolder
	{
		protected TextView userName,postText,updatedTime,points,replies,joinRequest,imageCaption,comment_count;
		protected TextView name,time,additional_info;
		protected ImageView flag,delete;
		protected ParseImageView userImage;
		//final ImageView acceptBtn,rejectBtn;
		protected ResizableImageView postImage;
		protected View line;
		protected FrameLayout upVoteFrame,downVoteFrame;
//		/protected Button acceptBtn,rejectBtn;
		
	}
	
/*	private void updateLike(final boolean isLike,final int position)
	{
		point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		//progressBar.setVisibility(View.VISIBLE);
		System.out.println("before get group feed "+Utility.getCurrentDate());
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							System.out.println("after get group feed "+Utility.getCurrentDate());
							List<String> likeArray=object.getList(Constants.LIKE_ARRAY);
							if(isLike)
							{
								likeArray.add(PreferenceSettings.getMobileNo());
								object.increment(Constants.POST_POINT, 50);
								object.put(Constants.LIKE_ARRAY, likeArray);
							}
							else
							{
								likeArray.remove(PreferenceSettings.getMobileNo());
								object.increment(Constants.POST_POINT, -50);
								object.put(Constants.LIKE_ARRAY, likeArray);
							}
							object.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
							//object.pinInBackground(list.get(position).get(Constants.GROUP_ID).toString());
							System.out.println("before update group feed "+Utility.getCurrentDate());
							object.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
							                 if (e == null) {
							                	 System.out.println("after update group feed "+Utility.getCurrentDate());
							                	//progressBar.setVisibility(View.GONE);
							                	//viewHolder.upVote.setEnabled(true);
							                	//viewHolder.downVote.setEnabled(true);
							                	 //list.remove(position);
							                	//list.set(position,object);
							                	//notifyDataSetChanged();
							                 }	//}
  							          }
									});
							
							}
						}
				}
			});
							                	
		//System.out.println("before get group feed "+Utility.getCurrentDate());
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
		query1.whereEqualTo(Constants.FEED_ID, list.get(position).getObjectId());
		query1.whereEqualTo(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID));
		query1.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list2, ParseException e) {
					if (e == null) 
					{
						//System.out.println("before get group feed "+Utility.getCurrentDate());
						if(list2.size() > 0)
						{
							list2.get(0).put(Constants.UP_VOTE, isLike);
							list2.get(0).put(Constants.ACTIVITY_LOCATION, point);
							System.out.println("before get group feed "+Utility.getCurrentDate());
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
						
						
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject object, ParseException e) {
									if (e == null) 
									{
										if(object!=null)
										{
											List<String> likeArray=object.getList(Constants.LIKE_ARRAY);
											if(isLike)
											{
												likeArray.add(PreferenceSettings.getMobileNo());
												object.increment(Constants.POST_POINT, 50);
												object.put(Constants.LIKE_ARRAY, likeArray);
											}
											else
											{
												likeArray.remove(PreferenceSettings.getMobileNo());
												object.increment(Constants.POST_POINT, -50);
												object.put(Constants.LIKE_ARRAY, likeArray);
											}
											object.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
											//object.pinInBackground(list.get(position).get(Constants.GROUP_ID).toString());
											object.saveInBackground(new SaveCallback() {
		      							          public void done(ParseException e) {
			 							                 if (e == null) {
			 							                	progressBar.setVisibility(View.GONE);
			 							                	//viewHolder.upVote.setEnabled(true);
			 							                	//viewHolder.downVote.setEnabled(true);
			 							                	 //list.remove(position);
			 							                	list.set(position,object);
			 							                	notifyDataSetChanged();
			 							                		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
			 							                		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			 							                		query.getFirstInBackground(new GetCallback<ParseObject>() {
			 							               			public void done(ParseObject object, ParseException e) {
			 							               					if (e == null) 
			 							               					{
			 							               						if(object!=null)
			 							               						{
			 							               								if(isLike)
			 							               								object.increment(Constants.BADGE_POINT, 50);
			 							                							else
			 							                								object.increment(Constants.BADGE_POINT, -50);
			 							                							
			 							               							object.saveInBackground(new SaveCallback() {
			 									      							          public void done(ParseException e) {
			 									 							                 if (e == null) {
			 									 							                	//activity.setposition(position);
						 							                							//activity.setAdapter();
						 							                							
			 									 							                 }
			 									      							          }});
			 							                						}
			 							                					}
			 							                				}
			 							             			});
			 							                		   	}	//}
		      							          }
												});
										
										}
									}
							}
						});
					}
				}
		
			});
		
	}
	
	private void updateDislike(final boolean isDisLike,final int position)
	{
		System.out.println("inside update dislike");
		//progressBar.setVisibility(View.VISIBLE);
		
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							List<String> likeArray=object.getList(Constants.DIS_LIKE_ARRAY);
							if(isDisLike)
							{
								likeArray.add(PreferenceSettings.getMobileNo());
								object.increment(Constants.POST_POINT, -50);
								object.put(Constants.DIS_LIKE_ARRAY, likeArray);
							}
							else
							{
								likeArray.remove(PreferenceSettings.getMobileNo());
								object.increment(Constants.POST_POINT, 50);
								object.put(Constants.DIS_LIKE_ARRAY, likeArray);
							}
							object.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
							//object.pinInBackground(list.get(position).get(Constants.GROUP_ID).toString());
							object.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
							                 if (e == null) {
							                	//progressBar.setVisibility(View.GONE);
							                	//viewHolder.upVote.setEnabled(true);
							                	//viewHolder.downVote.setEnabled(true);
							                	// list.remove(position);
							                	//list.set(position,object);
							                	//notifyDataSetChanged();
							               	}	//}
  							          }});
							
							}
						}
				}
			});
							                	
		
		point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		
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
						
						
						ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
						query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final ParseObject object, ParseException e) {
									if (e == null) 
									{
										if(object!=null)
										{
											List<String> likeArray=object.getList(Constants.DIS_LIKE_ARRAY);
											if(isDisLike)
											{
												likeArray.add(PreferenceSettings.getMobileNo());
												object.increment(Constants.POST_POINT, -50);
												object.put(Constants.DIS_LIKE_ARRAY, likeArray);
											}
											else
											{
												likeArray.remove(PreferenceSettings.getMobileNo());
												object.increment(Constants.POST_POINT, 50);
												object.put(Constants.DIS_LIKE_ARRAY, likeArray);
											}
											object.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
											//object.pinInBackground(list.get(position).get(Constants.GROUP_ID).toString());
											object.saveInBackground(new SaveCallback() {
		      							          public void done(ParseException e) {
			 							                 if (e == null) {
			 							                	progressBar.setVisibility(View.GONE);
			 							                	//viewHolder.upVote.setEnabled(true);
			 							                	//viewHolder.downVote.setEnabled(true);
			 							                	// list.remove(position);
			 							                	list.set(position,object);
			 							                	notifyDataSetChanged();
			 							                		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
			 							                		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			 							                		query.getFirstInBackground(new GetCallback<ParseObject>() {
			 							               			public void done(ParseObject object, ParseException e) {
			 							               					if (e == null) 
			 							               					{
			 							               						if(object!=null)
			 							               						{
			 							               							if(isDisLike)
			 							               									object.increment(Constants.BADGE_POINT, -50);
			 							                							else
			 							                								object.increment(Constants.BADGE_POINT, 50);
			 							                							
			 							               									object.saveInBackground(new SaveCallback() {
			 									      							          public void done(ParseException e) {
			 									 							                 if (e == null) {
						 							                							//activity.setAdapter();
			 									 							                 }
			 									      							          }});
			 							                						}
			 							                					}
			 							                				}
			 							                			});
			 							                	}	//}
		      							          }});
										
										}
									}
							}
						});
					}
				}
		
			});
		
	}
	
	private void updateFlagInTable(final int position)
	{
		ParseGeoPoint point;
		progressBar.setVisibility(View.VISIBLE);
		try
		{
			point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		}
		catch(Exception e)
		{
			point = new ParseGeoPoint(0.0, 0.0);
		}
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
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
								List<String> flagArrayList=object.getList(Constants.FLAG_ARRAY);
								flagArrayList.add(PreferenceSettings.getMobileNo());
								object.put(Constants.FLAG_ARRAY, flagArrayList);
								final int flagCount=object.getInt(Constants.FLAG_COUNT);
								
								if(flagCount == 2)
								{
									object.put(Constants.POST_STATUS, "InActive");
								}
								
								object.increment(Constants.FLAG_COUNT, 1);
								
								object.increment(Constants.POST_POINT, -200);
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
																	object.increment(Constants.BADGE_POINT, -200);
																	if(flagCount==2)
																	{
																		int count=object.getInt(Constants.POST_FLAG_COUNT);
																		
																		if(count==2)
																		{
																			object.put(Constants.SUSPENDED, true);
																			object.put(Constants.USER_STATE, "Suspended");
																		}
																	}
																	object.increment(Constants.POST_FLAG_COUNT,1);
																	object.saveInBackground(new SaveCallback() {
																          public void done(ParseException e) {
																                 if (e == null) {
																					 activity.setAdapter();
																					 progressBar.setVisibility(View.GONE);
																					 //Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
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
				 NearByGroupListActivity.flag2=true;
				 MyGroupListActivity.flag1=true;
				object.put(Constants.INVITATION_STATUS, "InActive");
				object.saveInBackground();
				progressBar.setVisibility(View.GONE);
	        	activity.setAdapter();
            	//Toast.makeText(activity, "Invitation Accepted", Toast.LENGTH_SHORT).show();
			}
			else
			{
				 NearByGroupListActivity.flag2=true;
					MyGroupListActivity.flag1=true;
				progressBar.setVisibility(View.GONE);
	        	activity.setAdapter();
            	//Toast.makeText(activity, "Invitation Accepted", Toast.LENGTH_SHORT).show();
			}
		}});
	}*/
}