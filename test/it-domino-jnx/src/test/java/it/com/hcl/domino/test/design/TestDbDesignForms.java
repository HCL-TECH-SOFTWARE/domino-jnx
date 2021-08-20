package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.ActionBar;
import com.hcl.domino.design.ActionBar.ButtonHeightMode;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.EdgeWidths;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.action.ActionBarAction;
import com.hcl.domino.design.action.ActionContent;
import com.hcl.domino.design.action.FormulaActionContent;
import com.hcl.domino.design.action.JavaScriptActionContent;
import com.hcl.domino.design.action.LotusScriptActionContent;
import com.hcl.domino.design.action.SimpleActionActionContent;
import com.hcl.domino.design.action.SystemActionContent;
import com.hcl.domino.design.form.AutoLaunchHideWhen;
import com.hcl.domino.design.form.AutoLaunchType;
import com.hcl.domino.design.form.AutoLaunchWhen;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.ActionBarTextAlignment;
import com.hcl.domino.design.format.ActionButtonHeightMode;
import com.hcl.domino.design.format.ActionWidthMode;
import com.hcl.domino.design.format.BorderStyle;
import com.hcl.domino.design.format.ButtonBorderDisplay;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.design.format.HtmlEventId;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.design.simpleaction.ModifyFieldAction;
import com.hcl.domino.design.simpleaction.ReadMarksAction;
import com.hcl.domino.design.simpleaction.SendDocumentAction;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.richtext.NotesBitmap;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignForms extends AbstractDesignTest {
  public static final int EXPECTED_IMPORT_FORMS = 6;
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
      assertFalse(action.isLeftAlignedInActionBar());
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
      assertTrue(action.isLeftAlignedInActionBar());
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
      assertEquals(expected, ((LotusScriptActionContent)content).getScript());
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
      assertFalse(action.isLeftAlignedInActionBar());
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
        Collection<JavaScriptActionContent.ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONCLICK) {
              if(event.isClient()) {
                if("window.alert(\"you poor soul, using JavaScript actions in a view\")\n".equals(event.getScript())) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONCLICK) {
              if(!event.isClient()) {
                if("alert(\"I'm on the web\")\n".equals(event.getScript())) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONMOUSEDOWN) {
              if("console.log(\"is there a console in Notes JS actions?\")\n".equals(event.getScript())) {
                return true;
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONMOUSEOVER) {
              if("alert(\"wait, do onMouseOver actions work? No; this is web-only\")\n".equals(event.getScript())) {
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
      assertFalse(action.isLeftAlignedInActionBar());
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
        Collection<JavaScriptActionContent.ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONCLICK) {
              if(event.isClient()) {
                if("window.alert(\"this is the common part\")\n".equals(event.getScript())) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONCLICK) {
              if(!event.isClient()) {
                if("window.alert(\"this is the common part\")\n".equals(event.getScript())) {
                  return true;
                }
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONMOUSEDOWN) {
              if("console.log(\"is there a console in Notes JS actions?\")\n".equals(event.getScript())) {
                return true;
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == HtmlEventId.ONMOUSEOVER) {
              if("alert(\"wait, do onMouseOver actions work?\")\n".equals(event.getScript())) {
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
      assertEquals(expected, ((LotusScriptActionContent)content).getScript());
    }
    {
      ActionBarAction action = actionList.get(15);
      assertEquals("Long JavaScript", action.getName());

      ActionContent content = action.getActionContent();
      assertInstanceOf(JavaScriptActionContent.class, content);
      Collection<JavaScriptActionContent.ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
      assertEquals(1, events.size());
      JavaScriptActionContent.ScriptEvent event = events.stream().findFirst().get();
      String expected = IOUtils.resourceToString("/text/testDbDesignCollections/longjs.js", StandardCharsets.UTF_8).replace('\n', '\r');
      String actual = event.getScript();
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
}
