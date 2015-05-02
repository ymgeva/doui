package com.ymgeva.doui.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ymgeva.doui.R;

public class ConnectToPartnerFragment extends Fragment {

    public static final String PARTNER_NAME = "partner_name";
    public static final String PARTNER_EMAIL = "partner_email";
    public static final String SHARED_PASSWORD = "shared_password";

    private ConnectToPartnerFragmentInteractionListener mListener;

    private TextView mExplanationView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mConnectButton;
    private Button mSkipButton;

    private boolean isPartnerPending;
    private String mPartnerName;
    private String mPartnerEmail;
    private String mSharedPassword;

    public ConnectToPartnerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_connect_to_partner, container, false);


        mExplanationView = (TextView) rootView.findViewById(R.id.connect_to_partner_explanation);
        Bundle bundle = this.getArguments();
        mPartnerName = bundle != null ? bundle.getString(PARTNER_NAME) : null;
        if (mPartnerName != null) {
            isPartnerPending = true;
            mPartnerEmail = bundle.getString(PARTNER_EMAIL);
            mSharedPassword = bundle.getString(SHARED_PASSWORD);

            mExplanationView.setText(getActivity().getString(R.string.enter_password_from_mail,mPartnerName));
        }
        else {
            isPartnerPending = false;
            mExplanationView.setText(getActivity().getString(R.string.enter_password_for_partner));
        }

        mEmailField = (EditText) rootView.findViewById(R.id.partner_email);
        mPasswordField = (EditText) rootView.findViewById(R.id.shared_password);
        mConnectButton = (Button) rootView.findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                if (email == null || password == null) {
                    Toast.makeText(getActivity(),getString(R.string.connect_to_partner_fields_empty_warning),Toast.LENGTH_LONG).show();
                    return;
                }

                if (isPartnerPending) {
                    if (email.equals(mPartnerEmail) && password.equals(mSharedPassword)) {
                        mListener.connectPartners();
                    }
                    else {
                        Toast.makeText(getActivity(),getString(R.string.connect_to_partner_details_dont_match,mPartnerName),Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    mListener.sendPasswordToPartner(email, password);
                }
            }
        });
        mSkipButton = (Button) rootView.findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSkipClicked();
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ConnectToPartnerFragmentInteractionListener) activity;
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

    public interface ConnectToPartnerFragmentInteractionListener {
        public void onSkipClicked();
        public void connectPartners();
        public void sendPasswordToPartner(String email, String password);
    }

}
