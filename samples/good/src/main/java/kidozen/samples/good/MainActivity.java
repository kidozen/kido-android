package kidozen.samples.good;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.good.gd.GDAndroid;

import kidozen.client.InitializationException;


public class MainActivity extends Activity implements IAuthenticationEvents {
    private KidoZenHelper helper = new KidoZenHelper();
    MainActivity mSelf = this;
    TextView textView;
    Button signIn , signOut, newActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        newActivity = (Button) findViewById(R.id.buttonNewActivity);
        newActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mSelf, MyNewActivity.class);
                startActivity(intent);
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
