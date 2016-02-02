package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Text;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.group.nearme.adapter.GroupPurposeGridAdapter;
import com.group.nearme.image.Crop;
import com.group.nearme.objects.GroupInfo;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.CustomAutoCompleteTextView;
import com.group.nearme.util.JSONParser;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;


public class CreateGroupProfile extends Activity implements AdapterView.OnItemClickListener
{
	private ImageView mBackImg,mGroupImage,placeHolder;//,transparentImageView,uploadImag;
	private Button mNext;
	private EditText mGroupNameEditTxt,mGroupDes;
	private GPSTracker gpsTracker;
	private ProgressBar mProgressBar;
	Circle mapCircle;
	ScrollView scrollView;
	String mGroupName="",groupDes="";
	public ParseFile mGroupImageFile=null,mGroupImageTumbnailFile=null;
	public Dialog mDialog;
	private FrameLayout uploadLayout;
	private LinearLayout categoryLayout,tagRootLayout,tagCheckboxLayout;
	private Spinner categorySpinner;
	private String groupType="";
	private ArrayList<String> categoryList=new ArrayList<String>();
	private ArrayList<String> selectedTags=new ArrayList<String>();
	private ArrayList<String> duplicateTags=new ArrayList<String>();
	private ArrayList<String> allowedRange=new ArrayList<String>();
	private boolean isCategory;
	ArrayList<CheckBox> selectedTagArray;
	private String purpose="",category="";
	public static Activity activity;
	public GroupInfo groupInfo=new GroupInfo();
	private AutoCompleteTextView tagAutoComplete1,tagAutoComplete2,tagAutoComplete3,tagAutoComplete4;
	ImageView addTag;
	LinearLayout childLayout;
	ArrayList<AutoCompleteTextView> autoCompleteList=new ArrayList<AutoCompleteTextView>();
	String purposeId="",selectedCategory="";
	TextView mandatoryStar;
	 private String blockCharacterSet = "~#^|$%&*!,.-+()[]?;:=@/{}";

	    private InputFilter filter = new InputFilter() {

	        @Override
	        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

	            if (source != null && blockCharacterSet.contains(("" + source))) {
	                return "";
	            }
	            return null;
	        }
	    };

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
	String businessName="",businessBranch="",businessPhoneNumber="",businessAddress="";
	boolean isBusinessGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.create_group_profile);
		Utility.getTracker(this, "CREATE GROUP - PROFILE SCREEN");
		initViews();
		activity=this;
		groupType=getIntent().getStringExtra(Constants.GROUP_TYPE);
		purpose=getIntent().getStringExtra(Constants.GROUP_PURPOSE);
		isCategory=getIntent().getBooleanExtra("flag", false);
		allowedRange=getIntent().getStringArrayListExtra(Constants.GROUP_PURPOSE_ALLOWED_RANGE);

		if(purpose.equals("Business Groups")){
			isBusinessGroup=true;
			businessLayout.setVisibility(View.VISIBLE);
		}

		else {
			isBusinessGroup = false;
			businessLayout.setVisibility(View.GONE);
		}



		if(!groupType.equals(Constants.SECRET_GROUP))
		{
			categoryList=getIntent().getStringArrayListExtra(Constants.GROUP_CATEGORY);
			if(categoryList.size() > 0)
			{
				categoryLayout.setVisibility(View.VISIBLE);
			
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				     R.layout.contact_us_spinner_item, categoryList);
			
			//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, categoryList , R.layout.contact_us_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			categorySpinner.setAdapter(adapter);
			purposeId=ChooseGroupTypeActivity.objectIDList.get(0);
			 selectedCategory=categoryList.get(0);
			selectedTags=ChooseGroupTypeActivity.tagsMap.get(categoryList.get(0));
			setAutoCompleteAdapter(selectedTags);
			categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				  public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
					 // createCheckBoxView(position);
					  selectedCategory=categoryList.get(position);
					  purposeId=ChooseGroupTypeActivity.objectIDList.get(position);
					  selectedTags=ChooseGroupTypeActivity.tagsMap.get(categoryList.get(position));
					  setAutoCompleteAdapter(selectedTags);
				    }
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
			    });		        
			}
			else
			{
				categoryLayout.setVisibility(View.GONE);
				 selectedCategory="";
				 purposeId=ChooseGroupTypeActivity.objectIDList.get(0);
				selectedTags=ChooseGroupTypeActivity.tagsMap.get("empty");
				setAutoCompleteAdapter(selectedTags);
			}
			
			tagRootLayout.setVisibility(View.VISIBLE);
	
			
		}
		else
		{
			categoryLayout.setVisibility(View.GONE);
			tagRootLayout.setVisibility(View.GONE);
		}
		
		  addTag.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(tagAutoComplete1.getText().toString().trim().isEmpty()
							|| tagAutoComplete2.getText().toString().trim().isEmpty())
					{
						Toast.makeText(CreateGroupProfile.this, "Please enter tag in empty box", Toast.LENGTH_SHORT).show();
					}
					else
					{
						addTag.setVisibility(View.INVISIBLE);
						childLayout.setVisibility(View.VISIBLE);
						//tagAutoComplete4.setVisibility(View.VISIBLE);
					}
				}
			});
		    
	}
	
	private void setAutoCompleteAdapter(ArrayList<String> list)
	{
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, list);
		
		tagAutoComplete1.setAdapter(adapter);
		tagAutoComplete2.setAdapter(adapter);
		tagAutoComplete3.setAdapter(adapter);
		tagAutoComplete4.setAdapter(adapter);
	}
	
	private void createCheckBoxView(int position)
	{
		 tagCheckboxLayout.removeAllViews();
		  
		  selectedTagArray=new ArrayList<CheckBox>();
		  ArrayList<String> list=ChooseGroupTypeActivity.tagsMap.get(categoryList.get(position));
		  if(list.size() > 0)
		  {
			  tagRootLayout.setVisibility(View.VISIBLE);
		  for(int i=0;i<list.size();i++)
		  {
			  CheckBox chkbx=new CheckBox(CreateGroupProfile.this);
			  LayoutParams param=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			  param.leftMargin=5;
			  chkbx.setLayoutParams(param);
			  chkbx.setText(list.get(i));
			  tagCheckboxLayout.addView(chkbx);
			  selectedTagArray.add(chkbx);
		  }
		  }
		  else
		  {
			  tagRootLayout.setVisibility(View.GONE);
		  }
		  
		
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGroupNameEditTxt=(EditText) findViewById(R.id.group_name);
		mGroupDes=(EditText) findViewById(R.id.group_des_box);
		mNext=(Button) findViewById(R.id.next);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		gpsTracker=new GPSTracker(this);
		
		
		scrollView=(ScrollView) findViewById(R.id.scroll_view);
		
		mGroupImage=(ImageView) findViewById(R.id.upload_profile_pic);
		uploadLayout=(FrameLayout) findViewById(R.id.image_root_layout);
		placeHolder=(ImageView) findViewById(R.id.placeholder);
		
		categorySpinner=(Spinner) findViewById(R.id.category_spinner);
		categoryLayout=(LinearLayout) findViewById(R.id.category_layout);
		tagRootLayout=(LinearLayout) findViewById(R.id.tag_layout);
		tagCheckboxLayout=(LinearLayout) findViewById(R.id.tag_checkbox_layout);
		
		tagAutoComplete1=(AutoCompleteTextView) findViewById(R.id.auto_complete1);
		tagAutoComplete2=(AutoCompleteTextView) findViewById(R.id.auto_complete2);
		tagAutoComplete3=(AutoCompleteTextView) findViewById(R.id.auto_complete3);
		tagAutoComplete4=(AutoCompleteTextView) findViewById(R.id.auto_complete4);
		childLayout=(LinearLayout) findViewById(R.id.child_tag_layout1);
		addTag=(ImageView) findViewById(R.id.add);
		mandatoryStar=(TextView) findViewById(R.id.star);

		businessLayout=(LinearLayout) findViewById(R.id.business_layout);
		businessNameEditTxt=(EditText) findViewById(R.id.business_name_box);
		businessBranchEditTxt=(EditText) findViewById(R.id.branch_edit_box);
		businessPhoneNoEditTxt=(EditText) findViewById(R.id.business_contact_no);
		addressAutoCompleteView =(AutoCompleteTextView) findViewById(R.id.business_office_address);
		
		mNext.setTypeface(Utility.getTypeface());
		tagAutoComplete1.setTypeface(Utility.getTypeface());
		tagAutoComplete2.setTypeface(Utility.getTypeface());
		tagAutoComplete3.setTypeface(Utility.getTypeface());
		tagAutoComplete4.setTypeface(Utility.getTypeface());
		
		tagAutoComplete1.setFilters((new InputFilter[]{filter}));
		tagAutoComplete2.setFilters((new InputFilter[]{filter}));
		tagAutoComplete3.setFilters((new InputFilter[]{filter}));
		tagAutoComplete4.setFilters((new InputFilter[]{filter}));
		
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_out);
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
				mDialog = new Dialog(CreateGroupProfile.this, R.style.customDialogStyle);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				mDialog.setContentView(R.layout.choose_picture);
				mDialog.setCancelable(true);
				mDialog.setCanceledOnTouchOutside(true);
				mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
				WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
				windowManager.gravity = Gravity.CENTER;
				TextView cameraImage = (TextView) mDialog.findViewById(R.id.take_photo);
				TextView galleryImage = (TextView) mDialog.findViewById(R.id.from_gallery);
				cameraImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						Utility.callCameraIntent(CreateGroupProfile.this);
					}
				});
				galleryImage.setOnClickListener(new View.OnClickListener() {
					@SuppressLint({"InlinedApi", "NewApi"})
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						Crop.pickImage(CreateGroupProfile.this);
					}
				});
				mDialog.show();
			}
		});


		cityAutoCompleteAdapter=new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item);
		cityAutoCompleteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		addressAutoCompleteView.setAdapter(cityAutoCompleteAdapter);

		addressAutoCompleteView.setOnItemClickListener(this);
		
	}
	@SuppressLint("NewApi")
	private void checkDuplicate()
	{
		mGroupName=mGroupNameEditTxt.getText().toString();
		groupDes=mGroupDes.getText().toString();
		businessName=businessNameEditTxt.getText().toString();
		businessBranch=businessBranchEditTxt.getText().toString();
		businessPhoneNumber=businessPhoneNoEditTxt.getText().toString();
		
		if(mGroupName.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.group_name_empty));
		else if(groupDes.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.group_des_empty));
		else if(mGroupImageFile==null)
			Utility.showToastMessage(this, getResources().getString(R.string.group_image_empty));
		else if(businessName.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.business_name_empty));
		else if(businessAddress.isEmpty())
			Utility.showToastMessage(this, getResources().getString(R.string.business_address_empty));

		else
		{
			mNext.setEnabled(false);
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
							mNext.setEnabled(true);
							if(totemList.size() > 0) // already exist 
							{
								mProgressBar.setVisibility(View.GONE);
								Utility.showToastMessage(CreateGroupProfile.this, getResources().getString(R.string.group_name_exist));
							}
							else
							{
								/*slectedTags=new ArrayList<String>();
								mProgressBar.setVisibility(View.GONE);
							if(selectedTagArray!=null)	
							{
								for(int i=0;i<selectedTagArray.size();i++)
								{
									if(selectedTagArray.get(i).isChecked())
										slectedTags.add(selectedTagArray.get(i).getText().toString());
										
								}
							}
							*/	
								//insertValuesInGroupTable();								
								System.out.println("slected tags ::: "+selectedTags);
								ArrayList<String> seleted=new ArrayList<String>();
								String str=tagAutoComplete1.getText().toString().trim();
								if(!str.isEmpty())
								{
									if(!seleted.contains(str))
									{
										seleted.add(str);
									}
									
									if(!selectedTags.contains(str))
									{
										selectedTags.add(str);
									}
								}
								String str1=tagAutoComplete2.getText().toString().trim();
								if(!str1.isEmpty())
								{
									if(!seleted.contains(str1))
									{
										seleted.add(str1);
									}
									
									if(!selectedTags.contains(str1))
									{
										selectedTags.add(str1);
									}
								}
								String str3=tagAutoComplete3.getText().toString().trim();
								if(!str3.isEmpty())
								{
									if(!seleted.contains(str3))
									{
										seleted.add(str3);
									}
									
									if(!selectedTags.contains(str3))
									{
										selectedTags.add(str3);
									}
								}
								String str4=tagAutoComplete4.getText().toString().trim();
								if(!str4.isEmpty())
								{
									if(!seleted.contains(str4))
									{
										seleted.add(str4);
									}
									
									if(!selectedTags.contains(str4))
									{
										selectedTags.add(str4);
									}
								}
								System.out.println("user selected tags "+seleted);
								System.out.println("final selected tags "+selectedTags);
								System.out.println("selcted purpose object id :: "+purposeId);
								insertValuesInGroupTable(seleted);
								/*Intent i=new Intent(CreateGroupProfile.this,PublicGroupLocationActivity.class);
								i.putExtra(Constants.GROUP_NAME, mGroupName);
								i.putExtra(Constants.GROUP_DESCRIPTION, groupDes);
								startActivity(i);
								overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);*/
							}
						}
						else
						{
							mProgressBar.setVisibility(View.GONE);
							Utility.showToastMessage(CreateGroupProfile.this, getResources().getString(R.string.server_issue));
						}
					}
			});
		}
	}
	
	private void insertValuesInGroupTable(ArrayList<String> tagList) {
		
		System.out.println("selcted purpose object id :: "+purposeId);
		
		 groupInfo.setGroupName(mGroupName);
		 groupInfo.setGroupDes(groupDes);
		 groupInfo.setGroupCategory(selectedCategory);
		 groupInfo.setGroupPurpose(purpose);
		 groupInfo.setGroupType(groupType);
		 groupInfo.setGroupLargeImage(mGroupImageFile);
		 groupInfo.setGroupThumbnailImage(mGroupImageTumbnailFile);
		 groupInfo.setTagsList(tagList);
		 groupInfo.setRangeList(allowedRange);

		if(isBusinessGroup) {
			groupInfo.setBusinessName(businessName);
			groupInfo.setBusinessAddress(businessAddress);
			groupInfo.setBusinessPhoneNo(businessPhoneNumber);
			groupInfo.setIfBusinessGroup(true);
			groupInfo.setBusinessBranch(businessBranch);
		}
		 
		 Utility.setGroupInfo(groupInfo);
		 if(groupType.equals(Constants.SECRET_GROUP))
			{
     		 	Intent i=new Intent(CreateGroupProfile.this,SecretGroupSettingsActivity.class);
  				startActivity(i);
  				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
			else
			{
				Intent i=new Intent(CreateGroupProfile.this,CreateGroupSettingsActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
	if(!groupType.equals(Constants.SECRET_GROUP))
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_CATEGORY_TAGS_TABLE);
			query.whereEqualTo(Constants.OBJECT_ID, purposeId);
			query.whereEqualTo(Constants.GROUP_CATEGORY, selectedCategory);
			query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(final ParseObject object, ParseException e) {
						if (e == null) 
						{
							if(object!=null)
							{
								//ArrayList<String> tagList=object.getList(Constants.GROUP_TAG_ARRAY);
								object.put(Constants.GROUP_TAG_ARRAY, selectedTags);
								object.saveInBackground();
							}
							
						}
				}});
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
				mandatoryStar.setVisibility(View.GONE);
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
		//city=strArray[0];
		//state=strArray[1];
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

				ParseGeoPoint point =new ParseGeoPoint(lat, lng);
				groupInfo.setBusinessLocationPoint(point);
				System.out.println("lat and lng ::: " + lat + "\t" + lng);
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Cannot process JSON results", e);
			}
			return true;
		}
	}


}
