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
package com.hcl.domino.commons.design;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.design.agent.DesignFormulaAgentImpl;
import com.hcl.domino.commons.design.agent.DesignImportedJavaAgentImpl;
import com.hcl.domino.commons.design.agent.DesignJavaAgentImpl;
import com.hcl.domino.commons.design.agent.DesignLotusScriptAgentImpl;
import com.hcl.domino.commons.design.agent.DesignSimpleActionAgentImpl;
import com.hcl.domino.commons.design.simpleaction.DefaultCopyToDatabaseAction;
import com.hcl.domino.commons.design.simpleaction.DefaultDeleteDocumentAction;
import com.hcl.domino.commons.design.simpleaction.DefaultFolderBasedAction;
import com.hcl.domino.commons.design.simpleaction.DefaultModifyByFormAction;
import com.hcl.domino.commons.design.simpleaction.DefaultModifyFieldAction;
import com.hcl.domino.commons.design.simpleaction.DefaultReplyAction;
import com.hcl.domino.commons.design.simpleaction.DefaultRunFormulaAction;
import com.hcl.domino.commons.design.simpleaction.DefaultSendDocumentAction;
import com.hcl.domino.commons.design.simpleaction.DefaultSendMailAction;
import com.hcl.domino.commons.design.simpleaction.DefaultSendNewsletterAction;
import com.hcl.domino.commons.design.simplesearch.DefaultByAuthorTerm;
import com.hcl.domino.commons.design.simplesearch.DefaultByDateFieldTerm;
import com.hcl.domino.commons.design.simplesearch.DefaultByFieldTerm;
import com.hcl.domino.commons.design.simplesearch.DefaultByFolderTerm;
import com.hcl.domino.commons.design.simplesearch.DefaultByNumberFieldTerm;
import com.hcl.domino.commons.design.simplesearch.DefaultExampleFormTerm;
import com.hcl.domino.commons.design.simplesearch.DefaultTextTerm;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.AboutDocument;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.CompositeApplication;
import com.hcl.domino.design.CompositeComponent;
import com.hcl.domino.design.DatabaseScriptLibrary;
import com.hcl.domino.design.DbProperties;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.FileResource;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.Frameset;
import com.hcl.domino.design.ImageResource;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.JavaScriptLibrary;
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.design.Navigator;
import com.hcl.domino.design.Outline;
import com.hcl.domino.design.Page;
import com.hcl.domino.design.ScriptLibrary;
import com.hcl.domino.design.ServerJavaScriptLibrary;
import com.hcl.domino.design.SharedActions;
import com.hcl.domino.design.SharedColumn;
import com.hcl.domino.design.SharedField;
import com.hcl.domino.design.StyleSheet;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.Theme;
import com.hcl.domino.design.UsingDocument;
import com.hcl.domino.design.View;
import com.hcl.domino.design.WiringProperties;
import com.hcl.domino.design.XPage;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.agent.DesignFormulaAgent;
import com.hcl.domino.design.agent.DesignImportedJavaAgent;
import com.hcl.domino.design.agent.DesignJavaAgent;
import com.hcl.domino.design.agent.DesignLotusScriptAgent;
import com.hcl.domino.design.agent.DesignSimpleActionAgent;
import com.hcl.domino.design.simpleaction.FolderBasedAction;
import com.hcl.domino.design.simpleaction.ReadMarksAction;
import com.hcl.domino.design.simpleaction.RunAgentAction;
import com.hcl.domino.design.simpleaction.RunFormulaAction;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.design.simplesearch.ByDateFieldTerm;
import com.hcl.domino.design.simplesearch.ByFieldTerm;
import com.hcl.domino.design.simplesearch.ByFormTerm;
import com.hcl.domino.design.simplesearch.ByNumberFieldTerm;
import com.hcl.domino.design.simplesearch.SimpleSearchTerm;
import com.hcl.domino.design.simplesearch.TextTerm;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionByForm;
import com.hcl.domino.richtext.records.CDActionDBCopy;
import com.hcl.domino.richtext.records.CDActionDelete;
import com.hcl.domino.richtext.records.CDActionFolder;
import com.hcl.domino.richtext.records.CDActionFormula;
import com.hcl.domino.richtext.records.CDActionModifyField;
import com.hcl.domino.richtext.records.CDActionNewsletter;
import com.hcl.domino.richtext.records.CDActionReadMarks;
import com.hcl.domino.richtext.records.CDActionReply;
import com.hcl.domino.richtext.records.CDActionRunAgent;
import com.hcl.domino.richtext.records.CDActionSendDocument;
import com.hcl.domino.richtext.records.CDActionSendMail;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDQueryByField;
import com.hcl.domino.richtext.records.CDQueryByFolder;
import com.hcl.domino.richtext.records.CDQueryByForm;
import com.hcl.domino.richtext.records.CDQueryFormula;
import com.hcl.domino.richtext.records.CDQueryHeader;
import com.hcl.domino.richtext.records.CDQueryTextTerm;
import com.hcl.domino.richtext.records.CDQueryUsesForm;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.CDEventEntry.ActionType;
import com.hcl.domino.richtext.records.CDEventEntry.Platform;
import com.hcl.domino.richtext.structures.AssistFieldStruct;
import com.hcl.domino.richtext.structures.ColorValue;

/**
 * @since 1.0.18
 */
public enum DesignUtil {
  ;

  /**
   * Represents the design-note properties mapped to a {@link DesignElement}
   * interface.
   * 
   * @param <T> the {@link DesignElement} type produced by the mapping
   * @param <I> the implementation class used by the constructor
   */
  public static class DesignMapping<T extends DesignElement, I extends AbstractDesignElement<T>> {
    private final DocumentClass noteClass;
    private final String flagsPattern;
    private final Function<Document, I> constructor;
    private final BiConsumer<Class<? extends DesignElement>, Document> docInitializer;
    
    protected DesignMapping(final DocumentClass noteClass, final String flagsPattern, BiConsumer<Class<? extends DesignElement>, Document> docInitializer, final Function<Document, I> constructor) {
      this.noteClass = noteClass;
      this.flagsPattern = flagsPattern;
      this.docInitializer = docInitializer;
      this.constructor = constructor;
    }

    public Function<Document, I> getConstructor() {
      return this.constructor;
    }

    public String getFlagsPattern() {
      return this.flagsPattern;
    }

    public DocumentClass getNoteClass() {
      return this.noteClass;
    }
    
    public BiConsumer<Class<? extends DesignElement>, Document> getDocInitializer() {
      return this.docInitializer;
    }
  }

  private static final Map<Class<? extends DesignElement>, DesignMapping<? extends DesignElement, ? extends AbstractDesignElement<?>>> mappings = new HashMap<>();
  static {
    DesignUtil.mappings.put(Outline.class,
        new DesignMapping<>(DocumentClass.FILTER, NotesConstants.DFLAGPAT_SITEMAP, null,
            OutlineImpl::new));
    DesignUtil.mappings.put(View.class,
        new DesignMapping<>(DocumentClass.VIEW, NotesConstants.DFLAGPAT_VIEW_ALL_VERSIONS, null, ViewImpl::new));
    DesignUtil.mappings.put(Folder.class,
        new DesignMapping<>(DocumentClass.VIEW, NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS, null, FolderImpl::new));
    DesignUtil.mappings.put(CollectionDesignElement.class,
        new DesignMapping<CollectionDesignElement<?>, AbstractCollectionDesignElement<CollectionDesignElement<?>>>(
            DocumentClass.VIEW,
            NotesConstants.DFLAGPAT_VIEWS_AND_FOLDERS_DESIGN,
            null,
            doc -> {
              final String flags = doc.getAsText(NotesConstants.DESIGN_FLAGS, ' ');
              if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS)) {
                @SuppressWarnings("unchecked")
                final AbstractCollectionDesignElement<CollectionDesignElement<?>> result = (AbstractCollectionDesignElement<CollectionDesignElement<?>>) (AbstractCollectionDesignElement<?>) new FolderImpl(
                    doc);
                return result;
              } else {
                @SuppressWarnings("unchecked")
                final AbstractCollectionDesignElement<CollectionDesignElement<?>> result = (AbstractCollectionDesignElement<CollectionDesignElement<?>>) (AbstractCollectionDesignElement<?>) new ViewImpl(
                    doc);
                return result;
              }
            }));

    DesignUtil.mappings.put(Form.class,
        new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_FORM_ALL_VERSIONS, null, FormImpl::new));
    DesignUtil.mappings.put(Subform.class,
        new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_SUBFORM_ALL_VERSIONS, null,
            SubformImpl::new));
    DesignUtil.mappings.put(DesignAgent.class,
        new DesignMapping<DesignAgent, AbstractDesignAgentImpl<DesignAgent>>(
            DocumentClass.FILTER,
            NotesConstants.DFLAGPAT_AGENTSLIST,
            (designClass, doc) -> {
              //initialize fields to indicate the type of agent
              if (DesignFormulaAgent.class.isAssignableFrom(designClass)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "f3"); //$NON-NLS-1$
                doc.replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, Short.toUnsignedInt(RichTextConstants.SIG_ACTION_FORMULAONLY));
              }
              else if (DesignImportedJavaAgent.class.isAssignableFrom(designClass)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "f3"); //$NON-NLS-1$
                doc.replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, Short.toUnsignedInt(RichTextConstants.SIG_ACTION_JAVAAGENT));
              }
              else if (DesignJavaAgent.class.isAssignableFrom(designClass)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "fJj3"); //$NON-NLS-1$
                doc.replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, Short.toUnsignedInt(RichTextConstants.SIG_ACTION_JAVAAGENT));
              }
              else if (DesignLotusScriptAgent.class.isAssignableFrom(designClass)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "fL3"); //$NON-NLS-1$
                doc.replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, Short.toUnsignedInt(RichTextConstants.SIG_ACTION_LOTUSSCRIPT));
              }
              else if (DesignSimpleActionAgent.class.isAssignableFrom(designClass)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "f3"); //$NON-NLS-1$
                doc.replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, -1);
              }
            },
            doc -> {
              final String flags = doc.getAsText(NotesConstants.DESIGN_FLAGS, ' ');
              short lang = doc.get(NotesConstants.ASSIST_TYPE_ITEM, short.class, (short) 0);

              if (lang == RichTextConstants.SIG_ACTION_FORMULAONLY) {
                lang = RichTextConstants.SIG_ACTION_FORMULA;
              } else if (lang == 0) {
                // Seen in ancient data
                lang = RichTextConstants.SIG_ACTION_FORMULA;
              }
              switch (lang) {
                case RichTextConstants.SIG_ACTION_JAVAAGENT:
                  // Imported Java agents are distinguished by a $Flags value
                  if (flags.contains(NotesConstants.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE)) {
                    @SuppressWarnings("unchecked")
                    final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignJavaAgentImpl(
                        doc);
                    return result;
                  } else {
                    @SuppressWarnings("unchecked")
                    final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignImportedJavaAgentImpl(
                        doc);
                    return result;
                  }
                case RichTextConstants.SIG_ACTION_LOTUSSCRIPT:
                {
                  @SuppressWarnings("unchecked")
                  final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignLotusScriptAgentImpl(
                      doc);
                  return result;
                }
                case RichTextConstants.SIG_ACTION_FORMULAONLY:
                {
                  @SuppressWarnings("unchecked")
                  final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignFormulaAgentImpl(
                      doc);
                  return result;
                }
                case RichTextConstants.SIG_ACTION_FORMULA: {
                  // This gets weird. Both FORMULA and FORMULAONLY have been observed to represent
                  // formula agents... but that's not all. If a Simple Action agent _starts_ with a
                  // @Function Formula action, then the $AssistType field is written as
                  // SIG_ACTION_FORMULA. Thus, we need to open the agent to check.
                  // Currently, the best check is to see if it has more than just the two opening
                  // records that it shared with Formula agents
                  final RichTextRecordList list = doc.getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION);
                  if (list.size() > 2) {
                    @SuppressWarnings("unchecked")
                    final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignSimpleActionAgentImpl(
                        doc);
                    return result;
                  }
                  @SuppressWarnings("unchecked")
                  final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignFormulaAgentImpl(
                      doc);
                  return result;
                }
                case RichTextConstants.SIG_ACTION_MODIFYFIELD:
                case RichTextConstants.SIG_ACTION_REPLY:
                case RichTextConstants.SIG_ACTION_SENDMAIL:
                case RichTextConstants.SIG_ACTION_DBCOPY:
                case RichTextConstants.SIG_ACTION_DELETE:
                case RichTextConstants.SIG_ACTION_BYFORM:
                case RichTextConstants.SIG_ACTION_MARKREAD:
                case RichTextConstants.SIG_ACTION_MARKUNREAD:
                case RichTextConstants.SIG_ACTION_MOVETOFOLDER:
                case RichTextConstants.SIG_ACTION_COPYTOFOLDER:
                case RichTextConstants.SIG_ACTION_REMOVEFROMFOLDER:
                case RichTextConstants.SIG_ACTION_NEWSLETTER:
                case RichTextConstants.SIG_ACTION_RUNAGENT:
                case RichTextConstants.SIG_ACTION_SENDDOCUMENT:
                case -1: // Set when there are no actions
                {
                  @SuppressWarnings("unchecked")
                  final AbstractDesignAgentImpl<DesignAgent> result = (AbstractDesignAgentImpl<DesignAgent>) (AbstractDesignAgentImpl<?>) new DesignSimpleActionAgentImpl(
                      doc);
                  return result;
                }
                default:
                  throw new UnsupportedOperationException(
                      MessageFormat.format("Unable to find implementation for language value {0} and flags value \"{1}\"", lang, flags));
              }
            }));
    
    DesignUtil.mappings.put(DbProperties.class,
        new DesignMapping<>(DocumentClass.ICON, "", null, DbPropertiesImpl::new) //$NON-NLS-1$
    );
    DesignUtil.mappings.put(FileResource.class,
        new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_FILE_RESOURCE, null,
            FileResourceImpl::new));
    
    DesignUtil.mappings.put(DatabaseScriptLibrary.class, new DesignMapping<>(DocumentClass.FILTER, NotesConstants.DFLAGPAT_DATABASESCRIPT, null, DatabaseScriptLibraryImpl::new));
    
    DesignUtil.mappings.put(ScriptLibrary.class,
        new DesignMapping<ScriptLibrary, AbstractScriptLibrary<ScriptLibrary>>(
            DocumentClass.FILTER,
            NotesConstants.DFLAGPAT_SCRIPTLIB,
            (designClass,doc) -> {
              //initialize fields to indicate the type of library
              if (designClass.isAssignableFrom(JavaLibrary.class)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "sj34Q"); //$NON-NLS-1$
              }
              else  if (designClass.isAssignableFrom(JavaScriptLibrary.class)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "h534Q"); //$NON-NLS-1$
              }
              else  if (designClass.isAssignableFrom(LotusScriptLibrary.class)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, "s34Q"); //$NON-NLS-1$
              }
              else  if (designClass.isAssignableFrom(ServerJavaScriptLibrary.class)) {
                doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, ".5834Q"); //$NON-NLS-1$
              }
            },
            doc -> {
              final String flags = doc.getAsText(NotesConstants.DESIGN_FLAGS, ' ');
              if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JAVA)) {
                @SuppressWarnings("unchecked")
                final AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>) (AbstractScriptLibrary<?>) new JavaLibraryImpl(
                    doc);
                return result;
              } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JS)) {
                @SuppressWarnings("unchecked")
                final AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>) (AbstractScriptLibrary<?>) new JavaScriptLibraryImpl(
                    doc);
                return result;
              } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_LS)) {
                @SuppressWarnings("unchecked")
                final AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>) (AbstractScriptLibrary<?>) new LotusScriptLibraryImpl(
                    doc);
                return result;
              } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
                @SuppressWarnings("unchecked")
                final AbstractScriptLibrary<ScriptLibrary> result = (AbstractScriptLibrary<ScriptLibrary>) (AbstractScriptLibrary<?>) new ServerJavaScriptLibraryImpl(
                    doc);
                return result;
              } else {
                throw new UnsupportedOperationException(
                    MessageFormat.format("Unable to find implementation for flags value \"{0}\"", flags));
              }
            }));
    DesignUtil.mappings.put(ImageResource.class,
        new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_IMAGE_RESOURCE, null,
            ImageResourceImpl::new));
    DesignUtil.mappings.put(Page.class,
        new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_WEBPAGE, null, PageImpl::new)
    );
    DesignUtil.mappings.put(AboutDocument.class, new DesignMapping<>(DocumentClass.INFO, "", null, AboutDocumentImpl::new)); //$NON-NLS-1$
    DesignUtil.mappings.put(UsingDocument.class, new DesignMapping<>(DocumentClass.HELP, "", null, UsingDocumentImpl::new)); //$NON-NLS-1$
    DesignUtil.mappings.put(SharedField.class, new DesignMapping<>(DocumentClass.FIELD, "", null, SharedFieldImpl::new)); //$NON-NLS-1$
    DesignUtil.mappings.put(Navigator.class, new DesignMapping<>(DocumentClass.VIEW, NotesConstants.DFLAGPAT_VIEWMAP_ALL_VERSIONS, null, NavigatorImpl::new));
    DesignUtil.mappings.put(SharedActions.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_SACTIONS_DESIGN, null, SharedActionsImpl::new));
    DesignUtil.mappings.put(SharedColumn.class, new DesignMapping<>(DocumentClass.VIEW, NotesConstants.DFLAGPAT_SHARED_COLS, null, SharedColumnImpl::new));
    DesignUtil.mappings.put(StyleSheet.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_STYLE_SHEET_RESOURCE, null, StyleSheetImpl::new));
    DesignUtil.mappings.put(WiringProperties.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_COMPDEF, null, WiringPropertiesImpl::new));
    DesignUtil.mappings.put(Theme.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_STYLEKIT, null, ThemeImpl::new));
    DesignUtil.mappings.put(Frameset.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_FRAMESET, null, FramesetImpl::new));
    DesignUtil.mappings.put(CompositeComponent.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_WIDGET, null, CompositeComponentImpl::new));
    DesignUtil.mappings.put(CompositeApplication.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_COMPAPP, null, CompositeApplicationImpl::new));
    DesignUtil.mappings.put(XPage.class, new DesignMapping<>(DocumentClass.FORM, NotesConstants.DFLAGPAT_XSPPAGE, null, XPageImpl::new));
  }

  /**
   * Constructs a {@link DesignElement} instance for the provided document
   * information.
   * 
   * @param database  the {@link Database} housing the design note; may be
   *                  {@code null} if {@code doc} is provided
   * @param noteId    the note ID of the design note; may be {@code 0} if
   *                  {@code doc} is provided
   * @param noteClass the {@link DocumentClass} of the design note; may be
   *                  {@code null} if {@code doc} is provided
   * @param flags     the {@code $Flags} value of the design note; may be
   *                  {@code null} if {@code noteClass} is unambiguous
   * @param doc       the design note to wrap
   * @return the newly-constructed {@link DesignElement} instance
   */
  public static DesignElement createDesignElement(final Database database, final int noteId, final DocumentClass noteClass,
      final String flags, final Optional<Document> doc) {
    switch (noteClass) {
      case ACL:
        throw new NotYetImplementedException();
      case FIELD:
        return new SharedFieldImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
      case FILTER:
        if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JAVA)) {
          return new JavaLibraryImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JS)) {
            return new JavaScriptLibraryImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_LS)) {
          return new LotusScriptLibraryImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_SERVER_JS)) {
          return new ServerJavaScriptLibraryImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SITEMAP)) {
          return new OutlineImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else {
          //create a DesignAgent implementation, DesignMapping uses flags and additional fields to check the language type
          Document docLocal = doc.orElseGet(() -> database.getDocumentById(noteId).get());
          @SuppressWarnings("unchecked")
          DesignMapping<DesignAgent, AbstractDesignAgentImpl<DesignAgent>> mapping = (DesignMapping<DesignAgent, AbstractDesignAgentImpl<DesignAgent>>) DesignUtil.mappings.get(DesignAgent.class);
          final AbstractDesignAgentImpl<DesignAgent> result = mapping.getConstructor().apply(docLocal);
          return result;
        }
      case FORM:
        if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SUBFORM_ALL_VERSIONS)) {
          return new SubformImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_STYLEKIT)) {
          return new ThemeImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_WIDGET)) {
          return new CompositeComponentImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_COMPAPP)) {
          return new CompositeApplicationImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_XSPPAGE)) {
          return new XPageImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FILE_RESOURCE)) {
          return new FileResourceImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FILE)) {
          return new FileResourceImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SACTIONS_DESIGN)) {
          return new SharedActionsImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_WEBPAGE)) {
          return new PageImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_IMAGE_RESOURCE)) {
          return new ImageResourceImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_STYLE_SHEET_RESOURCE)) {
          return new StyleSheetImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_COMPDEF)) {
          return new WiringPropertiesImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FRAMESET)) {
            return new FramesetImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else {
          return new FormImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        }
      case HELP:
        return new UsingDocumentImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
      case HELP_INDEX:
        throw new NotYetImplementedException();
      case ICON:
        return new DbPropertiesImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
      case INFO:
        return new AboutDocumentImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
      case REPLFORMULA:
        throw new NotYetImplementedException();
      case VIEW:
        if (DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_FOLDER_ALL_VERSIONS)) {
          return new FolderImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(DesignUtil.matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_SHARED_COLS)) {
          return new SharedColumnImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else if(matchesFlagsPattern(flags, NotesConstants.DFLAGPAT_VIEWMAP_ALL_VERSIONS)) {
          return new NavigatorImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        } else {
          return new ViewImpl(doc.orElseGet(() -> database.getDocumentById(noteId).get()));
        }
      case ALL:
      case ALLNONDATA:
      case DATA:
      case DOCUMENT:
      case DEFAULT:
      case DESIGNCOLLECTION:
      case SINGLE_INSTANCE:
      case PRIVATE:
      case NOTIFYDELETION:
      case NONE:
      default:
        throw new IllegalArgumentException(MessageFormat.format("Unsupported design note type {0}", noteClass));
    }
  }

  /**
   * Constructs a {@link DesignElement} instance for the provided document.
   * 
   * @param doc the design note to wrap
   * @return the newly-constructed {@link DesignElement} instance
   */
  public static DesignElement createDesignElement(final Document doc) {
    final String flags = doc.getAsText(NotesConstants.DESIGN_FLAGS, ' ');
    final DocumentClass noteClass = doc.getDocumentClass().iterator().next();
    return DesignUtil.createDesignElement(null, 0, noteClass, flags, Optional.of(doc));
  }

  /**
   * Retrieves the {@link DesignMapping} object that corresponds to the provided
   * design element interface.
   * 
   * @param <T>         a {@link DesignElement} sub-interface
   * @param <I>         a {@link AbstractDesignElement} implementation class
   * @param designClass a {@link Class} object representing {@code <T>}
   * @return the corresponding {@code DesignMapping}
   * @throws IllegalArgumentException when {@code <T>} has no mapping
   *                                  representation
   */
  public static <T extends DesignElement, I extends AbstractDesignElement<T>> DesignMapping<T, I> getDesignMapping(
      final Class<T> designClass) {
    @SuppressWarnings("unchecked")
    final DesignMapping<T, I> mapping = (DesignMapping<T, I>) DesignUtil.mappings.get(designClass);
    if (mapping == null) {
      throw new IllegalArgumentException(MessageFormat.format("Unsupported design class \"{0}\"", designClass.getName()));
    }
    return mapping;
  }

  /**
   * @param flags   a design flag value to test
   * @param pattern a flag pattern to test against (DFLAGPAT_*)
   * @return whether the flags match the pattern
   */
  public static boolean matchesFlagsPattern(final String flags, final String pattern) {
    if (pattern == null || pattern.isEmpty()) {
      return false;
    }

    final String toTest = flags == null ? "" : flags; //$NON-NLS-1$

    // Patterns start with one of four characters:
    // "+" (match any)
    // "-" (match none)
    // "*" (match all)
    // "(" (multi-part test)
    String matchers = null;
    String antiMatchers = null;
    String allMatchers = null;
    final char first = pattern.charAt(0);
    switch (first) {
      case '+':
        matchers = pattern.substring(1);
        antiMatchers = ""; //$NON-NLS-1$
        allMatchers = ""; //$NON-NLS-1$
        break;
      case '-':
        matchers = ""; //$NON-NLS-1$
        antiMatchers = pattern.substring(1);
        allMatchers = ""; //$NON-NLS-1$
        break;
      case '*':
        matchers = ""; //$NON-NLS-1$
        antiMatchers = ""; //$NON-NLS-1$
        allMatchers = pattern.substring(1);
        break;
      case '(':
        // The order is always +-*
        final int plusIndex = pattern.indexOf('+');
        final int minusIndex = pattern.indexOf('-');
        final int starIndex = pattern.indexOf('*');

        matchers = pattern.substring(plusIndex + 1, minusIndex == -1 ? pattern.length() : minusIndex);
        antiMatchers = minusIndex == -1 ? "" : pattern.substring(minusIndex + 1, starIndex == -1 ? pattern.length() : starIndex); //$NON-NLS-1$
        allMatchers = starIndex == -1 ? "" : pattern.substring(starIndex + 1); //$NON-NLS-1$
        break;
      default:
        // Not a matcher pattern
    }
    if (matchers == null) {
      matchers = ""; //$NON-NLS-1$
    }
    if (antiMatchers == null) {
      antiMatchers = ""; //$NON-NLS-1$
    }
    if (allMatchers == null) {
      allMatchers = ""; //$NON-NLS-1$
    }

    // Test "match against any" and fail if it doesn't
    boolean matchedAny = matchers.isEmpty();
    for (int i = 0; i < matchers.length(); i++) {
      if (toTest.indexOf(matchers.charAt(i)) > -1) {
        matchedAny = true;
        break;
      }
    }
    if (!matchedAny) {
      return false;
    }

    // Test "match none" and fail if it does
    for (int i = 0; i < antiMatchers.length(); i++) {
      if (toTest.indexOf(antiMatchers.charAt(i)) > -1) {
        // Exit immediately
        return false;
      }
    }

    // Test "match all" and fail if it doesn't
    for (int i = 0; i < allMatchers.length(); i++) {
      if (toTest.indexOf(allMatchers.charAt(i)) == -1) {
        // Exit immediately
        return false;
      }
    }

    // If we survived to here, it must match
    return true;
  }

  /**
   * Determines whether the provided name is a match for any of the design-element
   * titles.
   * 
   * @param name        the name of the design element to look for
   * @param titleValues the {@code $TITLE} item value from a design element
   * @return whether {@code name} matches any of the values in {@code $TITLE}
   */
  public static boolean matchesTitleValues(final String name, final List<String> titleValues) {
    final String normalName = DesignUtil.normalizeTitle(name);
    return titleValues.stream()
        .flatMap(val -> Arrays.stream(val.split("\\|"))) //$NON-NLS-1$
        .map(DesignUtil::normalizeTitle)
        .anyMatch(normalName::equalsIgnoreCase);
  }

  /**
   * Converts the provided title to its "normalized" form for the purposes of
   * lookups.
   * <p>
   * Specifically, this removes the "_" characters used for UI hints and strips
   * surrounding
   * parentheses as used to hide elements.
   * </p>
   * 
   * @param title the design element title to normalize
   * @return the normalized variant of the title
   */
  public static String normalizeTitle(final String title) {
    if (title == null) {
      return ""; //$NON-NLS-1$
    } else {
      String result = title.replace("_", ""); //$NON-NLS-1$ //$NON-NLS-2$
      if (result.charAt(0) == '(' && result.charAt(result.length() - 1) == ')') {
        result = result.substring(1, result.length() - 1);
      }
      return result;
    }
  }

  /**
   * Converts the provided $TITLE values - which may be multi-value or
   * pipe-separated - info a standardized list of titles and aliases.
   * 
   * @param values the item or entry values to standardize
   * @return a {@link List} of title and alias values
   * @since 1.0.27
   */
  public static List<String> toTitlesList(final List<String> values) {
    if (values == null || values.isEmpty()) {
      return Arrays.asList(""); //$NON-NLS-1$
    }
    final List<String> result = new ArrayList<>();
    for (final String val : values) {
      final StringTokenizer tokenizer = new StringTokenizer(val, "|"); //$NON-NLS-1$
      while (tokenizer.hasMoreElements()) {
        result.add(tokenizer.nextToken());
      }
    }
    return result;
  }
  
  /**
   * Processes the provided list of composite data records to produce a list of equivalent
   * encapsulated simple actions, filtering out any unmatched records.
   * 
   * @param records a {@link List} of {@link RichTextRecord} instances
   * @return a corresponding {@link List} of {@link SimpleAction} instances
   * @since 1.0.32
   */
  public static List<SimpleAction> toSimpleActions(List<? extends RichTextRecord<?>> records) {
    return records
      .stream()
      .map(record -> {
        if (record instanceof CDActionByForm) {
          final CDActionByForm action = (CDActionByForm) record;
          final Map<String, List<String>> modifications = new LinkedHashMap<>();
          for (final AssistFieldStruct field : action.getAssistFields()) {
            modifications.put(field.getFieldName(), field.getValues());
          }
          return new DefaultModifyByFormAction(action.getFormName(), modifications);
        } else if (record instanceof CDActionDBCopy) {
          final CDActionDBCopy action = (CDActionDBCopy) record;
          return new DefaultCopyToDatabaseAction(action.getServerName(), action.getDatabaseName());
        } else if (record instanceof CDActionDelete) {
          return new DefaultDeleteDocumentAction();
        } else if (record instanceof CDActionFolder) {
          final CDActionFolder action = (CDActionFolder) record;
          FolderBasedAction.Type type;
          switch (action.getHeader().getSignature()) {
            case RichTextConstants.SIG_ACTION_MOVETOFOLDER:
              type = FolderBasedAction.Type.MOVE;
              break;
            case RichTextConstants.SIG_ACTION_REMOVEFROMFOLDER:
              type = FolderBasedAction.Type.REMOVE;
              break;
            case RichTextConstants.SIG_ACTION_COPYTOFOLDER:
            default:
              type = FolderBasedAction.Type.COPY;
              break;
          }
          return new DefaultFolderBasedAction(action.getFolderName(), action.getFlags(), type);
        } else if (record instanceof CDActionFormula) {
          final CDActionFormula action = (CDActionFormula) record;
          RunFormulaAction.DocumentAction docAction;
          if (action.getFlags().contains(CDActionFormula.Flag.NEWCOPY)) {
            docAction = RunFormulaAction.DocumentAction.CREATE;
          } else if (action.getFlags().contains(CDActionFormula.Flag.SELECTDOCS)) {
            docAction = RunFormulaAction.DocumentAction.SELECT;
          } else {
            docAction = RunFormulaAction.DocumentAction.MODIFY;
          }
          return new DefaultRunFormulaAction(Optional.of(docAction), Optional.of(action.getFormula()));
        } else if (record instanceof CDActionModifyField) {
          final CDActionModifyField action = (CDActionModifyField) record;
          return new DefaultModifyFieldAction(action.getFieldName(), action.getValue());
        } else if (record instanceof CDActionNewsletter) {
          final CDActionNewsletter action = (CDActionNewsletter) record;
          return new DefaultSendNewsletterAction(
              action.getViewName(),
              action.getTo(),
              action.getCc(),
              action.getBcc(),
              action.getSubject(),
              action.getBody(),
              action.getGatherCount(),
              action.getFlags());
        } else if (record instanceof CDActionSendMail) {
          final CDActionSendMail action = (CDActionSendMail) record;
          return new DefaultSendMailAction(
              action.getTo(),
              action.getCc(),
              action.getBcc(),
              action.getSubject(),
              action.getBody(),
              action.getFlags());
        } else if (record instanceof CDActionReadMarks) {
          final CDActionReadMarks action = (CDActionReadMarks) record;
          switch (action.getHeader().getSignature()) {
            case (byte) RichTextConstants.SIG_ACTION_MARKUNREAD:
              return (ReadMarksAction) () -> ReadMarksAction.Type.MARK_UNREAD;
            case (byte) RichTextConstants.SIG_ACTION_MARKREAD:
            default:
              return (ReadMarksAction) () -> ReadMarksAction.Type.MARK_READ;
          }
        } else if (record instanceof CDActionReply) {
          final CDActionReply action = (CDActionReply) record;
          return new DefaultReplyAction(action.getFlags(), action.getBody());
        } else if (record instanceof CDActionRunAgent) {
          final CDActionRunAgent action = (CDActionRunAgent) record;
          final String agentName = action.getAgentName();
          return (RunAgentAction) () -> agentName;
        } else if (record instanceof CDActionSendDocument) {
          return new DefaultSendDocumentAction();
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
  /**
   * Processes a rich-text item on the provided document to encapsulate it into a
   * stream of higher-level representations when possible, mixed with direct
   * rich-text records when un-handled.
   * 
   * @param doc the document containing the rich text
   * @param itemName the item name to process
   * @return a {@link List} of encapsulated objects and/or {@link RichTextRecord}s
   * @since 1.0.34
   */
  public static List<?> encapsulateRichTextBody(Document doc, String itemName) {
    return doc.getRichTextItem(itemName);
  }
  
  /**
   * Processes the provided list of composite data records to produce a list of equivalent
   * encapsulated simple search terms, filtering out any unmatched records.
   * 
   * @param records a {@link List} of {@link RichTextRecord} instances
   * @return a corresponding {@link List} of {@link SimpleSearchTerm} instances
   * @since 1.0.38
   */
  public static List<? extends SimpleSearchTerm> toSimpleSearch(List<? extends RichTextRecord<?>> records) {
    return records
      .stream()
      .map(record -> {
        if(record instanceof CDQueryHeader) {
          // Ignored intentionally
          return null;
        } else if(record instanceof CDQueryByField) {
          CDQueryByField query = (CDQueryByField)record;

          // Not all below types have meanings, so set defaults first
          ByFieldTerm.TextRule textRule = ByFieldTerm.TextRule.CONTAINS;
          ByDateFieldTerm.DateRule dateRule = null;
          ByNumberFieldTerm.NumberRule numberRule = null;
          switch(query.getOperator().orElse(CDQueryByField.Operator.GREATER)) {
            case DOESNOTCONTAIN:
              textRule = ByFieldTerm.TextRule.DOES_NOT_CONTAIN;
              break;
            case CONTAINS:
              textRule = ByFieldTerm.TextRule.CONTAINS;
              break;
            case BETWEEN:
              dateRule = ByDateFieldTerm.DateRule.BETWEEN;
              numberRule = ByNumberFieldTerm.NumberRule.BETWEEN;
              break;
            case EQUAL:
              dateRule = ByDateFieldTerm.DateRule.ON;
              numberRule = ByNumberFieldTerm.NumberRule.EQUAL;
              break;
            case GREATER:
              dateRule = ByDateFieldTerm.DateRule.AFTER;
              numberRule = ByNumberFieldTerm.NumberRule.GREATER_THAN;
              break;
            case INTHELAST:
              dateRule = ByDateFieldTerm.DateRule.IN_LAST;
              break;
            case INTHENEXT:
              dateRule = ByDateFieldTerm.DateRule.IN_NEXT;
              break;
            case LESS:
              dateRule = ByDateFieldTerm.DateRule.BEFORE;
              numberRule = ByNumberFieldTerm.NumberRule.LESS_THAN;
              break;
            case NOTEQUAL:
              dateRule = ByDateFieldTerm.DateRule.NOT_ON;
              numberRule = ByNumberFieldTerm.NumberRule.NOT_EQUAL;
              break;
            case NOTWITHIN:
              dateRule = ByDateFieldTerm.DateRule.NOT_BETWEEN;
              numberRule = ByNumberFieldTerm.NumberRule.NOT_BETWEEN;
              break;
            case OLDERTHAN:
              dateRule = ByDateFieldTerm.DateRule.OLDER_THAN;
              break;
            case DUEIN:
              // ???
              dateRule = ByDateFieldTerm.DateRule.AFTER_NEXT;
              break;
            default:
              break;
          }
          
          String fieldName = query.getFieldName();
          String textValue = query.getValue();
          
          switch(query.getDataType().orElse(ItemDataType.TYPE_TEXT)) {
            case TYPE_NUMBER:
            case TYPE_NUMBER_RANGE: {
              switch(numberRule) {
              case BETWEEN:
              case NOT_BETWEEN:
                // Then it's a range
                return new DefaultByNumberFieldTerm(textRule, fieldName, textValue, numberRule, new Pair<>(query.getNumber1(), query.getNumber2()));
              default:
                // Otherwise, single value
                return new DefaultByNumberFieldTerm(textRule, fieldName, textValue, numberRule, query.getNumber1());
              }
            }
            case TYPE_TIME:
            case TYPE_TIME_RANGE: {
              ByDateFieldTerm.DateType dateType = ByDateFieldTerm.DateType.FIELD;
              if(query.getFlags().contains(CDQueryByField.Flag.BYDATE)) {
                // Mark it as explicitly By Date and figure out the type by field
                if("_RevisionDate".equals(fieldName)) { //$NON-NLS-1$
                  dateType = ByDateFieldTerm.DateType.MODIFIED;
                } else if("_CreationDate".equals(fieldName)) { //$NON-NLS-1$
                  dateType = ByDateFieldTerm.DateType.CREATED;
                }
              }
              DominoDateTime date1 = query.getDate1();
              DominoDateTime date2 = query.getDate2();
              if(date2.isValid()) {
                // Then it's definitely a range
                return new DefaultByDateFieldTerm(textRule, fieldName, textValue, dateType, dateRule, new DefaultDominoDateRange(date1, date2));
              } else if(date1.isValid()) {
                // Then it's a single-date query
                return new DefaultByDateFieldTerm(textRule, fieldName, textValue, dateType, dateRule, date1);
              } else {
                // Must be a number-based query
                return new DefaultByDateFieldTerm(textRule, fieldName, textValue, dateType, dateRule, (int)query.getNumber1());
              }
            }
            default:
              // Assume text
              if(query.getFlags().contains(CDQueryByField.Flag.BYAUTHOR)) {
                // Actually a $UpdatedBy query
                return new DefaultByAuthorTerm(textRule, fieldName, textValue);
              } else {
                // Generic item search
                return new DefaultByFieldTerm(textRule, fieldName, textValue);
              }
          }
        } else if(record instanceof CDQueryByFolder) {
          CDQueryByFolder query = (CDQueryByFolder)record;
          
          boolean isPrivate = query.getFlags().contains(CDQueryByFolder.Flag.PRIVATE);
          String folderName = query.getFolderName();
          return new DefaultByFolderTerm(folderName, isPrivate);
        } else if(record instanceof CDQueryByForm) {
          CDQueryByForm query = (CDQueryByForm)record;
          
          String formName = query.getFormName();
          Map<String, List<Object>> fieldMatches = new LinkedHashMap<>();
          for(AssistFieldStruct struct : query.getAssistFields()) {
            // Ignore operator
            String fieldName = struct.getFieldName();
            List<Object> values = struct.getValues();
            fieldMatches.put(fieldName, values);
          }
          
          return new DefaultExampleFormTerm(formName, fieldMatches);
        } else if(record instanceof CDQueryFormula) {
          // This has no representation in Designer, and so is not currently implemented here
          return null;
        } else if(record instanceof CDQueryTextTerm) {
          CDQueryTextTerm query = (CDQueryTextTerm)record;
          
          TextTerm.Type textType;
          Set<CDQueryTextTerm.Flag> flags = query.getFlags();
          if(flags.contains(CDQueryTextTerm.Flag.ACCRUE)) {
            textType = TextTerm.Type.ACCRUE;
          } else if(flags.contains(CDQueryTextTerm.Flag.AND)) {
            textType = TextTerm.Type.AND;
          } else if(flags.contains(CDQueryTextTerm.Flag.NEAR)) {
            textType = TextTerm.Type.NEAR;
          } else {
            textType = TextTerm.Type.PLAIN;
          }
          
          return new DefaultTextTerm(textType, query.getTerms());
        } else if(record instanceof CDQueryUsesForm) {
          CDQueryUsesForm query = (CDQueryUsesForm)record;
          
          Set<String> formNames = Collections.unmodifiableSet(new LinkedHashSet<>(query.getFormNames()));
          return (ByFormTerm)() -> formNames;
        }
        return (SimpleSearchTerm)null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
  /**
   * Initializes the {@code $HTMLCode} and {@code $Info} fields on a form or
   * related design element to default values like Designer.
   * 
   * @param doc the design note to initialize
   * @since 1.27.0
   */
  public static void initFormBasics(Document doc) {
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    
    try (RichTextWriter writer = doc.createRichTextItem("$HTMLCode")) { //$NON-NLS-1$
      writer.addRichTextRecord(RecordType.EVENT_LANGUAGE_ENTRY, (Consumer<CDEventEntry>) (record) -> {
        record.setPlatform(Platform.WEB)
        .setHtmlEventId(EventId.HEADER)
        .setActionType(ActionType.JAVASCRIPT);
      });
    }
    
    doc.forEachItem("$HTMLCode", (item, loop) -> { //$NON-NLS-1$
      item.setSigned(true);
      item.setEncrypted(false);
    });

    try (RichTextWriter writer = doc.createRichTextItem("$Info")) { //$NON-NLS-1$
      writer.addRichTextRecord(RecordType.DOCUMENT, (Consumer<CDDocument>) (record) -> {
        record.setPaperColor(StandardColors.White)
        .setFlags(EnumSet.of(CDDocument.Flag.SPARESOK))
        .setFlags2(EnumSet.of(CDDocument.Flag2.UPDATE_SIBLING))
        .setPaperColorRaw(1)
        .setFlags3(EnumSet.of(CDDocument.Flag3.RENDERPASSTHROUGH));
        
        record
        .getPaperColorValue()
        .setFlags(EnumSet.of(ColorValue.Flag.ISRGB))
        .setRed((short) (255 & 0xffff))
        .setGreen((short) (255 & 0xffff))
        .setBlue((short) (255 & 0xffff))
        .setComponent4((short) 0);
        
      });
    }
    
    doc.forEachItem("$Info", (item, loop) -> { //$NON-NLS-1$
      item.setSigned(true);
      item.setEncrypted(false);
    });
  }
  
  /**
   * Add an empty text item with default values, as commonly used in rich text
   * generated by Designer.
   * 
   * @param writer the rich text writer to use
   * @since 1.27.0
   */
  public static void addEmptyText(RichTextWriter writer) {

    writer.addRichTextRecord(RecordType.TEXT, (Consumer<CDText>) (record) -> {
      record.getStyle()
      .setEmboss(false)
      .setSub(false)
      .setColor(StandardColors.Black)
      .setShadow(false)
      .setSuper(false)
      .setExtrude(false)
      .setUnderline(false)
      .setStandardFont(StandardFonts.SWISS)
      .setItalic(false)
      .setBold(false)
      .setStrikeout(false)
      .setPointSize(10);
    });
  }
}
