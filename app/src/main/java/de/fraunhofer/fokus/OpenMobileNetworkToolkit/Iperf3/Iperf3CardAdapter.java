package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class Iperf3CardAdapter extends FragmentStateAdapter {

    private final int numPages;

    public Iperf3CardAdapter(@NonNull FragmentActivity fragmentActivity, int numPages) {
        super(fragmentActivity);
        this.numPages = numPages;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a new instance of the Iperf3CardFragment
        return Iperf3CardFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return numPages; // Number of pages you want
    }
}
