package dartmouth.timely;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;




// Handy utilities you'll need here and there

public class Utils {
	
	
	// Convert Location array to byte array, to store in SQLite database
	public static  byte[] fromLocationArrayToByteArray(Location[] locationArray) {

		int[] intArray = new int[locationArray.length * 2];

		for (int i = 0; i < locationArray.length; i++) {
			intArray[i * 2] = (int) (locationArray[i].getLatitude() * 1E6);
			intArray[(i * 2) + 1] = (int) (locationArray[i].getLongitude() * 1E6);
		}

		ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length
				* Integer.SIZE);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(intArray);

		return byteBuffer.array();
	}

	public static  Location[] fromByteArrayToLocationArray(byte[] bytePointArray) {

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytePointArray);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();

		int[] intArray = new int[bytePointArray.length / Integer.SIZE];
		intBuffer.get(intArray);

		Location[] locationArray = new Location[intArray.length / 2];

		assert (locationArray != null);

		for (int i = 0; i < locationArray.length; i++) {
			locationArray[i] = new Location("");
			locationArray[i].setLatitude((double) intArray[i * 2] / 1E6F);
			locationArray[i].setLongitude((double) intArray[i * 2 + 1] / 1E6F);
		}
		return locationArray;
	}
	

	public static LatLng fromLocationToLatLng(Location location){
		return new LatLng(location.getLatitude(), location.getLongitude());
		
	}

}
