package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by rs on 1/27/17.
 */
public class GamePage extends Activity {
    boolean player = true;

    GameSelection.BoardSizes boardSize = GameSelection.BoardSizes._10x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        boardSize = (GameSelection.BoardSizes) b.getSerializable("boardSize");


        //Bundle b = getIntent().getSerializableExtra("boardSize");
        //boardSize = (BoardSizes) getIntent().getSerializableExtra("boardSize");

        switch(boardSize) {
            case _10x10: setContentView(R.layout.game_page);
                break;
            case _15x15: setContentView(R.layout.game_page_15x15);
                break;
            case _20x20: setContentView(R.layout.game_page_20x20);
                break;
            default: setContentView(R.layout.game_page);
                break;
        }

        GridLayout gridlayout = (GridLayout) findViewById(R.id.gridlayout);
        gridlayout.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(GamePage.this, "Clicked " + boardSize ,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void placePiece(View view) {
        ImageButton aButton = (ImageButton) view;
        if (aButton.getTag() == null) {
            if (player) {
                    aButton.setImageResource(R.drawable.intersection_black_100px_100px);
                    aButton.setTag("Black");
                    player = !player;
                Toast.makeText(GamePage.this, "Clicked " + boardSize ,
                        Toast.LENGTH_SHORT).show();
                }
                else {
                    aButton.setImageResource(R.drawable.intersection_white_100px_100px);
                    aButton.setTag("White");
                    player = !player;
                Toast.makeText(GamePage.this, "Clicked " + boardSize ,
                        Toast.LENGTH_SHORT).show();
                }
            }

    }




}
