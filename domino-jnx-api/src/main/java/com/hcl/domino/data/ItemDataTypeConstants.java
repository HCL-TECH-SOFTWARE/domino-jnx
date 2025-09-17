package com.hcl.domino.data;

/**
 * The constant short values used by {@link ItemDataType}.
 * 
 * @since 1.48.0
 */
public interface ItemDataTypeConstants {
  /*    "Computable" Data Types */
  short TYPE_ERROR = (0 + (1 << 8));
  short TYPE_UNAVAILABLE = (0 + (2 << 8));
  short TYPE_TEXT = (0 + (5 << 8));
  short TYPE_TEXT_LIST = (1 + (5 << 8));
  short TYPE_NUMBER = (0 + (3 << 8));
  short TYPE_NUMBER_RANGE = (1 + (3 << 8));
  short TYPE_TIME = (0 + (4 << 8));
  short TYPE_TIME_RANGE = (1 + (4 << 8));
  short TYPE_FORMULA = (0 + (6 << 8));
  short TYPE_USERID = (0 + (7 << 8));
  /*    "Non-Computable" Data Types */
  short TYPE_SIGNATURE = (8 + (0 << 8));
  short TYPE_ACTION = (16 + (0 << 8));
  short TYPE_WORKSHEET_DATA = (13 + (0 << 8));
  short TYPE_VIEWMAP_LAYOUT = (19 + (0 << 8));
  short TYPE_SEAL2 = (31 + (0 << 8));
  short TYPE_LSOBJECT = (20 + (0 << 8));
  short TYPE_ICON = (6 + (0 << 8));
  short TYPE_VIEW_FORMAT = (5 + (0 << 8));
  short TYPE_SCHED_LIST = (22 + (0 << 8));
  short TYPE_VIEWMAP_DATASET = (18 + (0 << 8));
  short TYPE_SEAL = (9 + (0 << 8));
  short TYPE_MIME_PART = (25 + (0 << 8));
  short TYPE_SEALDATA = (10 + (0 << 8));
  short TYPE_NOTELINK_LIST = (7 + (0 << 8));
  short TYPE_COLLATION = (2 + (0 << 8));
  short TYPE_RFC822_TEXT = (2 + (5 << 8));
  /** Richtext item */
  short TYPE_COMPOSITE = (1 + (0 << 8));
  short TYPE_OBJECT = (3 + (0 << 8));
  short TYPE_HTML = (21 + (0 << 8));
  short TYPE_ASSISTANT_INFO = (17 + (0 << 8));
  short TYPE_HIGHLIGHTS = (12 + (0 << 8));
  short TYPE_NOTEREF_LIST = (4 + (0 << 8));
  short TYPE_QUERY = (15 + (0 << 8));
  short TYPE_USERDATA = (14 + (0 << 8));
  short TYPE_INVALID_OR_UNKNOWN = (0 + (0 << 8));
  short TYPE_SEAL_LIST = (11 + (0 << 8));
  short TYPE_CALENDAR_FORMAT = (24 + (0 << 8));
  short TYPE_OUTLINE_FORMAT = (26 + (0 << 8));
}
