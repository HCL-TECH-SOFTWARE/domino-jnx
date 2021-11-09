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
package com.hcl.domino.commons.design.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.design.agent.DesignImportedJavaAgent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionJavaAgent;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.util.JNXStringUtil;

/**
 * Implementation of {@link DesignImportedJavaAgent}
 * 
 * @author Karsten Lehmann
 * @since 1.0.46
 */
public class DesignImportedJavaAgentImpl extends AbstractDesignAgentImpl<DesignImportedJavaAgent> implements DesignImportedJavaAgent {

  public DesignImportedJavaAgentImpl(Document doc) {
    super(doc);
  }

  private Optional<CDActionJavaAgent> getJavaCDRecord() {
    return getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
    .stream()
    .filter(CDActionJavaAgent.class::isInstance)
    .map(CDActionJavaAgent.class::cast)
    .findFirst();
  }
  
  @Override
  public String getCodeFilesystemPath() {
    return getJavaCDRecord().map(CDActionJavaAgent::getCodePath).orElse(""); //$NON-NLS-1$
  }

  @Override
  public List<String> getFilenames() {
    Optional<CDActionJavaAgent> action = getJavaCDRecord();
    if (!action.isPresent()) {
      return Collections.emptyList();
    }
    else {
      return Arrays.stream(action.get().getFileList().split("(\r)?\n")) //$NON-NLS-1$
          .filter(StringUtil::isNotEmpty)
          .collect(Collectors.toList());
    }
  }

  @Override
  public Optional<InputStream> getFile(String name) {
    List<String> filenames = getFilenames();
    
    // Do a basic check to make sure it's in the list
    if(!filenames.contains(name)) {
      return Optional.empty();
    }
    
    return getDocument()
      .getAttachment(name)
      .map(t -> {
        try {
          return t.getInputStream();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }

  @Override
  public String getMainClassName() {
    return getJavaCDRecord().map(CDActionJavaAgent::getClassName).orElse(""); //$NON-NLS-1$
  }

  /**
   * Rewrites the {@link CDActionJavaAgent} with applied changes, creating a new one if not there yet
   * 
   * @param consumer consumer of {@link CDActionJavaAgent} record
   */
  private void withJavaCDRecord(Consumer<CDActionJavaAgent> consumer) {
    Document doc = getDocument();
    
    List<RichTextRecord<?>> records = new ArrayList<>(doc.getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION));
    
    CDActionHeader actionHeaderRecord = records.stream()
        .filter(CDActionHeader.class::isInstance)
        .map(CDActionHeader.class::cast)
        .findFirst().orElse(null);

    if (actionHeaderRecord==null) {
      actionHeaderRecord = RichTextRecord.create(RecordType.ACTION_HEADER, 0);
      records.add(actionHeaderRecord);
    }

    CDActionJavaAgent javaAgentRecord = records.stream()
        .filter(CDActionJavaAgent.class::isInstance)
        .map(CDActionJavaAgent.class::cast)
        .findFirst().orElse(null);
    
    if (javaAgentRecord==null) {
      javaAgentRecord = RichTextRecord.create(RecordType.ACTION_JAVAAGENT, 0);
      records.add(javaAgentRecord);
    }
    
    consumer.accept(javaAgentRecord);
    
    doc.removeItem(NotesConstants.ASSIST_ACTION_ITEM);
    try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_ACTION_ITEM)) {
      records.forEach(rtWriter::addRichTextRecord);
    }
    
    //fix wrong item type TYPE_COMPOSITE
    doc.forEachItem(NotesConstants.ASSIST_ACTION_ITEM, (item, loop) -> {
      item.setSigned(true);
      NativeDesignSupport.get().setCDRecordItemType(doc, item, ItemDataType.TYPE_ACTION);
    });
  }

  /**
   * Adds an attachment to the document with the specified filename
   * 
   * @param fileName filename
   * @param in attachment data
   */
  private void addSignedAttachment(String fileName, InputStream in) {
    Document doc = getDocument();
    Optional<Attachment> oldAtt = doc.getAttachment(fileName);
    if (oldAtt.isPresent()) {
      oldAtt.get().deleteFromDocument();
    }
    
    Instant now = Instant.now();
    doc.attachFile(fileName,
      now, now, new IAttachmentProducer() {

        @Override
        public long getSizeEstimation() {
          return -1;
        }

        @Override
        public void produceAttachment(OutputStream out) throws IOException {
          byte[] buf = new byte[8192];
          int len;
          
          while ((len = in.read(buf))>0) {
            out.write(buf, 0, len);
          }
        }
    });
    
    //change $file item flags
    doc.forEachItem("$file", (item,loop) -> { //$NON-NLS-1$
      if (item.getType() == ItemDataType.TYPE_OBJECT) {
        Attachment att = item.get(Attachment.class, null);
        if (att!=null && fileName.equals(att.getFileName())) {
          item.setSigned(true);
          item.setEncrypted(true);
          item.setSummary(true);
        }
      }
    });
  }
  
  @Override
  public void setMainClassName(String name) {
    withJavaCDRecord((record) -> {
      record.setClassName(Objects.requireNonNull(name));
    });
  }
  
  @Override
  public void setCodeFilesystemPath(String path) {
    withJavaCDRecord((record) -> {
      record.setCodePath(path);
    });
  }
  
  @Override
  public void setFiles(Map<String, InputStream> files) {
    Document doc = getDocument();
    
    //remove all old embedded jars
    getFilenames().forEach((fileName) -> {
      doc.getAttachment(fileName).ifPresent(Attachment::deleteFromDocument);
    });
    
    //add all new jars
    if (files!=null && !files.isEmpty()) {
      files
      .entrySet()
      .stream()
      .forEach((entry) -> {
        String fileName = entry.getKey();
        InputStream in = entry.getValue();
        
        addSignedAttachment(fileName, in);
      });
    }
    
    withJavaCDRecord((action) -> {
      String filenamesConc = files.keySet().stream().filter(JNXStringUtil::isEmpty).collect(Collectors.joining("\n")); //$NON-NLS-1$
      action.setFileList(filenamesConc);
    });
  }
  
  @Override
  public void setFile(String fileName, InputStream in) {
    if (in==null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    
    Document doc = getDocument();
    doc.getAttachment(fileName).ifPresent(Attachment::deleteFromDocument);
    
    addSignedAttachment(fileName, in);
    
    List<String> filenames = getFilenames();
    if (!filenames.contains(fileName)) {
      List<String> newFilenames = new ArrayList<>(filenames);
      newFilenames.add(fileName);
      
      withJavaCDRecord((action) -> { action.setFileList(newFilenames.stream().collect(Collectors.joining("\n"))); }); //$NON-NLS-1$
    }
  }
  
  @Override
  public void removeFile(String fileName) {
    List<String> filenames = getFilenames();
    if (!filenames.contains(fileName)) {
      return;
    }
    
    Document doc = getDocument();
    doc.getAttachment(fileName).ifPresent(Attachment::deleteFromDocument);
    
    String newFilesConc = filenames
        .stream()
        .filter((currName) -> { return !fileName.equals(currName); })
        .collect(Collectors.joining("\n")); //$NON-NLS-1$
    
    withJavaCDRecord((action) -> {
      action.setFileList(newFilesConc);
    });
  }
  
}
