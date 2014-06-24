package com.kidozen.samples.passive;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kidozen.client.*;

public class MainActivity extends Activity {
    KZApplication kido;

    Button initbutton , authbutton;
    MainActivity mSelf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSelf = this;
        initbutton = (Button) findViewById(R.id.buttonInit);
        initbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    kido = new kidozen.client.KZApplication("https://att.kidocloud.com","contacts", "", false, new kidozen.client.ServiceEventListener() {
                        @Override
                        public void onFinish(kidozen.client.ServiceEvent e) {
                            Log.d("Debug", "init");
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });

        authbutton = (Button) findViewById(R.id.buttonAuth);
        authbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kido.StartPassiveAuthentication(mSelf, new kidozen.client.ServiceEventListener() {
                    @Override
                    public void onFinish(kidozen.client.ServiceEvent e) {
                        Log.d("Debug", "auth");
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
