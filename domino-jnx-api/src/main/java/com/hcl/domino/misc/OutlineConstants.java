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
package com.hcl.domino.misc;

public interface OutlineConstants {
  
  /* Entry Type */
  short SITEMAP_SPECIAL_ENTRY_START = (NotesConstants.MAXWORD/2);

  short SITEMAP_DEFAULT_HOME_PAGE_ENTRY   = (NotesConstants.MAXWORD-31);    /*DefaultHomePage*/
  short SITEMAP_BROWSEDB_ENTRY    = (NotesConstants.MAXWORD-30);    /*Browse Databases*/
  short SITEMAP_GLOBAL_WORKBENCH_ENTRY  = (NotesConstants.MAXWORD-29);    /*Domino Global Workbench*/
  short SITEMAP_MOBILE_DESIGNER_ENTRY = (NotesConstants.MAXWORD-28);    /*Mobile Notes Designer*/
  short SITEMAP_WEB_STUDIO_ENTRY  = (NotesConstants.MAXWORD-27);    /*WebSphere Studio*/
  short SITEMAP_NOTES_ENTRY       = (NotesConstants.MAXWORD-26);    /*Notes*/
  short SITEMAP_SAMETIME_ENTRY    = (NotesConstants.MAXWORD-25);    /*Sametime*/
  short SITEMAP_ADMIN_ENTRY       = (NotesConstants.MAXWORD-24);    /*Admin*/
  short SITEMAP_DESIGNER_ENTRY    = (NotesConstants.MAXWORD-23);    /*Designer*/
  short SITEMAP_DESK_ENTRY        = (NotesConstants.MAXWORD-22);    /*Workspace*/
  short SITEMAP_BCASE_ENTRY       = (NotesConstants.MAXWORD-21);    /*Replication*/
  short SITEMAP_HOME_PAGE_ENTRY   = (NotesConstants.MAXWORD-20);    /*Home Page*/

  short SITEMAP_IMPLIED_FOLDER_ENTRY = (NotesConstants.MAXWORD-19);    /* Implied folder  */
  short SITEMAP_ARCHIVE_ENTRY = (NotesConstants.MAXWORD-18);    /* archive entry expanded from
                                                          archive placeholder */

  short SITEMAP_ARCHIVE_PLACEHOLDER_END_ENTRY = (NotesConstants.MAXWORD-17);    /* An ending indicator
                                                          for subsequent refreshes. 
                                                          Temporary, not saved
                                                          to disk. */
  short SITEMAP_ARCHIVE_PLACEHOLDER_ENTRY = (NotesConstants.MAXWORD-16);    /* A placeholder to be replaced by all
                                                                  archive profile folders */

  /* Taking advantage of the sitemap creation checks for special mail folders */
  short SITEMAP_TOOLBAR_ENTRY = (NotesConstants.MAXWORD-15);    /* Toolbar entry */
  short SITEMAP_INBOX_ENTRY   = (NotesConstants.MAXWORD-14);    /* Mail Inbox folder  */
  short SITEMAP_RULES_ENTRY   = (NotesConstants.MAXWORD-13);    /* Mail Rules folder  */
  short SITEMAP_DRAFTS_ENTRY  = (NotesConstants.MAXWORD-12);    /* Mail Drafts folder */
  short SITEMAP_TRASH_ENTRY   = (NotesConstants.MAXWORD-11);    /* Mail Trash folder  */
  short SITEMAP_SENT_ENTRY    = (NotesConstants.MAXWORD-10);    /* Mail Sent folder   */


  short SITEMAP_HISTORY_ENTRY     = (NotesConstants.MAXWORD-9); /* History folder */

  short SITEMAP_OTHER_VIEWS_END_ENTRY = (NotesConstants.MAXWORD-8); /* An ending indicator
                                                          for subsequent refreshes. 
                                                          Temporary, not saved
                                                          to disk. */
  short SITEMAP_OTHER_FOLDERS_END_ENTRY = (NotesConstants.MAXWORD-7); /* An ending indicator
                                                          for subsequent refreshes. 
                                                          Temporary, not saved
                                                          to disk. */

  short SITEMAP_NS_WEBBROWSER_ENTRY  = (NotesConstants.MAXWORD-6); /* Loads in user's favorites from NS.
                                                          Added by the UI and
                                                          populated by sitemap
                                                          during an expand. */

  short SITEMAP_IE_WEBBROWSER_ENTRY  = (NotesConstants.MAXWORD-5); /* Loads in user's favorites from IE.
                                                          Added by the UI and
                                                          populated by sitemap
                                                          during an expand. */

  short SITEMAP_CREATE_ENTRY      = (NotesConstants.MAXWORD-4); /* A drop point which causes
                                                          a new entry to get created. */

  short SITEMAP_OTHER_VIEWS_ENTRY = (NotesConstants.MAXWORD-3); /* A placeholder to be replaced by all
                                                          shared views not specifically 
                                                          mentioned */
  short SITEMAP_OTHER_FOLDERS_ENTRY  = (NotesConstants.MAXWORD-2); /* A placeholder to be replaced by all
                                                          shared folders not specifically 
                                                          mentioned */
  short SITEMAP_OPEN_ARCHIVE_LOGS_NBP = (NotesConstants.MAXWORD-1); /* Opening the archive logs from Notes Web using Notes Browser Plugin
                                                            for local archiving feature (Notes Web Companion)*/

  
  /* Flags */
  int SITEMAP_TOTHISDB_ENTRYFLAG = 0x00000001;   /* Link resolves to this database */
  int SITEMAP_HIDDEN_NOTES_ENTRYFLAG = 0x00000002;   /* Alias for old hidden flag */
  int SITEMAP_HIDDEN_ENTRYFLAG = 0x00000002; /* Used to specifically hide a view or folder which
                                              otherwise would have been displayed in a
                                              default placeholder list. */
  int SITEMAP_PRIVATE_ENTRYFLAG = 0x00000004;/* Applicable to other views and other folders only. */
  int SITEMAP_SORT_ENTRYFLAG = 0x00000008;   /* Sort this entry with respect to its peers
                                              that are marked for sorting. Sorting will only occur within
                                              contiguous sorted entries. This is powerful where
                                              the names may change for internationalzation
                                              or where the display may be computed. */
  int SITEMAP_HIDDEN_WEB_ENTRYFLAG = 0x00000010;  /*  this entry is hidden from the web */
  int SITEMAP_USEHIDEWHENFORMULA_ENTRYFLAG  = 0x00000020; /*  use the hidewhen formula if we have one */
  int SITEMAP_EXPANDED_ENTRYFLAG = 0x00000040;  /* Persist the expansion state.  This can be
                                              used by the designer to pre-expand a branch,
                                              as well as bookmarks to remember state of
                                              each page as the user left it. */
  int SITEMAP_DEFAULT_ENTRYFLAG  = 0x00000080;  /* An individual item in the sitemap may be
                                              marked as a default. Setting this on an
                                              entry will clear it on an previous entry. 
                                              In bookmarks, this is used to mark the home
                                              link that is opened in a special way on startup. */
  int SITEMAP_EXPANDABLE_ENTRYFLAG = 0x00000100;  /* Used to indicate that an element may be 
                                              expanded, even if there are no children.  Used
                                              by bookmarks to expand a folder on a click. */
  int SITEMAP_REFUSESEL_ENTRYFLAG = 0x00000200;  /* used to signal an entry that should never accept 
                                              selection */ 
  int SITEMAP_NEVERIMAGE_ENTRYFLAG = 0x00000400; /* used to indicate that this entry never, never
                                              wants an image */    
  int SITEMAP_MODIFIEDTITLE_ENTRYFLAG = 0x00000800;  /* Set if the user explicitly modified the title of an
                                                    entry (mostly for bookmarks). */
  int SITEMAP_RTLREADING_ENTRYFLAG = 0x00001000;     /* Set if the user sets the text reading order of an entry is to be 
                                                    Right-to-Left reading order. */
  int SITEMAP_OSELEMENT_ENTRYFLAG = 0x00002000;  /* Entry is a link to an operating system item */

  int SITEMAP_NAP_STILL_EXISTS_ENTRYFLAG = 0x00004000;  /* Notes App Plugin - still exist flag */
  int SITEMAP_WORKSPACE_ENTRYFLAG        = 0x00008000;  /* Temporary Workspace entry */
  int SITEMAP_NAP_CHECK_FOLDER_ENTRYFLAG = 0x00010000;  /* Notes App Plugin - folder changed and need checked */

  int SITEMAP_POSITION_SPECIFIED_ENTRYFLAG = 0x00020000;  /* Entries position in a grid is specified */
  int SITEMAP_READONLY_ENTRYFLAG         = 0x00040000;  /* if user shouldn't be allowed to change title */
  int SITEMAP_IMAGE_DONT_STRECH_ENTRYFLAG  = 0x00080000;  /* don't strech the entry image */
  int SITEMAP_QUERYVIEW_ENTRYFLAG        = 0x00100000;  /* applicable to DB2 based query views */
  int SITEMAP_ADD_SEPARATOR_ENTRYFLAG    = 0x00200000;  /* add separator above the entry */
  int SITEMAP_IS_INHERITED_ENTRYFLAG     = 0x00400000;  /* add separator above the entry */
  int SITEMAP_DONOTSAVE_ENTRYFLAG        = 0x80000000;  /* Used to add something to the internal list
                                                        which should not be saved back when the list is saved. */
}
