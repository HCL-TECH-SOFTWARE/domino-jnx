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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.domino.commons.json.AbstractJsonDeserializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;

/**
 * Deserializer for the {@link RawDocSerializer}.
 * 
 * @author Karsten Lehmann
 */
public class RawDocDeserializer extends AbstractJsonDeserializer {
  
  @Override
  public Document fromJson(String json) {
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    
    JsonNode docJson;
    try {
      JsonParser parser = factory.createParser(json);
      docJson = mapper.readTree(parser);
      
    } catch (IOException e) {
      throw new RuntimeException("Error parsing JSON string", e);
    }

    Document doc;
    if (this.targetDocument!=null) {
      doc = this.targetDocument;
    }
    else {
      if (this.targetDatabase == null) {
        throw new IllegalStateException("No target database specified");
      }
      
      doc = this.targetDatabase.createDocument();
    }
    
    JsonNode docClassNode = Objects.requireNonNull(docJson.get(RawDocSerializer.PROP_META_NOTECLASS), //$NON-NLS-1$
        MessageFormat.format("Property {0} is missing", RawDocSerializer.PROP_META_NOTECLASS));
    
    if (docClassNode.isArray()) {
      Set<DocumentClass> docClass =
          StreamSupport
          .stream(docClassNode.spliterator(), false)
          .map(JsonNode::textValue)
          .map(DocumentClass::valueOf)
          .collect(Collectors.toSet());

      if (docClass.isEmpty()) {
        docClass = EnumSet.of(DocumentClass.DATA);
      }
      doc.setDocumentClass(docClass);
    }
        
    JsonNode itemsArr = docJson.get(RawDocSerializer.PROP_ITEMS);
    if (itemsArr!=null && itemsArr.isArray()) {
      Set<String> processedNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      Set<String> cleanedUpItemNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      
      for (JsonNode currItemNode : itemsArr) {
        importItemIntoDoc(doc, currItemNode, processedNames, cleanedUpItemNames);
      }

      if (removeMissingItems) {
        doc.getItemNames().stream()
        .filter(name -> !"Form".equalsIgnoreCase(name)) //$NON-NLS-1$
        .filter(name -> !processedNames.contains(name))
        .filter(name -> !name.startsWith("$")) //$NON-NLS-1$
        .forEach(name -> {
          doc.removeItem(name);
        });
      }
    }

    return doc;
  }

  /**
   * Creates a new item in the document with data read from the
   * <code>itemNode</code>.
   * 
   * @param doc target document
   * @param itemNode JSON node with item data
   * @param retImportedItemNames we add the name if imported items here
   */
  private void importItemIntoDoc(Document doc, JsonNode itemNode, Set<String> retImportedItemNames,
      Set<String> cleanedUpItemNames) {
    String itemName = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_ITEM_NAME);
    String itemTypeStr = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_ITEM_TYPE);
    
    ItemDataType itemType;
    try {
      itemType = ItemDataType.valueOf(itemTypeStr);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid item type: {0}",
          itemTypeStr), e);
    }
    
    if ("$file".equalsIgnoreCase(itemName) && itemType==ItemDataType.TYPE_OBJECT) { //$NON-NLS-1$
      //reimport file attachment with referenced data
      String fileName = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_FILE_NAME);

      doc.removeAttachment(fileName);
      
      long fileSize = Objects
          .requireNonNull(
              itemNode.get(RawDocSerializer.PROP_FILE_SIZE),
              MessageFormat.format("Attribute {0} is missing for item", RawDocSerializer.PROP_FILE_SIZE))
          .asLong();

      String fileCreatedStr = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_FILE_CREATED);
      
      TemporalAccessor dtFileCreated = JsonUtil.tryDateTime(fileCreatedStr);
      if (dtFileCreated==null) {
        dtFileCreated = Instant.now();
      }
      
      String fileLastModifiedStr = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_FILE_LASTMODIFIED);
      
      TemporalAccessor dtLastFileModified = JsonUtil.tryDateTime(fileLastModifiedStr);
      if (dtLastFileModified==null) {
        dtLastFileModified = Instant.now();
      }
      
      String fileDataBase64 = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_FILE_DATA);
      
      try (InputStream in = Base64.getDecoder().wrap((new ByteArrayInputStream(fileDataBase64.getBytes(StandardCharsets.UTF_8))))) {
        doc.attachFile(fileName, dtFileCreated, dtLastFileModified, new IAttachmentProducer() {
          
          @Override
          public void produceAttachment(OutputStream out) throws IOException {
            byte[] buffer = new byte[16384];
            int len;
            
            while ((len = in.read(buffer))>0) {
              out.write(buffer, 0, len);
            }
          }
          
          @Override
          public long getSizeEstimation() {
            return fileSize;
          }
        });
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      
      retImportedItemNames.add("$file"); //$NON-NLS-1$
    }
    else {
      if (!cleanedUpItemNames.contains(itemName)) {
        cleanedUpItemNames.add(itemName);
        doc.removeItem(itemName);
      }
      
      JsonNode flagsNode = Objects.requireNonNull(
          itemNode.get(RawDocSerializer.PROP_ITEM_FLAGS),
          MessageFormat.format("Attribute {0} is missing for item", RawDocSerializer.PROP_ITEM_FLAGS));
      
      Set<ItemFlag> flags = EnumSet.noneOf(ItemFlag.class);
      
      if (flagsNode.isArray()) {
        for (JsonNode currFlagNode : flagsNode) {
          String currFlagStr = currFlagNode.textValue();
          if (!"".equals(currFlagStr)) { //$NON-NLS-1$
            try {
              flags.add(ItemFlag.valueOf(currFlagStr));
            }
            catch (IllegalArgumentException e) {
              throw new IllegalArgumentException(
                  MessageFormat.format("Invalid item flag value: {0}", currFlagStr), e);
            }
          }
        }
      }
      
      //normal item, write binary 
      String itemValueBase64 = getRequiredJsonItem(itemNode, RawDocSerializer.PROP_ITEM_VALUE_RAW);
      ByteBuffer binaryItemValue = ByteBuffer.wrap(Base64.getDecoder().decode(itemValueBase64)).order(ByteOrder.nativeOrder());
      
      doc.appendItemValue(itemName, flags, binaryItemValue);

      retImportedItemNames.add(itemName);
    }
  }
  
  private String getRequiredJsonItem(JsonNode json, String itemName) {
    return Objects
        .requireNonNull(
        json.get(itemName),
        MessageFormat.format("Attribute {0} is missing for item", itemName))
        .asText();
  }
  
}
