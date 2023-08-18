package com.werryxgames.messenger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * Client, that draws to screen player data.
 *
 * @since 1.0
 */
public class Client {
  public MessengerJava game;
  public Socket socket;
  public Protocol protocol;
  public String host;
  public int port;
  public SocketHints socketParams;
  /**
   * {@link SocketScreen}, that will receive all incoming Websocket messages.
   *
   * @since 1.0
   */
  public SocketScreen currentScreen = null;
  protected Thread receiveThread;
  protected Thread sendThread;
  protected InputStream inputStream;
  protected OutputStream outputStream;
  protected Queue<byte[]> receiveBytes = new Queue<>();
  protected Queue<byte[]> sendBytes = new Queue<>();

  /**
   * Client constructor.
   *
   * @param game     Instance of {@link MessengerJava}.
   * @param protocol Protocol for connection.
   * @param host     Host of server for connection.
   * @param port     Port of server for connection.
   * @param hints    {@link SocketHints} for connection.
   * @since 1.0
   */
  public Client(MessengerJava game, Protocol protocol, String host, int port, SocketHints hints) {
    this.game = game;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.socketParams = hints;
    this.receiveThread = new Thread(() -> {
      while (true) {
        boolean isConnected = this.isConnected();

        if (!isConnected) {
          if (this.currentScreen != null) {
            this.currentScreen.onDisconnect();
          }

          break;
        }

        int availableBytes = 0;

        try {
          availableBytes = this.inputStream.available();
        } catch (IOException e) {
          this.game.logger.severe(e.getMessage());
        }

        if (availableBytes == 0) {
          continue;
        }

        @SuppressWarnings("ObjectAllocationInLoop") // Better than allocate 4 GiB once.
        byte[] sentBytes = new byte[availableBytes];

        try {
          int receivedBytes = this.inputStream.read(sentBytes);

          if (receivedBytes != availableBytes) {
            //noinspection HardcodedFileSeparator
            this.game.logger.warning(
                String.format(Locale.ENGLISH, "Incorrect read size: %d / %d", receivedBytes,
                    availableBytes));
          }
        } catch (IOException e) {
          this.game.logger.severe(e.getMessage());
        }

        this.receiveBytes.addLast(sentBytes);

        if (this.currentScreen != null) {
          byte[] lastBytes;

          while (!this.receiveBytes.isEmpty()) {
            lastBytes = this.receiveBytes.removeFirst();
            @SuppressWarnings("ObjectAllocationInLoop") ByteBuffer packet =
                ByteBuffer.wrap(lastBytes);
            this.currentScreen.onMessage(packet.getInt(), packet);
          }
        }
      }
    });
    this.sendThread = new Thread(() -> {
      while (true) {
        if (this.sendBytes.size == 0) {
          return;
        }

        try {
          this.outputStream.write(this.sendBytes.removeFirst());
          this.outputStream.flush();
        } catch (IOException e) {
          this.game.logger.severe(e.getMessage());
        }
      }
    });
    this.reconnectBlocking(3);
  }

  /**
   * Tries to reconnect to server in current thread.
   *
   * @param attempts Number of attempts to reconnect.
   * @since 1.0
   */
  public void reconnectBlocking(int attempts) {
    for (int i = 0; i < attempts; i++) {
      this.reconnectBlocking();

      if (this.isConnected()) {
        return;
      }
    }
  }

  /**
   * Tries to reconnect to server in current thread 1 time.
   *
   * @since 1.0
   */
  public void reconnectBlocking() {
    try {
      this.socket = Gdx.net.newClientSocket(this.protocol, this.host, this.port, this.socketParams);
    } catch (GdxRuntimeException e) {
      return;
    }

    this.inputStream = this.socket.getInputStream();
    this.outputStream = this.socket.getOutputStream();
    this.receiveThread.start();
    this.sendThread.start();
  }

  public void send(byte[] bytes) {
    this.sendBytes.addLast(bytes);
  }

  public void send(ByteBuffer buffer) {
    this.sendBytes.addLast(buffer.array());
  }

  public boolean isConnected() {
    return this.socket != null && this.socket.isConnected();
  }

  /**
   * Disposes all used resources.
   * Must be called at program exit.
   *
   * @since 1.0
   */
  public void dispose() {
    if (this.socket != null) {
      this.socket.dispose();
    }
  }
}
