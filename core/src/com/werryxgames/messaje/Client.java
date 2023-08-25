package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Client, that draws to screen player data.
 *
 * @since 1.0
 */
public class Client {

  public Messaje game;
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
  public DefaultScreen currentScreen = null;
  public DataInputStream inputStream = null;
  public DataOutputStream outputStream = null;
  public boolean reconnecting = false;
  protected Thread receiveThread = null;
  protected Thread sendThread = null;
  protected Thread reconnectThread = null;
  protected ConcurrentLinkedQueue<byte[]> receiveBytes = new ConcurrentLinkedQueue<>();
  protected ConcurrentLinkedQueue<byte[]> sendBytes = new ConcurrentLinkedQueue<>();
  protected ErrorDialog reconnectDialog = null;
  protected boolean closing = false;

  /**
   * Client constructor.
   *
   * @param game     Instance of {@link Messaje}.
   * @param protocol Protocol for connection.
   * @param host     Host of server for connection.
   * @param port     Port of server for connection.
   * @param hints    {@link SocketHints} for connection.
   * @since 1.0
   */
  public Client(Messaje game, Protocol protocol, String host, int port, SocketHints hints) {
    this.game = game;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.socketParams = hints;
    this.reconnect(3);
  }

  protected void reconnectLoop() {
    while (!(this.closing || this.isConnected())) {
      this.reconnectBlocking();
    }
  }

  protected void receiveLoop() throws RuntimeException {
    while (!this.closing) {
      if (!this.isConnected() && !this.reconnecting) {
        if (this.currentScreen != null) {
          this.currentScreen.networkHandlerQueue.add(() -> {
            this.reconnectDialog = new ErrorDialog(this.currentScreen.game, "Reconnecting",
                "Disconnected from server. Reconnecting...",
                this.currentScreen.colorToDrawable(new Color(0x000000B8)));
            this.reconnectDialog.show(this.currentScreen.game.stage);
          });
          this.currentScreen.onDisconnect();
        }

        this.reconnectThread = new Thread(this::reconnectLoop);
        this.reconnectThread.start();
        this.reconnecting = true;
        break;
      }

      if (this.reconnecting) {
        if (this.currentScreen != null) {
          if (this.reconnectDialog != null) {
            this.reconnectDialog.hide();
            this.reconnectDialog = null;
          }

          this.currentScreen.onReconnect();
        }

        try {
          this.reconnectThread.join();
        } catch (InterruptedException e) {
          this.game.logException(e);
        }

        this.reconnectThread = null;
        this.reconnecting = false;
      }

      int availableBytes = 0;

      try {
        availableBytes = this.inputStream.readInt();
      } catch (EOFException e) {
        this.socket.dispose();
        continue;
      } catch (SocketException e) {
        continue;
      } catch (IOException e) {
        this.game.logException(e);
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
        this.game.logException(e);
      }

      try {
        this.receiveBytes.add(Aes.decrypt(sentBytes));
      } catch (InvalidAlgorithmParameterException | NoSuchPaddingException
               | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
               | InvalidKeyException e) {
        this.game.logException(e);
      }

      if (this.currentScreen != null) {
        byte[] lastBytes;

        while (!this.receiveBytes.isEmpty()) {
          lastBytes = this.receiveBytes.poll();
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
      if (this.sendBytes.size() == 0) {
        continue;
      }

      this.sendBlocking(this.sendBytes.poll());
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
        this.game.logException(e);
      }

      this.socket.dispose();
      this.socket = null;

      try {
        this.receiveThread.join(20);
        this.sendThread.join(20);
      } catch (InterruptedException e) {
        this.game.logException(e);
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
  public boolean reconnectBlocking(int attempts) {
    for (int i = 0; i < attempts; i++) {
      this.reconnectBlocking();

      if (this.isConnected()) {
        this.game.logger.fine("Connected to server");
        return true;
      }
    }

    return false;
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
      this.game.logException(e);
    }
  }

  public void sendBlocking(ByteBuffer buffer) {
    this.sendBlocking(buffer.array());
  }

  public void send(byte[] bytes) {
    this.sendBytes.add(bytes);
  }

  public void send(ByteBuffer buffer) {
    this.send(buffer.array());
  }

  public boolean isConnected() {
    return this.socket != null && this.socket.isConnected();
  }

  /**
   * Disposes all used resources. Must be called at program exit.
   *
   * @since 1.0
   */
  public void dispose() {
    this.closing = true;

    if (this.socket != null) {
      this.socket.dispose();
      this.socket = null;
    }

    try {
      if (this.sendThread != null) {
        this.sendThread.join();
      }

      if (this.receiveThread != null) {
        this.receiveThread.join();
      }

      if (this.reconnectThread != null) {
        this.reconnectThread.join();
      }
    } catch (InterruptedException e) {
      this.game.logException(e);
    }
  }
}
