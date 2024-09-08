package com.utic.appmusic;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new songsFragment();
            case 1:
                return new playlistsFragment();
            default:
                return new songsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Número de páginas/fragments
    }
}


