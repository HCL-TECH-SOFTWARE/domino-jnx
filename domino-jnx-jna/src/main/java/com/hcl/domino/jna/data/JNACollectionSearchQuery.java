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

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.Find;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNACollectionSearchQueryAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADominoCollectionAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.sun.jna.ptr.ShortByReference;

public class JNACollectionSearchQuery extends BaseJNAAPIObject<JNACollectionSearchQueryAllocations> implements CollectionSearchQuery {
	//where to start reading view data:
	private boolean m_startAtLastEntry;
	private int m_startAtEntryId;
	private String m_restrictToCategory;
	private List<Object> m_restrictToCategoryLevels;
	private String m_startAtPosition;

	//IDTables to control what is expanded/selected
	private ExpandedEntries m_expandedEntries;
	private JNAIDTable m_expandedEntriesResolved;
	
	private SelectedEntries m_selectedEntries;
	private JNAIDTable m_selectedEntriesResolved;
	private boolean m_hasSelectionSet;

	//how to traverse the view
	private Set<ReadMask> m_readMask;
	private Navigate m_direction;
	private Integer m_total;
	private boolean m_hasExpandedEntries;
  private Consumer<Integer> m_totalReceiver;
	
	JNACollectionSearchQuery(JNADominoCollection parentCollection) {
		super(parentCollection);
		
		m_direction = Navigate.NEXT_ENTRY;
		m_readMask = new HashSet<>();
		m_readMask.add(ReadMask.NOTEID);
		
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNACollectionSearchQueryAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNACollectionSearchQueryAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	protected void checkDisposedLocal() {
		JNADominoCollection parentCollection = (JNADominoCollection) getParent();
		
		if (parentCollection.isDisposed()) {
			throw new ObjectDisposedException(this);
		}
	}

	@Override
	public CollectionSearchQuery readSpecialValues(Collection<SpecialValue> values) {
		if (values!=null) {
			values.forEach(this::addSpecialValue);
		}
		return this;
	}
	
	@Override
	public CollectionSearchQuery readSpecialValues(SpecialValue... values) {
		if (values!=null) {
			for (SpecialValue currVal : values) {
				addSpecialValue(currVal);
			}
		}
		return this;
	}
	
	private void addSpecialValue(SpecialValue value) {
		switch (value) {
		case CHILDCOUNT:
			m_readMask.add(ReadMask.INDEXCHILDREN);
			break;
		case DESCENDANTCOUNT:
			m_readMask.add(ReadMask.INDEXDESCENDANTS);
			break;
		case INDEXPOSITION:
			m_readMask.add(ReadMask.INDEXPOSITION);
			break;
		case SIBLINGCOUNT:
			m_readMask.add(ReadMask.INDEXSIBLINGS);
			break;
		case UNREAD:
			m_readMask.add(ReadMask.INDEXUNREAD);
			break;
		case ANYUNREAD:
			m_readMask.add(ReadMask.INDEXANYUNREAD);
			break;
			default:
				// there's no ReadMask entry for SEQUENCENUMBER / SEQUENCETIME,
				// must be read from the DB / doc
		}
	}

	@Override
	public CollectionSearchQuery startAtFirstEntry() {
		//reset all
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_restrictToCategory = null;
		m_restrictToCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtEntryId(int noteId) {
		m_startAtEntryId = noteId;
		m_startAtLastEntry = false;
		m_restrictToCategory = null;
		m_restrictToCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtLastEntry() {
		m_startAtLastEntry = true;
		
		m_startAtEntryId = 0;
		m_restrictToCategory = null;
		m_restrictToCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery restrictToCategory(String category) {
		m_restrictToCategory = category;
		
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_restrictToCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery restrictToCategory(List<Object> categoryLevels) {
		m_restrictToCategoryLevels = categoryLevels==null ? null : new ArrayList<>(categoryLevels);
		
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_restrictToCategory = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtPosition(String pos) {
		m_startAtPosition = pos;
		
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_restrictToCategory = null;
		m_restrictToCategoryLevels = null;
		
		m_total = null;
		return this;
	}
	
	@Override
	public CollectionSearchQuery expand(ExpandedEntries expandedEntries) {
		m_expandedEntries = expandedEntries;
		m_expandedEntriesResolved = null;
		m_hasExpandedEntries = true;
		m_total = null;
		if (!isDirectionWithExpandCollapse(m_direction)) {
			//automatically select a traversal strategy that makes use of
			//selection / expanded info
			
			if (isDirectionWithSelection(m_direction)) {
				m_direction = Navigate.NEXT_EXPANDED_SELECTED;
			}
			else {
				m_direction = Navigate.NEXT_EXPANDED;
			}
		}
		return this;
	}

	@Override
	public CollectionSearchQuery select(SelectedEntries selectedEntries) {
		m_selectedEntries = selectedEntries;
		m_selectedEntriesResolved = null;
		m_total = null;
		m_hasSelectionSet = true;
		
		if (!isDirectionWithSelection(m_direction)) {
			//automatically select a traversal strategy that makes use of
			//selection / expanded info
			
			if (isDirectionWithExpandCollapse(m_direction)) {
				m_direction = Navigate.NEXT_EXPANDED_SELECTED;
			}
			else {
				m_direction = Navigate.NEXT_SELECTED;
			}
		}
		return this;
	}

	@Override
	public CollectionSearchQuery selectByKey(List<Object> key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllDeselectedEntries)) {
			select(SelectedEntries.deselectAll().selectByKey(key, exact));
		}
		else {
			((AllDeselectedEntries)m_selectedEntries).selectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery selectByKey(String key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllDeselectedEntries)) {
			select(SelectedEntries.deselectAll().selectByKey(key, exact));
		}
		else {
			((AllDeselectedEntries)m_selectedEntries).selectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery deselectByKey(List<Object> key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllSelectedEntries)) {
			select(SelectedEntries.selectAll().deselectByKey(key, exact));
		}
		else {
			((AllSelectedEntries)m_selectedEntries).deselectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery deselectByKey(String key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllSelectedEntries)) {
			select(SelectedEntries.selectAll().deselectByKey(key, exact));
		}
		else {
			((AllSelectedEntries)m_selectedEntries).deselectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery direction(Navigate mode) {
		m_direction = mode;
		return this;
	}

	@Override
	public CollectionSearchQuery readColumnValues() {
		m_readMask.add(ReadMask.SUMMARYVALUES);
		return this;
	}
	
	@Override
	public CollectionSearchQuery readUNID() {
		m_readMask.add(ReadMask.NOTEUNID);
		return this;
	}
	
	@Override
	public CollectionSearchQuery readDocumentClass() {
		m_readMask.add(ReadMask.NOTECLASS);
		return this;
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

	private Optional<JNACollectionEntry> findCategoryPosition(JNADominoCollection collection, Object[] categoryLevelsAsArr, boolean readDescendantCount,
	    boolean readChildCount) {
	  
	  while (true) {
	    //find current position of category entry
	    NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(Find.MATCH_CATEGORYORLEAF,
	        Find.REFRESH_FIRST, Find.CASE_INSENSITIVE),
	        EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), categoryLevelsAsArr);
	    
	    List<JNACollectionEntry> catEntries = catLkResult.getEntries();
	    
	    String categoryPosStr = catLkResult.getPosition();
	    if (StringUtil.isEmpty(categoryPosStr) || catEntries.isEmpty()) {
	      return Optional.empty();
	    }
	    
	    JNACollectionEntry categoryEntry = catEntries.get(0);
	    JNADominoCollectionPosition pos = new JNADominoCollectionPosition(categoryPosStr);
	    categoryEntry.setPosition(pos.toTumblerArray());
	    
	    if (!readDescendantCount && !readChildCount) {
	      return Optional.of(categoryEntry);
	    }
	    
	    //we need to read more values than findByKeyExtended2 supports
	    EnumSet<ReadMask> readMask = EnumSet.of(ReadMask.NOTEID);
	    if (readDescendantCount) {
	      readMask.add(ReadMask.INDEXDESCENDANTS);
	    }
	    if (readChildCount) {
	      readMask.add(ReadMask.INDEXCHILDREN); 
	    }
	    
	    NotesViewLookupResultData lkResult =
          collection.readEntriesExt(new JNADominoCollectionPosition(categoryPosStr),
              Navigate.CURRENT,
                  true,
              0, Navigate.CURRENT,
              1, readMask,
              (DominoDateTime) null,
              (JNAIDTable) null,
              (Integer) null);
	    
	    List<JNACollectionEntry> entriesAtPos = lkResult.getEntries();
	    if (entriesAtPos.isEmpty()) {
	      //category is gone
	      return Optional.empty();
	    }
	    
	    JNACollectionEntry entryAtPos = entriesAtPos.get(0);
	    if (entryAtPos.getNoteID() != categoryEntry.getNoteID()) {
	      //something has moved; refresh view & retry
	      collection.refresh();
	      continue;
	    }
	    
	    if (readDescendantCount) {
	      categoryEntry.setDescendantCount(entryAtPos.getSpecialValue(SpecialValue.DESCENDANTCOUNT, Integer.class, 0));
	    }
	    if (readChildCount) {
	      categoryEntry.setChildCount(entryAtPos.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, 0));
	    }
	    
	    return Optional.of(categoryEntry);
	  }
	}
	
	@Override
	public <T> T build(int skip, int count, CollectionEntryProcessor<T> processor) {
	  if (m_totalReceiver!=null) {
	    m_totalReceiver.accept(computeTotal());
	  }
	  
    JNADominoCollection collection = (JNADominoCollection) getParent();
    JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);
	  
    ShortByReference updateFiltersFlags = new ShortByReference();
    Navigate directionToUse = prepareCollectionReadRestrictions(collectionAllocations, updateFiltersFlags);

    EnumSet<ReadMask> readMaskToUse = EnumSet.copyOf(m_readMask);
    
    Navigate skipNav = directionToUse;
    Navigate returnNav = directionToUse;
    
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

    final short fUpdateFiltersFlagsVal = updateFiltersFlags.getValue();

    int fCount = count;
    final Navigate fSkipNav = skipNav;
    final Navigate fReturnNav = returnNav;
    
    JNADominoCollection.JNACollectionEntryProcessor<T> jnaProcessor = new JNADominoCollection.JNACollectionEntryProcessor<T>() {
      int entriesRead = 0;

      @Override
      public T start() {
        entriesRead = 0;
        return processor.start();
      }

      @Override
      public Action entryRead(T result, CollectionEntry entry) {
        Action action = processor.entryRead(result, entry);
        entriesRead++;

        if (entriesRead>=fCount) {
          return Action.Stop;
        }
        else {
          return action;
        }
      }

      @Override
      public T end(T result) {
        return processor.end(result);
      }

      @Override
      public String getNameForSingleColumnRead() {
        if (processor instanceof JNADominoCollection.JNACollectionEntryProcessor) {
          return ((JNADominoCollection.JNACollectionEntryProcessor)processor).getNameForSingleColumnRead();
        }
        return super.getNameForSingleColumnRead();
      }
      
      @Override
      public Action retryingReadBecauseViewIndexChanged(int nrOfRetries, long durationSinceStart) {
        if (processor instanceof JNADominoCollection.JNACollectionEntryProcessor) {
          return ((JNADominoCollection.JNACollectionEntryProcessor)processor).retryingReadBecauseViewIndexChanged(nrOfRetries, durationSinceStart);
        }
        return Action.Continue;
      }
    };
    
    String singleColumnName = jnaProcessor.getNameForSingleColumnRead();
    Integer singleColumnIndex = StringUtil.isEmpty(singleColumnName) ? null : collection.getColumnValuesIndex(singleColumnName);
    
    return LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
      if (fUpdateFiltersFlagsVal != 0) {
        //the method prepareCollectionReadRestrictions has modified the selected list; for remote databases, push IDTable changes via NRPC

        short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, fUpdateFiltersFlagsVal);
        NotesErrorUtils.checkResult(result);
      }
      
      if (m_restrictToCategory!=null || m_restrictToCategoryLevels!=null) {
        //read descendants of a category
        
        Object[] categoryLevelsAsArr;
        if (m_restrictToCategory!=null) {
          categoryLevelsAsArr = new Object[] {m_restrictToCategory};
        }
        else {
          categoryLevelsAsArr = m_restrictToCategoryLevels.toArray(new Object[m_restrictToCategoryLevels.size()]);
        }
        
        T result = jnaProcessor.start();

        //add INDEXPOSITION to be able to check if we're still below the category
        readMaskToUse.add(ReadMask.INDEXPOSITION);
        
        int currOffset = skip;

        String categoryPosStr = null;
        JNADominoCollectionPosition categoryPos = null;
        Integer descendantCount = null;
        int entriesRead = 0;
        
        while (true) {
          if (categoryPosStr==null) {
            JNACollectionEntry categoryEntry = findCategoryPosition(collection, categoryLevelsAsArr,
                descendantCount==null, false).orElse(null);
            
            if (categoryEntry==null) {
              //category not found or gone, return what we have
              result = jnaProcessor.end(result);
              return result;
            }
            
            categoryPosStr = categoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
                        
            if (StringUtil.isEmpty(categoryPosStr)) {
              //category not found or gone, return what we have
              result = jnaProcessor.end(result);
              return result;
            }
            
            categoryPos = new JNADominoCollectionPosition(categoryPosStr);
            if (descendantCount==null) {
              //find an upper limit for the NIFReadEntries call to not read too many entries
              descendantCount = categoryEntry.getSpecialValue(SpecialValue.DESCENDANTCOUNT, Integer.class, 0);
            }
          }
          
          JNADominoCollectionPosition readEntriesTopPos;
          
          Navigate skipNavToUse = fSkipNav;
          Navigate returnNavToUse = fReturnNav;
          
          if (skipNavToUse == Navigate.CHILD_ENTRY) {
            //for CHILD_ENTRY, we first return the first child of the category node, then its own first child etc.
            readEntriesTopPos = new JNADominoCollectionPosition(categoryPosStr);
          }
          else {
            String firstChildPosStr = categoryPosStr + ".0"; //$NON-NLS-1$
            readEntriesTopPos = new JNADominoCollectionPosition(firstChildPosStr);
          }
          
          readEntriesTopPos.setMinLevel(categoryPos.getLevel()+1);

          int maxEntriesToRead = Math.max(descendantCount - currOffset, 1);
          
          NotesViewLookupResultData lkResult =
              collection.readEntriesExt(readEntriesTopPos,
                  skipNavToUse,
                  true,
                  1 + currOffset, returnNavToUse,
                  maxEntriesToRead, readMaskToUse,
                  (DominoDateTime) null,
                  (JNAIDTable) null,
                  singleColumnIndex);

          if (lkResult.hasAnyNonDataConflicts()) {
            //view index has changed, update the view
            collection.refresh();
            
            //check if our category position is still correct; if yes, we can use the lookup result
            JNACollectionEntry newCategoryEntry = findCategoryPosition(collection, categoryLevelsAsArr,
                true, false).orElse(null);
            
            if (newCategoryEntry==null) {
              //category disappeared, return what we have read; since we cannot be sure that "lkResult" contains
              //data of the right category, we throw it away
              result = jnaProcessor.end(result);
              return result;
            }
            
            String newCategoryPosStr = newCategoryEntry.getSpecialValue(SpecialValue.INDEXPOSITION,
                String.class, ""); //$NON-NLS-1$
            
            if (StringUtil.isEmpty(newCategoryPosStr)) {
              //category disappeared, return what we have read; since we cannot be sure that "lkResult" contains
              //data of the right category, we throw it away
              result = jnaProcessor.end(result);
              return result;
            }
            
            if (!newCategoryPosStr.equals(categoryPosStr)) {
              //the category has changed position, so it's possible that our data comes from a different category;
              //it's better to rerun this iteration at the current offset
              categoryPosStr = newCategoryPosStr;
              categoryPos = new JNADominoCollectionPosition(newCategoryPosStr);
              descendantCount = newCategoryEntry.getSpecialValue(SpecialValue.DESCENDANTCOUNT, Integer.class, 0);
              continue;
            }
          }
          
          List<JNACollectionEntry> entries = lkResult.getEntries();
          if (entries.isEmpty()) {
            break;
          }
          
          boolean outOfScope = false;
          boolean aborted = false;
          int entriesReported = 0;
          
          String positionPrefixInScope = categoryPosStr + "."; //$NON-NLS-1$
          
          for (JNACollectionEntry currEntry : entries) {
            String currEntryPos = currEntry.getSpecialValue(SpecialValue.INDEXPOSITION,
                String.class, ""); //$NON-NLS-1$
            
            if (!currEntryPos.startsWith(positionPrefixInScope)) {
              //we left the category; we don't expect this to happen, because we set a minlevel
              outOfScope = true;
              break;
            }
            
            entriesRead++;
            entriesReported++;
            
            Action action = jnaProcessor.entryRead(null, currEntry);
            if (action==Action.Stop) {
              aborted = true;
              break;
            }
          }
          
          if (aborted || outOfScope || entriesReported==0 || !lkResult.hasMoreToDo()) {
            break;
          }
          
          if (entriesRead>=descendantCount) {
            //we read all descendants => we stopped at the last descendant of the category
            break;
          }
          
          currOffset+=entries.size();
        }

        result = jnaProcessor.end(result);
        return result;
      }

      String startPosStr;
      int additionalSkip;
      
      if (m_startAtLastEntry) {
        //skip over the whole view to find the last entry
        NotesViewLookupResultData lastEntryLkResult =
            collection.readEntriesExt(new JNADominoCollectionPosition("0"), //$NON-NLS-1$
            Navigate.NEXT_ENTRY, true,
            Integer.MAX_VALUE, Navigate.CURRENT,
            1, EnumSet.of(ReadMask.INDEXPOSITION), (DominoDateTime) null,
            (JNAIDTable) null,
            (Integer) singleColumnIndex);
        
        String lastEntryPosStr = ""; //$NON-NLS-1$
        
        if (!lastEntryLkResult.getEntries().isEmpty()) {
          lastEntryPosStr = lastEntryLkResult
              .getEntries()
              .get(0)
              .getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
          additionalSkip = 0;
        }
        
        if (StringUtil.isEmpty(lastEntryPosStr)) {
          T obj = jnaProcessor.start();
          obj = jnaProcessor.end(obj);
          return obj;
        }
        
        startPosStr = lastEntryPosStr;
        additionalSkip = 0;
      }
      else if (m_startAtPosition!=null) {
        startPosStr = m_startAtPosition;
        additionalSkip = 0;
      }
      else if (m_startAtEntryId!=0) {
        startPosStr = Integer.toString(m_startAtEntryId);
        readMaskToUse.add(ReadMask.INIT_POS_NOTEID);
        additionalSkip = 0;
      }
      else {
        //default: start at first entry
        startPosStr = "0"; //$NON-NLS-1$
        additionalSkip = 1;
      }
      
      JNADominoCollectionPosition currPos = new JNADominoCollectionPosition(startPosStr);

      boolean firstLoop = true;
      
      T obj = jnaProcessor.start();
      
      while (true) {
        NotesViewLookupResultData lkResult =
            collection.readEntriesExt(currPos,
                fSkipNav,
                    true,
                firstLoop ? (skip+additionalSkip) : 1, fReturnNav,
                    fCount, readMaskToUse, (DominoDateTime) null,
                (JNAIDTable) null,
                singleColumnIndex);

        firstLoop = false;
        
        List<JNACollectionEntry> entries = lkResult.getEntries();
        for (JNACollectionEntry currEntry : entries) {
          Action action = jnaProcessor.entryRead(obj, currEntry);
          if (action==Action.Stop) {
            obj = jnaProcessor.end(obj);
            return obj;
          }
        }
        
        if (!lkResult.hasMoreToDo()) {
          break;
        }
        
        if (readMaskToUse.contains(ReadMask.INIT_POS_NOTEID)) {
          //make sure to only use this flag on the first lookup call
          readMaskToUse.remove(ReadMask.INIT_POS_NOTEID);
        }

      }

      obj = jnaProcessor.end(obj);
      
      return obj;
    });
	}

	public <T> T build_old(int skip, int count, CollectionEntryProcessor<T> processor) {
		JNADominoCollection collection = (JNADominoCollection) getParent();
		JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);

		ShortByReference updateFiltersFlags = new ShortByReference();
		Navigate directionToUse = prepareCollectionReadRestrictions(collectionAllocations, updateFiltersFlags);

		if (directionToUse == Navigate.CURRENT && count>1) {
			//prevent reading too many entries if navigation is set to just read the current entry
			count = 1;
		}
		
		final short fUpdateFiltersFlagsVal = updateFiltersFlags.getValue();
		final int fCount = count;
		final Navigate fDirectionToUse = directionToUse;
		
		return LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
      if (fUpdateFiltersFlagsVal != 0) {
        //the method prepareCollectionReadRestrictions has modified the selected list; for remote databases, push IDTable changes via NRPC

        short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, fUpdateFiltersFlagsVal);
        NotesErrorUtils.checkResult(result);
      }

      JNADominoCollection.JNACollectionEntryProcessor<T> jnaProcessor = new JNADominoCollection.JNACollectionEntryProcessor<T>() {
        int entriesRead = 0;

        @Override
        public T start() {
          entriesRead = 0;
          return processor.start();
        }

        @Override
        public Action entryRead(T result, CollectionEntry entry) {
          Action action = processor.entryRead(result, entry);
          entriesRead++;

          if (entriesRead>=fCount) {
            return Action.Stop;
          }
          else {
            return action;
          }
        }

        @Override
        public T end(T result) {
          return processor.end(result);
        }

      };
      
			while (true) {
				if (m_restrictToCategory!=null || m_restrictToCategoryLevels!=null) {
          Object[] categoryLevelsAsArr;
          if (m_restrictToCategory!=null) {
            categoryLevelsAsArr = new Object[] {m_restrictToCategory};
          }
          else {
            categoryLevelsAsArr = m_restrictToCategoryLevels.toArray(new Object[m_restrictToCategoryLevels.size()]);
          }

          T result = collection.getAllEntriesInCategory(categoryLevelsAsArr, skip, fDirectionToUse, null, null,
              fCount, m_readMask, jnaProcessor);
          return result;
        }
				
				int indexModStart = collection.getIndexModifiedSequenceNo();

        //find first entry to read

        String startPos;
        int additionalSkip;

        if (m_startAtLastEntry) {
          startPos = "last"; //$NON-NLS-1$
          additionalSkip = 0;
        }
        else if (m_startAtPosition!=null) {
          startPos = m_startAtPosition;
          additionalSkip = 0;
        }
        else if (m_startAtEntryId!=0) {
          JNAIDTable selectedList = collectionAllocations.getSelectedList();
          JNAIDTable clonedSelectedList = (JNAIDTable) selectedList.clone();
          
          selectedList.setInverted(false);
          selectedList.clear();
          selectedList.add(m_startAtEntryId);
          
          //for remote databases, push IDTable changes via NRPC
          short resultUpdateFilter = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, NotesConstants.FILTER_SELECTED);
          NotesErrorUtils.checkResult(resultUpdateFilter);
          
          List<CollectionEntry> selectedEntries = collection.getAllEntries("0", 1, Navigate.NEXT_SELECTED, Integer.MAX_VALUE, //$NON-NLS-1$
              EnumSet.of(ReadMask.INDEXPOSITION, ReadMask.NOTEID),
              new JNADominoCollection.EntriesAsListCallback(Integer.MAX_VALUE));
          
          //reset selection IDTable
          selectedList.clear();
          selectedList.addAll(clonedSelectedList);
          selectedList.setInverted(clonedSelectedList.isInverted());
          clonedSelectedList.dispose();
          
          resultUpdateFilter = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, NotesConstants.FILTER_SELECTED);
          NotesErrorUtils.checkResult(resultUpdateFilter);

          if (selectedEntries.isEmpty()) {
            //start entry not found
            T result = processor.start();
            result = processor.end(result);
            return result;
          }
          
          String entryPos = selectedEntries.get(0).getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
          if (StringUtil.isEmpty(entryPos)) {
            //start entry not found
            T result = processor.start();
            result = processor.end(result);
            return result;
          }

          startPos = entryPos;
          additionalSkip = 0;
        }
        else {
          //default: start at first entry
          startPos = "0"; //$NON-NLS-1$
          additionalSkip = 1;
        }

        T result = collection.getAllEntries(startPos, additionalSkip + skip,
            fDirectionToUse, fCount, m_readMask, jnaProcessor);

        int indexModEnd = collection.getIndexModifiedSequenceNo();
        
        if (indexModStart != indexModEnd) {
          //restart lookup, index changed
          collection.refresh();
          continue;
        }
        return result;
      
        
			}
		});
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

	@Override
	public LinkedHashSet<Integer> collectIds(int skip, int count) {
		return build(skip, count, new CollectionEntryProcessor<LinkedHashSet<Integer>>() {

			@Override
			public LinkedHashSet<Integer> start() {
				return new LinkedHashSet<>();
			}

			@Override
			public Action entryRead(LinkedHashSet<Integer> result, CollectionEntry entry) {
				result.add(entry.getNoteID());
				return Action.Continue;
			}

			@Override
			public LinkedHashSet<Integer> end(LinkedHashSet<Integer> result) {
				return result;
			}
		});
	}

	@Override
	public void collectIds(int skip, int count, Collection<Integer> idTable) {
		LinkedHashSet<Integer> ids = collectIds(skip, count);
		idTable.addAll(ids);
		
		build(skip, count, new CollectionEntryProcessor<Collection<Integer>>() {

			@Override
			public Collection<Integer> start() {
				return idTable;
			}

			@Override
			public Action entryRead(Collection<Integer> result, CollectionEntry entry) {
				result.add(entry.getNoteID());
				return Action.Continue;
			}

			@Override
			public Collection<Integer> end(Collection<Integer> result) {
				return result;
			}
		});
	}

	@Override
	public List<CollectionEntry> collectEntries(int skip, int count) {
		return build(skip, count, new CollectionEntryProcessor<List<CollectionEntry>>() {

			@Override
			public List<CollectionEntry> start() {
				return new ArrayList<>();
			}

			@Override
			public Action entryRead(List<CollectionEntry> result, CollectionEntry entry) {
				result.add(entry);
				return Action.Continue;
			}

			@Override
			public List<CollectionEntry> end(List<CollectionEntry> result) {
				return result;
			}
		});
	}

	@Override
	public void collectEntries(int skip, int count, Collection<CollectionEntry> collection) {
		build(skip, count, new CollectionEntryProcessor<Collection<CollectionEntry>>() {

			@Override
			public Collection<CollectionEntry> start() {
				return collection;
			}

			@Override
			public Action entryRead(Collection<CollectionEntry> result, CollectionEntry entry) {
				result.add(entry);
				return Action.Continue;
			}

			@Override
			public Collection<CollectionEntry> end(Collection<CollectionEntry> result) {
				return result;
			}
		});
	}

	@Override
  public CollectionSearchQuery withTotalReceiver(Consumer<Integer> totalReceiver) {
	  m_totalReceiver = totalReceiver;
	  return this;
	}
	
	private int computeTotal() {
		if (m_total==null) {
			JNADominoCollection collection = (JNADominoCollection) getParent();
			JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);

			ShortByReference updateFiltersFlags = new ShortByReference();
			Navigate directionToUse = prepareCollectionReadRestrictions(collectionAllocations, updateFiltersFlags);
			
			final short fUpdateFiltersFlagsVal = updateFiltersFlags.getValue();
			final Navigate fDirectionToUse = directionToUse;
			
			m_total = LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
				if (fUpdateFiltersFlagsVal != 0) {
					//for remote databases, push IDTable changes via NRPC

					short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, fUpdateFiltersFlagsVal);
					NotesErrorUtils.checkResult(result);
				}
				
				if (m_restrictToCategory!=null || m_restrictToCategoryLevels!=null) {
					Object[] categoryLevelsAsArr;
					if (m_restrictToCategory!=null) {
						categoryLevelsAsArr = new Object[] {m_restrictToCategory};
					}
					else {
						categoryLevelsAsArr = m_restrictToCategoryLevels.toArray(new Object[m_restrictToCategoryLevels.size()]);
					}

					if (JNADominoCollection.isDescendingNav(fDirectionToUse)) {
						return 0;
					}
					
					while (true) {
						int indexModStart = collection.getIndexModifiedSequenceNo();

						//find category entry
						NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(Find.MATCH_CATEGORYORLEAF,
								Find.REFRESH_FIRST, Find.CASE_INSENSITIVE),
								EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), categoryLevelsAsArr);

						if (catLkResult.getReturnCount()==0) {
							//category not found
							return 0;
						}

						final String catPosStr = catLkResult.getPosition();
						if (StringUtil.isEmpty(catPosStr)) {
							//category not found
							return 0;
						}
						
						JNADominoCollectionPosition catPos = new JNADominoCollectionPosition(catPosStr);
						NotesViewLookupResultData skipResult = collection.readEntries(catPos, fDirectionToUse, false,
								Integer.MAX_VALUE, fDirectionToUse, 0, EnumSet.of(ReadMask.NOTEID));
						
						int indexModEnd = collection.getIndexModifiedSequenceNo();

						if (indexModStart != indexModEnd) {
							collection.refresh();
							continue;
						}
						
						return skipResult.getSkipCount();
					}
				}
				else {
					//find first entry to read
					String startPos;

					if (m_startAtLastEntry) {
						NotesViewLookupResultData findLastResult = collection.readEntries(
								new JNADominoCollectionPosition("0"), //$NON-NLS-1$
								Navigate.NEXT_ENTRY, true, Integer.MAX_VALUE, Navigate.CURRENT,
								0, EnumSet.of(ReadMask.INDEXPOSITION));
						
						List<JNACollectionEntry> entries = findLastResult.getEntries();
						if (entries.isEmpty()) {
							return 0;
						}
						
						startPos = entries.get(0).getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
					}
					else if (m_startAtEntryId!=0) {
						String entryPos = collection.locateNote("1", m_startAtEntryId); //$NON-NLS-1$
						if (StringUtil.isEmpty(entryPos)) {
							//start entry not found
							return 0;
						}

						startPos = entryPos;
					}
					else {
						//default: start at first entry
						startPos = "0"; //$NON-NLS-1$
					}

					NotesViewLookupResultData skipResult = collection.readEntries(
							new JNADominoCollectionPosition(startPos),
							fDirectionToUse, false, Integer.MAX_VALUE, Navigate.CURRENT,
							0, EnumSet.of(ReadMask.NOTEID));
					
					return skipResult.getSkipCount();
				}
			});
		}
		return m_total;
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
		
		Navigate directionToUse = m_direction;
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
			JNAIDTable resolvedSelectedList = resolveSelectedEntries(m_selectedEntries);
			
	    JNADominoCollection collection = (JNADominoCollection) getParent();
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
			JNAIDTable collapsedList = collectionAllocations.getCollapsedList();
			JNAIDTable resolvedCollapsedList = resolveExpandedEntries(m_expandedEntries);
			collapsedList.clear();
			collapsedList.addAll(resolvedCollapsedList);
			collapsedList.setInverted(resolvedCollapsedList.isInverted());

			updateFiltersFlagsVal |= NotesConstants.FILTER_COLLAPSED;
		}

		//return update flags to push the changed idtables to remote DBs
		updateFiltersFlags.setValue(updateFiltersFlagsVal);
		
		//return the direction that is used for the actual lookup
		return directionToUse;
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
	 * Collect all note ids configured in the {@link ExpandedEntries} object
	 * 
	 * @param expandedEntries info about expanded entries
	 * @param retIdTable IDTable to clear and write new note ids
	 */
	private JNAIDTable resolveExpandedEntries(ExpandedEntries expandedEntries) {
		if (m_expandedEntriesResolved==null || m_expandedEntriesResolved.isDisposed()) {
			JNAIDTable idTable = new JNAIDTable(getParentDominoClient());
			idTable.clear();
			
			if (expandedEntries!=null) {
			  JNADominoCollection collection = (JNADominoCollection) getParent();

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
            LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
                new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey());
            
            idTable.addAll(idsForKey);
          }
          
          for (MultiColumnLookupKey currKey : multiColLookups) {
            Set<Find> findFlags = EnumSet.of(Find.EQUAL, Find.RANGE_OVERLAP, Find.CASE_INSENSITIVE);
            if (!currKey.isExact()) {
              findFlags.add(Find.PARTIAL);
            }
            LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
                new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey().toArray(new Object[currKey.getKey().size()]));
            
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
        
				JNADatabase db = (JNADatabase) ((JNADominoCollection)getParent()).getParentDatabase();

				List<DQLTerm> dqlQueries = expandedEntries.getDQLQueries();
				for (DQLTerm currDQLQuery : dqlQueries) {
					db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, idTable);
				}

        List<Pair<String, Set<FTQuery>>> ftQueriesWithFlags = expandedEntries.getFTQueries();
        
        for (Pair<String,Set<FTQuery>> currFTQueryWithFlags : ftQueriesWithFlags) {
          String currFTQuery = currFTQueryWithFlags.getValue1();
          //ignore FT flags that do not make sense here
          Set<FTQuery> currFTFlags = currFTQueryWithFlags.getValue2()
              .stream()
              .filter(FTQuery.allSearchContentFlags::contains)
              .collect(Collectors.toSet());
          currFTFlags.add(FTQuery.RETURN_IDTABLE);
          
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
	
	/**
	 * Collect all note ids configured in the {@link ExpandedEntries} object
	 * 
	 * @param selectedEntries info about selected entries
	 */
	private JNAIDTable resolveSelectedEntries(SelectedEntries selectedEntries) {
		if (m_selectedEntriesResolved==null || m_selectedEntriesResolved.isDisposed()) {
			JNAIDTable idTable = new JNAIDTable(getParentDominoClient());
			idTable.clear();

			if (selectedEntries!=null) {
				JNADominoCollection collection = (JNADominoCollection) getParent();
				
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
						Set<Find> findFlags = EnumSet.of(Find.EQUAL, Find.RANGE_OVERLAP, Find.CASE_INSENSITIVE);
						if (!currKey.isExact()) {
							findFlags.add(Find.PARTIAL);
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
				
				JNADatabase db = (JNADatabase) ((JNADominoCollection)getParent()).getParentDatabase();

				List<DQLTerm> dqlQueries = selectedEntries.getDQLQueries();
				for (DQLTerm currDQLQuery : dqlQueries) {
					if (subtractMode) {
						JNAIDTable tableOfDQLResult = new JNAIDTable(getParentDominoClient());
						db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, tableOfDQLResult);
						idTable.removeAll(tableOfDQLResult);
						tableOfDQLResult.dispose();
					}
					else {
						db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, idTable);
					}
				}
				
        List<Pair<String,Set<FTQuery>>> ftQueriesWithFlags = selectedEntries.getFTQueries();

        for (Pair<String,Set<FTQuery>> currFTQueryWithFlags : ftQueriesWithFlags) {
          String currFTQuery = currFTQueryWithFlags.getValue1();
          //ignore FT flags that do not make sense here
          Set<FTQuery> currFTFlags = currFTQueryWithFlags.getValue2()
              .stream()
              .filter(FTQuery.allSearchContentFlags::contains)
              .collect(Collectors.toSet());
          currFTFlags.add(FTQuery.RETURN_IDTABLE);
          
          if (subtractMode) {
            JNAIDTable tableOfFTResult = new JNAIDTable(collection.getParentDominoClient());
            db.queryFTIndex(currFTQuery, 0, currFTFlags, null, 0, 0).collectIds(0, Integer.MAX_VALUE, tableOfFTResult);
            idTable.removeAll(tableOfFTResult);
            tableOfFTResult.dispose();
          }
          else {
            db.queryFTIndex(currFTQuery, 0, currFTFlags, null, 0, 0).collectIds(0, Integer.MAX_VALUE, idTable);
          }
        }
			}
			
			m_selectedEntriesResolved = idTable;
		}
		return m_selectedEntriesResolved;
	}
	
	@Override
	public void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer) {
		JNADominoCollection parentCollection = (JNADominoCollection) getParent();
		JNADatabase parentDb = (JNADatabase) parentCollection.getParentDatabase();
		
		LinkedHashSet<Integer> ids = collectIds(skip, count);
		Stream<Document> docs = ids.stream()
			.map(parentDb::getDocumentById)
			.filter(Optional::isPresent)
			.map(Optional::get);
		
		Iterator<Document> docsIt = docs.iterator();
		
		LoopImpl loop = new LoopImpl();
		
		while (docsIt.hasNext()) {
			Document currDoc = docsIt.next();
			
			if (!docsIt.hasNext()) {
				loop.setIsLast();
			}
			
			consumer.accept(currDoc, loop);
			if (loop.isStopped()) {
				break;
			}
			
			loop.next();
		}
	}

	private static class LoopImpl extends Loop {
		
		public void next() {
			super.setIndex(getIndex()+1);
		}
		
		@Override
		public void setIsLast() {
			super.setIsLast();
		}
	}
}
