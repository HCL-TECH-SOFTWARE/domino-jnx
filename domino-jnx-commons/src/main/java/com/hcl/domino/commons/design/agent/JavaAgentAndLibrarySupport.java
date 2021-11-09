package com.hcl.domino.commons.design.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
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

import com.hcl.domino.commons.design.AbstractDesignElement;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionJavaAgent;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.util.JNXStringUtil;

/**
 * Shared code for r/w access of Java agent and library resources
 * 
 * @author Karsten Lehmann
 */
public class JavaAgentAndLibrarySupport {
  /** standard filename for the source JAR */
  String SOURCE_JAR_FILENAME = "%%source%%.jar"; //$NON-NLS-1$
  /** standard filename for the bytecode JAR */
  String OBJECT_JAR_FILENAME = "%%object%%.jar"; //$NON-NLS-1$
  /** standard filename for the resources jar */
  String RESOURCE_JAR_FILENAME = "%%resource%%.jar"; //$NON-NLS-1$

  private AbstractDesignElement<?> designElement;
  
  public JavaAgentAndLibrarySupport(AbstractDesignElement<?> designElement) {
    this.designElement = designElement;
  }
  
  private Document getDocument() {
    return designElement.getDocument();
  }
  
  /**
   * Creates initial attachments for source and object content if they don't yet exist,
   * similar to Domino Designer creating a new agent/library.
   */
  public void initJavaContent() {
    Document doc = getDocument();
    String flags = doc.get(NotesConstants.DESIGN_FLAGS, String.class, ""); //$NON-NLS-1$
    boolean isLib = flags.contains(NotesConstants.DESIGN_FLAG_SCRIPTLIB);
    
    Optional<String> oldResourceAttName = getResourcesAttachmentName();
    List<String> embeddedJars = getEmbeddedJarNames();
    
    boolean changed = false;
    
    {
      Optional<Attachment> oldSourceAtt = getSourceAttachmentName().flatMap(doc::getAttachment);
      
      if (!oldSourceAtt.isPresent()) {
        //we need to add the source jar
        InputStream in = null;
        try {
          if (isLib) {
            final String LIB_DEFAULT_SOURCE_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javalibrary/%%source%%.jar"; //$NON-NLS-1$
            in = getClass().getResourceAsStream(LIB_DEFAULT_SOURCE_JAR_RESOURCEPATH);
            if (in==null) {
              throw new IllegalStateException(MessageFormat.format("Required resource not found: {0}", LIB_DEFAULT_SOURCE_JAR_RESOURCEPATH));
            }
          }
          else {
            final String AGENT_DEFAULT_SOURCE_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javaagent/%%source%%.jar"; //$NON-NLS-1$
            in = getClass().getResourceAsStream(AGENT_DEFAULT_SOURCE_JAR_RESOURCEPATH);
            if (in==null) {
              throw new IllegalStateException(MessageFormat.format("Required resource not found: {0}", AGENT_DEFAULT_SOURCE_JAR_RESOURCEPATH));
            }
          }
          
          addSignedAttachment(SOURCE_JAR_FILENAME, in);
          changed = true;
        }
        finally {
          if (in!=null) {
            try {
              in.close();
            } catch (IOException e) {
            }
          }
        }
      }
    }

    {
      Optional<Attachment> oldObjectAtt = getObjectAttachmentName().flatMap(doc::getAttachment);
      
      if (!oldObjectAtt.isPresent()) {
        InputStream in = null;
        try {
          if (isLib) {
            final String LIB_DEFAULT_OBJECT_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javalibrary/%%object%%.jar";
            in = JavaAgentAndLibrarySupport.class.getResourceAsStream(LIB_DEFAULT_OBJECT_JAR_RESOURCEPATH);
            if (in==null) {
              throw new IllegalStateException(MessageFormat.format("Required resource not found: {0}", LIB_DEFAULT_OBJECT_JAR_RESOURCEPATH));
            }
          }
          else {
            final String AGENT_DEFAULT_OBJECT_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javaagent/%%object%%.jar";
            in = JavaAgentAndLibrarySupport.class.getResourceAsStream(AGENT_DEFAULT_OBJECT_JAR_RESOURCEPATH);
            if (in==null) {
              throw new IllegalStateException(MessageFormat.format("Required resource not found: {0}", AGENT_DEFAULT_OBJECT_JAR_RESOURCEPATH));
            }
          }
          
          addSignedAttachment(OBJECT_JAR_FILENAME, in);
          changed = true;
        }
        finally {
          if (in!=null) {
            try {
              in.close();
            } catch (IOException e) {
            }
          }
        }
      }
    }
    
    if (changed) {
      //build new java class file list, keep old resource attachment name and other embedded jars
      // 0 - source attachment name
      // 1 - object attachment name
      // 2 - resource attachment name
      // 3,4,5 etc - embedded jar names
      List<String> newJavaClassFileList = new ArrayList<>();
      newJavaClassFileList.add(SOURCE_JAR_FILENAME);
      newJavaClassFileList.add(OBJECT_JAR_FILENAME);
      if (oldResourceAttName.isPresent()) {
        newJavaClassFileList.add(oldResourceAttName.get());
      }
      embeddedJars.forEach(newJavaClassFileList::add);
      
      setJavaClassFileList(newJavaClassFileList);
    }
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
        List<?> values = item.getValue();
        if (values!=null && values.size()==1 && values.get(0) instanceof Attachment) {
          Attachment att = (Attachment) values.get(0);
          
          if (fileName.equals(att.getFileName())) {
            item.setSigned(true);
            item.setEncrypted(true);
            item.setSummary(true);
            loop.stop();
          }
        }
      }
    });
  }

  private Optional<CDActionJavaAgent> getJavaCDRecord() {
    return getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
    .stream()
    .filter(CDActionJavaAgent.class::isInstance)
    .map(CDActionJavaAgent.class::cast)
    .findFirst();
  }
  
  /**
   * Returns the code filesystem path
   * 
   * @return path, not null
   */
  public String getCodeFilesystemPath() {
    return getJavaCDRecord().map(CDActionJavaAgent::getCodePath).orElse(""); //$NON-NLS-1$
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
  
  public void setCodeFilesystemPath(String path) {
    withJavaCDRecord((record) -> {
      record.setCodePath(path);
    });
  }
  
  private List<String> getJavaClassFileList() {
    return getJavaCDRecord()
        .map((action) -> {
          return Arrays.stream(action.getFileList().split("(\r)?\n")) //$NON-NLS-1$
              .filter(JNXStringUtil::isNotEmpty)
              .collect(Collectors.toList());
        })
        .orElseGet(Collections::emptyList);
  }
  
  private void setJavaClassFileList(List<String> list) {
    String listConc = list.stream().collect(Collectors.joining("\n")); //$NON-NLS-1$
    withJavaCDRecord((record) -> {
      record.setFileList(listConc);
    });
  }
  
  /**
   * Returns a list of embedded jars
   * 
   * @return list of filenames
   */
  public List<String> getEmbeddedJarNames() {
    List<String> javaClassFileList = getJavaClassFileList();

    if (javaClassFileList.size() > 2) {
      //position 2 might either contain %%resource%%.jar or the first embedded jar filename
      String fileName = javaClassFileList.get(2);
      if (RESOURCE_JAR_FILENAME.equals(fileName)) {
        if (javaClassFileList.size() > 3) {
          return Collections.unmodifiableList(javaClassFileList.subList(3, javaClassFileList.size()));
        }
      }
      else {
        return Collections.unmodifiableList(javaClassFileList.subList(2, javaClassFileList.size()));
      }
    }
    
    return Collections.emptyList();
  }

  /**
   * Replaces the existing embedded jars with new ones
   * 
   * @param embeddedJars map of filenames and their file data
   */
  public void setEmbeddedJars(Map<String,InputStream> embeddedJars) {
    //check for reserved filenames that could produce a mess
    if (embeddedJars.containsKey(SOURCE_JAR_FILENAME)) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid reserved filename found: \"{0}\"", SOURCE_JAR_FILENAME));
    }
    if (embeddedJars.containsKey(OBJECT_JAR_FILENAME)) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid reserved filename found: \"{0}\"", OBJECT_JAR_FILENAME));
    }
    if (embeddedJars.containsKey(RESOURCE_JAR_FILENAME)) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid reserved filename found: \"{0}\"", RESOURCE_JAR_FILENAME));
    }
    
    Document doc = getDocument();
    
    if (embeddedJars!=null && !embeddedJars.isEmpty()) {
      //make sure we have source/object jars if any embedded jars will be added later
      initJavaContent();
    }
    
    //remove all old embedded jars
    getEmbeddedJarNames().forEach((fileName) -> {
      doc.getAttachment(fileName).ifPresent(Attachment::deleteFromDocument);
    });
    
    //add all new jars
    if (embeddedJars!=null && !embeddedJars.isEmpty()) {
      embeddedJars
      .entrySet()
      .stream()
      .forEach((entry) -> {
        String fileName = entry.getKey();
        InputStream in = entry.getValue();
        
        addSignedAttachment(fileName, in);
      });
    }
    
    //write new java class file list
    List<String> newJavaClassFileList = new ArrayList<>();
   
    //we made sure that these two exist; Designer auto-creates them as well when adding resources or embedded jars:
    newJavaClassFileList.add(getSourceAttachmentName().get());
    newJavaClassFileList.add(getObjectAttachmentName().get());

    //resource is optional
    Optional<String> resourceAttName = getResourcesAttachmentName();
    if (resourceAttName.isPresent()) {
      newJavaClassFileList.add(resourceAttName.get());
    }
    
    if (embeddedJars!=null) {
      newJavaClassFileList.addAll(embeddedJars.keySet());
    }
    
    setJavaClassFileList(newJavaClassFileList);
  }

  /**
   * Adds/replaces a single embedded jars
   * 
   * @param fileName filename
   * @param in new data
   */
  public void setEmbeddedJar(String fileName, InputStream in) {
    //check for reserved filenames that could produce a mess
    if (fileName.equals(SOURCE_JAR_FILENAME)) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid reserved filename found: \"{0}\"", SOURCE_JAR_FILENAME));
    }
    if (fileName.equals(OBJECT_JAR_FILENAME)) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid reserved filename found: \"{0}\"", OBJECT_JAR_FILENAME));
    }
    if (fileName.equals(RESOURCE_JAR_FILENAME)) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid reserved filename found: \"{0}\"", RESOURCE_JAR_FILENAME));
    }

    if (in==null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    
    removeEmbeddedJar(fileName);
    
    initJavaContent();

    List<String> oldEmbeddedJarNames = getEmbeddedJarNames();
    
    //add attachment to doc
    addSignedAttachment(fileName, in);
    
    //write new java class file list
    List<String> newJavaClassFileList = new ArrayList<>();
   
    //we made sure that these two exist; Designer auto-creates them as well when adding resources or embedded jars:
    newJavaClassFileList.add(getSourceAttachmentName().get());
    newJavaClassFileList.add(getObjectAttachmentName().get());

    //resource is optional
    Optional<String> resourceAttName = getResourcesAttachmentName();
    if (resourceAttName.isPresent()) {
      newJavaClassFileList.add(resourceAttName.get());
    }
    
    newJavaClassFileList.addAll(oldEmbeddedJarNames);
    newJavaClassFileList.add(fileName);
    
    setJavaClassFileList(newJavaClassFileList);
  }
  
  /**
   * Removes a single embedded jar
   * 
   * @param fileNameToRemove filename of embedded jar to remove
   */
  public void removeEmbeddedJar(String fileNameToRemove) {
    List<String> oldEmbeddedJarNames = getEmbeddedJarNames();
    
    if (oldEmbeddedJarNames.contains(fileNameToRemove)) {
      Document doc = getDocument();
      
      doc.getAttachment(fileNameToRemove).ifPresent((att) -> {att.deleteFromDocument(); } );
      
      //write new java class file list
      List<String> newJavaClassFileList = new ArrayList<>();
     
      //we made sure that these two exist; Designer auto-creates them as well when adding resources or embedded jars:
      newJavaClassFileList.add(getSourceAttachmentName().get());
      newJavaClassFileList.add(getObjectAttachmentName().get());

      //resource is optional
      Optional<String> resourceAttName = getResourcesAttachmentName();
      if (resourceAttName.isPresent()) {
        newJavaClassFileList.add(resourceAttName.get());
      }
      
      oldEmbeddedJarNames
      .stream()
      .filter((currFileName) -> { return !fileNameToRemove.equals(currFileName); })
      .forEach(newJavaClassFileList::add);
      
      setJavaClassFileList(newJavaClassFileList);
    }
  }
  
  /**
   * Sets/Replaces the source attachment with the specified internal filename
   * 
   * @param in stream, not null
   */
  public void setSourceAttachment(InputStream in) {
    if (in==null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    
    Document doc = getDocument();

    //make sure we always have both source/object jars in the doc to not mess up the Java class file list
    initJavaContent();
    
    Optional<String> resourceAttachmentName = getResourcesAttachmentName();
    List<String> embeddedJars = getEmbeddedJarNames();
    
    //remove old source attachment
    getSourceAttachmentName().ifPresent((fileName) -> {
      doc.getAttachment(fileName).ifPresent(Attachment::deleteFromDocument);
    });
    
    addSignedAttachment(SOURCE_JAR_FILENAME, in);
    
    //write new java class file list
    List<String> newJavaClassFileList = new ArrayList<>();
    newJavaClassFileList.add(SOURCE_JAR_FILENAME);
    newJavaClassFileList.add(getObjectAttachmentName().get());
    
    if (resourceAttachmentName.isPresent()) {
      newJavaClassFileList.add(resourceAttachmentName.get());
    }
    newJavaClassFileList.addAll(embeddedJars);
    
    setJavaClassFileList(newJavaClassFileList);
  }
  
  /**
   * Sets/Replaces the object attachment
   * 
   * @param in stream, not null
   */
  public void setObjectAttachment(InputStream in) {
    if (in==null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    
    Document doc = getDocument();

    //make sure we always have both source/object jars in the doc to not mess up the Java class file list
    initJavaContent();
    
    Optional<String> resourceAttachmentName = getResourcesAttachmentName();
    List<String> embeddedJars = getEmbeddedJarNames();
 
    //remove old object attachment
    getObjectAttachmentName().ifPresent((fileName) -> {
      doc.getAttachment(fileName).ifPresent(Attachment::deleteFromDocument);
    });
    
    addSignedAttachment(OBJECT_JAR_FILENAME, in);
    
    //write new java class file list
    List<String> newJavaClassFileList = new ArrayList<>();
    newJavaClassFileList.add(getSourceAttachmentName().get());
    newJavaClassFileList.add(OBJECT_JAR_FILENAME);
    
    if (resourceAttachmentName.isPresent()) {
      newJavaClassFileList.add(resourceAttachmentName.get());
    }
    newJavaClassFileList.addAll(embeddedJars);
    
    setJavaClassFileList(newJavaClassFileList);
  }
  
  /**
   * Changes the resource attachment jar
   * 
   * @param in new data or null to just remove the old jar
   */
  public void setResourceAttachment(InputStream in) {
    Document doc = getDocument();
    
    List<String> embeddedJars = getEmbeddedJarNames();

    if (in==null) {
      Optional<String> sourceAttachmentName = getSourceAttachmentName();
      Optional<String> objectAttachmentName = getObjectAttachmentName();
      
      //remove existing resource jar filename if present
      Optional<Attachment> oldResourceAttachment = getResourcesAttachmentName().flatMap(doc::getAttachment);
      if (oldResourceAttachment.isPresent()) {
        oldResourceAttachment.get().deleteFromDocument();
      }

      //write new java class file list
      List<String> newJavaClassFileList = new ArrayList<>();
      
      if (sourceAttachmentName.isPresent()) {
        newJavaClassFileList.add(sourceAttachmentName.get());
        
        if (objectAttachmentName.isPresent()) {
          newJavaClassFileList.add(objectAttachmentName.get());
          
          embeddedJars.forEach(newJavaClassFileList::add);
        }
      }
   
      setJavaClassFileList(newJavaClassFileList);
    }
    else {
      //overwrite resource jar with new data
      Optional<Attachment> oldResourceAttachment = getResourcesAttachmentName().flatMap(doc::getAttachment);
      if (oldResourceAttachment.isPresent()) {
        oldResourceAttachment.get().deleteFromDocument();
      }

      //make sure we have source/object jars
      initJavaContent();

      addSignedAttachment(RESOURCE_JAR_FILENAME, in);
      
      List<String> newJavaClassFileList = new ArrayList<>();
      
      newJavaClassFileList.add(getSourceAttachmentName().get());
      newJavaClassFileList.add(getObjectAttachmentName().get());
      newJavaClassFileList.add(RESOURCE_JAR_FILENAME);
      newJavaClassFileList.addAll(embeddedJars);
      
      setJavaClassFileList(newJavaClassFileList);
    }
  }
  
  /**
   * Retrieves the contents of the named embedded JAR (as determined
   * in {@link #getEmbeddedJarNames()} as a stream of bytes.
   * 
   * @param name the name of the JAR to retrieve
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the JAR's contents if it exists, or an empty one otherwise
   * @see #getEmbeddedJarNames()
   * @since 1.0.43
   */
  public Optional<InputStream> getEmbeddedJar(String name) {
    // Do a basic check to make sure it's in the list
    List<String> jars = getEmbeddedJarNames();
    if(!jars.contains(name)) {
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

  /**
   * Reads the classname of the main Java class
   * 
   * @return classname, not null
   */
  public String getMainClassName() {
    return getJavaCDRecord().map(CDActionJavaAgent::getClassName).orElse(""); //$NON-NLS-1$
  }

  /**
   * Sets the classname of the main Java class
   * 
   * @param name new name
   */
  public void setMainClassName(String name) {
    withJavaCDRecord((record) -> {
      record.setClassName(Objects.requireNonNull(name));
    });
  }
  
  /**
   * Returns the filename of the bytecode JAR
   * 
   * @return filename if it exists
   */
  public Optional<String> getObjectAttachmentName() {
    List<String> javaClassFileList = getJavaClassFileList();
    
    if (javaClassFileList.size() > 1) {
      return Optional.of(javaClassFileList.get(1));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Retrieves the contents of the bytecode JAR as a stream of bytes.
   * 
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the bytecode JAR's contents if it exists, or an empty
   *         one otherwise
   * @since 1.0.43
   */
  public Optional<InputStream> getObjectAttachment() {
    return getObjectAttachmentName()
        .flatMap(name -> getDocument().getAttachment(name))
        .map(t -> {
          try {
            return t.getInputStream();
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  /**
   * Returns the filename of the resources attachment if the file exists.
   * 
   * @return filename
   */
  public Optional<String> getResourcesAttachmentName() {
    List<String> javaClassFileList = getJavaClassFileList();
    
    if (javaClassFileList.size() > 2) {
      String fileName = javaClassFileList.get(2);
      
      //since agents that contain embedded objects but no resources do not auto-create an
      //empty resources jar, we might find the first embedded jar filename at position 2
      //of the list instead of %%resource%%.jar, so check the filename
      if (RESOURCE_JAR_FILENAME.equals(fileName)) {
        return Optional.of(javaClassFileList.get(2));
      }
    }
    return Optional.empty();
  }

  /**
   * Retrieves the contents of the resources JAR as a stream of bytes.
   * 
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the resources JAR's contents if it exists, or an empty
   *         one otherwise
   * @since 1.0.43
   */
  public Optional<InputStream> getResourcesAttachment() {
    return getResourcesAttachmentName()
        .flatMap(name -> getDocument().getAttachment(name))
        .map(t -> {
          try {
            return t.getInputStream();
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  /**
   * Returns a list of shared libraries that this Java agent/library depends on
   * 
   * @return library names
   */
  public List<String> getSharedLibraryList() {
    final Optional<CDActionJavaAgent> action = getJavaCDRecord();
    if (!action.isPresent()) {
      return Collections.emptyList();
    }
    else {
      return Arrays.stream(action.get().getLibraryList().split("(\r)?\n")) //$NON-NLS-1$
          .filter(JNXStringUtil::isNotEmpty)
          .collect(Collectors.toList());
    }
  }

  /**
   * Changes the list of shared libraries that this Java agent/library depends on
   * 
   * @param libs shared library names
   */
  public void setSharedLibraryList(List<String> libs) {
    withJavaCDRecord((record) -> {
      String libsConc = libs
          .stream()
          .filter(JNXStringUtil::isNotEmpty)
          .collect(Collectors.joining("\n")); //$NON-NLS-1$
      record.setLibraryList(libsConc);
    });
  }
  
  /**
   * Returns the filename of the source JAR file
   * 
   * @return filename
   */
  public Optional<String> getSourceAttachmentName() {
    List<String> javaClassFileList = getJavaClassFileList();
    
    if (javaClassFileList.size() > 0) {
      return Optional.of(javaClassFileList.get(0));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Retrieves the contents of the source JAR as a stream of bytes.
   * 
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the source JAR's contents if it exists, or an empty
   *         one otherwise
   * @since 1.0.43
   */
  public Optional<InputStream> getSourceAttachment() {
    return getSourceAttachmentName()
        .flatMap(name -> getDocument().getAttachment(name))
        .map(t -> {
          try {
            return t.getInputStream();
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

}
