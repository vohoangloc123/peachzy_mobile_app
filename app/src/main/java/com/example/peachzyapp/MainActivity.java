package com.example.peachzyapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager=findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));

        bottomNavigationView=findViewById(R.id.bottom_navigation);

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
                            bottomNavigationView.getMenu().findItem(R.id.navigation_settings).setChecked(true);

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
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_notifications) {

                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_users) {
                    Toast.makeText(MainActivity.this, "Chuyển tab 3", Toast.LENGTH_SHORT).show();
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_settings) {
                    Toast.makeText(MainActivity.this, "Chuyển tab 4", Toast.LENGTH_SHORT).show();
                    viewPager.setCurrentItem(3);
                }
                return true;
            }
        });
    }
}