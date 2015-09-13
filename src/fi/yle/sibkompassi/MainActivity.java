package fi.yle.sibkompassi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * A compass which finds your way to Ainola, home of Siblius in the honor of his
 * 150 year birthday.
 * 
 * Note! The compass points to magnetic north, not true north.
 * 
 * Calculations for compass heading by
 * http://stackoverflow.com/users/4535635/orka.
 */
public class MainActivity extends Activity implements SensorEventListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	private static final String TAG = "MainActivity";
	public final static String EXTRA_SONG = "fi.yle.sibkompassi.SONG";

	private SensorManager sensorManager;
	private TextView heading;
	private TextView symphonyNr;
	private ImageView compass;
	private float currentDegree = 0f;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private boolean mLastAccelerometerSet = false;
	private boolean mLastMagnetometerSet = false;
	private float[] temporaryRotationMatrix = new float[9];
	private float[] rotationMatrix = new float[9];
	private float[] orientationData = new float[3];
	private ImageView ainola;
	private GoogleApiClient mGoogleApiClient;
	
	private Location mLastLocation;
	private LocationRequest mLocationRequest;
	float lastAinolaDegree = 0;

	public void playSong(View view) {
		Intent intent = new Intent(this, PlaySongActivity.class);
		intent.putExtra(EXTRA_SONG, getSongNr());
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// initialize device sensor to detect changes in heading
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		heading = (TextView) findViewById(R.id.heading);
		compass = (ImageView) findViewById(R.id.compass);
		ainola = (ImageView) findViewById(R.id.ainola);
		symphonyNr = (TextView) findViewById(R.id.symphony_nr);

		if (checkPlayServices()) {
			buildGoogleApiClient();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		isLocationServiceEnabled();

		sensorManager.registerListener(this, accelerometer,
				60000);
		sensorManager.registerListener(this, magnetometer,
				60000);

		checkPlayServices();

		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			startLocationUpdates();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);

		stopLocationUpdates();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// no need
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor == accelerometer) {
			System.arraycopy(event.values, 0, mLastAccelerometer, 0,
					event.values.length);
			mLastAccelerometerSet = true;
		} else if (event.sensor == magnetometer) {
			System.arraycopy(event.values, 0, mLastMagnetometer, 0,
					event.values.length);
			mLastMagnetometerSet = true;
		}
		if (mLastAccelerometerSet && mLastMagnetometerSet) {

			SensorManager.getRotationMatrix(temporaryRotationMatrix, null,
					mLastAccelerometer, mLastMagnetometer);

			int screenRotation = getWindowManager().getDefaultDisplay()
					.getRotation();
			int axisX, axisY;
			boolean isUpSideDown = mLastAccelerometer[2] < 0;
			switch (screenRotation) {

			case Surface.ROTATION_0:
				axisX = (isUpSideDown ? SensorManager.AXIS_MINUS_X
						: SensorManager.AXIS_X);
				axisY = (Math.abs(mLastAccelerometer[1]) > 6.0f ? (isUpSideDown ? SensorManager.AXIS_MINUS_Z
						: SensorManager.AXIS_Z)
						: (isUpSideDown ? SensorManager.AXIS_MINUS_Y
								: SensorManager.AXIS_Y));
				break;
			case Surface.ROTATION_90:
				axisX = (isUpSideDown ? SensorManager.AXIS_MINUS_Y
						: SensorManager.AXIS_Y);
				axisY = (Math.abs(mLastAccelerometer[0]) > 6.0f ? (isUpSideDown ? SensorManager.AXIS_Z
						: SensorManager.AXIS_MINUS_Z)
						: (isUpSideDown ? SensorManager.AXIS_X
								: SensorManager.AXIS_MINUS_X));
				break;
			case Surface.ROTATION_180:
				axisX = (isUpSideDown ? SensorManager.AXIS_X
						: SensorManager.AXIS_MINUS_X);
				axisY = (Math.abs(mLastAccelerometer[1]) > 6.0f ? (isUpSideDown ? SensorManager.AXIS_Z
						: SensorManager.AXIS_MINUS_Z)
						: (isUpSideDown ? SensorManager.AXIS_Y
								: SensorManager.AXIS_MINUS_Y));
				break;
			case Surface.ROTATION_270:
				axisX = (isUpSideDown ? SensorManager.AXIS_Y
						: SensorManager.AXIS_MINUS_Y);
				axisY = (Math.abs(mLastAccelerometer[0]) > 6.0f ? (isUpSideDown ? SensorManager.AXIS_MINUS_Z
						: SensorManager.AXIS_Z)
						: (isUpSideDown ? SensorManager.AXIS_MINUS_X
								: SensorManager.AXIS_X));
				break;
			default:
				axisX = (isUpSideDown ? SensorManager.AXIS_MINUS_X
						: SensorManager.AXIS_X);
				axisY = (isUpSideDown ? SensorManager.AXIS_MINUS_Y
						: SensorManager.AXIS_Y);
			}

			SensorManager.remapCoordinateSystem(temporaryRotationMatrix, axisX,
					axisY, rotationMatrix);
			
			SensorManager.getOrientation(rotationMatrix, orientationData);
			float azimuthInRadians = orientationData[0];

			float degree = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

			int displayHeading = Math.round(degree);
			heading.setText(Integer.toString(displayHeading) + " °");

			RotateAnimation animation = new RotateAnimation(currentDegree,
					-degree, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(210);
			animation.setFillAfter(true);
			compass.startAnimation(animation);
			currentDegree = -degree;
			updateSongNumber(displayHeading);
			animateAinolaNeedle();
		}

	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(60000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

	}

	private void updateSongNumber(float currentDegree) {
		String oldSongNr = getSongNr();
		String currentSongNr = Integer
				.toString(calculateCompassSector(currentDegree));
		if (!currentSongNr.equals(oldSongNr)) {
			symphonyNr.setText(currentSongNr);
			currentSongNr = oldSongNr;
		}
	}

	private String getSongNr() {
		return symphonyNr != null ? symphonyNr.getText().toString() : "1";
	}

	private int calculateCompassSector(float heading) {
		float sectorMin = 0;
		float sectorMax = 23;
		if (heading > 337 && heading < sectorMax) {
			return 1;
		}
		sectorMin = sectorMax;
		sectorMax = sectorMin + 45;
		for (int sector = 2; sector <= 8; sector++) {
			if (sectorMax < 360 && heading > sectorMin && heading < sectorMax) {
				return sector;
			}
			sectorMin = sectorMax;
			sectorMax = sectorMin + 45;
		}
		return 1;
	}

	private void animateAinolaNeedle() {		
		Location ainolaLocation = new Location("Ainola") ;
		ainolaLocation.setLatitude(60.458295);
		ainolaLocation.setLongitude(25.087905);

		float direction = (float) (-currentDegree * Math.PI / 180.0) 
				+ mLastLocation.bearingTo(ainolaLocation);	
		if (direction > 180) {
			direction = 360 - direction;
		} else {
			direction = 0 - direction;
		}
		
		RotateAnimation animation = new RotateAnimation(currentDegree, -direction,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animation.setDuration(20000);
		animation.setFillAfter(true);
		ainola.startAnimation(animation);
		lastAinolaDegree = direction;
	
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Could not connect to Location service.");
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null)
			mLastLocation = location;
	}

	@Override
	public void onConnected(Bundle arg0) {
		Location locationChanged = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		 if (locationChanged != null)
			 mLastLocation = locationChanged;
	}

	/**
	 * Method to verify google play services on the device
	 * */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000)
						.show();
			} else {
				Toast.makeText(getApplicationContext(), "Laite ei tuettu.",
						Toast.LENGTH_LONG).show();
				finish();
			}
			return false;
		}
		return true;
	}

	public boolean isLocationServiceEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (mLastLocation == null)
			mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || 
				locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			ainola.setVisibility(View.VISIBLE);
			return true;
		} else {
			ainola.setVisibility(View.INVISIBLE);
			return false;
		}
	}
}
