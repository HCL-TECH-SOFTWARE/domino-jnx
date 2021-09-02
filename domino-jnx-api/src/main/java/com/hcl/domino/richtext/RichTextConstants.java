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
package com.hcl.domino.richtext;

import com.hcl.domino.constants.EditOds;

public interface RichTextConstants extends EditOds {

  short LONGRECORDLENGTH = 0x0000;
  short WORDRECORDLENGTH = (short) (0xff00 & 0xffff);
  short BYTERECORDLENGTH = 0; /* High byte contains record length */

  /* Signatures for Composite Records in items of data type COMPOSITE */

  short SIG_CD_SRC = 81 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_IMAGEHEADER2 = 82 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_PDEF_MAIN = 83
      | RichTextConstants.WORDRECORDLENGTH /* Signatures for items used in Property Broker definitions. LI 3925.04 */;
  short SIG_CD_PDEF_TYPE = 84 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PDEF_PROPERTY = 85 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PDEF_ACTION = 86 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TABLECELL_DATAFLAGS = 87 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_EMBEDDEDCONTACTLIST = 88 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_IGNORE = 89 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TABLECELL_HREF2 = 90 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HREFBORDER = 91 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TABLEDATAEXTENSION = 92 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EMBEDDEDCALCTL = 93 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_ACTIONEXT = 94 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EVENT_LANGUAGE_ENTRY = 95 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_FILESEGMENT = 96 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_FILEHEADER = 97 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_DATAFLAGS = 98 | RichTextConstants.BYTERECORDLENGTH;

  short SIG_CD_BACKGROUNDPROPERTIES = 99 | RichTextConstants.BYTERECORDLENGTH;

  short SIG_CD_EMBEDEXTRA_INFO = 100 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_CLIENT_BLOBPART = 101 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_CLIENT_EVENT = 102 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BORDERINFO_HS = 103 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LARGE_PARAGRAPH = 104 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EXT_EMBEDDEDSCHED = 105 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BOXSIZE = 106 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_POSITIONING = 107 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_LAYER = 108 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_DECSFIELD = 109 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_SPAN_END = 110 | RichTextConstants.BYTERECORDLENGTH /* Span End */;
  short SIG_CD_SPAN_BEGIN = 111 | RichTextConstants.BYTERECORDLENGTH /* Span Begin */;
  short SIG_CD_TEXTPROPERTIESTABLE = 112 | RichTextConstants.WORDRECORDLENGTH /* Text Properties Table */;

  short SIG_CD_HREF2 = 113 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BACKGROUNDCOLOR = 114 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_INLINE = 115 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_V6HOTSPOTBEGIN_CONTINUATION = 116 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TARGET_DBLCLK = 117 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_CAPTION = 118 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LINKCOLORS = 119 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TABLECELL_HREF = 120 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_ACTIONBAREXT = 121 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_IDNAME = 122 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TABLECELL_IDNAME = 123 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_IMAGESEGMENT = 124 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_IMAGEHEADER = 125 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_V5HOTSPOTBEGIN = 126 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_V5HOTSPOTEND = 127 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TEXTPROPERTY = 128 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PARAGRAPH = 129 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_PABDEFINITION = 130 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PABREFERENCE = 131 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TEXT = 133 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_XML = 134 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HEADER = 142 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LINKEXPORT2 = 146 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BITMAPHEADER = 149 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_BITMAPSEGMENT = 150 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_COLORTABLE = 151 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_GRAPHIC = 153 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_PMMETASEG = 154 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_WINMETASEG = 155 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_MACMETASEG = 156 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_CGMMETA = 157 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_PMMETAHEADER = 158 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_WINMETAHEADER = 159 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_MACMETAHEADER = 160 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_TABLEBEGIN = 163 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TABLECELL = 164 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TABLEEND = 165 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_STYLENAME = 166 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_STORAGELINK = 196 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TRANSPARENTTABLE = 197 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_HORIZONTALRULE = 201 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_ALTTEXT = 202 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_ANCHOR = 203 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HTMLBEGIN = 204 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HTMLEND = 205 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HTMLFORMULA = 206 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_NESTEDTABLEBEGIN = 207 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_NESTEDTABLECELL = 208 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_NESTEDTABLEEND = 209 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_COLOR = 210 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TABLECELL_COLOR = 211 | RichTextConstants.BYTERECORDLENGTH;

  /* 212 thru 219 reserved for BSIG'S - don't use until we hit 255 */

  short SIG_CD_BLOBPART = 220 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BEGIN = 221 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_END = 222 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VERTICALALIGN = 223 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_FLOATPOSITION = 224 | RichTextConstants.BYTERECORDLENGTH;

  short SIG_CD_TIMERINFO = 225 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TABLEROWHEIGHT = 226 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TABLELABEL = 227 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BIDI_TEXT = 228 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BIDI_TEXTEFFECT = 229 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_REGIONBEGIN = 230 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_REGIONEND = 231 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TRANSITION = 232 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_FIELDHINT = 233 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PLACEHOLDER = 234 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HTMLNAME = 235 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EMBEDDEDOUTLINE = 236 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EMBEDDEDVIEW = 237 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_CELLBACKGROUNDDATA = 238 | RichTextConstants.WORDRECORDLENGTH;

  /* Signatures for Frameset CD records */
  short SIG_CD_FRAMESETHEADER = 239 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_FRAMESET = 240 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_FRAME = 241 | RichTextConstants.WORDRECORDLENGTH;
  /* Signature for Target Frame info on a link	*/
  short SIG_CD_TARGET = 242 | RichTextConstants.WORDRECORDLENGTH;

  short SIG_CD_NATIVEIMAGE = 243 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_MAPELEMENT = 244 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_AREAELEMENT = 245 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HREF = 246 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EMBEDDEDCTL = 247 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HTML_ALTTEXT = 248 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EVENT = 249 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PRETABLEBEGIN = 251 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BORDERINFO = 252 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_EMBEDDEDSCHEDCTL = 253 | RichTextConstants.WORDRECORDLENGTH;

  short SIG_CD_EXT2_FIELD = 254 | RichTextConstants.WORDRECORDLENGTH /* Currency, numeric, and data/time extensions */;
  short SIG_CD_EMBEDDEDEDITCTL = 255 | RichTextConstants.WORDRECORDLENGTH;

  /* Can not go beyond 255.  However, there may be room at the beginning of
  	the list.  Check there.   */

  /* Signatures for Composite Records that are reserved internal records, */
  /* whose format may change between releases. */

  short SIG_CD_DOCUMENT_PRE_26 = 128 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_FIELD_PRE_36 = 132 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_FIELD = 138 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_DOCUMENT = 134 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_METAFILE = 135 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BITMAP = 136 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_FONTTABLE = 139 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LINK = 140 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_LINKEXPORT = 141 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_KEYWORD = 143 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LINK2 = 145 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_CGM = 147 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_TIFF = 148 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_PATTERNTABLE = 152 | RichTextConstants.LONGRECORDLENGTH;
  short SIG_CD_DDEBEGIN = 161 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_DDEEND = 162 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_OLEBEGIN = 167 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_OLEEND = 168 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HOTSPOTBEGIN = 169 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_HOTSPOTEND = 170 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_BUTTON = 171 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_BAR = 172 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_V4HOTSPOTBEGIN = 173 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_V4HOTSPOTEND = 174 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_EXT_FIELD = 176 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LSOBJECT = 177 | RichTextConstants.WORDRECORDLENGTH/* Compiled LS code*/;
  short SIG_CD_HTMLHEADER = 178 | RichTextConstants.WORDRECORDLENGTH /* Raw HTML */;
  short SIG_CD_HTMLSEGMENT = 179 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_OLEOBJPH = 180 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_MAPIBINARY = 181 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LAYOUT = 183 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_LAYOUTTEXT = 184 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_LAYOUTEND = 185 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_LAYOUTFIELD = 186 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_PABHIDE = 187 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_PABFORMREF = 188 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_ACTIONBAR = 189 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_ACTION = 190 | RichTextConstants.WORDRECORDLENGTH;

  short SIG_CD_DOCAUTOLAUNCH = 191 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LAYOUTGRAPHIC = 192 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_OLEOBJINFO = 193 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_LAYOUTBUTTON = 194 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_TEXTEFFECT = 195 | RichTextConstants.WORDRECORDLENGTH;

  short SIG_ACTION_HEADER = 129 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_ACTION_MODIFYFIELD = 130 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_REPLY = 131 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_FORMULA = 132 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_LOTUSSCRIPT = 133 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_SENDMAIL = 134 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_DBCOPY = 135 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_DELETE = 136 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_ACTION_BYFORM = 137 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_MARKREAD = 138 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_ACTION_MARKUNREAD = 139 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_ACTION_MOVETOFOLDER = 140 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_COPYTOFOLDER = 141 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_REMOVEFROMFOLDER = 142 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_NEWSLETTER = 143 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_RUNAGENT = 144 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_SENDDOCUMENT = 145 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_ACTION_FORMULAONLY = 146 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_JAVAAGENT = 147 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_ACTION_JAVA = 148 | RichTextConstants.WORDRECORDLENGTH;
  
  /**
   * Speculative (and speculatively-named) prefix for a four-byte record that precedes
   * the CDACTION* records in a "Simple Actions"-type CDACTION record's data area.
   */
  short SIG_UNIDENTIFIED_CDACTION_PREFIX = 16 | RichTextConstants.BYTERECORDLENGTH;

  /* Signatures for items of type TYPE_VIEWMAP_DATASET */

  short SIG_VIEWMAP_DATASET = 87 | RichTextConstants.WORDRECORDLENGTH;

  /* Signatures for items of type TYPE_VIEWMAP */

  short SIG_CD_VMHEADER = 175 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMBITMAP = 176 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMRECT = 177 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMPOLYGON_BYTE = 178 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMPOLYLINE_BYTE = 179 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMREGION = 180 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMACTION = 181 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMELLIPSE = 182 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMSMALLTEXTBOX = 183 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMRNDRECT = 184 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMBUTTON = 185 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMACTION_2 = 186 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_VMTEXTBOX = 187 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_VMPOLYGON = 188 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_VMPOLYLINE = 189 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_VMPOLYRGN = 190 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_VMCIRCLE = 191 | RichTextConstants.BYTERECORDLENGTH;
  short SIG_CD_VMPOLYRGN_BYTE = 192 | RichTextConstants.BYTERECORDLENGTH;

  /* Signatures for alternate CD sequences*/
  short SIG_CD_ALTERNATEBEGIN = 198 | RichTextConstants.WORDRECORDLENGTH;
  short SIG_CD_ALTERNATEEND = 199 | RichTextConstants.BYTERECORDLENGTH;

  short SIG_CD_OLERTMARKER = 200 | RichTextConstants.WORDRECORDLENGTH;

  short CDIMAGETYPE_GIF = 1;
  short CDIMAGETYPE_JPEG = 2;
  short CDIMAGETYPE_BMP = 3;
  short CDIMAGETYPE_PNG = 4;
  /* Images not supported in Notes rich text, but which can be useful for MIME/HTML external files */
  short CDIMAGETYPE_SVG = 5;
  short CDIMAGETYPE_TIF = 6;
  short CDIMAGETYPE_PDF = 7;

  /* Version control of graphic header */
  byte CDGRAPHIC_VERSION1 = 0; /* Created by Notes version 2 */
  byte CDGRAPHIC_VERSION2 = 1; /* Created by Notes version 3 */
  byte CDGRAPHIC_VERSION3 = 2; /* Created by Notes version 4.5 */

  /*	The following flag indicates that the DestSize field contains
  pixel values instead of twips. */

  byte CDGRAPHIC_FLAG_DESTSIZE_IS_PIXELS = 0x01;
  byte CDGRAPHIC_FLAG_SPANSLINES = 0x02;

  /*	HOTSPOT_RUN Types */

  short HOTSPOTREC_TYPE_POPUP = 1;
  short HOTSPOTREC_TYPE_HOTREGION = 2;
  short HOTSPOTREC_TYPE_BUTTON = 3;
  short HOTSPOTREC_TYPE_FILE = 4;
  short HOTSPOTREC_TYPE_SECTION = 7;
  short HOTSPOTREC_TYPE_ANY = 8;
  short HOTSPOTREC_TYPE_HOTLINK = 11;
  short HOTSPOTREC_TYPE_BUNDLE = 12;
  short HOTSPOTREC_TYPE_V4_SECTION = 13;
  short HOTSPOTREC_TYPE_SUBFORM = 14;
  short HOTSPOTREC_TYPE_ACTIVEOBJECT = 15;
  short HOTSPOTREC_TYPE_OLERICHTEXT = 18;
  short HOTSPOTREC_TYPE_EMBEDDEDVIEW = 19; /* embedded view */
  short HOTSPOTREC_TYPE_EMBEDDEDFPANE = 20; /* embedded folder pane */
  short HOTSPOTREC_TYPE_EMBEDDEDNAV = 21; /* embedded navigator */
  short HOTSPOTREC_TYPE_MOUSEOVER = 22;
  short HOTSPOTREC_TYPE_FILEUPLOAD = 24; /* file upload placeholder */
  short HOTSPOTREC_TYPE_EMBEDDEDOUTLINE = 27; /* embedded outline */
  short HOTSPOTREC_TYPE_EMBEDDEDCTL = 28; /* embedded control window */
  short HOTSPOTREC_TYPE_EMBEDDEDCALENDARCTL = 30; /* embedded calendar control (date picker) */
  short HOTSPOTREC_TYPE_EMBEDDEDSCHEDCTL = 31; /* embedded scheduling control */
  short HOTSPOTREC_TYPE_RCLINK = 32; /* Not a new type, but renamed for V5 terms*/
  short HOTSPOTREC_TYPE_EMBEDDEDEDITCTL = 34; /* embedded editor control */
  short HOTSPOTREC_TYPE_CONTACTLISTCTL = 36; /* Embeddeble buddy list */

  int HOTSPOTREC_RUNFLAG_BEGIN = 0x00000001;
  int HOTSPOTREC_RUNFLAG_END = 0x00000002;
  int HOTSPOTREC_RUNFLAG_BOX = 0x00000004;
  int HOTSPOTREC_RUNFLAG_NOBORDER = 0x00000008;
  int HOTSPOTREC_RUNFLAG_FORMULA = 0x00000010; /*	Popup is a formula, not text. */
  int HOTSPOTREC_RUNFLAG_MOVIE = 0x00000020; /*	File is a QuickTime movie. */
  int HOTSPOTREC_RUNFLAG_IGNORE = 0x00000040; /*	Run is for backward compatibility
                                              (i.e. ignore the run)
                                              */
  int HOTSPOTREC_RUNFLAG_ACTION = 0x00000080; /*	Hot region executes a canned action	*/
  int HOTSPOTREC_RUNFLAG_SCRIPT = 0x00000100; /*	Hot region executes a script.	*/
  int HOTSPOTREC_RUNFLAG_INOTES = 0x00001000;
  int HOTSPOTREC_RUNFLAG_ISMAP = 0x00002000;
  int HOTSPOTREC_RUNFLAG_INOTES_AUTO = 0x00004000;
  int HOTSPOTREC_RUNFLAG_ISMAP_INPUT = 0x00008000;

  int HOTSPOTREC_RUNFLAG_SIGNED = 0x00010000;
  int HOTSPOTREC_RUNFLAG_ANCHOR = 0x00020000;
  int HOTSPOTREC_RUNFLAG_COMPUTED = 0x00040000; /*	Used in conjunction
                                                with computed hotspots.
                                                */
  int HOTSPOTREC_RUNFLAG_TEMPLATE = 0x00080000; /*	used in conjunction
                                                with embedded navigator
                                                panes.
                                                */
  int HOTSPOTREC_RUNFLAG_HIGHLIGHT = 0x00100000;
  int HOTSPOTREC_RUNFLAG_EXTACTION = 0x00200000; /*  Hot region executes an extended action */
  int HOTSPOTREC_RUNFLAG_NAMEDELEM = 0x00400000; /*	Hot link to a named element */

  /*	Allow R6 dual action type buttons, e.g. client LotusScript, web JS */
  int HOTSPOTREC_RUNFLAG_WEBJAVASCRIPT = 0x00800000;

  int HOTSPOTREC_RUNFLAG_ODSMASK = 0x00FFFFFC; /*	Mask for bits stored on disk*/

  /*	CDCAPTION - Text to display with an object (e.g., a graphic) */

  byte CAPTION_POSITION_BELOW_CENTER = 0; /*	Centered below object */
  byte CAPTION_POSITION_MIDDLE_CENTER = 1; /*	Centered on object */

  int DDESERVERNAMEMAX = 32;
  int DDEITEMNAMEMAX = 64;

  int IMAGE_SEGMENT_MAX = 10240;
  int SIZE_CDIMAGESEGMENT = 10;
  /** The amount of data to store in each CD image record item */
  int PER_IMAGE_ITEM_DATA_CAP = (RichTextConstants.SIZE_CDIMAGESEGMENT + RichTextConstants.IMAGE_SEGMENT_MAX) * 2;

  // It appears that CDFILESEGMENTs cap out at 10240 bytes of data
  int FILE_SEGMENT_SIZE_CAP = 10240;
  int SIZE_CDFILEHEADER = 24;
  int SIZE_CDFILESEGMENT = 18;
  /** The amount of data to store in each CD record item */
  int PER_FILE_ITEM_DATA_CAP = (RichTextConstants.SIZE_CDFILESEGMENT + RichTextConstants.FILE_SEGMENT_SIZE_CAP) * 2;

  int BLOBPART_SIZE_CAP = 20000;
  int SIZE_CDBLOBPART = 18;
  /** The amount of data to store in each CD image record item */
  int PER_BLOB_ITEM_DATA_CAP = (RichTextConstants.SIZE_CDBLOBPART + RichTextConstants.BLOBPART_SIZE_CAP) * 2;

  short COLOR_VALUE_FLAGS_ISRGB = 0x0001;
  short COLOR_VALUE_FLAGS_NOCOLOR = 0x0004;
  short COLOR_VALUE_FLAGS_SYSTEMCOLOR = 0x0008;
  short COLOR_VALUE_FLAGS_HASGRADIENT = 0x0010;
  short COLOR_VALUE_FLAGS_APPLICATION_MASK = (short) 0xf000;

  short COLOR_VALUE_FLAGS_RESERVED1 = (short) 0x8000;
  short COLOR_VALUE_FLAGS_RESERVED2 = 0x4000;
  short COLOR_VALUE_FLAGS_RESERVED3 = 0x2000;
  short COLOR_VALUE_FLAGS_RESERVED4 = 0x1000;

  /* Number Formats */
  byte NFMT_GENERAL = 0;
  byte NFMT_FIXED = 1;
  byte NFMT_SCIENTIFIC = 2;
  byte NFMT_CURRENCY = 3;
  /** LI 3926.07 Number Format */
  byte NFMT_BYTES = 4;

  /* Number Attributes */
  byte NATTR_PUNCTUATED = 0x0001;
  byte NATTR_PARENS = 0x0002;
  byte NATTR_PERCENT = 0x0004;
  byte NATTR_VARYING = 0x0008;
  /**
   * LI 3926.07, new number format type in Hannover, added for backward
   * compatibility
   */
  byte NATTR_BYTES = 0x0010;

  /** year, month, and day */
  byte TDFMT_FULL = 0;
  /** month and day, year if not this year */
  byte TDFMT_CPARTIAL = 1;
  /** month and day */
  byte TDFMT_PARTIAL = 2;
  /** year and month */
  byte TDFMT_DPARTIAL = 3;
  /** year(4digit), month, and day */
  byte TDFMT_FULL4 = 4;
  /** month and day, year(4digit) if not this year */
  byte TDFMT_CPARTIAL4 = 5;
  /** year(4digit) and month */
  byte TDFMT_DPARTIAL4 = 6;
  /** hour, minute, and second */
  byte TTFMT_FULL = 0;
  /** hour and minute */
  byte TTFMT_PARTIAL = 1;
  /** hour */
  byte TTFMT_HOUR = 2;
  /** all times converted to THIS zone */
  byte TZFMT_NEVER = 0;
  /** show only when outside this zone */
  byte TZFMT_SOMETIMES = 1;
  /** show on all times, regardless */
  byte TZFMT_ALWAYS = 2;

  /** DATE */
  byte TSFMT_DATE = 0;
  /** TIME */
  byte TSFMT_TIME = 1;
  /** DATE TIME */
  byte TSFMT_DATETIME = 2;
  /** DATE TIME or TIME Today or TIME Yesterday */
  byte TSFMT_CDATETIME = 3;

  /*	CDFIELD Flags Definitions */

  /** Clear these if FOCLEARSPARES is TRUE */
  short V3SPARESTOCLEAR = 0x0075;

  /** Field contains read/writers */
  short FREADWRITERS = 0x0001;
  /** Field is editable, not read only */
  short FEDITABLE = 0x0002;
  /** Field contains distinguished names */
  short FNAMES = 0x0004;
  /** Store DV, even if not spec'ed by user */
  short FSTOREDV = 0x0008;
  /** Field contains document readers */
  short FREADERS = 0x0010;
  /** Field contains a section */
  short FSECTION = 0x0020;
  /** can be assumed to be clear in memory, V3 &amp; later */
  short FSPARE3 = 0x0040;
  /** IF CLEAR, CLEAR AS ABOVE */
  short FV3FAB = 0x0080;
  /** Field is a computed field */
  short FCOMPUTED = 0x0100;
  /** Field is a keywords field */
  short FKEYWORDS = 0x0200;
  /** Field is protected */
  short FPROTECTED = 0x0400;
  /** Field name is simply a reference to a shared field note */
  short FREFERENCE = 0x0800;
  /** sign field */
  short FSIGN = 0x1000;
  /** seal field */
  short FSEAL = 0x2000;
  /** standard UI */
  short FKEYWORDS_UI_STANDARD = 0x0000;
  /** checkbox UI */
  short FKEYWORDS_UI_CHECKBOX = 0x4000;
  /** radiobutton UI */
  short FKEYWORDS_UI_RADIOBUTTON = (short) 0x8000;
  /** allow doc editor to add new values */
  short FKEYWORDS_UI_ALLOW_NEW = (short) 0xc000;

  short FIELD_TYPE_ERROR = 0;
  short FIELD_TYPE_NUMBER = 1;
  short FIELD_TYPE_TIME = 2;
  short FIELD_TYPE_RICH_TEXT = 3;
  short FIELD_TYPE_AUTHORS = 4;
  short FIELD_TYPE_READERS = 5;
  short FIELD_TYPE_NAMES = 6;
  short FIELD_TYPE_KEYWORDS = 7;
  short FIELD_TYPE_TEXT = 8;
  short FIELD_TYPE_SECTION = 9;
  short FIELD_TYPE_PASSWORD = 10;
  short FIELD_TYPE_FORMULA = 11;
  short FIELD_TYPE_TIMEZONE = 12;

  short LDELIM_COMMA = 0x0002;
  short LDELIM_SEMICOLON = 0x0004;
  short LDELIM_NEWLINE = 0x0008;
  short LDELIM_BLANKLINE = 0x0010;
  short LD_MASK = 0x0fff;

  short LDELIM_SPACE = 0x0001;
  short LDDELIM_SPACE = 0x1000;
  short LDDELIM_COMMA = 0x2000;
  short LDDELIM_SEMICOLON = 0x3000;
  short LDDELIM_NEWLINE = 0x4000;
  short LDDELIM_BLANKLINE = 0x5000;
  short LDD_MASK = (short) 0xf000;

  short FIELDHINT_LIMITED = 0x0001;

  /*	Flags for CDEXTFIELD Flags1.  Note that the low word in Flags1 is not used. */
  /** lookup name as each char typed */
  int FEXT_LOOKUP_EACHCHAR = 0x00010000;
  /** recalc on new keyword selection */
  int FEXT_KWSELRECALC = 0x00020000;
  /** suppress showing field hinky minky */
  int FEXT_KWHINKYMINKY = 0x00040000;
  /** recalc after validation */
  int FEXT_AFTERVALIDATION = 0x00080000;
  /** the first field with this bit set will accept the caret */
  int FEXT_ACCEPT_CARET = 0x00100000;

  /*	These bits are in use by the	0x02000000L
  	column value.  The result of	0x04000000L
  	the shifted bits is (cols - 1)	0x08000000L */
  int FEXT_KEYWORD_COLS_SHIFT = 25;
  int FEXT_KEYWORD_COLS_MASK = 0x0E000000;
  int FEXT_KEYWORD_FRAME_3D = 0x00000000;
  int FEXT_KEYWORD_FRAME_STANDARD = 0x10000000;
  int FEXT_KEYWORD_FRAME_NONE = 0x20000000;
  int FEXT_KEYWORD_FRAME_MASK = 0x30000000;
  int FEXT_KEYWORD_FRAME_SHIFT = 28;
  int FEXT_KEYWORDS_UI_COMBO = 0x40000000;
  int FEXT_KEYWORDS_UI_LIST = 0x80000000;

  /*	Flags for CDEXTFIELD Flags2. */

  /** TRUE to recalc the value choices. */
  int FEXT_KW_CHOICE_RECALC = 0x00000001;
  /** TRUE means we have a CD_EXTHTML field */
  int FEXT_HTML_IN_FIELDDEF = 0x00000002;
  /** TRUE if hiding delimeters */
  int FEXT_HIDEDELIMITERS = 0x00000004;
  int FEXT_KW_RTL_READING_ORDER = 0x00000008;
  /** TRUE if tab will exit field (used for richtext only) */
  int FEXT_ALLOWTABBINGOUT = 0x00000010;
  /** TRUE if field is a password field */
  int FEXT_PASSWORD = 0x00000020;
  /** TRUE if an applet should be used for a browser (richtext only) */
  int FEXT_USEAPPLETINBROWSER = 0x00000040;
  /** TRUE if field is a control */
  int FEXT_CONTROL = 0x00000080;
  /**
   * TRUE if this is a formula field which should have item substitution based
   * on items on the form. This is the counterpart to computed formula which
   * is a formula programmatically generated through at-formulas.
   */
  int FEXT_LITERALIZE = 0x00000100;
  /** TRUE if field is a dynamic control */
  int FEXT_CONTROLDYNAMIC = 0x00000200;

  /**
   * TRUE if should run exiting event when value changes. Currently only
   * implemented
   * for native date/time
   */
  int FEXT_RUNEXITINGONCHANGE = 0x00000400;
  /** TRUE if this is a time zone field */
  int FEXT_TIMEZONE = 0x00000800;
  /** TRUE if field has proportional height */
  int FEXT_PROPORTIONALHEIGHT = 0x00004000;
  /** TRUE if field has proportional width */
  int FEXT_PROPORTIONALWIDTH = 0x00008000;
  /** TRUE if a names type field displays im online status */
  int FEXT_SHOWIMSTATUS = 0x02000000;
  /** TRUE if we should use a JS Control in the browser */
  int FEXT_USEJSCTLINBROWSER = 0x04000000;

  /*	The following identifiers indicate the type of helper in use by the
  Keyword and the Name helper/pickers */
  /* these define the VarDataFlags signifying variable length data following struct */
  short CDEXTFIELD_KEYWORDHELPER = 0x0001;
  short CDEXTFIELD_NAMEHELPER = 0x0002;
  short FIELD_HELPER_NONE = 0;
  short FIELD_HELPER_ADDRDLG = 1;
  short FIELD_HELPER_ACLDLG = 2;
  short FIELD_HELPER_VIEWDLG = 3;

  /* DTFlags values (do not change - these values are also stored on disk!) */

  /** Validity bit: If 1, use new DTFMT; if 0, use old TFMT */
  int DT_VALID = 0x8000;
  /** Require 4 digit year on INPUT (not output) */
  int DT_4DIGITYEAR = 0x0001;
  /** Require months be INPUT as letters, not digits (e.g. "jan", not 01) */
  int DT_ALPHAMONTH = 0x0002;
  /** Display time element on output */
  int DT_SHOWTIME = 0x0004;
  /** Display date element on output */
  int DT_SHOWDATE = 0x0008;
  /** Display time on output using 24 hour clock format */
  int DT_24HOUR = 0x0040;
  /** Displays the date as an abbriviated date */
  int DT_SHOWABBREV = 0x0800;
  /** Date element order: Year, Month, Day, Day-of-week */
  int DT_STYLE_YMD = 1;
  /** Date element order: Day-of-week, Month, Day, Year */
  int DT_STYLE_MDY = 2;
  /** Date element order: Day-of-week, Day, Month, Year */
  int DT_STYLE_DMY = 3;
  /** This is where we store the style value in DTFlags */
  int DT_STYLE_MSK = 0x000f0000;

  /* DTFlags2 values (do not change - these values are also stored on disk!) */

  /** Use the 4.X format structure instead of this 5.X format structure */
  int DT_USE_TFMT = 0x0001;
  /**
   * Non-documented flag indicating that display should use the Hijri calendar
   * instead of Gregorian.
   */
  int DT_USE_HIJRI_CALENDAR = 0x0010;

  /* DTYearFmt values (do not change - these values are also stored on disk!) */

  /** 2 digit year */
  byte DT_YFMT_YY = 1;
  /** 4 digit year */
  byte DT_YFMT_YYYY = 2;
  /* The following DTYearFmt values are valid only for Imperial calendars */
  /**
   * Single letter (first letter ) of epoch name and 1 or 2 digit (no leading
   * zeros) year
   */
  byte DT_YFMT_GE = 3;
  /**
   * Single letter (first letter ) of epoch name and 2 digit (with leading zeros,
   * if necessary) year
   */
  byte DT_YFMT_GEE = 4;
  byte DT_YFMT_GGE = 5;
  /** Abbreviated spelling and 2 digit (with leading zeros, if necessary) year */
  byte DT_YFMT_GGEE = 6;
  byte DT_YFMT_GGGE = 7;
  /**
   * fully spelled out epoch name and 2 digit (with leading zeros, if necessary)
   * year
   */
  byte DT_YFMT_GGGEE = 8;

  /* DTDOWFmt values (Day-Of-Week) (do not change - these values are also stored on disk!) */

  byte DT_WFMT_WWW = 1;
  byte DT_WFMT_WWWW = 2;
  /** 3 letter abbreviation inside parenthesis */
  byte DT_WFMT_WWWP = 3;
  /** Spelled out fully inside parenthesis */
  byte DT_WFMT_WWWWP = 4;

  /* DTMonthFmt values (do not change - these values are also stored on disk!) */

  byte DT_MFMT_M = 1;
  byte DT_MFMT_MM = 2;
  byte DT_MFMT_MMM = 3;
  /** DTDayFmt values (do not change - these values are also stored on disk!) */
  byte DT_MFMT_MMMM = 4;

  byte DT_DFMT_D = 1;
  /** DTDShow values (controls what is shown on OUTPUT for date) */
  byte DT_DFMT_DD = 2;

  /* (do not change - these values are also stored on disk!) */

  byte DT_DSHOW_ALL = 1;
  byte DT_DSHOW_YM = 2;
  byte DT_DSHOW_WMD = 3;
  byte DT_DSHOW_W = 4;
  byte DT_DSHOW_M = 5;
  byte DT_DSHOW_MD = 6;
  byte DT_DSHOW_MDY = 7;
  byte DT_DSHOW_D = 8;
  byte DT_DSHOW_Y = 9;

  /* DTDSpecial bit values:  Special handling of date OUTPUT  */
  /* (do not change - these values are also stored on disk!) */

  /** No special handling */
  byte DT_DSPEC_NONE = 0;
  /** Use 'Today', 'Yesterday', 'Tomorrow', when possible */
  byte DT_DSPEC_TODAY = 0x0001;
  /** Always display year on OUTPUT as 4 digit year */
  byte DT_DSPEC_Y4 = 0x0002;
  /** Output 2 digit year for this century; use 4 digit year for other century */
  byte DT_DSPEC_21Y4 = 0x0004;
  /** Display year when not the current year */
  byte DT_DSPEC_CURYR = 0x0008;

  /* DTTShow values (controls what to shown on OUTPUT for time) */
  /* (do not change - these values are also stored on disk!) */

  byte DT_TSHOW_H = 1;
  byte DT_TSHOW_HM = 2;
  byte DT_TSHOW_HMS = 3;
  byte DT_TSHOW_ALL = 4;

  byte NPREF_CLIENT = 0;
  byte NPREF_FIELD = 1;

  /* Currency flags */

  /** The currency symbol follows the value */
  short NCURFMT_SYMFOLLOWS = 0x0001;
  /** Inset space between symbol and value */
  short NCURFMT_USESPACES = 0x0002;
  /** Using 3 letter ISO for currency symbol */
  short NCURFMT_ISOSYMUSED = 0x0004;

  /* Currency selection values */

  byte NCURFMT_COMMON = 0;
  byte NCURFMT_CUSTOM = 1;

  /**
   * field width proportional with font size (const # of chars) (height is part of
   * flags2)
   */
  short EC_FLAG_WIDTH_PROPORTIONAL = 0x0001;

  short CD_SECTION_ELEMENT = 128;
  short CD_FIELDLIMIT_ELEMENT = 129;
  short CD_BUTTONEX_ELEMENT = 130;
  short CD_TABLECELL_ELEMENT = 131;

  short CDKEYWORD_RADIO = 0x0001;
  /*
   * These bits are in use by the   0x0002
   * column value.  The result of   0x0004
   * the shifted bits is (cols - 1) 0x0008
   */
  short CDKEYWORD_COLS_SHIFT = 1;
  short CDKEYWORD_COLS_MASK = 0x000E;
  short CDKEYWORD_FRAME_3D = 0x0000;
  short CDKEYWORD_FRAME_STANDARD = 0x0010;
  short CDKEYWORD_FRAME_NONE = 0x0020;
  short CDKEYWORD_FRAME_MASK = 0x0030;
  short CDKEYWORD_FRAME_SHIFT = 4;
  short CDKEYWORD_KEYWORD_RTL = 0x0040;
  short CDKEYWORD_RO_ACTIVE = 0x0080;

  /* edit control styles */
  int EC_STYLE_EDITMULTILINE = 0x0001;
  int EC_STYLE_EDITVSCROLL = 0x0002;
  int EC_STYLE_EDITPASSWORD = 0x0004;
  /* combobox styles */
  int EC_STYLE_EDITCOMBO = 0x0001;
  /* list box styles */
  int EC_STYLE_LISTMULTISEL = 0x0001;
  /* time control styles */
  int EC_STYLE_CALENDAR = 0x0001;
  int EC_STYLE_TIME = 0x0002;
  int EC_STYLE_DURATION = 0x0004;
  int EC_STYLE_TIMEZONE = 0x0008;
  /* control style is valid */
  int EC_STYLE_VALID = 0x80000000;

  /* other control flags */
  short EC_FLAG_UNITS = 0x000F;
  /** Width/Height are in dialog units, not twips */
  short EC_FLAG_DIALOGUNITS = 0x0001;
  /** Width/Height should be adjusted to fit contents */
  short EC_FLAG_FITTOCONTENTS = 0x0002;
  /** this control is active regardless of docs R/W status */
  short EC_FLAG_ALWAYSACTIVE = 0x0010;
  /** let placeholder automatically fit to window */
  short EC_FLAG_FITTOWINDOW = 0x0020;
  /** position control to top of paragraph */
  short EC_FLAG_POSITION_TOP = 0x0040;
  /** position control to bottom of paragraph */
  short EC_FLAG_POSITION_BOTTOM = 0x0080;
  /** position control to ascent of paragraph */
  short EC_FLAG_POSITION_ASCENT = 0x0100;
  /** position control to height of paragraph */
  short EC_FLAG_POSITION_HEIGHT = 0x0200;

  short EMBEDDEDCTL_VERSION1 = 0;

  short EMBEDDEDCTL_EDIT = 0;
  short EMBEDDEDCTL_COMBO = 1;
  short EMBEDDEDCTL_LIST = 2;
  short EMBEDDEDCTL_TIME = 3;
  short EMBEDDEDCTL_KEYGEN = 4;
  short EMBEDDEDCTL_FILE = 5;
  short EMBEDDEDCTL_TIMEZONE = 6;
  short EMBEDDEDCTL_COLOR = 7;

  /** Unknown or unavailable */
  int ASSISTTRIGGER_TYPE_NONE = 0;
  /** According to time schedule */
  int ASSISTTRIGGER_TYPE_SCHEDULED = 1;
  /** When new mail delivered */
  int ASSISTTRIGGER_TYPE_NEWMAIL = 2;
  /** When documents pasted into database */
  int ASSISTTRIGGER_TYPE_PASTED = 3;
  /** Manually executed */
  int ASSISTTRIGGER_TYPE_MANUAL = 4;
  /** When doc is updated */
  int ASSISTTRIGGER_TYPE_DOCUPDATE = 5;
  /** Synchronous new mail agent executed by router */
  int ASSISTTRIGGER_TYPE_SYNCHNEWMAIL = 6;
  /** When an server event executes */
  int ASSISTTRIGGER_TYPE_EVENT = 7;
  /** On server start */
  int ASSISTTRIGGER_TYPE_SERVERSTART = 8;

  /** Unknown or unavailable */
  short ASSISTSEARCH_TYPE_NONE = 0;
  /** All documents in database */
  short ASSISTSEARCH_TYPE_ALL = 1;
  /** New documents since last run */
  short ASSISTSEARCH_TYPE_NEW = 2;
  /** New or modified docs since last run */
  short ASSISTSEARCH_TYPE_MODIFIED = 3;
  /** Selected documents */
  short ASSISTSEARCH_TYPE_SELECTED = 4;
  /** All documents in view */
  short ASSISTSEARCH_TYPE_VIEW = 5;
  /** All unread documents */
  short ASSISTSEARCH_TYPE_UNREAD = 6;
  /** Prompt user */
  short ASSISTSEARCH_TYPE_PROMPT = 7;
  /** Works on the selectable object */
  short ASSISTSEARCH_TYPE_UI = 8;

  /** Unknown */
  short ASSISTINTERVAL_TYPE_NONE = 0;
  short ASSISTINTERVAL_TYPE_MINUTES = 1;
  short ASSISTINTERVAL_TYPE_DAYS = 2;
  short ASSISTINTERVAL_TYPE_WEEK = 3;
  short ASSISTINTERVAL_TYPE_MONTH = 4;
  short ASSISTINTERVAL_TYPE_EVENT = 5;

  /** TRUE if manual assistant is hidden */
  int ASSISTODS_FLAG_HIDDEN = 0x00000001;
  /** Do not run on weekends */
  int ASSISTODS_FLAG_NOWEEKENDS = 0x00000002;
  /** TRUE if storing highlights */
  int ASSISTODS_FLAG_STOREHIGHLIGHTS = 0x00000004;
  /** TRUE if this is the V3-style mail and paste macro */
  int ASSISTODS_FLAG_MAILANDPASTE = 0x00000008;
  /** TRUE if server to run on should be chosed when enabled */
  int ASSISTODS_FLAG_CHOOSEWHENENABLED = 0x00000010;

  /** Select documents */
  int ACTIONFORMULA_FLAG_SELECTDOCS = 0x00000001;
  /** Create a new copy before modifying */
  int ACTIONFORMULA_FLAG_NEWCOPY = 0x00000002;

  /** Remove document from original database */
  int ACTIONDBCOPY_FLAG_MOVE = 0x00000001;

  /** Create new folder */
  int ACTIONFOLDER_FLAG_NEWFOLDER = 0x00000001;
  /** Folder is private */
  int ACTIONFOLDER_FLAG_PRIVATEFOLDER = 0x00000002;

  /** Replace field value */
  int MODIFYFIELD_FLAG_REPLACE = 0x00000001;
  /** Append field value */
  int MODIFYFIELD_FLAG_APPEND = 0x00000002;

  int ACTIONSENDMAIL_FIELDCOUNT = 5;
  int ACTIONSENDMAIL_TOFIELD = 0;
  int ACTIONSENDMAIL_CCFIELD = 1;
  int ACTIONSENDMAIL_BCCFIELD = 2;
  int ACTIONSENDMAIL_SUBJECTFIELD = 3;
  int ACTIONSENDMAIL_BODYFIELD = 4;

  /** Include matching document */
  int ACTIONSENDMAIL_FLAG_INCLUDEDOC = 0x00000001;
  /** Include doclink to document */
  int ACTIONSENDMAIL_FLAG_INCLUDELINK = 0x00000002;
  /** save copy */
  int ACTIONSENDMAIL_FLAG_SAVEMAIL = 0x00000004;
  /** To field is a formula */
  int ACTIONSENDMAIL_FLAG_TOFORMULA = 0x00000008;
  /** cc field is a formula */
  int ACTIONSENDMAIL_FLAG_CCFORMULA = 0x00000010;
  /** bcc field is a formula */
  int ACTIONSENDMAIL_FLAG_BCCFORMULA = 0x00000020;
  /** Subject field is a formula */
  int ACTIONSENDMAIL_FLAG_SUBJECTFORMULA = 0x00000040;

  /** Summary of docs (with DocLinks) */
  int ACTIONNEWSLETTER_FLAG_SUMMARY = 0x00000001;
  /** Gather at least n before mailing */
  int ACTIONNEWSLETTER_FLAG_GATHER = 0x00000002;
  /** Include all notes when mailing out multiple notes */
  int ACTIONNEWSLETTER_FLAG_INCLUDEALL = 0x00000004;

  /** Reply to all (otherwise, just to sender) */
  int ACTIONREPLY_FLAG_REPLYTOALL = 0x00000001;
  /** Include copy of document */
  int ACTIONREPLY_FLAG_INCLUDEDOC = 0x00000002;
  /** Save copy */
  int ACTIONREPLY_FLAG_SAVEMAIL = 0x00000004;
  /** Do not reply to agent-generated mail */
  int ACTIONREPLY_FLAG_NOAGENTREPLY = 0x00000008;
  /** Only reply once per sender */
  int ACTIONREPLY_FLAG_REPLYONCE = 0x00000010;

  /** the type's data is a formula valid for _TYPE_URL and _TYPE_NAMEDELEMENT */
  int CDRESOURCE_FLAGS_FORMULA = 0x00000001;
  /**
   * the notelink variable length data contains the notelink itself not an index
   * into a $Links items
   */
  int CDRESOURCE_FLAGS_NOTELINKINLINE = 0x00000002;
  /**
   * If specified, the link is to an absolute database or thing. Used to make a
   * hard link to a specific DB.
   */
  int CDRESOURCE_FLAGS_ABSOLUTE = 0x00000004;
  /**
   * If specified, the server and file hint are filled in and should be attempted
   * before trying other copies.
   */
  int CDRESOURCE_FLAGS_USEHINTFIRST = 0x00000008;
  /**
   * the type's data is a canned image file (data/domino/icons/[*].gif) valid for
   * {@code _TYPE_URL && _CLASS_IMAGE only}
   */
  int CDRESOURCE_FLAGS_CANNEDIMAGE = 0x00000010;
  /*	NOTE: _PRIVATE_DATABASE and _PRIVATE_DESKTOP are mutually exclusive. */
  /** the object is private in its database */
  int CDRESOURCE_FLAGS_PRIVATE_DATABASE = 0x00000020;
  /** the object is private in the desktop database */
  int CDRESOURCE_FLAGS_PRIVATE_DESKTOP = 0x00000040;

  /**
   * the replica in the CD resource needs to be obtained via RLGetReplicaID to
   * handle special replica IDs like 'current' mail file.
   */
  int CDRESOURCE_FLAGS_REPLICA_WILDCARD = 0x00000080;
  /** used with class view and folder to mean "Simple View" */
  int CDRESOURCE_FLAGS_SIMPLE = 0x00000100;
  /** open this up in design mode */
  int CDRESOURCE_FLAGS_DESIGN_MODE = 0x00000200;
  /** open this up in preivew mode, if supported. Not saved to disk */
  int CDRESOURCE_FLAGS_PREVIEW = 0x00000400;
  /** we will be doing a search after link opened. Not saved to disk */
  int CDRESOURCE_FLAGS_SEARCH = 0x00000800;

  /**
   * An UNID is added to the end of the hResource that means something to that
   * type - currently used in named element type
   */
  int CDRESOURCE_FLAGS_UNIDADDED = 0x00001000;
  /** document should be in edit mode */
  int CDRESOURCE_FLAGS_EDIT_MODE = 0x00002000;

  /** reserved meaning for each resource link class */
  int CDRESOURCE_FLAGS_RESERVED1 = 0x10000000;
  /** reserved meaning for each resource link class */
  int CDRESOURCE_FLAGS_RESERVED2 = 0x20000000;
  /** reserved meaning for each resource link class */
  int CDRESOURCE_FLAGS_RESERVED3 = 0x40000000;
  /** reserved meaning for each resource link class */
  int CDRESOURCE_FLAGS_RESERVED4 = 0x80000000;

  short CDRESOURCE_TYPE_EMPTY = 0;
  short CDRESOURCE_TYPE_URL = 1;
  short CDRESOURCE_TYPE_NOTELINK = 2;
  short CDRESOURCE_TYPE_NAMEDELEMENT = 3;
  /** Currently not written to disk only used in RESOURCELINK */
  short CDRESOURCE_TYPE_NOTEIDLINK = 4;
  /**
   * This would be used in conjunction with the formula flag. The formula is
   * an @Command that would
   * perform some action, typically it would also switch to a Notes UI element.
   * This will be used to
   * reference the replicator page and other UI elements.
   */
  short CDRESOURCE_TYPE_ACTION = 5;
  /** Currently not written to disk only used in RESOURCELINK */
  short CDRESOURCE_TYPE_NAMEDITEMELEMENT = 6;

  short CDRESOURCE_CLASS_UNKNOWN = 0;
  short CDRESOURCE_CLASS_DOCUMENT = 1;
  short CDRESOURCE_CLASS_VIEW = 2;
  short CDRESOURCE_CLASS_FORM = 3;
  short CDRESOURCE_CLASS_NAVIGATOR = 4;
  short CDRESOURCE_CLASS_DATABASE = 5;
  short CDRESOURCE_CLASS_FRAMESET = 6;
  short CDRESOURCE_CLASS_PAGE = 7;
  short CDRESOURCE_CLASS_IMAGE = 8;
  short CDRESOURCE_CLASS_ICON = 9;
  short CDRESOURCE_CLASS_HELPABOUT = 10;
  short CDRESOURCE_CLASS_HELPUSING = 11;
  short CDRESOURCE_CLASS_SERVER = 12;
  short CDRESOURCE_CLASS_APPLET = 13;
  /** A compiled formula someplace */
  short CDRESOURCE_CLASS_FORMULA = 14;
  short CDRESOURCE_CLASS_AGENT = 15;
  /** a file on disk (file:) */
  short CDRESOURCE_CLASS_FILE = 16;
  /** A file attached to a note */
  short CDRESOURCE_CLASS_FILEATTACHMENT = 17;
  short CDRESOURCE_CLASS_OLEEMBEDDING = 18;
  /** A shared image resource */
  short CDRESOURCE_CLASS_SHAREDIMAGE = 19;
  short CDRESOURCE_CLASS_FOLDER = 20;
  /**
   * An old (4.6) or new style portfolio. Which gets incorporated into the
   * bookmark bar as a
   * tab, rather than getting opened as a database.
   */
  short CDRESOURCE_CLASS_PORTFOLIO = 21;
  short CDRESOURCE_CLASS_OUTLINE = 22;

  short CDBORDERSTYLE_NONE = 0;
  short CDBORDERSTYLE_SOLID = 1;
  short CDBORDERSTYLE_DOUBLE = 2;
  short CDBORDERSTYLE_INSET = 3;
  short CDBORDERSTYLE_OUTSET = 4;
  short CDBORDERSTYLE_RIDGE = 5;
  short CDBORDERSTYLE_GROOVE = 6;
  short CDBORDERSTYLE_DOTTED = 7;
  short CDBORDERSTYLE_DASHED = 8;
  short CDBORDERSTYLE_PICTURE = 9;
  short CDBORDERSTYLE_GRAPHIC = 10;
  
  short CDBORDER_FLAGS_DROP_SHADOW = 0x0001;
  
  /** Width is calculated based on text length and image width */
  byte ACTIONBAR_BUTTON_WIDTH_DEFAULT = 0;
  /** Width is at least button background image width or wider if needed to fit text and image */
  byte ACTIONBAR_BUTTON_WIDTH_BACKGROUND = 1;
  /** Width is set to value in wBtnWidthAbsolute */
  byte ACTIONBAR_BUTTON_WIDTH_ABSOLUTE = 2;
  
  short ACTION_SET_3D_ONMOUSEOVER = 0;
  short ACTION_SET_3D_ALWAYS = 1;
  short ACTION_SET_3D_NEVER = 2;
  short ACTION_SET_3D_NOTES = 3;
  
  short ACTIONBAR_BACKGROUND_REPEATONCE = 1;
  short ACTIONBAR_BACKGROUND_REPEATVERT = 2;
  short ACTIONBAR_BACKGROUND_REPEATHORIZ = 3;
  short ACTIONBAR_BACKGROUND_TILE = 4;
  short ACTIONBAR_BACKGROUND_CENTER_TILE = 5;
  short ACTIONBAR_BACKGROUND_REPEATSIZE = 6;
  short ACTIONBAR_BACKGROUND_REPEATCENTER = 7;
  
  byte ACTIONBAR_BUTTON_TEXT_LEFT = 0;
  byte ACTIONBAR_BUTTON_TEXT_CENTER = 1;
  byte ACTIONBAR_BUTTON_TEXT_RIGHT = 2;
  
  int ACTIONBAREXT_WIDTH_STYLE_VALID_FLAG = 0x00000001;

  /** set when "auto" or "inherit" is set */
  short CDLENGTH_UNITS_UNKNOWN = 0;
  short CDLENGTH_UNITS_TWIPS = 1;
  short CDLENGTH_UNITS_PIXELS = 2;
  short CDLENGTH_UNITS_PERCENT = 3;
  short CDLENGTH_UNITS_EMS = 4;
  short CDLENGTH_UNITS_EXS = 5;
  /** average width of a character based on the font */
  short CDLENGTH_UNITS_CHARS = 6;
  
  int MAXFACESIZE = 32;
  
  short ACTION_CONTROL_TYPE_BUTTON = 0;
  short ACTION_CONTROL_TYPE_CHECKBOX = 1;
  short ACTION_CONTROL_TYPE_MENU_SEPARATOR = 2;
  
  /** Flag in CDTARGET for formula target */
  short FLAG_TARGET_IS_FORMULA = 0x0001;
  
  short PLATFORM_TYPE_CLIENT_ODS = 1;
  short PLATFORM_TYPE_WEB_ODS = 2;
  

  short HTML_EVENT_ONCLICK = 1;
  short HTML_EVENT_ONDBLCLICK = 2;
  short HTML_EVENT_ONMOUSEDOWN = 3;
  short HTML_EVENT_ONMOUSEUP = 4;
  short HTML_EVENT_ONMOUSEOVER = 5;
  short HTML_EVENT_ONMOUSEMOVE = 6;
  short HTML_EVENT_ONMOUSEOUT = 7;
  short HTML_EVENT_ONKEYPRESS = 8;
  short HTML_EVENT_ONKEYDOWN = 9;
  short HTML_EVENT_ONKEYUP = 10;
  short HTML_EVENT_ONFOCUS = 11;
  short HTML_EVENT_ONBLUR = 12;
  short HTML_EVENT_ONLOAD = 13;
  short HTML_EVENT_ONUNLOAD = 14;
  short HTML_EVENT_HEADER = 15;
  short HTML_EVENT_ONSUBMIT = 16;
  short HTML_EVENT_ONRESET = 17;
  short HTML_EVENT_ONCHANGE = 18;
  short HTML_EVENT_ONERROR = 19;
  short HTML_EVENT_ONHELP = 20;
  short HTML_EVENT_ONSELECT = 21;
  /** This isn't really an event */
  short HTML_EVENT_LIBRARY = 22;
  
  short HTML_EVENT_CLIENT_FORM_QUERYOPEN = 0x100;
  short HTML_EVENT_CLIENT_FORM_QUERYMODE = 0x101;
  short HTML_EVENT_CLIENT_FORM_POSTMODE = 0x102;
  short HTML_EVENT_CLIENT_FORM_POSTRECALC = 0x103;
  short HTML_EVENT_CLIENT_FORM_POSTSAVE = 0x104;
  short HTML_EVENT_CLIENT_FORM_POSTSEND = 0x105;
  short HTML_EVENT_CLIENT_FORM_QUERYRECALC = 0x106;
  short HTML_EVENT_CLIENT_FORM_QUERYSEND = 0x107;
  short HTML_EVENT_CLIENT_VIEW_QUERYOPEN = 0x108;
  short HTML_EVENT_CLIENT_VIEW_POSTOPEN = 0x109;
  short HTML_EVENT_CLIENT_VIEW_REGIONDBLCLK = 0x10a;
  short HTML_EVENT_CLIENT_VIEW_QUERYOPENDOC = 0x10b;
  short HTML_EVENT_CLIENT_VIEW_QUERYRECALC = 0x10c;
  short HTML_EVENT_CLIENT_VIEW_QUERYADDTOFOLDER = 0x10d;
  short HTML_EVENT_CLIENT_VIEW_QUERYPASTE = 0x10e;
  short HTML_EVENT_CLIENT_VIEW_POSTPASTE = 0x10f;
  short HTML_EVENT_CLIENT_VIEW_QUERYDRAGDROP = 0x110;
  short HTML_EVENT_CLIENT_VIEW_POSTDRAGDROP = 0x111;
  short HTML_EVENT_CLIENT_VIEW_QUERYCLOSE = 0x112;
  short HTML_EVENT_CLIENT_ONOBJECTEXECUTE = 0x113;
  short HTML_EVENT_CLIENT_DB_QUERYOPEN = 0x114;
  short HTML_EVENT_CLIENT_DB_POSTOPEN = 0x115;
  short HTML_EVENT_CLIENT_DB_DOCDELETE = 0x116;
  short HTML_EVENT_CLIENT_DB_QUERYCLOSE = 0x117;
  short HTML_EVENT_CLIENT_DB_QUERYDELETE = 0x118;
  short HTML_EVENT_CLIENT_DB_QUERYUNDELETE = 0x119;
  short HTML_EVENT_CLIENT_DB_QUERYDRAGDROP = 0x11a;
  short HTML_EVENT_CLIENT_DB_POSTDRAGDROP = 0x11b;
  short HTML_EVENT_CLIENT_VIEW_QUERYENTRYRESIZE = 0x11c;
  short HTML_EVENT_CLIENT_VIEW_POSTENTRYRESIZE = 0x11d;
  short HTML_EVENT_CLIENT_VIEW_INVIEWEDIT = 0x11e;
  short HTML_EVENT_CLIENT_SCHED_INTERVALCHANGE = 0x11f;
  short HTML_EVENT_CLIENT_DB_QUERYARCHIVEDRAGDROP = 0x120;
  short HTML_EVENT_CLIENT_DB_POSTARCHIVEDRAGDROP = 0x121;
  short HTML_EVENT_CLIENT_SCHED_SUGGESTIONSAVAIL = 0x122;
  short HTML_EVENT_CLIENT_VIEW_ONSELECT = 0x123;
  short HTML_EVENT_CLIENT_VIEW_ONFOCUS = 0x124;
  short HTML_EVENT_CLIENT_VIEW_ONBLUR = 0x125;
  /** Non-documented flag for the OnSize client event */
  short HTML_EVENT_CLIENT_FORM_ONSIZE = 0x0127;
  
  short ACTION_FORMULA = 0;
  short ACTION_CANNED_ACTION = 1;
  short ACTION_LOTUS_SCRIPT = 2;
  short ACTION_MISC = 3;
  short ACTION_COLLECTION_RULE = 4;
  short ACTION_JAVA_FILE = 5;
  short ACTION_JAVA = 6;
  short ACTION_JAVASCRIPT = 7;
  /** Use same JavaScript for both client and web */
  short ACTION_JAVASCRIPT_COMMON = 8;
  short ACTION_UNUSED = 9;
  /** fullpack search on 12/10/00 showed no use of this */
  short ACTION_SECTION_EDIT = 10;
  short ACTION_NULL = 11;
  /** Obj properties (initially for OLE properties) */
  short ACTION_PROPERTIES = 12;
  /** The code is JSP code */
  short ACTION_JSP = 13;
  /** The code is HTML */
  short ACTION_HTML = 14;
  /** The end of the 'real' types */
  short ACTION_MAX = 15;
  short ACTION_OTHER = 98;
  short ACTION_UNKNOWN = 99;
  

  int ACTION_SHOW_IN_MENU = 0x00000001;
  int ACTION_SHOW_IN_BAR = 0x00000002;
  int ACTION_SHOW_WHEN_PREVIEWING = 0x00000004;
  int ACTION_SHOW_WHEN_READING = 0x00000008;
  int ACTION_SHOW_WHEN_EDITING = 0x00000010;
  int ACTION_SHOW_ON_OLE_LAUNCH = 0x00000020;
  int ACTION_OLE_CLOSE_WHEN_CHOSEN = 0x00000040;
  int ACTION_NO_FORMULA = 0x00000080;
  int ACTION_SHOW_WHEN_PREVEDITING = 0x00000100;
  int ACTION_OLE_DOC_WINDOW_TO_FRONT = 0x00001000;
  int ACTION_HIDE_FROM_NOTES = 0x00002000;
  int ACTION_HIDE_FROM_WEB = 0x00004000;
  int ACTION_READING_ORDER_RTL = 0x00008000;
  /** action is shared */
  int ACTION_SHARED = 0x00010000;
  /** action has been modified (not saved on disk) */
  int ACTION_MODIFIED = 0x00020000;
  /** flag not saved on disk */
  int ACTION_ALWAYS_SHARED = 0x00040000;
  int ACTION_ALIGN_ICON_RIGHT = 0x00080000;
  int ACTION_IMAGE_RESOURCE_ICON = 0x00100000;
  int ACTION_FRAME_TARGET = 0x00400000;
  int ACTION_TEXT_ONLY_IN_MENU = 0x00800000;
  /** Show button on opposite side from action bar direction */
  int ACTION_BUTTON_TO_RIGHT = 0x01000000;
  /** action is hidden from mobile */
  int ACTION_HIDE_FROM_MOBILE = 0x04000000;
  int ACTION_SHOW_IN_POPUPMENU = 0x10000000;
  /** LI: 4602.02, Provide support for "Split button" for java action bar */
  int ACTION_MAKE_SPLIT_BUTTON = 0x20000000;
  /**
   * Non-documented (and speculatively-named) flag indicating that the action should be
   * included in the mobile-specific actions menu.
   */
  int ACTION_SHOW_IN_MOBILE_ACTIONS = 0x80000000;
  
  int ACTION_ODS_FLAG_MASK = 0xF5F9F1FF; 
  
  /**
   * Non-documented (and speculatively-named) flag used in CDACTIONEXT.dwFlags to indicate
   * that the action should be included in the "swipe left" actions on mobile devices.
   */
  int ACTIONEXT_INCLUDE_IN_SWIPE_LEFT = 0x00000001;
  /**
   * Non-documented (and speculatively-named) flag used in CDACTIONEXT.dwFlags to indicate
   * that the action should be included in the "swipe right" actions on mobile devices.
   */
  int ACTIONEXT_INCLUDE_IN_SWIPE_RIGHT = 0x00000002;

  short ACTION_RUN_FORMULA = 1;
  short ACTION_RUN_SCRIPT = 2;
  short ACTION_RUN_AGENT = 3;
  short ACTION_OLDSYS_COMMAND = 4;
  short ACTION_SYS_COMMAND = 5;
  short ACTION_PLACEHOLDER = 6;
  short ACTION_RUN_JAVASCRIPT = 7;

  /** Bitmap Uses &gt; 16 colors or &gt; 4 grey scale levels */
  short CDBITMAP_FLAG_REQUIRES_PALETTE = 1;
  /**
   * Initialized by import code for "first time" importing of bitmaps
   * from clipboard or file, to tell Notes that it should compute whether
   * or not to use a color palette or not.  All imports and API programs
   * should initially set this bit to let the Editor compute whether it
   * needs the palette or not.
   */
  short CDBITMAP_FLAG_COMPUTE_PALETTE = 2;
  
  byte CDTC_S_Left   = 0;
  byte CDTC_M_Left   = 0x03;
  byte CDTC_S_Right  = 2;
  byte CDTC_M_Right  = 0x0c;
  byte CDTC_S_Top    = 4;
  byte CDTC_M_Top    = 0x30;
  byte CDTC_S_Bottom = 6;
  byte CDTC_M_Bottom = (byte)0xc0;
  byte TABLE_BORDER_NONE = 0;
  byte TABLE_BORDER_SINGLE = 1;
  byte TABLE_BORDER_DOUBLE = 2;
  
  /**  True if background color  */
  byte CDTABLECELL_USE_BKGCOLOR = 0x01;
  /**  True if version 4.2 or after  */
  byte CDTABLECELL_USE_V42BORDERS = 0x02;
  /**  True if cell is spanned  */
  byte CDTABLECELL_INVISIBLEH = 0x04;
  /**  True if cell is spanned  */
  byte CDTABLECELL_INVISIBLEV = 0x08;
  /**  True if gradient color  */
  byte CDTABLECELL_USE_GRADIENT = 0x10;
  /**  True if contents centered vertically  */
  byte CDTABLECELL_VALIGNCENTER = 0x20;
  /**  True if gradient should go left to right  */
  byte CDTABLECELL_GRADIENT_LTR = 0x40;
  /**  True if contents bottomed vertically  */
  byte CDTABLECELL_VALIGNBOTTOM = (byte)0x80;
  
  short CDTC_S_V42_Left = 0;
  short CDTC_M_V42_Left = 0x000f;
  short CDTC_S_V42_Right = 4;
  short CDTC_M_V42_Right = 0x00f0;
  short CDTC_S_V42_Top = 8;
  short CDTC_M_V42_Top = 0x0f00;
  short CDTC_S_V42_Bottom = 12;
  short CDTC_M_V42_Bottom = (short)0xf000;
  
  /* more table cell flags stored via a CDTABLECELLDATAFLAGS record */
  int CDTABLECELL_COLUMN_HEADER = 0x00000001;
  int CDTABLECELL_ROW_HEADER    = 0x00000002;
  
  /**  True if automatic cell width calculation  */
  int CDPRETABLE_AUTO_CELL_WIDTH = 0x00000001;
  int CDPRETABLE_DONTWRAP = 0x00000002;
  int CDPRETABLE_DROPSHADOW = 0x00000004;
  int CDPRETABLE_FIELDDRIVEN = 0x00000008;
  int CDPRETABLE_V4SPACING = 0x00000010;
  int CDPRETABLE_USEBORDERCOLOR = 0x00000020;
  /**  True if the table width equal to window width  */
  int CDPRETABLE_WIDTHSAMEASWINDOW = 0x00000040;
  /**  True if field driven table should also show tabs  */
  int CDPRETABLE_SHOWTABS = 0x00000080;
  int CDPRETABLE_SHOWTABSONLEFT = 0x00000100;
  int CDPRETABLE_SHOWTABSONBOTTOM = 0x00000200;
  int CDPRETABLE_SHOWTABSONRIGHT = 0x00000400;
  

  byte CDTABLEVIEWER_ONCLICK = 1;
  byte CDTABLEVIEWER_ONLOADTIMER = 2;
  byte CDTABLEVIEWER_ONLOADCYCLEONCE = 3;
  byte CDTABLEVIEWER_TABS = 4;
  byte CDTABLEVIEWER_FIELDDRIVEN = 5;
  byte CDTABLEVIEWER_CYCLEONCE = 6;
  byte CDTABLEVIEWER_CAPTIONS = 8;
  byte CDTABLEVIEWER_LAST = 8;
  
  int MAXTABS = 20;
  
  byte TAB_LEFT        = 0; /* default - flush left starting at tab pos. */
  byte TAB_RIGHT       = 1; /* text is right justified before tab pos. */
  byte TAB_DECIMAL     = 2; /* text is placed so that decimal point is aligned with tab pos. */
  byte TAB_CENTER      = 3; /* text is centered around tab pos. */
  byte TAB_DEFAULT     = TAB_LEFT;
  
  /**  True if automatic cell width calculation  */
  short CDTABLE_AUTO_CELL_WIDTH = 0x0001;
  /**  True if the table was created in v4  */
  short CDTABLE_V4_BORDERS = 0x0002;
  /**  True if the table uses embossed borders  */
  short CDTABLE_3D_BORDER_EMBOSS = 0x0004;
  /**  True if the table uses extruded borders  */
  short CDTABLE_3D_BORDER_EXTRUDE = 0x0008;
  /**  True if the table reading order is right to left  */
  short CDTABLE_BIDI_RTLTABLE = 0x0010;
  /**  True if the table alignment is right  */
  short CDTABLE_ALIGNED_RIGHT = 0x0020;
  /**  True if the table is collapsible to one row  */
  short CDTABLE_COLLAPSIBLE = 0x0040;
  short CDTABLE_LEFTTOP = 0x0080;
  short CDTABLE_TOP = 0x0100;
  short CDTABLE_LEFT = 0x0200;
  short CDTABLE_ALTERNATINGCOLS = 0x0400;
  short CDTABLE_ALTERNATINGROWS = 0x0800;
  short CDTABLE_RIGHTTOP = 0x2000;
  short CDTABLE_RIGHT = 0x4000;
  /**  all styles on means solid color */
  short CDTABLE_SOLID = 0x6f80;
  short CDTABLE_TEMPLATEBITS = 0x6f80;
  /**  True if the table alignment is center  */
  short CDTABLE_ALIGNED_CENTER = 0x1000;
  /**  True if the table rows text flows cell to cell  */
  short CDTABLE_TEXTFLOWS = (short)0x8000;

  int LAYOUT_FLAG_SHOWBORDER = 0x00000001;
  int LAYOUT_FLAG_SHOWGRID = 0x00000002;
  int LAYOUT_FLAG_SNAPTOGRID = 0x00000004;
  int LAYOUT_FLAG_3DSTYLE = 0x00000008;
  int LAYOUT_FLAG_RTL = 0x00000010;
  int LAYOUT_FLAG_DONTWRAP = 0x00000020;
  

  int PLACEHOLDER_FLAG_FITTOWINDOW = 0x00000001;
  int PLACEHOLDER_FLAG_DRAWBACKGROUND = 0x00000002;
  int PLACEHOLDER_FLAG_USEPERCENTAGE = 0x00000004;
  int PLACEHOLDER_FLAG_SCROLLBARS = 0x00000008;
  int PLACEHOLDER_FLAG_CONTENTSONLY = 0x00000010;
  int PLACEHOLDER_FLAG_ALIGNCENTER = 0x00000020;
  int PLACEHOLDER_FLAG_ALIGNRIGHT = 0x00000040;
  int PLACEHOLDER_FLAG_FITTOWINDOWHEIGHT = 0x00000080;
  int PLACEHOLDER_FLAG_TILEIMAGE = 0x00000100;
  int PLACEHOLDER_FLAG_DISPLAYHORZ = 0x00000200;
  int PLACEHOLDER_FLAG_DONTEXPANDSELECTIONS = 0x00000400;
  int PLACEHOLDER_FLAG_EXPANDCURRENT = 0x00000800;
  int PLACEHOLDER_FLAG_FITCONTENTSWIDTH = 0x00001000;
  int PLACEHOLDER_FLAG_FIXEDWIDTH = 0x00002000;
  int PLACEHOLDER_FLAG_FIXEDHEIGHT = 0x00004000;
  int PLACEHOLDER_FLAG_FITCONTENTS = 0x00008000;
  int PLACEHOLDER_FLAG_PROP_WIDTH = 0x00010000;
  int PLACEHOLDER_FLAG_PROP_BOTH = 0x00020000;
  int PLACEHOLDER_FLAG_SCROLLERS = 0x00040000;
  
  short PLACEHOLDER_ALIGN_LEFT = 0;
  short PLACEHOLDER_ALIGN_CENTER = 1;
  short PLACEHOLDER_ALIGN_RIGHT = 2;
  
  short CDTABLELABEL_ROWLABEL = 0x0001;
  short CDTABLELABEL_TABLABEL = 0x0002;
  
  int BARREC_DISABLED_FOR_NON_EDITORS   = 1;
  int BARREC_EXPANDED                   = 2;
  int BARREC_PREVIEW                    = 4;
  int BARREC_BORDER_INVISIBLE           = 0x1000;
  int BARREC_ISFORMULA                  = 0x2000;
  int BARREC_HIDE_EXPANDED              = 0x4000;
  int BARREC_POSTREPLYSECTION           = 0x8000;
  
  int BARREC_INTENDED       = 0x1000000;
  int BARREC_HAS_COLOR      = 0x4000000;
  
  /** This constant represents the 28 used as a magic number for shifting to read BARREC border values */
  int BARREC_BORDER_SHIFT         = 28;
  
  int BARREC_BORDER_MASK          = 0x70000000;
  int BARREC_BORDER_SHADOW        = 0;
  int BARREC_BORDER_NONE          = 1;
  int BARREC_BORDER_SINGLE        = 2;
  int BARREC_BORDER_DOUBLE        = 3;
  int BARREC_BORDER_TRIPLE        = 4;
  int BARREC_BORDER_TWOLINE       = 5;
  int BARREC_BORDER_WINDOWCAPTION = 6;
  int BARREC_BORDER_OTHER         = 7;
  int BARREC_BORDER_GRADIENT      = 7;
  int BARREC_BORDER_TAB           = 8;
  int BARREC_BORDER_DIAG          = 9;
  int BARREC_BORDER_DUOCOLOR      = 10;

  int BARREC_INDENTED = 0x80000000;
  
  byte REPEAT_UNKNOWN  = 0;
  byte REPEAT_ONCE     = 1;
  byte REPEAT_VERT     = 2;
  byte REPEAT_HORIZ    = 3;
  byte REPEAT_BOTH     = 4;
  byte REPEAT_SIZE     = 5;
  byte REPEAT_CENTER   = 6;
  
  short BUTTON_UNUSED = 0x0000;
  short BUTTON_RUNFLAG_SCRIPT = 0x0001;
  short BUTTON_RUNFLAG_NOWRAP = 0x0002;
  short BUTTON_ODS_MASK = 0x7F02;
  short BUTTON_RUNFLAG_RTL = 0x0100;
  short BUTTON_RUNFLAG_FIXED = 0x0200;
  short BUTTON_RUNFLAG_MINIMUM = 0x0400;
  short BUTTON_RUNFLAG_CONTENT = 0x0800;
  short BUTTON_RUNFLAG_PROPORTIONAL = 0x4000;
  /**  button has focus   */
  short BUTTON_FOCUS_ON = (short)0x8000;
  
  short BUTTON_RUNFLAG_WIDTH_MASK = (BUTTON_RUNFLAG_FIXED | BUTTON_RUNFLAG_MINIMUM | BUTTON_RUNFLAG_CONTENT | BUTTON_RUNFLAG_PROPORTIONAL);

  short BUTTON_EDGE_ROUNDED = 0x1000;
  short BUTTON_EDGE_SQUARE = 0x2000;
  
  int MAXREGIONNAME = 35;
  

  int LAYOUT_TEXT_FLAG_TRANS = 0x10000000;
  int LAYOUT_TEXT_FLAG_LEFT = 0x00000000;
  int LAYOUT_TEXT_FLAG_CENTER = 0x20000000;
  int LAYOUT_TEXT_FLAG_RIGHT = 0x40000000;
  int LAYOUT_TEXT_FLAG_ALIGN_MASK = 0x60000000;
  int LAYOUT_TEXT_FLAG_VCENTER = 0x80000000;
  int LAYOUT_TEXT_FLAG_LTR = 0x01000000;
  int LAYOUT_TEXT_FLAG_RTL = 0x02000000;
  int LAYOUT_TEXT_FLAG_RO_MASK = 0x03000000;
  int LAYOUT_TEXT_FLAGS_MASK = 0xF0000000;
}
