package fi.yle.sibkompassi;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlaySongActivity extends ActionBarActivity {
	VideoView videoView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 Log.i("PlaySongActivity", "PlaySongActivity — onCreate");
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_play_song);
	   // requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		// Get the song nr from the intent
	    Intent intent = getIntent();
	    String songNr = intent.getStringExtra(MainActivity.EXTRA_SONG);
	    Uri video = Uri.parse("android.resource://" + getPackageName() + "/raw/sibbe_"+ songNr);
	    videoView = (VideoView) findViewById(R.id.myvideoview);
	    videoView.setVideoURI(video);
	    videoView.setMediaController(new MediaController(this));
	    videoView.start();
	    videoView.requestFocus();	
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
