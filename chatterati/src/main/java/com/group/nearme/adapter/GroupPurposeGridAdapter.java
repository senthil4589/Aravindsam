package com.group.nearme.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.group.nearme.R;
import com.group.nearme.settings.GroupNearMeApplication;

public class GroupPurposeGridAdapter extends BaseAdapter{
	Activity activity;
	//List<ParseObject> list;
	LayoutInflater inflater;
	ImageLoader imageLoader;
	ArrayList<String> purposeList,colorList;
	ArrayList<Integer> imageList;
	public GroupPurposeGridAdapter(Activity activity,ArrayList<String> purposelist,ArrayList<String> colorlist,ArrayList<Integer> imagelist) {
		this.activity = activity;
		this.purposeList = purposelist;
		this.colorList=colorlist;
		this.imageList=imagelist;
		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RecordHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.group_purpose_grid_item, null);
			holder = new RecordHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			holder.purposeTxt=(TextView) convertView.findViewById(R.id.text);
			
			//GridView.LayoutParams param=new GridView.LayoutParams(width,width);
			//GridView.LayoutParams param1=new GridView.LayoutParams(width,width);
			convertView.setTag(holder);
		} else {
			holder = (RecordHolder) convertView.getTag();
		}
		
		System.out.println("before settext");
		holder.purposeTxt.setText(purposeList.get(position));
		holder.purposeTxt.setBackgroundColor(Color.parseColor(colorList.get(position)));
	holder.imageView.setImageResource(imageList.get(position));
	System.out.println("w & H "+convertView.getWidth()+"  "+convertView.getHeight());
		//if(list.get(position).getParseFile(Constants.GROUP_PURPOSE_THUMBNAIL_IMAGE)!=null)
		//Picasso.with(activity).load(list.get(position).getParseFile(Constants.GROUP_PURPOSE_THUMBNAIL_IMAGE).getUrl()).placeholder(R.drawable.group_image).into(holder.imageView);
		return convertView;
	}

	protected class RecordHolder {
		ImageView imageView;
		TextView purposeTxt;
	}

	@Override
	public int getCount() {
		System.out.println("inside getcount :: "+purposeList.size());
			return purposeList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}