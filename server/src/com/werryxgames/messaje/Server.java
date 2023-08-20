package com.werryxgames.messaje;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Class, that is used as server launcher.
 */
public class Server {
  public static final String HOST = Config.get("server.host", "0.0.0.0");
  public static final int PORT = Config.get("server.port", 9451);
  public static final int MAX_PENDING_CONNECTIONS = Config.get("server.maxPendingConnections", 8);
  @SuppressWarnings("HardcodedFileSeparator")
  private static final String DB_URL = Config.get("db.url", "127.0.0.1/messaje");
  private static final String DB_USER = Config.get("db.user", "werryx");
  private static final String DB_PASSWORD = Config.get("db.password", "1234");

  public Logger logger;
  public ServerSocket socket;
  public Database db;
  protected ArrayList<Client> clients = new ArrayList<>(8);

  /**
   * Main entry point.
   *
   * @param args Unused.
   */
  public static void main(String[] args)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException,
      IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
    if (args.length == 2 && Objects.equals(args[0], "--generateKey")) {
      Aes.printKey(Aes.generateKey(Integer.parseInt(args[1])));
      return;
    }

    if (args.length == 1 && Objects.equals(args[0], "--saveConfig")) {
      Server.saveConfig("config" + File.separator
              + "client.properties",
          "assets" + File.separator
              + "config.properties.enc");
      Server.saveConfig("config" + File.separator
              + "server.properties",
          "server" + File.separator + "data" + File.separator + "config.properties.enc");
      System.out.println("Configuration saved");
      return;
    }

    new Server().start(args);
  }

  /**
   * Encrypts original *.properties file and saves it to specified path.
   *
   * @param originalFilePath Path to original *.properties file.
   * @param encryptedFilePath Path to encrypted *.properties file.
   */
  public static void saveConfig(String originalFilePath, String encryptedFilePath)
      throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException,
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    SecretKey key = Aes.generateKey(256);
    byte[] keyBytes = Aes.customEncryptAlgorithm(key.getEncoded());

    try (FileInputStream inputStream = new FileInputStream(originalFilePath)) {
      byte[] encryptedBytes = Aes.encrypt(inputStream.readAllBytes(), key);
      byte keyLength = (byte) (keyBytes.length - 1);
      byte[] resultBytes =
          ByteBuffer.allocate(1 + keyBytes.length + encryptedBytes.length).put(keyLength)
              .put(keyBytes)
              .put(encryptedBytes)
              .array();

      try (FileOutputStream outputStream = new FileOutputStream(encryptedFilePath)) {
        outputStream.write(resultBytes);
      }
    }
  }

  /**
   * Starts server.
   *
   * @param args Command line arguments.
   */
  public void start(String[] args) {
    this.logger = Logger.getLogger("Messaje Server");

    // TODO: Find logging level from args.
    this.logger.setLevel(Level.ALL);

    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(this.logger.getLevel());
    this.logger.addHandler(consoleHandler);
    FileHandler fileHandler;

    try {
      fileHandler = new FileHandler("log" + new SimpleDateFormat("ddMMyyyyHHmmss",
          Locale.US).format(new Date()) + ".txt", true);
    } catch (IOException e) {
      this.logException(e);
      return;
    }

    fileHandler.setFormatter(new SimpleFormatter()); // To change .xml logging format to .txt
    fileHandler.setLevel(this.logger.getLevel());
    this.logger.addHandler(fileHandler);
    this.logger.setUseParentHandlers(false);

    try {
      this.socket = new ServerSocket(Server.PORT, Server.MAX_PENDING_CONNECTIONS,
          InetAddress.getByName(Server.HOST));
    } catch (IOException e) {
      this.logException(e);
      return;
    }

    if (!(Config.hasKey("db.url") && Config.hasKey("db.password"))) {
      this.logger.warning(
          "Connecting to default database. Set config values to specify database "
              + "parameters: Required 'db.url' and 'db.password' (also you can set 'db.user')");
    }

    try {
      this.db = new Database(this, Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD);
    } catch (SQLException e) {
      this.logException(e);
      return;
    }

    this.logger.info("Server started");
    this.acceptLoop();
  }

  /**
   * Enters infinite loop, accepting clients and creating {@link Client} for each of them.
   */
  public void acceptLoop() {
    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        //noinspection ObjectAllocationInLoop
        clients.add(new Client(this, this.socket.accept()));
      } catch (IOException e) {
        this.logException(e);
        continue;
      }

      this.logger.info("Client connected");
    }
  }

  public void logException(Exception e, Level level) {
    this.logger.log(level, e.getMessage(), e);
  }

  public void logException(Exception e) {
    this.logException(e, Level.SEVERE);
  }
}
