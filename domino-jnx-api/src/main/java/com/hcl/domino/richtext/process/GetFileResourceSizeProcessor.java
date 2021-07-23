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
package com.hcl.domino.richtext.process;

import java.util.List;

import com.hcl.domino.richtext.records.CDFileHeader;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Retrieves the size of the file stored in a file-resource CD item (e.g.
 * {@code $FileData}).
 * <p>
 * This processing happens synchronously.
 * </p>
 *
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public class GetFileResourceSizeProcessor implements IRichTextProcessor<Long> {

  /**
   * Constructs a new file-size extractor.
   */
  public GetFileResourceSizeProcessor() {
  }

  @Override
  public Long apply(final List<RichTextRecord<?>> t) {
    final long size = t.stream()
        .filter(CDFileHeader.class::isInstance)
        .map(CDFileHeader.class::cast)
        .findFirst()
        .map(CDFileHeader::getFileDataSize)
        .orElseThrow(() -> new IllegalStateException("Could not find CDFileHeader segment"));
    return size;
  }

}
