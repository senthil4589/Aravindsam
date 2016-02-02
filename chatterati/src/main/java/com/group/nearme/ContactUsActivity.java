package com.group.nearme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.VideoView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class ContactUsActivity extends Activity
{
	EditText desEditTxt;
	Spinner categorySpinner;
	Button done;
	private ImageView mBackImg;
	String des,category;
	ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_us);
		Utility.getTracker(this, "CONTACT US SCREEN");
		initViews();
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,  R.array.category_array, R.layout.contact_us_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);
		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			  public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			           
			    }
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
		    });		        
		
		done.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				 des=desEditTxt.getText().toString();
				 category = categorySpinner.getSelectedItem().toString();
				if(category.equals("Choose Category"))
				{
					Utility.showToastMessage(ContactUsActivity.this, getResources().getString(R.string.contact_us_empty_category));
				}
				else if(des.isEmpty())
				{
					Utility.showToastMessage(ContactUsActivity.this, getResources().getString(R.string.contact_us_feedback_empty));
				}
				else
				{
					progressBar.setVisibility(View.VISIBLE);
					ParseObject userObject = new ParseObject("ContactUS");
					userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
					userObject.put("Category", category);
					userObject.put("Description", des);
					userObject.saveInBackground(new SaveCallback() {
				          public void done(ParseException e) {
				                 if (e == null) {
				                	 progressBar.setVisibility(View.GONE);
				                	 Utility.showToastMessage(ContactUsActivity.this, getResources().getString(R.string.contact_us_request_sent_success));
				                	 //Toast.makeText(ContactUsActivity.this, "Your request sent successfully", Toast.LENGTH_LONG).show();
				                	 desEditTxt.setText("");
				                	// finish();
				                 }
				                 else
				                	 progressBar.setVisibility(View.GONE);
				          }});
				}
			}		
			});    
	}

	private void initViews() {
		categorySpinner=(Spinner) findViewById(R.id.category_spinner);
		desEditTxt=(EditText) findViewById(R.id.description_box);
		mBackImg=(ImageView) findViewById(R.id.back);
		done=(Button) findViewById(R.id.send);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
		done.setTypeface(tf);
		desEditTxt.setTypeface(tf);
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