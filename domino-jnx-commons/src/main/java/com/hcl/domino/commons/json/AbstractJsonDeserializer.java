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
package com.hcl.domino.commons.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.json.JsonDeserializer;

public abstract class AbstractJsonDeserializer implements JsonDeserializer {
	protected Document targetDocument;
	protected Database targetDatabase;
	protected Collection<String> dateTimeItems = Collections.emptySet();
	protected boolean removeMissingItems;
	protected Object trueValue;
	protected Object falseValue;
	/**
	 * @since 1.0.27
	 */
	protected boolean detectDateTime;
	protected final Map<String, CustomProcessor> customProcessors = new HashMap<>();

	@Override
	public JsonDeserializer target(Database database) {
		this.targetDatabase = database;
		return this;
	}

	@Override
	public JsonDeserializer target(Document document) {
		this.targetDocument = document;
		return this;
	}

	@Override
	public JsonDeserializer dateTimeItems(Collection<String> dateTimeItems) {
		this.dateTimeItems = JsonUtil.toInsensitiveSet(dateTimeItems);
		return this;
	}

	@Override
	public JsonDeserializer removeMissingItems(boolean removeMissingItems) {
		this.removeMissingItems = removeMissingItems;
		return this;
	}
	
	@Override
	public JsonDeserializer booleanValues(Object trueValue, Object falseValue) {
		this.trueValue = trueValue;
		this.falseValue = falseValue;
		return this;
	}
	
	@Override
	public JsonDeserializer detectDateTime(boolean detectDateTime) {
		this.detectDateTime = detectDateTime;
		return this;
	}
	
	@Override
	public JsonDeserializer customProcessor(String propName, CustomProcessor processor) {
		Objects.requireNonNull(propName, "propName cannot be null");
		Objects.requireNonNull(processor, "processor cannot be null");
		if(StringUtil.isEmpty(propName)) {
			throw new IllegalArgumentException("propName cannot be null");
		}
		this.customProcessors.put(propName, processor);
		return this;
	}
}
