package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class Iperf3CardAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    public Iperf3CardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Initialize with the first fragment
        fragmentList.add(Iperf3CardFragment.newInstance(0));
        fragmentList.add(Iperf3CardAddFragment.newInstance());
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragmentList.size() - 1, fragment);
        notifyItemInserted(fragmentList.size() - 2);
    }

    public void removeFragment(int position) {
        try {
            fragmentList.remove(position);
            notifyItemRemoved(position-1);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}