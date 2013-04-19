package dartmouth.timely;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
//import com.google.api.client.http.HttpResponse;



public class MainActivity extends Activity {

	// API for calendar
    final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/calendar";
    final String TIMELY_API_URL = "http://pure-retreat-6606.herokuapp.com/api/v1/locations";
    final String MAPQUEST_API = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=json";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	AccountManager accountManager = AccountManager.get(MainActivity.this);    	
    	Account[] accounts = accountManager.getAccountsByType("com.google");
    	
        Account account = accounts[0];
        //Log.d("Timely","started");

        accountManager.invalidateAuthToken(account.type, accountManager.KEY_AUTHTOKEN);
        
        accountManager.getAuthToken(account,
            "oauth2:https://www.googleapis.com/auth/calendar", null,
            this,
            new AccountManagerCallback<Bundle>(){ 
                public void run(AccountManagerFuture<Bundle> future) {
                    try{
                         Bundle bundle = future.getResult();
                         if(bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                                String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                                sendLocation (token);
                          }else {
                              
                          }
                         }
                    catch(Exception e){
                        e.printStackTrace();
                        //->start again
                    }  
                  }
             }, null);        

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Log.d("Timely","created");
    }
    
    public void sendLocation(String accessToken) {    	
    	LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
    	Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	if (location != null) {
    		String latitude = Double.toString(location.getLatitude());
    		String longitude = Double.toString(location.getLongitude());
	    	System.out.println ("Latitude: " + latitude + "Longitude: " + longitude);
	    	new NetworkPost().execute(TIMELY_API_URL, latitude,longitude);
	    	
	    	// GET request to MapQuest with latitude longitude
		    String url = MAPQUEST_API+"&lat="+latitude+"&lon="+longitude;
	    	new NetworkGet().execute(url);
    	}
    	
    	final LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	    	String latitude = Double.toString(location.getLatitude());
        		String longitude = Double.toString(location.getLongitude());
        		
    	    	System.out.println ("Latitude_new: " + latitude + "Longitude_new: " + longitude);
    	    	
			    
    	        
    	    	// POST with latitude and longitude
    	    	new NetworkPost().execute(TIMELY_API_URL, latitude, longitude);
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
            //Do something with result
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
    
    private class NetworkGet extends AsyncTask<String, Void, HttpResponse>  {
        @Override
        protected HttpResponse doInBackground(String... params) {
            String url = params[0];
            
//		    try {
//		        HttpClient client = new DefaultHttpClient();  
//		        String getURL = url;
//		        HttpGet get = new HttpGet(getURL);
//		        HttpResponse responseGet = client.execute(get);  
//		        HttpEntity resEntityGet = responseGet.getEntity();  
//		        
//		        if (resEntityGet != null) {  
//		        	
//		            // do something with the response
//		            String response = EntityUtils.toString(resEntityGet);
//		            System.out.println("GET RESPONSE " + response);
//		            
//	            	TextView view = (TextView) findViewById(R.id.text);
//	            	view.setText(response);
//		            
//		            
//		        }
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }
		    
		    
	        HttpClient client = new DefaultHttpClient();  
	        String getURL = url;
	        HttpGet get = new HttpGet(getURL);
	        
            try {
                return client.execute(get); // returned to your onPostExecute(result) method
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } 
        }
        
        
        @Override
        protected void onPostExecute(HttpResponse result) {
		        HttpEntity resEntityGet = result.getEntity();  
		        
		        if (resEntityGet != null) {  
		        	
		            // do something with the response
		            String response;
					try {
						response = EntityUtils.toString(resEntityGet);
			            System.out.println("GET RESPONSE " + response);
			            
		            	TextView view = (TextView) findViewById(R.id.text);
		            	view.setText(response);
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

        
   /*     
    public void postData(String latitude, String longitude) {
    	// Create a new HttpClient and Post Header
    	AndroidHttpClient client = AndroidHttpClient.newInstance("Android UserAgent");
    	HttpPost httppost = new HttpPost(TIMELY_API_URL);
        
        System.out.println ("Posting data with Lat " + latitude + "And long " + longitude);
        
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("latitude", latitude));
            nameValuePairs.add(new BasicNameValuePair("longitude", longitude));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = client.execute(httppost);
            System.out.println ("Request Made");
            
            
            String location = EntityUtils.toString(response.getEntity());
            
            System.out.println ("Location from server " + location);
            
            TextView view = (TextView) findViewById(R.id.text);
            view.setText(location);
            
        } catch (Exception e) {
        	e.printStackTrace();
        }

    	
    }
    */  
}