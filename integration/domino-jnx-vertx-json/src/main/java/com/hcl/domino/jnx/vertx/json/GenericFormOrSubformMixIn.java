package com.hcl.domino.jnx.vertx.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.SubformReference;
import com.hcl.domino.richtext.FormField;

/**
 * vertx Json Serializer Mixin for View Object in Designs
 *
 * @since 1.0.32
 */
public abstract class GenericFormOrSubformMixIn {
  
  @JsonIgnore abstract List<String> getExplicitSubformRecursive();
  @JsonIgnore abstract List<FormField> getFields();
  @JsonIgnore abstract List<SubformReference> getSubforms();
  @JsonSerialize(using = DocumentToUnidSerializer.class) abstract Document getDocument();
  
}