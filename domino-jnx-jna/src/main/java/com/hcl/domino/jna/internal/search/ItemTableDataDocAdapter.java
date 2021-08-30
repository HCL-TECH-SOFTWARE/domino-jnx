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

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.commons.util.NotesDateTimeUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.TypedAccess;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.LMBCSString;

/**
 * Adapter that maps the {@link IItemTableData} interface onto a docunent
 * 
 * @author Karsten Lehmann
 */
public class ItemTableDataDocAdapter implements IItemTableData {
	private JNADocument m_doc;
	private LinkedHashMap<String,String> m_columnValues;
	private boolean m_preferTimeDate = true;
	private TypedAccess m_typedItems;
	private Map<String,Formula> m_compiledFormulas;
	private Map<String,List<Object>> m_compiledFormulaValues;
	private List<String> m_docItemNames;
	
	public ItemTableDataDocAdapter(JNADocument doc, LinkedHashMap<String,String> columnValues) {
		m_doc = doc;
		m_columnValues = columnValues;
		m_compiledFormulas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		m_compiledFormulaValues  = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		m_typedItems = new AbstractTypedAccess() {

			@Override
			public boolean hasItem(String itemName) {
				if (m_columnValues!=null) {
					return m_columnValues.containsKey(itemName);
				}
				else {
					return m_doc.hasItem(itemName);
				}
			}

			@Override
			public List<String> getItemNames() {
				if (m_docItemNames==null) {
					if (m_columnValues!=null) {
						m_docItemNames = new ArrayList<>(m_columnValues.keySet());
					}
					else {
						m_docItemNames = m_doc.getItemNames();
					}
				}
				return m_docItemNames;
			}

			@Override
			protected List<?> getItemValue(String itemName) {
				checkDisposed();
				
				if (m_columnValues!=null) {
					String formulaStr = m_columnValues.get(itemName);

					if (StringUtil.isEmpty(formulaStr)) {
						return m_doc.getItemValue(itemName);
					}
					else {
						List<Object> computedValues = m_compiledFormulaValues.get(itemName);
						if (computedValues==null) {
							Formula compiledFormula = m_compiledFormulas.get(itemName);
							if (compiledFormula==null) {
								compiledFormula = m_doc.getParentDatabase().getParentDominoClient().createFormula(formulaStr);
								m_compiledFormulas.put(itemName, compiledFormula);
							}
							computedValues = compiledFormula.evaluate(m_doc);
							m_compiledFormulaValues.put(itemName, computedValues);
						}
						return computedValues;
					}
				}
				else {
					return m_doc.getItemValue(itemName);
				}
			}};
	}
	
	@Override
	public <T> T get(String itemName, Class<T> valueType, T defaultValue) {
		return m_typedItems.get(itemName, valueType, defaultValue);
	}

	@Override
	public <T> List<T> getAsList(String itemName, Class<T> valueType, List<T> defaultValue) {
		return m_typedItems.getAsList(itemName, valueType, defaultValue);
	}

	@Override
	public Object getItemValue(int index) {
		List<String> itemNames = getItemNames();
		String itemName = itemNames.get(index);
		return get(itemName, Object.class, null);
	}

	@Override
	public int getItemDataType(int index) {
		List<String> itemNames = getItemNames();
		String itemName = itemNames.get(index);
		
		if (m_columnValues!=null) {
			String formulaStr = m_columnValues.get(itemName);
			if (StringUtil.isEmpty(formulaStr)) {
				return m_doc.getFirstItem(itemName)
					.map(Item::getTypeValue)
					.orElse(0);
			}
			else {
				List<Object> computedValues = m_compiledFormulaValues.get(itemName);
				if (computedValues==null) {
					Formula compiledFormula = m_compiledFormulas.get(itemName);
					if (compiledFormula==null) {
						compiledFormula = m_doc.getParentDatabase().getParentDominoClient().createFormula(formulaStr);
						m_compiledFormulas.put(itemName, compiledFormula);
					}
					computedValues = compiledFormula.evaluate(m_doc);
					m_compiledFormulaValues.put(itemName, computedValues);
				}
				
				if (computedValues!=null) {
					if (!computedValues.isEmpty()) {
						Object firstVal = computedValues.get(0);
						if (firstVal instanceof String) {
							return ItemDataType.TYPE_TEXT_LIST.getValue();
						}
						else if (firstVal instanceof Temporal) {
							return ItemDataType.TYPE_TIME_RANGE.getValue();
						}
						else if (firstVal instanceof Number) {
							return ItemDataType.TYPE_NUMBER_RANGE.getValue();
						}
					}
					else {
						return ItemDataType.TYPE_TEXT.getValue();
					}
				}
				return 0;
			}
		}
		else {
			return m_doc.getFirstItem(itemName)
				.map(Item::getTypeValue)
				.orElse(0);
		}
	}

	@Override
	public int getItemsCount() {
		return getItemNames().size();
	}

	@Override
	public void setPreferNotesTimeDates(boolean b) {
		m_preferTimeDate  = b;
	}

	@Override
	public boolean isPreferNotesTimeDates() {
		return m_preferTimeDate;
	}

	@Override
	public boolean hasItem(String itemName) {
		return m_typedItems.hasItem(itemName);
	}

	@Override
	public List<String> getItemNames() {
		return m_typedItems.getItemNames();
	}

	@Override
	public Map<String, Object> asMap() {
		return asMap(true);
	}

	@Override
	public Map<String, Object> asMap(boolean decodeLMBCS) {
		List<String> itemNames = getItemNames();
		
		Map<String,Object> data = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		int itemCount = getItemsCount();
		for (int i=0; i<itemCount; i++) {
			Object val = getItemValue(i);
			
			if (val instanceof LMBCSString) {
				if (decodeLMBCS) {
					data.put(itemNames.get(i), ((LMBCSString)val).getValue());
				}
				else {
					data.put(itemNames.get(i), val);
				}
			}
			else if(!isPreferNotesTimeDates() && val instanceof JNADominoDateTime) {
				data.put(itemNames.get(i), NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)val));
			}
			else if (val instanceof List) {
				if (decodeLMBCS) {
					//check for LMBCS strings and JNADominoDateTime
					List<?> valAsList = (List<?>) val;
					boolean hasLMBCS = false;
					boolean hasTimeDate = false;
					
					for (int j=0; j<valAsList.size(); j++) {
						if (valAsList.get(j) instanceof LMBCSString) {
							hasLMBCS = true;
							break;
						}
						else if (!isPreferNotesTimeDates() && valAsList.get(j) instanceof JNADominoDateTime) {
							hasTimeDate = true;
							break;
						}
					}
					
					if (hasLMBCS || hasTimeDate) {
						List<Object> convList = new ArrayList<>(valAsList.size());
						for (int j=0; j<valAsList.size(); j++) {
							Object currObj = valAsList.get(j);
							if (currObj instanceof LMBCSString) {
								convList.add(((LMBCSString)currObj).getValue());
							}
							else if (!isPreferNotesTimeDates() && currObj instanceof JNADominoDateTime) {
								convList.add(NotesDateTimeUtils.timeDateToCalendar((JNADominoDateTime)currObj));
							}
							else {
								convList.add(currObj);
							}
						}
						data.put(itemNames.get(i), convList);
					}
					else {
						data.put(itemNames.get(i), val);
					}
				}
				else {
					data.put(itemNames.get(i), val);
				}
			}
			else {
				data.put(itemNames.get(i), val);
			}
		}
		return data;
	}

	private void checkDisposed() {
		if (isFreed()) {
			throw new DominoException("Document already disposed");
		}
	}
	
	@Override
	public void free() {
		if (!m_doc.isDisposed()) {
			m_doc.dispose();
		}
	}

	@Override
	public boolean isFreed() {
		return m_doc.isDisposed();
	}
	
	@Override
	public <T> Optional<T> getOptional(String itemName, Class<T> valueType) {
	  return m_typedItems.getOptional(itemName, valueType);
	}
	
	@Override
	public <T> Optional<List<T>> getAsListOptional(String itemName, Class<T> valueType) {
	  return m_typedItems.getAsListOptional(itemName, valueType);
	}
	
}
