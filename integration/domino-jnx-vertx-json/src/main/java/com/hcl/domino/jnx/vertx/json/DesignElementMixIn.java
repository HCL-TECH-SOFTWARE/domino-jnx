package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hcl.domino.data.Document;

public abstract class DesignElementMixIn {
  @JsonSerialize(using = DocumentToUnidSerializer.class) abstract Document getDocument();
}
