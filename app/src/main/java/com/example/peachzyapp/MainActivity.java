package com.example.peachzyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.peachzyapp.adapters.ViewPagerAdapter;
import com.example.peachzyapp.entities.ChatBox;
import com.example.peachzyapp.entities.Profile;
import com.example.peachzyapp.fragments.MainFragments.AddFriendFragment;
import com.example.peachzyapp.fragments.MainFragments.ChatHistoryFragment;
import com.example.peachzyapp.fragments.MainFragments.ChatListsFragment;
import com.example.peachzyapp.fragments.MainFragments.ProfileFragment;
import com.example.peachzyapp.fragments.MainFragments.RequestReceivedFragment;
import com.example.peachzyapp.fragments.MainFragments.RequestSendFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.etFind), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager=findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
        //số lượng fragment được load
        viewPager.setOffscreenPageLimit(2);
        bottomNavigationView=findViewById(R.id.bottom_navigation);
        uid = getIntent().getStringExtra("uid");
        if (uid != null) {
            Log.d("checkIntent", uid);
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uid", uid);
            editor.apply();
        } else {
            Log.e("checkIntent", "UID is null");
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                    switch (position)
                    {
                        case 0:
                            bottomNavigationView.getMenu().findItem(R.id.navigation_chats).setChecked(true);
                            break;
                        case 1:
                            bottomNavigationView.getMenu().findItem(R.id.navigation_notifications).setChecked(true);
                            break;
                        case 2:
                            bottomNavigationView.getMenu().findItem(R.id.navigation_users).setChecked(true);
                            break;
                        case 3:
                            bottomNavigationView.getMenu().findItem(R.id.navigation_profile).setChecked(true);

                            break;
                    }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.navigation_chats) {
                    Toast.makeText(MainActivity.this, "Chuyển tab 1", Toast.LENGTH_SHORT).show();
                    // Gọi reloadData() khi chuyển sang tab 1
                    ChatListsFragment chatLists = (ChatListsFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    chatLists.reloadData();
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_notifications) {

                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_users) {
                    Toast.makeText(MainActivity.this, "Chuyển tab 3", Toast.LENGTH_SHORT).show();
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_profile) {
                    Toast.makeText(MainActivity.this, "Chuyển tab 4", Toast.LENGTH_SHORT).show();
                    viewPager.setCurrentItem(3);
                }
                return true;
            }
        });
    }
public void goToDetailFragment(ChatBox chatBox) {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    ChatHistoryFragment chatHistoryFragment = new ChatHistoryFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable("object_chatbox", chatBox);
    chatHistoryFragment.setArguments(bundle);

    // Thêm ChatHistoryFragment
    fragmentTransaction.add(R.id.etFind, chatHistoryFragment, ChatHistoryFragment.TAG);

    // Tìm và ẩn tất cả các Fragment khác
    Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
    Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
    Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
    Fragment profileFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

    if (chatListsFragment != null) {
        fragmentTransaction.hide(chatListsFragment);
    }
    if (notificationFragment != null) {
        fragmentTransaction.hide(notificationFragment);
    }
    if (usersFragment != null) {
        fragmentTransaction.hide(usersFragment);
    }
    if (profileFragment != null) {
        fragmentTransaction.hide(profileFragment);
    }

    // Ẩn bottomNavigationView
    showBottomNavigation(false);
    fragmentTransaction.addToBackStack(ChatHistoryFragment.TAG);
    fragmentTransaction.commit();
}
    public void goToDetailFragmentAddFriend() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        AddFriendFragment addFriendFragment=new AddFriendFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, addFriendFragment, addFriendFragment.TAG1);

        // Tìm và ẩn tất cả các Fragment khác
        Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment profileFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

        if (chatListsFragment != null) {
            fragmentTransaction.hide(chatListsFragment);
        }
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (profileFragment != null) {
            fragmentTransaction.hide(profileFragment);
        }

        // Ẩn bottomNavigationView
        showBottomNavigation(false);
        addFriendFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(addFriendFragment.TAG1);
        fragmentTransaction.commit();
    }
    public void goToRequestReceivedFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        RequestReceivedFragment requestReceivedFragment= new RequestReceivedFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, requestReceivedFragment, requestReceivedFragment.TAG2);

        // Tìm và ẩn tất cả các Fragment khác
        Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment settingsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

        if (chatListsFragment != null) {
            fragmentTransaction.hide(chatListsFragment);
        }
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (settingsFragment != null) {
            fragmentTransaction.hide(settingsFragment);
        }

        // Ẩn bottomNavigationView
        showBottomNavigation(false);
        requestReceivedFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(requestReceivedFragment.TAG2);
        fragmentTransaction.commit();
    }

    public void goToRequestSentFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        RequestSendFragment requestSendFragment = new RequestSendFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, requestSendFragment, requestSendFragment.TAG);

        // Tìm và ẩn tất cả các Fragment khác
        Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment settingsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

        if (chatListsFragment != null) {
            fragmentTransaction.hide(chatListsFragment);
        }
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (settingsFragment != null) {
            fragmentTransaction.hide(settingsFragment);
        }

        // Ẩn bottomNavigationView
        showBottomNavigation(false);
        requestSendFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(requestSendFragment.TAG);
        fragmentTransaction.commit();
    }
    public void showBottomNavigation(boolean show) {
        if (show) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }
    @Override
    public void onBackPressed() {
        // Kiểm tra xem có Fragment trong BackStack không
//        Toast.makeText(getApplicationContext(), "Worked", Toast.LENGTH_SHORT).show();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Pop Fragment ra khỏi BackStack
            showBottomNavigation(true);
            getSupportFragmentManager().popBackStack();
        } else {
            // Nếu không có Fragment trong BackStack, thoát ứng dụng
            super.onBackPressed();
        }
    }

}