package com.werryxgames.messenger;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessengerJava extends Game {
  public static final Protocol CONNECTION_PROTOCOL = Protocol.TCP;
  public static final String SERVER_HOST = "185.6.27.126";
  public static final int SERVER_PORT = 9451;
  public static final SocketHints CONNECTION_PARAMS = new SocketHints();
  public static final boolean DEBUG_BUILD = true;

  public FontManager fontManager;
  public Logger logger;
  public OrthographicCamera camera;
  public SpriteBatch batch;
  public Client client;
  public ScreenViewport viewport;
  public Stage stage;

  @Override
  public void create () {
    this.logger = Logger.getLogger("Messenger");

    if (MessengerJava.DEBUG_BUILD) {
      this.logger.setLevel(Level.ALL);
    }
    else {
      this.logger.setLevel(Level.WARNING);
    }

    this.camera = new OrthographicCamera();
    this.camera.setToOrtho(false, 1280, 720);
    this.batch = new SpriteBatch();
    this.fontManager = new FontManager("font.ttf");
    UIStyle.create();

    this.viewport = new ScreenViewport(camera);
    this.stage = new Stage(this.viewport);

    MessengerJava.CONNECTION_PARAMS.connectTimeout = 5000;
    MessengerJava.CONNECTION_PARAMS.tcpNoDelay = true;
    // TODO: Make it async
    this.client = new Client(
        this,
        MessengerJava.CONNECTION_PROTOCOL,
        MessengerJava.SERVER_HOST,
        MessengerJava.SERVER_PORT,
        MessengerJava.CONNECTION_PARAMS
    );

    if (!this.client.isConnected()) {
      this.logger.warning("Unable to connect to server");
    }

    this.setScreen(new LoginScreen(this));
    Gdx.input.setInputProcessor(this.stage);
    this.logger.info("Messenger by Werryx Games");
  }

  @Override
  public void render () {
    super.render();
  }

  @Override
  public void dispose () {
    this.client.dispose();
    this.batch.dispose();
    this.fontManager.dispose();
    this.stage.dispose();
  }
}
