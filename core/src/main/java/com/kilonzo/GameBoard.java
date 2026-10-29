package com.kilonzo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
import java.util.ArrayList;

public class GameBoard {
    private int[][] board;
    private int numBombs; // total number of bombs in grid
    public int numFlags; //the number of flags remaining to be placed by the player
    public static final int BOMB = -1; //constant help with readability
    private GameplayScreen gameplayScreen; //reference link back to the GameplayScreen
    private Texture emptyTile;
    private Texture emptyFloorTile;
    private Texture oneTile, twoTile, threeTile, fourTile, fiveTile, sixTile, sevenTile, eightTile;
    private Texture bombTile;
    private Texture flagTile;
    private boolean firstClick;
    private int TILE_SIZE = 25;
    public final int X_OFFSET = 50;
    private final int Y_OFFSET = 625;
    private Clip uncoverClip;
    public boolean winSoundPlayed = false;

    public GameBoard(GameplayScreen gameplayScreen) {
        this.gameplayScreen = gameplayScreen;
        board = new int[16][30];
        numBombs = 50;
        numFlags = numBombs;
        firstClick = true;
        loadGraphics();
    }
    public GameBoard(GameplayScreen gameplayScreen, int numRows, int numCols, int numBombs) {
        this.gameplayScreen = gameplayScreen;
        board = new int[numRows][numCols];
        this.numBombs = numBombs;
        numFlags = this.numBombs;
        firstClick = true;
        loadGraphics();
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
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    int x = X_OFFSET + (TILE_SIZE * j);
                    int y = Y_OFFSET - (TILE_SIZE * i);
                    int value = board[i][j];
                    if (value <= 8) spriteBatch.draw(emptyTile, x, y);
                    else if (value == 9) spriteBatch.draw(bombTile, x, y);
                    else if (value == 10) spriteBatch.draw(emptyFloorTile, x, y);
                    else if (value == 11) spriteBatch.draw(oneTile, x, y);
                    else if (value == 12) spriteBatch.draw(twoTile, x, y);
                    else if (value == 13) spriteBatch.draw(threeTile, x, y);
                    else if (value == 14) spriteBatch.draw(fourTile, x, y);
                    else if (value == 15) spriteBatch.draw(fiveTile, x, y);
                    else if (value == 16) spriteBatch.draw(sixTile, x, y);
                    else if (value == 17) spriteBatch.draw(sevenTile, x, y);
                    else if (value == 18) spriteBatch.draw(eightTile, x, y);
                    else if (value >= 19) spriteBatch.draw(flagTile, x, y);
                }
            }
    }

    /*given a (mouse_x, mouse_y) position,
    return the location on the board [row][col].
    Return null if Lcation is not valid
     */
    public Location getTileAt(int mouse_x, int mouse_y) {
        int x = 0, y = 0;
        x = (mouse_x - X_OFFSET) /TILE_SIZE;
        y = ((Y_OFFSET + mouse_y) / TILE_SIZE) - 28;
        //if (mouse_y > (GameplayScreen.WORLD_HEIGHT-Y_OFFSET-TILE_SIZE) / TILE_SIZE){
       //     y =(int)(GameplayScreen.WORLD_HEIGHT-Y_OFFSET-TILE_SIZE) / TILE_SIZE;
        //}
        Location l = new Location(x,y);
        /*if(firstClick) {
            placeAllBombs(l);
            firstClick = false;
            return l;
        }*/
        if(isValid(l)) return l;
        return null;
    }

    public void changeFlagStatus(Location loc){
        if(loc != null && isValid(loc)) {
            int boardValue = board[loc.getRow()][loc.getCol()];
            if(numFlags == 0){
                if(boardValue < 19) return;
            }
            if(boardValue >= -1 && boardValue <= 8) {
                board[loc.getRow()][loc.getCol()] += 20;
                numFlags--;
                flagSound();
            }
            else if(boardValue >= 19 && boardValue <= 28) {
                board[loc.getRow()][loc.getCol()] -= 20;
                numFlags++;
                flagRemoveSound();
            }
        }
    }

    public boolean hasWon() {
        if (hasLost()) return false;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int value = board[i][j];

                // Hidden safe tile
                if (value >= 0 && value <= 8) {
                    return false;
                }

                // Flagged safe tile: still not uncovered
                if (value >= 20 && value <= 28) {
                    return false;
                }
            }
        }

        if (!winSoundPlayed) {
            winSound();
            winSoundPlayed = true;
        }

        return true;
    }

    public boolean hasLost() {
        for (int[] row : board) {
            for (int value : row) {
                if (value == 9) {
                    return true;
                }
            }
        }

        return false;
    }

    //we +10 to uncover a tile
    public void uncoverTile(Location loc) {
        if(hasWon() || hasLost()) return;
        if (loc != null && isValid(loc)) {
            if(firstClick) {
                placeAllBombs(loc);
                numberBoard();
                firstClick = false;
            }
            int boardValue = board[loc.getRow()][loc.getCol()];
            if (boardValue == 0) {
                floodFill(loc);
            }
            else if (boardValue >= -1 && boardValue <= 8) {
                board[loc.getRow()][loc.getCol()] += 10;
                if (boardValue == -1) {
                    showAllBombs();
                }
            }

            if (hasWon() || hasLost()) {
                stopUncoverSound();
                return;
            }

            if (!hasWon() && !hasLost()) {
                if (boardValue >= 0 && boardValue <= 8) {
                    uncoverSound();
                } else if (boardValue == -1) {
                    uncoverBomb();
                }
            }
        }
    }

    //recursive algorithm
    /*
        If it's a zero
        +10
        recursive call on my 4 neighbors
        if it's -1 to 8
        +10
         */
    private void floodFill(Location loc) {
        int boardValue = board[loc.getRow()][loc.getCol()];
        if(isValid(loc)) {
            if (boardValue == 0) {
                board[loc.getRow()][loc.getCol()] += 10;
                Location l1 = new Location(loc.getCol()+1, loc.getRow());
                Location l2 = new Location(loc.getCol()-1, loc.getRow());
                Location l3 = new Location(loc.getCol(), loc.getRow()+1);
                Location l4 = new Location(loc.getCol(), loc.getRow()-1);
                if(isValid(l1)) floodFill(l1);
                if(isValid(l2)) floodFill(l2);
                if(isValid(l3)) floodFill(l3);
                if(isValid(l4)) floodFill(l4);
            } else if (boardValue >= -1 && boardValue <= 8) {
                board[loc.getRow()][loc.getCol()] += 10;
                return;
            }
        }
    }

    private void numberBoard() {
        for(int i=0; i<board.length; i++) {
            for(int j=0; j<board[i].length; j++){
                if(board[i][j] == -1) continue;
                int num = 0;
                ArrayList<Location> neighbors = getNeighbors(new Location(i,j));
                for(Location l : neighbors) {
                    if(board[l.getRow()][l.getCol()] == -1){
                        num++;
                    }
                }
                board[i][j] = num;
            }
        }
    }
    //return true if loc is valid in the grid
    private boolean isValid(Location loc) {
        boolean x = loc.getCol() < board[0].length && loc.getCol() > -1;
        boolean y = loc.getRow() < board.length && loc.getRow() > -1;
        return y && x;
    }

    //returns 3-8 Locations that neighbor the given loc
    private ArrayList<Location> getNeighbors(Location loc) {
        ArrayList<Location> locs = new ArrayList<>();

        for(int i=loc.getRow()-1; i <= loc.getRow()+1; i++) {
            for(int j=loc.getCol()-1; j <= loc.getCol()+1; j++){
                Location l = new Location(i,j);
                if(isValid(l)) {
                    locs.add(l);
                }
            }
        }
        return locs;
    }

    private void showAllBombs() {
        for(int i=0; i<board.length; i++){
            for(int j=0; j<board[i].length; j++){
                if(board[i][j] == -1) {
                    board[i][j] += 10;
                }
            }
        }
        uncoverBomb();
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

    private int getValue(Location l) {
        return board[l.getRow()][l.getCol()];
    }

    private void placeAllBombs(Location l) {
        int count = 0;

        while (count < numBombs) {
            int row = (int) (Math.random() * board.length);
            int col = (int) (Math.random() * board[row].length);

            if (row == l.getRow() && col == l.getCol()) continue;
            ArrayList<Location> neighbors = getNeighbors(l);
            for(Location loc : neighbors) {
                if(getValue(loc) == -1) continue;
            }

            if (board[row][col] == BOMB) continue;

            board[row][col] = BOMB;
            count++;
        }
    }

    private boolean isFourSides(int r, int c, Location l){
        if(l.getRow()-1 == r && l.getCol() == c) return true;
        if(l.getRow()+1 == r && l.getCol() == c) return true;
        if(l.getRow() == r && l.getCol()+1 == c) return true;
        if(l.getRow() == r && l.getCol()-1 == c) return true;
        return false;
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
        bombTile = new Texture("IMG_4958 (1) (1).jpeg");
        flagTile = new Texture("Screenshot 2026-10-10 at 11.07.26 PM (1) (1).jpg");
    }

    private void uncoverSound() {
        try {
            File soundFile = new File(
                "oh-yeah-baby-oh-yeah-sensual-male-moan-dan-barracuda-male-moan-12-oh-yeah-baby-1-0m06s.wav"
            );

            AudioInputStream audioStream =
                AudioSystem.getAudioInputStream(soundFile);

            uncoverClip = AudioSystem.getClip();
            uncoverClip.open(audioStream);
            uncoverClip.start();

        } catch (UnsupportedAudioFileException |
                 LineUnavailableException |
                 IOException e) {
            e.printStackTrace();
        }
    }

    public void stopUncoverSound() {
        if (uncoverClip != null && uncoverClip.isRunning()) {
            uncoverClip.stop();
        }
    }

    private void uncoverBomb(){
        try{
            File soundFile = new File("you-been-a-bad-boy-yandere-mommy-wife-x-listener-comfort-f4m-asmrva-himerose_1Pxz7XT3.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e){
            e.printStackTrace();
        }
    }

    private void flagSound(){
        try{
            File soundFile = new File("universfield-bubble-pop-06-351337_wuEwgPrc.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e){
            e.printStackTrace();
        }
    }

    private void flagRemoveSound(){
        try{
            File soundFile = new File("universfield-bubble-pop-06-351337_wuEwgPrc (mp3cut.net).wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e){
            e.printStackTrace();
        }
    }
    public void winSound(){
        try{
            File soundFile = new File("mommy-praises-her-hardworking-boy-f4m-good-boy-appreciation-kisses-mommy-as_dWJAN0qc.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        }
        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e){
            e.printStackTrace();
        }
    }
}
