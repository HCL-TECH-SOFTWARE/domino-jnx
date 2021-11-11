package com.hcl.domino.jnx.vertx.json;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class ImportedJavaAgentContentMixIn {
  @JsonIgnore abstract List<String> getFilenames();
  @JsonIgnore abstract Optional<InputStream> getFile(String file);
}
