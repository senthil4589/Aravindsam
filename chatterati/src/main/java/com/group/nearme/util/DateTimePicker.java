package com.group.nearme.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.group.nearme.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;


public class DateTimePicker {
	
	static DatePicker datePicker;
	static TimePicker timePicker;
	static Dialog dialog;
	static AlertDialog alertDialog;
	static ArrayList<Object> dateTimeList;
	public static Map<String, String> dayMap;
	public static String FROM_DATE = "";
	public static String TO_DATE = "";
	public static final String SELECT_FROMDATE="Select From Date First.";
	public static final String SELECT_TODATE="Select To Date First.";
	public static final String FROMDATE_SMALLER="From Date Should Be Smaller Than Or Equal To To Date.";
	public static final String FROMDATE_RANGE="From Date Should Be Within Travel Date Range.";
	public static final String TODATE_EQUAL_AFTER="To Date Should Be Equal or After From Date.";
	public static final String TODATE_RANGE="To Date Should Be Within Travel Date Range.";
	private static final String TAG_NAME = "DateTimePicker";
	
	public static ArrayList<Object> dateTimePicker(Activity activity,Date d) {
		dateTimeList = new ArrayList<Object>();	
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.alert_dialog_datepicker, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(view);
		alertDialog = builder.create();
		alertDialog.setCancelable(true);
		alertDialog.show();
		datePicker = (DatePicker) alertDialog.findViewById(R.id.datePicker);
		timePicker = (TimePicker) alertDialog.findViewById(R.id.timePicker);		
		
		//Calendar c = Calendar.getInstance();
    	//c.setTime(d);
    	Calendar c = Calendar.getInstance();
    
    	
    	int mYear = c.get(Calendar.YEAR);
    	int mMonth = c.get(Calendar.MONTH);
    	int mDay = c.get(Calendar.DAY_OF_MONTH);
		datePicker.updateDate(mYear,mMonth, mDay);
		datePicker.setMinDate(System.currentTimeMillis() - 1000);
		datePicker.setMaxDate(Utility.getMaxDate());
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    	
		String time=sdf.format(c.getTime()).toString(); 
    	String split[]=time.split(":");
		timePicker.setIs24HourView(false);
		timePicker.setCurrentHour(Integer.parseInt(split[0]));
		timePicker.setCurrentMinute(Integer.parseInt(split[1]));
		final Button btnDone = (Button) alertDialog.findViewById(R.id.done);
		final Button btnCancel = (Button) alertDialog.findViewById(R.id.cancel);
		dateTimeList.add(alertDialog);
		dateTimeList.add(datePicker);
		dateTimeList.add(timePicker);
		
		dateTimeList.add(btnDone);
		dateTimeList.add(btnCancel);
		
		return dateTimeList;
	}
	public static Map<String, String> addDaysToMap() {
		dayMap = new HashMap<String, String>();
		dayMap.put("0", "Jan");
		dayMap.put("1", "Feb");
		dayMap.put("2", "Mar");
		dayMap.put("3", "Apr");
		dayMap.put("4", "May");
		dayMap.put("5", "Jun");
		dayMap.put("6", "Jul");
		dayMap.put("7", "Aug");
		dayMap.put("8", "Sep");
		dayMap.put("9", "Oct");
		dayMap.put("10", "Nov");
		dayMap.put("11", "Dec");
		dayMap.put("Jan", "1");
		dayMap.put("Feb", "2");
		dayMap.put("Mar", "3");
		dayMap.put("Apr", "4");
		dayMap.put("May", "5");
		dayMap.put("Jun", "6");
		dayMap.put("Jul", "7");
		dayMap.put("Aug", "8");
		dayMap.put("Sep", "9");
		dayMap.put("Oct", "10");
		dayMap.put("Nov", "11");
		dayMap.put("Dec", "12");
		return dayMap;
	}
// To retrieve value from map
	public static String getDayFromMap(String no) {
		return dayMap.get(no);
	}
	public static boolean compareDates(String fromDate, String toDate) {
		SimpleDateFormat sdf;
		if (fromDate.contains("/")) {
			sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		} else {
			sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		}
		Date date1 = null;
		Date date2 = null;
		try {
			date1 = sdf.parse(fromDate);
			date2 = sdf.parse(toDate);
		} catch (ParseException e) {
			Log.e(TAG_NAME, "Parsing Date Is:" + e);
			e.printStackTrace();
		}
		if (date1.compareTo(date2) > 0) {
			return false;
		} else if (date1.compareTo(date2) < 0) {
			return true;

		} else if (date1.compareTo(date2) == 0) {
			return true;

		} else {
		}
		return true;
	}
}