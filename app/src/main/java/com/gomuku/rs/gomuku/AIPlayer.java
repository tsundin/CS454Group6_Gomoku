package com.gomuku.rs.gomuku;

import java.util.ArrayList;

/**
 * Created by Thaddeus Sundin on 3/1/2017.
 */

public class AIPlayer {

    //Create an integer to represent the player's stone color
    //1 represents white, 2 represents black
    private int stoneColor;

    //Constructor
    public AIPlayer() {
        stoneColor = 2;
    }

    public int minimax(GameBoard currentBoard, int depth, int a, int b, int playerStoneColor) {
        //If the other player (who just played) has a winning condition on the board
        //return the value of that board
        int opponent;
        if(playerStoneColor == 2)
            opponent = 1;
        else
            opponent = 2;
        if(currentBoard.isThereWinner(opponent)) {
            return evaluateWinner(opponent);
        }

        //If depth is 0, evaluate the current board are return its score
        if(depth == 0)
            return evaluateBoard(currentBoard, playerStoneColor);

        //Generate list of moves to make based on the current board state
        ArrayList<int[]> moves = currentBoard.getMoves();

        //Create integer to store value
        int v;

        //If we are maximizing player
        if(playerStoneColor == 2) {
            //For each move we can make
            for(int i=0; i<moves.size(); ++i) {
                //Get the next move
                int[] move = moves.get(i);
                //Make the move
                currentBoard.placeStone(playerStoneColor, move[0], move[1]);
                //Recursive call
                v = minimax(currentBoard, depth-1, a, b, 1);
                //Remove the move
                currentBoard.resetSpot(move[0], move[1]);
                if(v > a)
                    a = v;
                if(a >= b)
                    return a;
            }
            return a;
        }
        //If we are minimizing player
        else {
            //For each move we can make
            for(int i=0; i<moves.size(); ++i) {
                //Get the next move
                int[] move = moves.get(i);
                //Make the move
                currentBoard.placeStone(1, move[0], move[1]);
                //Recursive call
                v = minimax(currentBoard, depth-1, a, b, 2);
                //Remove the move
                currentBoard.resetSpot(move[0], move[1]);
                if(v < b)
                    b = v;
                if(b <= a)
                    return b;
            }
            return b;
        }
    }

    //Find the best move for the AI player using alpha-beta pruning tree minimax algorithm
    //This function acts as the first maximizer in the minimax call
    public int[] findBestMove(GameBoard currentBoard) {
        //Generate list of possible moves to make based on the current board state
        ArrayList<int[]> moves = currentBoard.getMoves();

        //AI player's next best move
        //AIMove[0] == x coordinate of move
        //AIMove[1] == y coordinate of move
        int[] bestMove = new int[2];

        //The value of the best move (Initialized to MIN_VALUE, anything is better than no move)
        int bestMoveValue = Integer.MIN_VALUE;

        //Set v to minimum value
        int v = Integer.MIN_VALUE;

        //Set up alpha and beta
        int a = Integer.MIN_VALUE;
        int b = Integer.MAX_VALUE;

        //For each move
        //Perform minimax search(calling minimizer first)
        //If value returned is greater than value of the best move, update best move
        for(int i=0; i<moves.size(); ++i) {
            //Get the next move
            int[] move = moves.get(i);
            //Make the move
            currentBoard.placeStone(this.stoneColor, move[0], move[1]);
            //Run minimax
            v = Math.max(v, minimax(currentBoard, 2, a, b, 1));
            //Remove the move
            currentBoard.resetSpot(move[0], move[1]);
            //Logic check
            if(v > a) {
                a = v;
                bestMove[0] = move[0];
                bestMove[1] = move[1];
            }
            //if(a >= b)
            //    break;
        }

        return bestMove;
    }

    //Return the score for the winner based on whether their stone color matches this player's or not
    public int evaluateWinner(int stoneColor) {
        //If their stone color matches ours, return positive value
        if(stoneColor == this.stoneColor)
            return Integer.MAX_VALUE;
            //Otherwise, they are the minimizing player so return negative value
        else
            return Integer.MIN_VALUE;
    }

    //Returns the score of the evaluated game board
    //Positive score is returned if the stone color matches this player's stone color
    //Negative score is returned otherwise.
    public int evaluateBoard(GameBoard currentBoard, int StoneColor) {
        //Evaluate the current board
        int score = currentBoard.evaluate2(stoneColor);
        if(score != 0)
            score = score + 0;
        //If their stone color matches ours, they are the maximizing player, so return positive value
        if(stoneColor == this.stoneColor) {
            return score;
        }
        //Otherwise, they are the minimizing player so return negative value
        else {
            return score * -1;
        }
    }

    public int[] playTurn(GameBoard gameBoard) {

        int[] toReturn = findBestMove(gameBoard);
        return toReturn;
    }

}
