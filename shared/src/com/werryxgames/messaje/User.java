package com.werryxgames.messaje;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Class, that represents user.
 *
 * @since 1.0
 */
public class User implements ByteSerializable {

  public long id;
  public String name;

  public User() {
  }

  public User(long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public int byteSize() {
    byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
    return 8 + 1 + nameBytes.length;
  }

  @Override
  public ByteBuffer toByteBuffer() {
    byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
    ByteBuffer buffer = ByteBuffer.allocate(8 + 1 + nameBytes.length);
    buffer.putLong(this.id);
    buffer.put((byte) nameBytes.length);
    buffer.put(nameBytes);
    return buffer;
  }

  @Override
  public byte[] toBytes() {
    return this.toByteBuffer().array();
  }

  @Override
  public User fromBytes(ByteBuffer buffer) {
    this.id = buffer.getLong();
    byte nameLength = buffer.get();
    byte[] nameBytes = new byte[nameLength];
    buffer.get(nameBytes);
    this.name = new String(nameBytes, StandardCharsets.UTF_8);
    return this;
  }

  @Override
  public User fromBytes(byte[] bytes) {
    return this.fromBytes(ByteBuffer.wrap(bytes));
  }
}
