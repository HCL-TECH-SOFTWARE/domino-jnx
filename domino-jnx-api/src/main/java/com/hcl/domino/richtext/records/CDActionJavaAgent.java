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

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDACTIONJAVAAGENT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "wClassNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "wCodePathLen", type = short.class, unsigned = true),
    @StructureMember(name = "wFileListBytes", type = short.class, unsigned = true),
    @StructureMember(name = "wLibraryListBytes", type = short.class, unsigned = true),
    @StructureMember(name = "wSpare", type = short.class),
    @StructureMember(name = "dwSpare", type = int.class)
})
public interface CDActionJavaAgent extends RichTextRecord<WSIG> {
  default String getClassName() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getClassNameLength());
  }

  @StructureGetter("wClassNameLen")
  int getClassNameLength();

  default String getCodePath() {
    return StructureSupport.extractStringValue(
        this,
        this.getClassNameLength(),
        this.getCodePathLength());
  }

  @StructureGetter("wCodePathLen")
  int getCodePathLength();

  default String getFileList() {
    return StructureSupport.extractStringValue(
        this,
        this.getClassNameLength() + this.getCodePathLength(),
        this.getFileListLength());
  }

  @StructureGetter("wFileListBytes")
  int getFileListLength();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getLibraryList() {
    return StructureSupport.extractStringValue(
        this,
        this.getClassNameLength() + this.getCodePathLength() + this.getFileListLength(),
        this.getLibraryListLength());
  }

  @StructureGetter("wLibraryListBytes")
  int getLibraryListLength();

  default CDActionJavaAgent setClassName(final String className) {
    StructureSupport.writeStringValue(
        this,
        0,
        this.getClassNameLength(),
        className,
        this::setClassNameLength);
    return this;
  }

  @StructureSetter("wClassNameLen")
  CDActionJavaAgent setClassNameLength(int len);

  default CDActionJavaAgent setCodePath(final String codePath) {
    StructureSupport.writeStringValue(
        this,
        this.getClassNameLength(),
        this.getCodePathLength(),
        codePath,
        this::setCodePathLength);
    return this;
  }

  @StructureSetter("wCodePathLen")
  CDActionJavaAgent setCodePathLength(int len);

  default CDActionJavaAgent setFileList(final String fileList) {
    StructureSupport.writeStringValue(
        this,
        this.getClassNameLength() + this.getCodePathLength(),
        this.getFileListLength(),
        fileList,
        this::setFileListLength);
    return this;
  }

  @StructureSetter("wFileListBytes")
  CDActionJavaAgent setFileListLength(int len);

  default CDActionJavaAgent setLibraryList(final String libraryList) {
    StructureSupport.writeStringValue(
        this,
        this.getClassNameLength() + this.getCodePathLength() + this.getFileListLength(),
        this.getLibraryListLength(),
        libraryList,
        this::setLibraryListLength);
    return this;
  }

  @StructureSetter("wLibraryListBytes")
  CDActionJavaAgent setLibraryListLength(int len);
}
