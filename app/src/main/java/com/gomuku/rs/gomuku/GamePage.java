package com.gomuku.rs.gomuku;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

/**
 * Created by rs on 1/27/17.
 */
public class GamePage extends Activity {
    private boolean player = true;
    private GameSelection.BoardSizes boardSizeEnum = GameSelection.BoardSizes._10x10;
    private int board_size = 10;
    private GameSelection.GameTypes gameTypeEnum;
    private GameSelection.GameModes gameModeEnum;
    private GameBoard gameBoard;
    private int gameType;
    private Player player1;
    private Player player2;

    private BluetoothAdapter BA;
    private BluetoothChatService mChatService = null;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    Button b1,b2,b3,b4;
    boolean isPaired = false;
    boolean iHoldBlackPieces = false;

    private Timer player1Time;
    private Timer player2Time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();

        gameTypeEnum = (GameSelection.GameTypes) b.getSerializable("gameType");
        gameModeEnum = (GameSelection.GameModes) b.getSerializable("gameMode");
        boardSizeEnum = (GameSelection.BoardSizes) b.getSerializable("boardSize");

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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        b1 = (Button) findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        b4=(Button)findViewById(R.id.button4);

        /**/
        /**** Initialize Bluetooth connection *****/
        //this.gameType = GetGameType(gameTypeEnum);
        if (gameTypeEnum == GameSelection.GameTypes.Online) {
            initializeBluetoothConnection();
        }

        player1 = new Player(1);
        player2 = new Player(2);
        FragmentManager fm = getFragmentManager();
        GameTimerFragment player1TimeFragment = (GameTimerFragment) fm.findFragmentById(R.id.timer);
        GameTimerFragment player2TimeFragment = (GameTimerFragment) fm.findFragmentById(R.id.timer2);
        player1Time = player1TimeFragment.createTimer();
        player2Time = player2TimeFragment.createTimer();
        initializeLayout();

    }



    //Restart game. This reinitializes everything except player1/2, so we can track wins
    public void restartGame(View view) {
        initializeLayout();
    }

    private void initializeLayout() {
        GridLayout gridlayout = (GridLayout) findViewById(R.id.gridlayout);

        gridlayout.removeAllViews();
        for (int i = 0; i < board_size; i++) {
            for (int j = 0; j < board_size; j++) {
                View inflatedView = View.inflate(GamePage.this, R.layout.intersection_button, gridlayout);
                View justAddedIntersection = (View) findViewById(R.id.empty_intersection);
                justAddedIntersection.setId(i * board_size + j);
            }
        }
        gridlayout.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(GamePage.this, "Clicked " + boardSizeEnum ,
                        Toast.LENGTH_SHORT).show();
            }
        });
        // Re-Initialize game objects
        this.gameBoard = new GameBoard(board_size, board_size, GetGameMode(gameModeEnum));

        this.gameType = GetGameType(gameTypeEnum);
        // Initialize Wins
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

    //End game
    public void endGame(View view) {
        //mChatService.stop();
        super.onDestroy ();
        Intent intent = new Intent(this, GameSelection.class);
        startActivity(intent);
    }

    // Place stone on board and verify
    public int playTurn(Player player, Timer timer, int x, int y) {
        int successfulPlace = -1;

        while (successfulPlace != 0) {
            //Place a stone on the board
            successfulPlace = this.gameBoard.placeStone(player.getStoneColor(), x, y);
            if (successfulPlace == 0) break;
            if (successfulPlace == -1)
                System.out.println("There is already a stone there! Pick a different square.");
            if (successfulPlace == -2)
                System.out.println("Those coordinates are out of bounds!");
            return 3;
        }

        return gameBoard.checkForWinner(player.getStoneColor(), timer.isTimerExpired(), x, y);
    }

    public void placePiece(View view) {
        // Get the coordinate from the button
        ImageButton aButton = (ImageButton) view;
        int id = aButton.getId();
        int y_coord = id % board_size;
        int x_coord = id / board_size;

        // Black pieces means player == true, so
        if (isPaired) {
            if (iHoldBlackPieces) {
                player = true;
            } else {
                player = false;
            }
        }
        if (aButton.getTag() == null) {
            if (player) {


                int play = playTurn(player1, player1Time, x_coord, y_coord);

                Log.i("PLAY", Integer.toString(play));
                String message = Integer.toString(id);
                if (isPaired) {
                    byte[] send = message.getBytes();
                    mChatService.write(send);
                }

                if (play == 0) {
                    Toast.makeText(getApplicationContext(), "Coord: " + x_coord + ", " + y_coord, Toast.LENGTH_LONG).show();

                    aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                    aButton.setTag("Black");
                    player = !player;
                    player1Time.pause();
                    player2Time.resume();
                    TextView textView = (TextView) findViewById(R.id.player1);
                    textView.setBackgroundColor(0xFFFFCB3D);
                    TextView textView2 = (TextView) findViewById(R.id.player2);
                    textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
                } else {
                    if (play == 1) {
                        player1.incrementWins();
                        TextView wins = (TextView) findViewById(R.id.player1_wins);
                        wins.setText("Wins: " + player1.getWins());
                        player1Time.pause();
                        player2Time.pause();
                        System.out.println("Player 1 Wins!");
                        aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                        aButton.setTag("Black");
                        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
                        TextView winnerText = (TextView) findViewById(R.id.winnerText);
                        winnerText.setText("Winner: Player 1!\nPlay Again?");
                        layout.setVisibility(View.VISIBLE);
                    } else if (play == 2) {
                        player2.incrementWins();
                        TextView wins = (TextView) findViewById(R.id.player2_wins);
                        wins.setText("Wins: " + player2.getWins());
                        player1Time.pause();
                        player2Time.pause();
                        System.out.println("Player 2 Wins!");
                        aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                        aButton.setTag("White");
                        LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
                        TextView winnerText = (TextView) findViewById(R.id.winnerText);
                        winnerText.setText("Winner: Player 2!\nPlay Again?");
                        layout.setVisibility(View.VISIBLE);
                    }
                }
                // wait for opponent move from bluetooth
                if (isPaired) {
                    //wait
                }
            } else {
                int play = playTurn(player2, player2Time, x_coord, y_coord);
                String message = Integer.toString(id);

                if (isPaired) {
                    byte[] send = message.getBytes();
                    mChatService.write(send);
                } else {

                    if (play == 0) {
                        Toast.makeText(getApplicationContext(), "Coord: " + x_coord + ", " + y_coord, Toast.LENGTH_LONG).show();
                        aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                        aButton.setTag("White");
                        player = !player;
                        TextView textView = (TextView) findViewById(R.id.player2);
                        textView.setBackgroundColor(0xFFFFCB3D);
                        TextView textView2 = (TextView) findViewById(R.id.player1);
                        textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
                        player2Time.pause();
                        player1Time.resume();
                    } else {
                        if (play == 1) {
                            player1.incrementWins();
                            TextView wins = (TextView) findViewById(R.id.player1_wins);
                            wins.setText("Wins: " + player1.getWins());
                            player1Time.pause();
                            player2Time.pause();
                            System.out.println("Player 1 Wins!");
                            aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                            aButton.setTag("Black");
                            LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
                            TextView winnerText = (TextView) findViewById(R.id.winnerText);
                            winnerText.setText("Winner: Player 1!\nPlay Again?");
                            layout.setVisibility(View.VISIBLE);
                        } else if (play == 2) {
                            player2.incrementWins();
                            TextView wins = (TextView) findViewById(R.id.player2_wins);
                            wins.setText("Wins: " + player2.getWins());
                            player1Time.pause();
                            player2Time.pause();
                            aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                            aButton.setTag("White");
                            LinearLayout layout = (LinearLayout) findViewById(R.id.winner);
                            TextView winnerText = (TextView) findViewById(R.id.winnerText);
                            winnerText.setText("Winner: Player 2!\nPlay Again?");
                            layout.setVisibility(View.VISIBLE);
                        }
                    }
                    if (isPaired) {
                        // wait for opponent move
                    }
                }
            }
        }
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
        if (pairedDevices.size() != 1) {
            // need to error out;

            Toast.makeText(getApplicationContext(), "No paired device found", Toast.LENGTH_LONG).show();
            for(BluetoothDevice bt : pairedDevices) ;

        } else {
            isPaired = true;
        }
        if (isPaired) {

            mChatService = new BluetoothChatService(this, mHandler);
            List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(pairedDevices);
            String pairedDeviceName = deviceList.get(0).getName();
            String myName = BA.getName();
            if (myName.compareTo(pairedDeviceName) < 0) {

                iHoldBlackPieces = true;
            } else {
                iHoldBlackPieces = false;
            }
            mChatService.connect(deviceList.get(0), false);
        }

    }
    public void placePieceFromPairedPlayer(int id) {
        ImageButton aButton = (ImageButton) findViewById(id);
        int y_coord = id % board_size;
        int x_coord = id / board_size;
        FragmentManager fm = getFragmentManager();
        boolean opponentPlayer;
        if (isPaired) {
            if (iHoldBlackPieces) {
                opponentPlayer = false;
            } else {
                opponentPlayer = true;
            }
        } else {
            return;
        }
        if (aButton.getTag() == null) {
            if (opponentPlayer) {
                int play = playTurn(player1, player1Time, x_coord, y_coord);
                String message = x_coord + "_" + y_coord;
                byte[] send = message.getBytes();
                if(play == 0){
                    aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                    aButton.setTag("Black");
                    player = !player;
                    player1Time.pause();
                    player2Time.resume();
                    TextView textView = (TextView)findViewById(R.id.player1);
                    textView.setBackgroundColor(0xFFFFCB3D);
                    TextView textView2 = (TextView)findViewById(R.id.player2);
                    textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
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
                // wait for opponent move from bluetooth
                if (isPaired) {
                    //wait
                }
            }
            else {
                int play = playTurn(player2, player2Time, x_coord, y_coord);
                String message = x_coord + "_" + y_coord;
                byte[] send = message.getBytes();
                if(play == 0){
                    aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                    aButton.setTag("White");
                    player = !player;
                    TextView textView = (TextView)findViewById(R.id.player2);
                    textView.setBackgroundColor(0xFFFFCB3D);
                    TextView textView2 = (TextView)findViewById(R.id.player1);
                    textView2.setBackgroundColor(getResources().getColor(R.color.yellow));
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
                if (isPaired) {
                    // wait for opponent move
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
            case Online:
                return 0;
            case Offline:
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
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            mChatService.stop();
                            break;
                    }
                    break;
                */

                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    //construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(getApplicationContext(), writeMessage,Toast.LENGTH_SHORT).show();

                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMessage,Toast.LENGTH_SHORT).show();
                    placePieceFromPairedPlayer(Integer.parseInt(readMessage));

                    //print this
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
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


}
