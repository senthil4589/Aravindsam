package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.image.Crop;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class CreateSecretGroupActivity extends Activity {
	private ImageView mBackImg,mGroupImage,placeHolder;
	private EditText mGroupNameEditTxt,mGroupDesEditTxt;
	private Button mGo;
	private RadioGroup mMemberApprovalRadioGroup;
	private ParseFile mGroupImageFile=null,mGroupImageTumbnailFile=null;
	private ProgressBar mProgressBar;
	private boolean memberApproval;
	private ArrayList<String> list=new ArrayList<String>();
	private ArrayList<String> list1=new ArrayList<String>();
	private GPSTracker gpsTracker;
	private Double mLatitude,mLongtitude;
	private ParseGeoPoint mLocationPoint;
	RadioButton r1,r2;
	private Dialog mDialog;
	private FrameLayout uploadLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_secret_group);
		Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGroupNameEditTxt=(EditText) findViewById(R.id.group_name);
		mGroupDesEditTxt=(EditText) findViewById(R.id.group_des_box);
		mGo=(Button) findViewById(R.id.go);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		mMemberApprovalRadioGroup=(RadioGroup) findViewById(R.id.membership_approval);
		
		mGroupImage=(ImageView) findViewById(R.id.upload_profile_pic);
		uploadLayout=(FrameLayout) findViewById(R.id.image_root_layout);
		placeHolder=(ImageView) findViewById(R.id.placeholder);
		
		r1=(RadioButton) findViewById(R.id.any_member);
		r2=(RadioButton) findViewById(R.id.only_admin);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		r1.setTypeface(tf);
		r2.setTypeface(tf);
		
		mGo.setTypeface(tf);
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		
		mGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 int selectedId = mMemberApprovalRadioGroup.getCheckedRadioButtonId();
				 if(selectedId!=-1){
					 RadioButton radioSexButton = (RadioButton) findViewById(selectedId);
				 	if(radioSexButton.getText().toString().equals(getResources().getString(R.string.only_admin)))
				 		memberApproval=true;
				 	else
				 		memberApproval=false;
				 }	
				 insertValuesInGroupTable(); 
				
			}
		});
		
		uploadLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog = new Dialog(CreateSecretGroupActivity.this,R.style.customDialogStyle);
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
						Utility.callCameraIntent(CreateSecretGroupActivity.this);
					}
				});
				galleryImage.setOnClickListener(new View.OnClickListener() {
					@SuppressLint({ "InlinedApi", "NewApi" })
					@Override
					public void onClick(View v) {
						  mDialog.dismiss();
						  Crop.pickImage(CreateSecretGroupActivity.this);
					}
				});
				mDialog.show();
			}
		});
	}
	@SuppressLint("NewApi")
	private void insertValuesInGroupTable() {
		final String groupDes=mGroupDesEditTxt.getText().toString();
		final String groupName=mGroupNameEditTxt.getText().toString();
	   
		if(groupName.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.group_name_empty));
		else if(groupDes.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.group_des_empty));
		else if(mGroupImageFile==null)
			Utility.showToastMessage(this, getResources().getString(R.string.group_image_empty));
		else
			insert(groupName,groupDes);
	}
	
	private void insert(final String groupName,final String groupDes)
	{
		mProgressBar.setVisibility(View.VISIBLE);
		ParseGeoPoint point = new ParseGeoPoint(mLatitude, mLongtitude);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.whereEqualTo(Constants.GROUP_NAME, groupName);
		query.whereEqualTo(Constants.GROUP_LOCATION, point);
		query.whereWithinMiles(Constants.GROUP_LOCATION, point,0.310);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> totemList, ParseException e) {
					if (e == null) 
					{
						if(totemList.size() > 0) // already exist 
						{
							mProgressBar.setVisibility(View.GONE);
							Utility.showToastMessage(CreateSecretGroupActivity.this, getResources().getString(R.string.group_name_exist));
						}
						else
						{
							insertValues(groupName,groupDes);
						}
					}
					else
					{
						Utility.showToastMessage(CreateSecretGroupActivity.this, getResources().getString(R.string.server_issue));
						mProgressBar.setVisibility(View.GONE);
					}
			}
		});
	}


	private void insertValues(final String groupName,String groupDes) {
		list.add(PreferenceSettings.getMobileNo());
		mGo.setEnabled(false);
		
		
		
		final ParseObject userObject = new ParseObject(Constants.GROUP_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		userObject.put(Constants.COUNTRY_NAME, PreferenceSettings.getCountry());
		userObject.put(Constants.GROUP_NAME, groupName);
		userObject.put(Constants.GROUP_PICTURE, mGroupImageFile);
		userObject.put(Constants.THUMBNAIL_PICTURE, mGroupImageTumbnailFile);
		userObject.put(Constants.GROUP_DESCRIPTION, groupDes);
		userObject.put(Constants.GROUP_TYPE, "Secret");
		userObject.put(Constants.GROUP_LOCATION, mLocationPoint);
		userObject.put(Constants.VISIBILITY_RADIUS, 0);
		userObject.put(Constants.MEMBER_COUNT, 1);
		userObject.put(Constants.NEWS_FEED_COUNT, 0);
		userObject.put(Constants.MEMBER_INVITATION, true);
		userObject.put(Constants.MEMBERSHIP_APPROVAL, memberApproval);
		userObject.put(Constants.JOB_SCHEDULED, false);
		userObject.put(Constants.JOB_HOURS, 0);
		userObject.put(Constants.GROUP_MEMBERS, list);
		userObject.put(Constants.ADDITIONAL_INFO_REQUIRED, false);
		userObject.put(Constants.INFO_REQUIRED, "");
		userObject.put(Constants.LATEST_POST, "");
		userObject.put(Constants.GREEN_CHANNEL, list1);
		userObject.put(Constants.GROUP_STATUS, "Active");
		userObject.put(Constants.SECRET_CODE, "");
		userObject.put(Constants.SECRET_STATUS, false);
		userObject.put(Constants.WHO_CAN_POST, 0);
		userObject.put(Constants.WHO_CAN_COMMENT, 0);
		userObject.put(Constants.ADMIN_ARRAY, list);
		userObject.put(Constants.VISIBILITY, "Nearby");
		userObject.put(Constants.GROUP_VISIBLE_TILL_DATE, Utility.getFutureDate());
		userObject.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
		userObject.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	 final String id=userObject.getObjectId();
	                	 
	                	ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
	             		memberObject.put(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
	             		memberObject.put(Constants.GROUP_ID, id);
	             		memberObject.put(Constants.ADDITIONAL_INFO_PROVIDED, "");
	             		memberObject.put(Constants.JOIN_DATE, Utility.getCurrentDate());
	             		memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
	             		memberObject.put(Constants.EXIT_GROUP, false);
	             		memberObject.put(Constants.EXITED_BY, "");
	             		//memberObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
	             		//memberObject.put(Constants.MEMBER_IMAGE, Utility.getUserImageFile());
	             		memberObject.put(Constants.MEMBER_STATUS, "Active");
	             		memberObject.put(Constants.GROUP_ADMIN, true);
	             		memberObject.put(Constants.UNREAD_MESSAGES, 0);
	             		memberObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
	             		memberObject.saveInBackground();
	              
	             							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
	             							query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	             							query.getFirstInBackground(new GetCallback<ParseObject>() {
	             								public void done(ParseObject object, ParseException e) {
	             										if (e == null) 
	             										{
	             											if(object!=null)
	             											{
	             												List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
	             												myGroupList.add(userObject.getObjectId());
	             												object.put(Constants.MY_GROUP_ARRAY, myGroupList);
	             												object.increment(Constants.BADGE_POINT, 1000);
	             												object.saveInBackground(new SaveCallback() {
							      							          public void done(ParseException e) {
								 							                 if (e == null) {
								 							                	MyGroupListActivity.flag1=true;
								 							                	mProgressBar.setVisibility(View.GONE);
								 							                	Utility.showToastMessage(CreateSecretGroupActivity.this, getResources().getString(R.string.create_group_success));
					             												Intent intent = new Intent(CreateSecretGroupActivity.this,TabGroupPostActivity.class);
					             												Utility.setGroupObject(userObject);
					             												startActivity(intent);
					             												CreateGroupActivity.activity.finish();
					             												finish();
								 							                 }
							      							        }});
	             												
	             											}
	             											else
	             												mProgressBar.setVisibility(View.GONE);
	             										}
	             										else
	             											mProgressBar.setVisibility(View.GONE);
	             								}
	             							});
	             	
	                 } else {
	                	 mProgressBar.setVisibility(View.GONE);
	                 }
	               }
	            });
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		gpsTracker = new GPSTracker(this);
		 if (gpsTracker.canGetLocation())
	       {
	    	   mLatitude=gpsTracker.getLatitude();
	    	   mLongtitude=gpsTracker.getLongitude();
	    	   mLocationPoint=new ParseGeoPoint(mLatitude,mLongtitude);
	       }
		 else
		 {
			 gpsTracker.showSettingsAlert(this);
		 }
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