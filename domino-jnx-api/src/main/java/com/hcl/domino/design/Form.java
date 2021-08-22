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

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.design.form.AutoLaunchHideWhen;
import com.hcl.domino.design.form.AutoLaunchType;
import com.hcl.domino.design.form.AutoLaunchWhen;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.richtext.NotesBitmap;
import com.hcl.domino.richtext.records.CDHeader;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.ColorValue;

/**
 * Represents a Form design element in a database
 */
public interface Form extends GenericFormOrSubform<Form>, DesignElement.XPageAlternativeElement,
  DesignElement.XPageNotesAlternativeElement, DesignElement.AutoFrameElement, DesignElement.ThemeableClassicElement {
  
  /**
   * Represents the settings for inheritance of the entire selected document
   * in new documents.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  interface InheritanceSettings {
    String getTargetField();
    InheritanceFieldType getType();
  }
  
  /**
   * Represents the settings for object auto-launch when opening a document with the
   * form in the client UI.
   * 
   * @author Jesse Gallagher
   * @since 1.0.34
   */
  interface AutoLaunchSettings {
    /**
     * Determines the auto-launch type for this form.
     * 
     * @return a {@link AutoLaunchType} instance
     */
    AutoLaunchType getType();
    
    /**
     * Retrieves the OLE GUID of the object class to launch when {@link #getType()} is
     * {@link AutoLaunchType#OLE_CLASS}.
     * 
     * @return an {@link Optional} describing the OLE object type to create, or
     *         an empty one if this is not applicable
     */
    Optional<String> getOleType();
    
    /**
     * Determines whether the auto-launched object is launched in-place or in its own
     * window when auto-launch is enabled and {@link #isPresentDocumentAsModal()} is
     * {@code false}.
     * 
     * @return {@code true} to launch the object in-place when applicable;
     *         {@code false} otherwise
     */
    boolean isLaunchInPlace();
    
    /**
     * Determines whether to present the document as a modal dialog when auto-launching.
     * 
     * @return {@code true} to launch the document as a modal;
     *         {@code false} otherwise
     */
    boolean isPresentDocumentAsModal();
    
    /**
     * Determines whether to create the auto-launch object in the first rich text field.
     * 
     * @return {@code true} if the object will use the first RT field on the form;
     *         {@code false} otherwise
     */
    boolean isCreateObjectInFirstRichTextField();
    
    /**
     * Determines the target rich text field for the object, if {@link #getType()} is an
     * applicable type, {@link #isCreateObjectInFirstRichTextField()} is {@code false},
     * and a field is specified.
     * 
     * @return an {@link Optional} describing the target rich text field, or an empty one
     *         if this is not applicable or the field is not specified
     */
    Optional<String> getTargetRichTextField();
    
    /**
     * Determines the conditions for launching an object when {@link #getType()} is an
     * applicable type.
     * 
     * @return a {@link Set} of {@link AutoLaunchWhen} instances
     */
    Set<AutoLaunchWhen> getLaunchWhen();
    
    /**
     * Determines the conditions for hiding an object when {@link #getHideWhen()} is an
     * applicable type.
     * 
     * @return a {@link Set} of {@link AutoLaunchHideWhen} instances
     */
    Set<AutoLaunchHideWhen> getHideWhen();
  }
  
  /**
   * Represents settings related to the rendering of the form when rendered
   * using the classic web renderer.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  interface WebRenderingSettings {
    
    /**
     * Determines whether the Domino server should render full rich content (e.g. styled text and editable
     * fields) when a document is displayed on the web with this form.
     * 
     * @return {@code true} to render rich content on the web;
     *         {@code false} otherwise
     */
    boolean isRenderRichContentOnWeb();
    
    /**
     * Retrieves the content type to use when displaying documents with this form on the web when
     * {@link #isRenderRichContentOnWeb()} is {@code false}.
     * 
     * <p>When the document should be rendered as {@code text/html}, this value will be an {@code Optional}
     * containing an empty string.</p>
     * 
     * @return an {@link Optional} describing the content type when rendering a document on the web,
     *         or an empty one if {@link #isRenderRichContentOnWeb()} is {@code true}
     * @since 1.0.33
     */
    Optional<String> getWebMimeType();
    
    /**
     * Retrieves the character set to use when displaying documents using this form on the web, if one is
     * set
     * 
     * @return an {@link Optional} describing the textual name of the character set to use when displaying
     *         on the web, or an empty one if the server should use the default
     * @since 1.0.33
     */
    Optional<String> getWebCharset();
    
    /**
     * Determines whether all fields, including those hidden from view, should have representations in the
     * HTML generated for web access.
     * 
     * @return {@code true} if all fields should be included in HTML;
     *         {@code false} otherwise
     * @since 1.0.33
     */
    boolean isGenerateHtmlForAllFields();
    
    /**
     * Retrieves the color used for active links when using HTML controls.
     * 
     * @return a {@link ColorValue} representing the active link color
     */
    ColorValue getActiveLinkColor();
    
    /**
     * Retrieves the color used for unvisited links when using HTML controls.
     * 
     * @return a {@link ColorValue} representing the unvisited link color
     */
    ColorValue getUnvisitedLinkColor();
    
    /**
     * Retrieves the color used for visited links when using HTML controls.
     * 
     * @return a {@link ColorValue} representing the visited link color
     */
    ColorValue getVisitedLinkColor();
  }
  
  /**
   * Represents settings related to the form background ("paper") color, image,
   * and behavior.
   * 
   * @author Jesse Gallagher
   * @since 1.0.34
   */
  interface BackgroundSettings {
    /**
     * Retrieves the background color as a standard Notes UI color if it is set as such.
     * 
     * @return an {@link Optional} describing the {@link StandardColors} instance for the background
     *         color, or an empty one if the color is non-standard
     */
    Optional<StandardColors> getStandardBackgroundColor();
    
    /**
     * Retrieves the background color as a color value.
     * 
     * @return a {@link ColorValue} instance
     */
    ColorValue getBackgroundColor();
    
    /**
     * Retrieves the image resource reference used for the background image, if set as such.
     * 
     * @return an {@link Optional} describing the background image resource, or an empty one if
     *         the background is not an image resource
     */
    Optional<CDResource> getBackgroundImageResource();
    
    /**
     * Retrieves the image used for the background, if set as such.
     * 
     * @return an {@link Optional} describing a {@link NotesBitmap}, or an empty one if the
     *         background is not a pasted image
     */
    Optional<NotesBitmap> getBackgroundImage();
    
    /**
     * Determines whether the background image, if present, should be hidden when editing the form
     * in design mode.
     * 
     * @return {@code true} if the background image should be hidden during design;
     *         {@code false} otherwise
     */
    boolean isHideGraphicInDesignMode();
    
    /**
     * Determines whether the background image, if present, should be hidden when the form is viewed
     * on a 16-color screen.
     * 
     * @return {@code true} if the background image should be hidden on 4-bit-color displays;
     *         {@code false} otherwise
     */
    boolean isHideGraphicOn4BitColor();
    
    /**
     * Determines whether the background display properties can be overridden by the user.
     * 
     * @return {@code true} if the display properties are user-customizable;
     *         {@code false} otherwise
     */
    boolean isUserCustomizable();
    
    /**
     * Retrieves the repeat mode for the background image.
     * 
     * @return an {@link ImageRepeatMode} for the background image
     */
    ImageRepeatMode getBackgroundImageRepeatMode();
  }
  
  /**
   * Represents settings related to the in-form (non-print) header frame.
   * 
   * @author Jesse Gallagher
   * @since 1.0.34
   */
  interface HeaderFrameSettings {
    /**
     * Determines whether the form uses a header frame at the top of the body area.
     * 
     * @return {@code true} to use a header frame region in the body;
     *         {@code false} otherwise
     */
    boolean isUseHeader();
    
    /**
     * Determines the sizing method for the header, if enabled.
     * 
     * @return an {@link Optional} describing a {@link FrameSizingType} instance for the
     *         header, or an empty one if the header is not enabled
     */
    Optional<FrameSizingType> getHeaderSizingType();
    
    /**
     * Determines the size of the header, if enabled.
     * 
     * <p>The meaning of this number is determined by {@link #getHeaderSizingType()}.
     * 
     * @return an {@link OptionalInt} describing the pixel or percentage size of the header,
     *         or an empty one if the header is not enabled
     */
    OptionalInt getHeaderSize();
    
    /**
     * Determines the scrolling behavior of the header, if enabled.
     * 
     * @return an {@link Optional} describing a {@link FrameScrollStyle} instance for the
     *         header, or an empty one if the header is not enabled
     */
    Optional<FrameScrollStyle> getScrollStyle();
    
    /**
     * Determines whether header resizing is enabled when the header itself is enabled.
     * 
     * @return {@code true} if the header is user-resizable;
     *         {@code false} otherwise
     */
    boolean isAllowResizing();
    
    /**
     * Determines the width, in pixels, of the border when the header is enabled.
     * 
     * @return an {@link OptionalInt} describing the pixel size of the border,
     *         or an empty one if the header is not enabled
     */
    OptionalInt getBorderWidth();
    
    /**
     * Determines the color of the border when the header is enabled.
     * 
     * @return an {@link Optional} describing the {@link ColorValue} of the border,
     *         or an empty one if the header is not enabled
     */
    Optional<ColorValue> getBorderColor();
    
    /**
     * Determines whether the header border should use 3D shading when the header
     * is enabled.
     * 
     * @return {@code true} if the header border should use 3D shading when shown;
     *         {@code false} otherwise
     */
    boolean isUse3DShading();
  }
  
  /**
   * Represents printing settings for the form.
   * 
   * @author Jesse Gallagher
   * @since 1.0.34
   */
  interface PrintSettings {
    
    /**
     * Determines whether the header and/or footer, if specified, should be printed on
     * the first page of print output.
     * 
     * @return {@code true} to print the header and footer on the first printed page;
     *         {@code false} to omit them
     */
    boolean isPrintHeaderAndFooterOnFirstPage();

    /**
     * Retrieves the print header content, if specified.
     * 
     * @return an {@link Optional} describing a {@link CDHeader} object, or an empty one
     *         if a print header is not specified
     */
    Optional<CDHeader> getPrintHeader();
    
    /**
     * Retrieves the print footer content, if specified.
     * 
     * @return an {@link Optional} describing a {@link CDHeader} object, or an empty one
     *         if a print footer is not specified
     */
    Optional<CDHeader> getPrintFooter();
  }

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
   * Represents the modes for storing an inherited document in a rich-text field.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  enum InheritanceFieldType {
    LINK, COLLAPSIBLE_RICH_TEXT, RICH_TEXT
  }
  
  /**
   * Represents behaviors for showing a context pane on document open.
   * 
   * @author Jesse Gallagher
   * @since 1.0.33
   */
  enum ContextPaneBehavior {
    NONE, DOCLINK, PARENT
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
  
  /**
   * Determines whether new documents created with this form should inherit item values from the
   * currently-selected document.
   * @return {@code true} if item values should be inherited on create;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isInheritSelectedDocumentValues();
  
  /**
   * Determines how newly-created documents with this form should inherit the selected document,
   * if at all.
   * 
   * @return an {@link Optional} describing how the selected document should be inherited into
   *         newly-created documents, or an empty one if they should not be
   * @since 1.0.33
   */
  Optional<InheritanceSettings> getSelectedDocumentInheritanceBehavior();
  
  /**
   * Determines whether existing documents opened with this form should begin in edit mode.
   * 
   * @return {@code true} if existing documents should be opened in edit mode;
   *         {@code false} to open in read mode
   * @since 1.0.33
   */
  boolean isAutomaticallyEnableEditMode();
  
  /**
   * Determines the behavior of the context pane when opening documents with this form in the
   * UI.
   * 
   * @return a {@link ContextPaneBehavior} instance
   * @since 1.0.33
   */
  ContextPaneBehavior getContextPaneBehavior();
  
  /**
   * Determines whether the UI should show the mail-send dialog when closing a document with this
   * form.
   * 
   * @return {@code true} to present the mail-send dialog on close;
   *         {@code false} otherwise
   * @since 1.0.33
   */
  boolean isShowMailDialogOnClose();
  
  /**
   * Retrieves an object that provides a view onto this form's web rendering
   * settings.
   * 
   * @return a {@link WebRenderingSettings} instance
   * @since 1.0.33
   */
  WebRenderingSettings getWebRenderingSettings();
  
  /**
   * Retrieves the name of the default data connection to use when integrating with relational
   * databases.
   * 
   * @return an {@link Optional} describing the name of the Data Connection design element to
   *         use by default, or an empty one if this is not configured
   * @since 1.0.33
   */
  Optional<String> getDefaultDataConnectionName();
  
  /**
   * Retrieves the name of the default metadata object to use when integrating with relational
   * databases.
   * 
   * @return an {@link Optional} describing the name metadata object to
   *         use by default, or an empty one if this is not configured
   * @since 1.0.33
   */
  Optional<String> getDefaultDataConnectionObject();
  
  /**
   * Retrieves an object that provides a view onto this form's auto-launch settings.
   * 
   * @return a {@link AutoLaunchSettings} instance
   * @since 1.0.34
   */
  AutoLaunchSettings getAutoLaunchSettings();
  
  /**
   * Retrieves an object that provides a view onto this form's background image and color
   * settings.
   * 
   * @return a {@link BackgroundSettings} instance
   * @since 1.0.34
   */
  BackgroundSettings getBackgroundSettings();
  
  /**
   * Retrieves an object that provides a view onto the settings for the in-form (non-print)
   * header frame of the form.
   * 
   * @return a {@link HeaderFrameSettings} instance
   * @since 1.0.34
   */
  HeaderFrameSettings getHeaderFrameSettings();
  
  /**
   * Retrieves an object that provides a view onto the settings for printing the form.
   * 
   * @return a {@link PrintSettings} instance
   * @since 1.0.34
   */
  PrintSettings getPrintSettings();
}