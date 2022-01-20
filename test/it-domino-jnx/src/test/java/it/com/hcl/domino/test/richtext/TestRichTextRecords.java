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
package it.com.hcl.domino.test.richtext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.DesignType;
import com.hcl.domino.design.format.ActionBarBackgroundRepeat;
import com.hcl.domino.design.format.ActionBarTextAlignment;
import com.hcl.domino.design.format.ActionWidthMode;
import com.hcl.domino.design.format.ButtonBorderDisplay;
import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DateTimeFlag;
import com.hcl.domino.design.format.DateTimeFlag2;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.design.format.LengthUnit;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NumberPref;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.TimeZoneFormat;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
import com.hcl.domino.design.navigator.NavigatorFillStyle;
import com.hcl.domino.design.navigator.NavigatorLineStyle;
import com.hcl.domino.richtext.HotspotType;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDAction;
import com.hcl.domino.richtext.records.CDActionBar;
import com.hcl.domino.richtext.records.CDActionBarExt;
import com.hcl.domino.richtext.records.CDActionByForm;
import com.hcl.domino.richtext.records.CDAltText;
import com.hcl.domino.richtext.records.CDAreaElement;
import com.hcl.domino.richtext.records.CDBlobPart;
import com.hcl.domino.richtext.records.CDBoxSize;
import com.hcl.domino.richtext.records.CDColor;
import com.hcl.domino.richtext.records.CDDataFlags;
import com.hcl.domino.richtext.records.CDEmbeddedCalendarControl;
import com.hcl.domino.richtext.records.CDEmbeddedContactList;
import com.hcl.domino.richtext.records.CDEmbeddedControl;
import com.hcl.domino.richtext.records.CDEmbeddedEditControl;
import com.hcl.domino.richtext.records.CDEmbeddedExtraInfo;
import com.hcl.domino.richtext.records.CDEmbeddedOutline;
import com.hcl.domino.richtext.records.CDEmbeddedSchedulerControl;
import com.hcl.domino.richtext.records.CDEmbeddedSchedulerControlExtra;
import com.hcl.domino.richtext.records.CDEmbeddedView;
import com.hcl.domino.richtext.records.CDExt2Field;
import com.hcl.domino.richtext.records.CDExtField;
import com.hcl.domino.richtext.records.CDExtField.HelperType;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDFieldHint;
import com.hcl.domino.richtext.records.CDHRule;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDHtmlHeader;
import com.hcl.domino.richtext.records.CDHtmlSegment;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.CDIgnore;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.CDImageHeader.ImageType;
import com.hcl.domino.richtext.records.CDImageSegment;
import com.hcl.domino.richtext.records.CDInline;
import com.hcl.domino.richtext.records.CDKeyword;
import com.hcl.domino.richtext.records.CDLSObjectR6;
import com.hcl.domino.richtext.records.CDLargeParagraph;
import com.hcl.domino.richtext.records.CDLayer;
import com.hcl.domino.richtext.records.CDLayoutButton;
import com.hcl.domino.richtext.records.CDMapElement;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDPositioning;
import com.hcl.domino.richtext.records.CDStorageLink;
import com.hcl.domino.richtext.records.CDStyleName;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.CDTextEffect;
import com.hcl.domino.richtext.records.CDVerticalAlign;
import com.hcl.domino.richtext.records.CDWinMetaHeader;
import com.hcl.domino.richtext.records.CDWinMetaSegment;
import com.hcl.domino.richtext.records.CurrencyFlag;
import com.hcl.domino.richtext.records.CurrencyType;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.ViewmapDrawingObject;
import com.hcl.domino.richtext.records.ViewmapHighlightDefaults;
import com.hcl.domino.richtext.records.ViewmapLineDefaults;
import com.hcl.domino.richtext.records.ViewmapShapeDefaults;
import com.hcl.domino.richtext.records.ViewmapTextboxDefaults;
import com.hcl.domino.richtext.records.ViewmapHeaderRecord;
import com.hcl.domino.richtext.records.ViewmapTextRecord;
import com.hcl.domino.richtext.records.ViewmapButtonDefaults;
import com.hcl.domino.richtext.records.ViewmapRegionRecord;
import com.hcl.domino.richtext.records.ViewmapActionRecord;
import com.hcl.domino.richtext.structures.AssistFieldStruct;
import com.hcl.domino.richtext.structures.AssistFieldStruct.ActionByField;
import com.hcl.domino.richtext.structures.CDPoint;
import com.hcl.domino.richtext.structures.CDRect;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.LengthValue;
import com.hcl.domino.richtext.structures.NFMT;
import com.hcl.domino.richtext.structures.TFMT;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestRichTextRecords extends AbstractNotesRuntimeTest {

  @Test
  public void testActionBar() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionBar.class, actionBar -> {
          actionBar.setBackgroundColor(1);
          actionBar.setLineColor(2);
          actionBar.setLineStyle(CDActionBar.LineStyle.TRIPLE);
          actionBar.setBorderStyle(CDActionBar.BorderStyle.ABS);
          actionBar.setFlags(EnumSet.of(CDActionBar.Flag.ABSOLUTE_HEIGHT, CDActionBar.Flag.BTNBCK_IMGRSRC));
          actionBar.setShareId(17);
          actionBar.getFontStyle().setStandardFont(StandardFonts.TYPEWRITER);
          actionBar.setButtonHeight(3);
          actionBar.setHeightSpacing(4);
        });
        rtWriter.addRichTextRecord(CDActionBarExt.class, actionBarExt -> {
          actionBarExt.getBackgroundColor().setBlue((short) 1);
          actionBarExt.getLineColor().setBlue((short) 2);
          actionBarExt.getFontColor().setBlue((short) 3);
          actionBarExt.getButtonColor().setBlue((short) 4);
          actionBarExt.setBorderDisplay(ButtonBorderDisplay.NOTES);
          actionBarExt.setAppletHeight(30);
          actionBarExt.setBackgroundRepeat(ActionBarBackgroundRepeat.REPEATHORIZ);
          actionBarExt.setWidthStyle(ActionWidthMode.BACKGROUND);
          actionBarExt.setTextJustify(ActionBarTextAlignment.RIGHT);
          actionBarExt.setButtonWidth(31);
          actionBarExt.setButtonInternalMargin(32);
          actionBarExt.setFlags(EnumSet.of(CDActionBarExt.Flag.WIDTH_STYLE_VALID));
          actionBarExt.getFontStyle().setStandardFont(StandardFonts.UNICODE);
          actionBarExt.getHeight().setUnit(LengthUnit.EMS);
          actionBarExt.getHeight().setLength(5.5);
        });
      }

      final RichTextRecordList body = doc.getRichTextItem("Body");
      final CDActionBar actionBar = (CDActionBar) body.get(0);
      assertEquals(1, actionBar.getBackgroundColor());
      assertEquals(2, actionBar.getLineColor());
      assertEquals(CDActionBar.LineStyle.TRIPLE, actionBar.getLineStyle());
      assertEquals(CDActionBar.BorderStyle.ABS, actionBar.getBorderStyle());
      assertEquals(EnumSet.of(CDActionBar.Flag.ABSOLUTE_HEIGHT, CDActionBar.Flag.BTNBCK_IMGRSRC), actionBar.getFlags());
      assertEquals(17, actionBar.getShareId());
      assertEquals(3, actionBar.getButtonHeight());
      assertEquals(4, actionBar.getHeightSpacing());

      final CDActionBarExt actionBarExt = (CDActionBarExt) body.get(1);
      assertEquals(1, actionBarExt.getBackgroundColor().getBlue());
      assertEquals(2, actionBarExt.getLineColor().getBlue());
      assertEquals(3, actionBarExt.getFontColor().getBlue());
      assertEquals(4, actionBarExt.getButtonColor().getBlue());
      assertEquals(ButtonBorderDisplay.NOTES, actionBarExt.getBorderDisplay());
      assertEquals(30, actionBarExt.getAppletHeight());
      assertEquals(ActionBarBackgroundRepeat.REPEATHORIZ, actionBarExt.getBackgroundRepeat().get());
      assertEquals(ActionWidthMode.BACKGROUND, actionBarExt.getWidthStyle());
      assertEquals(ActionBarTextAlignment.RIGHT, actionBarExt.getTextJustify());
      assertEquals(31, actionBarExt.getButtonWidth());
      assertEquals(32, actionBarExt.getButtonInternalMargin());
      assertEquals(EnumSet.of(CDActionBarExt.Flag.WIDTH_STYLE_VALID), actionBarExt.getFlags());
      assertEquals(StandardFonts.UNICODE, actionBarExt.getFontStyle().getStandardFont().get());
      assertEquals(LengthUnit.EMS, actionBarExt.getHeight().getUnit());
      assertEquals(5.5, actionBarExt.getHeight().getLength());
    });
  }
  
  @Test
  public void testActionBarExt() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      try(RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionBarExt.class, action -> {
          action.setBackgroundRepeatRaw((short)12);
          action.setBackgroundRepeat(ActionBarBackgroundRepeat.REPEATHORIZ);
        });
      }
      
      CDActionBarExt action = (CDActionBarExt)doc.getRichTextItem("Body").get(0);
      assertEquals(ActionBarBackgroundRepeat.REPEATHORIZ, action.getBackgroundRepeat().get());
      assertEquals(ActionBarBackgroundRepeat.REPEATHORIZ.getValue(), action.getBackgroundRepeatRaw());
    });
  }

  @Test
  public void testActionFormula() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDAction.class, action -> {
          action.setFlags(EnumSet.of(CDAction.Flag.ALIGN_ICON_RIGHT, CDAction.Flag.HIDE_FROM_MOBILE));
          action.setTitle("Hello there");
          action.setHideWhenFormula("Index>1");
          action.setActionFormula("@Command([AdminRemoteConsole])");
        });
      }

      final CDAction action = (CDAction) doc.getRichTextItem("Body").get(0);
      assertEquals(CDAction.Type.RUN_FORMULA, action.getActionType());
      assertEquals(EnumSet.of(CDAction.Flag.ALIGN_ICON_RIGHT, CDAction.Flag.HIDE_FROM_MOBILE), action.getFlags());
      assertEquals("Hello there", action.getTitle());
      assertEquals("Index>1", action.getHideWhenFormula());
      assertEquals("@Command([AdminRemoteConsole])", action.getActionFormula());
      assertThrows(UnsupportedOperationException.class, action::getActionLotusScript);
    });
  }

  @Test
  public void testActionLotusScript() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDAction.class, action -> {
          action.setFlags(EnumSet.of(CDAction.Flag.ALIGN_ICON_RIGHT, CDAction.Flag.HIDE_FROM_MOBILE));
          action.setTitle("Hello there from LotusScript");
          action.setHideWhenFormula("Index>1");
          action.setActionLotusScript("MsgBox | I am a bad language |");
        });
      }

      final CDAction action = (CDAction) doc.getRichTextItem("Body").get(0);
      assertEquals(CDAction.Type.RUN_SCRIPT, action.getActionType());
      assertEquals(EnumSet.of(CDAction.Flag.ALIGN_ICON_RIGHT, CDAction.Flag.HIDE_FROM_MOBILE), action.getFlags());
      assertEquals("Hello there from LotusScript", action.getTitle());
      assertEquals("Index>1", action.getHideWhenFormula());
      assertEquals("MsgBox | I am a bad language |", action.getActionLotusScript());
      assertThrows(UnsupportedOperationException.class, action::getActionFormula);
    });
  }

  @Test
  public void testAssistFieldStruct() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        final AssistFieldStruct assistField = rtWriter.createStructure(AssistFieldStruct.class, 0);
        assistField.setOperatorRaw((short) 2);
        assertEquals(AssistFieldStruct.ActionByField.APPEND, assistField.getActionOperator());
        assertEquals(AssistFieldStruct.QueryByField.LESS, assistField.getQueryOperator());

        assistField.setActionOperator(AssistFieldStruct.ActionByField.REMOVE);
        assertEquals(AssistFieldStruct.ActionByField.REMOVE, assistField.getActionOperator());
        assistField.setQueryOperator(AssistFieldStruct.QueryByField.DOESNOTCONTAIN);
        assertEquals(AssistFieldStruct.QueryByField.DOESNOTCONTAIN, assistField.getQueryOperator());

        assistField.setFieldName("foo");
        assertEquals("foo", assistField.getFieldName());
        assistField.setValues(Arrays.asList("bar", "baz", "ness"));
        assertEquals("foo", assistField.getFieldName());
        assertEquals(Arrays.asList("bar", "baz", "ness"), assistField.getValues());
      }
    });
  }

  @Test
  public void testCaptionText() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDHotspotBegin.class, begin -> {
          begin.setHotspotType(HotspotType.FILE);
          begin.setFileNames("foo.txt", "bar.txt");
        });
      }

      final CDHotspotBegin begin = (CDHotspotBegin) doc.getRichTextItem("Body").get(0);
      assertEquals("foo.txt", begin.getUniqueFileName().get());
      assertEquals("bar.txt", begin.getDisplayFileName().get());
    });
  }

  @Test
  public void testCDActionByForm() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionByForm.class, actionByForm -> {
          actionByForm.setFormName("FakeForm");
          actionByForm.setAssistFields(Arrays.asList(
              rtWriter.createStructure(AssistFieldStruct.class, 0)
                  .setFieldName("Foo")
                  .setActionOperator(ActionByField.REPLACE),
              rtWriter.createStructure(AssistFieldStruct.class, 0)
                  .setFieldName("Bar")
                  .setActionOperator(ActionByField.APPEND)));
        });
      }

      final RichTextRecordList body = doc.getRichTextItem("Body", Area.TYPE_ACTION);
      final CDActionByForm actionByForm = (CDActionByForm) body.get(0);
      assertEquals(2, actionByForm.getFieldCount());
      assertEquals("FakeForm", actionByForm.getFormName());

      final List<AssistFieldStruct> assistFields = actionByForm.getAssistFields();
      assertNotNull(assistFields);
      assertEquals(2, assistFields.size());
      assertEquals("Foo", assistFields.get(0).getFieldName());
      assertEquals(ActionByField.REPLACE, assistFields.get(0).getActionOperator());
      assertEquals("Bar", assistFields.get(1).getFieldName());
      assertEquals(ActionByField.APPEND, assistFields.get(1).getActionOperator());
    });
  }

  @Test
  public void testCDColor() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDColor.class, color -> {
          color.getColor().setRed((short) 1);
          color.getColor().setGreen((short) 2);
          color.getColor().setBlue((short) 3);
        });
      }

      final CDColor color = (CDColor) doc.getRichTextItem("Body").get(0);
      assertEquals((short) 1, color.getColor().getRed());
      assertEquals((short) 2, color.getColor().getGreen());
      assertEquals((short) 3, color.getColor().getBlue());
    });
  }

  @Test
  public void testCDDataFlags() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDDataFlags.class, flags -> {
          flags.setElementType(CDDataFlags.ElementType.BUTTONEX);
          flags.setFlags(new int[] { 0x00000000, 0x000010000, 0x10000000, 0xFFFFFFFF });
        });
      }

      final CDDataFlags flags = (CDDataFlags) doc.getRichTextItem("Body").get(0);
      assertEquals(4, flags.getFlagCount());
      assertArrayEquals(new int[] { 0x00000000, 0x000010000, 0x10000000, 0xFFFFFFFF }, flags.getFlags());
    });
  }

  @Test
  public void testCDEmbeddedControl() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDEmbeddedControl.class, control -> {
          control.setControlType(CDEmbeddedControl.Type.COMBO);
          control.setFlags(EnumSet.of(CDEmbeddedControl.Flag.DIALOGUNITS, CDEmbeddedControl.Flag.POSITION_HEIGHT));
          control.setHeight(480);
          control.setWidth(640);
          control.setStyle(CDEmbeddedControl.Style.EDITCOMBO);
          control.setVersion(CDEmbeddedControl.Version.VERSION1);
          control.setPercentage(80);
          control.setMaxChars(255);
        });
      }

      final CDEmbeddedControl control = (CDEmbeddedControl) doc.getRichTextItem("Body").get(0);
      assertEquals(CDEmbeddedControl.Type.COMBO, control.getControlType());
      assertEquals(EnumSet.of(CDEmbeddedControl.Flag.DIALOGUNITS, CDEmbeddedControl.Flag.POSITION_HEIGHT),
          control.getFlags());
      assertEquals(480, control.getHeight());
      assertEquals(640, control.getWidth());
      assertTrue(control.getStyle().contains(CDEmbeddedControl.Style.EDITCOMBO));
      assertEquals(CDEmbeddedControl.Version.VERSION1, control.getVersion());
      assertEquals(80, control.getPercentage());
      assertEquals(255, control.getMaxChars());
    });
  }

  @Test
  public void testCDExt2Field() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDExt2Field.class, field -> {
          field.setNumberSymbolPreference(NumberPref.FIELD);
          field.setDecimalSymbol("..");
          field.setMilliSeparator(",,");
          field.setNegativeSymbol("--");
          field.setMilliGroupSize(4);
          field.setVerticalSpacing((short) -3);
          field.setHorizontalSpacing((short) -4);
          field.setCurrencyPreference(NumberPref.FIELD);
          field.setCurrencyType(CurrencyType.CUSTOM);
          field.setCurrencyFlags(EnumSet.of(CurrencyFlag.SYMFOLLOWS));
          field.setCurrencySymbol("$$");
          field.setISOCountry(4);
          field.setThumbnailImageWidth(640);
          field.setThumbnailImageHeight(480);
          field.setThumbnailImageFileName("foo.png");
          field.setIMOnlineNameFormula("\"foo fooson\"");
          field.setDateTimePreference(NumberPref.FIELD);
          field.setDateTimeFlags(EnumSet.of(DateTimeFlag.SHOWDATE, DateTimeFlag.SHOWTIME));
          field.setDateTimeFlags2(EnumSet.of(DateTimeFlag2.USE_TFMT));
          field.setDayOfWeekFormat(WeekFormat.WWWW);
          field.setYearFormat(YearFormat.GGEE);
          field.setMonthFormat(MonthFormat.MMM);
          field.setDayFormat(DayFormat.DD);
          field.setDateSeparator1("//");
          field.setDateSeparator2("\\\\");
          field.setDateSeparator3("__");
          field.setTimeSeparator("??");
          field.setDateShowFormat(DateShowFormat.MDY);
          field.setDateShowSpecial(EnumSet.of(DateShowSpecial.SHOW_21ST_4DIGIT));
          field.setTimeShowFormat(TimeShowFormat.HM);
          field.setFormatFlags(EnumSet.of(CDExt2Field.FormatFlag.PROPORTIONAL));
          field.setProportionalWidthCharacters(16);
          field.setInputEnabledFormula("@False + @True");
          field.setIMGroupFormula("\"some group\"");
        });
      }

      final CDExt2Field field = (CDExt2Field) doc.getRichTextItem("Body").get(0);
      assertEquals(NumberPref.FIELD, field.getNumberSymbolPreference().get());
      assertEquals("..", field.getDecimalSymbol());
      assertEquals(",,", field.getMilliSeparator());
      assertEquals("--", field.getNegativeSymbol());
      assertEquals(4, field.getMilliGroupSize());
      assertEquals((short) -3, field.getVerticalSpacing());
      assertEquals((short) -4, field.getHorizontalSpacing());
      assertEquals(NumberPref.FIELD, field.getCurrencyPreference());
      assertEquals(CurrencyType.CUSTOM, field.getCurrencyType());
      assertEquals(EnumSet.of(CurrencyFlag.SYMFOLLOWS), field.getCurrencyFlags());
      assertEquals("$$", field.getCurrencySymbol());
      assertEquals(4, field.getISOCountry());
      assertEquals(640, field.getThumbnailImageWidth());
      assertEquals(480, field.getThumbnailImageHeight());
      assertEquals("foo.png", field.getThumbnailImageFileName());
      assertEquals("\"foo fooson\"", field.getIMOnlineNameFormula());
      assertEquals(NumberPref.FIELD, field.getDateTimePreference().get());
      assertEquals(EnumSet.of(DateTimeFlag.SHOWDATE, DateTimeFlag.SHOWTIME), field.getDateTimeFlags());
      assertEquals(EnumSet.of(DateTimeFlag2.USE_TFMT), field.getDateTimeFlags2());
      assertEquals(WeekFormat.WWWW, field.getDayOfWeekFormat().get());
      assertEquals(YearFormat.GGEE, field.getYearFormat().get());
      assertEquals(MonthFormat.MMM, field.getMonthFormat().get());
      assertEquals(DayFormat.DD, field.getDayFormat().get());
      assertEquals("//", field.getDateSeparator1());
      assertEquals("\\\\", field.getDateSeparator2());
      assertEquals("__", field.getDateSeparator3());
      assertEquals("??", field.getTimeSeparator());
      assertEquals(DateShowFormat.MDY, field.getDateShowFormat().get());
      assertEquals(EnumSet.of(DateShowSpecial.SHOW_21ST_4DIGIT), field.getDateShowSpecial());
      assertEquals(TimeShowFormat.HM, field.getTimeShowFormat().get());
      assertEquals(EnumSet.of(CDExt2Field.FormatFlag.PROPORTIONAL), field.getFormatFlags());
      assertEquals(16, field.getProportionalWidthCharacters());
      assertEquals("@False + @True", field.getInputEnabledFormula());
      assertEquals("\"some group\"", field.getIMGroupFormula());
    });
  }

  @Test
  public void testCDExtField() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDExtField.class, extField -> {
          extField.setFlags1(EnumSet.of(CDExtField.Flag.KWHINKYMINKY));
          extField.setFlags2(EnumSet.of(CDExtField.Flag2.CONTROL, CDExtField.Flag2.ALLOWTABBINGOUT));
          extField.setHelperType(HelperType.VIEWDLG);
          extField.setEntryDBName("foo.nsf");
          extField.setEntryViewName("Some view");
          extField.setEntryColumnNumber(6);
        });
      }

      final CDExtField extField = (CDExtField) doc.getRichTextItem("Body").get(0);
      // KEYWORD_FRAME_3D is 0x00000000 and so comes along for the ride
      assertEquals(EnumSet.of(CDExtField.Flag.KWHINKYMINKY, CDExtField.Flag.KEYWORD_FRAME_3D), extField.getFlags1());
      assertEquals(EnumSet.of(CDExtField.Flag2.CONTROL, CDExtField.Flag2.ALLOWTABBINGOUT), extField.getFlags2());
      assertEquals(HelperType.VIEWDLG, extField.getHelperType());
      assertEquals("foo.nsf", extField.getEntryDBName());
      assertEquals("Some view", extField.getEntryViewName());
      assertEquals(6, extField.getEntryColumnNumber());
    });
  }

  @Test
  public void testCDField() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDField.class, field -> {
          field.setFlags(EnumSet.of(CDField.Flag.COMPUTED, CDField.Flag.PROTECTED));
          field.setFieldType(ItemDataType.TYPE_TEXT);
          field.setListDisplayDelimiter(FieldListDisplayDelimiter.NEWLINE);
          field.setListDelimiters(EnumSet.of(FieldListDelimiter.NEWLINE, FieldListDelimiter.SEMICOLON));

          field.getNumberFormat().setAttributes(EnumSet.of(NFMT.Attribute.PARENS));
          field.getNumberFormat().setDigits((short) 4);
          field.getNumberFormat().setFormat(NFMT.Format.CURRENCY);

          field.getTimeFormat().setDateFormat(TFMT.DateFormat.CPARTIAL4);
          field.getTimeFormat().setTimeFormat(TFMT.TimeFormat.HOUR);
          field.getTimeFormat().setTimeStructure(TFMT.TimeStructure.CDATETIME);
          field.getTimeFormat().setZoneFormat(TimeZoneFormat.SOMETIMES);

          field.setDefaultValueFormula("\"hi\"");
          field.setInputTranslationFormula("@ThisValue+\"hi\"");
          field.setInputValidationFormula("@If(a; @Success; @Failure(\"nooo\"))");
          field.setDescription("Test Description");
          field.setTextValues(Arrays.asList("foo", "bar"));
          field.setName("TextField");
        });
      }

      final CDField field = (CDField) doc.getRichTextItem("Body").get(0);
      assertEquals(ItemDataType.TYPE_TEXT, field.getFieldType());
      // KEYWORDS_UI_STANDARD is 0x0000 and thus always shows up
      assertEquals(EnumSet.of(CDField.Flag.COMPUTED, CDField.Flag.PROTECTED, CDField.Flag.KEYWORDS_UI_STANDARD),
          field.getFlags());
      assertEquals(FieldListDisplayDelimiter.NEWLINE, field.getListDisplayDelimiter());
      assertEquals(EnumSet.of(FieldListDelimiter.NEWLINE, FieldListDelimiter.SEMICOLON), field.getListDelimiters());

      final NFMT numberFormat = field.getNumberFormat();
      assertEquals(EnumSet.of(NFMT.Attribute.PARENS), numberFormat.getAttributes());
      assertEquals((short) 4, numberFormat.getDigits());
      assertEquals(NFMT.Format.CURRENCY, numberFormat.getFormat());

      final TFMT timeFormat = field.getTimeFormat();
      assertEquals(TFMT.DateFormat.CPARTIAL4, timeFormat.getDateFormat());
      assertEquals(TFMT.TimeFormat.HOUR, timeFormat.getTimeFormat());
      assertEquals(TFMT.TimeStructure.CDATETIME, timeFormat.getTimeStructure());
      assertEquals(TimeZoneFormat.SOMETIMES, timeFormat.getZoneFormat());

      assertEquals("\"hi\"", field.getDefaultValueFormula());
      assertEquals("@ThisValue+\"hi\"", field.getInputTranslationFormula());
      assertEquals("@If(a; @Success; @Failure(\"nooo\"))", field.getInputValidationFormula());
      assertEquals("Test Description", field.getDescription());
      assertEquals(Arrays.asList("foo", "bar"), field.getTextValues().get());
      Assertions.assertFalse(field.getTextValueFormula().isPresent());
      assertEquals("TextField", field.getName());
    });
  }

  @Test
  public void testCDFieldFomula() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDField.class, field -> {
          field.setFlags(EnumSet.of(CDField.Flag.COMPUTED, CDField.Flag.PROTECTED));
          field.setFieldType(ItemDataType.TYPE_TEXT);
          field.setListDisplayDelimiter(FieldListDisplayDelimiter.NEWLINE);
          field.setListDelimiters(EnumSet.of(FieldListDelimiter.NEWLINE, FieldListDelimiter.SEMICOLON));

          field.getNumberFormat().setAttributes(EnumSet.of(NFMT.Attribute.PARENS));
          field.getNumberFormat().setDigits((short) 4);
          field.getNumberFormat().setFormat(NFMT.Format.CURRENCY);

          field.getTimeFormat().setDateFormat(TFMT.DateFormat.CPARTIAL4);
          field.getTimeFormat().setTimeFormat(TFMT.TimeFormat.HOUR);
          field.getTimeFormat().setTimeStructure(TFMT.TimeStructure.CDATETIME);
          field.getTimeFormat().setZoneFormat(TimeZoneFormat.SOMETIMES);

          field.setDefaultValueFormula("\"hi\"");
          field.setInputTranslationFormula("@ThisValue+\"hi\"");
          field.setInputValidationFormula("@If(a; @Success; @Failure(\"nooo\"))");
          field.setDescription("Test Description");
          field.setTextValueFormula("\"foo\":\"bar\"");
          field.setName("TextField");
        });
      }

      final CDField field = (CDField) doc.getRichTextItem("Body").get(0);
      assertEquals(ItemDataType.TYPE_TEXT, field.getFieldType());
      // KEYWORDS_UI_STANDARD is 0x0000 and thus always shows up
      assertEquals(EnumSet.of(CDField.Flag.COMPUTED, CDField.Flag.PROTECTED, CDField.Flag.KEYWORDS_UI_STANDARD),
          field.getFlags());
      assertEquals(FieldListDisplayDelimiter.NEWLINE, field.getListDisplayDelimiter());
      assertEquals(EnumSet.of(FieldListDelimiter.NEWLINE, FieldListDelimiter.SEMICOLON), field.getListDelimiters());

      final NFMT numberFormat = field.getNumberFormat();
      assertEquals(EnumSet.of(NFMT.Attribute.PARENS), numberFormat.getAttributes());
      assertEquals((short) 4, numberFormat.getDigits());
      assertEquals(NFMT.Format.CURRENCY, numberFormat.getFormat());

      final TFMT timeFormat = field.getTimeFormat();
      assertEquals(TFMT.DateFormat.CPARTIAL4, timeFormat.getDateFormat());
      assertEquals(TFMT.TimeFormat.HOUR, timeFormat.getTimeFormat());
      assertEquals(TFMT.TimeStructure.CDATETIME, timeFormat.getTimeStructure());
      assertEquals(TimeZoneFormat.SOMETIMES, timeFormat.getZoneFormat());

      assertEquals("\"hi\"", field.getDefaultValueFormula());
      assertEquals("@ThisValue+\"hi\"", field.getInputTranslationFormula());
      assertEquals("@If(a; @Success; @Failure(\"nooo\"))", field.getInputValidationFormula());
      assertEquals("Test Description", field.getDescription());
      Assertions.assertFalse(field.getTextValues().isPresent());
      assertEquals("\"foo\":\"bar\"", field.getTextValueFormula().get());
      assertEquals("TextField", field.getName());
    });
  }

  @Test
  public void testCDFieldHint() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDFieldHint.class, hint -> {
          hint.setHintText("foo bar");
        });
      }

      final CDFieldHint fieldHint = (CDFieldHint) doc.getRichTextItem("Body").get(0);
      assertEquals(EnumSet.noneOf(CDFieldHint.Flag.class), fieldHint.getFlags());
      assertEquals("foo bar", fieldHint.getHintText());
    });
  }

  @Test
  public void testCDFieldHint2() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDFieldHint.class, hint -> {
          hint.setFlags(EnumSet.of(CDFieldHint.Flag.LIMITED));
          hint.setHintText("pictures");
        });
      }

      final CDFieldHint fieldHint = (CDFieldHint) doc.getRichTextItem("Body").get(0);
      assertEquals(EnumSet.of(CDFieldHint.Flag.LIMITED), fieldHint.getFlags());
      assertEquals("pictures", fieldHint.getHintText());
    });
  }

  @Test
  public void testCDIDName() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDIDName.class, name -> {
          name.setID("foo");
          name.setClassName("bar baz");
          name.setStyle("border-left: 0");
          name.setTitle("I am a title");
          name.setHTMLAttributes("border=\"0\"");
          name.setName("foo-name");
        });
      }

      final CDIDName name = (CDIDName) doc.getRichTextItem("Body").get(0);
      assertEquals("foo", name.getID());
      assertEquals("bar baz", name.getClassName());
      assertEquals("border-left: 0", name.getStyle());
      assertEquals("I am a title", name.getTitle());
      assertEquals("border=\"0\"", name.getHTMLAttributes());
      assertEquals("foo-name", name.getName());
    });
  }

  @Test
  public void testCDKeyword() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDKeyword.class, keyword -> {
          keyword.setFlags(EnumSet.of(CDKeyword.Flag.FRAME_3D, CDKeyword.Flag.KEYWORD_RTL));
          keyword.setKeywords(Arrays.asList("foo", "bar", "baz"), Arrays.asList(true, false, true));
        });
      }

      Assertions.assertInstanceOf(CDKeyword.class, doc.getRichTextItem("Body").get(0));
      final CDKeyword keyword = (CDKeyword) doc.getRichTextItem("Body").get(0);
      assertEquals(EnumSet.of(CDKeyword.Flag.FRAME_3D, CDKeyword.Flag.KEYWORD_RTL), keyword.getFlags());
      assertEquals(3, keyword.getKeywordCount());
      assertEquals(Arrays.asList("foo", "bar", "baz"), keyword.getKeywords());
      assertEquals(Arrays.asList(true, false, true), keyword.getEnabledStates());
    });
  }

  @Test
  public void testCreateText() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDParagraph.class, p -> {
        });
        rtWriter.addRichTextRecord(CDText.class, text -> {
          text.getStyle().setBold(true);
          text.getStyle().setStandardFont(StandardFonts.TYPEWRITER);
        });
        rtWriter.addRichTextRecord(CDImageHeader.class, header -> {
          header.setHeight(600)
              .setWidth(800)
              .setImageDataSize(2048)
              .setSegCount(2)
              .setImageType(ImageType.PNG);
        });
      }

      final List<RichTextRecord<?>> body = doc.getRichTextItem("Body");
      assertEquals(3, body.size());

      {
        Assertions.assertInstanceOf(CDParagraph.class, body.get(0), "Unexpected first record " + body.get(0));
        assertEquals(2, body.get(0).getHeader().getLength().intValue());
        assertEquals(2, body.get(0).getData().capacity());
      }
      {
        final CDText text = Assertions.assertInstanceOf(CDText.class, body.get(1));
        assertEquals(8, body.get(1).getHeader().getLength().intValue());
        assertEquals(8, body.get(1).getData().capacity());
        assertTrue(text.getStyle().isBold());
        assertEquals(StandardFonts.TYPEWRITER, text.getStyle().getStandardFont().get());
        
        assertEquals(EnumSet.of(RecordType.TEXT), text.getType());
      }
      {
        Assertions.assertInstanceOf(CDImageHeader.class, body.get(2));
        final CDImageHeader header = (CDImageHeader) body.get(2);
        assertEquals(28, header.getData().capacity());
        assertEquals(800, header.getWidth());
        assertEquals(600, header.getHeight());
        assertEquals(2048, header.getImageDataSize());
        assertEquals(2, header.getSegCount());
        assertEquals(ImageType.PNG, header.getImageType());
      }
    });
  }

  @Test
  public void testImageResource() throws Exception {
    this.withTempDb(database -> {
      final byte[] imageData = IOUtils.resourceToByteArray("/images/help_vampire.gif");

      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        final long segCount = (long) Math.ceil(imageData.length / (RichTextConstants.IMAGE_SEGMENT_MAX * 1d));

        rtWriter.addRichTextRecord(CDImageHeader.class, header -> {
          header.setHeight(367)
              .setWidth(900)
              .setImageDataSize(imageData.length)
              .setSegCount(segCount)
              .setImageType(ImageType.GIF);
        });

        int offset = 0;
        for (long i = 0; i < segCount; i++) {
          final int dataSize = Math.min(imageData.length - offset, RichTextConstants.IMAGE_SEGMENT_MAX);
          final int segSize = dataSize + dataSize % 2;
          final byte[] segData = Arrays.copyOfRange(imageData, offset, offset + dataSize);

          rtWriter.addRichTextRecord(CDImageSegment.class, segSize, segment -> {
            segment.setDataSize(dataSize);
            segment.setSegSize(segSize);

            final ByteBuffer variable = segment.getData().slice();
            variable.position(10);
            variable.put(segData);
          });

          offset += dataSize;
        }
      }

      final ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
      doc.getRichTextItem("Body").stream()
          .filter(record -> record.getType().contains(RecordType.IMAGESEGMENT))
          .map(CDImageSegment.class::cast)
          .forEach(record -> {
            // LSIG Header (6)
            // WORD DataSize (2)
            // WORD SegSize (2)
            // Data

            final ByteBuffer data = record.getDataWithoutHeader();
            final int dataLen = record.getDataSize();
            final byte[] segData = new byte[dataLen];
            data.position(4);
            data.get(segData);
            try {
              imageStream.write(segData);
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          });

      // Read in the expected data
      assertArrayEquals(imageData, imageStream.toByteArray());
    });
  }

  @Test
  public void testInnerArrayStruct() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {

        // Test manipulating the default array
        {
          final FakeArrayStruct fakeArray = rtWriter.createStructure(FakeArrayStruct.class, 0);
          assertNotNull(fakeArray.getFontStyles());
          assertEquals(3, fakeArray.getFontStyles().length);

          fakeArray.getFontStyles()[0].setColor(StandardColors.DarkBlue);
          fakeArray.getFontStyles()[1].setColor(StandardColors.DarkMagenta);
          fakeArray.getFontStyles()[2].setColor(StandardColors.DarkGreen);

          assertEquals(StandardColors.DarkBlue, fakeArray.getFontStyles()[0].getColor().get());
          assertEquals(StandardColors.DarkMagenta, fakeArray.getFontStyles()[1].getColor().get());
          assertEquals(StandardColors.DarkGreen, fakeArray.getFontStyles()[2].getColor().get());
        }

        // Test writing an accurately-sized array back
        {
          final FakeArrayStruct fakeArray = rtWriter.createStructure(FakeArrayStruct.class, 0);

          final FontStyle[] styles = {
              rtWriter.createStructure(FontStyle.class, 0)
                  .setColor(StandardColors.Cyan),
              rtWriter.createStructure(FontStyle.class, 0)
                  .setColor(StandardColors.DarkYellow),
              rtWriter.createStructure(FontStyle.class, 0)
                  .setColor(StandardColors.Magenta)
          };
          fakeArray.setFontStyles(styles);

          assertEquals(StandardColors.Cyan, fakeArray.getFontStyles()[0].getColor().get());
          assertEquals(StandardColors.DarkYellow, fakeArray.getFontStyles()[1].getColor().get());
          assertEquals(StandardColors.Magenta, fakeArray.getFontStyles()[2].getColor().get());
        }

        // Test writing incorrectly-sized arrays back
        {
          final FakeArrayStruct fakeArray = rtWriter.createStructure(FakeArrayStruct.class, 0);
          assertThrows(IllegalArgumentException.class, () -> fakeArray.setFontStyles(new FontStyle[1]));
          assertThrows(IllegalArgumentException.class, () -> fakeArray.setFontStyles(new FontStyle[4]));
        }
      }
    });
  }

  @Test
  public void testWriteByteArray() throws Exception {
    this.withTempDb(database -> {
      final byte[] array = { 1, 2, 3, 4, 5, 6, 7, 8 };
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDBlobPart.class, p -> {
          p.setReserved(array);
        });
      }

      final List<RichTextRecord<?>> body = doc.getRichTextItem("Body");
      assertEquals(1, body.size());

      {
        assertTrue(body.get(0) instanceof CDBlobPart);
        assertArrayEquals(array, ((CDBlobPart) body.get(0)).getReserved());
      }
    });
  }

  @Test
  public void testWriteEnumArray() throws Exception {
    this.withTempDb(database -> {
      final CDEmbeddedOutline.Repeat[] array = new CDEmbeddedOutline.Repeat[] {
        CDEmbeddedOutline.Repeat.SIZE_TO_FIT, CDEmbeddedOutline.Repeat.ONCE,
        CDEmbeddedOutline.Repeat.SIZE_TO_FIT, CDEmbeddedOutline.Repeat.HORIZONTAL
      };
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDEmbeddedOutline.class, outline -> {
          outline.setBackgroundRepeatModes(array);
        });
      }

      final List<RichTextRecord<?>> body = doc.getRichTextItem("Body");
      assertEquals(1, body.size());

      {
        assertTrue(body.get(0) instanceof CDEmbeddedOutline);
        assertArrayEquals(array, ((CDEmbeddedOutline) body.get(0)).getBackgroundRepeatModes());
      }
    });
  }

  @Test
  public void testPreserveUnknownFlags() throws Exception {
    int fakeFlag = 0x00100000; // Not represented by a flag in the API
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionBar.class, bar -> {
          bar.setFlagsRaw(fakeFlag | CDActionBar.Flag.BACKGROUND_HEIGHT.getValue()); 
          bar.setFlags(EnumSet.of(CDActionBar.Flag.ABSOLUTE_HEIGHT));
        });
      }

      final List<RichTextRecord<?>> body = doc.getRichTextItem("Body");
      assertEquals(1, body.size());

      {
        assertTrue(body.get(0) instanceof CDActionBar);
        CDActionBar bar = (CDActionBar)body.get(0);
        assertEquals(EnumSet.of(CDActionBar.Flag.ABSOLUTE_HEIGHT), bar.getFlags());
        assertEquals(0x00100000 | CDActionBar.Flag.ABSOLUTE_HEIGHT.getValue(), bar.getFlagsRaw());
      }
    });
  }
  
  @Test
  public void testAltText() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDAltText.class, begin -> {
          begin.setAltText("foo.txt");
        });
      }

      final CDAltText begin = (CDAltText) doc.getRichTextItem("Body").get(0);
      assertEquals("foo.txt", begin.getAltText());
    });
  }
  
  @Test
  public void testStyleName() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDStyleName.class, begin -> {
          final String expected = "foo";
          begin.setStyleName(expected.getBytes());
          
          FontStyle font = rtWriter.createFontStyle();
          font.setBold(true);
          font.setColorRaw((short)80);
          font.setPointSize((short)12);
          font.setUnderline(true);
          begin.setFont(font);
          
          begin.setUserName("testuser");
          
        });
      }

      final CDStyleName begin = (CDStyleName) doc.getRichTextItem("Body").get(0);
      assertEquals("foo", begin.getStyleName());
      
      
      Optional<FontStyle> font = begin.getFont();
      assertEquals(true, font.get().isBold());
      assertEquals(true, font.get().isUnderline());
      assertEquals(80, font.get().getColorRaw());
      assertEquals(12, font.get().getPointSize());
      
      Optional<String> userName = begin.getUserName();
      assertEquals("testuser", userName.get());
    });
  }
  
  @Test
  public void testVerticalAlign() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDVerticalAlign.class, begin -> {
          begin.setAlignment(CDVerticalAlign.Alignment.CENTER);
        });
      }

      final CDVerticalAlign begin = (CDVerticalAlign) doc.getRichTextItem("Body").get(0);
      CDVerticalAlign.Alignment alignment = begin.getAlignment();
      
      assertEquals(CDVerticalAlign.Alignment.CENTER, alignment);
    });
  }
    
    @Test
    public void testMapElement() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDMapElement.class, begin -> {
            begin.setLastDefaultRegionID(10);
            begin.setLastCircleRegionID(20);
            begin.setLastRectRegionID(30);
            begin.setLastPolyRegionID(40);
            begin.setMapName("testmap");
          });
        }

        final CDMapElement begin = (CDMapElement) doc.getRichTextItem("Body").get(0);
        
        assertEquals(10, begin.getLastDefaultRegionID());
        assertEquals(20, begin.getLastCircleRegionID());
        assertEquals(30, begin.getLastRectRegionID());
        assertEquals(40, begin.getLastPolyRegionID());
        assertEquals("testmap", begin.getMapName());
      });
    }
    
    @Test
    public void testLargeParagraph() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDLargeParagraph.class, begin -> {
            begin.setFlags(EnumSet.of(CDLargeParagraph.Flag.CDLARGEPARAGRAPH_BEGIN));
          });
        }

        final CDLargeParagraph begin = (CDLargeParagraph) doc.getRichTextItem("Body").get(0);

        assertEquals(EnumSet.of(CDLargeParagraph.Flag.CDLARGEPARAGRAPH_BEGIN), begin.getFlags());
      });
    }
    
    @Test
    public void testTextEffect() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDTextEffect.class, begin -> {
            FontStyle style = rtWriter.createFontStyle();
            style.setBold(true);
            style.setColorRaw((short)80);
            begin.setFontStyle(style);
          });
        }

        final CDTextEffect begin = (CDTextEffect) doc.getRichTextItem("Body").get(0);
        FontStyle style = begin.getFontStyle();
        assertEquals(true, style.isBold());
        assertEquals(80, style.getColorRaw());
      });
    }
    
    @Test
    public void testFontStyleAttributes() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          {
            FontStyle style = rtWriter.createFontStyle();
            
            Assertions.assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes yet");

            style.setBold(true);
            Assertions.assertEquals(EnumSet.of(FontAttribute.BOLD), style.getAttributes(), "Add BOLD attribute");
            style.setBold(false);
            Assertions.assertTrue(style.getAttributes().isEmpty(), "BOLD attribute removed");

            style.setExtrude(true);
            Assertions.assertTrue(style.isExtrude(), "Add EXTRUDE attribute");
            style.setExtrude(false);
            Assertions.assertFalse(style.isExtrude(), "EXTRUDE attribute removed");

            style.setItalic(true);
            Assertions.assertEquals(EnumSet.of(FontAttribute.ITALIC), style.getAttributes(), "Add ITALIC attribute");
            style.setItalic(false);
            Assertions.assertTrue(style.getAttributes().isEmpty(), "Italic attribute removed");

            style.setShadow(true);
            Assertions.assertTrue(style.isShadow(), "Add SHADOW attribute");
            style.setShadow(false);
            Assertions.assertFalse(style.isShadow(), "SHADOW attribute removed");

            style.setStrikeout(true);
            Assertions.assertEquals(EnumSet.of(FontAttribute.STRIKEOUT), style.getAttributes(), "Add STRIKEOUT attribute");
            style.setStrikeout(false);
            Assertions.assertTrue(style.getAttributes().isEmpty(), "STRIKEOUT attribute removed");

            style.setSub(true);
            Assertions.assertEquals(EnumSet.of(FontAttribute.SUB), style.getAttributes(), "Add SUB attribute");
            style.setSub(false);
            Assertions.assertTrue(style.getAttributes().isEmpty(), "SUB attribute removed");

            style.setSuper(true);
            Assertions.assertEquals(EnumSet.of(FontAttribute.SUPER), style.getAttributes(), "Add SUPER attribute");
            style.setSuper(false);
            Assertions.assertTrue(style.getAttributes().isEmpty(), "SUPER attribute removed");

            style.setUnderline(true);
            Assertions.assertEquals(EnumSet.of(FontAttribute.UNDERLINE), style.getAttributes(), "Add UNDERLINE attribute");
            style.setUnderline(false);
            Assertions.assertTrue(style.getAttributes().isEmpty(), "UNDERLINE attribute removed");
          }
          {
            FontStyle style = rtWriter.createFontStyle();
            
            style.setExtrude(true);
            // Extrude uses the same bits as SHADOW and SUB
            assertTrue(style.isExtrude());
            assertTrue(style.isShadow());
            assertTrue(style.isSub());
            
            style.setExtrude(false);
            assertFalse(style.isExtrude());
            assertFalse(style.isShadow());
            assertFalse(style.isSub());
          }
          {
            FontStyle style = rtWriter.createFontStyle();
            style.setSub(true);
            assertTrue(style.isSub());
            assertFalse(style.isExtrude());
            
            style.setSub(false);
            assertFalse(style.isSub());
            assertFalse(style.isExtrude());
          }
          {
            FontStyle style = rtWriter.createFontStyle();
            style.setEmboss(true);
            assertTrue(style.isEmboss());
            assertFalse(style.isExtrude());
            assertTrue(style.isShadow());
            
            style.setEmboss(false);
            assertFalse(style.isEmboss());
            assertFalse(style.isExtrude());
            assertFalse(style.isShadow());
          }
        }
      });
    }
    
    @Test
    public void testAreaElementRectangle() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDAreaElement.class, begin -> {
            CDRect rectangle = rtWriter.createStructure(CDRect.class, 0);
            rectangle.setTop(1);
            rectangle.setBottom(10);
            rectangle.setLeft(2);
            rectangle.setRight(20);
            begin.setRectangle(rectangle);
          });
        }

        final CDAreaElement begin = (CDAreaElement) doc.getRichTextItem("Body").get(0);

        assertEquals(1, begin.getRectangle().get().getTop());
        assertEquals(10, begin.getRectangle().get().getBottom());
        assertEquals(2, begin.getRectangle().get().getLeft());
        assertEquals(20, begin.getRectangle().get().getRight());
      });
    }
    
    @Test
    public void testAreaElementCircle() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDAreaElement.class, begin -> {
            CDRect circle = rtWriter.createStructure(CDRect.class, 0);
            circle.setTop(1);
            circle.setBottom(10);
            circle.setLeft(2);
            circle.setRight(20);
            begin.setCircle(circle);
          });
        }

        final CDAreaElement begin = (CDAreaElement) doc.getRichTextItem("Body").get(0);

        assertEquals(1, begin.getCircle().get().getTop());
        assertEquals(10, begin.getCircle().get().getBottom());
        assertEquals(2, begin.getCircle().get().getLeft());
        assertEquals(20, begin.getCircle().get().getRight());
      });
    }
    
    @Test
    public void testAreaElementPolygon() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDAreaElement.class, begin -> {
            List<CDPoint> points = new ArrayList<>();
            for (int i=0; i<10; i++) {
              CDPoint point = rtWriter.createStructure(CDPoint.class, 0);
              point.setX(1+i);
              point.setY(10+i);
              points.add(point);
            }
            begin.setPolygon(points);
          });
        }

        final CDAreaElement begin = (CDAreaElement) doc.getRichTextItem("Body").get(0);

        assertEquals(10, begin.getPolygon().get().size());
      });
    }
    
    @Test
    public void testLayer() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDLayer.class, begin -> {
          });
        }

        @SuppressWarnings("unused")
        final CDLayer begin = (CDLayer) doc.getRichTextItem("Body").get(0);
      });
    }
    
    @Test
    public void testEmbeddedSchedulerControlExtra() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedSchedulerControlExtra.class, begin -> {
            begin.setFlags(EnumSet.of(CDEmbeddedSchedulerControlExtra.Flag.SUGG_COLORS_DEFINED));
            begin.setSchedulerName("foo");
            begin.setDetailDisplayFormFormula("@Accessed");
            begin.setFixedPartLength(30);
            begin.setIntervalChangeEventFormula("@Abs(-10)");
            begin.setIntervalEndDTItemFormula("@Abs(10)");
            begin.setIntervalStartDTItemFormula("@Abs(20)");
            begin.setOptPeopleItemsFormula("@Abs(30)");
            begin.setOptResourcesItemsFormula("@Abs(40)");
            begin.setPeopleTitle("people");
            begin.setReqResourcesItemsFormula("@Abs(50)");
            begin.setReqRoomsItemsFormula("@Abs(60)");
            begin.setResourcesTitle("resources");
            begin.setRoomsTitle("rooms");
            begin.setSchedDetailItemsFormula("@Abs(70)");
            begin.setSuggestionsAvailEventFormula("@Abs(80)");
          });
        }

        final CDEmbeddedSchedulerControlExtra begin = (CDEmbeddedSchedulerControlExtra) doc.getRichTextItem("Body").get(0);
        assertEquals(EnumSet.of(CDEmbeddedSchedulerControlExtra.Flag.SUGG_COLORS_DEFINED), begin.getFlags());
        assertEquals("foo", begin.getSchedulerName());
        assertEquals("@Accessed", begin.getDetailDisplayFormFormula().get());
        assertEquals(30, begin.getFixedPartLength());
        assertEquals("@Abs(-10)", begin.getIntervalChangeEventFormula().get());
        assertEquals("@Abs(10)", begin.getIntervalEndDTItemFormula().get());
        assertEquals("@Abs(20)", begin.getIntervalStartDTItemFormula().get());
        assertEquals("@Abs(30)", begin.getOptPeopleItemsFormula().get());
        assertEquals("@Abs(40)", begin.getOptResourcesItemsFormula().get());
        assertEquals("people", begin.getPeopleTitle());
        assertEquals("@Abs(50)", begin.getReqResourcesItemsFormula().get());
        assertEquals("@Abs(60)", begin.getReqRoomsItemsFormula().get());
        assertEquals("resources", begin.getResourcesTitle());
        assertEquals("rooms", begin.getRoomsTitle());
        assertEquals("@Abs(70)", begin.getSchedDetailItemsFormula().get());
        assertEquals("@Abs(80)", begin.getSuggestionsAvailEventFormula().get());
      });
    }
    
    @Test
    public void testEmbeddedSchedulerControl() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedSchedulerControl.class, begin -> {
            begin.setFlags(EnumSet.of(CDEmbeddedSchedulerControl.Flag.SHOW_TWISTIES));
            begin.setTargetFrameName("foo");
            begin.setDisplayStartDTItemFormula("@Abs(10)");
            begin.setHrsPerDayItemFormula("@Abs(20)");
            begin.setReqPeopleItemsFormula("@Abs(30)");
            begin.setNameColWidth(50);
            begin.setPeopleLines(60);
            begin.setRoomsLines(70);
            begin.setResourcesLines(80);
          });
        }

        final CDEmbeddedSchedulerControl begin = (CDEmbeddedSchedulerControl) doc.getRichTextItem("Body").get(0);
        assertEquals(EnumSet.of(CDEmbeddedSchedulerControl.Flag.SHOW_TWISTIES), begin.getFlags());
        assertEquals("foo", begin.getTargetFrameName());
        assertEquals("@Abs(10)", begin.getDisplayStartDTItemFormula().get());
        assertEquals("@Abs(20)", begin.getHrsPerDayItemFormula().get());
        assertEquals("@Abs(30)", begin.getReqPeopleItemsFormula().get());
        assertEquals(50, begin.getNameColWidth());
        assertEquals(60, begin.getPeopleLines());
        assertEquals(70, begin.getRoomsLines());
        assertEquals(80, begin.getResourcesLines());
      });
    }
    
    @Test
    public void testEmbeddedExtraInfo() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedExtraInfo.class, begin -> {
            begin.setName("foo");
          });
        }

        final CDEmbeddedExtraInfo begin = (CDEmbeddedExtraInfo) doc.getRichTextItem("Body").get(0);
        assertEquals("foo", begin.getName());
      });
    }
    
    @Test
    public void testPositioning() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDPositioning.class, begin -> {
            begin.setScheme(CDPositioning.Scheme.ABSOLUTE);
            begin.setBrowserLeftOffset(10);
            begin.setBrowserRightOffset(20);
            begin.setZIndex(30);
          });
        }

        final CDPositioning begin = (CDPositioning) doc.getRichTextItem("Body").get(0);
        assertEquals(CDPositioning.Scheme.ABSOLUTE, begin.getScheme());
        assertEquals(10, begin.getBrowserLeftOffset());
        assertEquals(20, begin.getBrowserRightOffset());
        assertEquals(30, begin.getZIndex());
      });
    }
    
    @Test
    public void testEmbeddedCalendarControl() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedCalendarControl.class, begin -> {
            begin.setFlags(EnumSet.of(CDEmbeddedCalendarControl.Flag.HASTARGETFRAME));
            begin.setTargetFrameName("foo.txt");
          });
        }

        final CDEmbeddedCalendarControl begin = (CDEmbeddedCalendarControl) doc.getRichTextItem("Body").get(0);
        assertEquals(EnumSet.of(CDEmbeddedCalendarControl.Flag.HASTARGETFRAME), begin.getFlags());
        assertEquals("foo.txt", begin.getTargetFrameName());
      });
    }
    
    @Test
    public void testEmbeddedView() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedView.class, begin -> {
            begin.setFlags(EnumSet.of(CDEmbeddedView.Flag.SIMPLE_VIEW_HEADER_OFF));
            begin.setRestrictFormula("@Abs(10)");
          });
        }

        final CDEmbeddedView begin = (CDEmbeddedView) doc.getRichTextItem("Body").get(0);
        assertEquals(EnumSet.of(CDEmbeddedView.Flag.SIMPLE_VIEW_HEADER_OFF), begin.getFlags());
        assertEquals("@Abs(10)", begin.getRestrictFormula().get());
      });
    }
    
    @Test
    public void testBoxSize() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDBoxSize.class, begin -> {
            begin.getHeight().setLength(10);
            begin.getHeight().setUnit(LengthUnit.PERCENT);
            begin.getHeight().setFlags(EnumSet.of(LengthValue.Flag.AUTO));
            begin.getWidth().setLength(20);
            begin.getWidth().setUnit(LengthUnit.PIXELS);
            begin.getWidth().setFlags(EnumSet.of(LengthValue.Flag.AUTO));
          });
        }

        final CDBoxSize begin = (CDBoxSize) doc.getRichTextItem("Body").get(0);
        assertEquals(10, begin.getHeight().getLength());
        assertEquals(LengthUnit.PERCENT, begin.getHeight().getUnit());
        assertEquals(EnumSet.of(LengthValue.Flag.AUTO), begin.getHeight().getFlags());
        assertEquals(20, begin.getWidth().getLength());
        assertEquals(LengthUnit.PIXELS, begin.getWidth().getUnit());
        assertEquals(EnumSet.of(LengthValue.Flag.AUTO), begin.getWidth().getFlags());
      });
    }
    
    @Test
    public void testEmbeddedContactList() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedContactList.class, begin -> {
            begin.getSelectedBackground().setBlue((short)10);
            begin.getSelectedBackground().setRed((short)20);
            begin.getSelectedBackground().setGreen((short)30);
            begin.getSelectedBackground().setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
            begin.getSelectedText().setBlue((short)40);
            begin.getSelectedText().setRed((short)50);
            begin.getSelectedText().setGreen((short)60);
            begin.getSelectedText().setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
            begin.getControlBackground().setBlue((short)70);
            begin.getControlBackground().setRed((short)80);
            begin.getControlBackground().setGreen((short)90);
            begin.getControlBackground().setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
          });
        }

        final CDEmbeddedContactList begin = (CDEmbeddedContactList) doc.getRichTextItem("Body").get(0);
        assertEquals(10, begin.getSelectedBackground().getBlue());
        assertEquals(20, begin.getSelectedBackground().getRed());
        assertEquals(30, begin.getSelectedBackground().getGreen());
        assertEquals(EnumSet.of(ColorValue.Flag.ISRGB), begin.getSelectedBackground().getFlags());
        assertEquals(40, begin.getSelectedText().getBlue());
        assertEquals(50, begin.getSelectedText().getRed());
        assertEquals(60, begin.getSelectedText().getGreen());
        assertEquals(EnumSet.of(ColorValue.Flag.ISRGB), begin.getSelectedText().getFlags());
        assertEquals(70, begin.getControlBackground().getBlue());
        assertEquals(80, begin.getControlBackground().getRed());
        assertEquals(90, begin.getControlBackground().getGreen());
        assertEquals(EnumSet.of(ColorValue.Flag.ISRGB), begin.getSelectedText().getFlags());
      });
    }
    
    @Test
    public void testEmbeddedEditControl() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDEmbeddedEditControl.class, begin -> {
            begin.setName("foo.txt");
            begin.setFlags(EnumSet.of(CDEmbeddedEditControl.Flag.HASNAME));
          });
        }

        final CDEmbeddedEditControl begin = (CDEmbeddedEditControl) doc.getRichTextItem("Body").get(0);
        assertEquals("foo.txt", begin.getName());
        assertEquals(EnumSet.of(CDEmbeddedEditControl.Flag.HASNAME), begin.getFlags());
      });
    }
    
    @Test
    public void testWinMetaHeader() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDWinMetaHeader.class, begin -> {
            begin.setMappingMode(CDWinMetaHeader.MappingMode.HIMETRIC);
            begin.setXExtent((short)20);
            begin.setYExtent((short)30);
            begin.getOriginalDisplaySize().setHeight(40);
            begin.getOriginalDisplaySize().setWidth(50);
            begin.setMetafileSize(90000);
            begin.setSegCount(2);
          });
        }

        final CDWinMetaHeader begin = (CDWinMetaHeader) doc.getRichTextItem("Body").get(0);
        assertEquals(CDWinMetaHeader.MappingMode.HIMETRIC, begin.getMappingMode().get());
        assertEquals(20, begin.getXExtent());
        assertEquals(30, begin.getYExtent());
        assertEquals(40, begin.getOriginalDisplaySize().getHeight());
        assertEquals(50, begin.getOriginalDisplaySize().getWidth());
        assertEquals(90000, begin.getMetafileSize());
        assertEquals(2, begin.getSegCount());
      });
    }
    
    @Test
    public void testWinMetaSegment() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDWinMetaSegment.class, begin -> {
            begin.setDataSize(60000);
            begin.setSegSize(65535);
          });
        }

        final CDWinMetaSegment begin = (CDWinMetaSegment) doc.getRichTextItem("Body").get(0);
        assertEquals(60000, begin.getDataSize());
        assertEquals(65535, begin.getSegSize());
      });
    }

           
    @Test
    public void testLayoutButton() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDLayoutButton.class, begin -> {
          });
        }

        @SuppressWarnings("unused")
        final CDLayoutButton begin = (CDLayoutButton) doc.getRichTextItem("Body").get(0);

      });
    }
  
    @Test
    public void testCDHRule() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDHRule.class, begin -> {
            begin.setHeight(72);
            begin.setWidth(100);
            begin.setFlags(EnumSet.of(CDHRule.Flag.HRULE_FLAG_USECOLOR, CDHRule.Flag.HRULE_FLAG_FITTOWINDOW,CDHRule.Flag.HRULE_FLAG_FITTOWINDOW,CDHRule.Flag.HRULE_FLAG_NOSHADOW));
          });
        }

        final CDHRule begin = (CDHRule) doc.getRichTextItem("Body").get(0);
        assertEquals(72, begin.getHeight());
        assertEquals(100, begin.getWidth());
        assertEquals(EnumSet.of(CDHRule.Flag.HRULE_FLAG_USECOLOR, CDHRule.Flag.HRULE_FLAG_FITTOWINDOW,CDHRule.Flag.HRULE_FLAG_FITTOWINDOW,CDHRule.Flag.HRULE_FLAG_NOSHADOW), begin.getFlags());
      });
    }
   
    @Test
    public void testsetGradientColor() throws Exception{
    	this.withTempDb(database -> {
            final Document doc = database.createDocument();
            try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
              rtWriter.addRichTextRecord(CDHRule.class, begin -> {
            	  begin.setColor(StandardColors.Black);
                  begin.setGradientColor(StandardColors.Black);
              });
            }
            final CDHRule begin = (CDHRule) doc.getRichTextItem("Body").get(0);
            assertEquals(StandardColors.Black, begin.getColor().get());
            assertEquals(StandardColors.Black, begin.getGradientColor().get());
            assertEquals(0, begin.getColorRaw());
            assertEquals(0, begin.getGradientColorRaw());
         });
    }
    @Test
    public void testsetGradientColorRaw() throws Exception{
    	this.withTempDb(database -> {
            final Document doc = database.createDocument();
            try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
              rtWriter.addRichTextRecord(CDHRule.class, begin -> {
            	  begin.setColorRaw(0);
                  begin.setGradientColorRaw(0);
              });
            }
            final CDHRule begin = (CDHRule) doc.getRichTextItem("Body").get(0);
            assertEquals(0, begin.getColorRaw());
            assertEquals(0, begin.getGradientColorRaw());
            assertEquals(StandardColors.Black, begin.getColor().get());
            assertEquals(StandardColors.Black, begin.getGradientColor().get());
         });
    } 

    @Test
    public void testVMButtonDefaults() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          final ViewmapButtonDefaults begin =rtWriter.createStructure(ViewmapButtonDefaults.class, 0);           
            begin.setLineColor(207);
            begin.setFillFGColor(15);
            begin.setFillBGColor(15);
            begin.setLineStyle(0);
            begin.setLineWidth(1);
            begin.setFillStyle(1);

            assertEquals(207, begin.getLineColor());
            assertEquals(15,begin.getFillFGColor());
            assertEquals(15,begin.getFillBGColor());
            assertEquals(0,begin.getLineStyle());
            assertEquals(1,begin.getLineWidth());
            assertEquals(1,begin.getFillStyle());
        }
        
      });
    }

    @Test
    public void testVMHeaderRecord() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(ViewmapHeaderRecord.class, begin -> {
        	  begin.setVersion(8);
              begin.setNameLen(0);
          });
        }

        final ViewmapHeaderRecord begin = (ViewmapHeaderRecord) doc.getRichTextItem("Body", Area.TYPE_VIEWMAP).get(0);
        assertEquals(8, begin.getVersion());
        assertEquals(0, begin.getNameLen());
      });
    }

    public void testViewmapActionRecord() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        final String actionName = "the action name";
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(ViewmapActionRecord.class, begin -> {
            begin.setHighlightTouch(1);
            begin.setHighlightCurrent(2);
            begin.setOutlineColorRaw(3);
            begin.setHighlightFillColorRaw(4);
            begin.setClickAction(ViewmapActionRecord.Action.GOTO_LINK);
            //begin.setActionStringLen(6); // this is set when calling setActionName()
            begin.setHighlightOutlineWidth(7);
            begin.setHighlightOutlineStyle(NavigatorLineStyle.SOLID);
            begin.getLinkInfo().setDocUnid("B51DB3939AB413C585256D4F00399408");
            begin.getLinkInfo().setReplicaId("0123456701234567");
            begin.getLinkInfo().setViewUnid("B51DB3939AB413C585256D4F00399409");
            begin.setExtDataLen(9);
            begin.setActionDataDesignType(DesignType.PRIVATE);
            begin.setActionString(actionName);

          });
        }

        final ViewmapActionRecord var = (ViewmapActionRecord) doc.getRichTextItem("Body", Area.TYPE_VIEWMAP).get(0);
        assertEquals(1, var.getHighlightTouch());
        assertEquals(2, var.getHighlightCurrent());
        assertEquals(3, var.getHighlightOutlineColorRaw());
        assertEquals(4, var.getHighlightFillColorRaw());
        assertEquals(ViewmapActionRecord.Action.GOTO_LINK, var.getClickAction().get());
        assertEquals(actionName.length(), var.getActionStringLen());
        assertEquals(7, var.getHighlightOutlineWidth());
        assertEquals(NavigatorLineStyle.SOLID, var.getHighlightOutlineStyle());
        assertEquals("B51DB3939AB413C585256D4F00399408", var.getLinkInfo().getDocUnid());
        assertEquals("0123456701234567", var.getLinkInfo().getReplicaId());
        assertEquals("B51DB3939AB413C585256D4F00399409", var.getLinkInfo().getViewUnid());
        assertEquals(9, var.getExtDataLen());
        assertEquals(DesignType.PRIVATE, var.getActionDataDesignType());
        assertEquals(actionName, var.getActionString());
      });
    }
    
    @Test
    public void testHtmlHeader() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDHtmlHeader.class, begin -> {
              begin.setSegments(20);
          });
        }

        final CDHtmlHeader begin = (CDHtmlHeader) doc.getRichTextItem("Body").get(0);
        assertEquals(20, begin.getSegments());
      });
    }
    
    @Test
    public void testHtmlSegment() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDHtmlSegment.class, begin -> {
              begin.setHTML("<html><body>test</body></html>");
          });
        }

        final CDHtmlSegment begin = (CDHtmlSegment) doc.getRichTextItem("Body").get(0);
        String expStr = "<html><body>test</body></html>";
        assertEquals(expStr, begin.getHTML());
        assertEquals(expStr.length(), begin.getHTMLLength());
      });
    }
    
    @Test
    public void testIgnore() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDIgnore.class, begin -> {
              begin.setFlags(EnumSet.of(CDIgnore.Flag.CDIGNORE_BEGIN));
              begin.setNotesVersion(CDIgnore.NotesVersion.NOTES_VERSION_6_0_0);
          });
        }

        final CDIgnore begin = (CDIgnore) doc.getRichTextItem("Body").get(0);
        assertEquals(EnumSet.of(CDIgnore.Flag.CDIGNORE_BEGIN), begin.getFlags());
        assertEquals(CDIgnore.NotesVersion.NOTES_VERSION_6_0_0, begin.getNotesVersion());
      });
    }
    
    @Test
    public void testInline() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDInline.class, begin -> {
              begin.setFlags(EnumSet.of(CDInline.Flag.HTML));
          });
        }

        final CDInline begin = (CDInline) doc.getRichTextItem("Body").get(0);
        assertEquals(EnumSet.of(CDInline.Flag.HTML), begin.getFlags());
        assertEquals(0, begin.getDatalength());
      });
    }
    
    @Test
    public void testStorageLink() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDStorageLink.class, begin -> {
              begin.setStorageType(CDStorageLink.StorageType.OBJECT);
              begin.setObject(doc.getUNID());
          });
        }

        final CDStorageLink begin = (CDStorageLink) doc.getRichTextItem("Body").get(0);
        assertEquals(CDStorageLink.StorageType.OBJECT, begin.getStorageType());
        assertEquals(doc.getUNID(), begin.getObject().get());
      });
    }
    
    @Test
    @Disabled("CDLSOBJECT_R6 does not have a documented SIG_CD value")
    public void testLSObjectR6() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(CDLSObjectR6.class, begin -> {
              begin.setFlags(CDLSObjectR6.Flag.LSOBJECT_R6_TYPE);
          });
        }

        final CDLSObjectR6 begin = (CDLSObjectR6) doc.getRichTextItem("Body").get(0);
        assertEquals(CDLSObjectR6.Flag.LSOBJECT_R6_TYPE, begin.getFlags());
      });
    }
    
    @Test
    public void testViewmapRegionRecord() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        Collection <ViewmapDrawingObject.Flag> expectedFlags = new HashSet <ViewmapDrawingObject.Flag> ();
        expectedFlags.add(ViewmapDrawingObject.Flag.VISIBLE);
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(ViewmapRegionRecord.class, begin -> {
            begin.setFillStyle(NavigatorFillStyle.SOLID);
            begin.setLineColor(StandardColors.Aqua);
            begin.setLineStyle(NavigatorLineStyle.SOLID);
            begin.setLineWidth(1);
            begin.getDrawingObject().setAlignment((short)0);
            begin.getDrawingObject().setbWrap((short)0);
            begin.getDrawingObject().setLabelLen(0);
            begin.getDrawingObject().setNameLen(8);
            begin.getDrawingObject().setTextColor(StandardColors.Blue);
            begin.getDrawingObject().setFlags(expectedFlags);
            begin.getDrawingObject().getObjRect().setBottom(307);
            begin.getDrawingObject().getObjRect().setLeft(13);
            begin.getDrawingObject().getObjRect().setRight(94);
            begin.getDrawingObject().getObjRect().setTop(237);
            begin.getDrawingObject().getFontID().setPointSize(10);
          });
        }
        final ViewmapRegionRecord begin = (ViewmapRegionRecord) doc.getRichTextItem("Body", Area.TYPE_VIEWMAP).get(0);
        assertEquals(NavigatorFillStyle.SOLID, begin.getFillStyle());
        assertEquals(StandardColors.Aqua, begin.getLineColor().get());
        assertEquals(NavigatorLineStyle.SOLID, begin.getLineStyle());
        assertEquals(1, begin.getLineWidth());
        assertEquals(0, begin.getDrawingObject().getAlignment());
        assertEquals(0, begin.getDrawingObject().getbWrap());
        assertEquals(0, begin.getDrawingObject().getLabelLen());
        assertEquals(8, begin.getDrawingObject().getNameLen());
        assertEquals(StandardColors.Blue, begin.getDrawingObject().getTextColor().get());
        assertEquals(expectedFlags, begin.getDrawingObject().getFlags());
        assertEquals(307, begin.getDrawingObject().getObjRect().getBottom());
        assertEquals(13, begin.getDrawingObject().getObjRect().getLeft());
        assertEquals(94, begin.getDrawingObject().getObjRect().getRight());
        assertEquals(237, begin.getDrawingObject().getObjRect().getTop());
        assertEquals(10, begin.getDrawingObject().getFontID().getPointSize());
      });
      }
    
    public void testViewmapHighlightDefaults() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          final ViewmapHighlightDefaults viewmapHighlightDefaults =rtWriter.createStructure(ViewmapHighlightDefaults.class, 0);
          viewmapHighlightDefaults.setbHighlightCurrent(0);
          viewmapHighlightDefaults.setbHighlightTouch(0);
          viewmapHighlightDefaults.setHLFillColor(65535);
          viewmapHighlightDefaults.setHLOutlineColor(2);
          viewmapHighlightDefaults.setHLOutlineStyle(0);
          viewmapHighlightDefaults.setHLOutlineWidth(2);   
          assertEquals(0,viewmapHighlightDefaults.getbHighlightCurrent());
          assertEquals(0,viewmapHighlightDefaults.getbHighlightTouch());
          assertEquals(65535,viewmapHighlightDefaults.getHLFillColor());
          assertEquals(2,viewmapHighlightDefaults.getHLOutlineColor());
          assertEquals(0,viewmapHighlightDefaults.getHLOutlineStyle());
          assertEquals(2,viewmapHighlightDefaults.getHLOutlineWidth());
        }

        
      });
    }
    
    @Test
    public void testViewmapLineDefaults() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          final ViewmapLineDefaults viewmapLineDefaults =rtWriter.createStructure(ViewmapLineDefaults.class, 0);
          viewmapLineDefaults.setFillFGColor(0);
          viewmapLineDefaults.setLineColor(0);
          viewmapLineDefaults.setFillStyle(0);
          viewmapLineDefaults.setFillBGColor(0);
          viewmapLineDefaults.setLineWidth(1);
          viewmapLineDefaults.setLineStyle(0);
          assertEquals(0,viewmapLineDefaults.getFillFGColor());
          assertEquals(0,viewmapLineDefaults.getLineColor());
          assertEquals(0,viewmapLineDefaults.getFillStyle());
          assertEquals(0,viewmapLineDefaults.getFillBGColor());
          assertEquals(1,viewmapLineDefaults.getLineWidth());
          assertEquals(0,viewmapLineDefaults.getLineStyle());
        } 
      });
    }
    
    @Test
    public void testViewmapTextboxDefaults() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          final ViewmapTextboxDefaults viewmapTextboxDefaults =rtWriter.createStructure(ViewmapTextboxDefaults.class, 0);
          viewmapTextboxDefaults.setFillFGColor(1);
          viewmapTextboxDefaults.setLineColor(0);
          viewmapTextboxDefaults.setFillStyle(0);
          viewmapTextboxDefaults.setFillBGColor(1);
          viewmapTextboxDefaults.setLineWidth(1);
          viewmapTextboxDefaults.setLineStyle(5);
          assertEquals(1,viewmapTextboxDefaults.getFillFGColor());
          assertEquals(0,viewmapTextboxDefaults.getLineColor());
          assertEquals(0,viewmapTextboxDefaults.getFillStyle());
          assertEquals(1,viewmapTextboxDefaults.getFillBGColor());
          assertEquals(1,viewmapTextboxDefaults.getLineWidth());
          assertEquals(5,viewmapTextboxDefaults.getLineStyle());
        } 
      });
    }
    @Test
    public void testViewmapShapeDefaults() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          final ViewmapShapeDefaults viewmapShapeDefaults =rtWriter.createStructure(ViewmapShapeDefaults.class, 0);
          viewmapShapeDefaults.setFillFGColor(7);
          viewmapShapeDefaults.setLineColor(0);
          viewmapShapeDefaults.setFillStyle(1);
          viewmapShapeDefaults.setFillBGColor(7);
          viewmapShapeDefaults.setLineWidth(1);
          viewmapShapeDefaults.setLineStyle(0);
          assertEquals(7,viewmapShapeDefaults.getFillFGColor());
          assertEquals(0,viewmapShapeDefaults.getLineColor());
          assertEquals(1,viewmapShapeDefaults.getFillStyle());
          assertEquals(7,viewmapShapeDefaults.getFillBGColor());
          assertEquals(1,viewmapShapeDefaults.getLineWidth());
          assertEquals(0,viewmapShapeDefaults.getLineStyle());
        } 
      });
    }
 
    @Test
    public void testVMTextRecord() throws Exception {
      this.withTempDb(database -> {
        final Document doc = database.createDocument();
        try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
          rtWriter.addRichTextRecord(ViewmapTextRecord.class, begin -> {
          	begin.setLineColor(StandardColors.Aubergine);
          	begin.setFillForegroundColor(StandardColors.AztecBlue);
          	begin.setFillBackgroundColor(StandardColors.DarkOlive);
          	begin.setLineStyle(NavigatorLineStyle.SOLID);
          	begin.setLineWidth(1);
          	begin.setFillStyle(NavigatorFillStyle.TRANSPARENT);
          	begin.getDrawingObject().setTextColor(StandardColors.Blue);
          	begin.getDrawingObject().setAlignment(0);
          	begin.getDrawingObject().setbWrap(0);
          	begin.getDrawingObject().setLabelLen(9);
          	begin.getDrawingObject().setNameLen(8);
          	begin.getDrawingObject().getFontID().setFontFace((byte) 1);
          	begin.getDrawingObject().getFontID().setBold(true);
          	begin.getDrawingObject().getFontID().setColor(StandardColors.Aqua);
          	begin.getDrawingObject().getFontID().setExtrude(true);
          	begin.getDrawingObject().getFontID().setFontFace((byte) 1);
          	begin.getDrawingObject().getFontID().setItalic(true);
          	begin.getDrawingObject().getFontID().setPointSize(1);
          	begin.getDrawingObject().getFontID().setShadow(true);
          	begin.getDrawingObject().getFontID().setStandardFont(StandardFonts.SWISS);
          	begin.getDrawingObject().getFontID().setStrikeout(true);
          	begin.getDrawingObject().getFontID().setSub(true);
          	begin.getDrawingObject().getFontID().setSuper(true);
          	begin.getDrawingObject().getFontID().setUnderline(true);
        	       	
          });
        }

        final ViewmapTextRecord begin = (ViewmapTextRecord) doc.getRichTextItem("Body", Area.TYPE_VIEWMAP).get(0);
        assertEquals(StandardColors.Aubergine, begin.getLineColor().get());
        assertEquals(StandardColors.AztecBlue, begin.getFillForegroundColor().get());
        assertEquals(StandardColors.DarkOlive, begin.getFillBackgroundColor().get());
        assertEquals(NavigatorLineStyle.SOLID, begin.getLineStyle());
        assertEquals(1, begin.getLineWidth());
        assertEquals(NavigatorFillStyle.TRANSPARENT, begin.getFillStyle());
        assertEquals(StandardColors.Blue, begin.getDrawingObject().getTextColor().get());
        assertEquals(0, begin.getDrawingObject().getAlignment());
        assertEquals(0, begin.getDrawingObject().getbWrap());
        assertEquals(9, begin.getDrawingObject().getLabelLen());
        assertEquals(8, begin.getDrawingObject().getNameLen());
        assertEquals(1, begin.getDrawingObject().getFontID().getFontFace());
        assertEquals(true, begin.getDrawingObject().getFontID().isBold());
        assertEquals(StandardColors.Aqua,begin.getDrawingObject().getFontID().getColor().get());
        assertEquals(true, begin.getDrawingObject().getFontID().isExtrude());
        assertEquals(1,begin.getDrawingObject().getFontID().getFontFace());
        assertEquals(true, begin.getDrawingObject().getFontID().isItalic());
        assertEquals(1, begin.getDrawingObject().getFontID().getPointSize());
        assertEquals(true, begin.getDrawingObject().getFontID().isShadow());
        assertEquals(StandardFonts.SWISS, begin.getDrawingObject().getFontID().getStandardFont().get());
        assertEquals(true, begin.getDrawingObject().getFontID().isStrikeout());
        assertEquals(true, begin.getDrawingObject().getFontID().isSub());
        assertEquals(true, begin.getDrawingObject().getFontID().isSuper());
        assertEquals(true, begin.getDrawingObject().getFontID().isUnderline());
      });
    }
}
