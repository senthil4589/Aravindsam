package com.group.nearme.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.group.nearme.MediaPostActivity;
import com.group.nearme.R;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.util.Constants;
import com.group.nearme.util.SquareImageView;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

public class MediaGridAdapter extends BaseAdapter {
	MediaPostActivity activity;
	List<ParseObject> list;
	LayoutInflater inflater;
	ImageLoader imageLoader;
	public MediaGridAdapter(MediaPostActivity activity,List<ParseObject> list) {
		this.activity = activity;
		this.list = list;
		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = GroupNearMeApplication.getInstance().getImageLoader();
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder = null;
		if (row == null) {
			row = inflater.inflate(R.layout.grid_item, parent, false);
			holder = new RecordHolder();
			holder.imageView = (ParseImageView) row.findViewById(R.id.image);
			holder.playImage = (ImageView) row.findViewById(R.id.play_image);
			holder.playImageFrame = (FrameLayout) row.findViewById(R.id.video_play_frame);
			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}
		
		if(position==list.size()-1)
		{
			activity.getNext100Records();
		}
		
		/*holder.imageView.setPlaceholder(activity.getResources().getDrawable(R.drawable.group_image));
		holder.imageView.setParseFile(list.get(position).getParseFile(Constants.POST_IMAGE));
		holder.imageView.loadInBackground(new GetDataCallback() {
		   @Override
		   public void done(byte[] data, ParseException e) {
		   }
		 });*/
		
		//holder.imageView.setImageUrl(list.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl(), imageLoader);
		//imageLoader.get((list.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()), ImageLoader.getImageListener(
			//	holder.imageView, R.drawable.group_image, 0));
		String postType=list.get(position).getString(Constants.POST_TYPE);
		if(postType.equals("GIFVideo"))
		{
			holder.playImageFrame.setVisibility(View.VISIBLE);
			holder.playImage.setImageResource(R.drawable.gif_play);
		}
		else if(postType.equals("Video") || postType.equals("SVideo"))
		{
			holder.playImageFrame.setVisibility(View.VISIBLE);
			holder.playImage.setImageResource(R.drawable.play_image);
		}
		else
		{
			holder.playImageFrame.setVisibility(View.GONE);
		}
		if(list.get(position).getParseFile(Constants.THUMBNAIL_PICTURE)!=null)
		Picasso.with(activity).load(list.get(position).getParseFile(Constants.THUMBNAIL_PICTURE).getUrl()).placeholder(R.drawable.group_image).into(holder.imageView);
		return row;
	}

	protected class RecordHolder {
		ParseImageView imageView;
		FrameLayout playImageFrame;
		ImageView playImage;
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
	
	public void setList(List<ParseObject> mediaList) {
		this.list=mediaList;
	}
}