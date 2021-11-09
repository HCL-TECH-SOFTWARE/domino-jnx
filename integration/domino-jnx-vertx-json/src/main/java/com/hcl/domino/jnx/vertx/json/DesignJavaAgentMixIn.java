package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.databind.annotation.JsonAppend;

@JsonAppend(
    attrs = {
        @JsonAppend.Attr(value = "agentLanguage")
    }
)
public abstract class DesignJavaAgentMixIn {

  
}
