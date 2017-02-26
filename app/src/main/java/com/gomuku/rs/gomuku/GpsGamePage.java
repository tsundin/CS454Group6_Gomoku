package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;

//Google Play Game Services Imports
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by charlesjuszczak on 2/26/17.
 */

public class GpsGamePage extends GamePage implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnTurnBasedMatchUpdateReceivedListener {
    private boolean player = true;
    private GameSelection.BoardSizes boardSizeEnum = GameSelection.BoardSizes._10x10;
    private int board_size = 10;
    private GameSelection.GameTypes gameTypeEnum;
    private GameSelection.GameModes gameModeEnum;
    private GameBoard gameBoard;
    private int gameType;
    private Player player1;
    private Player player2;
    private Timer timer1;
    private Timer timer2;

    // Turn-based multi-player objects
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private static int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_LOOK_AT_MATCHES = 10001;
    private TurnBasedMatch match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();

        if(b.getBoolean("isGoogle")) {
            gameTypeEnum = GameSelection.GameTypes.Online;
            if(b.getInt("gameMode") == 0) {
                gameModeEnum = GameSelection.GameModes.Standard;
            } else {
                gameModeEnum = GameSelection.GameModes.Freestyle;
            }

            if(b.getInt("boardSize") == 10) {
                boardSizeEnum = GameSelection.BoardSizes._10x10;
            } else if(b.getInt("boardSize") == 15) {
                boardSizeEnum = GameSelection.BoardSizes._15x15;
            } else {
                boardSizeEnum = GameSelection.BoardSizes._20x20;
            }

        } else {
            gameTypeEnum = (GameSelection.GameTypes) b.getSerializable("gameType");
            gameModeEnum = (GameSelection.GameModes) b.getSerializable("gameMode");
            boardSizeEnum = (GameSelection.BoardSizes) b.getSerializable("boardSize");
        }

        switch(boardSizeEnum) {
            case _10x10: setContentView(R.layout.game_page);
                board_size = 10;
                break;
            case _15x15: setContentView(R.layout.game_page_15x15);
                board_size = 15;
                break;
            case _20x20: setContentView(R.layout.game_page_20x20);
                board_size = 20;
                break;
            default: setContentView(R.layout.game_page);
                break;
        }

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        GridLayout gridlayout = (GridLayout) findViewById(R.id.gridlayout);
        for (int i = 0; i < board_size; i++) {
            for (int j = 0; j < board_size; j++) {
                View inflatedView = View.inflate(GpsGamePage.this, R.layout.intersection_button, gridlayout);
                View justAddedIntersection = (View) findViewById(R.id.empty_intersection);
                justAddedIntersection.setId(i * board_size + j);

            }
        }

        // Initialize game objects
        this.gameBoard = new GameBoard(board_size, board_size, GetGameMode(gameModeEnum));
        this.gameType = GetGameType(gameTypeEnum);
        player1 = new Player(1);
        player2 = new Player(2);
        timer1 = new Timer();
        timer2 = new Timer();


        if(b.getBoolean("isGoogle")) {
            int id = b.getInt("button");
            ImageButton button = (ImageButton) findViewById(id);
            button.setImageResource(R.drawable.intersection_black_100px_100px);
            button.setTag("Black");
            player = !player;
            match = b.getParcelable("game");
            setParticipants(match.getParticipants(), match, true);
        }
    }

    /*******************************START GPGS FUNCTIONS*********************************/

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void onStartMatchClicked(View view) {
        // TODO : Code I added
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1, true);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        } else {
            // Alternative implementation (or warn user that they must
            // sign in to use this feature)
            Toast.makeText(GpsGamePage.this, "You must sign in to use this feature!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);

        if (request == RC_SELECT_PLAYERS) {
            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // Get auto-match criteria.
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria)
                    .build();

            // Create and start the match.
            ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                    //processResult(result);
                    match = result.getMatch();
                    //setParticipants(match.getParticipants(), match, false);
                }
            };
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(cb);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
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
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    // Call when the sign-in button is clicked
    private void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    // Call when the sign-out button is clicked
    private void signOutclicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
    }

    // Sets player name
    private void setParticipants(ArrayList<Participant> players, TurnBasedMatch match, Boolean isInvitee) {
        if(isInvitee) {
            Participant first = players.get(1);
            Participant second = players.get(0);

            player1.setName(first.getDisplayName());
            player1.setId(first.getParticipantId());
            TextView firstText = (TextView) findViewById(R.id.player1);
            firstText.setText(player1.getName());

            player2.setName(second.getDisplayName());
            player2.setId(second.getParticipantId());
            TextView secondText = (TextView) findViewById(R.id.player2);
            secondText.setText(player2.getName());
        } else {
            Participant first = players.get(0);
            Participant second = players.get(1);

            player1.setName(first.getDisplayName());
            player1.setId(first.getParticipantId());
            TextView firstText = (TextView) findViewById(R.id.player1);
            firstText.setText(player1.getName());

            player2.setName(second.getDisplayName());
            player2.setId(second.getParticipantId());
            TextView secondText = (TextView) findViewById(R.id.player2);
            secondText.setText(player2.getName());
        }
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
        byte[] data = match.getData();
        String str = new String(data, Charset.forName("US-ASCII"));
        List<String> list = new ArrayList<String>(Arrays.asList(str.split(" , ")));
        String id = list.get(3) + list.get(2);

        int x = Integer.parseInt(list.get(2));
        int y = Integer.parseInt(list.get(3));
        int stoneColor = Integer.parseInt(list.get(4));
        int buttonId = Integer.parseInt(id);

        ImageButton button = (ImageButton) findViewById(buttonId);
        if(stoneColor == 1) {
            button.setImageResource(R.drawable.intersection_white_100px_100px);
            button.setTag("White");
        } else if(stoneColor == 2) {
            button.setImageResource(R.drawable.intersection_black_100px_100px);
            button.setTag("Black");
        }
        player = !player;
    }

    public void onTurnBasedMatchRemoved (String matchId) {

    }

    /*******************************END GPGS FUNCTIONS*********************************/

    //End game
    public void endGame(View view) {
        Intent intent = new Intent(this, GameSelection.class);
        startActivity(intent);
    }

    //Restart game
    public void restartGame(View view) {
        recreate();
    }

    // Place stone on board and verify
    public int playTurn(Player player, Timer timer, int x, int y) {
        int successfulPlace = -1;

        //Start the timer
        timer.startTimer();

        while (successfulPlace != 0) {
            //Place a stone on the board
            successfulPlace = this.gameBoard.placeStone(player.getStoneColor(), x, y);
            if (successfulPlace == -1)
                System.out.println("There is already a stone there! Pick a different square.");
            if (successfulPlace == -2)
                System.out.println("Those coordinates are out of bounds!");
        }

        //Stop timer
        timer.stopTimer();

        return gameBoard.checkForWinner(player.getStoneColor(), x, y);
    }


    public void placePiece(View view) {
        ImageButton aButton = (ImageButton) view;
        int id = aButton.getId();
        int y_coord = id % board_size;
        int x_coord = id / board_size;
        FragmentManager fm = getFragmentManager();

        if (aButton.getTag() == null) {
            if (player) {
                int play = playTurn(player1, timer1, x_coord, y_coord);
                if(play == 0){
                    aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                    aButton.setTag("Black");
                    player = !player;
                    com.gomuku.rs.gomuku.GameTimerFragment player1Time = (com.gomuku.rs.gomuku.GameTimerFragment) fm.findFragmentById(R.id.timer);
                    com.gomuku.rs.gomuku.GameTimerFragment player2Time = (com.gomuku.rs.gomuku.GameTimerFragment) fm.findFragmentById(R.id.timer2);
                    player1Time.pause();
                    player2Time.resume();
                    TextView textView = (TextView)findViewById(R.id.player1);
                    textView.setBackgroundColor(0xFFFFCB3D);
                    TextView textView2 = (TextView)findViewById(R.id.player2);
                    textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
                    //TODO : Add takeTurn call to send invitation and take turns
                    //String test = "gameMode , boardSize , x_coord , y_coord , stoneColor";
                    int intMode = gameBoard.getGameMode();
                    String mode = new String(Integer.toString(intMode));
                    String size = new String(Integer.toString(board_size));
                    String x = new String(Integer.toString(x_coord));
                    String y = new String(Integer.toString(y_coord));
                    String stone = new String(Integer.toString(player1.getStoneColor()));
                    String game = mode + " , " + size + " , " + x + " , " + y + " , " + stone;
                    byte[] data = game.getBytes(Charset.forName("UTF-8"));
                    Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(), data, player2.getId());
                } else {
                    if(play == 1) {
                        System.out.println("Player 1 Wins!");
                        aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                        aButton.setTag("Black");
                        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
                        TextView winnerText = (TextView) findViewById(R.id.winnerText);
                        winnerText.setText("Winner: Player 1!\nPlay Again?");
                        layout.setVisibility(View.VISIBLE);
                    } else if(play == 2) {
                        //TODO : Change to alert dialog
                        System.out.println("Player 2 Wins!");
                    }
                }
            }
            else {
                int play = playTurn(player2, timer2, x_coord, y_coord);
                if(play == 0){
                    aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                    aButton.setTag("White");
                    player = !player;
                    TextView textView = (TextView)findViewById(R.id.player2);
                    textView.setBackgroundColor(0xFFFFCB3D);
                    TextView textView2 = (TextView)findViewById(R.id.player1);
                    textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
                    com.gomuku.rs.gomuku.GameTimerFragment player1Time = (com.gomuku.rs.gomuku.GameTimerFragment) fm.findFragmentById(R.id.timer);
                    com.gomuku.rs.gomuku.GameTimerFragment player2Time = (com.gomuku.rs.gomuku.GameTimerFragment) fm.findFragmentById(R.id.timer2);
                    player2Time.pause();
                    player1Time.resume();
                } else {
                    if(play == 1) {
                        //TODO : Change to alert dialog
                        System.out.println("Player 1 Wins!");
                    } else if(play == 2) {
                        aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                        aButton.setTag("White");
                        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
                        TextView winnerText = (TextView) findViewById(R.id.winnerText);
                        winnerText.setText("Winner: Player 2!\nPlay Again?");
                        layout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        else { // click an already-placed piece, to demo stalemate box
            LinearLayout layout = (LinearLayout) findViewById(R.id.stalemate);
            layout.setVisibility(View.VISIBLE);
        }
    }

    public int GetGameType(GameSelection.GameTypes type) {
        switch(type) {
            case Offline:
                return 0;
            case Online:
                return 1;
            case AI:
                return 2;
        }

        return -1;
    }

    public int GetGameMode(GameSelection.GameModes mode) {
        switch(mode) {
            case Standard:
                return 0;
            case Freestyle:
                return 1;
        }

        return -1;
    }


}
