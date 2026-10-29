package com.kilonzo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private final Game game;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;

    private TextButton playButton, easyButton, mediumButton, hardButton;
    private TextButton funButton, classicButton;
    private Label settingsLabel;

    private int rows = 9;
    private int cols = 9;
    private int bombs = 10;
    private int mode = 0; // 0 = Fun, 1 = Classic

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        spriteBatch = new SpriteBatch();
        stage = new Stage(viewport, spriteBatch);
        Gdx.input.setInputProcessor(stage);
        skin = createSkin();

        Label titleLabel = new Label("MINESWEEPER", skin);
        settingsLabel = new Label("", skin);
        titleLabel.setPosition(200, 500);
        titleLabel.setFontScale(2f);
        settingsLabel.setPosition(200, 430);

        playButton = new TextButton("Play", skin);
        easyButton = new TextButton("Easy", skin);
        mediumButton = new TextButton("Medium", skin);
        hardButton = new TextButton("Hard", skin);
        funButton = new TextButton("Fun Mode", skin);
        classicButton = new TextButton("Classic Mode", skin);

        playButton.setPosition(200, 200);
        playButton.setSize(180, 60);
        easyButton.setPosition(600, 150);
        mediumButton.setPosition(600, 220);
        hardButton.setPosition(600, 290);
        funButton.setPosition(390, 220);
        classicButton.setPosition(390, 290);
        easyButton.setSize(160, 50);
        mediumButton.setSize(160, 50);
        hardButton.setSize(160, 50);
        funButton.setSize(180, 50);
        classicButton.setSize(180, 50);

        stage.addActor(titleLabel);
        stage.addActor(settingsLabel);
        stage.addActor(playButton);
        stage.addActor(easyButton);
        stage.addActor(mediumButton);
        stage.addActor(hardButton);
        stage.addActor(funButton);
        stage.addActor(classicButton);

        easyButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                rows = 9; cols = 9; bombs = 10;
                updateSelectionColors();
            }
        });
        mediumButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                rows = 16; cols = 16; bombs = 40;
                updateSelectionColors();
            }
        });
        hardButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                rows = 16; cols = 30; bombs = 99;
                updateSelectionColors();
            }
        });
        funButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                mode = 0;
                updateSelectionColors();
            }
        });
        classicButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                mode = 1;
                updateSelectionColors();
            }
        });
        playButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameplayScreen(game, rows, cols, bombs, mode));
            }
        });

        updateSelectionColors();
    }

    private Skin createSkin() {
        Skin newSkin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        BitmapFont font = new BitmapFont();
        newSkin.add("default-font", font, BitmapFont.class);
        newSkin.add("white", texture, Texture.class);

        TextureRegionDrawable normal = new TextureRegionDrawable(new TextureRegion(texture));
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = normal.tint(new Color(0.18f, 0.18f, 0.18f, 1f));
        buttonStyle.down = normal.tint(new Color(0.35f, 0.35f, 0.35f, 1f));
        buttonStyle.checked = normal.tint(new Color(0.25f, 0.45f, 0.25f, 1f));
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        newSkin.add("default", buttonStyle, TextButton.TextButtonStyle.class);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        newSkin.add("default", labelStyle, Label.LabelStyle.class);
        return newSkin;
    }

    private void updateSelectionColors() {
        String difficultyName;
        if (rows == 9 && cols == 9) difficultyName = "Easy";
        else if (rows == 16 && cols == 16) difficultyName = "Medium";
        else difficultyName = "Hard";
        String modeName = mode == 0 ? "Fun" : "Classic";
        settingsLabel.setText("Selected: " + difficultyName + " | " + modeName
                + " Mode | " + bombs + " bombs");

        easyButton.setColor(rows == 9 && cols == 9 ? Color.GREEN : Color.WHITE);
        mediumButton.setColor(rows == 16 && cols == 16 ? Color.GREEN : Color.WHITE);
        hardButton.setColor(rows == 16 && cols == 30 ? Color.GREEN : Color.WHITE);
        funButton.setColor(mode == 0 ? Color.GREEN : Color.WHITE);
        classicButton.setColor(mode == 1 ? Color.GREEN : Color.WHITE);
    }

    @Override public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
