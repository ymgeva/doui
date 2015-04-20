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

public class ConnectToPartnerFragment extends Fragment {

    private ConnectToPartnerFragmentInteractionListener mListener;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mConnectButton;
    private Button mSkipButton;

    public ConnectToPartnerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_connect_to_partner, container, false);

        mEmailField = (EditText) rootView.findViewById(R.id.partner_email);
        mPasswordField = (EditText) rootView.findViewById(R.id.shared_password);
        mConnectButton = (Button) rootView.findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onConnectClicked(mEmailField.getText().toString(),mPasswordField.getText().toString());
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
        public void onConnectClicked(String email,String sharedPassword);
        public void onSkipClicked();
    }

}
