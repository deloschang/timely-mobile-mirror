package dartmouth.timely;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/*This is the Reciever for the Brodcast sent, here our app will be notified if the User is 
 * in the region specified by our proximity alert.You will have to register the reciever 
 * with the same Intent you broadcasted in the previous Java file

 */


public class ProximityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "register prox loaded", Toast.LENGTH_LONG).show();
		
		// The receiver gets the Context & the Intent that fired the broadcast as arg0 & agr1 
		String k=LocationManager.KEY_PROXIMITY_ENTERING;

		// Key for determining whether user is leaving or entering 
		// Gives whether the user is entering or leaving in boolean form
		boolean state=intent.getBooleanExtra(k, false);

		// Unpack the extras
		Bundle extras = intent.getExtras();
		
		// User is entering because state is true
		if(state){
			int key = extras.getInt(Globals.PROX_TYPE_INDIC, -1);
			// key appears to be -1!
			Toast.makeText(context, "key "+key, Toast.LENGTH_LONG).show();

			// Entering geofence: check the different keys to see
			// what type of trigger this is 
			switch (key){
				// Event geofence to be entered
				// More keys to take out (snippet, title etc)
				case Globals.PROX_EVENT_MARKERS:
					String title = extras.getString("eventTitle");
					String concord = extras.getString("eventConcord");
					Toast.makeText(context, "Event " + title + " " + concord, 
							Toast.LENGTH_LONG).show();
					
					// Done loading, don't load again
					MainActivity.done_registering_event = 1;
					break;
					
				case Globals.PROX_LUNCH:
					Toast.makeText(context, "lunch here", Toast.LENGTH_LONG).show();
					// If in this geofence, turn on the FoCo card
					MainActivity.load_lunch = 1;
					break;

			}

			// Call the Notification Service or anything else that you would like to do here
//			Toast.makeText(context, "Welcome to my Area", Toast.LENGTH_LONG).show();

		}else{

			// User is leaving because of the state
			//Other custom Notification 
//			Toast.makeText(context, "Thank you for visiting my Area,come back again !!", Toast.LENGTH_LONG).show();

		}

	}


}
