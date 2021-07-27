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
import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DateTimeFlag;
import com.hcl.domino.design.format.DateTimeFlag2;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NumberPref;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
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
import com.hcl.domino.richtext.records.CDExt2Field.CurrencyFlag;
import com.hcl.domino.richtext.records.CDExt2Field.CurrencyType;
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
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.AssistFieldStruct;
import com.hcl.domino.richtext.structures.AssistFieldStruct.ActionByField;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.FontStyle.StandardColors;
import com.hcl.domino.richtext.structures.FontStyle.StandardFonts;
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
          actionBar.getFontStyle().setFontFace(StandardFonts.TYPEWRITER);
          actionBar.setButtonHeight(3);
          actionBar.setHeightSpacing(4);
        });
        rtWriter.addRichTextRecord(CDActionBarExt.class, actionBarExt -> {
          actionBarExt.getBackgroundColor().setBlue((short) 1);
          actionBarExt.getLineColor().setBlue((short) 2);
          actionBarExt.getFontColor().setBlue((short) 3);
          actionBarExt.getButtonColor().setBlue((short) 4);
          actionBarExt.setBorderDisplay(CDActionBarExt.BorderDisplay.NOTES);
          actionBarExt.setAppletHeight(30);
          actionBarExt.setBackgroundRepeat(CDActionBarExt.BackgroundRepeat.REPEATHORIZ);
          actionBarExt.setWidthStyle(CDActionBarExt.WidthStyle.BACKGROUND);
          actionBarExt.setTextJustify(CDActionBarExt.TextJustify.RIGHT);
          actionBarExt.setButtonWidth(31);
          actionBarExt.setButtonInternalMargin(32);
          actionBarExt.setFlags(EnumSet.of(CDActionBarExt.Flag.WIDTH_STYLE_VALID));
          actionBarExt.getFontStyle().setFontFace(StandardFonts.UNICODE);
          actionBarExt.getHeight().setUnit(LengthValue.Unit.EMS);
          actionBarExt.getHeight().setLength(5.5);
        });
      }

      final RichTextRecordList body = doc.getRichTextItem("Body");
      final CDActionBar actionBar = (CDActionBar) body.get(0);
      Assertions.assertEquals(1, actionBar.getBackgroundColor());
      Assertions.assertEquals(2, actionBar.getLineColor());
      Assertions.assertEquals(CDActionBar.LineStyle.TRIPLE, actionBar.getLineStyle());
      Assertions.assertEquals(CDActionBar.BorderStyle.ABS, actionBar.getBorderStyle());
      Assertions.assertEquals(EnumSet.of(CDActionBar.Flag.ABSOLUTE_HEIGHT, CDActionBar.Flag.BTNBCK_IMGRSRC), actionBar.getFlags());
      Assertions.assertEquals(17, actionBar.getShareId());
      Assertions.assertEquals(3, actionBar.getButtonHeight());
      Assertions.assertEquals(4, actionBar.getHeightSpacing());

      final CDActionBarExt actionBarExt = (CDActionBarExt) body.get(1);
      Assertions.assertEquals(1, actionBarExt.getBackgroundColor().getBlue());
      Assertions.assertEquals(2, actionBarExt.getLineColor().getBlue());
      Assertions.assertEquals(3, actionBarExt.getFontColor().getBlue());
      Assertions.assertEquals(4, actionBarExt.getButtonColor().getBlue());
      Assertions.assertEquals(CDActionBarExt.BorderDisplay.NOTES, actionBarExt.getBorderDisplay());
      Assertions.assertEquals(30, actionBarExt.getAppletHeight());
      Assertions.assertEquals(CDActionBarExt.BackgroundRepeat.REPEATHORIZ, actionBarExt.getBackgroundRepeat());
      Assertions.assertEquals(CDActionBarExt.WidthStyle.BACKGROUND, actionBarExt.getWidthStyle());
      Assertions.assertEquals(CDActionBarExt.TextJustify.RIGHT, actionBarExt.getTextJustify());
      Assertions.assertEquals(31, actionBarExt.getButtonWidth());
      Assertions.assertEquals(32, actionBarExt.getButtonInternalMargin());
      Assertions.assertEquals(EnumSet.of(CDActionBarExt.Flag.WIDTH_STYLE_VALID), actionBarExt.getFlags());
      Assertions.assertEquals(StandardFonts.UNICODE, actionBarExt.getFontStyle().getFontFace());
      Assertions.assertEquals(LengthValue.Unit.EMS, actionBarExt.getHeight().getUnit());
      Assertions.assertEquals(5.5, actionBarExt.getHeight().getLength());
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
      Assertions.assertEquals(CDAction.Type.RUN_FORMULA, action.getActionType());
      Assertions.assertEquals(EnumSet.of(CDAction.Flag.ALIGN_ICON_RIGHT, CDAction.Flag.HIDE_FROM_MOBILE), action.getFlags());
      Assertions.assertEquals("Hello there", action.getTitle());
      Assertions.assertEquals("Index>1", action.getHideWhenFormula());
      Assertions.assertEquals("@Command([AdminRemoteConsole])", action.getActionFormula());
      Assertions.assertThrows(UnsupportedOperationException.class, action::getActionLotusScript);
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
      Assertions.assertEquals(CDAction.Type.RUN_SCRIPT, action.getActionType());
      Assertions.assertEquals(EnumSet.of(CDAction.Flag.ALIGN_ICON_RIGHT, CDAction.Flag.HIDE_FROM_MOBILE), action.getFlags());
      Assertions.assertEquals("Hello there from LotusScript", action.getTitle());
      Assertions.assertEquals("Index>1", action.getHideWhenFormula());
      Assertions.assertEquals("MsgBox | I am a bad language |", action.getActionLotusScript());
      Assertions.assertThrows(UnsupportedOperationException.class, action::getActionFormula);
    });
  }

  @Test
  public void testAssistFieldStruct() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        final AssistFieldStruct assistField = rtWriter.createStructure(AssistFieldStruct.class, 0);
        assistField.setOperatorRaw((short) 2);
        Assertions.assertEquals(AssistFieldStruct.ActionByField.APPEND, assistField.getActionOperator());
        Assertions.assertEquals(AssistFieldStruct.QueryByField.LESS, assistField.getQueryOperator());

        assistField.setActionOperator(AssistFieldStruct.ActionByField.REMOVE);
        Assertions.assertEquals(AssistFieldStruct.ActionByField.REMOVE, assistField.getActionOperator());
        assistField.setQueryOperator(AssistFieldStruct.QueryByField.DOESNOTCONTAIN);
        Assertions.assertEquals(AssistFieldStruct.QueryByField.DOESNOTCONTAIN, assistField.getQueryOperator());

        assistField.setFieldName("foo");
        Assertions.assertEquals("foo", assistField.getFieldName());
        assistField.setValues(Arrays.asList("bar", "baz", "ness"));
        Assertions.assertEquals("foo", assistField.getFieldName());
        Assertions.assertEquals(Arrays.asList("bar", "baz", "ness"), assistField.getValues());
      }
    });
  }

  @Test
  public void testCaptionText() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDHotspotBegin.class, begin -> {
          begin.setFileNames("foo.txt", "bar.txt");
        });
      }

      final CDHotspotBegin begin = (CDHotspotBegin) doc.getRichTextItem("Body").get(0);
      Assertions.assertEquals("foo.txt", begin.getUniqueFileName());
      Assertions.assertEquals("bar.txt", begin.getDisplayFileName());
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
      Assertions.assertEquals(2, actionByForm.getFieldCount());
      Assertions.assertEquals("FakeForm", actionByForm.getFormName());

      final List<AssistFieldStruct> assistFields = actionByForm.getAssistFields();
      Assertions.assertNotNull(assistFields);
      Assertions.assertEquals(2, assistFields.size());
      Assertions.assertEquals("Foo", assistFields.get(0).getFieldName());
      Assertions.assertEquals(ActionByField.REPLACE, assistFields.get(0).getActionOperator());
      Assertions.assertEquals("Bar", assistFields.get(1).getFieldName());
      Assertions.assertEquals(ActionByField.APPEND, assistFields.get(1).getActionOperator());
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
      Assertions.assertEquals((short) 1, color.getColor().getRed());
      Assertions.assertEquals((short) 2, color.getColor().getGreen());
      Assertions.assertEquals((short) 3, color.getColor().getBlue());
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
      Assertions.assertEquals(4, flags.getFlagCount());
      Assertions.assertArrayEquals(new int[] { 0x00000000, 0x000010000, 0x10000000, 0xFFFFFFFF }, flags.getFlags());
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
      Assertions.assertEquals(CDEmbeddedControl.Type.COMBO, control.getControlType());
      Assertions.assertEquals(EnumSet.of(CDEmbeddedControl.Flag.DIALOGUNITS, CDEmbeddedControl.Flag.POSITION_HEIGHT),
          control.getFlags());
      Assertions.assertEquals(480, control.getHeight());
      Assertions.assertEquals(640, control.getWidth());
      Assertions.assertTrue(control.getStyle().contains(CDEmbeddedControl.Style.EDITCOMBO));
      Assertions.assertEquals(CDEmbeddedControl.Version.VERSION1, control.getVersion());
      Assertions.assertEquals(80, control.getPercentage());
      Assertions.assertEquals(255, control.getMaxChars());
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
          field.setDateShowSpecial(DateShowSpecial.TWO_CURRENT_FOUR_OTHER);
          field.setTimeShowFormat(TimeShowFormat.HM);
          field.setFormatFlags(EnumSet.of(CDExt2Field.FormatFlag.PROPORTIONAL));
          field.setProportionalWidthCharacters(16);
          field.setInputEnabledFormula("@False + @True");
          field.setIMGroupFormula("\"some group\"");
        });
      }

      final CDExt2Field field = (CDExt2Field) doc.getRichTextItem("Body").get(0);
      Assertions.assertEquals(NumberPref.FIELD, field.getNumberSymbolPreference());
      Assertions.assertEquals("..", field.getDecimalSymbol());
      Assertions.assertEquals(",,", field.getMilliSeparator());
      Assertions.assertEquals("--", field.getNegativeSymbol());
      Assertions.assertEquals(4, field.getMilliGroupSize());
      Assertions.assertEquals((short) -3, field.getVerticalSpacing());
      Assertions.assertEquals((short) -4, field.getHorizontalSpacing());
      Assertions.assertEquals(NumberPref.FIELD, field.getCurrencyPreference());
      Assertions.assertEquals(CurrencyType.CUSTOM, field.getCurrencyType());
      Assertions.assertEquals(EnumSet.of(CurrencyFlag.SYMFOLLOWS), field.getCurrencyFlags());
      Assertions.assertEquals("$$", field.getCurrencySymbol());
      Assertions.assertEquals(4, field.getISOCountry());
      Assertions.assertEquals(640, field.getThumbnailImageWidth());
      Assertions.assertEquals(480, field.getThumbnailImageHeight());
      Assertions.assertEquals("foo.png", field.getThumbnailImageFileName());
      Assertions.assertEquals("\"foo fooson\"", field.getIMOnlineNameFormula());
      Assertions.assertEquals(NumberPref.FIELD, field.getDateTimePreference());
      Assertions.assertEquals(EnumSet.of(DateTimeFlag.SHOWDATE, DateTimeFlag.SHOWTIME), field.getDateTimeFlags());
      Assertions.assertEquals(EnumSet.of(DateTimeFlag2.USE_TFMT), field.getDateTimeFlags2());
      Assertions.assertEquals(WeekFormat.WWWW, field.getDayOfWeekFormat());
      Assertions.assertEquals(YearFormat.GGEE, field.getYearFormat());
      Assertions.assertEquals(MonthFormat.MMM, field.getMonthFormat());
      Assertions.assertEquals(DayFormat.DD, field.getDayFormat());
      Assertions.assertEquals("//", field.getDateSeparator1());
      Assertions.assertEquals("\\\\", field.getDateSeparator2());
      Assertions.assertEquals("__", field.getDateSeparator3());
      Assertions.assertEquals("??", field.getTimeSeparator());
      Assertions.assertEquals(DateShowFormat.MDY, field.getDateShowFormat());
      Assertions.assertEquals(DateShowSpecial.TWO_CURRENT_FOUR_OTHER, field.getDateShowSpecial());
      Assertions.assertEquals(TimeShowFormat.HM, field.getTimeShowFormat());
      Assertions.assertEquals(EnumSet.of(CDExt2Field.FormatFlag.PROPORTIONAL), field.getFormatFlags());
      Assertions.assertEquals(16, field.getProportionalWidthCharacters());
      Assertions.assertEquals("@False + @True", field.getInputEnabledFormula());
      Assertions.assertEquals("\"some group\"", field.getIMGroupFormula());
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
      Assertions.assertEquals(EnumSet.of(CDExtField.Flag.KWHINKYMINKY, CDExtField.Flag.KEYWORD_FRAME_3D), extField.getFlags1());
      Assertions.assertEquals(EnumSet.of(CDExtField.Flag2.CONTROL, CDExtField.Flag2.ALLOWTABBINGOUT), extField.getFlags2());
      Assertions.assertEquals(HelperType.VIEWDLG, extField.getHelperType());
      Assertions.assertEquals("foo.nsf", extField.getEntryDBName());
      Assertions.assertEquals("Some view", extField.getEntryViewName());
      Assertions.assertEquals(6, extField.getEntryColumnNumber());
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
          field.getTimeFormat().setZoneFormat(TFMT.ZoneFormat.SOMETIMES);

          field.setDefaultValueFormula("\"hi\"");
          field.setInputTranslationFormula("@ThisValue+\"hi\"");
          field.setInputValidationFormula("@If(a; @Success; @Failure(\"nooo\"))");
          field.setDescription("Test Description");
          field.setTextValues(Arrays.asList("foo", "bar"));
          field.setName("TextField");
        });
      }

      final CDField field = (CDField) doc.getRichTextItem("Body").get(0);
      Assertions.assertEquals(ItemDataType.TYPE_TEXT, field.getFieldType());
      // KEYWORDS_UI_STANDARD is 0x0000 and thus always shows up
      Assertions.assertEquals(EnumSet.of(CDField.Flag.COMPUTED, CDField.Flag.PROTECTED, CDField.Flag.KEYWORDS_UI_STANDARD),
          field.getFlags());
      Assertions.assertEquals(FieldListDisplayDelimiter.NEWLINE, field.getListDisplayDelimiter());
      Assertions.assertEquals(EnumSet.of(FieldListDelimiter.NEWLINE, FieldListDelimiter.SEMICOLON), field.getListDelimiters());

      final NFMT numberFormat = field.getNumberFormat();
      Assertions.assertEquals(EnumSet.of(NFMT.Attribute.PARENS), numberFormat.getAttributes());
      Assertions.assertEquals((short) 4, numberFormat.getDigits());
      Assertions.assertEquals(NFMT.Format.CURRENCY, numberFormat.getFormat());

      final TFMT timeFormat = field.getTimeFormat();
      Assertions.assertEquals(TFMT.DateFormat.CPARTIAL4, timeFormat.getDateFormat());
      Assertions.assertEquals(TFMT.TimeFormat.HOUR, timeFormat.getTimeFormat());
      Assertions.assertEquals(TFMT.TimeStructure.CDATETIME, timeFormat.getTimeStructure());
      Assertions.assertEquals(TFMT.ZoneFormat.SOMETIMES, timeFormat.getZoneFormat());

      Assertions.assertEquals("\"hi\"", field.getDefaultValueFormula());
      Assertions.assertEquals("@ThisValue+\"hi\"", field.getInputTranslationFormula());
      Assertions.assertEquals("@If(a; @Success; @Failure(\"nooo\"))", field.getInputValidationFormula());
      Assertions.assertEquals("Test Description", field.getDescription());
      Assertions.assertEquals(Arrays.asList("foo", "bar"), field.getTextValues().get());
      Assertions.assertFalse(field.getTextValueFormula().isPresent());
      Assertions.assertEquals("TextField", field.getName());
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
          field.getTimeFormat().setZoneFormat(TFMT.ZoneFormat.SOMETIMES);

          field.setDefaultValueFormula("\"hi\"");
          field.setInputTranslationFormula("@ThisValue+\"hi\"");
          field.setInputValidationFormula("@If(a; @Success; @Failure(\"nooo\"))");
          field.setDescription("Test Description");
          field.setTextValueFormula("\"foo\":\"bar\"");
          field.setName("TextField");
        });
      }

      final CDField field = (CDField) doc.getRichTextItem("Body").get(0);
      Assertions.assertEquals(ItemDataType.TYPE_TEXT, field.getFieldType());
      // KEYWORDS_UI_STANDARD is 0x0000 and thus always shows up
      Assertions.assertEquals(EnumSet.of(CDField.Flag.COMPUTED, CDField.Flag.PROTECTED, CDField.Flag.KEYWORDS_UI_STANDARD),
          field.getFlags());
      Assertions.assertEquals(FieldListDisplayDelimiter.NEWLINE, field.getListDisplayDelimiter());
      Assertions.assertEquals(EnumSet.of(FieldListDelimiter.NEWLINE, FieldListDelimiter.SEMICOLON), field.getListDelimiters());

      final NFMT numberFormat = field.getNumberFormat();
      Assertions.assertEquals(EnumSet.of(NFMT.Attribute.PARENS), numberFormat.getAttributes());
      Assertions.assertEquals((short) 4, numberFormat.getDigits());
      Assertions.assertEquals(NFMT.Format.CURRENCY, numberFormat.getFormat());

      final TFMT timeFormat = field.getTimeFormat();
      Assertions.assertEquals(TFMT.DateFormat.CPARTIAL4, timeFormat.getDateFormat());
      Assertions.assertEquals(TFMT.TimeFormat.HOUR, timeFormat.getTimeFormat());
      Assertions.assertEquals(TFMT.TimeStructure.CDATETIME, timeFormat.getTimeStructure());
      Assertions.assertEquals(TFMT.ZoneFormat.SOMETIMES, timeFormat.getZoneFormat());

      Assertions.assertEquals("\"hi\"", field.getDefaultValueFormula());
      Assertions.assertEquals("@ThisValue+\"hi\"", field.getInputTranslationFormula());
      Assertions.assertEquals("@If(a; @Success; @Failure(\"nooo\"))", field.getInputValidationFormula());
      Assertions.assertEquals("Test Description", field.getDescription());
      Assertions.assertFalse(field.getTextValues().isPresent());
      Assertions.assertEquals("\"foo\":\"bar\"", field.getTextValueFormula().get());
      Assertions.assertEquals("TextField", field.getName());
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
      Assertions.assertEquals(EnumSet.noneOf(CDFieldHint.Flag.class), fieldHint.getFlags());
      Assertions.assertEquals("foo bar", fieldHint.getHintText());
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
      Assertions.assertEquals(EnumSet.of(CDFieldHint.Flag.LIMITED), fieldHint.getFlags());
      Assertions.assertEquals("pictures", fieldHint.getHintText());
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
      Assertions.assertEquals("foo", name.getID());
      Assertions.assertEquals("bar baz", name.getClassName());
      Assertions.assertEquals("border-left: 0", name.getStyle());
      Assertions.assertEquals("I am a title", name.getTitle());
      Assertions.assertEquals("border=\"0\"", name.getHTMLAttributes());
      Assertions.assertEquals("foo-name", name.getName());
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
      Assertions.assertEquals(EnumSet.of(CDKeyword.Flag.FRAME_3D, CDKeyword.Flag.KEYWORD_RTL), keyword.getFlags());
      Assertions.assertEquals(3, keyword.getKeywordCount());
      Assertions.assertEquals(Arrays.asList("foo", "bar", "baz"), keyword.getKeywords());
      Assertions.assertEquals(Arrays.asList(true, false, true), keyword.getEnabledStates());
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
          text.getStyle().setFontFace(StandardFonts.TYPEWRITER);
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
      Assertions.assertEquals(3, body.size());

      {
        Assertions.assertInstanceOf(CDParagraph.class, body.get(0), "Unexpected first record " + body.get(0));
        Assertions.assertEquals(2, body.get(0).getHeader().getLength().intValue());
        Assertions.assertEquals(2, body.get(0).getData().capacity());
      }
      {
        Assertions.assertInstanceOf(CDText.class, body.get(1));
        final CDText text = (CDText) body.get(1);
        Assertions.assertEquals(8, body.get(1).getHeader().getLength().intValue());
        Assertions.assertEquals(8, body.get(1).getData().capacity());
        Assertions.assertTrue(text.getStyle().isBold());
        Assertions.assertEquals(StandardFonts.TYPEWRITER, text.getStyle().getFontFace());
      }
      {
        Assertions.assertInstanceOf(CDImageHeader.class, body.get(2));
        final CDImageHeader header = (CDImageHeader) body.get(2);
        Assertions.assertEquals(28, header.getData().capacity());
        Assertions.assertEquals(800, header.getWidth());
        Assertions.assertEquals(600, header.getHeight());
        Assertions.assertEquals(2048, header.getImageDataSize());
        Assertions.assertEquals(2, header.getSegCount());
        Assertions.assertEquals(ImageType.PNG, header.getImageType());
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
      Assertions.assertArrayEquals(imageData, imageStream.toByteArray());
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
          Assertions.assertNotNull(fakeArray.getFontStyles());
          Assertions.assertEquals(3, fakeArray.getFontStyles().length);

          fakeArray.getFontStyles()[0].setColor(StandardColors.DKBLUE);
          fakeArray.getFontStyles()[1].setColor(StandardColors.DKMAGENTA);
          fakeArray.getFontStyles()[2].setColor(StandardColors.DKGREEN);

          Assertions.assertEquals(StandardColors.DKBLUE, fakeArray.getFontStyles()[0].getColor());
          Assertions.assertEquals(StandardColors.DKMAGENTA, fakeArray.getFontStyles()[1].getColor());
          Assertions.assertEquals(StandardColors.DKGREEN, fakeArray.getFontStyles()[2].getColor());
        }

        // Test writing an accurately-sized array back
        {
          final FakeArrayStruct fakeArray = rtWriter.createStructure(FakeArrayStruct.class, 0);

          final FontStyle[] styles = {
              rtWriter.createStructure(FontStyle.class, 0)
                  .setColor(StandardColors.CYAN),
              rtWriter.createStructure(FontStyle.class, 0)
                  .setColor(StandardColors.DKYELLOW),
              rtWriter.createStructure(FontStyle.class, 0)
                  .setColor(StandardColors.MAGENTA)
          };
          fakeArray.setFontStyles(styles);

          Assertions.assertEquals(StandardColors.CYAN, fakeArray.getFontStyles()[0].getColor());
          Assertions.assertEquals(StandardColors.DKYELLOW, fakeArray.getFontStyles()[1].getColor());
          Assertions.assertEquals(StandardColors.MAGENTA, fakeArray.getFontStyles()[2].getColor());
        }

        // Test writing incorrectly-sized arrays back
        {
          final FakeArrayStruct fakeArray = rtWriter.createStructure(FakeArrayStruct.class, 0);
          Assertions.assertThrows(IllegalArgumentException.class, () -> fakeArray.setFontStyles(new FontStyle[1]));
          Assertions.assertThrows(IllegalArgumentException.class, () -> fakeArray.setFontStyles(new FontStyle[4]));
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
      Assertions.assertEquals(1, body.size());

      {
        Assertions.assertTrue(body.get(0) instanceof CDBlobPart);
        Assertions.assertArrayEquals(array, ((CDBlobPart) body.get(0)).getReserved());
      }
    });
  }
}
