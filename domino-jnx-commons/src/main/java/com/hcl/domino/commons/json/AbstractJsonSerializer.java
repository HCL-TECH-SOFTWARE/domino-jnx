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
package com.hcl.domino.commons.json;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.json.DateRangeFormat;
import com.hcl.domino.json.JsonSerializer;

public abstract class AbstractJsonSerializer implements JsonSerializer {

  protected Collection<String> skippedItemNames;
  protected Collection<String> includedItemNames;
  protected Collection<ItemDataType> excludedTypes;

  protected boolean lowercaseProperties;
  protected boolean includeMetadata;
  protected boolean metaOnly;

  protected Collection<String> booleanItemNames = Collections.emptySet();

  protected Collection<Object> booleanTrueValues = Collections.emptySet();

  protected DateRangeFormat dateRangeFormat = DateRangeFormat.ISO;

  /**
   * This map contains values set by
   * {@link #richTextConvertOption(HtmlConvertOption, String)}.
   * When it is empty, the serializer should use the default behavior of
   * {@link HtmlConvertOption#XMLCompatibleHTML XMLCompatibleHTML=1}.
   * 
   * @since 1.0.27
   */
  protected final Map<HtmlConvertOption, String> htmlConvertOptions = new LinkedHashMap<>();

  /**
   * This map contains processors set by
   * {@link #customProcessor(String, BiFunction)}.
   * 
   * @since 1.0.28
   */
  protected final Map<String, BiFunction<Document, String, Object>> customProcessors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  @Override
  public JsonSerializer booleanItemNames(final Collection<String> booleanItemNames) {
    this.booleanItemNames = JsonUtil.toInsensitiveSet(booleanItemNames);
    return this;
  }

  @Override
  public JsonSerializer booleanTrueValues(final Collection<Object> trueValues) {
    this.booleanTrueValues = trueValues == null ? Collections.emptySet() : new HashSet<>(trueValues);
    return this;
  }

  @Override
  public JsonSerializer customProcessor(final String propName, final BiFunction<Document, String, Object> processor) {
    Objects.requireNonNull(propName, "propName cannot be null");
    Objects.requireNonNull(processor, "processor cannot be null");
    if (StringUtil.isEmpty(propName)) {
      throw new IllegalArgumentException("propName cannot be null");
    }
    this.customProcessors.put(propName, processor);
    return this;
  }

  @Override
  public JsonSerializer dateRangeFormat(final DateRangeFormat format) {
    this.dateRangeFormat = format == null ? DateRangeFormat.ISO : format;
    return this;
  }

  @Override
  public JsonSerializer excludeItems(final Collection<String> skippedItemNames) {
    if (skippedItemNames == null) {
      this.skippedItemNames = Collections.emptySet();
    } else {
      this.skippedItemNames = JsonUtil.toInsensitiveSet(skippedItemNames);
    }
    return this;
  }

  @Override
  public JsonSerializer excludeTypes(final Collection<ItemDataType> excludedTypes) {
    if (excludedTypes == null) {
      this.excludedTypes = Collections.emptySet();
    } else {
      this.excludedTypes = EnumSet.copyOf(excludedTypes);
    }
    return this;
  }

  @Override
  public JsonSerializer includeItems(final Collection<String> includedItemNames) {
    if (includedItemNames == null) {
      this.includedItemNames = Collections.emptySet();
    } else {
      this.includedItemNames = JsonUtil.toInsensitiveSet(includedItemNames);
    }
    return this;
  }

  @Override
  public JsonSerializer includeMetadata(final boolean includeMetadata) {
    this.includeMetadata = includeMetadata;
    return this;
  }

  @Override
  public JsonSerializer lowercaseProperties(final boolean lowercaseProperties) {
    this.lowercaseProperties = lowercaseProperties;
    return this;
  }

  @Override
  public JsonSerializer richTextConvertOption(final HtmlConvertOption option, final String value) {
    this.htmlConvertOptions.put(option, value);
    return this;
  }
  
  @Override
  public JsonSerializer metaOnly(boolean metaOnly) {
    this.metaOnly = metaOnly;
    return this;
  }

  // *******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

  /**
   * Excludes some known-skippable items based on their name patterns, such as
   * stored forms.
   * 
   * @param itemName the name of the item to check
   * @return whether the item should be excluded from export
   */
  public static boolean isExcludedField(final String itemName) {
    if (itemName == null || itemName.isEmpty() || itemName.toLowerCase().endsWith("_storedform") || itemName.toLowerCase().endsWith("_storedsubform")) { //$NON-NLS-1$ //$NON-NLS-2$
      return true;
    }
    return false;
  }

  public static boolean matchesBooleanValues(final Object value, final Collection<Object> booleanTrueValues) {
    if (booleanTrueValues == null || booleanTrueValues.isEmpty()) {
      return false;
    }
    for (final Object trueValue : booleanTrueValues) {
      if (value == trueValue) {
        return true;
      } else if (value instanceof Number && trueValue instanceof Number) {
        // All Domino values will be Double, but provided values may not be
        if (((Number) value).doubleValue() == ((Number) trueValue).doubleValue()) {
          return true;
        }
      } else if (value instanceof CharSequence && trueValue instanceof Character) {
        // Fudge the difference with chars and single-length strings
        if (((CharSequence) trueValue).length() == 1 && ((CharSequence) trueValue).charAt(0) == (char) trueValue) {
          return true;
        }
      } else if (Objects.equals(value, trueValue)) {
        return true;
      }
    }
    return false;
  }
}
