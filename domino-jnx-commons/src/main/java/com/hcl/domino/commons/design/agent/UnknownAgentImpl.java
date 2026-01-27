package com.hcl.domino.commons.design.agent;

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignAgent;

/**
 * Represents an agent with an unknown language code.
 * 
 * @since 1.50.0
 */
public class UnknownAgentImpl extends AbstractDesignAgentImpl<DesignAgent> {

  public UnknownAgentImpl(Document doc) {
    super(doc);
  }

}
