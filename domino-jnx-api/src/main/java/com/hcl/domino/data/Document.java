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
package com.hcl.domino.data;

import java.io.IOException;
import java.io.OutputStream;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Attachment.Compression;
import com.hcl.domino.data.Database.CreateFlags;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.exception.LotusScriptCompilationException;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.conversion.RemoveAttachmentIconConversion;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Represents a document within an NSF.
 */
public interface Document extends TypedAccess, IAdaptable {

  public enum ComputeWithFormAction {
    /**
     * End all processing by
     * {@link Document#computeWithForm(boolean, ComputeWithFormCallback)}
     * and return the error status to the caller.
     */
    ABORT((short) 1),

    /** End validation of the current field and go on to the next. */
    NEXT_FIELD((short) 2),

    /** Begin the validation process for this field over again. */
    RECHECK_FIELD((short) 3);

    short actionVal;

    ComputeWithFormAction(final short val) {
      this.actionVal = val;
    }

    public short getShortVal() {
      return this.actionVal;
    }

  }

  /**
   * Callback to get notified about errors during
   * {@link Document#computeWithForm(boolean, ComputeWithFormCallback)}
   */
  public interface ComputeWithFormCallback {

    /**
     * Method is called for each form field that is causing a computation/validation
     * error.
     *
     * @param fieldInfo information about the field causing the error
     * @param phase     phase of compute with form that produced the error
     * @param errorTxt  error text from field validation formula
     * @param status    exception from C API status code
     * @return whether to continue computing/validating fields or to abort
     */
    ComputeWithFormAction errorRaised(FormField fieldInfo, ComputeWithFormPhase phase, String errorTxt, DominoException status);
  }

  /**
   * Possible validation phases for
   * {@link Document#computeWithForm(boolean, ComputeWithFormCallback)}
   */
  public enum ComputeWithFormPhase {
    /** Error occurred when processing the Default Value formula. */
    DEFAULT_VALUE_FORMULA,
    /** Error occurred when processing the Translation formula. */
    INPUT_TRANSLATION_FORMULA,
    /** Error occurred when processing the Validation formula. */
    INPUT_VALIDATION_FORMULA,
    /** Error occurred when processing the computed field Value formula. */
    COMPUTED_FIELD_FORMULA,
    /** Error occurred when verifying the data type for the field. */
    DATATYPE_VERIFICATION,
    /**
     * Error occurred when processing the computed field Value formula, during the
     * "load" pass.
     */
    COMPUTED_FORMULA_LOAD,
    /**
     * Error occurred when processing the computed field Value formula, during the
     * "save" pass.
     */
    COMPUTED_FORMULA_SAVE
  }

  public enum EncryptionMode {
    /**
     * Encrypt the message with the key in the user's ID. This flag is not for
     * outgoing mail
     * messages because recipients, other than the sender, will not be able to
     * decrypt
     * the message. This flag can be useful to encrypt documents in a local database
     * to
     * keep them secure or to encrypt documents that can only be decrypted by the
     * same user.
     */
    ENCRYPT_WITH_USER_PUBLIC_KEY(0x0001),

    /**
     * Encrypt SMIME if MIME present
     */
    ENCRYPT_SMIME_IF_MIME_PRESENT(0x0002),

    /**
     * Encrypt SMIME no sender.
     */
    ENCRYPT_SMIME_NO_SENDER(0x0004),

    /**
     * Encrypt SMIME trusting all certificates.
     */
    ENCRYPT_SMIME_TRUST_ALL_CERTS(0x0008);

    private final int m_mode;

    EncryptionMode(final int mode) {
      this.m_mode = mode;
    }

    public int getMode() {
      return this.m_mode;
    }
  }

  /**
   * Interface to create document attachments in-memory without
   * the need to write files to disk.
   */
  public interface IAttachmentProducer {

    /**
     * This method is called before creating the binary object in
     * the database to get a size estimation. The final size is adjusted
     * to the produced amount of data. Return an estimated file size
     * here to improve efficiency, otherwise we will do the first NSF object
     * allocation with a default size (1.000.000 bytes) and auto-grow / -shrink
     * it based on the produced file data.
     *
     * @return size estimation or -1 if unknown
     */
    long getSizeEstimation();

    /**
     * Implement this method to produce the file attachment data
     *
     * @param out output stream
     * @throws IOException in case of I/O errors
     */
    void produceAttachment(OutputStream out) throws IOException;

  }

  /** Document locking mode */
  public enum LockMode {
    /** Hard lock can only be set if Master Locking Server is available */
    Hard,
    /**
     * Try to create a hard lock; if Master Locking Server is not available, use a
     * provisional lock
     */
    HardOrProvisional,
    /** Provisional lock can be set if Master Locking Server is not available */
    Provisional
  }

  /**
   * Container for note signature data
   */
  public interface SignatureData {

    /**
     * @return the name of the signer's certifier
     */
    String getCertifier();

    /**
     * @return the name of the document signer
     */
    String getSigner();

    /**
     * @return the timestamp of when the document was signed
     */
    DominoDateTime getWhenSigned();
  }

  /**
   * Returns a stream of all document items
   *
   * @return items
   */
  Stream<Item> allItems();

  /**
   * Appends an item value to the document, leaving any other items of the same
   * name in place.
   *
   * @param itemName the name of the item to append
   * @param value    the value of the item to store
   * @return this document
   */
  Document appendItemValue(String itemName, Object value);

  /**
   * Appends an item value to the document, leaving any other items of the same
   * name in place.
   *
   * @param itemName the name of the item to append
   * @param flags    a collection of {@link ItemFlag} to apply to the item
   * @param value    the value of the item to store
   * @return this document
   */
  Document appendItemValue(String itemName, Set<ItemFlag> flags, Object value);

  /**
   * Appends an item value to the document, leaving any other items of the same
   * name in place.
   *
   * @param itemName             the name of the item to append
   * @param flags                a collection of {@link ItemFlag} to apply to the
   *                             item
   * @param value                the value of the item to store
   * @param allowDataTypeChanges to append the item even if its type doesn't match
   *                             the existing values
   * @return this document
   */
  Document appendItemValue(String itemName, Set<ItemFlag> flags, Object value, boolean allowDataTypeChanges);

  /**
   * This function appends an entry to a text list item.<br>
   * <br>
   * If the text list item does not already exist, one will be created using the
   * name provided and the text provided as the FIRST entry. The item flags are
   * set
   * to {@link ItemFlag#SUMMARY}.
   *
   * @param itemName        item name
   * @param value           string containing the text you wish to add as the NEXT
   *                        entry in the existing text list in the document
   * @param allowDuplicates A boolean containing a flag indicating whether or not
   *                        to allow duplicate entries in the text list.
   *                        {@code true} - If you wish to allow them and
   *                        {@code false} - If you do not.
   * @return this document
   */
  Document appendToTextList(String itemName, String value, boolean allowDuplicates);

  /**
   * Attaches a file to the document, not associated with a rich-text item.
   * <p>
   * Unlike
   * {@link #attachFile(String, TemporalAccessor, TemporalAccessor, IAttachmentProducer)},
   * this
   * method reads data from the named file on disk directly.
   * </p>
   *
   * @param filePathOnDisk      a path to the file on disk
   * @param uniqueFileNameInDoc a name for the attachment in the document
   * @param compression         the compression scheme to use when storing the
   *                            attachment
   * @return the newly-created document attachment
   */
  Attachment attachFile(String filePathOnDisk, String uniqueFileNameInDoc, Compression compression);

  /**
   * Attaches a file to the document, not associated with a rich-text item.
   * <p>
   * Unlike {@link #attachFile(String, String, Attachment.Compression)}, this method
   * allows for specifying file metadata
   * and source binary data arbitrarily. It does not use compression. No temp
   * file is created on disk.
   * </p>
   *
   * @param uniqueFileNameInDoc a name for the attachment in the document
   * @param fileCreated         the creation date for the stored file
   * @param fileModified        the modification date for the stored file
   * @param producer            a {@link IAttachmentProducer} implementation that
   *                            provides the attachment data
   * @return the newly-created document attachment
   */
  Attachment attachFile(String uniqueFileNameInDoc,
      TemporalAccessor fileCreated, TemporalAccessor fileModified, IAttachmentProducer producer);

  /**
   * Returns an instance of this document that implements the
   * {@link AutoCloseableDocument}
   * interface and can be used within a try-with-resources block for immediate
   * handle
   * disposal. The purpose of this method is to get more control about the
   * platform memory
   * usage instead of leaving this work up to the JNX garbage collector.
   *
   * @return auto-closeable document
   */
  AutoCloseableDocument autoClosable();

  /**
   * Compiles all LotusScript attached to this note
   *
   * @throws LotusScriptCompilationException if there is a compilation problem
   * @since 1.0.5
   * @return this document
   */
  Document compileLotusScript();

  /**
   * For each form field associated with this document, this function will:<br>
   * <br>
   * <ol>
   * <li>Validate fields with Default Value formulas,</li>
   * <li>Validate fields with Translation formulas,</li>
   * <li>Validate fields with Validation formulas,</li>
   * <li>Calculate values for Computed fields.</li>
   * </ol>
   * <br>
   * If no error callback routine is supplied and the flag {@code continueOnError}
   * is not
   * set, if one of the validation formulas fails, an error is returned.<br>
   * If the {@code continueOnError} flag is set, the error is not returned.<br>
   * <br>
   * If a callback routine is supplied and one of the validation formulas fails,
   * the
   * error callback routine is called.<br>
   * If the {@code continueOnError} flag is set, error processing for the field is
   * skipped;<br>
   * if a callback function is supplied, the callback function will be ignored.
   *
   * @param continueOnError Ignore error processing for fields
   * @param callback        error callback routine
   * @return this document
   */
  Document computeWithForm(boolean continueOnError, final ComputeWithFormCallback callback);

  /**
   * Applies one of multiple conversions to a rich text item, writing the new
   * version in the target document and item.
   *
   * @param itemName       rich text item name
   * @param targetNote     note to copy to conversion result to
   * @param targetItemName item name in target note where we should save the
   *                       conversion result
   * @param conversions    conversions, processed from left to right
   * @return true if rich text has been updated, false if all conversion classes
   *         returned {@link IRichTextConversion#isMatch(List)} as false
   */
  boolean convertRichTextItem(String itemName, Document targetNote, String targetItemName,
      IRichTextConversion... conversions);

  /**
   * Applies one or multiple conversions to a rich text item, updating the
   * original item in-place.
   *
   * @param itemName    rich text item name
   * @param conversions conversions, processed from left to right
   * @return true if rich text has been updated, false if all conversion classes
   *         returned {@link IRichTextConversion#isMatch(List)} as false
   */
  boolean convertRichTextItem(String itemName, IRichTextConversion... conversions);
  
  /**
   * Converts any RFC 822 (MIME text) items to their traditional Domino equivalents.
   * 
   * @since 1.12.0
   */
  void convertRFC822Items();

  /**
   * This function copies and encrypts (seals) the encryption enabled fields in a
   * document
   * (including the document's file objects), using a {@link UserId}.<br>
   * <br>
   * It can encrypt a document in several ways -- by using the Domino public key
   * of the caller,
   * by using specified secret encryption keys stored in the caller's ID, or by
   * using the
   * Domino public keys of specified users, if the document does not have any mime
   * parts.<br>
   * <br>
   * The method decides which type of encryption to do based upon the setting of
   * the flag
   * passed to it in its <code>encryptionMode</code> argument.<br>
   * <br>
   * If the {@link EncryptionMode#ENCRYPT_WITH_USER_PUBLIC_KEY} flag is set, it
   * uses the
   * caller's public ID to encrypt the document.<br>
   * In this case, only the user who encodes the document can decrypt it.<br>
   * This feature allows an individual to protect information from anyone
   * else.<br>
   * <br>
   * If, instead, the {@link EncryptionMode#ENCRYPT_WITH_USER_PUBLIC_KEY} flag is
   * not set,
   * then the function expects the document to contain a field named
   * "SecretEncryptionKeys"
   * a field named "PublicEncryptionKeys", or both.<br>
   * Each field is either a {@link ItemDataType#TYPE_TEXT} or
   * {@link ItemDataType#TYPE_TEXT_LIST} field.<br>
   * <br>
   * "SecretEncryptionKeys" contains the name(s) of the secret encryption keys in
   * the
   * calling user's ID to be used to encrypt the document.<br>
   * This feature is intended to allow a group to encrypt some of the documents in
   * a single
   * database in a way that only they can decrypt them -- they must share the
   * secret encryption
   * keys among themselves first for this to work.<br>
   * <br>
   * "PublicEncryptionKeys" contains the name(s) of users, in canonical
   * format.<br>
   * The document will be encrypted with each user's Domino public key.<br>
   * The user can then decrypt the document with the private key in the user's
   * ID.<br>
   * This feature provides a way to encrypt documents, such as mail documents, for
   * another user.<br>
   * <br>
   * The document must contain at least one encryption enabled item (an item with
   * the {@link ItemFlag#ENCRYPTED} flag set)
   * in order to be encrypted.<br>
   * If the note has mime parts and flag
   * {@link EncryptionMode#ENCRYPT_SMIME_IF_MIME_PRESENT}
   * is set, then it is SMIME encrypted.<br>
   * <br>
   * If the document is to be signed as well as encrypted, you must sign the
   * document
   * before using this method.
   *
   * @param id             user id to be used for encryption, use null for current
   *                       id
   * @param encryptionMode encryption mode
   * @return encrypted document copy
   */
  Document copyAndEncrypt(UserId id, Collection<EncryptionMode> encryptionMode);

  /**
   * Copies the document to another database.
   *
   * @param otherDb the target database to store the copied document
   * @return the copied document in the target database
   */
  Document copyToDatabase(Database otherDb);

  /**
   * Creates a writer for a new rich-text item on the document.
   *
   * @param itemName the name of the rich-text item to create
   * @return a {@link RichTextWriter} to provide the item contents
   */
  RichTextWriter createRichTextItem(String itemName);

  /**
   * Decrypts the contents of the document.
   *
   * @param id the {@link UserId} to use for decryption, or {@code null}
   *           to use the active runtime Notes ID
   * @return this document
   */
  Document decrypt(UserId id);

  /**
   * Deletes the document from the database.
   * <p>
   * This is equivalent to calling {@code delete(false)}.
   * </p>
   */
  void delete();

  /**
   * Deletes the document from the database.
   *
   * @param noStub whether to purge the document without leaving a deletion stub
   */
  void delete(boolean noStub);

  /**
   * Iterates over each attachment in the document, until all attachments have
   * been
   * processed or the consumer calls {@link Loop#stop()}.
   *
   * @param consumer a {@link BiConsumer} to process the attachments
   * @return this document
   */
  Document forEachAttachment(BiConsumer<Attachment, Loop> consumer);

  /**
   * Iterates over all document items
   *
   * @param consumer consumer to receive the {@link Item}
   * @return this document
   */
  Document forEachItem(BiConsumer<Item, Loop> consumer);

  /**
   * Iterates over all document items with the specified name
   *
   * @param itemName item name
   * @param consumer consumer to receive the {@link Item}
   * @return this document
   */
  Document forEachItem(String itemName, BiConsumer<Item, Loop> consumer);

  /**
   * Retrives the time when the document was added to this NSF, regardless of when
   * it was
   * originally created.
   *
   * @return the timestamp of when the document was added to the file
   */
  DominoDateTime getAddedToFile();

  /**
   * Retrieves the item value as text.
   * <p>
   * This differs from calling {@link #get} with `String.class` as the type in
   * that it uses
   * an internal Domino routine to convert values to text.
   * </p>
   *
   * @param itemName  the name of the item to retrieve
   * @param separator a separator character to use when the item is multi-value
   * @return the item value as a string
   * @since 1.0.5
   */
  String getAsText(String itemName, char separator);

  /**
   * Retrieves a document attachment by name.
   *
   * @param fileName the name of the attachment stored in the document
   * @return an {@link Optional} describing the attachment, or an empty one if no
   *         such
   *         attachment exists
   */
  Optional<Attachment> getAttachment(String fileName);

  /**
   * Returns the names of all attachments
   * 
   * @return attachment names
   * @since 1.4.6
   */
  default Set<String> getAttachmentNames() {
    Set<String> attachmentNames = new TreeSet<>();
    forEachAttachment((att, loop) -> {
      attachmentNames.add(att.getFileName());
    });
    return attachmentNames;
  }
  
  /**
   * Retrieves the creation date of this document.
   * <p>
   * Note: by default, this value is derived from the document's UNID, but can be
   * overridden
   * by the presence of a {@code $Created} date/time item.
   * </p>
   *
   * @return the timestamp of the document's creation
   */
  DominoDateTime getCreated();

  /**
   * Returns the document classes that apply to this document
   *
   * @return a {@link Set} of {@link DocumentClass} values
   */
  Set<DocumentClass> getDocumentClass();

  /**
   * Retrieves the first item of the given name on the document.
   *
   * @param itemName the name of the item to retrieve
   * @return an {@link Optional} describing the {@link Item}, or an empty one if
   *         no such item exists
   */
  Optional<Item> getFirstItem(String itemName);

  /**
   * Retrieves the value of the first item of the given name on the document as
   * its
   * closest Java-type match.
   *
   * @param itemName the name of the item to retrieve
   * @return a {@link List} of item values, never {@code null}
   */
  List<?> getItemValue(String itemName);

  /**
   * Retrieves the last-accessed time of the document, if that information is
   * being tracked
   * in the database.
   *
   * @return the timestamp of the document last access; may be the minimum time
   *         value if
   *         access are not being tracked
   */
  DominoDateTime getLastAccessed();

  /**
   * Retrieves the time when the document was last modified.
   *
   * @return the timestamp of the document's last modification
   */
  DominoDateTime getLastModified();

  /**
   * @return a {@link List} of user names that currently have a lock on the
   *         document
   */
  List<String> getLockHolders();

  /**
   * Retrieves the time when the document was last modified in this NSF, which may
   * be later
   * than the last time it was modified on any replica.
   *
   * @return the timestamp of the document's last modification in this NSF
   */
  DominoDateTime getModifiedInThisFile();

  /**
   * Retrieves the note ID of this document. This value is specific to the NSF and
   * is not unique
   * across replicas.
   *
   * @return the integer note ID
   */
  int getNoteID();

  /**
   * Retrieves the originator ID of the document, which contains the UNID and
   * additional
   * modification information.
   *
   * @return a {@link DominoOriginatorId} object, never {@code null}
   */
  DominoOriginatorId getOID();

  /**
   * Retrieves the parent database of this document.
   *
   * @return the parent {@link Database}
   */
  Database getParentDatabase();

  /**
   * Retrieves the UNID of the parent document of this document, if present.
   * <p>
   * This is equivalent to calling {@code get("$REF", String.class, "")}.
   * </p>
   *
   * @return the UNID of the document's parent, or an empty string if the document
   *         has no parent
   * @since 1.0.7
   */
  default String getParentDocumentUNID() {
    return this.get("$REF", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns the category part of the note primary key
   *
   * @return category or empty string if no primary key has been assigned
   */
  String getPrimaryKeyCategory();

  /**
   * Returns the object id part of the note primary key
   *
   * @return object id or empty string if no primary key has been assigned
   */
  String getPrimaryKeyObjectId();

  /**
   * For profile documents, this method returns the name of the profile
   *
   * @return profile name or empty string
   */
  String getProfileName();

  /**
   * For profile documents, this method returns the username of the profile
   * owner
   *
   * @return username or empty string
   */
  String getProfileUserName();

  /**
   * The function returns all the readers items of the document
   *
   * @return array with readers fields
   */
  List<Item> getReadersFields();

  /**
   * Retrieves the count of response documents to this document.
   * <p>
   * Note: due to the lower-level API, this is likely to be more efficient than
   * {@link #getResponses() retrieving the response document collection} and
   * checking its count.
   * </p>
   *
   * @return the count of the document's responses
   * @since 1.0.9
   */
  int getResponseCount();

  /**
   * Retrieves the response documents to this document.
   * <p>
   * In order for this method to return the response table, this document must
   * have been opened
   * with the {@link OpenDocumentMode#LOAD_RESPONSES} flag.
   * </p>
   *
   * @return an {@link IDTable} of responses
   * @since 1.0.9
   */
  IDTable getResponses();

  /**
   * Retrieves the named rich-text item as an unmodifiable {@link List} of
   * composite data records.
   * <p>
   * This data may span multiple items of the same name, and all are included in
   * the returned list.
   * </p>
   * <p>
   * Note: though presented as a {@link List}, some operations (in particular
   * {@link List#size() size()})
   * are particularly expensive. The most efficient way to traverse the result is
   * via {@link List#stream()}
   * or {@link List#listIterator()}.
   * </p>
   *
   * @param itemName the name of the rich text item to retrieve
   * @return a {@link List} of {@link RichTextRecord} objects
   */
  default RichTextRecordList getRichTextItem(final String itemName) {
    return this.getRichTextItem(itemName, RecordType.Area.TYPE_COMPOSITE);
  }

  /**
   * Retrieves the named rich text item as an unmodifiable {@link List} of
   * composite data records, to be treated
   * as a specific variant of rich text.
   * <p>
   * This data may span multiple items of the same name, and all are included in
   * the returned list.
   * </p>
   * <p>
   * Note: though presented as a {@link List}, some operations (in particular
   * {@link List#size() size()})
   * are particularly expensive. The most efficient way to traverse the result is
   * via {@link List#stream()}
   * or {@link List#listIterator()}.
   * </p>
   *
   * @param itemName the name of the rich text item to retrieve
   * @param variant  the data area variant represented by the item
   * @return a {@link List} of {@link RichTextRecord} objects
   */
  RichTextRecordList getRichTextItem(String itemName, RecordType.Area variant);

  /**
   * Retrieves the modification sequence number of the document.
   *
   * @return an integer sequence number
   */
  int getSequenceNumber();

  /**
   * Returns the signer of a note
   *
   * @return signer or empty string if not signed
   */
  String getSigner();
  
  /**
   * Retrieves the thread ID of this document, if present.
   * <p>
   * This is equivalent to calling {@code get("$TUA", String.class, "")}.
   * </p>
   *
   * @return an {@link Optional} describing the the document's thread ID,
   *         or an empty one if the document has no stored thread
   * @since 1.11.0
   */
  Optional<String> getThreadID();

  /**
   * Retrieves the universal ID of the document, which is unique across replicas.
   *
   * @return the document's UNID as a 32-character hexadecimal string
   */
  String getUNID();

  /**
   * Returns TRUE if the given note contains any
   * {@link ItemDataType#TYPE_COMPOSITE} items.
   *
   * @return true if composite
   */
  boolean hasComposite();

  /**
   * The method checks whether an item exists
   *
   * @param itemName item name
   * @return true if the item exists
   */
  @Override
  boolean hasItem(String itemName);

  /**
   * Returns TRUE if the given note contains either
   * {@link ItemDataType#TYPE_RFC822_TEXT}
   * items or {@link ItemDataType#TYPE_MIME_PART} items.
   *
   * @return true if mime
   */
  boolean hasMIME();

  /**
   * The function returns TRUE if the given note contains any
   * {@link ItemDataType#TYPE_MIME_PART} items.
   *
   * @return true if mime part
   */
  boolean hasMIMEPart();

  /**
   * The function returns TRUE if the given document contains any items with
   * reader access flag
   *
   * @return true if readers field
   */
  boolean hasReadersField();

  /**
   * Returns true if the current user has sufficient access rights to modify the
   * document.
   *
   * @return true if editable
   */
  boolean isEditable();

  /**
   * Checks whether this note is sealed
   *
   * @return true if sealed
   */
  boolean isEncrypted();

  /**
   * Returns true if this is a ghost document. Ghost documents
   * do not show up in views. These kind of documents can be
   * created via {@link Database#createDocument(Set)} with
   * {@link CreateFlags#HIDE_FROM_VIEWS}
   *
   * @return true if hidden
   */
  boolean isHiddenFromViews();

  /**
   * Returns true if the document has not been saved yet
   *
   * @return true if new
   */
  boolean isNew();

  /**
   * Checks if this document is a response
   *
   * @return true if response
   */
  default boolean isResponse() {
    return this.hasItem("$ref"); //$NON-NLS-1$
  }

  /**
   * Indicates whether a document is saved to a database when mailed.
   *
   * @return true to save on send
   */
  boolean isSaveMessageOnSend();

  /**
   * Checks whether this note is signed
   *
   * @return true if signed
   */
  boolean isSigned();

  /**
   * Returns true if the document has been deleted in a database that has soft
   * deletions
   * enabled. Use {@link #undelete()} to undelete the document.
   *
   * @return true if soft deleted
   */
  boolean isSoftDeleted();

  /**
   * Indicates that the note has been abstracted (truncated).<br>
   * This is the case if the database containing the note has replication settings
   * set to
   * "Truncate large documents and remove attachments".
   *
   * @return true if truncated
   */
  boolean isTruncated();

  /**
   * Checks if the document is unread for the user returned by
   * {@link DominoClient#getEffectiveUserName()}.<br>
   * Uses {@link Database#isDocumentUnread(String, int)} internally.
   *
   * @return true if unread
   */
  boolean isUnread();

  /**
   * Checks if the document is unread for the specified user.<br>
   * Uses {@link Database#isDocumentUnread(String, int)} internally.
   *
   * @param userName username in abbreviated or canonical format, if null, we use
   *                 {@link DominoClient#getEffectiveUserName()}
   * @return true if unread
   */
  boolean isUnread(String userName);

  /**
   * This function adds an "$Writers" field to a note which contains a list of
   * "writers"
   * who will be able to update the note.<br>
   * <br>
   * Any user will be able to open the note, but only the members contained in the
   * "$Writers"
   * field are allowed to update the note.<br>
   * <br>
   * This function will only succeed if the database option "Allow document
   * locking" is set.<br>
   * <br>
   * Please refer to the Domino documentation for a full description of document
   * locking.
   *
   * @param lockHolders new lock holders
   * @param mode        lock mode
   * @return true if successful, false if already locked
   */
  boolean lock(List<String> lockHolders, LockMode mode);

  /**
   * This function adds an "$Writers" field to a note which contains a list of
   * "writers"
   * who will be able to update the note.<br>
   * <br>
   * Any user will be able to open the note, but only the members contained in the
   * "$Writers"
   * field are allowed to update the note.<br>
   * <br>
   * This function will only succeed if the database option "Allow document
   * locking" is set.<br>
   * <br>
   * Please refer to the Domino documentation for a full description of document
   * locking.
   *
   * @param lockHolder new lock holder
   * @param mode       lock mode
   * @return true if successful, false if already locked
   */
  boolean lock(String lockHolder, LockMode mode);

  /**
   * Makes the document a response to another document.
   *
   * @param doc the parent document
   * @return this document
   */
  Document makeResponse(Document doc);

  /**
   * Makes the document a response to another document identified by UNID.
   *
   * @param unid the UNID of the parent document
   * @return this document
   */
  Document makeResponse(String unid);

  /**
   * Removes an attachment by the unique internal file name.
   * <p>
   * Note: this does not remove references to the document from rich-text items.
   * </p>
   *
   * @param uniqueFileNameInDoc the unique internal name of the attachment in the
   *                            document
   * @see RemoveAttachmentIconConversion
   * @return this document
   */
  Document removeAttachment(String uniqueFileNameInDoc);

  /**
   * Removes <b>all occurrences</b> of items with the provided name from the document.
   *
   * @param itemName the case-insensitive name of the item to remove
   * @return this document
   */
  Document removeItem(String itemName);

  /**
   * Sets the value of an item value to the document, replacing any other items of
   * the same name.
   *
   * @param itemName the name of the item to store
   * @param value    the value of the item to store
   * @return this document
   */
  Document replaceItemValue(String itemName, Object value);

  /**
   * Sets the value of an item value to the document, replacing any other items of
   * the same name.
   *
   * @param itemName             the name of the item to store
   * @param value                the value of the item to store
   * @param allowDataTypeChanges to append the item even if its type doesn't match
   *                             the existing values
   * @return this document
   */
  Document replaceItemValue(String itemName, Object value, boolean allowDataTypeChanges);

  /**
   * Sets the value of an item value to the document, replacing any other items of
   * the same name.
   *
   * @param itemName the name of the item to store
   * @param value    the value of the item to store
   * @param flags    a collection of {@link ItemFlag} to apply to the item
   * @return this document
   */
  Document replaceItemValue(String itemName, Set<ItemFlag> flags, Object value);

  /**
   * Sets the value of an item value to the document, replacing any other items of
   * the same name.
   *
   * @param itemName             the name of the item to store
   * @param value                the value of the item to store
   * @param flags                a collection of {@link ItemFlag} to apply to the
   *                             item
   * @param allowDataTypeChanges to append the item even if its type doesn't match
   *                             the existing values
   * @return this document
   */
  Document replaceItemValue(String itemName, Set<ItemFlag> flags, Object value, boolean allowDataTypeChanges);

  /**
   * Writes in-memory changes to a document to the database.
   * <p>
   * This is equivalent to calling {@code save(false)}.
   * </p>
   * @return this document
   */
  Document save();

  /**
   * Writes in-memory changes to a document to the database.
   *
   * @param force whether to save the document even if another user has modified
   *              or deleted it
   * @return this document
   */
  Document save(boolean force);

  /**
   * Mails the note<br>
   * Convenience function that calls {@link #send(boolean, Collection)}.
   * @return this document
   */
  Document send();

  /**
   * Mails the document<br>
   * Convenience function that calls {@link #send(boolean, Collection)}.
   *
   * @param attachform If true, the form is stored and sent along with the
   *                   document. If false, it isn't. Do not attach a form that
   *                   uses computed subforms.
   * @return this document
   */
  Document send(boolean attachform);

  /**
   * Mails the document<br>
   * <br>
   * Two kinds of items can affect the mailing of the document when you use
   * send:<br>
   * <ul>
   * <li>If the document contains additional recipient items, such as CopyTo or
   * BlindCopyTo, the documents mailed to those recipients.</li>
   * <li>If the document contains items to control the routing of mail, such as
   * DeliveryPriority, DeliveryReport, or ReturnReceipt, they are used when
   * sending the document.</li>
   * </ul>
   * The {@link #isSaveMessageOnSend()} property controls whether the sent
   * document is saved
   * in the database. If {@link #isSaveMessageOnSend()} is true and you attach the
   * form to the document,
   * the form is saved with the document.<br>
   * Sending the form increases the size of the document, but ensures that the
   * recipient can see
   * all of the items on the document.
   *
   * @param attachform If true, the form is stored and sent along with the
   *                   document. If false, it isn't. Do not attach a form that
   *                   uses computed subforms.
   * @param recipients The recipients of the document, may include people, groups,
   *                   or mail-in databases.
   * @return this document
   */
  Document send(boolean attachform, Collection<String> recipients);

  /**
   * Mails the document<br>
   * Convenience function that calls {@link #send(boolean, Collection)}.
   *
   * @param attachform If true, the form is stored and sent along with the
   *                   document. If false, it isn't. Do not attach a form that
   *                   uses computed subforms.
   * @param recipient  The recipient of the document, may include people, groups,
   *                   or mail-in databases.
   * @return this document
   */
  Document send(boolean attachform, String recipient);

  /**
   * Mails the document<br>
   * Convenience function that calls {@link #send(boolean, Collection)}.
   *
   * @param recipients The recipients of the document, may include people, groups,
   *                   or mail-in databases.
   * @return this document
   */
  Document send(Collection<String> recipients);

  /**
   * Mails the document<br>
   * Convenience function that calls {@link #send(boolean, Collection)}.
   *
   * @param recipient The recipient of the document, may include people, groups,
   *                  or mail-in databases.
   * @return this document
   */
  Document send(String recipient);

  /**
   * Sets the class of this document.
   *
   * @param docClass the {@link DocumentClass} value to set
   * @return this document
   * @since 1.0.15
   */
  Document setDocumentClass(DocumentClass docClass);

  /**
   * Sets the class of this document.
   *
   * @param docClass the {@link DocumentClass} value to set
   * @return this document
   * @since 1.0.15
   */
  Document setDocumentClass(Collection<DocumentClass> docClass);

  /**
   * Writes a primary key information to the note. This primary key can be used
   * for
   * efficient note retrieval without any lookup views.<br>
   * <br>
   * Both <code>category</code> and <code>objectKey</code> are combined
   * to a string that is expected to be unique within the database.
   *
   * @param category category part of primary key
   * @param objectId object id part of primary key
   * @return this document
   */
  Document setPrimaryKey(String category, String objectId);

  /**
   * Indicates whether a document is saved to a database when mailed.
   *
   * @param b true to save on send
   * @return this document
   */
  Document setSaveMessageOnSend(boolean b);

  /**
   * Sets the universal ID for the document.
   *
   * @param newUNID a 32-character hexadecimal string
   * @return this document
   */
  Document setUNID(String newUNID);

  /**
   * Method to change the unread state of the document
   *
   * @param userName username in abbreviated or canonical format, if null, we use
   *                 {@link DominoClient#getEffectiveUserName()}
   * @param unread   true to mark unread, false to mark read
   * @return this document
   */
  Document setUnread(String userName, boolean unread);

  /**
   * Signs the document using the active Notes ID.
   * @return this document
   */
  Document sign();

  /**
   * Function to sign all items in the document.<br>
   * It allows you to pass a flag to determine how MIME parts will be signed.<br>
   * If the note has MIME parts then it will be SMIME signed.
   * If not, or the <code>signNotesIfMimePresent</code> parameter is set
   * to allow Notes signature on MIME parts, then it will be Notes signed.
   * If the document to be signed is encrypted, this function will attempt to
   * decrypt the document in order to generate a valid signature.<br>
   * <br>
   * If you want the document to be signed and encrypted, you must sign the
   * document before using {@link #copyAndEncrypt(UserId, Collection)}.
   *
   * @param id                     id to sign, null for active ID
   * @param signNotesIfMimePresent If the note has MIME parts and this flag is
   *                               true it will be SMIME signed, if not set it
   *                               will be Notes signed.
   * @return this document
   */
  Document sign(UserId id, boolean signNotesIfMimePresent);

  /**
   * Returns the total size of the document
   *
   * @return size
   */
  int size();

  /**
   * Undeletes a soft deleted document
   * @return this document
   */
  Document undelete();

  /**
   * This function removes the lock on a note.<br>
   * <br>
   * Only the members contained in the "writers" list are allowed to remove a
   * lock,
   * with the exception of person(s) designated as capable of removing locks.<br>
   * <br>
   * Please refer to the Domino documentation for a full description of document
   * locking.#
   *
   * @param mode lock mode
   * @return this document
   */
  Document unlock(LockMode mode);

  /**
   * Unsigns the note. This function removes the $Signature item from the note.
   * @return this document
   */
  Document unsign();

  /**
   * This function verifies a signature on a note or section(s) within a note.<br>
   * It returns an error if a signature did not verify.<br>
   * <br>
   *
   * @return signer data
   */
  SignatureData verifySignature();

}
