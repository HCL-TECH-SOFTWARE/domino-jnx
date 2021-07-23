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
package com.hcl.domino.mime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.mime.attachments.ByteArrayMimeAttachment;
import com.hcl.domino.mime.attachments.IMimeAttachment;
import com.hcl.domino.mime.attachments.LocalFileMimeAttachment;
import com.hcl.domino.mime.attachments.UrlMimeAttachment;

/**
 * Container for text and binary data of an item of type {@link ItemDataType#TYPE_MIME_PART}.
 * Use {@link Document#get(String, Class, Object)} with {@link MimeData} to return
 * the item value in this format or {@link Document#replaceItemValue(String, Object)} to
 * write it.
 */
public class MimeData {
	private String m_html;
	private String m_text;
	private Map<String,IMimeAttachment> m_embeds;
	private List<IMimeAttachment> m_attachments;
	private int m_uniqueCidCounter=1;
	
	private String m_toString;
	
	public MimeData() {
		this("", "", null, null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public MimeData(String html, String text,
			Map<String,IMimeAttachment> embeds, List<IMimeAttachment> attachments) {
		m_html = html;
		m_text = text;
		m_embeds = embeds==null ? new HashMap<>() : new HashMap<>(embeds);
		m_attachments = attachments==null ? new ArrayList<>() : new ArrayList<>(attachments);
	}
	
	/**
	 * Returns the html content
	 * 
	 * @return HTML, not null
	 */
	public String getHtml() {
		return m_html!=null ? m_html : ""; //$NON-NLS-1$
	}
	
	/**
	 * Sets the HTML content
	 * 
	 * @param html html
	 */
	public void setHtml(String html) {
		m_html = html;
		m_toString = null;
	}

	/**
	 * Returns alternative plaintext content
	 * 
	 * @return plaintext content or empty string
	 */
	public String getPlainText() {
		return m_text!=null ? m_text : ""; //$NON-NLS-1$
	}
	
	/**
	 * Sets the alternative plaintext content
	 * 
	 * @param text plaintext
	 */
	public void setPlainText(String text) {
		m_text = text;
		m_toString = null;
	}
	
	/**
	 * Returns an attachment for a content id
	 * 
	 * @param cid content id
	 * @return an {@link Optional} describing the attachment, or an empty one if the attachment
	 *      with that ID does not exist
	 */
	public Optional<IMimeAttachment> getEmbed(String cid) {
		return Optional.ofNullable(m_embeds.get(cid));
	}
	
	/**
	 * Adds an inline file
	 * 
	 * @param attachment attachment
	 * @return unique content id
	 */
	public String embed(IMimeAttachment attachment) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");

		//find a unique content id
		String cid;
		do {
			cid = "att_"+(m_uniqueCidCounter++)+"@jnxdoc"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		while (m_embeds.containsKey(cid));
		
		m_embeds.put(cid, attachment);
		m_toString = null;

		return cid;
	}
	
	/**
	 * Adds/changes an inline file with a given content id
	 * 
	 * @param cid content id
	 * @param attachment attachment
	 */
	public void embed(String cid, IMimeAttachment attachment) {
		if (attachment==null) {
			m_embeds.remove(cid);
		}
		else {
			m_embeds.put(cid, attachment);
		}
		m_toString = null;
	}
	
	/**
	 * Removes an inline file
	 * 
	 * @param cid content id
	 */
	public void removeEmbed(String cid) {
		m_embeds.remove(cid);
		m_toString = null;
	}
	
	/**
	 * Returns the content ids for all inline files.
	 * 
	 * @return content ids
	 */
	public Iterable<String> getContentIds() {
		return m_embeds.keySet();
	}

	/**
	 * Attaches a file. We provide several implementations for
	 * {@link IMimeAttachment}, e.g. {@link LocalFileMimeAttachment},
	 * {@link ByteArrayMimeAttachment} or {@link UrlMimeAttachment} or
	 * you can add your own implementation. When reading {@link MimeData}
	 * from a document, we transform the attachment to an internal class
	 * that returns the same filename, content type and binary content.
	 * 
	 * @param attachment attachment
	 */
	public void attach(IMimeAttachment attachment) {
		m_attachments.add(attachment);
		m_toString = null;
	}
	
	public List<IMimeAttachment> getAttachments() {
		return Collections.unmodifiableList(m_attachments);
	}

	/**
	 * Removes an attachment from the MIME data
	 * 
	 * @param attachment attachment to remove
	 */
	public void removeAttachment(IMimeAttachment attachment) {
		m_attachments.remove(attachment);
		m_toString = null;
	}

	@Override
	public String toString() {
		if (m_toString==null) {
			m_toString = "MimeData [hasHtml="+(m_html!=null && m_html.length()>0) //$NON-NLS-1$
					+", hasText=" + (m_text!=null && m_text.length()>0) //$NON-NLS-1$
					+ ", embeds="+m_embeds.keySet() //$NON-NLS-1$
					+ ", attachments="+ //$NON-NLS-1$
					m_attachments.stream().map((att) -> {
						try {
							return att.getFileName();
						} catch (IOException e) {
							return "-error-"; //$NON-NLS-1$
						}
					}).collect(Collectors.toList())+"]"; //$NON-NLS-1$
			
		}
		return m_toString;
	}
}
