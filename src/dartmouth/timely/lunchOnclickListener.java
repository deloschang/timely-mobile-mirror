package dartmouth.timely;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ScrollView;

public class lunchOnclickListener implements OnClickListener {
	Activity activity;
	ListView scrollMenu;
	
	public lunchOnclickListener(Activity activity, ListView scrollMenu) {
		this.activity = activity;
		this.scrollMenu = scrollMenu;
	}
	
	public lunchOnclickListener(Activity activity) {
		this.activity = activity;
		this.scrollMenu = null;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

};