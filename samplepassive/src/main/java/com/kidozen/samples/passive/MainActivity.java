package com.kidozen.samples.passive;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONObject;

import kidozen.client.*;

public class MainActivity extends Activity {
    KZApplication kido;
    Storage storage;
    TextView textviewMessages;
    Button initbutton , authbutton, storagebutton, logoffbutton;
    MainActivity mSelf;
    private String tenantMarketPlace;
    private String application;
    private String appkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSelf = this;



        tenantMarketPlace = "https://contoso.local.kidozen.com";
        application = "androide";
        appkey = "NvKVlORzDZCzgkoRwzT8FOm4tlN4O2T8EwLN4mVYojY="; //"o0vV8ZGZf6ZPrsWan3OrnZvJHuoCJym/o8W0t9pAwNI=";

        /*
        application = "testexpiration";
        appkey = "PaQIDZoDaI8nZD0fM2+8lkNiXvjWBdOO0sYzYntWkwo=";
        */
        initbutton = (Button) findViewById(R.id.buttonInit);
        textviewMessages= (TextView) findViewById(R.id.textViewMessages);
        initbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try
            {
                kido = new kidozen.client.KZApplication(tenantMarketPlace, application, appkey, false, new kidozen.client.ServiceEventListener() {
                    @Override
                    public void onFinish(kidozen.client.ServiceEvent e) {
                        authbutton.setEnabled(true);
                        textviewMessages.setText( String.valueOf(e.StatusCode));
                    }
                });
            }
            catch (Exception e)
            {
                textviewMessages.setText(e.getMessage());
            }
            }
        });

        logoffbutton = (Button) findViewById(R.id.buttonLogoff);
        logoffbutton.setEnabled(false);
        logoffbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                kido.SignOut();
                storagebutton.setEnabled(false);
                authbutton.setEnabled(false);

            }
        });


        authbutton = (Button) findViewById(R.id.buttonAuth);
        authbutton.setEnabled(false);
        authbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kido.Authenticate(mSelf, new kidozen.client.ServiceEventListener() {
                    @Override
                    public void onFinish(kidozen.client.ServiceEvent e) {
                        textviewMessages.setText(String.valueOf(e.StatusCode));
                        storagebutton.setEnabled(true);
                        logoffbutton.setEnabled(true);

                    }
                });
            }
        });

        storagebutton = (Button) findViewById(R.id.buttonStorage);
        storagebutton.setEnabled(false);
        storagebutton.setOnClickListener(LogEventClick());
    }

    private View.OnClickListener GetLogsEventClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    kido.AllLogMessages(new ServiceEventListener() {
                        @Override
                        public void onFinish(ServiceEvent e) {

                            textviewMessages.setText(String.valueOf(e.StatusCode));
                        }
                    });
                } catch (Exception e1) {
                    textviewMessages.setText(e1.getMessage());
                }
            }
        };
    }

    private View.OnClickListener LogEventClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject w = new JSONObject().put("error", "zarlanga");
                    kido.WriteLog("el message","la data",LogLevel.LogLevelCritical, new ServiceEventListener() {
                        @Override
                        public void onFinish(ServiceEvent e) {
                            textviewMessages.setText(String.valueOf(e.StatusCode));
                        }
                    });
                } catch (Exception e1) {
                    textviewMessages.setText(e1.getMessage());
                }
            }
        };
    }

    private View.OnClickListener SaveOnStorageClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    storage = kido.Storage("teststorage");
                    JSONObject itm = new JSONObject().put("name","value");
                    storage.Create(itm, new ServiceEventListener() {
                        @Override
                        public void onFinish(ServiceEvent e) {
                            textviewMessages.setText(String.valueOf(e.StatusCode));
                        }
                    });
                } catch (Exception e1) {
                    textviewMessages.setText(e1.getMessage());
                }
            }
        };
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
