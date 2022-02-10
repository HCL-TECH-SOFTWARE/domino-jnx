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
package com.hcl.domino.design;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JavaAgentOrLibrary<T extends JavaAgentOrLibrary<?>> extends DesignElement {

  /**
   * Changes the JDK version for Java source compatibility
   * 
   * @param target e.g. "1.3"
   */
  void setJavaCompilerSource(String target);
  
  /**
   * Gets the JDK version for Java source compatibility
   * 
   * @return target, e.g. "1.3"
   */
  String getJavaCompilerSource();
  
  /**
   * Changes the JDK version for Java class compatibility
   * 
   * @param target e.g. "1.3"
   */
  void setJavaCompilerTarget(String target);
  
  /**
   * Gets the JDK version for Java class compatibility
   * 
   * @return target, e.g. "1.2"
   */
  String getJavaCompilerTarget();

  /**
   * Creates initial attachments for source and object content if they don't yet exist,
   * similar to Domino Designer creating a new agent/library.
   * 
   * @return this instance
   */
  T initJavaContent();

  /**
   * Returns the code filesystem path
   * 
   * @return path, not null
   */
  String getCodeFilesystemPath();

  /**
   * Sets the code filesystem path
   * 
   * @param path new path
   * @return this instance
   */
  T setCodeFilesystemPath(String path);
  
  /**
   * Returns a list of embedded jars
   * 
   * @return list of filenames
   */
  List<String> getEmbeddedJarNames();
 
  /**
   * Replaces the existing embedded jars with new ones
   * 
   * @param embeddedJars map of filenames and their file data
   * @return this instance
   */
  T setEmbeddedJars(Map<String,InputStream> embeddedJars);

  /**
   * Adds/replaces a single embedded jars
   * 
   * @param fileName filename
   * @param in new data
   * @return this instance
   */
  T setEmbeddedJar(String fileName, InputStream in);
  
  /**
   * Removes a single embedded jar
   * 
   * @param fileNameToRemove filename of embedded jar to remove
   * @return this instance
   */
  T removeEmbeddedJar(String fileNameToRemove);
  
  /**
   * Sets/Replaces the source attachment with the specified internal filename
   * 
   * @param in stream, not null
   * @return this instance
   */
  T setSourceAttachment(InputStream in);
  
  /**
   * Sets/Replaces the object attachment
   * 
   * @param in stream, not null
   * @return this instance
   */
  T setObjectAttachment(InputStream in);
  
  /**
   * Changes the resource attachment jar
   * 
   * @param in new data or null to just remove the old jar
   * @return this instance
   */
  T setResourceAttachment(InputStream in);
  
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
  Optional<InputStream> getEmbeddedJar(String name);
  
  /**
   * Reads the classname of the main Java class
   * 
   * @return classname, not null
   */
  String getMainClassName();
  
  /**
   * Sets the classname of the main Java class
   * 
   * @param name new name
   * @return this instance
   */
  T setMainClassName(String name);
  
  /**
   * Returns the filename of the bytecode JAR
   * 
   * @return filename if it exists
   */
  Optional<String> getObjectAttachmentName();
 
  /**
   * Retrieves the contents of the bytecode JAR as a stream of bytes.
   * 
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the bytecode JAR's contents if it exists, or an empty
   *         one otherwise
   * @since 1.0.43
   */
  Optional<InputStream> getObjectAttachment();
  
  /**
   * Returns the filename of the resources attachment if the file exists.
   * 
   * @return filename
   */
  Optional<String> getResourcesAttachmentName();
  
  /**
   * Retrieves the contents of the resources JAR as a stream of bytes.
   * 
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the resources JAR's contents if it exists, or an empty
   *         one otherwise
   * @since 1.0.43
   */
  Optional<InputStream> getResourcesAttachment();
  
  /**
   * Returns a list of shared libraries that this Java agent/library depends on
   * 
   * @return library names
   */
  List<String> getSharedLibraryList();
  
  /**
   * Changes the list of shared libraries that this Java agent/library depends on
   * 
   * @param libs shared library names
   * @return this instance
  */
  T setSharedLibraryList(List<String> libs);

  /**
   * Returns the filename of the source JAR file
   * 
   * @return filename
   */
  Optional<String> getSourceAttachmentName();
  
  /**
   * Retrieves the contents of the source JAR as a stream of bytes.
   * 
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the source JAR's contents if it exists, or an empty
   *         one otherwise
   * @since 1.0.43
   */
  Optional<InputStream> getSourceAttachment();
  
  /**
   * Determines whether the Java agent or library is configured to be compiled
   * with debug information.
   * 
   * @return {@code true} if Java code should be compiled with debug information;
   *         {@code false} otherwise
   * @since 1.1.2
   */
  boolean isCompileDebug();
  
  /**
   * Sets whether the Java agent or library is configured to be compiled
   * with debug information.
   * 
   * @param debug {@code true} if Java code should be compiled with debug
   *              information; {@code false} otherwise
   * @return this instance
   * @since 1.1.2
   */
  T setCompileDebug(boolean debug);
  
}
