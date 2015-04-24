package com.ymgeva.doui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.ymgeva.doui.R;
import com.ymgeva.doui.tasks.TaskListActivity;


public class LoginActivity extends ActionBarActivity implements LoginFragement.LoginFragmentInteractionListener,
        SignUpFragment.SignUpFragmentInteractionListener,ConnectToPartnerFragment.ConnectToPartnerFragmentInteractionListener {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragement())
                    .commit();
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
    public void onLoginClicked(String email, String password) {
        ParseUser.logInInBackground(email,password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    goToMainScreen();
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
    public void onCreateAccountClicked(String email, String password, String passwordConfirmed, String name) {
        ParseUser user = new ParseUser();
        user.setUsername(email);
        user.setPassword(password);
        user.setEmail(email);
        user.put("name",name);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    showConnectToPartnerScreen();
                }
                else {
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showConnectToPartnerScreen() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new ConnectToPartnerFragment())
                .commit();
    }

    @Override
    public void onConnectClicked(final String email, final String sharedPassword) {

        final ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("partner_email",email);
        currentUser.put("shared_password",sharedPassword);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("email",email);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser partnerUser, ParseException e) {
                boolean success = false;
                if (e != null) {
                    //do nifty stuff
                }
                else if (partnerUser == null) {
                    Toast.makeText(getApplicationContext(),R.string.email_sent_to_partner,Toast.LENGTH_LONG);
                }
                else if (partnerUser.get("partner") != null) {
                    Toast.makeText(getApplicationContext(),R.string.partner_already_connected,Toast.LENGTH_LONG);
                }
                else {
                    String partnerEmail = (String) partnerUser.get("partner_email");
                    String partnerSharedPassword = (String)partnerUser.get("shared_password");
                    if (partnerEmail == null || partnerSharedPassword == null) {
                        Toast.makeText(getApplicationContext(),R.string.email_sent_to_partner,Toast.LENGTH_LONG);
                    }
                    else if (partnerEmail.contentEquals(email) && partnerSharedPassword.contentEquals(partnerSharedPassword)) {
                        currentUser.put("partner",partnerUser);
                        currentUser.put("partnerId",partnerUser.getObjectId());
                        partnerUser.put("partner", currentUser);
                        partnerUser.put("partnerId",currentUser.getObjectId());
                        saveUser(partnerUser);
                        success = true;
                        Toast.makeText(getApplicationContext(),R.string.conneced_to+(String)partnerUser.get("name"),Toast.LENGTH_LONG);

                    }
                    else {
                        Toast.makeText(getApplicationContext(),R.string.partner_not_matching,Toast.LENGTH_LONG);
                    }
                }
                saveUser(currentUser);
                if (success) {
                    goToMainScreen();
                }
            }
        });

    }

    @Override
    public void onSkipClicked() {
        goToMainScreen();
    }

    private void saveUser(ParseUser user){
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
    }

    private void goToMainScreen() {
        Intent intent = new Intent(this,TaskListActivity.class);
        startActivity(intent);
        finish();
    }
}
