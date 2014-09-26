package kidozen.samples.gplussample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpStatus;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;


public class MainActivity extends Activity {
    private String TAG = this.getClass().getSimpleName();

    KZApplication mApplication ;
    Storage mStorage;

    Context myContext;
    TextView mMessagesTv;

    String tenantMarketPlace = "https://contoso.kidocloud.com";
    String application = "myApplication";
    String appkey = "get this value from your marketplace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessagesTv = (TextView) findViewById(R.id.messages_text_view);
        myContext = this.getApplicationContext();

        mApplication = new KZApplication(tenantMarketPlace,application,appkey,false);

        final Button signOutButton =(Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mApplication.SignOutFromGPlus(myContext);
                } catch (Exception e) {
                    mMessagesTv.setText(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        final Button revokeButton =(Button) findViewById(R.id.revoke_button);
        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mApplication.RevokeAccessFromGPlus(myContext);
                } catch (Exception e) {
                    mMessagesTv.setText(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        final Button showUserNameButton = (Button) findViewById(R.id.get_name_button);
        showUserNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mApplication.GetKidoZenUser().Claims.get("name");
                mMessagesTv.setText("Hello: " + username);
            }
        });

        final Button signInButton =(Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mApplication.AuthenticateWithGPlus(myContext,new ServiceEventListener() {
                        @Override
                        public void onFinish(ServiceEvent e) {
                        if (e.StatusCode== HttpStatus.SC_OK)
                        {
                            mMessagesTv.setText("Authenticated");
                            try {
                                signOutButton.setEnabled(true);
                                revokeButton.setEnabled(true);
                                showUserNameButton.setEnabled(true);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                mMessagesTv.setText("Cannot log in. " + e1.getMessage());
                            }
                        }
                        else {
                            mMessagesTv.setText("Cannot log in.");
                        }
                        }
                    });
                } catch (Exception e) {
                    mMessagesTv.setText("there was an error trying to authenticate to G+");
                    e.printStackTrace();
                }
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
