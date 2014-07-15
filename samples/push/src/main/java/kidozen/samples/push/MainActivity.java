package kidozen.samples.push;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity implements IPushEvents {
    private KidoZenHelper helper = new KidoZenHelper(this);
    MainActivity mSelf = this;
    TextView textView;
    EditText channelName;
    Button signInBtn , subscribeBtn, unSubscribeBtn, pushBtn, initBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper.setPushEvents(mSelf);

        channelName = (EditText) findViewById(R.id.editTextChannel);

        textView = (TextView) findViewById(R.id.textView);

        signInBtn = (Button) findViewById(R.id.buttonSignIn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.SignIn();
            }
        });

        initBtn = (Button) findViewById(R.id.buttonInit);
        initBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.Initialize();
            }
        });

        subscribeBtn = (Button) findViewById(R.id.buttonSubscribe);
        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.Subscribe(channelName.getText().toString());
            }
        });

        unSubscribeBtn = (Button) findViewById(R.id.buttonUnSubscribe);
        unSubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.UnSubscribe(channelName.getText().toString());
            }
        });

        pushBtn = (Button) findViewById(R.id.buttonPushMsg);
        pushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.Push();
            }
        });
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
    public void onInitializationDone(String message) {
        textView.setText( "Hello: " + message );
        initBtn.setEnabled(true);
        subscribeBtn.setEnabled(true);
        //pushBtn.setEnabled(true);
        unSubscribeBtn.setEnabled(true);
    }

    @Override
    public void onSubscriptionDone(String message) {
        textView.setText( "Return message: " +  message );
        pushBtn.setEnabled(true);
    }

    @Override
    public void onPushDone(String message) {
        textView.setText( "Return message: " +  message );
    }

    @Override
    public void onRemoveSubscriptionDone(String message) {
        textView.setText( "Return message: " +  message );
    }
}
