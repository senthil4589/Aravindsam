package com.group.nearme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;

public class EventLocationActivity extends Activity{
	private ImageView mBackImg;
	private GoogleMap googleMap;
	private MapFragment mMapFragment;
	private Double mLatitude,mLongtitude;
	private String eventNAME="";
	private TextView titleTxt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.event_location);
		Utility.getTracker(this, "EVENT LOCATION SCREEN");
		initViews();
	}

	private void initViews() {
		
		mBackImg=(ImageView) findViewById(R.id.back);
		titleTxt=(TextView) findViewById(R.id.title);
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			finish();
				//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		
		mLatitude=getIntent().getDoubleExtra(Constants.LATITUDE, 0);
		mLongtitude=getIntent().getDoubleExtra(Constants.LONGTITUDE, 0);
		
		String title=getIntent().getStringExtra("event_name");
		titleTxt.setText(title);
		
		LatLng mLatLng=new LatLng(mLatitude, mLongtitude);
		
		mMapFragment =
	               (MapFragment) getFragmentManager().findFragmentById(R.id.map);
	       googleMap = mMapFragment.getMap();
	       googleMap.setMyLocationEnabled(false);
	    	   googleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
	    	  // marker= googleMap.addMarker(
	    			  // new MarkerOptions().position(mLatLng));
	    	   googleMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15.5));
	    	   googleMap.getUiSettings().setZoomControlsEnabled(true);
	    	   googleMap.getUiSettings().setAllGesturesEnabled(false);
	}
}
