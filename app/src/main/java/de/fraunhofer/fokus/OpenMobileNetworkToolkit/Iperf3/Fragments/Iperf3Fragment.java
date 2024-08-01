package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.ViewsMapManager;

public class Iperf3Fragment extends Fragment implements Iperf3CardAdapter.Callback {
    private static final String TAG = "iperf3InputFragment";

    private View v;
    private Button sendBtn;
    private Context ct;
    private ViewPager2 viewPager;
    private CoordinatorLayout bottomSheet;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private FrameLayout frameLayout;
    private LinearLayout buttonLinearLayout;
    private LinearLayout linearLayout;
    private Iperf3CardAdapter adapter;
    private ViewsMapManager viewsMapManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        ct = requireContext();
        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);
        viewPager = v.findViewById(R.id.iperf3_viewpager);
        linearLayout = v.findViewById(R.id.iperf3_plan);
        viewsMapManager = new ViewsMapManager(ct);
        adapter = new Iperf3CardAdapter(getActivity(), this);
        linearLayout.addView(viewsMapManager.getDraggableGridLayout());
        viewPager.setAdapter(adapter);
        sendBtn = v.findViewById(R.id.iperf3_send);
        bottomSheet = v.findViewById(R.id.iperf3_bottom_sheet_linearlayout);
        frameLayout = v.findViewById(R.id.standard_bottom_sheet);
        buttonLinearLayout = v.findViewById(R.id.iperf3_buttons);

        buttonLinearLayout.setVisibility(View.GONE);
        bottomSheetBehavior = BottomSheetBehavior.from(frameLayout);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (bottomSheetBehavior == null) return; // Check if behavior is not initialized

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        buttonLinearLayout.setVisibility(View.VISIBLE);
                        viewsMapManager.update();
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
        return v;
    }

    @Override
    public void onAddFragment() {
        View v = getLayoutInflater().inflate(R.layout.grid_item_card, null);
        CardView cardView = v.findViewById(R.id.card_view);
        TextView textView = v.findViewById(R.id.iperf3_desc);
        if(adapter == null) {
            textView.setText("Card "+1);
        } else {
            textView.setText("Card "+(adapter.getItemCount()-1));
        }
        viewsMapManager.addNewView(cardView);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}