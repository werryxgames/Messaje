package com.werryxgames.messaje;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
import javax.crypto.spec.GCMParameterSpec;
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
  public static final String ALGORITHM = "AES/GCM/NoPadding";
  public static final int IV_SIZE = 12;
  public static final int TAG_LENGTH = 128;
  protected static SecretKey key = null;

  /**
   * SHOULD BE REPLACED WITH SOMETHING MORE SECURE!
   * Custom algorithm for encrypting properties.
   * Must return non-empty byte array from non-empty data.
   * <br>
   * Note: Consider using {@link Aes#encrypt(byte[], SecretKey)} here.
   *
   * @param keyBytes Original bytes.
   * @return Encrypted bytes.
   */
  static byte[] customEncryptAlgorithm(byte[] keyBytes) {
    int keyLength = keyBytes.length;
    byte[] newBytes = new byte[keyLength];

    for (int i = 0; i < keyLength; i++) {
      newBytes[i] = (byte) (keyBytes[keyLength - i - 1] - 55);
    }

    return newBytes;
  }

  /**
   * SHOULD BE REPLACED WITH SOMETHING MORE SECURE!
   * Custom algorithm for decrypting properties, encrypted with
   * {@link Aes#customEncryptAlgorithm(byte[])}.
   *
   * @param encryptedBytes Encrypted bytes.
   * @return Original bytes.
   */
  static byte[] customDecryptAlgorithm(byte[] encryptedBytes) {
    int encryptedLength = encryptedBytes.length;
    byte[] originalBytes = new byte[encryptedLength];

    for (int i = 0; i < encryptedLength; i++) {
      originalBytes[i] = (byte) (encryptedBytes[encryptedLength - i - 1] + 55);
    }

    return originalBytes;
  }

  static String decryptProperties(byte[] encryptedBytes)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    ByteBuffer buffer = ByteBuffer.wrap(encryptedBytes);
    short keyLength = (short) ((buffer.get() + 1) & 0xFF);
    byte[] encryptedKeyBytes = new byte[keyLength];
    buffer.get(encryptedKeyBytes);
    byte[] keyBytes = Aes.customDecryptAlgorithm(encryptedKeyBytes);
    byte[] encryptedData = new byte[encryptedBytes.length - keyLength - 1];
    buffer.get(encryptedData);
    SecretKey key = new SecretKeySpec(keyBytes, "AES");
    byte[] decryptedData = Aes.decrypt(encryptedData, key);
    return new String(decryptedData, StandardCharsets.UTF_8);
  }

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
   * Returns key, specified as {@code aes.key} property, or it's default value.
   *
   * @return Key.
   */
  public static SecretKey getKey() {
    if (Aes.key == null) {
      Aes.key = Aes.getKey(
          Config.get("aes.key",
              "AB61498184100BBE904FC1B81C8CFD2A08B5F5226042AC117E9C84E6F86BF830"));
    }

    return Aes.key;
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
    return Aes.getKey(Utils.hexToBytes(keyHex));
  }

  /**
   * Prints array of bytes, using {@link System#out}.{@link java.io.PrintStream#println(Object)}.
   *
   * @param bytes Byte array, that will be printed.
   */
  public static void printBytes(byte[] bytes) {
    StringBuilder hexBuilder = new StringBuilder(bytes.length);

    for (byte b : bytes) {
      hexBuilder.append(String.format("%02X", b));
    }

    System.out.println(hexBuilder);
  }

  /**
   * Prints AES key, using {@link Aes#printBytes(byte[])}.
   * After that, destroys key.
   *
   * @param key AES key.
   */
  public static boolean printKey(SecretKey key) {
    Aes.printBytes(key.getEncoded());

    try {
      key.destroy();
    } catch (DestroyFailedException ignored) {
      return false;
    }

    return true;
  }

  public static void generateBytes(byte[] arr) {
    new SecureRandom().nextBytes(arr);
  }

  /**
   * Generates 16 bytes of Initialization Vector (IV).
   *
   * @return Generated IV.
   */
  public static IvParameterSpec generateIv() {
    byte[] iv = new byte[Aes.IV_SIZE];
    Aes.generateBytes(iv);
    return new IvParameterSpec(iv);
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
    GCMParameterSpec parameterSpec = new GCMParameterSpec(Aes.TAG_LENGTH, iv.getIV());
    cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
    return cipher.doFinal(bytes);
  }

  /**
   * Encrypts data.
   * {@link Aes#encrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] encrypt(byte[] bytes, SecretKey key)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    IvParameterSpec iv = Aes.generateIv();
    byte[] ivBytes = iv.getIV();
    byte[] encryptedBytes = Aes.encrypt(bytes, key, iv);
    ByteBuffer buffer = ByteBuffer.allocate(ivBytes.length + encryptedBytes.length);
    buffer.put(ivBytes);
    buffer.put(encryptedBytes);
    return buffer.array();
  }

  /**
   * Encrypts data.
   * {@link Aes#encrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] encrypt(byte[] bytes)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    return Aes.encrypt(bytes, Aes.getKey());
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
    GCMParameterSpec parameterSpec = new GCMParameterSpec(Aes.TAG_LENGTH, iv.getIV());
    cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
    return cipher.doFinal(encryptedBytes);
  }

  /**
   * Decrypts data.
   * {@link Aes#decrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] decrypt(byte[] encryptedBytes, SecretKey key)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    ByteBuffer buffer = ByteBuffer.wrap(encryptedBytes);
    byte[] ivBytes = new byte[Aes.IV_SIZE];
    buffer.get(ivBytes);
    IvParameterSpec iv = new IvParameterSpec(ivBytes);
    byte[] contentBytes = new byte[buffer.remaining()];
    buffer.get(contentBytes);
    return Aes.decrypt(contentBytes, key, iv);
  }

  /**
   * Decrypts data.
   * {@link Aes#decrypt(byte[], SecretKey, IvParameterSpec)}
   */
  public static byte[] decrypt(byte[] encryptedBytes)
      throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
      NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    return Aes.decrypt(encryptedBytes, Aes.getKey());
  }
}
