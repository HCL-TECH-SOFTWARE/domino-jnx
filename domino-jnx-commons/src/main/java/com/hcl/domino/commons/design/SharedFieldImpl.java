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
package com.hcl.domino.commons.design;

import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.SharedField;
import com.hcl.domino.misc.NotesConstants;

public class SharedFieldImpl extends AbstractDesignElement<SharedField> implements SharedField, IDefaultNamedDesignElement {

  public SharedFieldImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {

  }

  @Override
  public List<?> getFieldBody() {
    return DesignUtil.encapsulateRichTextBody(getDocument(), NotesConstants.ITEM_NAME_TEMPLATE);
  }

  @Override
  public String getLotusScript() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem("$$" + getTitle(), (item, loop) -> { //$NON-NLS-1$
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
}
