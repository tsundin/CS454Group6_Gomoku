package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
//Google Play Game Services Imports
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by charlesjuszczak on 2/26/17.
 */

public class GamePage extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnTurnBasedMatchUpdateReceivedListener {
    private boolean isMyTurn = true;
    private GameSelection.BoardSizes boardSizeEnum = GameSelection.BoardSizes._10x10;
    private int board_size = 10;
    private GameSelection.GameTypes gameTypeEnum;
    private GameSelection.GameModes gameModeEnum;
    private GameBoard gameBoard;
    private int gameType;
    private Player player1;
    private Player player2;
    private Player thisPlayer;
    private Player opponentPlayer;

    private BluetoothAdapter BA;
    private BluetoothChatService mChatService = null;
    private Set<BluetoothDevice> pairedDevices;
    private AIPlayer2 ai;
    ListView lv;
    Button b1,b2,b3,b4;
    boolean isPaired = false;
    boolean iHoldBlackPieces = false;

    private Timer player1Time;
    private Timer player2Time;
    private Timer thisPlayerTime;
    private Timer opponentPlayerTime;

    // Turn-based multi-isMyTurn objects
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private static int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    private TurnBasedMatch match;
    private Bundle incomingMatch;
    public  BluetoothDevice finalBondedDevice;

    /**
     * Creates the GamePage view.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        // Creating the view from the Google-initiated WelcomePage requires getting the
        // setup info from the Bundle b.
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

        } else { // if not in Google mode, get the setup info straight from the SetupPage bundle.
            gameTypeEnum = (GameSelection.GameTypes) b.getSerializable("gameType");
            gameModeEnum = (GameSelection.GameModes) b.getSerializable("gameMode");
            boardSizeEnum = (GameSelection.BoardSizes) b.getSerializable("boardSize");
        }

        // Setup board size
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

        // Lock GamePage to Portrait Mode.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Generic buttons. b2 is used to create the Google game.
        b1 = (Button) findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        b4=(Button)findViewById(R.id.button4);

        // Initialize the various layout elements.
        initializePlayers();
        initializeTimers();
        initializeLayout();

        // Initialize Bluetooth, if in Bluetooth mode.
        if (gameTypeEnum == GameSelection.GameTypes.OnlineBT) {
            initializeBluetoothConnection();

        }
        // Initialize Google Play Services, if in Online mode
        if (gameTypeEnum == GameSelection.GameTypes.Online) {
            initializeGooglePlayConnection(b);
        }
        // Initialize AI, if in AI mode.
        if (gameTypeEnum == GameSelection.GameTypes.AI) {
            initializeAI();

        }
        // TODO: This code was needed even in offline/bluetooth modes. Else we get a crash.
        // It should be in the if (gameTypeEnum == .Online), above?
        if (gameTypeEnum != GameSelection.GameTypes.Online) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    // add other APIs and scopes here as needed
                    .build();
        }
    }

    private void initializeLayout() {
        initializeBoard();
        initializeWins();
    }

    /**
     * Setup the players for AI mode.
     * The AI is always set to player 2, and as the opponent player.
     */
    private void initializeAI() {

        player2 = new AIPlayer2();
        thisPlayer = player1;
        opponentPlayer = player2;
    }

    /**
     * Initialize the views for representing the number of wins.
     */
    private void initializeWins() {
        TextView wins = (TextView) findViewById(R.id.player1_wins);
        wins.setText("Wins: " + player1.getWins());
        wins = (TextView) findViewById(R.id.player2_wins);
        wins.setText("Wins: " + player2.getWins());
        player1Time.reset();
        player2Time.reset();
        player1Time.pause();
        player2Time.pause();
        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
        layout.setVisibility(View.INVISIBLE);
        layout = (LinearLayout) findViewById(R.id.stalemate);
        layout.setVisibility(View.INVISIBLE);
    }

    /**
     * (Re)Initialize the game board, clearing all already placed pieces.
     * Create the intersections programmatically, in a board_size x board_size grid.
     * Also create the underlying GameBoard model object.
     */
    private void initializeBoard() {
        GridLayout gridlayout = (GridLayout) findViewById(R.id.gridlayout);
        gridlayout.removeAllViews();
        for (int i = 0; i < board_size; i++) {
            for (int j = 0; j < board_size; j++) {
                View inflatedView = View.inflate(GamePage.this, R.layout.intersection_button, gridlayout);
                View justAddedIntersection = (View) findViewById(R.id.empty_intersection);
                justAddedIntersection.setId(i * board_size + j);
            }
        }
        this.gameBoard = new GameBoard(board_size, board_size, GetGameMode(gameModeEnum));
        this.gameType = GetGameType(gameTypeEnum);
    }

    /**
     * Initialize players. Under these defaults, player 1 always starts as the black stone (1),
     * and player 2 the white (2). Note that these defaults may be sometimes overridden
     * elsewhere in the code. E.g. The AI mode overrides these initial settings to replace
     * player 2 with the AI. Online modes may swap thisPlayer and opponentPlayer depending
     * on which device initiated the online session.
     */
    private void initializePlayers() {
        player1 = new Player(1);
        player2 = new Player(2);
        thisPlayer = player1;
        opponentPlayer = player2;
    }

    /**
     * Initialize player time fragments.
     */
    private void initializeTimers() {
        FragmentManager fm = getFragmentManager();
        GameTimerFragment player1TimeFragment = (GameTimerFragment) fm.findFragmentById(R.id.timer);
        GameTimerFragment player2TimeFragment = (GameTimerFragment) fm.findFragmentById(R.id.timer2);
        player1Time = player1TimeFragment.createTimer();
        player2Time = player2TimeFragment.createTimer();
        thisPlayerTime = player1Time;
        opponentPlayerTime = player2Time;

    }

    /**
     * Restart game. The reinitialization affects everything -except- the two players,
     * so we can track player wins from game to game.
     */
    public void restartGame(View view) {
        initializeLayout();
    }

    /**
     * If the user chooses to end the game, go back to the GameSelection view.
     * @param view The view that caused us to end the game.
     */
    public void endGame(View view) {
        //mChatService.stop();
        super.onDestroy ();
        Intent intent = new Intent(this, GameSelection.class);
        startActivity(intent);
    }

    /**
     * Place stone on the gameBoard model and return the result of the play.
     * @return 0 on successful play and no winner yet,
     *         1 if play was successful and player 1 was the winner for this turn.
     *         2 if play was successful and player 2 was the winner for this turn.
     *         3 if already a stone at that location, or if coordinates are out of bounds.
     */
    public int playTurn(Player player, Timer timer, int x, int y) {
        int successfulPlace = -1;

        while (successfulPlace != 0) {
            //Place a stone on the board
            successfulPlace = this.gameBoard.placeStone(player.getStoneColor(), x, y);
            if (successfulPlace == 0) break; // Successful place. Fall-through to checkForWinner.
            if (successfulPlace == -1) // GameBoard indicates already a stone in that location.
                Toast.makeText(getApplicationContext(), "There is already a stone there! Pick a different square.", Toast.LENGTH_LONG).show();
            if (successfulPlace == -2) // GameBoard indicates that location is out of bounds.
                Toast.makeText(getApplicationContext(), "Those coordinates are out of bounds!", Toast.LENGTH_LONG).show();
            return 3;
        }
        // Fall through to here on successfully placing a piece. Check for winner
        // returns 0 if no winner; 1 if player 1 wins; 2 if player 2 wins
        return gameBoard.checkForWinner(player.getStoneColor(), timer.isTimerExpired(), x, y);
    }

    /**
     * Proxy for placing the piece from the board_size x board_size intersection imageViews.
     * ImageViews do not have access to which player is currently playing.
     * @param view The intersection imageView that was touched.
     */
    public void placePiece(View view) {
        placePiece(view, thisPlayer);

    }

    /**
     * Logic for placing the piece on the game board.
     * @param view The view (e.g. an intersection on the board) that requested a piece be played.
     * @param player The player that is requesting the piece be played.
     */
    public void placePiece(View view, Player player) {
        int play = -1;
        // Extract the id containing the (x,y) coordinates of where to place the piece
        ImageButton aButton = (ImageButton) view;
        int id = aButton.getId();
        int y_coord = id % board_size;
        int x_coord = id / board_size;

        // Only place a piece if no piece is currently placed there, and either
        // it is my turn and I am the player playing, or it is not my turn and the opponent
        // is playing a piece.
        if (aButton.getTag() == null) { // only play if a stone has not already been placed there
            if (isMyTurn && player == thisPlayer) {
                // Play the piece
                play = playTurn(player, thisPlayerTime, x_coord, y_coord);
                // Only send the coordinate via Bluetooth/Google if it's my turn and I am the player.
                writeStoneToBluetooth(player, id);
                writeStoneToGPS(player, x_coord, y_coord, play, id);
                // Swap the timers (this player just played)
                thisPlayerTime.pause();
                opponentPlayerTime.resume();

            }
            else if (!isMyTurn && player == opponentPlayer) { // Swap the timers (opponent just played)
                // Play the piece
                play = playTurn(player, opponentPlayerTime, x_coord, y_coord);
                // Swap the timers (opponent just played)
                opponentPlayerTime.pause();
                thisPlayerTime.resume();
            }
            if(play == 0){ // successful play, no winner
                if (player.getStoneColor() == 1) {
                    drawBlackStone(aButton);
                    highlightPlayer2();
                }
                else if (player.getStoneColor() == 2) {
                    drawWhiteStone(aButton);
                    highlightPlayer1();
                }
                if(gameBoard.isBoardFull()) {
                    LinearLayout layout = (LinearLayout) findViewById(R.id.stalemate);
                    layout.setVisibility(View.VISIBLE);
                } else {
                    changeTurns(); // Swap players, depending on the game mode
                }
            }
            else if(play == 1) { // player 1 wins
                drawBlackStone(aButton);
                player1Wins(player1);

            }
            else if(play == 2) { // player 2 wins
                drawWhiteStone(aButton);
                player2Wins(player2);
            }
        }
    }


    /**
     * Swap the players, depending on the game mode.
     */
    private void changeTurns() {
        // Offline mode: Swap players, and it always remains my turn.
        if (gameTypeEnum == GameSelection.GameTypes.Offline) {
            playerSwap();
            isMyTurn = true;
        }
        // AI mode where the AI is playing.
        // AI places its piece on the game board here.
        else if (gameTypeEnum == GameSelection.GameTypes.AI && opponentPlayer == player2) {
            int [] rawId;
            rawId = opponentPlayer.playTurn(gameBoard); // Get which piece to play from the AI
            int id = rawId[0] * board_size + rawId[1]; // Decode the ID
            ImageButton button = (ImageButton) findViewById(id);

            playerSwap(); // Swap players so it is the AI's turn before calling placePiece
            placePiece(button, thisPlayer); // AI places piece. playerSwap will be called again
            highlightPlayer1();
            isMyTurn = true;
        }
        // AI mode where the AI just played. Swap the player back to player 1
        else if (gameTypeEnum == GameSelection.GameTypes.AI && thisPlayer == player2) {
            playerSwap();
            isMyTurn = true;
        }
        else { // online modes don't swap. just change turns.
            isMyTurn = !isMyTurn;
        }
    }

    /**
     * Swap thisPlayer and opponentPlayer, along with which timer is active.
     */
    private void playerSwap() {
        Player swap = thisPlayer;
        thisPlayer = opponentPlayer;
        opponentPlayer = swap;

        Timer swapTime = thisPlayerTime;
        thisPlayerTime = opponentPlayerTime;
        opponentPlayerTime = swapTime;
    }

    private void highlightPlayer1() {
        TextView textView = (TextView)findViewById(R.id.player2);
        textView.setBackgroundColor(0xFFFFCB3D);
        TextView textView2 = (TextView)findViewById(R.id.player1);
        textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
        textView2.setBackgroundResource(R.drawable.rounded_bg);
        textView.setBackgroundResource(0);
    }

    private void highlightPlayer2() {
        TextView textView = (TextView)findViewById(R.id.player1);
        textView.setBackgroundColor(0xFFFFCB3D);
        TextView textView2 = (TextView)findViewById(R.id.player2);
        textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
        textView2.setBackgroundResource(R.drawable.rounded_bg);
        textView.setBackgroundResource(0);
    }

    /**
     * Send which stone was place in Online mode via Google Play Service.
     * @param thisPlayer Which player is currently playing
     * @param x_coord, y_coord The (x,y) coordinate to play
     */
    private void writeStoneToGPS(Player thisPlayer, int x_coord, int y_coord, int status, int buttonId) {
        if (gameTypeEnum == GameSelection.GameTypes.Online) {
            int intMode = gameBoard.getGameMode();
            String mode = new String(Integer.toString(intMode));
            String size = new String(Integer.toString(board_size));
            String x = new String(Integer.toString(x_coord));
            String y = new String(Integer.toString(y_coord));
            String id = new String(Integer.toString(buttonId));
            String stone = new String(Integer.toString(thisPlayer.getStoneColor()));
            String game = mode + " , " + size + " , " + x + " , " + y + " , " + stone + " , " + id;
            byte[] data = game.getBytes(Charset.forName("UTF-8"));

            ResultCallback<TurnBasedMultiplayer.UpdateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                    //processResult(result);
                    System.out.println("Match updated!");
                }
            };

            //If move results in winner finish match, else take turn
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
                                                data, opponentPlayer.getId())
                                                .setResultCallback(cb);

            if(status != 0) {
                Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, match.getMatchId());
            }
        }
    }

    /**
     * Send which stone was place in Bluetooth mode via Bluetooth.
     * @param thisPlayer Which player is currently playing
     * @param id The (x,y) coordinate to play
     */
    private void writeStoneToBluetooth(Player thisPlayer, int id) {
        if (gameTypeEnum == GameSelection.GameTypes.OnlineBT && isPaired) {
            String message = Integer.toString(id);
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    /**
     * Set the imageView for the intersection from blank to a black stone.
     * @param aButton The imageView button
     */
    private void drawBlackStone(ImageButton aButton) {
        aButton.setImageResource(R.drawable.intersection_black_100px_100px);
        aButton.setTag("Black");

    }

    /**
     * Set the imageView for the intersection from blank to a white stone.
     * @param aButton The imageView button
     */
    private void drawWhiteStone(ImageButton aButton) {
        aButton.setImageResource(R.drawable.intersection_white_100px_100px);
        aButton.setTag("White");

    }

    /**
     * Initialize the Player Wins pop-over and set it to visible.
     * @param player Update the recording of player wins count
     */
    private void player1Wins(Player player) {
        player.incrementWins();
        TextView wins = (TextView) findViewById(R.id.player1_wins);
        wins.setText("Wins: " + player.getWins());
        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
        TextView winnerText = (TextView) findViewById(R.id.winnerText);
        winnerText.setText("Winner: Player 1!\nPlay Again?");
        layout.setVisibility(View.VISIBLE);
    }
    private void player2Wins(Player player) {
        player.incrementWins();
        TextView wins = (TextView) findViewById(R.id.player2_wins);
        wins.setText("Wins: " + player.getWins());
        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
        TextView winnerText = (TextView) findViewById(R.id.winnerText);
        winnerText.setText("Winner: Player 2!\nPlay Again?");
        layout.setVisibility(View.VISIBLE);
    }



    /************************** Bluetooth Helper Methods *******************************/
    private void initializeBluetoothConnection() {
        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.listView);

        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
        pairedDevices = BA.getBondedDevices();
        ArrayList<String> pairedDeviceNames = new ArrayList<String>();
        for(BluetoothDevice device: pairedDevices) {
            pairedDeviceNames.add(device.getName());
        }
        final List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(pairedDevices);
        final CharSequence[] fol_list = pairedDeviceNames.toArray(new CharSequence[pairedDeviceNames.size()]);

        mChatService = new BluetoothChatService(this, mHandler);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        builder.setTitle("Pick a device to pair with");
        builder.setItems(fol_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                finalBondedDevice = deviceList.get(which);
                isPaired = true;
                String pairedDeviceName = finalBondedDevice.getName();
                Log.i("Paired to device", pairedDeviceName);
                String myName = BA.getName();
                if (myName.compareTo(pairedDeviceName) < 0) {

                    iHoldBlackPieces = true;
                    isMyTurn = true;
                } else {
                    iHoldBlackPieces = false;
                    isMyTurn = false;
                    thisPlayer = player2;
                    thisPlayerTime = player2Time;
                    opponentPlayer = player1;
                    opponentPlayerTime = player1Time;
                }
                mChatService.connect(finalBondedDevice, false);

            }
        });
        builder.show();
        Log.i("here","here");

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


    // Helper method for establishing Bluetooth connection
    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    // Helper method for disabling Bluetooth connection
    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }

    // Helper method showing Bluetooth connection
    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    // Helper method for listing Bluetooth connections
    public void list(View v){
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.GP_Layout);
        rl.setVisibility(LinearLayout.INVISIBLE);
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();


        for(BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // FragmentActivity activity = getActivity();
            switch (msg.what) {
                /*
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(GamePage.this, "Connected", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(GamePage.this, "Connecting", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(GamePage.this, "Closing Connection", Toast.LENGTH_SHORT).show();

                            mChatService.stop();
                            break;
                    }
                    break;
                */
                /**
                 * Transmitting logic for Bluetooth mode.
                 * Send which piece to play to the other device.
                 */
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    //construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(getApplicationContext(), writeMessage,Toast.LENGTH_SHORT).show();
                    break;
                /**
                 * Receiving logic for Bluetooth mode.
                 * Get which piece to play from the other device.
                 */
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMessage,Toast.LENGTH_SHORT).show();
                    int id = Integer.parseInt(readMessage);
                    ImageButton button = (ImageButton) findViewById(id);
                    placePiece(button, opponentPlayer);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    /*mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }*/
                    break;
                case Constants.MESSAGE_TOAST:
                   /* if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }*/
                    break;
            }
        }
    };

    /************************** End of Bluetooth Helper Methods *******************************/

    /*******************************START GPGS FUNCTIONS*********************************/

    /**
     * Initialization for Online mode.
     * Also does some game initialization logic and transmits the initial piece to the other device.
     */
    private void initializeGooglePlayConnection(Bundle b) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        if(b.getBoolean("isGoogle")) {
            // Initialize players for this device and send initial piece to other device.
            incomingMatch = b;
        } else {
            LinearLayout layout = (LinearLayout) findViewById(R.id.google_play_start);
            layout.setVisibility(View.VISIBLE);
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void onCheckForInvitesClicked(View view) {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
            startActivityForResult(intent, RC_INVITATION_INBOX);
        }
    }

    public void onStartMatchClicked(View view) {
        // TODO : Code I added
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1, true);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        } else {
            // Alternative implementation (or warn user that they must
            // sign in to use this feature)
            Toast.makeText(GamePage.this, "You must sign in to use this feature!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (request == RC_INVITATION_INBOX) {
            if(response != Activity.RESULT_OK) {
                return;
            }

            match = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if(match != null) {

            }
        }

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
                    setParticipants(match.getParticipants(), match);
                    LinearLayout layout = (LinearLayout) findViewById(R.id.google_play_start);
                    layout.setVisibility(View.GONE);
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
        // The isMyTurn is signed in. Hide the sign-in button and allow the
        // isMyTurn to proceed.
        //mGoogleApiClient.registerConnectionCallbacks(this);
        if(incomingMatch != null) {
            match = incomingMatch.getParcelable("game");
            //Button start = (Button) findViewById(R.id.start_button);
            //Button check = (Button) findViewById(R.id.check_for_invites);
            //start.setVisibility(View.GONE);
            //check.setVisibility(View.GONE);
        }

        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);

        if(incomingMatch != null) {
            setParticipants(match.getParticipants(), match);
            int id = incomingMatch.getInt("button");
            ImageButton button = (ImageButton) findViewById(id);
            thisPlayer = player2;
            thisPlayerTime = player2Time;
            opponentPlayer = player1;
            opponentPlayerTime = player1Time;
            isMyTurn = false;
            placePiece(button, opponentPlayer);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flowf
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
          //  if (!BaseGameUtils.resolveConnectionFailure(this,
              //      mGoogleApiClient, connectionResult,
                //    RC_SIGN_IN, getResources().getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button


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
    private void setParticipants(ArrayList<Participant> players, TurnBasedMatch match) {
        final int n = 7;
        Participant first = players.get(0);
        Participant second = players.get(1);

        if (first.getDisplayName().length() > n) { // shorten long names
            thisPlayer.setName(first.getDisplayName().substring(0, n));
        }
        else {
            thisPlayer.setName(first.getDisplayName());
        }
        thisPlayer.setId(first.getParticipantId());
        TextView firstText = (TextView) findViewById(R.id.player1);
        firstText.setText(thisPlayer.getName());

        if (first.getDisplayName().length() > n) { // shorten long names
            opponentPlayer.setName(second.getDisplayName().substring(0, n));
        }
        else {
            opponentPlayer.setName(second.getDisplayName());
        }

        opponentPlayer.setId(second.getParticipantId());
        TextView secondText = (TextView) findViewById(R.id.player2);
        secondText.setText(opponentPlayer.getName());
    }

    /**
     * Receiving logic for Online mode.
     * Get which piece to play from the other device.
     */
    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
        byte[] data = match.getData();
        String str = new String(data, Charset.forName("US-ASCII"));
        List<String> list = new ArrayList<String>(Arrays.asList(str.split(" , ")));
        //String id = list.get(2) + list.get(3);

        int x = Integer.parseInt(list.get(2));
        int y = Integer.parseInt(list.get(3));
        int stoneColor = Integer.parseInt(list.get(4)); // playPiece determines this from opponentPlayer now.
        int buttonId = Integer.parseInt(list.get(5));

        ImageButton button = (ImageButton) findViewById(buttonId);
        isMyTurn = false;
        placePiece(button, opponentPlayer);
        isMyTurn = true;

/*
        if(stoneColor == 1) {
            drawBlackStone(button);
        } else if(stoneColor == 2) {
            drawWhiteStone(button);
        }

        int play = playTurn(opponentPlayer, player1Time, x, y);
        if (play == 1) {
            player1Wins();
        }
        else if (play == 2) {
            player2Wins();
        }
        isMyTurn = true;
*/
    }

    public void onTurnBasedMatchRemoved (String matchId) {
        mGoogleApiClient.disconnect();
    }

    /*******************************END GPGS FUNCTIONS*********************************/
}
