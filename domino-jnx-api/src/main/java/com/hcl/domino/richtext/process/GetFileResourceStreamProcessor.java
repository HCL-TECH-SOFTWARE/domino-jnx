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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.hcl.domino.richtext.records.CDFileSegment;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Extracts the contents of a file-resource CD item (e.g. {@code $FileData}) as an
 * {@link InputStream}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class GetFileResourceStreamProcessor implements IRichTextProcessor<InputStream> {
	public static final GetFileResourceStreamProcessor instance = new GetFileResourceStreamProcessor();

	@Override
	public InputStream apply(List<RichTextRecord<?>> t) {
		// TODO don't pre-read all data
		// TODO probably validate the CD stream
		
		byte[] data;
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			t.forEach(record -> {
				if(record instanceof CDFileSegment) {
					try {
						os.write(((CDFileSegment)record).getFileSegmentData());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
			data = os.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		return new ByteArrayInputStream(data);
	}

}
