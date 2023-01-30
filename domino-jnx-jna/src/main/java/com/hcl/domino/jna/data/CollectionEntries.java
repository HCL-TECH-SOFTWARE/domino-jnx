package com.hcl.domino.jna.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery.AllDeselectedEntries;
import com.hcl.domino.data.CollectionSearchQuery.ExpandMode;
import com.hcl.domino.data.CollectionSearchQuery.ExpandedEntries;
import com.hcl.domino.data.CollectionSearchQuery.MultiColumnLookupKey;
import com.hcl.domino.data.CollectionSearchQuery.SelectMode;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.CollectionSearchQuery.SingleColumnLookupKey;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollection.Direction;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.Find;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.jna.data.JNADominoCollection.FindResult;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.NotesConstants;

/**
 * Builder to create an {@link Iterator} of Domino collection entries that returns
 * entries according to the selection criteria, e.g. call {@link #withDirection(Navigate)}
 * to return documents only, call {@link #withSkip(int)} and {@link #withLimit(int)}
 * to only read a few entries, use {@link #withRestrictionToCategory(String)}
 * to restrict the returned entries to a specific category.<br>
 * <br>
 * To get a calculation of total entries, use {@link #withTotalReceiver(Consumer)} and
 * if the return data should be restricted to a top level category, you can use
 * {@link #withCategoryReceiver(Consumer)} to get access to this category entry.
 * <br>
 * By default, we start reading at the first entry in the collection or top level category,
 * but there are methods like {@link #withStartAtLastEntry()}, {@link #withStartAtPosition(String)}
 * or {@link #withStartAtEntryId(int)} to change this behavior.<br>
 * <br>
 * <b>A few remarks about data consistency:</b><br>
 * <br>
 * Domino collections are highly dynamic and may change while traversing them. Domino does
 * not have a consistency level comparable to SQL, so there are situations where things move
 * in the view index between filling our buffer of collection entries. For best read performance,
 * the only way to keep track of the cursor position in the view is the tumbler, e.g. "1.2.3",
 * a list of indices within the hierarchical b-tree structure, but it's possible that between read operations
 * the actual data at position "1.2.3" has changed, e.g. old entries got moved up, down or
 * even disappear.<br>
 * The <b>classic Domino View API</b> provides a method <code>lotus.domino.View.setAutoUpdate(boolean)</code>
 * to tweak the recover strategy when index changes have been detected. With autoUpdate=true,
 * the code uses <code>NIFLocateNote</code> to find the previously read note id nearby the last position
 * (searches above and below the old position in an alternating way).<br>
 * <br>
 * This may lead to performance issues / timeouts, when <code>NIFLocateNote</code> takes a very long time for search (in some
 * cases without success when the note id has disappeared, preventing view index updates during search with a view lock).
 * Since category note ids are not stable between view index changes, this strategy only works for documents
 * (=&gt; under heavy load a ViewNavigator throws errors when the last read entry was a category and recovery is
 * not possible) and it's still possible that the same entries are returned multiple times when a row gets moved
 * in read direction or rows get skipped when a row gets moved in the reverse direction.<br>
 * <br>
 * That's why in this Iterator builder, we don't use this recovery strategy (effectively acting like autoUpdate=false).<br>
 * For best performance, we only traverse the collection once, there's no retry.<br>
 * However we make sure that we only return entries that match configured key lookups or search
 * results (DQL, FT search) and entries within a specified top level category.<br>
 * <br>
 * As a developer you need to be prepared that under heavy load collection entries may be returned multiple times,
 * may be skipped and that entries may have mixed tumbler positions in case a category
 * moves up or down (old category position "1", docs returned with position "1.1", "1.2", ... , "1.10";
 * index changes, so new category position is "2", following docs returned have position "2.11", "2.12", ... "2.20").<br>
 * <br>
 * How much data you read for each entry has a big influence on how likely these effects may occur. For
 * example, just reading note ids is very fast and one <code>NIFReadEntries</code> call can return up to
 * 16.000 note ids in one call (64k return buffer, 4 byte per note id). In contrast, many collection columns
 * with much text content could as well only return 50 rows.<br>
 * <br>
 * For a better data consistency level, use a database search (DQL, FT, formula), open docs for each returned note id
 * and after opening the doc, double check that the doc still matches the search criteria.<br>
 * You are safe as soon as the doc has been loaded, because when someone else concurrently
 * modifies it, your save operation will fail.<br>
 * 
 * @author Karsten Lehmann
 */
public class CollectionEntries implements Iterable<CollectionEntry> {
  private JNADominoCollection collection;
  private Set<ReadMask> readMask;
  private Navigate direction;
  private ExpandedEntries expandedEntries;
  private SelectedEntries selectedEntries;
  private List<Object> categoryLevels;
  private boolean startAtFirstEntry;
  private boolean startAtLastEntry;
  private String startAtPosition;
  private int startAtEntryId;
  private int skip;
  private int limit = -1;
  private Consumer<Integer> totalConsumer;
  private Consumer<CollectionEntry> categoryConsumer;
  private String nameOfSingleColumnToRead;
  private Integer indexOfSingleColumnToRead;
  private Function<DominoCollection, Action> viewIndexChangedHandler;
  private Integer maxBufferEntries;
  private String ftQuery;
  private int ftMaxDocs;
  private Set<FTQuery> ftFlags;
  
  //computed note ids of selected / expanded entries
  private JNAIDTable m_selectedEntriesResolved;
  private JNAIDTable m_expandedEntriesResolved;

  /**
   * Creates a new builder for the specified {@link JNADominoCollection}
   * 
   * @param collection collection to read entries from
   * @return new builder, call {@link #iterator()} to start reading collection entries or use a for-loop
   */
  public static CollectionEntries of(JNADominoCollection collection) {
    CollectionEntries builder = new CollectionEntries();
    builder.collection = collection;
    return builder;
  }
  
  JNADominoCollection getCollection() {
    return collection;
  }
  
  /**
   * Defines what to read for each collection entry
   * 
   * @param readMask read mask
   * @return this builder instance
   */
  public CollectionEntries withReadMask(Set<ReadMask> readMask) {
    this.readMask = readMask;
    return this;
  }
  
  Set<ReadMask> getReadMask() {
    return readMask;
  }
  
  /**
   * Defines the navigation strategy in the collection, e.g. to read documents only,
   * categories, only, read child entries, parent entries or unread docs.
   * 
   * @param direction navigation direction
   * @return this builder instance
   */
  public CollectionEntries withDirection(Navigate direction) {
    this.direction = direction;
    return this;
  }
  
  Navigate getDirection() {
    return direction;
  }
  
  /**
   * Defines expanded/collapsed collection rows
   * 
   * @param selectedEntries e.g. {@link ExpandedEntries#expandAll()} or {@link ExpandedEntries#collapseAll()} followed by adding note ids of documents or category entries
   * @return this builder instance
   */
  public CollectionEntries withExpandedEntries(ExpandedEntries expandedEntries) {
    this.expandedEntries = expandedEntries;
    return this;
  }

  /**
   * Defines selected collection rows to filter the output, e.g. via key lookup
   * 
   * @param selectedEntries e.g. {@link SelectedEntries#selectAll()} or {@link SelectedEntries#deselectAll()} followed by adding note ids of documents or category entries
   * @return this builder instance
   * @see AllDeselectedEntries#selectByKey(List, boolean)
   */
  public CollectionEntries withSelectedEntries(SelectedEntries selectedEntries) {
    this.selectedEntries = selectedEntries;
    return this;
  }
  
  /**
   * Sets a top level category to restrict the returned collection entries
   * 
   * @param category category, e.g. "A" or "A\B" for deeper level
   * @return this builder instance
   */
  public CollectionEntries withRestrictionToCategory(String category) {
    return withRestrictionToCategory(Arrays.asList(category));
  }
  
  /**
   * Sets a top level category to restrict the returned collection entries
   * 
   * @param categoryLevels list of category value for each entry (e.g. as string or number)
   * @return this builder instance
   */
  public CollectionEntries withRestrictionToCategory(List<Object> categoryLevels) {
    this.categoryLevels = categoryLevels;
    return this;
  }

  /**
   * Sets the start entry for reading to a specific view index position
   * 
   * @param pos position
   * @return this builder instance
   */
  public CollectionEntries withStartAtPosition(String pos) {
    this.startAtPosition = pos;
    this.startAtEntryId = 0;
    this.startAtFirstEntry = false;
    this.startAtLastEntry = false;
    return this;
  }
  
  /**
   * Sets the start entry for reading to a specific document note id
   * 
   * @param noteId note id of a document
   * @return this builder instance
   */
  public CollectionEntries withStartAtEntryId(int noteId) {
    if ((noteId & NotesConstants.RRV_DELETED) == NotesConstants.RRV_DELETED) {
      throw new IllegalArgumentException("Only document note ids are supported as start entries");
    }
    this.startAtEntryId = noteId;
    this.startAtPosition = null;
    this.startAtFirstEntry = false;
    this.startAtLastEntry = false;
    return this;
  }
  
  /**
   * Sets the start entry for reading to the last row of the collection or the descendants
   * of a category in case there's a defined top level category
   * 
   * @return this builder instance
   */
  public CollectionEntries withStartAtLastEntry() {
    this.startAtLastEntry = true;
    this.startAtFirstEntry = false;
    this.startAtEntryId = 0;
    this.startAtPosition = null;
    return this;
  }

  /**
   * Sets the start entry for reading to the first row of the collection or the descendants
   * of a category in case there's a defined top level category. This is the default
   * behaviour when no other start entry has been set.
   * 
   * @return this builder instance
   */
  public CollectionEntries withStartAtFirstEntry() {
    this.startAtFirstEntry = true;
    this.startAtLastEntry = false;
    this.startAtEntryId = 0;
    this.startAtPosition = null;
    return this;
  }

  /**
   * Sets the skip count for paging the data
   * 
   * @param skip skip count
   * @return this builder instance
   */
  public CollectionEntries withSkip(int skip) {
    this.skip = skip;
    return this;
  }
  
  /**
   * Sets the number of entries to return (for paging)
   * 
   * @param limit max number of entries
   * @return this builder instance
   */
  public CollectionEntries withLimit(int limit) {
    this.limit = limit;
    return this;
  }
  
  /**
   * If a total is required, use this function to register a {@link Consumer} to get the total count.
   * For performance reasons totals are only read when a consumer is registered.
   * 
   * @param totalConsumer {@link Consumer} to receive total
   * @return this builder instance
   */
  public CollectionEntries withTotalReceiver(Consumer<Integer> totalConsumer) {
    this.totalConsumer = totalConsumer;
    return this;
  }
  
  /**
   * If the output is restricted to the descendants of a category entry, the <code>categoryConsumer</code>
   * receives the category collection entry. We currently only read the {@link ReadMask#NOTEID}, {@link ReadMask#INDEXPOSITION}
   * and {@link ReadMask#SUMMARY} values for the category.
   * 
   * @param categoryConsumer {@link Consumer} to receive the category
   * @return this builder instance
   */
  public CollectionEntries withCategoryReceiver(Consumer<CollectionEntry> categoryConsumer) {
    this.categoryConsumer = categoryConsumer;
    return this;
  }
  
  /**
   * Sets a handler that gets notified when a view index change has been detected.
   * 
   * @param handler listener
   * @return this builder instance
   */
  public CollectionEntries withViewIndexChangedHandler(Function<DominoCollection, Action> handler) {
    this.viewIndexChangedHandler = handler;
    return this;
  }
  
  /**
   * Sets the name of the single column to read data for
   * 
   * @param columnName column name
   * @return this builder instance
   */
  public CollectionEntries withNameOfSingleColumnToRead(String columnName) {
    this.nameOfSingleColumnToRead = columnName;
    return this;
  }
  
  /**
   * Sets the name of the single column to read data for
   * 
   * @param idx column index
   * @return this builder instance
   */
  public CollectionEntries withIndexOfSingleColumnToRead(int idx) {
    this.indexOfSingleColumnToRead = idx;
    return this;
  }

  /**
   * Method to change the size of the internal buffer. By default, we use
   * the <code>limit</code> value if configured or {@link Integer#MAX_VALUE}.
   * 
   * @param entries max buffer entries
   * @return this builder instance
   */
  public CollectionEntries withMaxBufferEntries(int entries) {
    this.maxBufferEntries = entries;
    return this;
  }
  
  /**
   * Applies a fulltext search on the view, changing the order of entries, e.g. to "by descending relevance" (default) or
   * {@link FTQuery#SORT_DATE_CREATED}
   * 
   * @param ftQuery fulltext query
   * @param ftMaxDocs max results, 0 for all
   * @param ftFlags FT search flags
   * @return this builder instance
   */
  public CollectionEntries withRestrictionToFTResults(String ftQuery, int ftMaxDocs, Set<FTQuery> ftFlags) {
    this.ftQuery = ftQuery;
    this.ftMaxDocs = ftMaxDocs;
    this.ftFlags = ftFlags;
    return this;
  }
  
  /**
   * Sets up the {@link Iterator} that returns {@link CollectionEntry} objects
   * based on the configured direction and filters.
   * 
   * @return Iterator
   */
  @Override
  public Iterator<CollectionEntry> iterator() {
    JNACollectionEntryIterator it = new JNACollectionEntryIterator(collection);
    
    it.setSkip(skip);
    it.setCount(limit);
    
    if (readMask!=null) {
      it.setReadMask(readMask);
    }
    
    if (direction!=null) {
      it.setDirection(direction);
    }
    
    if (expandedEntries!=null) {
      JNAIDTable idTable = getExpandedEntries();
      it.expand(expandedEntries, idTable);
    }
    
    if (selectedEntries!=null) {
      JNAIDTable idTable = getSelectedEntries();
      it.select(selectedEntries, idTable);
    }
    
    if (categoryLevels!=null) {
      it.setRestrictToCategory(categoryLevels);
    }

    if (startAtFirstEntry) {
      it.setStartAtFirstEntry();
    }

    if (startAtLastEntry) {
      it.setStartAtLastEntry();
    }
    
    if (startAtEntryId!=0) {
      it.setStartAtEntryId(startAtEntryId);
    }
    
    if (startAtPosition!=null) {
      it.setStartAtPosition(startAtPosition);
    }
    
    if (nameOfSingleColumnToRead!=null) {
      it.setNameOfSingleColumnToRead(nameOfSingleColumnToRead);
    }
    
    if (indexOfSingleColumnToRead!=null) {
      it.setIndexOfSingleColumnToRead(indexOfSingleColumnToRead);
    }
    
    if (totalConsumer!=null) {
      it.setTotalReceiver(totalConsumer);
    }
    
    if (categoryConsumer!=null) {
      it.setCategoryReceiver(categoryConsumer);
    }
    
    if (viewIndexChangedHandler!=null) {
      it.setViewIndexChangedHandler(viewIndexChangedHandler);
    }
    
    if (maxBufferEntries!=null) {
      it.setMaxBufferEntries(maxBufferEntries);
    }
    
    if (ftQuery!=null) {
      it.setRestrictionToFTResults(ftQuery, ftMaxDocs, ftFlags);
    }
    
    return it;
  }
  
  /**
   * Collect all note ids configured in the {@link SelectedEntries} object
   * 
   * @return IDTable of selected entries
   */
  JNAIDTable getSelectedEntries() {
    if (m_selectedEntriesResolved==null || m_selectedEntriesResolved.isDisposed()) {
      JNAIDTable idTable = new JNAIDTable(collection.getParentDominoClient());

      if (selectedEntries!=null) {
        boolean subtractMode;
        
        if (selectedEntries.getMode() == SelectMode.AllSelected) {
          subtractMode = true;
          
          JNAIDTable tableWithAllIds = (JNAIDTable) collection.getAllIdsAsIDTable(false);
          idTable.addAll(tableWithAllIds);
          tableWithAllIds.dispose();
        }
        else {
          subtractMode = false;
        }
        
        //manually set note ids
        Set<Integer> noteIds = selectedEntries.getNoteIds();
        if (subtractMode) {
          idTable.removeAll(noteIds);
        }
        else {
          idTable.addAll(noteIds);
        }

        List<SingleColumnLookupKey> singleColLookups = selectedEntries.getLookupKeysSingleCol();
        List<MultiColumnLookupKey> multiColLookups = selectedEntries.getLookupKeysMultiCol();

        if (!singleColLookups.isEmpty() || !multiColLookups.isEmpty()) {
          
          for (SingleColumnLookupKey currKey : singleColLookups) {
            Set<Find> findFlags = EnumSet.of(Find.EQUAL, Find.RANGE_OVERLAP, Find.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(Find.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = getAllIdsByKey(findFlags, currKey.getKey());
            
            if (subtractMode) {
              idTable.removeAll(idsForKey);
            }
            else {
              idTable.addAll(idsForKey);
            }
          }
          
          for (MultiColumnLookupKey currKey : multiColLookups) {
            Set<Find> findFlags = EnumSet.of(Find.EQUAL, Find.RANGE_OVERLAP, Find.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(Find.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = getAllIdsByKey(findFlags, currKey.getKey().toArray(new Object[currKey.getKey().size()]));
            
            if (subtractMode) {
              idTable.removeAll(idsForKey);
            }
            else {
              idTable.addAll(idsForKey);
            }
          }
        }
        
        JNADatabase db = (JNADatabase) collection.getParentDatabase();

        List<DQLTerm> dqlQueries = selectedEntries.getDQLQueries();
        for (DQLTerm currDQLQuery : dqlQueries) {
          if (subtractMode) {
            JNAIDTable tableOfDQLResult = new JNAIDTable(collection.getParentDominoClient());
            db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, tableOfDQLResult);
            idTable.removeAll(tableOfDQLResult);
            tableOfDQLResult.dispose();
          }
          else {
            db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, idTable);
          }
        }
        
        List<String> ftQueries = selectedEntries.getFTQueries();
        for (String currFTQuery : ftQueries) {
          if (subtractMode) {
            JNAIDTable tableOfFTResult = new JNAIDTable(collection.getParentDominoClient());
            db.queryFTIndex(currFTQuery, 0, EnumSet.of(FTQuery.RETURN_IDTABLE), null, 0, 0).collectIds(0, Integer.MAX_VALUE, tableOfFTResult);
            idTable.removeAll(tableOfFTResult);
            tableOfFTResult.dispose();
          }
          else {
            db.queryFTIndex(currFTQuery, 0, EnumSet.of(FTQuery.RETURN_IDTABLE), null, 0, 0).collectIds(0, Integer.MAX_VALUE, idTable);
          }
        }
      }
      
      m_selectedEntriesResolved = idTable;
    }
    return m_selectedEntriesResolved;
  }
  
  /**
   * Collect all note ids configured in the {@link ExpandedEntries} object
   * 
   * @param retIdTable IDTable to clear and write new note ids
   */
  JNAIDTable getExpandedEntries() {
    if (m_expandedEntriesResolved==null || m_expandedEntriesResolved.isDisposed()) {
      JNAIDTable idTable = new JNAIDTable(collection.getParentDominoClient());
      
      if (expandedEntries!=null) {
        if (expandedEntries.getMode() == ExpandMode.AllExpanded) {
          idTable.setInverted(false);
        }
        else {
          idTable.setInverted(true);
        }
        //manually set note ids
        Set<Integer> noteIds = expandedEntries.getNoteIds();
        idTable.addAll(noteIds);

        List<SingleColumnLookupKey> singleColLookups = expandedEntries.getLookupKeysSingleCol();
        List<MultiColumnLookupKey> multiColLookups = expandedEntries.getLookupKeysMultiCol();
        Set<String> categories = expandedEntries.getCategories();
        
        if (!singleColLookups.isEmpty() || !multiColLookups.isEmpty()) {
          
          for (SingleColumnLookupKey currKey : singleColLookups) {
            Set<Find> findFlags = EnumSet.of(Find.EQUAL, Find.RANGE_OVERLAP, Find.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(Find.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = getAllIdsByKey(findFlags, currKey.getKey());
            
            idTable.addAll(idsForKey);
          }
          
          for (MultiColumnLookupKey currKey : multiColLookups) {
            Set<Find> findFlags = EnumSet.of(Find.EQUAL, Find.RANGE_OVERLAP, Find.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(Find.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = getAllIdsByKey(findFlags, currKey.getKey().toArray(new Object[currKey.getKey().size()]));
            
            idTable.addAll(idsForKey);
          }
          
          for (String currCategory : categories) {
            //find category entry
            NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(Find.MATCH_CATEGORYORLEAF,
                Find.REFRESH_FIRST, Find.CASE_INSENSITIVE),
                EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), currCategory);
            
            if (catLkResult.getReturnCount()>0 && !catLkResult.getEntries().isEmpty()) {
              int noteId = catLkResult.getEntries().get(0).getNoteID();
              idTable.add(noteId);
            }
            
          }
        }
        
        JNADatabase db = (JNADatabase) collection.getParentDatabase();

        List<DQLTerm> dqlQueries = expandedEntries.getDQLQueries();
        for (DQLTerm currDQLQuery : dqlQueries) {
          db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, idTable);
        }

        List<String> ftQueries = expandedEntries.getFTQueries();
        for (String currFTQuery : ftQueries) {
          db.queryFTIndex(currFTQuery, 0, EnumSet.of(FTQuery.RETURN_IDTABLE), null, 0, 0).collectIds(0, Integer.MAX_VALUE, idTable);
        }
      }
      else {
        idTable.setInverted(false);
      }
      
      m_expandedEntriesResolved = idTable;
    }
    return m_expandedEntriesResolved;
  }
  
  public LinkedHashSet<Integer> getAllIdsByKey(Set<Find> findFlags, Object... keys) {
    return toNoteIds(getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID), keys));
  }
  
  public List<JNACollectionEntry> getAllEntriesByKey(Set<Find> findFlags,Set<ReadMask> readMask, Object... keys) {
    //do the first lookup and read operation atomically; uses a large buffer for local calls
    
    while (true) {
      List<JNACollectionEntry> allEntries = new ArrayList<>();

      int remainingEntries;
      String firstMatchPosStr;
      int entriesToSkipOnFirstLoopRun = 0;

      int initialIndexMod = 0;
      
      if (canUseOptimizedLookupForKeyLookup(findFlags, readMask, keys)) {
        //use atomic find method that also reads the first 64k matches
        NotesViewLookupResultData data = collection.findByKeyExtended2(findFlags, readMask, keys);
        initialIndexMod = data.getIndexModifiedSequenceNo();
        
        int numEntriesFound = data.getReturnCount();
        if (numEntriesFound==-1) {
          data = collection.findByKeyExtended2(findFlags, EnumSet.noneOf(ReadMask.class), keys);
          
          numEntriesFound = data.getReturnCount();
        }
        
        firstMatchPosStr = data.getPosition();

        if (numEntriesFound!=-1) {
          List<JNACollectionEntry> entries = data.getEntries();
          //copy the data we have read
          allEntries.addAll(entries);

          if (!data.hasMoreToDo() && entries.size()>=numEntriesFound) {
            //we are done; return what we have, even if we get a index change signal,
            //because the data has been read with index r/w lock
            return allEntries;
          }

          if (data.hasAnyNonDataConflicts()) {
            if (!data.isViewTimeRelative()) {
              collection.refresh();
              continue;
            }
          }

          entriesToSkipOnFirstLoopRun = entries.size();

          //compute what we have left
          remainingEntries = numEntriesFound - entries.size();
        }
        else {
          initialIndexMod = collection.getIndexModifiedSequenceNo();
          
          //workaround for the case where the method NIFFindByKeyExtended2 returns -1 as numEntriesFound
          //and no buffer data (result exceeds 64k)
          //
          //fallback to classic lookup
          FindResult findResult = collection.findByKey(findFlags, keys);
          remainingEntries = findResult.getEntriesFound();

          firstMatchPosStr = findResult.getPosition();
        }
      }
      else {
        initialIndexMod = collection.getIndexModifiedSequenceNo();
        
        //first find the start position to read data
        FindResult findResult = collection.findByKey(findFlags, keys);
        remainingEntries = findResult.getEntriesFound();
        if (remainingEntries==0) {
          return allEntries;
        }
        firstMatchPosStr = findResult.getPosition();
      }

      if (remainingEntries==0) {
        return allEntries;
      }

      if (!canFindExactNumberOfMatches(findFlags)) {
        Direction currSortDirection = collection.getCurrentSortDirection().orElse(null);
        if (currSortDirection!=null) {
          //handle special case for inquality search where column sort order matches the find flag,
          //so we can read all view entries after findResult.getPosition()
          
          if (currSortDirection==Direction.Ascending && findFlags.contains(Find.GREATER_THAN)) {
            //read all entries after findResult.getPosition()
            remainingEntries = Integer.MAX_VALUE;
          }
          else if (currSortDirection==Direction.Descending && findFlags.contains(Find.LESS_THAN)) {
            //read all entries after findResult.getPosition()
            remainingEntries = Integer.MAX_VALUE;
          }
        }
      }

      if (firstMatchPosStr!=null) {
        //position of the first match; we skip (entries.size()) to read the remaining entries
        boolean isFirstLookup = true;
        
        JNADominoCollectionPosition currLookupPos = new JNADominoCollectionPosition(firstMatchPosStr);
        
        boolean viewIndexChanged = false;
        
        while (remainingEntries>0) {
          int maxBufferEntries = remainingEntries;
          
          //on first lookup, start at "posStr" and skip the amount of already read entries
          NotesViewLookupResultData data2 = collection.readEntriesExt(currLookupPos,
              Navigate.NEXT_DOCUMENT,
              false,
              isFirstLookup ? entriesToSkipOnFirstLoopRun : 1,
              Navigate.NEXT_DOCUMENT,
              maxBufferEntries,
              readMask,
              null,
              null,
              indexOfSingleColumnToRead);
          
          int indexMod2 = collection.getIndexModifiedSequenceNo();
          if (initialIndexMod==0) {
            initialIndexMod = indexMod2;
          }
          else if (!data2.isViewTimeRelative()) {
            if (initialIndexMod!=indexMod2 || data2.hasAnyNonDataConflicts()) {
              viewIndexChanged=true;
              break;
            }
          }
          
          isFirstLookup=false;
          
          List<JNACollectionEntry> entries = data2.getEntries();
          
          if (entries.isEmpty()) {
            //looks like we don't have any more data in the view
            break;
          }
          
          allEntries.addAll(entries);
          
          remainingEntries = remainingEntries - entries.size();
        }
        
        if (viewIndexChanged) {
          //refresh view and redo the whole lookup
          collection.refresh();
          continue;
        }
        
      }
      
      return allEntries;
    }
  }
  
  /**
   * If the specified find flag uses an inequality search like {@link FindFlag#LESS_THAN}
   * or {@link FindFlag#GREATER_THAN}, this method returns true, meaning that
   * the Notes API cannot return an exact number of matches.
   * 
   * @param findFlags find flags
   * @return true if exact number of matches can be returned
   */
  private boolean canFindExactNumberOfMatches(Set<Find> findFlags) {
    if (findFlags.contains(Find.LESS_THAN)) {
      return false;
    }
    else if (findFlags.contains(Find.GREATER_THAN)) {
      return false;
    }
    else {
      return true;
    }
  }
  
  /**
   * Method to check whether an optimized view lookup method can be used for
   * a set of find/return flags and the current Domino version
   * 
   * @param findFlags find flags
   * @param returnMask return flags
   * @param keys lookup keys
   * @return true if method can be used
   */
  private boolean canUseOptimizedLookupForKeyLookup(Set<Find> findFlags, Object... keys) {
    if (findFlags.contains(Find.GREATER_THAN) || findFlags.contains(Find.LESS_THAN)) {
      //TODO check this with IBM dev; we had crashes like "[0A0F:0002-21A00] PANIC: LookupHandle: null handle" using NIFFindByKeyExtended2
      return false;
    }
    
    return true;
  }
  
  private LinkedHashSet<Integer> toNoteIds(List<JNACollectionEntry> entries) {
    LinkedHashSet<Integer> ids = new LinkedHashSet<>();
    entries.forEach((entry) -> {
      ids.add(entry.getNoteID());
    });
    return ids;
  }
  
}
