package com.group.nearme.util;

import android.content.Context;
import android.widget.VideoView;

public class MyVideoView extends VideoView {
	 public MyVideoView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	  //int width = getDefaultSize(0, widthMeasureSpec);
	  int height = getDefaultSize(0, heightMeasureSpec);

	  setMeasuredDimension(widthMeasureSpec, height);
	 }
	}