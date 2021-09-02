package com.hcl.domino.commons.design.agent;

import java.nio.ByteBuffer;

import com.hcl.domino.design.DesignAgent.LastRunInfo;;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public enum AgentRunInfoDecoder {
  ;
  
  /**
   * Decodes the information found in an $AssistRunInfo item.
   * 
   * <p>The provided buffer is expected to start _after_ the data-type WORD if it
   * comes from an item.</p>
   * 
   * @param valueByteBuffer a {@link ByteBuffer} pointing to the run-info structures
   * @return a {@link LastRunInfo} instance for the data
   */
  public static LastRunInfo decodeAgentRunInfo(ByteBuffer valueByteBuffer) {
    return null;
  }

}
