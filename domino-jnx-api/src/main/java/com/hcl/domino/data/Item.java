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
package com.hcl.domino.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.mime.MimeEntity;
import com.hcl.domino.misc.INumberEnum;

/**
 * Carefull attention for date/time and the new Java Stuff and JNA TimeDate
 *
 * @author t.b.d
 * @since 0.5.0
 */
public interface Item extends IAdaptable {

  /**
   * Enum for the field flags for an Item
   */
  public enum ItemFlag implements INumberEnum<Integer> {
    SUMMARY(0x0004),
    NAMES(0x0040),
    READERS(0x0400),
    READWRITERS(0x0020),
    PROTECTED(0x0200),
    ENCRYPTED(0x0002),
    SIGNED(0x0001),
    /**
     * Special flag to keep \n in string item values instead of replacing them with
     * \0
     * (e.g. required when writing $$FormScript in design elements)
     */
    KEEPLINEBREAKS(0x8000000);

    private final Integer m_val;

    ItemFlag(final Integer val) {
      this.m_val = val;
    }

    @Override
    public long getLongValue() {
      return this.m_val & 0xffff;
    }

    @Override
    public Integer getValue() {
      return this.m_val;
    }
  }

  /**
   * This function converts the input {@link ItemDataType#TYPE_RFC822_TEXT} item
   * in an open document
   * to its pre-V5 equivalent; i.e. to {@link ItemDataType#TYPE_TEXT},
   * {@link ItemDataType#TYPE_TEXT_LIST},
   * or {@link ItemDataType#TYPE_TIME}.<br>
   * <br>
   * For example, we convert the PostedDate {@link ItemDataType#TYPE_RFC822_TEXT}
   * item to a
   * {@link ItemDataType#TYPE_TIME} item.<br>
   * <br>
   * It does not update the Domino database; to update the database, call
   * {@link Document#save()}.<br>
   *
   * @return this item instance (for chaining)
   */
  Item convertRFC822TextItem();

  /**
   * Copies this item to a new document, retaining the current name.
   * TODO: If overwrite is false, does it create a duplicate item or abort?
   *
   * @param doc       target document for the new item
   * @param overwrite true to replace any existing item with the same name
   */
  void copyToDocument(Document doc, boolean overwrite);

  /**
   * Copies this item to a new document, with a new Item name.
   * TODO: If overwrite is false, does it create a duplicate item or abort?
   *
   * @param doc         target document for the new item
   * @param newItemName new Item name to save as
   * @param overwrite   true to replace any existing item with the same name
   */
  void copyToDocument(Document doc, String newItemName, boolean overwrite);

  /**
   * Returns the item value converted to the specified data type.<br>
   * <br>
   * We currently support the following value types out of the box:<br>
   * <ul>
   * <li>{@link String}</li>
   * <li>{@link Integer}</li>
   * <li>{@link Long}</li>
   * <li>{@link Double}</li>
   * <li>{@link DominoDateTime}</li>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link TemporalAccessor} (returned as {@link DominoDateTime})</li>
   * </ul>
   * <br>
   * Additional value types are supported by implementing and registering
   * {@link DocumentValueConverter} as Java services.
   *
   * @param <T>          type of return value
   * @param valueType    class of return value
   * @param defaultValue default value to be returned of object property is not
   *                     set
   * @return return value
   * @throws IllegalArgumentException if the specified value type is unsupported
   */
  <T> T get(Class<T> valueType, T defaultValue);

  /**
   * Returns a list of item values converted to the specified data type.<br>
   * <br>
   * We currently support the following value types out of the box:<br>
   * <ul>
   * <li>{@link String}</li>
   * <li>{@link Integer}</li>
   * <li>{@link Long}</li>
   * <li>{@link Double}</li>
   * <li>{@link DominoDateTime}</li>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link TemporalAccessor} (returned as {@link DominoDateTime})</li>
   * </ul>
   * <br>
   * Additional value types are supported by implementing and registering
   * {@link DocumentValueConverter} as Java services.
   *
   * @param <T>          type of return value
   * @param valueType    class of return value
   * @param defaultValue default value to be returned of object property is not
   *                     set
   * @return value list
   * @throws IllegalArgumentException if the specified value type is unsupported
   */
  <T> List<T> getAsList(Class<T> valueType, List<T> defaultValue);

  /**
   * Gets all the field flags corresponding to the Item
   *
   * @return field flags applied, e.g. {SUMMARY,READERS}
   */
  Set<ItemFlag> getFlags();

  /**
   * Retrieves the item content as a MIME entity, if this is a MIME item.
   *
   * @return an {@link Optional} describing a {@link MimeEntity} for the item
   *         content, or
   *         an empty one if this is not a MIME item
   */
  Optional<MimeEntity> getMimeEntity();

  /**
   * Gets the field name. Fields beginning "$" are, by convention, system fields
   *
   * @return the field name as a String
   */
  String getName();

  /**
   * Extracts the key elements available in the Notes Client Document Properties
   * window,
   * so they can be made available in a serializable format
   *
   * @return a {@link DocumentProperties} object representing the document's meta
   *         properties
   */
  DocumentProperties getProperties();

  /**
   * Incremental integer for the nth save this Item was last updated.
   * Cross-reference with the $Revisions field for the date and time it was saved.
   * Be aware that for very frequently updating documents, the integer will cycle
   * back to 1
   *
   * @return integer corresponding to the nth time the document was saved
   */
  int getSequenceNumber();

  /**
   * Gets the data type for a field, converting it to the more readable enum
   *
   * @return enum corresponding to the internal int value for the data type,
   *         or {@link ItemDataType#TYPE_INVALID_OR_UNKNOWN} if it is unknown
   */
  ItemDataType getType();

  /**
   * Gets the data type for a field, as the internal int
   *
   * @return internal int value corresponding to the data type
   */
  int getTypeValue();

  /**
   * Decodes the item value(s). The data is always returned as a list even though
   * the list may contain only one element (e.g. for
   * {@link ItemDataType#TYPE_TEXT}.
   * <br>
   * The following data types are currently supported:<br>
   * <ul>
   * <li>{@link ItemDataType#TYPE_TEXT} - List with String object</li>
   * <li>{@link ItemDataType#TYPE_TEXT_LIST} - List of String objects</li>
   * <li>{@link ItemDataType#TYPE_NUMBER} - List with Double object</li>
   * <li>{@link ItemDataType#TYPE_NUMBER_RANGE} - List of Double objects</li>
   * <li>{@link ItemDataType#TYPE_TIME} - List with Calendar object</li>
   * <li>{@link ItemDataType#TYPE_TIME_RANGE} - List of Calendar objects</li>
   * <li>{@link ItemDataType#TYPE_OBJECT} with the subtype Attachment (e.g. $File
   * items) - List with {@link Attachment} object</li>
   * <li>{@link ItemDataType#TYPE_NOTEREF_LIST} - List with one UNID</li>
   * <li>{@link ItemDataType#TYPE_UNAVAILABLE} - returns an empty list</li>
   * </ul>
   *
   * @return a {@link List} of objects representing the item value
   */
  List<Object> getValue();

  /**
   * Returns the value length in bytes
   *
   * @return length
   */
  int getValueLength();

  /**
   * Whether or not the Item requires additional encryption key in addition to
   * database-level
   * and document-level Reader access in order to see the content.
   *
   * @return true if the Item is encrypted
   */
  boolean isEncrypted();

  /**
   * Whether or not the NAMES field flag is applied on this Item.
   * NAMES fields are used to store Domino usernames and these fields can be
   * checked
   * by the server's Administration Process (adminp) to update if the username
   * changes
   * or the user is removed.
   *
   * @return true for NAMES Items
   */
  boolean isNames();

  /**
   * Whether or not the Item prevents access if the person only has Author access
   * to the database.
   * If so, the developer will need to call {@link #setProtected(boolean)
   * setProtected(false)} before making a change, and
   * set it back to true afterwards.
   * <code>
   * Item itm = doc.getFirstItem("Foo");
   * if (itm.isProtected()) {
   *     itm.setProtected(false);
   *     doc.replaceItemValue("Foo", "bar");
   *     itm.setProtected(true);
   * }
   * </code>
   *
   * @return true if editor access or above is required to edit the Item
   */
  boolean isProtected();

  /**
   * Whether or not the READERS field flag is applied on this Item.
   * READERS fields are used to store Domino usernames and restrict read access to
   * documents.
   * These fields can be checked by the server's Administration Process (adminp)
   * to update
   * if the username changes or the user is removed.
   *
   * @return true for READERS Items
   */
  boolean isReaders();

  /**
   * Whether or not the READWRITERS field flag is applied on this Item.
   * READWRITERS fields are used to store Domino usernames and restrict saving
   * changes to documents
   * if the person only has Author access to the database.
   * These fields can be checked by the server's Administration Process (adminp)
   * to update
   * if the username changes or the user is removed.
   *
   * @return true for READWRITERS Items
   */
  boolean isReadWriters();

  /**
   * Whether or not the Item will be signed next time the document is saved.
   * TODO: Under what circumstances would this be true and under what
   * circumstances would it be false?
   * i.e. Why shouldn't a developer just ignore this?
   *
   * @return true if the Item will be signed
   */
  boolean isSigned();

  /**
   * Whether or not the SUMMARY field flag is applied on this Item.
   * A document can only have a maximum 64Mb total SUMMARY field data
   * Only SUMMARY items can be displayed in a View's column as a raw field value.
   * Some data types, e.g. TYPE_MIME_PART, cannot be SUMMARY.
   *
   * @return true for SUMMARY Items
   */
  boolean isSummary();

  /**
   * Remove this item from the parent document
   */
  void remove();

  /**
   * Change whether or not the Item requires additional encryption key in addition
   * to database-level
   * and document-level Reader access in order to see the content.
   *
   * @param isEncrypted true if the Item is encrypted
   */
  void setEncrypted(boolean isEncrypted);

  /**
   * Sets the NAMES field flag on this Item.
   * NAMES fields are used to store Domino usernames and these fields can be
   * checked
   * by the server's Administration Process (adminp) to update if the username
   * changes
   * or the user is removed.
   *
   * @param isNames true to enable NAMES flag on the Item
   */
  void setNames(boolean isNames);

  /**
   * Change whether or not the Item prevents access if the person only has Author
   * access to the database.
   * If it is already protected and the current user has only Author access,
   * the developer will need to call {@link #setProtected(boolean)
   * setProtected(false)} before making a change, and
   * set it back to true afterwards.
   * <code>
   * Item itm = doc.getFirstItem("Foo");
   * if (itm.isProtected()) {
   *     itm.setProtected(false);
   *     doc.replaceItemValue("Foo", "bar");
   *     itm.setProtected(true);
   * }
   * </code>
   *
   * @param isProtected true if editor access or above is required to edit the
   *                    Item
   */
  void setProtected(boolean isProtected);

  /**
   * Sets the READERS field flag on this Item.
   * READERS fields are used to store Domino usernames and restrict read access to
   * documents.
   * These fields can be checked by the server's Administration Process (adminp)
   * to update
   * if the username changes or the user is removed.
   *
   * @param isReaders true to enable READERS flag on the Item
   */
  void setReaders(boolean isReaders);

  /**
   * Sets the READWRITERS field flag on this Item.
   * READWRITERS fields are used to store Domino usernames and restrict saving
   * changes to documents
   * if the person only has Author access to the database.
   * These fields can be checked by the server's Administration Process (adminp)
   * to update
   * if the username changes or the user is removed.
   *
   * @param isReadWriters true to enable READWRITERS flag on the Item
   */
  void setReadWriters(boolean isReadWriters);

  /**
   * Change whether or not the Item will be signed next time the document is
   * saved.
   * TODO: Under what circumstances would this be true and under what
   * circumstances would it be false?
   * i.e. Why shouldn't a developer just ignore this?
   *
   * @param isSigned true if the Item will be signed
   */
  void setSigned(boolean isSigned);

  /**
   * Changes the summary field flag
   *
   * @param b new flag value
   */
  void setSummary(boolean b);

}
