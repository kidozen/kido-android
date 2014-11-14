package kidozen.samples.dataviz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import kidozen.client.InitializationException;
import kidozen.client.authentication.GPlusAuthenticationResponseReceiver;
import kidozen.client.datavisualization.Constants;


public class MainActivity extends Activity implements IAuthenticationEvents {
    private KidoZenHelper helper = new KidoZenHelper();
    MainActivity mSelf = this;
    TextView textView;
    EditText editVizName;
    Button signIn , signOut, displayDataVisualization;
    Context mContext;
    private MyBroadcastReceiver mMyBroadcastReceiver;
    private boolean exit=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper.setAuthEvents(this);
        mContext = this;

        signIn = (Button) findViewById(R.id.buttonSignIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    helper.SignIn(mSelf);
                } catch (InitializationException e) {
                    e.printStackTrace();
                }
            }
        });

        signOut = (Button) findViewById(R.id.buttonSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.SignOut();
            }
        });

        textView = (TextView) findViewById(R.id.textView);

        editVizName = (EditText) findViewById(R.id.editTextDataVizName);

        displayDataVisualization = (Button) findViewById(R.id.buttonDisplayDataViz);
        displayDataVisualization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.setDataVisualization(mContext, editVizName.getText().toString());
                registerReceiver();
            }
        });
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(Constants.DATA_VISUALIZATION_BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mMyBroadcastReceiver = new MyBroadcastReceiver();
        mContext.registerReceiver(mMyBroadcastReceiver, filter);
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            mContext.unregisterReceiver(mMyBroadcastReceiver);
            MainActivity.this.finish();
        }
        else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
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

    @Override
    public void ReturnUserName(String username) {
        this.signOut.setEnabled(true);
        this.displayDataVisualization.setEnabled(true);
        textView.setText( "Hello: " + username );
    }


    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.DATA_VISUALIZATION_BROADCAST_CONSOLE_MESSAGE);
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }
}
