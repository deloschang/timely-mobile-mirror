package dartmouth.timely;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;

import android.view.View;
import android.widget.ImageView;





public class PieChartActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        //main layout
        setContentView(R.layout.display_chart);
        
        
        //String data_values = getDataValues(LiveDataProvider)
        
        //pie chart parameters
        //int data_values[] = { 20,10,25,5,15,25};
        
        int data_values[] = getDataFromBio();
        int color_values[] = {Color.MAGENTA, Color.RED, Color.GREEN,Color.BLUE,Color.YELLOW,Color.CYAN};
        
        String labels[] = { "Study", "Sleep", "Class", "Gym", "Relax","Party"};
        
        //get the imageview
        ImageView imgView = (ImageView ) findViewById(R.id.image_placeholder);
         
        //create pie chart Drawable and set it to ImageView
        PieChart pieChart = new PieChart(this, imgView, labels, data_values, color_values);
        imgView.setImageDrawable(pieChart);

    }
    private int[] getDataFromBio() {

        int[] arrayToReturn = { 20,10,25,5,15,25 };
        return arrayToReturn;
    }
}
