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
package com.hcl.domino.html;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;

import com.hcl.domino.data.Database.Action;

/**
 * Interface gives access to referenced embedded images in the HTML conversion of rich text
 */
public interface EmbeddedImage {
	
	/**
	 * Returns the name of the item that contains this image. Use the values of<br>
	 * <ul>
	 * <li>{@link #getItemName()}</li>
	 * <li>{@link #getItemOffset()}</li>
	 * <li>{@link #getItemIndex()}</li>
	 * <li>{@link #getOptions()}</li>
	 * </ul>
	 * to locate the data for this image in the document via {@link RichTextHTMLConverter#readEmbeddedImage}.
	 * 
	 * @return item name
	 */
	String getItemName();

	/**
	 * Returns the item offset. Use the values of<br>
	 * <ul>
	 * <li>{@link #getItemName()}</li>
	 * <li>{@link #getItemOffset()}</li>
	 * <li>{@link #getItemIndex()}</li>
	 * <li>{@link #getOptions()}</li>
	 * </ul>
	 * to locate the data for this image in the document.
	 * 
	 * @return item offset
	 */
	int getItemOffset();
	
	/**
	 * Returns the item index. Use the values of<br>
	 * <ul>
	 * <li>{@link #getItemName()}</li>
	 * <li>{@link #getItemOffset()}</li>
	 * <li>{@link #getItemIndex()}</li>
	 * <li>{@link #getOptions()}</li>
	 * </ul>
	 * to locate the data for this image in the document.
	 * 
	 * @return item index
	 */
	int getItemIndex();
	
	/**
	 * Use the HTML convert options used to render this embedded image. Use the values of<br>
	 * <ul>
	 * <li>{@link #getItemName()}</li>
	 * <li>{@link #getItemOffset()}</li>
	 * <li>{@link #getItemIndex()}</li>
	 * <li>{@link #getOptions()}</li>
	 * </ul>
	 * to locate the data for this image in the document.
	 * 
	 * @return convert options
	 */
	Collection<String> getOptions();

	/**
	 * Returns img tag URL (src attribute value), can be used for string replacement
	 * if the image is stored locally on disk
	 * 
	 * @return relative image URL
	 */
	String getImageSrcAttr();
	
	/**
	 * Method to directly access the image data without storing it to disk
	 * 
	 * @param callback callback to receive data
	 */
	void readImage(HTMLImageReader callback);
	
	/**
	 * Convenience method to write the whole image to a file
	 * 
	 * @param filePath image filepath
	 * @throws IOException on case of I/O errors
	 */
	void writeImage(Path filePath) throws IOException;

	/**
	 * Convenience method to write the whole image to an output stream
	 * 
	 * @param out stream
	 * @throws IOException on case of I/O errors
	 */
	void writeImage(OutputStream out) throws IOException;

	/**
	 * Returns the image format, either "gif" or "jpg"
	 * 
	 * @return format
	 */
	String getFormat();
	
	/**
	 * Callback interface that receives data of embedded images
	 */
	public interface HTMLImageReader {
		
		/**
		 * Reports the size of the image
		 * 
		 * @param size size
		 * @return return how many bytes to skip before reading
		 */
		int setSize(int size);
		
		/**
		 * Implement this method to receive element data
		 * 
		 * @param data data
		 * @return action, either Continue or Stop
		 */
		Action read(byte[] data);
	}
}
