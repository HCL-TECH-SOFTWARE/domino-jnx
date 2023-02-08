package com.hcl.domino.jna.misc;

import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.jna.JNADominoClientBuilder;
import com.hcl.domino.misc.DominoClientBuilderFactory;

/**
 * JNA-based implementation of {@link DominoClientBuilderFactory} that
 * produces new instances of {@link JNADominoClientBuilder}.
 * 
 * @author Jesse Gallagher
 * @since 1.19.1
 */
public class JNADominoClientBuilderFactory implements DominoClientBuilderFactory {
  @Override
  public DominoClientBuilder get() {
    return new JNADominoClientBuilder();
  }

}
