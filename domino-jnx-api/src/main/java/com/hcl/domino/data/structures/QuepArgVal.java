/*
 * ==========================================================================
 * Copyright (C) 2019-2024 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.data.structures;

import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * Represents aggregate data about a view or folder.
 * 
 * @since 1.44.0
 */
@StructureDefinition(
    name = "QUEP_ARGVAL",
    members = {
      @StructureMember(name = "type", type = ItemDataType.class),
      @StructureMember(name = "padding", type = short.class),
      @StructureMember(name = "length", type = /* DWORD */ int.class, unsigned = true),
      @StructureMember(name = "ordinal", type = /* DWORD */ int.class, unsigned = true),
      @StructureMember(name = "bBinaryForm", type = /* BOOL */ int.class),
      @StructureMember(name = "ArgName", type = byte[].class, length = NotesConstants.MAXQARGNAMELEN),
      @StructureMember(name = "Value", type = byte[].class, length = NotesConstants.MAXTEXTARGVAL)
    }
  )
public interface QuepArgVal extends MemoryStructure {
  @StructureGetter("type")
  ItemDataType getType();
  
  /**
   * Sets the expected type of {@code Value}.
   * 
   * @param type one of {@link ItemDataType#TYPE_TEXT}, {@link ItemDataType#TYPE_NUMBER},
   *        and {@link ItemDataType#TYPE_TIME}.
   */
  @StructureSetter("type")
  QuepArgVal setType(ItemDataType type);
  
  @StructureGetter("length")
  long getLength();
  
  @StructureSetter("length")
  QuepArgVal setLength(long length);
  
  @StructureGetter("ordinal")
  long getOrdinal();
  
  @StructureSetter("ordinal")
  QuepArgVal setOrdinal(long ordinal);
  
  @StructureGetter("bBinaryForm")
  boolean isBinaryForm();
  
  @StructureSetter("bBinaryForm")
  QuepArgVal setBinaryForm(boolean binaryForm);
  
  @StructureGetter("ArgName")
  byte[] getArgName();
  
  @StructureSetter("ArgName")
  QuepArgVal setArgName(byte[] argName);
  
  @StructureGetter("Value")
  byte[] getValue();
  
  @StructureSetter("Value")
  QuepArgVal setValue(byte[] value);
}
