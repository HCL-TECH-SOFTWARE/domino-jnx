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
package com.hcl.domino.design.agent;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Represents the contents of a non-imported Java agent.
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public interface JavaAgentContent extends AgentContent {
  String getCodeFilesystemPath();

  List<String> getEmbeddedJars();
  
  /**
   * Retrieves the contents of the named embedded JAR (as determined
   * in {@link #getEmbeddedJars()} as a stream of bytes.
   * 
   * @param name the name of the JAR to retrieve
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the JAR's contents if it exists, or an empty one otherwise
   * @see #getEmbeddedJars()
   * @since 1.0.43
   */
  Optional<InputStream> getEmbeddedJar(String name);

  String getMainClassName();

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

  List<String> getSharedLibraryList();

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
}
