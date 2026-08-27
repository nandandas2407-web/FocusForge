package com.focusforge.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermissionsFragment extends Fragment {
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permissions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.permissionsContainer);
        loadPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (container != null) loadPermissions();
    }

    private void loadPermissions() {
        if (!isAdded()) return;
        container.removeAllViews();

        addPermissionCard(
            "Accessibility Service",
            "Required to detect and block distracting apps, YouTube Shorts, and non-educational content",
            isAccessibilityServiceEnabled(),
            v -> {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        );

        addPermissionCard(
            "Display Over Other Apps",
            "Required to show block overlay when content is blocked",
            Settings.canDrawOverlays(requireContext()),
            v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addPermissionCard(
                "Notifications",
                "Required to show foreground service notification and block alerts",
                requireContext().getSystemService(android.app.NotificationManager.class).areNotificationsEnabled(),
                v -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                    startActivity(intent);
                }
            );
        }

        addPermissionCard(
            "Ignore Battery Optimizations",
            "Prevents Android from killing the accessibility service in the background",
            isBatteryOptimizationIgnored(),
            v -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    android.net.Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            }
        );

        addPermissionCard(
            "Usage Access",
            "Optional: helps detect app usage patterns for better blocking",
            isUsageAccessGranted(),
            v -> {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        );
    }

    private void addPermissionCard(String title, String description, boolean enabled, View.OnClickListener onClick) {
        View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_permission, container, false);

        TextView titleView = card.findViewById(R.id.permTitle);
        TextView descView = card.findViewById(R.id.permDescription);
        TextView statusView = card.findViewById(R.id.permStatus);
        View enableBtn = card.findViewById(R.id.permEnableBtn);

        titleView.setText(title);
        descView.setText(description);
        statusView.setText(enabled ? "GRANTED" : "NOT GRANTED");
        statusView.setTextColor(enabled ? 0xFF4CAF50 : 0xFFF44336);
        enableBtn.setOnClickListener(onClick);

        card.setBackgroundResource(enabled ? R.drawable.card_green_border : R.drawable.card_bg);
        container.addView(card);
    }

    private boolean isAccessibilityServiceEnabled() {
        if (!isAdded()) return false;
        String serviceName = requireContext().getPackageName() + "/com.focusforge.app.FocusForgeAccessibilityService";
        String enabledServices = Settings.Secure.getString(
            requireContext().getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices == null) return false;
        return enabledServices.contains(serviceName);
    }

    private boolean isBatteryOptimizationIgnored() {
        if (!isAdded()) return false;
        android.os.PowerManager pm = (android.os.PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(requireContext().getPackageName());
    }

    private boolean isUsageAccessGranted() {
        if (!isAdded()) return false;
        try {
            android.app.usage.UsageStatsManager usm = (android.app.usage.UsageStatsManager)
                requireContext().getSystemService(Context.USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            java.util.List<android.app.usage.UsageStats> stats = usm.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY, now - 1000, now);
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
