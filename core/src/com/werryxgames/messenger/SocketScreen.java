package com.werryxgames.messenger;

import com.badlogic.gdx.Screen;
import java.nio.ByteBuffer;

/**
 * {@link Screen}, that can receive Websocket messages.
 *
 * @since 1.0
 */
public interface SocketScreen extends Screen {
  /**
   * Method, that is called, after current screen is changed to this screen.
   * Exists, because some operations (for example loading resources) can be done only in main
   * thread, but constructor can be called in non-main thread.
   *
   * @since 1.0
   */
  void init();

  /**
   * Method, that is called, when Websocket message is received.
   *
   * @param code    Code of websocket message.
   * @param message Websocket message. It is always ByteBuffer, sought to position 4.
   * @since 1.0
   */
  void onMessage(int code, ByteBuffer message);

  /**
   * Method, that is called, when client is disconnected from server.
   *
   * @since 1.0
   */
  void onDisconnect();
}
