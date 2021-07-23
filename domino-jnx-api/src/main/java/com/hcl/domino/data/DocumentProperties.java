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

import java.util.List;
import java.util.Set;

import com.hcl.domino.data.Item.ItemFlag;

/**
 * Interface to define Document Properties as a serializable object, avoiding
 * dependencies on the underlying Document and Items.
 * 
 * This allows quick programmatic access to summary details about a Document. A
 * common requirement for this is to display as a Properties window for
 * developers (as found in Notes Client). A strong use case for having it
 * available as an object is to compare two documents, most typically a main
 * document and its conflict. Another strong use case would be to store all or a
 * subset for audit tracking purposes.
 * 
 * @author Paul Withers
 * @since 0.5.0
 */
public interface DocumentProperties {

	/**
	 * DocumentProperty objects corresponding to each Item on the Document
	 * 
	 * @return List of DocumentProperty objects
	 */
	List<DocumentProperty> getDocumentProperties();

	/**
	 * Add an Item's summary details to this DocumentProperties object
	 * 
	 * @param item the Item to extract summary details from, not null
	 * @return this DocumentProperties object
	 */
	DocumentProperties addItemDetails(final Item item);

	/**
	 * Interface to define properties of an individual Item, without requiring
	 * constant reference to the underlying Item.
	 * 
	 * @author Paul Withers
	 * @since 0.5.0
	 */
	public interface DocumentProperty {

		/**
		 * The Item name. Items beginning with $ are by convention system fields, e.g.
		 * <ul>
		 * <li><b>$UpdatedBy</b> for list of editors (including by signers of
		 * programmatic code</li>
		 * <li><b>$Revisions</b> for the list of date/times the Document was saved</li>
		 * <li><b>$FILE</b> for attachments in the Document (each file will be in an
		 * Item always called "$FILE", not "$FILE1", "$FILE2" etc.
		 * </ul>
		 * 
		 * @return the Item's name, never null
		 */
		String getFieldName();

		/**
		 * Gets all the field flags corresponding to the Item
		 * 
		 * @return field flags applied, e.g. {SUMMARY,READERS}
		 */
		Set<ItemFlag> getFlags();

		/**
		 * Incremental integer for the nth save this Item was last updated.
		 * Cross-reference with the $Revisions field for the date and time it was saved.
		 * 
		 * Be aware that for very frequently updating documents, the integer will cycle
		 * back to 1
		 * 
		 * @return integer corresponding to the nth time the document was saved, never
		 *         null
		 */
		int getSequenceNumber();

		/**
		 * Gets the data type for a field, converting it to the more readable enum
		 * 
		 * @return enum corresponding to the internal int value for the data type
		 */
		ItemDataType getType();

		/**
		 * Decodes the item value(s). The data is always returned as a list even though
		 * the list may contain only one element (e.g. for {@link ItemDataType#TYPE_TEXT}. <br>
		 * 
		 * The following data types are currently supported:<br>
		 * <ul>
		 * <li>{@link ItemDataType#TYPE_TEXT} - List with String object</li>
		 * <li>{@link ItemDataType#TYPE_TEXT_LIST} - List of String objects</li>
		 * <li>{@link ItemDataType#TYPE_NUMBER} - List with Double object</li>
		 * <li>{@link ItemDataType#TYPE_NUMBER_RANGE} - List of Double objects</li>
		 * <li>{@link ItemDataType#TYPE_TIME} - List with Calendar object</li>
		 * <li>{@link ItemDataType#TYPE_TIME_RANGE} - List of Calendar objects</li>
		 * <li>{@link ItemDataType#TYPE_OBJECT} with the subtype Attachment (e.g. $File items) -
		 * List with {@link Attachment} object</li>
		 * <li>{@link ItemDataType#TYPE_NOTEREF_LIST} - List with one UNID</li>
		 * <li>{@link ItemDataType#TYPE_UNAVAILABLE} - returns an empty list</li>
		 * </ul>
		 * Other data types may be read via {@code Typed#getValueAsText(char)} or native
		 * support may be added at a later time.
		 * 
		 * @return list of values, never null. If empty, the value will be a List with a
		 *         single empty string
		 */
		List<Object> getValue();

		/**
		 * Concatenates the list of values as text.
		 * 
		 * TODO: Define format of dates - system format?
		 * 
		 * @return the value as a single String
		 */
		String getValueAsString();

	}

}
