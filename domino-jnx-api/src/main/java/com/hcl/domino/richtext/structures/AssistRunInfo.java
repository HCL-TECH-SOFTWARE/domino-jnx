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
package com.hcl.domino.richtext.structures;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.38
 */
@StructureDefinition(
  name = "ODS_ASSISTRUNINFO",
  members = {
    @StructureMember(name = "LastRun", type = OpaqueTimeDate.class),
    @StructureMember(name = "dwProcessed", type = int.class, unsigned = true),
    @StructureMember(name = "AssistMod", type = OpaqueTimeDate.class),
    @StructureMember(name = "DbID", type = OpaqueTimeDate.class),
    @StructureMember(name = "dwExitCode", type = int.class),
    @StructureMember(name = "dwSpare", type = int[].class, length = 4)
  }
)
public interface AssistRunInfo extends ResizableMemoryStructure {
  @StructureGetter("LastRun")
  DominoDateTime getLastRun();
  
  @StructureSetter("LastRun")
  AssistRunInfo setLastRun(DominoDateTime time);
  
  @StructureGetter("dwProcessed")
  long getProcessed();
  
  @StructureSetter("dwProcessed")
  AssistRunInfo setProcessed(long processed);
  
  @StructureGetter("AssistMod")
  DominoDateTime getAssistModified();
  
  @StructureSetter("AssistMod")
  AssistRunInfo setAssistModified(DominoDateTime time);
  
  @StructureGetter("DbID")
  DominoDateTime getDbId();
  
  @StructureSetter("DbID")
  AssistRunInfo setDbId(DominoDateTime time);
  
  @StructureGetter("dwExitCode")
  int getExitCode();
  
  @StructureSetter("dwExitCode")
  AssistRunInfo setExitCode(int code);
}
