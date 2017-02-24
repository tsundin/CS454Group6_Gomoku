package com.gomuku.rs.gomuku;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Timer controls two CountDownTimers, a primary counter nominally set for 10 minutes, and a
 * secondary counter nominally set for 1 minute. When the primary counter expires, the secondary
 * timer begins counting. When the secondary timer expires, an "Out of Time" message is sent
 * to the view.
 */

public class Timer {
    private static final long primaryDefaultTime = 1 * 60000;
    private static final long secondaryDefaultTime = 60000;
    private static final long defaultInterval = 1000;
    CountDownTimer primaryTimer;
    CountDownTimer secondaryTimer;

    long pausedTimeUntilFinished;
    boolean isPaused;
    TextView textView;
    boolean primaryTimerExpired;
    boolean secondaryTimerExpired;


    public Timer() {
        // TODO: Timer currently coupled into GameLogic, which requires this constructor.
        // Need to determine how the Winner/Loser is triggered by the timer fully expiring.
        // Will it be in GameLogic, or GameBoard?
    }

    public Timer(View view) {
        textView = (TextView) view.findViewById(R.id.playerTimer);
        pausedTimeUntilFinished = primaryDefaultTime;
        isPaused = false;
        primaryTimerExpired = false;
        secondaryTimerExpired = false;
        primaryTimer = createPrimaryCountDownTimer(primaryDefaultTime, defaultInterval);
        secondaryTimer = createSecondaryCountDownTimer(secondaryDefaultTime, defaultInterval);
    }

    /**
     * Factory method for recreating a new primary CountDownTimer with a new initial timeout.
     * This is necessary, because CountDownTimer does not have a pause/resume capability. It
     * must be destroyed and recreated with the last paused time. The management of the pausing
     * is handled here within the Timer class. On expiration, the primary CountDownTimer
     * kicks off the secondary CountDownTimer. Whereas on expiration of the secondary
     * CountDownTimer, a message indication is sent to the view.
     * @param time The countdown time in milliseconds
     * @param interval The countdown interval in milliseconds
     * @return The new CountDownTimer instance
     */
    public CountDownTimer createPrimaryCountDownTimer(long time, long interval) {
        return new CountDownTimer(time, interval) {
            @Override
            public void onTick(long l) {
                timerOnTick(l);
            }

            @Override
            public void onFinish() {
                primaryTimerExpired = true;
                pausedTimeUntilFinished = secondaryDefaultTime;
                secondaryTimer.start();
            }
        };
    }

    public CountDownTimer createSecondaryCountDownTimer(long length, long interval) {
        return new CountDownTimer(length, interval) {
            @Override
            public void onTick(long l) {
                timerOnTick(l);
            }

            @Override
            public void onFinish() {
                secondaryTimerExpired = true;
                textView.setText("Out of time!");
            }
        };
    }

    /**
     * The common onTick method for both primary and secondary CountDownTimers.
     * Manages pausing and updating the view.
     * @param currentTime The current countdown time
     */
    public void timerOnTick(long currentTime) {
        long millisToDisplay = currentTime;
        if (isPaused) {
            isPaused = false;
            pausedTimeUntilFinished = currentTime;
            primaryTimer.cancel();
            secondaryTimer.cancel();
            return;
        }
        String hms= String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentTime),
                TimeUnit.MILLISECONDS.toSeconds(currentTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisToDisplay))
        );
        textView.setText("Time: " + hms);
    }

    public void resume() {
        if (primaryTimerExpired) {
            secondaryTimer = createSecondaryCountDownTimer(secondaryDefaultTime, defaultInterval);
            secondaryTimer.start();
        }
        else {
            primaryTimer = createPrimaryCountDownTimer(pausedTimeUntilFinished, defaultInterval);
            primaryTimer.start();
        }
        isPaused = false;
    }

    public void pause() { isPaused = true; }

    public boolean isTimerExpired() { return secondaryTimerExpired; }
}


