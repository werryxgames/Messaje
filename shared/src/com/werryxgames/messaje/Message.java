package com.werryxgames.messaje;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message implements ByteSerializable {
  public long id;
  public long contactId;
  public boolean sentByMe;
  public String text;

  public Message() {
  }

  public Message(long id, long contactId, boolean sentByMe, String text) {
    this.id = id;
    this.contactId = contactId;
    this.sentByMe = sentByMe;
    this.text = text;
  }

  @Override
  public int byteSize() {
    byte[] textBytes = this.text.getBytes(StandardCharsets.UTF_8);
    return 8 + 8 + 1 + 2 + textBytes.length;
  }

  @Override
  public ByteBuffer toByteBuffer() {
    byte[] textBytes = this.text.getBytes(StandardCharsets.UTF_8);
    ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 1 + 2 + textBytes.length);
    buffer.putLong(this.id);
    buffer.putLong(this.contactId);
    buffer.put((byte) (this.sentByMe ? 1 : 0));
    buffer.putShort((short) textBytes.length);
    buffer.put(textBytes);
    return buffer;
  }

  @Override
  public byte[] toBytes() {
    return this.toByteBuffer().array();
  }

  @Override
  public Message fromBytes(ByteBuffer buffer) {
    this.id = buffer.getLong();
    this.contactId = buffer.getLong();
    this.sentByMe = buffer.get() != 0;
    short textLength = buffer.getShort();
    byte[] textBytes = new byte[textLength];
    buffer.get(textBytes);
    this.text = new String(textBytes, StandardCharsets.UTF_8);
    return this;
  }

  @Override
  public Message fromBytes(byte[] bytes) {
    return this.fromBytes(ByteBuffer.wrap(bytes));
  }
}
