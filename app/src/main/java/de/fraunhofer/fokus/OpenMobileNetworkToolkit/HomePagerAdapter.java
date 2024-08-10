package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new QuickFragment();
            case 1:
                return new DetailFragment();
            default:
                return new DetailFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}