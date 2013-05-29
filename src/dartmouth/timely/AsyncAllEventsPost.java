package dartmouth.timely;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Asynchronously grabs event listings from the Timely events API
 * 
 * @author Delos Chang
 */

// wrapper class
class EventsWrap {
	public String result; // result from the GET request
	public String result_movie; // result from the movie GET request
	public Activity activity;
}



class AsyncAllEventsPost extends AsyncTask<String, Void, EventsWrap>{

	Activity activity;
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_URL = "url";
	
	private static final String MOVIE_NAME = "moviename";
	private static final String MOVIE_TIME = "moveitime";
	private static final String MOVIE_PLACE = "movieplace";
	
// map that contains the id to url links
HashMap<Integer, String> posToURL= new HashMap<Integer, String>();

	AsyncAllEventsPost(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected EventsWrap doInBackground(String... params) {
		System.out.println("Checkpoint for the async task");

		String link = params[0];
		String movies_link = params[1];
		
		// Set up the GET request for events
		HttpClient client = new DefaultHttpClient();  
		String grabURL = link;
		HttpGet createget = new HttpGet(grabURL);

		
		EventsWrap wrapper = new EventsWrap();
		
		int work_check = 0;
		try {
			HttpResponse result = client.execute(createget); 


			wrapper.result =  EntityUtils.toString(result.getEntity());  // events
			
			work_check = 1;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
		
		// Set up the GET request for movies
		HttpClient movieclient = new DefaultHttpClient();  
		HttpGet createmovieget = new HttpGet(movies_link);
		
		try {
			HttpResponse movieresult = movieclient.execute(createmovieget); 
			wrapper.result_movie = EntityUtils.toString(movieresult.getEntity()); // movies

			return wrapper;
		} catch (IOException e) {
			e.printStackTrace();
			
			if (work_check == 1){
				return wrapper;
			} 
			return null;
		} 
	}

	@Override
	protected void onPostExecute(EventsWrap resultWrapper) {
		try {
			ListView scrollMenu = null;
			scrollMenu = (ListView) activity.findViewById(R.id.eventListing);
			
			// initialize adapter
			SeparatedListAdapter adapter = new SeparatedListAdapter(activity.getApplicationContext());

			ArrayList<ArrayList<HashMap<String, String>>> containerList = new ArrayList<ArrayList<HashMap<String, String>>>(); 
			ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();

			containerList.add(itemList);
			ArrayList<HashMap<String, String>> itemList2 = new ArrayList<HashMap<String, String>>();

			containerList.add(itemList2);
			scrollMenu.setVisibility(View.VISIBLE);

			
			int pos = 1; // start pos at 1
			// if events work
			if (resultWrapper.result != null) {

				String response = resultWrapper.result;

				// Parse JSON from the API response
				// contains all listings
				JSONArray jArray = new JSONArray(response);

				for (int i = 0; i < jArray.length(); i++){
					JSONObject jObject = jArray.getJSONObject(i); // main object


					// Create list with title, text, and then link to the actual URL
					JSONObject dateObject = jObject.getJSONObject("date");
					String title = jObject.getString("title");
					String text = dateObject.getString("text");
					String url = jObject.getString("url");

					// for the list container
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(TAG_ID, title);
					map.put(TAG_NAME, text);

					containerList.get(0).add(map);
					
					// Add the URL to the associated position
					posToURL.put(pos, url);
					pos++;
				}
				

				// Container always retrieves listing. Support for other genres etc.
				adapter.addSection("Events Listing", new SimpleAdapter(activity.getApplicationContext(), 
						containerList.get(0), R.layout.events_item_list, 
						new String[] { TAG_ID, TAG_NAME }, new int[] { R.id.itemName, R.id.itemText }));

				
				
				// if movies work
				pos++;
				if (resultWrapper.result_movie != null) {
					String movie_response = resultWrapper.result_movie;
					System.out.println(movie_response);
					
					JSONArray movieArray = new JSONArray(movie_response);
					
					for (int i = 0; i < movieArray.length(); i++){
						JSONObject movieObject = movieArray.getJSONObject(i); // main object
	
	
						// Create list with title, text, and then link to the actual URL
						String movie_name = movieObject.getString("name");
						System.out.println(movie_name);
						String movie_time = movieObject.getString("time");
						String movie_place = movieObject.getString("place");
	
						// for the list container
						HashMap<String, String> movie_map = new HashMap<String, String>();
						movie_map.put(MOVIE_NAME, movie_name);
						movie_map.put(MOVIE_TIME, movie_time);
						movie_map.put(MOVIE_PLACE, movie_place);
	
						containerList.get(1).add(movie_map);
						posToURL.put(pos, "http://www.imdb.com/find?s=all&q="+movie_name);
						pos++;
					}
					
					
				}
				
				// Container always retrieves listing. Support for other genres etc.
				adapter.addSection("Movies", new SimpleAdapter(activity.getApplicationContext(), 
						containerList.get(1), R.layout.movie_item_list, 
						new String[] { MOVIE_NAME, MOVIE_TIME, MOVIE_PLACE }, new int[] { R.id.itemName, R.id.itemText, R.id.itemPlace }));

				scrollMenu.setAdapter(adapter);
				MainActivity.eventsLoaded=true;

				// Unique links for each item
				scrollMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// When clicked, show a toast with the TextView text
						Toast.makeText(activity.getApplicationContext(), "pos: "+position+" id: "+id,
								Toast.LENGTH_SHORT).show();
						
						// Start the URL
						Intent i = new Intent();
						i.setAction(Intent.ACTION_VIEW);
						
						// Retrieve the URL from the position map
						String url = posToURL.get(position);
						i.setData(Uri.parse(url));
						
						activity.startActivity(i);
					}
				});


			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
