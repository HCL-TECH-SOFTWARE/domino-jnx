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
package com.hcl.domino.design.simplesearch;

import com.hcl.domino.misc.NotesConstants;

/**
 * Represents a search for documents with a given author.
 * 
 * <p>This is a specialization of {@link ByFieldTerm} that applies to specifically
 * the updated-by list (the {@value NotesConstants#FIELD_UPDATED_BY} item).</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByAuthorTerm extends ByFieldTerm {
}
