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

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.JavaAgentAndLibrarySupport;
import com.hcl.domino.design.agent.DesignJavaAgent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Implementation of {@link DesignJavaAgent}
 */
public class DesignJavaAgentImpl extends AbstractDesignAgentImpl<DesignJavaAgent> implements DesignJavaAgent, JavaAgentAndLibrarySupport {

  public DesignJavaAgentImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    super.initializeNewDesignNote();
    
    this.getDocument().replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, Short.toUnsignedInt(RichTextConstants.SIG_ACTION_JAVA));
  }

}
