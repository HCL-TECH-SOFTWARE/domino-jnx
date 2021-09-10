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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.38
 */
@StructureDefinition(
  name = "CDQUERYTEXTTERM",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDQueryTextTerm.Flag.class, bitfield = true),
    @StructureMember(name = "dwLength", type = int[].class, length = NotesConstants.MAXTEXTTERMCOUNT, unsigned = true)
  }
)
public interface CDQueryTextTerm extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /**   String is a Notes Full Text Search Query String  */
    RAW(NotesConstants.TEXTTERM_FLAG_RAW),
    /**   String is in Verity Syntax  */
    VERITY(NotesConstants.TEXTTERM_FLAG_VERITY),
    /**   String is comma-separated list of words; AND assumed  */
    AND(NotesConstants.TEXTTERM_FLAG_AND),
    /**   String is comma-separated list of words; ACCRUE assumed  */
    ACCRUE(NotesConstants.TEXTTERM_FLAG_ACCRUE),
    /**   String is comma-separated list of words; NEAR assumed  */
    NEAR(NotesConstants.TEXTTERM_FLAG_NEAR),
    /**   This object is displayed as plain text  */
    PLAINTEXT(NotesConstants.TEXTTERM_FLAG_PLAINTEXT);

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("dwFlags")
  Set<Flag> getFlags();
  
  @StructureSetter("dwFlags")
  CDQueryTextTerm setFlags(Collection<Flag> flags);
  
  @StructureGetter("dwLength")
  long[] getTermLengths();
  
  @StructureSetter("dwLength")
  CDQueryTextTerm setTermLengths(long[] lengths);
  
  default List<String> getTerms() {
    // Filter out blank entries, since there will always be 10 slots
    List<String> terms = StructureSupport.extractStringValues(
      this,
      0,
      getTermLengths()
    );
    return terms.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
  }
  
  default CDQueryTextTerm setTerms(List<String> terms) {
    return StructureSupport.writeStringValues(
      this,
      0,
      getTermLengths(),
      terms,
      this::setTermLengths
    );
  }
}
