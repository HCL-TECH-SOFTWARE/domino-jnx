package com.hcl.domino.jnx.vertx.json;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcl.domino.design.format.ActionBarBackgroundRepeat;
import com.hcl.domino.richtext.records.CDResource;

public abstract class DefaultActionBarMixIn {
  
 @JsonIgnore abstract ActionBarBackgroundRepeat getBackgroundImageRepeatMode();
 @JsonIgnore abstract Optional<CDResource> getBackgroundImage();
 @JsonIgnore abstract Optional<CDResource> getButtonBackgroundImage();
 
}
