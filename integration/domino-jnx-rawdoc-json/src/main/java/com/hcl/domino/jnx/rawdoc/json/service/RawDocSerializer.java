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
package com.hcl.domino.jnx.rawdoc.json.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hcl.domino.commons.json.AbstractJsonSerializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Attachment.IDataCallback;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoOriginatorId;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;

/**
 * JSON serializer for {@link Document} that provides full fidelity
 * during reimport, but is mostly exporting raw (base64 encoded) structure data that
 * is hard to read.
 * 
 * @author Karsten Lehmann
 */
public class RawDocSerializer extends AbstractJsonSerializer {
  public static final String PROP_ITEMS = "items"; //$NON-NLS-1$
  public static final String PROP_META_SIGNER = "signer"; //$NON-NLS-1$
  public static final String PROP_ITEM_NAME = "name"; //$NON-NLS-1$
  public static final String PROP_ITEM_SEQUENCENUMBER = PROP_META_SEQUENCENUMBER;
  public static final String PROP_ITEM_TYPE = "type"; //$NON-NLS-1$
  public static final String PROP_ITEM_FLAGS = "flags"; //$NON-NLS-1$
  public static final String PROP_ITEM_VALUE_RAW = "value_raw"; //$NON-NLS-1$
  public static final String PROP_ITEM_OBJECTTYPE = "objecttype"; //$NON-NLS-1$
  
  public static final String PROP_FILE_DATA = "filedata"; //$NON-NLS-1$
  public static final String PROP_FILE_LASTMODIFIED = "filelastmodified"; //$NON-NLS-1$
  public static final String PROP_FILE_CREATED = "filecreated"; //$NON-NLS-1$
  public static final String PROP_FILE_COMPRESSION = "filecompression"; //$NON-NLS-1$
  public static final String PROP_FILE_SIZE = "filesize"; //$NON-NLS-1$
  public static final String PROP_FILE_NAME = "filename"; //$NON-NLS-1$
  
  private Optional<JsonNode> toJson(ObjectMapper mapper, Item item) {
    String itemName = item.getName();
    
    if ("$updatedby".equalsIgnoreCase(itemName) || //$NON-NLS-1$
        "$revisions".equalsIgnoreCase(itemName)) { //$NON-NLS-1$
      return Optional.empty();
    }
    
    byte[] rawItemValue = item.getAdapter(byte[].class);
    if (rawItemValue!=null) {
      ObjectNode itemNode = mapper.createObjectNode();
      itemNode.put(PROP_ITEM_NAME, itemName.toLowerCase());
      
      itemNode.put(PROP_ITEM_SEQUENCENUMBER, item.getSequenceNumber());
      
      ItemDataType itemType = item.getType();
      JsonNode itemTypeJson = mapper.valueToTree(itemType);
      itemNode.set(PROP_ITEM_TYPE, itemTypeJson);
      
      Set<ItemFlag> itemFlags = item.getFlags();
      JsonNode itemFlagsJson = mapper.valueToTree(itemFlags);
      itemNode.set(PROP_ITEM_FLAGS, itemFlagsJson);
      
      String itemValueBase64 = Base64.getEncoder().encodeToString(rawItemValue);
      itemNode.put(PROP_ITEM_VALUE_RAW, itemValueBase64);
      
      if (item.getType() == ItemDataType.TYPE_OBJECT) {
        if ("$file".equalsIgnoreCase(item.getName())) { //$NON-NLS-1$
          Attachment att = item.getValue()
              .stream()
              .filter(Attachment.class::isInstance)
              .map(Attachment.class::cast)
              .findFirst().orElse(null);
          
          if (att!=null) {
            itemNode.put(PROP_FILE_NAME, att.getFileName());
            itemNode.put(PROP_FILE_SIZE, att.getFileSize());
            itemNode.put(PROP_FILE_COMPRESSION, att.getCompression().name());
            
            DominoDateTime dtCreated = att.getFileCreated();
            if (dtCreated!=null) {
              String createdIsoStr = JsonUtil.toIsoString(dtCreated);
              itemNode.put(PROP_FILE_CREATED, createdIsoStr);
            }
            DominoDateTime dtLastModified = att.getFileModified();
            if (dtLastModified!=null) {
              String modifiedIsoStr = JsonUtil.toIsoString(dtLastModified);
              itemNode.put(PROP_FILE_LASTMODIFIED, modifiedIsoStr);
            }
            
            //TODO uses much heap space; think about an alternative interface for JsonSerializer that writes JSON into a stream
            ByteArrayOutputStream attOut = new ByteArrayOutputStream();
            try (OutputStream base64AttOut = Base64.getEncoder().wrap(attOut);) {
              
              att.readData(new IDataCallback() {
                
                @Override
                public Action read(byte[] data) {
                  try {
                    base64AttOut.write(data);
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                  return Action.Continue;
                }
              });
            } catch (IOException e1) {
              throw new UncheckedIOException(e1);
            }
            
            itemNode.put(PROP_FILE_DATA, new String(attOut.toByteArray(), StandardCharsets.UTF_8));
          }
          else {
            //skip this entire item
            return Optional.empty();
          }
        }
        else {
          //ignore other TYPE_OBJECT items for now; does probably not make much
          //sense to serialize them, e.g. an agent's run info
          return Optional.empty();
        }
      }
      
      return Optional.of(itemNode);
    }
    else {
      return Optional.empty();
    }
  }
  
  @Override
  public JsonNode toJson(Document doc) {
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode docNode = mapper.createObjectNode();
    
    //write optional metadata about the document
    if (includeMetadata) {
      ObjectNode metaNode = mapper.createObjectNode();
      docNode.set(PROP_METADATA, metaNode);
      
      metaNode.put(PROP_META_NOTEID, doc.getNoteID());
      metaNode.put(PROP_META_UNID, doc.getUNID());
      
      DominoOriginatorId docOID = doc.getOID();
      metaNode.put(PROP_META_SEQUENCENUMBER, docOID.getSequence());
      
      DominoDateTime seqTime = docOID.getSequenceTime();
      String seqTimeIsoStr = JsonUtil.toIsoString(seqTime);
      metaNode.put(PROP_META_SEQUENCETIME, seqTimeIsoStr);
      
      DominoDateTime created = doc.getCreated();
      if (created!=null) {
        String createdIsoStr = JsonUtil.toIsoString(created);
        metaNode.put(PROP_META_CREATED, createdIsoStr);
      }
      
      DominoDateTime lastModified = doc.getLastModified();
      if (lastModified!=null) {
        String lastModifiedIsoStr = JsonUtil.toIsoString(lastModified);
        metaNode.put(PROP_META_LASTMODIFIED, lastModifiedIsoStr);
      }
      
      DominoDateTime lastAccessed = doc.getLastAccessed();
      if (lastAccessed!=null) {
        String lastAccessedIsoStr = JsonUtil.toIsoString(lastAccessed);
        metaNode.put(PROP_META_LASTACCESSED, lastAccessedIsoStr);
      }

      DominoDateTime addedToFile = doc.getAddedToFile();
      if (addedToFile!=null) {
        String addedToFileIsoStr = JsonUtil.toIsoString(addedToFile);
        metaNode.put(PROP_META_ADDEDTOFILE, addedToFileIsoStr);
      }

      if (doc.isSigned()) {
        String signer = doc.getSigner();
        if (!StringUtil.isEmpty(signer)) {
          metaNode.put( PROP_META_SIGNER, doc.getSigner());
        }
      }
    }
    
    //write required document content to recreate it from JSON
    
    Set<DocumentClass> docClass = doc.getDocumentClass();
    JsonNode docClassJson = mapper.valueToTree(docClass);
    docNode.set(PROP_META_NOTECLASS, docClassJson);

    ArrayNode itemsArr = mapper.createArrayNode();
    docNode.set(PROP_ITEMS, itemsArr);
    
    final Set<String> handledItems = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    //we don't exclude JsonSerializer.DEFAULT_EXCLUDED_ITEMS here which contains $fonts, because that one seems to make sense to be included here

    if (this.includedItemNames != null) {
      handledItems.removeAll(this.includedItemNames);
    }
    if (this.skippedItemNames != null) {
      handledItems.addAll(this.skippedItemNames);
    }

    doc.allItems()
    .filter((item) -> {
      String itemName = item.getName();
      return !handledItems.contains(item.getName()) && !AbstractJsonSerializer.isExcludedField(itemName);
    })
    .forEach((item) -> {
      final ItemDataType type = item.getType();
      if (this.excludedTypes != null && this.excludedTypes.contains(type)) {
        return;
      }
      
      Optional<JsonNode> itemJson = toJson(mapper, item);
      if (itemJson.isPresent()) {
        itemsArr.add(itemJson.get());
      }
    });

    return docNode;
  }

  @Override
  public Object toJson(Object value) {
    Objects.requireNonNull(value);
    
    if (value instanceof Document) {
      return toJson((Document)value);
    }
    else {
      throw new IllegalArgumentException(MessageFormat.format("Unsupported value type: {0}", value.getClass().getName()));
    }
  }

}
