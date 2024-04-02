package com.example.peachzyapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.peachzyapp.fragments.MainFragments.BlankFragmentTab1;
import com.example.peachzyapp.fragments.MainFragments.ChatListsFragment;
import com.example.peachzyapp.fragments.MainFragments.FriendsFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatListsFragment();
            case 1:
                return new BlankFragmentTab1();
            case 2:
                return new FriendsFragment();
            case 3:
                return new BlankFragmentTab1();
            default:
                return new ChatListsFragment();
        }

    }

    @Override
    public int getCount() {
        return 4;
    }
}
