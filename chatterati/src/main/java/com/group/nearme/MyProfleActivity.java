package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.image.Crop;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

public class MyProfleActivity extends Activity {
	Button mUpdateTxtView;
	TextView mNameChangeRule,userScore;
	private EditText mNameEditTxt;
	private ParseImageView mUploadImgView;
	private ParseFile mProfileImgFile,newProfileImageFile=null,mProfileImageTumbnailFile=null;
	private String mGender="Male",name="";
	private ProgressBar mProgressBar,mProgressBar1;
	private int count;
	private ImageView mBackImg;
	private Dialog mDialog;
	FrameLayout imageRootLayout,editProfileImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profile);
		Utility.getTracker(this, "MY PROFILE SCREEN");
		initViews();
	}

	private void initViews() {
		mUpdateTxtView=(Button) findViewById(R.id.update);
		mNameChangeRule=(TextView) findViewById(R.id.name_change_rules);
		mNameEditTxt=(EditText) findViewById(R.id.name);
		mUploadImgView=(ParseImageView) findViewById(R.id.upload_profile_pic);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		mProgressBar1=(ProgressBar) findViewById(R.id.progressBar1);
		
		mBackImg=(ImageView) findViewById(R.id.back);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mUpdateTxtView.setTypeface(tf);
		
		//upload=(ImageView) findViewById(R.id.upload);
		imageRootLayout=(FrameLayout) findViewById(R.id.image_root_layout);
		editProfileImage=(FrameLayout) findViewById(R.id.edit_image_layout);
		
		//Picasso.with(this).load(PreferenceSettings.getProfilePic()).into(mUploadImgView);
		
		mUploadImgView.setParseFile(Utility.getUserImageFile());
		mUploadImgView.loadInBackground();
		
		mNameEditTxt.setText(PreferenceSettings.getUserName());
		mNameEditTxt.setSelection(mNameEditTxt.getText().length());
		count=PreferenceSettings.getNameChangeCount();
		count=3-count;
		if(count<=1)
			mNameChangeRule.setText(getResources().getString(R.string.name_change_rules)+" "+"\nYou have "+count+" attempt left");
		else
			mNameChangeRule.setText(getResources().getString(R.string.name_change_rules)+" "+"\nYou have "+count+" attempts left");
		if(count==0)
			mNameEditTxt.setKeyListener(null);
		
		
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		
		editProfileImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog = new Dialog(MyProfleActivity.this,R.style.customDialogStyle);
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
						Utility.callCameraIntent(MyProfleActivity.this);
					}
				});
				galleryImage.setOnClickListener(new View.OnClickListener() {
					@SuppressLint({ "InlinedApi", "NewApi" })
					@Override
					public void onClick(View v) {
						  mDialog.dismiss();
						  //chooseImage();
						  Crop.pickImage(MyProfleActivity.this);
					}
				});
				mDialog.show();
			}
		});
		
		imageRootLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imageRootLayout.setEnabled(false);
					try {
						if(newProfileImageFile!=null)
						{
							Utility.setParseFile(newProfileImageFile);
						}
							//Utility.setBitmapdata(newProfileImageFile.getData());
						else
						{
							Utility.setParseFile(Utility.getUserImageFile());
						}
							//Utility.setBitmapdata(mProfileImgFile.getData());
					startActivity(new Intent(MyProfleActivity.this,ImageFullViewActivity.class)
					.putExtra(Constants.GROUP_NAME, PreferenceSettings.getUserName())
					.putExtra(Constants.POST_IMAGE, PreferenceSettings.getProfilePic()));
				} catch (Exception e) {
				}	
			}
			
		});
		
		
		mUpdateTxtView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				name=mNameEditTxt.getText().toString();
				if(name.isEmpty())
				{
					Utility.showToastMessage(MyProfleActivity.this, getResources().getString(R.string.user_name_empty));
				}
				else
				{
					saveProfileInfo();
				}
				
				
			}
		});
	}
	private void saveProfileInfo() {
		mUpdateTxtView.setEnabled(false);
		if(count!=0)
			name=mNameEditTxt.getText().toString();
		mProgressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (object != null) 
					{
							if(count!=0 && !name.equals(PreferenceSettings.getUserName()))
							{
								object.put(Constants.USER_NAME, name);
								//object.put(Constants.UPDATE_NAME, true);
								object.increment(Constants.NAME_CHANGE_COUNT,1);
							}
							object.put(Constants.GENDER, mGender);
							if(newProfileImageFile!=null)
							{
								//object.put(Constants.UPDATE_IMAGE, true);
								object.put(Constants.THUMBNAIL_PICTURE, mProfileImageTumbnailFile);
								object.put(Constants.PROFILE_PICTURE, newProfileImageFile);
								
							}
							//object.put(Constants.PUSH_NOTIFICATION, mPushNotificationToggle.isChecked());
							//object.put(Constants.SOUND_NOTIFICATION, mSoundNotificationToggle.isChecked());
							object.saveInBackground();
							object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
							if(count!=0 && !name.equals(PreferenceSettings.getUserName()))
							{
								PreferenceSettings.setUserName(name);
								PreferenceSettings.setNameChangeCount(PreferenceSettings.getNameChangeCount()+1);
							}
							
							PreferenceSettings.setGender(mGender);
							//PreferenceSettings.setPushStatus(mPushNotificationToggle.isChecked());
							//PreferenceSettings.setSoundStatus(mSoundNotificationToggle.isChecked());
							if(newProfileImageFile!=null)
							{
								Utility.setUserImageFile(newProfileImageFile);
								PreferenceSettings.setProfilePic(newProfileImageFile.getUrl());
							}
							mProgressBar.setVisibility(View.GONE);
							Utility.showToastMessage(MyProfleActivity.this, getResources().getString(R.string.my_profile_update_success));
							mUpdateTxtView.setEnabled(true);
							TabGroupActivity.mMenuAdapter.notifyDataSetChanged();
							//finish();
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
            newProfileImageFile = new ParseFile("Image.png", img);
            newProfileImageFile.saveInBackground();
            mUploadImgView.setImageBitmap(bitmap);
      	
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
		mProfileImageTumbnailFile = new ParseFile("ThumbImage.png", img);
		mProfileImageTumbnailFile.saveInBackground();
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
		imageRootLayout.setEnabled(true);
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
