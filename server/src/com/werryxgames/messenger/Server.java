package com.werryxgames.messenger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
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

  public Logger logger;
  public ServerSocket socket;
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
      this.logger.log(Level.SEVERE, e.getMessage(), e);
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
      this.logger.log(Level.SEVERE, e.getMessage(), e);
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
        this.logger.log(Level.SEVERE, e.getMessage(), e);
        continue;
      }

      this.logger.info("Client connected");
    }
  }
}
