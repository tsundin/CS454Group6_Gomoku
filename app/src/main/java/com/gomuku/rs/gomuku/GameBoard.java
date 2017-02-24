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
    public int checkForWinner(int stoneColor, boolean isTimerExpired, int originx, int originy) {
        int chainLength;

        //Check for timer expired
        if (isTimerExpired) {
            if (stoneColor == 1) {
                return 2;
            }
            return 1;
        }

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

        int chainLength = countLeft + countRight;
        return chainLength;
    }

    //Check for vertical winning conditions around an origin point
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

        int chainLength = countUp + countDown;
        return chainLength;
    }

    //Check for increasing sloped diagonal winning conditions around an origin point
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

    //Returns board size
    public int getBoardSize() {
        return boardSizeX;
    }
}