package dartmouth.timely;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;


public class PieChart extends Drawable implements OnTouchListener {

	Context context;
	View view;
	Paint paint;

	String timeText;

	String[] data_names;
	int[] color_values;
	int[] data_values;
	RectF arc_bounds;

	int value_sum = 0;

	public PieChart(Context c, View v, String timeText, String[] data_names, int[] data_values, int[] color_values) {
		context = c;
		view = v;

		this.timeText = timeText;
		this.data_values = data_values;
		this.color_values = color_values;
		this.data_names = data_names;

		paint = new Paint();
		view.setOnTouchListener(this);
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub

		//screen width & height
		int view_w = view.getWidth();
		int view_h = view.getHeight();

		//chart area rectangle
		arc_bounds = new RectF(
				10,
				10,
				250,
				250
				);


		if (data_values[0] == Globals.NO_DATA_FOUND) {

			Paint noDataPaint = new Paint();
			noDataPaint.setAntiAlias(true);
			noDataPaint.setColor(Color.WHITE);
			noDataPaint.setTextSize(30);
			//draw legend text
			int remainingSeconds = 60 - Integer.parseInt(timeText);
			canvas.drawText (Globals.NOT_ENOUGH_DATA_TEXT + ": Wait " + remainingSeconds + " seconds"  , 75, 75, noDataPaint);
			return;

		}

		/*
        Paint textPaint2 = new Paint();
		textPaint2.setAntiAlias(true);
		textPaint2.setColor(Color.WHITE);
		textPaint2.setTextSize(20);
		//draw legend text
		canvas.drawText (timeText, view_w/2, view_h/2, textPaint2);
		 */
		//sum of data values
		for (int datum : data_values)
			value_sum += datum;

		float startAngle = 0;
		int i = 0;

		for (int datum : data_values) {
			if (datum == 0) continue;

			//calculate start & end angle for each data value
			float endAngle = value_sum == 0 ? 0 : 360 * datum / (float) value_sum;
			float newStartAngle = startAngle + endAngle;


			int flickr_pink = color_values[i];
			paint.setColor(flickr_pink);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(0.5f);

			//gradient fill color
			LinearGradient linearGradient = new LinearGradient(arc_bounds.left, arc_bounds.top, arc_bounds.right,arc_bounds.bottom, flickr_pink, Color.WHITE, Shader.TileMode.CLAMP);
			paint.setShader(linearGradient);

			//draw fill arc
			canvas.drawArc(arc_bounds, startAngle, endAngle, true, paint);

			Paint linePaint = new Paint();
			linePaint.setAntiAlias(true);
			linePaint.setStyle(Paint.Style.STROKE);
			linePaint.setStrokeJoin(Join.ROUND);
			linePaint.setStrokeCap(Cap.ROUND);
			linePaint.setStrokeWidth(0.5f);
			linePaint.setColor(Color.BLACK);

			//draw border arc
			canvas.drawArc(arc_bounds, startAngle, endAngle, true, linePaint);

			int barStartX = 350;
			int barWidth = 20;
			int barStartY = 50+(i-1)*2*barWidth;

			Rect barRect = new Rect(barStartX,barStartY,barStartX+barWidth,barStartY+barWidth);

			//draw legend box
			paint.setColor(color_values[i]);
			canvas.drawRect(barRect, paint);
			canvas.drawRect(barRect,linePaint);

			Paint textPaint = new Paint();
			textPaint.setAntiAlias(true);
			textPaint.setColor(Color.WHITE);
			textPaint.setTextSize(20);

			//draw legend text
			canvas.drawText(data_names[i], barStartX+2*barWidth, barStartY+barWidth, textPaint);

			startAngle = newStartAngle;
			i++;
		}
	}


	@Override
	public void setAlpha(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//mouse down event
		if (event != null && arc_bounds != null) {
			try{
				if( event.getAction() == MotionEvent.ACTION_DOWN){

					double clickAngle;
					//relative x & y position
					float xPos = event.getX() - arc_bounds.centerX();
					float yPos = event.getY() - arc_bounds.centerY();
					//calcuate the click angle
					clickAngle = Math.atan2(yPos,xPos) * 180 / Math.PI;
					if(clickAngle < 0)
						clickAngle = 360 + clickAngle;
					float startAngle = 0;
					int itemIndex = 0;
					for (int datum : data_values) {
						if (datum == 0) continue;

						float endAngle = value_sum == 0 ? 0 : 360 * datum / (float) value_sum;
						float newStartAngle = startAngle + endAngle;

						//check the condition of start angle & end angle of data item.
						if(arc_bounds.contains(event.getX(),event.getY())  && clickAngle > startAngle && clickAngle < newStartAngle)
						{

							Toast.makeText(context, data_names[itemIndex], Toast.LENGTH_SHORT).show();
							Log.d("Pie","pie item is clicked-->" + data_names[itemIndex]);
							break;
						}

						itemIndex++;
						startAngle = newStartAngle;
					} 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}
}
