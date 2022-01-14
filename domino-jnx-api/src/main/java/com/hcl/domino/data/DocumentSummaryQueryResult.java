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
package com.hcl.domino.data;

/**
 * Query result of {@link Database#queryDocuments()} and
 * {@link Database#queryDocuments(java.util.Collection)}
 * to run an operations on all or a subset of database documents.<br>
 * <br>
 * Use {@link #sort(DominoCollection)} to project the document note ids onto a
 * {@link DominoCollection}
 * to get them back sorted/filtered and {@link #computeValues(java.util.Map)} to
 * compute document values from summary items on the fly.
 */
public interface DocumentSummaryQueryResult extends DbQueryResult<DocumentSummaryQueryResult> {

}
