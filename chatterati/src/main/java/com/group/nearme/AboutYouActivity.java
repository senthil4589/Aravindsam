package com.group.nearme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.group.nearme.image.Crop;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

@SuppressLint("NewApi")
public class AboutYouActivity extends Activity {
	private TextView mDoneTxtView;
	private EditText mNameEditTxt,mEmailEditTxt;
	private ImageView mUploadImgView,placeHolder;
	private ParseFile mProfileImgFile,mProfileImageTumbnailFile=null;
	private static String mGender="Male";
	private String mMobileNo="",mCountryName="",countryCode="";
	private ArrayList<String> list=new ArrayList<String>();
	private RadioGroup genderGroup;
	private RadioButton r1,r2;
	private FrameLayout uploadLayout;
	TextView spannableText;
	private Dialog mDialog;
	private ProgressBar progressBar;
	private Spinner mAgeSpinner,mOccupationSpinner;
	private GPSTracker gpsTracker;
	private String city="Bengaluru",state="Karnataka";
	
	//private static final String LOG_TAG = "ExampleApp";

	//private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	//private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
//	private static final String OUT_JSON = "/json";

	//------------ make your specific key ------------
	//private static final String API_KEY = "AIzaSyBRX3Xq0DTBhqpW2Z-Ns8hkELbipFSwgLQ";
	//private AutoCompleteTextView cityAutoCompleteTxtView;
	//GooglePlacesAutocompleteAdapter cityAutoCompleteAdapter;
	Tracker tracker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sign_up_user_info);
		tracker=Utility.getTracker(this, "ABOUT YOU SCREEN");

		initViews();
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.age_group__array, R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mAgeSpinner.setAdapter(adapter);
		
		//cityAutoCompleteAdapter=new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//cityAutoCompleteTxtView.setAdapter(cityAutoCompleteAdapter);
		
		//cityAutoCompleteTxtView.setOnItemClickListener(this);
		
		mAgeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			 @Override
	            public void onItemSelected(AdapterView<?> arg0, View arg1,
	                    int position, long arg3) {
				 Object item = arg0.getItemAtPosition(position);
				 if(item.toString().equals("15 to 20"))
				 {
					 setOccupationAdapter(getResources().getStringArray(R.array.occupation1_array));
				 }
				 else if(item.toString().equals("56 and above"))
				 {
					 setOccupationAdapter(getResources().getStringArray(R.array.occupation3_array));
				 }
				 else
				 {
					 setOccupationAdapter(getResources().getStringArray(R.array.occupation2_array));
				 }
				 
			 }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
	}
	
	private void setOccupationAdapter(String[] strings)
	{
		ArrayAdapter<CharSequence> adapter =new ArrayAdapter<CharSequence>(this,  R.layout.simple_spinner_item, strings) ;
				
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mOccupationSpinner.setAdapter(adapter);
	}

	private void initViews() {
		mDoneTxtView=(TextView) findViewById(R.id.done);
		spannableText=(TextView) findViewById(R.id.spannable_text);
		mNameEditTxt=(EditText) findViewById(R.id.name);
		mEmailEditTxt=(EditText) findViewById(R.id.email);
		mUploadImgView=(ImageView) findViewById(R.id.upload_profile_pic);
		placeHolder=(ImageView) findViewById(R.id.placeholder);
		genderGroup=(RadioGroup) findViewById(R.id.gender);
		uploadLayout=(FrameLayout) findViewById(R.id.image_root_layout);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		//uploadLabel=(TextView) findViewById(R.id.upload_label);
		mAgeSpinner=(Spinner) findViewById(R.id.age_spinner);
		mOccupationSpinner=(Spinner) findViewById(R.id.occupation_spinner);
		r1=(RadioButton) findViewById(R.id.male);
		r2=(RadioButton) findViewById(R.id.female);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		mNameEditTxt.setTypeface(tf);
		mEmailEditTxt.setTypeface(tf);
		r1.setTypeface(tf);
		r2.setTypeface(tf);
		
		//cityAutoCompleteTxtView=(AutoCompleteTextView) findViewById(R.id.city_auto_complete);
		
		
		mMobileNo=getIntent().getStringExtra(Constants.MOBILE_NO);
		mCountryName=getIntent().getStringExtra(Constants.COUNTRY_NAME);
		
		if(mCountryName.equals("US") || mCountryName.equals("USA"))
			countryCode="us";
		else
			countryCode="in";
			
		
		spannableText.setMovementMethod(LinkMovementMethod.getInstance());
		spannableText.setText(addClickablePart(getResources().getString(R.string.terms_and_privacy),41,58,62,76), BufferType.SPANNABLE);
		uploadLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDialog = new Dialog(AboutYouActivity.this,R.style.customDialogStyle);
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
								Utility.callCameraIntent(AboutYouActivity.this);
							}
						});
						galleryImage.setOnClickListener(new View.OnClickListener() {
							@SuppressLint({ "InlinedApi", "NewApi" })
							@Override
							public void onClick(View v) {
								  mDialog.dismiss();
								  Crop.pickImage(AboutYouActivity.this);
							}
						});
						mDialog.show();
					}
				});		
				
				mDoneTxtView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//mDoneTxtView.setEnabled(false);
						callTabGroupActivity();
					}
				});
				
				
	}
	
	private SpannableStringBuilder addClickablePart(String str,int idx1,int idx2,int idx3,int idx4) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);
      
	    ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openWebView("Terms of Service",Constants.TERMS_URL);
            }
        }, idx1, idx2, 0);

        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openWebView("Privacy Policy",Constants.PRIVACY_URL);
            }
        }, idx3, idx4, 0);
	    return ssb;
	}
	
	private void openWebView(String title, String url) {
		startActivity(new Intent(AboutYouActivity.this,WebViewActivity.class).putExtra("title", title).putExtra("url", url));
		overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	}	
	public boolean isValidEmail(CharSequence target) {
		  return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}
	 private void callTabGroupActivity() {
		final String name=mNameEditTxt.getText().toString();
		String email=mEmailEditTxt.getText().toString();
		
		if(name.isEmpty())
			Utility.showToastMessage(AboutYouActivity.this, getResources().getString(R.string.user_name_empty));
		else if(email.isEmpty())
			Utility.showToastMessage(AboutYouActivity.this, getResources().getString(R.string.email_empty));
		else if(mProfileImgFile==null)
			Utility.showToastMessage(AboutYouActivity.this, getResources().getString(R.string.user_image_empty));
		//else if(city.isEmpty())
		//	Utility.showToastMessage(AboutYouActivity.this,"Please enter your city");
		else if(!isValidEmail(email))
			Utility.showToastMessage(AboutYouActivity.this,"Please enter valid email");
		else
		{
			progressBar.setVisibility(View.VISIBLE);
			mDoneTxtView.setEnabled(false);
			int selectedId = genderGroup.getCheckedRadioButtonId();
			RadioButton radioSexButton = (RadioButton) findViewById(selectedId);
			mGender=radioSexButton.getText().toString();
			
			final ParseObject userObject = new ParseObject(Constants.USER_TABLE);
			userObject.put(Constants.USER_NAME, name);
			userObject.put(Constants.MOBILE_NO, mMobileNo);
			userObject.put(Constants.EMAIL, email);
			userObject.put(Constants.GENDER, mGender);
			userObject.put(Constants.PROFILE_PICTURE, mProfileImgFile);
			userObject.put(Constants.THUMBNAIL_PICTURE, mProfileImageTumbnailFile);
			userObject.put(Constants.COUNTRY_NAME, mCountryName);
			userObject.put(Constants.NAME_CHANGE_COUNT, 0);
			userObject.put(Constants.BADGE_POINT, 0);
			userObject.put(Constants.USER_STATE, "Active");
			userObject.put(Constants.PUSH_NOTIFICATION, true);
			userObject.put(Constants.SOUND_NOTIFICATION, true);
			userObject.put(Constants.GROUP_INVITATION, list);
			userObject.put(Constants.MY_GROUP_ARRAY, list);
			//userObject.put(Constants.MUTE_GROUP_ARRAY, list);
			userObject.put(Constants.POST_FLAG_COUNT, 0);
			userObject.put(Constants.SUSPENDED, false);
			userObject.put(Constants.USER_AGE, mAgeSpinner.getSelectedItem().toString());
			if(state!=null)
				userObject.put(Constants.USER_LOCATION_STATE, state);
			else
				userObject.put(Constants.USER_LOCATION_STATE, "");
			if(city!=null)
				userObject.put(Constants.USER_LOCATION_CITY, city);
			else
				userObject.put(Constants.USER_LOCATION_CITY, "");
			userObject.put(Constants.USER_OCCUPATION, mOccupationSpinner.getSelectedItem().toString());
			//userObject.put(Constants.UPDATE_IMAGE, true);
		//	userObject.put(Constants.UPDATE_NAME, true);
			userObject.saveInBackground(new SaveCallback() {
		          public void done(ParseException e) {
		                 if (e == null) {
		                	 PreferenceSettings.setUserID(userObject.getObjectId());
		                	 userObject.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
		                	 PreferenceSettings.setMobileNo(mMobileNo);
		         			PreferenceSettings.setCountry(mCountryName);
		         			PreferenceSettings.setUserName(name);
		         			Utility.setUserImageFile(mProfileImgFile);
		         			PreferenceSettings.setProfilePic(mProfileImgFile.getUrl());
		         			PreferenceSettings.setGender(mGender);
		         			PreferenceSettings.setPushStatus(true);
		         			PreferenceSettings.setSoundStatus(true);
		         			PreferenceSettings.setNameChangeCount(0);
		         			PreferenceSettings.setUserState("Active");
		         			PreferenceSettings.setLoginStatus(true);
		         			PreferenceSettings.setGroupInvitationList(list);
		         			Intent intent = new Intent(AboutYouActivity.this,TabGroupActivity.class);
		         			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		         			startActivity(intent);
		         			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
		         			progressBar.setVisibility(View.GONE);
		         			finish();
		                 }
		                 else
		                 {
		                	 System.out.println("E____________:"+e);
		                 }
		          }
		          }
			);
			
			
			
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
	            mProfileImgFile = new ParseFile("profile.png", img);
 				mProfileImgFile.saveInBackground();
 				placeHolder.setVisibility(View.GONE);
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
		super.onResume();
		gpsTracker = new GPSTracker(this);
		 if (gpsTracker.canGetLocation())
		 {
			 city=gpsTracker.getLocality(this);
			 state=gpsTracker.getState(this);
			 System.out.println("addaress line & locality ::: "+gpsTracker.getState(this)+"   "+gpsTracker.getLocality(this));
		 }
		 else
		 {
			 gpsTracker.showSettingsAlert(this);
		 }
	}
	 
/*	 class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
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
				System.out.println("country code"+countryCode);
				//String code="&components=country:"+countryCode;
				StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
				sb.append("?key=" + API_KEY);
				sb.append("&components=country:"+countryCode);
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
					String[] str=predsJsonArray.getJSONObject(i).getString("description").split(",");
					
					String city=str[0]+","+str[1];
					resultList.add(city);
				}
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Cannot process JSON results", e);
			}

			return resultList;
		}
	 
	 public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			String str = (String) adapterView.getItemAtPosition(position);
			String strArray[]=str.split(",");
			city=strArray[0];
			state=strArray[1];
			Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
		}*/
	 
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