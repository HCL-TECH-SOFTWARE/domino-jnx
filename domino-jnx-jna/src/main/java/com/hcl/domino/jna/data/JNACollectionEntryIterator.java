package com.hcl.domino.jna.data;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.FindFlag;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery.ExpandedEntries;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADominoCollectionAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.ptr.ShortByReference;

public class JNACollectionEntryIterator implements Iterator<CollectionEntry> {
  private CollectionEntries builder;
  private JNADominoCollection collection;
  private Navigate direction = Navigate.NEXT_ENTRY;
  private Set<ReadMask> readMask = EnumSet.of(ReadMask.NOTEID);
  
  private int skip;
  private int count = -1;
  
  private Object[] categoryLevelsAsArr;
  private boolean m_startAtLastEntry;
  private boolean m_startAtFirstEntry;
  private String m_startAtPosition;
  private int m_startAtEntryId;

  private String nameOfSingleColumnToRead;
  private Integer indexOfSingleColumnToRead;

  private Function<DominoCollection, Action> viewIndexChangedHandler;

  //IDTables to control what is expanded/selected
  private ExpandedEntries m_expandedEntries;
  private JNAIDTable m_expandedEntriesResolved;
  private boolean m_hasExpandedEntries;
  
  private SelectedEntries m_selectedEntries;
  private JNAIDTable m_selectedEntriesResolved;
  private boolean m_hasSelectionSet;
  
  // computed values during traversal
  
  private JNADominoCollectionPosition categoryEntryPos;
  
  private boolean firstRun;
  private int initialSkip;
  private Integer total;
  private Consumer<Integer> totalConsumer;
  private Consumer<CollectionEntry> categoryConsumer;
  private Navigate skipNav;
  private Navigate returnNav;
  private JNADominoCollectionPosition currPosForTotalComputation;
  private JNADominoCollectionPosition currPos;
  private int minLevel;
  private int entriesReturned;
  
  private boolean initialized;
  private LinkedList<CollectionEntry> nextPage;
  private int pageSize = 20;
  private boolean isDone;
  

  public JNACollectionEntryIterator(CollectionEntries builder) {
    this.builder = builder;
    this.collection = builder.getCollection();
  }
  
  public JNACollectionEntryIterator setPageSize(int pageSize) {
    if (pageSize < 1) {
      throw new IllegalArgumentException(MessageFormat.format("Page size must be 1 or higher: {0}", pageSize));
    }
    this.pageSize = pageSize;
    return this;
  }
  
  public JNACollectionEntryIterator setSkip(int skip) {
    this.skip = skip;
    return this;
  }
  
  public JNACollectionEntryIterator setCount(int count) {
    this.count = count;
    return this;
  }
  
  public JNACollectionEntryIterator setTotalReceiver(Consumer<Integer> totalConsumer) {
    this.totalConsumer = totalConsumer;
    return this;
  }
  
  public JNACollectionEntryIterator setCategoryReceiver(Consumer<CollectionEntry> categoryConsumer) {
    this.categoryConsumer = categoryConsumer;
    return this;
  }
  
  public JNACollectionEntryIterator setDirection(Navigate direction) {
    this.direction = direction;
    return this;
  }
  
  public JNACollectionEntryIterator setReadMask(Set<ReadMask> readMask) {
    this.readMask = EnumSet.copyOf(readMask);
    return this;
  }
  
  public JNACollectionEntryIterator setRestrictToCategory(List<Object> categoryLevels) {
    categoryLevelsAsArr = categoryLevels==null ? null : categoryLevels.toArray(new Object[categoryLevels.size()]);
    return this;
  }
  
  public JNACollectionEntryIterator setNameOfSingleColumnToRead(String nameOfSingleColumnToRead) {
    this.nameOfSingleColumnToRead = nameOfSingleColumnToRead;
    return this;
  }

  public JNACollectionEntryIterator setIndexOfSingleColumnToRead(int indexOfSingleColumnToRead) {
    this.indexOfSingleColumnToRead = indexOfSingleColumnToRead;
    return this;
  }

  public JNACollectionEntryIterator setViewIndexChangedHandler(Function<DominoCollection, Action> handler) {
    this.viewIndexChangedHandler = handler;
    return this;
  }
  
  public JNACollectionEntryIterator setStartAtFirstEntry() {
    this.m_startAtFirstEntry = true;
    this.m_startAtLastEntry = false;
    this.m_startAtPosition = null;
    this.m_startAtEntryId = 0;
    return this;
  }

  public JNACollectionEntryIterator setStartAtLastEntry() {
    this.m_startAtLastEntry = true;
    this.m_startAtFirstEntry = false;
    this.m_startAtPosition = null;
    this.m_startAtEntryId = 0;
    return this;
  }
  
  public JNACollectionEntryIterator setStartAtPosition(String pos) {
    this.m_startAtPosition = pos;
    this.m_startAtFirstEntry = false;
    this.m_startAtLastEntry = false;
    this.m_startAtEntryId = 0;
    return this;
  }

  public JNACollectionEntryIterator setStartAtEntryId(int noteId) {
    this.m_startAtEntryId = noteId;
    this.m_startAtFirstEntry = false;
    this.m_startAtLastEntry = false;
    this.m_startAtPosition = null;
    return this;
  }

  private boolean isUpwardDirection(Navigate nav) {
    return !isDownwardDirection(nav);
  }
  
  private boolean isDownwardDirection(Navigate nav) {
    switch (nav) {
    case CHILD_ENTRY:
    case NEXT_CATEGORY:
    case NEXT_DOCUMENT:
    case NEXT_ENTRY:
    case NEXT_EXPANDED:
    case NEXT_EXPANDED_CATEGORY:
    case NEXT_EXPANDED_DOCUMENT:
    case NEXT_EXPANDED_SELECTED:
    case NEXT_EXPANDED_UNREAD:
    case NEXT_ON_SAME_LEVEL:
    case NEXT_ON_TOPLEVEL:
    case NEXT_PARENT_ENTRY:
    case NEXT_SELECTED:
    case NEXT_SELECTED_ON_TOPLEVEL:
    case NEXT_UNREAD_ENTRY:
    case NEXT_UNREAD_TOPLEVEL_ENTRY:
    case LAST_ON_SAME_LEVEL:
      return true;
      default:
        return false;
    }
  }
  
  public JNACollectionEntryIterator select(SelectedEntries selectedEntries, JNAIDTable idTable) {
    m_selectedEntries = selectedEntries;
    m_selectedEntriesResolved = idTable;
    m_hasSelectionSet = true;
    
    if (!isDirectionWithSelection(direction)) {
      //automatically select a traversal strategy that makes use of
      //selection / expanded info
      
      if (isDownwardDirection(direction)) {
        if (isDirectionWithExpandCollapse(direction)) {
          this.direction = Navigate.NEXT_EXPANDED_SELECTED;
        }
        else {
          this.direction = Navigate.NEXT_SELECTED;
        }
      }
      else {
        if (isDirectionWithExpandCollapse(direction)) {
          this.direction = Navigate.PREV_EXPANDED_SELECTED;
        }
        else {
          this.direction = Navigate.PREV_SELECTED;
        }
      }
    }
    return this;
  }

  public JNACollectionEntryIterator expand(ExpandedEntries expandedEntries, JNAIDTable idTable) {
    m_expandedEntries = expandedEntries;
    m_expandedEntriesResolved = idTable;
    m_hasExpandedEntries = true;
    
    if (!isDirectionWithExpandCollapse(direction)) {
      //automatically select a traversal strategy that makes use of
      //selection / expanded info
      
      if (isDownwardDirection(direction)) {
        if (isDirectionWithSelection(direction)) {
          this.direction = Navigate.NEXT_EXPANDED_SELECTED;
        }
        else {
          this.direction = Navigate.NEXT_EXPANDED;
        }
      }
      else {
        if (isDirectionWithSelection(direction)) {
          this.direction = Navigate.PREV_EXPANDED_SELECTED;
        }
        else {
          this.direction = Navigate.PREV_EXPANDED;
        }
      }
    }
    return this;
  }
  
  @Override
  public boolean hasNext() {
    init();
    
    if (count==0 || entriesReturned>=count) {
      return false;
    }
    else if (nextPage==null) {
      //first call
      fetchNextPage();
    }
    else if (nextPage.isEmpty()) {
      //we need to fetch more entries
      if (isDone) {
        //but we have reached the end of the view
        return false;
      }
      else {
        fetchNextPage();
      }
    }
    
    return !nextPage.isEmpty();
  }

  @Override
  public CollectionEntry next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    
    CollectionEntry entry = nextPage.removeFirst();
    entriesReturned++;
    return entry;
  }

  private void markNoData() {
    currPos = null;
    nextPage = new LinkedList<>();
    isDone = true;
  }
  
  public int getTotal() {
    if (total==null) {
      init();
      
      if (total==null) { // init() might set total
        System.out.println("getTotal - currPos before: "+currPosForTotalComputation);
        
        JNADominoCollectionPosition tmpPos = (JNADominoCollectionPosition) currPosForTotalComputation.clone();
        
        NotesViewLookupResultData skipAllLkResult =
            collection.readEntriesExt(tmpPos,
            skipNav, true,
            Integer.MAX_VALUE, Navigate.CURRENT,
            0, readMask, (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) indexOfSingleColumnToRead);
        
        total = skipAllLkResult.getSkipCount();
        if (initialSkip == 0) {
          //add the first position to the count
          total++;
        }
      }
    }
    return total;
  }
  
  private void init() {
    if (initialized) {
      return;
    }
    
    initialized = true;
    firstRun = true;
    
    JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);
    
    ShortByReference updateFiltersFlags = new ShortByReference();
    //init selected / expanded IDTables and returns the direction to read entries
    Navigate directionToUse = prepareCollectionReadRestrictions(collectionAllocations, updateFiltersFlags);
    
    this.skipNav = directionToUse;
    this.returnNav = directionToUse;
    
    if (skipNav == Navigate.FIRST_ON_SAME_LEVEL) {
      //just return first element
      returnNav = Navigate.CURRENT;
    }
    else if (skipNav == Navigate.LAST_ON_SAME_LEVEL) {
      //just return last element
      returnNav = Navigate.CURRENT;
    }
    
    if (returnNav == Navigate.CURRENT && count>1) {
      //prevent reading too many entries if navigation is set to just read the current entry
      count = 1;
    }
    
    if (count < 0) {
      count = Integer.MAX_VALUE;
    }
    
    pageSize = Math.min(pageSize, count);

    if (this.indexOfSingleColumnToRead==null && this.nameOfSingleColumnToRead!=null) {
      this.indexOfSingleColumnToRead = collection.getColumnValuesIndex(nameOfSingleColumnToRead);
    }

    final short updateFiltersFlagsVal = updateFiltersFlags.getValue();
    if (updateFiltersFlagsVal != 0) {
      LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
        if (!collection.isDisposed()) {
          
          //the method prepareCollectionReadRestrictions has modified the selected list; for remote databases, push IDTable changes via NRPC

          short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, updateFiltersFlagsVal);
          NotesErrorUtils.checkResult(result);
        }
        
        return null;
      });
    }
    
    //compute where to start reading
    
    if (categoryLevelsAsArr!=null) {
      //we should only read a subset of the view
      
      //add INDEXPOSITION to be able to check if we're still below the category
      readMask.add(ReadMask.INDEXPOSITION);

      //find category entry position

      JNACollectionEntry categoryEntry = findCategoryPosition(collection, categoryLevelsAsArr).orElse(null);
      
      if (categoryEntry==null) {
        //category not found or gone
        markNoData();
        return;
      }

      if (categoryConsumer!=null) {
        categoryConsumer.accept(categoryEntry);
      }
      
      String categoryPosStr = categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
      categoryEntryPos = new JNADominoCollectionPosition(categoryPosStr);
      minLevel = categoryEntryPos.getLevel()+1;

      if (m_startAtPosition!=null) {
        //check if start position is within our subset
        
        if (!m_startAtPosition.startsWith(categoryPosStr+".")) { //$NON-NLS-1$
          //start position is out of scope
          markNoData();
          return;
        }
        
        currPos = new JNADominoCollectionPosition(m_startAtPosition);
      }
      else if (m_startAtEntryId!=0) {
        String entryPos = collection.locateNote(categoryPosStr, m_startAtEntryId);
        if (StringUtil.isEmpty(entryPos)) {
          //entry not found
          markNoData();
          return;
        }
        
        currPos = new JNADominoCollectionPosition(entryPos);
      }
      else if (m_startAtLastEntry) {
        //skip over the whole view subtree to find the last entry based on the selection / expanded entries
        JNADominoCollectionPosition lastEntrySearchStartPos = new JNADominoCollectionPosition(categoryPosStr);
        lastEntrySearchStartPos.setMinLevel(minLevel);
        
        //make sure we navigate downwards
        Navigate skipNavDownwards = toDownwardsDirection(skipNav);
        
        NotesViewLookupResultData lastEntryLkResult =
            collection.readEntriesExt(lastEntrySearchStartPos,
            skipNavDownwards, true,
            Integer.MAX_VALUE, Navigate.CURRENT,
            1, EnumSet.of(ReadMask.INDEXPOSITION), (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) indexOfSingleColumnToRead);
        
        if (isUpwardDirection(skipNav)) {
          //if our direction is upwards, we already counted the total entries to traverse
          total = lastEntryLkResult.getSkipCount();
        }
        
        String lastEntryPosStr = ""; //$NON-NLS-1$
        
        if (!lastEntryLkResult.getEntries().isEmpty()) {
          lastEntryPosStr = lastEntryLkResult
              .getEntries()
              .get(0)
              .getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
        }
        
        if (StringUtil.isEmpty(lastEntryPosStr)) {
          //view is empty
          markNoData();
          return;
        }
        else if (!lastEntryPosStr.startsWith(categoryPosStr+".")) { //$NON-NLS-1$
          //somehow our found last position is out of scope
          markNoData();
          return;
        }
        else {
          currPos = new JNADominoCollectionPosition(lastEntryPosStr);
        }
      }
      else {
        currPos = new JNADominoCollectionPosition(categoryPosStr+".0"); //$NON-NLS-1$
        initialSkip = 1;
      }
      
      currPos.setMinLevel(minLevel);
      currPosForTotalComputation = (JNADominoCollectionPosition) currPos.clone();
    }
    else {
      //no top level category
      
      categoryEntryPos = null;
      minLevel = 0;
      
      if (m_startAtLastEntry) {
      //skip over the whole view subtree to find the last entry based on the selection / expanded entries
        JNADominoCollectionPosition lastEntrySearchStartPos = new JNADominoCollectionPosition("0"); //$NON-NLS-1$
        
        //make sure we navigate downwards
        Navigate skipNavDownwards = toDownwardsDirection(skipNav);
        
        NotesViewLookupResultData lastEntryLkResult =
            collection.readEntriesExt(lastEntrySearchStartPos,
            skipNavDownwards, true,
            Integer.MAX_VALUE, Navigate.CURRENT,
            1, EnumSet.of(ReadMask.INDEXPOSITION), (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) indexOfSingleColumnToRead);
        
        if (isUpwardDirection(skipNav)) {
          //if our direction is upwards, we already counted the total entries to traverse
          total = lastEntryLkResult.getSkipCount();
        }
        
        String lastEntryPosStr = ""; //$NON-NLS-1$
        
        if (!lastEntryLkResult.getEntries().isEmpty()) {
          lastEntryPosStr = lastEntryLkResult
              .getEntries()
              .get(0)
              .getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
        }
        
        if (StringUtil.isEmpty(lastEntryPosStr)) {
          //view is empty
          markNoData();
          return;
        }
        else {
          currPos = new JNADominoCollectionPosition(lastEntryPosStr);
        }
      }
      else if (m_startAtEntryId!=0) {
        readMask.add(ReadMask.INIT_POS_NOTEID);
        currPos = new JNADominoCollectionPosition(Integer.toString(m_startAtEntryId));
      }
      else if (m_startAtPosition!=null) {
        currPos = new JNADominoCollectionPosition(m_startAtPosition);
      }
      else {
        currPos = new JNADominoCollectionPosition("0"); //$NON-NLS-1$
        initialSkip = 1;
      }
      
      currPosForTotalComputation = (JNADominoCollectionPosition) currPos.clone();
    }
  }
  
  private void fetchNextPage() {
    init();
    
    if (isDone || currPos==null) {
      return;
    }
    
    boolean wasFirstRun = firstRun;
    firstRun = false;
    
    if (nextPage==null) {
      nextPage = new LinkedList<>();
    }
    
    if (wasFirstRun && totalConsumer!=null) {
      int total = getTotal();
      totalConsumer.accept(total);
    }
 
    JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);

    LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {

      if (categoryLevelsAsArr!=null) {
        //normally the following code should run in one loop;
        //we only rerun the lookup if an index change is detected AND the top category has moved,
        //because in this case we cannot be sure that the data we read are valid
        while (true) {
          //save a copy of the current position in case we detect index changes
          JNADominoCollectionPosition currPosCopy = (JNADominoCollectionPosition) currPos.clone();
          
          NotesViewLookupResultData lkResult =
              collection.readEntriesExt(currPos,
                  skipNav,
                  false,
                  wasFirstRun ? (initialSkip + skip) : 1,
                  returnNav,
                  pageSize,
                  readMask,
                  (DominoDateTime) null,
                  (JNAIDTable) null,
                  indexOfSingleColumnToRead);
          
          if (lkResult.hasAnyNonDataConflicts()) {
            //view index has changed, update the view
            if (!lkResult.isViewTimeRelative()) {
              if (viewIndexChangedHandler!=null) {
                Action action = viewIndexChangedHandler.apply(collection);
                if (action==Action.Stop) {
                  isDone = true;
                  return null;
                }
              }
              collection.refresh();
            }

            //check if our category position is still correct; if yes, we can use the lookup result
            JNACollectionEntry newCategoryEntry = findCategoryPosition(collection, categoryLevelsAsArr).orElse(null);
            
            if (newCategoryEntry==null) {
              //category disappeared, return what we have read; since we cannot be sure that "lkResult" contains
              //data of the right category, we throw it away
              markNoData();
              return null;
            }
            
            String newCategoryPosStr = newCategoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION,
                String.class, ""); //$NON-NLS-1$
            
            if (StringUtil.isEmpty(newCategoryPosStr)) {
              //category disappeared, return what we have read; since we cannot be sure that "lkResult" contains
              //data of the right category, we throw it away
              markNoData();
              return null;
            }
            
            JNADominoCollectionPosition newCategoryPos = new JNADominoCollectionPosition(newCategoryPosStr);

            if (!newCategoryPos.equals(categoryEntryPos)) {
              //the category has changed position, so it's possible that our data comes from a different category;
              //it's better to rerun this iteration at the current offset
              
              int[] newTransposedCurrTumbler = new int[32];
              int newCategoryPosLevel = newCategoryPos.getLevel();
              
              //copy category position infos to move "currPosCopy" below new category
              for (int i=0; i<newCategoryPosLevel; i++) {
                newTransposedCurrTumbler[i] = newCategoryPos.getTumbler(i);
              }
              
              //and add position infos below old category
              for (int i=newCategoryPosLevel; i<32; i++) {
                newTransposedCurrTumbler[i] = currPosCopy.getTumbler(i);
              }
              currPos = new JNADominoCollectionPosition(newTransposedCurrTumbler);
              currPos.setMinLevel(minLevel);
              //remember our new top category position
              categoryEntryPos = newCategoryPos;
              
              continue;
            }
            
          }
          
          List<JNACollectionEntry> entries = lkResult.getEntries();
          if (entries.isEmpty()) {
            //no data received
            isDone = true;
          }
          else {
            if (entries.size() < pageSize && !lkResult.hasMoreToDo()) {
              //less data received than requested and there's not more in the view
              isDone = true;
            }
            
            for (JNACollectionEntry currEntry : entries) {
              int[] currEntryPosArr = currEntry.getSpecialValue(SpecialValue.INDEXPOSITION, int[].class, null);
              if (currEntryPosArr==null) {
                throw new IllegalStateException(
                    MessageFormat.format("Expected INDEXPOSITION attribute is missing in returned collection entry: {0}",
                        currEntry));
              }
              JNADominoCollectionPosition currEntryPos = new JNADominoCollectionPosition(currEntryPosArr);
              
              if (currEntryPos.isDescendantOf(categoryEntryPos)) {
                //make sure we are in the right category
                nextPage.add(currEntry);
              }
            }
          }
          
          break;
        }
      }
      else {
        //no top category
        System.out.println("pre-readEntriesExt: currPos="+currPos+", skipNav="+skipNav+", returnNav="+returnNav);
        NotesViewLookupResultData lkResult = collection.readEntriesExt(currPos,
                skipNav,
                  false,
                  wasFirstRun ? (initialSkip + skip) : 1,
                  returnNav,
                  pageSize,
                  readMask,
                  (DominoDateTime) null,
                (JNAIDTable) null,
                indexOfSingleColumnToRead);
        
        if (lkResult.hasAnyNonDataConflicts()) {
          if (!lkResult.isViewTimeRelative()) {
            if (viewIndexChangedHandler!=null) {
              Action action = viewIndexChangedHandler.apply(collection);
              if (action==Action.Stop) {
                isDone = true;
                return null;
              }
            }
          }
        }
        
        List<JNACollectionEntry> entries = lkResult.getEntries();
        System.out.println("post-readEntriesExt: currPos="+currPos+", entries.size="+entries.size());

        if (entries.isEmpty()) {
          isDone = true;
        }
        else {
          for (JNACollectionEntry currEntry : entries) {
            nextPage.add(currEntry);
          }
        }
        
        if (readMask.contains(ReadMask.INIT_POS_NOTEID)) {
          //make sure to only use this flag on the first lookup call
          readMask.remove(ReadMask.INIT_POS_NOTEID);
        }
      }
      
      return null;
    });
  
  }
  
  /**
   * 
   * @param collectionAllocations
   * @param updateFiltersFlags
   * @return navigate direction for the collection lookup (using m_direction
   */
  private Navigate prepareCollectionReadRestrictions(JNADominoCollectionAllocations collectionAllocations,
      ShortByReference updateFiltersFlags) {
    
    short updateFiltersFlagsVal = updateFiltersFlags.getValue();
    
    Navigate directionToUse = direction;
    if (directionToUse==null) {
      //read all entries by default
      directionToUse = Navigate.NEXT_ENTRY;
    }

    if (m_hasSelectionSet) {
      //make sure that the navigation direction respects the selection; noop if already the case
      directionToUse = addSelectionNavigation(directionToUse);
    }

    if (m_hasExpandedEntries) {
      //make sure that the navigation direction respects expanded entries; noop if already the case
      directionToUse = addExpandNavigation(directionToUse);
    }

    if (isDirectionWithSelection(directionToUse)) {
      //resolve and set the selected entries idtable
      JNAIDTable resolvedSelectedList = m_selectedEntriesResolved!=null ? (JNAIDTable) m_selectedEntriesResolved.clone() : new JNAIDTable(collection.getParentDominoClient());
      
      if (collection.isHierarchical()) {
        //Views with response hierarchy can have issues when working with NAVIGATE_NEXT_SELECTED.
        //We found out that as soon as the first response doc appears in the view index,
        //NIFReadEntries returns the wrong COLLECTIONPOSITION when reading view data via
        //selected list with less than 5000 note ids. In that case NIFReadEntries internally
        //creates a temp collection and messes things up, e.g. we get
        //
        //noteid=3662, pos=8.1
        //noteid=3662, pos=8.1
        //
        //although this would be correct:
        //
        //noteid=3662, pos=8.1
        //noteid=3662, pos=19.1
        //
        //which is very bad for our classic NIFReadEntries loop because it might run forever;
        //that's why we fill up the selected list with fake note ids so that it contains 5000 note ids.
        //for >=5000 note ids, NIF does not create a temp collection, but scans the collection index,
        //which is slower but correct. It should be avoided to set the flag "show response documents in hierarchy".
        int resolvedSelectedListSize = resolvedSelectedList.size();
        if (resolvedSelectedListSize<5000) {
          IDTable allIDsInView = collection.getAllIdsAsIDTable(false);

          int fakeNoteIdsToInsert = 5000 - resolvedSelectedListSize;
          int maxFakeNoteId = 2147483644;

          for (int i=0; i<fakeNoteIdsToInsert; i++) {
            int currNoteId = maxFakeNoteId - i*4;

            // make sure the ID we add does not exist in the view
            if (!allIDsInView.contains(currNoteId)) {
              resolvedSelectedList.add(currNoteId);
            }
          }
        }
      }
      
      JNAIDTable selectedList = collectionAllocations.getSelectedList();
      selectedList.clear();
      selectedList.addAll(resolvedSelectedList);
      selectedList.setInverted(resolvedSelectedList.isInverted());
      
      updateFiltersFlagsVal |= NotesConstants.FILTER_SELECTED;
    }
    
    if (isDirectionWithExpandCollapse(directionToUse)) {
      //resolve and set the expanded entries idtable
      JNAIDTable resolvedCollapsedList = m_expandedEntriesResolved!=null ? (JNAIDTable) m_expandedEntriesResolved.clone() : new JNAIDTable(collection.getParentDominoClient());
      boolean listContainsCollapsedEntries = !resolvedCollapsedList.isInverted();
      
      if (resolvedCollapsedList.isEmpty() && listContainsCollapsedEntries) {
        //all expanded, nothing collapsed
        //simplify nav to skip NIFUpdateFilters call if not required
        switch (directionToUse) {
        case NEXT_EXPANDED:
          directionToUse = Navigate.NEXT_ENTRY;
          break;
        case NEXT_EXPANDED_CATEGORY:
          directionToUse = Navigate.NEXT_CATEGORY;
          break;
        case NEXT_EXPANDED_DOCUMENT:
          directionToUse = Navigate.NEXT_DOCUMENT;
          break;
        case NEXT_EXPANDED_SELECTED:
          directionToUse = Navigate.NEXT_SELECTED;
          break;
        case NEXT_EXPANDED_UNREAD:
          directionToUse = Navigate.NEXT_UNREAD_ENTRY;
          break;
        case PREV_EXPANDED:
          directionToUse = Navigate.PREV_ENTRY;
          break;
        case PREV_EXPANDED_CATEGORY:
          directionToUse = Navigate.PREV_CATEGORY;
          break;
        case PREV_EXPANDED_DOCUMENT:
          directionToUse = Navigate.PREV_DOCUMENT;
          break;
        case PREV_EXPANDED_SELECTED:
          directionToUse = Navigate.PREV_SELECTED;
          break;
        case PREV_EXPANDED_UNREAD:
          directionToUse = Navigate.PREV_UNREAD_ENTRY;
          break;
        default:
          JNAIDTable collapsedList = collectionAllocations.getCollapsedList();
          collapsedList.clear();
          collapsedList.addAll(resolvedCollapsedList);
          collapsedList.setInverted(resolvedCollapsedList.isInverted());
          
          updateFiltersFlagsVal |= NotesConstants.FILTER_COLLAPSED;
        }
      }

    }

    //return update flags to push the changed idtables to remote DBs
    updateFiltersFlags.setValue(updateFiltersFlagsVal);
    
    //return the direction that is used for the actual lookup
    return directionToUse;
  }

  private boolean isDirectionWithExpandCollapse(Navigate nav) {
    if (nav == Navigate.NEXT_EXPANDED || nav == Navigate.NEXT_EXPANDED_CATEGORY ||
        nav == Navigate.NEXT_EXPANDED_DOCUMENT ||
        nav == Navigate.NEXT_EXPANDED_SELECTED ||
        nav == Navigate.NEXT_EXPANDED_UNREAD) {
      return true;
    }
    else {
      return false;
    }
  }

  private Navigate toDownwardsDirection(Navigate nav) {
    switch (nav) {
    case PREV_CATEGORY:
      return Navigate.NEXT_CATEGORY;
    case PREV_DOCUMENT:
      return Navigate.NEXT_DOCUMENT;
    case PREV_ENTRY:
      return Navigate.NEXT_ENTRY;
    case PREV_EXPANDED:
      return Navigate.NEXT_EXPANDED;
    case PREV_EXPANDED_CATEGORY:
      return Navigate.NEXT_EXPANDED_CATEGORY;
    case PREV_EXPANDED_DOCUMENT:
      return Navigate.NEXT_EXPANDED_DOCUMENT;
    case PREV_EXPANDED_SELECTED:
      return Navigate.NEXT_EXPANDED_SELECTED;
    case PREV_EXPANDED_UNREAD:
      return Navigate.NEXT_EXPANDED_UNREAD;
    case PREV_ON_SAME_LEVEL:
      return Navigate.NEXT_ON_SAME_LEVEL;
    case PREV_ON_TOPLEVEL:
      return Navigate.NEXT_ON_TOPLEVEL;
    case PREV_PARENT_ENTRY:
      return Navigate.NEXT_PARENT_ENTRY;
    case PREV_SELECTED:
      return Navigate.NEXT_SELECTED;
    case PREV_SELECTED_ON_TOPLEVEL:
      return Navigate.NEXT_SELECTED_ON_TOPLEVEL;
    case PREV_UNREAD_ENTRY:
      return Navigate.NEXT_UNREAD_ENTRY;
    case PREV_UNREAD_TOPLEVEL_ENTRY:
      return Navigate.NEXT_UNREAD_TOPLEVEL_ENTRY;
    default:
      return nav;
    }
  }
  
  /**
   * Looks up a category {@link JNACollectionEntry}. By default we 
   * 
   * @param collection
   * @param categoryLevelsAsArr
   * @param readDescendantCount
   * @param readChildCount
   * @return
   */
  private Optional<JNACollectionEntry> findCategoryPosition(JNADominoCollection collection, Object[] categoryLevelsAsArr) {
    //find current position of category entry, atomically reads its note id and summary data
    NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(FindFlag.MATCH_CATEGORYORLEAF,
        FindFlag.REFRESH_FIRST, FindFlag.RETURN_DWORD, FindFlag.AND_READ_MATCHES, FindFlag.CASE_INSENSITIVE),
        EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), categoryLevelsAsArr);
    
    List<JNACollectionEntry> catEntries = catLkResult.getEntries();
    
    String categoryPosStr = catLkResult.getPosition();
    if (StringUtil.isEmpty(categoryPosStr) || catEntries.isEmpty()) {
      return Optional.empty();
    }
    
    JNACollectionEntry categoryEntry = catEntries.get(0);
    JNADominoCollectionPosition pos = new JNADominoCollectionPosition(categoryPosStr);
    categoryEntry.setPosition(pos.toTumblerArray());
    
    return Optional.of(categoryEntry);
  }
  
  /**
   * Makes sure that the navigation rule respects an applied selection
   * 
   * @param nav navigation
   * @return navigation direction respecting selection
   * @throws UnsupportedOperationException in case the given navigation direction cannot be combined with a selection
   */
  private Navigate addSelectionNavigation(Navigate nav) {
    switch (nav) {
    //these already respect the selection
    case NEXT_SELECTED:
    case NEXT_SELECTED_ON_TOPLEVEL:
    case NEXT_EXPANDED_SELECTED:
    case PREV_SELECTED:
    case PREV_SELECTED_ON_TOPLEVEL:
    case PREV_EXPANDED_SELECTED:
      return nav;

    //here it's easy to add selection navigation
    case NEXT_EXPANDED:
      return Navigate.NEXT_EXPANDED_SELECTED;
    case PREV_EXPANDED:
      return Navigate.PREV_EXPANDED_SELECTED;
    case NEXT_ON_TOPLEVEL:
      return Navigate.NEXT_SELECTED_ON_TOPLEVEL;
    case PREV_ON_TOPLEVEL:
      return Navigate.PREV_SELECTED_ON_TOPLEVEL;
    case NEXT_ENTRY:
    case NEXT_DOCUMENT:
      return Navigate.NEXT_SELECTED;
    case PREV_ENTRY:
    case PREV_DOCUMENT:
      return Navigate.PREV_SELECTED;

    //this one is probably ok to ignore the selection
    case CURRENT:
      return nav;

    case CHILD_ENTRY:
    case PARENT_ENTRY:
    case FIRST_ON_SAME_LEVEL:
    case LAST_ON_SAME_LEVEL:

    case NEXT_CATEGORY:
    case NEXT_EXPANDED_CATEGORY:
    case NEXT_EXPANDED_DOCUMENT:
    case NEXT_EXPANDED_UNREAD:
    case NEXT_ON_SAME_LEVEL:
    case NEXT_PARENT_ENTRY:
    case NEXT_UNREAD_ENTRY:
    case NEXT_UNREAD_TOPLEVEL_ENTRY:

    case PREV_CATEGORY:
    case PREV_EXPANDED_CATEGORY:
    case PREV_EXPANDED_DOCUMENT:
    case PREV_EXPANDED_UNREAD:
    case PREV_UNREAD_TOPLEVEL_ENTRY:
    case PREV_UNREAD_ENTRY:
    case PREV_PARENT_ENTRY:
    case PREV_ON_SAME_LEVEL:
    default:
      throw new UnsupportedOperationException(MessageFormat.format("Combining navigation direction {0} with selecting entries is currently unsupported by NIF", nav));
    }
  }

  /**
   * Makes sure that the navigation rule respects expanded entries
   * 
   * @param nav navigation
   * @return navigation direction respecting selection
   * @throws UnsupportedOperationException in case the given navigation direction cannot be combined with expanded entries
   */
  private Navigate addExpandNavigation(Navigate nav) {
    if (isDirectionWithExpandCollapse(nav)) {
      //nothing to do
      return nav;
    }
    
    switch (nav) {
    //these already respect expand states
    case NEXT_EXPANDED:
    case NEXT_EXPANDED_CATEGORY:
    case NEXT_EXPANDED_DOCUMENT:
    case NEXT_EXPANDED_SELECTED:
    case NEXT_EXPANDED_UNREAD:
    case PREV_EXPANDED:
    case PREV_EXPANDED_CATEGORY:
    case PREV_EXPANDED_DOCUMENT:
    case PREV_EXPANDED_SELECTED:
    case PREV_EXPANDED_UNREAD:
      return nav;
      
    //this one is probably ok to ignore the expand states
    case CURRENT:
      return nav;

    //apply expand rule to forward directions
    case NEXT_CATEGORY:
      return Navigate.NEXT_EXPANDED_CATEGORY;
    case NEXT_DOCUMENT:
      return Navigate.NEXT_EXPANDED_DOCUMENT;
    case NEXT_ENTRY:
      return Navigate.NEXT_EXPANDED;
    case NEXT_UNREAD_ENTRY:
      return Navigate.NEXT_EXPANDED_UNREAD;
    case NEXT_SELECTED:
      return Navigate.NEXT_EXPANDED_SELECTED;
      
    //apply expand rule to backward directions
    case PREV_CATEGORY:
      return Navigate.PREV_EXPANDED_CATEGORY;
    case PREV_DOCUMENT:
      return Navigate.PREV_EXPANDED_DOCUMENT;
    case PREV_ENTRY:
      return Navigate.PREV_EXPANDED;
    case PREV_UNREAD_ENTRY:
      return Navigate.PREV_EXPANDED_UNREAD;
    case PREV_SELECTED:
      return Navigate.PREV_EXPANDED_SELECTED;
        
    //cannot yet be combined with expanded states in NIF
    case PARENT_ENTRY:
    case CHILD_ENTRY:
    case FIRST_ON_SAME_LEVEL:
    case LAST_ON_SAME_LEVEL:
      
    case NEXT_ON_SAME_LEVEL:
    case NEXT_ON_TOPLEVEL:
    case NEXT_PARENT_ENTRY:
    case NEXT_SELECTED_ON_TOPLEVEL:
    case NEXT_UNREAD_TOPLEVEL_ENTRY:
      
    case PREV_ON_SAME_LEVEL:
    case PREV_ON_TOPLEVEL:
    case PREV_PARENT_ENTRY:
    case PREV_SELECTED_ON_TOPLEVEL:
    case PREV_UNREAD_TOPLEVEL_ENTRY:
    default:
      throw new UnsupportedOperationException(MessageFormat.format("Combining navigation direction {0} with expanded states is currently unsupported by NIF", nav));
    }
  }
  
  private boolean isDirectionWithSelection(Navigate nav) {
    if (nav == Navigate.NEXT_SELECTED || /* m_direction == Navigate.NEXT_SELECTED_HIT || */
        nav == Navigate.NEXT_SELECTED_ON_TOPLEVEL ||
        nav == Navigate.PREV_SELECTED || /* m_direction == Navigate.PREV_SELECTED_HIT || */
        nav == Navigate.PREV_SELECTED_ON_TOPLEVEL ||
        nav == Navigate.NEXT_EXPANDED_SELECTED ||
        nav == Navigate.PREV_EXPANDED_SELECTED
        ) {
      return true;
    }
    else {
      return false;
    }
  }

}
