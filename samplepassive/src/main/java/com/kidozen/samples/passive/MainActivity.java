package com.kidozen.samples.passive;

import android.app.Activity;
import android.content.Intent;
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
    private static final int SETTINGS_RESULT = 1;
    KZApplication kido;
    Storage storage;
    TextView textviewMessages;
    Button initbutton , authbutton, storagebutton, logoffbutton;
    Button crashbutton, crashnullref, crashinvalidactivity;
    MainActivity mSelf;
    private String tenantMarketPlace;
    private String application;
    private String appkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSelf = this;

        tenantMarketPlace = "https://loadtests.qa.kidozen.com";
        application = "passiveauthpluscrash";
        appkey = "fbOqR5UVjn6Y+bkp2Z17k0R7TrqHtmeuP758YOE0M/k=";

        crashinvalidactivity = (Button) findViewById(R.id.buttonactivitynotfound);
        crashinvalidactivity.setEnabled(false);
        crashinvalidactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO NullRef
                Intent i = new Intent(getApplication(), DummyActivity.class);
                startActivityForResult(i, SETTINGS_RESULT);

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
