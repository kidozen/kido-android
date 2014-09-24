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

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;
import kidozen.client.authentication.GPlusIdentityProvider;


public class MainActivity extends Activity {
    private String TAG = this.getClass().getSimpleName();

    KZApplication mApplication ;
    Storage mStorage;

    Context myContext;
    TextView mMessagesTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessagesTv = (TextView) findViewById(R.id.messages_text_view);
        myContext = this.getApplicationContext();

        mApplication = new KZApplication("https://loadtests.qa.kidozen.com","tasks","NuSSOjO4d/4Zmm+lbG3ntlGkmeHCPn8x20cj82O4bIo=",false);

        Button signInButton =(Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mApplication.AuthenticateWithGPlus(myContext,new ServiceEventListener() {
                        @Override
                        public void onFinish(ServiceEvent e) {
                        Log.d(TAG, e.Body);
                        mMessagesTv.setText("Authenticated");
                            try {
                                mStorage = mApplication.Storage("tasks");
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    mMessagesTv.setText("there was an error trying to authenticate to G+");
                    e.printStackTrace();
                }
            }
        });


        Button signOutButton =(Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mApplication.SignOutFromGPlus(myContext);
                } catch (Exception e) {
                    mMessagesTv.setText("there was an error trying to signing out from G+");
                    e.printStackTrace();
                }
            }
        });

        Button revokeButton =(Button) findViewById(R.id.revoke_button);
        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mApplication.RevokeAccessFromGPlus(myContext);
                } catch (Exception e) {
                    mMessagesTv.setText("there was an error trying to revoke token from G+");
                    e.printStackTrace();
                }
            }
        });

        Button queryStorageButton = (Button) findViewById(R.id.query_storage_button);
        queryStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStorage.All(new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        Log.d(TAG, e.Body);
                    }
                });
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
