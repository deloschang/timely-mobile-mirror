package dartmouth.timely;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

	private static final int NOTIFICATION_ID = 1000;
	

	@Override
	public void onReceive(Context context, Intent intent) {

		// Key for determining whether user is leaving or entering 	
		String k=LocationManager.KEY_PROXIMITY_ENTERING;

		//Gives whether the user is entering or leaving in boolean form  
		boolean entering=intent.getBooleanExtra(k, false);
		
		
		//Unpack extras
		
		Bundle extras = intent.getExtras();
				
				
		if(entering){
			int key = extras.getInt(Globals.PROX_TYPE_INDIC, -1);
			// key appears to be -1!
//			Toast.makeText(context, "key "+key, Toast.LENGTH_LONG).show();

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
//					MainActivity.done_registering_event = 1;
					break;

				case Globals.PROX_LUNCH:
//					Toast.makeText(context, "lunch here", Toast.LENGTH_LONG).show();
					// If in this geofence, turn on the FoCo card
					MainActivity.load_lunch = 1;
					break;

			}

			// Call the Notification Service or anything else that you would like to do here
//			Toast.makeText(context, "Welcome to my Area", Toast.LENGTH_LONG).show();
		}else{
		    //Other custom Notification 
		    Toast.makeText(context, "Exiting Hotspot", Toast.LENGTH_LONG).show();
		    MainActivity.isLunchLaunched = false;
		  }
	  
		//Building notification
		Intent i = new Intent(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);
		Notification noti = new Notification.Builder(context)
		        .setContentTitle("Proximity Alert!")
		        .setContentText("You're near a hotspot.")
		        .setSmallIcon(R.drawable.timely_icon)
		        .setContentIntent(pIntent).build();
		    	  
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
//		noti.defaults |= Notification.DEFAULT_VIBRATE;

		NotificationManager notificationManager = 
		  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

		notificationManager.notify(NOTIFICATION_ID, noti); 
		
		
	}


 
 
}
