package com.example.peachzyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatBoxFragment;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatListForForwardMessageFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.GroupChatListForForwardMessageFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.Option.AddMemberFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.CreateGroupChatFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.Option.EditGroupNameFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.Option.ListMemberFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.Option.ManageMemberFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.GroupChatBoxFragment;
import com.example.peachzyapp.fragments.MainFragments.GroupChat.Option.GroupOptionFragment;
import com.example.peachzyapp.fragments.MainFragments.Profiles.ChangePasswordFragment;
import com.example.peachzyapp.fragments.MainFragments.Profiles.EditProfileFragment;
import com.example.peachzyapp.fragments.MainFragments.Profiles.ProfileFragment;
import com.example.peachzyapp.fragments.MainFragments.Profiles.ViewProfileFragment;
import com.example.peachzyapp.fragments.MainFragments.Users.AddFriendFragment;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatListFragment;
import com.example.peachzyapp.fragments.MainFragments.Users.RequestReceivedFragment;
import com.example.peachzyapp.fragments.MainFragments.Users.RequestSendFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private String test;
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
        // tạm khoá
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
        //số lượng fragment được load
        viewPager.setOffscreenPageLimit(3);
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
                    ChatListFragment chatLists = (ChatListFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_notifications) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_users) {
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_profile) {
                    viewPager.setCurrentItem(3);
                }
                return true;
            }
        });
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
        fragmentTransaction.add(R.id.etFind, requestReceivedFragment, requestReceivedFragment.TAG);

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
        fragmentTransaction.addToBackStack(requestReceivedFragment.TAG);
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
    public void goToRequestChangePasswordFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ChangePasswordFragment changePasswordFragment=new ChangePasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, changePasswordFragment, changePasswordFragment.TAG);

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
        changePasswordFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(changePasswordFragment.TAG);
        fragmentTransaction.commit();
    }
    public void goToCreateGroupChat() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        CreateGroupChatFragment createGroupChatFragment = new CreateGroupChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, createGroupChatFragment, createGroupChatFragment.TAG);

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
        createGroupChatFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(createGroupChatFragment.TAG);
        fragmentTransaction.commit();

    }
    public void goToChatBoxFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ChatBoxFragment chatBoxFragment=new ChatBoxFragment();
        bundle.putString("uid", uid);

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, chatBoxFragment, chatBoxFragment.TAG);

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
        chatBoxFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(chatBoxFragment.TAG);
        fragmentTransaction.commit();

    }
    public void goToGroupChat(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        GroupChatBoxFragment groupChatFragment = new GroupChatBoxFragment();

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, groupChatFragment, groupChatFragment.TAG);

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
        groupChatFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(groupChatFragment.TAG);
        fragmentTransaction.commit();

    }
    public void goToGroupOption(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        GroupOptionFragment groupOptionFragment=new GroupOptionFragment();

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, groupOptionFragment, groupOptionFragment.TAG);

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
        groupOptionFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(groupOptionFragment.TAG);
        fragmentTransaction.commit();

    }
    public void goToManageMember(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        ManageMemberFragment manageMemberFragment =new ManageMemberFragment();

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, manageMemberFragment, manageMemberFragment.TAG);

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
        manageMemberFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(manageMemberFragment.TAG);
        fragmentTransaction.commit();

    }
    public void goToListMember(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        ListMemberFragment listMemberFragment=new ListMemberFragment();

        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, listMemberFragment, listMemberFragment.TAG);

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
        listMemberFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(listMemberFragment.TAG);
        fragmentTransaction.commit();

    }

    public void goToAddMembersToGroup(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        AddMemberFragment addMemberFragment = new AddMemberFragment();
        bundle.putString("uid", uid);
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, addMemberFragment, addMemberFragment.TAG);

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
        addMemberFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(addMemberFragment.TAG);
        fragmentTransaction.commit();

    }
    public void goToEditProfileFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        EditProfileFragment editProfileFragment=new EditProfileFragment();
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, editProfileFragment, editProfileFragment.TAG);

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
        editProfileFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(editProfileFragment.TAG);
        fragmentTransaction.commit();
    }
    public void goToProfileFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ProfileFragment profileFragment = new ProfileFragment();
        // Thêm ProfileFragment
        fragmentTransaction.add(R.id.etFind, profileFragment, profileFragment.TAG);

        // Ẩn tất cả các Fragment khác
        Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment editProfileFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

        if (chatListsFragment != null) {
            fragmentTransaction.hide(chatListsFragment);
        }
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (editProfileFragment != null) {

        }

        profileFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(profileFragment.TAG);
        fragmentTransaction.commit();
        // Hiển thị bottomNavigationView
        showBottomNavigation(true);
    }
    public void goToEditGroupNameFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        EditGroupNameFragment editGroupNameFragment=new EditGroupNameFragment();
        // Thêm ProfileFragment
        fragmentTransaction.add(R.id.etFind, editGroupNameFragment, editGroupNameFragment.TAG);

        // Ẩn tất cả các Fragment khác
        Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment editProfileFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

        if (chatListsFragment != null) {
            fragmentTransaction.hide(chatListsFragment);
        }
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (editProfileFragment != null) {

        }

        editGroupNameFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(editGroupNameFragment.TAG);
        fragmentTransaction.commit();
        // Hiển thị bottomNavigationView
        showBottomNavigation(true);
    }

    public void goToViewProfileFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
        // Thêm ProfileFragment
        fragmentTransaction.add(R.id.etFind, viewProfileFragment, viewProfileFragment.TAG);
        // Ẩn tất cả các Fragment khác
        Fragment chatListsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment editProfileFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);

        if (chatListsFragment != null) {
            fragmentTransaction.hide(chatListsFragment);
        }
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (editProfileFragment != null) {

        }
        // Ẩn bottomNavigationView
        showBottomNavigation(false);
        viewProfileFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(viewProfileFragment.TAG);
        fragmentTransaction.commit();
    }
    public void goToGroupChatBoxListFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        GroupChatListForForwardMessageFragment groupChatListForForwardMessageFragment=new GroupChatListForForwardMessageFragment();
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, groupChatListForForwardMessageFragment, groupChatListForForwardMessageFragment.TAG);
        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment settingsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);
        if (notificationFragment != null) {
            fragmentTransaction.hide(notificationFragment);
        }
        if (usersFragment != null) {
            fragmentTransaction.hide(usersFragment);
        }
        if (settingsFragment != null) {
            fragmentTransaction.hide(settingsFragment);
        }
        showBottomNavigation(false);
        groupChatListForForwardMessageFragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(groupChatListForForwardMessageFragment.TAG);
        fragmentTransaction.commit();
    }
    public void goToChatBoxListFragment(Bundle bundle) {
        Log.d("CheckingFoward", "Run");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ChatListForForwardMessageFragment chatListForForwardMessageFragment=new ChatListForForwardMessageFragment();
        // Thêm ChatHistoryFragment
        fragmentTransaction.add(R.id.etFind, chatListForForwardMessageFragment, chatListForForwardMessageFragment.TAG);

        // Tìm và ẩn tất cả các Fragment khác

        Fragment notificationFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        Fragment usersFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 2);
        Fragment settingsFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 3);
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
        chatListForForwardMessageFragment.setArguments(bundle);
        showBottomNavigation(false);
        fragmentTransaction.addToBackStack(chatListForForwardMessageFragment.TAG);
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
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Lấy fragment đầu tiên trong BackStack
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.etFind);

            // Kiểm tra loại fragment và ẩn thanh điều hướng tương ứng
            if (fragment instanceof GroupChatBoxFragment || fragment instanceof GroupOptionFragment ||fragment instanceof ManageMemberFragment ||fragment instanceof AddMemberFragment) {
                showBottomNavigation(false); // Ẩn thanh điều hướng
            } else {
                showBottomNavigation(true); // Hiện thanh điều hướng cho các fragment khác
            }

            // Pop Fragment ra khỏi BackStack
            getSupportFragmentManager().popBackStack();
        } else {
            // Nếu không có Fragment trong BackStack, thoát ứng dụng
            super.onBackPressed();
        }
    }

}