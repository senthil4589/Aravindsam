package com.group.nearme.util;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class SqureLayout extends RelativeLayout {

	public SqureLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		
	}
	@Override
	  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    int width = getMeasuredWidth();
	    setMeasuredDimension(width, width);
	  }


}
