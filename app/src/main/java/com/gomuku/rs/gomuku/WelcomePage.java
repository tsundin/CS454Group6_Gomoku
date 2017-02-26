package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rs on 1/26/17.
 */
public class WelcomePage extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    final static int RC_INVITATION_INBOX = 10001;
    private static int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_LOOK_AT_MATCHES = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        mGoogleApiClient.connect();

        if(mGoogleApiClient.isConnected()) {
            Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
            startActivityForResult(intent, RC_INVITATION_INBOX);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
        if (connectionHint != null) {
            TurnBasedMatch match = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    //Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                    Toast.makeText(WelcomePage.this, "Not Connected!",
                            Toast.LENGTH_SHORT).show();
                }

                byte[] data = match.getData();
                String str = new String(data, Charset.forName("US-ASCII"));
                List<String> list = new ArrayList<String>(Arrays.asList(str.split(" , ")));
                String id = list.get(2) + list.get(3);

                Toast.makeText(WelcomePage.this, str,
                        Toast.LENGTH_SHORT).show();
                //updateMatch(mTurnBasedMatch);
                Bundle b = new Bundle();
                b.putParcelable("game", match);

                Intent intent = new Intent(this, GamePage.class);
                intent.putExtra("isGoogle", true);
                intent.putExtra("gameType", 1);
                intent.putExtra("gameMode", Integer.parseInt(list.get(0)));
                intent.putExtra("boardSize", Integer.parseInt(list.get(1)));
                intent.putExtra("x_coord", Integer.parseInt(list.get(3)));
                intent.putExtra("y_coord", Integer.parseInt(list.get(2)));
                intent.putExtra("stoneColor", Integer.parseInt(list.get(4)));
                intent.putExtra("button", Integer.parseInt(id));
                intent.putExtra("matchId", match.getMatchId());
                intent.putExtras(b);

                startActivity(intent);

                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getResources().getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendMessage(View view)
    {
        switch(view.getId()) {
            case (R.id.HomePagebutton):
                Intent gameSelection = new Intent(this, GameSelection.class);
                startActivity(gameSelection);
                break;
            case (R.id.GameRulesButton):
                Intent gameRules = new Intent(this, GameRules.class);
                startActivity(gameRules);
                break;
        }
    }

}
