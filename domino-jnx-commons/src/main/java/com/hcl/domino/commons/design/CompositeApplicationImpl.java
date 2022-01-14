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

import com.hcl.domino.data.Document;
import com.hcl.domino.design.CompositeApplication;
import com.hcl.domino.misc.NotesConstants;

public class CompositeApplicationImpl extends AbstractNamedFileElement<CompositeApplication> implements CompositeApplication {

  public CompositeApplicationImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub

  }
  
  @Override
  public String getCharsetName() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMECHARSET, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public String getMimeType() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMETYPE, String.class, ""); //$NON-NLS-1$
  }

}
