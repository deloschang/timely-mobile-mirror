package dartmouth.timely;


import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

@SuppressLint("UseValueOf")
public class SensorService extends Service implements LocationListener,
		SensorEventListener{
	
	
	private Context mContext;
	// Buffers to store all GPS & Accelerometer data
	public ArrayList<Location> mLocationList;
	private ArrayBlockingQueue<Double> mAccList;
	
	// Sensor manager for accelerometer
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	//Location manager
	private LocationManager mlocationManager;
	
	// ASyncTask for doing something with location/motion updates
	private ClassificationTask mClassificationTask;
	
	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mMotionUpdateBroadcast;
	
	
	//Creating standard binders
	private final IBinder binder = new SensorServiceBinder();

	public class SensorServiceBinder extends Binder{
		SensorService getService(){
			return SensorService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return binder;
	}
	
	@Override 
	public void onCreate() {
		
		Toast.makeText(this, "SENSOR SERVICE REACHED", Toast.LENGTH_LONG).show();
		
		//Initializations
		mContext = this;
		mLocationList = new ArrayList<Location>(Globals.GPS_CACHE_SIZE);
		mAccList = new ArrayBlockingQueue<Double>(Globals.ACC_CACHE_SIZE);
		
		//TODO Update broadcast intents
		mLocationUpdateBroadcast = new Intent();
		mLocationUpdateBroadcast.setAction("LOCATION_UPDATED");
		
		mMotionUpdateBroadcast = new Intent();
		mMotionUpdateBroadcast.setAction("MOTION_UPDATED");
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// Unregistering location manager,
		mlocationManager.removeUpdates(this);
  		mSensorManager.unregisterListener(this);
  		mClassificationTask.cancel(false);

		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		
		//Setting up location manager to collect locations
		mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				0, 0, this);

		//Setting up accelerometer data to collect accelerometer updates
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);
		
		
		mClassificationTask = new ClassificationTask();
		mClassificationTask.execute();

		return START_STICKY;
	}

	
	public void onLocationChanged(Location location) {

		if (location == null || Math.abs(location.getLatitude()) > 90
				|| Math.abs(location.getLongitude()) > 180)
			return;


		synchronized (mLocationList){
			mLocationList.add(location);
		}

		//TODO Send broadcast reporting location update
//		Toast.makeText(mContext, "Location:"+location.toString(), Toast.LENGTH_SHORT).show();
		mContext.sendBroadcast(mLocationUpdateBroadcast);
	}

	@SuppressLint("UseValueOf")
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {			
			
			// Compute m for 3-axis accelerometer input.
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			double m = Math.sqrt(x*x + y*y + z*z);
			
			
			
			// Add m to the mAccList one by one.
			try {
				mAccList.add(new Double(m));
			} catch (IllegalStateException e) {

				// Exception happens when reach the capacity.
				// Doubling the buffer. ListBlockingQueue has no such issue,
				// But generally has worse performance
				ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(
						mAccList.size()*2);
				mAccList.drainTo(newBuf);
				mAccList = newBuf;
				mAccList.add(new Double(m));
								
			}
		}
	}


	private class ClassificationTask extends
			AsyncTask<Void, Double, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			ArrayList<Double> featVect = new ArrayList<Double>();
			
			int blockSize = 0;
			FFT fft = new FFT(Globals.ACC_CACHE_SIZE);
			double[] accBlock = new double[Globals.ACC_CACHE_SIZE];
			double[] re = accBlock;
			double[] im = new double[Globals.ACC_CACHE_SIZE];
			double max = Double.MIN_VALUE;			
			
			
			while(true) {
				try{

					if(isCancelled()==true) return null;

					accBlock[blockSize++]=mAccList.take().doubleValue();
					
					if (blockSize == Globals.ACC_CACHE_SIZE) {
						blockSize = 0;
						max=.0;
						for (double val:accBlock){
							if (max < val) {
								max = val;
							}
						}
						
						fft.fft(re, im);
						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i]
									* im[i]);
							featVect.add(mag);
							im[i] = .0; // Clear the field
						}			
						
						featVect.add(max);
						
						
						//TODO
						int classifiedValue = (int) WekaClassifier.classify(featVect.toArray());											
						mMotionUpdateBroadcast.putExtra("CLASSIFICATION_RESULT", classifiedValue);
						sendBroadcast(mMotionUpdateBroadcast);
						featVect.clear();
					}
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				

				
			}
			

		}
					
		
	}


	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}	
	
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
}
