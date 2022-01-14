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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.List;
import com.hcl.domino.commons.design.outline.DominoOutlineFormat;
import com.hcl.domino.commons.design.outline.OutlineFormatDecoder;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Outline;
import com.hcl.domino.design.OutlineEntry;

public class OutlineImpl extends AbstractDesignElement<Outline> implements Outline, IDefaultNamedDesignElement {

  public OutlineImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    this.setFlags("m"); //$NON-NLS-1$
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<OutlineEntry> getSitemapList() {
    // The outline data may be spread across multiple items, so concatenate them into an array
    byte[] outlineData;
    try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      getDocument().forEachItem(DesignConstants.OUTLINE_SITEMAPLIST_ITEM, (item, loop) -> {
        byte[] data = item.get(byte[].class, new byte[0]);
        if(data.length > 2) {
          baos.write(data, 2, data.length-2);
        }
      });
      outlineData = baos.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    ByteBuffer buf = ByteBuffer.wrap(outlineData);
    DominoOutlineFormat format = OutlineFormatDecoder.decodeOutlineFormat(buf);
    return (List<OutlineEntry>)(List<?>)format.getOutlineEntries();
  }

}
