package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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


import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Service;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iperf3_card, container, false);
        // Initialize the TextView
        progressBar = view.findViewById(R.id.iperf3_progress);
        progressBar.setVisibility(View.INVISIBLE);
        iperf3Input = new Iperf3Input();
        sendBtn = view.findViewById(R.id.iperf3_send);
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

        ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setIp(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setPort(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        bandwidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setBandwidth(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        duration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setDuration(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        interval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setInterval(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        bytes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setBytes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        streams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setStreams(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        cport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                iperf3Input.setCport(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
    }

    private void updateProtocolState(MaterialButton activeButton, MaterialButton inactiveButton, Iperf3Input.Iperf3Protocol protocol) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.setProtocol(protocol);
    }

    private void updateDirectionState(MaterialButton activeButton, MaterialButton inactiveButton1, MaterialButton inactiveButton2, Iperf3Input.Iperf3Direction direction) {
        activeButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        inactiveButton1.setBackgroundColor(Color.TRANSPARENT);
        inactiveButton2.setBackgroundColor(Color.TRANSPARENT);
        iperf3Input.setDirection(direction);
    }
}