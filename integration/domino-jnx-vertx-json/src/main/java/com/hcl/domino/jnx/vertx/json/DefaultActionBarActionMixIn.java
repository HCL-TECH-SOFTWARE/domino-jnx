package com.hcl.domino.jnx.vertx.json;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcl.domino.richtext.records.CDResource;

public abstract class DefaultActionBarActionMixIn {
  
  @JsonIgnore abstract Optional<CDResource> getIconResource();
  
}
