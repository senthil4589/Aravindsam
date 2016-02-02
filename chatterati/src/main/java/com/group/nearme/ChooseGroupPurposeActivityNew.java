package com.group.nearme;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.GroupPurposeGridAdapter;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;

@SuppressLint("NewApi")
public class ChooseGroupPurposeActivityNew extends Activity
{
	private ImageView mBackImg;
	private GridView mGridView;
	private GroupPurposeGridAdapter mAdapter;
	//private List<ParseObject> purposeList=new ArrayList<ParseObject>();
	private ProgressBar mProgressBar;
	public static Activity activity;
	private ArrayList<String> purposeList1=new ArrayList<String>();
	private ArrayList<String> purposeIDList=new ArrayList<String>();
	private ArrayList<String> colorCodeList=new ArrayList<String>();
	private ArrayList<String> purposeDesList=new ArrayList<String>();
	private ArrayList<Integer> purposeThumbImgList=new ArrayList<Integer>();
	private ArrayList<Integer> purposeLargeImgList=new ArrayList<Integer>();
	private ArrayList<ArrayList<String>> allowedRangeList=new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> allowedTypeList=new ArrayList<ArrayList<String>>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_purpose_grid_view);
		Utility.getTracker(this, "CREATE GROUP - PURPOSE SCREEN");
		initViews();
		activity=this;
		//mProgressBar.setVisibility(View.VISIBLE);
		/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_PURPOSE_TABLE);
		query.orderByAscending("createdAt");
		query.fromPin(Constants.GROUP_PURPOSE_TABLE);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(final List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							purposeList=list;
							//setValues();
							setAdapter();
						}
						else
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_PURPOSE_TABLE);
							query.orderByAscending("createdAt");
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list1, ParseException e) {
										if (e == null) 
										{
											if(list1.size() > 0)
											{
												ParseObject.pinAllInBackground(Constants.GROUP_PURPOSE_TABLE,list1);
												purposeList=list;
												//setValues();
												setAdapter();
											}
										}
										else
											mProgressBar.setVisibility(View.GONE);
								}
							});
						}
						
					}
					else
						mProgressBar.setVisibility(View.GONE);
			}});
		
		*/
		mGridView.setOnItemClickListener(new ListView.OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View view, final int position,
					long arg3) 
			{
			 callChooseGroupTypeActivity(position);
			}
		});
		

	
	}
	private void callChooseGroupTypeActivity(int position)
	{
		boolean flag;
		if(allowedTypeList.get(position).size() == 1 && 
				allowedTypeList.get(position).get(0).equals("Secret")	)
		{
			flag=true;
		}
		else
		{
			flag=false;
		}
		Intent intent=new Intent(this,ChooseGroupTypeActivity.class);
		intent.putExtra(Constants.GROUP_PURPOSE_ID, purposeIDList.get(position));
		intent.putExtra(Constants.GROUP_PURPOSE, purposeList1.get(position));
		intent.putExtra(Constants.GROUP_PURPOSE_LARGE_IMAGE, (int)purposeLargeImgList.get(position));
		intent.putExtra(Constants.GROUP_PURPOSE_DESCRIPTION, purposeDesList.get(position));
		intent.putStringArrayListExtra(Constants.GROUP_PURPOSE_ALLOWED_RANGE,   allowedRangeList.get(position));
		intent.putExtra("flag", flag);
		startActivity(intent);
		overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	}
	
	private void setAdapter()
	{
		//System.out.println("name list ::: "+purposeList1);
		//System.out.println("image list ::: "+purposeThumbImgList);
		//System.out.println("name list ::: "+purposeList1);
	    mAdapter=new GroupPurposeGridAdapter(ChooseGroupPurposeActivityNew.this, purposeList1,colorCodeList,purposeThumbImgList);
		mGridView.setAdapter(mAdapter);
		mProgressBar.setVisibility(View.GONE);
		
		//mGridView.getColumnWidth()
	}
	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}

	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGridView=(GridView) findViewById(R.id.gridview);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		
		purposeList1.add("College and School Groups");
		purposeList1.add("Interest Groups");
		purposeList1.add("Activity Groups");
		purposeList1.add("Events and Conferences");
		purposeList1.add("Photo Sharing Groups");
		purposeList1.add("Work and Office");
		purposeList1.add("Apartments and Condos");
		purposeList1.add("Buy Sell Trade");
		purposeList1.add("Secret Groups");
		//purposeList1.add("Causes");
		purposeList1.add("Business Groups");
		
		purposeIDList.add("F1Ah2rvqPv");
		purposeIDList.add("uSjtUpPivu");
		purposeIDList.add("NtIYe4ss87");
		purposeIDList.add("ErabvdAtUL");
		purposeIDList.add("5Y63nkfZOK");
		purposeIDList.add("vFrIlDDvsA");
		purposeIDList.add("bd7VbUG1nw");
		purposeIDList.add("bUF4FPtEGp");
		purposeIDList.add("BYFIVRAAlW");
		//purposeIDList.add("see3eWtHaA");
		purposeIDList.add("F6U9F4OUOl");

		purposeDesList.add("Groups are pinned at campus and only visible nearby. Create groups for class, events and tests, projects and activities. Mobile numbers are not shared with group members.");
		purposeDesList.add("Gather people that share a common interest. Food, Music, Eating Out, Health, DIY, Cooking and Recipes, Beauty, Graphics, Aero modeling. Discuss, share and plan meet ups with people like you nearby and in the city.");
		purposeDesList.add("Gather your activities group instantly. Yoga, soccer, biking, baking, rock climbing, scuba diving or any group activity. Make announcements, share schedules, discuss and share photos.");
		purposeDesList.add("Increase engagement at your event. Corporate and consumer events, private parties, weddings, seminars, conferences, exhibitions, fairs etc. Groups are pinned at the event location and visible to attendees.");
		purposeDesList.add("Share photos with a group of people. At your office, college, apartment, locality etc. Groups are pinned at a location and visible to people nearby.");
		purposeDesList.add("Create groups for sales, marketing, diversity, functional teams, office projects and office events. Groups are pinned at your office and your colleagues can find and join easily.");
		purposeDesList.add("Create announcement groups to alert all residents on important issues. Create groups for cultural activities, sports, senior citizens, prayer meetings, classes etc.");
		purposeDesList.add("Buy, Sell or Trade anything with others in your neighborhood, at your campus or in your city.");
		purposeDesList.add("Create a secret group and invite friends, family or your clique to join. Secret groups cannot be discovered and are invite only.");
		//purposeDesList.add("Create groups for causes that you are passionate about - civic issues, volunteering, environment, mentoring, child health, giving, social causes, political causes etc.");
		//purposeDesList.add("Create groups for causes that you are passionate about - civic issues, volunteering, environment, mentoring, child health, giving, social causes, political causes etc.");
		purposeDesList.add("Customised for the needs of small businesses. This group allows business owners to post their products and service with price, discounts etc. Also allows business owners to add contact information making it easier for consumers to reach them.");
		
		colorCodeList.add("#9c1d2f");
		colorCodeList.add("#933f19");
		colorCodeList.add("#229ade");
		colorCodeList.add("#666666");
		colorCodeList.add("#165382");
		colorCodeList.add("#258358");
		colorCodeList.add("#996f27");
		colorCodeList.add("#322c2c");
		colorCodeList.add("#954b40");
		colorCodeList.add("#e17744");
		
		purposeThumbImgList.add(R.drawable.college_thumb);
		purposeThumbImgList.add(R.drawable.interest_thumb);
		purposeThumbImgList.add(R.drawable.acitivity_thumb);
		purposeThumbImgList.add(R.drawable.conference_thumb);
		purposeThumbImgList.add(R.drawable.photoshare_thumb);
		purposeThumbImgList.add(R.drawable.office_thumb);
		purposeThumbImgList.add(R.drawable.apartment_thumb);
		purposeThumbImgList.add(R.drawable.buy_sell_thumb);
		purposeThumbImgList.add(R.drawable.secret_thumb);	
		//purposeThumbImgList.add(R.drawable.causes_thumb);
		purposeThumbImgList.add(R.drawable.business_thumb);
		
		purposeLargeImgList.add(R.drawable.college_large);
		purposeLargeImgList.add(R.drawable.interest_large);
		purposeLargeImgList.add(R.drawable.activity_large);
		purposeLargeImgList.add(R.drawable.conference_large);
		purposeLargeImgList.add(R.drawable.photoshare_large);
		purposeLargeImgList.add(R.drawable.office_large);
		purposeLargeImgList.add(R.drawable.apartment_large);
		purposeLargeImgList.add(R.drawable.buy_sell_large);
		purposeLargeImgList.add(R.drawable.secret_large);	
		//purposeLargeImgList.add(R.drawable.causes_large);
		purposeLargeImgList.add(R.drawable.business_large);
		
		ArrayList<String> range=new ArrayList<String>();
		range.add("Nearby");
		ArrayList<String> range1=new ArrayList<String>();
		range.add("Nearby");
		range.add("City");
		allowedRangeList.add(range);
		allowedRangeList.add(range1);
		allowedRangeList.add(range1);
		allowedRangeList.add(range1);
		allowedRangeList.add(range1);
		allowedRangeList.add(range);
		allowedRangeList.add(range);
		allowedRangeList.add(range1);
		allowedRangeList.add(new ArrayList<String>());
		allowedRangeList.add(range1);
		allowedRangeList.add(range1);





		ArrayList<String> type=new ArrayList<String>();
		type.add("Open");
		type.add("Private");
		ArrayList<String> typeSecret=new ArrayList<String>();
		typeSecret.add("Secret");
		
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		allowedTypeList.add(typeSecret);
		allowedTypeList.add(type);
		allowedTypeList.add(type);
		
		
		setAdapter();
		
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
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
