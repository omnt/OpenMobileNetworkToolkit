package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;


import java.util.UUID;
import java.util.function.Consumer;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Fragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private ProgressBar progressBar;
    private Iperf3Input iperf3Input;
    private Context ct;
    private MaterialButton sendBtn;
    private View view;
    //todo start iperf3 as a service
    private TextInputEditText ip;
    private TextInputEditText port;
    private TextInputEditText bitrate;
    private TextInputEditText duration;
    private TextInputEditText interval;
    private TextInputEditText bytes;
    private TextInputEditText streams;
    private TextInputEditText cport;


    private MaterialButtonToggleGroup mode;
    private MaterialButtonToggleGroup protocol;
    private MaterialButtonToggleGroup direction;

    private MaterialButton modeClient;
    private MaterialButton modeServer;
    private MaterialButton protocolTCP;
    private MaterialButton protocolUDP;
    private MaterialButton directionUp;
    private MaterialButton directionDown;
    private MaterialButton directonBidir;

    private SharedPreferencesGrouper spg;

    private FrameLayout frameLayout;
    private String TAG = "Iperf3CardFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ct = requireContext();
    }

    /**
     * Create a text watcher
     * @param consumer
     * @param name
     * @return
     */
    private TextWatcher createTextWatcher(Consumer<String> consumer, String name) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                consumer.accept(charSequence.toString());
                spg.getSharedPreference(SPType.iperf3_sp).edit().putString(name, charSequence.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    /**
     * Set up the text watchers
     */
    private void setupTextWatchers() {
        ip.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setHost(s), Iperf3Parameter.HOST));
        port.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setPort(Integer.parseInt("0"+s)), Iperf3Parameter.PORT));
        bitrate.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setBandwidth(s), Iperf3Parameter.BITRATE));
        duration.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setTime(Integer.parseInt("0"+s)), Iperf3Parameter.TIME));
        interval.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setInterval(Integer.parseInt("0"+s)), Iperf3Parameter.INTERVAL));
        bytes.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setBytes(s), Iperf3Parameter.BYTES));
        streams.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setNstreams(Integer.parseInt("0"+s)), Iperf3Parameter.STREAMS));
        cport.addTextChangedListener(createTextWatcher(s -> iperf3Input.getParameter().setCport(Integer.parseInt("0"+s) ), Iperf3Parameter.CPORT));
    }

    /**
     * Set the text from the shared preferences
     * @param editText
     * @param key
     */
    private void setTextFromSharedPreferences(TextInputEditText editText, String key) {
        if (spg.getSharedPreference(SPType.iperf3_sp).contains(key)) {
            editText.setText(spg.getSharedPreference(SPType.iperf3_sp).getString(key, ""));
        }
    }

    /**
     * Set the texts from the shared preferences
     */
    private void setTextsFromSharedPreferences(){
        setTextFromSharedPreferences(ip, Iperf3Parameter.HOST);
        setTextFromSharedPreferences(port, Iperf3Parameter.PORT);
        setTextFromSharedPreferences(bitrate, Iperf3Parameter.BITRATE);
        setTextFromSharedPreferences(duration, Iperf3Parameter.TIME);
        setTextFromSharedPreferences(interval, Iperf3Parameter.INTERVAL);
        setTextFromSharedPreferences(bytes, Iperf3Parameter.BYTES);
        setTextFromSharedPreferences(streams, Iperf3Parameter.STREAMS);
        setTextFromSharedPreferences(cport, Iperf3Parameter.CPORT);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_iperf3_input, container, false);
        // Initialize the TextView
        progressBar = view.findViewById(R.id.iperf3_progress);
        progressBar.setVisibility(View.INVISIBLE);
        String iperf3UUID = UUID.randomUUID().toString();
        Iperf3Parameter iperf3Parameter = new Iperf3Parameter(iperf3UUID);
        iperf3Input = new Iperf3Input(iperf3Parameter);
        sendBtn = view.findViewById(R.id.iperf3_send);
        spg = SharedPreferencesGrouper.getInstance(ct);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.standard_bottom_sheet));
        bottomSheetBehavior.setPeekHeight(20);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);
        ip = view.findViewById(R.id.iperf3_ip);
        port = view.findViewById(R.id.iperf3_port);
        bitrate = view.findViewById(R.id.iperf3_bandwidth);
        duration = view.findViewById(R.id.iperf3_duration);
        interval = view.findViewById(R.id.iperf3_interval);
        bytes = view.findViewById(R.id.iperf3_bytes);
        streams = view.findViewById(R.id.iperf3_streams);
        cport = view.findViewById(R.id.iperf3_cport);


        mode = view.findViewById(R.id.iperf3_mode_toggle_group);
        protocol = view.findViewById(R.id.iperf3_protocol_toggle_group);
        direction = view.findViewById(R.id.iperf3_direction_toggle_group);

        modeClient = view.findViewById(R.id.iperf3_client_button);
        modeServer = view.findViewById(R.id.iperf3_server_button);

        protocolTCP = view.findViewById(R.id.iperf3_tcp_button);
        protocolUDP = view.findViewById(R.id.iperf3_udp_button);

        directionDown = view.findViewById(R.id.iperf3_download_button);
        directionUp = view.findViewById(R.id.iperf3_upload_button);
        directonBidir = view.findViewById(R.id.iperf3_bidir_button);

        setupTextWatchers();
        setTextsFromSharedPreferences();

        try {
            switch (Iperf3Parameter.Iperf3Mode.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Parameter.MODE, String.valueOf(Iperf3Parameter.Iperf3Mode.UNDEFINED)))){
                case CLIENT:
                    updateModeState(modeClient, modeServer, Iperf3Parameter.Iperf3Mode.CLIENT);
                    break;
                case SERVER:
                    updateModeState(modeServer, modeClient, Iperf3Parameter.Iperf3Mode.SERVER);
                    break;
                case UNDEFINED:
                default:
                    modeClient.setBackgroundColor(Color.TRANSPARENT);
                    modeServer.setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.MODE, Iperf3Parameter.Iperf3Mode.UNDEFINED.toString()).apply();
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        try {
            switch (Iperf3Parameter.Iperf3Protocol.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.UNDEFINED.toString()))){
                case TCP:
                    updateProtocolState(protocolTCP, protocolUDP, Iperf3Parameter.Iperf3Protocol.TCP);
                    break;
                case UDP:
                    updateProtocolState(protocolUDP, protocolTCP, Iperf3Parameter.Iperf3Protocol.UDP);
                    break;
                case UNDEFINED:
                default:
                    protocolTCP.setBackgroundColor(Color.TRANSPARENT);
                    protocolUDP.setBackgroundColor(Color.TRANSPARENT);
                    spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.PROTOCOL, Iperf3Parameter.Iperf3Protocol.UNDEFINED.toString()).apply();
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        try {
            switch (Iperf3Parameter.Iperf3Direction.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Parameter.DIRECTION, Iperf3Parameter.Iperf3Direction.UNDEFINED.toString()))) {
                case UP:
                    updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Parameter.Iperf3Direction.UP);
                    break;
                case DOWN:
                    updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Parameter.Iperf3Direction.DOWN);
                    break;
                case BIDIR:
                    updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Parameter.Iperf3Direction.BIDIR);
                    break;
                case UNDEFINED:
                default:
                    directionUp.setBackgroundColor(Color.TRANSPARENT);
                    directionDown.setBackgroundColor(Color.TRANSPARENT);
                    directonBidir.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }

        mode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.iperf3_client_button:
                            updateModeState(modeClient, modeServer, Iperf3Parameter.Iperf3Mode.CLIENT);
                            break;
                        case R.id.iperf3_server_button:
                            updateModeState(modeServer, modeClient, Iperf3Parameter.Iperf3Mode.SERVER);
                            break;
                    }
                }
            }
        });
        protocol.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.iperf3_tcp_button:
                            updateProtocolState(protocolTCP, protocolUDP, Iperf3Parameter.Iperf3Protocol.TCP);
                            break;
                        case R.id.iperf3_udp_button:
                            updateProtocolState(protocolUDP, protocolTCP, Iperf3Parameter.Iperf3Protocol.UDP);
                            break;
                    }
                }
            }
        });
        direction.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.iperf3_upload_button:
                            updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Parameter.Iperf3Direction.UP);
                            break;
                        case R.id.iperf3_download_button:
                            updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Parameter.Iperf3Direction.DOWN);
                            break;
                        case R.id.iperf3_bidir_button:
                            updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Parameter.Iperf3Direction.BIDIR);
                            break;
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        view.requestLayout();
    }



    private void updateModeState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Parameter.Iperf3Mode protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setMode(protocol);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.MODE, protocol.toString()).apply();
    }

    private void updateProtocolState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Parameter.Iperf3Protocol protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setProtocol(protocol);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.PROTOCOL, protocol.toString()).apply();
    }

    private void updateDirectionState(MaterialButton activeButton, MaterialButton inactiveButton1, MaterialButton inactiveButton2, Iperf3Parameter.Iperf3Direction direction) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton1.setBackgroundColor(Color.TRANSPARENT);
        inactiveButton2.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.getParameter().setDirection(direction);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Parameter.DIRECTION, direction.toString()).apply();
    }
}