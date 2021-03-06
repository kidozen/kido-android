package kidozen.samples.analytics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import kidozen.client.InitializationException;


public class MainActivity extends Activity implements IAuthenticationEvents {
    private KidoZenHelper helper ;
    MainActivity mSelf = this;
    TextView textView;
    Button signIn , signOut, newActivity, tagClick, tagCustomEvent;
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper= new KidoZenHelper(this);
        helper.setAuthEvents(this);

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

        newActivity = (Button) findViewById(R.id.buttonLaunchNewActivity);
        newActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mSelf, MyNewActivity.class);
                startActivity(intent);
            }
        });


        tagClick = (Button) findViewById(R.id.buttonTagClick);
        tagClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.TagClick("Tag Click Button Touched");
            }
        });

        tagCustomEvent = (Button) findViewById(R.id.buttonTagCustomEvent);
        tagCustomEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.TagCustom("Tag Custom Event Button Touched");
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (exit) {
            helper.StopAnalytics();
            MainActivity.this.finish();
        }
        else {
            Toast.makeText(this, "Press Back again to Exit.",Toast.LENGTH_SHORT).show();
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
        textView.setText( "Hello: " + username );
    }


}
