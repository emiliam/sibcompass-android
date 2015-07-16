package fi.yle.sibkompassi;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

/**
 * Calculations for compass heading by William J. Francis August 8, 2014
 *
 */
public class MainActivity extends ActionBarActivity implements
		SensorEventListener {
	public final static String EXTRA_SONG = "fi.yle.sibkompassi.SONG";
	ImageButton imgButton;
	private SensorManager sensorManager;
	private TextView heading;
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

	public void playSong(View view) {
		Intent intent = new Intent(this, PlaySongActivity.class);

		TextView songNr = (TextView) findViewById(R.id.symphony_nr);
		String song = songNr.getText().toString();
		intent.putExtra(EXTRA_SONG, song);
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
		// initialize your android device sensor capabilities
		heading = (TextView) findViewById(R.id.heading);
		// initialize device sensor to detect changes in heading
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		compass = (ImageView) findViewById(R.id.compass);
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
			int displayDegree = Math.round(degree);
			heading.setText(Integer.toString(displayDegree) + " °");

			RotateAnimation animation = new RotateAnimation(currentDegree,
					-degree, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(120);
			animation.setFillAfter(true);
			compass.startAnimation(animation);
			currentDegree = -degree;
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
	}

	@Override
	public void onPause() {
		super.onPause();
		// to save battery
		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);
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

}
