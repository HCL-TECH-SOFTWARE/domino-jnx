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
package it.com.hcl.domino.test.richtext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
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
import com.hcl.domino.richtext.HotspotType;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDAction;
import com.hcl.domino.richtext.records.CDActionBar;
import com.hcl.domino.richtext.records.CDActionBarExt;
import com.hcl.domino.richtext.records.CDActionByForm;
import com.hcl.domino.richtext.records.CDBlobPart;
import com.hcl.domino.richtext.records.CDColor;
import com.hcl.domino.richtext.records.CDDataFlags;
import com.hcl.domino.richtext.records.CDEmbeddedControl;
import com.hcl.domino.richtext.records.CDExt2Field;
import com.hcl.domino.richtext.records.CDExtField;
import com.hcl.domino.richtext.records.CDExtField.HelperType;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDFieldHint;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.CDImageHeader.ImageType;
import com.hcl.domino.richtext.records.CDImageSegment;
import com.hcl.domino.richtext.records.CDKeyword;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.CurrencyFlag;
import com.hcl.domino.richtext.records.CurrencyType;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.AssistFieldStruct;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.AssistFieldStruct.ActionByField;
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
        Assertions.assertInstanceOf(CDText.class, body.get(1));
        final CDText text = (CDText) body.get(1);
        assertEquals(8, body.get(1).getHeader().getLength().intValue());
        assertEquals(8, body.get(1).getData().capacity());
        assertTrue(text.getStyle().isBold());
        assertEquals(StandardFonts.TYPEWRITER, text.getStyle().getStandardFont().get());
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
}
