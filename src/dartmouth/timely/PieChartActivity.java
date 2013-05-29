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
	private long totalConversationTime = 0L;
	private long totalSilentStudyTime = 0L;
	private long totalOnTheMoveTime = 0L;
	private long totalRelaxTime = 0L;
	
	private boolean DEMO_ENABLED = false;
	
	private long totalTime = 0L;
	private String location = "";

	public static final String DATE_FORMAT = "H:mm:ss MMM d yyyy";

	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			mLocation = data.getString("key_bio_location", "");
			System.out.println ("Location Update Received!");
			String[] locationLines = mLocation.split(";");
			String locationStat = "";
			String currLocation = "";
			
			for (String line : locationLines) {
				String[] wifiComponents = line.split(",");
				locationStat = locationStat
						+ parseTime(Double.valueOf(wifiComponents[0]).longValue())+","
						+ wifiComponents[1] +","+wifiComponents[2] + "\n";
				currLocation = wifiComponents[1];
			}
			
			String tokens[] = currLocation.split("-");
			System.out.println( tokens[0] );
			
			if ( tokens[0].equalsIgnoreCase("BerryLib") || tokens[0].equalsIgnoreCase("Carson") ) {
				if ( currActivity.equalsIgnoreCase("Stationary") ) {
					System.out.println ("Incrementing Silent Study Time");
					MainActivity.totalSilentStudyTime += 10000L;
					System.out.println ("New silent study time " + MainActivity.totalSilentStudyTime);
				}
			} else {
				if ( currActivity.equalsIgnoreCase("Stationary") ) {
					System.out.println ("Incrementing Relax Time");
					MainActivity.totalRelaxTime += 10000L;
				} else 
					{
					System.out.println ("Incrementing On the Move Time");
					MainActivity.totalOnTheMoveTime += 10000L;
					}
				
			}
			
			
			mLocation = locationStat;
			//Log.e("MainActivity", "location" + mLocation);
		}
    };

	private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			Bundle data = intent.getExtras();
			System.out.println ("Activity Update Received!");
			
			
			mActivity = data.getString("key_bio_activity", "");
			String[] activityComponents = mActivity.split(",");
			int mActivityType = Long.valueOf(activityComponents[1]).intValue();
			switch (mActivityType) {

			case (0):
				currActivity = "Currently: Stationary";
			break;
			case (1):
				currActivity = "Currently: Walking";
				
			break;
			case (2):
				currActivity = "Currently: Unknown";
			break;
			case (3):
				currActivity = "Currently: Running";
			break;
			case (4):
				currActivity = "Currently: Unknown";
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
			
			System.out.println ("Conversation Update Received!");
			
			MainActivity.totalConversationTime += Double.valueOf(conversationComponents[1]).longValue() - Double.valueOf(conversationComponents[0]).longValue();

			
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
		registerReceiver(locationReceiver, new IntentFilter("bio_location"));
		registerReceiver(conversationReceiver, new IntentFilter(
				"bio_conversation"));
		//String data_values = getDataValues(LiveDataProvider)

		//pie chart parameters
		//int data_values[] = { 20,10,25,5,15,25};

		drawPiechart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(activityReceiver, new IntentFilter("bio_activity"));	
		registerReceiver(locationReceiver, new IntentFilter("bio_location"));
		registerReceiver(conversationReceiver, new IntentFilter(
				"bio_conversation"));
		
		drawPiechart();
	}
	
	public void drawPiechart() {
		int data_values[] = getDataFromBio();
		int color_values[] = {Color.RED, Color.GREEN,Color.BLUE,Color.YELLOW};

		String labels[] = { "Conversation", "Silent Study", "On the Move", "Relax"};

		//get the imageview
		ImageView imgView = (ImageView ) findViewById(R.id.image_placeholder);

		//create pie chart Drawable and set it to ImageView
		String timeText = Long.toString(totalTime/1000L);
		PieChart pieChart = new PieChart(this, imgView, timeText , labels, data_values, color_values);
		imgView.setImageDrawable(pieChart);
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(activityReceiver);
		unregisterReceiver(locationReceiver);
		unregisterReceiver(conversationReceiver);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private int[] getDataFromBio() {
		totalTime = System.currentTimeMillis() - MainActivity.appStartTime;
		if ( totalTime < 60000 ) {
			int arrayToReturn[] = {1};
			arrayToReturn[0] = -1;
			return arrayToReturn;
		} else 
		{
			if (!DEMO_ENABLED) {
			System.out.println ("Total conversation time " + MainActivity.totalConversationTime);
			System.out.println ("Total silent study time " + MainActivity.totalSilentStudyTime);
			System.out.println ("Total on the move time " + MainActivity.totalOnTheMoveTime);
			
			int arrayToReturn[] = { (int) MainActivity.totalConversationTime, (int)MainActivity.totalSilentStudyTime, (int) MainActivity.totalOnTheMoveTime, (int) MainActivity.totalRelaxTime };
			
			System.out.println (arrayToReturn[0]);
			return arrayToReturn;
			} else {
				int arrayToReturn[] = {10,15,20,15};
				return arrayToReturn;
			}
			
		}
	}
}
