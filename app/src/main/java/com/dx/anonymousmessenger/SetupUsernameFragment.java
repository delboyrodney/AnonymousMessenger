package com.dx.anonymousmessenger;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dx.anonymousmessenger.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import static android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING;

public class SetupUsernameFragment extends Fragment {
    private String error;

    public SetupUsernameFragment() {
        // Required empty public constructor
    }

    public static SetupUsernameFragment newInstance() {
        SetupUsernameFragment fragment = new SetupUsernameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_setup_username, container, false);
        final TextInputEditText txtNickname = rootView.findViewById(R.id.txt_nickname);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtNickname.setImeOptions(IME_FLAG_NO_PERSONALIZED_LEARNING);
        }
        txtNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>1&&s.length()<35){
                    Button next = Objects.requireNonNull(getView()).findViewById(R.id.next);
                    next.setEnabled(true);
                    error = getString(R.string.nickname_okay);
                    TextView errorview = getView().findViewById(R.id.txt_error);
                    errorview.setText(error);
                }else {
                    Button next = Objects.requireNonNull(getView()).findViewById(R.id.next);
                    next.setEnabled(false);
                    error = getString(R.string.nickname_error);
                    TextView errorview = getView().findViewById(R.id.txt_error);
                    errorview.setText(error);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final Button next = rootView.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            if(Objects.requireNonNull(txtNickname.getText()).length()>1){
                next.setEnabled(false);
                txtNickname.setEnabled(false);
                new Thread(()->{
                    ((CreateUserActivity) Objects.requireNonNull(getActivity())).setNickname(txtNickname.getText().toString());
                    ((CreateUserActivity)getActivity()).changeToPasswordActivity();
                }).start();
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }
}