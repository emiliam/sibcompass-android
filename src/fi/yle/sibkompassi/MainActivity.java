package fi.yle.sibkompassi;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    public final static String EXTRA_SONG = "fi.yle.sibkompassi.SONG";
    ImageButton imgButton;
    
	public void playSong(View view) {
		Intent intent = new Intent(this, PlaySongActivity.class);
		  TextView songNr = (TextView) findViewById(R.id.symphony_nr);
		  String song = songNr.getText().toString();
		  intent.putExtra(EXTRA_SONG, song);
		// Verify that the intent will resolve to an activity
		  if (intent.resolveActivity(getPackageManager()) != null) {
		      startActivity(intent);
		      Log.i("MainActivity", "MainActivity — intent started");
		  }
		  Log.i("MainActivity", "MainActivity — get song nr " + song);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		// Remember that you should never show the action bar if the
		// status bar is hidden, so hide that too if necessary.
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_main);
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
	

}
