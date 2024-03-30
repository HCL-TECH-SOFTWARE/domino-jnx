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

import java.util.List;
import java.util.Optional;

import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Document;

/**
 * Access to forms, views, resources read/write
 *
 * @author t.b.d
 */
public interface DesignElement {
  interface NamedDesignElement extends DesignElement {
    List<String> getAliases();

    String getTitle();

    /**
     * Sets the title of the design element.
     *
     * @param title the new element title and any aliases
     */
    void setTitle(String... title);
  }

  /**
   * This mixin interface describes a design element element that has
   * "display XPage instead" capabilities for web viewers.
   *
   * @author Jesse Gallagher
   * @since 1.0.27
   */
  interface XPageAlternativeElement extends DesignElement {
    Optional<String> getWebXPageAlternative();
  }

  /**
   * This mixin interface describes a design element element that has
   * "display XPage instead" capabilities for Notes viewers.
   *
   * @author Jesse Gallagher
   * @since 1.0.27
   */
  interface XPageNotesAlternativeElement extends DesignElement {
    Optional<String> getNotesXPageAlternative();
  }
  
  /**
   * This mixin interface describes a non-XPages design element that can
   * have a setting for NSF-level theme behavior.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface ThemeableClassicElement {
    ClassicThemeBehavior getClassicThemeBehavior();
  }
  
  /**
   * This mixin interface describes design elements that have "Auto Frame"
   * behavior available.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface AutoFrameElement extends DesignElement {
    /**
     * Retrieves the name of the frameset used for "Auto Frame" behavior in the
     * Notes client, if specified.
     * 
     * @return an {@link Optional} describing the auto-frame frameset name, or
     *         an empty one if this is unspecified
     */
    Optional<String> getAutoFrameFrameset();
    /**
     * Retrieves the target frame ID used for "Auto Frame" behavior in the
     * Notes client, if specified.
     * 
     * @return an {@link Optional} describing the auto-frame target frame, or
     *         an empty one if this is unspecified
     */
    Optional<String> getAutoFrameTarget();
  }
  
  /**
   * This mixin interface describes design elements that have an action bar
   * displayed across the top.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface ActionBarElement extends DesignElement {
    /**
     * Retrieves a view onto this design element's action bar and the associated
     * actions.
     * 
     * @return an {@link ActionBar} instance
     * @since 1.0.32
     */
    ActionBar getActionBar();
  }
  
  /**
   * This mixin interface describes design elements that can be restricted to specific
   * names and roles.
   * 
   * @author Jesse Gallagher
   * @since 1.0.35
   */
  interface ReadersRestrictedElement extends DesignElement {
    /**
     * Retrieves the list of users, groups, and roles allowed to access this design element.
     * 
     * @return an {@link Optional} describing the list of reader names, or an empty one
     *         if this element is not reader-restricted
     * @since 1.0.35
     */
    Optional<List<String>> getReaders();
  }
  
  /**
   * Deletes the design element note from the database.
   * 
   * @since 1.1.2
   */
  void delete();
  
  /**
   * Deletes the design element note from the database.
   *
   * @param noStub whether to purge the note without leaving a deletion stub
   */
  void delete(boolean noStub);
  
  /**
   * @return the comment assigned to the design element
   * @since 1.0.24
   */
  String getComment();

  String getDesignerVersion();

  /**
   * @return the document underlying this design element
   * @since 1.0.18
   */
  Document getDocument();
  
  /**
   * Convenience method to get the UNID of the underlying design document
   * 
   * @return UNID
   * @since 1.6.7
   */
  default String getUNID() {
    return getDocument().getUNID();
  }
  
  /**
   * Convenience method to get the note ID of the underlying design document
   * 
   * @return the integer note ID
   * @since 1.6.7
   */
  default int getNoteID() {
    return getDocument().getNoteID();
  }
  
  /**
   * Retrieves the name of the note-specific template this design element
   * is set to derive from.
   * 
   * @return an {@link Optional} describing the note's template, or an empty
   *         one if this note does not derive from a distinct template
   * @since 1.1.2
   */
  Optional<String> getTemplateName();

  boolean isHideFromMobile();

  boolean isHideFromNotes();

  boolean isHideFromWeb();

  boolean isProhibitRefresh();

  boolean save();

  /**
   * Sets a comment for the design element
   *
   * @param comment the comment to set
   * @since 1.0.24
   */
  void setComment(String comment);

  void setHideFromMobile(boolean hideFromMobile);

  void setHideFromNotes(boolean hideFromNotes);

  void setHideFromWeb(boolean hideFromWeb);

  void setProhibitRefresh(boolean prohibitRefresh);
  
  /**
   * Sets the name of the note-specific template this design element
   * should derive from.
   * 
   * @param templateName the name of the template the element should
   *                     derive from, or an empty value to un-set this
   * @since 1.1.2
   */
  void setTemplateName(String templateName);

  void sign();

  void sign(UserId id);
  
  /**
   * Determines whether the design element should be accessible to
   * sub-Reader users with Public Access rights.
   * 
   * @return {@code true} if the element should allow public access;
   *         {@code false} otherwise
   */
  boolean isAllowPublicAccess();
  
  /**
   * Determines whether the design element should be hidden from normal
   * design lists.
   * 
   * @return {@code true} if the element should be hidden from design
   *         lists; {@code false} otherwise
   * @since 1.39.0
   */
  boolean isHideFromDesignList();
  
  /**
   * Sets whether the design element should be hidden from normal
   * design lists.
   * 
   * @param hideFromDesignList {@code true} to mark the element as hidden
   *         from design lists; {@code false} otherwise
   * @since 1.39.0
   */
  void setHideFromDesignList(boolean hideFromDesignList);
  
  /**
   * Determines whether the design element should be hidden from the specified
   * major version of Notes.
   * 
   * <p>Note: currently only versions 3 through 9 have applicable flags.</p>
   * 
   * @param version the version to query
   * @return {@code true} if the element is hidden from the specified version;
   *         {@code false} otherwise
   * @throws IllegalArgumentException if {@code version} is not within 3 through
   *         9
   * @since 1.39.0
   */
  boolean isHideFromNotesVersion(int version);
  
  /**
   * Sets whether the design element should be hidden from the specified major
   * version of Notes.
   * 
   * <p>Note: currently only versions 3 through 9 have applicable flags.</p>
   * 
   * @param version the version to set the flag for
   * @param hide {@code true} if the element should be hidden from the specified
   *        version; {@code false} otherwise
   * @throws IllegalArgumentException if {@code version} is not within 3 through
   *         9
   * @since 1.39.0
   */
  void setHideFromNotesVersion(int version, boolean hide);
}
