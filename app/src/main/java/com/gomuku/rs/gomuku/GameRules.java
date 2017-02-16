package com.gomuku.rs.gomuku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by rs on 1/26/17.
 */
public class GameRules extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_rules);
        android.support.v7.widget.CardView cV = (android.support.v7.widget.CardView) findViewById(R.id.game_rules_card);
        cV.setCardBackgroundColor(getResources().getColor(R.color.transparent));


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


    public  void sendMessage(View view)
    {
        Intent intent = new Intent(this, GameSelection.class);
        startActivity(intent);
    }

}
