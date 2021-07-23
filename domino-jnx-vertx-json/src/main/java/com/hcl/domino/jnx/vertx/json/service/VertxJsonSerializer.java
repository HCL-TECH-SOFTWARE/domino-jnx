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
package com.hcl.domino.jnx.vertx.json.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.json.AbstractJsonSerializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoTimeType;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.exception.EntryNotFoundInIndexException;
import com.hcl.domino.exception.ItemNotFoundException;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.json.JsonSerializer;
import com.hcl.domino.mime.MimeReader.ReadMimeDataType;

import io.vertx.core.json.JsonObject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class VertxJsonSerializer extends AbstractJsonSerializer {

	@Override
	public JsonObject toJson(Document doc) {
		JsonObject result = new JsonObject();
		
		Set<String> handledItems = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		handledItems.addAll(JsonSerializer.DEFAULT_EXCLUDED_ITEMS);
		if(this.includedItemNames != null) {
			handledItems.removeAll(this.includedItemNames);
		}
		if(this.skippedItemNames != null) {
			handledItems.addAll(this.skippedItemNames);
		}
		
		if(this.includeMetadata) {
			JsonObject meta = new JsonObject();
			result.put(PROP_METADATA, meta);
			meta.put(PROP_META_NOTEID, doc.getNoteID());
			meta.put(PROP_META_UNID, doc.getUNID());
			meta.put(PROP_META_CREATED, JsonUtil.toIsoString(doc.getCreated()));
			meta.put(PROP_META_LASTMODIFIED, JsonUtil.toIsoString(doc.getLastModified()));
			meta.put(PROP_META_LASTACCESSED, JsonUtil.toIsoString(doc.getLastAccessed()));
			meta.put(PROP_META_LASTMODIFIEDINFILE, JsonUtil.toIsoString(doc.getModifiedInThisFile()));
			meta.put(PROP_META_ADDEDTOFILE, JsonUtil.toIsoString(doc.getAddedToFile()));
			meta.put(PROP_META_NOTECLASS,
				doc.getDocumentClass().stream()
					.map(DocumentClass::name)
					.collect(Collectors.toList())
			);
		}
		
		doc.forEachItem((item, loop) -> {
			String itemName = item.getName();
			if(itemName != null && !handledItems.contains(itemName)) {
				handledItems.add(itemName);
				
				if(this.includedItemNames != null && !this.includedItemNames.contains(itemName)) {
					// Skip
					return;
				}
				
				ItemDataType type = item.getType();
				if(this.excludedTypes != null && this.excludedTypes.contains(type)) {
					// Skip
					return;
				}
				
				if(isExcludedField(itemName)) {
					return;
				}
				
				String propName = this.lowercaseProperties ? itemName.toLowerCase() : itemName;
				
				if(customProcessors.containsKey(itemName)) {
					result.put(propName, customProcessors.get(itemName).apply(doc, itemName));
					return;
				}
				
				switch(type) {
				case TYPE_NUMBER: {
					double value = item.get(double.class, 0d);
					if(booleanItemNames.contains(propName)) {
						boolean val = AbstractJsonSerializer.matchesBooleanValues(value, booleanTrueValues);
						result.put(propName, val);
					} else {
						result.put(propName, value);
					}
					break;
				}
				case TYPE_NUMBER_RANGE: {
					List<Double> vals = item.getAsList(Double.class, Collections.emptyList());
					if(booleanItemNames.contains(propName)) {
						if(vals.size() == 1) {
							boolean val = AbstractJsonSerializer.matchesBooleanValues(vals.get(0), booleanTrueValues);
							result.put(propName, val);
						} else {
							result.put(propName, false);
						}
					} else {
						result.put(propName, vals);
					}
					break;
				}
				case TYPE_RFC822_TEXT:
				case TYPE_TEXT: {
					String val = item.get(String.class, null);
					if(booleanItemNames.contains(propName)) {
						boolean boolVal = AbstractJsonSerializer.matchesBooleanValues(val, booleanTrueValues);
						result.put(propName, boolVal);
					} else {
						if(val == null) {
							result.putNull(propName);
						} else {
							result.put(propName, val);
						}
					}
					break;
				}
				case TYPE_TEXT_LIST: {
					List<String> vals = item.getAsList(String.class, Collections.emptyList());
					if(booleanItemNames.contains(propName)) {
						if(vals.size() == 1) {
							boolean val = AbstractJsonSerializer.matchesBooleanValues(vals.get(0), booleanTrueValues);
							result.put(propName, val);
						} else {
							result.put(propName, false);
						}
					} else {
						result.put(propName, vals);
					}
					break;
				}
				case TYPE_TIME: {
					DominoTimeType val = item.get(DominoTimeType.class, null);
					writeTimeProperty(result, propName, val);
					break;
				}
				case TYPE_TIME_RANGE: {
					List<DominoTimeType> vals = item.getAsList(DominoTimeType.class, Collections.emptyList());
					if(vals.size() == 1) {
						writeTimeProperty(result, propName, vals.get(0));
					} else {
						result.put(propName,
							vals.stream()
								.map(dt -> {
									if(dt instanceof DominoDateTime) {
										return JsonUtil.toIsoString((DominoDateTime)dt);
									} else {
										switch(dateRangeFormat) {
										case OBJECT:
											JsonObject inner = new JsonObject();
											inner.put(PROP_RANGE_FROM, ((DominoDateRange)dt).getStartDateTime());
											inner.put(PROP_RANGE_TO, ((DominoDateRange)dt).getEndDateTime());
											return result;
										case ISO:
										default:
											return JsonUtil.toIsoString((DominoDateRange)dt);
										}
									}
								})
								.collect(Collectors.toList())
						);
					}
					break;
				}
				case TYPE_COMPOSITE:
					try {
						RichTextHTMLConverter.Builder builder = doc.getParentDatabase()
								.getParentDominoClient()
								.getRichTextHtmlConverter()
								.renderItem(doc, propName);
						if(this.htmlConvertOptions.isEmpty()) {
							builder.option(HtmlConvertOption.XMLCompatibleHTML, "1"); //$NON-NLS-1$
						} else {
							this.htmlConvertOptions.forEach(builder::option);
						}
						HtmlConversionResult conv = builder.convert();
						result.put(propName, conv.getHtml());
					} catch(ItemNotFoundException | EntryNotFoundInIndexException e) {
						// Occurs with design notes
						result.put(propName, ""); //$NON-NLS-1$
					} catch(DominoException e) {
						switch(e.getId()) {
						case 14941:
						case 14944:
							// Un-messaged error codes observed with design notes
							result.put(propName, ""); //$NON-NLS-1$
							break;
						default:
							throw e;
						}
					}
					break;
				case TYPE_MIME_PART:
					// TODO read inline?
					// TODO rationalize multiple body types
					MimeMessage mime = doc.getParentDatabase()
						.getParentDominoClient()
						.getMimeReader()
						.readMIME(doc, propName, EnumSet.of(ReadMimeDataType.MIMEHEADERS));
					String content;
					try(InputStream is = mime.getInputStream()) {
						content = IOUtils.toString(is, StandardCharsets.UTF_8);
					} catch (IOException | MessagingException e) {
						throw new RuntimeException(e);
					}
					if(content == null) {
						result.putNull(propName);
					} else {
						result.put(propName, content.toString());
					}
					break;
				case TYPE_HTML:
					// TODO this is probably a specialized value, but the underlying API could handle converting to string
					break;
				case TYPE_USERDATA:
					// TODO Base64? Custom adapters?
					break;
				case TYPE_FORMULA:
				case TYPE_ERROR:
				case TYPE_NOTEREF_LIST:
					// TODO convert to string?
					break;
				case TYPE_ACTION:
				case TYPE_ASSISTANT_INFO:
				case TYPE_CALENDAR_FORMAT:
				case TYPE_COLLATION:
				case TYPE_HIGHLIGHTS:
				case TYPE_ICON:
				case TYPE_INVALID_OR_UNKNOWN:
				case TYPE_LSOBJECT:
				case TYPE_NOTELINK_LIST:
				case TYPE_OBJECT:
				case TYPE_QUERY:
				case TYPE_SCHED_LIST:
				case TYPE_SEAL:
				case TYPE_SEAL2:
				case TYPE_SEALDATA:
				case TYPE_SEAL_LIST:
				case TYPE_SIGNATURE:
				case TYPE_UNAVAILABLE:
				case TYPE_USERID:
				case TYPE_VIEWMAP_DATASET:
				case TYPE_VIEWMAP_LAYOUT:
				case TYPE_VIEW_FORMAT:
				case TYPE_WORKSHEET_DATA:
				default:
					break;
				
				}
			}
		});
		
		return result;
	}

	private void writeTimeProperty(JsonObject result, String propName, DominoTimeType val) {
		if(val == null) {
			result.putNull(propName);
		} else {
			if(val instanceof DominoDateTime) {
				result.put(propName, JsonUtil.toIsoString((DominoDateTime)val));
			} else {
				switch(this.dateRangeFormat) {
				case OBJECT:
					JsonObject inner = new JsonObject();
					inner.put(PROP_RANGE_FROM, JsonUtil.toIsoString(((DominoDateRange)val).getStartDateTime()));
					inner.put(PROP_RANGE_TO, JsonUtil.toIsoString(((DominoDateRange)val).getEndDateTime()));
					result.put(propName, inner);
					break;
				case ISO:
				default:
					result.put(propName, JsonUtil.toIsoString((DominoDateRange)val));
				}
			}
		}
	}
}
