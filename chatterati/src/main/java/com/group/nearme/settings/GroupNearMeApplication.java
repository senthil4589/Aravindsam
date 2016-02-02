package com.group.nearme.settings;

import com.crashlytics.android.Crashlytics;
import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.fabric.sdk.android.Fabric;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.group.nearme.R;
import com.group.nearme.SendErroLogActivity;
import com.group.nearme.util.Constants;
import com.group.nearme.util.LruBitmapCache;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

public class GroupNearMeApplication extends Application
{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
  //  private static final String TWITTER_KEY = "YtTupEy6YFySrX4dYVxjENwyh";
  //  private static final String TWITTER_SECRET = "BBjNTDAwfE4AXxLTiRWPAcCBySd8IK0GSjKC4PQxtJ4DgwxOsi";

	
		// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY = "BR14yvEDmVuntXkqVCuxCkcHq";
	private static final String TWITTER_SECRET = "VpT5CyXUBGpLMWGRthR2eHaFHUHRHegX0QhPwbNDiCbNWCUETg";
	
	static Context mContext;
	public static final String TAG = GroupNearMeApplication.class
			.getSimpleName();
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static final String PROPERTY_ID = "UA-67491308-1";

	private static GroupNearMeApplication mInstance;
	@Override
	public void onCreate() {
		super.onCreate();
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig), new Digits());

		mContext = this;
		mInstance = this;
		ParseCrashReporting.enable(this);
		//Parse.enableLocalDatastore(this);
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, Constants.APPLICATION_ID, Constants.CLIENT_KEY);
		ParseInstallation.getCurrentInstallation().saveInBackground();
		//PushService.setDefaultPushCallback(this, TabGroupActivity.class);
		//ParseInstallation.getCurrentInstallation().saveInBackground();*/
		
		/* Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		    { Q1Q'R
		      @Override
		      public void uncaughtException (Thread thread, Throwable e)
		      {
		      // handleUncaughtException (thread, e);
		    	 // System.out.println("inside exception in application class");
		    	  System.out.println("exception "+e);
		    	  System.exit(1);  
		      }
		    });*/
	}
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	
	 public void handleUncaughtException (Thread thread, Throwable e)
	  {
	  //  e.printStackTrace(); // not all Android versions will print the stack trace automatically
	    
	    StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append("\n");
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append("\n");
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append("\n");
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append("\n");
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append("\n");
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append("\n");
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append("\n");
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append("\n");

	    Intent intent = new Intent (mContext,SendErroLogActivity.class);
	    intent.putExtra("error", errorReport.toString());
	   // intent.setAction ("com.group.nearme.SEND_LOG"); // see step 5.
	    intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
	    startActivity (intent);
	    android.os.Process.killProcess(android.os.Process.myPid());
	    System.exit(1); // kill off the crashed app
	  }
	
	
	public static Context getAppContext()
	{
		return mContext;
	}

	public static synchronized GroupNearMeApplication getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.mRequestQueue,
					new LruBitmapCache());
		}
		return this.mImageLoader;
	}
	
	public enum TrackerName {
	    APP_TRACKER, // Tracker used only in this app.
	    GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
	    ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
	  }

	  HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	  public synchronized Tracker getTracker(TrackerName trackerId) {
		    if (!mTrackers.containsKey(trackerId)) {

		      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		      Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
		          : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
		              : analytics.newTracker(R.xml.ecommerce_tracker);
		      mTrackers.put(trackerId, t);

		    }
		    return mTrackers.get(trackerId);
	  }



}
