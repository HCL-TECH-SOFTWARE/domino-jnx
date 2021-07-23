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

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.DominoClientDescendant;

/**
 * Subclass of {@link IDTable} to select data, design or admin documents from an NSF.
 */
public interface DocumentSelection extends IDTable, DominoClientDescendant {

	/**
	 * The available document types that can be selected via
	 * {@link DocumentSelection#select(Collection)} and {@link DocumentSelection#select(SelectionType...)}.
	 */
	public enum SelectionType {
		/** Indicates whether the collection contains the data documents. */
		DOCUMENTS,
		/** Indicates whether the collection contains profile documents */
		PROFILES,
		/** Indicates whether the collection contains notes for forms */
		FORMS,
		/**Indicates whether the collection contains notes for subforms */
		SUBFORMS,
		/** Indicates whether the collection contains notes for actions */
		ACTIONS,
		/** Indicates whether the collection contains notes for frame sets */
		FRAMESETS,
		/** Indicates whether the collection contains notes for pages */
		PAGES,
		/** Indicates whether the collection contains notes for image resources */
		IMAGE_RESOURCES,
		/** Indicates whether the collection contains notes for style sheet resources */
		STYLESHEETS,
		/** Indicates whether the collection contains notes for Java™ resources */
		JAVA_RESOURCES,
		/** Indicates whether the collection contains notes for miscellaneous format elements */
		MISC_FORMAT_ELEMENTS,
		/** Indicates whether the collection contains notes for views */
		VIEWS,
		/** Indicates whether the collection contains notes for folders */
		FOLDERS,
		/** Indicates whether the collection contains notes for navigators */
		NAVIGATORS,
		/** Indicates whether the collection contains notes for miscellaneous index elements */
		MISC_INDEX_ELEMENTS,
		/** Indicates whether the collection contains an icon note */
		ICON,
		/**Indicates whether the collection contains notes for agents */
		AGENTS,
		/** Indicates whether the collection contains notes for outlines */
		OUTLINES,
		/** Indicates whether the collection contains a database script note */
		DATASCRIPT_SCRIPT,
		/** Indicates whether the collection contains notes for script libraries */
		SCRIPT_LIBRARIES,
		/** Indicates whether the collection contains a data connection note */
		DATA_CONNECTIONS,
		/** Indicates whether the collection contains notes for miscellaneous code elements */
		MISC_CODE_ELEMENTS,
		/** Indicates whether the collection contains notes for shared fields */
		SHARED_FIELDS,
		/** Indicates whether the collection contains an "About Database" note */
		HELP_ABOUT,
		/** Indicates whether the collection contains a "Using Database" note */
		HELP_USING,
		/** Indicates whether the collection contains a help index note */
		HELP_INDEX,
		/** Indicates whether the collection contains replication formulas */
		REPLICATION_FORMULAS,
		/** Indicates whether the collection contains an ACL note */
		ACL
	}
	
	/**
	 * Returns the parent database
	 * 
	 * @return database
	 */
	Database getParentDatabase();
	
	/**
	 * Adds content types to the selection
	 * 
	 * @param selectionTypes content types
	 * @return document selection instance
	 */
	DocumentSelection select(SelectionType... selectionTypes);
	
	/**
	 * Adds content types to the selection
	 * 
	 * @param selectionTypes content types
	 * @return document selection instance
	 */
	DocumentSelection select(Collection<SelectionType> selectionTypes);
	
	/**
	 * Selects data, admin and design documents
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllDocuments();

	/**
	 * Selects normal data documents and profiles
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllDataDocuments();

	/**
	 * Selects replication formulas and ACLs
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllAdminDocuments();

	/**
	 * Selects format, index and code elements, the icon, shared fields,
	 * help about, help using and help imdex.
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllDesignElements();
	
	/**
	 * Selects forms, subforms, actions, framesets, pages, image resources,
	 * stylesheets, Java resources and misc format elements.
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllFormatElements();
	
	/**
	 * Selects views, folders, navigators and misc index elements
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllIndexElements();
	
	/**
	 * Selects agents, outlines, database script, script libraries,
	 * data connections and misc code elements.
	 * 
	 * @return document selection instance
	 */
	DocumentSelection selectAllCodeElements();

	/**
	 * Removes content types from the selection
	 * 
	 * @param selectionTypes content types
	 * @return document selection instance
	 */
	DocumentSelection deselect(SelectionType... selectionTypes);
	
	/**
	 * Removes content types from the selection
	 * 
	 * @param selectionTypes content types
	 * @return document selection instance
	 */
	DocumentSelection deselect(Collection<SelectionType> selectionTypes);

	/**
	 * Checks if a content type is selected
	 * 
	 * @param selectionType content type
	 * @return true if selected
	 */
	boolean isSelected(SelectionType selectionType);

	/**
	 * Returns the currently selected content types
	 * 
	 * @return selection
	 */
	Set<SelectionType> getSelection();
	
	/**
	 * Sets a formula that selects notes for inclusion in the collection.
	 * 
	 * @param formula formula
	 * @return document selection instance
	 */
	DocumentSelection withSelectionFormula(String formula);

	/**
	 * Returns the selection formula to selects notes for inclusion in the collection.
	 * @return the selection formula or an empty string if no selection formula was set
	 */
	String getSelectionFormula();

	/**
	 * Use this method to pre-filter the documents to be scanned by the search
	 * 
	 * @param noteIds note ids
	 * @return document selection instance
	 */
	DocumentSelection withPreselection(Collection<Integer> noteIds);
	
	/**
	 * Returns an {@link IDTable} with note ids to pre-filter the documents to
	 * be scanned by the search
	 * 
	 * @return an {@link Optional} describing the pre-selected documents, or an empty one
	 *      if this has not been set
	 */
	Optional<IDTable> getPreselection();
	
	/**
	 * Adds a since time to the search so that it only contains documents
	 * with a "Modified in this file" date after the given date.
	 * 
	 * @param dt since time
	 * @return document selection instance
	 */
	DocumentSelection withSinceTime(Temporal dt);

	/**
	 * Returns the since time so that the search only contains documents
	 * with a "Modified in this file" date after the given date.
	 * 
	 * @return an {@link Optional} describing the since time, or an empty one if
	 *      this has not been set
	 */
	Optional<DominoDateTime> getSinceTime();
	
	/**
	 * Returns a date that can be passed as "since time" to the next search
	 * for incremental searches.
	 * 
	 * @return an {@link Optional} describing the until time, or an empty one if
	 *      {@link #build()} has not yet been called
	 */
	Optional<DominoDateTime> getUntilTime();
	
	/**
	 * Returns the last time, the {@link #build()} has been invoked.
	 * 
	 * @return an {@link Optional} describing the build time, or an empty one if
	 *      {@link #build()} has not yet been called
	 */
	Optional<DominoDateTime> getLastBuildTime();

	/**
	 * Runs a database search, adding note ids of search matches to this
	 * object (subclass of {@link IDTable}).
	 * 
	 * @return document selection instance
	 */
	DocumentSelection build();
	
}
