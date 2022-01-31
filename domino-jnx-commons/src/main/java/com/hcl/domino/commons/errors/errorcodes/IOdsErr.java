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
package com.hcl.domino.commons.errors.errorcodes;

import com.hcl.domino.commons.errors.ErrorText;

public interface IOdsErr extends IGlobalErr {

  @ErrorText(text = "This database cannot be read due to an invalid on disk structure")
  short ERR_ODS = IGlobalErr.PKG_ODS + 1;
  @ErrorText(text = "Cannot convert field - unsupported datatype")
  short ERR_DATATYPE = IGlobalErr.PKG_ODS + 2;
  @ErrorText(text = "Text is too big")
  short ERR_ODS_TEXT_TOOBIG = IGlobalErr.PKG_ODS + 3;
  @ErrorText(text = "Done enumerating CD buffer")
  short ERR_ODS_ENUM_COMPLETE = IGlobalErr.PKG_ODS + 4;
  @ErrorText(text = "No such entry")
  short ERR_ODS_NO_SUCH_ENTRY = IGlobalErr.PKG_ODS + 5;
  @ErrorText(text = "Zero length scratch file")
  short ERR_ODS_FILE_ZEROLENGTH = IGlobalErr.PKG_ODS + 6;
  @ErrorText(text = "Encountered zero length record.")
  short ERR_ODS_REC_ZEROLENGTH = IGlobalErr.PKG_ODS + 7;
  @ErrorText(text = "Encountered unknown record type.")
  short ERR_ODS_REC_UNKNOWN = IGlobalErr.PKG_ODS + 8;
  @ErrorText(text = "Input text is too short to be canonicalized")
  short ERR_ODS_SHORT_INPUT = IGlobalErr.PKG_ODS + 9;
  @ErrorText(text = "Input buffer is too small for read")
  short ERR_ODS_SHORT_BUFFER = IGlobalErr.PKG_ODS + 10;
  @ErrorText(text = "List Entry count in TEXT_LIST is invalid.")
  short ERR_ODS_INVALID_LIST_ENTRY_COUNT = IGlobalErr.PKG_ODS + 11;
  @ErrorText(text = "Range List or Range Entry count in TIME_RANGE is invalid.")
  short ERR_ODS_INVALID_TIME_RANGE_ENTRY_COUNT = IGlobalErr.PKG_ODS + 12;
  @ErrorText(text = "Invalid SCHED_ENTRY_EXT size.")
  short ERR_ODS_INVALID_SCHED_LIST_EXT = IGlobalErr.PKG_ODS + 13;
  @ErrorText(text = "Invalid SCHED_DETAIL_LIST size.")
  short ERR_ODS_INVALID_SCHED_DETAIL_LIST = IGlobalErr.PKG_ODS + 14;
  @ErrorText(text = "Invalid ITEM_TABLE detected")
  short ERR_ODS_INVALID_ITEM_TABLE = IGlobalErr.PKG_ODS + 15;
  @ErrorText(text = "Invalid RFC822TextItem detected")
  short ERR_ODS_INVALID_RFC822TEXTITEM = IGlobalErr.PKG_ODS + 16;

}
