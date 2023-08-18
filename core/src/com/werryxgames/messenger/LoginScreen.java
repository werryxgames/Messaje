package com.werryxgames.messenger;

import java.nio.ByteBuffer;

/**
 * Screen for logging in to account.
 *
 * @since 1.0
 */
public class LoginScreen extends DefaultScreen {
  /**
   * Default constructor for {@code DefaultScreen}.
   *
   * @param game Instance of {@link MessengerJava}.
   * @since 1.0
   */
  public LoginScreen(MessengerJava game) {
    super(game);
  }

  @Override
  void onUpdate(float delta) {

  }

  @Override
  void onResize(int width, int height) {

  }

  @Override
  public void init() {

  }

  @Override
  public void onMessage(int code, ByteBuffer message) {
    this.game.logger.fine("Message from server: " + code + ", " + message.toString());
  }

  @Override
  public void onDisconnect() {

  }

  @Override
  public void show() {

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

  }
}
