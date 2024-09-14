package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.zip.Inflater;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.ViewsMapManager;

public class Iperf3Fragment extends Fragment{
    private static final String TAG = "iperf3InputFragment";

    private View v;
    private Context ct;
    private MaterialButton sendBtn;
    private ViewPager2 viewPager;
    private Iperf3CardAdapter adapter;
    private ViewsMapManager viewsMapManager;
    private Iperf3CardFragment iperf3CardFragment;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        ct = requireContext();
        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);
        viewPager = v.findViewById(R.id.iperf3_viewpager);
        //linearLayout = v.findViewById(R.id.iperf3_plan);
        viewsMapManager = new ViewsMapManager(ct);
        adapter = new Iperf3CardAdapter(getActivity());

        viewPager.setAdapter(adapter);
        return v;
    }





    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}