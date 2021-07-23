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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * Writer to produce a new rich text item
 */
public interface RichTextWriter extends AutoCloseable {

  /**
   * Enum of available positions to place the caption of a file hotspot
   *
   * @author Karsten Lehmann
   */
  public enum CaptionPosition {
    BELOWCENTER,
    MIDDLECENTER
  }

  RichTextWriter addAttachmentIcon(Attachment att, String captionTxt);

  RichTextWriter addAttachmentIcon(Attachment att, String filenameToDisplay, String captionText, FontStyle captionStyle,
      CaptionPosition captionPos, int captionColorRed, int captionColorGreen, int captionColorBlue,
      int resizeToWidth, int resizeToHeight, Path imagePath);

  RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String captionTxt);

  RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String filenameToDisplay, String captionText,
      FontStyle captionStyle, CaptionPosition captionPos, int captionColorRed, int captionColorGreen,
      int captionColorBlue, int resizeToWidth, int resizeToHeight, InputStream imageData) throws IOException;

  /**
   * This function inserts a DocLink for the specified {@link DominoCollection}.
   *
   * @param collection collection to create the link
   * @param comment    This string appears when the DocLink is selected (clicked
   *                   on).
   * @return this writer
   */
  RichTextWriter addCollectionLink(DominoCollection collection, String comment);

  /**
   * This function inserts a DocLink for the specified {@link Database}.
   *
   * @param db      database to create the link
   * @param comment This string appears when the DocLink is selected (clicked on).
   * @return this writer
   */
  RichTextWriter addDatabaseLink(Database db, String comment);

  /**
   * This function inserts a DocLink for the specified {@link Document}.
   *
   * @param doc     document to create the link
   * @param comment This string appears when the DocLink is selected (clicked on).
   * @return this writer
   */
  RichTextWriter addDocLink(Document doc, String comment);

  /**
   * This function inserts a DocLink using manual values.
   *
   * @param dbReplicaId Replica ID of the database that contains the document
   *                    pointed to by the DocLink.
   * @param viewUnid    UNID of the {@link DominoCollection} that contains the
   *                    document pointed to by the DocLink or null/empty string to
   *                    create a database link
   * @param docUNID     UNID of the document pointed to by the DocLink or
   *                    null/empty string to create a view/collection link
   * @param comment     This string appears when the DocLink is selected (clicked
   *                    on).
   * @return this writer
   */
  RichTextWriter addDocLink(String dbReplicaId, String viewUnid, String docUNID, String comment);

  /**
   * Adds the provided stream as a file resource into this rich text writer.
   * <p>
   * If the length of the content is unknown, pass {@code -1} for the
   * {@code length} parameter.
   * This is inefficient, however, as it will cause the underlying implementation
   * to read the
   * entire stream at the start instead of as needed.
   * </p>
   * <p>
   * Note: this uses the structure of file-resource design elements and differs
   * significantly
   * from how attachments are handled. Use
   * {@link #addAttachmentIcon(Attachment, String)} and the
   * related methods to add attachments to a rich-text field.
   * </p>
   *
   * @param is     the {@link InputStream} to write to the rich text buffer
   * @param length the length of the file, if known; {@code -1} otherwise
   * @return this writer
   * @throws IOException if there is a problem reading the file data
   * @since 1.0.15
   */
  RichTextWriter addFileResource(InputStream is, long length) throws IOException;

  /**
   * Adds the provided file as a file resource into this rich text writer.
   * <p>
   * Note: this uses the structure of file-resource design elements and differs
   * significantly
   * from how attachments are handled. Use
   * {@link #addAttachmentIcon(Attachment, String)} and the
   * related methods to add attachments.
   * </p>
   *
   * @param file the file to write to the rich text buffer
   * @return this writer
   * @throws IOException if there is a problem reading the file data
   * @since 1.0.15
   */
  default RichTextWriter addFileResource(final Path file) throws IOException {
    final long len = Files.size(file);
    try (InputStream is = Files.newInputStream(file)) {
      return this.addFileResource(is, len);
    }
  }

  /**
   * Adds an image from an input stream. We support BMP, JPG, GIF and PNG format.
   *
   * @param imageStream stream with image data
   * @return this writer
   */
  RichTextWriter addImage(InputStream imageStream);

  /**
   * Adds an image from an input stream. We support BMP, JPG, GIF and PNG format.
   *
   * @param imageStream    stream with image data
   * @param resizeToWidth  if not -1, resize the image to this width
   * @param resizeToHeight if not -1, resize the image to this width
   * @return this writer
   */
  RichTextWriter addImage(InputStream imageStream, int resizeToWidth, int resizeToHeight);

  /**
   * Adds an image from disk. We support BMP, JPG, GIF and PNG format.
   *
   * @param imagePath image filepath
   * @return this writer
   */
  RichTextWriter addImage(Path imagePath);

  /**
   * Adds an image from disk. We support BMP, JPG, GIF and PNG format.
   *
   * @param imagePath      image filepath
   * @param resizeToWidth  if not -1, resize the image to this width
   * @param resizeToHeight if not -1, resize the image to this width
   * @return this writer
   */
  RichTextWriter addImage(Path imagePath, int resizeToWidth, int resizeToHeight);

  /**
   * Adds the provided stream as an image resource into this rich text writer.
   * <p>
   * If the length of the content is unknown, pass {@code -1} for the
   * {@code length} parameter.
   * This is inefficient, however, as it will cause the underlying implementation
   * to read the
   * entire stream at the start instead of as needed.
   * </p>
   * <p>
   * Note: this uses the structure of image-resource design elements and differs
   * significantly
   * from how inline images are handled. Use {@link #addImage(InputStream)} and
   * the
   * related methods to add images to a UI rich-text field.
   * </p>
   *
   * @param is     the {@link InputStream} to write to the rich text buffer
   * @param length the length of the file, if known; {@code -1} otherwise
   * @return this writer
   * @throws IOException if there is a problem reading the file data
   * @since 1.0.15
   */
  RichTextWriter addImageResource(InputStream is, long length) throws IOException;

  /**
   * Adds the provided stream as an image resource into this rich text writer.
   * <p>
   * Note: this uses the structure of image-resource design elements and differs
   * significantly
   * from how inline images are handled. Use {@link #addImage(Path)} and the
   * related methods to add images to a UI rich-text field.
   * </p>
   *
   * @param file the file to write to the rich text buffer
   * @return this writer
   * @throws IOException if there is a problem reading the file data
   * @since 1.0.15
   */
  default RichTextWriter addImageResource(final Path file) throws IOException {
    final long len = Files.size(file);
    try (InputStream is = Files.newInputStream(file)) {
      return this.addImageResource(is, len);
    }
  }

  /**
   * Adds the provided script as a JavaScript library block into this rich text
   * writer.
   * <p>
   * Note: this users the structure of script-library design elements and is not
   * likely to be
   * useful in UI rich text.
   * </p>
   *
   * @param script the content of the script to write
   * @return this writer
   * @since 1.0.15
   */
  RichTextWriter addJavaScriptLibraryData(String script);

  /**
   * Appends a rich text item from another document
   *
   * @param doc      document
   * @param itemName rich text item name
   * @return this writer
   */
  RichTextWriter addRichText(Document doc, String itemName);

  /**
   * Appends rich text created in another writer
   *
   * @param rt writer
   * @return this writer
   */
  RichTextWriter addRichText(RichTextWriter rt);

  /**
   * Creates a new rich-text record of the given type and processes it through
   * the provided function before appending it to the record stream.
   *
   * @param <T>         the type of record to add
   * @param recordClass a {@link Class} record representing {@code <T>}
   * @param processor   a {@link Consumer} to configure the record
   * @return this writer
   */
  <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(Class<T> recordClass, Consumer<T> processor);

  /**
   * Creates a new rich-text record of the given type and processes it through
   * the provided function before appending it to the record stream.
   * <p>
   * This method allows you to specify an additional amount of space to allocate
   * for the structure's
   * variable data, which will be accessible just after the final part of the
   * structure.
   * </p>
   *
   * @param <T>                the type of record to add
   * @param recordClass        a {@link Class} record representing {@code <T>}
   * @param variableDataLength the amount of additional space, in bytes, to
   *                           allocate after the structure
   * @param processor          a {@link Consumer} to configure the record
   * @return this writer
   */
  <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(Class<T> recordClass, int variableDataLength,
      Consumer<T> processor);

  /**
   * Creates a new rich-text record of the given type and processes it through
   * the provided function before appending it to the record stream.
   *
   * @param <T>        the type of record that corresponds to {@code recordType}
   * @param recordType a {@link RecordType} record type representing the record to
   *                   create
   * @param processor  a {@link Consumer} to configure the record
   * @return this writer
   */
  <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(RecordType recordType, Consumer<T> processor);

  /**
   * Creates a new rich-text record of the given type and processes it through
   * the provided function before appending it to the record stream.
   * <p>
   * This method allows you to specify an additional amount of space to allocate
   * for the structure's
   * variable data, which will be accessible just after the final part of the
   * structure.
   * </p>
   *
   * @param <T>                the type of record that corresponds to
   *                           {@code recordType}
   * @param recordType         a {@link RecordType} record type representing the
   *                           record to create
   * @param variableDataLength the amount of additional space, in bytes, to
   *                           allocate after the structure
   * @param processor          a {@link Consumer} to configure the record
   * @return this writer
   */
  <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(RecordType recordType, int variableDataLength,
      Consumer<T> processor);

  /**
   * Adds a raw rich text record from an existing instance.
   *
   * @param record raw record
   * @return this writer
   */
  RichTextWriter addRichTextRecord(RichTextRecord<?> record);

  /**
   * Adds a text element
   *
   * @param txt text
   * @return this writer
   */
  RichTextWriter addText(String txt);

  /**
   * Adds a text element with the specified font style
   *
   * @param txt       text
   * @param textStyle text style
   * @param fontStyle font style
   * @return this writer
   */
  RichTextWriter addText(String txt, TextStyle textStyle, FontStyle fontStyle);

  /**
   * Adds a text element with the specified font style
   *
   * @param txt                        text
   * @param textStyle                  text style
   * @param fontStyle                  font style
   * @param createParagraphOnLinebreak true to create a paragraph for each
   *                                   linebreak found in the text
   * @return this writer
   */
  RichTextWriter addText(String txt, TextStyle textStyle, FontStyle fontStyle, boolean createParagraphOnLinebreak);

  /**
   * Closes this resource, relinquishing any underlying resources.
   * This method is invoked automatically on objects managed by the
   * {@code try}-with-resources statement.
   */
  @Override
  void close();

  /**
   * Creates a new font style
   *
   * @return style
   */
  FontStyle createFontStyle();

  /**
   * Creates a new {@link MemoryStructure} of the given class in memory, without
   * appending it to the
   * destination rich-text entity. This is useful in specific situations where
   * variable data consists
   * of structured values.
   *
   * @param <T>                the type of structure to create
   * @param structureClass     a {@link Class} representing {@code <T>}
   * @param variableDataLength the amount of additional space, in bytes, to
   *                           allocate after the structure
   * @return the newly-constructed structure
   */
  <T extends MemoryStructure> T createStructure(Class<T> structureClass, int variableDataLength);

  /**
   * Creates a new text style
   *
   * @param styleName style name
   * @return style
   */
  TextStyle createTextStyle(String styleName);

  /**
   * Discard any changes in this writer and free all resources
   */
  void discard();

  /**
   * Returns the name of the rich text item
   *
   * @return item name
   */
  String getItemName();

  /**
   * Returns the document that will contain the
   * rich text data
   *
   * @return document
   */
  Document getParentDocument();

  /**
   * Method to check if anything has been written into the writer
   *
   * @return true if empty
   */
  boolean isEmpty();

}
