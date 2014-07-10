package kidozen.samples.push;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends Activity implements  IPushEvents {
    private KidoZenHelper helper = new KidoZenHelper(this);
    MainActivity mSelf = this;
    TextView textView;
    Button signInBtn , subscribeBtn, pushBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper.setPushEvents(mSelf);
        signInBtn = (Button) findViewById(R.id.buttonSignIn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.SignIn();
            }
        });

        subscribeBtn = (Button) findViewById(R.id.buttonSubscribe);
        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.Register();
            }
        });


        textView = (TextView) findViewById(R.id.textView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // IPushEvents
    @Override
    public void ReturnUserName(String username) {
        textView.setText( "Hello: " + username );
        subscribeBtn.setEnabled(true);
    }

    @Override
    public void ReturnRegistrationMessage(String message) {
        textView.setText( "Return message: " +  message );
    }
    @Override
    public void ReturnPushMessage(String message) {
        textView.setText( "Return message: " +  message );
    }
}
