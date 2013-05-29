package dartmouth.timely;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;

import android.app.ActionBar.LayoutParams;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.Fragment;

import android.os.IBinder;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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


public class MainActivity extends FragmentActivity implements
OnMapClickListener, OnMarkerClickListener {
	// API for calendar
	final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/calendar";

	final String TIMELY_DEMO_URL = "http://timely-api.herokuapp.com/places";

	// Events API
	final String TIMELY_EVENTS_API = "http://timely-api.herokuapp.com/events";
	
	// Movies API
	final String TIMELY_MOVIES_API = "http://damp-forest-6177.herokuapp.com/api/v1/movies";

	// Menu API
	static final String TIMELY_MENU_API = "http://timely-api.herokuapp.com/menus";

	// Mapquest API
	final String MAPQUEST_API = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=json";

	// Google Maps API lat/lng for Hanover
	
    //private static WeakReference<FragmentActivity> wrActivity = null;
	
	public static long appStartTime;
	public static long totalConversationTime = 500L;
	public static long totalSilentStudyTime = 500L;
	public static long totalOnTheMoveTime = 500L;
	public static long totalRelaxTime = 500L;



	private SupportMapFragment mMapFragment;

	public static boolean menuUp;
	public boolean mapOn;
	public static GoogleMap map;
	static final LatLng DARTMOUTH_COORD = new LatLng(43.705105, -72.289582);
	static final LatLng DORM_LOCATION = new LatLng(43.703779, -72.290617); // starting
	// point
	static final LatLng CLASS_AT_KEMENY_LOCATION = new LatLng(43.706121,
			-72.289105); // Kemeny Loc
	static final LatLng HOP_LOCATION = new LatLng(43.70209, -72.28788); // Hop
	static final LatLng KAF = new LatLng(43.705239, -72.288503); // KAF

	static final int ZOOM_LEVEL = 17;

	// main options object for drawing the Google Map
	PolylineOptions polyline_options;

	// oAuth2 -- including Google Calendar API
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	GoogleAccountCredential credential;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	final JsonFactory jsonFactory = new GsonFactory();
	private static final String PREF_ACCOUNT_NAME = "accountName";
	com.google.api.services.calendar.Calendar client;

	// Dynamic updating location
	// This is a boolean flag that is assigned to update the shown location
	// every X sec (under the pause function)
	boolean isUpdating = true;

	// These are the markers that will be shown on the Map.
	// e.g. hopMarker will be the marker for the Hop at the specified coordinate
	static Marker kafMarker;
	static Marker hopMarker;

	// switches that activate different demo features
	// We will need to clear these out once we have the smartphone sensing
	// boilerplate set up.
	// Then, for example, we can infer where classes are. Then silence phone
	// based on that.
	static String at_building_location = ""; // string for where the user currently is (e.g. Baker Memorial Library)
	static int silence_phone = 0;
	static int class_visited = 0;
	static int load_lunch = 0;
//	static int load_event = 0;
	
	public static boolean isLunchLaunched = false;
	public static boolean isLibraryVibrate = false;


	

	// These are for Events API. 
	// The list is used to iterate through the markers and add them onto the map.

	// The event map is used to pass a Marker and a String around.
	static HashMap<Marker, String> eventMap = new HashMap<Marker, String>();
	static List<Map<Marker, String>> eventMarkers = new ArrayList<Map<Marker, String>>();

	// For the Google Now layout -- update bar
	// Basically, when inversed is true, the Google Now card will come from one
	// direction.
	// When it is false, it will come from another direction
	static boolean inversed = true;

	static public String fontPath;
	static public Typeface tf;
	static TextView mappview;

	public static boolean TIME_USAGE_CLICKED_FLAG = true;

	//SensorService Declarations
	public SensorService mSensorService;
	public boolean mIsBound;
	public Intent mServiceIntent;
	private IntentFilter mMotionUpdateFilter;
	private IntentFilter mLocationUpdateFilter;
	public ArrayList<Location> mLocationList;
	public ArrayList<LatLng> mLatLngList;
	
	
	// Use to set flags
	public LatLng curLatLng;
	public int curMotion;
	public Marker marker;
	boolean isFirstlocation=true;
	

	

	// Proximity Declarations

	public LocationManager mLocationManager;

	//public static boolean isLunchLaunched = false;

	


	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		
		mapOn = false;
		
		appStartTime = System.currentTimeMillis();

		// This section enables a different thread that can do something every X
		// sec
		// depending on the pause function below
		// Everything here is asynchronous
		fontPath = "fonts/Roboto-Light.ttf";
		tf = Typeface.createFromAsset(getAssets(), fontPath);

		final TextView current_location = (TextView) findViewById(R.id.current_location);
		//final TextView events_card = (TextView) findViewById(R.id.eventsShowCard);
		// Apply font
		current_location.setTypeface(tf);
		//events_card.setTypeface(tf);

		final TextView map_card = (TextView) findViewById(R.id.mapCard); 
		map_card.setTypeface(tf);


		mappview = (TextView) findViewById(R.id.mapCard);



		final Handler offMainHandler = new Handler();
		Runnable runnableOffMain = new Runnable() {
			@Override
			public void run() {
				while (true) {
					pause(); // pauses the function for X sec. Then continues.
					offMainHandler.post(new Runnable() {
						@Override
						public void run() {
							if (isUpdating) {
								// You can change the top header text here
								// current_location.setText(R.string.demo_location);
								isUpdating = false;

								// check switches by time
								// [demo feature] Checks for flags in this
								// asynchronous thread.
								// If certain flags are up, it will display
								// cards.
								// We COULD adapt this to our smartphone
								// sensing. Maybe it would be
								// better to have GCM push the data on the phone
								// though.
								delayedCheck();
							} else {
								if (at_building_location == ""){
									current_location.setText("Updating location..");
								}
								
								isUpdating = true;
							}
						}
					});
				}
			}
		};
		new Thread(runnableOffMain).start();

		final TextView card_obj = (TextView) findViewById(R.id.timeUsageCard);
		card_obj.setText(Globals.TIME_USAGE_TEXT);
		card_obj.setTypeface(tf);

		//findViewById(R.id.noChartText).setVisibility(View.GONE);

		//final PieChart piechart = new PieChart(this,imgView,data_values,color_values, labels);
		View.OnClickListener timeChartListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//v.setVisibility(View.GONE);
					Intent localIntent = new Intent ( getApplicationContext(), PieChartActivity.class );
					startActivity(localIntent);
				
			
			}
		};

		card_obj.setOnClickListener(timeChartListener);



		// end

		/*
		 * Deflate all Cards here!
		 */

		// deflate the update bar
		// Hide all the cards first




		menuUp=false;



		findViewById(R.id.phoneSilenceCard).setVisibility(View.GONE);
		findViewById(R.id.assignmentCard).setVisibility(View.GONE);
		findViewById(R.id.lunchCard).setVisibility(View.GONE);
		findViewById(R.id.focoCard).setVisibility(View.GONE);
		findViewById(R.id.focoMenuCard).setVisibility(View.GONE);
		findViewById(R.id.kafCard).setVisibility(View.GONE);
		findViewById(R.id.kafMenuCard).setVisibility(View.GONE);
		findViewById(R.id.hopCard).setVisibility(View.GONE);
		findViewById(R.id.hopMenuCard).setVisibility(View.GONE);
		findViewById(R.id.bolocoCard).setVisibility(View.GONE);
		findViewById(R.id.bolocoMenuCard).setVisibility(View.GONE);
		findViewById(R.id.nowlayout).setVisibility(View.GONE);
		findViewById(R.id.eventListing).setVisibility(View.GONE);
		findViewById(R.id.eventCard).setVisibility(View.GONE);
		
		findViewById(R.id.allEventsCard).setVisibility(View.VISIBLE);
		// Set up the events card which is always shown
//		 ignore the fact that it is lunchOnclickListener
		TextView event_card_obj = (TextView) findViewById(R.id.allEventsCard);

		OnClickListener eventCardListener = new lunchOnclickListener(this) {
			@Override
			public void onClick(View v) {
				// open events card
				// Asynchronous posting to the events and movies api
				new AsyncAllEventsPost(activity).execute(TIMELY_EVENTS_API, TIMELY_MOVIES_API);

			}

		};
		event_card_obj.setOnClickListener(eventCardListener);



		// These are for the Google OAuth 2 stuff.
		// This includes the Google Calendar API.
		credential = GoogleAccountCredential.usingOAuth2(this,
				CalendarScopes.CALENDAR);
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME,
				null));
		// Calendar client
		client = new com.google.api.services.calendar.Calendar.Builder(
				transport, jsonFactory, credential)
		.setApplicationName("Timely").build();

		mapJunk();
	}

	@Override
	protected void onSaveInstanceState (Bundle outstate) {
		//super.onSaveInstanceState (outstate);
	}


	public void hideMap(){
		new Handler().post(new Runnable() {
			public void run() {
				if(mMapFragment != null){
					mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
							.findFragmentById(R.id.map));
					getSupportFragmentManager().beginTransaction()
					.hide(mMapFragment).commit();
					mapOn = false;
				}
			}
		});
	}

	public void mapJunk() {

		mapStuff();

		//		Toast.makeText(this, "REACHED", Toast.LENGTH_LONG).show();

		//Register GPS sensor to receive location update
		mLocationUpdateFilter = new IntentFilter();
		mLocationUpdateFilter.addAction("LOCATION_UPDATED");

		//Register Motion sensor to receive motion updates
		mMotionUpdateFilter = new IntentFilter();
		mMotionUpdateFilter.addAction("MOTION_UPDATED");


		// Start and bind the tracking service
		mServiceIntent = new Intent(this, SensorService.class);
		startService(mServiceIntent);
		doBindService();			

		//LocationManager Initializer
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		//Add ProximityReceiver for Novack
		double lat=43.705816, lng=-72.288712;		
		addProximityAlert(43.70209, -72.28788,Globals.PROX_LUNCH);
		

	}

	private void addProximityAlert(double latitude, double longitude, int key) {
			Bundle localBundle = new Bundle();
			localBundle.putInt(Globals.PROX_TYPE_INDIC, key);

			// If the geofencing type is for event markers, unpackage the marker
			switch (key){
				case(Globals.PROX_EVENT_MARKERS):
					Toast.makeText(getApplicationContext(), "events prox loaded",
							Toast.LENGTH_LONG).show();

					// obj will be a Marker type
//					localBundle.putString("eventTitle", obj.getTitle());
//					localBundle.putString("eventConcord", obj.getSnippet()); // should get a description instead
					break;

				case(Globals.PROX_LUNCH):
					break;
			}
		
		
	    
	    Intent intent = new Intent(Globals.PROX_ALERT_INTENT);
	    PendingIntent proximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
	    
	    mLocationManager.addProximityAlert(
	        latitude, // the latitude of the central point of the alert region
	        longitude, // the longitude of the central point of the alert region
	        Globals.POINT_RADIUS, // the radius of the central point of the alert region, in meters
	        Globals.PROX_ALERT_EXPIRATION, // time for this proximity alert, in milliseconds, or -1 to indicate no expiration 
	        proximityIntent // will be used to generate an Intent to fire when entry to or exit from the alert region is detected
	   );
	    
	   IntentFilter filter = new IntentFilter(Globals.PROX_ALERT_INTENT);  
	   registerReceiver(new ProximityReceiver(), filter);	   
	}

	public void mapStuff() {

		// Google Maps API v2 dance
		// It first checks if Google Play Services is available on the phoen
		if (checkGooglePlayServicesAvailable()) {

			mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map));
			map = mMapFragment.getMap();

			// set camera to Dartmouth
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(DARTMOUTH_COORD,
					ZOOM_LEVEL));

			map.setOnMapClickListener(this);

			mapOn = true;
			
			
			// Scrape campus events and load onto map as markers
			// This loads the events from the API via the URL. Then it will
			// populate the map with
			// markers
			new AsyncEventsPost().execute(TIMELY_EVENTS_API);

			// Load routes: path of the user with clicks (shortest distance)
			polyline_options = new PolylineOptions();

			// 1st marker: User starts here
			// 2nd marker: Class added from AsyncLoadEvent [demo feature]
			// 3rd marker: Lunch options
			MainActivity.map.setOnMarkerClickListener(this); // for marker

			// clicks
			Polyline path_from_clicks = map.addPolyline(polyline_options);

		} else {
			Toast.makeText(getApplicationContext(), "No Google Play found",
					Toast.LENGTH_LONG).show();
		}



		mappview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mapOn == true) {
					getSupportFragmentManager().beginTransaction()
					.hide(mMapFragment).commit();
					mapOn = false;
				} else {
					getSupportFragmentManager().beginTransaction()
					.show(mMapFragment).commit();
					mapOn = true;
					View p = (View) v.getRootView();
					if (p != null) {
						TextView lunchstuff = (TextView) p.findViewById(R.id.lunchCard);
						if (lunchstuff.getVisibility() == View.VISIBLE)
						findViewById(R.id.focoCard).setVisibility(View.GONE);
						findViewById(R.id.focoMenuCard).setVisibility(View.GONE);
						findViewById(R.id.kafCard).setVisibility(View.GONE);
						findViewById(R.id.kafMenuCard).setVisibility(View.GONE);
						findViewById(R.id.hopCard).setVisibility(View.GONE);
						findViewById(R.id.hopMenuCard).setVisibility(View.GONE);
						findViewById(R.id.bolocoCard).setVisibility(View.GONE);
						findViewById(R.id.bolocoMenuCard).setVisibility(View.GONE);
						menuUp=false;
					}
				}
			}
		});
	}


	private void pause() {

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (checkGooglePlayServicesAvailable()) {
			haveGooglePlayServices();
		}
		//		class_visited = 0;

		//Register receivers for location and motion updates
		registerReceiver(mLocationUpdateReceiver, mLocationUpdateFilter);
		registerReceiver(mMotionUpdateReceiver, mMotionUpdateFilter);
	}

	@Override
	protected void onPause(){
		unregisterReceiver(mLocationUpdateReceiver);
		unregisterReceiver(mMotionUpdateReceiver);
		super.onPause();
	}

	protected void onDestroy() {
		if (mSensorService != null) {
			mSensorService.stopForeground(true);
			doUnbindService();
		}		
		super.onDestroy();

		// reset parameters
		//		class_visited = 0;
		//		estimate_reminder = 0;
		//		reset_estimate_click = 0;
	}

	/** GOOGLE PLAY OAUTH2 STUFF **/
	/** Check that Google Play services APK is installed and up to date. */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	void showGooglePlayServicesAvailabilityErrorDialog(
			final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, MainActivity.this,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	private void haveGooglePlayServices() {
		// check if there is already an account selected
		if (credential.getSelectedAccountName() == null) {
			// ask user to choose account
			chooseAccount();
		} else {
			// If already chosen, this fires
			// new AsyncLoadEvent(this).execute();
		}
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
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
				// Pull upcoming event
				// new AsyncLoadEvent(this).execute();

				// do something
			} else {
				chooseAccount();
			}
			break;

		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null
			&& data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();

					// Pull upcoming event
					// new AsyncLoadEvent(this).execute();
				}
			}
			break;
		}
	}

	/** END GOOGLE PLAY OAUTH2 STUFF **/

	/**
	 * Location tracking stuff. All this stuff needs to be changed to the
	 * foreground service ( Justice should handle this stuff )
	 */
	/** Grab location coordinates and do something **/
	// checks all the lunch menus and closes them
	public static void closeLunchMenus(Activity activity) {

		ListView focoMenuCard = (ListView) activity
				.findViewById(R.id.focoMenuCard);
		ListView kafMenuCard = (ListView) activity
				.findViewById(R.id.kafMenuCard);
		ListView hopMenuCard = (ListView) activity
				.findViewById(R.id.hopMenuCard);
		ListView bolocoMenuCard = (ListView) activity
				.findViewById(R.id.bolocoMenuCard);

		if (focoMenuCard.getVisibility() == View.VISIBLE) {
			focoMenuCard.setVisibility(View.GONE);
			TextView focoGeneralCard = (TextView) activity
					.findViewById(R.id.focoCard);
			focoGeneralCard.setTypeface(tf);
			focoGeneralCard.setVisibility(View.VISIBLE);

		}

		if (kafMenuCard.getVisibility() == View.VISIBLE) {
			kafMenuCard.setVisibility(View.GONE);
			TextView kafGeneralCard = (TextView) activity
					.findViewById(R.id.kafCard);
			kafGeneralCard.setTypeface(tf);
			kafGeneralCard.setVisibility(View.VISIBLE);
		}

		if (hopMenuCard.getVisibility() == View.VISIBLE) {
			hopMenuCard.setVisibility(View.GONE);
			TextView hopGeneralCard = (TextView) activity
					.findViewById(R.id.hopCard);
			hopGeneralCard.setTypeface(tf);
			hopGeneralCard.setVisibility(View.VISIBLE);
		}

		if (bolocoMenuCard.getVisibility() == View.VISIBLE) {
			bolocoMenuCard.setVisibility(View.GONE);
			TextView bolocoGeneralCard = (TextView) activity
					.findViewById(R.id.bolocoCard);
			bolocoGeneralCard.setTypeface(tf);
			bolocoGeneralCard.setVisibility(View.VISIBLE);
		}
	}

	// wrapper class
	public class Wrapper {
		public HttpResponse result;
		public LatLng point;
		public Activity activity;
	}

	// GET request for the Mapquest API
	// Wrapper class enables multiple type parameters
	private class NetworkGet extends AsyncTask<Object, Void, Wrapper> {
		Activity activity;

		NetworkGet(Activity activity) {
			this.activity = activity;
		}

		@Override
		// use Object type for different type parameters
		protected Wrapper doInBackground(Object... params) {
			Wrapper p = new Wrapper(); // Wrapper class is returned to
			// OnPostExecute
			String url = (String) params[0];

			// Check if the user clicked the map
			// If so, a LatLng point object will be passed
			try {
				p.point = (LatLng) params[1];
				p.activity = activity;
			} catch (Exception e) {
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
				HttpEntity resEntityGet = null;	
				if (p.result != null) {
					resEntityGet = p.result.getEntity();
				}
				if (resEntityGet != null) {
					String response;

					response = EntityUtils.toString(resEntityGet); // response
					// JSON

					// Set full JSON text (enable this and uncomment in main.xml
					// to view full JSON)
					// Or just use curl on the MAPQUEST_API URL
					// TextView view = (TextView) findViewById(R.id.text);
					// view.setText(response);
					// ///// end ///////

					// Parse JSON
					JSONObject jObject = new JSONObject(response);
					String display_name_obj = jObject.getString("display_name");

					String[] display_name_arr = display_name_obj.split(",");
					
					// Update the header with the location
					final TextView current_location = (TextView) findViewById(R.id.current_location);
					current_location.setText(display_name_arr[0]);
					
					// Update the global string with the new location
					at_building_location = display_name_arr[0];
					
					// Create marker at user's point
					// Marker usermarker = map.addMarker(new
					// MarkerOptions().position(p.point)
					// .title(display_name_arr[0]));
					// usermarker.showInfoWindow(); // display marker title
					// automatically
					

//					closeLunchMenus(activity);

					// Add the point to the path with options
					// polyline_options.add(p.point);
					// polyline_options.width(10);
					// polyline_options.color(Color.CYAN);
					// map.addPolyline(polyline_options);
					//
					// map.animateCamera(CameraUpdateFactory.newLatLng(p.point));

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// Pops a notification for user
	public static void noteLatLong(String header, String inner_info,
			Context ctx, String subtext) {
		// sound
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// fix intent
		Intent notificationIntent = new Intent(ctx,
				NotificationReceiverActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager nm = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(ctx)
		.setContentTitle(header).setContentText(inner_info)
		.setSubText(subtext);

		builder.setSmallIcon(R.drawable.timely)
		.setLargeIcon(
				BitmapFactory.decodeResource(ctx.getResources(),
						R.drawable.timely_icon)).setTicker(header)
						.setDefaults(Notification.DEFAULT_VIBRATE)
						.setSound(notification).setAutoCancel(true);
		Notification n = builder.build();

		final int YOUR_NOTIF_ID = 1000;
		nm.notify(YOUR_NOTIF_ID, n);
	}

	public static void noteLatLong(String header, String inner_info, Context ctx) {
		noteLatLong(header, inner_info, ctx, "");
	}

	@Override
	/*
	 * Called when user clicks on the Google Map Create a marker at that point,
	 * move camera
	 * 
	 * @see
	 * com.google.android.gms.maps.GoogleMap.OnMapClickListener#onMapClick(com
	 * .google.android.gms.maps.model.LatLng)
	 */
	public void onMapClick(LatLng point) {
		String url = MAPQUEST_API + "&lat=" + point.latitude + "&lon="
				+ point.longitude;

		// Send lat/lng in parameters to draw a marker on the map with the title
		new NetworkGet(this).execute(url, point); // reverse-geocode
	}

	public void addToPolyline(Marker marker) {
		polyline_options.add(marker.getPosition());
		polyline_options.width(10);
		polyline_options.color(Color.CYAN);
		map.addPolyline(polyline_options);

		marker.showInfoWindow(); // display marker title automatically

		map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
	}

	public void checkSwitches() {
//		if (silence_phone == 1) {
//			// Unsilence phone
//			AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//			// audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//			noteLatLong("Unsilencing phone", "you're out of class",
//					getApplicationContext());
//
//			// set status bar
//			updateBar(Globals.UNSILENCE_PHONE, this,
//					Globals.UNSILENCE_PHONE_TEXT);
//
//			silence_phone = 0;
//			load_lunch = 1; // unique param that loads lunch
//		}
	}

	public void delayedCheck() {
		// first update the location in human-readable (reverse-geocoded)
		if (curLatLng != null){
			String url = MAPQUEST_API + "&lat=" + curLatLng.latitude + "&lon="
					+ curLatLng.longitude;

			// Send lat/lng in parameters to draw a marker on the map with the title
			// Also update the header
			new NetworkGet(this).execute(url, curLatLng); // reverse-geocode

			// If the location is the library, make sure to set phone to vibrate.
			if (at_building_location.contains("Library") || at_building_location.contains("library")){
				if (isLibraryVibrate) return;

				// In library, set to vibrate
				AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				noteLatLong("Phone set to vibrate", "in the library",
						getApplicationContext());

				// set status bar
				updateBar(Globals.VIBRATE_PHONE, this,
						Globals.LIBRARY_VIBRATE);

				isLibraryVibrate = true;
			} else {
				findViewById(R.id.phoneSilenceCard).setVisibility(View.GONE);
				isLibraryVibrate = false;
			}
			
			if (at_building_location.contains("1953 Commons")){
				if (isLunchLaunched) return;
				hideMap();
				noteLatLong("Lunch Menu Options Loaded",
						"because of lunch time/location", getApplicationContext());
	
				updateBar(Globals.LOAD_LUNCH_OPTIONS, this, Globals.LOAD_LUNCH_TEXT);
				//load_lunch = 0;
				
				isLunchLaunched = true;
			} else {
				findViewById(R.id.lunchCard).setVisibility(View.GONE);
				findViewById(R.id.focoCard).setVisibility(View.GONE);
				findViewById(R.id.focoMenuCard).setVisibility(View.GONE);
				findViewById(R.id.kafCard).setVisibility(View.GONE);
				findViewById(R.id.kafMenuCard).setVisibility(View.GONE);
				findViewById(R.id.hopCard).setVisibility(View.GONE);
				findViewById(R.id.hopMenuCard).setVisibility(View.GONE);
				findViewById(R.id.bolocoCard).setVisibility(View.GONE);
				findViewById(R.id.bolocoMenuCard).setVisibility(View.GONE);
				isLunchLaunched = false;
			}
				
			
		}
		
		

		
//		if (load_lunch == 1) {
//			if (isLunchLaunched) return;
//			
//			hideMap();
//			noteLatLong("Lunch Menu Options Loaded",
//					"because of your usual lunch time", getApplicationContext());
//
//			updateBar(Globals.LOAD_LUNCH_OPTIONS, this, Globals.LOAD_LUNCH_TEXT);
//			load_lunch = 0;
//
//			// Add food options
//			hopMarker = map.addMarker(new MarkerOptions()
//			.position(HOP_LOCATION)
//			.title("Eat at the Hop")
//			.icon(BitmapDescriptorFactory
//					.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) // event
//					// color
//					.snippet("Lunch menu loaded"));
//
//			kafMarker = map.addMarker(new MarkerOptions()
//			.position(KAF)
//			.title("Eat at King Arthur's Flour")
//			.icon(BitmapDescriptorFactory
//					.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) // event
//					// color
//					.snippet("Lunch menu loaded"));
//
//			kafMarker.showInfoWindow();
//			isLunchLaunched = true;
//		}

//		if (estimate_reminder == 0) {
			// new AsyncLoadEstimate(this).execute();
//			estimate_reminder = 1;
//		}

	}

	// overloaded
	// These are the primary methods to update the Google Now Card bars. We will
	// need to shift this UI onto the bottom
	// so that it is more like Google Now. The premise could still be the same
	// though.
	public static void updateBar(int key, Activity activity, String card_text) {
		updateBar(key, activity, card_text, null, null, null, null);
	}

	public static void updateBar(int key, Activity activity, String card_text,
			String eventStartTime, String eventStartName) {
		updateBar(key, activity, card_text, eventStartTime, eventStartName,
				null, null);
	}

	public static void updateBar(int key, Activity activity, String card_text,
			String eventStartTime, String eventStartName,
			String assignEstimate, String assignDueDate) {

		Context context = activity.getApplicationContext();
		// always do
		activity.findViewById(R.id.nowlayout).setVisibility(View.VISIBLE);

		TextView card_obj = null;

		ListView focoMenuCard = (ListView) activity
				.findViewById(R.id.focoMenuCard);
		ListView kafMenuCard = (ListView) activity
				.findViewById(R.id.kafMenuCard);



		if (focoMenuCard.getVisibility() == View.VISIBLE) {
			focoMenuCard.setVisibility(View.GONE);
			TextView focoGeneralCard = (TextView) activity
					.findViewById(R.id.focoCard);
			focoGeneralCard.setTypeface(tf);
			focoGeneralCard.setVisibility(View.VISIBLE);
		}

		if (kafMenuCard.getVisibility() == View.VISIBLE) {
			kafMenuCard.setVisibility(View.GONE);
			TextView kafGeneralCard = (TextView) activity
					.findViewById(R.id.kafCard);
			kafGeneralCard.setTypeface(tf);
			kafGeneralCard.setVisibility(View.VISIBLE);
		}

		switch (key) {

		// User is in the library, set phone to vibrate
		case Globals.VIBRATE_PHONE:
			card_obj = (TextView) activity.findViewById(R.id.phoneSilenceCard);
			View.OnClickListener silenceListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// What to do if this card is clicked
//					v.setVisibility(View.GONE);
				}
			};

			card_obj.setOnClickListener(silenceListener);
			break;

		case Globals.UNSILENCE_PHONE:
			card_obj = (TextView) activity.findViewById(R.id.phoneSilenceCard);
			View.OnClickListener unsilenceListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					v.setVisibility(View.GONE);
				}
			};

			card_obj.setOnClickListener(unsilenceListener);
			break;

			// Load estimate Card. [Demo feature]
			// This card would load a "time estimate" from the Google Calnedar

		case Globals.LOAD_ESTIMATE:
			card_obj = (TextView) activity.findViewById(R.id.assignmentCard);
			OnClickListener estOnClickListener = new estOnClickListener(card_obj, assignEstimate, assignDueDate) {

				//					@Override
				//					public void onClick(View v) {
				//						if (reset_estimate_click == 0){
				//							card_obj.setTextColor(Color.BLUE);
				//							card_obj.setText(assignEstimate);
				//							reset_estimate_click = 1;
				//							
				//						} else if (reset_estimate_click == 1){
				//							card_obj.setTextColor(Color.parseColor("#707070"));
				//							card_obj.setText(assignDueDate);
				//							reset_estimate_click = 0;
				//						}
				//					}
			};

			card_obj.setOnClickListener(estOnClickListener);
			break;


			// These load all the lunch options at once.
			// In the real app, we will need to trigger this when it is typically
			// the user's lunch time
		case Globals.LOAD_LUNCH_OPTIONS:
			card_obj = (TextView) activity.findViewById(R.id.lunchCard);
			/*			activity.findViewById(R.id.phoneSilenceCard).setVisibility(
					View.GONE); // close
			 */
			OnClickListener lunchListener = new lunchOnclickListener(activity) {
				@Override
				public void onClick(View v) {
					//v.setVisibility(View.GONE);
					// open Foco card
					if(menuUp == false){
						updateBar(Globals.FOCO_MENU, activity, Globals.FOCO_TEXT);
						updateBar(Globals.KAF_MENU, activity, Globals.KAF_TEXT);
						updateBar(Globals.HOP_MENU, activity, Globals.HOP_TEXT);
						updateBar(Globals.BOLOCO_MENU, activity, Globals.BOLOCO_TEXT);
						menuUp=true;
					}
					else{
						activity.findViewById(R.id.focoMenuCard).setVisibility(View.GONE);
						activity.findViewById(R.id.kafMenuCard).setVisibility(View.GONE);
						activity.findViewById(R.id.hopMenuCard).setVisibility(View.GONE);
						activity.findViewById(R.id.bolocoMenuCard).setVisibility(View.GONE);
						activity.findViewById(R.id.focoCard).setVisibility(View.GONE);
						activity.findViewById(R.id.hopCard).setVisibility(View.GONE);
						activity.findViewById(R.id.bolocoCard).setVisibility(View.GONE);
						activity.findViewById(R.id.kafCard).setVisibility(View.GONE);
						menuUp=false;
					}
				}

			};
			card_obj.setOnClickListener(lunchListener);
			break;

			// when user selects an event to be scheduled

		case Globals.SCHEDULE_EVENT:
			card_obj = (TextView) activity.findViewById(R.id.eventCard);

			System.out.println(eventStartTime);
			OnClickListener calListener = new calendarOnClickListener(activity, eventStartTime, eventStartName){
				@Override
				public void onClick(View v) {
					// insert event into calendar
					new AsyncEventsInsert((MainActivity)activity, param, param2).execute();

					// remove after scheduled
					v.setVisibility(View.GONE);
					noteLatLong("Event Scheduled", param2, activity.getApplicationContext(), "");
				}

			};

			card_obj.setOnClickListener(calListener);
			break;

		case Globals.FOCO_MENU:
			card_obj = (TextView) activity.findViewById(R.id.focoCard);

			// GET request for the Foco Menu and set listener
			System.out.println("Reached async, about to execute");
			new AsyncMenuPost(activity, card_obj).execute(TIMELY_MENU_API);
			break;

		case Globals.KAF_MENU:
			card_obj = (TextView) activity.findViewById(R.id.kafCard);

			new AsyncMenuPost(activity, card_obj).execute(TIMELY_MENU_API);
			break;

		case Globals.HOP_MENU:
			card_obj = (TextView) activity.findViewById(R.id.hopCard);

			new AsyncMenuPost(activity, card_obj).execute(TIMELY_MENU_API);
			break;

		case Globals.BOLOCO_MENU:
			card_obj = (TextView) activity.findViewById(R.id.bolocoCard);

			new AsyncMenuPost(activity, card_obj).execute(TIMELY_MENU_API);
			break;

		default:
			break;

		}



		card_obj.setVisibility(View.VISIBLE);
		card_obj.setText(card_text);
		card_obj.setTypeface(tf);



		// These handle the Google Now card animations
		// for the animation to start
		if (!inversed) {
			card_obj.startAnimation(AnimationUtils.loadAnimation(context,
					R.anim.slide_up_left));
		} else {
			card_obj.startAnimation(AnimationUtils.loadAnimation(context,
					R.anim.slide_up_right));
		}

		inversed = !inversed;
	}

	@Override
	/**
	 * This function handles when a marker is clicked. 
	 * Generally, Aaditya will need to implement this for the Maps Card
	 */
	public boolean onMarkerClick(Marker clickedMarker) {

		closeLunchMenus(this);
//		checkSwitches();

		// try to match the event
		for (int i = 0; i < eventMarkers.size(); i++) {
			if (eventMarkers.get(i).containsKey(clickedMarker)) {
				clickedMarker.showInfoWindow();

				// show a card and schedule button
				String card_text = "Schedule: " + clickedMarker.getTitle();
				String eventStartTime = eventMarkers.get(i).get(clickedMarker);

updateBar(Globals.SCHEDULE_EVENT, this, card_text,
						eventStartTime, clickedMarker.getTitle());
				return true;
			}
		}

		// if not hide
		TextView card_obj = (TextView) findViewById(R.id.eventCard);
		card_obj.setVisibility(View.GONE);

		// DEMO FEATURE FOR WHEN A CLASS MARKER IS CLICKED

		// We don't need this but you guys can see how easy it is to silence the phone.
		//		if (clickedMarker.equals(classMarker) && class_visited == 0){
		//			// Add the point to the path  with options
		//			addToPolyline(classMarker);
		//				
		//			// Silence phone in class
		//			AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		//		    audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		//			noteLatLong("Auto-silencing phone", "in class", getApplicationContext());
		//			
		//			// set update bar
		//			updateBar(Globals.SILENCE_PHONE, this, Globals.SILENCE_PHONE_TEXT);
		//			
		//			silence_phone = 1;
		//			class_visited = 1;
		//			
		//			return true;
		//		}

		if (clickedMarker.equals(kafMarker)){
			addToPolyline(kafMarker);
			return true;
		}

		if (clickedMarker.equals(hopMarker)) {
			addToPolyline(hopMarker);
			return true;
		}

		return false;
	}

	/**
	 * Location tracking stuff. All this stuff needs to be changed to the foreground service 
	 *  ( Justice should handle this stuff )
	 */
	/** Grab location coordinates and do something **/
	//TODO Make Broadcast receivers for both location and motion updates

	// mLocationUpdateFilter = new IntentFilter();
	//mLocationUpdateFilter.addAction("LOCATION_UPDATED");
	private BroadcastReceiver mLocationUpdateReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			synchronized(mLocationList) {
				// Initialization
				if (mLocationList == null || mLocationList.isEmpty())
					return;
				curLatLng = Utils.fromLocationToLatLng(mLocationList.get(mLocationList.size() -1));	

				System.out.println("Lat: " + curLatLng.latitude + " " + curLatLng.longitude);

				
				//Center map on current location
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(curLatLng,ZOOM_LEVEL));

				//Draw markers and set proximity alert
				if(isFirstlocation) {
					addProximityAlert(curLatLng.latitude, curLatLng.longitude,Globals.PROX_LUNCH);
			
					marker = map.addMarker(new MarkerOptions().position(curLatLng)
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
										
					isFirstlocation = false;
				} else {
					marker.remove();
					marker = map.addMarker(new MarkerOptions().position(curLatLng)
							.title("Current location")
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				}				
				
				

			}				
		}		
	};
	private BroadcastReceiver mMotionUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			curMotion = intent.getIntExtra("CLASSIFICATION_RESULT", -1);
			//System.out.println("Current Motion: " + curMotion);
		}		

	};

	//Methods for Sensor Services - Justice
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			//Initialize service from SensorService
			mSensorService = ((SensorService.SensorServiceBinder)service).getService();

			// Get list of locations from Sensor Service
			mLocationList = mSensorService.mLocationList;


		}

		public void onServiceDisconnected(ComponentName name){
			stopService(mServiceIntent);
			mSensorService=null;
		}
	};

	// Bind service and set binding flag.
	private void doBindService() {
		if (!mIsBound) {
			bindService(mServiceIntent, connection, Context.BIND_AUTO_CREATE);
			mIsBound = true;
		}

	}

	// Unbind service and set binding flag.
	private void doUnbindService() {
		if (mIsBound) {
			unbindService(connection);
			mIsBound = false;
		}
	}	


}
