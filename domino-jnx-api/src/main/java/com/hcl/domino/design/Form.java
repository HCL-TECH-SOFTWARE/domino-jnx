*
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

/**
 * Represents a Form design element in a database
 */
public interface Form extends GenericFormOrSubform<Form>, DesignElement.XPageAlternativeElement,
  DesignElement.XPageNotesAlternativeElement, DesignElement.AutoFrameElement {

  /**
   * Represents the type of document the UI should create when using this
   * form.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  enum Type {
    DOCUMENT, RESPONSE, RESPONSE_TO_RESPONSE
  }
  
  /**
   * Represents the modes for menu inclusion for the form in the UI.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  enum MenuInclusion {
    NONE, CREATE, CREATE_OTHER
  }
  
  /**
   * Represents the modes for version handling in the form.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  enum VersioningBehavior {
    NONE, NEW_AS_RESPONSES, PRIOR_AS_RESPONSES, NEW_AS_SIBLINGS
  }
  
  /**
   * Represents the modes for conflict handling in the form..
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  enum ConflictBehavior {
    CREATE_CONFLICTS, MERGE_CONFLICTS, MERGE_NO_CONFLICTS, DO_NOT_CREATE_CONFLICTS
  }
  
  /**
   * Determines what type of document - normal, response, or response-to-response -
   * the UI should create when using this form.
   * 
   * @return a {@link Type} instance
   * @since 1.0.33
   */
  Type getType();
  
  /**
   * Determines the mode for menu inclusion of the form in the UI.
   * 
   * @return a {@link MenuInclusion} instance
   * @since 1.0.33
   */
  MenuInclusion getMenuInclusionMode();
  
  /**
   * Determines whether the form should be included as an option in the Search Builder
   * UI.
   * 
   * @return {@code true} if the form should be included;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isIncludeInSearchBuilder();
  
  /**
   * Determines whether the form should be included in print options in the UI.
   * 
   * @return {@code true} if the form should be included in print options;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isIncludeInPrint();
  
  /**
   * Determines the version handling used by the form.
   * 
   * @return a {@link VersioningBehavior} instance
   * @since 1.0.33
   */
  VersioningBehavior getVersioningBehavior();
  
  /**
   * Determines whether, when {@link #getVersioningBehavior()} is not
   * {@link VersioningBehavior#NONE NONE}, new versions should be created automatically
   * on save.
   * 
   * @return {@code true} if new versions should be created automatically on save;
   *         {@code false} if they should be created only manually via the UI
   * @since 1.0.33
   */
  boolean isVersionCreationAutomatic();
  
  /**
   * Determines whether this form should be used as the default in the database for displaying
   * documents with no other form specified.
   * 
   * @return {@code true} if this is the default form for the database;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isDefaultForm();
  
  /**
   * Determines whether documents created with this form should have a copy of the form stored
   * as an item in the document.
   * 
   * @return {@code true} if documents should store a copy of the form;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isStoreFormInDocument();
  
  /**
   * Determines whether the form should be included in Field Exchange use.
   * 
   * @return {@code true} if the form can participate in Field Exchange;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isAllowFieldExchange();
  
  /**
   * Determines whether hide-when formulas and other aspects should be automatically refreshed
   * in the UI on applicable changes to the document.
   * 
   * @return {@code true} to automatically refresh field formulas;
   *         {@code false} to only do so when configured per-field or in code
   * @since 1.0.33
   */
  boolean isAutomaticallyRefreshFields();
  
  /**
   * Determines whether the form should be "anonymous", meaning that author and editor names are not
   * tracked in the documents.
   * 
   * @return {@code true} to make form use anonymous;
   *         {@code false} to use normal edit user tracking
   * @since 1.0.33
   */
  boolean isAnonymousForm();
  
  /**
   * Determines whether the form should choose a field to use for initial focus on open in the UI.
   * 
   * @return {@code true} to use initial focus behavior;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isUseInitialFocus();
  
  /**
   * Determines whether the form should focus onto a field when the F6 key is pressed in the
   * Notes client UI.
   * 
   * @return {@code true} to focus on F6 press;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isFocusOnF6();
  
  /**
   * Determines whether documents edited with this form should be automatically signed on save in the
   * UI.
   * 
   * @return {@code true} to automatically sign documents on save;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isSignDocuments();
  
  /**
   * Determines whether this form should participate in autosave in the UI when enabled in the client.
   * 
   * @return {@code true} to allow this form to participate in autosave;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isAllowAutosave();
  
  /**
   * Determines the conflict-handling behavior for the form.
   * 
   * @return a {@link ConflictBehavior} instance
   * @since 1.0.33
   */
  ConflictBehavior getConflictBehavior();
}
