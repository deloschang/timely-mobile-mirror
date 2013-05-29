package dartmouth.timely;

import android.app.Activity;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;

import android.view.View;
import android.widget.ImageView;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class PieChartActivity extends Activity {
	private String mActivity;
	private String mColocation;
	private String mLocation;
	private String mConversation;

	private String currActivity = "";
	private boolean inConversation = false;
	private String location = "";

	public static final String DATE_FORMAT = "H:mm:ss MMM d yyyy";

	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			mLocation = data.getString("key_bio_location", "");
			String[] locationLines = mLocation.split(";");
			String locationStat = "";
			for (String line : locationLines) {
				String[] wifiComponents = line.split(",");
				locationStat = locationStat
						+ parseTime(Double.valueOf(wifiComponents[0]).longValue())+","
						+ wifiComponents[1] +","+wifiComponents[2] + "\n";
			}
			mLocation = locationStat;
			//Log.e("MainActivity", "location" + mLocation);
		}
	};

	private BroadcastReceiver colocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();

			mColocation = data.getString("key_bio_colocation", "");
			String[] colocationLines = mColocation.split(";");
			String colocationStat = "";
			for (String line : colocationLines) {
				String[] bluetoothComponents=line.split(",");
				colocationStat=colocationStat
						+ parseTime(Double.valueOf(bluetoothComponents[0]).longValue())+","
						+ bluetoothComponents[1] +","+ bluetoothComponents[2] + "\n";
			}
			mColocation = colocationStat;
			//Log.e("MainActivity", "colocation" + mColocation);
		}
	};


	private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();

			mActivity = data.getString("key_bio_activity", "");
			String[] activityComponents = mActivity.split(",");
			int mActivityType = Long.valueOf(activityComponents[1]).intValue();
			switch (mActivityType) {

			case (0):
				currActivity = "Stationary";
			break;
			case (1):
				currActivity = "Walking";
			break;
			case (2):
				currActivity = "Unknown";
			break;
			case (3):
				currActivity = "Running";
			break;
			case (4):
				currActivity = "Unknown";
			break;
			default:
				currActivity = "Error";
				break;
			}
		}
	};

	private BroadcastReceiver conversationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			mConversation = data.getString("key_bio_conversation", "");
			String[] conversationComponents = mConversation.split(",");

			//Log.e("MainActivity", "conversation" + mConversation);
		}
	};

	private String parseTime(long timeInSec) {

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(timeInSec * 1000);
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat(DATE_FORMAT);

		return dateFormat.format(calendar.getTime());
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//main layout
		setContentView(R.layout.display_chart);
		registerReceiver(activityReceiver, new IntentFilter("bio_activity"));	

		//String data_values = getDataValues(LiveDataProvider)

		//pie chart parameters
		//int data_values[] = { 20,10,25,5,15,25};

		drawPiechart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(activityReceiver, new IntentFilter("bio_activity"));
		
		drawPiechart();
	}
	
	public void drawPiechart() {
		int data_values[] = getDataFromBio();
		int color_values[] = {Color.MAGENTA, Color.RED, Color.GREEN,Color.BLUE,Color.YELLOW,Color.CYAN};

		String labels[] = { "Study", "Sleep", "Class", "Gym", "Relax","Party"};

		//get the imageview
		ImageView imgView = (ImageView ) findViewById(R.id.image_placeholder);

		//create pie chart Drawable and set it to ImageView
		PieChart pieChart = new PieChart(this, imgView, currActivity, labels, data_values, color_values);
		imgView.setImageDrawable(pieChart);
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(activityReceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private int[] getDataFromBio() {

		if ( ( System.currentTimeMillis() - MainActivity.appStartTime ) < 300000 ) {
			int arrayToReturn[] = {1};
			arrayToReturn[0] = -1;
			return arrayToReturn;
		} else 
		{
			int arrayToReturn[] = { 20,10,25,5,15,25 };
			return arrayToReturn;

		}
	}
}
