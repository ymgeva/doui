package com.ymgeva.doui.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ymgeva.doui.R;

public class LoginFragement extends android.support.v4.app.Fragment {


    private LoginFragmentInteractionListener mListener;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private Button mSignUpButton;

    public LoginFragement() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.com_parse_ui_parse_login_fragment, container, false);

        mEmailField = (EditText) rootView.findViewById(R.id.login_username_input);
        mPasswordField = (EditText) rootView.findViewById(R.id.login_password_input);

        mLoginButton = (Button) rootView.findViewById(R.id.parse_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoginClicked(mEmailField.getText().toString(),mPasswordField.getText().toString());
            }
        });

        mSignUpButton = (Button) rootView.findViewById(R.id.parse_signup_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSignUpClicked(mEmailField.getText().toString(),mPasswordField.getText().toString());
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LoginFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface LoginFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onLoginClicked(String email,String password);
        public void onSignUpClicked(String email,String password);
    }

}
