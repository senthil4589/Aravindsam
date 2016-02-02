package com.group.nearme.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.group.nearme.R;
import com.group.nearme.util.Constants;
import com.parse.ParseObject;

public class HashTagMemberAdapter extends BaseAdapter {

	Activity activity;
	List<ParseObject> list;
	LayoutInflater inflater;
	public HashTagMemberAdapter(Activity activity,List<ParseObject> list) {
		this.activity = activity;
		this.list = list;
		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RecordHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.hashtag_member_view, parent, false);
			holder = new RecordHolder();
			holder.tagName = (TextView) convertView.findViewById(R.id.tag);
			convertView.setTag(holder);
		} else {
			holder = (RecordHolder) convertView.getTag();
		}
		if(position==0)
		{
			holder.tagName.setText("# My Posts");
		}
		else if(position==1)
		{
			holder.tagName.setText("# Group Rules");
		}
	
		else
		{
			holder.tagName.setText("# "+list.get(position-2).getString(Constants.TAG_NAME));
		}
		
		return convertView;
	}

	private class RecordHolder {
		TextView tagName;
	}

	@Override
	public int getCount() {
			return list.size()+2;
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
