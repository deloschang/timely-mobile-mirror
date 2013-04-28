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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
					JSONObject focoObject = jObject.getJSONObject("foco");
					
					// parse for object and place in intent
					
					System.out.println(focoObject);
					
					ListView scrollMenu = (ListView) activity.findViewById(R.id.focoMenuCard);
//					TextView innerTextMenu = (TextView) activity.findViewById(R.id.focoInner);
					
					// when FoCo menu is clicked
					OnClickListener listener = new lunchOnclickListener(resultWrapper.activity, scrollMenu){
						@Override
						public void onClick(View v) {
							// hide after scheduled
							v.setVisibility(View.GONE);
							
							// set inner menu to visible
							scrollMenu.setVisibility(View.VISIBLE);
						}
						
					};
					card_obj.setOnClickListener(listener);
					
					// Hashmap for ListView
			        ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();

					// construct menu contents from JSON
					Iterator focoObjectKeys = focoObject.keys();
					while ( focoObjectKeys.hasNext()) {
						String key = (String) focoObjectKeys.next();
						
						System.out.println("reached " + key);
						System.out.println("reached " + focoObjectKeys.hasNext());
						
						JSONArray focoArray = focoObject.getJSONArray(key);
						System.out.println("reached array " + focoArray);
						
						HashMap<String, String> header = new HashMap<String, String>();
						header.put(TAG_NAME, key);
						itemList.add(header);
						
						for (int i = 0; i < focoArray.length(); i++){
							
							// creating new HashMap
							String item = (String) focoArray.get(i);
			                HashMap<String, String> map = new HashMap<String, String>();
			                map.put(TAG_ID, item);
			                
			                itemList.add(map);
						}
						
					}
					
				 ListAdapter adapter = new SimpleAdapter(activity.getApplicationContext(), itemList,
			                R.layout.item_list,
			                new String[] { TAG_ID, TAG_NAME }, new int[] {
			                        R.id.itemName, R.id.itemHeader});
			 
			        scrollMenu.setAdapter(adapter);
					System.out.println("reached done");
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
