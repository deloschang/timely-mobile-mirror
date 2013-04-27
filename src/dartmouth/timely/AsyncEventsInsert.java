package dartmouth.timely;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.net.ParseException;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events.Insert;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Asynchronously inserts event into Google Calendar 
 * after AutoScheduler
 * 
 * @author Delos Chang
 */



// Insert wrapper class
//class AsyncEventsInsertWrapper {
//	public String startDate;
//}
		
class AsyncEventsInsert extends AsyncTask<MainActivity, String, Void>{

	final String CLASS_CALENDAR_FROM_API = "5glrh8ja4ee6vgl4ghnluo4bmk@group.calendar.google.com";
	
	private final MainActivity activity;
	private final String startDate;
	private final String startName;
	Context context;
	
	AsyncEventsInsert(MainActivity activity, String startDate, String startName){
		this.activity = activity;
		this.context = activity.getApplicationContext();
		this.startDate = startDate;
		this.startName = startName;
	}
	

	@Override
	protected Void doInBackground(MainActivity... params){
		try {
//			System.out.println("Grabbing estimate listing");
			
			// add class as a marker
			Event eventBody = new Event();
			
			eventBody.setSummary(startName);
			
			// startdate
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");  
			try {  
			    Date date = format.parse(startDate);  
				DateTime start = new DateTime(date, TimeZone.getTimeZone("UTC"));
				
				Date endDate = new Date(date.getTime() + 3600000);
				DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
				
				eventBody.setStart(new EventDateTime().setDateTime(start));
				eventBody.setEnd(new EventDateTime().setDateTime(end));
				
			} catch (ParseException e) {  
			    // TODO Auto-generated catch block  
			    e.printStackTrace();  
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			eventBody.setDescription("scheduled from Timely");
			Event events = activity.client.events()
				.insert(CLASS_CALENDAR_FROM_API, eventBody)
				.execute();
			
			System.out.println("Event Checkpoint: " + events);
			
			// Grab first event's name
//			List<Event> event_list = events.getItems();
//			String assignment_name = event_list.get(0).getSummary();
//			String estimate = event_list.get(0).getDescription();
//			String class_name = event_list.get(0).getLocation();
//			
//			System.out.println("Summary: " + assignment_name);
//			System.out.println("Estimate: " + estimate);
			
//			InsertWrapper wrapper = new InsertWrapper();
//			wrapper.assignment_name = assignment_name;
			
//			return wrapper;
		} catch (UserRecoverableAuthIOException e) {
	          activity.startActivityForResult(e.getIntent(), activity.REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			return null;
		}
		return null;
	}
	
	protected final void onPostExecute() {
		MainActivity.noteLatLong("Scheduled ", startName, context, "");
//		MainActivity.updateBar(Globals.LOAD_ESTIMATE, activity, wrapper.assignment_name + " ("+wrapper.subtext+")");
	}
}
