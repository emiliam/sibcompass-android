package fi.yle.sibkompassi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;
import fi.yle.sibkompassi.provider.ZipFileContentProvider;

public class PlaySongActivity extends Activity {
	private VideoView videoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_song);

		// Get the song nr from the intent
		Intent intent = getIntent();
		String songNr = intent.getStringExtra(MainActivity.EXTRA_SONG);
		Uri video = ZipFileContentProvider.buildUri("sibbe_" + songNr + ".mp4");
		Log.i("PlaySongActivity", "Uri:" + video);		
		if (video != null) {
			videoView = (VideoView) findViewById(R.id.videoview);
			videoView.setVideoURI(video);
			videoView.setMediaController(new MediaController(this));
			videoView.start();
			videoView.requestFocus();
		}
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
