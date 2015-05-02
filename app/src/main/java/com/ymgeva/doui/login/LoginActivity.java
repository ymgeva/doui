package com.ymgeva.doui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.ymgeva.doui.R;
import com.ymgeva.doui.parse.DoUIParseSyncAdapter;
import com.ymgeva.doui.parse.DoUIPushBroadcastReceiver;
import com.ymgeva.doui.sync.DoUISyncAdapter;
import com.ymgeva.doui.tasks.TaskListActivity;

import java.util.HashMap;
import java.util.List;


public class LoginActivity extends ActionBarActivity implements LoginFragement.LoginFragmentInteractionListener,
        SignUpFragment.SignUpFragmentInteractionListener,ConnectToPartnerFragment.ConnectToPartnerFragmentInteractionListener {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private ParseUser mPartner;

    public static String LOGIN_MODE = "loginMode";
    public static int LOGIN_MODE_NEW_USER = 100;
    public static int LOGIN_MODE_CONNECT_TO_PARTNER = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            int loginMode = getIntent().getIntExtra(LOGIN_MODE,LOGIN_MODE_NEW_USER);
            if (loginMode == LOGIN_MODE_NEW_USER) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new LoginFragement())
                        .commit();
            }
            else {
                showConnectToPartnerScreen(ParseUser.getCurrentUser().getEmail());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginClicked(final String email, String password) {
        ParseUser.logInInBackground(email,password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    DoUIParseSyncAdapter.updateInstallation();
                    if (user.getString(DoUIParseSyncAdapter.PARTNER_ID) != null) {
                        goToMainScreen();
                    }
                    else {
                        showConnectToPartnerScreen(email);
                    }
                } else {
                    Log.e(LOG_TAG,"Login failed with exception: "+e.toString());
                }
            }
        });
    }

    @Override
    public void onSignUpClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new SignUpFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCreateAccountClicked(final String email, String password, String passwordConfirmed, String name) {
        ParseUser user = new ParseUser();
        user.setUsername(email.toLowerCase());
        user.setPassword(password);
        user.setEmail(email.toLowerCase());
        user.put("name",name);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    DoUIParseSyncAdapter.updateInstallation();

                    showConnectToPartnerScreen(email.toLowerCase());
                }
                else {
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showConnectToPartnerScreen(String email) {

        ConnectToPartnerFragment fragment = new ConnectToPartnerFragment();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ConnectToPartnerFragment.PARTNER_EMAIL,email);
        try {
            List<ParseUser> users = query.find();
            if (users != null && users.size() > 0) {
                mPartner = users.get(0);
                Bundle bundle = new Bundle();
                bundle.putString(ConnectToPartnerFragment.PARTNER_EMAIL,mPartner.getEmail());
                bundle.putString(ConnectToPartnerFragment.PARTNER_NAME,mPartner.getString("name"));
                bundle.putString(ConnectToPartnerFragment.SHARED_PASSWORD,mPartner.getString(ConnectToPartnerFragment.SHARED_PASSWORD));
                fragment.setArguments(bundle);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }



    @Override
    public void onSkipClicked() {
        goToMainScreen();
    }

    @Override
    public void connectPartners() {
        ParseUser me = ParseUser.getCurrentUser();
        me.put(ConnectToPartnerFragment.PARTNER_EMAIL,mPartner.getEmail());
        me.put(ConnectToPartnerFragment.SHARED_PASSWORD,mPartner.getString(ConnectToPartnerFragment.SHARED_PASSWORD));
        me.put(DoUIParseSyncAdapter.PARTNER_ID, mPartner.getObjectId());
        me.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });

        DoUIParseSyncAdapter.sendPush(DoUIPushBroadcastReceiver.PUSH_CODE_UPDATE_PARTNER,mPartner.getObjectId(),me.getObjectId());

        goToMainScreen();
    }

    @Override
    public void sendPasswordToPartner(String email, String password) {
        ParseUser me = ParseUser.getCurrentUser();
        me.put(ConnectToPartnerFragment.PARTNER_EMAIL,email.toLowerCase());
        me.put(ConnectToPartnerFragment.SHARED_PASSWORD,password);
        me.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });

        HashMap<String,Object> map = new HashMap<>();
        map.put("to",email);
        map.put("from",me.getEmail());
        map.put("name",me.getString("name"));
        map.put("shared_password",password);
        ParseCloud.callFunctionInBackground("SendEmail", map, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
        goToMainScreen();
    }

    private void goToMainScreen() {
        Intent intent = new Intent(this,TaskListActivity.class);
        startActivity(intent);
        finish();
    }
}
