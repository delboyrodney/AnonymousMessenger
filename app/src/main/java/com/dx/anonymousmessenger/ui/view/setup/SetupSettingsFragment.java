package com.dx.anonymousmessenger.ui.view.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dx.anonymousmessenger.DxApplication;
import com.dx.anonymousmessenger.R;
import com.dx.anonymousmessenger.db.DbHelper;
import com.dx.anonymousmessenger.ui.view.app.AppActivity;
import com.dx.anonymousmessenger.ui.view.single_activity.AboutActivity;
import com.dx.anonymousmessenger.ui.view.single_activity.LicenseActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupSettingsFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private boolean isInSetup;

    public SetupSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isInSetup boolean indicates whether the view is used within first time use setup or not.
     * @return A new instance of fragment SetupSettingsFragment.
     */
    public static SetupSettingsFragment newInstance(boolean isInSetup) {
        SetupSettingsFragment fragment = new SetupSettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isInSetup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isInSetup = getArguments().getBoolean(ARG_PARAM1);
        }
    }


    @Override
    public void onDestroyView() {
        ((DxApplication)requireActivity().getApplication()).reloadSettings();
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_setup_settings, container, false);

        final ConstraintLayout bridgesLayout = rootView.findViewById(R.id.bridge_settings_layout);
        final Button done = rootView.findViewById(R.id.done);
        final SwitchMaterial bridgesSwitch = rootView.findViewById(R.id.switch_bridges);
        final SwitchMaterial unknownContactsSwitch = rootView.findViewById(R.id.switch_allow_unknown);
        final SwitchMaterial allowCalls = rootView.findViewById(R.id.switch_allow_calls);
        final SwitchMaterial allowFilesSwitch = rootView.findViewById(R.id.switch_allow_files);
        final TextInputEditText txtFileLimit = rootView.findViewById(R.id.txt_file_limit);
        final TextInputLayout layoutFileLimit = rootView.findViewById(R.id.txt_layout_file_limit);
        final TextInputEditText txtCheckAddress = rootView.findViewById(R.id.txt_check_address);
        final RecyclerView rvBridges = rootView.findViewById(R.id.rv_bridges);
        final Button addBridge = rootView.findViewById(R.id.btn_add_bridge);
        final TextView reset = rootView.findViewById(R.id.btn_reset);
        rootView.findViewById(R.id.btn_request_bridge).setOnClickListener(v -> {
            TextView bridgeHelp = new TextView(requireContext());
            bridgeHelp.setText(R.string.request_bridge_help);
            bridgeHelp.setPadding(10,10,10,10);
            bridgeHelp.setTextIsSelectable(true);
            Linkify.addLinks(bridgeHelp, Linkify.ALL);
            View view = new View(requireContext());
            ArrayList<View>  viewArrayList = new ArrayList<>();
            viewArrayList.add(bridgeHelp);
            view.addChildrenForAccessibility(viewArrayList);
            AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext()).
                    setMessage(R.string.request_bridge).
                    setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).
                    setView(bridgeHelp);
            builder.create().show();
        });

        reset.setOnClickListener(v -> {
            Object[] settings = ((DxApplication)requireActivity().getApplication()).DEFAULT_SETTINGS;
            bridgesSwitch.setChecked(((int)settings[0]>0));
            bridgesLayout.setVisibility((int)settings[0]>0?View.VISIBLE:View.GONE);
            unknownContactsSwitch.setChecked(((int)settings[1]>0));
            allowCalls.setChecked((int)settings[2]>0);
            allowFilesSwitch.setChecked((int)settings[3]>0);
            layoutFileLimit.setVisibility((int)settings[3]>0?View.VISIBLE:View.GONE);
            txtCheckAddress.setText((String) settings[4]);
            txtFileLimit.setText((String) settings[5]);
        });

        //get default values
        if(isInSetup){
            bridgesSwitch.setChecked(((CreateUserActivity) requireActivity()).areBridgesEnabled());
            bridgesLayout.setVisibility(((CreateUserActivity) requireActivity()).areBridgesEnabled()?View.VISIBLE:View.GONE);
            allowCalls.setChecked(((CreateUserActivity) requireActivity()).isAcceptingCallsAllowed());
            unknownContactsSwitch.setChecked(((CreateUserActivity) requireActivity()).isAcceptingUnknownContactsEnabled());
            allowFilesSwitch.setChecked(((CreateUserActivity) requireActivity()).isReceivingFilesAllowed());
            layoutFileLimit.setVisibility(((CreateUserActivity) requireActivity()).isReceivingFilesAllowed()?View.VISIBLE:View.GONE);
            txtCheckAddress.setText(((CreateUserActivity) requireActivity()).getCheckAddress());
            txtFileLimit.setText(((CreateUserActivity) requireActivity()).getFileSizeLimit());
        }else{
            try{
                ((MaterialToolbar)requireActivity().findViewById(R.id.toolbar)).getMenu().clear();
                ((AppActivity)requireActivity()).setTitle(R.string.action_settings);
                ((AppActivity)requireActivity()).setBackEnabled(true);
            }catch (Exception ignored){}
            //set about & license text view buttons to be visible
            rootView.findViewById(R.id.txt_other).setVisibility(View.VISIBLE);
            TextView license = rootView.findViewById(R.id.btn_license);
            license.setVisibility(View.VISIBLE);
            license.setOnClickListener((v)->{
                try{
                    Intent intent = new Intent(getContext(), LicenseActivity.class);
                    if(getContext()!=null){
                        getContext().startActivity(intent);
                    }
                }catch (Exception ignored) {}
            });
            TextView about = rootView.findViewById(R.id.btn_about);
            about.setVisibility(View.VISIBLE);
            about.setOnClickListener((v)->{
                try{
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    if(getContext()!=null){
                        getContext().startActivity(intent);
                    }
                }catch (Exception ignored) {}
            });
            //read from db in this case or fallback to defaults
            try{
                Object[] settings = DbHelper.getSettingsList((DxApplication)requireActivity().getApplication());
                if (settings == null || settings.length <= 5) {
                    settings = ((DxApplication) requireActivity().getApplication()).DEFAULT_SETTINGS;
                }
                bridgesSwitch.setChecked(((int)settings[0]>0));
                bridgesLayout.setVisibility((int)settings[0]>0?View.VISIBLE:View.GONE);
                unknownContactsSwitch.setChecked(((int)settings[1]>0));
                allowCalls.setChecked((int)settings[2]>0);
                allowFilesSwitch.setChecked((int)settings[3]>0);
                layoutFileLimit.setVisibility((int)settings[3]>0?View.VISIBLE:View.GONE);
                txtCheckAddress.setText((String) settings[4]);
                txtFileLimit.setText((String) settings[5]);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // bridge related
        bridgesSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isInSetup){
                ((CreateUserActivity) requireActivity()).setBridgesEnabled(isChecked);
            }else{
                DbHelper.saveEnableBridges(isChecked,(DxApplication)requireActivity().getApplication());
            }
            bridgesLayout.setVisibility(isChecked?View.VISIBLE:View.GONE);
        }));

        addBridge.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Insert bridge line");

            // Set up the input
            final EditText input = new EditText(v.getContext());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                ((InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
                if(isInSetup){
                    List<String> list = ((CreateUserActivity) requireActivity()).getBridgeList();
                    list.add(input.getText().toString());
                    ((CreateUserActivity) requireActivity()).setBridgeList(list);
                }else{
                    DbHelper.saveBridge(input.getText().toString(),(DxApplication) requireActivity().getApplication());
                }
                updateBridgeList();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                ((InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
                dialog.cancel();
            });
            builder.show();
        });

        //other switches
        allowCalls.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isInSetup){
                ((CreateUserActivity) requireActivity()).setAcceptingCallsAllowed(isChecked);
            }else{
                DbHelper.saveIsAcceptingCallsAllowed(isChecked,(DxApplication)requireActivity().getApplication());
            }
        }));

        unknownContactsSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isInSetup){
                ((CreateUserActivity) requireActivity()).setAcceptingUnknownContactsEnabled(isChecked);
            }else{
                DbHelper.saveIsAcceptingUnknownContactsEnabled(isChecked,(DxApplication)requireActivity().getApplication());
            }
        }));

        allowFilesSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isInSetup){
                ((CreateUserActivity) requireActivity()).setReceivingFilesAllowed(isChecked);
            }else{
                DbHelper.saveIsReceivingFilesAllowed(isChecked,(DxApplication)requireActivity().getApplication());
            }
            layoutFileLimit.setVisibility(isChecked?View.VISIBLE:View.GONE);
            if (!isChecked) {
                ((InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
            }
        }));

        txtFileLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //validate and output errors
                //accepted format is : 0-999[gb/mb/kb]
                if(!s.toString().trim().endsWith("gb") && !s.toString().trim().endsWith("mb") && !s.toString().trim().endsWith("kb") && !s.toString().trim().endsWith("GB") && !s.toString().trim().endsWith("MB") && !s.toString().trim().endsWith("KB")){
                    txtFileLimit.setError("Bad format, setting unchanged");
                    return;
                }
                try{
                    String substring = s.toString().trim().substring(0, s.toString().length() - 2).trim();
                    if(substring.length()>3 || substring.length()<1 || Integer.parseInt(substring)<1){
                        txtFileLimit.setError("Bad format, setting unchanged");
                        return;
                    }
                }catch (NumberFormatException ignored){
                    txtFileLimit.setError("Bad format, setting unchanged");
                    return;
                }
                //if good: save it
                if(isInSetup){
                    ((CreateUserActivity) requireActivity()).setFileSizeLimit(s.toString());
                }else{
                    DbHelper.saveFileSizeLimit(s.toString(),(DxApplication)requireActivity().getApplication());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtCheckAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //validate and output errors
                if(s.toString().contains(":") || s.toString().contains("//")){
                    txtCheckAddress.setError("Bad address, setting unchanged");
                    return;
                }
                if(isInSetup){
                    ((CreateUserActivity) requireActivity()).setCheckAddress(s.toString());
                }else{
                    DbHelper.saveCheckAddress(s.toString(),(DxApplication)requireActivity().getApplication());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        List<String> list;
        if(isInSetup){
            list = ((CreateUserActivity) requireActivity()).getBridgeList();

            done.setText(R.string.next_button);
            done.setOnClickListener(v -> ((CreateUserActivity) requireActivity()).changeToPasswordFragment());
        }else{
            list = DbHelper.getBridgeList((DxApplication) requireActivity().getApplication());

            done.setVisibility(View.GONE);
//            done.setText(R.string.done_button);
//            done.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity());
        rvBridges.setLayoutManager(layoutManager);
        BridgeRecyclerViewAdapter adapter = new BridgeRecyclerViewAdapter(this.getActivity(), list);
        rvBridges.setAdapter(adapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateBridgeList(){
        new Thread(()->{
            List<String> list;
            if(isInSetup){
                list = ((CreateUserActivity) requireActivity()).getBridgeList();
            }else{
                list = DbHelper.getBridgeList((DxApplication) requireActivity().getApplication());
            }
            requireActivity().runOnUiThread(()->{
                RecyclerView recyclerView = requireActivity().findViewById(R.id.rv_bridges);
                LinearLayoutManager layoutManager
                        = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(layoutManager);
                BridgeRecyclerViewAdapter adapter = new BridgeRecyclerViewAdapter(getActivity(), list);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(list.size()-1);
            });
        }).start();
    }
}