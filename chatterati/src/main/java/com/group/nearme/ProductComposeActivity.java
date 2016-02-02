package com.group.nearme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.beanie.imagechooser.api.ImageChooserListener;
import com.beanie.imagechooser.api.ImageChooserManager;
import com.beanie.imagechooser.api.VideoChooserManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.image.Crop;
import com.group.nearme.services.GPSTracker;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by senthilc on 28/01/2016.
 */
public class ProductComposeActivity extends Activity implements ImageChooserListener
{
    EditText mProductNameEditTxt,mProductDesEditTxt,mProductActualPriceEditTxt;
    EditText mProductOfferPriceEditTxt,mProductOfferPercentageEditTxt;
    ImageView mGalleryImgView,mCameraImageView,mProductImgView,mSendImgView,mCloseImgView;
    RelativeLayout mUploadImgLayout,mProductImageLayout;
    private ProgressBar progressBar;
    private GPSTracker gpsTracker;
    public ParseFile mProductImageFile=null,mProductTumbnailImageFile=null;
    private ArrayList<String> list=new ArrayList<String>();
    private int imgWidth,imgHeight;
    private ImageChooserManager imageChooserManager;
    private String filePath;
    private int chooserType;
    byte[] imgByte;

    float mProductActualPrice,mProductOffer,mProductOfferPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.product_compose_view);
        Utility.getTracker(this, "PRODUCT COMPOSE SCREEN");
        initViews();
    }

    private void initViews() {
        mProductNameEditTxt=(EditText) findViewById(R.id.product_name_box);
        mProductDesEditTxt=(EditText) findViewById(R.id.product_des_box);
        mProductActualPriceEditTxt=(EditText) findViewById(R.id.product_price_box);
        mProductOfferPriceEditTxt=(EditText) findViewById(R.id.product_offer_price_box);
        mProductOfferPercentageEditTxt=(EditText) findViewById(R.id.product_offer_percentage_box);
        mProductOfferPriceEditTxt.setKeyListener(null);

        mGalleryImgView=(ImageView) findViewById(R.id.gallery_image);
        mCameraImageView=(ImageView) findViewById(R.id.camera_image);
        mProductImgView=(ImageView) findViewById(R.id.product_image);
        mSendImgView=(ImageView) findViewById(R.id.send_image);
        mCloseImgView=(ImageView) findViewById(R.id.close_image);

        mUploadImgLayout=(RelativeLayout) findViewById(R.id.upload_layout);
        mProductImageLayout=(RelativeLayout) findViewById(R.id.product_image_layout);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);

        gpsTracker=new GPSTracker(this);

        mGalleryImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        mCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        mSendImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertProductDetails();
            }
        });

        mCloseImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  mUploadImgLayout.setVisibility(View.VISIBLE);
                mProductImageLayout.setVisibility(View.GONE);
            }
        });


        mProductOfferPercentageEditTxt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                try {
                    float actualPrice = Float.parseFloat(mProductActualPriceEditTxt.getText().toString());
                    if (String.valueOf(cs).isEmpty())
                        mProductOfferPriceEditTxt.setText(String.valueOf(actualPrice));
                    else {
                        float offer = Float.parseFloat(String.valueOf(cs));
                        if (offer <= 100) {
                            if (actualPrice > 0) {
                                float reduc = (actualPrice * offer) / 100;
                                String offerPrice = String.valueOf(actualPrice - reduc);
                                mProductOfferPriceEditTxt.setText(offerPrice);
                            }
                        } else {
                            mProductOfferPercentageEditTxt.setText(String.valueOf(0));
                            mProductOfferPriceEditTxt.setText(String.valueOf(actualPrice));
                            Toast.makeText(ProductComposeActivity.this, "Invalid offer percentage", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {

                }

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });


    }

    private void insertProductDetails() {
        String pName = mProductNameEditTxt.getText().toString();
        String pDes = mProductDesEditTxt.getText().toString();
        String pPrice = mProductActualPriceEditTxt.getText().toString();
        String pOffer=mProductOfferPercentageEditTxt.getText().toString();
        String pOfferPrice=mProductOfferPriceEditTxt.getText().toString();

        if(pOffer.isEmpty()) {
            pOffer = String.valueOf(0);
            pOfferPrice=pPrice;
        }
        if(pOfferPrice.isEmpty()) {
            pOfferPrice=pPrice;
        }

        String mGroupId = Utility.getGroupObject().getObjectId();
        if (pName.isEmpty()) {
            Utility.showToastMessage(ProductComposeActivity.this, getResources().getString(R.string.product_name_empty));
        } else if (pDes.isEmpty()) {
            Utility.showToastMessage(ProductComposeActivity.this, getResources().getString(R.string.product_des_empty));
        } else if (pPrice.isEmpty()) {
            Utility.showToastMessage(ProductComposeActivity.this, getResources().getString(R.string.product_price_empty));
        } else if (mProductTumbnailImageFile == null) {
            Utility.showToastMessage(ProductComposeActivity.this, getResources().getString(R.string.product_image_empty));
        } else {
            mSendImgView.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            HashMap<String,String> postAttribute=new HashMap<String,String>();
            postAttribute.put(Constants.PRODUCT_ACTUAL_PRICE,pPrice);
            postAttribute.put(Constants.PRODUCT_OFFER_PERCENTAGE,pOffer);
            postAttribute.put(Constants.PRODUCT_OFFER_PRICE,pOfferPrice);
            ParseGeoPoint point=null;
            try{
                point = new ParseGeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            }
            catch (Exception e){
                point = new ParseGeoPoint(0.0, 0.0);
            }
            final ParseObject userObject = new ParseObject(Constants.GROUP_FEED_TABLE);

            userObject.put(Constants.GROUP_ID, mGroupId);
            userObject.put(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
            userObject.put(Constants.POST_TYPE, Constants.PRODUCT_POST);
            if (mProductImageFile != null)
                userObject.put(Constants.POST_IMAGE, mProductImageFile);
            else
                userObject.put(Constants.POST_IMAGE, mProductTumbnailImageFile);
            userObject.put(Constants.THUMBNAIL_PICTURE, mProductTumbnailImageFile);
            userObject.put(Constants.IMAGE_CAPTION, pName);
            userObject.put(Constants.ABSTRACT, pDes);
            userObject.put(Constants.POST_ATTRIBUTE, postAttribute);

            userObject.put(Constants.POST_TEXT, "");
            userObject.put(Constants.COMMENT_COUNT, 0);
            userObject.put(Constants.POST_POINT, 100);
            userObject.put(Constants.FLAG_COUNT, 0);
            userObject.put(Constants.LIKE_ARRAY, list);
            userObject.put(Constants.DIS_LIKE_ARRAY, list);
            userObject.put(Constants.FLAG_ARRAY, list);
            userObject.put(Constants.FEED_LOCATION, point);
            if (Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
                userObject.put(Constants.POST_STATUS, "Pending");
            else
                userObject.put(Constants.POST_STATUS, "Active");
            userObject.put(Constants.IMAGE_HEIGHT, imgHeight);
            userObject.put(Constants.IMAGE_WIDTH, imgWidth);
            userObject.put(Constants.FEED_UPDATED_TIME, Utility.getCurrentUTCDate());
            userObject.put(Constants.HASH_TAG_ARRAY, list);
            userObject.put(Constants.USER_ID, ParseObject.createWithoutData(Constants.USER_TABLE, PreferenceSettings.getUserID()));
            userObject.pinInBackground(mGroupId, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        mSendImgView.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        if (Utility.getGroupObject().getBoolean(Constants.POST_APPROVAL) && !Utility.getGroupObject().getList(Constants.ADMIN_ARRAY).contains(PreferenceSettings.getMobileNo()))
                            Utility.showToastMessage(ProductComposeActivity.this, getResources().getString(R.string.post_approval_text));
                        TopicListActivity.flag = true;
                        finish();
                       // Utility.showToastMessage(ProductComposeActivity.this, "Posted successfully");
                    }
                }
            });
            userObject.saveInBackground();


            ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
            query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        if (object != null) {
                            object.increment(Constants.BADGE_POINT, 100);
                            object.saveInBackground();
                        }
                    }
                }
            });

        }

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
                    System.out.println("image path in before compress :" + image.getFilePathOriginal());
                    Bitmap bitmap = Utility.compressImage(image.getFilePathOriginal());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    imgByte = stream.toByteArray();
                    imgWidth = bitmap.getWidth();
                    imgHeight = bitmap.getHeight();
                    mProductImageFile = new ParseFile("Image.png", imgByte);
                    mProductImageFile.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // progressBar.setVisibility(View.GONE);
                                //btnLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    mProductImageLayout.setVisibility(View.VISIBLE);
                    mUploadImgLayout.setVisibility(View.GONE);
                    mProductImgView.setImageBitmap(bitmap);

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
        mProductTumbnailImageFile = new ParseFile("ThumbImage.png", img);
        mProductTumbnailImageFile.saveInBackground();
    }

    @Override
    public void onError(final String reason) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //pbar.setVisibility(View.GONE);
                Toast.makeText(ProductComposeActivity.this, reason,
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
