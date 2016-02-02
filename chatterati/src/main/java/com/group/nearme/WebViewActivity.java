package com.group.nearme;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.util.Utility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WebViewActivity extends Activity {
	TextView titleTxtView;
	WebView webview;
	String title,url;
	private ImageView mBackImg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.webview);
		Utility.getTracker(this, "APP WEBVIEW SCREEN");
		initViews();
		webview.getSettings().setJavaScriptEnabled(true);
		/*webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setPluginState(PluginState.ON);
		*/webview.setWebViewClient(new WebViewClient() {
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        view.loadUrl(url);
		        return false;
		    }
		});
		System.out.println("url ::: "+url);
		titleTxtView.setText(title);
		webview.loadUrl(url);
	}

	private void initViews() {
		titleTxtView=(TextView) findViewById(R.id.title);
		webview=(WebView) findViewById(R.id.webview);
		mBackImg=(ImageView) findViewById(R.id.back);
		title=getIntent().getStringExtra("title");
		url=getIntent().getStringExtra("url");
		
		if(!url.contains("http")) {
			url = "http://"+url;
	    }
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		/overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
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
