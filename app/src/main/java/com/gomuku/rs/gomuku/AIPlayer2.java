package com.gomuku.rs.gomuku;

/**
 * Created by Thaddeus Sundin on 3/1/2017.
 */

public class AIPlayer2 extends Player {

    //Default constructor
    //Defaults the AI player's stone color to black
    public AIPlayer2() {
        super(2);
    }

    //Find the best move to play based on the current game board
    public int[] findBestMove(GameBoard currentBoard) {
        int[][] scoreBoard = currentBoard.generateScoreBoard();
        int[] bestMove = new int[2];
        int bestMoveScore = 0;
        for(int i=0; i<currentBoard.getBoardSizeX(); ++i) {
            for(int j=0; j<currentBoard.getBoardSizeY(); ++j) {
                //Check if that space has a higher score that the score of the current best move
                //If it does, update the best move and the best move score
                if(scoreBoard[i][j] >= bestMoveScore){
                    bestMove[0] = i;
                    bestMove[1] = j;
                    bestMoveScore = scoreBoard[i][j];
                }
            }
        }

        //Return the best move
        return bestMove;
    }

    //Returns an integer array with 2 elements
    //toReturn[0] == x coordinate of the AI player's move
    //toReturn[1] == y coordinate of the AI player's move
    public int[] playTurn(GameBoard gameBoard) {

        int[] toReturn = findBestMove(gameBoard);
        return toReturn;
    }
}
