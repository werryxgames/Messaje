package com.werryxgames.messaje;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class for getting and setting configuration in *.properties file.
 *
 * @since 1.0
 */
public class Config {

  public static final String DEFAULT_PROPERTIES_PATH = "config.properties.enc";
  protected static Properties configProperties = null;

  private static void loadProperties(InputStream inputStream)
      throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException,
      IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
      InvalidKeyException, IllegalArgumentException {
    Config.configProperties = new Properties();

    if (inputStream == null) {
      throw new IllegalArgumentException("inputStream == null");
    }

    byte[] fileBytes = new byte[inputStream.available()];
    int readBytes = 0;
    int reads = 0;

    while (inputStream.available() > 0) {
      readBytes += inputStream.read(fileBytes, readBytes, fileBytes.length - readBytes);

      if (++reads >= 128) {
        throw new RuntimeException("Can't read bytes from properties file in 128 iterations");
      }
    }

    String properties = Aes.decryptProperties(fileBytes);
    Config.configProperties.load(new StringReader(properties));
  }

  /**
   * Loads properties from encrypted file.
   *
   * @param file Path to file.
   */
  public static void loadProperties(String file) {
    try (InputStream inputStream = Config.class.getClassLoader()
        .getResourceAsStream(file)) {
      assert inputStream != null;
      Config.loadProperties(inputStream);
    } catch (IOException | NullPointerException | InvalidAlgorithmParameterException
             | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException
             | BadPaddingException | InvalidKeyException | IllegalArgumentException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads properties from encrypted file.
   *
   * @param file   Path to file.
   * @param logger Logger to log errors.
   */
  public static void loadProperties(String file, Logger logger) {
    try (ByteArrayInputStream inputStream = (ByteArrayInputStream) Config.class.getClassLoader()
        .getResourceAsStream(file)) {
      assert inputStream != null;
      Config.loadProperties(inputStream);
    } catch (IOException | NullPointerException | InvalidAlgorithmParameterException
             | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException
             | BadPaddingException | InvalidKeyException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Loads properties, if not loaded yet, then checks are properties loaded and contains key.
   *
   * @param key Key to check.
   * @return {@code true} if properties are loaded and they contains key, {@code else} otherwise.
   */
  public static boolean hasKey(String key) {
    if (Config.configProperties == null) {
      Config.loadProperties(Config.DEFAULT_PROPERTIES_PATH);
    }

    return Config.configProperties != null && Config.configProperties.containsKey(key);
  }

  /**
   * Returns {@link String} from properties file by {@code configKey}, or {@code defaultValue} if
   * not found.
   *
   * @param configKey    Key in properties file.
   * @param defaultValue Default string value.
   * @return Property value or {@code defaultValue}.
   */
  public static String get(String configKey, String defaultValue) {
    if (!Config.hasKey(configKey)) {
      return defaultValue;
    }

    return Config.configProperties.getProperty(configKey);
  }

  /**
   * See {@link Config#get(String, String)}.
   *
   * @param configKey    Key in properties file.
   * @param defaultValue Default integer value.
   * @return Property value or {@code defaultValue}.
   */
  public static int get(String configKey, int defaultValue) {
    if (!Config.hasKey(configKey)) {
      return defaultValue;
    }

    return Integer.parseInt(Config.configProperties.getProperty(configKey));
  }

  /**
   * See {@link Config#get(String, String)}.
   *
   * @param configKey    Key in properties file.
   * @param defaultValue Default boolean value.
   * @return Property value or {@code defaultValue}.
   */
  public static boolean get(String configKey, boolean defaultValue) {
    if (!Config.hasKey(configKey)) {
      return defaultValue;
    }

    return Boolean.parseBoolean(Config.configProperties.getProperty(configKey));
  }
}
