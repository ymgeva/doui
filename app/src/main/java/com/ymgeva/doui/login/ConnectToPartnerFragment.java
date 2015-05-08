package com.ymgeva.doui.login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
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
    public static final String NEW_PARTNER = "new_partner";

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


    public static ConnectToPartnerFragment newInstance(String partnerName,String partnerEmail, String sharedPassword) {

        Bundle args = new Bundle();
        args.putString(PARTNER_NAME,partnerName);
        args.putString(PARTNER_EMAIL,partnerEmail);
        args.putString(SHARED_PASSWORD,sharedPassword);
        args.putBoolean(NEW_PARTNER,true);

        ConnectToPartnerFragment fragment = new ConnectToPartnerFragment();
        fragment.setArguments(args);
        return fragment;

    }

    public static ConnectToPartnerFragment newInstance() {
        Bundle args = new Bundle();
        args.putBoolean(NEW_PARTNER,false);

        ConnectToPartnerFragment fragment = new ConnectToPartnerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_connect_to_partner, container, false);


        mExplanationView = (TextView) rootView.findViewById(R.id.connect_to_partner_explanation);
        Bundle bundle = this.getArguments();

        isPartnerPending = bundle.getBoolean(NEW_PARTNER,false);

        mPartnerName = bundle != null ? bundle.getString(PARTNER_NAME) : null;
        if (mPartnerName != null) {
            mPartnerEmail = bundle.getString(PARTNER_EMAIL);
            mSharedPassword = bundle.getString(SHARED_PASSWORD);

            mExplanationView.setText(getActivity().getString(R.string.enter_password_from_mail,mPartnerName));
        }
        else {
            String styledText = getActivity().getString(R.string.enter_password_for_partner1) +
                    "<br />" +
                    getActivity().getString(R.string.enter_password_for_partner2);
            mExplanationView.setText(Html.fromHtml(styledText));
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
