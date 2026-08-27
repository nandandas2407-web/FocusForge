package com.focusforge.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class YouTubeFragment extends Fragment {
    private Switch switchStudyMode;
    private Switch switchShortsBlock;
    private LinearLayout channelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_youtube, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchStudyMode = view.findViewById(R.id.switchStudyMode);
        switchShortsBlock = view.findViewById(R.id.switchShortsBlock);
        channelList = view.findViewById(R.id.channelList);

        switchStudyMode.setChecked(FocusForgeConfig.youtubeStudyModeEnabled);
        switchStudyMode.setOnCheckedChangeListener((b, checked) -> FocusForgeConfig.youtubeStudyModeEnabled = checked);

        switchShortsBlock.setChecked(FocusForgeConfig.reelsShortsBlockingEnabled);
        switchShortsBlock.setOnCheckedChangeListener((b, checked) -> FocusForgeConfig.reelsShortsBlockingEnabled = checked);

        loadChannels();
    }

    private void loadChannels() {
        channelList.removeAllViews();
        for (String channel : FocusForgeConfig.youtubeWhitelistChannels) {
            View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.item_channel, channelList, false);
            TextView channelName = itemView.findViewById(R.id.channelName);
            channelName.setText(channel);

            View btnRemove = itemView.findViewById(R.id.btnRemove);
            btnRemove.setOnClickListener(v -> {
                FocusForgeConfig.youtubeWhitelistChannels.remove(channel);
                channelList.removeView(itemView);
            });

            channelList.addView(itemView);
        }
    }
}
