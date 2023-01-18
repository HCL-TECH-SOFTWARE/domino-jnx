package com.hcl.domino.jna.data;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.hcl.domino.commons.views.FindFlag;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery.ExpandMode;
import com.hcl.domino.data.CollectionSearchQuery.ExpandedEntries;
import com.hcl.domino.data.CollectionSearchQuery.MultiColumnLookupKey;
import com.hcl.domino.data.CollectionSearchQuery.SelectMode;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.CollectionSearchQuery.SingleColumnLookupKey;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.NotesConstants;

public class CollectionEntryIteratorBuilder implements Iterable<CollectionEntry> {
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
  
  //computed note ids of selected / expanded entries
  private JNAIDTable m_selectedEntriesResolved;
  private JNAIDTable m_expandedEntriesResolved;

  public static CollectionEntryIteratorBuilder newBuilder(JNADominoCollection collection) {
    CollectionEntryIteratorBuilder builder = new CollectionEntryIteratorBuilder();
    builder.collection = collection;
    return builder;
  }
  
  JNADominoCollection getCollection() {
    return collection;
  }
  
  public CollectionEntryIteratorBuilder withReadMask(Set<ReadMask> readMask) {
    this.readMask = readMask;
    return this;
  }
  
  Set<ReadMask> getReadMask() {
    return readMask;
  }
  
  public CollectionEntryIteratorBuilder withDirection(Navigate direction) {
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
  public CollectionEntryIteratorBuilder withExpandedEntries(ExpandedEntries expandedEntries) {
    this.expandedEntries = expandedEntries;
    return this;
  }

  ExpandedEntries getExpandedEntries() {
    return expandedEntries;
  }
  
  /**
   * Defines selected collection rows to filter the output, e.g. via key lookup
   * 
   * @param selectedEntries e.g. {@link SelectedEntries#selectAll()} or {@link SelectedEntries#deselectAll()} followed by adding note ids of documents or category entries
   * @return this builder instance
   */
  public CollectionEntryIteratorBuilder withSelectedEntries(SelectedEntries selectedEntries) {
    this.selectedEntries = selectedEntries;
    return this;
  }
  
  /**
   * Sets a top level category to restrict the returned collection entries
   * 
   * @param category category, e.g. "A" or "A\B" for deeper level
   * @return this builder instance
   */
  public CollectionEntryIteratorBuilder withRestrictionToCategory(String category) {
    return withRestrictionToCategory(Arrays.asList(category));
  }
  
  /**
   * Sets a top level category to restrict the returned collection entries
   * 
   * @param categoryLevels list of category value for each entry (e.g. as string or number)
   * @return this builder instance
   */
  public CollectionEntryIteratorBuilder withRestrictionToCategory(List<Object> categoryLevels) {
    this.categoryLevels = categoryLevels;
    return this;
  }

  /**
   * Sets the start entry for reading to a specific view index position
   * 
   * @param pos position
   * @return this builder instance
   */
  public CollectionEntryIteratorBuilder withStartAtPosition(String pos) {
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
  public CollectionEntryIteratorBuilder withStartAtEntryId(int noteId) {
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
  public CollectionEntryIteratorBuilder withStartAtLastEntry() {
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
  public CollectionEntryIteratorBuilder withStartAtFirstEntry() {
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
  public CollectionEntryIteratorBuilder withSkip(int skip) {
    this.skip = skip;
    return this;
  }
  
  /**
   * Sets the number of entries to return (for paging)
   * 
   * @param limit max number of entries
   * @return this builder instance
   */
  public CollectionEntryIteratorBuilder withLimit(int limit) {
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
  public CollectionEntryIteratorBuilder withTotalReceiver(Consumer<Integer> totalConsumer) {
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
  public CollectionEntryIteratorBuilder withCategoryReceiver(Consumer<CollectionEntry> categoryConsumer) {
    this.categoryConsumer = categoryConsumer;
    return this;
  }
  
  @Override
  public Iterator<CollectionEntry> iterator() {
    JNACollectionEntryIterator it = new JNACollectionEntryIterator(this);
    
    it.setSkip(skip);
    it.setCount(limit);
    
    if (readMask!=null) {
      it.setReadMask(readMask);
    }
    
    if (direction!=null) {
      it.setDirection(direction);
    }
    
    if (expandedEntries!=null) {
      JNAIDTable idTable = getResolveExpandedEntries();
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
    
    if (totalConsumer!=null) {
      it.setTotalReceiver(totalConsumer);
    }
    
    if (categoryConsumer!=null) {
      it.setCategoryReceiver(categoryConsumer);
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
            Set<FindFlag> findFlags = EnumSet.of(FindFlag.EQUAL, FindFlag.RANGE_OVERLAP, FindFlag.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(FindFlag.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
                new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey());
            
            if (subtractMode) {
              idTable.removeAll(idsForKey);
            }
            else {
              idTable.addAll(idsForKey);
            }
          }
          
          for (MultiColumnLookupKey currKey : multiColLookups) {
            Set<FindFlag> findFlags = EnumSet.of(FindFlag.EQUAL, FindFlag.RANGE_OVERLAP, FindFlag.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(FindFlag.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
                new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey().toArray(new Object[currKey.getKey().size()]));
            
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
  JNAIDTable getResolveExpandedEntries() {
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
            Set<FindFlag> findFlags = EnumSet.of(FindFlag.EQUAL, FindFlag.RANGE_OVERLAP, FindFlag.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(FindFlag.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
                new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey());
            
            idTable.addAll(idsForKey);
          }
          
          for (MultiColumnLookupKey currKey : multiColLookups) {
            Set<FindFlag> findFlags = EnumSet.of(FindFlag.EQUAL, FindFlag.RANGE_OVERLAP, FindFlag.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(FindFlag.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
                new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey().toArray(new Object[currKey.getKey().size()]));
            
            idTable.addAll(idsForKey);
          }
          
          for (String currCategory : categories) {
            //find category entry
            NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(FindFlag.MATCH_CATEGORYORLEAF,
                FindFlag.REFRESH_FIRST, FindFlag.RETURN_DWORD, FindFlag.AND_READ_MATCHES, FindFlag.CASE_INSENSITIVE),
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
  
}
