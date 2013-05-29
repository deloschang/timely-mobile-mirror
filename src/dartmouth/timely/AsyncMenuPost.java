package dartmouth.timely;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ParseException;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Asynchronously grabs event listings from the Timely events API
 * 
 * @author Delos Chang
 */

// wrapper class
class Wrapper {
	public String result; // result from the GET request
	public TextView card_obj;
	public Activity activity;
}

class SeparatedListAdapter extends BaseAdapter {

	public final Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();
	public final ArrayAdapter<String> headers;
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.list_header);
	}

	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}

	public Object getItem(int position) {
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section 
			if(position == 0) return section;
			if(position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for(Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for(Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section 
			if(position == 0) return TYPE_SECTION_HEADER;
			if(position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section 
			if(position == 0) return headers.getView(sectionnum, convertView, parent);
			if(position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}

class AsyncMenuPost extends AsyncTask<String, Void, Wrapper>{

	TextView card_obj;
	Activity activity;
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";

	AsyncMenuPost(Activity activity, TextView card_obj) {
		this.card_obj = card_obj;
		this.activity = activity;
	}

	@Override
	protected Wrapper doInBackground(String... params) {
		System.out.println("Checkpoint for the async task");
		
		String link = params[0];
		// Set up the GET request
		HttpClient client = new DefaultHttpClient();  
		String grabURL = link;
		HttpGet createget = new HttpGet(grabURL);


		try {
			HttpResponse result = client.execute(createget); 

			Wrapper wrapper = new Wrapper();

			wrapper.card_obj = card_obj;
			wrapper.result =  EntityUtils.toString(result.getEntity());
			return wrapper;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}

	@Override
	protected void onPostExecute(Wrapper resultWrapper) {
		try {
			if (resultWrapper.result != null) {
				String response = resultWrapper.result;
				TextView card_obj = resultWrapper.card_obj;

				// Parse JSON from the API response
				JSONObject jObject = new JSONObject(response);


				JSONObject menuObject = null;
				ListView scrollMenu = null;
				if (card_obj.getId() == R.id.focoCard){
					menuObject = jObject.getJSONObject("foco");
					scrollMenu = (ListView) activity.findViewById(R.id.focoMenuCard);

				} else if (card_obj.getId() == R.id.kafCard){
					menuObject = jObject.getJSONObject("kaf");
					scrollMenu = (ListView) activity.findViewById(R.id.kafMenuCard);
				} else if (card_obj.getId() == R.id.hopCard){
					menuObject = jObject.getJSONObject("hop");
					scrollMenu = (ListView) activity.findViewById(R.id.hopMenuCard);
				} else if (card_obj.getId() == R.id.bolocoCard){
					menuObject = jObject.getJSONObject("boloco");
					scrollMenu = (ListView) activity.findViewById(R.id.bolocoMenuCard);
				} else {
					Toast.makeText(activity.getApplicationContext(), "No menu data found!", Toast.LENGTH_SHORT);
					return;
				}

				System.out.println("Activity " + activity);
				// when FoCo menu is clicked
				OnClickListener listener = new lunchOnclickListener(activity, scrollMenu){
					@Override
					public void onClick(View v) {
						// hide after scheduled
						//v.setVisibility(View.GONE);
						MainActivity.closeLunchMenus(activity);


						// set inner menu to visible
						
						if(MainActivity.menuUp == false){
						scrollMenu.setVisibility(View.VISIBLE);
						MainActivity.menuUp = true;
						}
						else{
							scrollMenu.setVisibility(View.GONE);
							MainActivity.menuUp=false;
					}
					}

				};
				card_obj.setOnClickListener(listener);


				// initialize adapter
				SeparatedListAdapter adapter = new SeparatedListAdapter(activity.getApplicationContext());

				// Hashmap for ListView
				ArrayList<ArrayList<HashMap<String, String>>> containerList = new ArrayList<ArrayList<HashMap<String, String>>>(); 

				ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
				ArrayList<HashMap<String, String>> itemList2 = new ArrayList<HashMap<String, String>>();
				ArrayList<HashMap<String, String>> itemList3 = new ArrayList<HashMap<String, String>>();
				ArrayList<HashMap<String, String>> itemList4 = new ArrayList<HashMap<String, String>>();
				ArrayList<HashMap<String, String>> itemList5 = new ArrayList<HashMap<String, String>>();
				ArrayList<HashMap<String, String>> itemList6 = new ArrayList<HashMap<String, String>>();

				containerList.add(itemList);
				containerList.add(itemList2);
				containerList.add(itemList3);
				containerList.add(itemList4);
				containerList.add(itemList5);
				containerList.add(itemList6);


				int j = 0;

				// construct menu contents from JSON
				Iterator focoObjectKeys = menuObject.keys();
				while ( focoObjectKeys.hasNext()) {
					String key = (String) focoObjectKeys.next();

					JSONArray focoArray = menuObject.getJSONArray(key);

					//						HashMap<String, String> header = new HashMap<String, String>();
					//						header.put(TAG_NAME, key);
					//						itemList.add(header);

					for (int i = 0; i < focoArray.length(); i++){

						// creating new HashMap
						String item = (String) focoArray.get(i);
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(TAG_ID, item);

						containerList.get(j).add(map);
					}
					adapter.addSection(key, new SimpleAdapter(activity.getApplicationContext(), 
							containerList.get(j), R.layout.item_list, 
							new String[] { TAG_ID }, new int[] { R.id.itemName }));

					j++; // increment search within the container list
				}


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
