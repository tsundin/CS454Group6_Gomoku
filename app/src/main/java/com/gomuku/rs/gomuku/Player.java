package com.gomuku.rs.gomuku;

/**
 * Created by Thaddeus Sundin on 2/6/2017.
 */

public class Player {

    //Create an integer to represent the player's stone color
    //1 represents white, 2 represents black
    protected int stoneColor;
    private String name;
    private String participantId;

    //How many times the player has won a game
    private int wins;

    //Constructor to set player stone color. Automatically sets wins to zero
    public Player(int stoneColor) {
        this.stoneColor = stoneColor;
        this.wins = 0;
    }

    //Set player's name
    public void setName(String name) { this.name = name; }

    //Get player's name
    public String getName() { return this.name; }

    //Set player's participant ID
    public void setId(String id) { this.participantId = id; }

    //Get player's participant ID
    public String getId() { return this.participantId; }

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

    //This function should only be called by the AIPlayer2 class
    //Therefore, simply return an invalid move
    public int[] playTurn(GameBoard gameBoard){
        int[] toReturn = new int[] {-1, -1};
        return toReturn;
    }
}
