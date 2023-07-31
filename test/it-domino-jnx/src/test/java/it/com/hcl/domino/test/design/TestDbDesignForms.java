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
package it.com.hcl.domino.test.design;

import static it.com.hcl.domino.test.util.ITUtil.toLf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.hcl.domino.DominoClient;
import com.hcl.domino.commons.richtext.records.GenericBSIGRecord;
import com.hcl.domino.commons.richtext.records.GenericLSIGRecord;
import com.hcl.domino.commons.richtext.records.GenericWSIGRecord;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.design.ActionBar;
import com.hcl.domino.design.ActionBar.ButtonHeightMode;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.EdgeWidths;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.action.ActionBarAction;
import com.hcl.domino.design.action.ActionContent;
import com.hcl.domino.design.action.FormulaActionContent;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.action.JavaScriptActionContent;
import com.hcl.domino.design.action.LotusScriptActionContent;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.design.action.SimpleActionActionContent;
import com.hcl.domino.design.action.SystemActionContent;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.ActionBarTextAlignment;
import com.hcl.domino.design.format.ActionButtonHeightMode;
import com.hcl.domino.design.format.ActionWidthMode;
import com.hcl.domino.design.format.BorderStyle;
import com.hcl.domino.design.format.ButtonBorderDisplay;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.design.forms.AutoLaunchHideWhen;
import com.hcl.domino.design.forms.AutoLaunchType;
import com.hcl.domino.design.forms.AutoLaunchWhen;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.design.simpleaction.ModifyFieldAction;
import com.hcl.domino.design.simpleaction.ReadMarksAction;
import com.hcl.domino.design.simpleaction.SendDocumentAction;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.HotspotType;
import com.hcl.domino.richtext.NotesBitmap;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.CDActionFolder;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionModifyField;
import com.hcl.domino.richtext.records.CDActionSendMail;
import com.hcl.domino.richtext.records.CDAnchor;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDBlobPart;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDEvent;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDExt2Field;
import com.hcl.domino.richtext.records.CDExtField;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDHeader;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDHtmlFormula;
import com.hcl.domino.richtext.records.CDLayout;
import com.hcl.domino.richtext.records.CDLayoutButton;
import com.hcl.domino.richtext.records.CDLayoutEnd;
import com.hcl.domino.richtext.records.CDLayoutField;
import com.hcl.domino.richtext.records.CDLayoutGraphic;
import com.hcl.domino.richtext.records.CDMacMetaHeader;
import com.hcl.domino.richtext.records.CDMacMetaSegment;
import com.hcl.domino.richtext.records.CDOLEBegin;
import com.hcl.domino.richtext.records.CDOLEEnd;
import com.hcl.domino.richtext.records.CDOLEObjectInfo;
import com.hcl.domino.richtext.records.CDPreTableBegin;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDSpanRecord;
import com.hcl.domino.richtext.records.CDTableBegin;
import com.hcl.domino.richtext.records.CDTableDataExtension;
import com.hcl.domino.richtext.records.CDTableEnd;
import com.hcl.domino.richtext.records.CDTableLabel;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.CDTextPropertiesTable;
import com.hcl.domino.richtext.records.CDTextProperty;
import com.hcl.domino.richtext.records.CDTimerInfo;
import com.hcl.domino.richtext.records.CDTransition;
import com.hcl.domino.richtext.records.CDWinMetaHeader;
import com.hcl.domino.richtext.records.CDWinMetaSegment;
import com.hcl.domino.richtext.records.DDEFormat;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.ActiveObject;
import com.hcl.domino.richtext.structures.ActiveObjectParam;
import com.hcl.domino.richtext.structures.ActiveObjectStorageLink;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.ElementHeader;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignForms extends AbstractDesignTest {
  public static final String ENV_DBDESIGN_FOLDER = "DBDESIGN_FORMFOLDER";
  
  public static final int EXPECTED_IMPORT_FORMS = 12;
  public static final int EXPECTED_IMPORT_SUBFORMS = 2;

  private static String dbPath;

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        
        Acl acl = this.database.getACL();
        Optional<AclEntry> entry = acl.getEntry(client.getEffectiveUserName());
        if(entry.isPresent()) {
          acl.updateEntry(client.getEffectiveUserName(), null, null, Arrays.asList("[Admin]"), null);
        } else {
          acl.addEntry(client.getEffectiveUserName(), AclLevel.MANAGER, Arrays.asList("[Admin]"), EnumSet.allOf(AclFlag.class));
        }
        acl.save();
        
        dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignForms", this.database);
      } else {
        this.database = client.openDatabase("", dbPath);
      }
    }
  }
  
  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + dbPath + ": " + t);
    }
  }
  
  @Test
  public void testActionForm() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("Action Form").get();
    
    ActionBar actions = form.getActionBar();
    
    assertEquals(ActionBar.Alignment.RIGHT, actions.getAlignment());
    assertTrue(actions.isUseJavaApplet());
    
    assertEquals(ActionButtonHeightMode.FIXED, actions.getHeightMode());
    assertEquals(33, actions.getHeightSpec());
    
    assertColorEquals(actions.getBackgroundColor(), 82, 145, 239);
    assertFalse(actions.getBackgroundImage().isPresent());
    assertEquals(ClassicThemeBehavior.INHERIT_FROM_OS, actions.getClassicThemeBehavior());
    
    assertEquals(BorderStyle.INSET, actions.getBorderStyle());
    assertColorEquals(actions.getBorderColor(), 127, 255, 255);
    assertFalse(actions.isUseDropShadow());
    {
      EdgeWidths inside = actions.getInsideMargins();
      assertEquals(0, inside.getTop());
      assertEquals(0, inside.getLeft());
      assertEquals(0, inside.getRight());
      assertEquals(0, inside.getBottom());
    }
    {
      EdgeWidths thickness = actions.getBorderWidths();
      assertEquals(0, thickness.getTop());
      assertEquals(0, thickness.getLeft());
      assertEquals(0, thickness.getRight());
      assertEquals(1, thickness.getBottom());
    }
    {
      EdgeWidths outside = actions.getOutsideMargins();
      assertEquals(0, outside.getTop());
      assertEquals(0, outside.getLeft());
      assertEquals(0, outside.getRight());
      assertEquals(0, outside.getBottom());
    }
    
    assertEquals(ButtonHeightMode.DEFAULT, actions.getButtonHeightMode());
    assertEquals(ActionWidthMode.DEFAULT, actions.getButtonWidthMode());
    assertFalse(actions.isFixedSizeButtonMargin());
    assertEquals(ButtonBorderDisplay.ALWAYS, actions.getButtonBorderMode());
    assertEquals(ActionBarTextAlignment.LEFT, actions.getButtonTextAlignment());
    assertEquals(1, actions.getButtonInternalMarginSize());
    
    assertColorEquals(actions.getButtonBackgroundColor(), 224, 224, 116);
    assertFalse(actions.getButtonBackgroundImage().isPresent());
    
    {
      NotesFont font = actions.getFont();
      assertFalse(font.getFontName().isPresent());
      assertEquals(StandardFonts.USERINTERFACE, font.getStandardFont().get());
      assertEquals(9, font.getPointSize());
      assertEquals(EnumSet.noneOf(FontAttribute.class), font.getAttributes());
    }
    assertTrue(actions.getFontColor().getFlags().contains(ColorValue.Flag.SYSTEMCOLOR));
    
    List<ActionBarAction> actionList = actions.getActions();
    assertEquals(16, actionList.size());
    {
      ActionBarAction action = actionList.get(0);
      assertEquals(2, action.getSharedActionIndex().getAsLong());
      assertEquals("Save", action.getName());
    }
    {
      ActionBarAction action = actionList.get(1);
      assertEquals(3, action.getSharedActionIndex().getAsLong());
      assertEquals("Save and Close", action.getName());
    }
    {
      ActionBarAction action = actionList.get(2);
      assertEquals("Formula Action", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.FORMULA, action.getActionLanguage());
      assertFalse(action.getLabelFormula().isPresent());
      assertEquals("NotesView", action.getTargetFrame().get());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertTrue(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NOTES, action.getIconType());
      assertEquals(68, action.getNotesIconIndex());
      assertTrue(action.isDisplayIconOnRight());
      
      assertEquals(EnumSet.of(HideFromDevice.WEB, HideFromDevice.MOBILE), action.getHideFromDevices());
      assertTrue(action.isUseHideWhenFormula());
      assertEquals("@False", action.getHideWhenFormula().get());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertEquals("testAction", action.getCompositeActionName().get());
      assertEquals("some program use", action.getProgrammaticUseText());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@StatusBar(\"I am formula action\")", ((FormulaActionContent)content).getFormula());
    }
    {
      ActionBarAction action = actionList.get(3);
      assertEquals("Mobile Left", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.FORMULA, action.getActionLanguage());
      assertTrue(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@StatusBar(\"hi\")", ((FormulaActionContent)content).getFormula());
    }
    {
      ActionBarAction action = actionList.get(4);
      assertEquals("Mobile Right", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.FORMULA, action.getActionLanguage());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertTrue(action.isIncludeInMobileSwipeRight());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@StatusBar(\"hi\")", ((FormulaActionContent)content).getFormula());
    }
    {
      ActionBarAction action = actionList.get(5);
      assertEquals("Action Group Right\\Sub-Action 1", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.FORMULA, action.getActionLanguage());
      assertEquals("\"Sub-Action\"", action.getLabelFormula().get());
      assertEquals("\"Right group action\"", action.getParentLabelFormula().get());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.CHECKBOX, action.getDisplayType());
      assertEquals("SomeField", action.getCheckboxFormula().get());
      assertTrue(action.isIncludeInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NOTES, action.getIconType());
      assertEquals(4, action.getNotesIconIndex());
      assertFalse(action.isDisplayIconOnRight());
      assertTrue(action.isDisplayAsSplitButton());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@SetField(\"SomeField\"; 0)", ((FormulaActionContent)content).getFormula());
    }
    {
      ActionBarAction action = actionList.get(6);
      assertEquals("Action Group Right\\", action.getName());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.MENU_SEPARATOR, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInContextMenu());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
    }
    {
      ActionBarAction action = actionList.get(7);
      assertEquals("Action Group Right\\Sub-Action 2", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.SIMPLE_ACTION, action.getActionLanguage());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInContextMenu());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(SimpleActionActionContent.class, content);
      List<SimpleAction> simpleActions = ((SimpleActionActionContent)content).getActions();
      assertEquals(3, simpleActions.size());
      {
        SimpleAction action0 = simpleActions.get(0);
        assertInstanceOf(SendDocumentAction.class, action0);
      }
      {
        SimpleAction action1 = simpleActions.get(1);
        assertInstanceOf(ModifyFieldAction.class, action1);
        assertEquals("Body", ((ModifyFieldAction)action1).getFieldName());
        assertEquals("hey", ((ModifyFieldAction)action1).getValue());
      }
      {
        SimpleAction action2 = simpleActions.get(2);
        assertInstanceOf(ReadMarksAction.class, action2);
        assertEquals(ReadMarksAction.Type.MARK_READ, ((ReadMarksAction)action2).getType());
      }
    }
    {
      ActionBarAction action = actionList.get(8);
      assertEquals("Menu Action", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.SIMPLE_ACTION, action.getActionLanguage());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertFalse(action.isIncludeInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      assertTrue(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NONE, action.getIconType());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertFalse(action.isPublishWithOle());
      assertTrue(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(SimpleActionActionContent.class, content);
      List<SimpleAction> simpleActions = ((SimpleActionActionContent)content).getActions();
      assertEquals(1, simpleActions.size());
      {
        SimpleAction action0 = simpleActions.get(0);
        assertInstanceOf(SendDocumentAction.class, action0);
      }
    }
    {
      ActionBarAction action = actionList.get(9);
      assertEquals("Mobile Guy", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.FORMULA, action.getActionLanguage());
      assertEquals("\"I'm going mobile\"", action.getLabelFormula().get());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertFalse(action.isIncludeInActionBar());
      assertFalse(action.isIncludeInActionMenu());
      assertTrue(action.isIncludeInMobileActions());
      assertTrue(action.isIncludeInMobileSwipeLeft());
      assertTrue(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NONE, action.getIconType());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertEquals("dsfd", action.getHideWhenFormula().get());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@StatusBar(\"Mobile status bar?\")", ((FormulaActionContent)content).getFormula());
    }
    {
      ActionBarAction action = actionList.get(10);
      assertEquals("LotusScript Action", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.LOTUSSCRIPT, action.getActionLanguage());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertTrue(action.isIconOnlyInActionBar());
      assertTrue(action.isOppositeAlignedInActionBar());
      assertFalse(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
//      assertFalse(action.isIncludeInMobileSwipeLeft());
//      assertTrue(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NONE, action.getIconType());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertTrue(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertTrue(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(LotusScriptActionContent.class, content);
      String expected = IOUtils.resourceToString("/text/testDbDesignCollections/shortls.txt", StandardCharsets.UTF_8);
      assertEquals(toLf(expected), toLf(((LotusScriptActionContent)content).getScript()));
    }
    {
      ActionBarAction action = actionList.get(11);
      assertEquals("JS Action", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.JAVASCRIPT, action.getActionLanguage());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
//      assertFalse(action.isIncludeInMobileSwipeLeft());
//      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NONE, action.getIconType());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());
      
      {
        ActionContent content = action.getActionContent();
        assertInstanceOf(JavaScriptActionContent.class, content);
        Collection<ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONCLICK) {
              if(event.isClient()) {
                if("window.alert(\"you poor soul, using JavaScript actions in a view\")\n".equals(toLf(event.getScript()))) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONCLICK) {
              if(!event.isClient()) {
                if("alert(\"I'm on the web\")\n".equals(toLf(event.getScript()))) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONMOUSEDOWN) {
              if("console.log(\"is there a console in Notes JS actions?\")\n".equals(toLf(event.getScript()))) {
                return true;
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONMOUSEOVER) {
              if("alert(\"wait, do onMouseOver actions work? No; this is web-only\")\n".equals(toLf(event.getScript()))) {
                return true;
              }
            }
            return false;
          })
        );
      }
    }
    {
      ActionBarAction action = actionList.get(12);
      assertEquals("JS Action 2", action.getName());
      assertEquals(ActionBarAction.ActionLanguage.COMMON_JAVASCRIPT, action.getActionLanguage());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
//      assertFalse(action.isIncludeInMobileSwipeLeft());
//      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      assertEquals(ActionBarAction.IconType.NONE, action.getIconType());
      
      assertEquals(EnumSet.noneOf(HideFromDevice.class), action.getHideFromDevices());
      assertFalse(action.isUseHideWhenFormula());
      assertFalse(action.getHideWhenFormula().isPresent());
      
      assertFalse(action.isPublishWithOle());
      assertFalse(action.isCloseOleWhenChosen());
      assertFalse(action.isBringDocumentToFrontInOle());
      assertFalse(action.getCompositeActionName().isPresent());
      assertEquals("", action.getProgrammaticUseText());

      {
        ActionContent content = action.getActionContent();
        assertInstanceOf(JavaScriptActionContent.class, content);
        Collection<ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONCLICK) {
              if(event.isClient()) {
                if("window.alert(\"this is the common part\")\n".equals(toLf(event.getScript()))) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONCLICK) {
              if(!event.isClient()) {
                if("window.alert(\"this is the common part\")\n".equals(toLf(event.getScript()))) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONMOUSEDOWN) {
              if("console.log(\"is there a console in Notes JS actions?\")\n".equals(toLf(event.getScript()))) {
                return true;
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONMOUSEOVER) {
              if("alert(\"wait, do onMouseOver actions work?\")\n".equals(toLf(event.getScript()))) {
                return true;
              }
            }
            return false;
          })
        );
      }
    }
    {
      ActionBarAction action = actionList.get(13);
      assertEquals(ActionBarAction.ActionLanguage.SYSTEM_COMMAND, action.getActionLanguage());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(SystemActionContent.class, content);
      assertEquals(SystemActionContent.SystemAction.CATEGORIZE, ((SystemActionContent)content).getAction());
    }
    {
      ActionBarAction action = actionList.get(14);
      assertEquals("Long LotusScript", action.getName());

      ActionContent content = action.getActionContent();
      assertInstanceOf(LotusScriptActionContent.class, content);
      String expected = IOUtils.resourceToString("/text/testDbDesignCollections/longls.txt", StandardCharsets.UTF_8);
      assertEquals(toLf(expected), toLf(((LotusScriptActionContent)content).getScript()));
    }
    {
      ActionBarAction action = actionList.get(15);
      assertEquals("Long JavaScript", action.getName());

      ActionContent content = action.getActionContent();
      assertInstanceOf(JavaScriptActionContent.class, content);
      Collection<ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
      assertEquals(1, events.size());
      ScriptEvent event = events.stream().findFirst().get();
      String expected = toLf(IOUtils.resourceToString("/text/testDbDesignCollections/longjs.js", StandardCharsets.UTF_8)).replace('\n', '\r');
      String actual = toLf(event.getScript());
      // Chomp the last line-ending character for consistency
      actual = actual.substring(0, actual.length()-1);
      assertEquals(expected, actual);
    }

    Form.AutoLaunchSettings auto = form.getAutoLaunchSettings();
    assertEquals(AutoLaunchType.NONE, auto.getType());
  }

  @Test
  public void testLsForm() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("Test LS Form").get();
    
    assertEquals(Form.Type.RESPONSE_TO_RESPONSE, form.getType());
    assertEquals(Form.MenuInclusion.NONE, form.getMenuInclusionMode());
    assertFalse(form.isIncludeInSearchBuilder());
    assertTrue(form.isIncludeInPrint());
    
    assertEquals(Form.VersioningBehavior.NEW_AS_RESPONSES, form.getVersioningBehavior());
    assertFalse(form.isVersionCreationAutomatic());
    
    assertFalse(form.isDefaultForm());
    assertTrue(form.isStoreFormInDocument());
    assertFalse(form.isAllowFieldExchange());
    assertTrue(form.isAutomaticallyRefreshFields());
    assertTrue(form.isAnonymousForm());
    assertFalse(form.isUseInitialFocus());
    assertFalse(form.isFocusOnF6());
    assertTrue(form.isSignDocuments());
    assertFalse(form.isRenderPassThroughHtmlInClient());
    assertFalse(form.isIncludeFieldsInIndex());
    assertTrue(form.isAllowAutosave());
    
    assertEquals(Form.ConflictBehavior.MERGE_CONFLICTS, form.getConflictBehavior());
    
    
    assertTrue(form.isInheritSelectedDocumentValues());
    Form.InheritanceSettings inheritance = form.getSelectedDocumentInheritanceBehavior().get();
    assertEquals("TargetBody", inheritance.getTargetField());
    assertEquals(Form.InheritanceFieldType.COLLAPSIBLE_RICH_TEXT, inheritance.getType());
    
    assertTrue(form.isAutomaticallyEnableEditMode());
    assertEquals(Form.ContextPaneBehavior.PARENT, form.getContextPaneBehavior());
    
    assertTrue(form.isShowMailDialogOnClose());
    
    Form.WebRenderingSettings web = form.getWebRenderingSettings();
    assertFalse(web.isRenderRichContentOnWeb());
    assertEquals("", web.getWebMimeType().get());
    assertEquals("Windows-1252", web.getWebCharset().get());
    assertColorEquals(web.getActiveLinkColor(), 0, 96, 160);
    assertColorEquals(web.getUnvisitedLinkColor(), 255, 192, 182);
    assertColorEquals(web.getVisitedLinkColor(), 159, 159, 224);
    
    assertEquals("testconn", form.getDefaultDataConnectionName().get());
    assertEquals("foo", form.getDefaultDataConnectionObject().get());
    

    Form.AutoLaunchSettings auto = form.getAutoLaunchSettings();
    assertEquals(AutoLaunchType.OLE_CLASS, auto.getType());
    assertEquals("D3E34B21-9D75-101A-8C3D-00AA001A1652", auto.getOleType().get().toUpperCase());
    assertFalse(auto.isLaunchInPlace());
    assertTrue(auto.isPresentDocumentAsModal());
    assertTrue(auto.isCreateObjectInFirstRichTextField());
    assertEquals(EnumSet.of(AutoLaunchWhen.CREATE, AutoLaunchWhen.READ), auto.getLaunchWhen());
    assertEquals(EnumSet.of(AutoLaunchHideWhen.OPEN_READ, AutoLaunchHideWhen.CLOSE_READ), auto.getHideWhen());
    assertEquals("Outer Frame", form.getAutoFrameFrameset().get());
    assertEquals("Nav", form.getAutoFrameTarget().get());
    
    Form.BackgroundSettings background = form.getBackgroundSettings();
    assertEquals(StandardColors.DustyViolet, background.getStandardBackgroundColor().get());
    assertColorEquals(background.getBackgroundColor(), 224, 129, 255);
    NotesBitmap image = background.getBackgroundImage().get();
    assertEquals(39, image.getSize().getWidth());
    assertEquals(31, image.getSize().getHeight());
    assertEquals(8, image.getBitsPerPixel());
    assertEquals(1, image.getSamplesPerPixel());
    assertEquals(8, image.getBitsPerSample());
    assertFalse(background.getBackgroundImageResource().isPresent());
    assertTrue(background.isHideGraphicInDesignMode());
    assertTrue(background.isHideGraphicOn4BitColor());
    assertFalse(background.isUserCustomizable());
    assertEquals(ImageRepeatMode.HORIZONTAL, background.getBackgroundImageRepeatMode());
    assertEquals(ClassicThemeBehavior.INHERIT_FROM_OS, form.getClassicThemeBehavior());
    
    {
      Form.HeaderFrameSettings headerFrame = form.getHeaderFrameSettings();
      assertTrue(headerFrame.isUseHeader());
      assertEquals(4, headerFrame.getHeaderSize().getAsInt());
      assertEquals(FrameSizingType.PERCENTAGE, headerFrame.getHeaderSizingType().get());
      assertEquals(FrameScrollStyle.AUTO, headerFrame.getScrollStyle().get());
      assertFalse(headerFrame.isAllowResizing());
      assertEquals(5, headerFrame.getBorderWidth().getAsInt());
      assertColorEquals(headerFrame.getBorderColor().get(), 97, 129, 255);
      assertTrue(headerFrame.isUse3DShading());
    }
    
    {
      Form.PrintSettings print = form.getPrintSettings();
      assertFalse(print.isPrintHeaderAndFooterOnFirstPage());
      
      CDHeader header = print.getPrintHeader().get();
      assertEquals("Courier New", header.getFontName());
      assertEquals(36, header.getFontStyle().getPointSize());
      assertEquals(EnumSet.of(FontAttribute.UNDERLINE), header.getFontStyle().getAttributes());
      assertEquals("I am header text", header.getText());
      
      CDHeader footer = print.getPrintFooter().get();
      assertEquals("Default Monospace", footer.getFontName());
      assertEquals(StandardFonts.ROMAN, footer.getFontStyle().getStandardFont().get());
      assertEquals(18, footer.getFontStyle().getPointSize());
      assertEquals(EnumSet.of(FontAttribute.ITALIC), footer.getFontStyle().getAttributes());
      assertEquals("I am footer text", footer.getText());
    }
    
    // Test global script
    String lsGlobalsExpected = IOUtils.resourceToString("/text/testDbDesignForms/form-testls-globals.txt", StandardCharsets.UTF_8);
    assertEquals(toLf(lsGlobalsExpected), toLf(form.getLotusScriptGlobals()));
  }

  @Test
  public void testLsForm2() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("Test LS Form 2").get();
    
    Form.AutoLaunchSettings auto = form.getAutoLaunchSettings();
    assertEquals(AutoLaunchType.OLEOBJ, auto.getType());
    assertEquals("RTItem", auto.getTargetRichTextField().get());
  }

  @Test
  public void testDefaultForm() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("Default Form").get();
    
    assertTrue(form.isDefaultForm());

    Form.WebRenderingSettings web = form.getWebRenderingSettings();
    assertTrue(web.isRenderRichContentOnWeb());
    assertFalse(web.getWebMimeType().isPresent());
    assertFalse(web.getWebCharset().isPresent());
    
    assertFalse(form.getDefaultDataConnectionName().isPresent());
    assertFalse(form.getDefaultDataConnectionObject().isPresent());
    
    Form.AutoLaunchSettings auto = form.getAutoLaunchSettings();
    assertEquals(AutoLaunchType.DOCLINK, auto.getType());

    {
      Form.HeaderFrameSettings headerFrame = form.getHeaderFrameSettings();
      assertTrue(headerFrame.isUseHeader());
      assertEquals(100, headerFrame.getHeaderSize().getAsInt());
      assertEquals(FrameSizingType.PIXELS, headerFrame.getHeaderSizingType().get());
      assertEquals(FrameScrollStyle.NEVER, headerFrame.getScrollStyle().get());
      assertTrue(headerFrame.isAllowResizing());
      assertEquals(1, headerFrame.getBorderWidth().getAsInt());
      assertColorEquals(headerFrame.getBorderColor().get(), 0, 0, 0);
      assertFalse(headerFrame.isUse3DShading());
    }
    
    {
      Form.PrintSettings print = form.getPrintSettings();
      assertTrue(print.isPrintHeaderAndFooterOnFirstPage());
      
      CDHeader header = print.getPrintHeader().get();
      assertEquals("default form header print", header.getText());
      assertEquals("Default Sans Serif", header.getFontName());
      assertEquals(StandardFonts.SWISS, header.getFontStyle().getStandardFont().get());
      assertEquals(9, header.getFontStyle().getPointSize());
      assertEquals(EnumSet.noneOf(FontAttribute.class), header.getFontStyle().getAttributes());
      
      assertFalse(print.getPrintFooter().isPresent());
    }
  }

  @Test
  public void testOtherTypeForm() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("Other Type Form").get();

    Form.WebRenderingSettings web = form.getWebRenderingSettings();
    assertFalse(web.isRenderRichContentOnWeb());
    assertEquals("text/css", web.getWebMimeType().get());
    assertFalse(web.getWebCharset().isPresent());
    
    Form.AutoLaunchSettings auto = form.getAutoLaunchSettings();
    assertEquals(AutoLaunchType.URL, auto.getType());
    
    Form.BackgroundSettings background = form.getBackgroundSettings();
    assertEquals(StandardColors.White, background.getStandardBackgroundColor().get());
    assertColorEquals(background.getBackgroundColor(), 255, 255, 255);
    assertFalse(background.getBackgroundImage().isPresent());
    CDResource image = background.getBackgroundImageResource().get();
    assertEquals("Untitled 3.gif", image.getNamedElement().get());
    assertFalse(background.isHideGraphicInDesignMode());
    assertFalse(background.isHideGraphicOn4BitColor());
    assertTrue(background.isUserCustomizable());
    assertEquals(ImageRepeatMode.TILE, background.getBackgroundImageRepeatMode());
    assertEquals(ClassicThemeBehavior.DONT_INHERIT_FROM_OS, form.getClassicThemeBehavior());

    {
      Form.HeaderFrameSettings headerFrame = form.getHeaderFrameSettings();
      assertFalse(headerFrame.isUseHeader());
      assertFalse(headerFrame.getBorderColor().isPresent());
      assertFalse(headerFrame.getBorderWidth().isPresent());
      assertFalse(headerFrame.getHeaderSize().isPresent());
      assertFalse(headerFrame.getHeaderSizingType().isPresent());
      assertFalse(headerFrame.getScrollStyle().isPresent());
    }
  }

  @Test
  public void testFooterSubform() throws IOException {
    DbDesign design = this.database.getDesign();
    Subform subform = design.getSubform("Footer").get();
    
    assertTrue(subform.isIncludeInInsertSubformDialog());
    assertFalse(subform.isIncludeInNewFormDialog());
    assertTrue(subform.isRenderPassThroughHtmlInClient());
    assertTrue(subform.isIncludeFieldsInIndex());
    
    assertTrue(subform.isAllowPublicAccess());
    
    ActionBar actions = subform.getActionBar();
    List<ActionBarAction> actionList = actions.getActions();
    assertEquals(1, actionList.size());
    {
      ActionBarAction action = actionList.get(0);
      assertEquals("Footer Action", action.getName());
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@StatusBar(\"hello.\")", ((FormulaActionContent)content).getFormula());
    }
    
    Collection<ScriptEvent> events = subform.getJavaScriptEvents();
    assertEquals(1, events.size());
    ScriptEvent evt = events.iterator().next();
    assertEquals(EventId.ONHELP, evt.getEventId());
    assertFalse(evt.isClient());
    assertEquals("/* I'm subform help */\n", toLf(evt.getScript()));
    
    // Test global script
    String lsGlobalsExpected = IOUtils.resourceToString("/text/testDbDesignForms/globals-blank.txt", StandardCharsets.UTF_8);
    assertEquals(toLf(lsGlobalsExpected), toLf(subform.getLotusScriptGlobals()));
  }

  @Test
  public void testComputedTargetSubform() throws IOException {
    DbDesign design = this.database.getDesign();
    Subform subform = design.getSubform("Computed Target").get();
    
    assertFalse(subform.isIncludeInInsertSubformDialog());
    assertTrue(subform.isIncludeInNewFormDialog());
    assertFalse(subform.isRenderPassThroughHtmlInClient());
    assertFalse(subform.isIncludeFieldsInIndex());
  }

  @Test
  public void testInherForm() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("Test Inher").get();
    
    Map<String, String> ls = form.getFieldLotusScript();
    assertEquals(Collections.singleton("SomeField"), ls.keySet());
    String expected = IOUtils.resourceToString("/text/testDbDesignForms/inherSomeField.txt", StandardCharsets.UTF_8);
    assertEquals(toLf(expected), toLf(ls.get("SomeField")));
    
    {
      List<CDResource> stylesheets = form.getIncludedStyleSheets();
      assertEquals(1, stylesheets.size());
      CDResource sheet = stylesheets.get(0);
      assertEquals("style.css", sheet.getNamedElement().get());
    }
    assertEquals("\"I am HTML head\"", form.getHtmlHeadContentFormula().get());
    assertEquals("\"I am HTML body\"", form.getHtmlBodyAttributesFormula().get());
    assertEquals("@StatusBar(\"I am webqueryopen\")", form.getWebQueryOpenFormula().get());
    assertEquals("@StatusBar(\"I am webquerysave\")", form.getWebQuerySaveFormula().get());
    assertEquals("\"I am target frame\"", form.getTargetFrameFormula().get());
    
    Map<EventId, String> formulas = form.getFormulaEvents();
    assertEquals("@StatusBar(\"I am queryopen\")", formulas.get(EventId.CLIENT_FORM_QUERYOPEN));
    assertEquals("@StatusBar(\"I am postopen\")", formulas.get(EventId.CLIENT_FORM_POSTOPEN));
    assertEquals("@StatusBar(\"I am querymodechange\")", formulas.get(EventId.CLIENT_FORM_QUERYMODE));
    assertEquals("@StatusBar(\"I am postmodechange\")", formulas.get(EventId.CLIENT_FORM_POSTMODE));
    assertEquals("@StatusBar(\"I am queryrecalc\")", formulas.get(EventId.CLIENT_FORM_QUERYRECALC));
    assertEquals("@StatusBar(\"I am postrecalc\")", formulas.get(EventId.CLIENT_FORM_POSTRECALC));
    assertEquals("@StatusBar(\"I am querysave\")", formulas.get(EventId.CLIENT_FORM_QUERYSAVE));
    assertEquals("@StatusBar(\"I am postsave\")", formulas.get(EventId.CLIENT_FORM_POSTSAVE));
    assertEquals("@StatusBar(\"I am querysend\")", formulas.get(EventId.CLIENT_FORM_QUERYSEND));
    assertEquals("@StatusBar(\"I am postsend\")", formulas.get(EventId.CLIENT_FORM_POSTSEND));
    assertEquals("@StatusBar(\"I am queryclose\")", formulas.get(EventId.CLIENT_FORM_QUERYCLOSE));
    assertEquals("@StatusBar(\"I am onsize\")", formulas.get(EventId.CLIENT_FORM_ONSIZE));
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
    "pernames.ntf",
    "bookmark.ntf",
    "log.ntf",
    "mailbox.ntf",
    "roamingdata.ntf",
    "autosave.ntf",
    "doclbs7.ntf",
    "headline.ntf",
    "busytime.ntf"
  })
  public void testStockFormUnknownRecords(String dbName) {
    Set<RecordType> types = new HashSet<>();
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getForms()
      .map(Form::getBody)
      .flatMap(List::stream)
      .forEach(rec -> {
        short type = 0;
        if(rec instanceof GenericBSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        } else if(rec instanceof GenericWSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        } else if(rec instanceof GenericLSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        }
        if(type != 0) {
          RecordType rtype = null;
          rtype = RecordType.getRecordTypeForConstant(type, Area.TYPE_COMPOSITE);
          if(rtype != null) {
            types.add(rtype);
          } else {
            System.out.println("Unable to locate rich text RecordType value for " + type + "; candidates are " + RecordType.getRecordTypesForConstant(type));
          }
        }
      });
    
    if(!types.isEmpty()) {
      System.out.println("Encountered unimplemented CD record types: " + types);
    }
  }
  
  @Test
  public void testImportedFormsUnknownRecords() {
    Set<RecordType> types = new HashSet<>();
    database.getDesign()
      .getForms()
      .map(Form::getBody)
      .flatMap(List::stream)
      .forEach(rec -> {
        short type = 0;
        if(rec instanceof GenericBSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        } else if(rec instanceof GenericWSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        } else if(rec instanceof GenericLSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        }
        if(type != 0) {
          RecordType rtype = null;
          rtype = RecordType.getRecordTypeForConstant(type, Area.TYPE_COMPOSITE);
          if(rtype != null) {
            types.add(rtype);
          } else {
            System.out.println("Unable to locate rich text RecordType value for " + type + "; candidates are " + RecordType.getRecordTypesForConstant(type));
          }
        }
      });
    
    if(!types.isEmpty()) {
      System.out.println("Encountered unimplemented CD record types: " + types);
    }
  }
  
  public static class FolderNSFsArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      String formFolder = System.getenv(ENV_DBDESIGN_FOLDER);
      Path dir = Paths.get(formFolder);
      return Files.find(dir, Integer.MAX_VALUE, (path, attr) -> path.getFileName().toString().toLowerCase().endsWith(".nsf"))
        .map(Arguments::of);
    }
  }
  
  @ParameterizedTest
  @EnabledIfEnvironmentVariable(named = ENV_DBDESIGN_FOLDER, matches = ".+")
  @ArgumentsSource(FolderNSFsArgumentsProvider.class)
  public void testConfiguredDirectoryFormsUnknownRecords(Path nsfPath) {
    Set<RecordType> types = new HashSet<>();
    Database db = getClient().openDatabase("", nsfPath.toString());
    db.getDesign()
      .getForms()
      .map(Form::getBody)
      .flatMap(List::stream)
      .forEach(rec -> {
        short type = 0;
        if(rec instanceof GenericBSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        } else if(rec instanceof GenericWSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        } else if(rec instanceof GenericLSIGRecord) {
          type = ((RichTextRecord<?>)rec).getTypeValue();
        }
        if(type != 0) {
          RecordType rtype = null;
          rtype = RecordType.getRecordTypeForConstant(type, Area.TYPE_COMPOSITE);
          if(rtype != null) {
            types.add(rtype);
          } else {
            System.out.println("Unable to locate rich text RecordType value for " + type + "; candidates are " + RecordType.getRecordTypesForConstant(type));
          }
        }
      });
    
    if(!types.isEmpty()) {
      System.out.println("Encountered unimplemented CD record types: " + types + " (" + nsfPath + ")");
    }
  }
  
  @ParameterizedTest
  @EnabledIfEnvironmentVariable(named = ENV_DBDESIGN_FOLDER, matches = ".+")
  @ArgumentsSource(FolderNSFsArgumentsProvider.class)
  public void testConfiguredDirectoryDocsUnknownRecords(Path nsfPath) {
    Set<RecordType> types = new HashSet<>();
    Database db = getClient().openDatabase("", nsfPath.toString());
    db.queryFormula("@All", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.DOCUMENT))
      .getDocuments()
      .forEach(doc -> {
        doc.allItems()
          .filter(item -> item.getType() == ItemDataType.TYPE_COMPOSITE)
          .map(item -> item.getValueRichText())
          .flatMap(List::stream)
          .forEach(rec -> {
            short type = 0;
            if(rec instanceof GenericBSIGRecord) {
              type = ((RichTextRecord<?>)rec).getTypeValue();
            } else if(rec instanceof GenericWSIGRecord) {
              type = ((RichTextRecord<?>)rec).getTypeValue();
            } else if(rec instanceof GenericLSIGRecord) {
              type = ((RichTextRecord<?>)rec).getTypeValue();
            }
            if(type != 0) {
              RecordType rtype = null;
              rtype = RecordType.getRecordTypeForConstant(type, Area.TYPE_COMPOSITE);
              if(rtype != null) {
                types.add(rtype);
              } else {
                System.out.println("Unable to locate rich text RecordType value for " + type + "; candidates are " + RecordType.getRecordTypesForConstant(type));
              }
            }
          });
      });
    
    if(!types.isEmpty()) {
      System.out.println("Encountered unimplemented CD record types: " + types + " (" + nsfPath + ")");
    }
  }
  
  @Test
  public void testActivityReport() {
    DbDesign design = database.getDesign();
    
    Form form = design.getForm("Activity Report").get();
    
    List<?> body = form.getBody();
    
    CDMacMetaHeader header = extract(body, 0, CDMacMetaHeader.class);
    assertEquals(640, header.getOriginalDisplaySize().getWidth());
    assertEquals(640, header.getOriginalDisplaySize().getHeight());
    assertEquals(2324, header.getMetafileSize());
    assertEquals(1, header.getSegCount());
    
    CDMacMetaSegment seg = extract(body, 0, CDMacMetaSegment.class);
    assertEquals(2324, seg.getDataSize());
    assertEquals(2324, seg.getSegSize());
  }
  
  @Test
  public void testLotusComponentsForm() {
    DbDesign design = database.getDesign();
    
    Form form = design.getForm("Lotus Components Form").get();
    
    List<?> body = form.getBody();
    
    // The form contains several embedded Lotus Components objects,
    //   the first of which has a single Win-meta header/segment pair
    {
      List<?> seg = extractOle(body, 0);
      
      CDOLEBegin begin = seg.stream()
        .filter(CDOLEBegin.class::isInstance)
        .map(CDOLEBegin.class::cast)
        .findFirst()
        .get();
      assertEquals(CDOLEBegin.Version.VERSION2, begin.getVersion().get());
      assertEquals(EnumSet.of(CDOLEBegin.Flag.OBJECT), begin.getFlags());
      assertEquals(CDOLEBegin.ClipFormat.METAFILE, begin.getClipFormat().get());
      assertEquals("EXT25566", begin.getAttachmentName());
      assertEquals("Lotus.Draw.1", begin.getClassName());
      assertEquals("", begin.getTemplateName());
      
      CDWinMetaHeader header = seg.stream()
        .filter(CDWinMetaHeader.class::isInstance)
        .map(CDWinMetaHeader.class::cast)
        .findFirst()
        .get();
      assertEquals(CDWinMetaHeader.MappingMode.ANISOTROPIC, header.getMappingMode().get());
      assertEquals((short)10583, header.getXExtent());
      assertEquals((short)7938, header.getYExtent());
      assertEquals(6000, header.getOriginalDisplaySize().getWidth());
      assertEquals(4500, header.getOriginalDisplaySize().getHeight());
      assertEquals(3548, header.getMetafileSize());
      assertEquals(1, header.getSegCount());
      
      CDWinMetaSegment segment = seg.stream()
        .filter(CDWinMetaSegment.class::isInstance)
        .map(CDWinMetaSegment.class::cast)
        .findFirst()
        .get();
      assertEquals(3548, segment.getDataSize());
      assertEquals(3548, segment.getSegSize());
    }
    
    // The file viewer has some Notes/FX bindings
    {
      List<?> seg = extractOle(body, 2);
      CDOLEBegin begin = seg.stream()
          .filter(CDOLEBegin.class::isInstance)
          .map(CDOLEBegin.class::cast)
          .findFirst()
          .get();
        assertEquals(CDOLEBegin.Version.VERSION2, begin.getVersion().get());
        assertEquals(EnumSet.of(CDOLEBegin.Flag.OBJECT), begin.getFlags());
        assertEquals(CDOLEBegin.ClipFormat.METAFILE, begin.getClipFormat().get());
        assertEquals("EXT05342", begin.getAttachmentName());
        assertEquals("Lotus.FileViewer.1", begin.getClassName());
        assertEquals("", begin.getTemplateName());
    }
    
    // There are four $OLEOBJINFO fields
    AtomicInteger index = new AtomicInteger(0);
    form.getDocument().forEachItem(DesignConstants.OLE_OBJECT_ITEM, (item, loop) -> {
      switch(index.get()) {
      case 0: {
        CDOLEObjectInfo info = (CDOLEObjectInfo)item.getValueRichText().get(0);
        assertEquals(CDOLEObjectInfo.StorageFormat.STRUCT_STORAGE, info.getStorageFormat().get());
        assertEquals(DDEFormat.METAFILE, info.getDisplayFormat().get());
        assertEquals(EnumSet.of(CDOLEObjectInfo.Flag.CONTROL, CDOLEObjectInfo.Flag.UPDATEFROMDOCUMENT), info.getFlags());
        assertEquals((short)0, info.getStorageFormatAppearedIn());
        assertEquals("EXT25566", info.getFileObjectName());
        assertEquals("Lotus Draw/Diagram Component", info.getDescription());
        assertEquals("$Body", info.getFieldName());
        assertEquals("", info.getTextIndexObjectName());
        assertEquals("", info.getHtmlData());
        assertArrayEquals(new byte[0], info.getAssociatedFileData());
        break;
      }
      case 1: {
        CDOLEObjectInfo info = (CDOLEObjectInfo)item.getValueRichText().get(0);
        assertEquals(CDOLEObjectInfo.StorageFormat.STRUCT_STORAGE, info.getStorageFormat().get());
        assertEquals(DDEFormat.METAFILE, info.getDisplayFormat().get());
        assertEquals(EnumSet.of(CDOLEObjectInfo.Flag.CONTROL, CDOLEObjectInfo.Flag.UPDATEFROMDOCUMENT), info.getFlags());
        assertEquals((short)0, info.getStorageFormatAppearedIn());
        assertEquals("EXT36062", info.getFileObjectName());
        assertEquals("Lotus Comment Component", info.getDescription());
        assertEquals("$Body", info.getFieldName());
        assertEquals("", info.getTextIndexObjectName());
        assertEquals("", info.getHtmlData());
        assertArrayEquals(new byte[0], info.getAssociatedFileData());
        break;
      }
      case 2: {
        CDOLEObjectInfo info = (CDOLEObjectInfo)item.getValueRichText().get(0);
        assertEquals(CDOLEObjectInfo.StorageFormat.STRUCT_STORAGE, info.getStorageFormat().get());
        assertEquals(DDEFormat.METAFILE, info.getDisplayFormat().get());
        assertEquals(EnumSet.of(CDOLEObjectInfo.Flag.CONTROL, CDOLEObjectInfo.Flag.UPDATEFROMDOCUMENT), info.getFlags());
        assertEquals((short)0, info.getStorageFormatAppearedIn());
        assertEquals("EXT05342", info.getFileObjectName());
        assertEquals("Lotus File Viewer Component", info.getDescription());
        assertEquals("$Body", info.getFieldName());
        assertEquals("", info.getTextIndexObjectName());
        assertEquals("", info.getHtmlData());
        assertArrayEquals(new byte[0], info.getAssociatedFileData());
        break;
      }
      case 3: {
        CDOLEObjectInfo info = (CDOLEObjectInfo)item.getValueRichText().get(0);
        assertEquals(CDOLEObjectInfo.StorageFormat.STRUCT_STORAGE, info.getStorageFormat().get());
        assertEquals(DDEFormat.METAFILE, info.getDisplayFormat().get());
        assertEquals(EnumSet.of(CDOLEObjectInfo.Flag.CONTROL, CDOLEObjectInfo.Flag.UPDATEFROMDOCUMENT), info.getFlags());
        assertEquals((short)0, info.getStorageFormatAppearedIn());
        assertEquals("EXT60758", info.getFileObjectName());
        assertEquals("Lotus Spreadsheet Component", info.getDescription());
        assertEquals("$Body", info.getFieldName());
        assertEquals("", info.getTextIndexObjectName());
        assertEquals("", info.getHtmlData());
        assertArrayEquals(new byte[0], info.getAssociatedFileData());
        break;
      }
      default:
        throw new IllegalStateException("Encounted unexpected OLE item index " + index.get());
      }
      index.incrementAndGet();
    });
  }
  
  @Test
  public void testTimerTable() {
    DbDesign design = database.getDesign();
    
    // This test re-uses the same form as above
    Form form = design.getForm("Test LS Form").get();
    
    List<?> pretable = extract(
      form.getBody(),
      0,
      r -> r instanceof CDBegin && RecordType.PRETABLEBEGIN.getConstant() == ((CDBegin)r).getSignature(),
      r -> r instanceof CDEnd && RecordType.PRETABLEBEGIN.getConstant() == ((CDEnd)r).getSignature()
    );
    assertTrue(pretable.stream().anyMatch(CDTableDataExtension.class::isInstance));
    
    List<?> table = extract(form.getBody(), 0, CDTableBegin.class, CDTableEnd.class);
    assertInstanceOf(CDTableBegin.class, table.get(0));
    assertInstanceOf(CDTableEnd.class, table.get(table.size()-1));
    
    // Make sure we can find out text bits
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("I")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("am")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("an")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("animated table")));
    
    CDTimerInfo timer = table.stream()
      .filter(CDTimerInfo.class::isInstance)
      .map(CDTimerInfo.class::cast)
      .findFirst()
      .get();
    assertEquals(2500l, timer.getInterval());
    
    CDTransition trans = table.stream()
      .filter(CDTransition.class::isInstance)
      .map(CDTransition.class::cast)
      .findFirst()
      .get();
    assertEquals(CDTransition.Type.TOPTOBOTTOM_ROW, trans.getTransitionType().get());
  }
  
  @Test
  public void testTabbedTable() {
    DbDesign design = database.getDesign();
    
    // This test re-uses the same form as above
    Form form = design.getForm("Test LS Form").get();
    
    List<?> pretable = extract(
      form.getBody(),
      1,
      r -> r instanceof CDBegin && RecordType.PRETABLEBEGIN.getConstant() == ((CDBegin)r).getSignature(),
      r -> r instanceof CDEnd && RecordType.PRETABLEBEGIN.getConstant() == ((CDEnd)r).getSignature()
    );
    assertTrue(pretable.stream().anyMatch(CDTableDataExtension.class::isInstance));
    CDPreTableBegin pre = pretable.stream()
      .filter(CDPreTableBegin.class::isInstance)
      .map(CDPreTableBegin.class::cast)
      .findFirst()
      .get();
    assertEquals(EnumSet.of(CDPreTableBegin.Flag.SHOWTABSONLEFT), pre.getFlags());
    
    List<?> table = extract(form.getBody(), 1, CDTableBegin.class, CDTableEnd.class);
    assertInstanceOf(CDTableBegin.class, table.get(0));
    assertInstanceOf(CDTableEnd.class, table.get(table.size()-1));
    
    // Make sure we can find out text bits
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("I")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("am")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("a")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("tabbed table")));
  }
  
  @Test
  public void testCaptionTable() {
    DbDesign design = database.getDesign();
    
    // This test re-uses the same form as above
    Form form = design.getForm("Test LS Form").get();
    
    List<?> pretable = extract(
      form.getBody(),
      2,
      r -> r instanceof CDBegin && RecordType.PRETABLEBEGIN.getConstant() == ((CDBegin)r).getSignature(),
      r -> r instanceof CDEnd && RecordType.PRETABLEBEGIN.getConstant() == ((CDEnd)r).getSignature()
    );
    assertTrue(pretable.stream().anyMatch(CDTableDataExtension.class::isInstance));
    
    List<?> table = extract(form.getBody(), 2, CDTableBegin.class, CDTableEnd.class);
    assertInstanceOf(CDTableBegin.class, table.get(0));
    assertInstanceOf(CDTableEnd.class, table.get(table.size()-1));
    
    // Make sure we can find out text bits
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("i")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("am")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("a")));
    assertTrue(table.stream().anyMatch(r -> r instanceof CDText && ((CDText)r).getText().equals("caption table")));
    CDTableLabel label = table.stream()
      .filter(CDTableLabel.class::isInstance)
      .map(CDTableLabel.class::cast)
      .findFirst()
      .get();
    assertEquals("i am caption", label.getLabel());
  }
  
  @Test
  public void testJavaApplet() {
    DbDesign design = database.getDesign();
    
    // This test re-uses the same form as above
    Form form = design.getForm("Test LS Form").get();
    
    List<?> hotspot = extract(
      form.getBody(),
      0,
      r -> r instanceof CDBegin && ((CDBegin)r).getSignature() == RecordType.V4HOTSPOTBEGIN.getConstant(),
      r -> r instanceof CDEnd
    );
    CDHotspotBegin hotspotBegin = hotspot.stream()
      .filter(CDHotspotBegin.class::isInstance)
      .map(CDHotspotBegin.class::cast)
      .filter(h -> h.getHotspotType().get().equals(HotspotType.ACTIVEOBJECT))
      .findFirst()
      .get();
    
    ActiveObject obj = hotspotBegin.getActiveObject().get();
    assertEquals(ActiveObject.Version.VERSION1, obj.getVersion().get());
    assertEquals(ActiveObject.Type.JAVA, obj.getObjectType().get());
    assertEquals(EnumSet.of(ActiveObject.Flag.CORBA_APPLET, ActiveObject.Flag.NOCORBADOWNLOAD), obj.getFlags());
    assertEquals(ActiveObject.Unit.PIXELS, obj.getWidthUnitType().get());
    assertEquals(ActiveObject.Unit.PIXELS, obj.getHeightUnitType().get());
    assertEquals(200, obj.getWidth());
    assertEquals(200, obj.getHeight());
    assertEquals("", obj.getDocUrlName());
    assertEquals("notes:///./$FILE", obj.getCodebase());
    assertEquals("somejar.class", obj.getCode());
    assertEquals("", obj.getObjectName());
    {
      List<ActiveObjectParam> params = obj.getParams();
      {
        ActiveObjectParam param = params.get(0);
        assertEquals("foo", param.getParam());
        assertEquals("\"ba\" + \"r\"", param.getFormula());
      }
      {
        ActiveObjectParam param = params.get(1);
        assertEquals("bar", param.getParam());
        assertEquals("\"baz\"", param.getFormula());
      }
    }
    {
      List<ActiveObjectStorageLink> links = obj.getStorageLinks();
      {
        ActiveObjectStorageLink link = links.get(0);
        assertEquals("somejar.jar", link.getLink());
      }
    }
    
    {
      CDHtmlFormula formula = (CDHtmlFormula)extract(hotspot, 0, CDHtmlFormula.class, CDHtmlFormula.class).get(0);
      assertEquals(EnumSet.of(CDHtmlFormula.Flag.ALT), formula.getFlags());
      assertEquals("\"i am alt html\"", formula.getFormula());
    }
    {
      CDHtmlFormula formula = (CDHtmlFormula)extract(hotspot, 1, CDHtmlFormula.class, CDHtmlFormula.class).get(0);
      assertEquals(EnumSet.of(CDHtmlFormula.Flag.ATTR), formula.getFlags());
      assertEquals("\"i am html attrs\"", formula.getFormula());
    }
  }
  
  @Test
  public void testLayoutRegion() {
    DbDesign design = database.getDesign();
    Form form = design.getForm("Layout Form").get();
    
    List<?> body = form.getBody();
    
    List<?> layout = extract(body, 0, CDLayout.class, CDLayoutEnd.class);
    
    {
      CDLayout begin = (CDLayout)layout.get(0);
      assertEquals(1440, begin.getLeft());
      assertEquals(10681, begin.getWidth());
      assertEquals(4501, begin.getHeight());
      assertEquals(EnumSet.of(CDLayout.Flag.SHOWBORDER, CDLayout.Flag.SHOWGRID, CDLayout.Flag.STYLE3D, CDLayout.Flag.DONTWRAP), begin.getFlags());
      assertEquals(144, begin.getGridSize());
    }
    {
      CDLayoutGraphic graphic = layout.stream()
        .filter(CDLayoutGraphic.class::isInstance)
        .map(CDLayoutGraphic.class::cast)
        .findFirst()
        .get();
      assertEquals(EnumSet.noneOf(CDLayoutGraphic.Flag.class), graphic.getFlags());
      ElementHeader header = graphic.getElementHeader();
      assertEquals(5820, header.getLeft());
      assertEquals(2106, header.getTop());
      assertEquals(586, header.getWidth());
      assertEquals(466, header.getHeight());
      assertColorEquals(header.getBackgroundColor(), 255, 255, 255);
      assertEquals(StandardColors.White, header.getPreV5BackgroundColor().get());
    }
    {
      CDLayoutField field = layout.stream()
        .filter(CDLayoutField.class::isInstance)
        .map(CDLayoutField.class::cast)
        .findFirst()
        .get();
      assertEquals(EnumSet.of(CDLayoutField.Flag.VSCROLL, CDLayoutField.Flag.LEFT), field.getFlags());
      assertEquals(CDLayoutField.Type.TEXT, field.getFieldType().get());
      ElementHeader header = field.getElementHeader();
      assertEquals(7740, header.getLeft());
      assertEquals(936, header.getTop());
      assertEquals(1921, header.getWidth());
      assertEquals(361, header.getHeight());
    }
    {
      CDLayoutButton field = layout.stream()
        .filter(CDLayoutButton.class::isInstance)
        .map(CDLayoutButton.class::cast)
        .findFirst()
        .get();
      
      ElementHeader header = field.getElementHeader();
      assertEquals(2580, header.getLeft());
      assertEquals(2661, header.getTop());
      assertEquals(2041, header.getWidth());
      assertEquals(451, header.getHeight());
    }
    {
      @SuppressWarnings("unused")
      CDLayoutEnd end = (CDLayoutEnd)layout.get(layout.size()-1);
    }
  }
  
  @Test
  public void testSpanDocument() {
    Document doc = database.queryFormula("Form='bar'", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.DOCUMENT))
      .getDocuments()
      .findFirst()
      .get();
    RichTextRecordList body = doc.getRichTextItem("Body");
    
    List<?> span = extract(
      body,
      0,
      rec -> rec instanceof CDSpanRecord && ((CDSpanRecord)rec).getType().contains(RecordType.SPAN_BEGIN),
      rec -> rec instanceof CDSpanRecord && ((CDSpanRecord)rec).getType().contains(RecordType.SPAN_END)
    );
    {
      CDSpanRecord begin = (CDSpanRecord)span.get(0);
      assertEquals(0, begin.getPropId());
    }
    {
      CDSpanRecord end = (CDSpanRecord)span.get(span.size()-1);
      assertEquals(0, end.getPropId());
    }

    span = extract(
      body,
      1,
      rec -> rec instanceof CDSpanRecord && ((CDSpanRecord)rec).getType().contains(RecordType.SPAN_BEGIN),
      rec -> rec instanceof CDSpanRecord && ((CDSpanRecord)rec).getType().contains(RecordType.SPAN_END)
    );
    {
      CDSpanRecord begin = (CDSpanRecord)span.get(0);
      assertEquals(1, begin.getPropId());
    }
    {
      CDSpanRecord end = (CDSpanRecord)span.get(span.size()-1);
      assertEquals(1, end.getPropId());
    }
    
    // Check for the text anchor
    CDAnchor anchor = extract(body, 0, CDAnchor.class);
    assertEquals("dsfdf", anchor.getAnchorText());
    
    
    // Now read the text properties info
    {
      RichTextRecordList textProperties = doc.getRichTextItem(NotesConstants.ITEM_NAME_TEXTPROPERTIES);
      
      CDTextPropertiesTable table = (CDTextPropertiesTable)textProperties.get(0);
      assertEquals(2, table.getNumberOfEntries());
      
      {
        CDTextProperty prop = (CDTextProperty)textProperties.get(1);
        assertEquals(0, prop.getPropId());
        assertEquals("EN-US", prop.getLangName());
      }
      {
        CDTextProperty prop = (CDTextProperty)textProperties.get(2);
        assertEquals(1, prop.getPropId());
        assertEquals("FR-FR", prop.getLangName());
      }
    }
  }
  
  @Test
  public void testOnPageButton() {
    DbDesign design = database.getDesign();
    Form form = design.getForm("Button Form").get();
    List<?> body = form.getBody();
    
    {
      CDHotspotBegin button = extract(body, 0, CDHotspotBegin.class);
      assertEquals(HotspotType.BUTTON, button.getHotspotType().get());
      assertEquals("@StatusBar(\"I am button output\")", button.getFormula().get());
    }
    {
      CDHotspotBegin button = extract(body, 1, CDHotspotBegin.class);
      assertEquals(HotspotType.BUTTON, button.getHotspotType().get());
      assertEquals("'++LotusScript Development Environment:2:5:(Options):0:66\n"
          + "\n"
          + "'++LotusScript Development Environment:2:5:(Forward):0:1\n"
          + "Declare Sub Click(Source As Button)\n"
          + "\n"
          + "'++LotusScript Development Environment:2:5:(Declarations):0:2\n"
          + "\n"
          + "'++LotusScript Development Environment:2:2:BindEvents:1:129\n"
          + "Private Sub BindEvents(Byval Objectname_ As String)\n"
          + "\tStatic Source As BUTTON\n"
          + "\tSet Source = Bind(Objectname_)\n"
          + "\tOn Event Click From Source Call Click\n"
          + "End Sub\n"
          + "\n"
          + "'++LotusScript Development Environment:2:2:Click:1:12\n"
          + "Sub Click(Source As Button)\n"
          + "\tMsgbox \"hi\"\n"
          + "End Sub\n"
          + "", button.getScript().get());
    }
    {
      CDHotspotBegin button = extract(body, 2, CDHotspotBegin.class);
      assertEquals(HotspotType.BUTTON, button.getHotspotType().get());
      
      List<RichTextRecord<?>> actions = button.getActions().get();
      assertInstanceOf(CDActionHeader.class, actions.get(0));
      CDActionModifyField field = (CDActionModifyField)actions.get(1);
      assertTrue(field.getFlags().contains(CDActionModifyField.Flag.REPLACE));
      assertEquals("DateComposed", field.getFieldName());
      assertEquals("1/1/2022", field.getValue());
    }
  }
  
  @Test
  public void testFieldEvents() {
    DbDesign design = database.getDesign();
    Form form = design.getForm("Field Events Form").get();
    List<?> body = form.getBody();
    
    List<?> field = extract(body, 0, CDBegin.class, CDEnd.class);
    {
      CDExt2Field ext2Field = extract(field, 0, CDExt2Field.class);
      assertEquals("\"I am input enabled\"", ext2Field.getInputEnabledFormula());
    }
    {
      CDExtField extField = extract(field, 0, CDExtField.class);
      assertEquals(1, extField.getEntryColumnNumber());
      assertEquals("", extField.getEntryDBName());
      assertEquals("Action View", extField.getEntryViewName());
      assertEquals("\"I am HTML attributes\"", extField.getHtmlAttributesFormula());
      
      extField.setHtmlAttributesFormula("\"I am new HTML attributes\"");
      assertEquals("", extField.getEntryDBName());
      assertEquals("Action View", extField.getEntryViewName());
      assertEquals("\"I am new HTML attributes\"", extField.getHtmlAttributesFormula());
    }
    {
      CDField someField = extract(field, 0, CDField.class);
      assertEquals("SomeField", someField.getName());
      assertEquals("\"I am the default value\"", someField.getDefaultValueFormula());
      assertEquals("\"I am the input translation\"", someField.getInputTranslationFormula());
      assertEquals("\"I am input validation\"", someField.getInputValidationFormula());
    }
    {
      CDEvent event = extract(field, 0, CDEvent.class);
      assertEquals(EventId.ONBLUR, event.getEventType().get());
      CDBlobPart blob = extract(field, 0, CDBlobPart.class);
      assertEquals("alert(\"I am web onBlur\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 1, CDEvent.class);
      assertEquals(EventId.ONCHANGE, event.getEventType().get());
      CDBlobPart blob = extract(field, 1, CDBlobPart.class);
      assertEquals("alert(\"I am common onChange\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 2, CDEvent.class);
      assertEquals(EventId.ONCLICK, event.getEventType().get());
      CDBlobPart blob = extract(field, 2, CDBlobPart.class);
      assertEquals("alert(\"I am web onClick\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 3, CDEvent.class);
      assertEquals(EventId.ONDBLCLICK, event.getEventType().get());
      CDBlobPart blob = extract(field, 3, CDBlobPart.class);
      assertEquals("alert(\"I am web onDblClick\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 4, CDEvent.class);
      assertEquals(EventId.ONFOCUS, event.getEventType().get());
      CDBlobPart blob = extract(field, 4, CDBlobPart.class);
      assertEquals("alert(\"I am web onFocus\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 5, CDEvent.class);
      assertEquals(EventId.ONKEYDOWN, event.getEventType().get());
      CDBlobPart blob = extract(field, 5, CDBlobPart.class);
      assertEquals("alert(\"I am web onKeyDown\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 6, CDEvent.class);
      assertEquals(EventId.ONKEYPRESS, event.getEventType().get());
      CDBlobPart blob = extract(field, 6, CDBlobPart.class);
      assertEquals("alert(\"I am web onKeyPress\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 7, CDEvent.class);
      assertEquals(EventId.ONKEYUP, event.getEventType().get());
      CDBlobPart blob = extract(field, 7, CDBlobPart.class);
      assertEquals("alert(\"I am web onKeyUp\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 8, CDEvent.class);
      assertEquals(EventId.ONMOUSEDOWN, event.getEventType().get());
      CDBlobPart blob = extract(field, 8, CDBlobPart.class);
      assertEquals("alert(\"I am web onMouseDown\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 9, CDEvent.class);
      assertEquals(EventId.ONMOUSEMOVE, event.getEventType().get());
      CDBlobPart blob = extract(field, 9, CDBlobPart.class);
      assertEquals("alert(\"I am web onMouseMove\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 10, CDEvent.class);
      assertEquals(EventId.ONMOUSEOUT, event.getEventType().get());
      CDBlobPart blob = extract(field, 10, CDBlobPart.class);
      assertEquals("alert(\"I am web onMouseOut\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 11, CDEvent.class);
      assertEquals(EventId.ONMOUSEOVER, event.getEventType().get());
      CDBlobPart blob = extract(field, 11, CDBlobPart.class);
      assertEquals("alert(\"I am web onMouseOver\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 12, CDEvent.class);
      assertEquals(EventId.ONMOUSEUP, event.getEventType().get());
      CDBlobPart blob = extract(field, 12, CDBlobPart.class);
      assertEquals("alert(\"I am web onMouseUp\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 13, CDEvent.class);
      assertEquals(EventId.ONSELECT, event.getEventType().get());
      CDBlobPart blob = extract(field, 13, CDBlobPart.class);
      assertEquals("alert(\"I am web onSelect\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEvent event = extract(field, 14, CDEvent.class);
      assertEquals(EventId.ONFOCUS, event.getEventType().get());
      CDBlobPart blob = extract(field, 14, CDBlobPart.class);
      assertEquals("alert(\"I am client onFocus\")", StructureSupport.readLmbcsValue(blob.getBlobPartData()));
    }
    {
      CDEventEntry entry = extract(field, 0, CDEventEntry.class);
      assertEquals(CDEventEntry.ActionType.LOTUS_SCRIPT, entry.getActionType().get());
    }
    {
      CDEventEntry entry = extract(field, 1, CDEventEntry.class);
      assertEquals(CDEventEntry.ActionType.JAVASCRIPT, entry.getActionType().get());
    }
    {
      CDEventEntry entry = extract(field, 2, CDEventEntry.class);
      assertEquals(CDEventEntry.ActionType.LOTUS_SCRIPT, entry.getActionType().get());
    }
    {
      CDEventEntry entry = extract(field, 3, CDEventEntry.class);
      assertEquals(CDEventEntry.ActionType.JAVASCRIPT, entry.getActionType().get());
      assertEquals(EventId.ONBLUR, entry.getHtmlEventId().get());
    }
    {
      CDEventEntry entry = extract(field, 4, CDEventEntry.class);
      assertEquals(EventId.ONCHANGE, entry.getHtmlEventId().get());
      assertEquals(CDEventEntry.ActionType.JAVASCRIPT, entry.getActionType().get());
    }
    {
      CDEventEntry entry = extract(field, 5, CDEventEntry.class);
      assertEquals(CDEventEntry.ActionType.JAVASCRIPT, entry.getActionType().get());
      assertEquals(EventId.ONFOCUS, entry.getHtmlEventId().get());
    }
  }
  
  /**
   * Tests reading the forms from sample.nsf to ensure that the early-exit loop
   * in RichTextUtil allows for still reading expected actions
   */
  @Test
  public void testSampleDbForms() throws Exception {
    withResourceDb("/nsf/sample.nsf", database -> {
      Form form = database.getDesign().getForm("FamilyInformation").get();
      List<CDHotspotBegin> buttons = form.getBody().stream()
        .filter(CDHotspotBegin.class::isInstance)
        .map(CDHotspotBegin.class::cast)
        .filter(hotspot -> hotspot.getFlags().contains(CDHotspotBegin.Flag.ACTION))
        .collect(Collectors.toList());
      
      {
        CDHotspotBegin button = buttons.get(0);
        List<RichTextRecord<?>> actions = button.getActions().get();
        assertEquals(2, actions.size());
        assertInstanceOf(CDActionHeader.class, actions.get(0));
        assertInstanceOf(CDActionFolder.class, actions.get(1));
      }
      {
        CDHotspotBegin button = buttons.get(1);
        List<RichTextRecord<?>> actions = button.getActions().get();
        assertEquals(2, actions.size());
        assertInstanceOf(CDActionHeader.class, actions.get(0));
        assertInstanceOf(CDActionSendMail.class, actions.get(1));
      }
      
    });
  }
  
  @Test
  public void testDbDesignComputedFields() throws Exception {
    withResourceDxl("/dxl/testDbDesignForms", database -> {
      Form form = database.getDesign().getForm("Computed When Composed").get();
      
      List<FormField> fields = form.getFields();
      
      {
        FormField field = fields.stream()
            .filter(f -> "NormalField".equals(f.getName()))
            .findFirst()
            .get();
        assertEquals(FormField.Kind.EDITABLE, field.getKind());
      }
      {
        FormField field = fields.stream()
            .filter(f -> "ComputedWhenComposed".equals(f.getName()))
            .findFirst()
            .get();
        assertEquals(FormField.Kind.COMPUTEDWHENCOMPOSED, field.getKind());
      }
      {
        FormField field = fields.stream()
            .filter(f -> "ComputedForDisplay".equals(f.getName()))
            .findFirst()
            .get();
        assertEquals(FormField.Kind.COMPUTEDFORDISPLAY, field.getKind());
      }
      {
        FormField field = fields.stream()
            .filter(f -> "Computed".equals(f.getName()))
            .findFirst()
            .get();
        assertEquals(FormField.Kind.COMPUTED, field.getKind());
      }
    });
  }
  
  // *******************************************************************************
  // * Internal utility methods
  // *******************************************************************************
  
  private List<?> extractOle(List<?> body, int index) {
    return extract(body, index, CDOLEBegin.class, CDOLEEnd.class);
  }
}
