package com.werryxgames.messenger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.logging.Level;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
  public DataInputStream inputStream = null;
  public DataOutputStream outputStream = null;
  protected Thread receiveThread;
  protected Thread sendThread;
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
    this.reconnect(3);
  }

  protected void receiveLoop() throws RuntimeException {
    while (true) {
      if (!this.isConnected()) {
        if (this.currentScreen != null) {
          this.currentScreen.onDisconnect();
        }

        break;
      }

      int availableBytes = 0;

      try {
        availableBytes = this.inputStream.readInt();
      } catch (EOFException e) {
        this.socket.dispose();
        this.socket = null;
        continue;
      } catch (SocketException e) {
        continue;
      } catch (IOException e) {
        this.game.logger.log(Level.SEVERE, e.getMessage(), e);
      }

      if (availableBytes < 4) {
        continue;
      }

      @SuppressWarnings("ObjectAllocationInLoop") // Better than 4 GiB once allocation.
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
        this.game.logger.log(Level.SEVERE, e.getMessage(), e);
      }

      try {
        this.receiveBytes.addLast(Aes.decrypt(sentBytes));
      } catch (InvalidAlgorithmParameterException | NoSuchPaddingException
               | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
               | InvalidKeyException e) {
        this.game.logger.log(Level.SEVERE, e.getMessage(), e);
      }

      if (this.currentScreen != null) {
        byte[] lastBytes;

        while (!this.receiveBytes.isEmpty()) {
          lastBytes = this.receiveBytes.removeFirst();
          @SuppressWarnings("ObjectAllocationInLoop") StringBuilder stringBuilder =
              new StringBuilder(lastBytes.length + 1);
          stringBuilder.append("Message from server: ");

          for (byte b : lastBytes) {
            stringBuilder.append(String.format("%02X", b));
          }

          this.game.logger.finest(stringBuilder.toString());
          @SuppressWarnings("ObjectAllocationInLoop") ByteBuffer packet =
              ByteBuffer.wrap(lastBytes);
          this.currentScreen.onMessage(packet.getShort(), packet);
        }
      }
    }
  }

  protected void sendLoop() {
    while (this.isConnected()) {
      if (this.sendBytes.size == 0) {
        return;
      }

      this.sendBlocking(this.sendBytes.removeFirst());
    }
  }

  /**
   * Tries to reconnect to server in current thread 1 time.
   *
   * @since 1.0
   */
  public void reconnectBlocking() {
    if (this.socket != null) {
      try {
        this.inputStream.close();
        this.outputStream.close();
      } catch (IOException e) {
        this.game.logger.log(Level.SEVERE, e.getMessage(), e);
      }

      this.socket.dispose();
      this.socket = null;

      try {
        this.receiveThread.join(20);
        this.sendThread.join(20);
      } catch (InterruptedException e) {
        this.game.logger.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    try {
      this.socket = Gdx.net.newClientSocket(this.protocol, this.host, this.port, this.socketParams);
    } catch (GdxRuntimeException e) {
      return;
    }

    this.inputStream = new DataInputStream(this.socket.getInputStream());
    this.outputStream = new DataOutputStream(this.socket.getOutputStream());
    this.receiveThread = new Thread(this::receiveLoop);
    this.sendThread = new Thread(this::sendLoop);
    this.receiveThread.start();
    this.sendThread.start();
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
   * Tries to reconnect to server asynchronously 1 time.
   *
   * @since 1.0
   */
  public void reconnect() {
    new Thread(this::reconnectBlocking).start();
  }

  /**
   * Tries to reconnect to server asynchronously.
   *
   * @param attempts Number of attempts to reconnect.
   * @since 1.0
   */
  public void reconnect(int attempts) {
    new Thread(() -> this.reconnectBlocking(attempts)).start();
  }

  /**
   * Sends message, blocking current thread until message is sent, or until failure.
   *
   * @param bytes Data to be sent.
   */
  public void sendBlocking(byte[] bytes) {
    try {
      byte[] encryptedBytes = Aes.encrypt(bytes);
      this.outputStream.writeInt(encryptedBytes.length);
      this.outputStream.write(encryptedBytes);
      this.outputStream.flush();
    } catch (InvalidAlgorithmParameterException | NoSuchPaddingException
             | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
             | InvalidKeyException | IOException e) {
      this.game.logger.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public void sendBlocking(ByteBuffer buffer) {
    this.sendBlocking(buffer.array());
  }

  public void send(byte[] bytes) {
    this.sendBytes.addLast(bytes);
  }

  public void send(ByteBuffer buffer) {
    this.send(buffer.array());
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
