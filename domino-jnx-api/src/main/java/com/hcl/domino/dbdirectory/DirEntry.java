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
package com.hcl.domino.dbdirectory;

import java.util.Map;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;

/**
 * Base class for files returned by the directory query
 */
public interface DirEntry extends IAdaptable {

  /**
   * Returns the length of a file
   *
   * @return length
   */
  long getFileLength();

  /**
   * Returns the filename of the entry
   *
   * @return filename
   */
  String getFileName();

  /**
   * Returns the complete relative path of the entry in the
   * data directory
   *
   * @return path
   */
  String getFilePath();

  /**
   * Returns the file modification date
   *
   * @return modification date
   */
  DominoDateTime getModified();

  /**
   * Returns the physical/absolute filepath of the entry in the scanned
   * directory
   *
   * @return absolute path
   */
  String getPhysicalFilePath();

  /**
   * Returns a map of properties for the entry
   *
   * @return data
   */
  Map<String, Object> getProperties();

  /**
   * Returns the name of the scanned server
   *
   * @return server
   */
  String getServer();

}