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
package com.hcl.domino.commons.design.outline;

import java.util.ArrayList;
import java.util.List;
import com.hcl.domino.data.IAdaptable;

public class DominoOutlineFormat implements IAdaptable {
  
  private final List<DominoOutlineEntry> outlineEntries = new ArrayList<>();

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    return null;
  }

  public List<DominoOutlineEntry> getOutlineEntries() {
    return this.outlineEntries;
  }
  
  public void addOutlineEntry(DominoOutlineEntry outlineEntry) {
    this.outlineEntries.add(outlineEntry);
  }
}
