package com.group.nearme.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.group.nearme.MemberListActivity;
import com.group.nearme.MyProfleActivity;
import com.group.nearme.R;
import com.group.nearme.TabGroupActivity;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.CircleTransform;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.ParseImageView;
import com.squareup.picasso.Picasso;

public class MenuAdapter extends BaseAdapter
{
	ViewHolder viewHolder;
	LayoutInflater inflater;
	Activity activity;
	String [] itemArray;
	ImageLoader imageLoader;
	int imagRes[]={R.drawable.profile_pic,R.drawable.new_my_profile,R.drawable.new_pending_invites,R.drawable.new_settings};
	public MenuAdapter(Activity activity,String [] itemArray)
	{
		this.activity=activity;
		this.itemArray=itemArray;
		inflater = activity.getLayoutInflater();
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemArray.length;
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

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(position == 0)
		{
			view = inflater.inflate(R.layout.user_menu, null);
			ParseImageView userImage=(ParseImageView) view.findViewById(R.id.user_image);
			EditText userName=(EditText) view.findViewById(R.id.user_name);
			//TextView edit=(TextView) view.findViewById(R.id.edit);
		
			//userImage.setImageUrl(PreferenceSettings.getProfilePic(), imageLoader);
			//imageLoader.get((PreferenceSettings.getProfilePic()), ImageLoader.getImageListener(
			//		userImage, R.drawable.app_icon, R.drawable.group_image));
			userName.setText(PreferenceSettings.getUserName());
			
			/*edit.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					activity.startActivity(new Intent(activity,MyProfleActivity.class));
					activity.overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
				}
			});
			*///.transform(new CircleTransform())
			//Picasso.with(activity).load(PreferenceSettings.getProfilePic()).placeholder(R.drawable.group_image).into(userImage);
			userImage.setParseFile(Utility.getUserImageFile());
			userImage.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
			userImage.loadInBackground();
		}
		/*else if(position == itemArray.length)
		{
			view = inflater.inflate(R.layout.version, null);
		}*/
		else
		{
			view = inflater.inflate(R.layout.menu_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.menuTxtView=(TextView) view.findViewById(R.id.text);
			viewHolder.line=(View) view.findViewById(R.id.line);
			viewHolder.menuImgView=(ImageView) view.findViewById(R.id.image);
			viewHolder.menuTxtView.setText(itemArray[position]);
			
			if(itemArray[position].equals("Terms of Service") || itemArray[position].equals("Privacy Policy") || 
					itemArray[position].equals("Policies and Guidelines") || itemArray[position].equals("Contact Us"))
			{
				System.out.println("inside if  ..");
				viewHolder.menuImgView.setVisibility(View.GONE);
				viewHolder.menuTxtView.setTextColor(Color.parseColor("#a3a3a3"));
			}
			else
			{
				viewHolder.menuImgView.setVisibility(View.VISIBLE);
				viewHolder.menuImgView.setImageResource(imagRes[position]);
				viewHolder.menuTxtView.setTextColor(Color.parseColor("#000000"));
			}
			/*if(itemArray[position].equals("Home"))
			{
				viewHolder.line.setVisibility(View.VISIBLE);
				viewHolder.line.setPadding(0, 0, 0, 0);
			}*/
			if(itemArray[position].equals("Settings"))
			{
				viewHolder.line.setVisibility(View.VISIBLE);
				viewHolder.line.setPadding(0, 5, 0, 0);
			}
			else 
			{
				viewHolder.line.setVisibility(View.GONE);
			}
			
		
		}
		return view;
	}

	protected class ViewHolder
	{
		protected TextView menuTxtView;
		protected ImageView menuImgView;
		protected View line;
	}
}
