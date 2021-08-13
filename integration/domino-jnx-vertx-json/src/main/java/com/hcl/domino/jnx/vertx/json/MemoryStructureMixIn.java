package com.hcl.domino.jnx.vertx.json;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MemoryStructureMixIn {
  @JsonIgnore abstract ByteBuffer getData();
}
