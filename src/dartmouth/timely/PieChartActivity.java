package dartmouth.timely;

import android.app.Activity;
import android.os.Bundle;

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
	private String activityType = "";

	private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();

			mActivity = data.getString("key_bio_activity", "");
			String[] activityComponents = mActivity.split(",");
			int mActivityType = Long.valueOf(activityComponents[1]).intValue();
			switch (mActivityType) {

			case (0):
				activityType = "Stationary";
			break;
			case (1):
				activityType = "Walking";
			break;
			case (2):
				activityType = "Unknown";
			break;
			case (3):
				activityType = "Running";
			break;
			case (4):
				activityType = "Unknown";
			break;
			default:
				activityType = "Error";
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//main layout
		setContentView(R.layout.display_chart);
		registerReceiver(activityReceiver, new IntentFilter("bio_activity"));	

		//String data_values = getDataValues(LiveDataProvider)

		//pie chart parameters
		//int data_values[] = { 20,10,25,5,15,25};

		int data_values[] = getDataFromBio();
		int color_values[] = {Color.MAGENTA, Color.RED, Color.GREEN,Color.BLUE,Color.YELLOW,Color.CYAN};

		String labels[] = { "Study", "Sleep", "Class", "Gym", "Relax","Party"};

		//get the imageview
		ImageView imgView = (ImageView ) findViewById(R.id.image_placeholder);

		//create pie chart Drawable and set it to ImageView
		PieChart pieChart = new PieChart(this, imgView, activityType, labels, data_values, color_values);
		imgView.setImageDrawable(pieChart);

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(activityReceiver, new IntentFilter("bio_activity"));
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



		int[] arrayToReturn = { 20,10,25,5,15,25 };
		return arrayToReturn;
	}
}
