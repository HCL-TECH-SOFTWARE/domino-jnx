package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;

/**
 * vertx Json Serializer Mixin for View Object in Designs
 *
 * @since 1.0.32
 */
public abstract class ViewMixIn {
  
  @JsonIgnore abstract DominoCollection getCollection();
  @JsonSerialize(using = DocumentToUnidSerializer.class) abstract Document getDocument();
  
}