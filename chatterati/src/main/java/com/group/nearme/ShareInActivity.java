package com.group.nearme;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beanie.imagechooser.api.ChooserType;
import com.beanie.imagechooser.api.ChosenVideo;
import com.beanie.imagechooser.api.VideoChooserListener;
import com.beanie.imagechooser.api.VideoChooserManager;
import com.embedly.api.Api;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.adapter.ShareInAdapter;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class ShareInActivity extends Activity{
	
	Api api = new Api("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2", "eea3a0754b2146698c346dddc5adf262");
	HashMap<String, Object> linkParams;
	
	ListView listView;
	ShareInAdapter adapter;
	ProgressBar progressBar;
	private ArrayList<String> list=new ArrayList<String>();
	public GPSTracker gpsTracker;
	List<ParseObject> groupList;
	Intent intent;
	String action,type;
	static Activity activity;
	
	public static  int imgWidth,imgHeight;
	byte[] imgByte;
	public static ParseFile mPostImgFile,mPostImageThumbnail;
	public static String linkTitle="",linkImage="",linkImagePath="",link="", mGroupId="",siteName="";
	ParseObject groupObject;
	
	public static ParseFile mParseVideoFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.share_in_listview);
		Utility.getTracker(this, "CHOOSE GROUP FOR SHARE FROM OUTSIDE");
		initViews();
		activity=this;
		if(Utility.checkInternetConnectivity(ShareInActivity.this))
			loadGroups();
		else
		{
			Utility.showToastMessage(ShareInActivity.this, getResources().getString(R.string.no_network));
			finish();
		}
		
		
		intent = getIntent();
	    action = intent.getAction();
	    type = intent.getType();


		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                                    long arg3) {
                groupObject = groupList.get(position);
                Utility.setGroupObject(groupObject);
                System.out.println("type :: " + type);
                listView.setEnabled(false);
                boolean isFromChatterati = intent.getBooleanExtra("isFromChatterati", false);
                System.out.println("isFromChatterati ::: " + isFromChatterati);
                if (isFromChatterati) {
                    shareInternal(groupList.get(position).getObjectId());
                } else {

                    if (Intent.ACTION_SEND.equals(action) && type != null) {
                        if ("text/plain".equals(type)) {
                            System.out.println("inside text");
                            handleSendText(intent, groupList.get(position).getObjectId()); // Handle text being sent
                        } else if (type.startsWith("image/")) {
                            System.out.println("inside image");
                            handleSendImage(intent, groupList.get(position).getObjectId()); // Handle single image being sent
                        } else if (type.startsWith("video/")) {
                            System.out.println("inside video");
                            handleGalleryVideo(intent, groupList.get(position).getObjectId());
                        }

                    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                        if (type.startsWith("image/")) {
                            handleSendMultipleImages(intent, groupList.get(position).getObjectId()); // Handle multiple images being sent
                        }
                    }
                }

            }
        });
	}
	
	void shareInternal(final String toGroupId)
	{
		
		GPSTracker gpsTracker=new GPSTracker(this);
		final ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
		final String fromGroupId=getIntent().getStringExtra(Constants.GROUP_ID);
    	final String feedId=getIntent().getStringExtra(Constants.FEED_ID);
		final String fromGroupName=getIntent().getStringExtra(Constants.GROUP_NAME);
    	
    	System.out.println("inside isFromChatterati");
    	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.OBJECT_ID, feedId);
		query.fromPin(fromGroupId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
						//	ArrayList<String> list=new ArrayList<String>();
							ArrayList<String> list1=new ArrayList<String>();
							list.add(PreferenceSettings.getMobileNo());
							final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
							userObject.put(Constants.GROUP_ID, toGroupId);
							if(object.get(Constants.POST_TEXT)!=null)
								userObject.put(Constants.POST_TEXT, object.get(Constants.POST_TEXT));
							 userObject.put(Constants.POST_TYPE, object.get(Constants.POST_TYPE));
							 userObject.put(Constants.COMMENT_COUNT, 0);
							 userObject.put(Constants.FLAG_COUNT, 0);
								userObject.put(Constants.LIKE_ARRAY, list1);
								userObject.put(Constants.DIS_LIKE_ARRAY,list1);
								userObject.put(Constants.POST_POINT, 100);
								userObject.put(Constants.POST_STATUS, "Active");
								userObject.put(Constants.FLAG_ARRAY, list1);
								userObject.put(Constants.FEED_LOCATION, point);
								userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
								userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
								userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
								userObject.put(Constants.HASH_TAG_ARRAY, list1);
								if(object.get(Constants.POST_VIDEO)!=null)
									userObject.put(Constants.POST_VIDEO, object.get(Constants.POST_VIDEO));
								if(object.get(Constants.POST_IMAGE)!=null)
								userObject.put(Constants.POST_IMAGE, object.get(Constants.POST_IMAGE));
								if(object.get(Constants.THUMBNAIL_PICTURE)!=null)
								userObject.put(Constants.THUMBNAIL_PICTURE, object.get(Constants.THUMBNAIL_PICTURE));
								if(object.get(Constants.LINK_URL)!=null)
								userObject.put(Constants.LINK_URL, object.get(Constants.LINK_URL));
								if(object.get(Constants.IMAGE_CAPTION)!=null)
								userObject.put(Constants.IMAGE_CAPTION, object.get(Constants.IMAGE_CAPTION));
								if(object.get(Constants.IMAGE_HEIGHT)!=null)
								userObject.put(Constants.IMAGE_HEIGHT, object.get(Constants.IMAGE_HEIGHT));
								if(object.get(Constants.IMAGE_WIDTH)!=null)
								userObject.put(Constants.IMAGE_WIDTH, object.get(Constants.IMAGE_WIDTH));
								if(object.get(Constants.ABSTRACT)!=null)
								userObject.put(Constants.ABSTRACT, object.get(Constants.ABSTRACT));
								if(object.get(Constants.CREDIT)!=null)
								userObject.put(Constants.CREDIT, object.get(Constants.CREDIT));
								if(object.get(Constants.CREDIT_URL)!=null)
								userObject.put(Constants.CREDIT_URL, object.get(Constants.CREDIT_URL));
								if(object.get(Constants.SITE_NAME)!=null)
								userObject.put(Constants.SITE_NAME, object.get(Constants.SITE_NAME));
								if(object.get(Constants.VIDEO_ID)!=null)
								userObject.put(Constants.VIDEO_ID, object.get(Constants.VIDEO_ID));


							if(object.get(Constants.POST_ATTRIBUTE)!=null)
							{
								HashMap<String, Object> map=(HashMap<String, Object>) object.get(Constants.POST_ATTRIBUTE);

								map.put(Constants.ORIGIN_GROUP_ID,fromGroupId);
								map.put(Constants.ORIGIN_GROUP_NAME,fromGroupName);
								userObject.put(Constants.POST_ATTRIBUTE, map);
							}
							else
							{
								HashMap<String, Object> map=new HashMap<String, Object>();
                                map.put(Constants.ORIGIN_GROUP_ID,fromGroupId);
                                map.put(Constants.ORIGIN_GROUP_NAME,fromGroupName);
								map.put(Constants.ORIGIN_FEED_ID,feedId);
                                userObject.put(Constants.POST_ATTRIBUTE, map);
							}


								userObject.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException arg0) {
										userObject.pinInBackground(toGroupId);
										Utility.showToastMessage(ShareInActivity.this, "You shared this post");
										finish();
									}
								});
							
						}
					}
					else
						System.out.println("exception in share ::: "+e);
			}});
	
	}
	
	void handleSendText(Intent intent,final String mGroupId1) {
		System.out.println("inside handleSendText");
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    System.out.println("link "+sharedText);
	    link=sharedText;
	    mGroupId=mGroupId1;
	    	if(Patterns.WEB_URL.matcher(sharedText).matches())
	    	{
	    		progressBar.setVisibility(View.VISIBLE);
	    		( new ParseURL() ).execute(new String[]{link});
	    	}
	    	else if(Utility.extractUrls(sharedText).size() > 0)
	    	{
			   link=Utility.extractUrls(sharedText).get(0).trim();
			   System.out.println("extracted url :: "+link.trim());
			   progressBar.setVisibility(View.VISIBLE);
		  	 ( new ParseURL() ).execute(new String[]{link});
	    	}
	    	else
	    	{
	    		insertValues(sharedText);
	    	}
	    
	}

	private void insertValues(String text) {
		progressBar.setVisibility(View.VISIBLE);
	    if (text != null) {
	    	gpsTracker = new GPSTracker(this);
			if (!gpsTracker.canGetLocation())
	        {
				 gpsTracker.showSettingsAlert(this);
	        }
			ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
			final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
			userObject.put(Constants.GROUP_ID, mGroupId);
			userObject.put(Constants.POST_TEXT, text);
			userObject.put(Constants.POST_TYPE, "Text");
			userObject.put(Constants.COMMENT_COUNT, 0);
			userObject.put(Constants.FLAG_COUNT, 0);
				userObject.put(Constants.IMAGE_CAPTION, "");
				userObject.put(Constants.LIKE_ARRAY, list);
				userObject.put(Constants.DIS_LIKE_ARRAY, list);
				userObject.put(Constants.POST_POINT, 100);
				if(groupObject.getBoolean(Constants.POST_APPROVAL) && !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
					userObject.put(Constants.POST_STATUS, "Pending");
				else
					userObject.put(Constants.POST_STATUS, "Active");
				userObject.put(Constants.FLAG_ARRAY, list);
				userObject.put(Constants.FEED_LOCATION, point);
				userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
				userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
				userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
				userObject.put(Constants.HASH_TAG_ARRAY, list);
				userObject.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException arg0) {
					userObject.pinInBackground(mGroupId);
					//Utility.showToastMessage(ShareInActivity.this, "Text posted successfully");
					//finish();
				}
			});
				if(groupObject.getBoolean(Constants.POST_APPROVAL) && !groupObject.getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
            		Utility.showToastMessage(ShareInActivity.this, getResources().getString(R.string.post_approval_text));
				else
					Utility.showToastMessage(ShareInActivity.this, "Text posted successfully");
				finish();
	    }
	}
	
	void handleGalleryVideo(Intent intent,String mGroupId)
	{
		String path=null;
	    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	   // path=intent.getStringExtra("path");
	   // if(path==null)
	    System.out.println("video path before convert :::"+imageUri.getPath());
	    	path=getRealPathFromURI(imageUri);
	    if(path==null)
	    {
	    	path=imageUri.getPath();
	    }
	    System.out.println("video path when sharing from outside app ::: "+path);
	    if (imageUri != null) {
	    	listView.setEnabled(true);
	    	startActivity(new Intent(ShareInActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("path",path).putExtra("share", true).putExtra("type", "video").putExtra("flag", true));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	    } 
		/* try{
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
         FileInputStream fis = new FileInputStream(new File(imageUri.getPath()));

         byte[] buf = new byte[1024];
         int n;
         while (-1 != (n = fis.read(buf)))
             baos.write(buf, 0, n);

         byte[] videoBytes = baos.toByteArray();
         
         mParseVideoFile = new ParseFile("Video.mp4", videoBytes);
			mPostImgFile.saveInBackground(new SaveCallback() {
		          public void done(ParseException e) {
		                 if (e == null) {
		                	 
		                 }
		          }});
    	}
    	catch (Exception e) {
			// TODO: handle exception
		}
    	 	
			//Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,200,200);
	    	//mPostImgView.setImageBitmap(bitmap);
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(imageUri.getPath(),
	                MediaStore.Images.Thumbnails.MINI_KIND);
			imgWidth=bitmap.getWidth();
			imgHeight=bitmap.getHeight();
		
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mPostImageThumbnail = new ParseFile("Image.png", img);
			mPostImageThumbnail.saveInBackground();
*/	}

	void handleSendImage(Intent intent,String mGroupId) {
		String path=null;
	    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    path=intent.getStringExtra("path");
	    if(path==null)
	    	path=getRealPathFromURI(imageUri);
	    if(path==null)
	    {
	    	path=getImageUrlWithAuthority(imageUri);
	    }
	    if (imageUri != null) {
	    	listView.setEnabled(true);
	    	startActivity(new Intent(ShareInActivity.this,PostImageActivity.class).putExtra(Constants.GROUP_ID, mGroupId).putExtra("path",path).putExtra("share", true).putExtra("type", "image").putExtra("flag", true));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	    }
	}
	public String getDataColumn(Uri uri) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
		column
		};
		try {
		cursor = getContentResolver().query(uri, projection, null, null,null);
		if (cursor != null && cursor.moveToFirst()) {
		final int index = cursor.getColumnIndexOrThrow(column);
		return cursor.getString(index);
		}
		} finally {
		if (cursor != null) {
		cursor.close();
		}
		}
		return null;
		}
	
	
	public String getImageUrlWithAuthority(Uri uri) {
	    InputStream is = null;
	    if (uri.getAuthority() != null) {
	        try {
	            is = getContentResolver().openInputStream(uri);
	            Bitmap bmp = BitmapFactory.decodeStream(is);
	            return writeToTempImageAndGetPathUri(bmp).toString();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return null;
	}

	public Uri writeToTempImageAndGetPathUri(Bitmap inImage) {
		
		String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/Chatterati");

        if (!mFolder.exists()) {
            mFolder.mkdir();
        }

        String s = "share.jpeg";

        File f = new File(mFolder.getAbsolutePath(),s);
        
        String strMyImagePath = f.getAbsolutePath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            inImage.compress(Bitmap.CompressFormat.JPEG,100, fos);

            fos.flush();
            fos.close();
         //   MediaStore.Images.Media.insertImage(getContentResolver(), b, "Screen", "screen");
        }catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
       // System.out.println("path after :::: "+path);
       /* 
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	    String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
	    System.out.println("path after :::: "+path);*/
	    return Uri.parse(strMyImagePath);
	}
	


public String getRealPathFromURI( Uri contentUri) {
  Cursor cursor = null;
  try { 
    String[] proj = { MediaStore.Images.Media.DATA };
    cursor = getContentResolver().query(contentUri,  proj, null, null, null);
    int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }
  catch (Exception e) {
	  return null;
}
}



	void handleSendMultipleImages(Intent intent,String mGroupId) {
		System.out.println("inside handleSendMultipleImages");
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	    	
	    }
	}
	
	private void initViews() {
		listView=(ListView) findViewById(R.id.listview);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
	}
	
	private void loadGroups()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.fromPin(Constants.USER_LOCAL_DATA_STORE);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject userObject, ParseException e) {
						if(userObject!=null)
						{
							List<String> myGroupIdList=userObject.getList(Constants.MY_GROUP_ARRAY);
							ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_TABLE);
							//query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
							query.whereContainedIn(Constants.OBJECT_ID, myGroupIdList);
							query.whereEqualTo(Constants.GROUP_STATUS, "Active");
							query.orderByDescending("updatedAt");
							query.fromPin(Constants.MY_GROUP_LOCAL_DATA_STORE);
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(final List<ParseObject> list, ParseException e) {
										if (e == null) 
										{
											groupList=list;
											if(list.size() > 0)
											{
												for(int i=0;i<groupList.size();i++)
												{
													if(groupList.get(i).getInt(Constants.WHO_CAN_POST)!=0)
													{
														if(!groupList.get(i).getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
														{
															groupList.remove(i);
														}
													}
													
													if(i==groupList.size()-1)
													{
														adapter=new ShareInAdapter(ShareInActivity.this, list);
														listView.setAdapter(adapter);
													}
												}
												
											}
											else
											{
												Utility.showToastMessage(ShareInActivity.this, getResources().getString(R.string.server_issue));
												finish();
											}
										}
										else
										{
											Utility.showToastMessage(ShareInActivity.this, getResources().getString(R.string.server_issue));
											finish();
										}
								}
							});
												
						}
						else
						{
							Utility.showToastMessage(ShareInActivity.this, getResources().getString(R.string.server_issue));
							finish();
						}
			}
		});
	}
	
	
	private class ParseURL extends AsyncTask<String, Void, Bitmap> {
		
	     @Override
	     protected Bitmap doInBackground(String... strings) {
	         try {
	        	 linkParams=new HashMap<String, Object>();
	        	 linkParams.put("url",strings[0]);
	        	 JSONArray oembed = api.oembed(linkParams);
	        	 System.out.println("json array :: "+oembed);
	        	 linkImage = oembed.getJSONObject(0).getString("thumbnail_url");
	        	 linkTitle=oembed.getJSONObject(0).getString("title");
	        	 siteName=oembed.getJSONObject(0).getString("provider_name");
	        	 
	        	 //System.out.println("link type :: "+oembed.getJSONObject(0).getString("type"));
	        	 
	        	 if(siteName.isEmpty())
	        	 {
	        		 URL url=new URL(link);
					 siteName=url.getHost();
	        	 }
	        	 
	        	 if(!linkImage.isEmpty())
	        	 {
	        		 Bitmap bitmap=getImageBitmapFromUrl(new URL(linkImage));
                	 return bitmap;
	        	 }
	        	 
	         }
	         catch(Exception t) {
	        	System.out.println("exception  :: "+t);
	           return null;
	         }
	 
	         return null;
	     }
	 
	     @Override
	     protected void onPostExecute(Bitmap bitmap) {
	         super.onPostExecute(bitmap);
	         
	       if(bitmap!=null)
	       {
	    	   if(!linkTitle.isEmpty() && !linkImage.isEmpty())
	    	   {
	    		   System.out.println("image url :: "+linkImage);
	    		   generateThumbnail(bitmap);
	    		   //Bitmap bitmap=Utility.compressImage(linkImagePath);
	   			   ByteArrayOutputStream stream = new ByteArrayOutputStream();
	   			   bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
	               imgByte = stream.toByteArray();
	               mPostImgFile = new ParseFile("Image.png", imgByte);
	             //  mPostImgFile.saveInBackground();
	               imgWidth=bitmap.getWidth();
	   			   imgHeight=bitmap.getHeight();
	   			   mPostImgFile.saveInBackground(new SaveCallback() {
	  	          public void done(ParseException e) {
	  	                 if (e == null) {
	  	                	progressBar.setVisibility(View.GONE);
	  	                	listView.setEnabled(true);
	  	                
	  	                	startActivity(new Intent(ShareInActivity.this,ShareLinkActivity.class).putExtra("isShare", true).putExtra(Constants.GROUP_ID, mGroupId)
	  	                			.putExtra("link_title",linkTitle).putExtra(Constants.LINK_URL,link).putExtra(Constants.SITE_NAME,siteName)
	  	                			.putExtra(Constants.IMAGE_WIDTH,imgWidth).putExtra(Constants.IMAGE_HEIGHT,imgHeight));
	  	                	overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
	  	                 }
	  	          }});
	    		   
	    	   }
	    	   else
		    	   insertValues(link);
	    	   
	       }
	       else
	    	   insertValues(link);
	     }
	}
	
	 private void generateThumbnail(Bitmap bitmap)
	    {
	    	//Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,200,200);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mPostImageThumbnail = new ParseFile("ThumbImage.png", img);
			mPostImageThumbnail.saveInBackground();
	    }

	 public Bitmap getImageBitmapFromUrl(URL url)
	  { 
	    Bitmap bm = null; 
	    try { 
	      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	      if(conn.getResponseCode() != 200)
	      {
	        return bm;
	      }
	      conn.connect();
	      InputStream is = conn.getInputStream();

	      BufferedInputStream bis = new BufferedInputStream(is); 
	      try
	      {
	        bm = BitmapFactory.decodeStream(bis); 
	      }
	      catch(OutOfMemoryError ex)
	      {
	        bm = null;
	      }
	      bis.close(); 
	      is.close(); 
	    } catch (Exception e) {}
	    
	      return bm; 
	  }
	 
		 private void generateVideoThumbnail(String picturePath)
	    {
	    	Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,200,200);
	    	//mPostImgView.setImageBitmap(bitmap);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mPostImageThumbnail = new ParseFile("Image.png", img);
			mPostImageThumbnail.saveInBackground();
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
