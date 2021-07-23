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
package com.hcl.domino.design;

import java.util.List;
import java.util.Set;

import com.hcl.domino.data.Database;
import com.hcl.domino.dbdirectory.DatabaseData;

/**
 * Access to database design information on the server
 */
public interface DbDesignCatalog {

  /**
   * Result of the template inheritance scan
   */
  public interface DatabaseDesignAnalysis {
    /**
     * Returns all databases that have a value in the database property
     * "Inherit design from master template"
     *
     * @return databases
     */
    List<DatabaseData> getAllDatabasesWithInheritedTemplate();

    /**
     * Returns all the inherited template names that exist on the server
     *
     * @return inherited template names
     */
    Set<String> getAllInheritedTemplateNames();

    /**
     * Returns all databases that have a value in the database property
     * "Database file is a master template"
     *
     * @return databases
     */
    List<DatabaseData> getAllTemplateDatabases();

    /**
     * Returns all the template names that exist on the server
     *
     * @return template names
     */
    Set<String> getAllTemplateNames();

    /**
     * Get all databases with the specified value in the database property
     * "Inherit design from master template"
     *
     * @param templateName template name or empty string
     * @return databases
     */
    List<DatabaseData> getDatabasesInheritingTemplate(String templateName);

    /**
     * Returns the scanned directory
     *
     * @return directory or empty string if the whole server was scanned
     */
    String getDirectory();

    /**
     * Returns the name of the scanned server
     *
     * @return server
     */
    String getServer();

    /**
     * Get all databases with the specified value in the database property
     * "Database file is a master template"
     *
     * @param templateName template name or empty string
     * @return databases
     */
    List<DatabaseData> getTemplateDatabases(String templateName);

  }

  public interface DesignRefreshBreakHandler {

    boolean shouldInterrupt();

  }

  /**
   * Scans the server data directory to find all databases that inherit their
   * design
   * from the specified template
   *
   * @param server       server
   * @param templateName template name
   * @return list of databases
   */
  List<DatabaseData> findAllDatabaseInheritingTemplate(String server, String templateName);

  DbDesign readDatabaseDesign(Database db);

  /**
   * Refresh the design of a database
   *
   * @param db                 database
   * @param serverWithTemplate server containing the template
   */
  void refreshDesign(Database db, String serverWithTemplate);

  /**
   * Refresh the design of a database with advanced options
   *
   * @param db                    database
   * @param serverWithTemplate    server containing the template
   * @param force                 true to force operation, even if destination "up
   *                              to date"
   * @param errIfTemplateNotFound true to return an error if the template is not
   *                              found
   * @param breakHandler          optional break handler to abort the operation
   *                              (e.g. for graphical user interfaces) or null
   */
  void refreshDesign(Database db, String serverWithTemplate, boolean force, boolean errIfTemplateNotFound,
      DesignRefreshBreakHandler breakHandler);

  /**
   * Fast scan of the server directory to get information about template
   * inheritance
   *
   * @param server    server to scan
   * @param directory restrict scan to a subdirectory or use empty string to scan
   *                  the whole server
   * @return scan result
   */
  DatabaseDesignAnalysis scanTemplateInheritanceOnServer(String server, String directory);

}
