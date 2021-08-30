package com.hcl.domino.commons.structures;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.function.BiFunction;

class StructMember {
  private final String name;
  final int offset;
  final Class<?> type;
  private final boolean unsigned;
  final int length;
  final BiFunction<ByteBuffer, Integer, Object> reader;
  final TriConsumer<ByteBuffer, Integer, Object> writer;

  public StructMember(final String name, final int offset, final Class<?> clazz, final boolean unsigned, final boolean bitfield,
      final int length) {
    this.name = name;
    this.offset = offset;
    this.type = clazz;
    this.unsigned = unsigned;
    this.length = length;
    this.reader = MemoryStructureProxy.reader(clazz, unsigned, bitfield, length);
    this.writer = MemoryStructureProxy.writer(clazz, unsigned, bitfield, length);
  }

  @Override
  public String toString() {
    return MessageFormat.format("StructMember [name={0}, offset={1}, type={2}, unsigned={3}, length={4}]", this.name, this.offset, //$NON-NLS-1$
        this.type, this.unsigned, this.length);
  }
}
