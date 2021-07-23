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
package com.hcl.domino.richtext;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;

import com.hcl.domino.richtext.process.ExtractFileResourceProcessor;
import com.hcl.domino.richtext.process.ExtractImageResourceProcessor;
import com.hcl.domino.richtext.process.ExtractTextProcessor;
import com.hcl.domino.richtext.process.GetFileResourceSizeProcessor;
import com.hcl.domino.richtext.process.GetImageResourceSizeProcessor;
import com.hcl.domino.richtext.process.IRichTextProcessor;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * This sub-interface of {@link List} represents an ordered collection of
 * {@link RichTextRecord}s.
 * 
 * @since 1.0.15
 */
public interface RichTextRecordList extends List<RichTextRecord<?>> {
	default <T> T process(IRichTextProcessor<T> processor) {
		return processor.apply(this);
	}
	
	/**
	 * Retrieves the size of the file stored in a file-resource CD item (e.g. {@code $FileData}).
	 * 
	 * @return the size of the file resource in bytes
	 */
	default long getFileResourceSize() {
		return process(new GetFileResourceSizeProcessor());
	}
	
	/**
	 * Extracts the contents of a file-resource CD item (e.g. {@code $FileData}) and writes
	 * the content to the provided {@link OutputStream}.
	 * 
	 * @param os the target {@link OutputStream}
	 */
	default void extractFileResource(OutputStream os) {
		process(new ExtractFileResourceProcessor(os));
	}
	
	/**
	 * Retrieves the size of the file stored in a image-resource CD item (e.g. {@code $ImageData}).
	 * 
	 * @return the size of the file resource in bytes
	 */
	default long getImageResourceSize() {
		return process(new GetImageResourceSizeProcessor());
	}
	
	/**
	 * Extracts the contents of a image-resource CD item (e.g. {@code $ImageData}) and writes
	 * the content to the provided {@link OutputStream}.
	 * 
	 * @param os the target {@link OutputStream}
	 */
	default void extractImageResource(OutputStream os) {
		process(new ExtractImageResourceProcessor(os));
	}
	
	/**
	 * Extracts the contents of {@link CDText} CD item and writes the content
	 * to the provided {@link Appendable}.
	 * 
	 * @param out the target {@link Appendable}
	 * @since 1.0.20
	 */
	default void extractText(Appendable out) {
		process(new ExtractTextProcessor(out));
	}
	
	/**
	 * Extracts the contents of {@link CDText} CD item and writes the content
	 * to the provided {@link Appendable}.
	 * 
	 * @return text content
	 * @since 1.0.20
	 */
	default String extractText() {
		StringWriter writer = new StringWriter();
		process(new ExtractTextProcessor(writer));
		return writer.toString();
	}
	
}
