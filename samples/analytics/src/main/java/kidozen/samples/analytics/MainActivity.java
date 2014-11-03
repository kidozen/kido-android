package kidozen.samples.analytics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import kidozen.client.InitializationException;


public class MainActivity extends Activity implements IAuthenticationEvents {
    private KidoZenHelper helper ;
    MainActivity mSelf = this;
    TextView textView;
    Button signIn , signOut, newActivity, tagClick, tagCustomEvent;

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
                helper.TagClick("hello");
            }
        });

        tagCustomEvent = (Button) findViewById(R.id.buttonTagCustomEvent);
        tagCustomEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.TagCustom("custom");
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
    public void ReturnUserName(String username) {
        textView.setText( "Hello: " + username );
    }


}
