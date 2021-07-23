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

import static com.hcl.domino.misc.NotesConstants.DESIGN_FLAGS;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Function;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
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

/**
 * @since 1.0.18
 */
public enum DesignUtil {
	;
	
	/**
	 * Represents the design-note properties mapped to a {@link DesignElement} interface.
	 * 
	 * @param <T> the {@link DesignElement} type produced by the mapping
	 * @param <I> the implementation class used by the constructor
	 */
	public static class DesignMapping<T extends DesignElement, I extends AbstractDesignElement<T>> {
		private final DocumentClass noteClass;
		private final String flagsPattern;
		private final Function<Document, I> constructor;
		
		protected DesignMapping(DocumentClass noteClass, String flagsPattern, Function<Document, I> constructor) {
			this.noteClass = noteClass;
			this.flagsPattern = flagsPattern;
			this.constructor = constructor;
		}

		public DocumentClass getNoteClass() {
			return noteClass;
		}

		public String getFlagsPattern() {
			return flagsPattern;
		}

		public Function<Document, I> getConstructor() {
			return constructor;
		}
	}
	private static final Map<Class<? extends DesignElement>, DesignMapping<? extends DesignElement, ? extends AbstractDesignElement<?>>> mappings = new HashMap<>();
	static {
		mappings.put(View.class,
			new DesignMapping<View, ViewImpl>(DocumentClass.VIEW,  NotesConstants.DFLAGPAT_VIEW_ALL_VERSIONS, ViewImpl::new)
		);
		mappings.put(Folder.class,
			new DesignMapping<Folder, FolderImpl>(DocumentClass.VIEW, NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS, FolderImpl::new)
		);
		mappings.put(CollectionDesignElement.class,
			new DesignMapping<CollectionDesignElement, AbstractCollectionDesignElement<CollectionDesignElement>>(
				DocumentClass.VIEW,
				NotesConstants.DFLAGPAT_VIEWS_AND_FOLDERS_DESIGN,
				doc -> {
					String flags = doc.getAsText(DESIGN_FLAGS, ' ');
					if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS)) {
						@SuppressWarnings("unchecked")
						AbstractCollectionDesignElement<CollectionDesignElement> result = (AbstractCollectionDesignElement<CollectionDesignElement>)(AbstractCollectionDesignElement<?>)new FolderImpl(doc);
						return result;
					} else {
						@SuppressWarnings("unchecked")
						AbstractCollectionDesignElement<CollectionDesignElement> result = (AbstractCollectionDesignElement<CollectionDesignElement>)(AbstractCollectionDesignElement<?>)new ViewImpl(doc);
						return result;
					}
				}
			)
		);
		
		mappings.put(Form.class,
			new DesignMapping<Form, FormImpl>(DocumentClass.FORM, NotesConstants.DFLAGPAT_FORM_ALL_VERSIONS, FormImpl::new)
		);
		mappings.put(Subform.class,
			new DesignMapping<Subform, SubformImpl>(DocumentClass.FORM, NotesConstants.DFLAGPAT_SUBFORM_ALL_VERSIONS, SubformImpl::new)
		);
		mappings.put(DesignAgent.class,
			new DesignMapping<DesignAgent, AgentImpl>(DocumentClass.FILTER, NotesConstants.DFLAGPAT_AGENTSLIST, AgentImpl::new)
		);
		mappings.put(DbProperties.class,
			new DesignMapping<DbProperties, DbPropertiesImpl>(DocumentClass.ICON, "", DbPropertiesImpl::new) //$NON-NLS-1$
		);
		mappings.put(FileResource.class,
			new DesignMapping<FileResource, FileResourceImpl>(DocumentClass.FORM, NotesConstants.DFLAGPAT_FILE_RESOURCE, FileResourceImpl::new)
		);
		mappings.put(ScriptLibrary.class,
			new DesignMapping<ScriptLibrary, AbstractScriptLibrary<ScriptLibrary>>(
				DocumentClass.FILTER,
				NotesConstants.DFLAGPAT_SCRIPTLIB,
				doc -> {
					String flags = doc.getAsText(DESIGN_FLAGS, ' ');
					if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JAVA)) {
						@SuppressWarnings("unchecked")
						AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>)(AbstractScriptLibrary<?>)new JavaLibraryImpl(doc);
						return result;
					} else if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JS)) {
						@SuppressWarnings("unchecked")
						AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>)(AbstractScriptLibrary<?>)new JavaScriptLibraryImpl(doc);
						return result;
					} else if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_LS)) {
						@SuppressWarnings("unchecked")
						AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>)(AbstractScriptLibrary<?>)new LotusScriptLibraryImpl(doc);
						return result;
					} else if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
						@SuppressWarnings("unchecked")
						AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>)(AbstractScriptLibrary<?>)new ServerJavaScriptLibraryImpl(doc);
						return result;
					} else {
						throw new UnsupportedOperationException(MessageFormat.format("Unable to find implementation for flags value \"{0}\"", flags));
					}
				}
			)
		);
		mappings.put(ImageResource.class,
			new DesignMapping<ImageResource, ImageResourceImpl>(DocumentClass.FORM, NotesConstants.DFLAGPAT_IMAGE_RESOURCE, ImageResourceImpl::new)
		);
	}
	
	/**
	 * Retrieves the {@link DesignMapping} object that corresponds to the provided
	 * design element interface.
	 * 
	 * @param <T> a {@link DesignElement} sub-interface
	 * @param designClass a {@link Class} object representing {@code <T>}
	 * @return the corresponding {@code DesignMapping}
	 * @throws IllegalArgumentException when {@code <T>} has no mapping representation
	 */
	public static <T extends DesignElement, I extends AbstractDesignElement<T>> DesignMapping<T, I> getDesignMapping(Class<T> designClass) {
		@SuppressWarnings("unchecked")
		DesignMapping<T, I> mapping = (DesignMapping<T, I>)mappings.get(designClass);
		if(mapping == null) {
			throw new IllegalArgumentException(MessageFormat.format("Unsupported design class \"{0}\"", designClass.getName()));
		}
		return mapping;
	}
	
	/**
	 * @param flags a design flag value to test
	 * @param pattern a flag pattern to test against (DFLAGPAT_*)
	 * @return whether the flags match the pattern
	 */
	public static boolean matchesFlagsPattern(String flags, String pattern) {
		if(pattern == null || pattern.isEmpty()) {
			return false;
		}
		
		String toTest = flags == null ? "" : flags; //$NON-NLS-1$
		
		// Patterns start with one of four characters:
		// "+" (match any)
		// "-" (match none)
		// "*" (match all)
		// "(" (multi-part test)
		String matchers = null;
		String antiMatchers = null;
		String allMatchers = null;
		char first = pattern.charAt(0);
		switch(first) {
		case '+':
			matchers = pattern.substring(1);
			antiMatchers = ""; //$NON-NLS-1$
			allMatchers = ""; //$NON-NLS-1$
			break;
		case '-':
			matchers = ""; //$NON-NLS-1$
			antiMatchers = pattern.substring(1);
			allMatchers = ""; //$NON-NLS-1$
			break;
		case '*':
			matchers = ""; //$NON-NLS-1$
			antiMatchers = ""; //$NON-NLS-1$
			allMatchers = pattern.substring(1);
			break;
		case '(':
			// The order is always +-*
			int plusIndex = pattern.indexOf('+');
			int minusIndex = pattern.indexOf('-');
			int starIndex = pattern.indexOf('*');
			
			matchers = pattern.substring(plusIndex+1, minusIndex == -1 ? pattern.length() : minusIndex);
			antiMatchers = minusIndex == -1 ? "" : pattern.substring(minusIndex+1, starIndex == -1 ? pattern.length() : starIndex); //$NON-NLS-1$
			allMatchers = starIndex == -1 ? "" : pattern.substring(starIndex+1); //$NON-NLS-1$
			break;
		default:
			// Not a matcher pattern
		}
		if(matchers == null) { matchers = ""; } //$NON-NLS-1$
		if(antiMatchers == null) { antiMatchers = ""; } //$NON-NLS-1$
		if(allMatchers == null) { allMatchers = ""; } //$NON-NLS-1$
		
		// Test "match against any" and fail if it doesn't
		boolean matchedAny = matchers.isEmpty();
		for(int i = 0; i < matchers.length(); i++) {
			if(toTest.indexOf(matchers.charAt(i)) > -1) {
				matchedAny = true;
				break;
			}
		}
		if(!matchedAny) {
			return false;
		}
		
		// Test "match none" and fail if it does
		for(int i = 0; i < antiMatchers.length(); i++) {
			if(toTest.indexOf(antiMatchers.charAt(i)) > -1) {
				// Exit immediately
				return false;
			}
		}
		
		// Test "match all" and fail if it doesn't
		for(int i = 0; i < allMatchers.length(); i++) {
			if(toTest.indexOf(allMatchers.charAt(i)) == -1) {
				// Exit immediately
				return false;
			}
		}
		
		// If we survived to here, it must match
		return true;
	}
	
	/**
	 * Constructs a {@link DesignElement} instance for the provided document.
	 * 
	 * @param doc the design note to wrap
	 * @return the newly-constructed {@link DesignElement} instance
	 */
	public static DesignElement createDesignElement(Document doc) {
		String flags = doc.getAsText(DESIGN_FLAGS, ' ');
		DocumentClass noteClass = doc.getDocumentClass().iterator().next();
		return createDesignElement(null, 0, noteClass, flags, Optional.of(doc));
	}
	
	/**
	 * Constructs a {@link DesignElement} instance for the provided document information.
	 * 
	 * @param database the {@link Database} housing the design note; may be {@code null} if {@code doc} is provided
	 * @param noteId the note ID of the design note; may be {@code 0} if {@code doc} is provided
	 * @param noteClass the {@link DocumentClass} of the design note; may be {@code null} if {@code doc} is provided
	 * @param flags the {@code $Flags} value of the design note; may be {@code null} if {@code noteClass} is unambiguous
	 * @param doc the design note to wrap
	 * @return the newly-constructed {@link DesignElement} instance
	 */
	public static DesignElement createDesignElement(Database database, int noteId, DocumentClass noteClass, String flags, Optional<Document> doc) {
		switch(noteClass) {
		case ACL:
			throw new NotYetImplementedException();
		case FIELD:
			throw new NotYetImplementedException();
		case FILTER:
			return new AgentImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
		case FORM:
			// TODO handle the flood of edge cases here
			if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SUBFORM_ALL_VERSIONS)) {
				return new SubformImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
			} else if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FILE_RESOURCE)) {
				return new FileResourceImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
			} else {
				return new FormImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
			}
		case HELP:
			throw new NotYetImplementedException();
		case HELP_INDEX:
			throw new NotYetImplementedException();
		case ICON:
			throw new NotYetImplementedException();
		case INFO:
			throw new NotYetImplementedException();
		case REPLFORMULA:
			throw new NotYetImplementedException();
		case VIEW:
			if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS)) {
				return new FolderImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
			} else {
				return new ViewImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
			}
		case ALL:
		case ALLNONDATA:
		case DATA:
		case DOCUMENT:
		case DEFAULT:
		case DESIGNCOLLECTION:
		case SINGLE_INSTANCE:
		case PRIVATE:
		case NOTIFYDELETION:
		case NONE:
		default:
			throw new IllegalArgumentException(MessageFormat.format("Unsupported design note type {0}", noteClass));
		}
	}
	
	/**
	 * Converts the provided title to its "normalized" form for the purposes of lookups.
	 * 
	 * <p>Specifically, this removes the "_" characters used for UI hints and strips surrounding
	 * parentheses as used to hide elements.</p>
	 * 
	 * @param title the design element title to normalize
	 * @return the normalized variant of the title
	 */
	public static String normalizeTitle(String title) {
		if(title == null) {
			return ""; //$NON-NLS-1$
		} else {
			String result = title.replace("_", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if(result.charAt(0) == '(' && result.charAt(result.length()-1) == ')') {
				result = result.substring(1, result.length()-1);
			}
			return result;
		}
	}
	
	/**
	 * Determines whether the provided name is a match for any of the design-element titles.
	 * 
	 * @param name the name of the design element to look for
	 * @param titleValues the {@code $TITLE} item value from a design element
	 * @return whether {@code name} matches any of the values in {@code $TITLE}
	 */
	public static boolean matchesTitleValues(String name, List<String> titleValues) {
		String normalName = normalizeTitle(name);
		return titleValues.stream()
			.flatMap(val -> Arrays.stream(val.split("\\|"))) //$NON-NLS-1$
			.map(DesignUtil::normalizeTitle)
			.anyMatch(normalName::equalsIgnoreCase);
	}
	
	/**
	 * Converts the provided $TITLE values - which may be multi-value or pipe-separated -
	 * info a standardized list of titles and aliases.
	 * 
	 * @param values the item or entry values to standardize
	 * @return a {@link List} of title and alias values
	 * @since 1.0.27
	 */
	public static List<String> toTitlesList(List<String> values) {
		if(values == null || values.isEmpty()) {
			return Arrays.asList(""); //$NON-NLS-1$
		}
		List<String> result = new ArrayList<>();
		for(String val : values) {
			StringTokenizer tokenizer = new StringTokenizer(val, "|"); //$NON-NLS-1$
			while(tokenizer.hasMoreElements()) {
				result.add(tokenizer.nextToken());
			}
		}
		return result;
	}
}
