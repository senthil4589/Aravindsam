package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.group.nearme.R;
import com.group.nearme.util.Constants;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

public class ShareInAdapter extends BaseAdapter {

	Activity activity;
	LayoutInflater inflater;
	List<ParseObject> list;
	ViewHolder holder;
	public ShareInAdapter(Activity activity1,List<ParseObject> list1)
	{
		this.activity=activity1;
		this.list=list1;
		inflater = activity.getLayoutInflater();
		
	}
	
	@Override
	public int getCount() {
		return list.size();
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
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.share_in_list_item, null);
			holder = new ViewHolder();
			holder.groupName=(TextView) convertView.findViewById(R.id.group_name);
			holder.groupImage=(ImageView) convertView.findViewById(R.id.group_image);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.groupName.setText(list.get(position).get(Constants.GROUP_NAME).toString());
		if(list.get(position).getParseFile(Constants.THUMBNAIL_PICTURE)!=null)
			Picasso.with(activity).load(list.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).placeholder(R.drawable.group_image).into(holder.groupImage);		
		
		return convertView;
	}
	
	public class ViewHolder
	{
		TextView groupName;
		ImageView groupImage;
	}

}
