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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.commons.util.BufferingCallbackOutputStream;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.NamedFileElement;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.process.GetFileResourceSizeProcessor;
import com.hcl.domino.richtext.process.GetFileResourceStreamProcessor;

/**
 * @param <T> The {@link NamedFileElement} implementation type
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public abstract class AbstractNamedFileElement<T extends NamedFileElement<T>> extends AbstractDesignElement<T>
    implements NamedFileElement<T>, IDefaultNamedDesignElement {

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
  
  @SuppressWarnings("unchecked")
  @Override
  public T setFileNames(Collection<String> fileNames) {
    this.getDocument().replaceItemValue(NotesConstants.ITEM_NAME_FILE_NAMES, fileNames);
    return (T)this;
  }

  @Override
  public long getFileSize() {
    return GetFileResourceSizeProcessor.instance.apply(this.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_FILE_DATA));
  }

  @Override
  public OutputStream newOutputStream() {
    // TODO don't use a buffering stream here, and instead write bytes in chunks as they come along,
    //      then update the header record
    return new BufferingCallbackOutputStream(bytes -> {
      Document doc = getDocument();
      doc.removeItem(NotesConstants.ITEM_NAME_FILE_DATA);
      try(
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        RichTextWriter w = doc.createRichTextItem(NotesConstants.ITEM_NAME_FILE_DATA)
      ) {
        w.addFileResource(bais, bytes.length);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      doc.replaceItemValue(NotesConstants.ITEM_NAME_FILE_SIZE, bytes.length);
      doc.replaceItemValue(NotesConstants.ITEM_NAME_FILE_MODINFO, OffsetDateTime.now());
    });
  }

  
  @Override
  public String getCharsetName() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMECHARSET, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public String getMimeType() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMETYPE, String.class, ""); //$NON-NLS-1$
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public T setMimeType(String mimeType) {
    this.getDocument().replaceItemValue(NotesConstants.ITEM_NAME_FILE_MIMETYPE, mimeType);
    return (T)this;
  }
  
  @Override
  public boolean isNeedsRefresh() {
    return getFlags().contains(NotesConstants.DESIGN_FLAG_NEEDSREFRESH);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public T setNeedsRefresh(boolean refresh) {
    setFlag(NotesConstants.DESIGN_FLAG_NEEDSREFRESH, refresh);
    return (T)this;
  }
}
