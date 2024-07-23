package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3CardFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private int position;
    private TextView pageNumberTextView;
    private ProgressBar progressBar;
    private LinearLayout header;
    private Button removeButton;

    public static Iperf3CardFragment newInstance(int position) {
        Iperf3CardFragment fragment = new Iperf3CardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
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
        // Get the position argument and set the text
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION, 1);
            pageNumberTextView.setText("Page " + (position + 1));
        }

        return view;
    }
}