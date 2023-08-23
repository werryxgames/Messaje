package com.werryxgames.messaje;

import java.nio.ByteBuffer;

/**
 * Interface, that represents object, that can be serialized to, or deserialized from bytes.
 *
 * @since 1.0
 */
public interface ByteSerializable {
  int byteSize();

  ByteBuffer toByteBuffer();

  byte[] toBytes();

  ByteSerializable fromBytes(ByteBuffer buffer);

  ByteSerializable fromBytes(byte[] bytes);
}
