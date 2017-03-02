package com.gomuku.rs.gomuku;

import java.util.ArrayList;

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


    //Constructor that takes a GameBoard object as input and uses it to initialize itself
    public GameBoard(GameBoard toCopy) {
        //Initialize the board sizes
        this.boardSizeX = toCopy.boardSizeX;
        this.boardSizeY = toCopy.boardSizeY;

        //Initialize the gameBoard to be the same as the one passed in
        for(int i=0; i<this.boardSizeX; ++i) {
            for(int j=0; j<this.boardSizeY; ++j) {
                this.gameBoard[i][j] = toCopy.gameBoard[i][j];
            }
        }
    }

    //Getters for board sizes
    public int getBoardSizeX() {
        return boardSizeX;
    }
    public int getBoardSizeY() {
        return boardSizeY;
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


    //Check if the chain length fulfills the winning conditions set forth by the game mode
    public boolean isChainLengthValid(int chainLength) {
        //Chain lengths must always be at least 5
        //If we are in standard mode, a chain length of six or more stones does not count
        if (gameMode == 0) {
            if (chainLength == 5)
                return true;
        }
        //In freestyle mode, a line of six or more stones is allowed
        if (gameMode == 1) {
            if (chainLength >= 5)
                return true;
        }
        return false;
    }


    //Check the area surrounding the origin for winning conditions
    //Return the stone color if the winning conditions are met
    //Returns 0 if no winning conditions are met
    public int checkForWinner(int stoneColor, int originx, int originy) {
        int chainLength;
        //Check for horizontal winning conditions
        chainLength = checkHorizontal(stoneColor, originx, originy);
        if (isChainLengthValid(chainLength))
            return stoneColor;

        //Check for vertical winning conditions
        chainLength = checkVertical(stoneColor, originx, originy);
        if (isChainLengthValid(chainLength))
            return stoneColor;

        //Check for diagonal winning conditions
        chainLength = checkDiagonalInc(stoneColor, originx, originy);
        if (isChainLengthValid(chainLength))
            return stoneColor;
        chainLength = checkDiagonalDec(stoneColor, originx, originy);
        if (isChainLengthValid(chainLength))
            return stoneColor;

        //Return 0 if no winning chain length was found
        return 0;
    }


    //Check for horizontal winning conditions around an origin point
    //Returns the length of the horizontal chain measured from the origin point
    //Returns 0 if the chain is blocked at both ends
    public int checkHorizontal(int stoneColor, int originx, int originy) {
        //Check to the left of the origin (Start at 1 to count the origin point)
        int countLeft = 1;
        for (int i=originx-1; i>=0; --i) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[i][originy] == stoneColor)
                ++countLeft;
            else
                break;
        }

        //Check to the right of the origin
        int countRight = 0;
        for (int i=originx+1; i<boardSizeX; ++i) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[i][originy] == stoneColor)
                ++countRight;
            else
                break;
        }

        //Check if the chain length is not blocked at both ends
        boolean blockedLeft = false;
        boolean blockedRight = false;
        //First, check if the offset to the left of the origin point
        // is out of bounds (less than 0)
        // or is the opposite stone color (!= 0 && != stoneColor)
        int leftOffset = originx - countLeft;
        if(leftOffset < 0 || (gameBoard[leftOffset][originy] != 0 && gameBoard[leftOffset][originy] != stoneColor)) {
            blockedLeft = true;
        }

        //Then, check if the offset to the right of the origin point)
        // is out of bounds (greater than or equal to the boardSizeX vlue)
        // or is the opposite stone color (!= 0 && !+ stoneColor)
        int rightOffset = originx + countRight + 1;
        if(rightOffset >= boardSizeX || (gameBoard[rightOffset][originy] != 0 && gameBoard[rightOffset][originy] != stoneColor)) {
            blockedRight = true;
        }

        //If the stone is blocked at both ends, return 0 to invalidate chain length
        if(blockedLeft && blockedRight)
            return 0;
        //Otherwise, return the valid chain length
        int chainLength = countLeft + countRight;
        return chainLength;
    }


    //Check for vertical winning conditions around an origin point
    //Returns the length of the vertical chain measured from the origin point
    //Returns 0 if the chain is blocked at both ends
    public int checkVertical(int stoneColor, int originx, int originy) {
        //Check above the origin (Start at 1 to count the origin point)
        int countUp = 1;
        for (int i=originy-1; i>=0; --i) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[originx][i] == stoneColor)
                ++countUp;
            else
                break;
        }

        //Check below the origin
        int countDown = 0;
        for (int i=originy+1; i<boardSizeY; ++i) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[originx][i] == stoneColor)
                ++countDown;
            else
                break;
        }

        //Check if the chain length is not blocked at both ends
        boolean blockedUp = false;
        boolean blockedDown = false;
        //First, check if the offset above the origin point
        // is out of bounds (less than 0)
        // or is the opposite stone color (!= 0 && != stoneColor)
        int upOffset = originy - countUp;
        if(upOffset < 0 || (gameBoard[originx][upOffset] != 0 && gameBoard[originx][upOffset] != stoneColor)) {
            blockedUp = true;
        }

        //Then, check if the offset below the origin point
        // is out of bounds (greater than or equal to the boardSizeX value)
        // or is the opposite stone color (!= 0 && !+ stoneColor)
        int downOffset = originy + countDown + 1;
        if(downOffset >= boardSizeX || (gameBoard[originx][downOffset] != 0 && gameBoard[originx][downOffset] != stoneColor)) {
            blockedDown = true;
        }

        //If the stone is blocked at both ends, return 0 to invalidate chain length
        if(blockedUp && blockedDown)
            return 0;
        //Otherwise, return the valid chain length
        int chainLength = countUp + countDown;
        return chainLength;
    }


    //Check for increasing sloped diagonal winning conditions around an origin point
    //Returns the length of the diagonal chain measured from the origin point
    //Returns 0 if the chain is blocked at both ends
    public int checkDiagonalInc(int stoneColor, int originx, int originy) {
        //Check above and to the right of the origin (Start at 1 to count the origin point)
        int countUp = 1;
        int i = originx+1;
        int j = originy-1;
        while (i<boardSizeX && j>=0) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[i][j] == stoneColor)
                ++countUp;
            else
                break;
            ++i;
            --j;
        }

        //Check below and to the left of the origin
        int countDown = 0;
        i = originx-1;
        j = originy+1;
        while (i>=0 && j<boardSizeY) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[i][j] == stoneColor)
                ++countDown;
            else
                break;
            --i;
            ++j;
        }

        int chainLength = countUp + countDown;
        return chainLength;
    }


    //Check for decreasing sloped diagonal winning conditions around an origin point
    //Returns the length of the diagonal chain measured from the origin point
    //Returns 0 if the chain is blocked at both ends
    public int checkDiagonalDec(int stoneColor, int originx, int originy) {
        //Check above and to the left of the origin (Start at 1 to count the origin point)
        int countUp = 1;
        int i = originx-1;
        int j = originy-1;
        while (i>=0 && j>=0) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[i][j] == stoneColor)
                ++countUp;
            else
                break;
            --i;
            --j;
        }

        //Check below and to the right of the origin point
        int countDown = 0;
        i = originx+1;
        j = originy+1;
        while (i<boardSizeX && j<boardSizeY) {
            //If the next stone matches the stone color to search for, increment the count
            //Otherwise, break the loop (Because the chain has ended)
            if (gameBoard[i][j] == stoneColor)
                ++countUp;
            else
                break;
            ++i;
            ++j;
        }

        int chainLength = countUp + countDown;
        return chainLength;
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


    //Return true if the coordinates given are within the bounds of the game board
    //Return false otherwise
    public boolean isSpaceValid(int x, int y) {
        if(x>=0 && y>=0 && x<boardSizeX && y<boardSizeY)
            return true;
        return false;
    }


    //Print the game board to standard output
    public void printBoard() {

        //Loop through the board printing one character at a time
        for(int i=0; i<boardSizeY; ++i) {
            for(int j=0; j<boardSizeX; ++j) {
                System.out.print('[');
                System.out.print(gameBoard[j][i]);
                System.out.print("] ");
            }
            System.out.print('\n');
        }
    }


    //Configure the game board for testing purposes
    public void configBoard(){
        //Configure player 1 pieces
        // Template gameBoard[][] = 1;
        gameBoard[0][5] = 1;
        gameBoard[2][3] = 1;
        gameBoard[7][7] = 1;

        //Configure player 2 pieces
        // Template gameBoard[][] = 2;
        gameBoard[0][2] = 2;
        gameBoard[0][3] = 2;
        gameBoard[0][4] = 2;
    }

    //Configure the game board for testing purposes
    public void configBoard2() {
        //Configure player 1 pieces
        // Template gameBoard[][] = 1;
        gameBoard[5][5] = 1;
        gameBoard[2][3] = 1;
        gameBoard[7][7] = 1;

        //Configure player 2 pieces
        // Template gameBoard[][] = 2;
        gameBoard[5][2] = 2;
        gameBoard[5][3] = 2;
        gameBoard[5][4] = 2;
    }


    /*
    --------------------------------------
    ----- Functions to be used by AI -----
    --------------------------------------
    */

    //Generate list of possible moves
    public ArrayList<int[]> getMoves() {
        ArrayList<int[]> availableMoves = new ArrayList<int[]>();
        //Populate the list with any space that is free (is a 0 on the board)
        for(int i=0; i<boardSizeX; ++i) {
            for(int j=0; j<boardSizeY; ++j) {
                if(gameBoard[i][j] == 0) {
                    int[] toAdd = new int[2];
                    toAdd[0] = i;
                    toAdd[1] = j;
                    availableMoves.add(toAdd);
                }
            }
        }
        return availableMoves;
    }

    //Check entire board for winner with the matching stone color
    //Return true if that winner is found, false otherwise
    public boolean isThereWinner(int stoneColor){
        for(int i=0; i<boardSizeX; ++i) {
            for(int j=0; j<boardSizeY; ++j) {
                if(checkForWinner(stoneColor, i, j) == stoneColor)
                    return true;
            }
        }
        return false;
    }

    //Evaluate the board
    public int evaluate(int stoneColor){
        int score = 0;
        int opponentScore = 0;
        int opponentStoneColor;
        if (stoneColor == 1)
            opponentStoneColor = 2;
        else
            opponentStoneColor = 1;

        //For each spot on the board, tally each player's score
        for(int i=0; i<boardSizeX; ++i) {
            for (int j = 0; j < boardSizeY; ++j) {
                //If this space doesn't contain our stone, skip it
                if(gameBoard[i][j] != stoneColor)
                    break;
                //Get the length of all the player's chains from this point
                int hLength = checkHorizontal(stoneColor, i, j);
                int vLength = checkVertical(stoneColor, i, j);
                int dIncLength = checkDiagonalInc(stoneColor, i, j);
                int dDecLength = checkDiagonalDec(stoneColor, i, j);
                //Add them to the player's score
                score = score + hLength + vLength + dIncLength + dDecLength;

                //Get the length of all the opponent's chains from this point
                int hLengthOpp = checkHorizontal(opponentStoneColor, i, j);
                int vLengthOpp = checkVertical(opponentStoneColor, i, j);
                int dIncLengthOpp = checkDiagonalInc(opponentStoneColor, i, j);
                int dDecLengthOpp = checkDiagonalDec(opponentStoneColor, i, j);
                //Add them to the opponent's score
                opponentScore = opponentScore + hLengthOpp + vLengthOpp + dIncLengthOpp + dDecLengthOpp;
            }
        }
        return score - opponentScore;
    }

    //A better evaluation function
    public int evaluate2(int stoneColor) {
        int score = 0;
        int opponentScore = 0;
        int opponentStoneColor;
        if (stoneColor == 1)
            opponentStoneColor = 2;
        else
            opponentStoneColor = 1;

        //For each spot on the board, tally each player's score
        for(int i=0; i<boardSizeX; ++i) {
            for (int j = 0; j < boardSizeY; ++j) {
                //If this space doesn't contain a stone, skip it
                if(gameBoard[i][j] == 0)
                    break;
                    //If the space contains our stone color, search for chains
                else if(gameBoard[i][j] == stoneColor) {
                    //Get the score of all the player chains at this point
                    score += scoreChain(checkHorizontal(stoneColor, i, j));
                    score += scoreChain(checkVertical(stoneColor, i, j));
                    score += scoreChain(checkDiagonalInc(stoneColor, i, j));
                    score += scoreChain(checkDiagonalDec(stoneColor, i, j));
                }
                //Otherwise, search for opponent's chain color
                else {
                    //Get the score of all the opponent chains at this point
                    opponentScore += scoreChain(checkHorizontal(opponentStoneColor, i, j));
                    opponentScore += scoreChain(checkVertical(opponentStoneColor, i, j));
                    opponentScore += scoreChain(checkDiagonalInc(opponentStoneColor, i, j));
                    opponentScore += scoreChain(checkDiagonalDec(opponentStoneColor, i, j));
                }
            }
        }
        return score - opponentScore;
    }

    //Return the scored value of a chain length
    //Should only be run after checking for winners, so a chain length of 5 or greater is never expected
    public int scoreChain(int chainLength) {
        switch(chainLength) {
            case 1: return 1;
            case 2: return 10;
            case 3: return 25;
            case 4: return 50;
            default: return 0;
        }
    }

    //Reset spot (Reset the given coordinates to 0)
    public void resetSpot(int x, int y) {
        gameBoard[x][y] = 0;
    }


    /*
    ---------------------------------------------
    ----- Functions to be used by AIPlayer2 -----
    ---------------------------------------------
    */

    //Generate a board of scores based on possible player next moves
    //NOTE: Assumes AI player's stone color is black (stoneColor == 2)
    public int[][] generateScoreBoard() {
        //Create n x n matrix to hold scores
        int[][] scoreBoard = new int[boardSizeX][boardSizeY];
        //Generate list of possible player moves
        ArrayList<int[]> moves = getMoves();
        //Use to store current move
        int[] currentMove;

        //For each move, score that space on the board
        for(int i=0; i<moves.size(); i++) {
            currentMove = moves.get(i);
            scoreBoard[currentMove[0]][currentMove[1]] = scoreSpace(currentMove, 2);
        }
        return scoreBoard;
    }

    //Return the score of the move to evaluate
    public int scoreSpace(int[] spaceToEval, int playerStoneColor) {
        int score = 0;
        int x = spaceToEval[0];
        int y = spaceToEval[1];

        //Search all adjacent spaces and generate a chain length score based on the color stone that exists there
        //Need to score:
        //[x+1][y], [x+1][y+1], [x][y+1], [x-1][y+1], [x-1][y], [x-1][y-1], [x][y-1], [x+1][y-1]
        score += scoreAdjacentSpace(x+1, y, 0);
        score += scoreAdjacentSpace(x+1, y+1, 3);
        score += scoreAdjacentSpace(x, y+1, 1);
        score += scoreAdjacentSpace(x-1, y+1, 2);
        score += scoreAdjacentSpace(x-1, y, 0);
        score += scoreAdjacentSpace(x-1, y-1, 3);
        score += scoreAdjacentSpace(x, y-1, 1);
        score += scoreAdjacentSpace(x+1, y-1, 2);

        //Sum these values to score the spot
        return score;
    }

    //Return the score of an adjacent space
    //Modes: 0 = horizontal
    //       1 = vertical
    //       2 = increasing diagonal
    //       3 = decreasing diagonal
    public int scoreAdjacentSpace(int x, int y, int mode) {
        int chainLength = 0;
        //If the space to check is valid and contains a player stone, score it
        if(isSpaceValid(x, y) && gameBoard[x][y] != 0) {
            //Check for chain based on the mode specified
            if(mode == 0)
                chainLength = checkHorizontal(gameBoard[x][y], x, y);
            if(mode == 1)
                chainLength = checkVertical(gameBoard[x][y], x, y);
            if(mode == 2)
                chainLength = checkDiagonalInc(gameBoard[x][y], x, y);
            if(mode == 3)
                chainLength = checkDiagonalDec(gameBoard[x][y], x, y);

            //Return the score for that chain length based on the color of stone on that space
            if(gameBoard[x][y] == 2)
                return scorePlayerChain(chainLength);
            else
                return scoreOpponentChain(chainLength);
        }
        //Otherwise, return a score of 0
        else
            return 0;
    }

    //Return the weighted score of a player chain
    public int scorePlayerChain(int chainLength) {
        switch(chainLength) {
            case 1: return 1;
            case 2: return 10;
            case 3: return 25;
            case 4: return 50;
            default: return 0;
        }
    }

    //Return the weighted score of an opponent chain
    public int scoreOpponentChain(int chainLength) {
        switch(chainLength) {
            case 1: return 1;
            case 2: return 5;
            case 3: return 15;
            case 4: return 35;
            default: return 0;
        }
    }
}