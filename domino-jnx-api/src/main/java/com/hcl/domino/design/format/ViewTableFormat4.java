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
package com.hcl.domino.design.format;

import java.util.Optional;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

@StructureDefinition(name = "VIEW_TABLE_FORMAT4", members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = int.class), /*  Reserved for future use */
    @StructureMember(name = "RepeatType", type = ImageRepeatMode.class)
})
public interface ViewTableFormat4 extends MemoryStructure {
  public static ViewTableFormat4 newInstanceWithDefaults() {
    ViewTableFormat4 format4 = MemoryStructureWrapperService.get().newStructure(ViewTableFormat4.class, 0);
    
    format4.setRepeatType(ImageRepeatMode.ONCE);
    
    return format4;
  }

  @StructureGetter("Length")
  int getLength();

  @StructureSetter("Length")
  ViewTableFormat4 setLength(int len);

  @StructureSetter("Flags")
  ViewTableFormat4 setFlagsRaw(int flags);

  @StructureGetter("Flags")
  int getFlagsRaw();

  @StructureGetter("RepeatType")
  Optional<ImageRepeatMode> getRepeatType();

  /**
   * Retrieves the repeat type as a raw {@code short}.
   * 
   * @return the repeat type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("RepeatType")
  short getRepeatTypeRaw();

  @StructureSetter("RepeatType")
  ViewTableFormat4 setRepeatType(ImageRepeatMode type);

  /**
   * Sets the repeat type as a raw {@code short}.
   * 
   * @param type the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("RepeatType")
  ViewTableFormat4 setRepeatTypeRaw(short type);
}
