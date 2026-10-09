package com.kilonzo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class GameBoard {
    private int[][] board;
    private int numBombs; // total number of bombs in grid
    private int numFlags; //the number of flags remaining to be placed by the player
    public static final int BOMB = -1; //constant help with readability
    private GameplayScreen gameplayScreen; //reference link back to the GameplayScreen
    private Texture emptyTile;
    private Texture emptyFloorTile;
    private Texture oneTile, twoTile, threeTile, fourTile, fiveTile, sixTile, sevenTile, eightTile;
    private Texture bombTile;
    private Texture flagTile;
    private int TILE_SIZE = 25;
    private final int X_OFFSET = 50;
    private final int Y_OFFSET = 125;


    public GameBoard(GameplayScreen gameplayScreen) {
        this.gameplayScreen = gameplayScreen;
        board = new int[16][30];
        numBombs = 50;
        numFlags = numBombs;
        loadGraphics();
        placeAllBombs();
    }
    public GameBoard(GameplayScreen gameplayScreen, int numRows, int numCols, int numBombs) {
        this.gameplayScreen = gameplayScreen;
        board = new int[numRows][numCols];
        this.numBombs = numBombs;
        numFlags = this.numBombs;
        loadGraphics();
        placeAllBombs();
        Location l = new Location(0,0);
        System.out.println(getNeighbors(l));
    }

    private void testBoard() {
        board[0][0] = 11;
        board[0][1] = 12;
        board[0][2] = 13;
        board[0][3] = 14;
        board[0][4] = 15;
        board[1][0] = 16;
        board[1][1] = 17;
        board[1][2] = 18;
        board[1][3] = 9;
        board[1][4] = -1;
        board[2][0] = 21;
        board[2][1] = 10;
    }

    public void draw(SpriteBatch spriteBatch) {
        for(int i=0; i<board.length;i++) {
            for(int j=0; j<board[i].length; j++){
                int x = X_OFFSET + (TILE_SIZE*j);
                int y = Y_OFFSET + (TILE_SIZE*i);
                int value = board[i][j];
                if(value <= 8) spriteBatch.draw(emptyTile, x, y);
                else if(value == 9) spriteBatch.draw(bombTile, x, y);
                else if(value == 10) spriteBatch.draw(emptyFloorTile, x, y);
                else if(value == 11) spriteBatch.draw(oneTile, x, y);
                else if(value == 12) spriteBatch.draw(twoTile, x, y);
                else if(value == 13) spriteBatch.draw(threeTile, x, y);
                else if(value == 14) spriteBatch.draw(fourTile, x, y);
                else if(value == 15) spriteBatch.draw(fiveTile, x, y);
                else if(value == 16) spriteBatch.draw(sixTile, x, y);
                else if(value == 17) spriteBatch.draw(sevenTile, x, y);
                else if(value == 18) spriteBatch.draw(eightTile, x, y);
                else if(value >= 19) spriteBatch.draw(flagTile, x, y);

                //temp draw code to show all bombs
                if(value == -1) spriteBatch.draw(bombTile, x, y);
            }
        }
    }

    //return true if loc is valid in the grid
    private boolean isValid(Location loc) {
        boolean x = loc.getCol() < board[0].length && loc.getCol() > -1;
        boolean y = loc.getRow() < board.length && loc.getRow() > -1;
        return x && y;
    }

    //returns 3-8 Locations that neighbor the given loc
    private ArrayList<Location> getNeighbors(Location loc) {
        ArrayList<Location> locs = new ArrayList<>();

        for(int i=loc.getRow()-1; i < loc.getRow()+2; i++) {
            for(int j=loc.getCol()-1; j < loc.getCol()+2; j++){
                Location l = new Location(i,j);
                if(l.equals(loc)) continue;
                if(isValid(l)) {
                    locs.add(l);
                }
            }
        }
        return locs;
    }

    private void placeAllBombs() {
        int count = numBombs;
        while(count > 0) {
            int row = (int)(Math.random() * board.length) ;
            int col = (int)(Math.random() * board[row].length) ;

            board[row][col] = -1;
            count--;
        }
    }

    public void loadGraphics() {
        emptyTile = new Texture("emptyTile.jpg");
        emptyFloorTile = new Texture("empty floor.jpg");
        oneTile = new Texture("oneTile.jpg");
        twoTile = new Texture("twoTile.jpg");
        threeTile = new Texture("threeTile.jpg");
        fourTile = new Texture("fourTile.jpg");
        fiveTile = new Texture("fiveTile.jpg");
        sixTile = new Texture("sixTile.jpg");
        sevenTile = new Texture("sevenTile.jpg");
        eightTile = new Texture("eightTile.jpg");
        bombTile = new Texture("bomb.jpg");
        flagTile = new Texture("flagTile.jpg");
    }
}
