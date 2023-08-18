package com.werryxgames.messenger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
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

  /**
   * Constructor for {@link Client}.
   *
   * @param server Instance of {@link Server}.
   * @param clientSocket Socket of connected client.
   */
  public Client(Server server, Socket clientSocket) {
    this.server = server;
    this.socket = clientSocket;

    try {
      this.inputStream = new DataInputStream(clientSocket.getInputStream());
      this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
    } catch (IOException e) {
      this.server.logger.log(Level.SEVERE, e.getMessage(), e);
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
    ByteBuffer sendBuffer = ByteBuffer.allocate(2);
    sendBuffer.putShort(code);
    this.send(sendBuffer);
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
                packetLength)
        );
        return;
      }

      this.packets.add(packet);
    } catch (EOFException e) {
      throw e;
    } catch (IOException e) {
      this.server.logger.log(Level.SEVERE, e.getMessage(), e);
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
      this.server.logger.log(Level.SEVERE, e.getMessage(), e);
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
      this.server.logger.log(Level.SEVERE, e.getMessage(), e);
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
      this.server.logger.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
