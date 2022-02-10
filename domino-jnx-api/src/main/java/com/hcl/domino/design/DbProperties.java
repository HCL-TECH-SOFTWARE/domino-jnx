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

/**
 * Access to a database design. Search for design, database as constructor
 * parameter
 *
 * @author t.b.d
 */
public interface DbProperties extends DesignElement {

  public enum LaunchContextNotes {
    LAST_VIEWED(""), //$NON-NLS-1$
    ABOUT_DOC("openaboutdocument"), //$NON-NLS-1$
    NAVIGATOR("opennavigator"), //$NON-NLS-1$
    NAVIGATOR_IN_WINDOW("opennavigatorinwindow"), //$NON-NLS-1$
    FRAMESET("openframeset"), //$NON-NLS-1$
    XPAGE("openxpage"), //$NON-NLS-1$
    ABOUT_ATTACHMENT("openfirstaboutattachment"), //$NON-NLS-1$
    ABOUT_DOCLINK("openfirstdoclink"), //$NON-NLS-1$
    COMPOSITE_APP("opencompapp"); //$NON-NLS-1$

    String propName;

    LaunchContextNotes(final String propName) {
      this.propName = propName;
    }

    public String getPropertyName() {
      return this.propName;
    }
  }

  public enum LaunchContextWeb {
    NOTES_LAUNCH(""), //$NON-NLS-1$
    ABOUT_DOC("openaboutdocument"), //$NON-NLS-1$
    NAVIGATOR("opennavigator"), //$NON-NLS-1$
    NAVIGATOR_IN_WINDOW("opennavigatorinwindow"), //$NON-NLS-1$
    FRAMESET("openframeset"), //$NON-NLS-1$
    PAGE("openpage"), //$NON-NLS-1$
    XPAGE("openxpage"), //$NON-NLS-1$
    ABOUT_DOCLINK("openfirstdoclink"), //$NON-NLS-1$
    SPECIFIC_DOC_LINK("openspecifieddoclink"), //$NON-NLS-1$
    FIRST_DOC_IN_VIEW("openfirstdocumentinview"); //$NON-NLS-1$

    String propName;

    LaunchContextWeb(final String propName) {
      this.propName = propName;
    }

    public String getPropertyName() {
      return this.propName;
    }
  }

  public enum PreviewPaneDefault {
    BOTTOM(""), BOTTOM_RIGHT("bottomright"), RIGHT("right"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    String propName;

    PreviewPaneDefault(final String propName) {
      this.propName = propName;
    }

    public String getPropertyName() {
      return this.propName;
    }
  }

  public enum PropertiesOptions {
    USE_JS("usejavascriptinpages"), REQUIRE_SSL("requiressl"), NO_URL_OPEN("nourlopen"), ENHANCED_HTML("$AllowPost8HTML"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    BLOCK_ICAA("$DisallowOpenInNBP"), DISABLE_BACKGROUND_AGENTS("allowbackgroundagents"), ALLOW_STORED_FORMS("allowstoredforms"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    DEFER_IMAGE_LOADING("imageloadsdeferred"), ALLOW_DOC_LOCKING("allowdocumentlocking"), INHERIT_OS_THEME("inheritostheme"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ALLOW_DESIGN_LOCKING("allowdesignlocking"), SHOW_IN_OPEN_DIALOG("showinopendialog"), MULTI_DB_INDEXING("multidbindexed"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    MODIFIED_NOT_UNREAD("markmodifiedunread"), MARK_PARENT_REPLY_FORWARD("trackreplyforward"), //$NON-NLS-1$ //$NON-NLS-2$
    INHERIT_FROM_TEMPLATE("fromtemplate"),  //$NON-NLS-1$
    DB_IS_TEMPLATE("templatename"), ADVANCED_TEMPLATE("advancedtemplate"), MULTILINGUAL("multilingual"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    DONT_MAINTAIN_UNREAD("maintainunread"), REPLICATE_UNREAD("replicateunread"), OPTIMIZE_DOC_MAP("optimizetablebitmap"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    DONT_OVERWRITE_FREE_SPACE("overwritefreespace"), MAINTAIN_LAST_ACCESSED("savelastaccessed"), //$NON-NLS-1$ //$NON-NLS-2$
    DISABLE_TRANSACTION_LOGGING("logtransactions"), NO_SPECIAL_RESPONSE_HIERARCHY("allowspecialhierarchy"), USE_LZ1("uselz1"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    NO_HEADLINE_MONITORING("allowheadlinemonitoring"), ALLOW_MORE_FIELDS("increasemaxfields"), //$NON-NLS-1$ //$NON-NLS-2$
    SUPPORT_RESPONSE_THREADS("supportrespthread"), NO_SIMPLE_SEARCH("nosimplesearch"), COMPRESS_DESIGN("compressdesign"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    COMPRESS_DATA("compressdata"), NO_AUTO_VIEW_UPDATE("noautoviewupdate"), NO_EXPORT_VIEW("$DisableExport"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ALLOW_SOFT_DELETE("allowsoftdeletion"), SOFT_DELETE_EXPIRY("softdeletionsexpirein"), MAX_UPDATED_BY("maxupdatedbyentries"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    MAX_REVISIONS("maxrevisionentries"), ALLOW_DAS("$AllowRESTDbAPI"), DAOS_ENABLED("$Daos"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    LAUNCH_XPAGE_ON_SERVER("$LaunchXPageRunOnServer"), DOCUMENT_SUMMARY_16MB("$LargeSummary"); //$NON-NLS-1$ //$NON-NLS-2$

    String propName;

    PropertiesOptions(final String propName) {
      this.propName = propName;
    }

    public String getPropertyName() {
      return this.propName;
    }

  }

  public enum UnreadReplicationSetting {
    NEVER(""), CLUSTER("cluster"), ALL_SERVERS("all"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    String propName;

    UnreadReplicationSetting(final String propName) {
      this.propName = propName;
    }

    public String getPropertyName() {
      return this.propName;
    }
  }

  boolean isGenerateEnhancedHtml();

  void setGenerateEnhancedHtml(boolean generateEnhancedHtml);
}