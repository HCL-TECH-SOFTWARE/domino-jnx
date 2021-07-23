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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import com.hcl.domino.richtext.records.CDImageSegment;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Extracts the contents of a image-resource CD item (e.g. {@code $ImageData}) and writes
 * the content to the provided {@link OutputStream}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public class ExtractImageResourceProcessor implements IRichTextProcessor<Void> {

	private final OutputStream os;
	
	/**
	 * Constructs an extraction processor to output to the provided stream.
	 * 
	 * @param os the non-null {@link OutputStream} to target
	 */
	public ExtractImageResourceProcessor(OutputStream os) {
		this.os = Objects.requireNonNull(os, "OutputStream must not be null");
	}
	
	@Override
	public Void apply(List<RichTextRecord<?>> t) {
		t.stream()
			.filter(CDImageSegment.class::isInstance)
			.map(CDImageSegment.class::cast)
			.forEach(record -> {
				try {
					os.write(record.getImageSegmentData());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		return null;
	}

}