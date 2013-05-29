package dartmouth.timely;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.ParseException;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

//class SeparatedListAdapter extends BaseAdapter {
//
//	public final Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();
//	public final ArrayAdapter<String> headers;
//	public final static int TYPE_SECTION_HEADER = 0;
//
//	public SeparatedListAdapter(Context context) {
//		headers = new ArrayAdapter<String>(context, R.layout.list_header);
//	}
//
//	public void addSection(String section, Adapter adapter) {
//		this.headers.add(section);
//		this.sections.put(section, adapter);
//	}
//
//	public Object getItem(int position) {
//		for(Object section : this.sections.keySet()) {
//			Adapter adapter = sections.get(section);
//			int size = adapter.getCount() + 1;
//
//			// check if position inside this section 
//			if(position == 0) return section;
//			if(position < size) return adapter.getItem(position - 1);
//
//			// otherwise jump into next section
//			position -= size;
//		}
//		return null;
//	}
//
//	public int getCount() {
//		// total together all sections, plus one for each section header
//		int total = 0;
//		for(Adapter adapter : this.sections.values())
//			total += adapter.getCount() + 1;
//		return total;
//	}
//
//	public int getViewTypeCount() {
//		// assume that headers count as one, then total all sections
//		int total = 1;
//		for(Adapter adapter : this.sections.values())
//			total += adapter.getViewTypeCount();
//		return total;
//	}
//
//	public int getItemViewType(int position) {
//		int type = 1;
//		for(Object section : this.sections.keySet()) {
//			Adapter adapter = sections.get(section);
//			int size = adapter.getCount() + 1;
//
//			// check if position inside this section 
//			if(position == 0) return TYPE_SECTION_HEADER;
//			if(position < size) return type + adapter.getItemViewType(position - 1);
//
//			// otherwise jump into next section
//			position -= size;
//			type += adapter.getViewTypeCount();
//		}
//		return -1;
//	}
//
//	public boolean areAllItemsSelectable() {
//		return false;
//	}
//
//	public boolean isEnabled(int position) {
//		return (getItemViewType(position) != TYPE_SECTION_HEADER);
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		int sectionnum = 0;
//		for(Object section : this.sections.keySet()) {
//			Adapter adapter = sections.get(section);
//			int size = adapter.getCount() + 1;
//
//			// check if position inside this section 
//			if(position == 0) return headers.getView(sectionnum, convertView, parent);
//			if(position < size) return adapter.getView(position - 1, convertView, parent);
//
//			// otherwise jump into next section
//			position -= size;
//			sectionnum++;
//		}
//		return null;
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//}

class AsyncAllEventsPost extends AsyncTask<String, Void, EventsWrap>{

	Activity activity;
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_URL = "url";

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
					map.put(TAG_URL, url);

					containerList.get(0).add(map);
				}

				
				// Container always retrieves listing. Support for other genres etc.
				adapter.addSection("Events Listing", new SimpleAdapter(activity.getApplicationContext(), 
						containerList.get(0), R.layout.item_list, 
						new String[] { TAG_ID, TAG_NAME, TAG_URL}, new int[] { R.id.itemName }));
				
				scrollMenu.setAdapter(adapter);

				// Hashmap for ListView
//				ArrayList<ArrayList<HashMap<String, String>>> containerList = new ArrayList<ArrayList<HashMap<String, String>>>(); 
//
//				ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
//				ArrayList<HashMap<String, String>> itemList2 = new ArrayList<HashMap<String, String>>();
//				ArrayList<HashMap<String, String>> itemList3 = new ArrayList<HashMap<String, String>>();
//				ArrayList<HashMap<String, String>> itemList4 = new ArrayList<HashMap<String, String>>();
//				ArrayList<HashMap<String, String>> itemList5 = new ArrayList<HashMap<String, String>>();
//				ArrayList<HashMap<String, String>> itemList6 = new ArrayList<HashMap<String, String>>();
//
//				containerList.add(itemList);
//				containerList.add(itemList2);
//				containerList.add(itemList3);
//				containerList.add(itemList4);
//				containerList.add(itemList5);
//				containerList.add(itemList6);
//
//
//				int j = 0;

				// construct menu contents from JSON
//				Iterator focoObjectKeys = menuObject.keys();
//				while ( focoObjectKeys.hasNext()) {
//					String key = (String) focoObjectKeys.next();
//
//					JSONArray focoArray = menuObject.getJSONArray(key);

					//						HashMap<String, String> header = new HashMap<String, String>();
					//						header.put(TAG_NAME, key);
					//						itemList.add(header);

//					for (int i = 0; i < focoArray.length(); i++){
//
//						// creating new HashMap
//						String item = (String) focoArray.get(i);
//						HashMap<String, String> map = new HashMap<String, String>();
//						map.put(TAG_ID, item);
//
//						containerList.get(j).add(map);
//					}
//					adapter.addSection(key, new SimpleAdapter(activity.getApplicationContext(), 
//							containerList.get(j), R.layout.item_list, 
//							new String[] { TAG_ID }, new int[] { R.id.itemName }));

//					j++; // increment search within the container list
//				}


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
