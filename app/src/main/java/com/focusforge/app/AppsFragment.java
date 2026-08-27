package com.focusforge.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppsFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private List<AppItem> apps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadApps();
        adapter = new AppAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadApps() {
        apps = new ArrayList<>();

        String[][] knownApps = {
            {"com.instagram.android", "Instagram"},
            {"com.zhiliaoapp.musically", "TikTok"},
            {"com.ss.android.ugc.trill", "TikTok"},
            {"com.twitter.android", "Twitter / X"},
            {"com.facebook.katana", "Facebook"},
            {"com.facebook.lite", "Facebook Lite"},
            {"com.google.android.youtube", "YouTube"},
            {"com.google.android.apps.youtube.music", "YouTube Music"},
            {"com.reddit.frontpage", "Reddit"},
            {"com.snapchat.android", "Snapchat"},
            {"com.pinterest", "Pinterest"},
            {"com.linkedin.android", "LinkedIn"},
            {"com.discord", "Discord"},
            {"com.spotify.music", "Spotify"},
            {"com.netflix.mediaclient", "Netflix"},
            {"com.amazon.mShop.android.shopping", "Amazon"},
            {"com.ebay.mobile", "eBay"},
            {"com.android.chrome", "Chrome"},
            {"org.mozilla.firefox", "Firefox"},
            {"com.UCMobile", "UC Browser"},
            {"com.opera.browser", "Opera"},
            {"com.brave.browser", "Brave"},
            {"com.viber.voip", "Viber"},
            {"org.telegram.messenger", "Telegram"},
            {"com.whatsapp", "WhatsApp"},
            {"com.google.android.gm", "Gmail"},
            {"com.google.android.apps.docs", "Google Docs"},
            {"com.google.android.apps.photos", "Google Photos"},
            {"com.google.android.maps", "Google Maps"},
            {"com.waze", "Waze"},
            {"com.twitch.android.app", "Twitch"},
        };

        for (String[] app : knownApps) {
            apps.add(new AppItem(app[0], app[1], FocusForgeConfig.blockedPackages.contains(app[0])));
        }
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AppItem item = apps.get(position);
            holder.appName.setText(item.name);
            holder.appPackage.setText(item.packageName);
            holder.appSwitch.setChecked(item.blocked);
            holder.appSwitch.setOnCheckedChangeListener((b, checked) -> {
                item.blocked = checked;
                if (checked) {
                    FocusForgeConfig.blockedPackages.add(item.packageName);
                } else {
                    FocusForgeConfig.blockedPackages.remove(item.packageName);
                }
            });
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView appName, appPackage;
            Switch appSwitch;

            ViewHolder(View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.appName);
                appPackage = itemView.findViewById(R.id.appPackage);
                appSwitch = itemView.findViewById(R.id.appSwitch);
            }
        }
    }

    static class AppItem {
        String packageName;
        String name;
        boolean blocked;

        AppItem(String packageName, String name, boolean blocked) {
            this.packageName = packageName;
            this.name = name;
            this.blocked = blocked;
        }
    }
}
