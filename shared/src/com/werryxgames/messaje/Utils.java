package com.werryxgames.messaje;

/**
 * Utilities, that used in client and server.
 *
 * @since 1.0
 */
public class Utils {

  /**
   * Converts hex string to byte array.
   *
   * @param hex String, that will be converted.
   * @return Byte array from hex string, specified earlier.
   */
  public static byte[] hexToBytes(String hex) {
    int length = hex.length();
    byte[] hexBytes = new byte[length / 2];

    for (int i = 0; i < length; i += 2) {
      hexBytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(
          hex.charAt(i + 1), 16));
    }

    return hexBytes;
  }
}
