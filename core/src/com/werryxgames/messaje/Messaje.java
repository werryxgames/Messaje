package com.werryxgames.messaje;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for client.
 *
 * @since 1.0
 */
public class Messaje extends Game {
  public static final boolean DEBUG_BUILD = Config.get("debug", true);
  public static final Protocol CONNECTION_PROTOCOL = Protocol.TCP;
  public static final String SERVER_HOST = Config.get("server.host",
      DEBUG_BUILD ? "127.0.0.1" : "185.6.27.126");
  public static final int SERVER_PORT = Config.get("server.port", 9451);
  public static final SocketHints CONNECTION_PARAMS = new SocketHints();

  public FontManager fontManager;
  public Logger logger;
  public OrthographicCamera camera;
  public SpriteBatch batch;
  public Client client;
  public ScreenViewport viewport;
  public Stage stage;

  public Messaje() {
    super();
  }

  @Override
  public void create() {
    this.logger = Logger.getLogger("Messaje");

    if (Messaje.DEBUG_BUILD) {
      this.logger.setLevel(Level.ALL);
    } else {
      this.logger.setLevel(Level.WARNING);
    }

    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(this.logger.getLevel());
    this.logger.addHandler(consoleHandler);
    this.logger.setUseParentHandlers(false);

    if (Messaje.DEBUG_BUILD) {
      this.logger.fine("Using debug build is not recommended, consider using release builds");
    }

    this.camera = new OrthographicCamera();
    this.camera.setToOrtho(false, 1280, 720);
    this.batch = new SpriteBatch();
    this.fontManager = new FontManager(
        "fonts" + File.separator + "OpenSans-Regular.ttf",
        "fonts" + File.separator + "OpenSans-Light.ttf",
        "fonts" + File.separator + "OpenSans-Italic.ttf",
        "fonts" + File.separator + "OpenSans-Bold.ttf",
        "fonts" + File.separator + "OpenSans-BoldItalic.ttf",
        "fonts" + File.separator + "PTMono-Regular.ttf"
    );
    UiStyle.create();

    this.viewport = new ScreenViewport(camera);
    this.stage = new Stage(this.viewport);

    Messaje.CONNECTION_PARAMS.connectTimeout = 5000;
    this.client = new Client(this, Messaje.CONNECTION_PROTOCOL, Messaje.SERVER_HOST,
        Messaje.SERVER_PORT, Messaje.CONNECTION_PARAMS);

    this.setScreen(new LoginScreen(this));
    Gdx.input.setInputProcessor(this.stage);
    this.logger.info("Messaje by Werryx Games");
  }

  @Override
  public void render() {
    super.render();
  }

  public void logException(Exception e, Level level) {
    this.logger.log(level, e.getMessage(), e);
  }

  public void logException(Exception e) {
    this.logException(e, Level.SEVERE);
  }

  @Override
  public void dispose() {
    this.client.dispose();
    this.batch.dispose();
    this.fontManager.dispose();
    this.stage.dispose();
  }
}
