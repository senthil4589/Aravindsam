package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.model.Circle;
import com.group.nearme.image.Crop;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CreatePublicGroupActivity extends Activity{
	private ImageView mBackImg,mGroupImage,placeHolder;//,transparentImageView,uploadImag;
	private Button mNext;
	private EditText mGroupNameEditTxt,mGroupDes;
	private GPSTracker gpsTracker;
	private ProgressBar mProgressBar;
	Circle mapCircle;
	public static Activity activity;
	ScrollView scrollView;
	String mGroupName="",groupDes="";
	public static ParseFile mGroupImageFile=null,mGroupImageTumbnailFile=null;
	public static Dialog mDialog;
	private FrameLayout uploadLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_public_group);
		Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
		
		activity=this;
		
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGroupNameEditTxt=(EditText) findViewById(R.id.group_name);
		mGroupDes=(EditText) findViewById(R.id.group_des_box);
		mNext=(Button) findViewById(R.id.next);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		gpsTracker=new GPSTracker(this);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNext.setTypeface(tf);
		scrollView=(ScrollView) findViewById(R.id.scroll_view);
		
		mGroupImage=(ImageView) findViewById(R.id.upload_profile_pic);
		uploadLayout=(FrameLayout) findViewById(R.id.image_root_layout);
		placeHolder=(ImageView) findViewById(R.id.placeholder);
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		
		mNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkDuplicate();
				
			}
		});
		
		
		
		uploadLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog = new Dialog(CreatePublicGroupActivity.this,R.style.customDialogStyle);
		    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				mDialog.setContentView(R.layout.choose_picture);
				mDialog.setCancelable(true);
				mDialog.setCanceledOnTouchOutside(true);
				mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
				WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
				windowManager.gravity = Gravity.CENTER;
				TextView cameraImage=(TextView) mDialog.findViewById(R.id.take_photo);
				TextView galleryImage=(TextView) mDialog.findViewById(R.id.from_gallery);
				cameraImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						Utility.callCameraIntent(CreatePublicGroupActivity.this);
					}
				});
				galleryImage.setOnClickListener(new View.OnClickListener() {
					@SuppressLint({ "InlinedApi", "NewApi" })
					@Override
					public void onClick(View v) {
						  mDialog.dismiss();
						  Crop.pickImage(CreatePublicGroupActivity.this);
					}
				});
				mDialog.show();
			}
		});
		
	}
	@SuppressLint("NewApi")
	private void checkDuplicate()
	{
		mGroupName=mGroupNameEditTxt.getText().toString();
		groupDes=mGroupDes.getText().toString();
		
		if(mGroupName.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.group_name_empty));
		else if(groupDes.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.group_des_empty));
		else if(mGroupImageFile==null)
			Utility.showToastMessage(this, getResources().getString(R.string.group_image_empty));
		else
		{
			mProgressBar.setVisibility(View.VISIBLE);
			ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
			query.whereEqualTo(Constants.GROUP_NAME, mGroupName);
			query.whereEqualTo(Constants.GROUP_LOCATION, point);
			query.whereWithinMiles(Constants.GROUP_LOCATION, point,0.310);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> totemList, ParseException e) {
						if (e == null) 
						{
							if(totemList.size() > 0) // already exist 
							{
								mProgressBar.setVisibility(View.GONE);
								Utility.showToastMessage(CreatePublicGroupActivity.this, getResources().getString(R.string.group_name_exist));
							}
							else
							{
								mProgressBar.setVisibility(View.GONE);
								Intent i=new Intent(CreatePublicGroupActivity.this,PublicGroupLocationActivity.class);
								i.putExtra(Constants.GROUP_NAME, mGroupName);
								i.putExtra(Constants.GROUP_DESCRIPTION, groupDes);
								startActivity(i);
								overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
							}
						}
						else
						{
							mProgressBar.setVisibility(View.GONE);
							Utility.showToastMessage(CreatePublicGroupActivity.this, getResources().getString(R.string.server_issue));
						}
					}
			});
		}
	}
	

	 @Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        
	        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
	             beginCrop(data.getData());
	         }
	         else if (resultCode == RESULT_OK && requestCode == Utility.REQUEST_CODE){
	         	File image = new File(Utility.photoPath());
	         	Uri uri=Uri.fromFile(image);
	    			beginCrop(uri);
	         }
	         else if (requestCode == Crop.REQUEST_CROP) {
	             handleCrop(resultCode, data);
	         }
	        	
		}
	 
	  private void beginCrop(Uri source) {
	    	File mediaStorageDir = new File(
					Environment.getExternalStorageDirectory(), "/Chatterati/Crop");
	    	File file = new File(mediaStorageDir.getPath() + File.separator + "Crop");
	    	if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					Log.d("MatchIt", "failed to create directory");
				}
			}
	    	 Uri destination = Uri.fromFile(file);
	    	 if(file.exists()){
					file.delete();
				}
	        Crop.of(source, destination).asSquare().start(this);
	    }
	    
	    

	    private void handleCrop(int resultCode, Intent result) {
	        if (resultCode == RESULT_OK) {
	        	File mediaStorageDir = new File(
	    				Environment.getExternalStorageDirectory(), "/Chatterati/Crop");
	        	File file = new File(mediaStorageDir.getPath() + File.separator + "Crop");
	        	generateThumbnail(file.getAbsolutePath());		
	        	Bitmap bitmap=Utility.compressImage(file.getAbsolutePath());
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
	            byte[] img = stream.toByteArray();
	            mGroupImageFile = new ParseFile("Image.png", img);
				mGroupImageFile.saveInBackground();
				mGroupImage.setImageBitmap(bitmap);
				placeHolder.setVisibility(View.GONE);
	        } else if (resultCode == Crop.RESULT_ERROR) {
	            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
	        }
	    }
	    private void generateThumbnail(String picturePath)
	    {
	    	Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,200,200);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mGroupImageTumbnailFile = new ParseFile("ThumbImage.png", img);
			mGroupImageTumbnailFile.saveInBackground();
	    }

		
		@Override
		public void onBackPressed() {
			super.onBackPressed();
			overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
		}
		
		@Override
		protected void onStart() {
			super.onStart();
			GoogleAnalytics.getInstance(this).reportActivityStart(this);
		}

		@Override
		protected void onStop() {
			GoogleAnalytics.getInstance(this).reportActivityStop(this);
			super.onStop();
		}
}
