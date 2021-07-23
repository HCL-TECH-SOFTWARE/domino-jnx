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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.NamedFileElement;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.process.GetFileResourceStreamProcessor;
import com.hcl.domino.richtext.process.GetImageResourceSizeProcessor;

/**
 * @param <T> The {@link NamedFileElement} implementation type
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public abstract class AbstractNamedFileElement<T extends NamedFileElement> extends AbstractNamedDesignElement<T>
    implements NamedFileElement {

  public AbstractNamedFileElement(final Document doc) {
    super(doc);
  }

  @Override
  public InputStream getFileData() {
    return GetFileResourceStreamProcessor.instance.apply(this.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_FILE_DATA));
  }

  @Override
  public DominoDateTime getFileModified() {
    final Document doc = this.getDocument();
    return doc.get(NotesConstants.ITEM_NAME_FILE_MODINFO, DominoDateTime.class, doc.getLastModified());
  }

  @Override
  public List<String> getFileNames() {
    return this.getDocument().getAsList(NotesConstants.ITEM_NAME_FILE_NAMES, String.class, Collections.emptyList());
  }

  @Override
  public long getFileSize() {
    return GetImageResourceSizeProcessor.instance.apply(this.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA));
  }

}
