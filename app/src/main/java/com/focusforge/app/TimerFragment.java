package com.focusforge.app;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timerText = view.findViewById(R.id.timerText);
        timerLabel = view.findViewById(R.id.timerLabel);
        btnStart = view.findViewById(R.id.btnStart);
        btnReset = view.findViewById(R.id.btnReset);
        workDuration = view.findViewById(R.id.workDuration);
        breakDuration = view.findViewById(R.id.breakDuration);
        sessionCount = view.findViewById(R.id.sessionCount);

        workDuration.setText((workTime / 60000) + " min");
        breakDuration.setText((breakTime / 60000) + " min");
        sessionCount.setText(String.valueOf(sessions));

        btnStart.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        updateDisplay();
    }

    private void startTimer() {
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
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
        btnStart.setText("RESUME");
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
        isWork = true;
        timeLeft = workTime;
        timerLabel.setText("WORK");
        btnStart.setText("START");
        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = (int) (timeLeft / 60000);
        int seconds = (int) (timeLeft / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            timer.cancel();
        }
    }
}
