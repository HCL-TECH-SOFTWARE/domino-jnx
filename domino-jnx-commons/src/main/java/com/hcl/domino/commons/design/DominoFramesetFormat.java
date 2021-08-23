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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.hcl.domino.richtext.records.CDFrame;
import com.hcl.domino.richtext.records.CDFrameset;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Represents and provides utility access to the records that make up a Frameset.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public class DominoFramesetFormat {
  private final List<RichTextRecord<?>> records;

  public DominoFramesetFormat(List<RichTextRecord<?>> records) {
    this.records = new ArrayList<>(records);
  }
  
  public Optional<CDFrameset> getFramesetRecord() {
    return records.stream()
      .filter(CDFrameset.class::isInstance)
      .map(CDFrameset.class::cast)
      .findFirst();
  }
  
  public Stream<CDFrame> getFrameRecords() {
    return records.stream()
      .filter(CDFrame.class::isInstance)
      .map(CDFrame.class::cast);
  }

}
