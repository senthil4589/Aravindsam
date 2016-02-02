package com.group.nearme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.objects.GroupInfo;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChooseGroupTypeActivity extends Activity
{
	private ImageView mBackImg,mLargeImage;
	private RadioGroup mGroupTypeRadioGroup;
	private RadioButton mOpenTypeRadioBtn,mPrivateRadioBtn,mSecretTypeRadioBtn;
	private TextView mGroupPurposeTxtView,mGroupPurposeDesTxtView,mGroupTypeDes;
	private Button mNextBtn;
	private RelativeLayout layout;
	private ProgressBar progressBar;
	private String purposeID="",purposeTile="",purposeDes="",largeImageUrl="",groupType="Open";
	private boolean isSecret;
	private ArrayList<String> allowedRangeList=new ArrayList<String>();
	private ArrayList<String> categoryList;
	public static HashMap<String,ArrayList<String>> tagsMap=new HashMap<String,ArrayList<String>>();
	public static ArrayList<String> objectIDList=new ArrayList<String>();
	public static Activity activity;
	int imageResId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_group_type);
		Utility.getTracker(this, "CREATE GROUP - GROUP TYPE SCREEN");
		initViews();
		activity=this;
		purposeID=getIntent().getStringExtra(Constants.GROUP_PURPOSE_ID);
		purposeTile=getIntent().getStringExtra(Constants.GROUP_PURPOSE);
		purposeDes=getIntent().getStringExtra(Constants.GROUP_PURPOSE_DESCRIPTION);
		isSecret=getIntent().getBooleanExtra("flag", false);
		imageResId=getIntent().getIntExtra(Constants.GROUP_PURPOSE_LARGE_IMAGE,0);
		allowedRangeList=getIntent().getStringArrayListExtra(Constants.GROUP_PURPOSE_ALLOWED_RANGE);

		mGroupPurposeTxtView.setText(purposeTile.toUpperCase());
		mGroupPurposeDesTxtView.setText(purposeDes);
		
		//Picasso.with(this).load(largeImageUrl).placeholder(R.drawable.group_image).into(mLargeImage);
		mLargeImage.setImageResource(imageResId);
		
		if(isSecret)
			layout.setVisibility(View.GONE);
		
		mGroupTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            // checkedId is the RadioButton selected
	            RadioButton rb=(RadioButton)findViewById(checkedId);
	            groupType=rb.getText().toString();
	            if(rb.getText().toString().equals(Constants.OPEN_GROUP))
	            	mGroupTypeDes.setText(getResources().getString(R.string.public_group_des));
	            else if(rb.getText().toString().equals(Constants.PRIVATE_GROUP))
	            	mGroupTypeDes.setText(getResources().getString(R.string.private_group_des));
	           /* else
	            	mGroupTypeDes.setText(getResources().getString(R.string.secret_group_des)+"          ");*/
	        }
	    });
		
		mNextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(isSecret)
				{
					Intent i=new Intent(ChooseGroupTypeActivity.this,CreateGroupProfile.class);
					i.putExtra(Constants.GROUP_TYPE,Constants.SECRET_GROUP);
					i.putExtra("flag", false);
					i.putStringArrayListExtra(Constants.GROUP_PURPOSE_ALLOWED_RANGE, new ArrayList<String>());
					i.putExtra(Constants.GROUP_PURPOSE, purposeTile);
					startActivity(i);	
				
				}
				else
				{
					progressBar.setVisibility(View.VISIBLE);
				ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_CATEGORY_TAGS_TABLE);
				query.whereEqualTo(Constants.GROUP_PURPOSE_ID_STRING, purposeID);
				query.orderByDescending("createdAt");
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> list, ParseException e) {
							if (e == null) 
							{
								if(list.size() > 0) // already exist 
								{
									objectIDList=new ArrayList<String>();
									categoryList=new ArrayList<String>();
									tagsMap=new HashMap<String, ArrayList<String>>();
									for(int i=0;i<list.size();i++)
									{
										objectIDList.add(list.get(i).getObjectId());
										ArrayList<String> tag=(ArrayList<String>) list.get(i).get(Constants.GROUP_TAG_ARRAY);
										if(list.get(i).getString(Constants.GROUP_CATEGORY).isEmpty())
										{
											//categoryList.add(list.get(i).getString(Constants.GROUP_CATEGORY));
											tagsMap.put("empty",tag);
										}
										else
										{
											categoryList.add(list.get(i).getString(Constants.GROUP_CATEGORY));
											tagsMap.put(list.get(i).getString(Constants.GROUP_CATEGORY),tag);
										}
										
									}
									Intent i=new Intent(ChooseGroupTypeActivity.this,CreateGroupProfile.class);
									if(isSecret)
										i.putExtra(Constants.GROUP_TYPE,Constants.SECRET_GROUP);
									else
										i.putExtra(Constants.GROUP_TYPE,groupType);
									i.putExtra("flag", true);
									i.putStringArrayListExtra(Constants.GROUP_CATEGORY, categoryList);
									i.putStringArrayListExtra(Constants.GROUP_PURPOSE_ALLOWED_RANGE, allowedRangeList);
									i.putExtra(Constants.GROUP_PURPOSE, purposeTile);
									startActivity(i);
									overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
									
									progressBar.setVisibility(View.GONE);
								}
								else
								{
									Intent i=new Intent(ChooseGroupTypeActivity.this,CreateGroupProfile.class);
									if(isSecret)
										i.putExtra(Constants.GROUP_TYPE,Constants.SECRET_GROUP);
									else
										i.putExtra(Constants.GROUP_TYPE,groupType);
									i.putExtra("flag", false);
									i.putStringArrayListExtra(Constants.GROUP_PURPOSE_ALLOWED_RANGE, allowedRangeList);
									i.putExtra(Constants.GROUP_PURPOSE, purposeTile);
									startActivity(i);	
									progressBar.setVisibility(View.GONE);
								}
							}
							else
							{
								/*Intent i=new Intent(ChooseGroupTypeActivity.this,CreateGroupProfile.class);
								i.putExtra(Constants.GROUP_TYPE,groupType);
								startActivity(i);
								
								progressBar.setVisibility(View.GONE);*/
							}
					}});
				
				}
				
				/*if(allowedRangeList.size() > 0)
				{
					if(allowedRangeList.size() == 1 && allowedRangeList.get(0).equals("Nearby"))
					{
						if(groupType.equals(Constants.OPEN_GROUP))
						{
							
						}
						else if(groupType.equals(Constants.PRIVATE_GROUP))
						{
							
						}
						else if(groupType.equals(Constants.SECRET_GROUP))
						{
							
						}
						
					}
					else if(allowedRangeList.size() == 2)
					{
						
					}
				}
				else
				{
					
				}*/
			}
		});
		
		
	
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mLargeImage=(ImageView) findViewById(R.id.large_image);
		mGroupTypeRadioGroup=(RadioGroup) findViewById(R.id.group_type_radio_group);
		mGroupPurposeTxtView=(TextView) findViewById(R.id.group_purpose);
		mGroupTypeDes=(TextView) findViewById(R.id.group_type_des);
		mGroupPurposeDesTxtView=(TextView) findViewById(R.id.group_purpose_des);
		mOpenTypeRadioBtn=(RadioButton) findViewById(R.id.open_group);
		mPrivateRadioBtn=(RadioButton) findViewById(R.id.private_group);
		mSecretTypeRadioBtn=(RadioButton) findViewById(R.id.secret_group);
		mNextBtn=(Button) findViewById(R.id.next);
		layout=(RelativeLayout) findViewById(R.id.group_type_layout);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		mOpenTypeRadioBtn.setTypeface(Utility.getTypeface());
		mPrivateRadioBtn.setTypeface(Utility.getTypeface());
		mSecretTypeRadioBtn.setTypeface(Utility.getTypeface());
		mNextBtn.setTypeface(Utility.getTypeface());

		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
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
