package com.gomuku.rs.gomuku;

/**
 * Created by Thaddeus Sundin on 2/6/2017.
 */

public class Player {

    //Create an integer to represent the player's stone color
    //1 represents white, 2 represents black
    private int stoneColor;

    //How many times the player has won a game
    private int wins;

    //Constructor to set player stone color. Automatically sets wins to zero
    public Player(int stoneColor) {
        this.stoneColor = stoneColor;
        this.wins = 0;
    }

    //Return the player's stone color
    public int getStoneColor() {
        return stoneColor;
    }

    //Return the player's win total
    public int getWins() {
        return wins;
    }

    //Increment the player's win total
    public void incrementWins() {
        this.wins = this.wins + 1;
    }
}