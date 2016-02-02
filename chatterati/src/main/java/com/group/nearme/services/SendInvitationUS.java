package com.group.nearme.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Base64;

public class SendInvitationUS extends AsyncTask<Void, Void, Void>
{
	private static final String ACCOUNT_SID = "AC867b012600c2cea93a1ec999eb88870d";
	private static final String AUTH_TOKEN = "1f65e4a8e3c80cfaeb88f356112dbfef";
	private static final String FROM="+18708980344";
	String userName="",groupName="",groupId="",mobileNo="";
	public SendInvitationUS(String user,String name,String id,String no)
	{
		this.userName=user;
		this.groupName=name;
		this.groupId=id;
		this.mobileNo=no;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
	
		//String url="http://bhashsms.com/api/sendmsg.php?user=9445163340&pass=9ca58b4&sender=AJVISH&phone="+mobileNo+"&text=From Chatterati Application : To Join" +groupName+ " you can join by clicking here groupsnearme.com/Groups/"+groupName+"/"+groupId+" &priority=ndnd&stype=normal";
		String url="https://api.twilio.com/2010-04-01/Accounts/"+ACCOUNT_SID+"/SMS/Messages.json";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		String basicAuth = "Basic " + new String(Base64.encode((ACCOUNT_SID+":"+AUTH_TOKEN).getBytes(),Base64.NO_WRAP ));
		httppost.setHeader("Authorization", basicAuth);
		//httppost.addHeader("UserName", ACCOUNT_SID);
		//httppost.addHeader("Password", AUTH_TOKEN);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("From", FROM));
		nameValuePairs.add(new BasicNameValuePair("To", mobileNo));
		nameValuePairs.add(new BasicNameValuePair("Body", "You are invited to join Chatterati group "+groupName+". Download the Chatterati mobile app from http://getchatterati.com"));
	//	nameValuePairs.add(new BasicNameValuePair("Body", "Your friend "+userName+" invited you to join the group "+groupName+" on Chatterati App. Download Chatterati app from http://getchatterati.com"));
		
		 
		    try {
		    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				StatusLine statusLine = response.getStatusLine();
				System.out.println("result ::: "+statusLine.getStatusCode());
			}
			 catch (Exception e) {
				 System.out.println("exception :: "+e);
			 }
			return null;
	}
}
