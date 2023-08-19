package com.werryxgames.messenger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
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

/**
 * Class, that is used as server launcher.
 */
public class Server {
  public static final String HOST = "0.0.0.0";
  public static final int PORT = 9451;
  public static final int MAX_PENDING_CONNECTIONS = 8;
  @SuppressWarnings("HardcodedFileSeparator")
  private static final String DB_URL = getenv("MESSENGER_DB_URL", "127.0.0.1/messenger");
  private static final String DB_USER = getenv("MESSENGER_DB_USER", "werryx");
  private static final String DB_PASSWORD = getenv("MESSENGER_DB_PASSWORD", "1234");
  private static final boolean DB_DEFAULT =
      Integer.parseInt(getenv("MESSENGER_DB_DEFAULT", "1")) == 1;

  public Logger logger;
  public ServerSocket socket;
  public Database db;
  protected ArrayList<Client> clients = new ArrayList<>(8);

  /**
   * Main entry point.
   *
   * @param args Unused.
   */
  public static void main(String[] args) throws NoSuchAlgorithmException {
    if (args.length == 2 && Objects.equals(args[0], "--generateKey")) {
      Aes.printKey(Aes.generateKey(Integer.parseInt(args[1])));
      return;
    }

    new Server().start(args);
  }

  public static String getenv(String envKey, String defaultValue) {
    if (System.getenv().containsKey(envKey)) {
      // Suppressed because it is server. On Android it's still possible to run server in Termux.
      //noinspection CallToSystemGetenv
      return System.getenv(envKey);
    }

    return defaultValue;
  }

  /**
   * Starts server.
   *
   * @param args Command line arguments.
   */
  public void start(String[] args) {
    this.logger = Logger.getLogger("Messenger Server");

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

    if (Server.DB_DEFAULT) {
      this.logger.warning(
          "Connecting to default database. Set environment variables to specify database "
              + "parameters.");
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
