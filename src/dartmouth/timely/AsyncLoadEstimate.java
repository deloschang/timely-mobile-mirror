package dartmouth.timely;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Asynchronously grabs estimate from Google Calendar event 
 * after AutoScheduler
 * 
 * @author Delos Chang
 */



// String wrapper class
class StringWrapper {
	public String assignment_name;
	public String estimate;
	public String subtext;
	
}
		
class AsyncLoadEstimate extends AsyncTask<MainActivity, Void, StringWrapper>{

	final String CLASS_CALENDAR_FROM_API = "2i4qubb29vdurj8qntlklsdvp4@group.calendar.google.com";
	
	private final MainActivity activity;
	Context context;
	
	AsyncLoadEstimate(MainActivity activity){
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	

	@Override
	protected StringWrapper doInBackground(MainActivity... params){
		try {
//			System.out.println("Grabbing estimate listing");
			
			// add class as a marker
			Events events = activity.client.events()
					.list(CLASS_CALENDAR_FROM_API)
					.set("singleEvents", true)
					.execute();
//			System.out.println("Event Checkpoint: " + events);
			
			// Grab first event's name
			List<Event> event_list = events.getItems();
			String assignment_name = event_list.get(0).getSummary();
			String estimate = event_list.get(0).getDescription();
			String class_name = event_list.get(0).getLocation();
			
			System.out.println("Summary: " + assignment_name);
			System.out.println("Estimate: " + estimate);
			
			StringWrapper wrapper = new StringWrapper();
			wrapper.assignment_name = assignment_name;
			wrapper.estimate = estimate;
			wrapper.subtext = class_name;
			
			return wrapper;
		} catch (UserRecoverableAuthIOException e) {
	          activity.startActivityForResult(e.getIntent(), activity.REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return null;
	}
	
	protected final void onPostExecute(StringWrapper wrapper) {
		// continue
//		MainActivity.classMarker = MainActivity.map.addMarker(new MarkerOptions()
//				.position(MainActivity.CLASS_AT_KEMENY_LOCATION)
//				.title(success)
//				.snippet("from Class Scheduler")
//				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // event color
//			);
		
		try {
			MainActivity.noteLatLong(wrapper.assignment_name, wrapper.estimate, context, wrapper.subtext);
			MainActivity.updateBar(Globals.LOAD_ESTIMATE, activity, wrapper.assignment_name + " ("+wrapper.subtext+")");
		} catch (NullPointerException e){
			Toast.makeText(context, "No estimates found", Toast.LENGTH_SHORT);
			
		}
	}
}
