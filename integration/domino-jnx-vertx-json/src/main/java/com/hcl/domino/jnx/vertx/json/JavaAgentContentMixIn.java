package com.hcl.domino.jnx.vertx.json;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class JavaAgentContentMixIn {
  @JsonSerialize(using = OptInputStreamToBase64Serializer.class) abstract Optional<InputStream> getObjectAttachment();
  
  @JsonSerialize(using = OptInputStreamToBase64Serializer.class) abstract Optional<InputStream> getResourcesAttachment();

  @JsonSerialize(using = OptInputStreamToBase64Serializer.class) abstract Optional<InputStream> getSourceAttachment();
  
  @JsonSerialize(using = JavaAgentContentEmbeddedJarsSerializer.class) abstract List<String> getEmbeddedJars();
}
