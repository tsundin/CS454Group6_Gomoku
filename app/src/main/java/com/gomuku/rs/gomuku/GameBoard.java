package com.gomuku.rs.gomuku;

/**
 * Created by Thaddeus Sundin on 2/7/2017.
 */

public class GameBoard {

    //This 2 dimensional array will represent the game board
    //A '0' on the board represents an empty square
    //A '1' represents a white stone, and a '2' represents a black stone
    private int [][] gameBoard;

    //Create integer values to hold the size of the game board
    private int boardSizeX;
    private int boardSizeY;

    //This int will represent the game mode
    //0 represents standard, 1 represents freestyle
    private int gameMode;

    public GameBoard(int boardSizeX, int boardSizeY, int gameMode) {
        //Store the board sizes
        this.boardSizeX = boardSizeX;
        this.boardSizeY = boardSizeY;

        //Create a new array of the specified size
        this.gameBoard = new int[boardSizeX][boardSizeY];

        //Initialize the array to all zeros
        for(int i=0; i<boardSizeX; ++i) {
            for(int j=0; j<boardSizeY; ++j) {
                gameBoard[i][j] = 0;
            }
        }

        //Set the game mode
        this.gameMode = gameMode;
    }

    //Place a specified color of stone at the given coordinates
    //If a stone already exists there (the array does not contain 0 at that space), return -1
    public int placeStone(int stoneColor, int x, int y) {
        //If the coordinates are out of bounds, return -2
        if(x > boardSizeX || y > boardSizeY)
            return -2;

        //Return -1 if the specified coordinates already contain a stone
        if(gameBoard[x][y] != 0)
            return -1;

        //Insert a new game piece at the specified coordinates
        gameBoard[x][y] = stoneColor;

        //Return 0 to indicate success
        return 0;
    }

    //Return the stone color of the winner (1 = white, 2 = black)
    //Return 0 if there is no winner
    public int checkForWinner() {
        return 0;
    }

    //Return true if there are no more free spaces on the game board (if there are no more 0s of the board)
    //Return false otherwise
    public boolean isBoardFull() {

        //If there exists a zero on the game board, then the board is not full
        for(int i=0; i<boardSizeX; ++i) {
            for(int j=0; j<boardSizeY; ++j) {
                if (gameBoard[i][j] == 0)
                    return false;
            }
        }

        //If the loop has terminated, then no 0s exists on the game board and hte board is therefore full
        return true;
    }

    //Print the game board to standard output
    public void printBoard() {

        //Loop through the board printing one character at a time
        for(int i=0; i<boardSizeX; ++i) {
            for(int j=0; j<boardSizeY; ++j) {
                System.out.print('[');
                System.out.print(gameBoard[i][j]);
                System.out.print("] ");
            }
            System.out.print('\n');
        }
    }
}