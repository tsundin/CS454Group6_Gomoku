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
        public enum BoardSizes {
            _10x10, _15x15, _20x20
        }
        BoardSizes boardSize = BoardSizes._10x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_selection);

        /*
            Determine Board Size from radio buttons
         */
        RadioButton radioButton_10x10 = (RadioButton) findViewById(R.id.radioButton5);
        RadioButton radioButton_15x15 = (RadioButton) findViewById(R.id.radioButton6);
        RadioButton radioButton_20x20 = (RadioButton) findViewById(R.id.radioButton7);

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
        intent.putExtra("boardSize", boardSize);
        startActivity(intent);


    }
}