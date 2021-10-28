package com.hcl.domino.jnx.vertx.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class ImportedJavaAgentContentMixIn {
  @JsonSerialize(using = ImportedJavaAgentContentFileSerializer.class) abstract List<String> getFiles();
  @JsonIgnore abstract List<String> getFile(String file);
}
