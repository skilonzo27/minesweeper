package com.kilonzo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameBoard {
    public static final int BOMB = -1;
    public final int X_OFFSET = 50;
    private static final int TILE_SIZE = 20;
    private static final int Y_OFFSET = 660;

    private int[][] board;
    private final int numBombs;
    public int numFlags;
    private final int mode; // 0 = Fun sounds, 1 = Classic (no custom sounds)
    private boolean firstClick = true;
    private boolean lost = false;
    private boolean winSoundPlayed = false;

    private Texture emptyTile, emptyFloorTile;
    private Texture oneTile, twoTile, threeTile, fourTile;
    private Texture fiveTile, sixTile, sevenTile, eightTile;
    private Texture bombTile, flagTile;

    public GameBoard(GameplayScreen gameplayScreen, int rows, int cols, int numBombs, int mode) {
        this.numBombs = Math.min(numBombs, Math.max(0, rows * cols - 5));
        this.numFlags = this.numBombs;
        this.mode = mode;
        this.board = new int[rows][cols];
        loadGraphics();
    }

    public void draw(SpriteBatch batch) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                int x = X_OFFSET + TILE_SIZE * col;
                int y = Y_OFFSET - TILE_SIZE * row;
                int value = board[row][col];

                if (value <= 8) batch.draw(emptyTile, x, y, TILE_SIZE, TILE_SIZE);
                else if (value == 9) batch.draw(bombTile, x, y, TILE_SIZE, TILE_SIZE);
                else if (value == 10) batch.draw(emptyFloorTile, x, y, TILE_SIZE, TILE_SIZE);
                else if (value >= 11 && value <= 18) {
                    batch.draw(getNumberTexture(value - 10), x, y, TILE_SIZE, TILE_SIZE);
                } else if (value >= 19) batch.draw(flagTile, x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private Texture getNumberTexture(int n) {
        switch (n) {
            case 1: return oneTile;
            case 2: return twoTile;
            case 3: return threeTile;
            case 4: return fourTile;
            case 5: return fiveTile;
            case 6: return sixTile;
            case 7: return sevenTile;
            default: return eightTile;
        }
    }

    // Mouse coordinates must already be converted to world coordinates.
    public Location getTileAt(int mouseX, int mouseY) {
        int col = (mouseX - X_OFFSET) / TILE_SIZE;
        int row = (Y_OFFSET + TILE_SIZE - 1 - mouseY) / TILE_SIZE;
        if (mouseX < X_OFFSET || mouseY > Y_OFFSET + TILE_SIZE - 1) return null;
        Location loc = new Location(row, col);
        return isValid(loc) ? loc : null;
    }

    public void changeFlagStatus(Location loc) {
        if (loc == null || !isValid(loc) || lost || firstClick || isWon()) return;
        int value = board[loc.getRow()][loc.getCol()];
        if (value >= 0 && value <= 8) {
            if (numFlags <= 0) return;
            board[loc.getRow()][loc.getCol()] = value + 20;
            numFlags--;
            playSound("flag-on");
        } else if (value == BOMB) {
            if (numFlags <= 0) return;
            board[loc.getRow()][loc.getCol()] = 19;
            numFlags--;
            playSound("flag-on");
        } else if (value >= 20 && value <= 28) {
            board[loc.getRow()][loc.getCol()] = value - 20;
            numFlags++;
            playSound("flag-off");
        } else if (value == 19) {
            board[loc.getRow()][loc.getCol()] = BOMB;
            numFlags++;
            playSound("flag-off");
        }
    }

    public void uncoverTile(Location loc) {
        if (loc == null || !isValid(loc) || gameOver()) return;

        if (firstClick) {
            placeAllBombs(loc);
            numberBoard();
            firstClick = false;
        }

        int row = loc.getRow();
        int col = loc.getCol();
        int value = board[row][col];
        if (value >= 19) return;

        if (value == BOMB) {
            lost = true;
            showAllBombs();
            playSound("lose");
            return;
        }

        if (value >= 0 && value <= 8) {
            if (value == 0) floodFill(loc);
            else board[row][col] = value + 10;
            if (!gameOver()) {
                playSound("reveal");
            }
            if (isWon() && !winSoundPlayed) {
                winSoundPlayed = true;
                playSound("win");
            }
        }
    }

    private void floodFill(Location loc) {
        if (!isValid(loc)) return;
        int row = loc.getRow();
        int col = loc.getCol();
        int value = board[row][col];
        if (value < 0 || value > 8) return;

        board[row][col] = value + 10;
        if (value == 0) {
            for (Location neighbor : getNeighbors(loc)) floodFill(neighbor);
        }
    }

    private void numberBoard() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == BOMB) continue;
                int count = 0;
                for (Location neighbor : getNeighbors(new Location(row, col))) {
                    if (board[neighbor.getRow()][neighbor.getCol()] == BOMB) count++;
                }
                board[row][col] = count;
            }
        }
    }

    private boolean isValid(Location loc) {
        return loc != null && loc.getRow() >= 0 && loc.getRow() < board.length
                && loc.getCol() >= 0 && loc.getCol() < board[0].length;
    }

    private ArrayList<Location> getNeighbors(Location loc) {
        ArrayList<Location> neighbors = new ArrayList<>();
        for (int row = loc.getRow() - 1; row <= loc.getRow() + 1; row++) {
            for (int col = loc.getCol() - 1; col <= loc.getCol() + 1; col++) {
                if (row == loc.getRow() && col == loc.getCol()) continue;
                Location candidate = new Location(row, col);
                if (isValid(candidate)) neighbors.add(candidate);
            }
        }
        return neighbors;
    }

    private void placeAllBombs(Location first) {
        List<Location> candidates = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (row == first.getRow() && col == first.getCol()) continue;
                if (isOrthogonalNeighbor(row, col, first)) continue;
                candidates.add(new Location(row, col));
            }
        }
        Collections.shuffle(candidates);
        for (int i = 0; i < Math.min(numBombs, candidates.size()); i++) {
            Location loc = candidates.get(i);
            board[loc.getRow()][loc.getCol()] = BOMB;
        }
    }

    private boolean isOrthogonalNeighbor(int row, int col, Location first) {
        int dr = Math.abs(row - first.getRow());
        int dc = Math.abs(col - first.getCol());
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
    }

    private void showAllBombs() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == BOMB || board[row][col] == 19) board[row][col] = 9;
            }
        }
    }

    public boolean isWon() {
        if (lost) return false;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                int value = board[row][col];
                if (value != BOMB && value != 19 && !(value >= 10 && value <= 18)) return false;
            }
        }
        return !firstClick;
    }

    public boolean gameOver() {
        return lost || isWon();
    }

    // Fun mode uses the existing sound files; Classic mode uses synthesized retro beeps.
    private void playSound(String sound) {
        if (mode == 0) {
            String filename;
            switch (sound) {
                case "flag-on": filename = "universfield-bubble-pop-06-351337_wuEwgPrc.wav"; break;
                case "flag-off": filename = "universfield-bubble-pop-06-351337_wuEwgPrc (mp3cut.net).wav"; break;
                case "lose": filename = "you-been-a-bad-boy-yandere-mommy-wife-x-listener-comfort-f4m-asmrva-himerose_1Pxz7XT3.wav"; break;
                case "reveal": filename = "oh-yeah-baby-oh-yeah-sensual-male-moan-dan-barracuda-male-moan-12-oh-yeah-baby-1-0m06s.wav"; break;
                default: playClassicSound("win"); return;
            }
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filename));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                System.err.println("Could not play sound " + filename + ": " + e.getMessage());
            }
        } else {
            playClassicSound(sound);
        }
    }

    private void playClassicSound(String sound) {
        float sampleRate = 22050f;
        int[][] notes;
        switch (sound) {
            case "flag-on": notes = new int[][] {{1050, 65}, {1350, 55}}; break;
            case "flag-off": notes = new int[][] {{700, 75}}; break;
            case "reveal": notes = new int[][] {{880, 45}}; break;
            case "lose": notes = new int[][] {{500, 130}, {360, 160}, {220, 230}}; break;
            case "win": notes = new int[][] {{523, 90}, {659, 90}, {784, 90}, {1047, 180}}; break;
            default: return;
        }
        try {
            int totalSamples = 0;
            for (int[] note : notes) totalSamples += (int) (sampleRate * note[1] / 1000);
            byte[] pcm = new byte[totalSamples * 2];
            int offset = 0;
            for (int[] note : notes) {
                int count = (int) (sampleRate * note[1] / 1000);
                for (int i = 0; i < count; i++) {
                    double envelope = Math.min(1.0, Math.min(i / 100.0, (count - i) / 300.0));
                    short sample = (short) (Math.sin(2.0 * Math.PI * note[0] * i / sampleRate) * 9000 * Math.max(0, envelope));
                    pcm[offset++] = (byte) (sample & 0xff);
                    pcm[offset++] = (byte) ((sample >> 8) & 0xff);
                }
            }
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            try (AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(pcm), format, totalSamples)) {
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.start();
            }
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Could not play Classic sound " + sound + ": " + e.getMessage());
        }
    }
    private void loadGraphics() {
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
        flagTile = new Texture("Screenshot 2026-10-10 at 11.07.26 PM (1) (1).jpg");
    }

    public void dispose() {
        if (emptyTile != null) emptyTile.dispose();
        if (emptyFloorTile != null) emptyFloorTile.dispose();
        if (oneTile != null) oneTile.dispose();
        if (twoTile != null) twoTile.dispose();
        if (threeTile != null) threeTile.dispose();
        if (fourTile != null) fourTile.dispose();
        if (fiveTile != null) fiveTile.dispose();
        if (sixTile != null) sixTile.dispose();
        if (sevenTile != null) sevenTile.dispose();
        if (eightTile != null) eightTile.dispose();
        if (bombTile != null) bombTile.dispose();
        if (flagTile != null) flagTile.dispose();
    }
}
