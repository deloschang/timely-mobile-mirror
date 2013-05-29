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
	public Activity activity;
}



class AsyncAllEventsPost extends AsyncTask<String, Void, EventsWrap>{

	Activity activity;
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_URL = "url";
	
// map that contains the id to url links
HashMap<Integer, String> posToURL= new HashMap<Integer, String>();

	AsyncAllEventsPost(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected EventsWrap doInBackground(String... params) {
		System.out.println("Checkpoint for the async task");

		String link = params[0];
		// Set up the GET request
		HttpClient client = new DefaultHttpClient();  
		String grabURL = link;
		HttpGet createget = new HttpGet(grabURL);


		try {
			HttpResponse result = client.execute(createget); 

			EventsWrap wrapper = new EventsWrap();

			wrapper.result =  EntityUtils.toString(result.getEntity());
			return wrapper;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}

	@Override
	protected void onPostExecute(EventsWrap resultWrapper) {
		try {
			if (resultWrapper.result != null) {
				ListView scrollMenu = null;
				scrollMenu = (ListView) activity.findViewById(R.id.eventListing);
				scrollMenu.setVisibility(View.VISIBLE);



				// initialize adapter
				SeparatedListAdapter adapter = new SeparatedListAdapter(activity.getApplicationContext());

				ArrayList<ArrayList<HashMap<String, String>>> containerList = new ArrayList<ArrayList<HashMap<String, String>>>(); 
				ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
				containerList.add(itemList);

				///


				String response = resultWrapper.result;

				// Parse JSON from the API response
				// contains all listings
				JSONArray jArray = new JSONArray(response);

				int pos = 1; // start pos at 1
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

				scrollMenu.setAdapter(adapter);



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
