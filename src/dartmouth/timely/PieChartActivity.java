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
	
	private boolean DEMO_ENABLED = true;
	
	private long totalTime = 0L;
	private String location = "";

	public static final String DATE_FORMAT = "H:mm:ss MMM d yyyy";

	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			mLocation = data.getString("key_bio_location", "");
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
			
			if ( tokens[0].equalsIgnoreCase("BerryLib") || tokens[0].equalsIgnoreCase("Carson") ) {
				if ( currActivity.equalsIgnoreCase("Stationary") ) {
					totalSilentStudyTime += 10000;
				}
			} else {
				if ( currActivity.equalsIgnoreCase("Stationary") ) {
					totalRelaxTime += 10000;
				} else totalOnTheMoveTime += 10000;
				
			}
			
			
			mLocation = locationStat;
			//Log.e("MainActivity", "location" + mLocation);
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

			totalConversationTime += Double.valueOf(conversationComponents[1]).longValue() - Double.valueOf(conversationComponents[0]).longValue();
			
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
		int color_values[] = {Color.RED, Color.GREEN,Color.BLUE,Color.YELLOW};

		String labels[] = { "Conversation", "Silent Study", "On the Move", "Relax"};

		//get the imageview
		ImageView imgView = (ImageView ) findViewById(R.id.image_placeholder);

		//create pie chart Drawable and set it to ImageView
		PieChart pieChart = new PieChart(this, imgView, Long.toString(totalTime/1000L) , labels, data_values, color_values);
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
		totalTime = System.currentTimeMillis() - MainActivity.appStartTime;
		if ( totalTime < 60000 ) {
			int arrayToReturn[] = {1};
			arrayToReturn[0] = -1;
			return arrayToReturn;
		} else 
		{
			if (!DEMO_ENABLED) {
			System.out.println ("Total conversation time " + totalConversationTime);
			System.out.println ("Total silent study time " + totalSilentStudyTime);
			System.out.println ("Total on the move time " + totalOnTheMoveTime);
			
			int arrayToReturn[] = { (int) totalConversationTime/(int)totalTime , (int)totalSilentStudyTime/(int)totalTime , (int) totalOnTheMoveTime/(int)totalTime, (int) totalRelaxTime/(int)totalTime };
			System.out.println (arrayToReturn[0]);
			return arrayToReturn;
			} else {
				int arrayToReturn[] = {10,15,20,15};
				return arrayToReturn;
			}
			
		}
	}
}
