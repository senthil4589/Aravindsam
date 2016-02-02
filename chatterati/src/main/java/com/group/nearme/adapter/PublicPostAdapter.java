package com.group.nearme.adapter;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.group.nearme.CommentActivity;
import com.group.nearme.EventLocationActivity;
import com.group.nearme.GroupPostListActivity;
import com.group.nearme.MemberProfileActivity;
import com.group.nearme.MyGroupListActivity;
import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.R;
import com.group.nearme.VideoViewActivity;
import com.group.nearme.WebViewActivity;
import com.group.nearme.YoutubePlayerActivity;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.ResizableImageView;
import com.group.nearme.util.Utility;
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

public class PublicPostAdapter extends BaseAdapter
{
	ViewHolder viewHolder;
	LayoutInflater inflater;
	GroupPostListActivity activity;
	List<ParseObject> list;//,userList;
	private ProgressBar progressBar;
	public Dialog mDialog;
	AlertDialog mAlertDialog;
	String flagValue="";
	boolean likeFlag,unLikeFlag;
	private String mGroupId="",mGroupName="",mGroupImage="";
	ImageLoader imageLoader;
	ParseGeoPoint point;
	int memberCount,userPoints;
	String groupType="",youtubeID="";
	ArrayList<List<String>> tempLikeList,tempDisLikeList;
	Typeface tf;
	FeedHashTagAdapter adapter=null;
	int videoStopPosition;
	public boolean isFileDownloading=false,isScrolled=false;
	public PublicPostAdapter(GroupPostListActivity activity,List<ParseObject> list1,ProgressBar progressBar1,String id,String name,String image,String groupType)
	{
		this.activity=activity;
		this.list=list1;
		//this.userList=list2;
		this.progressBar=progressBar1;
		this.mGroupId=id;
		this.mGroupName=name;
		this.mGroupImage=image;
		this.groupType=groupType;
		tempLikeList=new ArrayList<List<String>>();
		tempDisLikeList=new ArrayList<List<String>>();
		inflater = activity.getLayoutInflater();
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
		point = new ParseGeoPoint(activity.gpsTracker.getLatitude(), activity.gpsTracker.getLongitude());
		
		for(int i=0;i<list.size();i++)
		{
			tempLikeList.add((List<String>) list.get(i).get(Constants.LIKE_ARRAY));
			tempDisLikeList.add((List<String>) list.get(i).get(Constants.DIS_LIKE_ARRAY));
		}
		tf = Typeface.createFromAsset(activity.getAssets(), "Lato-Regular.ttf");
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
	
	public void setList(List<ParseObject> list1)
	{
		this.list=list1;
		tempLikeList=new ArrayList<List<String>>();
		tempDisLikeList=new ArrayList<List<String>>();
		for(int i=0;i<list.size();i++)
		{
			tempLikeList.add((List<String>) list.get(i).get(Constants.LIKE_ARRAY));
			tempDisLikeList.add((List<String>) list.get(i).get(Constants.DIS_LIKE_ARRAY));
		}
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		if(list.get(position).get(Constants.POST_TYPE).toString().equals("Event"))
		{
			view = inflater.inflate(R.layout.post_event_view, null);
			viewHolder = new ViewHolder();
			viewHolder.postImage=(ResizableImageView) view.findViewById(R.id.post_image);
			viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
			final ImageView upVote=(ImageView) view.findViewById(R.id.up_vote);
			final ImageView downVote=(ImageView) view.findViewById(R.id.down_vote);
			//viewHolder.share=(ImageView) view.findViewById(R.id.share);
			viewHolder.flag=(ImageView) view.findViewById(R.id.flag);
			viewHolder.delete=(ImageView) view.findViewById(R.id.delete);
			viewHolder.imageCaption=(TextView) view.findViewById(R.id.image_caption);
			viewHolder.userName=(TextView) view.findViewById(R.id.user_name);
			viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
			final TextView points=(TextView) view.findViewById(R.id.points);
			viewHolder.replies=(TextView) view.findViewById(R.id.replies);
			viewHolder.comment_count=(TextView) view.findViewById(R.id.comments_count);
			viewHolder.upVoteFrame=(FrameLayout) view.findViewById(R.id.upvote_frame);
			viewHolder.downVoteFrame=(FrameLayout) view.findViewById(R.id.downvote_frame);
			viewHolder.hashTag=(TextView) view.findViewById(R.id.hash);
			viewHolder.stypeAbstract=(TextView) view.findViewById(R.id.stype_abstract);
			viewHolder.postImageFrame=(FrameLayout) view.findViewById(R.id.post_image_layout);
			
			viewHolder.eventShortMonth=(TextView) view.findViewById(R.id.event_start_month);
			viewHolder.eventShortDate=(TextView) view.findViewById(R.id.event_start_day);
			viewHolder.eventStartEndDate=(TextView) view.findViewById(R.id.event_start_end_date_txt);
			viewHolder.eventStartEndTime=(TextView) view.findViewById(R.id.event_start_end_time_txt);
			viewHolder.eventLocationAddress1=(TextView) view.findViewById(R.id.event_address_line1);
			viewHolder.eventLocationAddress2=(TextView) view.findViewById(R.id.event_address_line2);
			viewHolder.eventAreaName=(TextView) view.findViewById(R.id.area_name);
			viewHolder.eventLocationLayout=(RelativeLayout) view.findViewById(R.id.event_location_root_layout);
			viewHolder.eventDateLayout=(RelativeLayout) view.findViewById(R.id.event_date_time_root_layout);
			
			View line=(View) view.findViewById(R.id.line1);
			
			LayoutParams param=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			FrameLayout.LayoutParams param1=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,list.get(position).getInt(Constants.IMAGE_HEIGHT));
			param1.leftMargin=10;
			param1.rightMargin=10;
			param.addRule(RelativeLayout.BELOW,viewHolder.stypeAbstract.getId());
			viewHolder.postImageFrame.setLayoutParams(param);
			viewHolder.postImage.setAdjustViewBounds(true);
			
			//ParseObject userObject=ParseObject.create(Constants.USER_TABLE); 
			ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.userImage.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.userImage.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.userName.setText(list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
				viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Utility.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
					activity.startActivity(new Intent(activity,MemberProfileActivity.class)
					.putExtra("isFromFeed", true)
					.putExtra(Constants.USER_NAME, list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
					.putExtra(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString())
					.putExtra(Constants.PROFILE_PICTURE, list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
					activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
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
			}
			else
			{
				viewHolder.userName.setVisibility(View.VISIBLE);
				viewHolder.userImage.setVisibility(View.VISIBLE);
				//line.setVisibility(View.VISIBLE);
				line.setPadding(0, 0, 0, 0);
				viewHolder.updatedTime.setPadding(0, 0, 0, 0);
				viewHolder.updatedTime.setTextSize(12);
			}
		if(Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo())
				 || list.get(position).getString(Constants.MOBILE_NO).equals(PreferenceSettings.getMobileNo()))
		{	
			viewHolder.hashTag.setVisibility(View.VISIBLE);
			
			viewHolder.hashTag.setOnClickListener(new View.OnClickListener() {
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
										Utility.showToastMessage(activity, "No tags available for this group");
									}
								}
								else
								{
									System.out.println("exception ::: "+e);
								}
						}});
					
				}
			});
		}
		else
		{
			viewHolder.hashTag.setVisibility(View.GONE);
		}
			
			
		if(list.get(position).get(Constants.MOBILE_NO).toString().equals(PreferenceSettings.getMobileNo())
				|| Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			viewHolder.delete.setVisibility(View.VISIBLE);
			viewHolder.flag.setVisibility(View.GONE);
			
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utility.checkInternetConnectivity(activity))
					{	
						if(list.get(position).getObjectId()!=null)
							showDeleteAlertDialog(position);
				}
				else
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				}
			});
		}
		else
		{
			viewHolder.flag.setVisibility(View.VISIBLE);
			viewHolder.delete.setVisibility(View.GONE);
		}

		viewHolder.stypeAbstract.setText(list.get(position).getString(Constants.ABSTRACT));
		viewHolder.imageCaption.setText(list.get(position).getString(Constants.IMAGE_CAPTION));
		
		HashMap<String, Object> map=(HashMap<String, Object>) list.get(position).get(Constants.POST_ATTRIBUTE);
		final Double latitude=(Double) map.get(Constants.LATITUDE);
		final Double longtitude=(Double) map.get(Constants.LONGTITUDE);
		
		/*viewHolder.eventLocationLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.startActivity(new Intent(activity,EventLocationActivity.class)
				.putExtra(Constants.LATITUDE, latitude).putExtra(Constants.LONGTITUDE, longtitude)
				.putExtra("event_name", list.get(position).getString(Constants.IMAGE_CAPTION)));
			}
		});
		
		viewHolder.eventDateLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();              
				Intent intent = new Intent(Intent.ACTION_EDIT);
				intent.setType("vnd.android.cursor.item/event");
				intent.putExtra("beginTime", cal.getTimeInMillis());
				intent.putExtra("allDay", true);
				intent.putExtra("rrule", "FREQ=YEARLY");
				intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
				intent.putExtra("title", "A Test Event from android app");
				activity.startActivity(intent);
			}
		});
		*/
		viewHolder.eventShortMonth.setText(map.get(Constants.SHORT_START_MONTH).toString());
		viewHolder.eventShortDate.setText(map.get(Constants.START_DATE).toString());
		if(map.get(Constants.START_DATE).toString().equals(map.get(Constants.END_DATE).toString())
				&& map.get(Constants.START_MONTH).toString().equals(map.get(Constants.END_MONTH).toString()))
			viewHolder.eventStartEndDate.setText(map.get(Constants.START_MONTH).toString()+" "+map.get(Constants.START_DATE).toString());
		else
			viewHolder.eventStartEndDate.setText(map.get(Constants.START_MONTH).toString()+" "+map.get(Constants.START_DATE).toString()
					+" - "+map.get(Constants.END_MONTH).toString()+" "+map.get(Constants.END_DATE).toString());
		
		viewHolder.eventStartEndTime.setText(map.get(Constants.START_TIME).toString()+" to "+map.get(Constants.END_TIME).toString());
		viewHolder.eventAreaName.setText(map.get(Constants.AREA_NAME).toString());
		viewHolder.eventLocationAddress2.setText(map.get(Constants.ADDRESS).toString());
		
		try{
			viewHolder.eventLocationAddress1.setText(map.get(Constants.PLACE_NAME).toString());
			
		}
		catch(Exception e){
		}
		
		ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
		viewHolder.postImage.setParseFile(file);
		viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
		viewHolder.postImage.loadInBackground();
		
		final java.util.Date f=list.get(position).getCreatedAt();
		if(f!=null)
			viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
		System.out.println("point :: "+list.get(position).getInt(Constants.POST_POINT));
		//points.setText(list.get(position).getInt(Constants.POST_POINT)+" Points");
		points.setText(list.get(position).getInt(Constants.POST_POINT)+"");
		if(list.get(position).getInt(Constants.COMMENT_COUNT)!=0)
			viewHolder.comment_count.setText(list.get(position).get(Constants.COMMENT_COUNT).toString());
		
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
		
		viewHolder.replies.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callCommentActivity(position);
			}
		});
		viewHolder.postImageFrame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callCommentActivity(position);
			}
		});
		viewHolder.stypeAbstract.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callCommentActivity(position);
			}
		});
		/*viewHolder.replies.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callCommentActivity(position);
			}
		});*/
		
		
		upVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int point=list.get(position).getInt(Constants.POST_POINT);
				if(Utility.checkInternetConnectivity(activity))
				{	
					if(list.get(position).getObjectId()!=null){
				if(tempLikeList.get(position).contains(PreferenceSettings.getMobileNo()))
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					downVote.setEnabled(true);
					upVote.setEnabled(true); 
					tempLikeList.get(position).remove(PreferenceSettings.getMobileNo());
					updateLike(false,position,points);
				}
				
				else
				{
					upVote.setImageResource(R.drawable.up2);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(false);
					tempLikeList.get(position).add(PreferenceSettings.getMobileNo());
					updateLike(true,position,points);
				}
				}
				}
				else
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				
			}
		});
		downVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int point=list.get(position).getInt(Constants.POST_POINT);
				if(Utility.checkInternetConnectivity(activity))
				{	
					if(list.get(position).getObjectId()!=null){
				if(tempDisLikeList.get(position).contains(PreferenceSettings.getMobileNo()))
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(true);
					tempDisLikeList.get(position).remove(PreferenceSettings.getMobileNo());
					updateDislike(false,position,points);
				}
				else
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down2);
					downVote.setEnabled(true);
					upVote.setEnabled(false);
					tempDisLikeList.get(position).add(PreferenceSettings.getMobileNo());
					updateDislike(true,position,points);
				}
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
				if(list.get(position).getList(Constants.FLAG_ARRAY).contains(PreferenceSettings.getMobileNo()))
				{
					//viewHolder.flag.setEnabled(false);
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.flag_already_done));
				}
				else
				{
					if(list.get(position).getObjectId()!=null)
						showFlagDialog(position);
				}
			}
			else
				Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				
			}
		});
		

			
			return view;
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Member"))
		{
			view = inflater.inflate(R.layout.feed_info, null);
			viewHolder = new ViewHolder();
			viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
			viewHolder.postText=(TextView) view.findViewById(R.id.post_text);
			viewHolder.userName=(TextView) view.findViewById(R.id.user_name);
			viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
			viewHolder.updatedTime.setTextSize(12);
			
			
		//viewHolder.userName.setText(list.get(position).get(Constants.MEMBER_NAME)+" joined this group");
		//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
		
		ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
		
		if( userObject!=null ){
		viewHolder.userImage.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
		viewHolder.userImage.loadInBackground();
		//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
		viewHolder.userName.setText(list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME)+" - "+activity.getResources().getString(R.string.new_member));
		}
		
		viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utility.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
				activity.startActivity(new Intent(activity,MemberProfileActivity.class)
				.putExtra("isFromFeed", true)
				.putExtra(Constants.USER_NAME, list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
				.putExtra(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString())
				.putExtra(Constants.PROFILE_PICTURE, list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
				activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		//viewHolder.postText.setText(list.get(position).get(Constants.POST_TEXT).toString());
		final java.util.Date f=list.get(position).getCreatedAt();
		viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
			
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Invitation"))
		{
			view = inflater.inflate(R.layout.join_invitation, null);
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
			ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.userImage.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.userImage.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.name.setText(list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
			
			final java.util.Date f=list.get(position).getCreatedAt();
			viewHolder.time.setText(Utility.getTimeAgo(f.getTime()));
			viewHolder.time.setTextSize(12);
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
					if(Utility.checkInternetConnectivity(activity))
					{
						acceptBtn.setEnabled(false);
						rejectBtn.setEnabled(false);
						acceptJoinInvitation();
					}
					else
						Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
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
													             							        	 removeFromInvitation(position);
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
					else
						Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
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
										List<String> adminList=object.getList(Constants.ADMIN_ARRAY);
										if(!object.getBoolean(Constants.MEMBERSHIP_APPROVAL) || adminList.contains(PreferenceSettings.getMobileNo()))
										{
											object.increment(Constants.NEWS_FEED_COUNT, -1);
	             							//list.get(0).put(Constants.LATEST_POST,request);
											object.saveInBackground(new SaveCallback() {
          							        public void done(ParseException e) {
          							          if (e == null) {
          							        	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
          										query.whereEqualTo(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO));
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
														            						object.unpinInBackground(list.get(position).get(Constants.GROUP_ID).toString());
														            						object.deleteInBackground(new DeleteCallback() {
												             							        public void done(ParseException e) {
													             							          if (e == null) {
													             							        	 NearByGroupListActivity.flag2=true;
													             										 MyGroupListActivity.flag1=true;
													             							        		progressBar.setVisibility(View.GONE);
													             							        		activity.setAdapter();
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
		  if (list.get(position).getString(Constants.POST_STATUS).equals("Pending") ) {
			  
			  view = inflater.inflate(R.layout.post_approval_view, null);
				viewHolder = new ViewHolder();
				viewHolder.postImage=(ResizableImageView) view.findViewById(R.id.post_image);
				viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
				viewHolder.postText=(TextView) view.findViewById(R.id.post_text);
				viewHolder.imageCaption=(TextView) view.findViewById(R.id.image_caption);
				viewHolder.userName=(TextView) view.findViewById(R.id.user_name);
				viewHolder.linkDes=(TextView) view.findViewById(R.id.link_des);
				viewHolder.linkUrl=(TextView) view.findViewById(R.id.link_url);
				viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
				
				viewHolder.stypeAbstract=(TextView) view.findViewById(R.id.stype_abstract);
				final FrameLayout playImage=(FrameLayout) view.findViewById(R.id.video_play_frame);
				viewHolder.postImageFrame=(FrameLayout) view.findViewById(R.id.post_image_layout);
				//viewHolder.youTubeView=(YouTubePlayerView) view.findViewById(R.id.youtube_view);
				viewHolder.sourceUrl=(TextView) view.findViewById(R.id.source_url);
				final ProgressBar videoProgress=(ProgressBar) view.findViewById(R.id.progressBar);
				//viewHolder.shareImage=(ImageView) view.findViewById(R.id.share);
				
				final VideoView videoView = (VideoView) view.findViewById(R.id.videoView);
				viewHolder.sVideoLayout=(RelativeLayout) view.findViewById(R.id.svideo_layout);
				final FrameLayout sVideoPlayImage=(FrameLayout) view.findViewById(R.id.svideo_play_frame);
				final ResizableImageView sVideoBg=(ResizableImageView) view.findViewById(R.id.svideo_bg_image);
				final ProgressBar sVideoProgress=(ProgressBar) view.findViewById(R.id.video_progrss);
				
			
				final ImageView acceptBtn=(ImageView) view.findViewById(R.id.accept);
				final ImageView rejectBtn=(ImageView) view.findViewById(R.id.reject);
				
				acceptBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(Utility.checkInternetConnectivity(activity))
						{
							acceptBtn.setEnabled(false);
							rejectBtn.setEnabled(false);
							progressBar.setVisibility(View.VISIBLE);
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
	    					query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
	    					query.getFirstInBackground(new GetCallback<ParseObject>() {
	    					public void done(ParseObject object, ParseException e) {
	    						if(object!=null)
	    						{
	    							object.put(Constants.POST_STATUS, "Active");
	    							object.saveInBackground();
	    							object.pinInBackground(new SaveCallback() {
										
										@Override
										public void done(ParseException arg0) {
											
											activity.setAdapter();
											progressBar.setVisibility(View.GONE);
										}
									});
	    							progressBar.setVisibility(View.GONE);
	    						}
	    						else
	    						{
	    							progressBar.setVisibility(View.GONE);
	    						}
	    					
	    					}});
						}
						else
							Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
						
					}
				});
				
				rejectBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						if(Utility.checkInternetConnectivity(activity))
						{
							acceptBtn.setEnabled(false);
							rejectBtn.setEnabled(false);
							progressBar.setVisibility(View.VISIBLE);
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
	    					query.whereEqualTo(Constants.OBJECT_ID, list.get(position).getObjectId());
	    					query.getFirstInBackground(new GetCallback<ParseObject>() {
	    					public void done(ParseObject object, ParseException e) {
	    						if(object!=null)
	    						{
	    							object.put(Constants.POST_STATUS, "InActive");
	    							object.saveInBackground();
	    							object.pinInBackground(new SaveCallback() {
										
										@Override
										public void done(ParseException arg0) {
											
											activity.setAdapter();
											progressBar.setVisibility(View.GONE);
										}
									});
	    							
	    						}
	    						else
	    						{
	    							progressBar.setVisibility(View.GONE);
	    						}
	    					
	    					}});
						}
						else
							Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
						
					}
				});
				
				
				View line=(View) view.findViewById(R.id.line1);
				
				/*LayoutParams param=new LayoutParams(LayoutParams.MATCH_PARENT,list.get(position).getInt(Constants.IMAGE_HEIGHT));
				param.leftMargin=10;
				param.rightMargin=10;
				param.addRule(RelativeLayout.BELOW,viewHolder.imageCaption.getId());
				viewHolder.postImage.setLayoutParams(param);
				viewHolder.postImage.setAdjustViewBounds(true);
				*/
				LayoutParams param=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
				FrameLayout.LayoutParams param1=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,list.get(position).getInt(Constants.IMAGE_HEIGHT));
				param1.leftMargin=10;
				param1.rightMargin=10;
				param.addRule(RelativeLayout.BELOW,viewHolder.stypeAbstract.getId());
				viewHolder.postImageFrame.setLayoutParams(param);
				viewHolder.postImage.setAdjustViewBounds(true);
				
				ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
				
				if( userObject!=null ){
				viewHolder.userImage.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
				viewHolder.userImage.loadInBackground();
				viewHolder.userName.setText(list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
				}
				
				viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Utility.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
						activity.startActivity(new Intent(activity,MemberProfileActivity.class)
						.putExtra("isFromFeed", true)
						.putExtra(Constants.USER_NAME, list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
						.putExtra(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString())
						.putExtra(Constants.PROFILE_PICTURE, list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
						activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
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
					line.setPadding(0, 0, 0, 0);
					viewHolder.updatedTime.setPadding(0, 0, 0, 0);
					viewHolder.updatedTime.setTextSize(12);
					viewHolder.postText.setAutoLinkMask(Linkify.WEB_URLS);
				}
				
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Image"))
				{
					viewHolder.sVideoLayout.setVisibility(View.GONE);
					viewHolder.sourceUrl.setVisibility(View.GONE);
					playImage.setVisibility(View.GONE);
					viewHolder.stypeAbstract.setVisibility(View.GONE);
					viewHolder.linkUrl.setVisibility(View.GONE);
					viewHolder.linkDes.setVisibility(View.GONE);
					if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
					{
						viewHolder.imageCaption.setVisibility(View.GONE);
					}
					else
					{
						viewHolder.imageCaption.setVisibility(View.VISIBLE);
						viewHolder.imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
					}
					viewHolder.postImage.setVisibility(View.VISIBLE);
					viewHolder.postText.setVisibility(View.GONE);
					ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
					viewHolder.postImage.setParseFile(file);
					viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
					viewHolder.postImage.loadInBackground();
				}
				
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("SVideo"))
				{
					 sVideoBg.setVisibility(View.VISIBLE);
					 videoView.setVisibility(View.GONE);
					viewHolder.sVideoLayout.setVisibility(View.GONE);
					viewHolder.sourceUrl.setVisibility(View.GONE);
					playImage.setVisibility(View.VISIBLE);
					viewHolder.stypeAbstract.setVisibility(View.GONE);
					viewHolder.linkUrl.setVisibility(View.GONE);
					viewHolder.linkDes.setVisibility(View.GONE);
					if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
					{
						viewHolder.imageCaption.setVisibility(View.GONE);
					}
					else
					{
						viewHolder.imageCaption.setVisibility(View.VISIBLE);
						viewHolder.imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
					}
					viewHolder.postImage.setVisibility(View.VISIBLE);
					viewHolder.postText.setVisibility(View.GONE);
					ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
					
					viewHolder.postImage.setParseFile(file);
					viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
					viewHolder.postImage.loadInBackground();
					//Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(viewHolder.postImage);
				}
			
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("GIFVideo"))
				{
					 sVideoBg.setVisibility(View.VISIBLE);
					 videoView.setVisibility(View.GONE);
					viewHolder.sVideoLayout.setVisibility(View.VISIBLE);
					viewHolder.sourceUrl.setVisibility(View.GONE);
					playImage.setVisibility(View.GONE);
					viewHolder.stypeAbstract.setVisibility(View.GONE);
					viewHolder.linkUrl.setVisibility(View.GONE);
					viewHolder.linkDes.setVisibility(View.GONE);
					if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
					{
						viewHolder.imageCaption.setVisibility(View.GONE);
					}
					else
					{
						viewHolder.imageCaption.setVisibility(View.VISIBLE);
						viewHolder.imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
					}
					viewHolder.postImage.setVisibility(View.GONE);
					viewHolder.postText.setVisibility(View.GONE);
					ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
					
					file.getDataInBackground();
					
					Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(sVideoBg);
					final ViewTreeObserver observer = sVideoBg.getViewTreeObserver();
					observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
					    @Override 
					    public void onGlobalLayout() { 
					        sVideoBg.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
					        int width  = sVideoBg.getMeasuredWidth();
					        int height = sVideoBg.getMeasuredHeight(); 
							RelativeLayout.LayoutParams videoParam=new RelativeLayout.LayoutParams(sVideoBg.getWidth(),sVideoBg.getHeight());
							videoParam.leftMargin=20;
							videoParam.rightMargin=20;
							//videoView.setBackgroundColor(Color.WHITE);
							videoParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
							//videoParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
							videoParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
							videoParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
							videoView.setLayoutParams(videoParam);
					    } 
					});
				}
				
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link") ||
						list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				{
					viewHolder.sVideoLayout.setVisibility(View.GONE);
					viewHolder.sourceUrl.setVisibility(View.GONE);
					//viewHolder.youTubeView.setVisibility(View.GONE);
					viewHolder.stypeAbstract.setVisibility(View.GONE);
					//viewHolder.imageCaption.setPadding(0, 0, 0, 0);
					viewHolder.linkUrl.setPadding(0, 15, 0, 0);
					viewHolder.linkUrl.setTextSize(15);
					viewHolder.postText.setVisibility(View.GONE);
					viewHolder.postImage.setVisibility(View.VISIBLE);
					
					viewHolder.imageCaption.setVisibility(View.VISIBLE);
					viewHolder.imageCaption.setText(list.get(position).get(Constants.POST_TEXT).toString());
					//viewHolder.postText.setVisibility(View.VISIBLE);
					viewHolder.linkDes.setVisibility(View.VISIBLE);
					viewHolder.linkUrl.setVisibility(View.VISIBLE);
					
					viewHolder.linkDes.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());//+"\n\n"+list.get(position).get(Constants.LINK_URL).toString());
					viewHolder.linkUrl.setText(list.get(position).get(Constants.SITE_NAME).toString());
					ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
					viewHolder.postImage.setParseFile(file);
					viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
					viewHolder.postImage.loadInBackground();
					
					if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
						playImage.setVisibility(View.VISIBLE);
					else
						playImage.setVisibility(View.GONE);
					
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					viewHolder.sVideoLayout.setVisibility(View.GONE);
					viewHolder.sourceUrl.setVisibility(View.VISIBLE);
					playImage.setVisibility(View.GONE);
					//viewHolder.youTubeView.setVisibility(View.GONE);
					viewHolder.stypeAbstract.setVisibility(View.VISIBLE);
					//viewHolder.imageCaption.setPadding(0, 10, 0, 0);
					viewHolder.linkUrl.setPadding(0, 0, 0, 0);
					viewHolder.linkUrl.setTextSize(13);
					System.out.println("inside stype fsdgdfngdfgndfjk");
					viewHolder.postText.setVisibility(View.GONE);
					viewHolder.linkDes.setVisibility(View.GONE);
					viewHolder.postImage.setVisibility(View.VISIBLE);
					viewHolder.imageCaption.setVisibility(View.VISIBLE);
					viewHolder.stypeAbstract.setText(list.get(position).getString(Constants.ABSTRACT));
					viewHolder.imageCaption.setText(list.get(position).getString(Constants.IMAGE_CAPTION));
					//viewHolder.linkDes.setVisibility(View.VISIBLE);
					
					 try {
						URL url=new URL(list.get(position).getString(Constants.LINK_URL));
						viewHolder.sourceUrl.setText("Source: "+url.getHost().replace("www.", ""));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
					//viewHolder.linkDes.setText(list.get(position).getString(Constants.IMAGE_CAPTION));//+"\n\n"+list.get(position).get(Constants.LINK_URL).toString());
					if(list.get(position).getString(Constants.CREDIT)==null)
					{
						viewHolder.linkUrl.setVisibility(View.GONE);
						//viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
					}
					else
					{
						viewHolder.linkUrl.setVisibility(View.VISIBLE);
						viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
					}
					
					ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
					viewHolder.postImage.setParseFile(file);
					viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
					viewHolder.postImage.loadInBackground();
					
					//viewHolder.linkUrl.setMovementMethod(LinkMovementMethod.getInstance());
					viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
					//viewHolder.linkUrl.setText(addClickablePart("Credit: "+list.get(position).getString(Constants.CREDIT),8,8+list.get(position).getString(Constants.CREDIT).length(),list.get(position).getString(Constants.CREDIT_URL)),BufferType.SPANNABLE);
					
				}
				else //if(list.get(position).get(Constants.POST_TEXT).toString().equals("Text"))
				{
					viewHolder.sVideoLayout.setVisibility(View.GONE);
					viewHolder.sourceUrl.setVisibility(View.GONE);
					playImage.setVisibility(View.GONE);
					//viewHolder.youTubeView.setVisibility(View.GONE);
					viewHolder.stypeAbstract.setVisibility(View.GONE);
					//viewHolder.youTubeView.setVisibility(View.GONE);
					viewHolder.linkDes.setVisibility(View.GONE);
					viewHolder.linkUrl.setVisibility(View.GONE);
					viewHolder.postImage.setVisibility(View.GONE);
					viewHolder.imageCaption.setVisibility(View.GONE);
					viewHolder.postText.setVisibility(View.VISIBLE);
					viewHolder.postText.setText(list.get(position).get(Constants.POST_TEXT).toString());
				}
				
				
				final java.util.Date f=list.get(position).getCreatedAt();
				if(f!=null)
					viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
				
				viewHolder.postText.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link"))
						{
							activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
						else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
						{
							
						}
						else
						{
							//callCommentActivity(position);
						}
							
					}
				});
				
				viewHolder.linkUrl.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link")
								|| list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
						{
							activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
						else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
						{
							activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.CREDIT_URL).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
					}
				});
				
				viewHolder.linkDes.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link"))
						{
							activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
						/*else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
						{
							activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.CREDIT_URL).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
		*/			}
				});
				
				
				viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link"))
						{
							activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
						else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
						{
							System.out.println("video id ::: "+list.get(position).get(Constants.VIDEO_ID).toString());
							if(list.get(position).getString(Constants.SITE_NAME).equals("YouTube"))
							{
							activity.startActivity(new Intent(activity,YoutubePlayerActivity.class).putExtra("title", list.get(position).getString(Constants.IMAGE_CAPTION)).putExtra(Constants.VIDEO_ID, list.get(position).get(Constants.VIDEO_ID).toString()));
							//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
							}
							else
							{
								activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
								//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
							}
						}
						else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
						{
							//callCommentActivity(position);
						}
						else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Image"))
						{
							//callCommentActivity(position);
						}
						else if(list.get(position).get(Constants.POST_TYPE).toString().equals("SVideo"))
						{
								isScrolled=false;
								isFileDownloading=true;
								ParseFile file=(ParseFile) list.get(position).get(Constants.POST_VIDEO);
								playImage.setVisibility(View.GONE);
								videoProgress.setVisibility(View.VISIBLE);
								//videoProgress.setp
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
											//byte[] videoByte= list.get(position).getParseFile(Constants.POST_VIDEO).getData();
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
										//	return false;
										}
									    System.out.println("isScrolled :: "+isScrolled);
									    if(!isScrolled)
									    activity.startActivity(new Intent(activity,VideoViewActivity.class).putExtra("video", uriSting));
									
								}
							}, new ProgressCallback() {
								@Override
								public void done(Integer progress) {
									videoProgress.setProgress(progress);
								}
							});
						}
						
					}
				});
				
				viewHolder.sVideoLayout.setOnClickListener(new View.OnClickListener() {
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
							    	isScrolled=false;
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
										    if(!isScrolled)  
										    {
										    sVideoPlayImage.setVisibility(View.GONE);
											videoView.setVisibility(View.VISIBLE);
									    	videoView.setVideoPath(uriSting);
										    videoView.requestFocus();
										   System.out.println("before start play video");
										    videoView.start();
										    
										}
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
				
						
				viewHolder.stypeAbstract.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
					   if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
						{
						  // callCommentActivity(position);
						}
					}
				});
				
				viewHolder.sourceUrl.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
					   if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
						{
						   activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
						}
					}
				});
		  }// end of if
		  else 
		  {
			view = inflater.inflate(R.layout.public_post_list_item, null);
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
			viewHolder.linkDes=(TextView) view.findViewById(R.id.link_des);
			viewHolder.linkUrl=(TextView) view.findViewById(R.id.link_url);
			viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
			final TextView points=(TextView) view.findViewById(R.id.points);
			viewHolder.replies=(TextView) view.findViewById(R.id.replies);
			viewHolder.comment_count=(TextView) view.findViewById(R.id.comments_count);
			viewHolder.upVoteFrame=(FrameLayout) view.findViewById(R.id.upvote_frame);
			viewHolder.downVoteFrame=(FrameLayout) view.findViewById(R.id.downvote_frame);
			viewHolder.hashTag=(TextView) view.findViewById(R.id.hash);
			viewHolder.stypeAbstract=(TextView) view.findViewById(R.id.stype_abstract);
			final FrameLayout playImage=(FrameLayout) view.findViewById(R.id.video_play_frame);
			viewHolder.postImageFrame=(FrameLayout) view.findViewById(R.id.post_image_layout);
			//viewHolder.youTubeView=(YouTubePlayerView) view.findViewById(R.id.youtube_view);
			viewHolder.sourceUrl=(TextView) view.findViewById(R.id.source_url);
			final ProgressBar videoProgress=(ProgressBar) view.findViewById(R.id.progressBar);
			//viewHolder.shareImage=(ImageView) view.findViewById(R.id.share);
			
			final VideoView videoView = (VideoView) view.findViewById(R.id.videoView);
			viewHolder.sVideoLayout=(RelativeLayout) view.findViewById(R.id.svideo_layout);
			final FrameLayout sVideoPlayImage=(FrameLayout) view.findViewById(R.id.svideo_play_frame);
			final ResizableImageView sVideoBg=(ResizableImageView) view.findViewById(R.id.svideo_bg_image);
			final ProgressBar sVideoProgress=(ProgressBar) view.findViewById(R.id.video_progrss);
			
			/*RelativeLayout.LayoutParams videoParam=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,list.get(position).getInt(Constants.IMAGE_HEIGHT));
			videoView.setLayoutParams(videoParam);
			videoParam.addRule(RelativeLayout.BELOW,viewHolder.imageCaption.getId());
			viewHolder.sVideoLayout.setLayoutParams(videoParam);*/
			
			View line=(View) view.findViewById(R.id.line1);
			
			LayoutParams param=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			FrameLayout.LayoutParams param1=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,list.get(position).getInt(Constants.IMAGE_HEIGHT));
			param1.leftMargin=10;
			param1.rightMargin=10;
			param.addRule(RelativeLayout.BELOW,viewHolder.stypeAbstract.getId());
			viewHolder.postImageFrame.setLayoutParams(param);
			viewHolder.postImage.setAdjustViewBounds(true);
			
			//ParseObject userObject=ParseObject.create(Constants.USER_TABLE); 
			ParseObject userObject = list.get(position).getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.userImage.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.userImage.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.userName.setText(list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
			
		/*	viewHolder.shareImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Utility.sharePost(activity,mGroupId,list.get(position).getObjectId());
				}
			});
		*/	
			viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Utility.setParseFile(list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
					activity.startActivity(new Intent(activity,MemberProfileActivity.class)
					.putExtra("isFromFeed", true)
					.putExtra(Constants.USER_NAME, list.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
					.putExtra(Constants.MOBILE_NO, list.get(position).get(Constants.MOBILE_NO).toString())
					.putExtra(Constants.PROFILE_PICTURE, list.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
					activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
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
		if(Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo())
				 || list.get(position).getString(Constants.MOBILE_NO).equals(PreferenceSettings.getMobileNo()))
		{	
			viewHolder.hashTag.setVisibility(View.VISIBLE);
			
			viewHolder.hashTag.setOnClickListener(new View.OnClickListener() {
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
										Utility.showToastMessage(activity, "No tags available for this group");
									}
								}
								else
								{
									System.out.println("exception ::: "+e);
								}
						}});
					
				}
			});
		}
		else
		{
			viewHolder.hashTag.setVisibility(View.GONE);
		}
			
			
		if(list.get(position).get(Constants.MOBILE_NO).toString().equals(PreferenceSettings.getMobileNo())
				|| Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
		{
			viewHolder.delete.setVisibility(View.VISIBLE);
			viewHolder.flag.setVisibility(View.GONE);
			
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utility.checkInternetConnectivity(activity))
					{	
						if(list.get(position).getObjectId()!=null)
							showDeleteAlertDialog(position);
				}
				else
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				}
			});
		}
		else
		{
			viewHolder.flag.setVisibility(View.VISIBLE);
			viewHolder.delete.setVisibility(View.GONE);
		}
		videoProgress.setVisibility(View.GONE);
		if(list.get(position).get(Constants.POST_TYPE).toString().equals("Image"))
		{
			viewHolder.sVideoLayout.setVisibility(View.GONE);
			//viewHolder.imageCaption.setPadding(0, 0, 0, 0);
			//viewHolder.youTubeView.setVisibility(View.GONE);
			viewHolder.sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			viewHolder.stypeAbstract.setVisibility(View.GONE);
			viewHolder.linkUrl.setVisibility(View.GONE);
			viewHolder.linkDes.setVisibility(View.GONE);
			if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				viewHolder.imageCaption.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.imageCaption.setVisibility(View.VISIBLE);
				viewHolder.imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
			}
			viewHolder.postImage.setVisibility(View.VISIBLE);
			viewHolder.postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			//viewHolder.postImage.setParseFile(file);
			//viewHolder.postImage.loadInBackground();
			//Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(viewHolder.postImage);
			viewHolder.postImage.setParseFile(file);
			viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
			viewHolder.postImage.loadInBackground();
		}
		
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("SVideo"))
		{
			 sVideoBg.setVisibility(View.VISIBLE);
			 videoView.setVisibility(View.GONE);
			viewHolder.sVideoLayout.setVisibility(View.GONE);
			viewHolder.sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.VISIBLE);
			viewHolder.stypeAbstract.setVisibility(View.GONE);
			viewHolder.linkUrl.setVisibility(View.GONE);
			viewHolder.linkDes.setVisibility(View.GONE);
			if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				viewHolder.imageCaption.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.imageCaption.setVisibility(View.VISIBLE);
				viewHolder.imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
			}
			viewHolder.postImage.setVisibility(View.VISIBLE);
			viewHolder.postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			
			viewHolder.postImage.setParseFile(file);
			viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
			viewHolder.postImage.loadInBackground();
			//Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(viewHolder.postImage);
		}
	
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("GIFVideo"))
		{
			 sVideoBg.setVisibility(View.VISIBLE);
			 videoView.setVisibility(View.GONE);
			viewHolder.sVideoLayout.setVisibility(View.VISIBLE);
			viewHolder.sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			viewHolder.stypeAbstract.setVisibility(View.GONE);
			viewHolder.linkUrl.setVisibility(View.GONE);
			viewHolder.linkDes.setVisibility(View.GONE);
			if(list.get(position).get(Constants.IMAGE_CAPTION).toString().equals(""))
			{
				viewHolder.imageCaption.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.imageCaption.setVisibility(View.VISIBLE);
				viewHolder.imageCaption.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());
			}
			viewHolder.postImage.setVisibility(View.GONE);
			viewHolder.postText.setVisibility(View.GONE);
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			
			file.getDataInBackground();
			
			Picasso.with(activity).load(file.getUrl()).placeholder(activity.getResources().getDrawable(R.drawable.group_image)).into(sVideoBg);
			final ViewTreeObserver observer = sVideoBg.getViewTreeObserver();
			observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
			    @Override 
			    public void onGlobalLayout() { 
			        sVideoBg.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
			        int width  = sVideoBg.getMeasuredWidth();
			        int height = sVideoBg.getMeasuredHeight(); 
					RelativeLayout.LayoutParams videoParam=new RelativeLayout.LayoutParams(sVideoBg.getWidth(),sVideoBg.getHeight());
					videoParam.leftMargin=20;
					videoParam.rightMargin=20;
					//videoView.setBackgroundColor(Color.WHITE);
					videoParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					//videoParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					videoParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					videoParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					videoView.setLayoutParams(videoParam);
			    } 
			});
		}
		
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link") ||
				list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
		{
			viewHolder.sVideoLayout.setVisibility(View.GONE);
			viewHolder.sourceUrl.setVisibility(View.GONE);
			//viewHolder.youTubeView.setVisibility(View.GONE);
			viewHolder.stypeAbstract.setVisibility(View.GONE);
			//viewHolder.imageCaption.setPadding(0, 0, 0, 0);
			viewHolder.linkUrl.setPadding(0, 15, 0, 0);
			viewHolder.linkUrl.setTextSize(15);
			viewHolder.postText.setVisibility(View.GONE);
			/*if(list.get(position).get(Constants.POST_TEXT).toString().equals(""))
					viewHolder.postText.setVisibility(View.GONE);
			else
			{
				viewHolder.postText.setVisibility(View.VISIBLE);
				viewHolder.postText.setText(list.get(position).get(Constants.POST_TEXT).toString());
			}*/
			viewHolder.postImage.setVisibility(View.VISIBLE);
			
			viewHolder.imageCaption.setVisibility(View.VISIBLE);
			viewHolder.imageCaption.setText(list.get(position).get(Constants.POST_TEXT).toString());
			//viewHolder.postText.setVisibility(View.VISIBLE);
			viewHolder.linkDes.setVisibility(View.VISIBLE);
			viewHolder.linkUrl.setVisibility(View.VISIBLE);
			
			viewHolder.linkDes.setText(list.get(position).get(Constants.IMAGE_CAPTION).toString());//+"\n\n"+list.get(position).get(Constants.LINK_URL).toString());
			viewHolder.linkUrl.setText(list.get(position).get(Constants.SITE_NAME).toString());
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			viewHolder.postImage.setParseFile(file);
			viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
			viewHolder.postImage.loadInBackground();
			
			if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				playImage.setVisibility(View.VISIBLE);
			else
				playImage.setVisibility(View.GONE);
			
		}
		else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
		{
			viewHolder.sVideoLayout.setVisibility(View.GONE);
			viewHolder.sourceUrl.setVisibility(View.VISIBLE);
			playImage.setVisibility(View.GONE);
			//viewHolder.youTubeView.setVisibility(View.GONE);
			viewHolder.stypeAbstract.setVisibility(View.VISIBLE);
			//viewHolder.imageCaption.setPadding(0, 10, 0, 0);
			viewHolder.linkUrl.setPadding(0, 0, 0, 0);
			viewHolder.linkUrl.setTextSize(13);
			System.out.println("inside stype fsdgdfngdfgndfjk");
			viewHolder.postText.setVisibility(View.GONE);
			viewHolder.linkDes.setVisibility(View.GONE);
			viewHolder.postImage.setVisibility(View.VISIBLE);
			viewHolder.imageCaption.setVisibility(View.VISIBLE);
			viewHolder.stypeAbstract.setText(list.get(position).getString(Constants.ABSTRACT));
			viewHolder.imageCaption.setText(list.get(position).getString(Constants.IMAGE_CAPTION));
			//viewHolder.linkDes.setVisibility(View.VISIBLE);
			
			 try {
				URL url=new URL(list.get(position).getString(Constants.LINK_URL));
				viewHolder.sourceUrl.setText("Source: "+url.getHost().replace("www.", ""));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			//viewHolder.linkDes.setText(list.get(position).getString(Constants.IMAGE_CAPTION));//+"\n\n"+list.get(position).get(Constants.LINK_URL).toString());
			if(list.get(position).getString(Constants.CREDIT)==null)
			{
				viewHolder.linkUrl.setVisibility(View.GONE);
				//viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			}
			else
			{
				viewHolder.linkUrl.setVisibility(View.VISIBLE);
				viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			}
			
			ParseFile file=(ParseFile) list.get(position).get(Constants.POST_IMAGE);
			viewHolder.postImage.setParseFile(file);
			viewHolder.postImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
			viewHolder.postImage.loadInBackground();
			
			//viewHolder.linkUrl.setMovementMethod(LinkMovementMethod.getInstance());
			viewHolder.linkUrl.setText("Credit: "+list.get(position).getString(Constants.CREDIT));
			//viewHolder.linkUrl.setText(addClickablePart("Credit: "+list.get(position).getString(Constants.CREDIT),8,8+list.get(position).getString(Constants.CREDIT).length(),list.get(position).getString(Constants.CREDIT_URL)),BufferType.SPANNABLE);
			
		}
		/*else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
		{
			System.out.println("inside video post type");
			viewHolder.postImage.setVisibility(View.GONE);
			viewHolder.youTubeView.setVisibility(View.VISIBLE);
			viewHolder.youTubeView.initialize("AIzaSyA60BDL4qE5zPlej1Wq1jcrpcpaD5hl1us",  new OnInitializedListener() {
		        @Override
		        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
System.out.println("inside onInitializationSuccess");
		            if (!wasRestored) {
		                //YPlayer = player;
		                player.setFullscreen(true);
		                player.loadVideo("2zNSgSzhBfM");
		                player.play();
		            }
		        }
		        @Override
		        public void onInitializationFailure(Provider arg0, YouTubeInitializationResult arg1) {
		        	System.out.println("inside onInitializationFailure"+arg1.toString());

		        }
		    });
		}*/
		else //if(list.get(position).get(Constants.POST_TEXT).toString().equals("Text"))
		{
			viewHolder.sVideoLayout.setVisibility(View.GONE);
			viewHolder.sourceUrl.setVisibility(View.GONE);
			playImage.setVisibility(View.GONE);
			//viewHolder.youTubeView.setVisibility(View.GONE);
			viewHolder.stypeAbstract.setVisibility(View.GONE);
			//viewHolder.youTubeView.setVisibility(View.GONE);
			viewHolder.linkDes.setVisibility(View.GONE);
			viewHolder.linkUrl.setVisibility(View.GONE);
			viewHolder.postImage.setVisibility(View.GONE);
			viewHolder.imageCaption.setVisibility(View.GONE);
			viewHolder.postText.setVisibility(View.VISIBLE);
			viewHolder.postText.setText(list.get(position).get(Constants.POST_TEXT).toString());
			
		}
		
		final java.util.Date f=list.get(position).getCreatedAt();
		if(f!=null)
			viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
		System.out.println("point :: "+list.get(position).getInt(Constants.POST_POINT));
		//points.setText(list.get(position).getInt(Constants.POST_POINT)+" Points");
		points.setText(list.get(position).getInt(Constants.POST_POINT)+"");
		
		if(list.get(position).getInt(Constants.COMMENT_COUNT)!=0)
		viewHolder.comment_count.setText(list.get(position).get(Constants.COMMENT_COUNT).toString());
		
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
		
		upVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int point=list.get(position).getInt(Constants.POST_POINT);
				if(Utility.checkInternetConnectivity(activity))
				{	
					if(list.get(position).getObjectId()!=null){
				if(tempLikeList.get(position).contains(PreferenceSettings.getMobileNo()))
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					downVote.setEnabled(true);
					upVote.setEnabled(true); 
					tempLikeList.get(position).remove(PreferenceSettings.getMobileNo());
					updateLike(false,position,points);
				}
				
				else
				{
					upVote.setImageResource(R.drawable.up2);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(false);
					tempLikeList.get(position).add(PreferenceSettings.getMobileNo());
					updateLike(true,position,points);
				}
				}
				}
				else
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				
			}
		});
		downVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int point=list.get(position).getInt(Constants.POST_POINT);
				if(Utility.checkInternetConnectivity(activity))
				{	
					if(list.get(position).getObjectId()!=null){
				if(tempDisLikeList.get(position).contains(PreferenceSettings.getMobileNo()))
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down1);
					upVote.setEnabled(true);
					downVote.setEnabled(true);
					tempDisLikeList.get(position).remove(PreferenceSettings.getMobileNo());
					updateDislike(false,position,points);
				}
				else
				{
					upVote.setImageResource(R.drawable.up1);
					downVote.setImageResource(R.drawable.down2);
					downVote.setEnabled(true);
					upVote.setEnabled(false);
					tempDisLikeList.get(position).add(PreferenceSettings.getMobileNo());
					updateDislike(true,position,points);
				}
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
				if(list.get(position).getList(Constants.FLAG_ARRAY).contains(PreferenceSettings.getMobileNo()))
				{
					//viewHolder.flag.setEnabled(false);
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.flag_already_done));
				}
				else
				{
					if(list.get(position).getObjectId()!=null)
						showFlagDialog(position);
				}
			}
			else
				Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				
			}
		});
		viewHolder.postText.setLinksClickable(false);
		viewHolder.postText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link"))
				{
					activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				{
					
				}
				else
				{
					callCommentActivity(position);
				}
					
			}
		});
		
		viewHolder.linkUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link")
						|| list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				{
					activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.CREDIT_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			}
		});
		
		viewHolder.linkDes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link"))
				{
					activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				/*else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.CREDIT_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
*/			}
		});
		
		
		viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("Link"))
				{
					activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Video"))
				{
					System.out.println("video id ::: "+list.get(position).get(Constants.VIDEO_ID).toString());
					if(list.get(position).getString(Constants.SITE_NAME).equals("YouTube"))
					{
					activity.startActivity(new Intent(activity,YoutubePlayerActivity.class).putExtra("title", list.get(position).getString(Constants.IMAGE_CAPTION)).putExtra(Constants.VIDEO_ID, list.get(position).get(Constants.VIDEO_ID).toString()));
					//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
					else
					{
						activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
						//activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
					}
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
					callCommentActivity(position);
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("Image"))
				{
					callCommentActivity(position);
				}
				else if(list.get(position).get(Constants.POST_TYPE).toString().equals("SVideo"))
				{
						isScrolled=false;
						isFileDownloading=true;
						ParseFile file=(ParseFile) list.get(position).get(Constants.POST_VIDEO);
						playImage.setVisibility(View.GONE);
						videoProgress.setVisibility(View.VISIBLE);
						//videoProgress.setp
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
									//byte[] videoByte= list.get(position).getParseFile(Constants.POST_VIDEO).getData();
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
								//	return false;
								}
							    System.out.println("isScrolled :: "+isScrolled);
							    if(!isScrolled)
							    activity.startActivity(new Intent(activity,VideoViewActivity.class).putExtra("video", uriSting));
							
						}
					}, new ProgressCallback() {
						@Override
						public void done(Integer progress) {
							videoProgress.setProgress(progress);
						}
					});
				}
				
			}
		});
		
		viewHolder.sVideoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(list.get(position).get(Constants.POST_TYPE).toString().equals("GIFVideo"))
				{
					 if(videoView.isPlaying())
					    {
						 isFileDownloading=false;
					    	sVideoPlayImage.setVisibility(View.VISIBLE);
					    	//sVideoPlayImage.setImageResource(R.drawable.play_image);
					    	//videoStopPosition=videoView.getCurrentPosition();
					    	videoView.stopPlayback();
					    }
					    else
					    {
					    	/*if(isFileDownloading)
					    	{
					    		Toast.makeText(activity, "Please wait until download complete", Toast.LENGTH_LONG).show();
					    	}
					    	else
					    	{
					    	System.out.println("play click");
					    	sVideoPlayImage.setVisibility(View.GONE);
					    	sVideoProgress.setVisibility(View.VISIBLE);
					    	 
					    	 new VideoDownloadTask(position,sVideoProgress,videoView,sVideoBg,sVideoPlayImage).execute();	
					    	}*/
					    	
					    	isScrolled=false;
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
								    if(!isScrolled)  
								    {
								    sVideoPlayImage.setVisibility(View.GONE);
									videoView.setVisibility(View.VISIBLE);
							    	videoView.setVideoPath(uriSting);
								    videoView.requestFocus();
								   System.out.println("before start play video");
								    videoView.start();
								    
								}
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
		
				
		viewHolder.stypeAbstract.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			   if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
				   callCommentActivity(position);
				}
			}
		});
		
		viewHolder.sourceUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			   if(list.get(position).get(Constants.POST_TYPE).toString().equals("Stype"))
				{
				   activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", list.get(position).get(Constants.LINK_URL).toString()));
				}
			}
		});
		
		
		
		viewHolder.replies.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callCommentActivity(position);
			}
		});
		
		} // end of else
	}//end of else
	return view;
}
	
	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2,final String url) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
	    ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
            	activity.startActivity(new Intent(activity,WebViewActivity.class).putExtra("title", "Chatterati").putExtra("url", url));
				activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
            }
        }, idx1, idx2, 0);
  	    return ssb;
	}
	
	
	public static String extractYTId(String ytUrl) {
	    String vId = null;
	    Pattern pattern = Pattern.compile(
	                     "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$", 
	                     Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(ytUrl);
	    if (matcher.matches()){
	        vId = matcher.group(1);
	    }
	    return vId;
	}
	
	public  void showDeleteAlertDialog(final int position){
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
	
	
	
	public void showManageHashTagDialog(List<ParseObject> tagList,final String feedId,final List<String> alreadySelectedTag)
    {
		System.out.println("already seclted tag ::: "+alreadySelectedTag);
    	mDialog = new Dialog(activity,R.style.customDialogStyle);
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
		adapter=new FeedHashTagAdapter(activity, tagList,alreadySelectedTag);
		listview.setAdapter(adapter);
		
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
									final ArrayList<String> selectedTagList=(ArrayList<String>) adapter.getSelectedList();
									//selectedTagList.addAll(alreadySelectedTag);
									System.out.println("selected tags before updated feed table "+selectedTagList);
									object.put(Constants.HASH_TAG_ARRAY, selectedTagList);
									
									object.saveInBackground(new SaveCallback() {
										
										@Override
										public void done(ParseException arg0) {
											object.pinInBackground(mGroupId);
											Utility.showToastMessage(activity, activity.getResources().getString(R.string.tag_post_success));
											progressBar.setVisibility(View.GONE);
											
											final List<String> newlySelectedTag=selectedTagList;
											System.out.println("newly selected tag ::: "+newlySelectedTag);
											List<String> unSelectedTag=new ArrayList<String>();
											for(int k=0;k<alreadySelectedTag.size();k++)
											{
												if(newlySelectedTag.contains(alreadySelectedTag.get(k)))
													newlySelectedTag.remove(alreadySelectedTag.get(k));
												else
													unSelectedTag.add(alreadySelectedTag.get(k));
											}
											System.out.println("before loop ::: "+newlySelectedTag);
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
								}
								else
									progressBar.setVisibility(View.GONE);
							}
							else
								progressBar.setVisibility(View.GONE);
					}});
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
	
	
	private void callCommentActivity(int position)
	{
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

	protected class ViewHolder
	{
		protected TextView userName,postText,updatedTime,replies,joinRequest,imageCaption,comment_count;//points
		protected TextView name,time,additional_info,linkDes,linkUrl,stypeAbstract,hashTag,sourceUrl;
		protected ImageView flag,delete,sVideoPlayImage;//playImage,shareImage;
		protected ParseImageView userImage;
		//final ImageView acceptBtn,rejectBtn;
		protected ResizableImageView postImage;
		protected View line;
		//protected YouTubePlayerView youTubeView;
		protected FrameLayout upVoteFrame,downVoteFrame,postImageFrame;
		protected RelativeLayout sVideoLayout;
		//protected VideoView videoView;
//		/protected Button acceptBtn,rejectBtn;
		//protected ProgressBar videoProgress;
		
		protected TextView eventStartEndDate,eventStartEndTime,eventLocationAddress1,eventLocationAddress2;
		protected TextView eventShortMonth,eventShortDate,eventAreaName;
		protected RelativeLayout eventLocationLayout,eventDateLayout;
		
	}
	
	private void updateLike(final boolean isLike,final int position,final TextView points)
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
							                	 //points.setText(object.getInt(Constants.POST_POINT)+" Points");
							                	 points.setText(object.getInt(Constants.POST_POINT)+"");
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
						
						
						/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
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
			 							                	notifyDataSetChanged();*/
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
			 							                		/*   	}	//}
		      							          }
												});
										
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
							                	// points.setText(object.getInt(Constants.POST_POINT)+" Points");
							                	 points.setText(object.getInt(Constants.POST_POINT)+"");
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
		query1.whereEqualTo(Constants.GROUP_ID, list.get(position).get(Constants.GROUP_ID).toString());
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
						
						
						/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
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
			 							                	notifyDataSetChanged();*/
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
			 							  /*              	}	//}
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
	}

	public class VideoDownloadTask1 extends AsyncTask<Void, Integer, Boolean>
	{
		int position=0;
		String filePath;
		ProgressBar sVideoProgress;
		public VideoDownloadTask1(int position,ProgressBar progressBar)
		{
			this.sVideoProgress=progressBar;
			this.position=position;
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
			
			progressBar.setVisibility(View.GONE);
			if(result)
			{
				activity.startActivity(new Intent(activity,VideoViewActivity.class).putExtra("video", filePath));
			}
			else
			{
				
			}
		}
		
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
