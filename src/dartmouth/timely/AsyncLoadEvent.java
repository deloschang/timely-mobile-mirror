package dartmouth.timely;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Asynchronously grabs event from Google Calendar
 * 
 * @author Delos Chang
 */
class AsyncLoadEvent extends AsyncTask<MainActivity, Void, String>{

	final String CLASS_CALENDAR_FROM_API = "hskbfmkfc5dhbb517ih1r11gjs@group.calendar.google.com";
	
	private final MainActivity activity;
	Context context;
	
//	final com.google.api.services.calendar.Calendar client;
	AsyncLoadEvent(MainActivity activity){
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	@Override
	protected String doInBackground(MainActivity... params){
		// grab client from Activity 
//		com.google.api.services.calendar.Calendar client = params[0].client;
//		String pageToken = null;
		
		// to do
		try {
			System.out.println("Grabbing events listing");
			
			// add class as a marker
			Events events = activity.client.events()
					.list(CLASS_CALENDAR_FROM_API)
					.set("singleEvents", true)
					.execute();
//			System.out.println("Event Checkpoint: " + events);
			
			// Grab first event's name
			List<Event> event_list = events.getItems();
			String class_name = event_list.get(0).getSummary();
			
			System.out.println("Event name: " + class_name);
			return class_name;
			
			
			
		} catch (UserRecoverableAuthIOException e) {
	          activity.startActivityForResult(e.getIntent(), activity.REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected final void onPostExecute(String success) {
		// continue
		MainActivity.classMarker = MainActivity.map.addMarker(new MarkerOptions()
				.position(MainActivity.CLASS_AT_KEMENY_LOCATION)
				.title(success)
				.snippet("from Class Scheduler")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // event color
				);
		
		MainActivity.noteLatLong("Leave for class", success, context);
	}
}
