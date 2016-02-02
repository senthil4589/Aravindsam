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
import com.group.nearme.util.Utility;
import com.parse.ParseObject;

public class FeedHashTagAdapter extends BaseAdapter {

	Activity activity;
	List<ParseObject> list;
	LayoutInflater inflater;
	public final ArrayList<String> selectedTagIdList=new ArrayList<String>();
	ArrayList<ParseObject> selectedTagList=new ArrayList<ParseObject>();
	List<String> alreadySelectedTag;
	HashMap<String, Boolean> checkedMap=new HashMap<String, Boolean>();
	public FeedHashTagAdapter(Activity activity,List<ParseObject> list, List<String> alreadySelectedTag) {
		this.activity = activity;
		this.list = list;
		this.alreadySelectedTag=alreadySelectedTag;
		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(int i=0;i<list.size();i++)
		{
			checkedMap.put(list.get(i).getObjectId(), false);
		}
		for(int j=0;j<alreadySelectedTag.size();j++)
		{
			selectedTagIdList.add(alreadySelectedTag.get(j));
			if(checkedMap.containsKey(alreadySelectedTag.get(j)))
				checkedMap.put(alreadySelectedTag.get(j), true);
		}
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RecordHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.hash_tag_list_item, parent, false);
			holder = new RecordHolder();
			holder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
			
			convertView.setTag(holder);
		} else {
			holder = (RecordHolder) convertView.getTag();
		}
		 
		final CheckBox checkBox=(CheckBox) convertView.findViewById(R.id.check_box);
		holder.tagName.setText(list.get(position).getString(Constants.TAG_NAME));
		checkBox.setOnCheckedChangeListener(null);
		
			checkBox.setChecked(checkedMap.get(list.get(position).getObjectId()));
			/*if(checkedMap.get(list.get(position).getObjectId()) && !selectedTagIdList.contains(list.get(position).getObjectId()))
			{
				selectedTagIdList.add(list.get(position).getObjectId());
			}*/
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkedMap.put(list.get(position).getObjectId(),isChecked);
				if(isChecked)
				{
					selectedTagIdList.add(list.get(position).getObjectId());
					selectedTagList.add(list.get(position));
					//alreadySelectedTag.add(list.get(position).getObjectId());
				
				}
				else
				{
					//alreadySelectedTag.add(list.get(position).getObjectId());
					selectedTagIdList.remove(list.get(position).getObjectId());
					selectedTagList.add(list.get(position));
				}
				//ArrayList<String> 
				//Utility.setSelectedTagList(alreadySelectedTag);
			}
		});
		
		return convertView;
	}

	protected class RecordHolder {
		TextView tagName;
        //CheckBox checkBox;
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
	
	public List<String> getSelectedList()
	{
		ArrayList<String> list1=new ArrayList<String>();
		for(int i=0;i<list.size();i++)
		{
			if(checkedMap.get(list.get(i).getObjectId()))
				list1.add(list.get(i).getObjectId());
		}
		return list1;
	}

	public ArrayList<ParseObject> getSelectedTagList()
	{
		return selectedTagList;
	}
}
