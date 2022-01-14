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
package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code oleods.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.0.43
 */
public interface OleOds {

  /** Object is scripted */
  int OBJINFO_FLAGS_SCRIPTED = 0x00000001;
  /** Object is run in read-only mode */
  int OBJINFO_FLAGS_RUNREADONLY = 0x00000002;
  /** Object is a control */
  int OBJINFO_FLAGS_CONTROL = 0x00000004;
  /** Object is sized to fit to window */
  int OBJINFO_FLAGS_FITTOWINDOW = 0x00000008;
  /** Object is sized to fit below fields */
  int OBJINFO_FLAGS_FITBELOWFIELDS = 0x00000010;
  /** Object is to be updated from document */
  int OBJINFO_FLAGS_UPDATEFROMDOCUMENT = 0x00000020;
  /** Object is to be updated from document */
  int OBJINFO_FLAGS_INCLUDERICHTEXT = 0x00000040;
  /** Object is stored in IStorage/IStream format rather than RootIStorage/IStorage/IStream */
  int OBJINFO_FLAGS_ISTORAGE_ISTREAM = 0x00000080;
  /** Object has HTML data */
  int OBJINFO_FLAGS_HTMLDATA = 0x00000100;
  
  /** OLE "Docfile" structured storage format, RootIStorage/IStorage/IStream (Notes format) */
  short OLE_STG_FMT_STRUCT_STORAGE = 1;
  /** OLE IStorage/IStream structured storage format */
  short OLE_STG_FMT_ISTORAGE_ISTREAM = 2;
  /** OLE RootIStorage/IStream structured storage format */
  short OLE_STG_FMT_STRUCT_STREAM = 3;
}
