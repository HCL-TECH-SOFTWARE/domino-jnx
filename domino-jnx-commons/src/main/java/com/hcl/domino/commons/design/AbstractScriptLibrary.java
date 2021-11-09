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

import java.util.EnumSet;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.ScriptLibrary;
import com.hcl.domino.misc.NotesConstants;

public class AbstractScriptLibrary<T extends ScriptLibrary> extends AbstractDesignElement<T> implements ScriptLibrary,
  IDefaultNamedDesignElement {

  public AbstractScriptLibrary(final Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    Document doc = getDocument();
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    doc.replaceItemValue(NotesConstants.FIELD_PUBLICACCESS, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), "1"); //$NON-NLS-1$
  }

}
