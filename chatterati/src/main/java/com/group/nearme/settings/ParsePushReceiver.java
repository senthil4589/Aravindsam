package com.group.nearme.settings;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.group.nearme.NotificationListActivity;
import com.group.nearme.R;
import com.group.nearme.TabGroupActivity;
import com.group.nearme.util.Constants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;

@SuppressLint("NewApi") public class ParsePushReceiver extends ParsePushBroadcastReceiver {

	public static final String TAG = "ParsePushReceiver";

	protected void onPushReceive(Context mContext, Intent intent) {
		System.out.println("inside onPushReceive");

		try {
			if (intent == null) {
				Log.d(TAG, "Receiver intent null");
			} else {
				String action = intent.getAction();
				Log.d(TAG, "got action " + action);
				if (action
						.equals("com.parse.push.intent.RECEIVE")) {
					JSONObject json = new JSONObject(intent.getExtras()
							.getString("com.parse.Data"));
					boolean flag=json.getBoolean("Flag");
					
					if(flag) // send UI notifications
					{
						final String groupId=json.getString("GroupId");
						if(json.getString("Type").equals("Post") || json.getString("Type").equals("Comment") || json.getString("Type").equals("Flag"))
						{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
							query.whereEqualTo(Constants.OBJECT_ID, json.getString("FeedId"));
							
							query.include(Constants.USER_ID);
							query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(final  ParseObject object, ParseException e) {
								if(object!=null)
								{
									object.pinInBackground(groupId);
								}
							}});
						}
						saveInSharedPreference(mContext,json);
						//callTabPostActivity(json.getString("GroupId"));
						generateNotification(mContext,json.getString("alert"));
						//if(json.getString("FeedId").isEmpty())
							//callTabPostActivity(json.getString("GroupId"),json.getString("GroupType"),json.getString("MobileNo"));
						//else
							//cacheComment(json.getString("FeedId"));
					}
					else
					{
						//if(json.getString("FeedId").isEmpty())
							//callTabPostActivity(json.getString("GroupId"),json.getString("GroupType"),json.getString("MobileNo"));
						//else
						//	cacheComment(json.getString("FeedId"));
						
					}
				}
			}
		} catch (JSONException e) {
			Log.d(TAG, "JSONException: " + e.getMessage());
		}
	}
	
	
	private void saveInSharedPreference(Context mContext,JSONObject json) {
		DatabaseHelper db = new DatabaseHelper(mContext);
		int count = db.getNotificationCount();
		System.out.println("count "+count);
		if(count>=20)
		{
			int lastId=db.getLastRowId();
			System.out.println("20 item id :: "+lastId);
			int id=lastId-(count-1);
			
			System.out.println("delete row :: "+id);
			db.deleteRow(id);
			System.out.println("after delete count :: "+db.getNotificationCount());
			try {
				db.insertMsg(json.getString("alert"),json.getString("ImageURL"),json.getString("GroupId"),json.getString("FeedId"),
						json.getString("GroupType"),json.getString("MobileNo"),json.getString("Type"),json.getString("Time"));
				} catch (JSONException e) {
			  }
		}
		else
		{
			try {
			db.insertMsg(json.getString("alert"),json.getString("ImageURL"),json.getString("GroupId"),json.getString("FeedId"),
					json.getString("GroupType"),json.getString("MobileNo"),json.getString("Type"),json.getString("Time"));
			} catch (JSONException e) {
				System.out.println("inside catch"+e);
		  }
		}
		
	}
	
	
	/*@SuppressLint("CommitPrefEdits") 
	private void getNotificationObjectList(Context mContext)
	{
		 SharedPreferences appSharedPrefs = mContext.getSharedPreferences("Notification",Context.MODE_PRIVATE);
			      Editor prefsEditor = appSharedPrefs.edit();
			      Gson gson = new Gson();
			 String json = appSharedPrefs.getString("MyObject", "");
			 NotificationObject mStudentObject = gson.fromJson(json, NotificationObject.class);
	}
	
*/	

	/*private void cacheComment(final String feedid)
	{
		System.out.println("inside comment activity");
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ACTIVITY_TABLE);
		query.whereEqualTo(Constants.FEED_ID, feedid);
		query.whereEqualTo(Constants.FEED_TYPE, "Comment");
		query.findInBackground();
	}
	
	private void callTabPostActivity(final String groupid,final String grouptype,String no)
	{
		System.out.println("id\ttype\tno"+groupid+"\t"+grouptype+"\n"+no);
		
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.USER_TABLE);
		//query1.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query1.whereEqualTo(Constants.MOBILE_NO, no);
		query1.getFirstInBackground();
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
		query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.OBJECT_ID, groupid);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object1, ParseException e) {
					if (e == null) 
					{
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
							query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
							query.whereEqualTo(Constants.GROUP_ID, groupid);
							query.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
							//query.whereEqualTo(Constants.MEMBER_STATUS, "Active");
							query.findInBackground();
							
							ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
							query1.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
							query1.whereEqualTo(Constants.GROUP_ID, groupid);
							if(grouptype.equals("Public"))
							{
								query1.whereNotEqualTo(Constants.POST_TYPE, "Member");
							}
							query1.whereEqualTo(Constants.POST_STATUS, "Active");
							query1.findInBackground();
						}
			}
		});	
		
		ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query2.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query2.whereEqualTo(Constants.GROUP_ID, groupid);
		query2.whereEqualTo(Constants.POST_TYPE, "Image");
		query2.whereEqualTo(Constants.POST_STATUS, "Active");
		query2.setLimit(20);
		query.findInBackground();
	}*/
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) @SuppressLint("NewApi") private void generateNotification(Context context,String contenttext) {
		
		int notification_id = (int) System.currentTimeMillis();
		Uri sound_uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    Intent intent = new Intent(context, NotificationListActivity.class).putExtra("flag", true);
	    
	    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
	 // All the parents of SecondActivity will be added to task stack.
	 stackBuilder.addParentStack(TabGroupActivity.class);
	 // Add a SecondActivity intent to the task stack.
	 stackBuilder.addNextIntent(intent);
	 
	 //PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


	    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);

	    NotificationManager mNotifM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

	    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSound(sound_uri).setContentTitle("Chatterati").setContentText(contenttext);
	    
	    if(Build.VERSION.SDK_INT >= 21)
	    {
	    	Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.noti_icon);
	    	mBuilder.setLargeIcon(bm) ;
		    
	    	mBuilder.setSmallIcon(R.drawable.chatterati_white) ;
	    }
	    else
	    	mBuilder.setSmallIcon(R.drawable.launcher_icon) ;
	    
	    mBuilder.setContentIntent(contentIntent);
	    mBuilder.setAutoCancel(true);
	    //mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
	    mNotifM.notify(notification_id, mBuilder.build());

	}
}