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

/**
 * @since 1.0.24
 */
public interface ViewFormatConstants {

  byte VIEW_FORMAT_VERSION = 1;

  // flags1 values of VIEW_TABLE_FORMAT
  /** Default to fully collapsed */
  short VIEW_TABLE_FLAG_COLLAPSED = 0x0001;
  /**
   * Do not index hierarchically. If FALSE, MUST have NSFFormulaSummaryItem($REF)
   * as LAST item!
   */
  short VIEW_TABLE_FLAG_FLATINDEX = 0x0002;
  /** Display unread flags in margin at ALL levels */
  short VIEW_TABLE_FLAG_DISP_ALLUNREAD = 0x0004;
  /**
   * Display replication conflicts. If TRUE, MUST have
   * NSFFormulaSummaryItem($Conflict) as SECOND-TO-LAST item!
   */
  short VIEW_TABLE_FLAG_CONFLICT = 0x0008;
  /** Display unread flags in margin for documents only */
  short VIEW_TABLE_FLAG_DISP_UNREADDOCS = 0x0010;
  /** Position to top when view is opened. */
  short VIEW_TABLE_GOTO_TOP_ON_OPEN = 0x0020;
  /** Position to bottom when view is opened. */
  short VIEW_TABLE_GOTO_BOTTOM_ON_OPEN = 0x0040;
  /** Color alternate rows. */
  short VIEW_TABLE_ALTERNATE_ROW_COLORING = 0x0080;
  /** Hide headings. */
  short VIEW_TABLE_HIDE_HEADINGS = 0x0100;
  /** Hide left margin. */
  short VIEW_TABLE_HIDE_LEFT_MARGIN = 0x0200;
  /** Show simple (background color) headings. */
  short VIEW_TABLE_SIMPLE_HEADINGS = 0x0400;
  /** TRUE if LineCount is variable (can be reduced as needed). */
  short VIEW_TABLE_VARIABLE_LINE_COUNT = 0x0800;
  /*	Refresh flags.
   *
   * When both flags are clear, automatic refresh of display on update notification is disabled.
   * In this case, the refresh indicator will be displayed.
   *
   * When VIEW_TABLE_GOTO_TOP_ON_REFRESH is set, the view will fe refreshed from the top row of
   * the collection (as if the user pressed F9 and Ctrl-Home).
   *
   * When VIEW_TABLE_GOTO_BOTTOM_ON_REFRESH is set, the view will be refreshed so the bottom row of
   * the collection is visible (as if the user pressed F9 and Ctrl-End).
   *
   * When BOTH flags are set (done to avoid using another bit in the flags), the view will be
   * refreshed from the current top row (as if the user pressed F9). */

  /** Position to top when view is refreshed. */
  short VIEW_TABLE_GOTO_TOP_ON_REFRESH = 0x1000;
  /** Position to bottom when view is refreshed. */
  short VIEW_TABLE_GOTO_BOTTOM_ON_REFRESH = 0x2000;

  /** TRUE if last column should be extended to fit the window width. */
  short VIEW_TABLE_EXTEND_LAST_COLUMN = 0x4000;
  /** TRUE if the View indexing should work from the Right most column */
  short VIEW_TABLE_RTLVIEW = (short) (0x8000 & 0xffff);

  // flags2 values of VIEW_TABLE_FORMAT

  /** TRUE if we should display no-borders at all on the header */
  short VIEW_TABLE_FLAT_HEADINGS = 0x0001;
  /** TRUE if the icons displayed inthe view should be colorized */
  short VIEW_TABLE_COLORIZE_ICONS = 0x0002;
  /** TRUE if we should not display a search bar for this view */
  short VIEW_TABLE_HIDE_SB = 0x0004;
  /** TRUE if we should hide the calendar header */
  short VIEW_TABLE_HIDE_CAL_HEADER = 0x0008;
  /** TRUE if view has not been customized (i.e. not saved by Designer) */
  short VIEW_TABLE_NOT_CUSTOMIZED = 0x0010;
  /** TRUE if view supports display of partial thread hierarchy (Hannover v8) */
  short VIEW_TABLE_SHOW_PARITAL_THREADS = 0x0020;
  /** show partial index hierarchically, if TRUE */
  short VIEW_TABLE_FLAG_PARTIAL_FLATINDEX = 0x0020;

  /** Value for the wSig member of the VIEW_TABLE_FORMAT2 structure. */
  short VALID_VIEW_FORMAT_SIG = 0x2BAD;

  /**
   * The VIEW_COLUMN_FORMAT record begins with a WORD value for the Signature of
   * the record.<br>
   * This symbol specifies the signature of the VIEW_COLUMN_FORMAT record.
   */
  short VIEW_COLUMN_FORMAT_SIGNATURE = 0x4356;
  /**
   * The VIEW_COLUMN_FORMAT2 record begins with a WORD value for the Signature of
   * the record.<br>
   * This symbol specifies the signature of the VIEW_COLUMN_FORMAT2 record.
   */
  short VIEW_COLUMN_FORMAT_SIGNATURE2 = 0x4357;
  /**
   * The VIEW_COLUMN_FORMAT3 record begins with a WORD value for the Signature of
   * the record.<br>
   * This symbol specifies the signature of the VIEW_COLUMN_FORMAT3 record.
   */
  short VIEW_COLUMN_FORMAT_SIGNATURE3 = 0x4358;
  /**
   * The VIEW_COLUMN_FORMAT4 record begins with a WORD value for the Signature of
   * the record.<br>
   * This symbol specifies the signature of the VIEW_COLUMN_FORMAT4 record.
   */
  short VIEW_COLUMN_FORMAT_SIGNATURE4 = 0x4359;
  /**
   * The VIEW_COLUMN_FORMAT5 record begins with a WORD value for the Signature of
   * the record.<br>
   * This symbol specifies the signature of the VIEW_COLUMN_FORMAT5 record.
   */
  short VIEW_COLUMN_FORMAT_SIGNATURE5 = 0x4360;
  short VIEW_COLUMN_FORMAT_SIGNATURE6 = 0x4361;

  byte VIEW_CLASS_TABLE = 0 << 4;
  byte VIEW_CLASS_CALENDAR = 1 << 4;
  byte VIEW_CLASS_MASK = (byte) 0xF0;

  byte CALENDAR_TYPE_DAY = 0;
  byte CALENDAR_TYPE_WEEK = 1;
  byte CALENDAR_TYPE_MONTH = 2;

  byte VIEW_STYLE_TABLE = ViewFormatConstants.VIEW_CLASS_TABLE;
  byte VIEW_STYLE_DAY = ViewFormatConstants.VIEW_CLASS_CALENDAR + 0;
  byte VIEW_STYLE_WEEK = ViewFormatConstants.VIEW_CLASS_CALENDAR + 1;
  byte VIEW_STYLE_MONTH = ViewFormatConstants.VIEW_CLASS_CALENDAR + 0;

  short VCF1_M_Sort = 0x0001;
  short VCF1_M_SortCategorize = 0x0002;
  short VCF1_M_SortDescending = 0x0004;
  short VCF1_M_Hidden = 0x0008;
  short VCF1_M_Response = 0x0010;
  short VCF1_M_HideDetail = 0x0020;
  short VCF1_M_Icon = 0x0040;
  short VCF1_M_NoResize = 0x0080;
  short VCF1_M_ResortAscending = 0x0100;
  short VCF1_M_ResortDescending = 0x0200;
  short VCF1_M_Twistie = 0x0400;
  short VCF1_M_ResortToView = 0x0800;
  short VCF1_M_SecondResort = 0x1000;
  short VCF1_M_SecondResortDescending = 0x2000;
  /* The following 4 constants are obsolete - see new VCF3_ constants below. */
  short VCF1_M_CaseInsensitiveSort = 0x4000;
  short VCF1_M_AccentInsensitiveSort = (short) (0x8000 & 0xffff);

  short VCF2_S_DisplayAlignment = 0;
  short VCF2_M_DisplayAlignment = 0x0003;
  short VCF2_S_SubtotalCode = 2;
  short VCF2_M_SubtotalCode = 0x003c;
  short VCF2_S_HeaderAlignment = 6;
  short VCF2_M_HeaderAlignment = 0x00c0;
  short VCF2_S_SortPermute = 8;
  short VCF2_M_SortPermute = 0x0100;
  short VCF2_S_SecondResortUniqueSort = 9;
  short VCF2_M_SecondResortUniqueSort = 0x0200;
  short VCF2_S_SecondResortCategorized = 10;
  short VCF2_M_SecondResortCategorized = 0x0400;
  short VCF2_S_SecondResortPermute = 11;
  short VCF2_M_SecondResortPermute = 0x0800;
  short VCF2_S_SecondResortPermutePair = 12;
  short VCF2_M_SecondResortPermutePair = 0x1000;
  short VCF2_S_ShowValuesAsLinks = 13;
  short VCF2_M_ShowValuesAsLinks = 0x2000;
  short VCF2_S_DisplayReadingOrder = 14;
  short VCF2_M_DisplayReadingOrder = 0x4000;
  short VCF2_S_HeaderReadingOrder = 15;
  short VCF2_M_HeaderReadingOrder = (short) (0x8000 & 0xffff);

  /**
   * Unofficial derived mask to include only the actual flag bits from
   * VIEW_COLUMN_FORMAT.Flags2
   */
  short VCF2_MASK_FLAGS = ~(ViewFormatConstants.VCF2_M_DisplayAlignment | ViewFormatConstants.VCF2_M_SubtotalCode
      | ViewFormatConstants.VCF2_M_HeaderAlignment | ViewFormatConstants.VCF2_M_DisplayReadingOrder
      | ViewFormatConstants.VCF2_M_HeaderReadingOrder);

  byte VIEW_COL_ALIGN_LEFT = 0;
  byte VIEW_COL_ALIGN_RIGHT = 1;
  byte VIEW_COL_ALIGN_CENTER = 2;

  byte VIEW_COL_LTR = 0;
  byte VIEW_COL_RTL = 1;

  short VIEW_COL_NUMBER = 0;
  short VIEW_COL_TIMEDATE = 1;
  short VIEW_COL_TEXT = 2;

  short VCF3_M_FlatInV5 = 0x0001;
  short VCF3_M_CaseSensitiveSortInV5 = 0x0002;
  short VCF3_M_AccentSensitiveSortInV5 = 0x0004;
  short VCF3_M_HideWhenFormula = 0x0008;
  short VCF3_M_TwistieResource = 0x0010;
  short VCF3_M_Color = 0x0020;
  /** column has extended date info */
  short VCF3_ExtDate = 0x0040;
  /** column has extended number format */
  short VCF3_NumberFormat = 0x0080;
  /** V6 - color col and user definable color */
  short VCF3_M_IsColumnEditable = 0x0100;
  short VCF3_M_UserDefinableColor = 0x0200;
  short VCF3_M_HideInR5 = 0x0400;
  short VCF3_M_NamesFormat = 0x0800;
  short VCF3_M_HideColumnTitle = 0x1000;
  /** Is this a shared column? */
  short VCF3_M_IsSharedColumn = 0x2000;
  /** Use only the formula from shared column - let use modify everything else */
  short VCF3_M_UseSharedColumnFormulaOnly = 0x4000;
  short VCF3_M_ExtendedViewColFmt6 = (short) 0x8000;

  short VCF_HIDE_M_NormalView = 0x0001;
  short VCF_HIDE_M_CalFormatTwoDay = 0x0002;
  short VCF_HIDE_M_CalFormatOneWeek = 0x0004;
  short VCF_HIDE_M_CalFormatTwoWeeks = 0x0008;
  short VCF_HIDE_M_CalFormatOneMonth = 0x0010;
  short VCF_HIDE_M_CalFormatOneYear = 0x0020;
  short VCF_HIDE_M_CalFormatOneDay = 0x0040;
  short VCF_HIDE_M_CalFormatWorkWeek = 0x0080;

  short VCF_HIDE_M_DB2_MAPPING = 0x0100;
  short VCF_HIDE_M_DB2_DATATYPE_TEXT = 0x0200;
  short VCF_HIDE_M_DB2_DATATYPE_NUMBER = 0x0400;
  short VCF_HIDE_M_DB2_DATATYPE_TIMEDATE = 0x0800;
  short VCF_HIDE_M_DB2_DATATYPE_NONE = 0x1000;
  short VCF_HIDE_M_MOBILE = 0x2000;

  int VCF5_M_IS_NAME = 0x00000001;
  int VCF5_M_SHOW_IM_STATUS = 0x00000002;
  int VCF5_M_VERT_ORIENT_TOP = 0x00000004;
  int VCF5_M_VERT_ORIENT_MID = 0x00000008;
  int VCF5_M_VERT_ORIENT_BOTTOM = 0x00000010;

  int VCF6_M_BeginWrapUnder = 0x00000001;
  int VCF6_M_PublishColumn = 0x00000002;
  int VCF6_M_ExtendColWidthToAvailWindowWidth = 0x00000004;
  int VCF6_M_BuildCollationOnDemand = 0x00000008;
  int VCF6_M_UserDefinableExtended = 0x00000010;
  int VCF6_M_IgnorePrefixes = 0x00000020;
  int VCF6_M_AbbreviatedDate = 0x00000040;
  int VCF6_M_AbbreviatedDateSet = 0x00000080;

  byte VIEW_TABLE_SINGLE_SPACE = 0;
  byte VIEW_TABLE_ONE_POINT_25_SPACE = 1;
  byte VIEW_TABLE_ONE_POINT_50_SPACE = 2;
  byte VIEW_TABLE_ONE_POINT_75_SPACE = 3;
  byte VIEW_TABLE_DOUBLE_SPACE = 4;

  byte VIEW_TABLE_HAS_LINK_COLUMN = 0x01;
  byte VIEW_TABLE_HTML_PASSTHRU = 0x02;

  int VTF3_M_GridStyleSolid = 0x00000001;
  int VTF3_M_GridStyleDash = 0x00000002;
  int VTF3_M_GridStyleDot = 0x00000004;
  int VTF3_M_GridStyleDashDot = 0x00000008;
  int VTF3_M_AllowCustomizations = 0x00000010;
  int VTF3_M_EvaluateActionsHideWhen = 0x00000020;
  /** V6 - Hide border after left margin */
  int VTF3_M_HideLeftMarginBorder = 0x00000040;
  /** V6 - bold the unread rows. */
  int VTF3_M_BoldUnreadRows = 0x00000080;
  /** V6 - inviewedit-newdocs in view */
  int VTF3_M_AllowCreateNewDoc = 0x00000100;
  /** V6 - View has background image. */
  int VTF3_M_HasBackgroundImage = 0x00000200;
  /** V7 - Limit the max rows returned for a Query View */
  int VTF3_M_MaxRowsLimit = 0x00000400;
  /** Hannover */
  int VTF3_M_ShowVerticalHorizontalSwitcher = 0x00000800;
  /** Hannover Java Views */
  int VTF3_M_ShowTabNavigator = 0x00001000;
  /** Hannover Java Views */
  int VTF3_M_AllowThreadGathering = 0x00002000;
  /** Hannover Java Views */
  int VTF3_M_DisableHideJaveView = 0x00004000;
  /** Hannover */
  int VTF3_M_HideColumnHeader = 0x00010000;

  byte VIEW_CALENDAR_FORMAT_VERSION = 1;

  byte VIEW_CAL_FORMAT_TWO_DAY = 0x01;
  byte VIEW_CAL_FORMAT_ONE_WEEK = 0x02;
  byte VIEW_CAL_FORMAT_TWO_WEEKS = 0x04;
  byte VIEW_CAL_FORMAT_ONE_MONTH = 0x08;
  byte VIEW_CAL_FORMAT_ONE_YEAR = 0x10;
  byte VIEW_CAL_FORMAT_ONE_DAY = 0x20;
  byte VIEW_CAL_FORMAT_WORK_WEEK = 0x40;
  byte VIEW_CAL_FORMAT_ALL = (byte) 0xff;

  byte VIEW_CAL_VALID_PRE_503_FORMATS = 0x3f;
  byte VIEW_CAL_VALID_503_FORMATS = 0x7f;

  /** Display Conflict marks */
  short CAL_DISPLAY_CONFLICTS = 0x0001;
  /** Disable Time Slots */
  short CAL_ENABLE_TIMESLOTS = 0x0002;
  /** Show Time Slot Bitmaps */
  short CAL_DISPLAY_TIMESLOT_BMPS = 0x0004;
  /** Enable Timegrouping */
  short CAL_ENABLE_TIMEGROUPING = 0x0008;
  /** Allow user to override time slots */
  short CAL_TIMESLOT_OVERRIDE = 0x0010;
  /** Don't show the month header in the view (i.e. January 2001) */
  short CAL_HIDE_MONTH_HEADER = 0x0020;
  /** Don't show the GoToToday button in the view */
  short CAL_HIDE_GOTOTODAY = 0x0040;
  /** Don't show the trash view in the header */
  short CAL_SHOW_TRASHVIEW = 0x0080;
  /** Don't show the all docs view in the header */
  short CAL_SHOW_ALLDOCSVIEW = 0x0100;
  /** Don't show the formatting button */
  short CAL_HIDE_FORMATBTN = 0x0200;
  /** Don't show the day tab */
  short CAL_HIDE_DAYTAB = 0x0400;
  /** Don't show the week tab */
  short CAL_HIDE_WEEKTAB = 0x0800;
  /** Don't show the month tab */
  short CAL_HIDE_MONTHTAB = 0x1000;
  /** show the header as dayplanner */
  short CAL_SHOW_DAYPLANNER = 0x2000;
  /** show the owner name */
  short CAL_HIDE_OWNERNAME = 0x4000;
  /** TRUE if RTL Calendar, Note: same as VIEW_TABLE_RTLVIEW */
  short VIEW_CALENDAR_RTLVIEW = (short) 0x8000;

  /** V4.5, V4.6 has minor version of 0 */
  byte VIEW_CAL_FORMAT_MINOR_V4x = 0;
  /** V5 */
  byte VIEW_CAL_FORMAT_MINOR_1 = 1;
  /** V5.03 and up - added custom work week format */
  byte VIEW_CAL_FORMAT_MINOR_2 = 2;
  /** Calendar Grid Color */
  byte VIEW_CAL_FORMAT_MINOR_3 = 3;
  /** more damn colors */
  byte VIEW_CAL_FORMAT_MINOR_4 = 4;

  /** Secondary Calendar View Format. Introduced in build 161 (for v5). */
  short VIEW_CALENDAR_FORMAT2_SIGNATURE = 0x0323;
}
