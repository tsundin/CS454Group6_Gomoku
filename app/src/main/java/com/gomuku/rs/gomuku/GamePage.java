package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;

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
    private Timer timer1;
    private Timer timer2;

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


        GridLayout gridlayout = (GridLayout) findViewById(R.id.gridlayout);
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

        // Initialize game objects
        this.gameBoard = new GameBoard(board_size, board_size, GetGameMode(gameModeEnum));
        this.gameType = GetGameType(gameTypeEnum);
        player1 = new Player(1);
        player2 = new Player(2);
        timer1 = new Timer();
        timer2 = new Timer();
    }

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
        //timer.startTimer();

        while (successfulPlace != 0) {
            //Place a stone on the board
            successfulPlace = this.gameBoard.placeStone(player.getStoneColor(), x, y);
            if (successfulPlace == -1)
                System.out.println("There is already a stone there! Pick a different square.");
            if (successfulPlace == -2)
                System.out.println("Those coordinates are out of bounds!");
        }

        //Stop timer
        //timer.stopTimer();

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


}
