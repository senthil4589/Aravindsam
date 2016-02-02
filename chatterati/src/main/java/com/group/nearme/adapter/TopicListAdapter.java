package com.group.nearme.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.group.nearme.R;
import com.group.nearme.util.Constants;
import com.parse.ParseObject;

public class TopicListAdapter extends BaseAdapter
{
	Activity activity;
	List<ParseObject> list;
	LayoutInflater inflater;
	boolean isTopic;
	static boolean expand;
	public TopicListAdapter(Activity activity,List<ParseObject> list,boolean istopic) {
		this.activity = activity;
		this.list = list;
		this.isTopic=istopic;
		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RecordHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.topic_list_item, parent, false);
			holder = new RecordHolder();
			holder.topicName = (TextView) convertView.findViewById(R.id.topic_name);
			holder.topicPostCount = (TextView) convertView.findViewById(R.id.topic_post_count);
			//holder.topicImage=(ImageView) convertView.findViewById(R.id.topic_image);
			holder.expandCollapse=(ImageView) convertView.findViewById(R.id.expand_collapse);
			holder.root=(RelativeLayout) convertView.findViewById(R.id.root);
			convertView.setTag(holder);
		} else {
			holder = (RecordHolder) convertView.getTag();
		}
		/*if(position==0)
		{
			holder.topicName.setText("# My Posts");
		}
		else if(position==1)
		{
			holder.topicName.setText("# Pending Posts");
		}
		else if(position==2)
		{
			holder.topicName.setText("# Group Rules");
		}
		else
		{
			holder.topicName.setText(list.get(position-3).getString(Constants.TAG_NAME));
		}
		*/
		if(!expand)
		{
		if(position==3 && list.size() >4)
		{
			holder.expandCollapse.setImageResource(R.drawable.down_arrow2);
			holder.expandCollapse.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.expandCollapse.setVisibility(View.GONE);
		}
		}
		else
		{
			if(position==3 && list.size()> 4)
			{
				holder.expandCollapse.setImageResource(R.drawable.up_arrow1);
				holder.expandCollapse.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.expandCollapse.setVisibility(View.GONE);
			}
		}
		
		/*if(expand)
		{
			holder.root.setVisibility(View.VISIBLE);
		}
		else
		{
		if(list.size() > 4 && position >3)
		{
			
			holder.root.setVisibility(View.GONE);
		}
		else
		{
			holder.root.setVisibility(View.VISIBLE);
		}
		}
		*/
		holder.expandCollapse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(expand)
				{
					expand=false;
					notifyDataSetChanged();
				}
				else
				{
					expand=true;
					notifyDataSetChanged();
				}
				
			}
		});
		
		if(isTopic)
		{
		//holder.topicName.setText("# "+list.get(position).getString(Constants.TAG_NAME));
		//holder.topicPostCount.setText(list.get(position).getInt(Constants.TAG_POST_COUNT)+" post");
		//String imageUrl=list.get(position).getParseFile(Constants.TAG_IMAGE).getUrl();
		//Picasso.with(activity).load(imageUrl).into(holder.topicImage);
		
		Spannable word = new SpannableString("# "+list.get(position).getString(Constants.TAG_NAME));        
		word.setSpan(new RelativeSizeSpan(1f), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
		 word.setSpan(new ForegroundColorSpan(Color.BLACK), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		 holder.topicName.setText(word);
		 if(list.get(position).getInt(Constants.TAG_POST_COUNT)>0)
		 {
			 String post=" post";
			 if(list.get(position).getInt(Constants.TAG_POST_COUNT)>1)
			 {
				 post=" posts";
			 }
				 Spannable wordTwo = new SpannableString("  "+list.get(position).getInt(Constants.TAG_POST_COUNT)+post);        
				 wordTwo.setSpan(new RelativeSizeSpan(0.9f),  0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
				 wordTwo.setSpan(new ForegroundColorSpan(Color.GRAY), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				 holder.topicName.append(wordTwo);
		 }
		}
		else
		{
			//holder.topicName.setText("# "+list.get(position).getString(Constants.SUB_TAG_NAME));
			//holder.topicPostCount.setText(list.get(position).getInt(Constants.SUB_TAG_POST_COUNT)+" post");
			Spannable word = new SpannableString("# "+list.get(position).getString(Constants.SUB_TAG_NAME));        
			word.setSpan(new RelativeSizeSpan(1f), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
			 word.setSpan(new ForegroundColorSpan(Color.BLACK), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			 holder.topicName.setText(word);
			 if(list.get(position).getInt(Constants.SUB_TAG_POST_COUNT)>0)
			 {
				 String post=" post";
				 if(list.get(position).getInt(Constants.SUB_TAG_POST_COUNT)>1)
				 {
					 post=" posts";
				 }
					 Spannable wordTwo = new SpannableString("  "+list.get(position).getInt(Constants.SUB_TAG_POST_COUNT)+post);        
					 wordTwo.setSpan(new RelativeSizeSpan(0.9f),  0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
					 wordTwo.setSpan(new ForegroundColorSpan(Color.GRAY), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					 holder.topicName.append(wordTwo);
			 }

		}
		return convertView;
	}

	private class RecordHolder {
		TextView topicName,topicPostCount;
		ImageView expandCollapse;
		RelativeLayout root;
	}

	@Override
	public int getCount() {
		if(!expand)
		{
			if(list.size() >4)
				return 4;
			else
				return list.size();
		}
		else
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
	
}
