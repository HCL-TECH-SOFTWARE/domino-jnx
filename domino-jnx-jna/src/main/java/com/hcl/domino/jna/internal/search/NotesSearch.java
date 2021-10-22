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
package com.hcl.domino.jna.internal.search;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.commons.views.Search;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Database.DocFlags;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Database.SearchMatch;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dbdirectory.FileType;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADatabase.DbMode;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.data.JNAIDTable;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesOriginatorIdData;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAIDTableAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserNamesListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.views.NotesLookupResultBufferDecoder;
import com.hcl.domino.jna.internal.views.ViewFormulaCompiler;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * Utility class to search Notes data
 * 
 * @author Karsten Lehmann
 */
public class NotesSearch {
	public static final String PROP_USELARGEDATA = "jnx.nsfsearch.uselargedata"; //$NON-NLS-1$
	public static final String ENV_USELARGEDATA = "JNX_NSFSEARCH_USELARGEDATA"; //$NON-NLS-1$

	private static Boolean useLargeDataForSearch;

	/**
	 * This function scans all the notes in a database, ID table or files in a directory.<br>
	 * <br>
	 * Based on several search criteria, the function calls a user-supplied routine (an action routine)
	 * for every note or file that matches the criteria. NSFSearch is a powerful function that provides
	 * the general search mechanism for tasks that process all or some of the documents in a
	 * database or all or some of the databases in a directory.<br>
	 * <br>
	 * Specify a formula argument to improve efficiency when processing a subset of the notes in a database.<br>
	 * <br>
	 * In addition, the formula argument can be used to return computed "on-the-fly" information.<br>
	 * <br>
	 * To do this, you specify that a value returned from a formula is to be stored in a
	 * temporary field of each note.<br>
	 * <br>
	 * This temporary field and its value is then accessible in the summary buffer received by
	 * the NSFSearch action routine without having to open the note.<br>
	 * <br>
	 * For example, suppose you want the size of each note found by NSFSearch.<br>
	 * Do the following before the call to NSFSearch:<br>
	 * Call search with a formula like this:<br>
	 * "DEFAULT dLength := @DocLength; @All"<br>
	 * and specify {@link Search#SUMMARY} for the SearchFlags argument.<br>
	 * <br>
	 * In the action routine of NSFSearch, if you get a search match, look at the summary information.<br>
	 * The dLength field will be one of the items in the summary information buffer.<br>
	 * <br>
	 * Specify a note class to restrict the search to certain classes of notes.<br>
	 * Specify {@link NotesConstants#NOTE_CLASS_DOCUMENT} to find documents.<br>
	 * Specify the "since" argument to limit the search to notes created or modified
	 * in the database since a certain time/date.<br>
	 * When used to search a database, NSFSearch will search the database file sequentially
	 * if NULL is passed as the "Since" time.<br>
	 * If the search is not time-constrained (the "Since" argument is NULL or specifies
	 * the TIMEDATE_WILDCARD, ANYDAY/ALLDAY), then NSFSearch may find a given note more
	 * than once during the same search. If a non-time-constrained search passes a
	 * certain note to the action routine, and that note is subsequently updated,
	 * then NSFSearch may find that note again and pass it to the action routine a
	 * second time during the same search. This may happen if Domino or Notes relocates
	 * the updated note to a position farther down in the file. If your algorithm requires
	 * processing each note once and only once, then use time-constrained searches.<br>
	 * Save the return value of type {@link JNADominoDateTime} of the present search and use
	 * that as the "Since" time on the next search.<br>
	 * <br>
	 * Alternatively, build an ID table as you search, avoid updating notes in the action
	 * routine, and process the ID table after the search completes. ID tables are
	 * guaranteed not to contain a given ID more than once.
	 * 
	 * @param db database to search in
	 * @param searchFilter optional search scope as {@link JNAIDTable} or null
	 * @param formula formula or null
	 * @param viewTitle optional view title that will be returned for "@ ViewTitle" within the formula or null
	 * @param searchFlags flags to control searching ({@link Search})
	 * @param noteClasses noteclasses to search
	 * @param since The date of the earliest modified note that is matched. The note's "Modified in this file" date is compared to this date. Specify NULL if you do not wish any filtering by date.
	 * @param callback callback to be called for every found note
	 * @return The ending (current) time/date of this search. Returned so that it can be used in a subsequent call to {@link #search} as the "Since" argument.
	 * @throws FormulaCompilationException if formula syntax is invalid
	 */
	public static JNADominoDateTime search(final JNADatabase db, JNAIDTable searchFilter, final String formula, String viewTitle, final Set<SearchFlag> searchFlags, Set<DocumentClass> noteClasses, JNADominoDateTime since, final SearchCallback callback) throws FormulaCompilationException {
		return search(db, searchFilter, formula, null, viewTitle, searchFlags, DominoEnumUtil.toBitField(DocumentClass.class, noteClasses), since, callback);
	}
	
	/**
	 * This function scans all the notes in a database, ID table or files in a directory.<br>
	 * <br>
	 * Based on several search criteria, the function calls a user-supplied routine (an action routine)
	 * for every note or file that matches the criteria. NSFSearch is a powerful function that provides
	 * the general search mechanism for tasks that process all or some of the documents in a
	 * database or all or some of the databases in a directory.<br>
	 * <br>
	 * Specify a formula argument to improve efficiency when processing a subset of the notes in a database.<br>
	 * <br>
	 * In addition, the formula argument can be used to return computed "on-the-fly" information.<br>
	 * <br>
	 * To do this, you specify that a value returned from a formula is to be stored in a
	 * temporary field of each note.<br>
	 * <br>
	 * This temporary field and its value is then accessible in the summary buffer received by
	 * the NSFSearch action routine without having to open the note.<br>
	 * <br>
	 * For example, suppose you want the size of each note found by NSFSearch.<br>
	 * Do the following before the call to NSFSearch:<br>
	 * Call search with a formula like this:<br>
	 * "DEFAULT dLength := @DocLength; @All"<br>
	 * and specify {@link Search#SUMMARY} for the SearchFlags argument.<br>
	 * <br>
	 * In the action routine of NSFSearch, if you get a search match, look at the summary information.<br>
	 * The dLength field will be one of the items in the summary information buffer.<br>
	 * <br>
	 * Specify a note class to restrict the search to certain classes of notes.<br>
	 * Specify {@link NotesConstants#NOTE_CLASS_DOCUMENT} to find documents.<br>
	 * Specify the "since" argument to limit the search to notes created or modified
	 * in the database since a certain time/date.<br>
	 * When used to search a database, NSFSearch will search the database file sequentially
	 * if NULL is passed as the "Since" time.<br>
	 * If the search is not time-constrained (the "Since" argument is NULL or specifies
	 * the TIMEDATE_WILDCARD, ANYDAY/ALLDAY), then NSFSearch may find a given note more
	 * than once during the same search. If a non-time-constrained search passes a
	 * certain note to the action routine, and that note is subsequently updated,
	 * then NSFSearch may find that note again and pass it to the action routine a
	 * second time during the same search. This may happen if Domino or Notes relocates
	 * the updated note to a position farther down in the file. If your algorithm requires
	 * processing each note once and only once, then use time-constrained searches.<br>
	 * Save the return value of type {@link JNADominoDateTime} of the present search and use
	 * that as the "Since" time on the next search.<br>
	 * <br>
	 * Alternatively, build an ID table as you search, avoid updating notes in the action
	 * routine, and process the ID table after the search completes. ID tables are
	 * guaranteed not to contain a given ID more than once.
	 * 
	 * @param db database to search in
	 * @param searchFilter optional search scope as {@link JNAIDTable} or null
	 * @param formula formula or null
	 * @param columnFormulas map with programmatic column names (key) and formulas (value) with keys sorted in column order or null to output all items
	 * @param viewTitle optional view title that will be returned for "@ ViewTitle" within the formula or null
	 * @param searchFlags flags to control searching ({@link SearchFlag})
	 * @param noteClasses noteclasses to search
	 * @param since The date of the earliest modified note that is matched. The note's "Modified in this file" date is compared to this date. Specify NULL if you do not wish any filtering by date.
	 * @param callback callback to be called for every found note
	 * @return The ending (current) time/date of this search. Returned so that it can be used in a subsequent call to {@link #search} as the "Since" argument.
	 * @throws FormulaCompilationException if formula syntax is invalid
	 */
	public static JNADominoDateTime search(final JNADatabase db, JNAIDTable searchFilter, final String formula, Map<String,String> columnFormulas, String viewTitle, final Set<SearchFlag> searchFlags, Set<DocumentClass> noteClasses, JNADominoDateTime since, final SearchCallback callback) throws FormulaCompilationException {
		return search(db, searchFilter, formula, columnFormulas, viewTitle, searchFlags, DominoEnumUtil.toBitField(DocumentClass.class, noteClasses), since, callback);
	}
	
	/**
	 * This function scans all the notes in a database, ID table or files in a directory.<br>
	 * <br>
	 * Based on several search criteria, the function calls a user-supplied routine (an action routine)
	 * for every note or file that matches the criteria. NSFSearch is a powerful function that provides
	 * the general search mechanism for tasks that process all or some of the documents in a
	 * database or all or some of the databases in a directory.<br>
	 * <br>
	 * Specify a formula argument to improve efficiency when processing a subset of the notes in a database.<br>
	 * <br>
	 * In addition, the formula argument can be used to return computed "on-the-fly" information.<br>
	 * <br>
	 * To do this, you specify that a value returned from a formula is to be stored in a
	 * temporary field of each note.<br>
	 * <br>
	 * This temporary field and its value is then accessible in the summary buffer received by
	 * the NSFSearch action routine without having to open the note.<br>
	 * <br>
	 * For example, suppose you want the size of each note found by NSFSearch.<br>
	 * Do the following before the call to NSFSearch:<br>
	 * Call search with a formula like this:<br>
	 * "DEFAULT dLength := @DocLength; @All"<br>
	 * and specify {@link Search#SUMMARY} for the SearchFlags argument.<br>
	 * <br>
	 * In the action routine of NSFSearch, if you get a search match, look at the summary information.<br>
	 * The dLength field will be one of the items in the summary information buffer.<br>
	 * <br>
	 * Specify a note class to restrict the search to certain classes of notes.<br>
	 * Specify {@link NotesConstants#NOTE_CLASS_DOCUMENT} to find documents.<br>
	 * Specify the "since" argument to limit the search to notes created or modified
	 * in the database since a certain time/date.<br>
	 * When used to search a database, NSFSearch will search the database file sequentially
	 * if NULL is passed as the "Since" time.<br>
	 * If the search is not time-constrained (the "Since" argument is NULL or specifies
	 * the TIMEDATE_WILDCARD, ANYDAY/ALLDAY), then NSFSearch may find a given note more
	 * than once during the same search. If a non-time-constrained search passes a
	 * certain note to the action routine, and that note is subsequently updated,
	 * then NSFSearch may find that note again and pass it to the action routine a
	 * second time during the same search. This may happen if Domino or Notes relocates
	 * the updated note to a position farther down in the file. If your algorithm requires
	 * processing each note once and only once, then use time-constrained searches.<br>
	 * Save the return value of type {@link JNADominoDateTime} of the present search and use
	 * that as the "Since" time on the next search.<br>
	 * <br>
	 * Alternatively, build an ID table as you search, avoid updating notes in the action
	 * routine, and process the ID table after the search completes. ID tables are
	 * guaranteed not to contain a given ID more than once.
	 * 
	 * @param db database to search in
	 * @param searchFilter optional search scope as {@link JNAIDTable} or null
	 * @param formula formula or null
	 * @param viewTitle optional view title that will be returned for "@ ViewTitle" within the formula or null
	 * @param searchFlags flags to control searching ({@link SearchFlag})
	 * @param fileTypes filetypes to search
	 * @param since The date of the earliest modified note that is matched. The note's "Modified in this file" date is compared to this date. Specify NULL if you do not wish any filtering by date.
	 * @param callback callback to be called for every found note
	 * @return The ending (current) time/date of this search. Returned so that it can be used in a subsequent call to {@link #search} as the "Since" argument.
	 * @throws FormulaCompilationException if formula syntax is invalid
	 */
	public static JNADominoDateTime searchFiles(final JNADatabase db, Object searchFilter, final String formula, String viewTitle, final Set<SearchFlag> searchFlags, EnumSet<FileType> fileTypes, JNADominoDateTime since, final SearchCallback callback) throws FormulaCompilationException {
		return search(db, searchFilter, formula, null, viewTitle, searchFlags, FileType.toBitMaskInt(fileTypes), since, callback);
	}
	
	/**
	 * This function scans all the notes in a database, ID table or files in a directory.<br>
	 * <br>
	 * Based on several search criteria, the function calls a user-supplied routine (an action routine)
	 * for every note or file that matches the criteria. NSFSearch is a powerful function that provides
	 * the general search mechanism for tasks that process all or some of the documents in a
	 * database or all or some of the databases in a directory.<br>
	 * <br>
	 * Specify a formula argument to improve efficiency when processing a subset of the notes in a database.<br>
	 * <br>
	 * In addition, the formula argument can be used to return computed "on-the-fly" information.<br>
	 * <br>
	 * To do this, you specify that a value returned from a formula is to be stored in a
	 * temporary field of each note.<br>
	 * <br>
	 * This temporary field and its value is then accessible in the summary buffer received by
	 * the NSFSearch action routine without having to open the note.<br>
	 * <br>
	 * For example, suppose you want the size of each note found by NSFSearch.<br>
	 * Do the following before the call to NSFSearch:<br>
	 * Call search with a formula like this:<br>
	 * "DEFAULT dLength := @DocLength; @All"<br>
	 * and specify {@link Search#SUMMARY} for the SearchFlags argument.<br>
	 * <br>
	 * In the action routine of NSFSearch, if you get a search match, look at the summary information.<br>
	 * The dLength field will be one of the items in the summary information buffer.<br>
	 * <br>
	 * Specify a note class to restrict the search to certain classes of notes.<br>
	 * Specify {@link NotesConstants#NOTE_CLASS_DOCUMENT} to find documents.<br>
	 * Specify the "since" argument to limit the search to notes created or modified
	 * in the database since a certain time/date.<br>
	 * When used to search a database, NSFSearch will search the database file sequentially
	 * if NULL is passed as the "Since" time.<br>
	 * If the search is not time-constrained (the "Since" argument is NULL or specifies
	 * the TIMEDATE_WILDCARD, ANYDAY/ALLDAY), then NSFSearch may find a given note more
	 * than once during the same search. If a non-time-constrained search passes a
	 * certain note to the action routine, and that note is subsequently updated,
	 * then NSFSearch may find that note again and pass it to the action routine a
	 * second time during the same search. This may happen if Domino or Notes relocates
	 * the updated note to a position farther down in the file. If your algorithm requires
	 * processing each note once and only once, then use time-constrained searches.<br>
	 * Save the return value of type {@link JNADominoDateTime} of the present search and use
	 * that as the "Since" time on the next search.<br>
	 * <br>
	 * Alternatively, build an ID table as you search, avoid updating notes in the action
	 * routine, and process the ID table after the search completes. ID tables are
	 * guaranteed not to contain a given ID more than once.
	 * 
	 * @param db database to search in
	 * @param searchFilter optional search scope as {@link JNAIDTable} or null
	 * @param formula formula or null
	 * @param columnFormulas map with programmatic column names (key) and formulas (value) with keys sorted in column order or null to output all items
	 * @param viewTitle optional view title that will be returned for "@ ViewTitle" within the formula or null
	 * @param searchFlags flags to control searching ({@link Search})
	 * @param fileTypes filetypes to search
	 * @param since The date of the earliest modified note that is matched. The note's "Modified in this file" date is compared to this date. Specify NULL if you do not wish any filtering by date.
	 * @param callback callback to be called for every found note
	 * @return The ending (current) time/date of this search. Returned so that it can be used in a subsequent call to {@link #search} as the "Since" argument.
	 * @throws FormulaCompilationException if formula syntax is invalid
	 */
	public static JNADominoDateTime searchFiles(final JNADatabase db, Object searchFilter, final String formula, Map<String,String> columnFormulas, String viewTitle, final EnumSet<SearchFlag> searchFlags, EnumSet<FileType> fileTypes, JNADominoDateTime since, final SearchCallback callback) throws FormulaCompilationException {
		return search(db, searchFilter, formula, columnFormulas, viewTitle, searchFlags, FileType.toBitMaskInt(fileTypes), since, callback);
	}
	
	private static boolean isUseLargeNSFSearchData(DominoClient client) {
		if (useLargeDataForSearch==null) {
			//large data requires a local R12 C API (remote server version not relevant)
			if (client.getBuildVersion("").getMajorVersion() >= 12) { //$NON-NLS-1$
				String str = System.getProperty(PROP_USELARGEDATA);

				if (StringUtil.isEmpty(str)) {
					str = System.getenv(ENV_USELARGEDATA);
				}

				//enable by default on R12
				useLargeDataForSearch = !"false".equalsIgnoreCase(str); //$NON-NLS-1$
			}
			else {
				useLargeDataForSearch = Boolean.FALSE;
			}
		}
		return useLargeDataForSearch;
	}
	
	/**
	 * This function scans all the notes in a database, ID table or files in a directory.<br>
	 * <br>
	 * Based on several search criteria, the function calls a user-supplied routine (an action routine)
	 * for every note or file that matches the criteria. NSFSearch is a powerful function that provides
	 * the general search mechanism for tasks that process all or some of the documents in a
	 * database or all or some of the databases in a directory.<br>
	 * <br>
	 * Specify a formula argument to improve efficiency when processing a subset of the notes in a database.<br>
	 * <br>
	 * In addition, the formula argument can be used to return computed "on-the-fly" information.<br>
	 * <br>
	 * To do this, you specify that a value returned from a formula is to be stored in a
	 * temporary field of each note.<br>
	 * <br>
	 * This temporary field and its value is then accessible in the summary buffer received by
	 * the NSFSearch action routine without having to open the note.<br>
	 * <br>
	 * For example, suppose you want the size of each note found by NSFSearch.<br>
	 * Do the following before the call to NSFSearch:<br>
	 * Call search with a formula like this:<br>
	 * "DEFAULT dLength := @DocLength; @All"<br>
	 * and specify {@link Search#SUMMARY} for the SearchFlags argument.<br>
	 * <br>
	 * In the action routine of NSFSearch, if you get a search match, look at the summary information.<br>
	 * The dLength field will be one of the items in the summary information buffer.<br>
	 * <br>
	 * Specify a note class to restrict the search to certain classes of notes.<br>
	 * Specify {@link NotesConstants#NOTE_CLASS_DOCUMENT} to find documents.<br>
	 * Specify the "since" argument to limit the search to notes created or modified
	 * in the database since a certain time/date.<br>
	 * When used to search a database, NSFSearch will search the database file sequentially
	 * if NULL is passed as the "Since" time.<br>
	 * If the search is not time-constrained (the "Since" argument is NULL or specifies
	 * the TIMEDATE_WILDCARD, ANYDAY/ALLDAY), then NSFSearch may find a given note more
	 * than once during the same search. If a non-time-constrained search passes a
	 * certain note to the action routine, and that note is subsequently updated,
	 * then NSFSearch may find that note again and pass it to the action routine a
	 * second time during the same search. This may happen if Domino or Notes relocates
	 * the updated note to a position farther down in the file. If your algorithm requires
	 * processing each note once and only once, then use time-constrained searches.<br>
	 * Save the return value of type {@link NotesTimeDate} of the present search and use
	 * that as the "Since" time on the next search.<br>
	 * <br>
	 * Alternatively, build an ID table as you search, avoid updating notes in the action
	 * routine, and process the ID table after the search completes. ID tables are
	 * guaranteed not to contain a given ID more than once.
	 * 
	 * @param db database to search in
	 * @param searchFilter optional search scope as {@link NotesIDTable} or null
	 * @param formula formula or null
	 * @param columnFormulas map with programmatic column names (key) and formulas (value) with keys sorted in column order or null to output all items; automatically uses {@link SearchFlag#NOITEMNAMES} and {@link SearchFlag#SUMMARY} search flag
	 * @param viewTitle optional view title that will be returned for "@ ViewTitle" within the formula or null
	 * @param searchFlags flags to control searching ({@link SearchFlag})
	 * @param noteClassMask bitmask of {@link NoteClass} or {@link FileType} to search
	 * @param since The date of the earliest modified note that is matched. The note's "Modified in this file" date is compared to this date. Specify NULL if you do not wish any filtering by date.
	 * @param callback callback to be called for every found note
	 * @return The ending (current) time/date of this search. Returned so that it can be used in a subsequent call to {@link #search(NotesDatabase, Object, String, String, EnumSet, int, NotesTimeDate, SearchCallback)} as the "Since" argument.
	 * @throws FormulaCompilationException if formula syntax is invalid
	 */
	private static JNADominoDateTime search(final JNADatabase db, Object searchFilter, final String formula, Map<String,String> columnFormulas, String viewTitle,
			final Set<SearchFlag> searchFlags, int noteClassMask, JNADominoDateTime since,
			final SearchCallback callback) throws FormulaCompilationException {
		
		if (db.isDisposed()) {
			throw new DominoException("Database already recycled");
		}

		boolean hasLargeSearchSupport = isUseLargeNSFSearchData(db.getParentDominoClient());
		
		if (searchFilter instanceof JNAIDTable) {
			if (since==null) {
				//in R9, since must have any value to make this work in NSFSearchExtended3, so we use one in the past
				//hard coded innards to work around a date conversion issue for dates in the past
				since = new JNADominoDateTime(new int[] {-360000, -1054549587});
			}
			if (StringUtil.isEmpty(viewTitle)) {
				//in R9, view title cannot be empty if filtering with IDTable
				viewTitle = "-"; //$NON-NLS-1$
			}
		}

		final NotesTimeDateStruct sinceStruct = since==null ? null : NotesTimeDateStruct.newInstance(since.getInnards());

		final EnumSet<SearchFlag> useSearchFlags = EnumSet.noneOf(SearchFlag.class);
		if(searchFlags != null) {
			useSearchFlags.addAll(searchFlags);
		}
		
		LinkedHashMap<String,String> columnFormulasFixedOrder = (columnFormulas==null || columnFormulas.isEmpty()) ? null : new LinkedHashMap<>(columnFormulas);
		
		if (columnFormulasFixedOrder!=null) {
			useSearchFlags.add(SearchFlag.SUMMARY);
			useSearchFlags.add(SearchFlag.NOITEMNAMES);
		}
		
		int searchFlagsBitMask = SearchFlag.toBitMaskStdFlagsInt(useSearchFlags);
		int search1FlagsBitMask = SearchFlag.toBitMaskSearch1Flags(useSearchFlags);
		search1FlagsBitMask = search1FlagsBitMask | NotesConstants.SEARCH1_LARGE_BUCKETS;

		boolean hasLargeSearchResult = hasLargeSearchSupport && useSearchFlags.contains(SearchFlag.SUMMARY);
		if (hasLargeSearchResult) {
			//if the C API supports returning large search results, we request it
			search1FlagsBitMask = search1FlagsBitMask | NotesConstants.SEARCH1_RET_LARGE_DATA;
		}
		
		DbMode mode = db.getMode();

		final Throwable invocationEx[] = new Throwable[1];

		NotesCallbacks.NsfSearchProc apiCallback = new NotesCallbacks.NsfSearchProc() {

			@SuppressWarnings("deprecation")
			@Override
			public short invoke(Pointer enumRoutineParameter, Pointer searchMatchPtr,
					Pointer summaryBufferPtr) {
				JNASearchMatch searchMatch = hasLargeSearchResult ? SearchMatchDecoder.decodeSearchMatchLarge(searchMatchPtr) : SearchMatchDecoder.decodeSearchMatch(searchMatchPtr);
				
				IItemTableData itemTableData=null;
				JNADocument doc=null;
				try {
					boolean isMatch = formula==null || searchMatch.matchesFormula();
					
					if (isMatch && useSearchFlags.contains(SearchFlag.SUMMARY)) {
						if (searchMatch.isLargeSummary()) {
							//summary buffer limit exceeded for Domino V11 and below,
							//we need to load the document to get access to item values
							doc = (JNADocument) db.getDocumentById(searchMatch.getNoteID(), EnumSet.of(OpenDocumentMode.SUMMARY_ONLY, OpenDocumentMode.NOOBJECTS)).orElse(null);
							if (doc!=null) {
								itemTableData = new ItemTableDataDocAdapter(doc, columnFormulasFixedOrder);
							}
						}
						else {
							if (summaryBufferPtr!=null && Pointer.nativeValue(summaryBufferPtr)!=0) {
								boolean convertStringsLazily = true;
								boolean convertNotesTimeDateToCalendar = false;
								
								if (useSearchFlags.contains(SearchFlag.NOITEMNAMES)) {
									//flag to just return the column values is used; so the
									//buffer contains an ITEM_VALUE_TABLE with column values
									//in the column order instead of an ITEM_TABLE with columnname/columnvalue
									//pairs
									//create an ItemTableData by adding the column names to make this invisible to callers
									if (hasLargeSearchSupport) {
										itemTableData = NotesLookupResultBufferDecoder.decodeItemValueTableLargeWithColumnNames(db, searchMatch.getNoteID(), columnFormulasFixedOrder, summaryBufferPtr, convertStringsLazily, convertNotesTimeDateToCalendar, false);
									}
									else {
										itemTableData = NotesLookupResultBufferDecoder.decodeItemValueTableWithColumnNames(db, searchMatch.getNoteID(), columnFormulasFixedOrder, summaryBufferPtr, convertStringsLazily, convertNotesTimeDateToCalendar, false);
									}
								}
								else {
									if (hasLargeSearchSupport) {
										itemTableData = NotesLookupResultBufferDecoder.decodeItemTableLarge(summaryBufferPtr,
												convertStringsLazily, convertNotesTimeDateToCalendar, false);
									}
									else {
										itemTableData = NotesLookupResultBufferDecoder.decodeItemTable(summaryBufferPtr,
												convertStringsLazily, convertNotesTimeDateToCalendar, false);
									}
								}
							}
						}
					}

					Action action;
					if (searchMatch.getDocumentClass().contains(DocumentClass.NOTIFYDELETION)) {
						action = callback.deletionStubFound(db, searchMatch, itemTableData);
					}
					else {
						if (!isMatch) {
							action = callback.noteFoundNotMatchingFormula(db, searchMatch, itemTableData);
						}
						else {
							action = callback.noteFound(db, searchMatch, itemTableData);
						}
					}
					if (action==Action.Stop) {
						return INotesErrorConstants.ERR_CANCEL;
					}
					else {
						return 0;
					}
				}
				catch (Throwable t) {
					invocationEx[0] = t;
					return INotesErrorConstants.ERR_CANCEL;
				}
				finally {
					if (itemTableData!=null) {
						itemTableData.free();
					}
					if (doc!=null) {
						doc.dispose();
						doc = null;
					}
				}
			}

		};
	

		if (PlatformUtils.isWin32()) {}
		else {}
		
		HANDLE.ByReference hFormula = HANDLE.newInstanceByReference();
		
		if (!StringUtil.isEmpty(formula)) {
			hFormula = ViewFormulaCompiler.compile(formula, columnFormulasFixedOrder);
		}

		JNAIDTable tableWithHighOrderBit = null;
		boolean tableWithHighOrderBitCanBeRecycled = false;
		
		try {
			final NotesTimeDateStruct retUntil = NotesTimeDateStruct.newInstance();

			final Memory viewTitleBuf = NotesStringUtils.toLMBCS(viewTitle==null ? "" : viewTitle, true); //$NON-NLS-1$

			DHANDLE hFilter=null;
			int filterFlags=NotesConstants.SEARCH_FILTER_NONE;
			
			if (searchFilter instanceof JNAIDTable) {
				//NSFSearchExtended3 required that the high order bit for each ID in the table
				//must be set; we check if a new table must be created
				JNAIDTable idTable = ((JNAIDTable)searchFilter);
				if (idTable.isEmpty()) {
					tableWithHighOrderBit = idTable;
					tableWithHighOrderBitCanBeRecycled = false;
				}
				else {
					long firstId = idTable.getFirstId();
					long lastId = idTable.getLastId();

					if (((firstId & NotesConstants.NOTEID_RESERVED)==NotesConstants.NOTEID_RESERVED) &&
					((lastId & NotesConstants.NOTEID_RESERVED)==NotesConstants.NOTEID_RESERVED)) {
						//high order bit already set for every ID
						tableWithHighOrderBit = idTable;
						tableWithHighOrderBitCanBeRecycled = false;
					}
					else {
						//create a new table
						tableWithHighOrderBit = idTable.withHighOrderBit();
						tableWithHighOrderBitCanBeRecycled = true;
					}
				}
				JNAIDTableAllocations tableWithHighOrderBitAllocations = (JNAIDTableAllocations) tableWithHighOrderBit.getAdapter(APIObjectAllocations.class);
				hFilter = tableWithHighOrderBitAllocations.getIdTableHandle();
				filterFlags = NotesConstants.SEARCH_FILTER_NOTEID_TABLE;
			}
			else if (searchFilter instanceof JNADominoCollection) {
				//produces a crash:
//				NotesCollection col = (NotesCollection) searchFilter;
//				LongByReference retFilter = new LongByReference();
//				short result = notesAPI.b64_NSFGetFolderSearchFilter(db.getHandle64(), db.getHandle64(), col.getNoteId(), since, 0, retFilter);
//				NotesErrorUtils.checkResult(result);
//				hFilter = retFilter.getValue();
//				filterFlags = NotesConstants.SEARCH_FILTER_FOLDER;
			}
			
			final HANDLE.ByReference hFormulaFinal = hFormula;
			final DHANDLE hFilterFinal = hFilter;
			final int filterFlagsFinal = filterFlags;
			final int searchFlagsBitMaskFinal = searchFlagsBitMask;
			final int searchFlags1Final = search1FlagsBitMask;
			final int searchFlags2Final = 0;
			final int searchFlags3Final = 0;
			final int searchFlags4Final = 0;
			final int noteClassMaskFinal = noteClassMask;

			List<String> builderNames = db.getParentDominoClient().getBuilderNamesList();
			
			final DHANDLE hNamesList;
			if (mode == DbMode.DIRECTORY) {
				hNamesList = null;
			}
			else {
				UserNamesList namesList = db.getAdapter(UserNamesList.class);
				if (namesList!=null) {
					boolean openAsIdUser;
					
					if (builderNames.isEmpty()) {
						openAsIdUser = NotesNamingUtils.equalNames(namesList.getPrimaryName(), db.getParentDominoClient().getIDUserName());
					}
					else {
						openAsIdUser = false;
					}

					if (openAsIdUser) {
						hNamesList = null;
					}
					else {
						JNAUserNamesListAllocations namesListAllocations = (JNAUserNamesListAllocations) namesList.getAdapter(APIObjectAllocations.class);
						hNamesList = namesListAllocations.getHandle();
					}
				}
				else {
					hNamesList = null;
				}
			}
			
			JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) db.getAdapter(APIObjectAllocations.class);
			
			short result;
			try {
				result = LockUtil.lockHandles(
						dbAllocations.getDBHandle(),
						hFormulaFinal,
						hFilterFinal,
						hNamesList,

						(dbHandleByVal, hFormulaByVal, hFilterByVal, hNamesListByVal) -> {
							if (PlatformUtils.isWin32()) {
								Win32NotesCallbacks.NsfSearchProcWin32 apiCallbackWin32 = (enumRoutineParameter, searchMatchPtr,
										summaryBufferPtr) -> {
											return apiCallback.invoke(enumRoutineParameter, searchMatchPtr, summaryBufferPtr);
										};

								return NotesCAPI.get().NSFSearchExtended3(dbHandleByVal, hFormulaByVal,
										hFilterByVal, filterFlagsFinal,
										viewTitleBuf, searchFlagsBitMaskFinal, searchFlags1Final, searchFlags2Final, searchFlags3Final,
										searchFlags4Final,
										(short) (noteClassMaskFinal & 0xffff), sinceStruct, apiCallbackWin32, null, retUntil,
										hNamesListByVal);
							}
							else {
								return NotesCAPI.get().NSFSearchExtended3(dbHandleByVal, hFormulaByVal,
										hFilterByVal, filterFlagsFinal,
										viewTitleBuf, searchFlagsBitMaskFinal, searchFlags1Final, searchFlags2Final, searchFlags3Final,
										searchFlags4Final,
										(short) (noteClassMaskFinal & 0xffff), sinceStruct, apiCallback, null, retUntil,
										hNamesListByVal);
							}

						});

			} catch (Exception e) {
				throw new DominoException(0, "Error searching database", e);
			}

			if (invocationEx[0]!=null) {
				//special case for JUnit testcases
				if (invocationEx[0] instanceof AssertionError) {
					throw (AssertionError) invocationEx[0];
				}
				throw new DominoException(0, "Error searching database", invocationEx[0]);
			}
			
			if (result!=INotesErrorConstants.ERR_CANCEL) {
				NotesErrorUtils.checkResult(result);
			}
			else {
				return null;
			}
			JNADominoDateTime retUntilWrap = retUntil==null ? null : new  JNADominoDateTime(retUntil);
			return retUntilWrap;
		}
		finally {
			//free handle of formula
			if (hFormula!=null && !hFormula.isNull()) {
				short result = LockUtil.lockHandle(hFormula, (hFormulaByVal) -> {
					return Mem.OSMemFree(hFormulaByVal);
				});
				NotesErrorUtils.checkResult(result);
			}
			if (tableWithHighOrderBit!=null && tableWithHighOrderBitCanBeRecycled) {
				tableWithHighOrderBit.dispose();
			}
		}
	}

	/**
	 * Callback interface to process database search results
	 * 
	 * @author Karsten Lehmann
	 */
	public static abstract class SearchCallback {
		
		/**
		 * Implement this method to receive search results
		 * 
		 * @param parentDb parent database
		 * @param searchMatch data about search match
		 * @param summaryBufferData gives access to the note's summary buffer if {@link Search#SUMMARY} was specified; otherwise this value is null
		 * @return either {@link Action#Continue} to go on searching or {@link Action#Stop} to stop
		 */
		public abstract Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData);
		
		/**
		 * Implement this method to read deletion stubs. Method
		 * is only called when a <code>since</code> date is specified.
		 * 
		 * @param parentDb parent database
		 * @param searchMatch data about search match
		 * @param summaryBufferData gives access to the note's summary buffer if {@link Search#SUMMARY} was specified; otherwise this value is null
		 * @return either {@link Action#Continue} to go on searching or {@link Action#Stop} to stop
		 */
		public Action deletionStubFound(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData) {
			return Action.Continue;
		}
		
		/**
		 * Implement this method to receive notes that do not match the selection formula. Method
		 * is only called when a <code>since</code> date is specified.
		 * 
		 * @param parentDb parent database
		 * @param searchMatch data about search match
		 * @param summaryBufferData gives access to the note's summary buffer if {@link Search#SUMMARY} was specified; otherwise this value is null
		 * @return either {@link Action#Continue} to go on searching or {@link Action#Stop} to stop
		 */
		public Action noteFoundNotMatchingFormula(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData) {
			return Action.Continue;
		}
		
	}

	public interface JNASearchMatch extends SearchMatch {

		//global instance id properties
		
		/**
		 * Gives raw access to the global instance id's file timedate data
		 * 
		 * @return file innards
		 */
		int[] getGIDFileInnards();
		
		/**
		 * Gives raw access to the global instance id's note timedate data
		 * 
		 * @return note innards
		 */
		int[] getGIDNoteInnards();
		
		//originator id properties
		
		/**
		 * Gives raw access to the originator id's file timedate data
		 * 
		 * @return file innards
		 */
		int[] getOIDFileInnards();
		
		/**
		 * Gives raw access to the originator id's note timedate data
		 * 
		 * @return note innards
		 */
		int[] getOIDNoteInnards();
		
		/**
		 * Gives raw access to the note's sequence time data
		 * 
		 * @return sequence time innards
		 */
		int[] getSeqTimeInnards();
		
		//other data
	
		/**
		 * Convenience function that checks whether the result of {@link #getFlags()}
		 * contains {@link DocFlags#Match}. When a formula and a date is specified for an NSF
		 * search, the search not only returns notes matching the formula, but also
		 * deleted notes and notes not matching the formula.
		 * 
		 * @return true if matches formula
		 */
		boolean matchesFormula();
		
		/**
		 * Returns true if this document has a large summary. In this case,
		 * the document needs to be opened to read the summary buffer data.
		 * 
		 * @return true if large summary
		 */
		boolean isLargeSummary();
		
		/**
		 * Returns the length of the returned summary buffer
		 * 
		 * @return summary buffer
		 */
		int getSummaryLength();
	
		//methods with the same content but different return types
		
		/**
		 * Returns all the data of the originator id
		 * 
		 * @return originator id data
		 */
		NotesOriginatorIdData getOIDData();
		
		/**
		 * Returns the "file" part of the global instance id as a {@link JNADominoDateTime}.
		 * This is the creation date of the database.
		 * 
		 * @return db creation date
		 */
		DominoDateTime getDbCreated();
		
	}
}
