package fi.yle.sibkompassi.extension;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class DownloaderServiceBroadcastReceiver extends android.content.BroadcastReceiver {  


	@Override
	public void onReceive(Context context, Intent intent) {
		 try {
	            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, DownloaderService.class);
	        } catch (PackageManager.NameNotFoundException e) {
	            e.printStackTrace();
	        }
		
	}
}