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

import java.util.Collections;
import java.util.List;

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.commons.design.DesignUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.agent.DesignSimpleActionAgent;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.RecordType.Area;

/**
 * Implementation of {@link DesignSimpleActionAgent}
 */
public class DesignSimpleActionAgentImpl extends AbstractDesignAgentImpl<DesignSimpleActionAgent> implements DesignSimpleActionAgent {
  private final List<SimpleAction> actions;

  public DesignSimpleActionAgentImpl(Document doc) {
    super(doc);
    
    RichTextRecordList records = doc.getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION);
    this.actions = DesignUtil.toSimpleActions(records);
  }
  
  @Override
  public List<SimpleAction> getActions() {
    return Collections.unmodifiableList(this.actions);
  }

}
