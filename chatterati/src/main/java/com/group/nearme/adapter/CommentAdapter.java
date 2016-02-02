package com.group.nearme.adapter;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.group.nearme.MemberProfileActivity;
import com.group.nearme.R;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

public class CommentAdapter extends BaseAdapter{
	ViewHolder viewHolder;
	LayoutInflater inflater;
	Activity activity;
	List<ParseObject> commentList;
	String groupType="";
	AlertDialog mAlertDialog;
	public CommentAdapter(Activity activity,List<ParseObject> list,String groupType)
	{
		this.activity=activity;
		this.commentList=list;
	//	this.userList=list1;
		this.groupType=groupType;
		inflater = activity.getLayoutInflater();
	}
	
	@Override
	public int getCount() {
		return commentList.size();
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
		if (view == null) {
			view = inflater.inflate(R.layout.comment_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.userName=(TextView) view.findViewById(R.id.user_name);
			viewHolder.userImage=(ParseImageView) view.findViewById(R.id.user_pic);
			viewHolder.postText=(TextView) view.findViewById(R.id.comment_text);
			viewHolder.updatedTime=(TextView) view.findViewById(R.id.updated_time);
			viewHolder.layout=(RelativeLayout) view.findViewById(R.id.layout);
			viewHolder.delete=(ImageView) view.findViewById(R.id.delete);
			view.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		if(groupType.equals("Public"))
		{
			viewHolder.userName.setVisibility(View.GONE);
			viewHolder.userImage.setVisibility(View.GONE);
			viewHolder.postText.setPadding(0, 50, 0, 0);
		}
		else
		{
			viewHolder.userName.setVisibility(View.VISIBLE);
			viewHolder.userImage.setVisibility(View.VISIBLE);
			viewHolder.postText.setPadding(0, 0, 0, 0);
			ParseObject userObject = commentList.get(position).getParseObject(Constants.USER_ID);
			
			if( userObject!=null ){
			viewHolder.userImage.setParseFile(commentList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
			viewHolder.userImage.loadInBackground();
			//Picasso.with(activity).load(list.get(position).getParseFile(Constants.MEMBER_IMAGE).getUrl()).into(viewHolder.userImage);
			viewHolder.userName.setText(commentList.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME));
			}
				
			viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Utility.setParseFile(commentList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE));
					activity.startActivity(new Intent(activity,MemberProfileActivity.class)
					.putExtra("isFromFeed", true)
					.putExtra(Constants.USER_NAME, commentList.get(position).getParseObject(Constants.USER_ID).getString(Constants.USER_NAME))
					.putExtra(Constants.MOBILE_NO, commentList.get(position).get(Constants.MOBILE_NO).toString())
					.putExtra(Constants.PROFILE_PICTURE, commentList.get(position).getParseObject(Constants.USER_ID).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()));
					activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			});
		}
		
		
		viewHolder.postText.setText(commentList.get(position).get(Constants.COMMENT_TEXT).toString());
		//viewHolder.userName.setText(commentList.get(position).get(Constants.USER_NAME).toString());
		
		final java.util.Date f=commentList.get(position).getCreatedAt();
		if(f!=null)
		viewHolder.updatedTime.setText(Utility.getTimeAgo(f.getTime()));
		
		if(Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()) 
				|| commentList.get(position).getString(Constants.MOBILE_NO).equals(PreferenceSettings.getMobileNo()))
		{
			viewHolder.delete.setVisibility(View.VISIBLE);
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utility.checkInternetConnectivity(activity))
					{	
						if(commentList.get(position).getObjectId()!=null)
							showDeleteAlertDialog(position);
				}
				else
					Utility.showToastMessage(activity, activity.getResources().getString(R.string.no_network));
				}
				
			});
		}
		else
		{
			viewHolder.delete.setVisibility(View.GONE);
		}
		
		
		
		return view;
	}

	protected class ViewHolder
	{
		protected TextView userName,postText,updatedTime;
		ParseImageView userImage;
		RelativeLayout layout;
		ImageView delete;
		
	}
	
	public void setCommentList(List<ParseObject> commentList1)
	{
		this.commentList=commentList1;
	}
	
	public  void showDeleteAlertDialog(final int position){
		mAlertDialog=new AlertDialog.Builder(activity)
		.setMessage("Are you sure you want to delete this post?")
		.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utility.showToastMessage(activity, activity.getResources().getString(R.string.post_delete));
				final ParseObject object=commentList.get(position);
				
				commentList.remove(position);
				notifyDataSetChanged();
				
				
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
				query.whereEqualTo(Constants.OBJECT_ID, object.getString(Constants.FEED_ID));
				query.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object1, ParseException e) {
							if (e == null) 
							{
								if(object1!=null)
								{
									object1.increment(Constants.COMMENT_COUNT,-1);
									object1.increment(Constants.POST_POINT,-100);
									object1.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
									object1.saveInBackground();
								}
							}
					}
				});
				
				ParseQuery<ParseObject> query1= ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
				query1.whereEqualTo(Constants.OBJECT_ID, object.getObjectId());
				query1.getFirstInBackground(new GetCallback<ParseObject>() {
					public void done(ParseObject object, ParseException e) {
						object.deleteInBackground();
					}
				});
				
			}
		}).setNegativeButton("No", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mAlertDialog.dismiss();
			}
		}).show();
	}
	
}
