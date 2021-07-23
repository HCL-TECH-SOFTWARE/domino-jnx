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
 * Container for text and binary data of an item of type
 * {@link ItemDataType#TYPE_MIME_PART}.
 * Use {@link Document#get(String, Class, Object)} with {@link MimeData} to
 * return
 * the item value in this format or
 * {@link Document#replaceItemValue(String, Object)} to
 * write it.
 */
public class MimeData {
  private String m_html;
  private String m_text;
  private final Map<String, IMimeAttachment> m_embeds;
  private final List<IMimeAttachment> m_attachments;
  private int m_uniqueCidCounter = 1;

  private String m_toString;

  public MimeData() {
    this("", "", null, null); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public MimeData(final String html, final String text,
      final Map<String, IMimeAttachment> embeds, final List<IMimeAttachment> attachments) {
    this.m_html = html;
    this.m_text = text;
    this.m_embeds = embeds == null ? new HashMap<>() : new HashMap<>(embeds);
    this.m_attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
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
  public void attach(final IMimeAttachment attachment) {
    this.m_attachments.add(attachment);
    this.m_toString = null;
  }

  /**
   * Adds an inline file
   *
   * @param attachment attachment
   * @return unique content id
   */
  public String embed(final IMimeAttachment attachment) {
    Objects.requireNonNull(attachment, "Attachment cannot be null");

    // find a unique content id
    String cid;
    do {
      cid = "att_" + this.m_uniqueCidCounter++ + "@jnxdoc"; //$NON-NLS-1$ //$NON-NLS-2$
    } while (this.m_embeds.containsKey(cid));

    this.m_embeds.put(cid, attachment);
    this.m_toString = null;

    return cid;
  }

  /**
   * Adds/changes an inline file with a given content id
   *
   * @param cid        content id
   * @param attachment attachment
   */
  public void embed(final String cid, final IMimeAttachment attachment) {
    if (attachment == null) {
      this.m_embeds.remove(cid);
    } else {
      this.m_embeds.put(cid, attachment);
    }
    this.m_toString = null;
  }

  public List<IMimeAttachment> getAttachments() {
    return Collections.unmodifiableList(this.m_attachments);
  }

  /**
   * Returns the content ids for all inline files.
   *
   * @return content ids
   */
  public Iterable<String> getContentIds() {
    return this.m_embeds.keySet();
  }

  /**
   * Returns an attachment for a content id
   *
   * @param cid content id
   * @return an {@link Optional} describing the attachment, or an empty one if the
   *         attachment
   *         with that ID does not exist
   */
  public Optional<IMimeAttachment> getEmbed(final String cid) {
    return Optional.ofNullable(this.m_embeds.get(cid));
  }

  /**
   * Returns the html content
   *
   * @return HTML, not null
   */
  public String getHtml() {
    return this.m_html != null ? this.m_html : ""; //$NON-NLS-1$
  }

  /**
   * Returns alternative plaintext content
   *
   * @return plaintext content or empty string
   */
  public String getPlainText() {
    return this.m_text != null ? this.m_text : ""; //$NON-NLS-1$
  }

  /**
   * Removes an attachment from the MIME data
   *
   * @param attachment attachment to remove
   */
  public void removeAttachment(final IMimeAttachment attachment) {
    this.m_attachments.remove(attachment);
    this.m_toString = null;
  }

  /**
   * Removes an inline file
   *
   * @param cid content id
   */
  public void removeEmbed(final String cid) {
    this.m_embeds.remove(cid);
    this.m_toString = null;
  }

  /**
   * Sets the HTML content
   *
   * @param html html
   */
  public void setHtml(final String html) {
    this.m_html = html;
    this.m_toString = null;
  }

  /**
   * Sets the alternative plaintext content
   *
   * @param text plaintext
   */
  public void setPlainText(final String text) {
    this.m_text = text;
    this.m_toString = null;
  }

  @Override
  public String toString() {
    if (this.m_toString == null) {
      this.m_toString = "MimeData [hasHtml=" + (this.m_html != null && this.m_html.length() > 0) //$NON-NLS-1$
          + ", hasText=" + (this.m_text != null && this.m_text.length() > 0) //$NON-NLS-1$
          + ", embeds=" + this.m_embeds.keySet() //$NON-NLS-1$
          + ", attachments=" + //$NON-NLS-1$
          this.m_attachments.stream().map(att -> {
            try {
              return att.getFileName();
            } catch (final IOException e) {
              return "-error-"; //$NON-NLS-1$
            }
          }).collect(Collectors.toList()) + "]"; //$NON-NLS-1$

    }
    return this.m_toString;
  }
}
