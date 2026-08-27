package com.focusforge.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ImageView navDashboard, navApps, navYoutube, navWebsites, navTimer, navPermissions;
    private int selectedNav = R.id.nav_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        navDashboard = findViewById(R.id.nav_dashboard);
        navApps = findViewById(R.id.nav_apps);
        navYoutube = findViewById(R.id.nav_youtube);
        navWebsites = findViewById(R.id.nav_websites);
        navTimer = findViewById(R.id.nav_timer);
        navPermissions = findViewById(R.id.nav_permissions);

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                loadFragment(item.getItemId());
                return true;
            });
        }

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> loadFragment(R.id.nav_dashboard));
            navApps.setOnClickListener(v -> loadFragment(R.id.nav_apps));
            navYoutube.setOnClickListener(v -> loadFragment(R.id.nav_youtube));
            navWebsites.setOnClickListener(v -> loadFragment(R.id.nav_websites));
            navTimer.setOnClickListener(v -> loadFragment(R.id.nav_timer));
            navPermissions.setOnClickListener(v -> loadFragment(R.id.nav_permissions));
        }

        if (savedInstanceState == null) {
            loadFragment(R.id.nav_dashboard);
        }
    }

    private void loadFragment(int navId) {
        selectedNav = navId;
        Fragment fragment = null;
        if (navId == R.id.nav_dashboard) fragment = new DashboardFragment();
        else if (navId == R.id.nav_apps) fragment = new AppsFragment();
        else if (navId == R.id.nav_youtube) fragment = new YouTubeFragment();
        else if (navId == R.id.nav_websites) fragment = new WebsitesFragment();
        else if (navId == R.id.nav_timer) fragment = new TimerFragment();
        else if (navId == R.id.nav_permissions) fragment = new PermissionsFragment();

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }
        updateNavSelection();
    }

    private void updateNavSelection() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(selectedNav);
        }
        if (navDashboard != null) {
            navDashboard.setSelected(selectedNav == R.id.nav_dashboard);
            navApps.setSelected(selectedNav == R.id.nav_apps);
            navYoutube.setSelected(selectedNav == R.id.nav_youtube);
            navWebsites.setSelected(selectedNav == R.id.nav_websites);
            navTimer.setSelected(selectedNav == R.id.nav_timer);
            if (navPermissions != null) navPermissions.setSelected(selectedNav == R.id.nav_permissions);
        }
    }
}
