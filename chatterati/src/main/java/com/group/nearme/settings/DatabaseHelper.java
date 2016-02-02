package com.group.nearme.settings;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "ParsePush";
	private static final String TABLE_NAME = "NotificationTable";
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		db.execSQL("CREATE TABLE "
				+ TABLE_NAME
				+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT, Message STRING,Image STRING,GroupId STRING,FeedId STRNG,GroupType STRING,Type STRING,MobileNo STRING,Time STRING)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	public void insertMsg(String text, String imgUrl, String groupid,
			String feedid, String grouptype, String no, String type, String time) {
		System.out.println("List values ::" + text + " " + imgUrl + " "
				+ groupid + " " + feedid + " " + grouptype + " " + type + " "
				+ no + " " + time);
		SQLiteDatabase db = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("Message", text);
		cv.put("Image", imgUrl);
		cv.put("GroupId", groupid);
		cv.put("FeedId", feedid);
		cv.put("GroupType", grouptype);
		cv.put("Type", type);
		cv.put("MobileNo", no);
		cv.put("Time", time);

		db.insert(TABLE_NAME, null, cv);

		db.close();

	}

	public void updateMsg(String text, String imgUrl, String groupid,
			String feedid, String grouptype, String no, String type, String time) {
		System.out.println("List values ::" + text + " " + imgUrl + " "
				+ groupid + " " + feedid + " " + grouptype + " " + type + " "
				+ no + " " + time);
		SQLiteDatabase db = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("Message", text);
		cv.put("Image", imgUrl);
		cv.put("GroupId", groupid);
		cv.put("FeedId", feedid);
		cv.put("GroupType", grouptype);
		cv.put("Type", type);
		cv.put("MobileNo", no);
		cv.put("Time", time);

		db.insert(TABLE_NAME, null, cv);

		db.close();

	}

	public int getNotificationCount() {
		String countQuery = "SELECT  * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int cnt = cursor.getCount();
		cursor.close();
		return cnt;
	}
	public ArrayList<NotificationObject> getAllNotification()
	{
		ArrayList<NotificationObject> list =new ArrayList<NotificationObject>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor  cursor = db.rawQuery("select * from NotificationTable ORDER BY ID DESC",null);
		System.out.println("cursor count :: "+cursor.getCount());
		if(cursor!=null && cursor.getCount() > 0)
		{
		if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                String msg = cursor.getString(cursor.getColumnIndex("Message"));
                String img = cursor.getString(cursor.getColumnIndex("Image"));
                String gid = cursor.getString(cursor.getColumnIndex("GroupId"));
                String fid = cursor.getString(cursor.getColumnIndex("FeedId"));
                String gtype = cursor.getString(cursor.getColumnIndex("GroupType"));
                String type = cursor.getString(cursor.getColumnIndex("Type"));
                String time = cursor.getString(cursor.getColumnIndex("Time"));
                String no = cursor.getString(cursor.getColumnIndex("MobileNo"));
              //  new NotificationObject(msg, img, fid, gid, gtype, no, type, time);
                list.add(new NotificationObject(msg, img, fid, gid, gtype, no, type, time));
                cursor.moveToNext();
            }
            cursor.close();
        }
		}
		else
		{
			System.out.println("cursor count :: "+cursor.getCount());
			  //cursor.close();
		}
		
		return list;
	}
	
	public void deleteRow(int rowId)
	{
		SQLiteDatabase db = this.getWritableDatabase();
	 db.execSQL("DELETE FROM NotificationTable WHERE ID='" +rowId+"'");
		
		//cursor.close();
	}
	
	public int getLastRowId()
	{
		int id=0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor  cursor = db.rawQuery("select * from NotificationTable",null);
		System.out.println("cursor count :: "+cursor.getCount());
		cursor .moveToLast();
		id= Integer.parseInt(cursor.getString(cursor.getColumnIndex("ID")));

		return id;
	}
	
}
