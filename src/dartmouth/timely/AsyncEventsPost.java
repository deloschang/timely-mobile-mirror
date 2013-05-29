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

import android.net.ParseException;
import android.os.AsyncTask;

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

		// will shift lat / lng
		
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
						
						if (i == 0){
							JSONObject dateObject = jObject.getJSONObject("date");
							String event_concordance  = dateObject.getString("concordance").replaceAll("<[^>]*", "");;
							String startDate  = dateObject.getString("startDate");
							String index = dateObject.getString("index");
							
							// demo case
							if (startDate == 4){
								// location exists, retrieve lat and long
								// lat and lon named on the API  -- go to the Hop for this event
								double blatitude = 43.70209;
								double blongitude = -72.28788;
								
								LatLng event_location = new LatLng(blatitude, blongitude);
								
								// retrieve title and concordance
								// strip tags
								String event_title  = jObject.getString("title").replaceAll("<[^>]*", "");
								
								
								// add event marker to the map
								Marker eventMarker = MainActivity.map.addMarker(new MarkerOptions().position(event_location)
										.title(event_title)
										.snippet(event_concordance)
										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // event color
										);
								
								// add to the array
								MainActivity.eventMap.put(eventMarker, startDate); // dict with marker and startDate
								MainActivity.eventMarkers.add(MainActivity.eventMap);
								
							}
							
						} 
						
						if (i == 2){
							JSONObject dateObject = jObject.getJSONObject("date");
							String event_concordance  = dateObject.getString("concordance").replaceAll("<[^>]*", "");;
							String startDate  = dateObject.getString("startDate");
							String index = dateObject.getString("index");
							
							// demo case
							if (index == 12){
								// location exists, retrieve lat and long
								// lat and lon named on the API  -- go to the Sigma Delta for this event
								double alatitude = 43.702166;
								double alongitude = -72.291206;
								
								LatLng event_location = new LatLng(alatitude, alongitude);
								
								// retrieve title and concordance
								// strip tags
								String event_title  = jObject.getString("title").replaceAll("<[^>]*", "");
								
								
								
								// add event marker to the map
								Marker eventMarker = MainActivity.map.addMarker(new MarkerOptions().position(event_location)
										.title(event_title)
										.snippet(event_concordance)
										.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // event color
										);
								
								// add to the array
								MainActivity.eventMap.put(eventMarker, startDate); // dict with marker and startDate
								MainActivity.eventMarkers.add(MainActivity.eventMap);
								
							}
							
						}
						
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
						
						// add some object tag
						
						
					}
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
	}
