package com.kidozen.client.push.pubsub.pubsub;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import kidozen.client.KZApplication;
import kidozen.client.PubSubChannel;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.ServiceResponseListener;
import kidozen.client.Storage;
import kidozen.client.SynchronousException;


public class MainActivity extends Activity {
    private Button mButtonPush,mButtonSubscribe;
    private TextView mTextView;
    private EditText mEditText;
    private KidoZenHelper mHelper ;
    private Storage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setEnabled(false);
        mEditText = (EditText) findViewById(R.id.editText);
        mEditText.setEnabled(false);
        mButtonPush = (Button) findViewById(R.id.buttonPublish);
        //mButtonPush.setEnabled(false);
        mButtonPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG","abc");
                /*
                try {
                    mHelper.Publish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                */
            }
        });
        mButtonSubscribe= (Button) findViewById(R.id.buttonSubscribe);
        mButtonSubscribe.setEnabled(false);
        mButtonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mHelper.Subscribe();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mHelper = new KidoZenHelper();
        try {
            mHelper.Authenticate();
        } catch (Exception e) {
            e.printStackTrace();
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

    private class KidoZenHelper {
        private final String key = "1iezHjBY61cLXaDKSlLXszzCStZvYqiU7axVrNIGTrU=";
        private final String app = "integration-tests";
        private final String tenant = "https://loadtests.qa.kidozen.com";
        private final String user = "loadtests@kidozen.com";
        private final String pass = "pass";

        KZApplication mKidoApp;
        PubSubChannel mPS ;
        private final String TAG = this.getClass().getSimpleName();


        private ServiceEventListener mGetMessages = new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                Log.d(TAG,e.Body);
            }
        };

        public KidoZenHelper() {
            mKidoApp = new KZApplication(tenant,app,key,false);
        }

        public void Authenticate() throws Exception{
            mKidoApp.Authenticate("Kidozen",user,pass, new ServiceResponseListener() {
                @Override
                public void onSuccess(int statusCode, String response) {
                    try {
                        mStorage = mKidoApp.Storage("tasks");
                        JSONObject metadata = mStorage.Create(new JSONObject().put("a","b"));
                        Log.d(TAG,metadata.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (SynchronousException e) {
                        e.printStackTrace();
                    }

                }
            });

        }

        public void Subscribe() throws Exception {
            if (mPS==null) { mPS = mKidoApp.PubSubChannel("sampleapp"); }

            mPS.Subscribe(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    Log.d(TAG,e.Body);
                }
            });

            mPS.GetMessages(mGetMessages);
        }

        public void Publish() throws JSONException {
            JSONObject msg = new JSONObject().put("hello","world");
            mPS.Publish(msg,true,new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    Log.d(TAG,e.Body);
                }
            });
        }
    }
}
