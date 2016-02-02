package com.group.nearme.services;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.group.nearme.NearByGroupListActivity;
import com.group.nearme.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class GPSTracker extends Service implements LocationListener {
	private final Context mContext;

	// flag for GPS Status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	boolean canGetLocation = false;

	Location location;
	double latitude;
	double longitude;

	// The minimum distance to change updates in metters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10
																	// metters

	// The minimum time beetwen updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}
	public boolean flag;

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				
					Log.d("Network", "Network");

					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(location.getAccuracy() >5000)
						{
							flag=false;
						}
						else
						{
							flag=true;
							updateGPSCoordinates();
						}
					}
				}

				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

						Log.d("GPS Enabled", "GPS Enabled");

						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							System.out.println("location accuracy ::: "+location.getAccuracy());
							if(location.getAccuracy() >5000)
							{
								flag=false;
							}
							else
							{
								flag=true;
								updateGPSCoordinates();
							}
							
						}
					}
				}
		}
		} catch (Exception e) {
			// e.printStackTrace();
			flag=false;
			Log.e("Error : Location",
					"Impossible to connect to LocationManager", e);
		}

		return location;
	}

	public void updateGPSCoordinates() {
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 */

	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude
	 */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		return latitude;
	}

	/**
	 * Function to get longitude
	 */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog
	 */
	public void showSettingsAlert(final Activity mActivity) {
		/*AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS Settings");

		// Setting Dialog Message
		alertDialog.setMessage("Please enable GPS");

		// On Pressing Setting button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});

		// On pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_HOME);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						mActivity.startActivity(intent);
						mActivity.finish();
					}
				});

		alertDialog.show();
*/		
		final Dialog mDialog = new Dialog(mActivity);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.two_btn_dialog);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		Button yes=(Button) mDialog.findViewById(R.id.yes);
		Button no=(Button) mDialog.findViewById(R.id.no);
		TextView message=(TextView) mDialog.findViewById(R.id.msg);
		
		message.setText("Please Enable Location Service");
		yes.setText("Settings");
		no.setText("Cancel");
		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		
		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mActivity.startActivity(intent);
				mActivity.finish();
			}
		});
		mDialog.show();


	}

	/**
	 * Get list of address by latitude and longitude
	 * 
	 * @return null or List<Address>
	 */
	public List<Address> getGeocoderAddress(Context context) {
		if (location != null) {
			Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
			try {
				List<Address> addresses = geocoder.getFromLocation(latitude,
						longitude, 1);
				return addresses;
			} catch (IOException e) {
				// e.printStackTrace();
				Log.e("Error : Geocoder", "Impossible to connect to Geocoder",
						e);
			}
		}

		return null;
	}

	/**
	 * Try to get AddressLine
	 * 
	 * @return null or addressLine
	 */
	public String getAddressLine(Context context) {
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String addressLine = address.getAddressLine(0);

			return addressLine;
		} else {
			return null;
		}
	}
	
	public String getState(Context context) {
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String addressLine = address.getAdminArea();

			return addressLine;
		} else {
			return null;
		}
	}
	
	public String getCountryCode(Context context) {
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String code = address.getCountryCode();

			return code;
		} else {
			return "IN";
		}
	}


	/**
	 * Try to get Locality
	 * 
	 * @return null or locality
	 */
	public String getLocality(Context context) {
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String locality = address.getLocality();

			return locality;
		} else {
			return null;
		}
	}

	/**
	 * Try to get Postal Code
	 * 
	 * @return null or postalCode
	 */
	public String getPostalCode(Context context) {
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String postalCode = address.getPostalCode();

			return postalCode;
		} else {
			return null;
		}
	}

	/**
	 * Try to get CountryName
	 * 
	 * @return null or postalCode
	 */
	public String getCountryName(Context context) {
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			String countryName = address.getCountryName();

			return countryName;
		} else {
			return null;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		//location.get
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}