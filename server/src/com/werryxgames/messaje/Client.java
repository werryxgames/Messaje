package com.werryxgames.messaje;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class for connected client.
 *
 * @since 1.0
 */
public class Client {
  public Server server;
  public Socket socket;
  public DataInputStream inputStream;
  public DataOutputStream outputStream;
  protected ConcurrentLinkedQueue<byte[]> packets = new ConcurrentLinkedQueue<>();
  protected long accountId = 0;

  /**
   * Constructor for {@link Client}.
   *
   * @param server       Instance of {@link Server}.
   * @param clientSocket Socket of connected client.
   */
  public Client(Server server, Socket clientSocket) {
    this.server = server;
    this.socket = clientSocket;

    try {
      this.inputStream = new DataInputStream(clientSocket.getInputStream());
      this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
    } catch (IOException e) {
      this.server.logException(e);
    }

    new Thread(this::receiveHandleLoop).start();
  }

  /**
   * Handles message from client.
   *
   * @param buffer Message from client.
   */
  public void handle(ByteBuffer buffer) {
    byte[] array = buffer.array();
    StringBuilder stringBuilder = new StringBuilder(array.length + 1);
    stringBuilder.append("Message from client: ");

    for (byte b : array) {
      stringBuilder.append(String.format("%02X", b));
    }

    this.server.logger.finest(stringBuilder.toString());

    short code = buffer.getShort();

    switch (code) {
      case 0 -> {
        int loginLength = buffer.get();
        byte[] loginBytes = new byte[loginLength];

        if (loginBytes.length > 64) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 3);
          this.send(sendBuffer);
          return;
        }

        buffer.get(loginBytes);
        String login = new String(loginBytes, StandardCharsets.UTF_8);

        if (login.length() > 16) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 3);
          this.send(sendBuffer);
          return;
        }

        byte[] passwordHash = new byte[32];
        buffer.get(passwordHash);

        try (ResultSet userWithSameLogin = this.server.db.query(
            "SELECT 1 FROM accounts WHERE login = ?", login)) {
          if (userWithSameLogin == null) {
            ByteBuffer sendBuffer = ByteBuffer.allocate(2);
            sendBuffer.putShort((short) 1);
            this.send(sendBuffer);
            return;
          }

          if (userWithSameLogin.next()) {
            ByteBuffer sendBuffer = ByteBuffer.allocate(2);
            sendBuffer.putShort((short) 2);
            this.send(sendBuffer);
            return;
          }
        } catch (SQLException e) {
          this.server.logException(e);
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 1);
          this.send(sendBuffer);
          return;
        }

        if (this.server.db.update(
            "INSERT INTO accounts (login, passwordHash, passwordSalt) VALUES (?, ?, ?)",
            login,
            passwordHash,
            "No salt."
        ) < 1) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 1);
          this.send(sendBuffer);
          return;
        }

        ByteBuffer sendBuffer = ByteBuffer.allocate(2);
        sendBuffer.putShort((short) 0);
        this.send(sendBuffer);
        this.server.logger.fine("New account has been created. Login: '" + login + "'");
      }
      case 1 -> {
        int loginLength = buffer.get();
        byte[] loginBytes = new byte[loginLength];

        if (loginBytes.length > 64) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 4);
          this.send(sendBuffer);
          return;
        }

        buffer.get(loginBytes);
        String login = new String(loginBytes, StandardCharsets.UTF_8);

        if (login.length() > 16) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 4);
          this.send(sendBuffer);
          return;
        }

        byte[] passwordHash = new byte[32];
        buffer.get(passwordHash);
        try (ResultSet specifiedUser = this.server.db.query(
            "SELECT 1 FROM accounts WHERE login = ? AND passwordHash = ? AND passwordSalt = ?",
            login, passwordHash, "No salt.")) {
          if (!specifiedUser.next()) {
            ByteBuffer sendBuffer = ByteBuffer.allocate(2);
            sendBuffer.putShort((short) 5);
            this.send(sendBuffer);
            return;
          }

          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 6);
          this.send(sendBuffer);
          this.accountId = specifiedUser.getLong(1);
          this.server.logger.fine("Logged in user with id " + this.accountId);
        } catch (SQLException e) {
          this.server.logException(e);
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 1);
          this.send(sendBuffer);
        }
      }
      // FINER to prevent spamming from modified (or broken) client, slowing down the server
      default -> this.server.logger.finer("Unexpected operation code from client: " + code);
    }
  }

  /**
   * Loop, that receives message from client and calls {@link Client#handle(ByteBuffer)}.
   */
  public void receiveHandleLoop() {
    while (true) {
      if (!this.socket.isConnected()) {
        break;
      }

      try {
        this.receiveOnce();
      } catch (EOFException e) {
        this.server.logger.info("Client disconnected");
        break;
      }

      ByteBuffer buffer = this.receive();

      if (buffer == null) {
        continue;
      }

      this.handle(buffer);
    }
  }

  /**
   * Receives one packet from client.
   *
   * @throws EOFException Client disconnected (buffer closed).
   */
  public void receiveOnce() throws EOFException {
    try {
      int packetLength = this.inputStream.readInt();
      byte[] packet = new byte[packetLength];
      int readBytes = this.inputStream.read(packet, 0, packetLength);

      if (packetLength != readBytes) {
        this.server.logger.warning(
            String.format(Locale.ENGLISH, "Read not all bytes (%d from %d).", readBytes,
                packetLength));
        return;
      }

      this.packets.add(packet);
    } catch (EOFException e) {
      throw e;
    } catch (IOException e) {
      this.server.logException(e);
    }
  }

  /**
   * Sends message to client.
   *
   * @param bytes Data to be sent.
   */
  public void send(byte[] bytes) {
    try {
      byte[] encryptedBytes = Aes.encrypt(bytes);
      this.outputStream.writeInt(encryptedBytes.length);
      this.outputStream.write(encryptedBytes);
      this.outputStream.flush();
    } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException
             | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
             | InvalidKeyException e) {
      this.server.logException(e);
    }
  }

  /**
   * Sends message to client.
   * See also {@link Client#send(byte[])}.
   *
   * @param buffer Data to be sent.
   */
  public void send(ByteBuffer buffer) {
    this.send(buffer.array());
  }

  /**
   * Receives bytes from this client.
   *
   * @return Array of received bytes.
   */
  public byte[] receiveBytes() {
    byte[] packet = packets.poll();

    try {
      return Aes.decrypt(packet);
    } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException
             | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
      this.server.logException(e);
    }

    return null;
  }

  /**
   * Receives {@link ByteBuffer} from this client.
   *
   * @return Received {@code ByteBuffer}.
   */
  public ByteBuffer receive() {
    byte[] bytes = this.receiveBytes();

    if (bytes == null) {
      return null;
    }

    return ByteBuffer.wrap(bytes);
  }

  /**
   * Closes connection with client, letting client know about disconnect.
   */
  public void disconnect() {
    try {
      this.socket.close();
    } catch (IOException e) {
      this.server.logException(e);
    }
  }
}
