package com.group.nearme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.util.Utility;

public class SendErroLogActivity extends Activity
{
	Button ok;
	Dialog mDialog;
	String error="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.send_log);
		Utility.getTracker(this, this.getClass().getSimpleName());
		initViews();
		error=getIntent().getStringExtra("error");
		System.out.println("error in send class "+error);
		showExitAlertDialog();
	}

	private void initViews() {
		ok=(Button) findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("inside ok click");
			}
		});
	}
	
	 private void sendLogFile ()
	 {
		/*
	   Intent i = new Intent(Intent.ACTION_SEND);
	   i.setType("text/plain");
	   i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"senthil4589@gmail.com"});
	   i.putExtra(Intent.EXTRA_SUBJECT, "Chatterati log );
	   i.putExtra(Intent.EXTRA_TEXT   , error);
	   try {
	       startActivity(Intent.createChooser(i, "Send mail..."));
	   } catch (android.content.ActivityNotFoundException ex) {
	       Toast.makeText(SendErroLogActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
	   }*/
	   finish();
	 }
	 
	 private String extractLogToFile()
	 {
	   PackageManager manager = this.getPackageManager();
	   PackageInfo info = null;
	   try {
	     info = manager.getPackageInfo (this.getPackageName(), 0);
	   } catch (NameNotFoundException e2) {
	   }
	   String model = Build.MODEL;
	   if (!model.startsWith(Build.MANUFACTURER))
	     model = Build.MANUFACTURER + " " + model;

	   // Make file name - file must be saved to external storage or it wont be readable by
	   // the email app.
	   String path = Environment.getExternalStorageDirectory() + "/" + "MyApp/";
	   String fullName = path + "log";

	   // Extract to file.
	   File file = new File (fullName);
	   InputStreamReader reader = null;
	   FileWriter writer = null;
	   try
	   {
	     // For Android 4.0 and earlier, you will get all app's log output, so filter it to
	     // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
	     String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
	                   "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
	                   "logcat -d -v time";

	     // get input stream
	     Process process = Runtime.getRuntime().exec(cmd);
	     reader = new InputStreamReader (process.getInputStream());

	     // write output stream
	     writer = new FileWriter (file);
	     writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
	     writer.write ("Device: " + model + "\n");
	     writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

	     char[] buffer = new char[10000];
	     do 
	     {
	       int n = reader.read (buffer, 0, buffer.length);
	       if (n == -1)
	         break;
	       writer.write (buffer, 0, n);
	     } while (true);

	     reader.close();
	     writer.close();
	   }
	   catch (IOException e)
	   {
	     if (writer != null)
	       try {
	         writer.close();
	       } catch (IOException e1) {
	       }
	     if (reader != null)
	       try {
	         reader.close();
	       } catch (IOException e1) {
	       }

	     // You might want to write a failure message to the log here.
	     return null;
	   }

	   return fullName;
	 }
	 
	 public  void showExitAlertDialog(){
			mDialog = new Dialog(SendErroLogActivity.this);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mDialog.setContentView(R.layout.two_btn_dialog);
			mDialog.setCancelable(true);
			mDialog.setCanceledOnTouchOutside(true);
			
			WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
			windowManager.gravity = Gravity.CENTER;
			Button yes=(Button) mDialog.findViewById(R.id.yes);
			Button no=(Button) mDialog.findViewById(R.id.no);
			TextView message=(TextView) mDialog.findViewById(R.id.msg);
			
			yes.setText("Send Error Report");
			no.setText("Don't Send");
			
			message.setText("Are you sure you want to exit?");
			
			yes.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sendLogFile ();
				}
			});
			
			no.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					finish();
				}
			});
			mDialog.show();
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
