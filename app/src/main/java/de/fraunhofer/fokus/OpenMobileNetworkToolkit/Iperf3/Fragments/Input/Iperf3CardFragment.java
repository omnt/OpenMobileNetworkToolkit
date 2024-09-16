package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;


import java.util.function.Consumer;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Service;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3CardFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private ProgressBar progressBar;
    private Iperf3Input iperf3Input;
    private Context ct;
    private MaterialButton sendBtn;
    //todo start iperf3 as a service
    private TextInputEditText ip;
    private TextInputEditText port;
    private TextInputEditText bandwidth;
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
    private String TAG = "Iperf3CardFragment";


    public static Iperf3CardFragment newInstance(int position) {
        Iperf3CardFragment fragment = new Iperf3CardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

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
        ip.addTextChangedListener(createTextWatcher(iperf3Input::setIp, Iperf3Input.IPERF3IP));
        port.addTextChangedListener(createTextWatcher(iperf3Input::setPort, Iperf3Input.IPERF3PORT));
        bandwidth.addTextChangedListener(createTextWatcher(iperf3Input::setBandwidth, Iperf3Input.IPERF3BANDWIDTH));
        duration.addTextChangedListener(createTextWatcher(iperf3Input::setDuration, Iperf3Input.IPERF3DURATION));
        interval.addTextChangedListener(createTextWatcher(iperf3Input::setInterval, Iperf3Input.IPERF3INTERVAL));
        bytes.addTextChangedListener(createTextWatcher(iperf3Input::setBytes, Iperf3Input.IPERF3BYTES));
        streams.addTextChangedListener(createTextWatcher(iperf3Input::setStreams, Iperf3Input.IPERF3STREAMS));
        cport.addTextChangedListener(createTextWatcher(iperf3Input::setCport, Iperf3Input.IPERF3CPORT));
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
        setTextFromSharedPreferences(ip, Iperf3Input.IPERF3IP);
        setTextFromSharedPreferences(port, Iperf3Input.IPERF3PORT);
        setTextFromSharedPreferences(bandwidth, Iperf3Input.IPERF3BANDWIDTH);
        setTextFromSharedPreferences(duration, Iperf3Input.IPERF3DURATION);
        setTextFromSharedPreferences(interval, Iperf3Input.IPERF3INTERVAL);
        setTextFromSharedPreferences(bytes, Iperf3Input.IPERF3BYTES);
        setTextFromSharedPreferences(streams, Iperf3Input.IPERF3STREAMS);
        setTextFromSharedPreferences(cport, Iperf3Input.IPERF3CPORT);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iperf3_card, container, false);
        // Initialize the TextView
        progressBar = view.findViewById(R.id.iperf3_progress);
        progressBar.setVisibility(View.INVISIBLE);
        iperf3Input = new Iperf3Input();
        sendBtn = view.findViewById(R.id.iperf3_send);
        spg = SharedPreferencesGrouper.getInstance(ct);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ct, Iperf3Service.class);
                if(!iperf3Input.isValid()){
                    Toast.makeText(ct, "Please give at least an IP!", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("input", iperf3Input);
                ct.startService(intent);
            }
        });


        ip = view.findViewById(R.id.iperf3_ip);
        port = view.findViewById(R.id.iperf3_port);
        bandwidth = view.findViewById(R.id.iperf3_bandwidth);
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
            switch (Iperf3Input.Iperf3Mode.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Input.IPERF3MODE, ""))){
                case CLIENT:
                    updateModeState(modeClient, modeServer, Iperf3Input.Iperf3Mode.CLIENT);
                    break;
                case SERVER:
                    updateModeState(modeServer, modeClient, Iperf3Input.Iperf3Mode.SERVER);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        try {
            switch (Iperf3Input.Iperf3Protocol.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Input.IPERF3PROTOCOL, ""))){
                case TCP:
                    updateProtocolState(protocolTCP, protocolUDP, Iperf3Input.Iperf3Protocol.TCP);
                    break;
                case UDP:
                    updateProtocolState(protocolUDP, protocolTCP, Iperf3Input.Iperf3Protocol.UDP);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        try {
            switch (Iperf3Input.Iperf3Direction.valueOf(spg.getSharedPreference(SPType.iperf3_sp).getString(Iperf3Input.IPERF3DIRECTION, ""))) {
                case UP:
                    updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Input.Iperf3Direction.UP);
                    break;
                case DOWN:
                    updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Input.Iperf3Direction.DOWN);
                    break;
                case BIDIR:
                    updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Input.Iperf3Direction.BIDIR);
                    break;
                default:
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
                                    updateModeState(modeClient, modeServer, Iperf3Input.Iperf3Mode.CLIENT);
                                    break;
                                case R.id.iperf3_server_button:
                                    updateModeState(modeServer, modeClient, Iperf3Input.Iperf3Mode.SERVER);
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
                                                                updateProtocolState(protocolTCP, protocolUDP, Iperf3Input.Iperf3Protocol.TCP);
                                                                break;
                                                            case R.id.iperf3_udp_button:
                                                                updateProtocolState(protocolUDP, protocolTCP, Iperf3Input.Iperf3Protocol.UDP);
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
                            updateDirectionState(directionUp, directionDown, directonBidir, Iperf3Input.Iperf3Direction.UP);
                            break;
                        case R.id.iperf3_download_button:
                            updateDirectionState(directionDown, directionUp, directonBidir, Iperf3Input.Iperf3Direction.DOWN);
                            break;
                        case R.id.iperf3_bidir_button:
                            updateDirectionState(directonBidir, directionUp, directionDown, Iperf3Input.Iperf3Direction.BIDIR);
                            break;
                    }
                }
            }
        });

        return view;
    }



    private void updateModeState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Input.Iperf3Mode protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.setMode(protocol);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Input.IPERF3MODE, protocol.toString()).apply();
    }

    private void updateProtocolState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Input.Iperf3Protocol protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.setProtocol(protocol);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Input.IPERF3PROTOCOL, protocol.toString()).apply();
    }

    private void updateDirectionState(MaterialButton activeButton, MaterialButton inactiveButton1, MaterialButton inactiveButton2, Iperf3Input.Iperf3Direction direction) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton1.setBackgroundColor(Color.TRANSPARENT);
        inactiveButton2.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.setDirection(direction);
        spg.getSharedPreference(SPType.iperf3_sp).edit().putString(Iperf3Input.IPERF3DIRECTION, direction.toString()).apply();
    }
}