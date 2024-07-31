package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DraggableGridLayout;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class Iperf3Fragment extends Fragment {
    private static final String TAG = "iperf3InputFragment";

    private View v;
    private SharedPreferencesGrouper spg;
    private Button sendBtn;
    private Context ct;
    private ViewPager2 viewPager;
    private CoordinatorLayout bottomSheet;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private FrameLayout frameLayout;
    private LinearLayout buttonLinearLayout;
    private DraggableGridLayout tableLayout;
    private LinearLayout linearLayout;
    private Iperf3CardAdapter adapter;
    private HashMap <Integer, List<View>> viewsMap = new HashMap<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);
        viewPager = v.findViewById(R.id.iperf3_viewpager);
        adapter = new Iperf3CardAdapter(getActivity());
        viewPager.setAdapter(adapter);
        ct = requireContext();
        spg = SharedPreferencesGrouper.getInstance(ct);
        sendBtn = v.findViewById(R.id.iperf3_send);
        bottomSheet = v.findViewById(R.id.iperf3_bottom_sheet_linearlayout);
        frameLayout = v.findViewById(R.id.standard_bottom_sheet);
        buttonLinearLayout = v.findViewById(R.id.iperf3_buttons);
        linearLayout = v.findViewById(R.id.iperf3_plan);
        tableLayout = new DraggableGridLayout(ct);
        linearLayout.addView(tableLayout);
        buttonLinearLayout.setVisibility(View.GONE);
        bottomSheetBehavior = BottomSheetBehavior.from(frameLayout);






        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (bottomSheetBehavior == null) return; // Check if behavior is not initialized

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        buttonLinearLayout.setVisibility(View.VISIBLE);
                        updateViews();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        buttonLinearLayout.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Optionally handle slide events
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        updateViews();
        return v;
    }

    private void updateViews(){
        for (int i = 0; i < adapter.getItemCount(); i++) {
            List<View> views = new ArrayList<>();
            View cardView = getLayoutInflater().inflate(R.layout.grid_item_card, tableLayout, false);
            MaterialTextView textView = cardView.findViewById(R.id.iperf3_desc);
            textView.setText("Card " + i);
            views.add(cardView);
            viewsMap.put(i, views);
        }
        tableLayout.setViews(viewsMap);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
