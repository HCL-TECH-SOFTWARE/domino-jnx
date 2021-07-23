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
package com.hcl.domino.dxl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.misc.CNativeEnum;

public interface DxlExporter {

	boolean exportErrorWasLogged();
	
	/**
	 * Export a single document into XML format.
	 * 
	 * @param doc document to export
	 * @param out result writer
	 * @throws IOException in case of I/O errors
	 */
	void exportDocument(Document doc, Writer out) throws IOException;
	
	/**
	 * Export a single document into XML format.
	 * 
	 * @param doc document to export
	 * @param out result stream
	 * @throws IOException in case of I/O errors
	 */
	default void exportDocument(Document doc, final OutputStream out) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		exportDocument(doc, w);
		w.flush();
	}
	
	/**
	 * Export a set of note ids into XML format.
	 * 
	 * @param db database containing the export ids
	 * @param ids ids to export
	 * @param out result writer
	 * @throws IOException in case of I/O errors
	 */
	void exportIDs(Database db, Collection<Integer> ids, Writer out) throws IOException;
	
	/**
	 * Export a set of note ids into XML format.
	 * 
	 * @param db database containing the export ids
	 * @param ids ids to export
	 * @param out result stream
	 * @throws IOException in case of I/O errors
	 */
	default void exportIDs(Database db, Collection<Integer> ids, OutputStream out) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		exportIDs(db, ids, w);
		w.flush();
	}
	
	/**
	 * Export an entire database in XML format.
	 * 
	 * @param db database to export
	 * @param out result writer
	 * @throws IOException in case of I/O errors
	 */
	void exportDatabase(final Database db, final Writer out) throws IOException;
	
	/**
	 * Export an entire database in XML format.
	 * 
	 * @param db database to export
	 * @param out result stream
	 * @throws IOException in case of I/O errors
	 */
	default void exportDatabase(final Database db, final OutputStream out) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		exportDatabase(db, w);
		w.flush();
	}
	
	/**
	 * Export the ACL of the specified database in XML format.
	 * 
	 * @param db database to export
	 * @param out result writer
	 * @throws IOException in case of I/O errors
	 */
	void exportACL(final Database db, final Writer out) throws IOException;
	
	/**
	 * Export the ACL of the specified database in XML format.
	 * 
	 * @param db database to export
	 * @param out result stream
	 * @throws IOException in case of I/O errors
	 */
	default void exportACL(final Database db, final OutputStream out) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		exportACL(db, w);
		w.flush();
	}
	
	boolean isOutputXmlDecl();
	
	void setOutputXmlDecl(boolean b);
	
	boolean isOutputDoctype();
	
	void setOutputDoctype(boolean b);
	
	boolean isConvertNotesbitmapsToGIF();
	
	void setConvertNotesbitmapsToGIF(boolean b);
	
	boolean isOmitRichTextAttachments();
	
	void setOmitRichTextAttachments(boolean b);
	
	boolean isOmitOLEObjects();
	
	void setOmitOLEObjects(boolean b);
	
	boolean isOmitMiscFileObjects();
	
	void setOmitMiscFileObjects(boolean b);
	
	boolean isOmitPictures();
	
	void setOmitPictures(boolean b);
	
	boolean isUncompressAttachments();
	
	void setUncompressAttachments(boolean b);
	
	String getDxlExportResultLog();
	
	String getDefaultDoctypeSYSTEM();
	
	String getDoctypeSYSTEM();
	
	void setDoctypeSYSTEM(String docType);
	
	String getDXLBannerComments();
	
	void setDXLBannerComments(String comments);
	
	String getDxlExportResultLogComment();
	
	void setDxlExportResultLogComment(String comment);
	
	String getDxlDefaultSchemaLocation();
	
	String getDxlSchemaLocation();
	
	void setDxlSchemaLocation(String loc);
	
	String getAttachmentOmittedText();
	
	void setAttachmentOmittedText(String txt);
	
	String getOLEObjectOmittedText();
	
	void setOLEObjectOmittedText(String txt);
	
	String getPictureOmittedText();
	
	void setPictureOmittedText(String txt);
	
	List<String> getOmitItemNames();
	
	void setOmitItemNames(List<String> itemNames);
	
	List<String> getRestrictToItemNames();
	
	void setRestrictToItemNames(List<String> itemNames);
	
	/** Specifies output charset */
	public enum DXLExportCharset implements CNativeEnum {
		/** (default) "encoding =" attribute is set to utf8 and output charset is utf8 */
		UTF8(0),
		/** "encoding =" attribute is set to utf16 and charset is utf16 */
		UTF16(1);
		
		private final int value;
		DXLExportCharset(int value) {
			this.value = value;
		}
		
		@Override
		public long getLongValue() {
			return value;
		}

		@Override
		public Integer getValue() {
			return value;
		}
	}
	
	DXLExportCharset getExportCharset();
	
	/**
	 * @return an {@link Optional} describing the export character set as a {@link Charset}
	 *      or an empty one if the charset cannot be mapped
	 */
	Optional<Charset> getJDKExportCharset();
	
	void setExportCharset(DXLExportCharset charset);
	
	/** Specifies rule for exporting rich text */
	public enum DXLRichTextOption implements CNativeEnum {
		/** (default) output rich text as DXL with warning 
		   comments if uninterpretable CD records */
		DXL(0),
		/** output rich text as uninterpretted (base64'ed) item data */
		ITEMDATA(1);
		
		private final int value;
		DXLRichTextOption(int value) {
			this.value = value;
		}
		
		@Override
		public long getLongValue() {
			return value;
		}

		@Override
		public Integer getValue() {
			return value;
		}
	}
	
	DXLRichTextOption getRichTextOption();
	
	void setRichTextOption(DXLRichTextOption option);
	
	/** Specifies style of validation info emitted by exporter. Can override other settings, eg - output doctype */
	public enum DXLValidationStyle implements CNativeEnum {
		NONE(0),
		DTD(1),
		XMLSCHEMA(2);
		
		private final int value;
		DXLValidationStyle(int value) {
			this.value = value;
		}
		
		@Override
		public long getLongValue() {
			return value;
		}

		@Override
		public Integer getValue() {
			return value;
		}
	}

	DXLValidationStyle getValidationStyle();
	
	void setValidationStyle(DXLValidationStyle style);
	
	/** Specifies rule for exporting native MIME */
	public enum DXLMIMEOption implements CNativeEnum {
		/** (default) output native MIME within &lt;mime&gt; element in DXL */
		DXL(0),
		/** output MIME as uninterpretted (base64'ed) item data */
		ITEMDATA(1);
		
		private final int value;
		DXLMIMEOption(int value) {
			this.value = value;
		}
		
		@Override
		public long getLongValue() {
			return value;
		}

		@Override
		public Integer getValue() {
			return value;
		}
	}
	
	DXLMIMEOption getMIMEOption();
	
	void setMIMEOption(DXLMIMEOption option);
	
	/**
	 * @return whether this exporter is configured to use raw note format
	 * @since 1.0.26
	 */
	boolean isForceNoteFormat();
	/**
	 * @param forceNoteFormat whether this exporter should use raw note format
	 * @since 1.0.26
	 */
	void setForceNoteFormat(boolean forceNoteFormat);
	
}
