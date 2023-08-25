package com.werryxgames.messaje;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
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
        int loginMbLength = login.length();

        if (loginMbLength > 16 || loginMbLength < 3) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 3);
          this.send(sendBuffer);
          return;
        }

        byte[] passwordHash = new byte[32];
        buffer.get(passwordHash);
        byte[] salt = new byte[8];
        // Salt should be unique, not cryptographically secure
        //noinspection UnsecureRandomNumberGeneration
        new Random().nextBytes(salt);
        byte[] saltedPassword;

        try {
          saltedPassword = MessageDigest.getInstance("SHA3-256").digest(
              ByteBuffer.allocate(passwordHash.length + salt.length).put(passwordHash).put(salt)
                  .array());
        } catch (NoSuchAlgorithmException e) {
          this.send(ByteBuffer.allocate(2).putShort((short) 1));
          this.server.logException(e);
          return;
        }

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
            saltedPassword,
            salt
        ) < 1) {
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 1);
          this.send(sendBuffer);
          return;
        }

        try (ResultSet accountId = this.server.db.query("SELECT id FROM accounts WHERE login = ?",
            login)) {
          accountId.next();
          this.accountId = accountId.getLong(1);
        } catch (SQLException e) {
          this.server.logException(e);
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

        try (ResultSet specifiedUser = this.server.db.query(
            "SELECT id, passwordHash, passwordSalt FROM accounts WHERE login = ?",
            login)) {
          if (!specifiedUser.next()) {
            ByteBuffer sendBuffer = ByteBuffer.allocate(2);
            sendBuffer.putShort((short) 5);
            this.send(sendBuffer);
            return;
          }

          byte[] passwordHash = new byte[32];

          buffer.get(passwordHash);
          byte[] correctHash = specifiedUser.getBytes(2);
          byte[] passwordSalt = specifiedUser.getBytes(3);
          byte[] computedHash = MessageDigest.getInstance("SHA3-256").digest(
              ByteBuffer.allocate(passwordHash.length + passwordSalt.length).put(passwordHash)
                  .put(passwordSalt)
                  .array());

          if (!Arrays.equals(computedHash, correctHash)) {
            this.server.logger.fine("Incorrect password");
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
        } catch (SQLException | NoSuchAlgorithmException e) {
          this.server.logException(e);
          ByteBuffer sendBuffer = ByteBuffer.allocate(2);
          sendBuffer.putShort((short) 1);
          this.send(sendBuffer);
        }
      }
      case 2 -> {
        if (this.accountId == 0) {
          this.server.logger.finer("Received unauthorized request: 2");
          return;
        }

        ArrayList<Message> messages = new ArrayList<>(64);
        ArrayList<User> users = new ArrayList<>(16);
        ArrayList<Long> userIds = new ArrayList<>(16);

        try (ResultSet sentMessages = this.server.db.query(
            "SELECT * FROM privateMessages WHERE sender = ?", this.accountId)) {
          while (sentMessages.next()) {
            long messageId = sentMessages.getLong(1);
            long senderId = sentMessages.getLong(2);
            long receiverId = sentMessages.getLong(3);

            if (!userIds.contains(receiverId)) {
              String name = "<unnamed>";

              try (ResultSet userName = this.server.db.query(
                  "SELECT login FROM accounts WHERE id = ?", receiverId)) {
                if (userName.next()) {
                  name = userName.getString(1);
                }
              }

              users.add(new User(receiverId, name));
              userIds.add(receiverId);
            }

            InputStream textStream = sentMessages.getBinaryStream(4);
            int available = textStream.available();
            String text;

            if (available > 0) {
              //noinspection ObjectAllocationInLoop
              byte[] textBytes = new byte[textStream.available()];
              int attempt = 0;
              int totalReadBytes = 0;

              while (attempt < 16) {
                totalReadBytes += textStream.read(textBytes, totalReadBytes,
                    textBytes.length - totalReadBytes);
                attempt++;

                if (totalReadBytes >= textBytes.length) {
                  break;
                }
              }

              if (totalReadBytes != textBytes.length) {
                throw new IllegalArgumentException("read bytes != textBytes.length");
              }

              //noinspection ObjectAllocationInLoop
              text = new String(textBytes, StandardCharsets.UTF_8);
            } else {
              text = "";
            }

            //noinspection ObjectAllocationInLoop
            messages.add(new Message(messageId, receiverId, true, text));
          }
        } catch (SQLException | IOException e) {
          this.server.logException(e);
        }

        try (ResultSet receivedMessages = this.server.db.query(
            "SELECT * FROM privateMessages WHERE receiver = ?", this.accountId)) {
          while (receivedMessages.next()) {
            long messageId = receivedMessages.getLong(1);
            long senderId = receivedMessages.getLong(2);

            if (!userIds.contains(senderId)) {
              String name = "<unnamed>";

              try (ResultSet userName = this.server.db.query(
                  "SELECT login FROM accounts WHERE id = ?", senderId)) {
                if (userName.next()) {
                  name = userName.getString(1);
                }
              }

              users.add(new User(senderId, name));
              userIds.add(senderId);
            }
            long receiverId = receivedMessages.getLong(3);
            InputStream textStream = receivedMessages.getBinaryStream(4);
            //noinspection ObjectAllocationInLoop
            byte[] textBytes = new byte[textStream.available()];

            if (textStream.read(textBytes) != textBytes.length) {
              throw new IllegalArgumentException("read bytes != textBytes.length");
            }

            //noinspection ObjectAllocationInLoop
            String text = new String(textBytes, StandardCharsets.UTF_8);
            //noinspection ObjectAllocationInLoop
            messages.add(new Message(messageId, receiverId, false, text));
            this.server.logger.fine(
                "%d, %d, %d, %s".formatted(messageId, senderId, receiverId, text));
          }
        } catch (SQLException | IOException e) {
          this.server.logException(e);
        }

        int usersSize = 0;

        for (User user : users) {
          usersSize += user.byteSize();
        }

        int messagesSize = 0;

        for (Message message : messages) {
          messagesSize += message.byteSize();
        }

        ByteBuffer sendBuffer = ByteBuffer.allocate(2 + 4 + usersSize + 4 + messagesSize);
        sendBuffer.putShort((short) 7);
        sendBuffer.putInt(users.size());

        for (User user : users) {
          sendBuffer.put(user.toBytes());
        }

        sendBuffer.putInt(messages.size());

        for (Message message : messages) {
          sendBuffer.put(message.toBytes());
        }

        this.send(sendBuffer);
      }
      case 3 -> {
        long contactId = buffer.getLong();
        short messageLength = buffer.getShort();
        byte[] messageBytes = new byte[messageLength];
        buffer.get(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        if (this.server.db.update(
            "INSERT INTO privateMessages (sender, receiver, text) VALUES (?, ?, ?)", this.accountId,
            contactId, message) < 1) {
          this.server.logger.warning("Message not delivered");
        }
      }
      case 4 -> {
        byte loginLength = buffer.get();
        byte[] loginBytes = new byte[loginLength];
        buffer.get(loginBytes);
        String login = new String(loginBytes, StandardCharsets.UTF_8);

        try (ResultSet result = this.server.db.query("SELECT id FROM accounts WHERE login = ?",
            login)) {
          if (result.next()) {
            this.send(ByteBuffer.allocate(2 + 8).putShort((short) 8).putLong(result.getLong(1)));
          } else {
            this.send(ByteBuffer.allocate(2).putShort((short) 9));
          }
        } catch (SQLException e) {
          this.server.logException(e);
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
   * Sends message to client. See also {@link Client#send(byte[])}.
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
