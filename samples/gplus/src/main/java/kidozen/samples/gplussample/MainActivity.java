package kidozen.samples.gplussample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import kidozen.client.KZApplication;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.authentication.GPlusIdentityProvider;


public class MainActivity extends Activity {
    GPlusIdentityProvider ip;
    KZApplication mApplication ;
    Context myContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ip  = new GPlusIdentityProvider(this);
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
                            Log.d("this", e.Body);
                        }
                    });
                    //String t = ip.RequestToken();
                    //Log.d("this", t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

/*
        Button signOutButton =(Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ip.SignOut();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button revokeButton =(Button) findViewById(R.id.revoke_button);
        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ip.Revoke();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        */
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
