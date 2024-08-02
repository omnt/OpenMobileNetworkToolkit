package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class Iperf3CardAdapter extends FragmentStateAdapter {
    public interface Callback {
        void onAddFragment(Iperf3CardFragment fragment);
    }

    private Callback callback;
    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    public Iperf3CardAdapter(@NonNull FragmentActivity fragmentActivity, Callback callback) {
        super(fragmentActivity);
        // Initialize with the first fragment
        this.callback = callback;
        fragmentList.add(Iperf3CardAddFragment.newInstance());
        this.addFragment(Iperf3CardFragment.newInstance(0));

    }

    public void addFragment(Iperf3CardFragment fragment) {
        fragmentList.add(fragmentList.size() - 1, fragment);
        notifyItemInserted(fragmentList.size() - 2);
        if (callback != null) {
            callback.onAddFragment(fragment);
        }
    }

    public void removeFragment(int position) {
        try {
            fragmentList.remove(position);
            notifyItemRemoved(position - 1);
        } catch (IndexOutOfBoundsException e) {
            // Handle exception
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