package dartmouth.timely;

import android.app.Activity;
import android.os.Bundle;
import android.accounts.*;

public class MainActivity extends Activity
{

    final String AUTH_TOKEN_TYPE = "Manage your tasks";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	AccountManager manager = AccountManager.get(MainActivity.this);
    	Account[] accounts = manager.getAccountsByType("com.google");
    	
        Account myAccount = accounts[0];
        
        //Log.d("Timely","started");


        manager.getAuthToken(myAccount, AUTH_TOKEN_TYPE, null, this, new AccountManagerCallback<Bundle>() {
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                System.out.println(token);
            } catch (OperationCanceledException e) {
                //TODO: user has denied access, handle appropriately
            } catch (Exception e) {

                //handle

            }
        }

        }, null);


        

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Log.d("Timely","created");
    }

    

}
