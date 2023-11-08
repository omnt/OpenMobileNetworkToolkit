package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.work.OneTimeWorkRequest;
import java.util.ArrayList;

public class Iperf3FragmentStateAdapter extends FragmentStateAdapter {

    ArrayList<Iperf3Fragment> iperf3Fragments;
    public Iperf3FragmentStateAdapter(Fragment fragment) {
        super(fragment);
        iperf3Fragments = new ArrayList<>();
    }


    public ArrayList<Iperf3Fragment> getIperf3Fragments() {
        return iperf3Fragments;
    }

    public ArrayList<Iperf3Fragment.Iperf3Input> getIperf3Inputs(){
        ArrayList<Iperf3Fragment>  iperf3fragments = this.getIperf3Fragments();
        ArrayList<Iperf3Fragment.Iperf3Input> iperf3Inputs = new ArrayList<>();
        for(Iperf3Fragment iperf3Fragment: iperf3fragments){
            iperf3Inputs.add(iperf3Fragment.getInput());
        }
        return iperf3Inputs;
    }

    public ArrayList<OneTimeWorkRequest> getIperf3Worker(){
        ArrayList<Iperf3Fragment>  iperf3fragments = this.getIperf3Fragments();
        ArrayList<OneTimeWorkRequest> iperf3Workers = new ArrayList<>();
        for(Iperf3Fragment iperf3Fragment: iperf3fragments){
            iperf3Workers.add(iperf3Fragment.getIperf3Worker());
        }
        return iperf3Workers;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Iperf3Fragment fragment = new Iperf3Fragment();
        Bundle args = new Bundle();
        // The object is just an integer.
        args.putInt(Iperf3Fragment.FRAGMENT_ID, position);
        fragment.setArguments(args);
        iperf3Fragments.add(fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
