package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Callable;
import java.util.zip.Inflater;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3CardFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private int position;
    private TextView pageNumberTextView;
    private ProgressBar progressBar;
    private LinearLayout header;
    private Button removeButton;
    private Iperf3Input iperf3Input = new Iperf3Input();
    private Context ct;
    private View overView;
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

    public Iperf3Input getIperf3Input() {
        return iperf3Input;
    }

    public View getOverView() {
        if(overView == null) return null;
        fillView();
        return overView;
    }

    private void fillView() {
        setTextInput(overView, R.id.iperf3_ip, iperf3Input.getIp());
        setTextInput(overView, R.id.iperf3_port, iperf3Input.getPort());
        setTextInput(overView, R.id.iperf3_bandwidth, iperf3Input.getBandwidth());
        setTextInput(overView, R.id.iperf3_duration, iperf3Input.getDuration());
        setTextInput(overView, R.id.iperf3_interval, iperf3Input.getInterval());
        setTextInput(overView, R.id.iperf3_bytes, iperf3Input.getBytes());
        setTextInput(overView, R.id.iperf3_streams, iperf3Input.getStreams());
        setTextInput(overView, R.id.iperf3_cport, iperf3Input.getCport());
    }

    private static void setTextInput(View view, int viewId, String text) {
        TextInputEditText input = view.findViewById(viewId);
        input.setEnabled(false);
        input.setText(text);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iperf3_card, container, false);
        // Initialize the TextView
        pageNumberTextView = view.findViewById(R.id.page_number_text_view);
        progressBar = view.findViewById(R.id.iperf3_progress);
        progressBar.setVisibility(View.INVISIBLE);
        header = view.findViewById(R.id.iperf3_header);
        removeButton = view.findViewById(R.id.iperf3_close_button);
        iperf3Input.setContext(ct);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the fragment
                ViewPager2 viewPager = getActivity().findViewById(R.id.iperf3_viewpager);
                Iperf3CardAdapter adapter = (Iperf3CardAdapter) viewPager.getAdapter();
                if (adapter == null) {
                    return;
                }
                adapter.removeFragment(position);
                viewPager.setCurrentItem(position - 1, true);
            }
        });

        LayoutInflater inflater2 = LayoutInflater.from(ct);
        ViewGroup layoutInflater = (ViewGroup) inflater2.inflate(R.layout.fragment_iperf3_card, container, false);

        overView = layoutInflater.findViewById(R.id.input_card);
        if (overView != null && overView.getParent() != null) {
            ((ViewGroup) overView.getParent()).removeView(overView);
        }



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

        ip.addTextChangedListener(iperf3Input.getIpTextWatcher());
        port.addTextChangedListener(iperf3Input.getPortTextWatcher());
        bandwidth.addTextChangedListener(iperf3Input.getBandwidthTextWatcher());
        duration.addTextChangedListener(iperf3Input.getDurationTextWatcher());
        interval.addTextChangedListener(iperf3Input.getIntervalTextWatcher());
        bytes.addTextChangedListener(iperf3Input.getBytesTextWatcher());
        streams.addTextChangedListener(iperf3Input.getStreamsTextWatcher());
        cport.addTextChangedListener(iperf3Input.getCportTextWatcher());

        mode.addOnButtonCheckedListener(
                iperf3Input.getModeButtonCheckedListener(modeClient, modeServer));
        protocol.addOnButtonCheckedListener(
                iperf3Input.getProtocolButtonCheckedListener(protocolTCP, protocolUDP));
        direction.addOnButtonCheckedListener(
                iperf3Input.getDirectionButtonCheckedListener(directionUp, directionDown, directonBidir));

        // Get the position argument and set the text
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION, 1);
            pageNumberTextView.setText("Page " + (position + 1));
        }


        return view;
    }
}