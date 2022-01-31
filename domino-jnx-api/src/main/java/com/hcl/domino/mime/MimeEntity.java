/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.ParseException;

/**
 * Represents a MIME entity stored in a document, which may be standalone or
 * part of a
 * tree.
 */
public interface MimeEntity {
  public enum Encoding {
    NONE, QUOTED_PRINTABLE, BASE64, IDENTITY_7BIT, IDENTITY_8BIT, BINARY, EXTENSION
  }

  /**
   * Adds a header with the provied {@code headerName} and blank content.
   *
   * @param headerName the name of the header to create
   * @return the newly-created {@link MimeHeader}
   */
  MimeHeader addHeader(String headerName);

  /**
   * Adds a header with the provied {@code headerName} and blank with the
   * provided value.
   * <p>
   * {@code value} is treated as the entire content of the header, and may contain
   * parameters.
   * </p>
   *
   * @param headerName the name of the header to create
   * @param value      the value of the header to set
   * @return the newly-created {@link MimeHeader}
   */
  MimeHeader addHeader(String headerName, String value);

  /**
   * Append a child entity beneath this entity, with an auto-generated boundary.
   * <p>
   * If the provided entity already has a parent entity, it is removed from that
   * tree.
   * </p>
   * <p>
   * If this entity does not currently have a {@code multipart} content type, this
   * method sets the content type to {@code multipart/mixed} and any existing
   * content
   * is removed.
   * </p>
   *
   * @param entity the entity to append
   */
  // TODO consider whether this makes sense to do - it's good conceptually, but
  // the immediate
  // C API calls for manipulating note-based MIME entities don't work with them in
  // quite
  // this way
  void appendChildEntity(MimeEntity entity);

  /**
   * Creates a child entity beneath this entity, with an auto-generated boundary.
   * <p>
   * If this entity does not currently have a {@code multipart} content type, this
   * method sets the content type to {@code multipart/mixed} and any existing
   * content
   * is removed.
   * </p>
   *
   * @return the newly-created child entity
   */
  MimeEntity createChildEntity();

  /**
   * Creates a child entity beneath this entity and placed immediately before the
   * provided
   * entity, with an auto-generated boundary.
   * <p>
   * If this entity does not currently have a {@code multipart} content type, this
   * method sets the content type to {@code multipart/mixed} and any existing
   * content
   * is removed.
   * </p>
   *
   * @param sibling the entity in front of which to place the new child
   * @return the newly-created child entity
   * @throws IllegalArgumentException if {@code sibling} is not a child of the
   *                                  current entity
   */
  MimeEntity createChildEntity(MimeEntity sibling);

  /**
   * Retrieves the boundary value used to separate children within this multipart
   * entity
   *
   * @return an {@link Optional} describing the multipart boundary string, or an
   *         empty one
   *         if this is not a multipart entity
   */
  Optional<String> getBoundary();

  /**
   * Returns a mutable {@link Iterable} view of the children of this multipart
   * entity's
   * children.
   * <p>
   * Calling {@link #createChildEntity(MimeEntity)} will invalidate any objects
   * previously
   * returned by this method.
   * </p>
   *
   * @return an {@link Optional} describing a mutable {@link Iterable}, or an
   *         empty one if
   *         this is not a multipart entity
   */
  Optional<Iterable<MimeEntity>> getChildren();

  /**
   * Retrieves the content of this entity as a string. If the entity's content is
   * encoded, this decodes it.
   *
   * @return an {@link Optional} describing the content of the entity as a
   *         {@link String}, or
   *         an empty one if this is a multipart entity
   */
  // TODO consider whether this should promise to handle any Content-Encoding,
  // etc. headers
  Optional<String> getContentAsString();

  // *******************************************************************************
  // * Multipart
  // *******************************************************************************

  /**
   * Retrieves and parses the {@code Content-Type} header of this entity,
   * if present.
   *
   * @return an {@link Optional} describing the {@link ContentType} object, or
   *         an empty one if no header is set
   * @throws ParseException if the {@code Content-Type} header is present and
   *                        invalid
   */
  default Optional<ContentType> getContentType() throws ParseException {
    final List<MimeHeader> h = this.getHeaders("Content-Type"); //$NON-NLS-1$
    if (h != null && !h.isEmpty()) {
      return Optional.of(new ContentType(h.get(0).getValue()));
    }
    return Optional.empty();
  }

  /**
   * @return the encoding used to store the MIME content
   */
  Encoding getEncoding();

  /**
   * Returns a list of all headers for this entity.
   *
   * @return an immutable {@link List} of {@link MimeHeader}s
   */
  List<MimeHeader> getHeaders();

  /**
   * Returns a list of all headers for this entity that match the provided
   * {@code headerName}.
   *
   * @param headerName the name of the headers to retrieve
   * @return an immutable {@link List} of {@link MimeHeader}s
   */
  List<MimeHeader> getHeaders(String headerName);

  /**
   * Retrieves the content of the entity as an {@link InputStream}. If the
   * entity's content is
   * encoded, this decodes it.
   * <p>
   * This is equivalent to calling {@code getInputStream(true)}.
   * </p>
   *
   * @return an {@link Optional} describing a {@link InputStream} of the content
   *         of the entity,
   *         decoded from storage format, or an empty one if this is a multipart
   *         entity
   */
  default Optional<InputStream> getInputStream() {
    return this.getInputStream(true);
  }

  /**
   * Retrieves the content of the entity as an {@link InputStream}. If the
   * entity's content is
   * encoded, this decodes it if {@code decode} is {@code true} and leaves it
   * encoded if it is
   * {@code false}.
   * <p>
   * This is equivalent to calling {@code getInputStream(true)}.
   * </p>
   *
   * @param decode whether the content should be decoded from its storage format
   * @return an {@link Optional} describing a {@link InputStream} of the content
   *         of the entity,
   *         optionally decoded from storage format, or an empty one if this is a
   *         multipart entity
   */
  Optional<InputStream> getInputStream(boolean decode);

  /**
   * Retrieves the parent of this entity, if it is part of a multipart tree.
   *
   * @return an {@link Optional} describing the the parent {@link MimeEntity}, or
   *         an empty one if this entity has no parent
   */
  Optional<MimeEntity> getParent();

  /**
   * Gets the preamble of this multipart entity.
   *
   * @return an {@link Optional} describing the preamble text of the entity,
   *         an {@code Optional} empty string if there is no preamble, or an empty
   *         {@code Optional} if this is not a multipart entity
   */
  Optional<String> getPreamble();

  /**
   * Retrieves a {@link Reader} for the text content of this entity. If the entity
   * is encoded,
   * this decodes it. Additionally, this reader will obey any {@code charset}
   * parameter in the
   * entity's {@code Content-Type} header, if present.
   *
   * @return an {@link Optional} describing a {@link Reader} for the content of
   *         this entity as text,
   *         or an empty one if this is a multipart entity
   */
  Optional<Reader> getReader();

  // *******************************************************************************
  // * Headers
  // *******************************************************************************

  /**
   * @return {@code true} if this entity is a {@code multipart} type
   * @throws ParseException if the {@code Content-Type} header is present and
   *                        invalid
   */
  default boolean isMultipart() throws ParseException {
    return this.getContentType()
        .map(ct -> "multipart".equals(ct.getPrimaryType())) //$NON-NLS-1$
        .orElse(false);
  }

  /**
   * Removes the provided child entity from this multipart entity.
   *
   * @param child the child {@link MimeEntity} to remove
   */
  void removeChildEntity(MimeEntity child);

  /**
   * Removes all headers of the given name from this entity.
   *
   * @param headerName the name of the headers to remove
   */
  void removeHeaders(String headerName);

  /**
   * Sets the content of the MIME entity from the provided {@link InputStream},
   * applying
   * the requested storage encoding.
   * <p>
   * Note: the content of the stream should not be already encoded.
   * </p>
   *
   * @param is       an {@link InputStream} to read; cannot be {@code null}
   * @param encoding the {@link Encoding} value to use
   * @throws IllegalStateException if this is a multipart entity
   */
  void setContent(InputStream is, Encoding encoding);

  /**
   * Sets the preamble of this multipart entity.
   *
   * @param preamble the text of the preamble to set, or {@code null} to clear an
   *                 existing
   *                 preamble
   * @throws IllegalStateException if this is not a multipart entity
   */
  void setPreamble(String preamble);
}
