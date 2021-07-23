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
package com.hcl.domino.commons.design;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.commons.design.DesignUtil.DesignMapping;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DbProperties;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.FileResource;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.ImageResource;
import com.hcl.domino.design.ScriptLibrary;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.View;
import com.hcl.domino.misc.NotesConstants;

public abstract class AbstractDbDesign implements DbDesign {
	
	private final Database database;

	public AbstractDbDesign(Database database) {
		this.database = database;
	}

	@Override
	public Stream<DesignElement> queryDesignElements(String formula) {
		return database.queryFormula(formula, null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.ALLNONDATA))
			.getNoteIds()
			.get()
			.stream()
			.map(database::getDocumentById)
			.map(Optional::get)
			.map(DesignUtil::createDesignElement);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DesignElement> Stream<T> getDesignElements(Class<T> type) {
		DesignMapping<T, ?> mapping = DesignUtil.getDesignMapping(type);
		return (Stream<T>)findDesignNotes(mapping.getNoteClass(), mapping.getFlagsPattern())
			.map(entry -> database.getDocumentById(entry.noteId))
			.map(Optional::get)
			.map(mapping.getConstructor());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DesignElement> Stream<T> queryDesignElements(Class<T> type, String formula) {
		DesignMapping<T, ?> mapping = DesignUtil.getDesignMapping(type);
		return (Stream<T>)database.queryFormula(formula, null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(mapping.getNoteClass()))
			.getNoteIds()
			.get()
			.stream()
			.map(database::getDocumentById)
			.map(Optional::get)
			.map(mapping.getConstructor()::apply);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DesignElement> Stream<T> getDesignElementsByName(Class<T> type, String name) {
		DesignMapping<T, ?> mapping = DesignUtil.getDesignMapping(type);
		return (Stream<T>)findDesignNotes(mapping.getNoteClass(), mapping.getFlagsPattern())
			.filter(entry -> DesignUtil.matchesTitleValues(name, entry.getTitles()))
			.map(entry -> database.getDocumentById(entry.noteId))
			.map(Optional::get)
			.map(mapping.getConstructor());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DesignElement> Optional<T> getDesignElementByName(Class<T> type, String name) {
		DesignMapping<T, ?> mapping = DesignUtil.getDesignMapping(type);
		int noteId = findDesignNote(mapping.getNoteClass(), mapping.getFlagsPattern(), name, false);
		if(noteId == 0) {
			return Optional.empty();
		} else {
			return Optional.of((T)mapping.getConstructor().apply(database.getDocumentById(noteId).get()));
		}
	}

	@Override
	public Folder createFolder(String folderName) {
		return createDesignNote(Folder.class, folderName);
	}
	
	@Override
	public View createView(String viewName) {
		return createDesignNote(View.class, viewName);
	}

	@Override
	public Optional<CollectionDesignElement> getCollection(String name) {
		return getDesignElementByName(CollectionDesignElement.class, name);
	}

	@Override
	public Stream<CollectionDesignElement> getCollections() {
		return getDesignElements(CollectionDesignElement.class);
	}

	@Override
	public Optional<View> getView(String name) {
		return getDesignElementByName(View.class, name);
	}

	@Override
	public Stream<View> getViews() {
		return getDesignElements(View.class);
	}

	@Override
	public Optional<Folder> getFolder(String name) {
		return getDesignElementByName(Folder.class, name);
	}

	@Override
	public Stream<Folder> getFolders() {
		return getDesignElements(Folder.class);
	}

	@Override
	public Form createForm(String formName) {
		return createDesignNote(Form.class, formName);
	}
	
	@Override
	public Subform createSubform(String subformName) {
		return createDesignNote(Subform.class, subformName);
	}

	@Override
	public Optional<Form> getForm(String name) {
		return getDesignElementByName(Form.class, name);
	}

	@Override
	public Stream<Form> getForms() {
		return getDesignElements(Form.class);
	}
	
	@Override
	public Optional<Subform> getSubform(String name) {
		return getDesignElementByName(Subform.class, name);
	}
	
	@Override
	public Stream<Subform> getSubforms() {
		return getDesignElements(Subform.class);
	}

	@Override
	public DesignAgent createAgent(String agentName) {
		return createDesignNote(DesignAgent.class, agentName);
	}

	@Override
	public Optional<DesignAgent> getAgent(String name) {
		return getDesignElementByName(DesignAgent.class, name);
	}

	@Override
	public Stream<DesignAgent> getAgents() {
		return getDesignElements(DesignAgent.class);
	}
	
	@Override
	public Optional<FileResource> getFileResource(String name) {
		return getDesignElementByName(FileResource.class, name);
	}
	
	@Override
	public Stream<FileResource> getFileResources() {
		return getDesignElements(FileResource.class);
	}
	
	@Override
	public Optional<ScriptLibrary> getScriptLibrary(String name) {
		return getDesignElementByName(ScriptLibrary.class, name);
	}
	
	@Override
	public Stream<ScriptLibrary> getScriptLibraries() {
		return getDesignElements(ScriptLibrary.class);
	}
	
	@Override
	public Optional<ImageResource> getImageResource(String name) {
		return getDesignElementByName(ImageResource.class, name);
	}
	
	@Override
	public Stream<ImageResource> getImageResources() {
		return getDesignElements(ImageResource.class);
	}
	
	@Override
	public DbProperties getDatabaseProperties() {
		Database database = getDatabase();
		// TODO check for cases where the icon note doesn't exist - I _think_ this is possible with fresh DBs
		Document iconNote = database.getDocumentById(NotesConstants.NOTE_ID_SPECIAL | NotesConstants.NOTE_CLASS_ICON).get();
		return (DbProperties)DesignUtil.getDesignMapping(DbProperties.class).getConstructor().apply(iconNote);
	}

	@Override
	public void signAll(Set<DocumentClass> docClass, UserId id, SignCallback callback) {
		docClass.stream()
			.flatMap(c -> findDesignNotes(c, null))
			.map(entry -> entry.toDesignElement(database))
			.forEach(element -> {
				if(callback.shouldSign(element, id.getUsername())) {
					element.sign(id);
				}
			});
	}
	
	protected Database getDatabase() {
		return database;
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	/**
	 * Creates a new design note of the provided class. This creates the backing {@link Document},
	 * sets its class, constructs the {@link DesignElement} subclass, and calls
	 * {@link AbstractDesignElement#initializeNewDesignNote()}.
	 * 
	 * @param <T> the {@link DesignElement} interface implemented by the design class
	 * @param <I> the specific {@link AbstractDesignElement} implementation class used internally
	 * @param designClass a {@link Class} object representing {@code <I>}
	 * @return the newly-created and -initialized design element
	 */
	@SuppressWarnings("unchecked")
	protected <T extends DesignElement, I extends AbstractDesignElement<T>> T createDesignNote(Class<T> designClass) {
		DesignMapping<T, I> mapping = DesignUtil.getDesignMapping(designClass);
		
		Document doc = database.createDocument();
		doc.setDocumentClass(mapping.getNoteClass());
		
		I result = (I)mapping.getConstructor().apply(doc);
		result.initializeNewDesignNote();
		return (T)result;
	}
	
	/**
	 * Creates a new design note of the provided class. This creates the backing {@link Document},
	 * sets its class, constructs the {@link DesignElement} subclass, and calls
	 * {@link AbstractDesignElement#initializeNewDesignNote()}. Additionally, this method calls
	 * {@link DesignElement.NamedDesignElement#setTitle(String...)} before returning the object.
	 * 
	 * @param <T> the {@link DesignElement} interface implemented by the design class
	 * @param designClass a {@link Class} object representing {@code <I>}
	 * @return the newly-created and -initialized design element
	 */
	protected <T extends DesignElement.NamedDesignElement> T createDesignNote(Class<T> designClass, String title) {
		T element = createDesignNote(designClass);
		element.setTitle(title);
		return element;
	}
	
	/**
	 * Queries the design collection for a single design note.
	 * 
	 * @param noteClass the class of note to query (see <code>NOTE_CLASS_*</code> in {@link NotesConstants})
	 * @param pattern the note flag pattern to query (see <code>DFLAGPAT_*</code> in {@link NotesConstants})
	 * @param name the name or alias of the design note
	 * @param partialMatch whether partial matches are allowed
	 * @return the note ID of the specified design note, or <code>0</code> if the note was not found
	 */
	protected abstract int findDesignNote(DocumentClass noteClass, String pattern, String name, boolean partialMatch);
	
	public Stream<DesignEntry> findDesignNotes(DocumentClass noteClass, String pattern) {
		/*
		 * Design collection columns:
		 * 	- $TITLE (string)
		 * 	- $FormPrivs
		 * 	- $FormUsers
		 * 	- $Body
		 * 	- $Flags (string)
		 * 	- $Class
		 * 	- $Modified (TIMEDATE)
		 * 	- $Comment (string)
		 * 	- $AssistTrigger
		 * 	- $AssistType
		 * 	- $AssistFlags
		 * 	- $AssistFlags2
		 * 	- $UpdatedBy (string)
		 * 	- $$FormScript_0
		 * 	- $LANGUAGE
		 * 	- $Writers
		 *	- $PWriters
		 *	- $FlagsExt
		 *	- $FileSize (number)
		 *	- $MimeType
		 *	- $DesinerVersion (string)
		 */

		DominoCollection designColl = database.openDesignCollection();
		CollectionSearchQuery query = designColl.query()
			.readDocumentClass()
			.readColumnValues();
		boolean hasPattern = pattern != null && !pattern.isEmpty();
		
		return query.build(0, Integer.MAX_VALUE, new CollectionSearchQuery.CollectionEntryProcessor<List<DesignEntry>>() {
			@Override
			public List<DesignEntry> start() {
				return new LinkedList<>();
			}

			@Override
			public Action entryRead(List<DesignEntry> result, CollectionEntry entry) {
				DocumentClass entryClass = entry.getDocumentClass().orElse(null);
				if(noteClass.equals(entryClass)) {
					if(hasPattern) {
						String flags = entry.get(4, String.class, ""); //$NON-NLS-1$
						if(DesignUtil.matchesFlagsPattern(flags, pattern)) {
							result.add(new DesignEntry(entry.getNoteID(), entryClass, entry));
						}
					} else {
						result.add(new DesignEntry(entry.getNoteID(), entryClass, entry));
					}
				}
				return Action.Continue;
			}

			@Override
			public List<DesignEntry> end(List<DesignEntry> result) {
				return result;
			}
		})
		.stream();
	}
	
	public static class DesignEntry {
		private final int noteId;
		private final DocumentClass noteClass;
		private final CollectionEntry entry;
		
		public DesignEntry(int noteId, DocumentClass noteClass, CollectionEntry entry) {
			this.noteId = noteId;
			this.noteClass = noteClass;
			this.entry = entry;
		}
		
		public DesignElement toDesignElement(Database database) {
			return DesignUtil.createDesignElement(database, noteId, noteClass, getFlags(), Optional.empty());
		}
		
		public List<String> getTitles() {
			return DesignUtil.toTitlesList(entry.getAsList(0, String.class, Collections.emptyList()));
		}
		
		public String getComment() {
			return entry.get(7, String.class, ""); //$NON-NLS-1$
		}
		
		public String getFlags() {
			return entry.get(4, String.class, ""); //$NON-NLS-1$
		}
		
		public String getLanguage() {
			return entry.get(14, String.class, ""); //$NON-NLS-1$
		}
		
		public int getNoteId() {
			return noteId;
		}

		@Override
		public String toString() {
			return String.format("DesignEntry [noteId=%s, noteClass=%s, entry=%s]", noteId, noteClass, entry); //$NON-NLS-1$
		}
	}
}
