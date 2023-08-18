package com.werryxgames.messenger;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

/**
 * Class for cryptographical operations using AES-CBC.
 *
 * @since 1.0
 */
public class Aes {
  @SuppressWarnings("HardcodedFileSeparator")
  public static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  // Don't forget to change it, if you will use custom server with custom client.
  public static final SecretKey DEFAULT_KEY =
      Aes.getKey("AB61498184100BBE904FC1B81C8CFD2A08B5F5226042AC117E9C84E6F86BF830");

  /**
   * Generates key for AES.
   *
   * @param keyLength Length of resulting key.
   * @return Generated key.
   * @throws NoSuchAlgorithmException Thrown if AES is not supported.
   */
  public static SecretKey generateKey(int keyLength) throws NoSuchAlgorithmException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(keyLength);
    return keyGenerator.generateKey();
  }

  /**
   * Returns key from byte array.
   *
   * @param keyBytes Array of key bytes.
   * @return AES Key.
   */
  public static SecretKey getKey(byte[] keyBytes) {
    return new SecretKeySpec(keyBytes, "AES");
  }

  /**
   * Returns key from hexadecimal string.
   *
   * @param keyHex Hexadecimal string of key.
   * @return AES key.
   */
  public static SecretKey getKey(String keyHex) {
    int length = keyHex.length();
    byte[] keyBytes = new byte[length / 2];

    for (int i = 0; i < length; i += 2) {
      keyBytes[i / 2] = (byte) ((Character.digit(keyHex.charAt(i), 16) << 4) + Character.digit(
          keyHex.charAt(i + 1), 16));
    }

    return Aes.getKey(keyBytes);
  }

  /**
   * Prints AES key, using {@link System#out}.{@link java.io.PrintStream#print(Object)}.
   *
   * @param key AES key.
   */
  public static boolean printKey(SecretKey key) {
    byte[] keyBytes = key.getEncoded();
    StringBuilder keyHexBuilder = new StringBuilder(keyBytes.length);

    for (byte b : keyBytes) {
      keyHexBuilder.append(String.format("%02X", b));
    }

    System.out.println(keyHexBuilder);

    try {
      key.destroy();
    } catch (DestroyFailedException ignored) {
      return false;
    }

    return true;
  }

  /**
   * Generates 16 bytes of Initialization Vector (IV).
   *
   * @return Generated IV.
   */
  public static IvParameterSpec generateIv() {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    return new IvParameterSpec(iv);
  }

  /**
   * Returns length of encrypted data, when data with {@code originalLength} will be encrypted.
   *
   * @param originalLength Length of original data.
   * @return Length of encrypted data.
   */
  public static int getEncryptedLength(int originalLength) {
    return originalLength + 16 - (originalLength % 16);
  }

  /**
   * Encrypts data.
   *
   * @param bytes Data to encrypt.
   * @param key   AES key, used to encrypt data.
   * @param iv    Randomly generated Initialization Vector.
   * @return Encrypted data.
   */
  public static byte[] encrypt(byte[] bytes, SecretKey key, IvParameterSpec iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance(Aes.ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    return cipher.doFinal(bytes);
  }

  /**
   * Encrypts data.
   * {@link Aes#encrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] encrypt(byte[] bytes, SecretKey key)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    ByteBuffer buffer = ByteBuffer.allocate(16 + Aes.getEncryptedLength(bytes.length));
    IvParameterSpec iv = Aes.generateIv();
    buffer.put(iv.getIV());
    buffer.put(Aes.encrypt(bytes, key, iv));
    return buffer.array();
  }

  /**
   * Encrypts data.
   * {@link Aes#encrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] encrypt(byte[] bytes)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    return Aes.encrypt(bytes, Aes.DEFAULT_KEY);
  }

  /**
   * Decrypts data.
   *
   * @param encryptedBytes Data to decrypt.
   * @param key            AES key, used to encrypt given data.
   * @param iv             Initialization Vector used to encrypt given data.
   * @return Original data.
   */
  public static byte[] decrypt(byte[] encryptedBytes, SecretKey key, IvParameterSpec iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance(Aes.ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    return cipher.doFinal(encryptedBytes);
  }

  /**
   * Decrypts data.
   * {@link Aes#decrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] decrypt(byte[] encryptedBytes)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    return Aes.decrypt(encryptedBytes, Aes.DEFAULT_KEY);
  }

  /**
   * Decrypts data.
   * {@link Aes#decrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] decrypt(byte[] encryptedBytes, SecretKey key)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    ByteBuffer buffer = ByteBuffer.wrap(encryptedBytes);
    byte[] ivBytes = new byte[16];
    buffer.get(ivBytes);
    IvParameterSpec iv = new IvParameterSpec(ivBytes);
    byte[] contentBytes = new byte[buffer.remaining()];
    buffer.get(contentBytes);
    return Aes.decrypt(contentBytes, key, iv);
  }
}
