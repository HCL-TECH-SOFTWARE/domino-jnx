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
import com.hcl.domino.design.ImageResource;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.process.GetImageResourceStreamProcessor;

public class ImageResourceImpl extends AbstractNamedFileElement<ImageResource> implements ImageResource {

  public ImageResourceImpl(final Document doc) {
    super(doc);
  }

  @Override
  public String getCharsetName() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMECHARSET, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public InputStream getFileData() {
    return GetImageResourceStreamProcessor.instance.apply(this.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA));
  }

  @Override
  public List<String> getFileNames() {
    return this.getDocument().getAsList(NotesConstants.ITEM_NAME_IMAGE_NAMES, String.class, Collections.emptyList());
  }

  @Override
  public int getImagesAcross() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_IMAGES_WIDE, int.class, 1);
  }

  @Override
  public int getImagesDown() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_IMAGES_HIGH, int.class, 1);
  }

  @Override
  public String getMimeType() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_FILE_MIMETYPE, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isColorizeGrays() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_IMAGES_COLORIZE, int.class, 0) == 1;
  }

  @Override
  public boolean isMarkedForReplace() {
    return this.getFlags().contains("$"); //$NON-NLS-1$
  }

  @Override
  public boolean isWebCompatible() {
    return this.getDocument().get(NotesConstants.ITEM_NAME_IMAGES_WEB_BROWSER_COMPATIBLE, int.class, 0) == 1;
  }

  @Override
  public boolean isWebReadOnly() {
    return this.getFlags().contains("&"); //$NON-NLS-1$
  }

}
