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
package com.hcl.domino.jna.data;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.util.ListUtil;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery.ExpandedEntries;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.FTQueryResult;
import com.hcl.domino.data.Find;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.jna.data.JNADominoCollection.FindResult;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADominoCollectionAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.ptr.ShortByReference;

public class JNACollectionEntryIterator implements Iterator<CollectionEntry> {
  private CollectionTraversalContext m_defaultCtx;

  public JNACollectionEntryIterator(JNADominoCollection collection) {
    this.m_defaultCtx = new CollectionTraversalContext();
    m_defaultCtx.m_collection = collection;
  }
  
  public JNACollectionEntryIterator setMaxBufferEntries(int maxBufferEntries) {
    if (maxBufferEntries < 1) {
      throw new IllegalArgumentException(MessageFormat.format("Max buffer size must be 1 or higher: {0}", maxBufferEntries));
    }
    m_defaultCtx.m_maxBufferEntries = maxBufferEntries;
    return this;
  }
  
  public JNACollectionEntryIterator setSkip(int skip) {
    m_defaultCtx.m_skip = skip;
    return this;
  }
  
  public JNACollectionEntryIterator setLimit(int limit) {
    m_defaultCtx.m_limit = limit;
    return this;
  }
  
  public JNACollectionEntryIterator setTotalReceiver(Consumer<Integer> totalConsumer) {
    m_defaultCtx.m_totalConsumer = totalConsumer;
    return this;
  }
  
  public JNACollectionEntryIterator setCategoryReceiver(Consumer<CollectionEntry> categoryConsumer) {
    m_defaultCtx.m_categoryConsumer = categoryConsumer;
    return this;
  }
  
  public JNACollectionEntryIterator setDirection(Navigate direction) {
    m_defaultCtx.m_direction = direction;
    return this;
  }
  
  public JNACollectionEntryIterator setReadMask(Set<ReadMask> readMask) {
    m_defaultCtx.m_readMask = EnumSet.copyOf(readMask);
    return this;
  }
  
  public JNACollectionEntryIterator setRestrictToCategory(List<Object> categoryLevels) {
    m_defaultCtx.m_categoryLevelsAsArr = categoryLevels==null ? null : categoryLevels.toArray(new Object[categoryLevels.size()]);
    return this;
  }
  
  public JNACollectionEntryIterator setRestrictToLookupKey(List<Object> lookupKey, boolean exact) {
    m_defaultCtx.m_lookupKeysAsArr = lookupKey==null ? null : lookupKey.toArray(new Object[lookupKey.size()]);
    m_defaultCtx.m_lookupKeysExact = exact;
    return this;
  }
  
  public JNACollectionEntryIterator setNameOfSingleColumnToRead(String nameOfSingleColumnToRead) {
    m_defaultCtx.m_nameOfSingleColumnToRead = nameOfSingleColumnToRead;
    return this;
  }

  public JNACollectionEntryIterator setIndexOfSingleColumnToRead(int indexOfSingleColumnToRead) {
    m_defaultCtx.m_indexOfSingleColumnToRead = indexOfSingleColumnToRead;
    return this;
  }

  public JNACollectionEntryIterator setViewIndexChangedHandler(Function<DominoCollection, Action> handler) {
    m_defaultCtx.m_viewIndexChangedHandler = handler;
    return this;
  }
  
  public JNACollectionEntryIterator setStartAtFirstEntry() {
    m_defaultCtx.m_startAtLastEntry = false;
    m_defaultCtx.m_startAtPosition = null;
    m_defaultCtx.m_startAtEntryId = 0;
    return this;
  }

  public JNACollectionEntryIterator setStartAtLastEntry() {
    m_defaultCtx.m_startAtLastEntry = true;
    m_defaultCtx.m_startAtPosition = null;
    m_defaultCtx.m_startAtEntryId = 0;
    return this;
  }
  
  public JNACollectionEntryIterator setStartAtPosition(String pos) {
    m_defaultCtx.m_startAtPosition = pos;
    m_defaultCtx.m_startAtLastEntry = false;
    m_defaultCtx.m_startAtEntryId = 0;
    return this;
  }

  public JNACollectionEntryIterator setStartAtEntryId(int noteId) {
    m_defaultCtx.m_startAtEntryId = noteId;
    m_defaultCtx.m_startAtLastEntry = false;
    m_defaultCtx.m_startAtPosition = null;
    return this;
  }

  public JNACollectionEntryIterator setRestrictionToFTResults(String ftQuery, int ftMaxDocs, Set<FTQuery> ftFlags) {
    m_defaultCtx.m_ftQuery = ftQuery;
    m_defaultCtx.m_ftMaxDocs = ftMaxDocs;
    m_defaultCtx.m_ftFlags = ftFlags;
    return this;
  }

  private static boolean isUpwardDirection(Navigate nav) {
    return !isDownwardDirection(nav);
  }
  
  private static boolean isDownwardDirection(Navigate nav) {
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
    m_defaultCtx.m_selectedEntries = selectedEntries;
    m_defaultCtx.m_selectedEntriesResolved = idTable;
    m_defaultCtx.m_hasSelectionSet = true;
    
    if (!isDirectionWithSelection(m_defaultCtx.m_direction)) {
      //automatically select a traversal strategy that makes use of
      //selection / expanded info
      
      if (isDownwardDirection(m_defaultCtx.m_direction)) {
        if (isDirectionWithExpandCollapse(m_defaultCtx.m_direction)) {
          m_defaultCtx.m_direction = Navigate.NEXT_EXPANDED_SELECTED;
        }
        else {
          m_defaultCtx.m_direction = Navigate.NEXT_SELECTED;
        }
      }
      else {
        if (isDirectionWithExpandCollapse(m_defaultCtx.m_direction)) {
          m_defaultCtx.m_direction = Navigate.PREV_EXPANDED_SELECTED;
        }
        else {
          m_defaultCtx.m_direction = Navigate.PREV_SELECTED;
        }
      }
    }
    return this;
  }

  public JNACollectionEntryIterator expand(ExpandedEntries expandedEntries, JNAIDTable idTable) {
    m_defaultCtx.m_expandedEntries = expandedEntries;
    m_defaultCtx.m_expandedEntriesResolved = idTable;
    m_defaultCtx.m_hasExpandedEntries = true;
    
    if (!isDirectionWithExpandCollapse(m_defaultCtx.m_direction)) {
      //automatically select a traversal strategy that makes use of
      //selection / expanded info
      
      if (isDownwardDirection(m_defaultCtx.m_direction)) {
        if (isDirectionWithSelection(m_defaultCtx.m_direction)) {
          m_defaultCtx.m_direction = Navigate.NEXT_EXPANDED_SELECTED;
        }
        else {
          m_defaultCtx.m_direction = Navigate.NEXT_EXPANDED;
        }
      }
      else {
        if (isDirectionWithSelection(m_defaultCtx.m_direction)) {
          m_defaultCtx.m_direction = Navigate.PREV_EXPANDED_SELECTED;
        }
        else {
          m_defaultCtx.m_direction = Navigate.PREV_EXPANDED;
        }
      }
    }
    return this;
  }

  private static boolean hasNext(CollectionTraversalContext ctx) {
    init(ctx);
    
    if (ctx.m_limit==0 && !ctx.m_totalReported && ctx.m_totalConsumer!=null) {
      //handle special case where we just want to get the total, but don't want to read any entries (limit=0)
      int total = getTotal(ctx);
      ctx.m_totalReported = true;
      ctx.m_totalConsumer.accept(total);
      return false;
    }

    if (ctx.m_entriesReturned>=ctx.m_limit) {
      return false;
    }
    else if (ctx.m_nextPage==null) {
      //first call
      fetchNextPage(ctx);
    }
    else if (ctx.m_nextPage.isEmpty()) {
      //we need to fetch more entries
      if (ctx.m_isDone) {
        //but we have reached the end of the view
        return false;
      }
      else {
        fetchNextPage(ctx);
      }
    }
    
    return !ctx.m_nextPage.isEmpty();
  }

  @Override
  public boolean hasNext() {
    return hasNext(m_defaultCtx);
  }

  private static CollectionEntry next(CollectionTraversalContext ctx) {
    if (!hasNext(ctx)) {
      throw new NoSuchElementException();
    }
    
    CollectionEntry entry = ctx.m_nextPage.removeFirst();
    ctx.m_entriesReturned++;
    return entry;
  }
  
  @Override
  public CollectionEntry next() {
    return next(m_defaultCtx);
  }

  private static void markNoData(CollectionTraversalContext ctx) {
    ctx.m_currPos = null;
    ctx.m_nextPage = new LinkedList<>();
    ctx.m_isDone = true;
    
    if (ctx.m_totalConsumer!=null && !ctx.m_totalReported) {
      ctx.m_totalReported = true;
      ctx.m_totalConsumer.accept(0);
    }
  }
  
  private static int getTotal(CollectionTraversalContext ctx) {
    if (ctx.m_total==null) {
      init(ctx);
      
      if (ctx.m_total==null) { // init() might set total
        JNADominoCollectionPosition tmpPos = (JNADominoCollectionPosition) ctx.m_currPosForTotalComputation.clone();
        
        NotesViewLookupResultData skipAllLkResult =
            ctx.m_collection.readEntriesExt(tmpPos,
                ctx.m_skipNav, true,
            Integer.MAX_VALUE, Navigate.CURRENT,
            0, ctx.m_readMask, (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) ctx.m_indexOfSingleColumnToRead);
        
        ctx.m_total = skipAllLkResult.getSkipCount();
        if (ctx.m_initialSkip == 0) {
          //add the first position to the count
          ctx.m_total++;
        }
      }
    }
    return ctx.m_total;
  }
  
  private static void init(CollectionTraversalContext ctx) {
    if (ctx.m_initialized) {
      return;
    }
    
    ctx.m_initialized = true;
    ctx.m_firstRun = true;
    
    JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) ctx.m_collection.getAdapter(APIObjectAllocations.class);
    
    ShortByReference updateFiltersFlags = new ShortByReference();
    //init selected / expanded IDTables and returns the direction to read entries
    Navigate directionToUse = prepareCollectionReadRestrictions(ctx, collectionAllocations, updateFiltersFlags);
    
    ctx.m_skipNav = directionToUse;
    ctx.m_returnNav = directionToUse;
    
    if (ctx.m_skipNav == Navigate.FIRST_ON_SAME_LEVEL) {
      //just return first element
      ctx.m_returnNav = Navigate.CURRENT;
    }
    else if (ctx.m_skipNav == Navigate.LAST_ON_SAME_LEVEL) {
      //just return last element
      ctx.m_returnNav = Navigate.CURRENT;
    }
    
    if (ctx.m_returnNav == Navigate.CURRENT && ctx.m_limit>1) {
      //prevent reading too many entries if navigation is set to just read the current entry
      ctx.m_limit = 1;
    }
    
    if (ctx.m_limit < 0) {
      ctx.m_limit = Integer.MAX_VALUE;
    }
    
    if (ctx.m_indexOfSingleColumnToRead==null && ctx.m_nameOfSingleColumnToRead!=null) {
      ctx.m_indexOfSingleColumnToRead = ctx.m_collection.getColumnValuesIndex(ctx.m_nameOfSingleColumnToRead);
    }

    final short updateFiltersFlagsVal = updateFiltersFlags.getValue();
    if (updateFiltersFlagsVal != 0) {
      LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
        if (!ctx.m_collection.isDisposed()) {
          
          //the method prepareCollectionReadRestrictions has modified the selected list; for remote databases, push IDTable changes via NRPC

          short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, updateFiltersFlagsVal);
          NotesErrorUtils.checkResult(result);
        }
        
        return null;
      });
    }
    
    if (ctx.m_lookupKeysAsArr!=null) {
      ctx.m_readMask.add(ReadMask.NOTEID);
      
      Set<Find> findEqualFlags = EnumSet.of(Find.EQUAL);
      if (!ctx.m_lookupKeysExact) {
        findEqualFlags.add(Find.PARTIAL);
      }
      
      ctx.m_lookupKeyResolvedMatchingIds = CollectionEntries.getAllIdsByKey(ctx.m_collection, ctx.m_indexOfSingleColumnToRead,
          findEqualFlags, ctx.m_lookupKeysAsArr);

//      if (ctx.m_readMask.size()==1 && ctx.m_readMask.contains(ReadMask.NOTEID) && ctx.m_categoryLevelsAsArr==null) {
//        
//        if (ctx.m_nextPage==null) {
//          ctx.m_nextPage = new LinkedList<>();
//        }
//        ctx.m_lookupKeyResolvedMatchingIds
//            .stream()
//            .map((id) -> {
//              JNACollectionEntry newEntry = new JNACollectionEntry(ctx.m_collection);
//              newEntry.setNoteID(id);
//              return newEntry;
//            })
//            .forEach(ctx.m_nextPage::add);
//        
//        if (ctx.m_skip!=0 || ctx.m_limit!=Integer.MAX_VALUE) {
//          ctx.m_nextPage = new LinkedList<>(ListUtil.subListChecked(ctx.m_nextPage, ctx.m_skip, ctx.m_limit));
//        }
//        ctx.m_isDone = true;
//        return;
//      }
      
      if (ctx.m_lookupKeyResolvedMatchingIds.isEmpty()) {
        markNoData(ctx);
        return;
      }
      
      Set<Find> findFirstFlags = EnumSet.of(Find.FIRST_EQUAL);
      if (!ctx.m_lookupKeysExact) {
        findFirstFlags.add(Find.PARTIAL);
      }
      
      FindResult findFirstResult = ctx.m_collection.findByKey(findFirstFlags, ctx.m_lookupKeysAsArr);
      
      if (StringUtil.isEmpty(findFirstResult.getPosition())) {
        //key not found
        ctx.m_lookupKeyResolvedMatchingIds = new LinkedHashSet<>();
        markNoData(ctx);
        return;
      }

      ctx.m_lookupKeyResolvedStartPos = new JNADominoCollectionPosition(findFirstResult.getPosition());

      Set<Find> findLastFlags = EnumSet.of(Find.LAST_EQUAL);
      if (!ctx.m_lookupKeysExact) {
        findLastFlags.add(Find.PARTIAL);
      }
      
      FindResult findLastResult = ctx.m_collection.findByKey(findLastFlags, ctx.m_lookupKeysAsArr);
      
      if (StringUtil.isEmpty(findLastResult.getPosition())) {
        //key not found
        ctx.m_lookupKeyResolvedMatchingIds = new LinkedHashSet<>();
        markNoData(ctx);
        return;
      }

      ctx.m_lookupKeyResolvedEndPos = new JNADominoCollectionPosition(findLastResult.getPosition());

      //don't plan to buffer and return more entries than the number of note ids we found
      ctx.m_limit = Math.min(ctx.m_limit, ctx.m_lookupKeyResolvedMatchingIds.size());
      ctx.m_total = ctx.m_lookupKeyResolvedMatchingIds.size();
      
      if (ctx.m_startAtEntryId!=0) {
        //find position of start note id:
        ctx.m_startAtPosition = ctx.m_collection.locateNote(ctx.m_lookupKeyResolvedStartPos.toString(true), ctx.m_startAtEntryId);
        if (StringUtil.isEmpty(ctx.m_startAtPosition)) {
          //entry not found
          markNoData(ctx);
          return;
        }
        ctx.m_startAtLastEntry = false;
        ctx.m_startAtEntryId = 0;
        //found position will be compared to our key matches block in the following if block
      }
      
      if (ctx.m_startAtLastEntry) {
        ctx.m_startAtLastEntry = false;
        ctx.m_startAtPosition = ctx.m_lookupKeyResolvedEndPos.toString(true);
        ctx.m_startAtEntryId = 0;
      }
      else if (!StringUtil.isEmpty(ctx.m_startAtPosition)) {
        //check if we should correct the specified start position to start at the key matches block (to not skip too many irrelevant rows)
        JNADominoCollectionPosition startAtPos = new JNADominoCollectionPosition(ctx.m_startAtPosition);
        JNADominoCollectionPosition keyStartPos = new JNADominoCollectionPosition(ctx.m_lookupKeyResolvedStartPos);
        if (startAtPos.compareTo(keyStartPos)<0) {
          //move start pos to start of key matches
          ctx.m_startAtPosition = ctx.m_lookupKeyResolvedStartPos.toString(true);
        }

        JNADominoCollectionPosition keyEndPos = new JNADominoCollectionPosition(ctx.m_lookupKeyResolvedEndPos);
        if (startAtPos.compareTo(keyEndPos)<0) {
          //we should start reading after the key matches block, so no data to read
          markNoData(ctx);
          return;
        }
      }
      else {
        ctx.m_startAtPosition = ctx.m_lookupKeyResolvedStartPos.toString(true);
        ctx.m_startAtLastEntry = false;
        ctx.m_startAtEntryId = 0;
      }

      
      
      
      //TODO add shortcut code if we should only do a key lookup and return NOTEID only
      
      ctx.m_readMask.add(ReadMask.INDEXPOSITION);
    }

    //compute where to start reading
    
    if (ctx.m_categoryLevelsAsArr!=null) {
      //we should only read a subset of the view
      
      //add INDEXPOSITION to be able to check if we're still below the category
      ctx.m_readMask.add(ReadMask.INDEXPOSITION);

      //find category entry position

      JNACollectionEntry categoryEntry = findCategoryPosition(ctx.m_collection, ctx.m_categoryLevelsAsArr).orElse(null);
      
      if (categoryEntry==null) {
        //category not found or gone
        markNoData(ctx);
        return;
      }

      if (ctx.m_categoryConsumer!=null) {
        ctx.m_categoryConsumer.accept(categoryEntry);
      }
      
      String categoryPosStr = getIndexPositionAsString(categoryEntry);
      ctx.m_categoryEntryPos = new JNADominoCollectionPosition(categoryPosStr);
      ctx.m_minLevel = ctx.m_categoryEntryPos.getLevel()+1;

      if (ctx.m_startAtPosition!=null) {
        //check if start position is within our subset
        
        if (!ctx.m_startAtPosition.startsWith(categoryPosStr+".")) { //$NON-NLS-1$
          //start position is out of scope
          markNoData(ctx);
          return;
        }
        
        ctx.m_currPos = new JNADominoCollectionPosition(ctx.m_startAtPosition);
      }
      else if (ctx.m_startAtEntryId!=0) {
        String entryPos = ctx.m_collection.locateNote(categoryPosStr, ctx.m_startAtEntryId);
        if (StringUtil.isEmpty(entryPos) || // note id not found
            !entryPos.startsWith(categoryPosStr+".")) { // or not in the category //$NON-NLS-1$
          //entry not found
          markNoData(ctx);
          return;
        }
        
        ctx.m_currPos = new JNADominoCollectionPosition(entryPos);
      }
      else if (ctx.m_startAtLastEntry) {
        //skip over the whole view subtree to find the last entry based on the selection / expanded entries
        JNADominoCollectionPosition lastEntrySearchStartPos = new JNADominoCollectionPosition(categoryPosStr);
        lastEntrySearchStartPos.setMinLevel(ctx.m_minLevel);
        
        //make sure we navigate downwards
        Navigate skipNavDownwards = toDownwardsDirection(ctx.m_skipNav);
        
        NotesViewLookupResultData lastEntryLkResult =
            ctx.m_collection.readEntriesExt(lastEntrySearchStartPos,
            skipNavDownwards, true,
            Integer.MAX_VALUE, Navigate.CURRENT,
            1, EnumSet.of(ReadMask.INDEXPOSITION), (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) ctx.m_indexOfSingleColumnToRead);
        
        if (isUpwardDirection(ctx.m_skipNav)) {
          //if our direction is upwards, we already counted the total entries to traverse
          ctx.m_total = lastEntryLkResult.getSkipCount();
        }
        
        String lastEntryPosStr = ""; //$NON-NLS-1$
        
        if (!lastEntryLkResult.getEntries().isEmpty()) {
          lastEntryPosStr = getIndexPositionAsString(
              lastEntryLkResult
              .getEntries()
              .get(0));
        }
        
        if (StringUtil.isEmpty(lastEntryPosStr)) {
          //view is empty
          markNoData(ctx);
          return;
        }
        else if (!lastEntryPosStr.startsWith(categoryPosStr+".")) { //$NON-NLS-1$
          //somehow our found last position is out of scope
          markNoData(ctx);
          return;
        }
        else {
          ctx.m_currPos = new JNADominoCollectionPosition(lastEntryPosStr);
        }
      }
      else {
        ctx.m_currPos = new JNADominoCollectionPosition(categoryPosStr+".0"); //$NON-NLS-1$
        ctx.m_initialSkip = 1;
      }
      
      ctx.m_currPos.setMinLevel(ctx.m_minLevel);
      ctx.m_currPosForTotalComputation = (JNADominoCollectionPosition) ctx.m_currPos.clone();
    }
    else {
      //no top level category
      
      ctx.m_categoryEntryPos = null;
      ctx.m_minLevel = 0;
      
      if (ctx.m_startAtLastEntry) {
      //skip over the whole view subtree to find the last entry based on the selection / expanded entries
        JNADominoCollectionPosition lastEntrySearchStartPos = new JNADominoCollectionPosition("0"); //$NON-NLS-1$
        
        //make sure we navigate downwards
        Navigate skipNavDownwards = toDownwardsDirection(ctx.m_skipNav);

        String lastEntryPosStr = ""; //$NON-NLS-1$

        JNADominoCollectionPosition lastEntrySearchStartPosCopy = (JNADominoCollectionPosition) lastEntrySearchStartPos.clone();
        
        NotesViewLookupResultData lastEntryLkResult =
            ctx.m_collection.readEntriesExt(lastEntrySearchStartPosCopy,
            skipNavDownwards, true,
            Integer.MAX_VALUE, skipNavDownwards, // don't use Navigate.CURRENT here, returns no data for fulltext search results
            1, EnumSet.of(ReadMask.INDEXPOSITION), (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) ctx.m_indexOfSingleColumnToRead);
        
        if (isUpwardDirection(ctx.m_skipNav)) {
          //if our direction is upwards, we already counted the total entries to traverse
          ctx.m_total = lastEntryLkResult.getSkipCount();
        }

        if (!lastEntryLkResult.getEntries().isEmpty()) {
          lastEntryPosStr = getIndexPositionAsString(lastEntryLkResult
              .getEntries()
              .get(0));
        }
        
        if (StringUtil.isEmpty(lastEntryPosStr)) {
          //view is empty
          markNoData(ctx);
          return;
        }
        else {
          ctx.m_currPos = new JNADominoCollectionPosition(lastEntryPosStr);
        }
      }
      else if (ctx.m_startAtEntryId!=0) {
        ctx.m_readMask.add(ReadMask.INIT_POS_NOTEID);
        ctx.m_currPos = new JNADominoCollectionPosition(Integer.toString(ctx.m_startAtEntryId));
      }
      else if (ctx.m_startAtPosition!=null) {
        ctx.m_currPos = new JNADominoCollectionPosition(ctx.m_startAtPosition);
      }
      else {
        ctx.m_currPos = new JNADominoCollectionPosition("0"); //$NON-NLS-1$
        ctx.m_initialSkip = 1;
      }
      
      ctx.m_currPosForTotalComputation = (JNADominoCollectionPosition) ctx.m_currPos.clone();
    }
    
    
    ctx.m_maxBufferEntries = Math.min(ctx.m_maxBufferEntries, ctx.m_limit);
  }
  
  private static String getIndexPositionAsString(CollectionEntry entry)  {
    return entry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
  }

  private static int[] getIndexPositionAsArray(CollectionEntry entry)  {
    return entry.getSpecialValue(SpecialValue.INDEXPOSITION, int[].class, null); //$NON-NLS-1$
  }

  private static class CollectionTraversalContext {
    private boolean m_initialized;
    
    private JNADominoCollection m_collection;
    private Set<ReadMask> m_readMask = EnumSet.of(ReadMask.NOTEID);
    
    private int m_maxBufferEntries = Integer.MAX_VALUE;
    private Consumer<Integer> m_totalConsumer;
    private boolean m_totalReported;
    private Consumer<CollectionEntry> m_categoryConsumer;

    
    private JNADominoCollectionPosition m_categoryEntryPos;
    private boolean m_firstRun;
    private int m_initialSkip;
    private Integer m_total;
    private Navigate m_skipNav;
    private Navigate m_returnNav;
    private JNADominoCollectionPosition m_currPosForTotalComputation;
    private JNADominoCollectionPosition m_currPos;
    private int m_minLevel;
    private int m_entriesReturned;
    
    private LinkedList<CollectionEntry> m_nextPage;
    private boolean m_isDone;

    private Navigate m_direction = Navigate.NEXT_ENTRY;
    
    private int m_skip;
    private int m_limit = -1;
    
    private Object[] m_categoryLevelsAsArr;
    
    private Object[] m_lookupKeysAsArr;
    private boolean m_lookupKeysExact;
    
    private boolean m_startAtLastEntry;
    private String m_startAtPosition;
    private int m_startAtEntryId;

    private String m_nameOfSingleColumnToRead;
    private Integer m_indexOfSingleColumnToRead;

    private Function<DominoCollection, Action> m_viewIndexChangedHandler;

    //IDTables to control what is expanded/selected
    private ExpandedEntries m_expandedEntries;
    private JNAIDTable m_expandedEntriesResolved;
    private boolean m_hasExpandedEntries;
    
    private SelectedEntries m_selectedEntries;
    private JNAIDTable m_selectedEntriesResolved;
    private boolean m_hasSelectionSet;
    private String m_ftQuery;
    private int m_ftMaxDocs;
    private Set<FTQuery> m_ftFlags;
    
    private JNADominoCollectionPosition m_lookupKeyResolvedStartPos;
    private JNADominoCollectionPosition m_lookupKeyResolvedEndPos;
    private LinkedHashSet<Integer> m_lookupKeyResolvedMatchingIds;

    public CollectionTraversalContext() {
    }

  }

  private static void fetchNextPage(CollectionTraversalContext ctx) {
    init(ctx);
    
    if (ctx.m_isDone || ctx.m_currPos==null) {
      return;
    }
    
    boolean wasFirstRun = ctx.m_firstRun;
    ctx.m_firstRun = false;
    
    if (ctx.m_nextPage==null) {
      ctx.m_nextPage = new LinkedList<>();
    }
    
    if (wasFirstRun && !ctx.m_totalReported && ctx.m_totalConsumer!=null) {
      int total = getTotal(ctx);
      ctx.m_totalReported = true;
      ctx.m_totalConsumer.accept(total);
    }
 
    JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) ctx.m_collection.getAdapter(APIObjectAllocations.class);

    LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {

      if (ctx.m_categoryLevelsAsArr!=null) {
        //normally the following code should run in one loop;
        //we only rerun the lookup if an index change is detected AND the top category has moved,
        //because in this case we cannot be sure that the data we read are valid
        while (true) {
          //save a copy of the current position in case we detect index changes
          JNADominoCollectionPosition currPosCopy = (JNADominoCollectionPosition) ctx.m_currPos.clone();
          
          NotesViewLookupResultData lkResult =
              ctx.m_collection.readEntriesExt(ctx.m_currPos,
                  ctx.m_skipNav,
                  false,
                  wasFirstRun ? (ctx.m_initialSkip + ctx.m_skip) : 1,
                      ctx.m_returnNav,
                      ctx.m_maxBufferEntries,
                      ctx.m_readMask,
                  (DominoDateTime) null,
                  (JNAIDTable) null,
                  ctx.m_indexOfSingleColumnToRead);
          
          if (lkResult.hasAnyNonDataConflicts()) {
            //view index has changed, update the view
            if (!lkResult.isViewTimeRelative()) {
              if (ctx.m_viewIndexChangedHandler!=null) {
                Action action = ctx.m_viewIndexChangedHandler.apply(ctx.m_collection);
                if (action==Action.Stop) {
                  ctx.m_isDone = true;
                  return null;
                }
              }
              ctx.m_collection.refresh();
            }

            //check if our category position is still correct; if yes, we can use the lookup result
            JNACollectionEntry newCategoryEntry = findCategoryPosition(ctx.m_collection, ctx.m_categoryLevelsAsArr).orElse(null);
            
            if (newCategoryEntry==null) {
              //category disappeared, return what we have read; since we cannot be sure that "lkResult" contains
              //data of the right category, we throw it away
              markNoData(ctx);
              return null;
            }
            
            String newCategoryPosStr = getIndexPositionAsString(newCategoryEntry);
            
            if (StringUtil.isEmpty(newCategoryPosStr)) {
              //category disappeared, return what we have read; since we cannot be sure that "lkResult" contains
              //data of the right category, we throw it away
              markNoData(ctx);
              return null;
            }
            
            JNADominoCollectionPosition newCategoryPos = new JNADominoCollectionPosition(newCategoryPosStr);

            if (!newCategoryPos.equals(ctx.m_categoryEntryPos)) {
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
              ctx.m_currPos = new JNADominoCollectionPosition(newTransposedCurrTumbler);
              ctx.m_currPos.setMinLevel(ctx.m_minLevel);
              //remember our new top category position
              ctx.m_categoryEntryPos = newCategoryPos;
              
              continue;
            }
            
          }
          
          List<JNACollectionEntry> entries = lkResult.getEntries();
          if (entries.isEmpty()) {
            //no data received
            ctx.m_isDone = true;
          }
          else {
            if (entries.size() < ctx.m_maxBufferEntries && !lkResult.hasMoreToDo()) {
              //less data received than requested and there's not more in the view
              ctx.m_isDone = true;
            }
            
            for (JNACollectionEntry currEntry : entries) {
              int[] currEntryPosArr = getIndexPositionAsArray(currEntry);
              
              if (currEntryPosArr==null) {
                throw new IllegalStateException(
                    MessageFormat.format("Expected INDEXPOSITION attribute is missing in returned collection entry: {0}",
                        currEntry));
              }
              JNADominoCollectionPosition currEntryPos = new JNADominoCollectionPosition(currEntryPosArr);
              
              //make sure we are in the right category
              if (currEntryPos.isDescendantOf(ctx.m_categoryEntryPos)) {

                if (ctx.m_lookupKeyResolvedMatchingIds!=null) {
                  boolean isAccepted = true;
                  
                  int currNoteId = currEntry.getNoteID();
                  if (currNoteId==0 || !ctx.m_lookupKeyResolvedMatchingIds.contains(currNoteId)) {
                    //not in lookup key matches IDTable, ignore this entry and go on with the next
                    isAccepted = false;
                  }
                  
                  if (isAccepted) {
                    ctx.m_nextPage.add(currEntry);
                  }
                  
                  if (isDownwardDirection(ctx.m_skipNav)) {
                    //check if we have left the key matches block
                    if (currEntryPos.compareTo(ctx.m_lookupKeyResolvedEndPos) >=0) {
                      ctx.m_isDone = true;
                      break;
                    }
                  }
                  else {
                    if (currEntryPos.compareTo(ctx.m_lookupKeyResolvedStartPos) <=0) {
                      ctx.m_isDone = true;
                      break;
                    }
                  }
                  
                }
                else {
                  ctx.m_nextPage.add(currEntry);
                }

              }
            }
          }
          
          break;
        }
      }
      else {
        //no top category
        System.out.println("pre-readEntriesExt: currPos="+ctx.m_currPos+", skipNav="+ctx.m_skipNav+", returnNav="+ctx.m_returnNav);
        NotesViewLookupResultData lkResult = ctx.m_collection.readEntriesExt(ctx.m_currPos,
            ctx.m_skipNav,
                  false,
                  wasFirstRun ? (ctx.m_initialSkip + ctx.m_skip) : 1,
                      ctx.m_returnNav,
                      ctx.m_maxBufferEntries,
                      ctx.m_readMask,
                  (DominoDateTime) null,
                (JNAIDTable) null,
                ctx.m_indexOfSingleColumnToRead);
        
        if (lkResult.hasAnyNonDataConflicts()) {
          if (!lkResult.isViewTimeRelative()) {
            if (ctx.m_viewIndexChangedHandler!=null) {
              Action action = ctx.m_viewIndexChangedHandler.apply(ctx.m_collection);
              if (action==Action.Stop) {
                ctx.m_isDone = true;
                return null;
              }
            }
          }
        }
        
        List<JNACollectionEntry> entries = lkResult.getEntries();
        System.out.println("post-readEntriesExt: currPos="+ctx.m_currPos+", entries.size="+entries.size());

        if (entries.isEmpty()) {
          ctx.m_isDone = true;
        }
        else {
          if (entries.size() < ctx.m_maxBufferEntries && !lkResult.hasMoreToDo()) {
            //less data received than requested and there's not more in the view
            ctx.m_isDone = true;
          }

          for (JNACollectionEntry currEntry : entries) {
            if (ctx.m_lookupKeyResolvedMatchingIds!=null) {
              boolean isAccepted = true;
              
              int currNoteId = currEntry.getNoteID();
              if (currNoteId==0 || !ctx.m_lookupKeyResolvedMatchingIds.contains(currNoteId)) {
                //not in lookup key matches IDTable, ignore this entry and go on with the next
                isAccepted = false;
              }
              
              if (isAccepted) {
                ctx.m_nextPage.add(currEntry);
              }
              
              int[] currEntryPosArr = getIndexPositionAsArray(currEntry);
              if (currEntryPosArr!=null) {
                JNADominoCollectionPosition currEntryPos = new JNADominoCollectionPosition(currEntryPosArr);
                
                if (isDownwardDirection(ctx.m_skipNav)) {
                  //check if we have left the key matches block
                  if (currEntryPos.compareTo(ctx.m_lookupKeyResolvedEndPos)>=0) {
                    ctx.m_isDone = true;
                    break;
                  }
                }
                else {
                  if (currEntryPos.compareTo(ctx.m_lookupKeyResolvedStartPos)<=0) {
                    ctx.m_isDone = true;
                    break;
                  }
                }
                
              }
              
            }
            else {
              ctx.m_nextPage.add(currEntry);
            }
            
          }
        }
        
        if (ctx.m_readMask.contains(ReadMask.INIT_POS_NOTEID)) {
          //make sure to only use this flag on the first lookup call
          ctx.m_readMask.remove(ReadMask.INIT_POS_NOTEID);
        }
      }
      
      return null;
    });
  
  }
  
  /**
   * 
   * @param ctx traversal context
   * @param collectionAllocations
   * @param updateFiltersFlags
   * @return navigate direction for the collection lookup (using m_direction
   */
  private static Navigate prepareCollectionReadRestrictions(CollectionTraversalContext ctx, JNADominoCollectionAllocations collectionAllocations,
      ShortByReference updateFiltersFlags) {
    
    //clear any active FT search mode
    ctx.m_collection.clearSearch();

    short updateFiltersFlagsVal = updateFiltersFlags.getValue();
    
    Navigate directionToUse = ctx.m_direction;
    if (directionToUse==null) {
      //read all entries by default
      directionToUse = Navigate.NEXT_ENTRY;
    }

    if (ctx.m_hasSelectionSet) {
      //make sure that the navigation direction respects the selection; noop if already the case
      directionToUse = addSelectionNavigation(directionToUse);
    }

    if (ctx.m_hasExpandedEntries) {
      //make sure that the navigation direction respects expanded entries; noop if already the case
      directionToUse = addExpandNavigation(directionToUse);
    }

    if (!StringUtil.isEmpty(ctx.m_ftQuery)) {
      //view index is reduced to just return the FT search matches
      directionToUse = addFTSearchNavigation(directionToUse);
    }
    
    if (isDirectionWithSelection(directionToUse)) {
      //resolve and set the selected entries idtable
      JNAIDTable resolvedSelectedList = ctx.m_selectedEntriesResolved!=null ? (JNAIDTable) ctx.m_selectedEntriesResolved.clone() : new JNAIDTable(ctx.m_collection.getParentDominoClient());
      
      if (!StringUtil.isEmpty(ctx.m_ftQuery)) {
        if (ctx.m_categoryLevelsAsArr!=null) {
          //FT search should be used to reduce/reorder the view index to FT matches (e.g. sort by relevance);
          //only return documents in a top level category
          Set<Integer> allNoteIdsInCategoryFilter = getAllDocNoteIdsInCategory(ctx.m_collection, ctx.m_categoryLevelsAsArr);
          
          //restrict the selected list to the note ids in the category
          resolvedSelectedList.retainAll(allNoteIdsInCategoryFilter);
        }
        
        //pass cloned selected list to refine FT search; not the one that we might fill up with fake note ids below
        FTQueryResult ftResult = ctx.m_collection.ftSearch(ctx.m_ftQuery, ctx.m_ftMaxDocs, ctx.m_ftFlags,
            (JNAIDTable) resolvedSelectedList.clone());
        System.out.println(ftResult);
      }

      long t0_hier=System.currentTimeMillis();
      boolean isHierarchical = ctx.m_collection.isHierarchical();
      long t1_hier=System.currentTimeMillis();
      System.out.println("isHierarchical check took "+(t1_hier-t0_hier)+"ms");
      
      if (isHierarchical) {
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
          long t0_allids = System.currentTimeMillis();
          IDTable allIDsInView = ctx.m_collection.getAllIdsAsIDTable(false);
          long t1_allids = System.currentTimeMillis();
          System.out.println("Reading all view ids took "+(t1_allids-t0_allids)+"ms");
          
          long t0_addids = System.currentTimeMillis();
          int fakeNoteIdsToInsert = 5000 - resolvedSelectedListSize;
          int maxFakeNoteId = 2147483644;

          for (int i=0; i<fakeNoteIdsToInsert; i++) {
            int currNoteId = maxFakeNoteId - i*4;

            // make sure the ID we add does not exist in the view
            if (!allIDsInView.contains(currNoteId)) {
              resolvedSelectedList.add(currNoteId);
            }
          }
          long t1_addids = System.currentTimeMillis();
          System.out.println("Adding "+(t1_addids-t0_addids)+"ms");
        }
      }
      
      JNAIDTable selectedList = collectionAllocations.getSelectedList();
      selectedList.clear();
      selectedList.addAll(resolvedSelectedList);
      selectedList.setInverted(resolvedSelectedList.isInverted());
      
      updateFiltersFlagsVal |= NotesConstants.FILTER_SELECTED;
    }
    else {
      if (!StringUtil.isEmpty(ctx.m_ftQuery)) {
        //view index is reduced to just return the FT search matches;#
        Set<Integer> allNoteIdsInCategoryFilter = null;
        
        if (ctx.m_categoryLevelsAsArr!=null) {
          //FT search should only return documents in a top level category
          allNoteIdsInCategoryFilter = getAllDocNoteIdsInCategory(ctx.m_collection, ctx.m_categoryLevelsAsArr);
        }
        
        //pass cloned selected list to refine FT search; not the one that we might fill up with fake note ids below
        FTQueryResult ftResult = ctx.m_collection.ftSearch(ctx.m_ftQuery, ctx.m_ftMaxDocs, ctx.m_ftFlags, allNoteIdsInCategoryFilter);
        System.out.println(ftResult);
      }
    }

    if (isDirectionWithExpandCollapse(directionToUse)) {
      //resolve and set the expanded entries idtable
      JNAIDTable resolvedCollapsedList = ctx.m_expandedEntriesResolved!=null ? (JNAIDTable) ctx.m_expandedEntriesResolved.clone() : new JNAIDTable(ctx.m_collection.getParentDominoClient());
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

  private static boolean isDirectionWithExpandCollapse(Navigate nav) {
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

  private static Navigate toDownwardsDirection(Navigate nav) {
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
    case PREV_HIT:
      return Navigate.NEXT_HIT;
    case PREV_SELECTED_HIT:
      return Navigate.NEXT_SELECTED_HIT;
    case PREV_UNREAD_HIT:
      return Navigate.NEXT_UNREAD_HIT;
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
  private static Optional<JNACollectionEntry> findCategoryPosition(JNADominoCollection collection, Object[] categoryLevelsAsArr) {
    //find current position of category entry, atomically reads its note id and summary data
    NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(Find.MATCH_CATEGORYORLEAF,
        Find.REFRESH_FIRST, Find.CASE_INSENSITIVE),
        EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), categoryLevelsAsArr);
        
    List<JNACollectionEntry> catEntries = catLkResult.getEntries();
    
    if (!catEntries.isEmpty()) {
      String categoryPosStr = catLkResult.getPosition();
      if (!StringUtil.isEmpty(categoryPosStr)) {
        JNACollectionEntry categoryEntry = catEntries.get(0);
        JNADominoCollectionPosition pos = new JNADominoCollectionPosition(categoryPosStr);
        categoryEntry.setPosition(pos.toTumblerArray());
        return Optional.of(categoryEntry);
      }
    }
    
    return Optional.empty();
  }
  
  /**
   * Makes sure that the navigation rule respects an applied selection
   * 
   * @param nav navigation
   * @return navigation direction respecting selection
   * @throws UnsupportedOperationException in case the given navigation direction cannot be combined with a selection
   */
  private static Navigate addSelectionNavigation(Navigate nav) {
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

  private static Navigate addFTSearchNavigation(Navigate nav) {
    switch (nav) {
    case NEXT_ENTRY:
    case NEXT_DOCUMENT:
      return Navigate.NEXT_HIT;
    case PREV_ENTRY:
    case PREV_DOCUMENT:
      return Navigate.PREV_HIT;
    case NEXT_UNREAD_ENTRY:
      return Navigate.NEXT_UNREAD_HIT;
    case PREV_UNREAD_ENTRY:
      return Navigate.PREV_UNREAD_HIT;
    case NEXT_SELECTED:
      return Navigate.NEXT_SELECTED_HIT;
    case PREV_SELECTED:
      return Navigate.PREV_SELECTED_HIT;
    case NEXT_HIT:
    case NEXT_SELECTED_HIT:
    case NEXT_UNREAD_HIT:
    case PREV_HIT:
    case PREV_SELECTED_HIT:
    case PREV_UNREAD_HIT:
      return nav;
    default:
        throw new IllegalArgumentException(MessageFormat.format("Unable to apply FT search hit navigation to current navigation strategy: {0}", nav));
    }
  }
  
  /**
   * Makes sure that the navigation rule respects expanded entries
   * 
   * @param nav navigation
   * @return navigation direction respecting selection
   * @throws UnsupportedOperationException in case the given navigation direction cannot be combined with expanded entries
   */
  private static Navigate addExpandNavigation(Navigate nav) {
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
  
  private static boolean isDirectionWithSelection(Navigate nav) {
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

  private static Set<Integer> getAllDocNoteIdsInCategory(JNADominoCollection collection, Object[] categoryLevelsAsArr) {
    //reuse our traversal methods to collect all note ids in a top level category
    CollectionTraversalContext ctx = new CollectionTraversalContext();
    ctx.m_collection = collection;
    ctx.m_readMask = EnumSet.of(ReadMask.NOTEID);
    ctx.m_categoryLevelsAsArr = categoryLevelsAsArr;
    ctx.m_direction = Navigate.NEXT_DOCUMENT;
    
    init(ctx);
    
    HashSet<Integer> retNoteIds = new HashSet<>();
    
    while (hasNext(ctx)) {
      CollectionEntry entry = next(ctx);
      retNoteIds.add(entry.getNoteID());
    }
    
    return retNoteIds;
  }

}
