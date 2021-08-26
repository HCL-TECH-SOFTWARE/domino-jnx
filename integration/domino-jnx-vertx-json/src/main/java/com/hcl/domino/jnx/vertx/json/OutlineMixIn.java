package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;

public abstract class OutlineMixIn {
  @JsonIgnore abstract DominoCollection getCollection();
  @JsonSerialize(using = DocumentToUnidSerializer.class) abstract Document getDocument();
}
