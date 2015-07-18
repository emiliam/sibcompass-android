package fi.yle.sibkompassi;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
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
 * Calculations for compass heading by William J. Francis, August 8, 2014
 * Location based code is partly written by Ravi Tamada, February 3, 2015
 */
public class MainActivity extends ActionBarActivity implements
		SensorEventListener, ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener {
	private static final String TAG = "MainActivity";
	public final static String EXTRA_SONG = "fi.yle.sibkompassi.SONG";
	ImageButton imgButton;
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
	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];
	private TextView songNr;
	private String currentSongNr;
	private float angleBetweenAinolaAndCurrentLocation;
	private ImageView ainola;
	private GoogleApiClient mGoogleApiClient;
	private double userlatitude = 0;
	private double userlongitude = 0;
	private Location mLastLocation;
	// boolean flag to toggle periodic location updates
	private boolean mRequestingLocationUpdates = false;
	private LocationRequest mLocationRequest;

	public void playSong(View view) {
		Intent intent = new Intent(this, PlaySongActivity.class);
		intent.putExtra(EXTRA_SONG, getSongNr());
		// Verify that the intent will resolve to an activity
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideStatusBar();
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
		// Toggling the periodic location updates

		ainola.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				togglePeriodicLocationUpdates();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
			SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
					mLastMagnetometer);
			SensorManager.getOrientation(mR, mOrientation);
			float azimuthInRadians = mOrientation[0];
			float degree = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

			// use displayDegree for displaying integer value of heading
			int displayHeading = Math.round(degree);
			heading.setText(Integer.toString(displayHeading) + " °");

			RotateAnimation animation = new RotateAnimation(currentDegree,
					-degree, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(120);
			animation.setFillAfter(true);
			compass.startAnimation(animation);
			currentDegree = -degree;
			currentSongNr = Integer.toString(calculateCompassSector(degree));
			updateSongNumber();
			angleBetweenAinolaAndCurrentLocation = calculateAngleToAinola();
			animateAinolaNeedle();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// to save battery
		// Register this class as a listener for the accelerometer sensor
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		// ...and the orientation sensor
		sensorManager.registerListener(this, magnetometer,
				SensorManager.SENSOR_DELAY_GAME);

		checkPlayServices();

		// Resuming the periodic location updates
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
			startLocationUpdates();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// to save battery
		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);

		stopLocationUpdates();
	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	protected void stopLocationUpdates() {
		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(
					mGoogleApiClient, this);
		}
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setSmallestDisplacement(100); // 100 meters
	}

	private void togglePeriodicLocationUpdates() {
		if (!mRequestingLocationUpdates) {
			mRequestingLocationUpdates = true;
			startLocationUpdates();
		} else {
			mRequestingLocationUpdates = false;
			stopLocationUpdates();
		}
	}

	private void hideStatusBar() {
		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		// Remember that you should never show the action bar if the
		// status bar is hidden, so hide that too if necessary.
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
	}

	private void updateSongNumber() {
		String songNr = getSongNr();
		if (!currentSongNr.equals(songNr)) {
			symphonyNr.setText(songNr);
			currentSongNr = songNr;
		}
	}

	private String getSongNr() {
		songNr = (TextView) findViewById(R.id.symphony_nr);
		return songNr != null ? songNr.getText().toString() : "1";
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

	private float calculateAngleToAinola() {
		double latitude = userlatitude * Math.PI / 180.0;
		double longitude = userlongitude * Math.PI / 180.0;
		double ainolaLatitude = 60.458295 * Math.PI / 180.0;
		double ainolaLongitude = 25.087905 * Math.PI / 180.0;

		double diffLongitude = ainolaLongitude - longitude;
		double y = Math.sin(diffLongitude) * Math.cos(ainolaLongitude);
		double x = Math.cos(latitude) * Math.sin(ainolaLatitude)
				- Math.sin(latitude) * Math.cos(ainolaLatitude)
				* Math.cos(diffLongitude);
		double radians = Math.atan2(y, x);
		if (radians < 0.0) {
			radians += 2 * Math.PI;
		}
		return (float) radians;
	}

	private void animateAinolaNeedle() {
		float direction = currentDegree;
		if (direction > 180) {
			direction = 360 - direction;
		} else {
			direction = 0 - direction;
		}

		float degree = (float) (direction * Math.PI / 180.0)
				+ angleBetweenAinolaAndCurrentLocation;

		RotateAnimation animation = new RotateAnimation(currentDegree, -degree,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animation.setDuration(60);
		animation.setFillAfter(true);
		ainola.startAnimation(animation);
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
		ainola.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Assign the new location
		mLastLocation = location;
		// Displaying the new location on UI
		getLocation();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// Once connected with google api, get the location
		getLocation();
		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
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

	private void getLocation() {
		if (mGoogleApiClient != null) {
			mLastLocation = LocationServices.FusedLocationApi
					.getLastLocation(mGoogleApiClient);
			if (mLastLocation != null) {
				userlatitude = mLastLocation.getLatitude();
				userlongitude = mLastLocation.getLongitude();
			}
		}
	}
}
