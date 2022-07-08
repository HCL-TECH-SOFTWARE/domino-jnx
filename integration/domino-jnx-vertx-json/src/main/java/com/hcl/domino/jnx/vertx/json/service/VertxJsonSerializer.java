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
package com.hcl.domino.jnx.vertx.json.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.design.DefaultActionBar;
import com.hcl.domino.commons.design.action.DefaultActionBarAction;
import com.hcl.domino.commons.json.AbstractJsonSerializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoOriginatorId;
import com.hcl.domino.data.DominoTimeType;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.GenericFormOrSubform;
import com.hcl.domino.design.JavaAgentOrLibrary;
import com.hcl.domino.design.Outline;
import com.hcl.domino.design.View;
import com.hcl.domino.design.agent.DesignImportedJavaAgent;
import com.hcl.domino.exception.EntryNotFoundInIndexException;
import com.hcl.domino.exception.ItemNotFoundException;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.jnx.vertx.json.DefaultActionBarActionMixIn;
import com.hcl.domino.jnx.vertx.json.DefaultActionBarMixIn;
import com.hcl.domino.jnx.vertx.json.DesignAgentMixIn;
import com.hcl.domino.jnx.vertx.json.DesignElementMixIn;
import com.hcl.domino.jnx.vertx.json.FolderMixIn;
import com.hcl.domino.jnx.vertx.json.GenericFormOrSubformMixIn;
import com.hcl.domino.jnx.vertx.json.ImportedJavaAgentContentMixIn;
import com.hcl.domino.jnx.vertx.json.JavaAgentOrLibraryContentMixIn;
import com.hcl.domino.jnx.vertx.json.JnxTypesModule;
import com.hcl.domino.jnx.vertx.json.MemoryStructureMixIn;
import com.hcl.domino.jnx.vertx.json.OutlineMixIn;
import com.hcl.domino.jnx.vertx.json.ResizableMemoryStructureMixIn;
import com.hcl.domino.jnx.vertx.json.RichTextRecordMixIn;
import com.hcl.domino.jnx.vertx.json.ViewMixIn;
import com.hcl.domino.json.JsonSerializer;
import com.hcl.domino.mime.MimeReader.ReadMimeDataType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

import io.vertx.core.json.JsonObject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class VertxJsonSerializer extends AbstractJsonSerializer {
  
  public VertxJsonSerializer() {
    //add custom mixin classes for json serialization
    io.vertx.core.json.jackson.DatabindCodec.prettyMapper().setSerializationInclusion(Include.NON_EMPTY);
    io.vertx.core.json.jackson.DatabindCodec.mapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(DesignElement.class, DesignElementMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(Outline.class, OutlineMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(View.class, ViewMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(Folder.class, FolderMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(GenericFormOrSubform.class, GenericFormOrSubformMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(MemoryStructure.class, MemoryStructureMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(RichTextRecord.class, RichTextRecordMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(ResizableMemoryStructure.class, ResizableMemoryStructureMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(DefaultActionBar.class, DefaultActionBarMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(DefaultActionBarAction.class, DefaultActionBarActionMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(JavaAgentOrLibrary.class, JavaAgentOrLibraryContentMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(DesignAgent.class, DesignAgentMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().addMixIn(DesignImportedJavaAgent.class, ImportedJavaAgentContentMixIn.class);
    io.vertx.core.json.jackson.DatabindCodec.mapper().registerModule(new Jdk8Module());
    io.vertx.core.json.jackson.DatabindCodec.mapper().registerModule(new JavaTimeModule());
    io.vertx.core.json.jackson.DatabindCodec.mapper().registerModule(new JnxTypesModule());
  }

  /**
   * Json serialization of Objects with vertx ObjectMapper
   *
   * @since 1.0.32
   */
  @Override
  public JsonObject toJson(final Object value) {
	  return JsonObject.mapFrom(value);
  }
  
  @Override
  public JsonObject toJson(final Document doc) {
    final JsonObject result = new JsonObject();

    final Set<String> handledItems = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    handledItems.addAll(JsonSerializer.DEFAULT_EXCLUDED_ITEMS);
    if (this.includedItemNames != null) {
      handledItems.removeAll(this.includedItemNames);
    }
    if (this.skippedItemNames != null) {
      handledItems.addAll(this.skippedItemNames);
    }

    if (this.includeMetadata) {
      final JsonObject meta = new JsonObject();
      result.put(JsonSerializer.PROP_METADATA, meta);
      meta.put(JsonSerializer.PROP_META_NOTEID, doc.getNoteID());
      meta.put(JsonSerializer.PROP_META_UNID, doc.getUNID());
      meta.put(JsonSerializer.PROP_META_CREATED, JsonUtil.toIsoString(doc.getCreated()));
      meta.put(JsonSerializer.PROP_META_LASTMODIFIED, JsonUtil.toIsoString(doc.getLastModified()));
      meta.put(JsonSerializer.PROP_META_LASTACCESSED, JsonUtil.toIsoString(doc.getLastAccessed()));
      meta.put(JsonSerializer.PROP_META_LASTMODIFIEDINFILE, JsonUtil.toIsoString(doc.getModifiedInThisFile()));
      meta.put(JsonSerializer.PROP_META_ADDEDTOFILE, JsonUtil.toIsoString(doc.getAddedToFile()));
      meta.put(JsonSerializer.PROP_META_NOTECLASS,
          doc.getDocumentClass().stream()
              .map(DocumentClass::name)
              .collect(Collectors.toList()));
      meta.put(JsonSerializer.PROP_META_UNREAD, doc.isUnread());
      {
        DominoOriginatorId oid = doc.getOID();
        int[] seqTime = oid.getSequenceTime().getAdapter(int[].class);
        
        try(Formatter formatter = new Formatter()) {
          formatter.format("%08x", oid.getSequence()); //$NON-NLS-1$
          formatter.format("%08x", seqTime[0]); //$NON-NLS-1$
          formatter.format("%08x", seqTime[1]); //$NON-NLS-1$
          meta.put(JsonSerializer.PROP_META_REVISION, formatter.toString().toUpperCase());
        }
      }
      
      if(doc.isResponse()) {
        meta.put(JsonSerializer.PROP_META_PARENTUNID, doc.getParentDocumentUNID());
      }
      Optional<String> threadId = doc.getThreadID();
      if(threadId.isPresent()) {
        meta.put(JsonSerializer.PROP_META_THREADID, threadId.get());
      }
    }

    doc.forEachItem((item, loop) -> {
      final String itemName = item.getName();
      if (itemName != null && !handledItems.contains(itemName)) {
        handledItems.add(itemName);

        if (this.includedItemNames != null && !this.includedItemNames.contains(itemName)) {
          // Skip
          return;
        }

        final ItemDataType type = item.getType();
        if ((this.excludedTypes != null && this.excludedTypes.contains(type)) || AbstractJsonSerializer.isExcludedField(itemName)) {
          return;
        }

        final String propName = this.lowercaseProperties ? itemName.toLowerCase() : itemName;

        if (this.customProcessors.containsKey(itemName)) {
          result.put(propName, this.customProcessors.get(itemName).apply(doc, itemName));
          return;
        }

        switch (type) {
          case TYPE_NUMBER: {
            final double value = item.get(double.class, 0d);
            if (this.booleanItemNames.contains(propName)) {
              final boolean val = AbstractJsonSerializer.matchesBooleanValues(value, this.booleanTrueValues);
              result.put(propName, val);
            } else {
              result.put(propName, value);
            }
            break;
          }
          case TYPE_NUMBER_RANGE: {
            final List<Double> vals = item.getAsList(Double.class, Collections.emptyList());
            if (this.booleanItemNames.contains(propName)) {
              if (vals.size() == 1) {
                final boolean val = AbstractJsonSerializer.matchesBooleanValues(vals.get(0), this.booleanTrueValues);
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
            final String val = item.get(String.class, null);
            if (this.booleanItemNames.contains(propName)) {
              final boolean boolVal = AbstractJsonSerializer.matchesBooleanValues(val, this.booleanTrueValues);
              result.put(propName, boolVal);
            } else {
              if (val == null) {
                result.putNull(propName);
              } else {
                result.put(propName, val);
              }
            }
            break;
          }
          case TYPE_TEXT_LIST: {
            final List<String> vals = item.getAsList(String.class, Collections.emptyList());
            if (this.booleanItemNames.contains(propName)) {
              if (vals.size() == 1) {
                final boolean val = AbstractJsonSerializer.matchesBooleanValues(vals.get(0), this.booleanTrueValues);
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
            final DominoTimeType val = item.get(DominoTimeType.class, null);
            this.writeTimeProperty(result, propName, val);
            break;
          }
          case TYPE_TIME_RANGE: {
            final List<DominoTimeType> vals = item.getAsList(DominoTimeType.class, Collections.emptyList());
            if (vals.size() == 1) {
              this.writeTimeProperty(result, propName, vals.get(0));
            } else {
              result.put(propName,
                  vals.stream()
                      .map(dt -> {
                        if (dt instanceof DominoDateTime) {
                          return JsonUtil.toIsoString((DominoDateTime) dt);
                        } else {
                          switch (this.dateRangeFormat) {
                            case OBJECT:
                              final JsonObject inner = new JsonObject();
                              inner.put(JsonSerializer.PROP_RANGE_FROM, ((DominoDateRange) dt).getStartDateTime());
                              inner.put(JsonSerializer.PROP_RANGE_TO, ((DominoDateRange) dt).getEndDateTime());
                              return result;
                            case ISO:
                            default:
                              return JsonUtil.toIsoString((DominoDateRange) dt);
                          }
                        }
                      })
                      .collect(Collectors.toList()));
            }
            break;
          }
          case TYPE_COMPOSITE:
            try {
              final RichTextHTMLConverter.Builder builder = doc.getParentDatabase()
                  .getParentDominoClient()
                  .getRichTextHtmlConverter()
                  .renderItem(doc, propName);
              if (this.htmlConvertOptions.isEmpty()) {
                builder.option(HtmlConvertOption.XMLCompatibleHTML, "1"); //$NON-NLS-1$
              } else {
                this.htmlConvertOptions.forEach(builder::option);
              }
              final HtmlConversionResult conv = builder.convert();
              result.put(propName, conv.getHtml());
            } catch (ItemNotFoundException | EntryNotFoundInIndexException e) {
              // Occurs with design notes
              result.put(propName, ""); //$NON-NLS-1$
            } catch (final DominoException e) {
              switch (e.getId()) {
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
            final MimeMessage mime = doc.getParentDatabase()
                .getParentDominoClient()
                .getMimeReader()
                .readMIME(doc, propName, EnumSet.of(ReadMimeDataType.MIMEHEADERS));
            String content;
            try (InputStream is = mime.getInputStream()) {
              content = IOUtils.toString(is, StandardCharsets.UTF_8);
            } catch (IOException | MessagingException e) {
              throw new RuntimeException(e);
            }
            if (content == null) {
              result.putNull(propName);
            } else {
              result.put(propName, content.toString());
            }
            break;
          case TYPE_HTML:
            // TODO this is probably a specialized value, but the underlying API could
            // handle converting to string
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

  private void writeTimeProperty(final JsonObject result, final String propName, final DominoTimeType val) {
    if (val == null) {
      result.putNull(propName);
    } else {
      if (val instanceof DominoDateTime) {
        result.put(propName, JsonUtil.toIsoString((DominoDateTime) val));
      } else {
        switch (this.dateRangeFormat) {
          case OBJECT:
            final JsonObject inner = new JsonObject();
            inner.put(JsonSerializer.PROP_RANGE_FROM, JsonUtil.toIsoString(((DominoDateRange) val).getStartDateTime()));
            inner.put(JsonSerializer.PROP_RANGE_TO, JsonUtil.toIsoString(((DominoDateRange) val).getEndDateTime()));
            result.put(propName, inner);
            break;
          case ISO:
          default:
            result.put(propName, JsonUtil.toIsoString((DominoDateRange) val));
        }
      }
    }
  }
}
