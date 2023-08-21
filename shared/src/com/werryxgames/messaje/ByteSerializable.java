package com.werryxgames.messaje;

import java.nio.ByteBuffer;

public interface ByteSerializable {
  int byteSize();
  ByteBuffer toByteBuffer();
  byte[] toBytes();
  ByteSerializable fromBytes(ByteBuffer buffer);
  ByteSerializable fromBytes(byte[] bytes);
}
