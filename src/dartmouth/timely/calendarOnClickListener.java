package dartmouth.timely;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class calendarOnClickListener implements OnClickListener {

	 String param;
	 String param2;
	 Activity activity;
     public calendarOnClickListener(Activity activity, String param, String param2) {
    	 this.activity = activity;
          this.param = param;
          this.param2 = param2;
     }
     
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public void onClick(View v) {
//		// do cal scheduling here
//		//// grab event time
//		new AsyncEventsInsert(this, eventStartTime, eventName).execute();
//		
//		// remove after scheduled
//		v.setVisibility(View.GONE);
//	}
  };