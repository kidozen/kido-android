package kidozen.samples.crash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import kidozen.client.KZApplication;


public class MainActivity extends Activity {
    MainActivity mSelf;
    KZApplication kido;
    TextView textviewMessages, textviewUrl, textviewApp, textviewKey;
    Button initbutton, crashbutton, crashnullref, crashinvalidactivity;

    String tenantMarketPlace = "http://contoso.kidocloud.com";
    String application = "myApplication";
    String appkey = "get this value from your marketplace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSelf = this;

        textviewMessages= (TextView) findViewById(R.id.textViewMessages);

        initbutton = (Button) findViewById(R.id.buttonInit);
        initbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    kido = new kidozen.client.KZApplication(tenantMarketPlace, application, appkey, false);
                    kido.Initialize(new kidozen.client.ServiceEventListener() {
                        @Override
                        public void onFinish(kidozen.client.ServiceEvent e) {
                            crashbutton.setEnabled(true);
                            crashnullref.setEnabled(true);
                            crashinvalidactivity.setEnabled(true);
                            textviewMessages.setText( String.valueOf(e.StatusCode));
                            kido.EnableCrashReporter(mSelf.getApplication());
                        }
                    });
                }
                catch (Exception e)
                {
                    textviewMessages.setText(e.getMessage());
                }
            }
        });

        crashinvalidactivity = (Button) findViewById(R.id.buttonactivitynotfound);
        crashinvalidactivity.setEnabled(false);
        crashinvalidactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO NullRef
                Intent i = new Intent(getApplication(), DummyActivity.class);
                startActivityForResult(i, 1);
            }
        });

        crashnullref = (Button) findViewById(R.id.buttonNullref);
        crashnullref.setEnabled(false);
        crashnullref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = null;
                Integer crash = value.indexOf("boom");
            }
        });

        crashbutton = (Button) findViewById(R.id.buttonOutOfIndex);
        crashbutton.setEnabled(false);
        crashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer[] arrayOfInts = {0,1};
                Integer crash = arrayOfInts[3];
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
}
