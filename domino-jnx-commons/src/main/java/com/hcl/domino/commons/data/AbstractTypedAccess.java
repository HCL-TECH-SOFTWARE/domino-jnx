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
package com.hcl.domino.commons.data;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoTimeType;
import com.hcl.domino.data.IndexedTypedAccess;
import com.hcl.domino.data.PreV3Author;
import com.hcl.domino.data.TypedAccess;
import com.hcl.domino.data.UserData;
import com.hcl.domino.design.DesignAgent;

/**
 * Utility class to convert data of an object to different value types
 * 
 * @author Karsten Lehmann
 */
public abstract class AbstractTypedAccess implements TypedAccess, IndexedTypedAccess {

	protected <T> T getViaValueConverter(String itemName, Class<T> valueType, T defaultValue) {
		throw new IllegalArgumentException(MessageFormat.format("Unsupported return value type: {0}", valueType.getName()));
	}
	
	protected <T> List<T> getAsListViaValueConverter(String itemName, Class<T> valueType, List<T> defaultValue) {
		throw new IllegalArgumentException(MessageFormat.format("Unsupported return value type: {0}", valueType.getName()));
	}
	
	protected <T> T getViaValueConverter(int index, Class<T> valueType, T defaultValue) {
		throw new IllegalArgumentException(MessageFormat.format("Unsupported return value type: {0}", valueType.getName()));
	}
	
	protected <T> List<T> getAsListViaValueConverter(int index, Class<T> valueType, List<T> defaultValue) {
		throw new IllegalArgumentException(MessageFormat.format("Unsupported return value type: {0}", valueType.getName()));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String itemName, Class<T> valueType, T defaultValue) {
		if (String.class == valueType || CharSequence.class.equals(valueType)) {
			return (T) getAsString(getItemValue(itemName), (String) defaultValue);
		}
		else if (Integer.class == valueType || int.class.equals(valueType)) {
			return (T) getAsInteger(getItemValue(itemName), (Integer) defaultValue);
		}
		else if (Long.class == valueType || long.class.equals(valueType)) {
			return (T) getAsLong(getItemValue(itemName), (Long) defaultValue);
		}
		else if (Double.class == valueType || double.class.equals(valueType)) {
			return (T) getAsDouble(getItemValue(itemName), (Double) defaultValue);
		}
		else if (DominoDateTime.class == valueType) {
			return (T) filterToScalar(getItemValue(itemName), DominoDateTime.class, (DominoDateTime) defaultValue);
		}
		else if (DominoDateRange.class == valueType) {
			return filterToScalar(getItemValue(itemName), valueType, defaultValue);
		}
		else if (DominoTimeType.class == valueType) {
			return filterToScalar(getItemValue(itemName), valueType, defaultValue);
		}
		else if (LocalDate.class == valueType) {
			return (T) getAsJavaDate(getItemValue(itemName), (LocalDate) defaultValue);
		}
		else if (LocalTime.class == valueType) {
			return (T) getAsJavaTime(getItemValue(itemName), (LocalTime) defaultValue);
		}
		else if (OffsetDateTime.class == valueType) {
			return (T) getAsJavaDateTime(getItemValue(itemName), (OffsetDateTime) defaultValue);
		}
		else if (DesignAgent.LastRunInfo.class == valueType) {
		  List<?> val = getItemValue(itemName);
		  return (T) (val.isEmpty() ? null : val.get(0));
		}
		else if (Object.class == valueType || Collection.class.isAssignableFrom(valueType)) {
			//pass raw value
			return (T) getItemValue(itemName);
		}
		else if (PreV3Author.class == valueType) {
      List<?> val = getItemValue(itemName);
      return (T) (val.isEmpty() ? null : val.get(0));
		}
		else if (DominoViewFormat.class == valueType) {
		  List<?> val = getItemValue(itemName);
      return (T) (val.isEmpty() ? null : val.get(0));
		}
		else if (UserData.class == valueType) {
		  List<?> val = getItemValue(itemName);
      return (T) (val.isEmpty() ? null : val.get(0));
		}
		else {
			return getViaValueConverter(itemName, valueType, defaultValue);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getAsList(String itemName, Class<T> valueType, List<T> defaultValue) {
		if (String.class == valueType) {
			return (List<T>) getAsStringList(getItemValue(itemName), (List<String>) defaultValue);
		}
		else if (Integer.class == valueType) {
			return (List<T>) getAsIntegerList(getItemValue(itemName), (List<Integer>) defaultValue);
		}
		else if (Long.class == valueType) {
			return (List<T>) getAsLongList(getItemValue(itemName), (List<Long>) defaultValue);
		}
		else if (Double.class == valueType) {
			return (List<T>) getAsDoubleList(getItemValue(itemName), (List<Double>) defaultValue);
		}
		else if (DominoDateTime.class == valueType) {
			return filterToList(getItemValue(itemName), valueType, defaultValue);
		}
		else if (DominoDateRange.class == valueType) {
			return filterToList(getItemValue(itemName), valueType, defaultValue);
		}
		else if (DominoTimeType.class == valueType) {
			return filterToList(getItemValue(itemName), valueType, defaultValue);
		}
		else if (LocalDate.class == valueType) {
			return (List<T>) getAsJavaDateList(getItemValue(itemName), (List<LocalDate>) defaultValue);
		}
		else if (LocalTime.class == valueType) {
			return (List<T>) getAsJavaTimeList(getItemValue(itemName), (List<LocalTime>) defaultValue);
		}
		else if (OffsetDateTime.class == valueType) {
			return (List<T>) getAsJavaDateTimeList(getItemValue(itemName), (List<OffsetDateTime>) defaultValue);
		}
		else if (Object.class == valueType) {
			//pass raw value
			return (List<T>) getItemValue(itemName);
		}
		else {
			return getAsListViaValueConverter(itemName, valueType, defaultValue);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(int index, Class<T> valueType, T defaultValue) {
		if (String.class == valueType || CharSequence.class.equals(valueType)) {
			return (T) getAsString(getItemValue(index), (String) defaultValue);
		}
		else if (Integer.class == valueType || int.class.equals(valueType)) {
			return (T) getAsInteger(getItemValue(index), (Integer) defaultValue);
		}
		else if (Long.class == valueType || long.class.equals(valueType)) {
			return (T) getAsLong(getItemValue(index), (Long) defaultValue);
		}
		else if (Double.class == valueType || double.class.equals(valueType)) {
			return (T) getAsDouble(getItemValue(index), (Double) defaultValue);
		}
		else if (DominoDateTime.class == valueType) {
			return (T) filterToScalar(getItemValue(index), DominoDateTime.class, (DominoDateTime) defaultValue);
		}
		else if (DominoDateRange.class == valueType) {
			return filterToScalar(getItemValue(index), valueType, defaultValue);
		}
		else if (DominoTimeType.class == valueType) {
			return filterToScalar(getItemValue(index), valueType, defaultValue);
		}
		else if (LocalDate.class == valueType) {
			return (T) getAsJavaDate(getItemValue(index), (LocalDate) defaultValue);
		}
		else if (LocalTime.class == valueType) {
			return (T) getAsJavaTime(getItemValue(index), (LocalTime) defaultValue);
		}
		else if (OffsetDateTime.class == valueType) {
			return (T) getAsJavaDateTime(getItemValue(index), (OffsetDateTime) defaultValue);
		}
		else if (Object.class == valueType || Collection.class.isAssignableFrom(valueType)) {
			//pass raw value
			return (T) getItemValue(index);
		}
		else {
			return getViaValueConverter(index, valueType, defaultValue);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getAsList(int index, Class<T> valueType, List<T> defaultValue) {
		if (String.class == valueType) {
			return (List<T>) getAsStringList(getItemValue(index), (List<String>) defaultValue);
		}
		else if (Integer.class == valueType) {
			return (List<T>) getAsIntegerList(getItemValue(index), (List<Integer>) defaultValue);
		}
		else if (Long.class == valueType) {
			return (List<T>) getAsLongList(getItemValue(index), (List<Long>) defaultValue);
		}
		else if (Double.class == valueType) {
			return (List<T>) getAsDoubleList(getItemValue(index), (List<Double>) defaultValue);
		}
		else if (DominoDateTime.class == valueType) {
			return filterToList(getItemValue(index), valueType, defaultValue);
		}
		else if (DominoDateRange.class == valueType) {
			return filterToList(getItemValue(index), valueType, defaultValue);
		}
		else if (DominoTimeType.class == valueType) {
			return filterToList(getItemValue(index), valueType, defaultValue);
		}
		else if (LocalDate.class == valueType) {
			return (List<T>) getAsJavaDateList(getItemValue(index), (List<LocalDate>) defaultValue);
		}
		else if (LocalTime.class == valueType) {
			return (List<T>) getAsJavaTimeList(getItemValue(index), (List<LocalTime>) defaultValue);
		}
		else if (OffsetDateTime.class == valueType) {
			return (List<T>) getAsJavaDateTimeList(getItemValue(index), (List<OffsetDateTime>) defaultValue);
		}
		else if (Object.class == valueType) {
			//pass raw value
			return (List<T>) getItemValue(index);
		}
		else {
			return getAsListViaValueConverter(index, valueType, defaultValue);
		}
	}
	
	@Override
  public <T> Optional<T> getOptional(String itemName, Class<T> valueType) {
    if(hasItem(itemName)) {
      return Optional.ofNullable(get(itemName, valueType, null));
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public <T> Optional<List<T>> getAsListOptional(String itemName, Class<T> valueType) {
    if(hasItem(itemName)) {
      return Optional.ofNullable(getAsList(itemName, valueType, Collections.emptyList()));
    } else {
      return Optional.empty();
    }
  }

	protected abstract List<?> getItemValue(String itemName);
	protected List<?> getItemValue(int index) {
		return null;
	}

	private String getAsString(List<?> docValues, String defaultValue) {
		List<String> strList = getAsStringList(docValues, null);
		if (strList!=null && !strList.isEmpty()) {
			return strList.get(0);
		}
		else {
			return defaultValue;
		}
	}

	private List<String> getAsStringList(List<?> docValues, List<String> defaultValue) {
		if (docValues != null) {
			List<String> strList = new ArrayList<>(docValues.size());
			for (int i = 0; i < docValues.size(); i++) {
				String currStr = StringUtil.toString(docValues.get(i));
				if (!"".equals(currStr)) { //$NON-NLS-1$
					strList.add(currStr);
				}
			}
			if (!strList.isEmpty()) {
				return strList;
			}
		}
		return defaultValue;
	}

	private Integer getAsInteger(List<?> docValues, Integer defaultValue) {
		List<Integer> intList = getAsIntegerList(docValues, null);
		if (intList!=null && !intList.isEmpty()) {
			return intList.get(0);
		}
		else {
			return defaultValue;
		}
	}

	private List<Integer> getAsIntegerList(List<?> docValues, List<Integer> defaultValue) {
		if (docValues!=null && docValues.size()==1 && "".equals(docValues.get(0))) { //$NON-NLS-1$
			return defaultValue;
		}
		
		if (docValues != null) {
			List<Integer> intList = new ArrayList<>(docValues.size());
			for (int i = 0; i < docValues.size(); i++) {
				if (docValues.get(i) instanceof Number) {
					intList.add(((Number)docValues.get(i)).intValue());
				}
			}
			
			return intList;
		}
		return defaultValue;
	}

	private Double getAsDouble(List<?> docValues, Double defaultValue) {
		List<Double> dblList = getAsDoubleList(docValues, null);
		if (dblList!=null && !dblList.isEmpty()) {
			return dblList.get(0);
		}
		else {
			return defaultValue;
		}
	}

	private List<Double> getAsDoubleList(List<?> docValues, List<Double> defaultValue) {
		if (docValues!=null && docValues.size()==1 && "".equals(docValues.get(0))) { //$NON-NLS-1$
			return defaultValue;
		}
		
		if (docValues != null) {
			List<Double> dblList = new ArrayList<>(docValues.size());
			for (int i = 0; i < docValues.size(); i++) {
				if (docValues.get(i) instanceof Number) {
					dblList.add(((Number)docValues.get(i)).doubleValue());
				}
			}
			
			return dblList;
		}
		return defaultValue;
	}

	private Long getAsLong(List<?> docValues, Long defaultValue) {
		List<Long> lList = getAsLongList(docValues, null);
		if (lList!=null && !lList.isEmpty()) {
			return lList.get(0);
		}
		else {
			return defaultValue;
		}
	}

	private List<Long> getAsLongList(List<?> docValues, List<Long> defaultValue) {
		if (docValues!=null && docValues.size()==1 && "".equals(docValues.get(0))) { //$NON-NLS-1$
			return defaultValue;
		}
		
		if (docValues != null) {
			List<Long> lList = new ArrayList<>(docValues.size());
			for (int i = 0; i < docValues.size(); i++) {
				if (docValues.get(i) instanceof Number) {
					lList.add(((Number)docValues.get(i)).longValue());
				}
			}
			
			return lList;
		}
		return defaultValue;
	}

	/**
	 * Retrieves the named item value a single value of the provided Domino-friendly type.
	 * 
	 * <p>This method is applicable only to types that are emitted from the underlying {@code #getItemValue(String)}
	 * implementation.</p>
	 * 
	 * @param <T> the type to filter to
	 * @param docValues the raw value list from the source
	 * @param clazz a {@link Class} object representing {@code <T>}
	 * @param defaultValue the default value to return when the item is empty
	 * @return the item value as an instance of {@code <T>}
	 */
	private <T> T filterToScalar(List<?> docValues, Class<T> clazz, T defaultValue) {
		List<T> dtList = filterToList(docValues, clazz, null);
		if (dtList!=null && !dtList.isEmpty()) {
			return dtList.get(0);
		}
		else {
			return defaultValue;
		}
	}
	
	/**
	 * Retrieves the named item value as a list filtered to the provided Domino-friendly type.
	 * 
	 * <p>This method is applicable only to types that are emitted from the underlying {@code #getItemValue(String)}
	 * implementation.</p>
	 * 
	 * @param <T> the type to filter to
	 * @param docValues the raw value list from the source
	 * @param clazz a {@link Class} object representing {@code <T>}
	 * @param defaultValue the default value to return when the item is empty
	 * @return the item value as a {@link List} of {@code <T>}
	 */
	private <T> List<T> filterToList(List<?> docValues, Class<T> clazz, List<T> defaultValue) {
		if (docValues!=null && docValues.size()==1 && "".equals(docValues.get(0))) { //$NON-NLS-1$
			return defaultValue;
		}
		if(docValues != null) {
			return docValues.stream()
				.filter(clazz::isInstance)
				.map(clazz::cast)
				.collect(Collectors.toList());
		}
		return defaultValue;
	}

	private OffsetDateTime getAsJavaDateTime(List<?> docValues, OffsetDateTime defaultValue) {
		DominoDateTime dt = filterToScalar(docValues, DominoDateTime.class, null);
		if (dt!=null) {
			return dt.toOffsetDateTime();
		}
		else {
			return defaultValue;
		}
	}

	private LocalTime getAsJavaTime(List<?> docValues, LocalTime defaultValue) {
		DominoDateTime dt = filterToScalar(docValues, DominoDateTime.class, null);
		if (dt!=null) {
			return dt.toLocalTime();
		}
		else {
			return defaultValue;
		}
	}

	private LocalDate getAsJavaDate(List<?> docValues, LocalDate defaultValue) {
		DominoDateTime dt = filterToScalar(docValues, DominoDateTime.class, null);
		if (dt!=null) {
			return dt.toLocalDate();
		}
		else {
			return defaultValue;
		}
	}

	private List<OffsetDateTime> getAsJavaDateTimeList(List<?> docValues, List<OffsetDateTime> defaultValue) {
		List<DominoDateTime>  dtList = filterToList(docValues, DominoDateTime.class, null);
		if (dtList!=null) {
			return dtList
					.stream()
					.map((item) -> {
						return item.toOffsetDateTime();
					})
					.filter((item) -> {
						return item!=null;
					})
					.collect(Collectors.toList());
		}
		else {
			return defaultValue;
		}

	}

	private List<LocalTime> getAsJavaTimeList(List<?> docValues, List<LocalTime> defaultValue) {
		List<DominoDateTime>  dtList = filterToList(docValues, DominoDateTime.class, null);
		if (dtList!=null) {
			return dtList
					.stream()
					.map((item) -> {
						return item.toLocalTime();
					})
					.filter((item) -> {
						return item!=null;
					})
					.collect(Collectors.toList());
		}
		else {
			return defaultValue;
		}
	}

	private List<LocalDate> getAsJavaDateList(List<?> docValues, List<LocalDate> defaultValue) {
		List<DominoDateTime>  dtList = filterToList(docValues, DominoDateTime.class, null);
		if (dtList!=null) {
			return dtList
					.stream()
					.map((item) -> {
						return item.toLocalDate();
					})
					.filter((item) -> {
						return item!=null;
					})
					.collect(Collectors.toList());
		}
		else {
			return defaultValue;
		}
	}

	@Override
	public int getIndexedValueCount() {
		return 0;
	}
	
}
