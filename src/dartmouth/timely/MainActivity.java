package dartmouth.timely;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;


public class MainActivity extends FragmentActivity implements OnMapClickListener{

	// API for calendar
	final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/calendar";

	// Test API for now; to be replaced
//	final String TIMELY_API_URL = "http://pure-retreat-6606.herokuapp.com/api/v1/locations";
	final String TIMELY_DEMO_URL = "http://timely-api.herokuapp.com/places";

	// Mapquest API
	final String MAPQUEST_API = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=json";

	// Google Maps API lat/lng for Hanover
	private GoogleMap map;
	static final LatLng DARTMOUTH_COORD = new LatLng(43.704446,-72.288697);
	static final int ZOOM_LEVEL = 17;


	// main options object
	PolylineOptions polyline_options;


	// polling
	Map<String, Integer> pollmap = new HashMap<String, Integer>();

	// oAuth2
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	final JsonFactory jsonFactory = new GsonFactory();
	private static final String PREF_ACCOUNT_NAME = "accountName";

	com.google.api.services.calendar.Calendar client;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Old oAuth Code
		//		AccountManager accountManager = AccountManager.get(MainActivity.this);    	
		//		Account[] accounts = accountManager.getAccountsByType("com.google");
		//
		//		Account account = accounts[0];
		//		//Log.d("Timely","started");
		//
		//
		//		accountManager.invalidateAuthToken(account.type, accountManager.KEY_AUTHTOKEN);
		//
		//		accountManager.getAuthToken(account,
		//				"oauth2:https://www.googleapis.com/auth/calendar", null,
		//				this,
		//				new AccountManagerCallback<Bundle>(){ 
		//			public void run(AccountManagerFuture<Bundle> future) {
		//				try{
		//					Bundle bundle = future.getResult();
		//					if(bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
		//						String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		//						sendLocation (token);
		//					}else {
		//
		//					}
		//				}
		//				catch(Exception e){
		//					e.printStackTrace();
		//				}  
		//			}
		//		}, null);        

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		// Google Accounts
		credential = GoogleAccountCredential.usingOAuth2(this, CalendarScopes.CALENDAR);
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
		// Calendar client
		client = new com.google.api.services.calendar.Calendar.Builder(
				transport, jsonFactory, credential).setApplicationName("Timely")
				.build();


		// Google Maps API v2 dance
		if (checkGooglePlayServicesAvailable()){
			map = ((SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap(); // generate the map

			// set camera to Dartmouth
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(DARTMOUTH_COORD, ZOOM_LEVEL));
			map.setOnMapClickListener(this);

			// path of the user with clicks (shortest distance)
			polyline_options = new PolylineOptions();
			Polyline path_from_clicks = map.addPolyline(polyline_options);

		} else {
			Toast.makeText(getApplicationContext(), "No Google Play found", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (checkGooglePlayServicesAvailable()) {
			haveGooglePlayServices();
		}
	}

	/** Check that Google Play services APK is installed and up to date. */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	private void haveGooglePlayServices() {
		// check if there is already an account selected
		if (credential.getSelectedAccountName() == null) {
			// ask user to choose account
			chooseAccount();
		} 
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_GOOGLE_PLAY_SERVICES:
			if (resultCode == Activity.RESULT_OK) {
				haveGooglePlayServices();
			} else {
				checkGooglePlayServicesAvailable();
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				// do something
			} else {
				chooseAccount();
			}
			break;
			
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();
					
					
					// Pull upcoming event 
					new AsyncLoadEvent(this).execute();
				}
			}
			break;
		}
	}


	/** Grab location coordinates and do something **/
	public void sendLocation(String accessToken) {    	
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location != null) {
			// LatLng object and Strings of coordinates
			String latitude = Double.toString(location.getLatitude());
			String longitude = Double.toString(location.getLongitude());

			// Post to API with latitude and longitude
			new NetworkPost().execute(TIMELY_DEMO_URL, latitude,longitude);

			
			// GET request to MapQuest with latitude longitude
			String url = MAPQUEST_API+"&lat="+latitude+"&lon="+longitude;
			new NetworkGet().execute(url);

			// test send notification
			noteLatLong(latitude, longitude);
		}

		final LocationListener locationListener = new LocationListener() {
			// Once location has changed
			public void onLocationChanged(Location location) {
				String latitude = Double.toString(location.getLatitude());
				String longitude = Double.toString(location.getLongitude());

				// POST to API with latitude and longitude
				new NetworkPost().execute(TIMELY_DEMO_URL, latitude, longitude);

				// GET request to MapQuest with latitude longitude
				String url = MAPQUEST_API+"&lat="+latitude+"&lon="+longitude;

				// creates a marker at current user location // 
				//				LatLng user_coord = new LatLng(location.getLatitude(), location.getLongitude());
				//				new NetworkGet().execute(url, user_coord);
				new NetworkGet().execute(url);

				// test send notification
				noteLatLong(latitude, longitude);
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}
		};

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

		//new JsonFactory();

		//service.accessKey = "zwe7TX17stsEOnB7FeAqQN7E";
		//service.setApplicationName("Timely");

	}


	// POST request to the Timely API
	private class NetworkPost extends AsyncTask<String, Void, HttpResponse>  {
		@Override
		protected HttpResponse doInBackground(String... params) {
			String link = params[0];

			HttpPost httppost = new HttpPost(link);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("latitude", params[1]));
			nameValuePairs.add(new BasicNameValuePair("longitude", params[2]));

			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}            

			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			try {
				return client.execute(httppost);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				client.close();
			}
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			if (result != null) {
				String location;
				try {
					location = EntityUtils.toString(result.getEntity());
					System.out.println ("Location from server " + location);
					//	            	TextView view = (TextView) findViewById(R.id.text);
					//	            	view.setText(location);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	// wrapper class
	public class Wrapper {
		public HttpResponse result;
		public LatLng point;
	}

	// GET request for the Mapquest API
	// Wrapper class enables multiple type parameters
	private class NetworkGet extends AsyncTask<Object, Void, Wrapper>  {

		@Override
		// use Object type for different type parameters
		protected Wrapper doInBackground(Object... params) {
			Wrapper p = new Wrapper(); // Wrapper class is returned to OnPostExecute
			String url = (String) params[0];

			// Check if the user clicked the map
			// If so, a LatLng point object will be passed
			try {
				p.point = (LatLng) params[1];
			} catch (Exception e){
				// not a map click, continue
			}

			// Set up the GET request
			HttpClient client = new DefaultHttpClient();  
			String getURL = url;
			HttpGet get = new HttpGet(getURL);

			try {
				p.result = client.execute(get); 
				return p;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
		}


		@Override
		// after GET request finished
		protected void onPostExecute(Wrapper p) {
			try {
				HttpEntity resEntityGet = p.result.getEntity();  

				if (resEntityGet != null) {  
					String response;

					response = EntityUtils.toString(resEntityGet); // response JSON 

					// Set full JSON text (enable this and uncomment in main.xml to view full JSON)
					// Or just use curl on the MAPQUEST_API URL
					//					TextView view = (TextView) findViewById(R.id.text);
					//					view.setText(response);
					/////// end ///////

					// Parse JSON
					JSONObject jObject = new JSONObject(response);
					String display_name_obj = jObject.getString("display_name");

					String[] display_name_arr = display_name_obj.split(",");

					// Check if user clicked on map
					if (p.point != null){
						// Generate some interesting stats
						JSONObject addressObject = jObject.getJSONObject("address");
						String city = addressObject.getString("city");

						int poll;
						if (!pollmap.containsKey(display_name_arr[0])){
							poll = 0;
							if (display_name_arr[0].contains("Cemetery") ||
									display_name_arr[0].contains("St") || 
									display_name_arr[0].contains("Street") ||
									display_name_arr[0].contains("River") || 
									display_name_arr[0].contains("Road") ||
									display_name_arr[0].contains("Rd") ||
									display_name_arr[0].contains("Lane") ||
									display_name_arr[0].contains("Ln") ||
									display_name_arr[0].contains("High School") ||
									display_name_arr[0].contains("Catholic") ||
									display_name_arr[0].contains("Avenue") ||
									display_name_arr[0].contains("Trail") ||
									display_name_arr[0].contains("Park") ||
									display_name_arr[0].contains("Route") ||
									display_name_arr[0].contains("President") ||
									display_name_arr[0].contains("Emergency") ||
									display_name_arr[0].contains("Pond") ||
									display_name_arr[0].contains("Ridge") ||
									display_name_arr[0].contains("Church") ||
									display_name_arr[0].contains("Terrace") ||
									display_name_arr[0].contains("Alumni Center") ||
									display_name_arr[0].contains("Esker")){
								poll = 0;
								pollmap.put(display_name_arr[0], poll);
							} else if (city.contains("Hanover")){
								double first_step = Math.random();

								if (display_name_arr[0].contains("Library")
										|| display_name_arr[0].contains("Hall")
										|| display_name_arr[0].contains("Webster Avenue") 
										|| display_name_arr[0].contains("Gymnasium")){
									poll = 10 + (int)(Math.random()*60);
									pollmap.put(display_name_arr[0], poll);
								} else {

									// check other conditions
									if (first_step < 0.3){
										poll = (int)(Math.random()*40);
										pollmap.put(display_name_arr[0], poll);
									} else {
										double second_step = Math.random();
										if (second_step < 0.5){
											poll = (int)(Math.random()*20);
											pollmap.put(display_name_arr[0], poll);
										} else {
											double third_step = Math.random();
											if (third_step < 0.8){
												poll = (int)(Math.random()*10);
												pollmap.put(display_name_arr[0], poll);
											} else {
												poll = (int)(Math.random()*5);
												pollmap.put(display_name_arr[0], poll);
											}
										}
									}
								}

							}
						} else {
							// contains
							poll = pollmap.get(display_name_arr[0]);
						}


						// Create marker at user's point
						Marker usermarker = map.addMarker(new MarkerOptions().position(p.point)
								.title(display_name_arr[0])
								.snippet(Integer.toString(poll) + " Timely users"));
						usermarker.showInfoWindow(); // display marker title automatically

						// Add the point to the path  with options
						polyline_options.add(p.point);
						polyline_options.width(10);
						polyline_options.color(Color.CYAN);
						map.addPolyline(polyline_options);


						map.animateCamera(CameraUpdateFactory.newLatLng(p.point));
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Pops a notification for user
	private void noteLatLong(String latitude, String longitude){
		Intent notificationIntent = new Intent(this, NotificationReceiverActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		System.out.println("Notification portion reached");
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(this)
		.setContentTitle("Timely")
		.setContentText("Lat: "+latitude+", Long: "+longitude);

		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.timely)
		.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.timely));
		//                    .setTicker(res.getString("Test ticker"))
		//                    .setWhen(System.currentTimeMillis())
		//                    .setAutoCancel(true)
		Notification n = builder.build();

		final int YOUR_NOTIF_ID = 0;
		nm.notify(YOUR_NOTIF_ID, n);
	}

	@Override
	/* 
	 * Called when user clicks on the Google Map
	 * Create a marker at that point, move camera
	 * @see com.google.android.gms.maps.GoogleMap.OnMapClickListener#onMapClick(com.google.android.gms.maps.model.LatLng)
	 */
	public void onMapClick(LatLng point) {
		String url = MAPQUEST_API+"&lat="+point.latitude+"&lon="+point.longitude;

		// Send lat/lng in parameters to draw a marker on the map with the title 
		new NetworkGet().execute(url, point); // reverse-geocode
	}
}