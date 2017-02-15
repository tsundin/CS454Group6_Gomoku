package com.gomuku.rs.gomuku;

import java.util.Scanner;

/**
 * Created by Thaddeus Sundin on 2/12/2017.
 */

public class GameLogic {

    private GameBoard gameBoard;
    //Specify the type of game to play
    //0 means offline, 1 means online, 2 means AI
    private int gameType;
    private Player player1;
    private Player player2;
    private Timer timer1;
    private Timer timer2;

    public GameLogic(int boardSizeX, int boardSizeY, int gameType, int gameMode) {
        this.gameBoard = new GameBoard(boardSizeX, boardSizeY, gameMode);
        this.gameType = gameType;
        player1 = new Player(1);
        player2 = new Player(2);
        timer1 = new Timer();
        timer2 = new Timer();
    }

    //Run the game
    public void runGame() {
        boolean gameRunning = true;
        int winner = 0;
        while(gameRunning) {
            //---------Player 1 turn---------
            printBoard();
            System.out.println("Player 1: Your turn!");
            playTurn(player1, timer1);
            printBoard();
            //---------Logic check---------
            //Check if there is a winner
            winner = gameBoard.checkForWinner();
            if(winner != 0) {
                gameRunning = false;
            }
            //Check if the game board is full
            if(gameBoard.isBoardFull())
                gameRunning = false;

            //---------Player 2 turn---------
            //(Only taken if the game is still running)
            if (gameRunning) {
                System.out.println("Player 2: Your turn!");
                playTurn(player2, timer2);
                //---------Logic check---------
                //Check if there is a winner
                winner = gameBoard.checkForWinner();
                if(winner != 0) {
                    gameRunning = false;
                }
                //Check if the game board is full
                if (gameBoard.isBoardFull())
                    gameRunning = false;
            }
        }

        //Check who won the game
        if(winner == 1) {
            System.out.println("Player 1 has won the game!");
            player1.incrementWins();
        }
        else if(winner == 2) {
            System.out.println("Player 2 has won the game!");
            player2.incrementWins();
        }
        else if (winner == 0) {
            System.out.println("The game ended in a stalemate!");
        }
    }

    //Play a turn
    public int playTurn(Player player, Timer timer) {
        int x;
        int y;
        int successfulPlace = -1;
        Scanner in = new Scanner(System.in);

        //Start the timer
        timer.startTimer();

        while (successfulPlace != 0) {
            //Read input from the user
            System.out.println("Where would you like to place a stone?");
            System.out.print("x: ");
            x = in.nextInt();
            System.out.print("y: ");
            y = in.nextInt();

            //Place a stone on the board
            successfulPlace = this.gameBoard.placeStone(player.getStoneColor(), x, y);
            if (successfulPlace == -1)
                System.out.println("There is already a stone there! Pick a different square.");
            if (successfulPlace == -2)
                System.out.println("Those coordinates are out of bounds!");
        }

        //Stop timer
        timer.stopTimer();

        return 0;
    }

    //Print the game board
    void printBoard() {
        this.gameBoard.printBoard();
    }

}
