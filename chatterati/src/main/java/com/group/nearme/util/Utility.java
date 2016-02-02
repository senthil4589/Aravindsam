package com.group.nearme.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;
import com.group.nearme.R;
import com.group.nearme.ShareInActivity;
import com.group.nearme.objects.ContactInfoObject;
import com.group.nearme.objects.GroupInfo;
import com.group.nearme.settings.GroupNearMeApplication;
import com.group.nearme.settings.GroupNearMeApplication.TrackerName;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class Utility {
	public static final int REQUEST_CODE = 15;
	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
	private static final Pattern urlPattern = Pattern.compile(
	        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
	                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
	                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
	        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	public static LatLng latlng;
	public static Dialog mDialog;
	public static List<ParseObject> list=new ArrayList<ParseObject>();
	
	public static ArrayList<String> selectedTagList;
	
	public static ArrayList<String> getSelectedTagList() {
		return selectedTagList;
	}

	public static void setSelectedTagList(ArrayList<String> selectedTagList) {
		Utility.selectedTagList = selectedTagList;
	}

	public static GroupInfo groupInfo=new GroupInfo();
	
	public static GroupInfo getGroupInfo() {
		return groupInfo;
	}

	public static void setGroupInfo(GroupInfo groupInfo) {
		Utility.groupInfo = groupInfo;
	}

	public static ParseObject groupObject;

	public static ParseObject getShareOriginGroupObject() {
		return shareOriginGroupObject;
	}

	public static void setShareOriginGroupObject(ParseObject shareOriginGroupObject) {
		Utility.shareOriginGroupObject = shareOriginGroupObject;
	}

	public static ParseObject shareOriginGroupObject;
	public static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	public static boolean  isRefershNeed;
	static AlertDialog mAlertDialog;
	static ConnectivityManager connectivityManager;
	static ParseFile userImageFile=null;
	
	static ArrayList<ContactInfoObject> contactList=new ArrayList<ContactInfoObject>();
	static HashMap<String, Boolean> checkedMap=new HashMap<String, Boolean>();
	
	public static HashMap<String, Boolean> getCheckedMap() {
		return checkedMap;
	}

	public static void setCheckedMap(HashMap<String, Boolean> checkedMap) {
		Utility.checkedMap=new HashMap<String, Boolean>();
		Utility.checkedMap.putAll(checkedMap);
	}

	public static ArrayList<ContactInfoObject> getContactList() {
		return contactList;
	}

	public static void setContactList(ArrayList<ContactInfoObject> contactList) {
		Utility.contactList.addAll(contactList);
	}

	public static ParseFile getUserImageFile() {
		return userImageFile;
	}

	public static void setUserImageFile(ParseFile userImageFile) {
		Utility.userImageFile = userImageFile;
	}

	public static byte[] bitmapdata = null;
	public static ParseFile parseFile = null;
	
	/*public static byte[] getBitmapdata() {
		return bitmapdata;
	}

	public static void setBitmapdata(byte[] bitmapdata) {
		Utility.bitmapdata = bitmapdata;
	}
*/
	
	public static ParseFile getParseFile() {
		return parseFile;
	}

	public static void setParseFile(ParseFile parseFile) {
		Utility.parseFile = parseFile;
	}

	public static boolean isRefershNeed() {
		return isRefershNeed;
	}

	public static void setRefershNeed(boolean isRefershNeed) {
		Utility.isRefershNeed = isRefershNeed;
	}

	private static String mPhotoPath;
	
	
	public static int imgWidth,imgHeight;
	
	
	public static ParseObject getGroupObject() {
		return groupObject;
	}

	public static void setGroupObject(ParseObject groupObject) {
		Utility.groupObject = groupObject;
	}
	

	

	public static String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

		Log.e("LOOK", imageEncoded);
		return imageEncoded;
	}

	public static Bitmap decodeBase64(String input) {
		byte[] decodedByte = Base64.decode(input, 0);
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}
	
	public static void setLatLng(LatLng latlng1)
	{
		latlng=latlng1;
	}
	
	public static LatLng getLatLng()
	{
		return latlng;
	}
	
	public static String photoPath() {
		return mPhotoPath;
	}

	
	public static void uploadImage(final Activity activity,final int cameraRequestCode,final int galleryRequestCode)
    {
    	mDialog = new Dialog(activity,R.style.customDialogStyle);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.choose_picture);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		TextView cameraImage=(TextView) mDialog.findViewById(R.id.take_photo);
		TextView galleryImage=(TextView) mDialog.findViewById(R.id.from_gallery);
		cameraImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				Intent pickImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				/*// pickImageIntent.setType("image/*");
				  pickImageIntent.putExtra("crop", "true");
				  pickImageIntent.putExtra("outputX", 100);
				  pickImageIntent.putExtra("outputY", 100);
				  pickImageIntent.putExtra("aspectX", 1);
				  pickImageIntent.putExtra("aspectY", 1);
				  pickImageIntent.putExtra("scale", true);
				  pickImageIntent.putExtra("outputFormat",  Bitmap.CompressFormat.JPEG.toString());*/
				File file = null;
				try {
					file = setUpPhotoFile();
					mPhotoPath = file.getAbsolutePath();
					pickImageIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(file));
					pickImageIntent.putExtra("Path", mPhotoPath);
				} catch (IOException e) {
					//e.printStackTrace();
					file = null;
					mPhotoPath = null;
				}
				activity.startActivityForResult(pickImageIntent, cameraRequestCode);
				
			}
		});
		galleryImage.setOnClickListener(new View.OnClickListener() {
			@SuppressLint({ "InlinedApi", "NewApi" })
			@Override
			public void onClick(View v) {
				  mDialog.dismiss();
				 /* Intent i = new Intent(  Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				  activity.startActivityForResult(i, galleryRequestCode);	*/	
				  Intent pickImageIntent = new Intent(  Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				  //pickImageIntent.addCategory(Intent.CATEGORY_APP_GALLERY);
				 // pickImageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
				 // pickImageIntent.putExtra(Intent.EXTRA_RESTRICTIONS_INTENT, false);
				  //pickImageIntent.putExtra(Intent.EXTRA_INTENT, "android.intent.action.MAIN");
				  pickImageIntent.setType("image/*");
				  pickImageIntent.putExtra("crop", "true");
				  pickImageIntent.putExtra("outputX", 200);
				  pickImageIntent.putExtra("outputY", 200);
				  pickImageIntent.putExtra("aspectX", 1);
				  pickImageIntent.putExtra("aspectY", 1);
				  pickImageIntent.putExtra("scale", true);
				  
				//  pickImageIntent.putExtra(Intent., true);
				  File file = null;

					try {
						file = setUpPhotoFile();
						mPhotoPath = file.getAbsolutePath();
						pickImageIntent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(file));
						pickImageIntent.putExtra("Path", mPhotoPath);
					} catch (IOException e) {
						//e.printStackTrace();
						file = null;
						mPhotoPath = null;
					}
				  pickImageIntent.putExtra("outputFormat",  Bitmap.CompressFormat.JPEG.toString());
				  activity.startActivityForResult(pickImageIntent, galleryRequestCode);
			}
		});
		mDialog.show();
    }
	
	public static void callCameraIntent(final Activity activity)
	{
		Intent pickImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = null;
		try {
			file = setUpPhotoFile();
			mPhotoPath = file.getAbsolutePath();
			pickImageIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(file));
			pickImageIntent.putExtra("Path", mPhotoPath);
		} catch (IOException e) {
			file = null;
			mPhotoPath = null;
		}
		
		activity.startActivityForResult(pickImageIntent, REQUEST_CODE);
	}
	
	public static File getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		/*deleteDirectory();
		// For future implementation: store videos in a separate directory
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "/Chatterati/Crop");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MatchIt", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
				.format(new Date());
		//String fileName = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
		 * 
*/		
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "/Chatterati");
		
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MatchIt", "failed to create directory");
				return null;
			}
		}
		
		File file = new File(mediaStorageDir.getPath() + File.separator
		+ JPEG_FILE_PREFIX + "cameraImage" + JPEG_FILE_SUFFIX);
		if(file.exists()){
				file.delete();
			}
		try {
			file.createNewFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		//mPhotoPath=file.getAbsolutePath();
		/*if(mediaFile.exists())
		{
			mediaFile.delete();
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ JPEG_FILE_PREFIX + "image" + JPEG_FILE_SUFFIX);
			try {
				mediaFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		return file;
	}

	private static File setUpPhotoFile() throws IOException {
		File temp = getOutputMediaFile();
		System.out.println("image path = " + temp.getAbsolutePath());
		mPhotoPath = temp.getAbsolutePath();
		return temp;
	}
	
	private static void deleteDirectory()
	{
		File dir = new File(Environment.getExternalStorageDirectory()+"/Chatterati/Crop"); 
		if (dir.isDirectory()) {
		        String[] children = dir.list();
		        for (int i = 0; i < children.length; i++) {
		            new File(dir, children[i]).delete();
		        }
		}	}
	
	public static  String getTimeAgo(long time) {
	    if (time < 1000000000000L) {
	        // if timestamp given in seconds, convert to millis
	        time *= 1000;
	    }

	    long now = System.currentTimeMillis();
	    if (time > now || time <= 0) {
	        return null;
	    }

	    // TODO: localize
	    final long diff = now - time;
	    if (diff < MINUTE_MILLIS) {
	        return "just now";
	    } else if (diff < 2 * MINUTE_MILLIS) {
	        return "a minute ago";
	    } else if (diff < 50 * MINUTE_MILLIS) {
	        return diff / MINUTE_MILLIS + " minutes ago";
	    } else if (diff < 90 * MINUTE_MILLIS) {
	        return "an hour ago";
	    } else if (diff < 24 * HOUR_MILLIS) {
	        return diff / HOUR_MILLIS + " hours ago";
	    } else if (diff < 48 * HOUR_MILLIS) {
	        return "yesterday";
	    } else {
	        return diff / DAY_MILLIS + " days ago";
	    }
	}
	
	@SuppressLint("SimpleDateFormat")
	
	public static String date() {
		DateFormat dateFormat = new SimpleDateFormat(" MM/dd/yyyy h.mm:ss a");
		Date systemDate = new Date();
		return dateFormat.format(systemDate);

	}

	public static void setList(List<ParseObject> list1)
	{
		list=list1;
	}
	
	public static List<ParseObject> getList()
	{
		return list;
	}
	
	/*public static void setInvitationList(List<ParseObject> list)
	{
		invitationList=list;
	}
	public static List<ParseObject> getInvitationList()
	{
		return invitationList;
	}*/
	/*public static void setUserList(List<ParseObject> list)
	{
		userList=list;
	}*/
	/*public static List<ParseObject> getUserList()
	{
		return userList;
	}*/
	
	
	
	public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
	}
	
	public static boolean hasImageCaptureBug() {

	    // list of known devices that have the bug
	    ArrayList<String> devices = new ArrayList<String>();
	    devices.add("android-devphone1/dream_devphone/dream");
	    devices.add("generic/sdk/generic");
	    devices.add("vodafone/vfpioneer/sapphire");
	    devices.add("tmobile/kila/dream");
	    devices.add("verizon/voles/sholes");
	    devices.add("google_ion/google_ion/sapphire");

	    return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
	            + android.os.Build.DEVICE);

	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
	
	public static Bitmap decodeSampledBitmapFromResource(String path,int width,int height) {
	        
		 File image = new File(path);
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;

         Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),options);
         options.inSampleSize = Utility.calculateInSampleSize(options, width, height);

         // Decode bitmap with inSampleSize set
         options.inJustDecodeBounds = false;

        // bitmap=ExifUtils.rotateBitmap(picturePath, bitmap);
         bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),options);
         bitmap=ExifUtils.rotateBitmap(path, bitmap);
         return bitmap;
	}
	
	public static Bitmap decodeSampledBitmapFromCamera(String path,int width,int height) {
        
		File image = new File(path);
    	// File image = new File(path);
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;

         Bitmap bitmap = BitmapFactory.decodeFile(image.getPath(),options);
         options.inSampleSize = Utility.calculateInSampleSize(options,width,height);

         // Decode bitmap with inSampleSize set
         options.inJustDecodeBounds = false;

        // bitmap=ExifUtils.rotateBitmap(picturePath, bitmap);
         bitmap = BitmapFactory.decodeFile(image.getPath(),options);
         System.out.println("bitmap :::"+bitmap);
        	 bitmap=ExifUtils.rotateBitmap(image.getPath(), bitmap);
        return bitmap;
	}
	
	 public static void performCrop(String path,Activity activity) {
	        // take care of exceptions
		 File image = new File(path);
		 Uri uri=Uri.fromFile(image); 
	        try {
	            // call the standard crop action intent (the user device may not
	            // support it)
	            Intent cropIntent = new Intent("com.android.camera.action.CROP");
	            // indicate image type and Uri
	            cropIntent.setDataAndType(uri, "image/*");
	            // set crop properties
	            cropIntent.putExtra("crop", "true");
	            // indicate aspect of desired crop
	            cropIntent.putExtra("aspectX", 1);
	            cropIntent.putExtra("aspectY", 1);
	            // indicate output X and Y
	            cropIntent.putExtra("outputX", 150);
	            cropIntent.putExtra("outputY", 150);
	            // retrieve data on return
	            cropIntent.putExtra("return-data", true);
	            // start the activity - we handle returning in onActivityResult
	            activity.startActivityForResult(cropIntent, 9);
	        }
	        // respond to users whose devices do not support the crop action
	        catch (ActivityNotFoundException anfe) {
	            Toast toast = Toast
	                    .makeText(activity, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
	            toast.show();
	        }
	    }
	 
	 public static void performCrop(Uri contentUri,Activity activity) {
	        try {
	            //Start Crop Activity

	            Intent cropIntent = new Intent("com.android.camera.action.CROP");
	            // indicate image type and Uri
	            //File f = new File(picUri);
	           // Uri contentUri = Uri.fromFile(f);

	            cropIntent.setDataAndType(contentUri, "image/*");
	            // set crop properties
	            cropIntent.putExtra("crop", "true");
	            // indicate aspect of desired crop
	            cropIntent.putExtra("aspectX", 1);
	            cropIntent.putExtra("aspectY", 1);
	            // indicate output X and Y
	            cropIntent.putExtra("outputX", 150);
	            cropIntent.putExtra("outputY", 150);

	            // retrieve data on return
	            cropIntent.putExtra("return-data", true);
	            // start the activity - we handle returning in onActivityResult
	            activity.startActivityForResult(cropIntent, 9);
	        }
	        // respond to users whose devices do not support the crop action
	        catch (ActivityNotFoundException anfe) {
	            // display an error message
	            String errorMessage = "your device doesn't support the crop action!";
	            Toast toast = Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT);
	            toast.show();
	        }
	    }   

	 
		public static void showOkDilaog(final Activity mActivity, String msg){
			mDialog = new Dialog(mActivity);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mDialog.setContentView(R.layout.two_btn_dialog);
			mDialog.setCancelable(true);
			mDialog.setCanceledOnTouchOutside(true);
			
			WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
			windowManager.gravity = Gravity.CENTER;
			Button yes=(Button) mDialog.findViewById(R.id.yes);
			Button no=(Button) mDialog.findViewById(R.id.no);
			TextView message=(TextView) mDialog.findViewById(R.id.msg);
			message.setText(msg);
			no.setVisibility(View.GONE);
			yes.setText("OK");
			
			yes.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			
			mDialog.show();
		}
		

		public static boolean checkInternetConnectivity(Context mContext){
			try{
	            connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() || 
	            		connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
	            	
	                return true;
	            }
	        }
	        catch(Exception e){
	        	
	        }
			return false;
		}
		
		public static Date getCurrentDate()
		{
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

			// Get the date today using Calendar object.
			Date today = Calendar.getInstance().getTime();     
			return today;
		}
		
		/*public static ParseFile getDefaultImage(Activity activity)
		{
			Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),
					R.drawable.icon3);
					// Convert it to byte
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					// Compress image to lower quality scale 1 - 100
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] image = stream.toByteArray();
					// Create the ParseFile
					ParseFile mGroupImageFile = new ParseFile("GroupImage.png", image);
					// Upload the image into Parse Cloud
					mGroupImageFile.saveInBackground(new SaveCallback() {
				          public void done(ParseException e) {
				                 if (e == null) {
				                	 
				                 }
				          }});
					
		}*/
		
		public static Bitmap compressImage(String imageUri) {
			 
	        String filePath = getRealPathFromURI(imageUri);
	        Bitmap scaledBitmap = null;
	 
	        BitmapFactory.Options options = new BitmapFactory.Options();
	 
//	      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//	      you try the use the bitmap here, you will get null.
	        options.inJustDecodeBounds = true;
	        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
	 
	        int actualHeight = options.outHeight;
	        int actualWidth = options.outWidth;
	 
//	      max Height and width values of the compressed image is taken as 816x612
	 
	        float maxHeight = 816.0f;
	        float maxWidth = 612.0f;
	        float imgRatio = actualWidth / actualHeight;
	        float maxRatio = maxWidth / maxHeight;
	 
//	      width and height values are set maintaining the aspect ratio of the image
	 
	        if (actualHeight > maxHeight || actualWidth > maxWidth) {
	            if (imgRatio < maxRatio) {               
	            	imgRatio = maxHeight / actualHeight;                
	            	actualWidth = (int) (imgRatio * actualWidth);               
	            	actualHeight = (int) maxHeight;             
	            	} else if (imgRatio > maxRatio) {
	                imgRatio = maxWidth / actualWidth;
	                actualHeight = (int) (imgRatio * actualHeight);
	                actualWidth = (int) maxWidth;
	            } else {
	                actualHeight = (int) maxHeight;
	                actualWidth = (int) maxWidth;
	 
	            }
	        }
	 
//	      setting inSampleSize value allows to load a scaled down version of the original image
	 
	        options.inSampleSize = calculateInSampleSize1(options, actualWidth, actualHeight);
	 
//	      inJustDecodeBounds set to false to load the actual bitmap
	        options.inJustDecodeBounds = false;
	 
//	      this options allow android to claim the bitmap memory if it runs low on memory
	        options.inPurgeable = true;
	        options.inInputShareable = true;
	        options.inTempStorage = new byte[16 * 1024];
	 
	        try {
//	          load the bitmap from its path
	            bmp = BitmapFactory.decodeFile(filePath, options);
	        } catch (OutOfMemoryError exception) {
	            exception.printStackTrace();
	 
	        }
	        try {
	            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
	        } catch (OutOfMemoryError exception) {
	            exception.printStackTrace();
	        }
	 
	        float ratioX = actualWidth / (float) options.outWidth;
	        float ratioY = actualHeight / (float) options.outHeight;
	        float middleX = actualWidth / 2.0f;
	        float middleY = actualHeight / 2.0f;
	 
	        Matrix scaleMatrix = new Matrix();
	        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
	 
	        Canvas canvas = new Canvas(scaledBitmap);
	        canvas.setMatrix(scaleMatrix);
	        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
	 
//	      check the rotation of the image and display it properly
	        ExifInterface exif;
	        try {
	            exif = new ExifInterface(filePath);
	 
	            int orientation = exif.getAttributeInt(
	                    ExifInterface.TAG_ORIENTATION, 0);
	            Log.d("EXIF", "Exif: " + orientation);
	            Matrix matrix = new Matrix();
	            if (orientation == 6) {
	                matrix.postRotate(90);
	                Log.d("EXIF", "Exif: " + orientation);
	            } else if (orientation == 3) {
	                matrix.postRotate(180);
	                Log.d("EXIF", "Exif: " + orientation);
	            } else if (orientation == 8) {
	                matrix.postRotate(270);
	                Log.d("EXIF", "Exif: " + orientation);
	            }
	            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
	                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
	                    true);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	 
	        FileOutputStream out = null;
	        String filename = getFilename();
	        try {
	            out = new FileOutputStream(filename);
	 
//	          write the compressed bitmap at the destination specified by filename.
	            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
	            	
	        } catch (FileNotFoundException e) {
	            //e.printStackTrace();
	        }
	 
	        return scaledBitmap;
	 
	    }
	 
	 public static String getFilename() {
		    File file = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/FeedImages");
		    if (!file.exists()) {
		        file.mkdirs();
		    }
		    File file1 = new File(file.getAbsolutePath() + "/" + "feed" + ".jpg");
        	
		    if(file1.exists()){
				file1.delete();
			}
		    
		    String uriSting = (file.getAbsolutePath() + "/" + "feed" + ".jpg");
		    return uriSting;
		 
		}
	 private static String getRealPathFromURI(String contentURI) {
	        Uri contentUri = Uri.parse(contentURI);
	        Cursor cursor = GroupNearMeApplication.getAppContext().getContentResolver().query(contentUri, null, null, null, null);
	        if (cursor == null) {
	            return contentUri.getPath();
	        } else {
	            cursor.moveToFirst();
	            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
	            return cursor.getString(index);
	        }
	    }
	 public static int calculateInSampleSize1(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		    final int height = options.outHeight;
		    final int width = options.outWidth;
		    int inSampleSize = 1;
		 
		    if (height > reqHeight || width > reqWidth) {
		        final int heightRatio = Math.round((float) height/ (float) reqHeight);
		        final int widthRatio = Math.round((float) width / (float) reqWidth);
		        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
		        inSampleSize++;
		    }
		 
		    return inSampleSize;
		}
	 
	 public static void showToastMessage(Activity activity,String msg)
	 {
			Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
			
			//Toast toast = Toast.makeText(AboutYouActivity.this, "Please enter your name and email", Toast.LENGTH_LONG);
			//toast.setGravity(Gravity.CENTER, 0, 0);
			//toast.show();
	 }
	 
	 @SuppressLint("SimpleDateFormat") public static Date getCurrentUTCDate()
	 {
		Date currentTime = new Date();
		 /*//System.out.println("date utc :: "+currentTime.);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateFormat.format(currentTime);
			//dateFormat.getCalendar().getInstance().getTime();
			try {
				return dateFormat.parse(dateFormat.format(currentTime));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Dat
		*/	return currentTime;
	 }
	 
	// Google Analytics
				public static Tracker getTracker(Context context,String scrennName)
				{
					// Get tracker.
					Tracker tracker = ((GroupNearMeApplication) context.getApplicationContext())
							.getTracker(TrackerName.APP_TRACKER);
					// Set screen name.
					// Where path is a String representing the screen name.
					 tracker.setScreenName(scrennName);
					// Send a screen view.
					 tracker.send(new HitBuilders.AppViewBuilder().build());
					return tracker;
					
				}
				
				
	
	 
	 public static Date getFutureDate()
	 {
		 Calendar c = Calendar.getInstance();

			c.set(Calendar.YEAR, 2100);
			c.set(Calendar.MONTH, Calendar.DECEMBER);
			c.set(Calendar.DAY_OF_MONTH, 31);
			
			return c.getTime();
	 }
	 
	 public static long getMaxDate()
	 {
		 Calendar c = Calendar.getInstance();

			c.set(Calendar.YEAR, 2099);
			c.set(Calendar.MONTH, Calendar.DECEMBER);
			c.set(Calendar.DAY_OF_MONTH, 31);
			
			return c.getTimeInMillis();
	 }
	 
	 public static Date getPastDate()
	 {
		 Calendar c = Calendar.getInstance();

			c.set(Calendar.YEAR, 1900);
			c.set(Calendar.MONTH, Calendar.DECEMBER);
			c.set(Calendar.DAY_OF_MONTH, 31);
			
			return c.getTime();
	 }
	 
	 public static String extractYTId(String ytUrl) {
		    String vId = null;
		  /*  Pattern pattern = Pattern.compile(
		    		"(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*", 
		                     Pattern.CASE_INSENSITIVE);
		    Matcher matcher = pattern.matcher(ytUrl);
		    if (matcher.matches()){
		        vId = matcher.group(1);
		    }
*/		    String pattern = "((?<=(v|V)/)|(?<=be/)|(?<=(\\?|\\&)v=)|(?<=embed/))([\\w-]++)";

			Pattern compiledPattern = Pattern.compile(pattern);
			Matcher matcher = compiledPattern.matcher(ytUrl);

			if(matcher.find()){
				return matcher.group();
			}
		    
		    return vId;
		}
	 
	 public static List<String> extractUrls(String text)
	 {
	     List<String> containedUrls = new ArrayList<String>();
	     //String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	     //Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
	     Matcher urlMatcher = urlPattern.matcher(text);

	     while (urlMatcher.find())
	     {
	         containedUrls.add(text.substring(urlMatcher.start(0),
	                 urlMatcher.end(0)));
	     }

	     return containedUrls;
	 }
	 
	 public static Typeface getTypeface()
	 {
		 return Typeface.createFromAsset(GroupNearMeApplication.getAppContext().getAssets(), "Lato-Regular.ttf");
	 }
	 public static void sharePost(final Activity activity,final String groupName,final String mGroupId,final String feedId)
	    {
	    	mDialog = new Dialog(activity,R.style.customDialogStyle);
	    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mDialog.setContentView(R.layout.share_view);
			mDialog.setCancelable(true);
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.getWindow().setBackgroundDrawableResource(R.drawable.borders);
			WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
			windowManager.gravity = Gravity.CENTER;
			TextView shareInternal=(TextView) mDialog.findViewById(R.id.chatterati);
			TextView shareExternal=(TextView) mDialog.findViewById(R.id.external_share);
			shareInternal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					Intent sharingIntent = new Intent(activity,ShareInActivity.class);
					sharingIntent.putExtra(Constants.GROUP_ID, mGroupId);
					sharingIntent.putExtra(Constants.GROUP_NAME, groupName);
					sharingIntent.putExtra(Constants.FEED_ID, feedId);
					sharingIntent.putExtra("isFromChatterati", true);
					activity.startActivity(sharingIntent);
				}
			});
			shareExternal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String str="http://chtr.in/"+feedId;
					mDialog.dismiss();
					Intent sharingIntent = new Intent(Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					sharingIntent.putExtra(Intent.EXTRA_TEXT, str);
					System.out.println("url ::: "+str);
					//sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Chatterati "+mGroupName+" group");
					activity.startActivity(Intent.createChooser(sharingIntent, "Chatterati Sharing..."));
				}
			});
			mDialog.show();
	    }


}
