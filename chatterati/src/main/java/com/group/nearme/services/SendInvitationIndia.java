package com.group.nearme.services;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class SendInvitationIndia extends AsyncTask<Void, Void, Void>
{
	String userName="",groupName="",groupId="",mobileNo="";
	public SendInvitationIndia(String user,String name,String id,String no)
	{
		this.userName=user;
		this.groupName=name;
		this.groupId=id;
		this.mobileNo=no;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		System.out.println("mobile no ::: "+mobileNo+"  "+groupName);
		//String url="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo=+919710577519&SenderID=CHATTR&CDMAHeader=CHATTR&Message=Your mobile verification code from Chatterati App is (Variable1). Do not disclose or share.";
		//String url="http://hapi.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo=+919710577519&SenderID=CHATTR&CDMAHeader=xxxx&Message=xxxx";
		//String url="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo="+mobileNo+"&SenderID=CHATTR&CDMAHeader=CHATTR&Message=From Chatterati Application : To Join " +groupName+ " you can join by clicking here groupsnearme.com/Groups/GroupName/"+groupId;
		//String url="http://bhashsms.com/api/sendmsg.php?user=9445163340&pass=9ca58b4&sender=AJVISH&phone="+mobileNo+"&text=From Chatterati Application : To Join " +groupName+ " you can join by clicking here groupsnearme.com/Groups/GroupName/"+groupId+" &priority=ndnd&stype=normal";
		//String url="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo="+mobileNo+"&SenderID= CHATTR&CDMAHeader=CHATTR&Message=Your friend "+userName+" invited you to join the group "+groupName+" on Chatterati App. Download Chatterati app from http://getchatterati.com";
		String url="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo="+mobileNo+"&SenderID= CHATTR&CDMAHeader=CHATTR&Message=You are invited to join Chatterati group "+groupName+". Download the Chatterati mobile app from http://getchatterati.com";
		//String url="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo="+mMobileNo+"&SenderID=CHATTR&CDMAHeader=CHATTR&Message=Your mobile verification code from Chatterati App is "+mOTPCode+". Do not disclose or share.";
		//String url = null;
		//String url1="http://whitelist.smsapi.org/SendSMS.aspx?UserName=Delasoft_SMS&password=617995&MobileNo=XXXXXXXXXX&SenderID= CHATTR&CDMAHeader=CHATTR&Message=You are invited to join Chatterati group %3CVariable1%3E. Download the Chatterati mobile app from http://getchatterati.com";
		HttpClient httpclient = new DefaultHttpClient();
		    try {
		    	URI uri = new URI(url.replace(" ", "%20"));
				HttpResponse response = httpclient.execute(new HttpGet(uri));
				StatusLine statusLine = response.getStatusLine();
				System.out.println("result ::: "+statusLine.getStatusCode());
			} catch (Exception e) {
			 }
			return null;
	}
}
