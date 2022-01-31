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
package com.hcl.domino.runtime;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents the configuration of the running Notes or Domino environment.
 */
public interface DominoRuntime {

  /**
   * Retrieves the path of the data directory for the current Notes/Domino
   * runtime.
   *
   * @return an {@link Optional} describing a {@link Path} representing the data
   *         directory
   *         on the filesystem, or an empty one if the directory cannot be
   *         determined
   */
  Optional<Path> getDataDirectory();

  /**
   * Retrieves the path of the program directory for the current Notes/Domino
   * runtime.
   *
   * @return an {@link Optional} describing a {@link Path} representing the
   *         program directory
   *         on the filesystem, or an empty one if the directory cannot be
   *         determined
   */
  Optional<Path> getProgramDirectory();

  /**
   * Retrieves the value of a Notes configuration property as an integer. When
   * the property is not set, this returns {@code 0}.
   * <p>
   * These properties correspond to notes.ini settings.
   * </p>
   *
   * @param propertyName the name of the property to get
   * @return the value of the property as an {@code int}
   */
  int getPropertyInt(String propertyName);

  /**
   * Retrieves the value of a Notes configuration property as a string.
   * <p>
   * These properties correspond to notes.ini settings.
   * </p>
   *
   * @param propertyName the name of the property to get
   * @return the value of the property as a string
   */
  String getPropertyString(String propertyName);

  /**
   * Retrieves the path of the temporary-files directory used by the current
   * Notes/Domino runtime.
   *
   * @return an {@link Optional} describing a {@link Path} representing the temp
   *         directory on the filesystem, or an empty one if the directory
   *         cannot be determined
   */
  Optional<Path> getTempDirectory();

  /**
   * Retrieves the path of the directory used by the current Notes/Domino runtime
   * for temporary storage during view rebuilding.
   *
   * @return an {@link Optional} describing a {@link Path} representing the
   *         view-rebuild directory on the filesystem, or an empty one if the
   *         directory cannot be determined
   */
  Optional<Path> getViewRebuildDirectory();
  
  /**
   * Retrieves the path of the directory used to house shared data for a multi-user
   * installation.
   * 
   * @return an {@link Optional} describing a {@link Path} representing the
   *         shared data directory on the filesystem, or an empty one if no
   *         shared data directory is configured
   * @since 1.0.43
   */
  Optional<Path> getSharedDataDirectory();

  /**
   * Tells the local runtime to run NSD.
   *
   * @param modes configuration options for the NSD execution
   */
  void invokeNSD(Collection<NSDMode> modes);

  /**
   * Sets a Notes configuration property to the specified integer.
   * <p>
   * These properties correspond to notes.ini settings.
   * </p>
   *
   * @param propertyName the property to set
   * @param value        the value to set
   */
  void setProperty(String propertyName, int value);

  /**
   * Sets a Notes configuration property to the specified string.
   * <p>
   * These properties correspond to notes.ini settings.
   * </p>
   *
   * @param propertyName the property to set
   * @param value        the value to set
   */
  void setProperty(String propertyName, String value);
}
