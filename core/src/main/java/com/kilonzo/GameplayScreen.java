package com.kilonzo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class GameplayScreen implements Screen {
    private static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;

    private final Game game;
    private final int rows;
    private final int cols;
    private final int bombs;
    private final int mode;

    private int time = 0;
    private long startTime = 0;
    private boolean started = false;

    private BitmapFont titleFont;
    private BitmapFont infoFont;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private GameBoard board;
    private FreeTypeFontGenerator generator;

    public GameplayScreen(Game game, int rows, int cols, int bombs, int mode) {
        this.game = game;
        this.rows = rows;
        this.cols = cols;
        this.bombs = bombs;
        this.mode = mode;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("Robot Crush.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 2f;
        infoFont = generator.generateFont(parameter);

        parameter.size = 48;
        parameter.borderWidth = 0;
        titleFont = generator.generateFont(parameter);

        board = new GameBoard(this, rows, cols, bombs, mode);
    }

    private void handleMouseClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 world = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            Location loc = board.getTileAt((int) world.x, (int) world.y);
            if (loc != null) {
                if (!started) {
                    started = true;
                    startTime = System.currentTimeMillis();
                }
                board.uncoverTile(loc);
            }
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            Vector2 world = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            board.changeFlagStatus(board.getTileAt((int) world.x, (int) world.y));
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        handleMouseClick();

        if (started && !board.gameOver()) {
            time = (int) ((System.currentTimeMillis() - startTime) / 1000);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) resetBoard();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        board.draw(spriteBatch);
        titleFont.draw(spriteBatch, "Minesweeper", 50, 710);
        infoFont.draw(spriteBatch, "Flags left: " + board.numFlags, 800, 650);
        infoFont.draw(spriteBatch, "Time: " + time, 800, 615);
        String difficultyName = rows == 9 && cols == 9 ? "Easy"
                : rows == 16 && cols == 16 ? "Medium" : "Hard";
        String modeName = mode == 0 ? "Fun" : "Classic";
        infoFont.draw(spriteBatch, "Difficulty: " + difficultyName, 800, 580);
        infoFont.draw(spriteBatch, "Mode: " + modeName, 800, 545);

        if (board.gameOver()) {
            infoFont.draw(spriteBatch, board.isWon() ? "You Win!" : "Game Over", 800, 500);
            infoFont.draw(spriteBatch, "Press R to restart", 800, 465);
            infoFont.draw(spriteBatch, "Press ESC for menu", 800, 430);
        }
        spriteBatch.end();
    }

    private void resetBoard() {
        board.dispose();
        board = new GameBoard(this, rows, cols, bombs, mode);
        started = false;
        startTime = 0;
        time = 0;
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        if (board != null) board.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
        if (infoFont != null) infoFont.dispose();
        if (generator != null) generator.dispose();
    }
}
