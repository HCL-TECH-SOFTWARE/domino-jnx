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
package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.CollectionColumn.TotalType;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.ActionBar;
import com.hcl.domino.design.ActionBar.ButtonHeightMode;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.CollectionDesignElement.DisplaySettings;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.EdgeWidths;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.design.View;
import com.hcl.domino.design.action.ActionBarAction;
import com.hcl.domino.design.action.ActionContent;
import com.hcl.domino.design.action.FormulaActionContent;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.action.JavaScriptActionContent;
import com.hcl.domino.design.action.LotusScriptActionContent;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.design.action.SimpleActionActionContent;
import com.hcl.domino.design.action.SystemActionContent;
import com.hcl.domino.design.format.ActionBarBackgroundRepeat;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.ActionBarTextAlignment;
import com.hcl.domino.design.format.ActionButtonHeightMode;
import com.hcl.domino.design.format.ActionWidthMode;
import com.hcl.domino.design.format.BorderStyle;
import com.hcl.domino.design.format.ButtonBorderDisplay;
import com.hcl.domino.design.format.CalendarType;
import com.hcl.domino.design.format.DateComponentOrder;
import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NarrowViewPosition;
import com.hcl.domino.design.format.NumberDisplayFormat;
import com.hcl.domino.design.format.TileViewerPosition;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.TimeZoneFormat;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewLineSpacing;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
import com.hcl.domino.design.simpleaction.ModifyFieldAction;
import com.hcl.domino.design.simpleaction.ReadMarksAction;
import com.hcl.domino.design.simpleaction.SendDocumentAction;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.exception.FileDoesNotExistException;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignCollections extends AbstractDesignTest {
  public static final int EXPECTED_IMPORT_VIEWS = 10;
  public static final int EXPECTED_IMPORT_FOLDERS = 1;

  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(TestDbDesignCollections.dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + TestDbDesignCollections.dbPath + ": " + t);
    }
  }

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (TestDbDesignCollections.dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        
        Acl acl = this.database.getACL();
        Optional<AclEntry> entry = acl.getEntry(client.getEffectiveUserName());
        if(entry.isPresent()) {
          acl.updateEntry(client.getEffectiveUserName(), null, null, Arrays.asList("[Admin]"), null);
        } else {
          acl.addEntry(client.getEffectiveUserName(), AclLevel.MANAGER, Arrays.asList("[Admin]"), EnumSet.allOf(AclFlag.class));
        }
        acl.save();
        
        TestDbDesignCollections.dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignCollections", this.database);
      } else {
        this.database = client.openDatabase("", TestDbDesignCollections.dbPath);
      }
    }
  }

  @Test
  public void testExampleView() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View").get();
    assertTrue(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_TOP, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.REFRESH_DISPLAY, view.getOnRefreshUISetting());
    assertEquals(ClassicThemeBehavior.USE_DATABASE_SETTING, view.getClassicThemeBehavior());
    assertEquals(CollectionDesignElement.Style.STANDARD_OUTLINE, view.getStyle());
    assertFalse(view.isDefaultCollection());
    assertFalse(view.isDefaultCollectionDesign());
    assertFalse(view.isCollapseAllOnFirstOpen());
    assertTrue(view.isShowResponseDocumentsInHierarchy());
    assertFalse(view.isShowInViewMenu());
    assertTrue(view.isEvaluateActionsOnDocumentChange());
    assertFalse(view.isCreateDocumentsAtViewLevel());
    assertEquals("Home_1.xsp", view.getWebXPageAlternative().get());
    assertFalse(view.isAllowPublicAccess());
    assertEquals(Arrays.asList("[Admin]"), view.getReaders().get());
    assertEquals("SELECT @Like(Form; \"Foo\") & IsComplexView=1", view.getSelectionFormula());
    
    CollectionDesignElement.CompositeAppSettings comp = view.getCompositeAppSettings();
    assertNotNull(comp);
    assertTrue(comp.isHideColumnHeader());
    assertFalse(comp.isShowPartialHierarchies());
    assertTrue(comp.isShowSwitcher());
    assertFalse(comp.isShowTabNavigator());
    assertEquals("viewers1", comp.getViewers());
    assertEquals("Trash", comp.getThreadView());
    assertFalse(comp.isAllowConversationMode());
    
    CollectionDesignElement.DisplaySettings disp = view.getDisplaySettings();
    ColorValue background = disp.getBackgroundColor();
    assertEquals(255, background.getRed());
    assertEquals(255, background.getGreen());
    assertEquals(255, background.getBlue());
    assertTrue(disp.isUseAlternateRowColor());
    
    Optional<CDResource> backgroundImage = disp.getBackgroundImage();
    assertTrue(backgroundImage.isPresent());
    assertTrue(backgroundImage.get().getFlags().contains(CDResource.Flag.FORMULA));
    assertEquals("\"hey.png\"", backgroundImage.get().getNamedElementFormula().get());
    
    assertEquals(ImageRepeatMode.SIZE_TO_FIT, disp.getBackgroundImageRepeatMode());
    
    assertEquals(CollectionDesignElement.GridStyle.SOLID, disp.getGridStyle());
    ColorValue gridColor = disp.getGridColor();
    assertEquals(255, gridColor.getRed());
    assertEquals(255, gridColor.getGreen());
    assertEquals(255, gridColor.getBlue());
    
    assertEquals(CollectionDesignElement.HeaderStyle.BEVELED, disp.getHeaderStyle());
    assertEquals(2, disp.getHeaderLines());
    ColorValue headerColor = disp.getHeaderColor();
    assertEquals(255, headerColor.getRed());
    assertEquals(255, headerColor.getGreen());
    assertEquals(255, headerColor.getBlue());
    
    assertEquals(5, disp.getRowLines());
    assertEquals(ViewLineSpacing.ONE_POINT_25_SPACE, disp.getLineSpacing());
    assertTrue(disp.isShrinkRowsToContent());
    assertTrue(disp.isHideEmptyCategories());
    assertFalse(disp.isColorizeViewIcons());
    
    ColorValue unreadColor = disp.getUnreadColor();
    assertEquals(0, unreadColor.getRed());
    assertEquals(0, unreadColor.getGreen());
    assertEquals(0, unreadColor.getBlue());
    assertFalse(unreadColor.getFlags().contains(ColorValue.Flag.NOCOLOR));
    
    assertTrue(disp.isUnreadBold());
    
    ColorValue totalColor = disp.getColumnTotalColor();
    assertEquals(192, totalColor.getRed());
    assertEquals(98, totalColor.getGreen());
    assertEquals(255, totalColor.getBlue());
    
    assertTrue(disp.isShowSelectionMargin());
    assertFalse(disp.isHideSelectionMarginBorder());
    assertTrue(disp.isExtendLastColumnToWindowWidth());
    
    EdgeWidths margin = disp.getMargin();
    assertEquals(1, margin.getTop());
    assertEquals(2, margin.getLeft());
    assertEquals(3, margin.getRight());
    assertEquals(4, margin.getBottom());
    assertEquals(5, disp.getBelowHeaderMargin());
    
    ColorValue marginColor = disp.getMarginColor();
    assertEquals(255, marginColor.getRed());
    assertEquals(255, marginColor.getGreen());
    assertEquals(255, marginColor.getBlue());
    
    assertFalse(view.getAutoFrameFrameset().isPresent());
    assertFalse(view.getAutoFrameTarget().isPresent());
    
    assertEquals(CollectionDesignElement.UnreadMarksMode.NONE, view.getUnreadMarksMode());
    
    CollectionDesignElement.IndexSettings index = view.getIndexSettings();
    assertEquals(CollectionDesignElement.IndexRefreshMode.AUTO_AT_MOST_EVERY, index.getRefreshMode());
    assertEquals(TimeUnit.HOURS.toSeconds(3), index.getRefreshMaxIntervalSeconds().getAsInt());
    assertEquals(CollectionDesignElement.IndexDiscardMode.INACTIVE_FOR, index.getDiscardMode());
    assertEquals(TimeUnit.DAYS.toHours(7), index.getDiscardAfterHours().getAsInt());
    assertFalse(index.isRestrictInitialBuildToDesigner());
    assertTrue(index.isGenerateUniqueKeysInIndex());
    assertFalse(index.isIncludeUpdatesInTransactionLog());
    
    CollectionDesignElement.WebRenderingSettings web = view.getWebRenderingSettings();
    assertFalse(web.isTreatAsHtml());
    assertFalse(web.isUseJavaApplet());
    assertFalse(web.isAllowSelection());
    assertColorEquals(web.getActiveLinkColor(), 255, 0, 0);
    assertColorEquals(web.getUnvisitedLinkColor(), 0, 0, 255);
    assertColorEquals(web.getVisitedLinkColor(), 128, 0, 128);
    assertFalse(web.isAllowWebCrawlerIndexing());
    assertFalse(view.isAllowDominoDataService());
    
    assertEquals("hello", view.getColumnProfileDocName().get());
    assertEquals(Collections.singleton("$9"), view.getUserDefinableNonFallbackColumns());

    final List<CollectionColumn> columns = view.getColumns();
    assertEquals(12, columns.size());
    {
      final CollectionColumn column = columns.get(0);
      assertEquals("Form", column.getTitle());
      assertEquals("Form", column.getItemName());
      assertEquals("", column.getFormula());
      assertFalse(column.isConstant());
      assertEquals(188, column.getDisplayWidth());
      assertEquals(ViewColumnFormat.ListDelimiter.SEMICOLON, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      assertTrue(column.isResizable());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertFalse(column.isUserEditable());
      assertFalse(column.isColor());
      assertFalse(column.isUserDefinableColor());
      assertFalse(column.isHideTitle());
      assertFalse(column.isHideDetailRows());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      assertTrue(sortConfig.isCategory());
      assertTrue(sortConfig.isSorted());
      assertTrue(sortConfig.isSortPermuted());
      assertFalse(sortConfig.getResortToViewUnid().isPresent());

      assertTrue(column.isShowTwistie());
      Optional<CDResource> twistie = column.getTwistieImage();
      assertTrue(twistie.isPresent());
      assertEquals("Untitled.gif", twistie.get().getNamedElement().get());
      assertFalse(twistie.get().getFlags().contains(CDResource.Flag.FORMULA));
      
      {
        NotesFont font = column.getRowFont();
        assertFalse(font.getStandardFont().isPresent());
        assertEquals("Courier New", font.getFontName().get());
        assertEquals(10, font.getPointSize());
        assertEquals(EnumSet.of(FontAttribute.UNDERLINE, FontAttribute.STRIKEOUT), font.getAttributes());
        assertEquals(StandardColors.LightMauve, font.getStandardColor().get());
      }
      {
        NotesFont font = column.getHeaderFont();
        assertFalse(font.getStandardFont().isPresent());
        assertEquals("Georgia", font.getFontName().get());
        assertEquals(9, font.getPointSize());
        assertEquals(EnumSet.of(FontAttribute.UNDERLINE, FontAttribute.BOLD, FontAttribute.ITALIC), font.getAttributes());
        assertEquals(StandardColors.Green, font.getStandardColor().get());
      }
      
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.DECIMAL, numbers.getFormat());
        assertTrue(numbers.isVaryingDecimal());
        assertFalse(numbers.isOverrideClientLocale());
        assertFalse(numbers.isUseParenthesesWhenNegative());
        assertFalse(numbers.isPunctuateThousands());
      }
      
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertFalse(dateTime.isOverrideClientLocale());
        assertFalse(dateTime.isDisplayAbbreviatedDate());
        
        assertTrue(dateTime.isDisplayDate());
        assertEquals(DateShowFormat.MDY, dateTime.getDateShowFormat());
        assertEquals(EnumSet.of(DateShowSpecial.SHOW_21ST_4DIGIT), dateTime.getDateShowBehavior());
        assertEquals(CalendarType.GREGORIAN, dateTime.getCalendarType());
        
        assertTrue(dateTime.isDisplayTime());
        assertEquals(TimeShowFormat.HMS, dateTime.getTimeShowFormat());
        assertEquals(TimeZoneFormat.NEVER, dateTime.getTimeZoneFormat());
      }
      
      // No values are specified for this column, so it should use the defaults
      {
        CollectionColumn.NamesSettings names = column.getNamesSettings();
        assertFalse(names.isNamesValue());
        assertFalse(names.isShowOnlineStatus());
        assertFalse(names.getNameColumnName().isPresent());
      }
      
      assertFalse(column.isHidden());
      assertFalse(column.isHiddenFromMobile());
      assertEquals("", column.getHideWhenFormula());
      assertFalse(column.isHiddenInPreV6());
      assertFalse(column.isExtendToWindowWidth());
      assertEquals("", column.getExtraAttributes());
      assertTrue(column.isShowAsLinks());
      
      {
        CollectionColumn.CompositeApplicationSettings compApp = column.getCompositeApplicationSettings();
        assertEquals(NarrowViewPosition.KEEP_ON_TOP, compApp.getNarrowViewPosition());
        assertTrue(compApp.isJustifySecondRow());
        assertEquals(TileViewerPosition.TOP, compApp.getTileViewerPosition());
        assertEquals(1, compApp.getTileLineNumber());
        assertEquals("testprop", compApp.getCompositeProperty());
      }
    }
    {
      final CollectionColumn column = columns.get(1);
      assertEquals("Size", column.getTitle());
      assertEquals("$2", column.getItemName());
      assertEquals("@AttachmentLengths", column.getFormula());
      assertFalse(column.isConstant());
      assertEquals(ViewColumnFormat.ListDelimiter.SPACE, column.getListDisplayDelimiter());
      assertEquals(TotalType.Total, column.getTotalType());
      assertTrue(column.isResizable());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertFalse(column.isUserEditable());
      assertFalse(column.isColor());
      assertFalse(column.isUserDefinableColor());
      assertFalse(column.isHideTitle());
      assertTrue(column.isHideDetailRows());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      assertFalse(sortConfig.isCategory());
      assertFalse(sortConfig.isSorted());
      assertFalse(sortConfig.isSortPermuted());
      assertTrue(sortConfig.isResortToView());
      assertEquals("F7FAC064F4062A4885257BBE006FA09B", sortConfig.getResortToViewUnid().get());

      assertFalse(column.isShowTwistie());
      Optional<CDResource> twistie = column.getTwistieImage();
      assertFalse(twistie.isPresent());
      
      {
        NotesFont font = column.getRowFont();
        assertEquals(StandardFonts.SWISS, font.getStandardFont().get());
        assertFalse(font.getFontName().isPresent());
        assertEquals(10, font.getPointSize());
        assertEquals(EnumSet.noneOf(FontAttribute.class), font.getAttributes());
        assertEquals(StandardColors.Black, font.getStandardColor().get());
      }
      {
        NotesFont font = column.getHeaderFont();
        assertEquals(StandardFonts.SWISS, font.getStandardFont().get());
        assertFalse(font.getFontName().isPresent());
        assertEquals(9, font.getPointSize());
        assertEquals(EnumSet.of(FontAttribute.BOLD), font.getAttributes());
        assertEquals(StandardColors.AtlanticGray, font.getStandardColor().get());
      }
      
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.BYTES, numbers.getFormat());
        assertFalse(numbers.isOverrideClientLocale());
        assertFalse(numbers.isUseParenthesesWhenNegative());
        assertFalse(numbers.isPunctuateThousands());
      }
      
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertFalse(dateTime.isOverrideClientLocale());
        assertFalse(dateTime.isDisplayAbbreviatedDate());
        
        assertFalse(dateTime.isDisplayDate());
        
        assertTrue(dateTime.isDisplayTime());
        assertEquals(TimeShowFormat.HM, dateTime.getTimeShowFormat());
        assertEquals(TimeZoneFormat.ALWAYS, dateTime.getTimeZoneFormat());
      }
      
      assertFalse(column.isHidden());
      assertTrue(column.isHiddenFromMobile());
      assertEquals("", column.getHideWhenFormula());
      assertFalse(column.isHiddenInPreV6());
      assertFalse(column.isExtendToWindowWidth());
      assertEquals("", column.getExtraAttributes());
      assertFalse(column.isShowAsLinks());
      
      {
        CollectionColumn.CompositeApplicationSettings compApp = column.getCompositeApplicationSettings();
        assertEquals(NarrowViewPosition.WRAP, compApp.getNarrowViewPosition());
        assertEquals(3, compApp.getSequenceNumber());
        assertEquals(TileViewerPosition.BOTTOM, compApp.getTileViewerPosition());
        assertEquals(1, compApp.getTileLineNumber());
        assertEquals("", compApp.getCompositeProperty());
      }
    }
    {
      final CollectionColumn column = columns.get(2);
      assertEquals("Created", column.getTitle());
      assertEquals("$3", column.getItemName());
      assertEquals("@Created", column.getFormula());
      assertFalse(column.isConstant());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.Average, column.getTotalType());
      assertFalse(column.isResizable());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertTrue(column.isUserEditable());
      assertFalse(column.isColor());
      assertTrue(column.isUserDefinableColor());
      assertFalse(column.isHideTitle());

      final CollectionColumn.SortConfiguration sortConfig = column.getSortConfiguration();
      assertFalse(sortConfig.isCategory());
      assertFalse(sortConfig.isSorted());
      assertFalse(sortConfig.isSortPermuted());
      assertFalse(sortConfig.isResortToView());
      assertTrue(sortConfig.isResortAscending() && sortConfig.isResortDescending());
      assertTrue(sortConfig.isDeferResortIndexing());
      assertFalse(sortConfig.getResortToViewUnid().isPresent());
      
      NotesFont font = column.getRowFont();
      assertFalse(font.getStandardFont().isPresent());
      assertEquals("Consolas", font.getFontName().get());
      assertEquals(14, font.getPointSize());
      assertEquals(EnumSet.of(FontAttribute.UNDERLINE, FontAttribute.STRIKEOUT), font.getAttributes());
      assertEquals(StandardColors.Cyan, font.getStandardColor().get());
      
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.SCIENTIFIC, numbers.getFormat());
        assertEquals(3, numbers.getFixedDecimalPlaces());
        assertTrue(numbers.isOverrideClientLocale());
        assertEquals("-", numbers.getDecimalSymbol());
        assertEquals("%", numbers.getThousandsSeparator());
        assertTrue(numbers.isUseParenthesesWhenNegative());
        assertFalse(numbers.isPunctuateThousands());
      }
      
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertFalse(dateTime.isOverrideClientLocale());
        assertFalse(dateTime.isDisplayAbbreviatedDate());
        
        assertTrue(dateTime.isDisplayDate());
        assertEquals(DateShowFormat.Y, dateTime.getDateShowFormat());
        assertEquals(EnumSet.of(DateShowSpecial.CURYR, DateShowSpecial.TODAY, DateShowSpecial.Y4), dateTime.getDateShowBehavior());
        assertEquals(CalendarType.GREGORIAN, dateTime.getCalendarType());
        
        assertFalse(dateTime.isDisplayTime());
      }
      
      {
        CollectionColumn.NamesSettings names = column.getNamesSettings();
        assertFalse(names.isNamesValue());
        assertTrue(names.isShowOnlineStatus());
        assertFalse(names.getNameColumnName().isPresent());
        assertEquals(CollectionColumn.OnlinePresenceOrientation.TOP, names.getPresenceIconOrientation());
      }
      
      assertFalse(column.isHidden());
      assertFalse(column.isHiddenFromMobile());
      assertEquals("\"hey\" = \"there\"", column.getHideWhenFormula());
      assertTrue(column.isHiddenInPreV6());
      assertFalse(column.isExtendToWindowWidth());
      assertEquals("foo=\"barss\"", column.getExtraAttributes());
      assertTrue(column.isShowAsLinks());
      
      {
        CollectionColumn.CompositeApplicationSettings compApp = column.getCompositeApplicationSettings();
        assertEquals(NarrowViewPosition.HIDE, compApp.getNarrowViewPosition());
        assertEquals(5, compApp.getSequenceNumber());
        assertEquals(TileViewerPosition.HIDE, compApp.getTileViewerPosition());
        assertEquals(3, compApp.getTileLineNumber());
        assertEquals("", compApp.getCompositeProperty());
      }
    }
    {
      final CollectionColumn column = columns.get(3);
      assertEquals("Modified", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.COMMA, column.getListDisplayDelimiter());
      assertEquals(TotalType.AveragePerSubcategory, column.getTotalType());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertFalse(column.isColor());
      assertFalse(column.isHideTitle());
      
      NotesFont font = column.getRowFont();
      assertEquals(StandardFonts.UNICODE, font.getStandardFont().get());
      assertFalse(font.getFontName().isPresent());
      assertEquals(11, font.getPointSize());
      assertEquals(EnumSet.of(FontAttribute.ITALIC), font.getAttributes());
      
//      assertColorEquals(column.getRowFontColor(), 224, 0, 224);
//      assertColorEquals(column.getHeaderFontColor(), 255, 255, 0);
      
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.PERCENT, numbers.getFormat());
        assertTrue(numbers.isVaryingDecimal());
        assertFalse(numbers.isOverrideClientLocale());
        assertFalse(numbers.isUseParenthesesWhenNegative());
        assertTrue(numbers.isPunctuateThousands());
      }
      
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertTrue(dateTime.isOverrideClientLocale());
        assertFalse(dateTime.isDisplayAbbreviatedDate());
        
        assertTrue(dateTime.isDisplayDate());
        assertEquals(DateShowFormat.W, dateTime.getDateShowFormat());
        assertEquals(EnumSet.of(DateShowSpecial.Y4, DateShowSpecial.SHOW_21ST_4DIGIT), dateTime.getDateShowBehavior());
        assertEquals(CalendarType.GREGORIAN, dateTime.getCalendarType());
        assertEquals(DateComponentOrder.WMDY, dateTime.getDateComponentOrder());
        assertEquals(" ", dateTime.getCustomDateSeparator1());
        assertEquals("/", dateTime.getCustomDateSeparator2());
        assertEquals("-", dateTime.getCustomDateSeparator3());
        assertEquals(DayFormat.D, dateTime.getDayFormat());
        assertEquals(MonthFormat.MMMM, dateTime.getMonthFormat());
        assertEquals(YearFormat.YY, dateTime.getYearFormat());
        assertEquals(WeekFormat.WWWWP, dateTime.getWeekdayFormat());
        
        assertTrue(dateTime.isDisplayTime());
        assertEquals(TimeShowFormat.HM, dateTime.getTimeShowFormat());
        assertEquals(TimeZoneFormat.SOMETIMES, dateTime.getTimeZoneFormat());
        assertTrue(dateTime.isTime24HourFormat());
        assertEquals("_", dateTime.getCustomTimeSeparator());
      }
      
      assertFalse(column.isHidden());
      assertFalse(column.isHiddenFromMobile());
      assertEquals("", column.getHideWhenFormula());
      assertFalse(column.isHiddenInPreV6());
      assertTrue(column.isExtendToWindowWidth());
      assertEquals("", column.getExtraAttributes());
      assertFalse(column.isShowAsLinks());
    }
    {
      final CollectionColumn column = columns.get(4);
      assertEquals("Static Value!", column.getTitle());
      assertFalse(column.isUseHideWhen());
      assertEquals("SecretHideWhen", column.getHideWhenFormula());
      assertEquals(ViewColumnFormat.ListDelimiter.NEWLINE, column.getListDisplayDelimiter());
      assertEquals(TotalType.PercentOfParentCategory, column.getTotalType());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertTrue(column.isColor());
      assertTrue(column.isHideTitle());
      
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.CURRENCY, numbers.getFormat());
        assertEquals(0, numbers.getFixedDecimalPlaces());
        assertTrue(numbers.isOverrideClientLocale());
        assertEquals(".", numbers.getDecimalSymbol());
        assertEquals(",", numbers.getThousandsSeparator());
        assertFalse(numbers.isUseParenthesesWhenNegative());
        assertFalse(numbers.isPunctuateThousands());
        assertEquals("€", numbers.getCurrencySymbol());
        assertFalse(numbers.isUseCustomCurrencySymbol());
        assertFalse(numbers.isCurrencySymbolPostfix());
        assertFalse(numbers.isUseSpaceNextToNumber());
      }
      
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertTrue(dateTime.isDisplayAbbreviatedDate());
      }
    }
    {
      final CollectionColumn column = columns.get(5);
      assertEquals("#", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertTrue(column.isSharedColumn());
      assertEquals("testcol", column.getSharedColumnName().get());

      // This column does not have numbers settings specified, and should use the defaults
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.DECIMAL, numbers.getFormat());
        assertTrue(numbers.isVaryingDecimal());
        assertFalse(numbers.isOverrideClientLocale());
        assertFalse(numbers.isUseParenthesesWhenNegative());
        assertFalse(numbers.isPunctuateThousands());
      }
      
      // This column does not have date/time settings specified, and should use the defaults
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertFalse(dateTime.isOverrideClientLocale());
        assertFalse(dateTime.isDisplayAbbreviatedDate());
        
        assertTrue(dateTime.isDisplayDate());
        assertEquals(DateShowFormat.MDY, dateTime.getDateShowFormat());
        assertEquals(EnumSet.of(DateShowSpecial.SHOW_21ST_4DIGIT), dateTime.getDateShowBehavior());
        assertEquals(CalendarType.GREGORIAN, dateTime.getCalendarType());
        
        assertTrue(dateTime.isDisplayTime());
        assertEquals(TimeShowFormat.HMS, dateTime.getTimeShowFormat());
        assertEquals(TimeZoneFormat.NEVER, dateTime.getTimeZoneFormat());
      }
    }
    {
      final CollectionColumn column = columns.get(6);
      assertEquals("I am test col 2", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      assertFalse(column.isResponsesOnly());
      assertFalse(column.isIcon());
      assertTrue(column.isSharedColumn());
      assertEquals("testcol2", column.getSharedColumnName().get());
      
      assertFalse(column.isHidden());
      assertFalse(column.isHiddenFromMobile());
      assertEquals("", column.getHideWhenFormula());
      assertFalse(column.isHiddenInPreV6());
      assertFalse(column.isExtendToWindowWidth());
      assertEquals("", column.getExtraAttributes());
      assertFalse(column.isShowAsLinks());
    }
    {
      final CollectionColumn column = columns.get(7);
      assertEquals("Names Guy", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.Percent, column.getTotalType());
      assertTrue(column.isResponsesOnly());
      assertFalse(column.isIcon());

      assertTrue(column.isShowTwistie());
      Optional<CDResource> twistie = column.getTwistieImage();
      assertTrue(twistie.isPresent());
      assertEquals("tango/utilities-terminal.png", twistie.get().getNamedElement().get());
      
      {
        CollectionColumn.NumberSettings numbers = column.getNumberSettings();
        assertEquals(NumberDisplayFormat.CURRENCY, numbers.getFormat());
        assertEquals(0, numbers.getFixedDecimalPlaces());
        assertTrue(numbers.isOverrideClientLocale());
        assertEquals(".", numbers.getDecimalSymbol());
        assertEquals(",", numbers.getThousandsSeparator());
        assertFalse(numbers.isUseParenthesesWhenNegative());
        assertFalse(numbers.isPunctuateThousands());
        assertEquals("gg", numbers.getCurrencySymbol());
        assertTrue(numbers.isUseCustomCurrencySymbol());
        assertTrue(numbers.isCurrencySymbolPostfix());
        assertFalse(numbers.isUseSpaceNextToNumber());
      }
      
      {
        CollectionColumn.DateTimeSettings dateTime = column.getDateTimeSettings();
        assertTrue(dateTime.isOverrideClientLocale());
        assertFalse(dateTime.isDisplayAbbreviatedDate());
        
        assertTrue(dateTime.isDisplayDate());
        assertEquals(DateShowFormat.MDY, dateTime.getDateShowFormat());
        assertEquals(CalendarType.HIJRI, dateTime.getCalendarType());
        assertEquals(DateComponentOrder.WDMY, dateTime.getDateComponentOrder());
        assertEquals(" ", dateTime.getCustomDateSeparator1());
        assertEquals("/", dateTime.getCustomDateSeparator2());
        assertEquals("/", dateTime.getCustomDateSeparator3());
        assertEquals(DayFormat.DD, dateTime.getDayFormat());
        assertEquals(MonthFormat.MM, dateTime.getMonthFormat());
        assertEquals(WeekFormat.WWW, dateTime.getWeekdayFormat());
        
        assertTrue(dateTime.isDisplayTime());
        assertEquals(TimeShowFormat.HMS, dateTime.getTimeShowFormat());
        assertEquals(TimeZoneFormat.NEVER, dateTime.getTimeZoneFormat());
        assertFalse(dateTime.isTime24HourFormat());
        assertEquals(":", dateTime.getCustomTimeSeparator());
      }
      
      {
        CollectionColumn.NamesSettings names = column.getNamesSettings();
        assertTrue(names.isNamesValue());
        assertTrue(names.isShowOnlineStatus());
        assertEquals("foobarnamesfield", names.getNameColumnName().get());
        assertEquals(CollectionColumn.OnlinePresenceOrientation.MIDDLE, names.getPresenceIconOrientation());
      }
      
      assertFalse(column.isHidden());
      assertFalse(column.isHiddenFromMobile());
      assertEquals("@ClientType=\"Notes\"", column.getHideWhenFormula());
      assertFalse(column.isHiddenInPreV6());
      assertFalse(column.isExtendToWindowWidth());
      assertEquals("names=\"this\"", column.getExtraAttributes());
      assertFalse(column.isShowAsLinks());
    }
    {
      final CollectionColumn column = columns.get(8);
      assertEquals("Names Guy 2", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      assertFalse(column.isIcon());

      // Not enabled, but present
      assertFalse(column.isShowTwistie());
      Optional<CDResource> twistie = column.getTwistieImage();
      assertTrue(twistie.isPresent());
      assertEquals("Untitled 2.gif", twistie.get().getNamedElement().get());
      
      {
        CollectionColumn.NamesSettings names = column.getNamesSettings();
        assertFalse(names.isNamesValue());
        assertTrue(names.isShowOnlineStatus());
        assertEquals("othernamesfield", names.getNameColumnName().get());
        assertEquals(CollectionColumn.OnlinePresenceOrientation.BOTTOM, names.getPresenceIconOrientation());
      }
    }
    {
      final CollectionColumn column = columns.get(9);
      assertEquals("I am test col 2", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      assertFalse(column.isIcon());
      assertTrue(column.isSharedColumn());
      assertEquals("testcol2", column.getSharedColumnName().get());
    }
    {
      final CollectionColumn column = columns.get(10);
      assertEquals("Hidden Guy", column.getTitle());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      assertTrue(column.isIcon());
      
      assertTrue(column.isHidden());
      assertFalse(column.isHiddenFromMobile());
      assertEquals("", column.getHideWhenFormula());
      assertFalse(column.isHiddenInPreV6());
      assertFalse(column.isExtendToWindowWidth());
      assertEquals("", column.getExtraAttributes());
      assertFalse(column.isShowAsLinks());
    }
    {
      final CollectionColumn column = columns.get(11);
      assertEquals("Column of constant value", column.getTitle());
      assertTrue(column.isConstant());
      assertEquals("\"hello\"", column.getFormula());
      assertEquals(ViewColumnFormat.ListDelimiter.NONE, column.getListDisplayDelimiter());
      assertEquals(TotalType.None, column.getTotalType());
      
      Optional<CDResource> twistie = column.getTwistieImage();
      assertTrue(twistie.isPresent());
      assertTrue(twistie.get().getFlags().contains(CDResource.Flag.FORMULA));
      assertEquals("\"foo.png\"", twistie.get().getNamedElementFormula().get());
    }

  }

  @Test
  public void testExampleView2() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 2").get();
    assertEquals("Example View 2", view.getTitle());
    assertEquals(Arrays.asList("test alias for view 2", "other alias"), view.getAliases());
    assertEquals("I am a comment'", view.getComment());
    assertFalse(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.DISPLAY_INDICATOR, view.getOnRefreshUISetting());
    assertEquals(ClassicThemeBehavior.DONT_INHERIT_FROM_OS, view.getClassicThemeBehavior());
    assertEquals(CollectionDesignElement.Style.STANDARD_OUTLINE, view.getStyle());
    assertFalse(view.isDefaultCollection());
    assertTrue(view.isDefaultCollectionDesign());
    assertTrue(view.isCollapseAllOnFirstOpen());
    assertTrue(view.isShowResponseDocumentsInHierarchy());
    assertFalse(view.isShowInViewMenu());
    assertFalse(view.isEvaluateActionsOnDocumentChange());
    assertTrue(view.isCreateDocumentsAtViewLevel());
    assertFalse(view.getWebXPageAlternative().isPresent());
    assertTrue(view.isAllowPublicAccess());
    assertFalse(view.getReaders().isPresent());
    assertFalse(view.getColumnProfileDocName().isPresent());
    assertTrue(view.getUserDefinableNonFallbackColumns().isEmpty());
    // This view uses Simple Searched, but this formula is stored as residual
    assertEquals("SELECT ((Form = \"Alias\") | (Form = \"Content\") | (Form = \"foo\") | (Form = \"bar\"))", view.getSelectionFormula());
    
    CollectionDesignElement.CompositeAppSettings comp = view.getCompositeAppSettings();
    assertNotNull(comp);
    assertFalse(comp.isHideColumnHeader());
    assertFalse(comp.isShowPartialHierarchies());
    assertTrue(comp.isShowSwitcher());
    assertFalse(comp.isShowTabNavigator());
    assertFalse(comp.isAllowConversationMode());
    
    DisplaySettings disp = view.getDisplaySettings();
    assertNotNull(disp);
    
    ColorValue background = disp.getBackgroundColor();
    assertEquals(0, background.getRed());
    assertEquals(255, background.getGreen());
    assertEquals(255, background.getBlue());

    assertTrue(disp.isUseAlternateRowColor());
    ColorValue altBackground = disp.getAlternateRowColor();
    assertEquals(192, altBackground.getRed());
    assertEquals(192, altBackground.getGreen());
    assertEquals(192, altBackground.getBlue());
    
    assertEquals(ImageRepeatMode.ONCE, disp.getBackgroundImageRepeatMode());
    
    assertEquals(CollectionDesignElement.GridStyle.DOTS, disp.getGridStyle());
    ColorValue gridColor = disp.getGridColor();
    assertEquals(191, gridColor.getRed());
    assertEquals(191, gridColor.getGreen());
    assertEquals(255, gridColor.getBlue());

    assertEquals(CollectionDesignElement.HeaderStyle.FLAT, disp.getHeaderStyle());
    assertEquals(1, disp.getHeaderLines());
    ColorValue headerColor = disp.getHeaderColor();
    assertEquals(255, headerColor.getRed());
    assertEquals(159, headerColor.getGreen());
    assertEquals(255, headerColor.getBlue());
    
    assertEquals(1, disp.getRowLines());
    assertEquals(ViewLineSpacing.SINGLE_SPACE, disp.getLineSpacing());
    assertFalse(disp.isShrinkRowsToContent());
    assertFalse(disp.isHideEmptyCategories());
    assertTrue(disp.isColorizeViewIcons());
    
    ColorValue unreadColor = disp.getUnreadColor();
    assertEquals(0, unreadColor.getRed());
    assertEquals(0, unreadColor.getGreen());
    assertEquals(255, unreadColor.getBlue());
    assertFalse(unreadColor.getFlags().contains(ColorValue.Flag.NOCOLOR));
    
    assertTrue(disp.isUnreadBold());
    
    ColorValue totalColor = disp.getColumnTotalColor();
    assertEquals(255, totalColor.getRed());
    assertEquals(0, totalColor.getGreen());
    assertEquals(128, totalColor.getBlue());
    
    assertTrue(disp.isShowSelectionMargin());
    assertFalse(disp.isHideSelectionMarginBorder());
    assertTrue(disp.isExtendLastColumnToWindowWidth());
    
    EdgeWidths margin = disp.getMargin();
    assertEquals(2, margin.getTop());
    assertEquals(2, margin.getLeft());
    assertEquals(10, margin.getRight());
    assertEquals(5, margin.getBottom());
    assertEquals(2, disp.getBelowHeaderMargin());
    
    ColorValue marginColor = disp.getMarginColor();
    assertEquals(130, marginColor.getRed());
    assertEquals(66, marginColor.getGreen());
    assertEquals(255, marginColor.getBlue());

    
    assertTrue(view.getAutoFrameFrameset().isPresent());
    assertEquals("Outer Frame", view.getAutoFrameFrameset().get());
    assertTrue(view.getAutoFrameTarget().isPresent());
    assertEquals("NotesView", view.getAutoFrameTarget().get());
    
    assertEquals(CollectionDesignElement.UnreadMarksMode.DOCUMENTS_ONLY, view.getUnreadMarksMode());
    
    CollectionDesignElement.IndexSettings index = view.getIndexSettings();
    assertEquals(CollectionDesignElement.IndexRefreshMode.AUTO_AFTER_FIRST_USE, index.getRefreshMode());
    assertFalse(index.getRefreshMaxIntervalSeconds().isPresent());
    assertEquals(CollectionDesignElement.IndexDiscardMode.INACTIVE_45_DAYS, index.getDiscardMode());
    assertFalse(index.getDiscardAfterHours().isPresent());
    assertFalse(index.isRestrictInitialBuildToDesigner());
    assertTrue(index.isGenerateUniqueKeysInIndex());
    assertFalse(index.isIncludeUpdatesInTransactionLog());
    
    CollectionDesignElement.WebRenderingSettings web = view.getWebRenderingSettings();
    assertFalse(web.isTreatAsHtml());
    assertFalse(web.isUseJavaApplet());
    assertTrue(web.isAllowSelection());
    assertColorEquals(web.getActiveLinkColor(), 0, 98, 225);
    assertColorEquals(web.getUnvisitedLinkColor(), 255, 64, 64);
    assertColorEquals(web.getVisitedLinkColor(), 255, 159, 255);
    assertFalse(web.isAllowWebCrawlerIndexing());
    assertTrue(view.isAllowDominoDataService());
    
    ActionBar actionBar = view.getActionBar();
    assertNotNull(actionBar);
    assertEquals(0, actionBar.getActions().size());
  }

  @Test
  public void testExampleView3() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 3").get();
    assertFalse(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_BOTTOM, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_TOP, view.getOnRefreshUISetting());
    assertEquals(ClassicThemeBehavior.INHERIT_FROM_OS, view.getClassicThemeBehavior());
    assertFalse(view.isDefaultCollection());
    assertFalse(view.isDefaultCollectionDesign());
    assertFalse(view.isCollapseAllOnFirstOpen());
    assertFalse(view.isShowResponseDocumentsInHierarchy());
    assertFalse(view.isShowInViewMenu());
    assertFalse(view.isEvaluateActionsOnDocumentChange());
    assertFalse(view.getWebXPageAlternative().isPresent());
    assertFalse(view.isAllowPublicAccess());
    assertFalse(view.getReaders().isPresent());
    assertFalse(view.getColumnProfileDocName().isPresent());
    assertTrue(view.getUserDefinableNonFallbackColumns().isEmpty());
    assertEquals("SELECT @All", view.getSelectionFormula());
    
    DisplaySettings disp = view.getDisplaySettings();
    assertNotNull(disp);
    
    ColorValue background = disp.getBackgroundColor();
    assertEquals(255, background.getRed());
    assertEquals(255, background.getGreen());
    assertEquals(255, background.getBlue());

    assertTrue(disp.isUseAlternateRowColor());
    ColorValue altBackground = disp.getAlternateRowColor();
    assertEquals(239, altBackground.getRed());
    assertEquals(239, altBackground.getGreen());
    assertEquals(239, altBackground.getBlue());
    
    Optional<CDResource> backgroundImage = disp.getBackgroundImage();
    assertFalse(backgroundImage.isPresent());
    assertEquals(ImageRepeatMode.ONCE, disp.getBackgroundImageRepeatMode());
    
    assertEquals(CollectionDesignElement.GridStyle.SOLID, disp.getGridStyle());
    ColorValue gridColor = disp.getGridColor();
    assertEquals(255, gridColor.getRed());
    assertEquals(95, gridColor.getGreen());
    assertEquals(255, gridColor.getBlue());

    assertEquals(CollectionDesignElement.HeaderStyle.SIMPLE, disp.getHeaderStyle());
    assertEquals(4, disp.getHeaderLines());
    ColorValue headerColor = disp.getHeaderColor();
    assertEquals(225, headerColor.getRed());
    assertEquals(225, headerColor.getGreen());
    assertEquals(64, headerColor.getBlue());
    
    assertEquals(1, disp.getRowLines());
    assertEquals(ViewLineSpacing.SINGLE_SPACE, disp.getLineSpacing());
    assertFalse(disp.isShrinkRowsToContent());
    assertFalse(disp.isHideEmptyCategories());
    assertFalse(disp.isColorizeViewIcons());
    
    ColorValue unreadColor = disp.getUnreadColor();
    assertEquals(255, unreadColor.getRed());
    assertEquals(0, unreadColor.getGreen());
    assertEquals(0, unreadColor.getBlue());
    assertTrue(unreadColor.getFlags().contains(ColorValue.Flag.NOCOLOR));
    
    assertTrue(disp.isUnreadBold());
    
    ColorValue totalColor = disp.getColumnTotalColor();
    assertEquals(0, totalColor.getRed());
    assertEquals(0, totalColor.getGreen());
    assertEquals(0, totalColor.getBlue());
    
    assertTrue(disp.isShowSelectionMargin());
    assertTrue(disp.isHideSelectionMarginBorder());
    assertTrue(disp.isExtendLastColumnToWindowWidth());
    
    EdgeWidths margin = disp.getMargin();
    assertEquals(0, margin.getTop());
    assertEquals(0, margin.getLeft());
    assertEquals(0, margin.getRight());
    assertEquals(0, margin.getBottom());
    assertEquals(0, disp.getBelowHeaderMargin());
    
    ColorValue marginColor = disp.getMarginColor();
    assertEquals(255, marginColor.getRed());
    assertEquals(255, marginColor.getGreen());
    assertEquals(255, marginColor.getBlue());

    
    assertFalse(view.getAutoFrameFrameset().isPresent());
    assertFalse(view.getAutoFrameTarget().isPresent());
    
    assertEquals(CollectionDesignElement.UnreadMarksMode.ALL, view.getUnreadMarksMode());
    
    CollectionDesignElement.IndexSettings index = view.getIndexSettings();
    assertEquals(CollectionDesignElement.IndexRefreshMode.AUTO, index.getRefreshMode());
    assertFalse(index.getRefreshMaxIntervalSeconds().isPresent());
    assertEquals(CollectionDesignElement.IndexDiscardMode.INACTIVE_FOR, index.getDiscardMode());
    assertEquals(TimeUnit.DAYS.toHours(19), index.getDiscardAfterHours().getAsInt());
    assertTrue(index.isRestrictInitialBuildToDesigner());
    assertFalse(index.isGenerateUniqueKeysInIndex());
    assertFalse(index.isIncludeUpdatesInTransactionLog());
    
    CollectionDesignElement.WebRenderingSettings web = view.getWebRenderingSettings();
    assertTrue(web.isTreatAsHtml());
    assertFalse(web.isUseJavaApplet());
    assertFalse(web.isAllowSelection());
    assertColorEquals(web.getActiveLinkColor(), 255, 0, 0);
    assertColorEquals(web.getUnvisitedLinkColor(), 0, 0, 255);
    assertColorEquals(web.getVisitedLinkColor(), 128, 0, 128);
    assertFalse(web.isAllowWebCrawlerIndexing());
    assertFalse(view.isAllowDominoDataService());
  }

  @Test
  public void testExampleView4() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("Example View 4").get();
    assertFalse(view.isAllowCustomizations());
    assertEquals(CollectionDesignElement.OnOpen.GOTO_LAST_OPENED, view.getOnOpenUISetting());
    assertEquals(CollectionDesignElement.OnRefresh.REFRESH_FROM_BOTTOM, view.getOnRefreshUISetting());
    assertFalse(view.isDefaultCollection());
    assertFalse(view.isDefaultCollectionDesign());
    assertTrue(view.isShowInViewMenu());
    assertFalse(view.isEvaluateActionsOnDocumentChange());
    assertFalse(view.getWebXPageAlternative().isPresent());
    assertFalse(view.isAllowPublicAccess());
    assertFalse(view.getReaders().isPresent());
    assertFalse(view.getColumnProfileDocName().isPresent());
    assertTrue(view.getUserDefinableNonFallbackColumns().isEmpty());
    assertEquals("SELECT @All", view.getSelectionFormula());
    
    assertEquals("1", view.getFormulaClass());
    view.setFormulaClass("2");
    assertEquals("2", view.getFormulaClass());
    
    CollectionDesignElement.CompositeAppSettings comp = view.getCompositeAppSettings();
    assertNotNull(comp);
    assertTrue(comp.isHideColumnHeader());
    assertTrue(comp.isShowPartialHierarchies());
    assertFalse(comp.isShowSwitcher());
    assertTrue(comp.isShowTabNavigator());
    assertEquals("foo,bar", comp.getViewers());
    assertEquals("test alias", comp.getThreadView());
    assertTrue(comp.isAllowConversationMode());
    
    DisplaySettings disp = view.getDisplaySettings();
    assertNotNull(disp);
    
    ColorValue background = disp.getBackgroundColor();
    assertEquals(255, background.getRed());
    assertEquals(255, background.getGreen());
    assertEquals(255, background.getBlue());

    assertTrue(disp.isUseAlternateRowColor());
    ColorValue altBackground = disp.getAlternateRowColor();
    assertEquals(239, altBackground.getRed());
    assertEquals(239, altBackground.getGreen());
    assertEquals(239, altBackground.getBlue());
    
    Optional<CDResource> backgroundImage = disp.getBackgroundImage();
    assertEquals("Untitled.gif", backgroundImage.get().getNamedElement().get());
    assertEquals(ImageRepeatMode.HORIZONTAL, disp.getBackgroundImageRepeatMode());
    
    assertEquals(CollectionDesignElement.GridStyle.NONE, disp.getGridStyle());
    ColorValue gridColor = disp.getGridColor();
    assertEquals(255, gridColor.getRed());
    assertEquals(255, gridColor.getGreen());
    assertEquals(255, gridColor.getBlue());

    assertEquals(CollectionDesignElement.HeaderStyle.NONE, disp.getHeaderStyle());
    assertEquals(1, disp.getHeaderLines());
    ColorValue headerColor = disp.getHeaderColor();
    assertEquals(255, headerColor.getRed());
    assertEquals(255, headerColor.getGreen());
    assertEquals(255, headerColor.getBlue());
    
    assertEquals(6, disp.getRowLines());
    assertEquals(ViewLineSpacing.ONE_POINT_75_SPACE, disp.getLineSpacing());
    assertFalse(disp.isShrinkRowsToContent());
    assertFalse(disp.isHideEmptyCategories());
    assertFalse(disp.isColorizeViewIcons());
    
    ColorValue unreadColor = disp.getUnreadColor();
    assertEquals(255, unreadColor.getRed());
    assertEquals(0, unreadColor.getGreen());
    assertEquals(0, unreadColor.getBlue());
    assertFalse(unreadColor.getFlags().contains(ColorValue.Flag.NOCOLOR));
    
    assertFalse(disp.isUnreadBold());
    
    ColorValue totalColor = disp.getColumnTotalColor();
    assertEquals(0, totalColor.getRed());
    assertEquals(0, totalColor.getGreen());
    assertEquals(0, totalColor.getBlue());
    
    assertFalse(disp.isShowSelectionMargin());
    assertFalse(disp.isHideSelectionMarginBorder());
    assertFalse(disp.isExtendLastColumnToWindowWidth());
    
    EdgeWidths margin = disp.getMargin();
    assertEquals(0, margin.getTop());
    assertEquals(0, margin.getLeft());
    assertEquals(0, margin.getRight());
    assertEquals(0, margin.getBottom());
    assertEquals(0, disp.getBelowHeaderMargin());
    
    ColorValue marginColor = disp.getMarginColor();
    assertEquals(255, marginColor.getRed());
    assertEquals(255, marginColor.getGreen());
    assertEquals(255, marginColor.getBlue());

    
    assertFalse(view.getAutoFrameFrameset().isPresent());
    assertFalse(view.getAutoFrameTarget().isPresent());
    
    assertEquals(CollectionDesignElement.UnreadMarksMode.NONE, view.getUnreadMarksMode());
    
    CollectionDesignElement.IndexSettings index = view.getIndexSettings();
    assertEquals(CollectionDesignElement.IndexRefreshMode.MANUAL, index.getRefreshMode());
    assertFalse(index.getRefreshMaxIntervalSeconds().isPresent());
    assertEquals(CollectionDesignElement.IndexDiscardMode.AFTER_EACH_USE, index.getDiscardMode());
    assertFalse(index.getDiscardAfterHours().isPresent());
    assertFalse(index.isRestrictInitialBuildToDesigner());
    assertFalse(index.isGenerateUniqueKeysInIndex());
    assertTrue(index.isIncludeUpdatesInTransactionLog());
    
    CollectionDesignElement.WebRenderingSettings web = view.getWebRenderingSettings();
    assertFalse(web.isTreatAsHtml());
    assertTrue(web.isUseJavaApplet());
    assertFalse(web.isAllowSelection());
    assertColorEquals(web.getActiveLinkColor(), 255, 0, 0);
    assertColorEquals(web.getUnvisitedLinkColor(), 0, 255, 0);
    assertColorEquals(web.getVisitedLinkColor(), 128, 0, 128);
    assertTrue(web.isAllowWebCrawlerIndexing());
    assertFalse(view.isAllowDominoDataService());
  }
  
  @Test
  public void testAllView() {
    final DbDesign dbDesign = this.database.getDesign();
    final View view = dbDesign.getView("All").get();
    assertFalse(view.isAllowCustomizations());
    assertTrue(view.isDefaultCollection());
  }

  @Test
  public void testFolders() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
    assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_FOLDERS, collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      assertNotNull(view);
      assertInstanceOf(Folder.class, view);
      assertEquals("test folder", view.getTitle());
    }
  }

  @Test
  public void testFoldersAndViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
    assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_VIEWS + TestDbDesignCollections.EXPECTED_IMPORT_FOLDERS + 1,
        collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      assertNotNull(view);
      assertInstanceOf(View.class, view);
      assertEquals("test view", view.getTitle());
    }
    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      assertNotNull(view);
      assertInstanceOf(Folder.class, view);
      assertEquals("test folder", view.getTitle());
    }
  }

  /**
   * Iterates over all views and folders in mail12.ntf (if present) to test for
   * exceptions in
   * view-format reading
   */
  @Test
  public void testIterateMailNtf() {
    final DominoClient client = this.getClient();
    Database database;
    try {
      database = client.openDatabase("mail12.ntf");
    } catch (final FileDoesNotExistException e) {
      // That's fine - not on 12, so skip the test
      return;
    }

    database.getDesign()
        .getCollections()
        .forEach(collection -> {
          final String title = collection.getTitle();
          try {
            collection.getColumns().forEach(col -> {
              @SuppressWarnings("unused")
              final String colName = col.getItemName();
            });
          } catch (final Throwable t) {
            throw new RuntimeException(MessageFormat.format("Encountered exception in view \"{0}\"", title), t);
          }
        });
  }

  @Test
  public void testViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getViews().collect(Collectors.toList());
    assertEquals(TestDbDesignCollections.EXPECTED_IMPORT_VIEWS + 1, collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      assertNotNull(view);
      assertInstanceOf(View.class, view);
      assertEquals("test view", view.getTitle());
      assertEquals("8.5.3", view.getDesignerVersion());

      {
        assertTrue(view.isProhibitRefresh());
        view.setProhibitRefresh(false);
        assertFalse(view.isProhibitRefresh());
        view.setProhibitRefresh(true);
        assertTrue(view.isProhibitRefresh());
      }

      {
        assertFalse(view.isHideFromWeb());
        view.setHideFromWeb(true);
        assertTrue(view.isHideFromWeb());
        view.setHideFromWeb(false);
        assertFalse(view.isHideFromWeb());
      }
      {
        assertFalse(view.isHideFromNotes());
        view.setHideFromNotes(true);
        assertTrue(view.isHideFromNotes());
        view.setHideFromNotes(false);
        assertFalse(view.isHideFromNotes());
      }
      {
        assertFalse(view.isHideFromMobile());
        view.setHideFromMobile(true);
        assertTrue(view.isHideFromMobile());
        view.setHideFromMobile(false);
        assertFalse(view.isHideFromMobile());
      }
    }
  }
  
  @Test
  public void testDeletedSharedColumnView() {
    DbDesign design = this.database.getDesign();
    View view = design.getView("Shared Column Deletion View").get();
    List<CollectionColumn> columns = view.getColumns();
    assertEquals(6, columns.size());
    {
      CollectionColumn col = columns.get(0);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(1);
      assertEquals("Real col", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertTrue(col.isNameColumn());
      assertTrue(col.getOnlinePresenceNameColumn().isPresent());
      assertEquals("SomeOnlineCol", col.getOnlinePresenceNameColumn().get());
    }
    {
      CollectionColumn col = columns.get(2);
      assertEquals("#", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(3);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(4);
      assertEquals("Real col 2", col.getTitle());
      assertEquals("@NoteID + \"I am real col 2\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(5);
      assertEquals("I am test col 2", col.getTitle());
      assertEquals("Foo", col.getItemName());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol2", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
  }

  @Test
  public void testDeletedSharedColumnView2() {
    DbDesign design = this.database.getDesign();
    View view = design.getView("Shared Column Deletion View 2").get();
    List<CollectionColumn> columns = view.getColumns();
    assertEquals(6, columns.size());
    {
      CollectionColumn col = columns.get(0);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(1);
      assertEquals("Real col", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(2);
      assertEquals("#", col.getTitle());
      assertEquals("@DocNumber", col.getFormula());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(3);
      assertEquals("Shared Col to Delete", col.getTitle());
      assertEquals("@NoteID + \"ghost column\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(4);
      assertEquals("Real col 2", col.getTitle());
      assertEquals("@NoteID + \"I am real col 2\"", col.getFormula());
      assertFalse(col.isSharedColumn());
      assertFalse(col.getSharedColumnName().isPresent());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
    {
      CollectionColumn col = columns.get(5);
      assertEquals("I am test col 2", col.getTitle());
      assertEquals("Foo", col.getItemName());
      assertTrue(col.isSharedColumn());
      assertTrue(col.getSharedColumnName().isPresent());
      assertEquals("testcol2", col.getSharedColumnName().get());
      assertFalse(col.isNameColumn());
      assertFalse(col.getOnlinePresenceNameColumn().isPresent());
    }
  }
  
  @Test
  public void testMail12NtfByCategory() {
    final DominoClient client = this.getClient();
    Database database;
    try {
      database = client.openDatabase("mail12.ntf");
    } catch (final FileDoesNotExistException e) {
      // That's fine - not on 12, so skip the test
      return;
    }
    
    DbDesign design = database.getDesign();
    View byCategory = design.getView("$ByCategory").get();
    byCategory.getColumns().forEach(col -> {
      
    });
  }
  
  @Test
  public void testActionView() throws IOException {
    DbDesign design = this.database.getDesign();
    View view = design.getView("Action View").get();
    
    ActionBar actions = view.getActionBar();
    
    assertEquals(ActionBar.Alignment.RIGHT, actions.getAlignment());
    assertTrue(actions.isUseJavaApplet());
    assertTrue(actions.isShowDefaultItemsInContextMenu());
    
    assertEquals(ActionButtonHeightMode.EXS, actions.getHeightMode());
    assertEquals(5.167, actions.getHeightSpec());
    {
      NotesFont heightFont = actions.getHeightSizingFont();
      assertFalse(heightFont.getStandardFont().isPresent());
      assertEquals("Calibri", heightFont.getFontName().get());
      assertEquals(14, heightFont.getPointSize());
      assertEquals(EnumSet.of(FontAttribute.ITALIC), heightFont.getAttributes());
    }
    
    assertColorEquals(actions.getBackgroundColor(), 255, 159, 255);
    {
      CDResource background = actions.getBackgroundImage().get();
      assertEquals("Untitled.gif", background.getNamedElement().get());
    }
    assertEquals(ActionBarBackgroundRepeat.CENTER_TILE, actions.getBackgroundImageRepeatMode());
    assertEquals(ClassicThemeBehavior.DONT_INHERIT_FROM_OS, actions.getClassicThemeBehavior());
    
    assertEquals(BorderStyle.DOUBLE, actions.getBorderStyle());
    assertColorEquals(actions.getBorderColor(), 97, 129, 255);
    assertTrue(actions.isUseDropShadow());
    assertEquals(4, actions.getDropShadowWidth());
    {
      EdgeWidths inside = actions.getInsideMargins();
      assertEquals(5, inside.getTop());
      assertEquals(6, inside.getLeft());
      assertEquals(2, inside.getRight());
      assertEquals(24, inside.getBottom());
    }
    {
      EdgeWidths thickness = actions.getBorderWidths();
      assertEquals(1, thickness.getTop());
      assertEquals(2, thickness.getLeft());
      assertEquals(3, thickness.getRight());
      assertEquals(4, thickness.getBottom());
    }
    {
      EdgeWidths outside = actions.getOutsideMargins();
      assertEquals(5, outside.getTop());
      assertEquals(6, outside.getLeft());
      assertEquals(7, outside.getRight());
      assertEquals(8, outside.getBottom());
    }
    
    assertEquals(ButtonHeightMode.FIXED_SIZE, actions.getButtonHeightMode());
    assertEquals(17, actions.getButtonHeightSpec());
    assertEquals(ActionWidthMode.BACKGROUND, actions.getButtonWidthMode());
    assertTrue(actions.isFixedSizeButtonMargin());
    assertEquals(6, actions.getButtonVerticalMarginSize());
    assertEquals(ButtonBorderDisplay.ONMOUSEOVER, actions.getButtonBorderMode());
    assertEquals(ActionBarTextAlignment.CENTER, actions.getButtonTextAlignment());
    assertEquals(2, actions.getButtonInternalMarginSize());
    assertTrue(actions.isAlwaysShowDropDowns());
    
    assertColorEquals(actions.getButtonBackgroundColor(), 255, 129, 0);
    {
      CDResource buttonBackground = actions.getButtonBackgroundImage().get();
      assertEquals("Untitled 2.gif", buttonBackground.getNamedElement().get());
    }
    
    {
      NotesFont font = actions.getFont();
      assertEquals("Courier New", font.getFontName().get());
      assertEquals(18, font.getPointSize());
      assertEquals(EnumSet.of(FontAttribute.UNDERLINE), font.getAttributes());
    }
    assertColorEquals(actions.getFontColor(), 0, 0, 255);
    
    
    
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
        Collection<ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONCLICK) {
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
            if(event.getEventId() == EventId.ONCLICK) {
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
            if(event.getEventId() == EventId.ONMOUSEDOWN) {
              if("console.log(\"is there a console in Notes JS actions?\")\n".equals(event.getScript())) {
                return true;
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONMOUSEOVER) {
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
        Collection<ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONCLICK) {
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
            if(event.getEventId() == EventId.ONCLICK) {
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
            if(event.getEventId() == EventId.ONMOUSEDOWN) {
              if("console.log(\"is there a console in Notes JS actions?\")\n".equals(event.getScript())) {
                return true;
              }
            }
            return false;
          })
        );
        assertTrue(
          events.stream().anyMatch(event -> {
            if(event.getEventId() == EventId.ONMOUSEOVER) {
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
      Collection<ScriptEvent> events = ((JavaScriptActionContent)content).getEvents();
      assertEquals(1, events.size());
      ScriptEvent event = events.stream().findFirst().get();
      String expected = IOUtils.resourceToString("/text/testDbDesignCollections/longjs.js", StandardCharsets.UTF_8).replace('\n', '\r');
      String actual = event.getScript();
      // Chomp the last line-ending character for consistency
      actual = actual.substring(0, actual.length()-1);
      assertEquals(expected, actual);
    }
  }
  
  @Test
  public void testEmptyActions() {
    DbDesign design = this.database.getDesign();
    View view = design.getView("Empty V5Actions").get();
    
    ActionBar actions = view.getActionBar();
    assertEquals(0, actions.getActions().size());
    assertEquals(ActionBar.Alignment.LEFT, actions.getAlignment());
  }
  
  @Test
  public void testTestView() throws IOException {
    DbDesign design = this.database.getDesign();
    View view = design.getView("test view").get();
    
    assertEquals("SELECT @IsAvailable(SomeField)", view.getSelectionFormula());
    assertEquals("\"I am form formula\"", view.getFormFormula().get());
    assertEquals("\"I am help request\"", view.getHelpRequestFormula().get());
    assertEquals("\"I am single-click target\"", view.getSingleClickTargetFrameFormula().get());
    
    Map<EventId, String> formulas = view.getFormulaEvents();
    assertEquals("@StatusBar(\"I am queryopen\")", formulas.get(EventId.CLIENT_VIEW_QUERYOPEN));
    assertEquals("@StatusBar(\"I am postopen\")", formulas.get(EventId.CLIENT_VIEW_POSTOPEN));
    assertEquals("@StatusBar(\"I am regiondoubleclick\")", formulas.get(EventId.CLIENT_VIEW_REGIONDBLCLK));
    assertEquals("@StatusBar(\"I am queryopendocument\")", formulas.get(EventId.CLIENT_VIEW_QUERYOPENDOC));
    assertEquals("@StatusBar(\"I am queryrecalc\")", formulas.get(EventId.CLIENT_VIEW_QUERYRECALC));
    assertEquals("@StatusBar(\"I am queryaddtofolder\")", formulas.get(EventId.CLIENT_VIEW_QUERYADDTOFOLDER));
    assertEquals("@StatusBar(\"I am querypaste\")", formulas.get(EventId.CLIENT_VIEW_QUERYPASTE));
    assertEquals("@StatusBar(\"I am postpaste\")", formulas.get(EventId.CLIENT_VIEW_POSTPASTE));
    assertEquals("@StatusBar(\"I am querydragdrop\")", formulas.get(EventId.CLIENT_VIEW_QUERYDRAGDROP));
    assertEquals("@StatusBar(\"I am postdragdrop\")", formulas.get(EventId.CLIENT_VIEW_POSTDRAGDROP));
    assertEquals("@StatusBar(\"I am queryclose\")", formulas.get(EventId.CLIENT_VIEW_QUERYCLOSE));
    assertEquals("@StatusBar(\"I am queryentryresize\")", formulas.get(EventId.CLIENT_VIEW_QUERYENTRYRESIZE));
    assertEquals("@StatusBar(\"I am postentryresize\")", formulas.get(EventId.CLIENT_VIEW_POSTENTRYRESIZE));
    assertEquals("@StatusBar(\"I am onselect\")", formulas.get(EventId.CLIENT_VIEW_ONSELECT));
    assertEquals("@StatusBar(\"I am onsize\")", formulas.get(EventId.CLIENT_VIEW_ONSIZE));
    
    String expectedLs = IOUtils.resourceToString("/text/testDbDesignCollections/viewtestls.txt", StandardCharsets.UTF_8);
    assertEquals(expectedLs, view.getLotusScript());
  }
}
