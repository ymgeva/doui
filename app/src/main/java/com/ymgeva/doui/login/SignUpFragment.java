package com.ymgeva.doui.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ymgeva.doui.R;

public class SignUpFragment extends Fragment {


    private SignUpFragmentInteractionListener mListener;

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private EditText mNameField;
    private Button mCreateAccountButton;

    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance(String email, String password) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        if (email != null) {
            args.putString(EMAIL,email);
        }

        if (password != null) {
            args.putString(PASSWORD,password);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.com_parse_ui_parse_signup_fragment, container, false);
        Bundle args = getArguments();

        mEmailField = (EditText) rootView.findViewById(R.id.signup_email_input);
        mEmailField.setText(args.getString(EMAIL,""));
        mPasswordField = (EditText) rootView.findViewById(R.id.signup_password_input);
        mPasswordField.setText(args.getString(PASSWORD,""));
        mPasswordConfirmField = (EditText) rootView.findViewById(R.id.signup_confirm_password_input);
        mNameField = (EditText) rootView.findViewById(R.id.signup_name_input);

        mCreateAccountButton = (Button)rootView.findViewById(R.id.create_account);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCreateAccountClicked(mEmailField.getText().toString(),
                        mPasswordField.getText().toString(),mPasswordConfirmField.getText().toString(),mNameField.getText().toString());
            }
        });



        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SignUpFragmentInteractionListener) activity;
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


    public interface SignUpFragmentInteractionListener {
        public void onCreateAccountClicked(String email,String password,String passwordConfirmed,String name);
    }

}
