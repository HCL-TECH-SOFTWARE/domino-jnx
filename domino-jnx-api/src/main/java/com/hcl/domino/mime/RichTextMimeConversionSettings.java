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

import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.misc.INumberEnum;

/**
 * Settings to control the rich text to MIME conversion
 */
public interface RichTextMimeConversionSettings extends IAdaptable {

  public enum AttachmentEncoding implements INumberEnum<Short> {
    BASE64(1), QUOTEDPRINTABLE(2), UUENCODE(3), BINHEX40(4);

    private final short value;

    AttachmentEncoding(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  public enum MessageContentEncoding implements INumberEnum<Short> {
    /** text/plain (w/o images and attachments) */
    TEXT_PLAIN_NO_IMAGES_ATTACHMENTS(0),
    /** text/plain (w/images and attachments) */
    TEXT_PLAIN_WITH_IMAGES_ATTACHMENTS(1),
    /** text/html (w/images and attachments) */
    TEXT_HTML_WITH_IMAGES_ATTACHMENTS(2),
    /**
     * multipart/alternative: text/plain and text/html (w/images and attachments)
     */
    TEXT_PLAIN_AND_HTML_WITH_IMAGES_ATTACHMENTS(3);

    private final short value;

    MessageContentEncoding(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  public enum ReadReceipt implements INumberEnum<Short> {
    /** Do not pass read receipt requests when importing or exporting */
    DONT_PASS_READ_RECEIPT(0),
    /** Support read receipt requests (as Return-Receipt-To when exporting) */
    RETURN_RECEIPT_TO(1),
    /**
     * Support read receipt requests (as Disposition-Notification-To when exporting)
     */
    DISPOSITION_NOTIFICATION_TO(2);

    private final short value;

    ReadReceipt(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  public enum Typeface implements INumberEnum<Short> {
    TIMESROMAN(0), HELVETICA(1), COURIER(2);

    private final short value;

    Typeface(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  /**
   * The function returns the Conversions Controls 'add items setting' setting.
   *
   * @return list of items to add during export
   */
  List<String> getAddItems();

  /**
   * The function returns the Conversions Controls 'attachment encoding' setting.
   *
   * @return an {@link Optional} describing the attachment encoding, or an empty
   *         one
   *         if known return value
   */
  Optional<AttachmentEncoding> getAttachmentEncoding();

  /**
   * The function returns the Conversions Controls 'drop items' setting.
   *
   * @return list of items to drop during export
   */
  List<String> getDropItems();

  /**
   * The function returns the current Conversions Controls 'message content
   * encoding' setting.
   *
   * @return an {@link Optional} describing the encoding, or an empty one if
   *         unknown value received
   */
  Optional<MessageContentEncoding> getMessageContentEncoding();

  /**
   * The function returns the Conversions Controls 'point size' setting.
   *
   * @return point size
   */
  int getPointSize();

  /**
   * The function returns the Conversions Controls 'read receipt' setting.
   *
   * @return an {@link Optional} describing the read receipt, or an empty one if
   *         we received
   *         an unknown value
   */
  Optional<ReadReceipt> getReadReceipt();

  /**
   * The function returns the Conversions Controls 'skip X-Notes-Item headers'
   * setting.
   *
   * @return if TRUE, don't export any headers named x-notes-item (default FALSE)
   */
  boolean getSkipX();

  /**
   * The function returns the Conversions Controls 'typeface' setting.
   *
   * @return an {@link Optional} describing the typeface, or an empty one if we
   *         received
   *         an unknown value
   */
  Optional<Typeface> getTypeface();

  /**
   * The function returns the Conversions Controls 'keep tabs' setting.
   *
   * @return true to keep tabs
   */
  boolean isKeepTabs();

  /**
   * The function sets the Conversions Controls 'add items setting' setting to the
   * input value.
   *
   * @param itemNames item names, list of item names to preserve in msgs as they
   *                  are exported (default is empty list)
   * @return this instance
   */
  RichTextMimeConversionSettings setAddItems(List<String> itemNames);

  /**
   * The function sets the Conversions Controls 'attachment encoding' setting to
   * the input value.<br>
   *
   * @param encoding new encoding
   * @return this instance
   */
  RichTextMimeConversionSettings setAttachmentEncoding(AttachmentEncoding encoding);

  /**
   * The function sets Conversion Controls configuration settings to their default
   * values.
   *
   * @return this instance
   */
  RichTextMimeConversionSettings setDefaults();

  /**
   * The function sets the Conversions Controls 'drop items setting' setting to
   * the input value,
   * a list of items to drop during export.
   *
   * @param itemNames item names
   * @return this instance
   */
  RichTextMimeConversionSettings setDropItems(List<String> itemNames);

  /**
   * The function sets the Conversions Controls 'keep tabs' setting to the input
   * value.
   *
   * @param b true to keep tabs
   * @return this instance
   */
  RichTextMimeConversionSettings setKeepTabs(boolean b);

  /**
   * The function sets the Conversions Controls 'message content encoding' setting
   * to the input value.
   *
   * @param enc encoding
   * @return this instance
   */
  RichTextMimeConversionSettings setMessageContentEncoding(MessageContentEncoding enc);

  /**
   * The function sets the Conversions Controls 'point size' setting to the input
   * value.
   *
   * @param size new size, one of: 6, 8, 9, 10 (default), 12, 14, 18, 24
   * @return this instance
   */
  RichTextMimeConversionSettings setPointSize(int size);

  /**
   * The function sets the Conversions Controls read receipt setting to the input
   * value
   *
   * @param rc read receipt
   * @return this instance
   */
  RichTextMimeConversionSettings setReadReceipt(ReadReceipt rc);

  /**
   * The function sets the Conversions Controls 'skip X-Notes-Item headers'
   * setting to the input value.
   *
   * @param b if TRUE, don't export any headers named x-notes-item (default FALSE)
   * @return this instance
   */
  RichTextMimeConversionSettings setSkipX(boolean b);

  /**
   * The function sets the Conversions Controls 'typeface' setting to the input
   * value.
   *
   * @param tf new typeface
   * @return this instance
   */
  RichTextMimeConversionSettings setTypeface(Typeface tf);

}
