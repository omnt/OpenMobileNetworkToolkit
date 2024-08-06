package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3CardAddFragment extends Fragment {
    private LinearLayout iperf3CardAddLayout;

    public static Iperf3CardAddFragment newInstance() {
        return new Iperf3CardAddFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iperf3_card_add, container, false);
        iperf3CardAddLayout = view.findViewById(R.id.iperf3_card_add);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        // Create the plus button programmatically
        Button plusButton = new Button(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(16, 16, 16, 16);
        plusButton.setLayoutParams(layoutParams);
        plusButton.setText("+");
        plusButton.setTextSize(50);
        plusButton.setPadding(16, 16, 16, 16);

        // Set an OnClickListener for the button
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager2 viewPager = getActivity().findViewById(R.id.iperf3_viewpager);
                Iperf3CardAdapter adapter = (Iperf3CardAdapter) viewPager.getAdapter();

                if (adapter == null) {
                    return;
                }
                if(adapter.getItemCount() > 10) {
                    Toast.makeText(getContext(), "Maximum number of iPerf3 reached", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.addFragment(Iperf3CardFragment.newInstance(adapter.getItemCount() - 1));
                viewPager.setCurrentItem(adapter.getItemCount(), true);

            }
        });

        // Add the button to the LinearLayout
        linearLayout.addView(plusButton);
        iperf3CardAddLayout.addView(linearLayout);
        return view;
    }
}