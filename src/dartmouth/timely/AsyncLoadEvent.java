package dartmouth.timely;

import java.io.IOException;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.model.Events;

/**
 * Asynchronously grabs event from Google Calendar
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
//		String pageToken = null;
		
		// to do
		try {
			System.out.println("Grabbing events listing");
			
			Events events = activity.client.events().list("primary").execute();
			System.out.println("Event: " + events);
		} catch (UserRecoverableAuthIOException e) {
	          activity.startActivityForResult(e.getIntent(), activity.REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected final void onPostExecute(Boolean success) {
		// continue
	}
}
