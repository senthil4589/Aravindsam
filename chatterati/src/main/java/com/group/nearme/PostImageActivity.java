package com.group.nearme;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beanie.imagechooser.api.ChooserType;
import com.beanie.imagechooser.api.ChosenImage;
import com.beanie.imagechooser.api.ChosenVideo;
import com.beanie.imagechooser.api.ImageChooserListener;
import com.beanie.imagechooser.api.ImageChooserManager;
import com.beanie.imagechooser.api.VideoChooserListener;
import com.beanie.imagechooser.api.VideoChooserManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.ResizableImageView;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class PostImageActivity extends Activity implements ImageChooserListener,VideoChooserListener{
	public static final String LOG_TAG="PostImageActivity";
	
	private EditText mImageCaptionEditTxt;
	private ImageView mCancelImgBtn,mPostImgBtn;
	private ResizableImageView mPostImgView;
	private boolean mFlag,isShare;
	private ParseFile mPostImgFile,mPostImageThumbnail,mParseVideoFile;
	private String mImageCaption="",mGroupId="";
	private ArrayList<String> list=new ArrayList<String>();
	private ProgressBar progressBar;
	public static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private GPSTracker gpsTracker;
	private int imgWidth,imgHeight;
	private ImageChooserManager imageChooserManager;
	private String filePath;
	private int chooserType;
	private RelativeLayout btnLayout;
	byte[] imgByte;
	String userMobileNo="",inputType="",shareType="";
	
	private VideoChooserManager videoChooserManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_image);
		Utility.getTracker(this, "IMAGE POST WITH CAPTION SCREEN");
		initViews();
		if(isShare)
		{
			shareType=getIntent().getStringExtra("type");
			String path=getIntent().getStringExtra("path");
			System.out.println("image path ::: "+path);
			if(shareType.equals("image"))
			{	
				generateThumbnail(path);
				Bitmap bitmap=Utility.compressImage(path);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
	            imgByte = stream.toByteArray();
	            imgWidth=bitmap.getWidth();
				imgHeight=bitmap.getHeight();
				mPostImgView.setImageBitmap(bitmap);
			}
			else if(shareType.equals("video"))
			{
				
				int fileSizeInBytes=(int) new File(path).length();
            	long fileSizeInKB = fileSizeInBytes / 1024;
            	// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            	long fileSizeInMB = fileSizeInKB / 1024;
            	float remainder=fileSizeInKB%1024;
            	System.out.println("file size mb ::: remain :: "+fileSizeInMB+"  "+remainder);
            	System.out.println("file size ::: "+fileSizeInMB);
            	if (fileSizeInMB <= 9.5) {

					mImageCaptionEditTxt.setHint("Say something about this video...");
					//generateVideoThumbnail(path,true);
					Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
			                MediaStore.Images.Thumbnails.MINI_KIND);
					mPostImgView.setImageBitmap(bitmap);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
					byte[] img = stream.toByteArray();
					mPostImageThumbnail = new ParseFile("Image.png", img);
					imgWidth=bitmap.getWidth();
					imgHeight=bitmap.getHeight();
					try
	            	{
	            	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                 FileInputStream fis = new FileInputStream(new File(path));
	
	                 byte[] buf = new byte[1024];
	                 int n;
	                 while (-1 != (n = fis.read(buf)))
	                     baos.write(buf, 0, n);
	
	                 byte[] videoBytes = baos.toByteArray();
	                 
	                 mParseVideoFile = new ParseFile("Video.mp4", videoBytes);
	                 mParseVideoFile.saveInBackground(new SaveCallback() {
	 			          public void done(ParseException e) {
	 			                 if (e == null) {
	 			                	 
	 			                 }
	 			          }});
	            	}
	            	catch (Exception e) {
					}
			
	   			}
	            else
		    	{
	            	ShareInActivity.activity.finish();
	            	finish();
		    	    Toast.makeText(PostImageActivity.this, "File size is too large. Limit is less than 10MB", Toast.LENGTH_LONG).show();
		    	}
			}
		}
		else
		{
			inputType=getIntent().getStringExtra("type");
			if(inputType.equals("CameraImage"))
			{
				takePicture();
			}
			else if(inputType.equals("GalleryImage"))
			{
				chooseImage();
			}
			else if(inputType.equals("Video"))
			{
				mImageCaptionEditTxt.setHint("Say something about this video...");
				mPostImgBtn.setEnabled(false);
				//String workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
				//copyLicenseFromAssetsToSDIfNeeded(this,workFolder);
				pickVideo();
			}
			else if(inputType.equals("GIF"))
			{
				mImageCaptionEditTxt.setHint("Say something about this GIF...");
				mPostImgBtn.setEnabled(false);
				//String workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
				//copyLicenseFromAssetsToSDIfNeeded(this,workFolder);
				pickVideo();
			}
		}
		
		
		mCancelImgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mPostImgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mImageCaption=mImageCaptionEditTxt.getText().toString();
				
				if (gpsTracker.canGetLocation())
		        {
					if(isShare && shareType.equals("video"))
					{
						insertVideoInfo();
					}
					else if(inputType.equals("Video") || inputType.equals("GIF"))
					{
						insertVideoInfo();
					}
					else
					{
						insertValues();
					}
				 	
		        }
		        else
		        {
		            gpsTracker.showSettingsAlert(PostImageActivity.this);
		        }
			}
		});
	}
	
	public void copyLicenseFromAssetsToSDIfNeeded(Activity act, String destinationFolderPath) {
		InputStream is = null;
		BufferedOutputStream o = null;
		boolean copyLic = true;
		File destLic = null;
		try {
		 is = getResources().openRawResource(
		            getResources().getIdentifier("raw/ffmpeg",
		                    "raw", getPackageName()));
		} catch (Exception e) {
			Log.i("", "License file does not exist in the assets.");
			copyLic = false;
		}

		if (copyLic) {
			destLic = new File(destinationFolderPath + "ffmpeg");
			Log.i("", "Adding lic file at " + destLic.getAbsolutePath());

			o = null;
			try {
				byte[] buff = new byte[10000];
				int read = -1;
				o = new BufferedOutputStream(new FileOutputStream(destLic), 10000);
				while ((read = is.read(buff)) > -1) { 
					o.write(buff, 0, read);
				}
				Log.i("", "Copy " + destLic.getAbsolutePath() + " from assets to SDCARD finished succesfully");
			}
			catch (Exception e) {
				Log.e("", "Error when coping license file from assets to working folder: " + e.getMessage());
			}
			finally {
				try {
					is.close();
					if (o != null) o.close();
				} catch (IOException e) {
					Log.w("", "Error when closing license file io: " + e.getMessage());
				}  

			}

		}
		else {
			Log.i("", "Not coping license");
		}
	
	}
	
	
	/*private void compressVideo()
	{
		  MediaCodec decoder = MediaCodec.createDecoderByType(mime);
		    decoder.configure(format, null , null , 0);
		    decoder.start();
		    ByteBuffer[] codecInputBuffers = decoder.getInputBuffers();
		    ByteBuffer[] codecOutputBuffers = decoder.getOutputBuffers();

		    //init encoder
		    MediaCodec encoder = MediaCodec.createEncoderByType(mime);
		    int width = format.getInteger(MediaFormat.KEY_WIDTH);
		    int height = format.getInteger(MediaFormat.KEY_HEIGHT);
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
		    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 400000);
		    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
		    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
		    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		    encoder.configure(mediaFormat, null , null , MediaCodec.CONFIGURE_FLAG_ENCODE);
		    encoder.start();
		    ByteBuffer[] encoderInputBuffers = encoder.getInputBuffers();
		    ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

		    extractor.selectTrack(0);

		    boolean sawInputEOS = false;
		    boolean sawOutputEOS = false;
		    boolean sawOutputEOS2 = false;
		    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		    BufferInfo encoderInfo = new MediaCodec.BufferInfo();

		    while (!sawInputEOS || !sawOutputEOS || !sawOutputEOS2) {
		        if (!sawInputEOS) {
		            sawInputEOS = decodeInput(extractor, decoder, codecInputBuffers);
		        }

		        if (!sawOutputEOS) {
		            int outputBufIndex = decoder.dequeueOutputBuffer(info, 0);
		            if (outputBufIndex >= 0) {
		                sawOutputEOS = decodeEncode(extractor, decoder, encoder, codecOutputBuffers, encoderInputBuffers, info, outputBufIndex);
		            } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
		                Log.d(LOG_TAG, "decoding INFO_OUTPUT_BUFFERS_CHANGED");
		                codecOutputBuffers = decoder.getOutputBuffers();
		            } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
		                final MediaFormat oformat = decoder.getOutputFormat();
		                Log.d(LOG_TAG, "decoding Output format has changed to " + oformat);
		            } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
		                 Log.d(LOG_TAG, "decoding dequeueOutputBuffer timed out!");
		            }
		        }

		        if (!sawOutputEOS2) {
		            int encodingOutputBufferIndex = encoder.dequeueOutputBuffer(encoderInfo, 0);
		            if (encodingOutputBufferIndex >= 0) {
		                sawOutputEOS2 = encodeOuput(outputStream, encoder, encoderOutputBuffers, encoderInfo, encodingOutputBufferIndex);
		            } else if (encodingOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
		                Log.d(LOG_TAG, "encoding INFO_OUTPUT_BUFFERS_CHANGED");
		                encoderOutputBuffers = encoder.getOutputBuffers();
		            } else if (encodingOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
		                final MediaFormat oformat = encoder.getOutputFormat();
		                Log.d(LOG_TAG, "encoding Output format has changed to " + oformat);
		            } else if (encodingOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
		                Log.d(LOG_TAG, "encoding dequeueOutputBuffer timed out!");
		            }
		        }
		    }
	}
	
	  private boolean decodeInput(MediaExtractor extractor, MediaCodec decoder, ByteBuffer[] codecInputBuffers) {
	        boolean sawInputEOS = false;
	        int inputBufIndex = decoder.dequeueInputBuffer(0);
	        if (inputBufIndex >= 0) {
	            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
	            input1count++;

	            int sampleSize = extractor.readSampleData(dstBuf, 0);
	            long presentationTimeUs = 0;
	            if (sampleSize < 0) {
	                sawInputEOS = true;
	                sampleSize = 0;
	                Log.d(LOG_TAG, "done decoding input: #" + input1count);
	            } else {
	                presentationTimeUs = extractor.getSampleTime();
	            }

	            decoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
	            if (!sawInputEOS) {
	                extractor.advance();
	            }
	        }
	        return sawInputEOS;
	    }
	    private boolean decodeOutputToFile(MediaExtractor extractor, MediaCodec decoder, ByteBuffer[] codecOutputBuffers,
	            MediaCodec.BufferInfo info, int outputBufIndex, OutputStream output) throws IOException {
	        boolean sawOutputEOS = false;

	        ByteBuffer buf = codecOutputBuffers[outputBufIndex];
	        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
	            sawOutputEOS = true;
	            Log.d(LOG_TAG, "done decoding output: #" + output1count);
	        }

	        if (info.size > 0) {
	            output1count++;
	            byte[] outData = new byte[info.size];
	            buf.get(outData);
	            output.write(outData, 0, outData.length);
	        } else {
	            Log.d(LOG_TAG, "no data available " + info.size);
	        }
	        buf.clear();
	        decoder.releaseOutputBuffer(outputBufIndex, false);
	        return sawOutputEOS;
	    }

	    private boolean encodeInputFromFile(MediaCodec encoder, ByteBuffer[] encoderInputBuffers, MediaCodec.BufferInfo info, FileChannel channel) throws IOException {
	            boolean sawInputEOS = false;
	            int inputBufIndex = encoder.dequeueInputBuffer(0);
	            if (inputBufIndex >= 0) {
	                ByteBuffer dstBuf = encoderInputBuffers[inputBufIndex];
	                input1count++;

	                int sampleSize = channel.read(dstBuf);
	                if (sampleSize < 0) {
	                    sawInputEOS = true;
	                    sampleSize = 0;
	                    Log.d(LOG_TAG, "done encoding input: #" + input1count);
	                }

	                encoder.queueInputBuffer(inputBufIndex, 0, sampleSize, channel.position(), sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
	            }
	            return sawInputEOS;
	    }*/
	
	private void initViews() {
		mImageCaptionEditTxt=(EditText) findViewById(R.id.image_caption);
		mCancelImgBtn=(ImageView) findViewById(R.id.cancel);
		mPostImgBtn=(ImageView) findViewById(R.id.post);
		mPostImgView=(ResizableImageView) findViewById(R.id.image_post);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		btnLayout=(RelativeLayout) findViewById(R.id.bottom);
		gpsTracker=new GPSTracker(PostImageActivity.this);
		mFlag=getIntent().getBooleanExtra("flag", false);
		isShare=getIntent().getBooleanExtra("share", false);
		mGroupId=getIntent().getStringExtra(Constants.GROUP_ID);
		//mImageCaptionEditTxt.setAlpha((float) 0.5);
		
		
	}
	
	
	
	private void insertValues()
	{
		mPostImgBtn.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		mPostImgFile = new ParseFile("Image.png", imgByte);
		mPostImgFile.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
	             		final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
	             		userObject.put(Constants.GROUP_ID, mGroupId);
	            		/*if(userImage!=null)
	            		userObject.put(Constants.MEMBER_IMAGE, userImage);*/
	            		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	            		//userObject.put(Constants.MOBILE_NO, userMobileNo);
	            		//userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	             		userObject.put(Constants.POST_TEXT, "");
	             		userObject.put(Constants.POST_TYPE, "Image");
	             		userObject.put(Constants.POST_IMAGE, mPostImgFile);
	             		userObject.put(Constants.THUMBNAIL_PICTURE, mPostImageThumbnail);
	             		userObject.put(Constants.COMMENT_COUNT, 0);
	             		userObject.put(Constants.POST_POINT, 100);
	             		userObject.put(Constants.FLAG_COUNT, 0);
	             		userObject.put(Constants.IMAGE_CAPTION, mImageCaption);
	             		userObject.put(Constants.LIKE_ARRAY, list);
	             		userObject.put(Constants.DIS_LIKE_ARRAY, list);
	             		userObject.put(Constants.FLAG_ARRAY, list);
	             		userObject.put(Constants.FEED_LOCATION, point);
	             		if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
	        				userObject.put(Constants.POST_STATUS, "Pending");
	        			else
	        				userObject.put(Constants.POST_STATUS, "Active");
	             		userObject.put(Constants.IMAGE_HEIGHT, imgHeight);
	             		userObject.put(Constants.IMAGE_WIDTH, imgWidth);
	             		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
	             		userObject.put(Constants.HASH_TAG_ARRAY, list);
	             		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
	             		userObject.pinInBackground(mGroupId,new SaveCallback() {
	                        @Override
	                        public void done(ParseException e) {
	                            if(e == null) {
	                            	mPostImgBtn.setEnabled(true);
	                            	progressBar.setVisibility(View.GONE);
	                            	if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
            	                		Utility.showToastMessage(PostImageActivity.this, getResources().getString(R.string.post_approval_text));
	                            	if(isShare)
	                            	{
	                            		//Utility.showToastMessage(PostImageActivity.this, "Image posted successfully");
	                            		ShareInActivity.activity.finish();
	                            		finish();
	                            	}
	                            	else
	                            	{
	                            		if(GroupPostListActivity.mPostImgFile1!=null){
	                            			GroupPostListActivity.flag=true;
	                            			finish();
	                            		}
	                            		else{
	                            			TopicListActivity.flag=true;
	                            			finish();
	                            		}
	                            	}
	                            	
	                            }}});
	             		userObject.saveInBackground();
	                 }
	          }});
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							object.increment(Constants.BADGE_POINT, 100);
							object.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
						                 if (e == null) {
						                	
						                 }
							          }});
						}
					}
				}
			});
			
	}
	
	private void insertVideoInfo()
	{
		mPostImgBtn.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		//mPostImgFile = new ParseFile("Image.png", imgByte);
		System.out.println("before save thump");
		mPostImageThumbnail.saveInBackground(new SaveCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	 System.out.println("before save thump");
	                	ParseGeoPoint point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
	             		final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);
	             		userObject.put(Constants.GROUP_ID, mGroupId);
	            		/*if(userImage!=null)
	            		userObject.put(Constants.MEMBER_IMAGE, userImage);*/
	            		userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	            		//userObject.put(Constants.MOBILE_NO, userMobileNo);
	            		//userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
	             		userObject.put(Constants.POST_TEXT, "");
	             		if(inputType.equals("GIF"))
	             			userObject.put(Constants.POST_TYPE, "GIFVideo");
	             		else
	             			userObject.put(Constants.POST_TYPE, "SVideo");
	             		userObject.put(Constants.POST_IMAGE, mPostImageThumbnail);
	             		userObject.put(Constants.THUMBNAIL_PICTURE, mPostImageThumbnail);
	             		userObject.put(Constants.POST_VIDEO, mParseVideoFile);
	             		userObject.put(Constants.COMMENT_COUNT, 0);
	             		userObject.put(Constants.POST_POINT, 100);
	             		userObject.put(Constants.FLAG_COUNT, 0);
	             		userObject.put(Constants.IMAGE_CAPTION, mImageCaption);
	             		userObject.put(Constants.LIKE_ARRAY, list);
	             		userObject.put(Constants.DIS_LIKE_ARRAY, list);
	             		userObject.put(Constants.FLAG_ARRAY, list);
	             		userObject.put(Constants.FEED_LOCATION, point);
	             		if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
	        				userObject.put(Constants.POST_STATUS, "Pending");
	        			else
	        				userObject.put(Constants.POST_STATUS, "Active");
	             		userObject.put(Constants.IMAGE_HEIGHT, imgHeight);
	             		userObject.put(Constants.IMAGE_WIDTH, imgWidth);
	             		userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
	             		userObject.put(Constants.HASH_TAG_ARRAY, list);
	             		userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
	             		/*,new SaveCallback() {
	                        @Override
	                        public void done(ParseException e) {
	                            if(e == null) {
	                            	mPostImgBtn.setEnabled(true);
	                            	progressBar.setVisibility(View.GONE);
	                            	if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
            	                		Utility.showToastMessage(PostImageActivity.this, getResources().getString(R.string.post_approval_text));
	                            		GroupPostListActivity.flag=true;
		                            	finish();
	                            }}});*/
	             		userObject.saveInBackground(new SaveCallback() {
							
							@Override
							public void done(ParseException arg0) {
								userObject.pinInBackground(mGroupId);
								mPostImgBtn.setEnabled(true);
                            	progressBar.setVisibility(View.GONE);
                            	if(Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
        	                		Utility.showToastMessage(PostImageActivity.this, getResources().getString(R.string.post_approval_text));
                            	if(isShare)
                            	{
                            		//Utility.showToastMessage(PostImageActivity.this, "Image posted successfully");
                            		ShareInActivity.activity.finish();
                            		finish();
                            	}
                            	else
                            	{
                            		if(GroupPostListActivity.mPostImgFile1!=null){
                            			GroupPostListActivity.flag=true;
                            			finish();
                            		}
                            		else{
                            			TopicListActivity.flag=true;
                            			finish();
                            		}
                            	}
                            	
							}
						});
	                 }
	          }});
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
						if(object!=null)
						{
							object.increment(Constants.BADGE_POINT, 100);
							object.saveInBackground(new SaveCallback() {
							          public void done(ParseException e) {
						                 if (e == null) {
						                	
						                 }
							          }});
						}
					}
				}
			});
			
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		
        if (resultCode == RESULT_OK
				&& (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
			if (imageChooserManager == null) {
				reinitializeImageChooser();
			}
			imageChooserManager.submit(requestCode, data);
		}
        else if (resultCode == RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_VIDEO ))
        {
        	  if (videoChooserManager == null) {
                  reinitializeVideoChooser();
              }
              videoChooserManager.submit(requestCode, data);
        }
        else
        {
        	finish();
        }
	}
	
	private void chooseImage() {
		chooserType = ChooserType.REQUEST_PICK_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_PICK_PICTURE, "ChatteratiCamera", true);
		imageChooserManager.setImageChooserListener(this);
		try {
			//pbar.setVisibility(View.VISIBLE);
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	private void takePicture() {
		System.out.println("inside takePicture");
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_CAPTURE_PICTURE, "ChatteratiCamera", true);
		imageChooserManager.setImageChooserListener(this);
		try {
			//pbar.setVisibility(View.VISIBLE);
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void onImageChosen(final ChosenImage image) {
		System.out.println("inside onImageChosen");
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (image != null) {
					generateThumbnail(image.getFilePathOriginal());
					//progressBar.setVisibility(View.VISIBLE);
					System.out.println("image path in before compress :"+image.getFilePathOriginal());
					Bitmap bitmap=Utility.compressImage(image.getFilePathOriginal());
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
		             imgByte = stream.toByteArray();
		            imgWidth=bitmap.getWidth();
					imgHeight=bitmap.getHeight();
					/*mPostImgFile = new ParseFile("Image.png", img);
					mPostImgFile.saveInBackground(new SaveCallback() {
				          public void done(ParseException e) {
				                 if (e == null) {
				                	// progressBar.setVisibility(View.GONE);
				                	 //btnLayout.setVisibility(View.VISIBLE);
				                 }
				          }});
*/
					mPostImgView.setImageBitmap(bitmap);
				}
			}
		});
	}
	
	 private void generateThumbnail(String picturePath)
	    {
	    	Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,200,200);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mPostImageThumbnail = new ParseFile("ThumbImage.png", img);
			mPostImageThumbnail.saveInBackground();
	    }
	 
	@Override
	public void onError(final String reason) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//pbar.setVisibility(View.GONE);
				Toast.makeText(PostImageActivity.this, reason,
						Toast.LENGTH_LONG).show();
				finish();
			}
		});
	}

	// Should be called if for some reason the ImageChooserManager is null (Due
	// to destroying of activity for low memory situations)
	private void reinitializeImageChooser() {
		imageChooserManager = new ImageChooserManager(this, chooserType,
				"ChatteratiCamera", true);
		imageChooserManager.setImageChooserListener(this);
		imageChooserManager.reinitialize(filePath);
	}
	
	 private void reinitializeVideoChooser() {
	        videoChooserManager = new VideoChooserManager(this, chooserType, true);
	        videoChooserManager.setVideoChooserListener(this);
	        videoChooserManager.reinitialize(filePath);
	    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("chooser_type", chooserType);
		outState.putString("media_path", filePath);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("chooser_type")) {
				chooserType = savedInstanceState.getInt("chooser_type");
			}

			if (savedInstanceState.containsKey("media_path")) {
				filePath = savedInstanceState.getString("media_path");
			}
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public void onBackPressed() {
	}
	
	 public void pickVideo() {
	        chooserType = ChooserType.REQUEST_PICK_VIDEO;
	        videoChooserManager = new VideoChooserManager(this,
	                ChooserType.REQUEST_PICK_VIDEO);
	        videoChooserManager.setVideoChooserListener(this);
	        try {
	        	System.out.println("before choose video");
	        	filePath=videoChooserManager.choose();
	        	System.out.println("file path after choose"+filePath);
	           // pbar.setVisibility(View.VISIBLE);
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	 
	 @Override
	    public void onVideoChosen(final ChosenVideo video) {
		 mPostImgBtn.setEnabled(false);
	        runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	              //  pbar.setVisibility(View.GONE);
	            	
	                if (video != null) {
	                	//new CompressAsyntask(video.getVideoFilePath().toString()).execute();
	                	int fileSizeInBytes=(int) new File(video.getVideoFilePath().toString()).length();
	                	long fileSizeInKB = fileSizeInBytes / 1024;
	                	// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
	                	long fileSizeInMB = fileSizeInKB / 1024;
	                	float remainder=fileSizeInKB%1024;
	                	System.out.println("file size mb ::: remain :: "+fileSizeInMB+"  "+remainder);
	                	System.out.println("file size ::: "+fileSizeInMB);
	                	if (fileSizeInMB <= 9.5) {
	                	
	                	System.out.println("video.getThumbnailPath()::: "+video.getThumbnailPath());
	                	generateVideoThumbnail(video.getVideoPreviewImage(),video.getMediaWidth(),video.getMediaHeight(),false);
	                	try
	                	{
	                	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                     FileInputStream fis = new FileInputStream(new File(video.getVideoFilePath().toString()));

	                     byte[] buf = new byte[1024];
	                     int n;
	                     while (-1 != (n = fis.read(buf)))
	                         baos.write(buf, 0, n);

	                     byte[] videoBytes = baos.toByteArray();
	                     
	                     mParseVideoFile = new ParseFile("Video.mp4", videoBytes);
	                     mParseVideoFile.saveInBackground(new SaveCallback() {
	     			          public void done(ParseException e) {
	     			                 if (e == null) {
	     			                	 
	     			                 }
	     			          }});
	                	}
	                	catch (Exception e) {
							// TODO: handle exception
						}
	                	// imgWidth=Integer.parseInt(video.getMediaWidth());
	 					//imgHeight=Integer.parseInt(video.getMediaHeight());
	 					System.out.println("video path before compress::: "+video.getVideoFilePath().toString());
	 						 				    

	 					
	                   /* videoView.setVideoURI(Uri.parse(new File(video
	                            .getVideoFilePath()).toString()));
	                    videoView.start();
	                    imageViewThumb.setImageURI(Uri.parse(new File(video
	                            .getThumbnailPath()).toString()));
	                    imageViewThumbSmall.setImageURI(Uri.parse(new File(video
	                            .getThumbnailSmallPath()).toString()));*/
	                	 }
		    	        else
		    	        {
		    	        	finish();
		    	        	Toast.makeText(PostImageActivity.this, "File size is too large. Limit is 10MB", Toast.LENGTH_LONG).show();
		    	        }
	                }
	              
	            }
	        });
	       
	    }
	 
	 public class CompressAsyntask extends AsyncTask<Void, Void, Void>
	 {
		String path; 
		 CompressAsyntask(String path1)
		 {
			 this.path=path1;
		 }
		@Override
		protected Void doInBackground(Void... params) {
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/Videos/Compressed");
			    if (!file.exists()) {
			        file.mkdirs();
			    }
			    File file1 = new File(file.getAbsolutePath() + "/" + "my_video" + ".mp4");
			   File videoPath = new File(Environment.getExternalStorageDirectory().getPath(), "Chatterati/Videos/VID_20151119_131446_rotated_to_180.mp4");
			   /* if(file1.exists()){
					file1.delete();
				}*/
			    String uriSting = (file.getAbsolutePath() + "/" + "my_vid" + ".mp4");
			 String ffmepgPath= "android.resource://" + getPackageName() + File.separator + "raw"+File.separator+"ffmpeg";
			 //String workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "ffmpeg";
			    //String ffmepgPath = getCacheDir().getAbsolutePath() + File.separator + "ffmpeg";
			    String command = ffmepgPath + " -y -i " +path+ " -c:v libx264 -crf 19 -preset slow -c:a aac -strict experimental -b:a 192k -ac 2 -vf transpose=2 " + uriSting;
			    try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				System.out.println("inside catch run command"+e);
			}

			return null;
		}
		 
	 }
	 
	 private void generateVideoThumbnail(String picturePath,String width,String height,boolean isShare)
	    {
	    	Bitmap bitmap=Utility.decodeSampledBitmapFromResource(picturePath,Integer.parseInt(width),Integer.parseInt(height));
	    	mPostImgView.setImageBitmap(bitmap);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] img = stream.toByteArray();
			mPostImageThumbnail = new ParseFile("Image.png", img);
			//mPostImageThumbnail.saveInBackground();
			mPostImgBtn.setEnabled(true);
			//if(isShare)
			//{
				imgWidth=bitmap.getWidth();
				imgHeight=bitmap.getHeight();
				System.out.println("video width ::: height  "+imgWidth+"  "+imgHeight);
			//}
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