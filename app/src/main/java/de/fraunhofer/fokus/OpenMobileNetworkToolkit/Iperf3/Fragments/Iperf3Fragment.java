package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Fragment extends Fragment{
    private static final String TAG = "iperf3InputFragment";

    private View v;
    private Context ct;
    private MaterialButton sendBtn;
    private ViewPager2 viewPager;
    private Iperf3CardAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        ct = requireContext();
        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);
        viewPager = v.findViewById(R.id.iperf3_viewpager);
        //linearLayout = v.findViewById(R.id.iperf3_plan);
        adapter = new Iperf3CardAdapter(getActivity());

        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                adapter.notifyDataSetChanged();
                v.requestLayout();
            }
        });


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}