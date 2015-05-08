package com.ymgeva.doui.login;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.ymgeva.doui.MainActivity;
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

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.logging_in));
        progress.show();

        try {
            ParseUser.logIn(email,password);
            ParseUser me = ParseUser.getCurrentUser();
            if (me != null) {
                DoUIParseSyncAdapter.updateInstallation();
                if (me.getString(DoUIParseSyncAdapter.PARTNER_ID) != null) {
                    goToMainScreen();
                }
                else {
                    showConnectToPartnerScreen(email);
                }
            }
            else {
                Toast.makeText(this,R.string.login_failed,Toast.LENGTH_LONG).show();
            }

        } catch (ParseException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        finally {
            progress.dismiss();
        }
    }

    @Override
    public void onSignUpClicked(String email,String password) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, SignUpFragment.newInstance(email,password))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCreateAccountClicked(final String email, String password, String passwordConfirmed, String name) {
        final ParseUser user = new ParseUser();
        user.setUsername(email.toLowerCase());
        user.setPassword(password);
        user.setEmail(email.toLowerCase());
        user.put("name",name);

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.signing_up));
        progress.show();

        try {
            user.signUp();
            ParseUser.logIn(email,password);
            DoUIParseSyncAdapter.updateInstallation();
            showConnectToPartnerScreen(email.toLowerCase());
        }
        catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

        }
        finally {
            progress.dismiss();
        }
    }

    private void showConnectToPartnerScreen(String email) {

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ConnectToPartnerFragment.PARTNER_EMAIL,email);
        try {
            List<ParseUser> users = query.find();
            ConnectToPartnerFragment fragment;
            if (users != null && users.size() > 0) {
                mPartner = users.get(0);
                fragment = ConnectToPartnerFragment.newInstance(mPartner.getString("name"),mPartner.getEmail(),mPartner.getString(ConnectToPartnerFragment.SHARED_PASSWORD));

            }
            else {
                fragment = ConnectToPartnerFragment.newInstance();
            }
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }



    @Override
    public void onSkipClicked() {
        new AlertDialog.Builder(this).
                setMessage(R.string.connect_to_partner_skipped).
                setCancelable(false).
                setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        goToMainScreen();
                    }
                }).show();
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
        DoUISyncAdapter.onAccountCreated(getApplicationContext());
        Intent intent = new Intent(this,TaskListActivity.class);
        startActivity(intent);
        finish();
    }
}
