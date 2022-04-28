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
package it.com.hcl.domino.test.json;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.commons.json.AbstractJsonSerializer;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.jnx.rawdoc.json.service.RawDocDeserializer;
import com.hcl.domino.jnx.rawdoc.json.service.RawDocSerializer;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle;
import com.hcl.domino.richtext.TextStyle.Justify;
import com.hcl.domino.richtext.records.CDQueryHeader;
import com.hcl.domino.richtext.structures.FontStyle;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

/**
 * Tests doc / JSON roundtripping with full fidelity as alternative
 * to binary DXL.
 * 
 * @author Karsten Lehmann
 */
public class TestRawDocSerializer extends AbstractNotesRuntimeTest {
  private static final String TEST_IMAGE_PATH = "/images/test-png-large.png";

  @Test
  public void testSerializer() throws Exception {
    withTempDb((db) -> {
      DominoClient client = getClient();
      
      Document docOrig = db.createDocument();
      docOrig.setDocumentClass(EnumSet.of(DocumentClass.VIEW));
      
      docOrig.replaceItemValue("textitem", "abc\ndef"); //$NON-NLS-1$
      docOrig.replaceItemValue("textitem_nosummary", EnumSet.noneOf(ItemFlag.class), "def"); //$NON-NLS-1$
      docOrig.replaceItemValue("textitem_signed", EnumSet.of(ItemFlag.SIGNED), "ghi"); //$NON-NLS-1$
      docOrig.replaceItemValue("textitem_signedprotected", EnumSet.of(ItemFlag.SIGNED, ItemFlag.PROTECTED), "ghi"); //$NON-NLS-1$
      
      docOrig.replaceItemValue("numberitem", 123); //$NON-NLS-1$
      docOrig.replaceItemValue("numberitem_nosummary", EnumSet.noneOf(ItemFlag.class), 123); //$NON-NLS-1$
      docOrig.replaceItemValue("numberitem_signed", EnumSet.of(ItemFlag.SIGNED), 456); //$NON-NLS-1$
      
      docOrig.replaceItemValue("datetimeitem", Instant.now()); //$NON-NLS-1$
      docOrig.replaceItemValue("datetimeitem_nosummary", EnumSet.noneOf(ItemFlag.class), Instant.now()); //$NON-NLS-1$
      docOrig.replaceItemValue("datetimeitem_signed", EnumSet.of(ItemFlag.SIGNED), Instant.now()); //$NON-NLS-1$
      
      docOrig.replaceItemValue("textlistitem", Arrays.asList("str1\nline2", "str2")); //$NON-NLS-1$
      docOrig.replaceItemValue("textlistitem_nosummary", EnumSet.noneOf(ItemFlag.class), Arrays.asList("str1", "str2")); //$NON-NLS-1$
      docOrig.replaceItemValue("textlistitem_signed", EnumSet.of(ItemFlag.SIGNED), Arrays.asList("str1", "str2")); //$NON-NLS-1$
      
      docOrig.replaceItemValue("namesItem", EnumSet.of(ItemFlag.NAMES), client.getEffectiveUserName()); //$NON-NLS-1$
      docOrig.replaceItemValue("authorListItem", //$NON-NLS-1$
          EnumSet.of(ItemFlag.NAMES, ItemFlag.READWRITERS), Arrays.asList(client.getEffectiveUserName(), "CN=Test User/O=MyOrg"));
      
      docOrig.attachFile("myfilename.txt", Instant.now(), //$NON-NLS-1$
          Instant.now(),
          new IAttachmentProducer() {

            @Override
            public long getSizeEstimation() {
              return -1;
            }

            @Override
            public void produceAttachment(OutputStream out) throws IOException {
              out.write("HELLO WORLD! :-)".getBytes(StandardCharsets.UTF_8));
            }});
      
      try (RichTextWriter rtWriter = docOrig.createRichTextItem("Body");
          InputStream imageIn = getClass().getResourceAsStream(TEST_IMAGE_PATH)) {
        FontStyle fontStyle = rtWriter.createFontStyle().setBold(true);
        TextStyle textStyle = rtWriter.createTextStyle("Mystyle").setAlign(Justify.RIGHT);
        rtWriter.addText("This is a test!", textStyle, fontStyle);
        
        rtWriter.addImage(imageIn);
      }
      
      //write simple search query header
      try (RichTextWriter rtWriter = docOrig.createRichTextItem(NotesConstants.ASSIST_QUERY_ITEM)) {
        rtWriter.addRichTextRecord(CDQueryHeader.class, (record) -> {
          record
          .getHeader()
          .setSignature((byte) (RichTextConstants.SIG_QUERY_HEADER & 0xff))
          .setLength((short) (MemoryStructureUtil.sizeOf(CDQueryHeader.class) & 0xffff));
        });
      }
      NativeDesignSupport designSupport = NativeDesignSupport.get();
      
      //switch search query item from TYPE_COMPOSITE to TYPE_QUERY
      docOrig.forEachItem(NotesConstants.ASSIST_QUERY_ITEM, (item,loop) -> {
        item.setSigned(true);
        designSupport.setCDRecordItemType(docOrig, item, ItemDataType.TYPE_QUERY);
      });

      designSupport.initAgentRunInfo(docOrig);
      docOrig.save();
      
      RawDocSerializer serializer = new RawDocSerializer();
      serializer.includeMetadata(true);
      String json = serializer.toJson(docOrig).toString();
      assertTrue(StringUtil.isNotEmpty(json) && json.startsWith("{") && json.endsWith("}"));  //$NON-NLS-1$//$NON-NLS-2$
      
      RawDocDeserializer deserializer = new RawDocDeserializer();
      deserializer.target(db);
      
      Document docClone = deserializer.fromJson(json);
      
      compareDocs(docOrig, docClone);
      
      //test if importing twice works as well
      
      deserializer.target(docClone);
      docClone = deserializer.fromJson(json);
      
      compareDocs(docOrig, docClone);
    });
  }
  
  private void compareDocs(Document docOrig, Document docClone) {
    Set<DocumentClass> origDocClass = docOrig.getDocumentClass();
    Set<DocumentClass> cloneDocClass = docClone.getDocumentClass();
    assertEquals(origDocClass, cloneDocClass);
    
    Stream<Item> compareItemsOrig = docOrig.allItems()
        .filter((item) -> {
          String itemName = item.getName();
          return !AbstractJsonSerializer.isExcludedField(itemName);
        });
    
    Stream<Item> compareItemsClone = docClone.allItems()
        .filter((item) -> {
          String itemName = item.getName();
          return !AbstractJsonSerializer.isExcludedField(itemName);
        });

    Iterator<Item> origItemIt = compareItemsOrig.iterator();
    Iterator<Item> cloneItemIt = compareItemsClone.iterator();
    
    while (origItemIt.hasNext() && cloneItemIt.hasNext()) {
      Item itemOrig = origItemIt.next();
      Item itemClone = cloneItemIt.next();
      
      String itemNameOrig = itemOrig.getName();
      String itemNameClone = itemClone.getName();

      assertTrue(itemNameOrig.equalsIgnoreCase(itemNameClone), "Item name mismatch: "+itemNameOrig+"<->"+itemNameClone);

      ItemDataType typeOrig = itemOrig.getType();
      ItemDataType typeClone = itemClone.getType();
      assertEquals(typeOrig, typeClone, "Item type mismatch for "+itemNameOrig);
      
      if (!"$file".equalsIgnoreCase(itemNameOrig)) { //$NON-NLS-1$
        byte[] dataOrig = itemOrig.getAdapter(byte[].class);
        byte[] dataClone = itemClone.getAdapter(byte[].class);
        assertArrayEquals(dataOrig, dataClone, "Item value mismatch for "+itemNameOrig);
      }
    }
    
    //compare attachments
    
    Set<String> origAttachmentNames = docOrig.getAttachmentNames();
    Set<String> cloneAttachmentNames = docClone.getAttachmentNames();
    
    assertEquals(origAttachmentNames, cloneAttachmentNames);
    
    for (String currFileName : origAttachmentNames) {
      Attachment origAttachment = docOrig.getAttachment(currFileName).orElse(null);
      Attachment cloneAttachment = docOrig.getAttachment(currFileName).orElse(null);
      
      assertNotNull(origAttachment);
      assertNotNull(cloneAttachment);
      assertEquals(origAttachment.getFileSize(), cloneAttachment.getFileSize());
      assertEquals(origAttachment.getFileCreated(), cloneAttachment.getFileCreated());
      assertEquals(origAttachment.getFileModified(), cloneAttachment.getFileModified());
    }
    
    for (String currFileName : cloneAttachmentNames) {
      Attachment origAttachment = docOrig.getAttachment(currFileName).orElse(null);
      Attachment cloneAttachment = docOrig.getAttachment(currFileName).orElse(null);
      
      assertNotNull(origAttachment);
      assertNotNull(cloneAttachment);
      assertEquals(origAttachment.getFileSize(), cloneAttachment.getFileSize());
      assertEquals(origAttachment.getFileCreated(), cloneAttachment.getFileCreated());
      assertEquals(origAttachment.getFileModified(), cloneAttachment.getFileModified());
    }
  }
}
