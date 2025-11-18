package com.hcl.domino.jnx.jep454.misc;

import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.misc.DominoClientBuilderFactory;

public class JEPDominoClientBuilderFactory implements DominoClientBuilderFactory {

  @Override
  public DominoClientBuilder get() {
    return new JEPDominoClientBuilder();
  }


}
