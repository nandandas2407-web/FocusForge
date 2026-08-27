package com.focusforge.app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class TimerFragment extends Fragment {
    private TextView timerText;
    private TextView timerLabel;
    private Button btnStart;
    private Button btnReset;
    private TextView workDuration;
    private TextView breakDuration;
    private TextView sessionCount;

    private CountDownTimer timer;
    private boolean isRunning = false;
    private boolean isWork = true;
    private long timeLeft = 25 * 60 * 1000;
    private long workTime = 25 * 60 * 1000;
    private long breakTime = 5 * 60 * 1000;
    private int sessions = 0;

    private static final String KEY_TIME_LEFT = "time_left";
    private static final String KEY_IS_WORK = "is_work";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_SESSIONS = "sessions";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timerText = view.findViewById(R.id.timerText);
        timerLabel = view.findViewById(R.id.timerLabel);
        btnStart = view.findViewById(R.id.btnStart);
        btnReset = view.findViewById(R.id.btnReset);
        workDuration = view.findViewById(R.id.workDuration);
        breakDuration = view.findViewById(R.id.breakDuration);
        sessionCount = view.findViewById(R.id.sessionCount);

        if (savedInstanceState != null) {
            timeLeft = savedInstanceState.getLong(KEY_TIME_LEFT, workTime);
            isWork = savedInstanceState.getBoolean(KEY_IS_WORK, true);
            isRunning = savedInstanceState.getBoolean(KEY_IS_RUNNING, false);
            sessions = savedInstanceState.getInt(KEY_SESSIONS, 0);
        }

        workDuration.setText((workTime / 60000) + " min");
        breakDuration.setText((breakTime / 60000) + " min");
        sessionCount.setText(String.valueOf(sessions));

        btnStart.setOnClickListener(v -> {
            if (isRunning) pauseTimer();
            else startTimer();
        });

        btnReset.setOnClickListener(v -> resetTimer());

        updateDisplay();
        if (isRunning) startTimer();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_TIME_LEFT, timeLeft);
        outState.putBoolean(KEY_IS_WORK, isWork);
        outState.putBoolean(KEY_IS_RUNNING, isRunning);
        outState.putInt(KEY_SESSIONS, sessions);
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateDisplay();
            }

            @Override
            public void onFinish() {
                if (isWork) {
                    sessions++;
                    sessionCount.setText(String.valueOf(sessions));
                    isWork = false;
                    timeLeft = breakTime;
                    timerLabel.setText("BREAK");
                } else {
                    isWork = true;
                    timeLeft = workTime;
                    timerLabel.setText("WORK");
                }
                isRunning = false;
                btnStart.setText("START");
                updateDisplay();
            }
        }.start();
        isRunning = true;
        btnStart.setText("PAUSE");
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        btnStart.setText("RESUME");
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        isWork = true;
        timeLeft = workTime;
        timerLabel.setText("WORK");
        btnStart.setText("START");
        updateDisplay();
    }

    private void updateDisplay() {
        if (!isAdded()) return;
        int minutes = (int) (timeLeft / 60000);
        int seconds = (int) (timeLeft / 1000) % 60;
        timerText.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
}
