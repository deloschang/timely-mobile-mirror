package dartmouth.timely;

import java.io.IOException;

import android.os.AsyncTask;

import com.google.api.services.calendar.model.Events;

/**
 * Asynchronously grabs Timely event
 * 
 * @author Delos Chang
 */
class AsyncLoadEvent extends AsyncTask<MainActivity, Void, Boolean>{

	private final MainActivity activity;
	
//	final com.google.api.services.calendar.Calendar client;
	AsyncLoadEvent(MainActivity activity){
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(MainActivity... params){
		// grab client from Activity 
//		com.google.api.services.calendar.Calendar client = params[0].client;
		String pageToken = null;
		
		// to do
		try {
			Events events = activity.client.events().list("primary").setPageToken(pageToken).execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Calendar calendar = client.calendars().insert(entry).setFields(CalendarInfo.FIELDS).execute();
		
		return null;
	}
	
	protected final void onPostExecute(Boolean success) {
		// continue
	}
}
