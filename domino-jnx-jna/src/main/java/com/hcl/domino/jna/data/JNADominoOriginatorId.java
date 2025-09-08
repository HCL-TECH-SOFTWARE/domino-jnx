/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.jna.data;

import java.util.Formatter;
import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoOriginatorId;
import com.hcl.domino.richtext.structures.OriginatorID;

public class JNADominoOriginatorId implements DominoOriginatorId {
  private String unid;
  private int sequence;
  /** C type : TIMEDATE */
  private DominoDateTime sequenceTime;

  /** structure is lazily created */
  private OriginatorID m_struct;

  public JNADominoOriginatorId(OriginatorID oid) {
    this.m_struct = oid;
  }

  @Override
  public DominoDateTime getFile() {
    return new DefaultDominoDateTime(m_struct.getFile().getInnards());
  }

  @Override
  public DominoDateTime getNote() {
    return new DefaultDominoDateTime(m_struct.getNote().getInnards());
  }

  @Override
  public int getSequence() {
    if (m_struct != null) {
      this.sequence = m_struct.getSequence();
    }
    return this.sequence;
  }

  @Override
  public DominoDateTime getSequenceTime() {
    if (this.m_struct != null) {
      this.sequenceTime = new DefaultDominoDateTime(m_struct.getSequenceTime().getInnards());
    }
    return this.sequenceTime;
  }

  @Override
  public String getUNID() {
    if (this.unid == null && m_struct != null) {
      this.unid = m_struct.getUNID();
    }
    return this.unid;

  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(getUNID());

    int[] seqTime = getSequenceTime().getAdapter(int[].class);

    try (Formatter formatter = new Formatter()) {
      formatter.format("%08x", getSequence()); //$NON-NLS-1$
      formatter.format("%08x", seqTime[0]); //$NON-NLS-1$
      formatter.format("%08x", seqTime[1]); //$NON-NLS-1$
      result.append(formatter.toString().toUpperCase());
    }

    return result.toString();
  }
}
