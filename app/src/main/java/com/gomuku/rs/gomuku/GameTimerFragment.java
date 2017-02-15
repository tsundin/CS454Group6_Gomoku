package com.gomuku.rs.gomuku;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

/**
 * Created by charlesjuszczak on 2/1/17.
 */

public class GameTimerFragment extends Fragment {
    Timer countdown;
    TextView timerText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_timer_fragment, container, false);
        timerText = (TextView) view.findViewById(R.id.playerTimer);
        countdown = new Timer(600000, 1000, timerText);
        //startTimer();
        return view;
    }

    public void startTimer() {
        countdown.start();
    }
    public void pause() {
        countdown.pause();
    }
    public void resume() {
        long pausedTime = countdown.getPausedTime();
        countdown = new Timer(pausedTime, 1000, timerText);
        countdown.start();
    }

    public class Timer extends CountDownTimer {
        TextView text;
        long pausedTimeUntilFinished;
        boolean isPaused;

        public Timer(long startTime, long interval, TextView timerText) {
            super(startTime, interval);
            text = timerText;
            pausedTimeUntilFinished = startTime;
            isPaused = false;
        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millisToDisplay = millisUntilFinished;
            if (isPaused) {
                isPaused = false;
                pausedTimeUntilFinished = millisUntilFinished;
                cancel();
                return;
            }
            String hms= String.format("%02d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisToDisplay))
            );
            text.setText("Time: " + hms);
            //text.setText("Time remaining:\n\n" + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            text.setText("Out of time!");
        }

        public void pause() {
            isPaused = true;
        }
        public long getPausedTime() {
            return pausedTimeUntilFinished;
        }

    }
}
