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
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.game_timer_fragment, container, false);
        return view;
    }

    public Timer createTimer() {
        return countdown = new Timer(view);
    }
}
