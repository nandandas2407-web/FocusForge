package com.focusforge.app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {
    private View statusDot;
    private TextView statusText;
    private TextView statusDetail;
    private Button btnToggle;
    private Switch switchStudy;
    private Switch switchShorts;
    private TextView statApps;
    private TextView statSites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
            if (!isAccessibilityServiceEnabled()) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            } else {
                FocusForgeConfig.globalBlockerEnabled = !FocusForgeConfig.globalBlockerEnabled;
                FocusForgeConfig.save();
                updateStatus();
            }
        });

        switchStudy.setOnCheckedChangeListener((b, checked) -> {
            FocusForgeConfig.youtubeStudyModeEnabled = checked;
            FocusForgeConfig.save();
        });

        switchShorts.setOnCheckedChangeListener((b, checked) -> {
            FocusForgeConfig.reelsShortsBlockingEnabled = checked;
            FocusForgeConfig.save();
        });

        statApps.setText(String.valueOf(FocusForgeConfig.blockedPackages.size()));
        statSites.setText(String.valueOf(FocusForgeConfig.blockedDomains.size()));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        if (!isAdded()) return;
        boolean enabled = isAccessibilityServiceEnabled() && FocusForgeConfig.globalBlockerEnabled;
        if (enabled) {
            statusDot.setBackgroundResource(R.drawable.dot_green);
            statusText.setText("Protection Active");
            statusDetail.setText("FocusForge is blocking distractions");
            btnToggle.setText("PAUSE PROTECTION");
        } else if (isAccessibilityServiceEnabled()) {
            statusDot.setBackgroundResource(R.drawable.dot_red);
            statusText.setText("Protection Paused");
            statusDetail.setText("Service is running but protection is paused");
            btnToggle.setText("RESUME PROTECTION");
        } else {
            statusDot.setBackgroundResource(R.drawable.dot_red);
            statusText.setText("Protection Disabled");
            statusDetail.setText("Enable accessibility service to start blocking");
            btnToggle.setText("ENABLE PROTECTION");
        }
        switchStudy.setChecked(FocusForgeConfig.youtubeStudyModeEnabled);
        switchShorts.setChecked(FocusForgeConfig.reelsShortsBlockingEnabled);
        statApps.setText(String.valueOf(FocusForgeConfig.blockedPackages.size()));
        statSites.setText(String.valueOf(FocusForgeConfig.blockedDomains.size()));
    }

    private boolean isAccessibilityServiceEnabled() {
        if (getActivity() == null) return false;
        String serviceName = getActivity().getPackageName() + "/com.focusforge.app.FocusForgeAccessibilityService";
        String enabledServices = Settings.Secure.getString(
            getActivity().getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices == null) return false;
        return enabledServices.contains(serviceName);
    }
}
