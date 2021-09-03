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
package com.hcl.domino.richtext.structures;

import java.nio.ByteBuffer;
import java.util.Formatter;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;

@StructureDefinition(name = "UNIVERSALNOTEID", members = {
    @StructureMember(name = "File", type = OpaqueTimeDate.class),
    @StructureMember(name = "Note", type = OpaqueTimeDate.class)
})
public interface UNID extends MemoryStructure {
  @StructureGetter("File")
  OpaqueTimeDate getFile();

  @StructureGetter("Note")
  OpaqueTimeDate getNote();

  /**
   * @return {@code true} if this UNID is a zero value; {@code false} otherwise
   */
  default boolean isUnset() {
    final ByteBuffer data = this.getData();
    final long file = data.getLong();
    final long note = data.getLong();
    return file == 0 && note == 0;
  }

  /**
   * Changes the internal value to a UNID formatted as string
   *
   * @param unidStr UNID string
   * @return UNID a {@link UNID} object for the string
   */
  default UNID setUnid(final String unidStr) {
	  if (unidStr==null || unidStr.length()==0) {
		  this.getFile().setInnards(new int[] { 0, 0 });
		  this.getNote().setInnards(new int[] { 0, 0 });
		  return this;
	  }
	  else {
		  if (unidStr.length() != 32) {
			  throw new IllegalArgumentException("UNID is expected to have 32 characters");
		  }

		  final int fileInnards1 = (int) (Long.parseLong(unidStr.substring(0, 8), 16) & 0xffffffff);
		  final int fileInnards0 = (int) (Long.parseLong(unidStr.substring(8, 16), 16) & 0xffffffff);

		  final int noteInnards1 = (int) (Long.parseLong(unidStr.substring(16, 24), 16) & 0xffffffff);
		  final int noteInnards0 = (int) (Long.parseLong(unidStr.substring(24, 32), 16) & 0xffffffff);

		  this.getFile().setInnards(new int[] { fileInnards0, fileInnards1 });
		  this.getNote().setInnards(new int[] { noteInnards0, noteInnards1 });
	  }

	  return this;
  }

  /**
   * Computes the hex UNID from the OID data
   *
   * @return UNID
   */
  default String toUnidString() {
    final Formatter formatter = new Formatter();
    final ByteBuffer data = this.getData();
    formatter.format("%016x", data.getLong()); //$NON-NLS-1$
    formatter.format("%016x", data.getLong()); //$NON-NLS-1$
    final String unidStr = formatter.toString().toUpperCase();
    formatter.close();
    return unidStr;
  }
}
