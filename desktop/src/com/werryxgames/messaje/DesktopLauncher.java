package com.werryxgames.messaje;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import java.io.File;

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
    config.setForegroundFPS(30); // Not 60 because it is application, not game
    config.setTitle("Messaje");
    config.setWindowSizeLimits(400, 300, -1, -1);
    config.setWindowIcon(
        Files.FileType.Internal,
        "icons" + File.separator + "icon16.png",
        "icons" + File.separator + "icon32.png",
        "icons" + File.separator + "icon48.png",
        "icons" + File.separator + "icon128.png"
    );
    new Lwjgl3Application(new Messaje(), config);
  }
}
