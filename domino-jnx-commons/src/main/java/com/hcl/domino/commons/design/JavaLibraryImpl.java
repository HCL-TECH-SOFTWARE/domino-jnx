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
package com.hcl.domino.commons.design;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.agent.DefaultJavaAgentContent;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.agent.JavaAgentContent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.records.CDActionJavaAgent;
import com.hcl.domino.richtext.records.RecordType.Area;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class JavaLibraryImpl extends AbstractScriptLibrary<JavaLibrary> implements JavaLibrary {

  public JavaLibraryImpl(final Document doc) {
    super(doc);
  }

  @Override
  public JavaAgentContent getScriptContent() {
    final CDActionJavaAgent action = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
        .stream()
        .filter(CDActionJavaAgent.class::isInstance)
        .map(CDActionJavaAgent.class::cast)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find Java action data"));
    return new DefaultJavaAgentContent(
        this,
        action.getClassName(),
        action.getCodePath(),
        Arrays.stream(action.getFileList().split("\\n")) //$NON-NLS-1$
            .filter(StringUtil::isNotEmpty)
            .collect(Collectors.toList()),
        Arrays.stream(action.getLibraryList().split("\\n")) //$NON-NLS-1$
            .filter(StringUtil::isNotEmpty)
            .collect(Collectors.toList()));
  }
}
