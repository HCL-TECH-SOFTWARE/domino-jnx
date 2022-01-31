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

public interface IFtErr extends IGlobalErr {

  @ErrorText(text = "Unknown full text indexing error")
  short ERR_FT = IGlobalErr.PKG_FT + 0;
  @ErrorText(text = "Error from full text package")
  short ERR_FT_RAW = IGlobalErr.PKG_FT + 1;
  @ErrorText(text = "Database is not full text indexed")
  short ERR_FT_NOT_INDEXED = IGlobalErr.PKG_FT + 2;
  @ErrorText(text = "Unable to initialize full text package")
  short ERR_FT_INIT_API = IGlobalErr.PKG_FT + 3;
  @ErrorText(text = "Full text index not found for this database")
  short ERR_FT_NOT_FOUND = IGlobalErr.PKG_FT + 4;
  @ErrorText(text = "Document is not in the search results")
  short ERR_FT_NO_EXIST = IGlobalErr.PKG_FT + 5;
  @ErrorText(text = "FT Summarize error - %s")
  short LOG_FT_SUMM_ERR = IGlobalErr.PKG_FT + 6;
  @ErrorText(text = "Application must be full text indexed before search is allowed")
  short ERR_FT_DB_NOT_FOUND = IGlobalErr.PKG_FT + 7;
  @ErrorText(text = "Unable to obtain word highlights")
  short ERR_FT_HIGHLIGHTS = IGlobalErr.PKG_FT + 8;
  @ErrorText(text = "Query is not understandable")
  short ERR_FT_BAD_QUERY = IGlobalErr.PKG_FT + 9;
  @ErrorText(text = "Highlights are not available; this document has been modified since being indexed")
  short ERR_STALE_HIGHLIGHTS = IGlobalErr.PKG_FT + 10;
  @ErrorText(text = "Unable to obtain search results; re-open database")
  short ERR_FT_BAD_SEARCH_HANDLE = IGlobalErr.PKG_FT + 11;
  @ErrorText(text = "The full text index for this database is in use")
  short ERR_FT_IN_USE = IGlobalErr.PKG_FT + 12;
  @ErrorText(text = "The full text index needs to be rebuilt")
  short ERR_FT_REBUILD = IGlobalErr.PKG_FT + 13;
  @ErrorText(text = "The existing full text index was built by later version of this product")
  short ERR_FT_NEWER_VER = IGlobalErr.PKG_FT + 14;
  @ErrorText(text = "(The allocated structure size is too small for this engine)")
  short ERR_FT_STRUCT_SIZE = IGlobalErr.PKG_FT + 15;
  @ErrorText(text = "(The full text index is corrupt - version 0)")
  short ERR_FT_VER0 = IGlobalErr.PKG_FT + 16;
  @ErrorText(text = "Full text error; see log for more information")
  short ERR_FT_TOPIC = IGlobalErr.PKG_FT + 17;
  @ErrorText(text = "Full text directory links must contain a valid directory name")
  short ERR_FT_DIR_LINK = IGlobalErr.PKG_FT + 18;
  @ErrorText(text = "Database is currently being indexed by another process")
  short ERR_FT_BEING_INDEXED = IGlobalErr.PKG_FT + 19;
  @ErrorText(text = "Maximum allowable documents exceeded for a temporary full text index")
  short ERR_FT_TEMP_MAXDOCS = IGlobalErr.PKG_FT + 20;
  @ErrorText(text = "Full Text message: %s")
  short LOG_FT_TOPIC_MSG = IGlobalErr.PKG_FT + 21;
  @ErrorText(text = "Topic error stack [%lu]:  %s")
  short LOG_FT_TOPIC_ERR_STACK = IGlobalErr.PKG_FT + 22;
  @ErrorText(text = "Topic error %ld deleting document from full text index")
  short LOG_FT_DELETE_ERR = IGlobalErr.PKG_FT + 23;
  @ErrorText(text = "The full text index structure for %s is incompatible and is being rebuilt.")
  short LOG_FT_LEGACY_ERR = IGlobalErr.PKG_FT + 24;
  @ErrorText(text = "Error full text indexing document NT%08lx %s %s")
  short LOG_FT_INDEX_ERR = IGlobalErr.PKG_FT + 25;
  @ErrorText(text = "Document has no title")
  short LOG_FT_DOCNOTITLE = IGlobalErr.PKG_FT + 26;
  @ErrorText(text = "Query was not converted into web-style syntax.")
  short ERR_FT_NO_WEBQUERY = IGlobalErr.PKG_FT + 27;
  @ErrorText(text = "Warning: cannot merge full text index %p due to insufficient disk space")
  short LOG_FT_INDEX_MERGE_DISK_WARN = IGlobalErr.PKG_FT + 28;
  @ErrorText(text = "Full Text Error (FTG): Exceeded max configured index size while indexing document NT%08lx for database %p in domain index %p")
  short ERR_FT_DOMAIN_INDEX_LIMIT = IGlobalErr.PKG_FT + 29;
  @ErrorText(text = "More than 5000 documents found; please refine the search")
  short ERR_FT_MORE_THAN_5000_DOCS = IGlobalErr.PKG_FT + 30;
  @ErrorText(text = "The field name used in the query cannot be found in the database.")
  short ERR_FT_BAD_FIELD = IGlobalErr.PKG_FT + 31;
  @ErrorText(text = "Unbalanced parentheses in query.")
  short ERR_FT_BAD_PAREN = IGlobalErr.PKG_FT + 32;
  @ErrorText(text = "Unknown operator in field query.")
  short ERR_FT_BAD_OPERATOR = IGlobalErr.PKG_FT + 33;
  @ErrorText(text = "No documents found")
  short ERR_FT_NOMATCHES = IGlobalErr.PKG_FT + 34;
  @ErrorText(text = "Relational operators are not supported in text fields")
  short ERR_FT_TEXT_FIELD = IGlobalErr.PKG_FT + 35;
  @ErrorText(text = "The query contains a new number or date field that was added after the full text index was created.  Please recreate the full text index to use this field in queries.")
  short ERR_FT_FIELD_NOT_INDEXED = IGlobalErr.PKG_FT + 36;
  @ErrorText(text = "Full text index is missing a necessary field")
  short ERR_FT_INDEX_BAD = IGlobalErr.PKG_FT + 37;
  @ErrorText(text = "Full text error from Topic")
  short ERR_FT_TOPIC_NOSEE_LOG = IGlobalErr.PKG_FT + 38;
  @ErrorText(text = "The full text index for this database is corrupted.  Delete and recreate the full text index.")
  short ERR_FT_TOPIC_CORRUPT_INDEX = IGlobalErr.PKG_FT + 39;
  @ErrorText(text = "Indexing rate too slow. (target ms/100 documents:%ld, actual:%ld) for %s")
  short ERR_FT_INDEXING_TOO_SLOW = IGlobalErr.PKG_FT + 41;
  @ErrorText(text = "Expected Verity format query must be inside double quotes")
  short ERR_FT_NEEDQUOTES = IGlobalErr.PKG_FT + 46;
  @ErrorText(text = "Invalid Argument Structure")
  short ERR_FT_BADARG_STRUCT = IGlobalErr.PKG_FT + 47;
  @ErrorText(text = "Wrong Handle Type")
  short ERR_FT_BADHANDLE_TYPE = IGlobalErr.PKG_FT + 48;
  @ErrorText(text = "Warning - search handle remains locked")
  short ERR_FT_SRCH_HDLOCK = IGlobalErr.PKG_FT + 49;
  @ErrorText(text = "No documents specified")
  short ERR_FT_NO_DOCS = IGlobalErr.PKG_FT + 50;
  @ErrorText(text = "Error in Full Text document streaming.  See preceding log messages.")
  short ERR_FT_INVALIDDOC = IGlobalErr.PKG_FT + 51;
  @ErrorText(text = "Not enough memory for Full Text Indexing or Search")
  short ERR_FT_LOW_MEMORY = IGlobalErr.PKG_FT + 52;
  @ErrorText(text = "Full Text Index is Down for Repairs")
  short ERR_FT_DOWN = IGlobalErr.PKG_FT + 53;
  @ErrorText(text = "Full text indexing documents in (%p) '%p' for multi db index")
  short ERR_FT_INDEXING_FILE = IGlobalErr.PKG_FT + 54;
  @ErrorText(text = "%lu documents (%lu bytes) indexed in (%p) '%p' for multi db index")
  short ERR_FT_INDEXING_DONE = IGlobalErr.PKG_FT + 55;
  @ErrorText(text = "Extended search features not supported by server")
  short ERR_FT_NO_EXT_SUPPORT = IGlobalErr.PKG_FT + 56;
  @ErrorText(text = "Domain Search Pool is Full")
  short ERR_FT_DOMAIN_POOLFULL = IGlobalErr.PKG_FT + 57;
  @ErrorText(text = "Full Text unable to Filter File")
  short ERR_FT_FILTER_FILE = IGlobalErr.PKG_FT + 58;
  @ErrorText(text = "Full Text Error (FTG): Exceeded max configured index size while indexing document NT%08lx in database index %p")
  short ERR_FT_INDEX_LIMIT = IGlobalErr.PKG_FT + 59;
  @ErrorText(text = "Full Text Error - not enough space on disk to build index")
  short ERR_FT_NO_SPACE = IGlobalErr.PKG_FT + 60;
  @ErrorText(text = "Full Text Error - failure to initialize KV filter")
  short ERR_FT_KV_INIT = IGlobalErr.PKG_FT + 61;
  @ErrorText(text = "FT KV Text Filter error processing NT%08lx, Attachment = %s in database %p - %s")
  short LOG_FT_KEYVIEW_ERR_DB = IGlobalErr.PKG_FT + 62;
  @ErrorText(text = "FT KV Text Filter error processing %p - %s|Removing full-text index directory %s")
  short LOG_FT_STRINGS1 = IGlobalErr.PKG_FT + 63;

}
