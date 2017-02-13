package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by rs on 1/27/17.
 */
public class GamePage extends Activity {
    boolean player = true;

    GameSelection.BoardSizes boardSizeEnum = GameSelection.BoardSizes._10x10;
    int board_size = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();

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

    }

    public void placePiece(View view) {
        ImageButton aButton = (ImageButton) view;
        int id = aButton.getId();
        int y_coord = id % board_size;
        int x_coord = id / board_size;
        if (aButton.getTag() == null) {
            if (player) {
                    aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                    aButton.setTag("Black");
                    player = !player;
                Toast.makeText(GamePage.this,
                        "Clicked ID: " + id + " (x,y): (" + x_coord + "," + y_coord + ")",
                        Toast.LENGTH_SHORT).show();
                }
                else {
                    aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                    aButton.setTag("White");
                    player = !player;
                Toast.makeText(GamePage.this,
                        "Clicked ID: " + id + " (x,y): (" + x_coord + "," + y_coord + ")",
                        Toast.LENGTH_SHORT).show();
                }
            }
        else { // click an already-placed piece, to demo stalemate box
            LinearLayout layout = (LinearLayout) findViewById(R.id.stalemate_and_winner);
            layout.setVisibility(View.VISIBLE);

        }

    }




}
