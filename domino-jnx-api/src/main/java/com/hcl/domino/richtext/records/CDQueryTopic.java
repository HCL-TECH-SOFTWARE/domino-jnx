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
package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.45
 */
@StructureDefinition(
  name = "CDQUERYTOPIC",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = int.class),
    @StructureMember(name = "wTopicQueryLen", type = short.class, unsigned = true),
    @StructureMember(name = "wDataLen", type = short.class, unsigned = true),
  }
)
public interface CDQueryTopic extends RichTextRecord<WSIG> {
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("wTopicQueryLen")
  int getTopicLength();
  
  @StructureSetter("wTopicQueryLen")
  CDQueryTopic setTopicLength(int len);
  
  @StructureGetter("wDataLen")
  int getQueryDataLength();
  
  @StructureSetter("wDataLen")
  CDQueryTopic setQueryDataLength(int len);
  
  default String getTopic() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getTopicLength()
    );
  }
  
  default CDQueryTopic setTopic(String topic) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getTopicLength(),
      topic,
      this::setTopicLength
    );
  }
  
  default byte[] getQueryData() {
    return StructureSupport.extractByteArray(
      this,
      getTopicLength(),
      getQueryDataLength()
    );
  }
  
  default CDQueryTopic setQueryData(byte[] data) {
    return StructureSupport.writeByteValue(
      this,
      getTopicLength(),
      getQueryDataLength(),
      data,
      this::setQueryDataLength
    );
  }
}
