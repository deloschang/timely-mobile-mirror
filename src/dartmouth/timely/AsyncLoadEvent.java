package dartmouth.timely;

import java.io.IOException;

import android.os.AsyncTask;


/**
 * Asynchronously grabs Timely event
 * 
 * @author Delos Chang
 */
class AsyncLoadEvent extends AsyncTask<MainActivity, Void, Boolean>{

//	final com.google.api.services.calendar.Calendar client;

	@Override
	protected Boolean doInBackground(MainActivity... params){
		String pageToken = null;
		com.google.api.services.calendar.Calendar client = params[0].client;
		
		events = client.events().list('primary').setPageToken(pageToken).execute();
		Calendar calendar = client.calendars().insert(entry).setFields(CalendarInfo.FIELDS).execute();
		
		return null;
	}
	
	protected final void onPostExecute(Boolean success) {
		// continue
	}
}
