package com.kilonzo;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameplayScreen implements Screen {

    private static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;
    private int width = 15;
    private int height = 30;
    private int bombs = 45;
    private int time = 0;
    private long startTime = 0;
    private boolean started = false;

    //Objects that allows us to write stuff
    private BitmapFont endFont;
    private BitmapFont titleFont;
    private BitmapFont infoFont;

    //Object that allows us to draw all our graphics
    private SpriteBatch spriteBatch;

    //Object that allwos us to draw shapes
    private ShapeRenderer shapeRenderer;

    //Camera to view our virtual world
    private Camera camera;

    //control how the camera views the world
    //zoom in/out? Keep everything scaled?
    private Viewport viewport;

    //Game variables
    GameBoard board;

    //runs one time, at the very beginning
    //all setup should happen here
    @Override
    public void show() {
        camera = new OrthographicCamera(); //2D camera
        camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2,0);//move the camera
        camera.update();

        //freeze my view to 800x600, no matter the window size
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        board = new GameBoard(this);

        spriteBatch = new SpriteBatch();

        endFont = new BitmapFont();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Robot Crush.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 2f;

        infoFont = generator.generateFont(parameter);

        parameter.size = 48;
        parameter.borderWidth = 0;
        titleFont = generator.generateFont(parameter);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true); //???, I just know that this was the solution to an annoying problem

        board = new GameBoard(this, width, height, bombs);
    }

    public void clearScreen() {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void handlemouseClick() {
        //check for left click
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            board.uncoverTile(board.getTileAt(Gdx.input.getX(), Gdx.input.getY()));
        }
        if(Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            board.changeFlagStatus(board.getTileAt(Gdx.input.getX(), Gdx.input.getY()));
        }
        if(!started && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            started = true;
            startTime = System.currentTimeMillis();
        }
    }

    //this method runs as fast as it can, repeatedly, constantly looped
    @Override
    public void render(float delta) {
        clearScreen();

        //user input
        handlemouseClick();
        if(board.gameOver()) {
            pause();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            board = new GameBoard(this, width, height, bombs);
            started = false;
        }

        if(started) {
            time = (int) ((System.currentTimeMillis()-startTime) / 1000);
        }

        //all drawing of shapes MUST be in between begin/end
        shapeRenderer.begin();
        shapeRenderer.end();

        //all drawing of graphics MUST be in between begin/end
        spriteBatch.begin();
        board.draw(spriteBatch);
        titleFont.draw(spriteBatch, "Minesweeper", 50, 700);
        infoFont.draw(spriteBatch, "Flags left: " + board.numFlags, (height * 25)+board.X_OFFSET+50, 650);
        infoFont.draw(spriteBatch, "Time: " + time, (height * 25)+board.X_OFFSET+50,625);
        if(board.gameOver()) {
            if (board.numFlags == 0){
                infoFont.draw(spriteBatch, "You Win!", 100, 150);
                infoFont.draw(spriteBatch, "Press R to restart", 100, 100);
            }
            else {
                infoFont.draw(spriteBatch, "You lost dummy", 100, 150);
                infoFont.draw(spriteBatch, "Press R to restart", 100, 100);
            }
        }

        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
}

