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
package com.hcl.domino.richtext.conversion;

import java.util.List;

import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Interface for a conversion class that transforms rich text structures
 * 
 * @author Karsten Lehmann
 */
public interface IRichTextConversion {
	
	/**
	 * Method is called before starting the CD record traversal. Can be used
	 * to reset variables, e.g. to track if we are within a BEGIN/END block.
	 */
	void richTextNavigationStart();
	
	/**
	 * Method to check whether the rich text item actually requires a conversion
	 * 
	 * @param nav rich text navigator
	 * @return true if conversion is required
	 */
	boolean isMatch(List<RichTextRecord<?>> nav);
	
	/**
	 * Method to do the actual conversion, e.g. traversing the CD records of the specified
	 * {@link RichTextRecord} list and writing the resulting output to the <code>target</code>.
	 * 
	 * @param source source rich text navigator
	 * @param target target to write conversion result
	 */
	void convert(List<RichTextRecord<?>> source, RichTextWriter target);
	
	/**
	 * Method is called when the rich text navigation is done
	 */
	void richTextNavigationEnd();

}
