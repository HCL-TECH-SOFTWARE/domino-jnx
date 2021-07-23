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
package com.hcl.domino.dbdirectory;

import com.hcl.domino.data.DominoDateTime;

/**
 * Subclass of {@link DirEntry} that is used to return
 * parsed data of folders.
 */
public interface FolderData extends DirEntry {
  /**
   * Returns the length of a folder
   *
   * @return length
   */
  @Override
  long getFileLength();

  /**
   * Returns the name of the folder
   *
   * @return name
   */
  @Override
  String getFileName();

  /**
   * Returns the complete relative path of the folder in the
   * data directory
   *
   * @return path
   */
  @Override
  String getFilePath();

  /**
   * Returns the folder modification date
   *
   * @return modification date
   */
  @Override
  DominoDateTime getModified();

}
