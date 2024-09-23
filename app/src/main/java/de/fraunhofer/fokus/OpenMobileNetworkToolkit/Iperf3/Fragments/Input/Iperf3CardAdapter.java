package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Iperf3Fragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Output.Iperf3ListFragment;

public class Iperf3CardAdapter extends FragmentStateAdapter {


    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    public Iperf3CardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        this.addFragment(Iperf3CardFragment.newInstance(0));
        this.fragmentList.add(Iperf3ListFragment.newInstance());
    }

    public void addFragment(Iperf3CardFragment fragment) {
        int fragmentPosition = Math.max(fragmentList.size() - 1, 0);
        fragmentList.add(fragmentPosition, fragment);
        notifyItemInserted(fragmentList.size() - 2);
    }

    public ArrayList<Iperf3CardFragment> getFragments() {
        ArrayList<Iperf3CardFragment> fragments = new ArrayList<>();
        for (Fragment fragment : fragmentList) {
            if(fragment instanceof Iperf3CardFragment) fragments.add((Iperf3CardFragment) fragment);
        }
        return fragments;
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