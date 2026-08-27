package com.focusforge.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

public class DashboardFragment extends Fragment {
    private View statusDot;
    private TextView statusText;
    private TextView statusDetail;
    private Button btnToggle;
    private Switch switchStudy;
    private Switch switchShorts;
    private TextView statApps;
    private TextView statSites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusDot = view.findViewById(R.id.statusDot);
        statusText = view.findViewById(R.id.statusText);
        statusDetail = view.findViewById(R.id.statusDetail);
        btnToggle = view.findViewById(R.id.btnToggle);
        switchStudy = view.findViewById(R.id.switchStudy);
        switchShorts = view.findViewById(R.id.switchShorts);
        statApps = view.findViewById(R.id.statApps);
        statSites = view.findViewById(R.id.statSites);

        btnToggle.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        switchStudy.setChecked(FocusForgeConfig.youtubeStudyModeEnabled);
        switchStudy.setOnCheckedChangeListener((b, checked) -> FocusForgeConfig.youtubeStudyModeEnabled = checked);

        switchShorts.setChecked(FocusForgeConfig.reelsShortsBlockingEnabled);
        switchShorts.setOnCheckedChangeListener((b, checked) -> FocusForgeConfig.reelsShortsBlockingEnabled = checked);

        statApps.setText(String.valueOf(FocusForgeConfig.blockedPackages.size()));
        statSites.setText(String.valueOf(FocusForgeConfig.blockedDomains.size()));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        boolean enabled = FocusForgeAccessibilityService.isServiceEnabled();
        if (enabled) {
            statusDot.setBackgroundResource(R.drawable.dot_green);
            statusText.setText("Protection Active");
            statusDetail.setText("FocusForge is blocking distractions");
            btnToggle.setText("DISABLE PROTECTION");
        } else {
            statusDot.setBackgroundResource(R.drawable.dot_red);
            statusText.setText("Protection Disabled");
            statusDetail.setText("Enable accessibility service to block apps and content");
            btnToggle.setText("ENABLE PROTECTION");
        }
    }
}
