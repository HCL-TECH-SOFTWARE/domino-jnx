/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.commons.design.agent;

import java.nio.charset.Charset;
import java.util.List;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.DesignAgent.LastRunInfo;
import com.hcl.domino.richtext.structures.AssistRunInfo;
import com.hcl.domino.richtext.structures.AssistRunObjectEntry;
import com.hcl.domino.richtext.structures.AssistRunObjectHeader;

public class DefaultAgentLastRunInfo implements LastRunInfo {
  @SuppressWarnings("unused")
  private final AssistRunObjectHeader header;
  @SuppressWarnings("unused")
  private final List<AssistRunObjectEntry> entries;
  private final AssistRunInfo info;
  private final List<byte[]> varData;

  public DefaultAgentLastRunInfo(AssistRunObjectHeader header, List<AssistRunObjectEntry> entries, AssistRunInfo info, List<byte[]> varData) {
    this.header = header;
    this.entries = entries;
    this.info = info;
    this.varData = varData;
  }

  @Override
  public DominoDateTime getTime() {
    return info.getLastRun();
  }

  @Override
  public long getDocumentCount() {
    return info.getProcessed();
  }

  @Override
  public DominoDateTime getVersion() {
    return info.getAssistModified();
  }

  @Override
  public int[] getDbId() {
    return info.getDbId().getAdapter(int[].class);
  }

  @Override
  public long getExitCode() {
    return info.getExitCode();
  }

  @Override
  public String getLog() {
    // This will be the variable data from the third entry, which is guaranteed to be present
    byte[] data = varData.get(2);
    String result = new String(data, Charset.forName("LMBCS")); //$NON-NLS-1$
    
    // It _appears_ that the log actually ends with this sequence, and is followed by errant data.
    // For now, at least, we'll assume this is expected
    int endIndex = result.lastIndexOf("\r\n\n\r\n"); //$NON-NLS-1$
    if(endIndex > -1) {
      result = result.substring(0, endIndex);
    }
    
    return result;
  }
}
