package com.group.nearme.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.group.nearme.R;
import com.group.nearme.util.Constants;
import com.parse.ParseObject;

public class HashTagAdapter extends BaseAdapter {

	Activity activity;
	List<ParseObject> list;
	LayoutInflater inflater;
	ArrayList<String> selectedList=new ArrayList<String>();
	HashMap<String, Boolean> checkedMap=new HashMap<String, Boolean>();
	ArrayList<String> selectedTagIdList=new ArrayList<String>();
	public HashTagAdapter(Activity activity,List<ParseObject> list) {
		this.activity = activity;
		this.list = list;
		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(int i=0;i<this.list.size();i++)
		{
			checkedMap.put(list.get(i).getObjectId(), false);
		}
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RecordHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.hash_tag_list_item, parent, false);
			holder = new RecordHolder();
			holder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
			holder.checkBox=(CheckBox) convertView.findViewById(R.id.check_box);
			convertView.setTag(holder);
		} else {
			holder = (RecordHolder) convertView.getTag();
		}
		//final CheckBox checkBox=(CheckBox) convertView.findViewById(R.id.check_box);
		if(position==0)
		{
			holder.tagName.setText("# My Posts");
			holder.checkBox.setVisibility(View.GONE);
		}
		else if(position==1)
		{
			holder.tagName.setText("# Pending Posts");
			holder.checkBox.setVisibility(View.GONE);
		}
		else if(position==2)
		{
			holder.tagName.setText("# Group Rules");
			holder.checkBox.setVisibility(View.GONE);
		}
		else
		{
			holder.checkBox.setVisibility(View.VISIBLE);
			holder.tagName.setText(list.get(position-3).getString(Constants.TAG_NAME));
			holder.checkBox.setOnCheckedChangeListener(null);
			holder.checkBox.setChecked(checkedMap.get(list.get(position-3).getObjectId()));
		
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checkedMap.put(list.get(position-3).getObjectId(),isChecked);
					if(isChecked)
					{
						selectedList.add(list.get(position-3).getString(Constants.TAG_NAME));
						selectedTagIdList.add(list.get(position-3).getObjectId());
					}
					else
					{
						selectedList.remove(list.get(position-3).getString(Constants.TAG_NAME));
						selectedTagIdList.remove(list.get(position-3).getObjectId());
					}
				}
			});
		}
		
		return convertView;
	}

	private class RecordHolder {
		TextView tagName;
		CheckBox checkBox;
	}

	@Override
	public int getCount() {
			return list.size()+3;
	}
	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	public ArrayList<String> getSelectedList()
	{
		return selectedList;
	}
	public ArrayList<String> getSelectedTagIDList()
	{
		return selectedTagIdList;
	}
}
