package com.group.nearme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.ContactListAdapter;
import com.group.nearme.objects.ContactInfoObject;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.services.SendInvitationIndia;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ContactListActivity extends Activity {
	private ImageView mBackImg,mEnterNoImg;
	private ListView listView;
	private ContactListAdapter mAdapter;
	private EditText searchEditTxt;
	Button inviteBtn;
	String number="";
	ParseObject groupObject;
	public LinearLayout layout;
	public TextView selectedContactTxt;
	ProgressBar progressbar;
	 ArrayList<ContactInfoObject> list;
	 HashMap<String, Boolean> checkedMap=new HashMap<String, Boolean>();
	 ArrayList<String> selectedMobileList;
	 ParseGeoPoint mLocationPoint;
	 private GPSTracker gpsTracker;
	 private static final String[] PROJECTION = new String[] {
		    ContactsContract.Contacts.DISPLAY_NAME,
		    ContactsContract.Contacts.HAS_PHONE_NUMBER,
		    ContactsContract.CommonDataKinds.Phone.NUMBER,
		    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
		};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_listview);
		Utility.getTracker(this, "INVITE MEMBER - PHONEBOOK LIST SCREEN");
		initViews();
		groupObject=Utility.getGroupObject();
		
		
		searchEditTxt.addTextChangedListener(new TextWatcher() {
		    @Override
		    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
		    		 mAdapter.filter(String.valueOf(cs));
		    }
		     
		    @Override
		    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		            int arg3) {
		    }
		     
		    @Override
		    public void afterTextChanged(Editable arg0) {
		    }
		});
		
		
		inviteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 selectedMobileList=new ArrayList<String>();
				
				for(int i=0;i<mAdapter.selectedPhoneNo.size();i++)
				{
					
					String result = mAdapter.selectedPhoneNo.get(i).replaceAll("[-+.^:,]","");
					String finalNo=result.replace(" ", "");
					System.out.println("finalNo  :: "+finalNo);
					
						if(finalNo.length()==10)
						{
							String no="+91"+finalNo;
							if(!groupObject.getList(Constants.GROUP_MEMBERS).contains(no))
							{
								System.out.println("inside ");
								if(number.isEmpty())
									number=no;
								else
									number=number+","+no;
								selectedMobileList.add(no);
							}
						}
						else if(finalNo.length() == 11)
						{
							String no="+91"+finalNo.substring(1, 11);
							if(!(no.equals(groupObject.get(Constants.MOBILE_NO).toString()) || groupObject.getList(Constants.GROUP_MEMBERS).contains(no)))
							{	
							if(number.isEmpty())
								number=no;
							else
								number=number+","+no;
							selectedMobileList.add(no);
							}
						}
						else if(finalNo.length() == 12)
						{
							String no="+91"+finalNo.substring(2, 12);
							if(!(no.equals(groupObject.get(Constants.MOBILE_NO).toString()) || groupObject.getList(Constants.GROUP_MEMBERS).contains(no)))
							{
							if(number.isEmpty())
								number=no;
							else
								number=number+","+no;
							selectedMobileList.add(no);
							}
						}
					
				}
				
				System.out.println("numberr ::: "+number);
				//String num="+918124277853"+","+"+919710577519";
				
				if(mAdapter.selectedPhoneNo.size()==0)
				{
					Utility.showToastMessage(ContactListActivity.this, "Please select anyone to invite");
				}
				
				else if(mAdapter.selectedPhoneNo.size()==1)
				{
					if(selectedMobileList.size() > 0)
					{
						new SendInvitationIndia(PreferenceSettings.getUserName(),groupObject.getString(Constants.GROUP_NAME), groupObject.getObjectId(), number).execute();
						insertInvitation();
					}
					else
					{
						Utility.showToastMessage(ContactListActivity.this, "Invitation has been sent to "+mAdapter.selectedPhoneNo.size()+" user");
						finish();
					}
				}
				else
				{
					if(selectedMobileList.size() > 0)
					{
						new SendInvitationIndia(PreferenceSettings.getUserName(),groupObject.getString(Constants.GROUP_NAME), groupObject.getObjectId(), number).execute();
						insertInvitation();
					}
					else
					{
						Utility.showToastMessage(ContactListActivity.this, "Invitation has been sent to "+mAdapter.selectedPhoneNo.size()+" users");
						finish();
					}
				}
			}
		});
		
		mEnterNoImg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ContactListActivity.this,InviteSecretGroupActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
		});
		
		/*listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> myAdapter, View myView, int pos, long mylng) {

            	CheckBox checkBox=(CheckBox) myView.findViewById(R.id.chk);
            	if(checkBox)
              }
          });*/
		/*
		if(Utility.getContactList().size() > 0)
		{
			 mAdapter=new ContactListAdapter(ContactListActivity.this,Utility.getContactList(),Utility.getCheckedMap());
			 listView.setAdapter(mAdapter);
			 progressbar.setVisibility(View.GONE);
		}
		else
		{*/
			new GetContactAsynTask().execute();
	//	}
		
		
	}
	
	private void insertInvitation()
	{
		 inviteBtn.setEnabled(false);
		progressbar.setVisibility(View.VISIBLE);
		 mLocationPoint=new ParseGeoPoint(gpsTracker.getLatitude(),gpsTracker.getLongitude());
    	 System.out.println("selectedMobileList :: "+selectedMobileList);
    	for(int j=0;j<selectedMobileList.size();j++)
    	{
    		final int i=j;
    	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
 		query.whereEqualTo(Constants.TO_USER, selectedMobileList.get(i));
 		query.whereEqualTo(Constants.GROUP_ID, groupObject.getObjectId());
    	query.getFirstInBackground(new GetCallback<ParseObject>() {
  			public void done(ParseObject object, ParseException e) {
  					if(object!=null)
    				{
  						object.put(Constants.INVITATION_STATUS, "Active");
  						object.put(Constants.INVITATION_LOCATION, mLocationPoint);
  						object.saveInBackground();
    				}
  					else
  					{
  						ParseObject userObject = new ParseObject(Constants.INVITATION_TABLE);
  						userObject.put(Constants.TO_USER, selectedMobileList.get(i));
  						userObject.put(Constants.FROM_USER, PreferenceSettings.getMobileNo());
  						userObject.put(Constants.GROUP_ID, groupObject.getObjectId());
  						userObject.put(Constants.INVITATION_STATUS, "Active");
  						userObject.put(Constants.INVITATION_LOCATION, mLocationPoint);
  						userObject.saveInBackground();
  					}
  			}});
    	
    	if(j==selectedMobileList.size()-1)
    	{
    		 inviteBtn.setEnabled(true);
    		progressbar.setVisibility(View.GONE);
    		if(selectedMobileList.size()==1)
    			Utility.showToastMessage(ContactListActivity.this, "Invitation has been sent to "+mAdapter.selectedPhoneNo.size()+" user");
    		else
    			Utility.showToastMessage(ContactListActivity.this, "Invitation has been sent to "+mAdapter.selectedPhoneNo.size()+" users");
        	 finish();
    	}
    	}
	}
	
	private void read1()
	{
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
		                ContactsContract.CommonDataKinds.Phone.NUMBER};

		Cursor people = getContentResolver().query(uri, projection, null, null, null);
	//	System.out.println("count :: "+people.getCount());

		int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		
		int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		//System.out.println("index name && number :: "+indexName+"\t"+indexNumber);
		
		people.moveToFirst();
		do {
		    String name   = people.getString(indexName);
		    String number = people.getString(indexNumber);
		//    System.out.println("name && number :: "+name+"\t"+number);
		    // Do work...
		} while (people.moveToNext());
	}
	
	 private void readContactData() {
          list=new ArrayList<ContactInfoObject>();
	        try {
	             
	            /*********** Reading Contacts Name And Number **********/
	             
	            String phoneNumber = "",image="";
	            ContentResolver cr = getBaseContext()
	                    .getContentResolver();
	             
	            //Query to get contact name
	             
	            Cursor cur = cr
	                    .query(ContactsContract.Contacts.CONTENT_URI,
	                            null,
	                            null,
	                            null,
	                            null);
	             
	            // If data data found in contacts
	            if (cur.getCount() > 0) {
	                 
	                Log.i("AutocompleteContacts", "Reading   contacts........");
	                 
	                int k=0;
	                String name = "";
	                String image_uri = "";
	                while (cur.moveToNext())
	                {
	                     
	                    String id = cur
	                            .getString(cur
	                                    .getColumnIndex(ContactsContract.Contacts._ID));
	                    name = cur
	                            .getString(cur
	                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	                    image_uri = cur
	                    	      .getString(cur
	                    	        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
	                     
	                    //Check contact have phone number
	                    if (Integer
	                            .parseInt(cur
	                                    .getString(cur
	                                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
	                    {
	                             
	                        //Create query to get phone number by contact id
	                        Cursor pCur = cr
	                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	                                            null,
	                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
	                                                    + " = ?",
	                                            new String[] { id },
	                                            null);
	                            int j=0;
	                             
	                            while (pCur
	                                    .moveToNext())
	                            {
	                                // Sometimes get multiple data
	                                if(j==0)
	                                {
	                                	ContactInfoObject object=new ContactInfoObject();
	                                    // Get Phone number
	                                    phoneNumber =""+pCur.getString(pCur
	                                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	                                    
	                                   // image =""+pCur.getString(pCur
                                           //     .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
	                                    // imageList.add(image_uri);
	                                    // Add contacts names to adapter
	                                   // nameList.add(name);
	                                     
	                                    // Add ArrayList names to adapter
	                                  //  phoneNumberList.add(phoneNumber.toString());
	                                  //  nameValueArr.add(name.toString());
	                                    object.setContactImage(image_uri);
	                                    object.setContactName(name);
	                                    object.setContactNo(phoneNumber);
	                                    
	                                    checkedMap.put(name, false);
	                                    list.add(object);
	                                    j++;
	                                    k++;
	                                }
	                            }  // End while loop
	                            pCur.close();
	                        } // End if
	                     
	                }  // End while loop
	               
	 
	            } // End Cursor value check
	            
	            
	            cur.close();
	                     
	          
	        } catch (Exception e) {
	             Log.i("AutocompleteContacts","Exception : "+ e);
	        }
	        
	      
	                     
	     
	   }
	 
	 private void readContacts()
	 {
		 list=new ArrayList<ContactInfoObject>();

		 ContentResolver cr = getContentResolver();
		 Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
		 if (cursor != null) {
		     try {
		         final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		        
		         final int phoneNo=cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		         final int noIndex=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		         final int image=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
                       
		         String displayName, no,image_uri;
		         while (cursor.moveToNext()) {
		             displayName = cursor.getString(displayNameIndex);

		            
		             image_uri= cursor.getString(image);
		             if (Integer.parseInt(cursor.getString(phoneNo)) > 0)
		             {
		            	   no = cursor.getString(noIndex);
		            	   System.out.println("mobile no "+no);
		            	 ContactInfoObject object=new ContactInfoObject();
		            	 object.setContactImage(image_uri);
                         object.setContactName(displayName);
                         object.setContactNo(no);
                         checkedMap.put(displayName, false);
                         list.add(object);
		             }
		         }
		     } finally {
		         cursor.close();
		     }
		 }
	 }
	 
	public class GetContactAsynTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			readContacts();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if(list.size() > 0)
			{
				Utility.setContactList(list);
				Utility.setCheckedMap(checkedMap);
			 mAdapter=new ContactListAdapter(ContactListActivity.this,list,checkedMap);
			 listView.setAdapter(mAdapter);
			 progressbar.setVisibility(View.GONE);
			}
		}
		
	}

	private void initViews() {
		listView=(ListView) findViewById(R.id.listview);
		mBackImg=(ImageView) findViewById(R.id.back);
		mEnterNoImg=(ImageView) findViewById(R.id.add);
		searchEditTxt=(EditText) findViewById(R.id.search_box);
		inviteBtn=(Button) findViewById(R.id.send);
		//layout=(LinearLayout) findViewById(R.id.layout);
		selectedContactTxt=(TextView) findViewById(R.id.selected_contact_txt);
		
		progressbar=(ProgressBar) findViewById(R.id.progressBar1);
		
		progressbar.setVisibility(View.VISIBLE);
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );				
			}
		});
		gpsTracker = new GPSTracker(this);
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
