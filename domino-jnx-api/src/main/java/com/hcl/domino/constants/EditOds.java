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
package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code editods.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.0.35
 */
public interface EditOds {

  int EMBEDDEDOUTLINE_FLAG_DISPLAYHORZ = 0x00000001;
  int EMBEDDEDOUTLINE_FLAG_HASIMAGELABEL = 0x00000002;
  int EMBEDDEDOUTLINE_FLAG_TILEIMAGE = 0x00000004;
  int EMBEDDEDOUTLINE_FLAG_USEAPPLET_INBROWSER = 0x00000008;
  int EMBEDDEDOUTLINE_FLAG_TYPE_TITLE = 0x00000010;
  int EMBEDDEDOUTLINE_FLAG_SHOWTWISTIE = 0x00000020;
  int EMBEDDEDOUTLINE_FLAG_TITLEFIXED = 0x00000040;
  int EMBEDDEDOUTLINE_FLAG_TOPLEVELFIXED = 0x00000080;
  int EMBEDDEDOUTLINE_FLAG_SUBLEVELFIXED = 0x00000100;
  int EMBEDDEDOUTLINE_FLAG_TREE_STYLE = 0x00000200;
  int EMBEDDEDOUTLINE_FLAG_HASNAME = 0x00000400;
  int EMBEDDEDOUTLINE_FLAG_HASTARGETFRAME = 0x00000800;
  int EMBEDDEDOUTLINE_FLAG_ALLTHESAME = 0x00001000;
  int EMBEDDEDOUTLINE_FLAG_BACK_ALLTHESAME = 0x00002000;
  int EMBEDDEDOUTLINE_FLAG_EXPAND_DATA = 0x00004000;
  int EMBEDDEDOUTLINE_FLAG_EXPAND_ALL = 0x00008000;
  int EMBEDDEDOUTLINE_FLAG_EXPAND_FIRST = 0x00010000;
  int EMBEDDEDOUTLINE_FLAG_EXPAND_SAVED = 0x00020000;
  int EMBEDDEDOUTLINE_FLAG_EXPAND_NONE = 0x00040000;
  int EMBEDDEDOUTLINE_FLAG_HASROOTNAME = 0x00080000;
  int EMBEDDEDOUTLINE_FLAG_RTLREADING = 0x00100000;
  int EMBEDDEDOUTLINE_FLAG_TWISTIEIMAGE = 0x00200000;
  int EMBEDDEDOUTLINE_FLAG_HANDLEFOLDERUNREAD = 0x00400000;
  int EMBEDDEDOUTLINE_FLAG_NEWSTYLE_TWISTIE = 0x00800000;
  int EMBEDDEDOUTLINE_FLAG_MAINTAINFOLDERUNREAD = 0x01000000;
  int EMBEDDEDOUTLINE_FLAG_USEJSCTLINBROWSER = 0x02000000;
  int EMBEDDEDOUTLINE_FLAG_USECUSTOMJSINBROWSER = 0x04000000;
  
  /*  defines for Background Color Offset for title and each level - 0 relative*/
  int TITLE_BACK_COLOR_OFFSET = 0;
  int MAINLEVEL_BACK_COLOR_OFFSET = 3;
  int SUBLEVEL_BACK_COLOR_OFFSET = 6;
  
  /*  defines for back color offset for each type */
  int NORMAL_BACK_COLOR = 0;
  int SELECTED_BACK_COLOR = 1;
  int MOUSE_OVER_BACK_COLOR = 2;
  
  /* defines for entry/background image alignment */
  short ALIGNMENT_TOPLEFT = 0;
  short ALIGNMENT_TOPCENTER = 1;
  short ALIGNMENT_TOPRIGHT = 2;
  short ALIGNMENT_MIDDLELEFT = 3;
  short ALIGNMENT_MIDDLECENTER = 4;
  short ALIGNMENT_MIDDLERIGHT = 5;
  short ALIGNMENT_BOTTOMLEFT = 6;
  short ALIGNMENT_BOTTOMCENTER = 7;
  short ALIGNMENT_BOTTOMRIGHT = 8;
  
  short NOTES_OLEVERSION1 = 1;
  short NOTES_OLEVERSION2 = 2;
  
  /** The data is an OLE embedded OBJECT */
  int OLEREC_FLAG_OBJECT = 0x01;
  /** The data is an OLE Link */
  int OLEREC_FLAG_LINK = 0x02;
  /** If link, Link type == Automatic (hot) */
  int OLEREC_FLAG_AUTOLINK = 0x04;
  /** If link, Link type == Manual (warm) */
  int OLEREC_FLAG_MANUALLINK = 0x08;
  /** New object, just inserted */
  int OLEREC_FLAG_NEWOBJECT = 0x10;
  /** New object, just pasted */
  int OLEREC_FLAG_PASTED = 0x20;
  /** Object came from form and should be saved every time it changes in server */
  int OLEREC_FLAG_SAVEOBJWHENCHANGED = 0x40;
  /** Object inherited from form, so don't visualize or object incabable of rendering itself. */
  int OLEREC_FLAG_NOVISUALIZE = 0x80;
  
  /** CF_TEXT */
  short DDEFORMAT_TEXT = 0x01;
  /** CF_METAFILE or CF_METAFILEPICT */
  short DDEFORMAT_METAFILE = 0x02;
  /** CF_BITMAP */
  short DDEFORMAT_BITMAP = 0x03;
  /** Rich Text Format */
  short DDEFORMAT_RTF = 0x04;
  /** OLE Ownerlink (never saved in CD_DDE or CD_OLE: used at run time) */
  short DDEFORMAT_OWNERLINK = 0x06;
  /** OLE Objectlink (never saved in CD_DDE or CD_OLE: used at run time) */
  short DDEFORMAT_OBJECTLINK = 0x07;
  /** OLE Native (never saved in CD_DDE or CD_OLE: used at run time) */
  short DDEFORMAT_NATIVE = 0x08;
  /** Program Icon for embedded object */
  short DDEFORMAT_ICON = 0x09;
}
