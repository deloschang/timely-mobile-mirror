package dartmouth.timely;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class estOnClickListener implements OnClickListener {

	TextView card_obj;
	String assignDueDate;
	String assignEstimate;
	
	public estOnClickListener(TextView card_obj, String assignEstimate, String assignDueDate) {
		this.card_obj = card_obj;
		this.assignEstimate = assignEstimate;
		this.assignDueDate = assignDueDate;
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