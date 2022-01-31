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
package com.hcl.domino.jna.dbdirectory;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.dbdirectory.DatabaseData;
import com.hcl.domino.dbdirectory.DirEntry;
import com.hcl.domino.dbdirectory.DirectorySearchQuery;
import com.hcl.domino.dbdirectory.FileType;
import com.hcl.domino.dbdirectory.FolderData;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.gc.allocations.JNADirectorySearchQueryAllocations;
import com.hcl.domino.jna.internal.search.NotesSearch;
import com.hcl.domino.jna.internal.search.NotesSearch.JNASearchMatch;
import com.hcl.domino.misc.Loop;

/**
 * Implementation of a directory search query for the Domino data directories.
 *
 * @author Tammo Riedinger
 */
public class JNADirectorySearchQuery extends BaseJNAAPIObject<JNADirectorySearchQueryAllocations>
    implements DirectorySearchQuery {
  private String m_server;
  private String m_directory;
  private String m_formula;
  private EnumSet<SearchFlag> m_searchFlags = EnumSet.of(SearchFlag.FILETYPE, SearchFlag.SUMMARY);
  private EnumSet<FileType> m_fileTypes = EnumSet.of(FileType.DIRS);
  private JNADominoDateTime m_since;

  public JNADirectorySearchQuery(IAPIObject<?> parent) {
    super(parent);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected JNADirectorySearchQueryAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
      APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

    return new JNADirectorySearchQueryAllocations(parentDominoClient, parentAllocations, this,
        queue);
  }

  @Override
  public DirectorySearchQuery withServer(String server) {
    m_server = server;

    return this;
  }

  @Override
  public DirectorySearchQuery withDirectory(String directory) {
    m_directory = directory;

    return this;
  }

  @Override
  public DirectorySearchQuery withFormula(String formula) {
    m_formula = formula;

    return this;
  }

  @Override
  public DirectorySearchQuery withFlags(Collection<SearchFlag> searchFlags) {
    m_searchFlags =
        searchFlags == null ? EnumSet.noneOf(SearchFlag.class) : EnumSet.copyOf(searchFlags);

    return this;
  }

  @Override
  public DirectorySearchQuery withFileTypes(Collection<FileType> fileTypes) {
    m_fileTypes = fileTypes == null ? EnumSet.noneOf(FileType.class) : EnumSet.copyOf(fileTypes);

    return this;
  }

  @Override
  public DirectorySearchQuery since(TemporalAccessor since) {
    m_since = JNADominoDateTime.from(since);

    return this;
  }

  @Override
  public void forEach(int skip, int limit, BiConsumer<DirEntry, Loop> consumer)
      throws DominoException {
    JNADatabase dir = (JNADatabase) getParentDominoClient().openDatabase(m_server,
        (m_directory == null) ? "" : m_directory); //$NON-NLS-1$

    try {
    	//make sure to enable directory mode and
    	//read the summary buffer with the details for each search result (e.g. path/title/type)
    	EnumSet<SearchFlag> searchFlagsWithSummary = EnumSet.copyOf(m_searchFlags);
    	searchFlagsWithSummary.add(SearchFlag.SUMMARY);
    	searchFlagsWithSummary.add(SearchFlag.FILETYPE);
    	
    	NotesSearch.searchFiles(dir, null, m_formula, "-", searchFlagsWithSummary, m_fileTypes, //$NON-NLS-1$
          m_since, new NotesSearch.SearchCallback() {
            LoopImpl loop = new LoopImpl();
            AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch,
                IItemTableData summaryBufferData) {
              if (limit >= 0 && loop.getIndex() >= limit) {
                return Action.Stop;
              }

              JNADirEntry entry = toEntry(summaryBufferData);
              if (entry != null) {
                if ("..".equals(entry.getFileName())) { //$NON-NLS-1$
                  // skip ".." entry
                  return Action.Continue;
                }

                entry.setServer(m_server);

                if (counter.incrementAndGet() > skip) {

                  consumer.accept(entry, loop);

                  loop.next();
                }
                return loop.isStopped() ? Action.Stop : Action.Continue;
              } else {
                return Action.Continue;
              }
            }
          });
    } finally {
      dir.close();
    }
  }

  @Override
  public Stream<DirEntry> stream() throws DominoException {
    // TODO consider whether it might be worthwhile to implement a custom stream
    // without reading (and hence decoding) all entries to a list first
    final ArrayList<DirEntry> foundEntries = new ArrayList<>();

    forEach(0, -1, (t, u) -> foundEntries.add(t));

    return foundEntries.stream();
  }

  /**
   * Parses the summary-buffer to different subclasses of FileEntry.
   * 
   * @param summaryBufferData the summary data
   * @return the parsed entry or null
   */
  private JNADirEntry toEntry(IItemTableData summaryBufferData) {
	  Map<String, Object> dataAsMap = summaryBufferData.asMap(true);


	  JNADirEntry retEntry;

	  String typeStr;
	  Object typeObj = dataAsMap.get("$type"); //$NON-NLS-1$
	  if (!(typeObj instanceof String)) {
		  //$type is missing for any non-Domino files like .tmp
		  typeStr = ""; //$NON-NLS-1$
	  }
	  else {
		  typeStr = (String) typeObj;
	  }
	  
	  if ("$DIR".equals(typeStr)) { //$NON-NLS-1$
		  retEntry = new JNAFolderData();
		  
	  } else if ("$NOTEFILE".equals(typeStr)) { //$NON-NLS-1$
		  String dbTitle = ""; //$NON-NLS-1$
		  String dbCategory = ""; //$NON-NLS-1$
		  String dbTemplateName = ""; //$NON-NLS-1$
		  String dbInheritTemplateName = ""; //$NON-NLS-1$

		  Object infoObj = dataAsMap.get("$Info"); //$NON-NLS-1$
		  if (infoObj instanceof String) {
			  // parse weird $Info format:
			  // $info=Database title\n
			  // Database category\n
			  // #1Database template\n
			  // #2Database inherit template
			  String infoStr = ((String) infoObj).replace("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
			  StringTokenizerExt st = new StringTokenizerExt(infoStr, "\n"); //$NON-NLS-1$
			  if (st.hasMoreTokens()) {
				  dbTitle = st.nextToken();

				  boolean secondLine = true;
				  while (st.hasMoreTokens()) {
					  String currLine = st.nextToken();

					  if (secondLine) {
						  secondLine = false;

						  if (!currLine.startsWith("1#") && !currLine.startsWith("2#")) { //$NON-NLS-1$ //$NON-NLS-2$
							  dbCategory = currLine;
							  continue;
						  }
					  }

					  if (currLine.startsWith("#1")) { //$NON-NLS-1$
						  dbTemplateName = currLine.substring(2);
					  } else if (currLine.startsWith("#2")) { //$NON-NLS-1$
						  dbInheritTemplateName = currLine.substring(2);
					  }
				  }
			  }
		  }

		  DominoDateTime dbCreated = getDateValue(dataAsMap, "$DBCREATED"); //$NON-NLS-1$
		  DominoDateTime lastFixup = getDateValue(dataAsMap, "$lastfixup"); //$NON-NLS-1$
		  DominoDateTime lastCompact = getDateValue(dataAsMap, "$lastcompact"); //$NON-NLS-1$
		  DominoDateTime nonDataMod = getDateValue(dataAsMap, "$nondatamod"); //$NON-NLS-1$
		  DominoDateTime dataMod = getDateValue(dataAsMap, "$datamod"); //$NON-NLS-1$

		  JNADatabaseData dbData = new JNADatabaseData();
		  dbData.setTitle(dbTitle);
		  dbData.setCreated(dbCreated);
		  dbData.setLastFixup(lastFixup);
		  dbData.setLastCompact(lastCompact);
		  dbData.setDesignModifiedDate(nonDataMod);
		  dbData.setDataModifiedDate(dataMod);
		  dbData.setCategory(dbCategory);
		  dbData.setTemplateName(dbTemplateName);
		  dbData.setInheritTemplateName(dbInheritTemplateName);

		  retEntry = dbData;
	  } else {
		  // some unknown type
		  retEntry = new JNADirEntry();
	  }
	  
	  //read common attributes
	  retEntry.setProperties(dataAsMap);

	  DominoDateTime fileModified = getDateValue(dataAsMap, "$Modified"); //$NON-NLS-1$
	  retEntry.setModified(fileModified);

	  String fileName = null;
	  Object fileNameObj = dataAsMap.get("$TITLE"); //$NON-NLS-1$
	  if (fileNameObj instanceof String) {
		  fileName = (String) fileNameObj;
	  }
	  
	  String filePath = null;
	  Object filePathObj = dataAsMap.get("$path"); //$NON-NLS-1$
	  if (filePathObj instanceof String) {
		  filePath = (String) filePathObj;
	  }

	  String physicalFilePath = null;
	  Object physicalFilePathObj = dataAsMap.get("$PHYSICALPATH"); //$NON-NLS-1$
	  if (physicalFilePathObj instanceof String) {
		  physicalFilePath = (String) physicalFilePathObj;
	  }
	  
	  long fileLength = 0;
	  Object fileLengthObj = dataAsMap.get("$Length"); //$NON-NLS-1$
	  if (fileLengthObj instanceof Number) {
		  fileLength = ((Number)fileLengthObj).longValue();
	  }
	  
	  if (fileName!=null && filePath!=null) {
		  retEntry.setFileName(fileName);
		  retEntry.setFilePath(filePath);
		  retEntry.setPhysicalFilePath(physicalFilePath);
		  retEntry.setFileLength(fileLength);
		  
		  return retEntry;
	  }
	  else {
		  //ignore entries without filename/filepath if they ever exist
		  return null;
	  }
  }

  private DominoDateTime getDateValue(Map<String, Object> data, String key) {
    Object valueObj = data.get(key);
    if (valueObj instanceof DominoDateTime) {
      return (DominoDateTime) valueObj;
    } else if (valueObj instanceof Calendar) {
      return new JNADominoDateTime(((Calendar) valueObj).toInstant());
    }
    return null;
  }

  private static class LoopImpl extends Loop {

    public void next() {
      super.setIndex(getIndex() + 1);
    }

    @Override
    public void setIsLast() {
      super.setIsLast();
    }
  }

  /**
   * Base class for directory scan search results
   * 
   * @author Karsten Lehmann
   */
  public static class JNADirEntry implements DirEntry {
    private Map<String, Object> m_properties;
    private String m_server;
    private String m_fileName;
    private String m_filePath;
    private String m_physicalPath;
    private long m_fileLength;
    private DominoDateTime m_modifiedDate;
    
    @Override
    public Map<String, Object> getProperties() {
      return m_properties;
    }

    /**
     * Sets the raw data of the search result entry
     * 
     * @param rawData data
     */
    void setProperties(Map<String, Object> rawData) {
      this.m_properties = new HashMap<>(rawData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> clazz) {
      if (clazz.isAssignableFrom(DirEntry.class)) {
        return (T) this;
      }
      return null;
    }

    @Override
    public String getServer() {
      return m_server;
    }

    void setServer(String server) {
      this.m_server = server;
    }

    /**
     * Sets the name of the entry in the data diretory
     * 
     * @param fileName name
     */
    void setFileName(String fileName) {
      this.m_fileName = fileName;
    }

    @Override
    public String getFileName() {
      return this.m_fileName;
    }

    @Override
    public String getFilePath() {
      return m_filePath;
    }

    /**
     * Sets the complete relative path of the file in the
     * data directory
     * 
     * @param filePath path
     */
    void setFilePath(String filePath) {
      this.m_filePath = filePath;
    }

    @Override
    public String getPhysicalFilePath() {
    	return m_physicalPath;
    }

    /**
     * Sets the physical/absolute path of the file in the
     * scanned directory
     * @param filePath path
     */
    void setPhysicalFilePath(String filePath) {
    	this.m_physicalPath = filePath;
    }

    @Override
    public long getFileLength() {
    	return m_fileLength;
    }

    /**
     * Sets the length of the file
     * 
     * @param length length
     */
    void setFileLength(long length) {
    	this.m_fileLength = length;
    }
    
    @Override
    public DominoDateTime getModified() {
    	return m_modifiedDate;
    }

    /**
     * Sets the last modified date
     * 
     * @param modified date
     */
    void setModified(DominoDateTime modified) {
    	this.m_modifiedDate = modified;
    }
    
    @Override
    public String toString() {
      return MessageFormat.format("JNADirEntry [properties={0}, server={1}]", m_properties, m_server); //$NON-NLS-1$
    }

  }

  /**
   * Subclass of {@link DirEntry} that is used to return
   * parsed data of folders.
   * 
   * @author Karsten Lehmann
   */
  public static class JNAFolderData extends JNADirEntry implements FolderData {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> clazz) {
      if (clazz.isAssignableFrom(FolderData.class)) {
        return (T) this;
      }

      return super.getAdapter(clazz);
    }

    @Override
    public String toString() {
      return MessageFormat.format("JNAFolderData [folderpath={0}]", getFilePath()); //$NON-NLS-1$
    }

  }

  /**
   * Subclass of {@link DirEntry} that is used to return
   * parsed data of databases.
   * 
   * @author Karsten Lehmann
   */
  public static class JNADatabaseData extends JNADirEntry implements DatabaseData {
    private String m_title;
    private DominoDateTime m_created;
    private DominoDateTime m_modified;
    private DominoDateTime m_lastFixup;
    private DominoDateTime m_lastCompact;
    private DominoDateTime m_nonDataMod;
    private DominoDateTime m_dataMod;
    private String m_category;
    private String m_templateName;
    private String m_inheritTemplateName;

    /**
     * Returns the database title
     * 
     * @return title
     */
    @Override
    public String getTitle() {
      return m_title;
    }

    /**
     * Sets the database title
     * 
     * @param title title
     */
    private void setTitle(String title) {
      this.m_title = title;
    }

    /**
     * Returns the filename of the database
     * 
     * @return filename
     */
    @Override
    public String getFileName() {
      return super.getFileName();
    }

    /**
     * Sets the filename of the database
     * 
     * @param fileName filename
     */
    @Override
    void setFileName(String fileName) {
      super.setFileName(fileName);
    }

    /**
     * Returns the complete relative path of the database in the data directory
     * 
     * @return path
     */
    @Override
    public String getFilePath() {
      return super.getFilePath();
    }

    /**
     * Sets the complete relative path of the database in the data directory
     * 
     * @param filePath path
     */
    @Override
    void setFilePath(String filePath) {
      super.setFilePath(filePath);
    }

    /**
     * Returns the database creation date
     * 
     * @return creation date
     */
    @Override
    public DominoDateTime getCreated() {
      return m_created;
    }

    /**
     * Sets the database creation date
     * 
     * @param created creation date
     */
    private void setCreated(DominoDateTime created) {
      this.m_created = created;
    }

    /**
     * Returns the date of the last fixup
     * 
     * @return last fixup
     */
    @Override
    public Optional<DominoDateTime> getLastFixup() {
      return Optional.ofNullable(this.m_lastFixup);
    }

    /**
     * Sets the date of the last db fixup
     * 
     * @param lastFixup last fixup
     */
    private void setLastFixup(DominoDateTime lastFixup) {
      this.m_lastFixup = lastFixup;
    }

    /**
     * Returns the date of the last compact
     * 
     * @return last compact
     */
    @Override
    public Optional<DominoDateTime> getLastCompact() {
      return Optional.ofNullable(this.m_lastCompact);
    }

    /**
     * Sets the date of the last db compact
     * 
     * @param lastCompact last compact
     */
    private void setLastCompact(DominoDateTime lastCompact) {
      this.m_lastCompact = lastCompact;
    }

    /**
     * Returns the date of the last design change
     * 
     * @return design modified date
     */
    @Override
    public DominoDateTime getDesignModifiedDate() {
      return this.m_nonDataMod;
    }

    /**
     * Sets the date of the last design change
     * 
     * @param nonDataMod design modified date
     */
    private void setDesignModifiedDate(DominoDateTime nonDataMod) {
      this.m_nonDataMod = nonDataMod;
    }

    /**
     * Returns the date of the last data change
     * 
     * @return data modified date
     */
    public DominoDateTime getDataModifiedDate() {
      return this.m_dataMod;
    }

    /**
     * Sets the date of the last data change
     * 
     * @param dataMod data modified date
     */
    private void setDataModifiedDate(DominoDateTime dataMod) {
      this.m_dataMod = dataMod;
    }


    /**
     * Returns the database category
     * 
     * @return category or empty string
     */
    @Override
    public String getCategory() {
      return this.m_category;
    }

    /**
     * Sets the database category
     * 
     * @param category category
     */
    private void setCategory(String category) {
      this.m_category = category;
    }

    /**
     * Returns the template name
     * 
     * @return template name if this database is a template, empty string otherwise
     */
    @Override
    public String getTemplateName() {
      return m_templateName;
    }

    private void setTemplateName(String templateName) {
      this.m_templateName = templateName;
    }

    /**
     * Returns the name of the template that this database inherits its design from
     * 
     * @return inherit template name or empty string
     */
    @Override
    public String getInheritTemplateName() {
      return m_inheritTemplateName;
    }

    /**
     * Sets the inherit template name
     * 
     * @param inheritTemplateName inherit template name
     */
    private void setInheritTemplateName(String inheritTemplateName) {
      this.m_inheritTemplateName = inheritTemplateName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> clazz) {
      if (clazz.isAssignableFrom(DatabaseData.class)) {
        return (T) this;
      }

      return super.getAdapter(clazz);
    }

    @Override
    public String toString() {
      return MessageFormat.format(
			"JNADatabaseData [title={0}, filename={1}, filepath={2}, created={3}, modified={4}, lastfixup={5}, lastcompact={6}, datamod={7}, nondatamod={8}, category={9}, templatename={10}, inherittemplateName={11}]", //$NON-NLS-1$
			m_title, getFileName(), getFilePath(), m_created, m_modified, m_lastFixup, m_lastCompact, m_dataMod, m_nonDataMod, m_category, m_templateName,
			m_inheritTemplateName
		);
    }


  }
}
