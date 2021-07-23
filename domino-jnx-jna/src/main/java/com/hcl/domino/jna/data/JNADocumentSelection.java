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
package com.hcl.domino.jna.data;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DocumentSelection;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.search.NotesSearch;
import com.hcl.domino.jna.internal.search.NotesSearch.JNASearchMatch;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;

public class JNADocumentSelection extends JNAIDTable implements DocumentSelection {
	private JNADatabase m_db;
	private final Set<SelectionType> m_selection;
	private String m_formula;
	private JNADominoDateTime m_sinceTime;
	private JNADominoDateTime m_untilTime;
	private JNADominoDateTime m_lastBuildTime;
	private JNAIDTable m_preselectedNoteIds;

	// LMBCS conversion of design flags
	
	private Memory m_DFLAGPAT_VIEWFORM_ALL_VERSIONS;
	private Memory m_DFLAGPAT_SUBFORM_ALL_VERSIONS;
	private Memory m_DFLAGPAT_SACTIONS_DESIGN;
	private Memory m_DFLAGPAT_FRAMESET;
	private Memory m_DFLAGPAT_WEBPAGE;
	private Memory m_DFLAGPAT_IMAGE_RESOURCE;
	private Memory m_DFLAGPAT_STYLE_SHEET_RESOURCE;
	private Memory m_DFLAGPAT_JAVA_RESOURCE;
	private Memory m_DFLAGPAT_VIEW_DESIGN;
	private Memory m_DFLAGPAT_FOLDER_ALL_VERSIONS;
	private Memory m_DFLAGPAT_VIEWMAP_DESIGN;
	private Memory m_DFLAGPAT_AGENTSLIST;
	private Memory m_DFLAGPAT_SITEMAP;
	private Memory m_DFLAGPAT_DATABASESCRIPT;
	private Memory m_DFLAGPAT_SCRIPTLIB;
	private Memory m_DFLAGPAT_DATA_CONNECTION_RESOURCE;

	public JNADocumentSelection(JNADominoClient parent, JNADatabase db) {
		super(parent);
		m_db = db;
		m_selection = new HashSet<>();
	}

	@Override
	public Database getParentDatabase() {
		return m_db;
	}

	@Override
	public DocumentSelection select(SelectionType... selectionTypes) {
		if (selectionTypes!=null) {
			for (SelectionType currType : selectionTypes) {
				m_selection.add(currType);
			}
		}
		return this;
	}

	@Override
	public DocumentSelection select(Collection<SelectionType> selectionTypes) {
		m_selection.addAll(selectionTypes);
		return this;
	}

	@Override
	public DocumentSelection selectAllDocuments() {
		selectAllDataDocuments();
		selectAllAdminDocuments();
		selectAllDesignElements();

		return this;
	}
	
	@Override
	public DocumentSelection selectAllDataDocuments() {
		select(SelectionType.DOCUMENTS, SelectionType.PROFILES);

		return this;
	}
	
	@Override
	public DocumentSelection selectAllAdminDocuments() {
		select(SelectionType.REPLICATION_FORMULAS, SelectionType.ACL);
		
		return this;
	}

	@Override
	public DocumentSelection selectAllDesignElements() {
		selectAllFormatElements();
		selectAllIndexElements();
		selectAllCodeElements();
		
		select(SelectionType.ICON, SelectionType.SHARED_FIELDS, SelectionType.HELP_ABOUT,
				SelectionType.HELP_USING, SelectionType.HELP_INDEX);
		
		return this;
	}

	@Override
	public DocumentSelection selectAllFormatElements() {
		select(SelectionType.FORMS, SelectionType.SUBFORMS, SelectionType.ACTIONS,
				SelectionType.FRAMESETS, SelectionType.PAGES, SelectionType.IMAGE_RESOURCES,
				SelectionType.STYLESHEETS, SelectionType.JAVA_RESOURCES, SelectionType.MISC_FORMAT_ELEMENTS);
		
		return this;
	}
	
	@Override
	public DocumentSelection selectAllIndexElements() {
		select(SelectionType.VIEWS, SelectionType.FOLDERS, SelectionType.NAVIGATORS, SelectionType.MISC_INDEX_ELEMENTS);
		
		return this;
	}

	@Override
	public DocumentSelection selectAllCodeElements() {
		select(SelectionType.AGENTS, SelectionType.OUTLINES, SelectionType.DATASCRIPT_SCRIPT,
				SelectionType.SCRIPT_LIBRARIES, SelectionType.DATA_CONNECTIONS, SelectionType.MISC_CODE_ELEMENTS);
		
		return this;
	}
	
	@Override
	public DocumentSelection deselect(SelectionType... selectionTypes) {
		if (selectionTypes!=null) {
			for (SelectionType currType : selectionTypes) {
				m_selection.remove(currType);
			}
		}
		return this;
	}

	@Override
	public DocumentSelection deselect(Collection<SelectionType> selectionTypes) {
		m_selection.removeAll(selectionTypes);
		return this;
	}

	@Override
	public boolean isSelected(SelectionType selectionType) {
		return m_selection.contains(selectionType);
	}

	@Override
	public Set<SelectionType> getSelection() {
		return m_selection;
	}
	
	@Override
	public DocumentSelection withSelectionFormula(String formula) {
		m_formula = formula;
		return this;
	}

	@Override
	public String getSelectionFormula() {
		return StringUtil.toString(m_formula);
	}

	@Override
	public DocumentSelection withSinceTime(Temporal dt) {
		m_sinceTime = JNADominoDateTime.from(dt);
		return this;
	}

	@Override
	public Optional<DominoDateTime> getSinceTime() {
		return Optional.ofNullable(m_sinceTime);
	}

	@Override
	public Optional<DominoDateTime> getUntilTime() {
		return Optional.ofNullable(m_untilTime);
	}
	
	@Override
	public Optional<DominoDateTime> getLastBuildTime() {
		return Optional.ofNullable(m_lastBuildTime);
	}

	@Override
	public DocumentSelection withPreselection(Collection<Integer> noteIds) {
		m_preselectedNoteIds = new JNAIDTable(getParentDominoClient(), noteIds);
		return this;
	}
	
	@Override
	public Optional<IDTable> getPreselection() {
		return Optional.ofNullable(m_preselectedNoteIds);
	}
	
	@Override
	public DocumentSelection build() {
		Map<String,String> columnFormulas = new HashMap<>();
		columnFormulas.put("$Name", ""); //$NON-NLS-1$ //$NON-NLS-2$
		columnFormulas.put("$Flags", ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		EnumSet<DocumentClass> docClass = EnumSet.of(DocumentClass.PRIVATE);

		if (isSelected(SelectionType.DOCUMENTS) || isSelected(SelectionType.PROFILES)) {
			docClass.add(DocumentClass.DOCUMENT);
		}

		if (isSelected(SelectionType.HELP_ABOUT)) {
			docClass.add(DocumentClass.INFO);
		}
		
		if (isSelected(SelectionType.FORMS) || isSelected(SelectionType.SUBFORMS) ||
				isSelected(SelectionType.ACTIONS) || isSelected(SelectionType.FRAMESETS) ||
				isSelected(SelectionType.PAGES) || isSelected(SelectionType.IMAGE_RESOURCES) ||
				isSelected(SelectionType.STYLESHEETS) || isSelected(SelectionType.JAVA_RESOURCES) ||
				isSelected(SelectionType.MISC_FORMAT_ELEMENTS)) {
			docClass.add(DocumentClass.FORM);
		}

		if (isSelected(SelectionType.VIEWS) || isSelected(SelectionType.FOLDERS) ||
				isSelected(SelectionType.NAVIGATORS) || isSelected(SelectionType.MISC_INDEX_ELEMENTS)) {
			docClass.add(DocumentClass.VIEW);
		}
		
		if (isSelected(SelectionType.ICON)) {
			docClass.add(DocumentClass.ICON);
		}

		if (isSelected(SelectionType.ACL)) {
			docClass.add(DocumentClass.ACL);
		}

		if (isSelected(SelectionType.HELP_INDEX)) {
			docClass.add(DocumentClass.HELP_INDEX);
		}

		if (isSelected(SelectionType.HELP_USING)) {
			docClass.add(DocumentClass.HELP);
		}
		
		if (isSelected(SelectionType.AGENTS) || isSelected(SelectionType.OUTLINES) ||
				isSelected(SelectionType.DATASCRIPT_SCRIPT) || isSelected(SelectionType.SCRIPT_LIBRARIES) ||
				isSelected(SelectionType.DATA_CONNECTIONS) || isSelected(SelectionType.MISC_CODE_ELEMENTS)) {
			docClass.add(DocumentClass.FILTER);
		}
		
		if (isSelected(SelectionType.SHARED_FIELDS)) {
			docClass.add(DocumentClass.FIELD);
		}

		if (isSelected(SelectionType.REPLICATION_FORMULAS)) {
			docClass.add(DocumentClass.REPLFORMULA);
		}

		EnumSet<SearchFlag> searchFlags = EnumSet.of(SearchFlag.NOTIFYDELETIONS, SearchFlag.SUMMARY);
		
		if (isSelected(SelectionType.PROFILES)) {
			// tell search to include profile docs, a specific type of named ghost, but not other types of named ghosts.
			searchFlags.add(SearchFlag.PROFILE_DOCS);

			// tell search we want the selection formula to apply to named ghosts. Without this, all named ghosts
			// (ie, all profile docs in this case) would be returned, even those that might be filtered out by the
			// selection formula.
			searchFlags.add(SearchFlag.NAMED_GHOSTS);
		}
		
		clear();
		
		m_untilTime = NotesSearch.search(m_db, m_preselectedNoteIds,
				StringUtil.isEmpty(m_formula) ? "@All" : m_formula, //$NON-NLS-1$
				columnFormulas,
				"-", searchFlags, docClass, m_sinceTime, new NotesSearch.SearchCallback() { //$NON-NLS-1$
					
					@Override
					public Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch,
							IItemTableData summaryBufferData) {

						// Ignore notes marked for deletion and notes not picked by selection formula.
						Set<DocumentClass> docClass = searchMatch.getDocumentClass();
						if (docClass.contains(DocumentClass.NOTIFYDELETION)) {
							return Action.Continue;
						}
						if (!searchMatch.matchesFormula()) {
							return Action.Continue;
						}

						// Test for documents first - the normal case
						if (docClass.contains(DocumentClass.DOCUMENT)) {
							// test for the profile doc subclass first (docs that have a $Name
							// field that starts with "$Profile").
							String nameVal = summaryBufferData.get("$Name", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$

							if ("$Profile".equalsIgnoreCase(nameVal)) { //$NON-NLS-1$
								if (isSelected(SelectionType.PROFILES)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							// everything else is just a regular document.
							if (isSelected(SelectionType.DOCUMENTS)) {
								add(searchMatch.getNoteID());
							}
							return Action.Continue;
						}
						else if (DocumentClass.isDesignElement(docClass)) {
							// Get DESIGN_FLAGS item ($Flags) for note classes that can have subclasses
							String flags = null;

							if (docClass.contains(DocumentClass.FORM) || 
									docClass.contains(DocumentClass.VIEW) ||
									docClass.contains(DocumentClass.FILTER)) {
								flags = summaryBufferData.get("$Flags", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
							}

							// Check for individual note types in the
							// order of NOTE_CLASS... definitions
							if (docClass.contains(DocumentClass.INFO)) {
								if (isSelected(SelectionType.HELP_ABOUT)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							Memory flagsMem = null;
							short flagsLength = 0;
							if (flags!=null) {
								flagsMem = NotesStringUtils.toLMBCS(flags, true);
								flagsLength = (short) ((flagsMem.size()-1) & 0xffff);
							}

							if (docClass.contains(DocumentClass.FORM)) {
								if (flags!=null) {
									if (m_DFLAGPAT_VIEWFORM_ALL_VERSIONS==null) {
										m_DFLAGPAT_VIEWFORM_ALL_VERSIONS = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_VIEWFORM_ALL_VERSIONS, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_VIEWFORM_ALL_VERSIONS)) {
										if (isSelected(SelectionType.FORMS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_SUBFORM_ALL_VERSIONS==null) {
										m_DFLAGPAT_SUBFORM_ALL_VERSIONS = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_SUBFORM_ALL_VERSIONS, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_SUBFORM_ALL_VERSIONS)) {
										if (isSelected(SelectionType.SUBFORMS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_SACTIONS_DESIGN==null) {
										m_DFLAGPAT_SACTIONS_DESIGN = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_SACTIONS_DESIGN, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_SACTIONS_DESIGN)) {
										if (isSelected(SelectionType.ACTIONS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_FRAMESET==null) {
										m_DFLAGPAT_FRAMESET = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_FRAMESET, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_FRAMESET)) {
										if (isSelected(SelectionType.FRAMESETS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_WEBPAGE==null) {
										m_DFLAGPAT_WEBPAGE = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_WEBPAGE, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_WEBPAGE)) {
										if (isSelected(SelectionType.PAGES)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_IMAGE_RESOURCE==null) {
										m_DFLAGPAT_IMAGE_RESOURCE = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_IMAGE_RESOURCE, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_IMAGE_RESOURCE)) {
										if (isSelected(SelectionType.IMAGE_RESOURCES)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_STYLE_SHEET_RESOURCE==null) {
										m_DFLAGPAT_STYLE_SHEET_RESOURCE = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_STYLE_SHEET_RESOURCE, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_STYLE_SHEET_RESOURCE)) {
										if (isSelected(SelectionType.STYLESHEETS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_JAVA_RESOURCE==null) {
										m_DFLAGPAT_JAVA_RESOURCE = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_JAVA_RESOURCE, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_JAVA_RESOURCE)) {
										if (isSelected(SelectionType.JAVA_RESOURCES)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}
									return Action.Continue;
								}

								if (isSelected(SelectionType.FORMS)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							if (docClass.contains(DocumentClass.VIEW)) {
								if (flagsMem!=null) {
									if (m_DFLAGPAT_VIEW_DESIGN==null) {
										m_DFLAGPAT_VIEW_DESIGN = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_VIEW_DESIGN, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_VIEW_DESIGN)) {
										if (isSelected(SelectionType.VIEWS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_FOLDER_ALL_VERSIONS==null) {
										m_DFLAGPAT_FOLDER_ALL_VERSIONS = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_FOLDER_ALL_VERSIONS)) {
										if (isSelected(SelectionType.FOLDERS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_VIEWMAP_DESIGN==null) {
										m_DFLAGPAT_VIEWMAP_DESIGN = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_VIEWMAP_DESIGN, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_VIEWMAP_DESIGN)) {
										if (isSelected(SelectionType.NAVIGATORS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									return Action.Continue;
								}

								if (isSelected(SelectionType.VIEWS)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							if (docClass.contains(DocumentClass.ICON)) {
								if (isSelected(SelectionType.ICON)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							if (docClass.contains(DocumentClass.HELP_INDEX)) {
								if (isSelected(SelectionType.HELP_INDEX)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							if (docClass.contains(DocumentClass.HELP)) {
								if (isSelected(SelectionType.HELP_USING)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							if (docClass.contains(DocumentClass.FILTER)) {
								if (flagsMem!=null) {
									if (m_DFLAGPAT_AGENTSLIST==null) {
										m_DFLAGPAT_AGENTSLIST = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_AGENTSLIST, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_AGENTSLIST)) {
										if (isSelected(SelectionType.AGENTS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_SITEMAP==null) {
										m_DFLAGPAT_SITEMAP = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_SITEMAP, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_SITEMAP)) {
										if (isSelected(SelectionType.OUTLINES)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_DATABASESCRIPT==null) {
										m_DFLAGPAT_DATABASESCRIPT = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_DATABASESCRIPT, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_DATABASESCRIPT)) {
										if (isSelected(SelectionType.DATASCRIPT_SCRIPT)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_SCRIPTLIB==null) {
										m_DFLAGPAT_SCRIPTLIB = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_SCRIPTLIB, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_SCRIPTLIB)) {
										if (isSelected(SelectionType.SCRIPT_LIBRARIES)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}

									if (m_DFLAGPAT_DATA_CONNECTION_RESOURCE==null) {
										m_DFLAGPAT_DATA_CONNECTION_RESOURCE = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_DATA_CONNECTION_RESOURCE, true);
									}
									if (NotesCAPI.get().CmemflagTestMultiple(flagsMem, flagsLength,
											m_DFLAGPAT_DATA_CONNECTION_RESOURCE)) {
										if (isSelected(SelectionType.DATA_CONNECTIONS)) {
											add(searchMatch.getNoteID());
										}
										return Action.Continue;
									}
								}
							}

							if (docClass.contains(DocumentClass.FIELD)) {
								if (isSelected(SelectionType.SHARED_FIELDS)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}
						}
						else {
							// Handle administration notes next
							if (docClass.contains(DocumentClass.ACL)) {
								if (isSelected(SelectionType.ACL)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							if (docClass.contains(DocumentClass.REPLFORMULA)) {
								if (isSelected(SelectionType.REPLICATION_FORMULAS)) {
									add(searchMatch.getNoteID());
								}
								return Action.Continue;
							}

							return Action.Continue;
						}

						return Action.Continue;
					}
				});
		m_lastBuildTime = new JNADominoDateTime();
		
		return this;
	}
	
}
