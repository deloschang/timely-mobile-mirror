package dartmouth.timely;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

	/**
	 * Asynchronously grabs event listings from the Timely events API
	 * 
	 * @author Delos Chang
	 */
	class AsyncEventsPost extends AsyncTask<String, Void, String>
		implements OnMarkerClickListener{
		
		private final MainActivity activity;
		Context context;
		public LocationManager mLocationManager;

		AsyncEventsPost(MainActivity activity){
			this.activity = activity;
			this.context = activity.getApplicationContext();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			String link = params[0];
			// Set up the GET request
			HttpClient client = new DefaultHttpClient();  
			String grabURL = link;
			HttpGet createget = new HttpGet(grabURL);
			
			try {
				HttpResponse result = client.execute(createget); 
				return EntityUtils.toString(result.getEntity());
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (result != null) {
					String response = result;
					
					// Parse JSON from the API response
					JSONArray jArray = new JSONArray(response);
					
					for (int i = 0; i < jArray.length(); i++){
						JSONObject jObject = jArray.getJSONObject(i); // main object
						
						try { 
							// test to see if the location object is null
							jObject.getJSONObject("location");
						} catch (Exception e) {
							// these are null location; throw them out
							// continue to the next loop
							continue;
						}
						
						JSONObject locationObject = jObject.getJSONObject("location");
						
						// location exists, retrieve lat and long
						// lat and lon named on the API 
						double latitude = Double.parseDouble(locationObject.getString("lat"));
						double longitude = Double.parseDouble(locationObject.getString("lon"));
						
						LatLng event_location = new LatLng(latitude, longitude);
						
						// retrieve title and concordance
						// strip tags
						String event_title  = jObject.getString("title").replaceAll("<[^>]*", "");
						
						// retrieve time
						
						JSONObject dateObject = jObject.getJSONObject("date");
						String event_concordance  = dateObject.getString("concordance").replaceAll("<[^>]*", "");;
						String startDate  = dateObject.getString("startDate");
						
						// add event marker to the map
						Marker eventMarker = MainActivity.map.addMarker(new MarkerOptions().position(event_location)
								.title(event_title)
								.snippet(event_concordance)
								.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // event color
								);
						
						// add to the array
						MainActivity.eventMap.put(eventMarker, startDate); // dict with marker and startDate
						MainActivity.eventMarkers.add(MainActivity.eventMap);
						
						// The asynchronous task is done loading the vent
//						if (done_loading_event == 1 && done_registering_event == 0){
//							//			double lat=43.705816, lng=-72.288712;		
//							//			addProximityAlert(lat,lng, Globals.PROX_EVENT_MARKERS);
//
//							for (int i = 0; i < eventMarkers.size(); i++){
//								// First load the set of markers
//								Set<Marker> eventForLoad = eventMarkers.get(i).keySet();
//								Iterator<Marker> iterator = eventForLoad.iterator();
//
//
//								while (iterator.hasNext()){
//									Marker eventIter = iterator.next();
//
//									// For every event that is available, load a proximity alert to load..
//									double eventLat = eventIter.getPosition().latitude;
//									double eventLong = eventIter.getPosition().longitude;
//
//
//									// Testing for the Novack
//
//									//					addProximityAlert(eventLat, eventLong, 
//									//							Globals.PROX_EVENT_MARKERS, eventIter);
//									//					addProximityAlert(eventLat, eventLong, Globals.PROX_EVENT_MARKERS);
//								}
//							}
//						}
						
						
						
						
						
					}
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
						// for each of these eventMarkers, add the proximity geofencing!
						double eventLat =  -43.705816;
						double eventLong = -72.288712;
//						addProximityAlert(eventLat, eventLong, Globals.PROX_EVENT_MARKERS, eventMarker);
						addProximityAlert(eventLat, eventLong, Globals.PROX_LUNCH);
		}
		

		@Override
		public boolean onMarkerClick(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
		
		
		// overloaded method
		public void addProximityAlert(double latitude, double longitude, int key) {
			addProximityAlert(latitude, longitude, key, null);
		}
		
		/**
		 * addProximityAlert
		 * @param latitude
		 * @param longitude
		 * @param key
		 */
		public void addProximityAlert(double latitude, double longitude, int key, Marker obj) {
			Bundle localBundle = new Bundle();
			localBundle.putInt(Globals.PROX_TYPE_INDIC, key);

			// If the geofencing type is for event markers, unpackage the marker
			switch (key){
				case(Globals.PROX_EVENT_MARKERS):
					Toast.makeText(context, "events prox loaded",
							Toast.LENGTH_LONG).show();
	
				// obj will be a Marker type
				//				localBundle.putString("eventTitle", obj.getTitle());
				//				localBundle.putString("eventConcord", obj.getSnippet()); // should get a description instead
				break;

				case(Globals.PROX_LUNCH):
					Toast.makeText(context, "lunch prox ",
							Toast.LENGTH_LONG).show();
					break;
			}

			Intent intent = new Intent(Globals.PROX_ALERT_INTENT);
			intent.putExtras(localBundle);

			PendingIntent proximityIntent = PendingIntent.getBroadcast(context, 0, intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);

			mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
			mLocationManager.addProximityAlert(
					latitude, // the latitude of the central point of the alert region
					longitude, // the longitude of the central point of the alert region
					Globals.POINT_RADIUS, // the radius of the central point of the alert region, in meters
					Globals.PROX_ALERT_EXPIRATION, // time for this proximity alert, in milliseconds, or -1 to indicate no expiration 
					proximityIntent // will be used to generate an Intent to fire when entry to or exit from the alert region is detected
					);

			IntentFilter filter = new IntentFilter(Globals.PROX_ALERT_INTENT);  
			activity.registerReceiver(new ProximityReceiver(), filter);	   
		}
	}
