package com.werryxgames.messaje;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;

/**
 * Default screen for Partitioned, that covers most cases.
 *
 * @since 1.0
 */
public abstract class DefaultScreen implements SocketScreen {

  final Messaje game;
  /**
   * Screen will be changed to {@code nextScreen} in next {@link DefaultScreen#render(float)}.
   *
   * @since 1.0
   */
  protected DefaultScreen nextScreen = null;
  ArrayList<Disposable> disposables = new ArrayList<>(8);
  private boolean initialized = false;

  /**
   * Default constructor for {@code DefaultScreen}.
   *
   * @param game Instance of {@link Messaje}.
   * @since 1.0
   */
  public DefaultScreen(final Messaje game) {
    super();
    this.game = game;
    this.game.stage.clear();
    this.game.client.currentScreen = this;
  }

  /**
   * Called every frame.
   *
   * @param delta Time since previous frame (in seconds).
   * @since 1.0
   */
  abstract void onUpdate(float delta);

  /**
   * Called, when window resizes.
   *
   * @param width  New width of window.
   * @param height New height of window.
   * @since 1.0
   */
  abstract void onResize(int width, int height);

  @Override
  public void render(float delta) {
    if (!this.initialized) {
      this.onResize(this.game.viewport.getScreenWidth(), this.game.viewport.getScreenHeight());
      this.init();
      this.initialized = true;
    }

    if (this.nextScreen != null) {
      this.dispose();
      this.game.setScreen(this.nextScreen);
      return;
    }

    ScreenUtils.clear(0.0f, 0.0f, 0.0f, 1.0f);
    this.game.camera.update();
    this.game.stage.act(delta);
    this.game.stage.draw();
    this.onUpdate(delta);
  }

  @Override
  public void resize(int width, int height) {
    Viewport viewport = this.game.stage.getViewport();
    viewport.update(width, height, true);
    this.onResize(width, height);
  }

  /**
   * Converts color to {@link TextureRegionDrawable}.
   *
   * @param color Color to convert.
   * @return Converted drawable.
   */
  public TextureRegionDrawable colorToDrawable(Color color) {
    Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    this.disposables.add(bgPixmap);
    bgPixmap.setColor(color);
    bgPixmap.fill();
    Texture bgTexture = new Texture(bgPixmap);
    this.disposables.add(bgTexture);
    return new TextureRegionDrawable(new TextureRegion(bgTexture));
  }

  abstract void onDispose();

  @Override
  public void dispose() {
    for (Disposable disposable : this.disposables) {
      disposable.dispose();
    }

    this.onDispose();
  }

  /**
   * Shows warning dialog.
   *
   * @param title       Tilte of warning.
   * @param description Description of warning.
   */
  public void warning(String title, String description) {
    Dialog dialog = new Dialog("", UiStyle.getWindowStyle(this.game.fontManager, 0, 32,
        this.colorToDrawable(new Color(0x00000080))));
    TextButton closeButton = new TextButton("Close",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24));
    ErrorDialog errorDialog = ErrorDialog.fromDialog(dialog, this.game, title,
        description, this.colorToDrawable(new Color(0x000000B8)),
        closeButton);
    closeButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        errorDialog.hide();
      }
    });
    errorDialog.show(this.game.stage);
  }

  /**
   * Changes screen to another, disposing current screen.
   *
   * @param nextScreen Screen, that will appear after this.
   * @since 1.0
   */
  public void changeScreen(DefaultScreen nextScreen) {
    this.nextScreen = nextScreen;
  }
}
