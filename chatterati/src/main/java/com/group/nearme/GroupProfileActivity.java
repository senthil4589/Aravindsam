package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.image.Crop;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.JSONParser;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupProfileActivity extends Activity implements AdapterView.OnItemClickListener{
	private ImageView mBackImg;
	ParseImageView mGroupImage;
	//ImageView groupImageBg;
	private Button mNext;
	private EditText mGroupNameEditTxt,mGroupDesEditTxt;
	private GPSTracker gpsTracker;
	private ProgressBar mProgressBar,progressBar2;
	ScrollView scrollView;
    public ParseFile mGroupImageFile=null,mGroupImageTumbnailFile=null;
    String mGroupId="",mGroupName="",groupDes="",mMobileNo="",mGroupType="";
    private TextView memberCountTxt,groupCreatedAt;
    ParseObject groupObject;
    int totalMembers;
    LinearLayout rootLayout;
    TextView title;
    List<String> adminList=new ArrayList<String>();
	private Dialog mDialog;
	ImageLoader imageLoader;
	boolean isFromNearByGroup,fromShareOrigin=false;
	Activity activity;
	ArrayList<String> invitationList=new ArrayList<String>();
	int memberCount,userPoints;
	ArrayList<ParseObject> groupList=new ArrayList<ParseObject>();
	FrameLayout imageRootLayout,editProfileImage;
	private ParseFile mProfileImgFile;
	//public GPSTracker gpsTracker;

	/** for business group */
	LinearLayout businessLayout;
	EditText businessNameEditTxt,businessBranchEditTxt,businessPhoneNoEditTxt;
	AutoCompleteTextView addressAutoCompleteView;
	//PlacesTask placesTask;
	//ParserTask parserTask;


	private static final String LOG_TAG = "ExampleApp";

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";

	//------------ make your specific key ------------
	private static final String API_KEY = "AIzaSyBRX3Xq0DTBhqpW2Z-Ns8hkELbipFSwgLQ";
	GooglePlacesAutocompleteAdapter cityAutoCompleteAdapter;
	String businessName="",businessBranch="",businessPhoneNumber="",businessAddress="",groupPurpose="";
	boolean isBusinessGroup;
	ParseGeoPoint businessLocationPoint;
	HashMap<String, Object> groupAttribute=new HashMap<String, Object>();
	ImageView callImage,locationImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_profile);
		Utility.getTracker(this, "GROUP INFO - GROUP PROFILE SCREEN");
		activity=this;
		initViews();
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mNext=(Button) findViewById(R.id.next);
		mGroupNameEditTxt=(EditText) findViewById(R.id.group_name);
		mGroupDesEditTxt=(EditText) findViewById(R.id.group_des_box);
		scrollView=(ScrollView) findViewById(R.id.scroll_view);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		progressBar2=(ProgressBar) findViewById(R.id.progressBar1);
		mGroupImage=(ParseImageView) findViewById(R.id.upload_img);
		editProfileImage=(FrameLayout) findViewById(R.id.upload);
		memberCountTxt=(TextView) findViewById(R.id.member_count);
		title=(TextView) findViewById(R.id.title);
		groupCreatedAt=(TextView) findViewById(R.id.established);
		
		imageRootLayout=(FrameLayout) findViewById(R.id.image_root_layout);
		rootLayout=(LinearLayout) findViewById(R.id.root);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNext.setTypeface(tf);

		/** for business group */

		businessLayout=(LinearLayout) findViewById(R.id.business_layout);
		businessNameEditTxt=(EditText) findViewById(R.id.business_name_box);
		businessBranchEditTxt=(EditText) findViewById(R.id.branch_edit_box);
		businessPhoneNoEditTxt=(EditText) findViewById(R.id.business_contact_no);
		addressAutoCompleteView =(AutoCompleteTextView) findViewById(R.id.business_office_address);

		callImage=(ImageView) findViewById(R.id.call_image);
		locationImage=(ImageView) findViewById(R.id.location_image);

		addressAutoCompleteView.setOnItemClickListener(this);
		gpsTracker=new GPSTracker(this);
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_out);
			}
		});
		imageRootLayout.setEnabled(false);
		imageRootLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imageRootLayout.setEnabled(false);
				//Utility.setBitmapdata(mGroupImageFile.getData());
				Utility.setParseFile(mGroupImageFile);
				startActivity(new Intent(GroupProfileActivity.this, ImageFullViewActivity.class)
						.putExtra(Constants.GROUP_NAME, mGroupName)
						.putExtra(Constants.POST_IMAGE, mGroupImageFile.getUrl()));
			}
		});
		
		mNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mNext.setEnabled(false);
				if(isFromNearByGroup)
				{
					groupList.add(Utility.getGroupObject());
					updateGroupTable(0);
				}
				else
				{
					if(adminList.contains(PreferenceSettings.getMobileNo()))
						updateValues();
					else
						finish();
				}
			}
		});
		fromShareOrigin=getIntent().getBooleanExtra("share_origin", false);
		if(fromShareOrigin)
			groupObject=Utility.getShareOriginGroupObject();
		else
			groupObject=Utility.getGroupObject();
		if(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE)==null)
			mGroupImageFile=groupObject.getParseFile(Constants.GROUP_PICTURE);
		else
			mGroupImageFile=groupObject.getParseFile(Constants.THUMBNAIL_PICTURE);
		mGroupImage.setParseFile(mGroupImageFile);
		mGroupImage.loadInBackground(new GetDataCallback() {
			@Override
			public void done(byte[] arg0, ParseException arg1) {
				imageRootLayout.setEnabled(true);
				progressBar2.setVisibility(View.GONE);
			}
		});
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.fromLocalDatastore();
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject userObject, ParseException e) {
						if(userObject!=null)
						{
							mProfileImgFile=userObject.getParseFile(Constants.PROFILE_PICTURE);
						}
			}});
		
		
		/*mGroupImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utility.uploadImage(GroupProfileActivity.this,REQUEST_CODE,GALLERY_REQUEST);						
			}
		});*/
		
		editProfileImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog = new Dialog(GroupProfileActivity.this,R.style.customDialogStyle);
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
						Utility.callCameraIntent(GroupProfileActivity.this);
					}
				});
				galleryImage.setOnClickListener(new View.OnClickListener() {
					@SuppressLint({ "InlinedApi", "NewApi" })
					@Override
					public void onClick(View v) {
						  mDialog.dismiss();
						  Crop.pickImage(GroupProfileActivity.this);
					}
				});
				mDialog.show();
			}
		});
		isFromNearByGroup=getIntent().getBooleanExtra("flag", false);
		/*if(isFromNearByGroup)
			mNext.setText("JOIN");
		else
			mNext.setText("DONE");*/
		
		setValues();
		
	}
	private void updateGroupTable(final int position) {
		
		/*if(groupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Open") || groupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Public"))
		{
			insertValues(position);
		}*/
		if(groupList.get(position).get(Constants.GROUP_TYPE).toString().equals("Private"))
		{
			/*ArrayList<String> list=(ArrayList<String>) nearGroupList.get(position).get(Constants.GREEN_CHANNEL);
			for(int i=0;i<myGroupList.size();i++)
			{
				for(int j=0;j<list.size();j++)
				{
					if((myGroupList.get(i)).equals(list.get(j).toString()))
					{
						flag1=true;
						break;
					}
				}
			}*/
			
			if(groupList.get(position).getInt(Constants.JOB_HOURS) > 0)
			{
				insertValues(position);
			}
			/*else if(flag1)
			{
				insertValues(position);
			}*/
			else if(groupList.get(position).getBoolean(Constants.ADDITIONAL_INFO_REQUIRED))
			{
				
				mDialog = new Dialog(activity);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);     
				mDialog.setContentView(R.layout.ask_additional_info);
				mDialog.setCancelable(true);
				mDialog.setCanceledOnTouchOutside(true);
				WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
				windowManager.gravity = Gravity.CENTER;
				TextView text=(TextView) mDialog.findViewById(R.id.additional_info_text);
				final EditText box=(EditText) mDialog.findViewById(R.id.additional_info_box);
				text.setText("Enter "+groupList.get(position).get(Constants.INFO_REQUIRED).toString());

				Button send=(Button) mDialog.findViewById(R.id.send);
				Button cancel=(Button) mDialog.findViewById(R.id.cancel);
				send.setOnClickListener(new View.OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						
						final String text=box.getText().toString();
						if(text.isEmpty())
						{
							Utility.showToastMessage(activity, getResources().getString(R.string.additional_info_empty));
						}
						else
						{
							mDialog.dismiss();
							String str=groupList.get(position).get(Constants.INFO_REQUIRED).toString()+" - "+text;
							sendInvitation(str,position);	
						}
					}
				});
				
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v){
						IBinder token = box.getWindowToken();
						( ( InputMethodManager ) activity.getSystemService( activity.INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
						mDialog.dismiss();
					}
				});
				
				mDialog.show();
			}
			else if(groupList.get(position).getBoolean(Constants.SECRET_STATUS))
			{
				mDialog = new Dialog(activity);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);     
				mDialog.setContentView(R.layout.ask_additional_info);
				mDialog.setCancelable(true);
				mDialog.setCanceledOnTouchOutside(true);
				WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
				windowManager.gravity = Gravity.CENTER;
				TextView text=(TextView) mDialog.findViewById(R.id.additional_info_text);
				final EditText box=(EditText) mDialog.findViewById(R.id.additional_info_box);
				text.setText("Enter Invitation Code");

				Button send=(Button) mDialog.findViewById(R.id.send);
				Button cancel=(Button) mDialog.findViewById(R.id.cancel);
				send.setOnClickListener(new View.OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						
						final String text=box.getText().toString();
						if(text.isEmpty())
						{
							Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.ask_secret_code));
							//Toast toast = Toast.makeText(activity, "Please enter invitation code", Toast.LENGTH_LONG);
							//toast.setGravity(Gravity.CENTER, 0, 0);
							//toast.show();
							//Toast.makeText(activity, "Please enter invitation code", Toast.LENGTH_LONG).show();
						}
						else if(text.equals(groupList.get(position).get(Constants.SECRET_CODE).toString()))
						{
							mDialog.dismiss();
							insertValues(position);
						}
						else
						{
							box.setText("");
							Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.invalid_secret_code));
							//Toast toast = Toast.makeText(activity, "Invalid invitation code", Toast.LENGTH_LONG);
							//toast.setGravity(Gravity.CENTER, 0, 0);
							//toast.show();
							//Toast.makeText(activity, "Invalid invitation code", Toast.LENGTH_LONG).show();
						}
					}
				});
				
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						IBinder token = box.getWindowToken();
						( ( InputMethodManager ) activity.getSystemService( activity.INPUT_METHOD_SERVICE ) ).hideSoftInputFromWindow( token, 0 );
						mDialog.dismiss();
					}
				});
				
				mDialog.show();
			}
			else
			{
				sendInvitation("",position);	
			}
		}
		else
		 {
			 insertValues(position);
		 }
	}
	
	@SuppressLint("NewApi")
	private void sendInvitation(final String additionalInfo,final int position)
	{
		mProgressBar.setVisibility(View.VISIBLE);
		ArrayList<String> list1=new ArrayList<String>();
		ParseGeoPoint point=null;
		try{
		 point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		}
		catch(Exception e){
			point=new ParseGeoPoint(0.0,0.0);
		}
		ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		//userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		//userObject.put(Constants.MEMBER_IMAGE, mProfileImgFile);
		userObject.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
		userObject.put(Constants.POST_TEXT, additionalInfo);
		userObject.put(Constants.POST_TYPE, "Invitation");
		//userObject.put(Constants.UP_VOTE_COUNT, 0);
		//userObject.put(Constants.DOWN_VOTE_COUNT, 0);
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.POST_POINT, 0);
		userObject.put(Constants.LIKE_ARRAY, list1);
		userObject.put(Constants.DIS_LIKE_ARRAY, list1);
		userObject.put(Constants.FLAG_ARRAY, list1);
		//userObject.put(Constants.POST_IMAGE, mGroupImageFile);
		userObject.put(Constants.IMAGE_CAPTION, "");
		userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();
		//userObject.pinInBackground(groupList.get(position).getObjectId());
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							List<String> list2=object.getList(Constants.GROUP_INVITATION);
							list2.add(groupList.get(position).getObjectId());
							object.put(Constants.GROUP_INVITATION, list2);
							PreferenceSettings.setGroupInvitationList(list2);
							object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
							object.saveInBackground(new SaveCallback() {
						          public void done(ParseException e) {
						                 if (e == null) {
						                	 NearByGroupListActivity.flag2=true;
							                	mProgressBar.setVisibility(View.GONE);
		            							//Toast.makeText(activity, "Join request sent", Toast.LENGTH_LONG).show();
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
		
		
	}
	
	private void insertValues(final int position)
	{
		mProgressBar.setVisibility(View.VISIBLE);
		ParseGeoPoint point=null;
		try{
			 point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
			}
			catch(Exception e){
				point=new ParseGeoPoint(0.0,0.0);
			}
		final String latestPost=PreferenceSettings.getUserName()+" - "+activity.getResources().getString(R.string.new_member);
		ArrayList<String> list1=new ArrayList<String>();
		ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		//userObject.put(Constants.MEMBER_IMAGE, mProfileImgFile);
		//userObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
		userObject.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
		userObject.put(Constants.POST_TEXT, latestPost);
		userObject.put(Constants.POST_TYPE, "Member");
		//userObject.put(Constants.UP_VOTE_COUNT, 0);
		//userObject.put(Constants.DOWN_VOTE_COUNT, 0);
		userObject.put(Constants.COMMENT_COUNT, 0);
		userObject.put(Constants.FLAG_COUNT, 0);
		userObject.put(Constants.POST_POINT, 0);
		userObject.put(Constants.LIKE_ARRAY, list1);
		userObject.put(Constants.DIS_LIKE_ARRAY, list1);
		userObject.put(Constants.FLAG_ARRAY, list1);
		//userObject.put(Constants.POST_IMAGE, mGroupImageFile);
		userObject.put(Constants.IMAGE_CAPTION, "");
		userObject.put(Constants.POST_STATUS, "Active");
		userObject.put(Constants.FEED_LOCATION, point);
		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
		userObject.saveInBackground();
		userObject.pinInBackground(groupList.get(position).getObjectId());
		ParseObject memberObject = new ParseObject(Constants.MEMBER_DETAIL_TABLE);
 		memberObject.put(Constants.MEMBER_NO,  PreferenceSettings.getMobileNo());
 		memberObject.put(Constants.GROUP_ID, groupList.get(position).getObjectId());
 		memberObject.put(Constants.ADDITIONAL_INFO_PROVIDED, "");
 		memberObject.put(Constants.JOIN_DATE, Utility.getCurrentDate());
 		memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
 		memberObject.put(Constants.EXIT_GROUP, false);
 		memberObject.put(Constants.EXITED_BY, "");
 		memberObject.put(Constants.MEMBER_STATUS, "Active");
 		//memberObject.put(Constants.MEMBER_NAME, PreferenceSettings.getUserName());
 		//memberObject.put(Constants.MEMBER_IMAGE, Utility.getUserImageFile());
 		memberObject.put(Constants.GROUP_ADMIN, false);
 		memberObject.put(Constants.UNREAD_MESSAGES, 0);
 		memberObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
 		memberObject.saveInBackground();
	
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
 		query.whereEqualTo(Constants.OBJECT_ID, groupList.get(position).getObjectId());
 		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(final ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
  						{
							
							object.increment(Constants.MEMBER_COUNT, 1);
 							memberCount=object.getInt(Constants.MEMBER_COUNT);
 							ArrayList<String> memberNoList=(ArrayList<String>) object.get(Constants.GROUP_MEMBERS);
 							memberNoList.add(PreferenceSettings.getMobileNo());
 							object.put(Constants.GROUP_MEMBERS, memberNoList);
 							object.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
 							
 							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		             		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		             		query.getFirstInBackground(new GetCallback<ParseObject>() {
		            			public void done(ParseObject object, ParseException e) {
		            					if (e == null) 
		            					{
		            						if(object!=null)
		              						{
		             								List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
		             									myGroupList.add(groupList.get(position).getObjectId());
		             									object.put(Constants.MY_GROUP_ARRAY, myGroupList);
		             								if(PreferenceSettings.isJoinFirst())
		             								{
		             									userPoints=1000;
		             									//list.get(0).increment(Constants.BADGE_POINT, 1000);
		             									PreferenceSettings.setJoinStatus(false);
		             								}
		             								else
		             								{
		             									userPoints=100;
		             								}
		             								
		             								if(memberCount==10)
		             									userPoints=userPoints+1000;
		             								else if(memberCount==50)
		             									userPoints=userPoints+2000;
		             								object.increment(Constants.BADGE_POINT, userPoints);
		             								object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
		             								object.saveInBackground();
		             								mProgressBar.setVisibility(View.GONE);
			             							NearByGroupListActivity.flag2=true;
			             							MyGroupListActivity.flag1=true;
			             							
			             							//activity.startActivity(new Intent(activity,TabGroupPostActivity.class));
			             							if(fromShareOrigin)
													{
														Toast.makeText(activity,"Joined successfully. Please check under My Groups",Toast.LENGTH_LONG).show();
													}
												else
													{
														activity.startActivity(new Intent(activity,TopicListActivity.class));
													}


													finish();
     							                	 /* new SaveCallback() {
		             							        public void done(ParseException e) {
		             							                 if (e == null) {
		             							                	mProgressBar.setVisibility(View.GONE);
							             							NearByGroupListActivity.flag2=true;
							             							MyGroupListActivity.flag1=true;
							             							Utility.setGroupObject(groupObject);
							             							activity.startActivity(new Intent(activity,TabGroupPostActivity.class));
		             							                	finish();
							             							//MyGroupListActivity.flag1=true;
							             							//Toast.makeText(activity, "You joined successfully", Toast.LENGTH_LONG).show();
							             							//startActivity(new Intent(GroupProfileActivity.this,TabGroupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
							             						 }
		             							          }});*/
		             							}
			             					}
			             			}
			             		});
 							
 							object.saveInBackground(new SaveCallback() {
						          public void done(ParseException e) {
					                 if (e == null) {
					                	 final ParseObject groupObject=object;
					                	
					                 }
						          }});
 							
 							
 						}
 					}
 			}
 		});
	
	}

	
	
	@SuppressLint("NewApi")
	private void updateValues()
	{
		final String name=mGroupNameEditTxt.getText().toString();
		 groupDes=mGroupDesEditTxt.getText().toString();
		String bizName="",bizAdrress="",bizNo="";
		if(groupPurpose.equals("Business Groups")) {
			 bizName = businessNameEditTxt.getText().toString();
			 bizAdrress = addressAutoCompleteView.getText().toString();
			bizNo = businessPhoneNoEditTxt.getText().toString();
		}

		if(name.isEmpty())
		{
			Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.group_name_empty));
		}
		else if(groupDes.isEmpty())
		{
			Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.group_des_empty));
		}
		else if(groupPurpose.equals("Business Groups") && bizName.isEmpty()) {
			Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.business_name_empty));
		}
		else if(groupPurpose.equals("Business Groups") && bizAdrress.isEmpty()) {
			Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.business_address_empty));
		}
		else if(groupPurpose.equals("Business Groups") && bizNo.isEmpty()) {
			Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.business_phone_empty));
		}
		else
		{
			mNext.setEnabled(false);
			 mProgressBar.setVisibility(View.VISIBLE);
			
			if(name.equals(mGroupName))
			{
				updateGroupProfile(mGroupName);
			}
			else
			{
			ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
			query.whereEqualTo(Constants.GROUP_NAME, name);
			query.whereEqualTo(Constants.GROUP_LOCATION, groupObject.getParseGeoPoint(Constants.GROUP_LOCATION));
			query.whereWithinMiles(Constants.GROUP_LOCATION, point,0.310);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> list, ParseException e) {
						if (e == null) 
						{
							if(list.size() > 0) // already exist 
							{
								mProgressBar.setVisibility(View.GONE);
								Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.group_name_exist));
							}
							else
							{
								updateGroupProfile(name);
							}
						}
						else
							mProgressBar.setVisibility(View.GONE);
					}
			});
			}
			
		}
	}

	private void updateGroupProfile(String name)
	{
		System.out.println("inside updateGroupProfile group attribute");
		groupObject.put(Constants.GROUP_NAME, name);
		if(mGroupImageTumbnailFile!=null)
		{
			groupObject.put(Constants.GROUP_PICTURE, mGroupImageFile);
			groupObject.put(Constants.THUMBNAIL_PICTURE, mGroupImageTumbnailFile);
		}

		groupObject.put(Constants.GROUP_DESCRIPTION, groupDes);
		System.out.println("before if group attribute");
		if(groupPurpose.equals("Business Groups")) {
			String bizName=businessNameEditTxt.getText().toString();
			String bizPhoneNo=businessPhoneNoEditTxt.getText().toString();
			String bizBranch=businessBranchEditTxt.getText().toString();
			String bizAdrress=addressAutoCompleteView.getText().toString();
			try {
				groupAttribute = (HashMap<String, Object>) groupObject.get(Constants.GROUP_ATTRIBUTE);
			}
			catch (Exception e){
				System.out.println("inside catch group attribute");
			}
			System.out.println("after catch if group attribute");
			groupAttribute.put(Constants.BUSINESS_NAME, bizName);
			groupAttribute.put(Constants.BUSINESS_PHONE_NUMBER, bizPhoneNo);
			groupAttribute.put(Constants.BUSINESS_BRANCH, bizBranch);
			groupAttribute.put(Constants.BUSINESS_ADDRESS, bizAdrress);
			if(businessLocationPoint!=null) {
				groupAttribute.put(Constants.LATITUDE, businessLocationPoint.getLatitude());
				groupAttribute.put(Constants.LONGTITUDE, businessLocationPoint.getLongitude());
			}
			groupObject.put(Constants.GROUP_ATTRIBUTE, groupAttribute);
		}
		System.out.println("outside if group attribute");

		groupObject.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e == null) {
					mProgressBar.setVisibility(View.GONE);
					Utility.showToastMessage(GroupProfileActivity.this, getResources().getString(R.string.group_info_update_success));
					MyGroupListActivity.flag1=true;
					GroupPostListActivity.flag1=true;
					//Utility.setRefershNeed(true);
					if(fromShareOrigin)
						Utility.setShareOriginGroupObject(groupObject);
					else
						Utility.setGroupObject(groupObject);
					finish();
				}
				else
				{
					System.out.println("exception "+e);
				}
			}});
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
		super.onResume();
		imageRootLayout.setEnabled(true);
	}

	private void setValues() {
	
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		//mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		try {
			 groupPurpose=groupObject.getString(Constants.GROUP_PURPOSE);
		if(groupPurpose.equals("Business Groups")){
			isBusinessGroup=true;
			businessLayout.setVisibility(View.VISIBLE);
			final HashMap<String, Object> groupAttribute=(HashMap<String, Object>) groupObject.get(Constants.GROUP_ATTRIBUTE);


				businessNameEditTxt.setText(groupAttribute.get(Constants.BUSINESS_NAME).toString());
				businessBranchEditTxt.setText(groupAttribute.get(Constants.BUSINESS_BRANCH).toString());
				businessPhoneNoEditTxt.setText(groupAttribute.get(Constants.BUSINESS_PHONE_NUMBER).toString());
				addressAutoCompleteView.setText(groupAttribute.get(Constants.BUSINESS_ADDRESS).toString());
			cityAutoCompleteAdapter=new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item);
			cityAutoCompleteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			addressAutoCompleteView.setAdapter(cityAutoCompleteAdapter);


			locationImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					double lat = (double) groupAttribute.get(Constants.LATITUDE);
					double lng = (double) groupAttribute.get(Constants.LONGTITUDE);
					startActivity(new Intent(activity, EventLocationActivity.class)
							.putExtra(Constants.LATITUDE, lat).putExtra(Constants.LONGTITUDE, lng)
							.putExtra("event_name", groupAttribute.get(Constants.BUSINESS_NAME).toString()));

				}
			});

			callImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String no="tel:"+groupAttribute.get(Constants.BUSINESS_PHONE_NUMBER).toString();
					Uri number = Uri.parse(no);
					Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
					startActivity(callIntent);

				}
			});


		}

		else {
			isBusinessGroup = false;
			businessLayout.setVisibility(View.GONE);
		}


		}
			catch (Exception e){
				isBusinessGroup = false;
				businessLayout.setVisibility(View.GONE);
			}

		
		adminList=groupObject.getList(Constants.ADMIN_ARRAY);
		
		DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
		String str=format.format(groupObject.getCreatedAt());
		
		groupCreatedAt.setText("Estd: "+str);

		if(isFromNearByGroup)
		{
			boolean isFromPendingInvites=getIntent().getBooleanExtra("fromPendingInvitation", false);
			if(isFromPendingInvites)
			{
				mNext.setVisibility(View.GONE);
			}
			else
			{
				invitationList=getIntent().getStringArrayListExtra("invitation");
				mNext.setVisibility(View.VISIBLE);
				if(mGroupType.equals("Private") && invitationList.contains(groupObject.getObjectId()))
				{
					mNext.setEnabled(false);
					mNext.setText("PENDING");
					//mNext.setBackgroundColor(getResources().getColor(R.color.pending_color));
				}
				else
				{
					mNext.setEnabled(true);
					mNext.setText("JOIN");
					//mNext.setBackgroundColor(getResources().getColor(R.color.join_color));
				}
			}
			mGroupNameEditTxt.setKeyListener(null);
			mGroupDesEditTxt.setKeyListener(null);
			mGroupDesEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
			mGroupNameEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
			mGroupImage.setEnabled(false);
			mGroupNameEditTxt.setBackgroundResource(0);
			mGroupDesEditTxt.setBackgroundResource(0);
			if(groupPurpose.equals("Business Groups")) {
				businessBranchEditTxt.setKeyListener(null);
				businessPhoneNoEditTxt.setKeyListener(null);
				businessNameEditTxt.setKeyListener(null);
				addressAutoCompleteView.setKeyListener(null);

				businessBranchEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
				businessPhoneNoEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
				businessNameEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
				addressAutoCompleteView.setTextColor(getResources().getColor(R.color.light_gray));

				businessBranchEditTxt.setBackgroundResource(0);
				businessPhoneNoEditTxt.setBackgroundResource(0);
				businessNameEditTxt.setBackgroundResource(0);
				addressAutoCompleteView.setBackgroundResource(0);
			}

		}
		else
		{
		if(adminList.contains(PreferenceSettings.getMobileNo()))
		{
			//rootLayout.setEnabled(true);
			mGroupImage.setEnabled(true);
			mGroupNameEditTxt.setEnabled(true);
			mGroupDesEditTxt.setEnabled(true);
			//mNext.setEnabled(true);
			mNext.setVisibility(View.VISIBLE);
			editProfileImage.setVisibility(View.VISIBLE);
			//mGroupImage.setScaleType(ScaleType.FIT_XY);
		}
		else
		{
			mGroupNameEditTxt.setKeyListener(null);
			mGroupDesEditTxt.setKeyListener(null);
			mGroupDesEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
			mGroupNameEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
			mGroupImage.setEnabled(false);
			//mGroupNameEditTxt.setEnabled(false);
			//mGroupDesEditTxt.setEnabled(false);
			mGroupNameEditTxt.setBackgroundResource(0);
			mGroupDesEditTxt.setBackgroundResource(0);
			//if business group
			if(groupPurpose.equals("Business Groups")) {
				businessBranchEditTxt.setKeyListener(null);
				businessPhoneNoEditTxt.setKeyListener(null);
				businessNameEditTxt.setKeyListener(null);
				addressAutoCompleteView.setKeyListener(null);

				businessBranchEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
				businessPhoneNoEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
				businessNameEditTxt.setTextColor(getResources().getColor(R.color.light_gray));
				addressAutoCompleteView.setTextColor(getResources().getColor(R.color.light_gray));

				businessBranchEditTxt.setBackgroundResource(0);
				businessPhoneNoEditTxt.setBackgroundResource(0);
				businessNameEditTxt.setBackgroundResource(0);
				addressAutoCompleteView.setBackgroundResource(0);
			}
			//mNext.setEnabled(false);
			mNext.setVisibility(View.GONE);
		}
		}
		
		totalMembers=groupObject.getInt(Constants.MEMBER_COUNT);
		groupDes=groupObject.get(Constants.GROUP_DESCRIPTION).toString();
		

		mGroupNameEditTxt.setText(mGroupName);
		mGroupDesEditTxt.setText(groupDes);
		memberCountTxt.setText(String.valueOf(totalMembers));
		//Picasso.with(GroupProfileActivity.this).load(mGroupImageFile.getUrl()).into(mGroupImage);//
		
		
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




	class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
		private ArrayList<String> resultList;

		public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = autocomplete(constraint.toString());

						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}
	public ArrayList<String> autocomplete(String input) {
		ArrayList<String> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			//System.out.println("country code"+countryCode);
			//String code="&components=country:"+countryCode;
			StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?key=" + API_KEY);
			//sb.append("&components=country:"+countryCode);
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));
			System.out.println("url :::: "+sb.toString());
			URL url = new URL(sb.toString());

			System.out.println("URL: "+url);
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {

			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
			System.out.println("json array ::: "+predsJsonArray);
			// Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
				String address=predsJsonArray.getJSONObject(i).getString("description");
				String placeID=predsJsonArray.getJSONObject(i).getString("place_id");
				String place=address+"place"+placeID;
				resultList.add(place);
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Cannot process JSON results", e);
		}

		return resultList;
	}

	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		String str = (String) adapterView.getItemAtPosition(position);
		String strArray[]=str.split("place");
		businessAddress=strArray[0];
		addressAutoCompleteView.setText(businessAddress);
		new LatLongAsynTask(strArray[1]).execute();
		//Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	class LatLongAsynTask extends AsyncTask<Void,Void,Boolean> {
		String placeId = "";

		public LatLongAsynTask(String placeid) {
			this.placeId = placeid;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				String placeUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&key=" + API_KEY;

				JSONParser parser=new JSONParser();
				String jsonResults=parser.getJSONString(placeUrl);
				System.out.println("Place json :: "+jsonResults);
				// Create a JSON object hierarchy from the results
				JSONObject jsonObj = new JSONObject(jsonResults);
				System.out.println("place json object");
				JSONObject jsonObj1 = jsonObj.getJSONObject("result");
				JSONObject jsonObj2 = jsonObj1.getJSONObject("geometry");
				JSONObject jsonObj3 = jsonObj2.getJSONObject("location");
				double lat = Double.parseDouble(jsonObj3.getString("lat"));
				double lng = Double.parseDouble(jsonObj3.getString("lng"));

				 businessLocationPoint =new ParseGeoPoint(lat, lng);
				//groupInfo.setBusinessLocationPoint(point);
				System.out.println("lat and lng ::: " + lat + "\t" + lng);
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Cannot process JSON results", e);
			}
			return true;
		}
	}
		
}