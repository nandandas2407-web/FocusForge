package com.focusforge.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeFragment extends Fragment {
    private Switch switchStudyMode;
    private Switch switchShortsBlock;
    private LinearLayout channelList;
    private EditText editChannelUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_youtube, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchStudyMode = view.findViewById(R.id.switchStudyMode);
        switchShortsBlock = view.findViewById(R.id.switchShortsBlock);
        channelList = view.findViewById(R.id.channelList);
        editChannelUrl = view.findViewById(R.id.editChannelUrl);

        switchStudyMode.setChecked(FocusForgeConfig.youtubeStudyModeEnabled);
        switchStudyMode.setOnCheckedChangeListener((b, checked) -> {
            FocusForgeConfig.youtubeStudyModeEnabled = checked;
            FocusForgeConfig.save();
        });

        switchShortsBlock.setChecked(FocusForgeConfig.reelsShortsBlockingEnabled);
        switchShortsBlock.setOnCheckedChangeListener((b, checked) -> {
            FocusForgeConfig.reelsShortsBlockingEnabled = checked;
            FocusForgeConfig.save();
        });

        view.findViewById(R.id.btnAddChannel).setOnClickListener(v -> addChannel());

        loadChannels();
    }

    private void addChannel() {
        if (!isAdded()) return;
        String url = editChannelUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(requireContext(), "Paste a YouTube channel URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String channelId = extractChannelId(url);
        String channelHandle = extractChannelHandle(url);
        String channelName = extractChannelName(url);

        if (channelId == null && channelHandle == null && channelName == null) {
            Toast.makeText(requireContext(), "Invalid YouTube channel URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = channelName != null ? channelName :
                             channelHandle != null ? channelHandle : channelId;

        if (FocusForgeConfig.youtubeWhitelistChannels.contains(displayName)) {
            Toast.makeText(requireContext(), "Channel already added", Toast.LENGTH_SHORT).show();
            return;
        }

        FocusForgeConfig.youtubeWhitelistChannels.add(displayName);
        if (channelHandle != null) {
            FocusForgeConfig.youtubeWhitelistHandles.add(channelHandle);
        }
        FocusForgeConfig.save();

        editChannelUrl.setText("");
        loadChannels();
        Toast.makeText(requireContext(), "Added: " + displayName, Toast.LENGTH_SHORT).show();
    }

    private String extractChannelId(String url) {
        Pattern p1 = Pattern.compile("youtube\\.com/channel/([a-zA-Z0-9_-]+)");
        Matcher m1 = p1.matcher(url);
        if (m1.find()) return m1.group(1);

        Pattern p2 = Pattern.compile("youtube\\.com/c/([a-zA-Z0-9_-]+)");
        Matcher m2 = p2.matcher(url);
        if (m2.find()) return m2.group(1);

        Pattern p3 = Pattern.compile("youtube\\.com/user/([a-zA-Z0-9_-]+)");
        Matcher m3 = p3.matcher(url);
        if (m3.find()) return m3.group(1);

        return null;
    }

    private String extractChannelHandle(String url) {
        Pattern p = Pattern.compile("youtube\\.com/@([a-zA-Z0-9._-]+)");
        Matcher m = p.matcher(url);
        if (m.find()) return "@" + m.group(1);
        return null;
    }

    private String extractChannelName(String url) {
        Pattern p = Pattern.compile("youtube\\.com/(?:c/|channel/|@)([a-zA-Z0-9._-]+)");
        Matcher m = p.matcher(url);
        if (m.find()) {
            String name = m.group(1);
            if (name.startsWith("@")) return name;
            return name.replace("-", " ").replace(".", " ");
        }
        return null;
    }

    private void loadChannels() {
        if (!isAdded()) return;
        channelList.removeAllViews();

        List<String> channels = new ArrayList<>(FocusForgeConfig.youtubeWhitelistChannels);
        for (String channel : channels) {
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_channel, channelList, false);
            TextView channelName = itemView.findViewById(R.id.channelName);
            channelName.setText(channel);

            View btnRemove = itemView.findViewById(R.id.btnRemove);
            btnRemove.setOnClickListener(v -> {
                FocusForgeConfig.youtubeWhitelistChannels.remove(channel);
                FocusForgeConfig.save();
                loadChannels();
            });

            channelList.addView(itemView);
        }

        if (channels.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No channels added yet. Paste a YouTube channel URL above.");
            empty.setTextColor(0xFF666666);
            empty.setTextSize(14);
            empty.setPadding(0, 16, 0, 0);
            channelList.addView(empty);
        }
    }
}
