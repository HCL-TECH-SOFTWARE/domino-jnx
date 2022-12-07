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
package com.hcl.domino;

import java.nio.file.Path;

import com.hcl.domino.exception.DominoInitException;

/**
 * This service interface allows access to initialization and termination of
 * the Domino runtime for the process.
 * <p>
 * This service is not guaranteed to be provided by every implementation.
 * </p>
 *
 * @author Jesse Gallagher
 */
public interface DominoProcess {
  static DominoCommonFactory factory = DominoCommonFactory.getCommonFactory();

  public interface DominoThreadContext extends AutoCloseable {

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.<br>
     * <br>
     * Calls {@link DominoProcess#terminateThread()} internally.
     */
    @Override
    void close();
  }

  /**
   * @return an implementation of {@code DominoProcess}
   * @throws IllegalStateException if the active API implementation does not
   *                               provide one
   */
  static DominoProcess get() {
	 return factory.createDominoProcess();
  }

  /**
   * Initializes the Domino runtime for the process.
   * <p>
   * In implementations that require it, this method should be called once per
   * process,
   * before any other API operations.
   * </p>
   *
   * @param initArgs the arguments to pass to the initialization call
   * @throws DominoInitException if initialization fails
   */
  void initializeProcess(String[] initArgs) throws DominoInitException;

  /**
   * Initializes the current thread for Domino API use.
   * <p>
   * Note: it is preferable to use threads spawned by
   * {@link DominoClient#getThreadFactory()}.
   * </p>
   *
   * @return AutoCloseable to terminate thread, same as calling
   *         {@link #terminateThread()}
   */
  DominoThreadContext initializeThread();

  /**
   * This function switches to the specified ID file and returns the user name
   * associated with it.<br>
   * <br>
   * Multiple passwords are not supported.<br>
   * <br>
   * NOTE: This function should only be used in a C API stand alone application.
   *
   * @param idPath        path to the ID file that is to be switched to; if
   *                      null/empty, we read the ID file path from the Notes.ini
   *                      (KeyFileName)
   * @param password      password of the ID file that is to be switched to
   * @param dontSetEnvVar If specified, the notes.ini file (either
   *                      ServerKeyFileName or KeyFileName) is modified to reflect
   *                      the ID change.
   * @return user name, in the ID file that is to be switched to
   */
  String switchToId(Path idPath, String password, boolean dontSetEnvVar);

  /**
   * Destroys the Domino runtime for the entire process.
   * <p>
   * This method should be called only when all Domino operations are finished for
   * the
   * running application.
   * </p>
   */
  void terminateProcess();

  /**
   * Terminates the current thread for Domino API use.
   * <p>
   * Note: it is preferable to use threads spawned by
   * {@link DominoClient#getThreadFactory()}.
   * </p>
   */
  void terminateThread();
}
