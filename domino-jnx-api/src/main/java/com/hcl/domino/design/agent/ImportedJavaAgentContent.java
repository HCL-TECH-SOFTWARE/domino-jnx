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
 * Represents the contents of an imported Java agent.
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public interface ImportedJavaAgentContent extends AgentContent {
  String getCodeFilesystemPath();

  List<String> getFiles();
  
  /**
   * Retrieves the contents of the named file (as determined
   * in {@link #getFiles()} as a stream of bytes.
   * 
   * @param name the name of the file to retrieve
   * @return an {@link Optional} describing an {@link InputStream} of
   *         the file's contents if it exists, or an empty one otherwise
   * @see #getFiles()
   * @since 1.0.43
   */
  Optional<InputStream> getFile(String name);

  String getMainClassName();
}
