package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by rs on 1/26/17.
 */

    public class GameSelection extends Activity {

    public enum GameTypes {
        Online, Offline, AI
    }

    GameTypes gameType = GameTypes.Offline;

    public enum GameModes {
        Standard, Freestyle
    }

    GameModes gameMode = GameModes.Standard;

    public enum BoardSizes {
        _10x10, _15x15, _20x20
    }
    BoardSizes boardSize = BoardSizes._10x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_selection);

        // Determine game type from radio buttons
        RadioButton radioButton_online = (RadioButton) findViewById(R.id.online);
        RadioButton radioButton_offline = (RadioButton) findViewById(R.id.offline);
        RadioButton radioButton_ai = (RadioButton) findViewById(R.id.ai);

        radioButton_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { gameType = GameTypes.Online; }
        });
        radioButton_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { gameType = GameTypes.Offline; }
        });
        radioButton_ai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { gameType = GameTypes.AI; }
        });

        // Determine game mode from radio buttons
        RadioButton radioButton_standard = (RadioButton) findViewById(R.id.standard);
        RadioButton radioButton_freestyle = (RadioButton) findViewById(R.id.freestyle);

        radioButton_standard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { gameMode = gameMode.Standard; }
        });
        radioButton_freestyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { gameMode = gameMode.Freestyle; }
        });

        // Determine Board Size from Radio Buttons
        RadioButton radioButton_10x10 = (RadioButton) findViewById(R.id.ten);
        RadioButton radioButton_15x15 = (RadioButton) findViewById(R.id.fifteen);
        RadioButton radioButton_20x20 = (RadioButton) findViewById(R.id.twenty);
        android.support.v7.widget.CardView cV = (android.support.v7.widget.CardView) findViewById(R.id.game_options_card);


        radioButton_10x10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { boardSize = BoardSizes._10x10; }
        });
        radioButton_15x15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { boardSize = BoardSizes._15x15; }
        });
        radioButton_20x20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { boardSize = BoardSizes._20x20; }
        });
        cV.setCardBackgroundColor(getResources().getColor(R.color.transparent));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gs, menu);
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


    public  void sendMessage(View view)
    {
        Intent intent = new Intent(this, GamePage.class);
        intent.putExtra("gameType", gameType);
        intent.putExtra("gameMode", gameMode);
        intent.putExtra("boardSize", boardSize);
        startActivity(intent);
    }
}