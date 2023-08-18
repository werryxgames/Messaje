package com.werryxgames.messenger;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM
//  argument

/**
 * Class, that is used as launcher on desktop platforms.
 */
public class DesktopLauncher {
  /**
   * Main entry point.
   *
   * @param args Unused.
   */
  public static void main(String[] args) {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setForegroundFPS(60);
    config.setTitle("MessengerJava");
    new Lwjgl3Application(new MessengerJava(), config);
  }
}
