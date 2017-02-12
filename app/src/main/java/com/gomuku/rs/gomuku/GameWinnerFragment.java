package com.gomuku.rs.gomuku;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

/**
 * Created by sfabini on 2/11/17.
 */

public class GameWinnerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_winner_fragment, container, false);
        TextView timerText = (TextView) view.findViewById(R.id.playerTimer);
        Timer countdown = new Timer(600000, 1000, timerText);
        countdown.start();
        return view;
    }

    public class Timer extends CountDownTimer {
        TextView text;

        public Timer(long startTime, long interval, TextView timerText) {
            super(startTime, interval);
            text = timerText;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            /*
            String hms= String.format("%02d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
            );
            text.setText("Time remaining:\n\n" + hms);
            //text.setText("Time remaining:\n\n" + millisUntilFinished / 1000);
            */
        }

        @Override
        public void onFinish() {
            text.setText("Out of time!");
        }
    }
}
