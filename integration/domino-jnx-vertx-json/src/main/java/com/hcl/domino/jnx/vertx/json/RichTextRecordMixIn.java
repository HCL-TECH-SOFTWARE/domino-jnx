package com.hcl.domino.jnx.vertx.json;

import java.nio.ByteBuffer;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcl.domino.richtext.records.RecordType;

public abstract class RichTextRecordMixIn {
  @JsonIgnore abstract int getCDRecordLength();
  @JsonIgnore abstract ByteBuffer getDataWithoutHeader();
  @JsonIgnore abstract int getPayloadLength();
  @JsonIgnore abstract int getRecordHeaderLength();
  abstract Set<RecordType> getType();
  @JsonIgnore abstract short getTypeValue();
}
