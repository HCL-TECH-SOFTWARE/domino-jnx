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
package com.hcl.domino.richtext.process;

import java.util.List;

import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Retrieves the size of the file stored in a image-resource CD item (e.g. {@code $ImageData}).
 * 
 * <p>This processing happens synchronously.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public class GetImageResourceSizeProcessor implements IRichTextProcessor<Long> {
	public static final GetImageResourceSizeProcessor instance = new GetImageResourceSizeProcessor();
	
	/**
	 * Constructs a new file-size extractor.
	 */
	public GetImageResourceSizeProcessor() {
	}

	@Override
	public Long apply(List<RichTextRecord<?>> t) {
		long size = t.stream()
			.filter(CDImageHeader.class::isInstance)
			.map(CDImageHeader.class::cast)
			.findFirst()
			.map(CDImageHeader::getImageDataSize)
			.orElseThrow(() -> new IllegalStateException("Could not find CDImageHeader segment"));
		return size;
	}

}
