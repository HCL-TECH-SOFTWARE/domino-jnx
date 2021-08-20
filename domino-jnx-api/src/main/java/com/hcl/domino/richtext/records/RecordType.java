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
package com.hcl.domino.richtext.records;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.hcl.domino.richtext.RichTextConstants;

/**
 * Enum with all available CD record types, which are the building blocks of
 * Notes rich text.
 */
public enum RecordType {

  /* Signatures for Composite Records in items of data type COMPOSITE */

  SRC(RichTextConstants.SIG_CD_SRC, 1),
  IMAGEHEADER2(RichTextConstants.SIG_CD_IMAGEHEADER2, 1, CDImageHeader2.class),
  PDEF_MAIN(RichTextConstants.SIG_CD_PDEF_MAIN, 1),
  PDEF_TYPE(RichTextConstants.SIG_CD_PDEF_TYPE, 1),
  PDEF_PROPERTY(RichTextConstants.SIG_CD_PDEF_PROPERTY, 1),
  PDEF_ACTION(RichTextConstants.SIG_CD_PDEF_ACTION, 1),
  /**
   * This definition was added to provide a unique signature for table cell
   * dataflags.<br>
   * As an alternative, the standard CDDATAFLAGS structure can also be used.
   */
  TABLECELL_DATAFLAGS(RichTextConstants.SIG_CD_TABLECELL_DATAFLAGS, 1),
  /** This CD record defines properties of an embedded contact list */
  EMBEDDEDCONTACTLIST(RichTextConstants.SIG_CD_EMBEDDEDCONTACTLIST, 1),
  /** Used to ignore a block of CD records for a particular version of Notes */
  IGNORE(RichTextConstants.SIG_CD_IGNORE, 1),
  TABLECELL_HREF2(RichTextConstants.SIG_CD_TABLECELL_HREF2, 1),
  HREFBORDER(RichTextConstants.SIG_CD_HREFBORDER, 1),
  /**
   * This record was added because the Pre Table Begin Record can not be expanded
   * and R6 required
   * more data to be stored.<br>
   * This CD record specifies extended table properties
   */
  TABLEDATAEXTENSION(RichTextConstants.SIG_CD_TABLEDATAEXTENSION, 1),
  /**
   * This CD record defines properties of an embedded calendar control (date
   * picker).
   */
  EMBEDDEDCALCTL(RichTextConstants.SIG_CD_EMBEDDEDCALCTL, 1),
  /**
   * New field attributes have been added in Notes/Domino 6.<br>
   * To preserve compatibility with existing applications, the new attributes have
   * been
   * placed in this extension to the CDACTION record. This record is optional, and
   * may not
   * be present in the $Body item of the form note
   */
  ACTIONEXT(RichTextConstants.SIG_CD_ACTIONEXT, 1, CDActionExt.class),
  EVENT_LANGUAGE_ENTRY(RichTextConstants.SIG_CD_EVENT_LANGUAGE_ENTRY, 1, CDEventEntry.class),
  /**
   * This structure defines the file segment data of a Cascading Style Sheet (CSS)
   * and
   * follows a CDFILEHEADER structure
   */
  FILESEGMENT(RichTextConstants.SIG_CD_FILESEGMENT, 1, CDFileSegment.class),
  /**
   * This structure is used to define a Cascading Style Sheet (CSS) that is part
   * of a Domino database.
   */
  FILEHEADER(RichTextConstants.SIG_CD_FILEHEADER, 1, CDFileHeader.class),
  /**
   * Contains collapsible section, button type, style sheet or field limit
   * information for Notes/Domino 6.
   * A CD record (CDBAR, CDBUTTON, CDBORDERINFO, CDFIELDHINT, etc.) may be
   * followed by a CDDATAFLAGS structure.
   */
  DATAFLAGS(RichTextConstants.SIG_CD_DATAFLAGS, 1, CDDataFlags.class),
  /**
   * This CD Record gives information pertaining to Background Properties for a
   * box. A
   * CDBACKGROUNDPROPERTIES record may be encapsulated within a CDBEGINRECORD and
   * CDENDRECORD.
   */
  BACKGROUNDPROPERTIES(RichTextConstants.SIG_CD_BACKGROUNDPROPERTIES, 1),
  EMBEDEXTRA_INFO(RichTextConstants.SIG_CD_EMBEDEXTRA_INFO, 1),
  CLIENT_BLOBPART(RichTextConstants.SIG_CD_CLIENT_BLOBPART, 1, CDBlobPart.class),
  CLIENT_EVENT(RichTextConstants.SIG_CD_CLIENT_EVENT, 1, CDEvent.class),
  BORDERINFO_HS(RichTextConstants.SIG_CD_BORDERINFO_HS, 1),
  LARGE_PARAGRAPH(RichTextConstants.SIG_CD_LARGE_PARAGRAPH, 1),
  EXT_EMBEDDEDSCHED(RichTextConstants.SIG_CD_EXT_EMBEDDEDSCHED, 1),
  /**
   * This CD record contains size information for a layer box. The units (pixels,
   * twips, etc.)
   * for the Width and Height are set in the "Units" members of the "Top", "Left",
   * "Bottom"
   * and "Right" members of the CDPOSITIONING structure.
   */
  BOXSIZE(RichTextConstants.SIG_CD_BOXSIZE, 1),
  /** This CD record contains position information for a layer box. */
  POSITIONING(RichTextConstants.SIG_CD_POSITIONING, 1),
  /**
   * The definition for a layer on a form is stored as CD records in the $Body
   * item of the form note.<br>
   * A layer is comprised of a Layer Object Run (pointer to box that represents
   * the layer), Box Run and Position Data.
   */
  LAYER(RichTextConstants.SIG_CD_LAYER, 1),
  /**
   * This CD Record gives information pertaining to data connection resource
   * information in a field or form.
   */
  DECSFIELD(RichTextConstants.SIG_CD_DECSFIELD, 1, CDDECSField.class),
  SPAN_END(RichTextConstants.SIG_CD_SPAN_END, 1),
  SPAN_BEGIN(RichTextConstants.SIG_CD_SPAN_BEGIN, 1),
  /**
   * A field or a run of rich text may contain language information. This language
   * information is stored in
   * a $TEXTPROPERTIES item. The start or end of a span of language information is
   * indicated by a
   * CDSPANRECORD structure. The $TEXTPROPERTIES item and the CDSPANRECORD
   * structures may be stored on a
   * form note and/or a document.
   */
  TEXTPROPERTIESTABLE(RichTextConstants.SIG_CD_TEXTPROPERTIESTABLE, 1),
  HREF2(RichTextConstants.SIG_CD_HREF2, 1, CDResource.class),
  BACKGROUNDCOLOR(RichTextConstants.SIG_CD_BACKGROUNDCOLOR, 1),
  /**
   * This CD Record gives information pertaining to shared resources and/or shared
   * code in a form.<br>
   * A CDINLINE record may be preceded by a CDBEGINRECORD and followed by a
   * CDRESOURCE and then a CDENDRECORD.
   */
  INLINE(RichTextConstants.SIG_CD_INLINE, 1),
  V6HOTSPOTBEGIN_CONTINUATION(RichTextConstants.SIG_CD_V6HOTSPOTBEGIN_CONTINUATION, 1),
  TARGET_DBLCLK(RichTextConstants.SIG_CD_TARGET_DBLCLK, 1),
  /** This CD record defines the properties of a caption for a grapic record. */
  CAPTION(RichTextConstants.SIG_CD_CAPTION, 1, CDCaption.class),
  /** Color properties to various HTML Links. */
  LINKCOLORS(RichTextConstants.SIG_CD_LINKCOLORS, 1, CDLinkColors.class),
  TABLECELL_HREF(RichTextConstants.SIG_CD_TABLECELL_HREF, 1),
  /**
   * This CD record defines the Action Bar attributes. It is an extension of the
   * CDACTIONBAR record.<br>
   * It is found within a $V5ACTIONS item and is preceded by a CDACTIONBAR record.
   */
  ACTIONBAREXT(RichTextConstants.SIG_CD_ACTIONBAREXT, 1, CDActionBarExt.class),
  /**
   * This CD record describes the HTML field properties, ID, Class, Style, Title,
   * Other and Name associated for any given field defined within a Domino Form
   */
  IDNAME(RichTextConstants.SIG_CD_IDNAME, 1, CDIDName.class),
  TABLECELL_IDNAME(RichTextConstants.SIG_CD_TABLECELL_IDNAME, 1),
  /**
   * This structure defines the image segment data of a JPEG or GIF image and
   * follows a CDIMAGEHEADER structure.<br>
   * The number of segments in the image is contained in the CDIMAGEHEADER and
   * specifies the number of
   * CDIMAGESEGMENT structures to follow. An image segment size is 10250 bytes.
   */
  IMAGESEGMENT(RichTextConstants.SIG_CD_IMAGESEGMENT, 1, CDImageSegment.class),
  /**
   * This structure is used to define a JPEG or GIF Image that is part of a Domino
   * document.<br>
   * The CDIMAGEHEADER structure follows a CDGRAPHIC structure.<br>
   * CDIMAGESEGMENT structure(s) then follow the CDIMAGEHEADER.
   */
  IMAGEHEADER(RichTextConstants.SIG_CD_IMAGEHEADER, 1, CDImageHeader.class),
  V5HOTSPOTBEGIN(RichTextConstants.SIG_CD_V5HOTSPOTBEGIN, 1),
  V5HOTSPOTEND(RichTextConstants.SIG_CD_V5HOTSPOTEND, 1),
  /**
   * This CD record contains language information for a field or a run of rich
   * text.
   */
  TEXTPROPERTY(RichTextConstants.SIG_CD_TEXTPROPERTY, 1),
  /**
   * This structure defines the start of a new paragraph within a rich-text
   * field.<br>
   * Each paragraph in a rich text field may have different style attributes, such
   * as indentation
   * and interline spacing. Use this structure when accessing a rich text field at
   * the level of the CD records.
   */
  PARAGRAPH(RichTextConstants.SIG_CD_PARAGRAPH, 1, CDParagraph.class),
  /**
   * This structure specifies a format for paragraphs in a rich-text field. There
   * may be more than one paragraph
   * using the same paragraph format, but there may be no more than one
   * CDPABDEFINITION with the same
   * ID in a rich-text field.
   */
  PABDEFINITION(RichTextConstants.SIG_CD_PABDEFINITION, 1),
  /**
   * This structure is placed at the start of each paragraph in a rich-text field,
   * and specifies which
   * CDPABDEFINITION is used as the format for the paragraph.
   */
  PABREFERENCE(RichTextConstants.SIG_CD_PABREFERENCE, 1, CDPabReference.class),
  /** This structure defines the start of a run of text in a rich-text field. */
  TEXT(RichTextConstants.SIG_CD_TEXT, 1, CDText.class),
  XML(RichTextConstants.SIG_CD_XML, 1),
  /** Contains the header or footer used in a document. */
  HEADER(RichTextConstants.SIG_CD_HEADER, 1),
  /**
   * This structure is used to create a document link in a rich text field.<br>
   * It contains all the information necessary to open the specified document from
   * any database on any server.
   */
  LINKEXPORT2(RichTextConstants.SIG_CD_LINKEXPORT2, 1),
  /**
   * A rich text field may contain a bitmap image. There are three types,
   * monochrome, 8-bit mapped color,
   * and 16-bit color; a gray scale bitmap is stored as an 8-bit color bitmap with
   * a color table
   * having entries [0, 0, 0], [1, 1, 1], . . . , [255, 255, 255]. All bitmaps are
   * stored as a single
   * plane (some graphics devices support multiple planes).
   */
  BITMAPHEADER(RichTextConstants.SIG_CD_BITMAPHEADER, 1, CDBitmapHeader.class),
  /**
   * The bitmap data is divided into segments to optimize data storage within
   * Domino.<br>
   * It is recommended that each segment be no larger than 10k bytes. For best
   * display speed,
   * the segments sould be as large as possible, up to the practical 10k limit. A
   * scanline must
   * be contained within a single segment, and cannot be divided between two
   * segments. A bitmap
   * must contain at least one segment, but may have many segments.
   */
  BITMAPSEGMENT(RichTextConstants.SIG_CD_BITMAPSEGMENT, 1),
  /**
   * A color table is one of the optional records following a CDBITMAPHEADER
   * record.<br>
   * The color table specifies the mapping between 8-bit bitmap samples and 24-bit
   * Red/Green/Blue colors.
   */
  COLORTABLE(RichTextConstants.SIG_CD_COLORTABLE, 1),
  /**
   * The CDGRAPHIC record contains information used to control display of graphic
   * objects in a document.<br>
   * This record marks the beginning of a composite graphic object, and must be
   * present for any graphic
   * object to be loaded or displayed.
   */
  GRAPHIC(RichTextConstants.SIG_CD_GRAPHIC, 1, CDGraphic.class),
  /**
   * A portion of a Presentation Manager GPI metafile. This record must be
   * preceded by a CDPMMETAHEADER record.<br>
   * Since metafiles can be large, but Domino and Notes have an internal limit of
   * 65,536 bytes (64kB) for a
   * segment, a metafile may be divided into segments of up to 64kB;<br>
   * each segment must be preceded by a CDPMMETASEG record.
   */
  PMMETASEG(RichTextConstants.SIG_CD_PMMETASEG, 1),
  /**
   * A portion of a Windows GDI metafile. This record must be preceded by a
   * CDWINMETAHEADER record.<br>
   * Since Windows GDI metafiles can be large, but Domino and Notes have an
   * internal limit of 65,536 bytes
   * (64kB) for a segment, a metafile may be divided into segments of up to 64kB;
   * each segment must be
   * preceded by a CDWINMETASEG record.
   */
  WINMETASEG(RichTextConstants.SIG_CD_WINMETASEG, 1),
  /**
   * A portion of a Macintosh metafile. This record must be preceded by a
   * CDMACMETAHEADER record.<br>
   * Since metafiles can be large, but Domino and Notes have an internal limit of
   * 65,536 bytes (64kB)
   * for a segment, a metafile may be divided into segments of up to 64kB; each
   * segment must be
   * preceded by a CDMACMETASEG record.
   */
  MACMETASEG(RichTextConstants.SIG_CD_MACMETASEG, 1),
  /**
   * Identifies a CGM metafile embedded in a rich text field. This record must be
   * preceded by a CDGRAPHIC record.<br>
   * A CDCGMMETA record may contain all or part of a CGM metafile, and is limited
   * to 65,536 bytes (64K).
   */
  CGMMETA(RichTextConstants.SIG_CD_CGMMETA, 1),
  /**
   * Identifies a Presentation Manger GPI metafile embedded in a rich text
   * field.<br>
   * This record must be preceded by a CDGRAPHIC record. Since metafiles can be
   * large, but Domino
   * and Notes have an internal limit of 65,536 bytes (64kB) for a segment, a
   * metafile may be divided
   * into segments of up to 64kB; each segment must be preceded by a CDPMMETASEG
   * record.
   */
  PMMETAHEADER(RichTextConstants.SIG_CD_PMMETAHEADER, 1),
  /**
   * Identifies a Windows Graphics Device Interface (GDI) metafile embedded in a
   * rich text field.<br>
   * This record must be preceded by a CDGRAPHIC record.<br>
   * Since Windows GDI metafiles can be large, but Domino and Notes have an
   * internal limit of
   * 65,536 bytes (64kB) for a segment, a metafile may be divided into segments of
   * up to 64kB;<br>
   * each segment must be preceded by a CDWINMETASEG record.
   */
  WINMETAHEADER(RichTextConstants.SIG_CD_WINMETAHEADER, 1),
  /**
   * Identifies a Macintosh metafile embedded in a rich text field.<br>
   * This record must be preceded by a CDGRAPHIC record.<br>
   * Since metafiles can be large, but Domino and Notes has an internal limit of
   * 65,536 bytes (64kB)
   * for a segment, a metafile may be divided into segments of up to 64kB;<br>
   * each segment must be preceded by a CDMACMETASEG record.
   */
  MACMETAHEADER(RichTextConstants.SIG_CD_MACMETAHEADER, 1),
  /**
   * This structure specifies the beginning of a table.<br>
   * It contains information about the format and size of the table.<br>
   * Use this structure when accessing a table in a rich text field.<br>
   * As of R5, this structure is preceded by a CDPRETABLEBEGIN structure.<br>
   * The CDPRETABLEBEGIN structure specifies additional table properties.
   */
  TABLEBEGIN(RichTextConstants.SIG_CD_TABLEBEGIN, 1),
  /**
   * This structure specifies the cell of a table. Use this structure when
   * accessing a table in a rich text field.
   */
  TABLECELL(RichTextConstants.SIG_CD_TABLECELL, 1),
  /**
   * This structure specifies the end of a table. Use this structure when
   * accessing a table in a rich text field.
   */
  TABLEEND(RichTextConstants.SIG_CD_TABLEEND, 1),
  /**
   * This structure stores the style name for a Paragraph Attributes Block (PAB).
   */
  STYLENAME(RichTextConstants.SIG_CD_STYLENAME, 1),
  /** This structure stores information for an externally stored object. */
  STORAGELINK(RichTextConstants.SIG_CD_STORAGELINK, 1),
  /**
   * Bitmap Transparency Table (optionally one per bitmap). The colors in this
   * table specify the bitmap
   * colors that are "transparent".<br>
   * The pixels in the bitmap whose colors are in this table will not affect the
   * background; the
   * background will "bleed through" into the bitmap.<br>
   * The entries in the transparency table should be in the same format as entries
   * in the color
   * table.<br>
   * If a transparency table is used for a bitmap, it must immediately follow the
   * CDBITMAPHEADER.
   */
  TRANSPARENTTABLE(RichTextConstants.SIG_CD_TRANSPARENTTABLE, 1),
  /** Specifies a horizontal line. */
  HORIZONTALRULE(RichTextConstants.SIG_CD_HORIZONTALRULE, 1),
  /**
   * Documents stored on a Lotus Domino server that are viewed via a Web browser
   * may contain
   * elements that cannot be displayed by the browser.<br>
   * These elements may have alternate text that the browser will display in their
   * place.<br>
   * The alternate text is stored in a CDALTTEXT record.
   */
  ALTTEXT(RichTextConstants.SIG_CD_ALTTEXT, 1),
  /**
   * An anchor hotlink points to a specific location in a rich text field of a
   * document.<br>
   * That target location is identified by a CDANCHOR record containing a
   * specified text string.<br>
   * When the anchor hotlink is selected by a user, Notes displays the anchor
   * location in the target document.
   */
  ANCHOR(RichTextConstants.SIG_CD_ANCHOR, 1),
  /**
   * Text in a rich-text field can have the "Pass-Thru HTML" attribute.<br>
   * Pass-through HTML text is not translated to the Domino rich text format.<br>
   * Pass-through HTML text is marked by CDHTMLBEGIN and CDHTMLEND records.
   */
  HTMLBEGIN(RichTextConstants.SIG_CD_HTMLBEGIN, 1),
  /**
   * Text in a rich-text field can have the "Pass-Thru HTML" attribute.<br>
   * Pass-through HTML text is not translated to the Domino rich text format.<br>
   * Pass-through HTML text is marked by CDHTMLBEGIN and CDHTMLEND records.
   */
  HTMLEND(RichTextConstants.SIG_CD_HTMLEND, 1),
  /**
   * A CDHTMLFORMULA record contains a formula used to generate either an
   * attribute or alternate HTML text for a Java applet.
   */
  HTMLFORMULA(RichTextConstants.SIG_CD_HTMLFORMULA, 1),
  NESTEDTABLEBEGIN(RichTextConstants.SIG_CD_NESTEDTABLEBEGIN, 1),
  NESTEDTABLECELL(RichTextConstants.SIG_CD_NESTEDTABLECELL, 1),
  NESTEDTABLEEND(RichTextConstants.SIG_CD_NESTEDTABLEEND, 1),
  /** This CD Record identifies the paper color for a given document. */
  COLOR(RichTextConstants.SIG_CD_COLOR, 1, CDColor.class),
  TABLECELL_COLOR(RichTextConstants.SIG_CD_TABLECELL_COLOR, 1),
  /**
   * This CD record is used in conjunction with CD record CDEVENT.<br>
   * If a CDEVENT record has an ActionType of ACTION_TYPE_JAVASCRIPT then
   * CDBLOBPART
   * contains the JavaScript code. There may be more then one CDBLOBPART record
   * for each CDEVENT.<br>
   * Therefore it may be necessary to loop thorough all of the CDBLOBPART records
   * to read in
   * the complete JavaScript code.
   */
  BLOBPART(RichTextConstants.SIG_CD_BLOBPART, 1, CDBlobPart.class),
  /**
   * Structure which defines simple actions, formulas or LotusScript for an image
   * map
   */
  EVENT(RichTextConstants.SIG_CD_EVENT, 1, CDEvent.class),
  /**
   * This CD record defines the beginning of a series of CD Records.<br>
   * Not all CD records are enclosed within a CDBEGINRECORD/CDENDRECORD
   * combination.
   */
  BEGIN(RichTextConstants.SIG_CD_BEGIN, 1, CDBegin.class),
  /**
   * This CD record defines the end of a series of CD records.<br>
   * Not all CD records are enclosed within a CDBEGINRECORD/CDENDRECORD
   * combination
   */
  END(RichTextConstants.SIG_CD_END, 1, CDEnd.class),
  /**
   * This CD record allows for additional information to be provided for a
   * graphic.
   */
  VERTICALALIGN(RichTextConstants.SIG_CD_VERTICALALIGN, 1),
  FLOATPOSITION(RichTextConstants.SIG_CD_FLOATPOSITION, 1),
  /**
   * This CD record provides the time interval information for tables created
   * where a
   * different row is displayed within the time interval specified.<br>
   * This structure is stored just before the CDTABLEEND structure.
   */
  TIMERINFO(RichTextConstants.SIG_CD_TIMERINFO, 1),
  /** This CD record describes the Row Height property for a table. */
  TABLEROWHEIGHT(RichTextConstants.SIG_CD_TABLEROWHEIGHT, 1),
  /**
   * This CD Record further defines information for a table.<br>
   * Specifically the tab and row labels.
   */
  TABLELABEL(RichTextConstants.SIG_CD_TABLELABEL, 1),
  BIDI_TEXT(RichTextConstants.SIG_CD_BIDI_TEXT, 1),
  BIDI_TEXTEFFECT(RichTextConstants.SIG_CD_BIDI_TEXTEFFECT, 1),
  /** This CD Record is used within mail templates. */
  REGIONBEGIN(RichTextConstants.SIG_CD_REGIONBEGIN, 1),
  REGIONEND(RichTextConstants.SIG_CD_REGIONEND, 1),
  TRANSITION(RichTextConstants.SIG_CD_TRANSITION, 1),
  /**
   * The designer of a form may define a "hint" associated with a field. This
   * descriptive text
   * is visible in dimmed text within the field when a document is created using
   * the form and
   * helps the user to know what to select or fill in for the field. This text
   * does not get saved
   * with the document and disappears when the cursor enters the field.
   */
  FIELDHINT(RichTextConstants.SIG_CD_FIELDHINT, 1, CDFieldHint.class),
  /**
   * A CDPLACEHOLDER record stores additional information about various embedded
   * type CD records,
   * such as CDEMBEDDEDCTL, CDEMBEDDEDOUTLINE and other embedded CD record types
   * defined in HOTSPOTREC_TYPE_xxx.
   */
  PLACEHOLDER(RichTextConstants.SIG_CD_PLACEHOLDER, 1),
  HTMLNAME(RichTextConstants.SIG_CD_HTMLNAME, 1),
  /**
   * This CD Record defines the attributes of an embedded outline.<br>
   * It is preceded by a CDHOTSPOTBEGIN and a CDPLACEHOLDER.<br>
   * The CD record, CDPLACEHOLDER, further defines the CDEMBEDDEDOUTLINE.
   */
  EMBEDDEDOUTLINE(RichTextConstants.SIG_CD_EMBEDDEDOUTLINE, 1),
  /**
   * This CD Record describes a view as an embedded element.<br>
   * A CDEMBEDDEDVIEW record will be preceded by a CDPLACEHOLDER record.<br>
   * Further description of the embedded view can be found in the CD record
   * CDPLACEHOLDER.
   */
  EMBEDDEDVIEW(RichTextConstants.SIG_CD_EMBEDDEDVIEW, 1),
  /**
   * This CD Record gives information pertaining to Background Data for a Table,
   * specifically the 'Cell Image' repeat value.
   */
  CELLBACKGROUNDDATA(RichTextConstants.SIG_CD_CELLBACKGROUNDDATA, 1),
  /**
   * This CD record provides additional table properties, expanding the
   * information provided in CDTABLEBEGIN.<br>
   * It will only be recognized in Domino versions 5.0 and greater. This record
   * will be ignored in pre 5.0 versions.
   */
  PRETABLEBEGIN(RichTextConstants.SIG_CD_PRETABLEBEGIN, 1),
  EXT2_FIELD(RichTextConstants.SIG_CD_EXT2_FIELD, 1, CDExt2Field.class),
  /**
   * This CD record may further define attributes within a CDFIELD such as tab
   * order.
   */
  EMBEDDEDCTL(RichTextConstants.SIG_CD_EMBEDDEDCTL, 1, CDEmbeddedControl.class),
  /**
   * This CD record describes border information for a given table.<br>
   * This CD record will be preceded with CD record CDPRETABLEBEGIN both
   * encapsulated between a
   * CDBEGINRECORD and a CDENDRECORD record with CD record signature
   * CDPRETABLEBEGIN.
   */
  BORDERINFO(RichTextConstants.SIG_CD_BORDERINFO, 1, CDBorderInfo.class),
  /**
   * This CD record defines an embedded element of type 'group scheduler'.<br>
   * It is preceded by a CDHOTSPOTBEGIN and a CDPLACEHOLDER. The CD record,
   * CDPLACEHOLDER,
   * further defines the CDEMBEDDEDSCHEDCTL.
   */
  EMBEDDEDSCHEDCTL(RichTextConstants.SIG_CD_EMBEDDEDSCHEDCTL, 1),
  /**
   * This CD record defines an embedded element of type 'editor'. It is preceded
   * by a
   * CDHOTSPOTBEGIN and a CDPLACEHOLDER. The CD record, CDPLACEHOLDER, further
   * defines the CDEMBEDDEDEDITCTL
   */
  EMBEDDEDEDITCTL(RichTextConstants.SIG_CD_EMBEDDEDEDITCTL, 1),

  /* Signatures for Frameset CD records */

  /** Beginning header record to both a CDFRAMESET and CDFRAME record. */
  FRAMESETHEADER(RichTextConstants.SIG_CD_FRAMESETHEADER, 2, CDFramesetHeader.class),
  /** Used to specify an HTML FRAMESET element */
  FRAMESET(RichTextConstants.SIG_CD_FRAMESET, 2, CDFrameset.class),
  /** Used to specify an HTML FRAME element */
  FRAME(RichTextConstants.SIG_CD_FRAME, 2, CDFrame.class),

  /* Signature for Target Frame info on a link */

  /**
   * The CDTARGET structure specifies the target (ie: the frame) where a resource
   * link hotspot is to be displayed.
   */
  TARGET(RichTextConstants.SIG_CD_TARGET, new int[] { 1, 3 }, CDTarget.class),
  /**
   * Part of a client side image MAP which describes each region in an image and
   * indicates the
   * location of the document to be retrieved when the defined area is activated..
   */
  MAPELEMENT(RichTextConstants.SIG_CD_MAPELEMENT, 3),
  /**
   * An AREA element defines the shape and coordinates of a region within a client
   * side image MAP.
   */
  AREAELEMENT(RichTextConstants.SIG_CD_AREAELEMENT, 3),
  HREF(RichTextConstants.SIG_CD_HREF, new int[] { 1, 3 }, CDResource.class),
  HTML_ALTTEXT(RichTextConstants.SIG_CD_HTML_ALTTEXT, 3),
  /**
   * Structure which defines simple actions, formulas or LotusScript for an image
   * map, similar to {@link #EVENT}
   */
  TARGET_EVENT(RichTextConstants.SIG_CD_EVENT, 3, CDEvent.class),

  /* Signatures for Composite Records that are reserved internal records, */
  /* whose format may change between releases. */

  NATIVEIMAGE(RichTextConstants.SIG_CD_NATIVEIMAGE, 4),
  DOCUMENT_PRE_26(RichTextConstants.SIG_CD_DOCUMENT_PRE_26, 4),
  /** * OBSOLETE * Defines the attributes of a field in a form. */
  FIELD_PRE_36(RichTextConstants.SIG_CD_FIELD_PRE_36, 4),
  /**
   * This defines the structure of a CDFIELD record in the $Body item of a form
   * note.<br>
   * Each CDFIELD record defines the attributes of one field in the form.
   */
  FIELD(RichTextConstants.SIG_CD_FIELD, 4, CDField.class),
  /**
   * This defines the structure of the document information field in a form note.
   * A
   * document information field is an item with name $INFO (ITEM_NAME_DOCUMENT)
   * and
   * data type TYPE_COMPOSITE. The document information field defines attributes
   * of
   * documents created with that form.
   */
  DOCUMENT(RichTextConstants.SIG_CD_DOCUMENT, 4, CDDocument.class),
  METAFILE(RichTextConstants.SIG_CD_METAFILE, 4),
  BITMAP(RichTextConstants.SIG_CD_BITMAP, 4),
  /**
   * This defines part of the structure of a font table item in a note.<br>
   * A font table item in a note allows rich text in the note to be displayed
   * using
   * fonts other than those defined in FONT_FACE_xxx.
   */
  FONTTABLE(RichTextConstants.SIG_CD_FONTTABLE, 4, CDFontTable.class),
  LINK(RichTextConstants.SIG_CD_LINK, 4),
  LINKEXPORT(RichTextConstants.SIG_CD_LINKEXPORT, 4),
  /**
   * This structure is the header of a record containing the predefined keywords
   * allowed for a field (defined by a CDFIELD record).
   */
  KEYWORD(RichTextConstants.SIG_CD_KEYWORD, 4, CDKeyword.class),
  /**
   * This structure implements a document link in a rich text field.<br>
   * It contains an index into a Doc Link Reference List.<br>
   * A Doc Link Reference (a NOTELINK structure) contains all the information
   * necessary to open the specified document from any database on any server.
   */
  LINK2(RichTextConstants.SIG_CD_LINK2, 4),
  CGM(RichTextConstants.SIG_CD_CGM, 4),
  TIFF(RichTextConstants.SIG_CD_TIFF, 4),
  /**
   * A pattern table is one of the optional records following a CDBITMAPHEADER
   * record.<br>
   * The pattern table is used to compress repetitive bitmap data.
   */
  PATTERNTABLE(RichTextConstants.SIG_CD_PATTERNTABLE, 4),
  /** A CD record of this type specifies the start of a DDE link. */
  DDEBEGIN(RichTextConstants.SIG_CD_DDEBEGIN, 4, CDDDEBegin.class),
  /** This structure specifies the end of a DDE link. */
  DDEEND(RichTextConstants.SIG_CD_DDEEND, 4),
  /** This structure specifies the start of an OLE Object. */
  OLEBEGIN(RichTextConstants.SIG_CD_OLEBEGIN, 4),
  /** This structure specifies the end of an OLE Object in a rich text field. */
  OLEEND(RichTextConstants.SIG_CD_OLEEND, 4),
  /**
   * This structure specifies the start of a "hot" region in a rich text
   * field.<br>
   * Clicking on a hot region causes some other action to occur.<br>
   * For instance, clicking on a popup will cause a block of text associated
   * with that popup to be displayed.
   */
  HOTSPOTBEGIN(RichTextConstants.SIG_CD_HOTSPOTBEGIN, 4, CDHotspotBegin.class),
  /** This structure specifies the end of a hot region in a rich text field. */
  HOTSPOTEND(RichTextConstants.SIG_CD_HOTSPOTEND, 4, CDHotspotEnd.class),
  /** This structure defines the appearance of a button in a rich text field. */
  BUTTON(RichTextConstants.SIG_CD_BUTTON, 4),
  /**
   * This structure defines the appearance of the bar used with collapsible
   * sections.
   */
  BAR(RichTextConstants.SIG_CD_BAR, 4),
  V4HOTSPOTBEGIN(RichTextConstants.SIG_CD_V4HOTSPOTBEGIN, 4, CDHotspotBegin.class),
  V4HOTSPOTEND(RichTextConstants.SIG_CD_V4HOTSPOTEND, 4),
  EXT_FIELD(RichTextConstants.SIG_CD_EXT_FIELD, 4, CDExtField.class),
  /** The CD record contains Lotus Script object code. */
  LSOBJECT(RichTextConstants.SIG_CD_LSOBJECT, 4),
  /**
   * This record is included for future use.<br>
   * Applications should not generate these records.<br>
   * Domino and Notes will ignore this record.
   */
  HTMLHEADER(RichTextConstants.SIG_CD_HTMLHEADER, 4),
  /**
   * This record is included for future use. Applications should not generate
   * these records.<br>
   * Domino and Notes will ignore this record.
   */
  HTMLSEGMENT(RichTextConstants.SIG_CD_HTMLSEGMENT, 4),
  OLEOBJPH(RichTextConstants.SIG_CD_OLEOBJPH, 4),
  MAPIBINARY(RichTextConstants.SIG_CD_MAPIBINARY, 4),

  /**
   * The definition for a layout region on a form is stored as CD records in the
   * $Body item of the form note.<br>
   * The layout region begins with a CDLAYOUT record and ends with a CDLAYOUTEND
   * record.<br>
   * Other records in the layout region define buttons, graphics, fields, or other
   * rich text elements.
   */
  LAYOUT(RichTextConstants.SIG_CD_LAYOUT, 4),
  /**
   * A text element in a layout region of a form is defined by a CDLAYOUTTEXT
   * record.<br>
   * This record must be between a CDLAYOUT record and a CDLAYOUTEND record.<br>
   * This record is usually followed by other CD records identifying text,
   * graphical, or
   * action elements associated with the element
   */
  LAYOUTTEXT(RichTextConstants.SIG_CD_LAYOUTTEXT, 4),
  /**
   * The CDLAYOUTEND record marks the end of the elements defining a layout region
   * within a form.
   */
  LAYOUTEND(RichTextConstants.SIG_CD_LAYOUTEND, 4),
  /**
   * A field in a layout region of a form is defined by a CDLAYOUTFIELD
   * record.<br>
   * This record must be between a CDLAYOUT record and a CDLAYOUTEND record.<br>
   * This record is usually followed by other CD records identifying text,
   * graphical, or
   * action elements associated with the field.
   */
  LAYOUTFIELD(RichTextConstants.SIG_CD_LAYOUTFIELD, 4),
  /**
   * This record contains the "Hide When" formula for a paragraph attributes
   * block.
   */
  PABHIDE(RichTextConstants.SIG_CD_PABHIDE, 4, CDPabHide.class),
  PABFORMREF(RichTextConstants.SIG_CD_PABFORMREF, 4),
  /**
   * The designer of a form or view may define custom actions for that form or
   * view.<br>
   * The attributes for the button bar are stored in the CDACTIONBAR record in the
   * $ACTIONS
   * and/or $V5ACTIONS item for the design note describing the form or view.
   */
  ACTIONBAR(RichTextConstants.SIG_CD_ACTIONBAR, 4, CDActionBar.class),
  /**
   * The designer of a form or view may define custom actions associated with that
   * form or view.<br>
   * Actions may be presented to the user as buttons on the action button bar or
   * as options on the "Actions" menu.
   */
  ACTION(RichTextConstants.SIG_CD_ACTION, 4, CDAction.class),
  /**
   * Structure of an on-disk autolaunch item.<br>
   * Most of the information contained in this structure refers to OLE
   * autolaunching behaviors.
   */
  DOCAUTOLAUNCH(RichTextConstants.SIG_CD_DOCAUTOLAUNCH, 4, CDDocAutoLaunch.class),
  /**
   * A graphical element in a layout region of a form is defined by a
   * CDLAYOUTGRAPHIC record.<br>
   * This record must be between a CDLAYOUT record and a CDLAYOUTEND record.<br>
   * This record is usually followed by other CD records identifying text,
   * graphical, or
   * action elements associated with the graphical element.
   */
  LAYOUTGRAPHIC(RichTextConstants.SIG_CD_LAYOUTGRAPHIC, 4),
  OLEOBJINFO(RichTextConstants.SIG_CD_OLEOBJINFO, 4),
  /**
   * A button in a layout region of a form is defined by a CDLAYOUTBUTTON
   * record.<br>
   * This record must be between a CDLAYOUT record and a CDLAYOUTEND record.<br>
   * This record is usually followed by other CD records identifying text,
   * graphical, or
   * action elements associated with the button.
   */
  LAYOUTBUTTON(RichTextConstants.SIG_CD_LAYOUTBUTTON, 4),
  /**
   * The CDTEXTEFFECT record stores a "special effect" font ID for a run of rich
   * text.
   */
  TEXTEFFECT(RichTextConstants.SIG_CD_TEXTEFFECT, 4),

  /* Signatures for items of type TYPE_VIEWMAP */

  VMHEADER(RichTextConstants.SIG_CD_VMHEADER, 5),
  VMBITMAP(RichTextConstants.SIG_CD_VMBITMAP, 5),
  VMRECT(RichTextConstants.SIG_CD_VMRECT, 5),
  VMPOLYGON_BYTE(RichTextConstants.SIG_CD_VMPOLYGON_BYTE, 5),
  VMPOLYLINE_BYTE(RichTextConstants.SIG_CD_VMPOLYLINE_BYTE, 5),
  VMREGION(RichTextConstants.SIG_CD_VMREGION, 5),
  VMACTION(RichTextConstants.SIG_CD_VMACTION, 5),
  VMELLIPSE(RichTextConstants.SIG_CD_VMELLIPSE, 5),
  VMSMALLTEXTBOX(RichTextConstants.SIG_CD_VMSMALLTEXTBOX, 5),
  VMRNDRECT(RichTextConstants.SIG_CD_VMRNDRECT, 5),
  VMBUTTON(RichTextConstants.SIG_CD_VMBUTTON, 5),
  VMACTION_2(RichTextConstants.SIG_CD_VMACTION_2, 5),
  VMTEXTBOX(RichTextConstants.SIG_CD_VMTEXTBOX, 5),
  VMPOLYGON(RichTextConstants.SIG_CD_VMPOLYGON, 5),
  VMPOLYLINE(RichTextConstants.SIG_CD_VMPOLYLINE, 5),
  VMPOLYRGN(RichTextConstants.SIG_CD_VMPOLYRGN, 5),
  VMCIRCLE(RichTextConstants.SIG_CD_VMCIRCLE, 5),
  VMPOLYRGN_BYTE(RichTextConstants.SIG_CD_VMPOLYRGN_BYTE, 5),

  /* Signatures for alternate CD sequences*/

  /**
   * Documents converted from HTML (Hyper-Text Markup Language), usually from a
   * World Wide Web source,
   * may contain "active object" instructions (such as a Java applet).<br>
   * An active object may have an alternate representation which is to be
   * displayed if the
   * active object is not supported or is disabled. When an active object is
   * converted to a
   * compound text representation, the alternate representation is stored
   * beginning with a
   * CDALTERNATEBEGIN record and ending with a CDALTERNATEEND record. An alternate
   * representation
   * corresponds to the most recent active object with the same
   * ACTIVEOBJECT_TYPE_xxx code, found in the Type field.
   */
  ALTERNATEBEGIN(RichTextConstants.SIG_CD_ALTERNATEBEGIN, 6),
  /**
   * This record marks the end of a sequence of CD records comprising the
   * alternate representation of
   * an active object.<br>
   * For more information, please see the entry for CDALTERNATEBEGIN.
   */
  ALTERNATEEND(RichTextConstants.SIG_CD_ALTERNATEEND, 6),
  /**
   * This record is simply a marker in an OLE rich text hot spot that indicates
   * that an OLE
   * object with an associated rich text field (a $OLEObjRichTextField item) was
   * updated by
   * Release 4.6 or later of Domino or Notes.
   */
  OLERTMARKER(RichTextConstants.SIG_CD_OLERTMARKER, 6),

  /* Embedded action records */
  ACTION_HEADER(RichTextConstants.SIG_ACTION_HEADER, 7, CDActionHeader.class),
  ACTION_MODIFYFIELD(RichTextConstants.SIG_ACTION_MODIFYFIELD, 7, CDActionModifyField.class),
  ACTION_REPLY(RichTextConstants.SIG_ACTION_REPLY, 7, CDActionReply.class),
  ACTION_FORMULA(RichTextConstants.SIG_ACTION_FORMULA, 7, CDActionFormula.class),
  ACTION_LOTUSSCRIPT(RichTextConstants.SIG_ACTION_LOTUSSCRIPT, 7, CDActionLotusScript.class),
  ACTION_SENDMAIL(RichTextConstants.SIG_ACTION_SENDMAIL, 7, CDActionSendMail.class),
  ACTION_DBCOPY(RichTextConstants.SIG_ACTION_DBCOPY, 7, CDActionDBCopy.class),
  ACTION_DELETE(RichTextConstants.SIG_ACTION_DELETE, 7, CDActionDelete.class),
  ACTION_BYFORM(RichTextConstants.SIG_ACTION_BYFORM, 7, CDActionByForm.class),
  ACTION_MARKREAD(RichTextConstants.SIG_ACTION_MARKREAD, 7, CDActionReadMarks.class),
  ACTION_MARKUNREAD(RichTextConstants.SIG_ACTION_MARKUNREAD, 7, CDActionReadMarks.class),
  ACTION_MOVETOFOLDER(RichTextConstants.SIG_ACTION_MOVETOFOLDER, 7, CDActionFolder.class),
  ACTION_COPYTOFOLDER(RichTextConstants.SIG_ACTION_COPYTOFOLDER, 7, CDActionFolder.class),
  ACTION_REMOVEFROMFOLDER(RichTextConstants.SIG_ACTION_REMOVEFROMFOLDER, 7, CDActionFolder.class),
  ACTION_NEWSLETTER(RichTextConstants.SIG_ACTION_NEWSLETTER, 7, CDActionNewsletter.class),
  ACTION_RUNAGENT(RichTextConstants.SIG_ACTION_RUNAGENT, 7, CDActionRunAgent.class),
  ACTION_SENDDOCUMENT(RichTextConstants.SIG_ACTION_SENDDOCUMENT, 7, CDActionSendDocument.class),
  ACTION_FORMULAONLY(RichTextConstants.SIG_ACTION_FORMULAONLY, 7),
  ACTION_JAVAAGENT(RichTextConstants.SIG_ACTION_JAVAAGENT, 7, CDActionJavaAgent.class),
  ACTION_JAVA(RichTextConstants.SIG_ACTION_JAVA, 7),
  
  /**
   * This record was seen via observation only when reading the contents of a CDACTION
   * "Simple Actions" record
   */
  UNIDENTIFIED_CDACTION_PREFIX(RichTextConstants.SIG_UNIDENTIFIED_CDACTION_PREFIX, 7);

  public enum Area {
    /** Signatures for Composite Records in items of data type COMPOSITE */
    TYPE_COMPOSITE,
    /** Signatures for Frameset CD records */
    FRAMESETS,
    /** Signature for Target Frame info on a link */
    TARGET_FRAME,
    /**
     * Signatures for Composite Records that are reserved internal records,
     * whose format may change between releases.
     */
    RESERVED_INTERNAL,
    /** Signatures for items of type TYPE_VIEWMAP */
    TYPE_VIEWMAP,
    /** Signatures for alternate CD sequences */
    ALTERNATE_SEQ,
    /** Signatures for agent action data */
    TYPE_ACTION
  }

  private static Map<String, RecordType> m_recordsByConstant;
  static {
    RecordType.m_recordsByConstant = new HashMap<>();
    for (final RecordType currType : RecordType.values()) {
      for(int area : currType.getArea()) {
        final String key = currType.getConstant() + "|" + area; //$NON-NLS-1$
        RecordType.m_recordsByConstant.put(key, currType);
      }
    }
  }

  /**
   * Looks up a {@link RecordType} for the provided encapsulating interface.
   *
   * @param <T>   the {@link RichTextRecord} sub-interface to find
   * @param clazz a {@link Class} object representing {@code <T>}
   * @return the corresponding {@link RecordType}
   * @throws IllegalArgumentException if {@code clazz} does not correspond to any
   *                                  record type
   */
  public static <T extends RichTextRecord<?>> RecordType forEncapsulationClass(final Class<T> clazz) {
    Objects.requireNonNull(clazz, "class cannot be null");
    for (final RecordType type : RecordType.values()) {
      if (clazz.equals(type.m_encapsulation)) {
        return type;
      }
    }
    throw new IllegalArgumentException(MessageFormat.format("Unable to find RecordType for class {0}", clazz.getName()));
  }

  /**
   * Looks up an {@link RecordType} for the signature WORD value contained
   * in a CD record.
   *
   * @param constant signature WORD defining the record type
   * @param area     type of data you are processing, e.g.
   *                 {@link Area#TYPE_COMPOSITE} for rich text or design element
   *                 $Body items (required because the same record type constant
   *                 value is used multiple times)
   * @return record type or <code>null</code> if unknown constant
   */
  public static RecordType getRecordTypeForConstant(final short constant, final Area area) {
    switch (area) {
      case TYPE_COMPOSITE:
        return RecordType.m_recordsByConstant.get(constant + "|1"); //$NON-NLS-1$
      case FRAMESETS:
        return RecordType.m_recordsByConstant.get(constant + "|2"); //$NON-NLS-1$
      case TARGET_FRAME:
        return RecordType.m_recordsByConstant.get(constant + "|3"); //$NON-NLS-1$
      case RESERVED_INTERNAL:
        return RecordType.m_recordsByConstant.get(constant + "|4"); //$NON-NLS-1$
      case TYPE_VIEWMAP:
        return RecordType.m_recordsByConstant.get(constant + "|5"); //$NON-NLS-1$
      case ALTERNATE_SEQ:
        return RecordType.m_recordsByConstant.get(constant + "|6"); //$NON-NLS-1$
      case TYPE_ACTION:
        return RecordType.m_recordsByConstant.get(constant + "|7"); //$NON-NLS-1$
      default:
        throw new IllegalArgumentException(MessageFormat.format("Unknown area: {0}", area));
    }
  }

  /**
   * Looks up an {@link RecordType} for the signature WORD value contained
   * in a CD record.
   *
   * @param constant signature WORD defining the record type
   * @return a set of {@link RecordType} values that have the value
   *         {@link RecordType#getConstant()} (there may be duplicates like
   *         PABHIDE/VMTEXTBOX or ACTION/VMPOLYRGN)
   */
  public static Set<RecordType> getRecordTypesForConstant(final short constant) {
    final Set<RecordType> types = new HashSet<>();
    for (final RecordType currType : RecordType.values()) {
      if (constant == currType.getConstant()) {
        types.add(currType);
      }
    }
    return types;
  }

  private int[] m_area;

  private short m_val;

  private final Class<? extends RichTextRecord<?>> m_encapsulation;

  RecordType(final short val, final int area) {
    this(val, area, null);
  }

  RecordType(final short val, final int area, final Class<? extends RichTextRecord<?>> encapsulation) {
    this(val, new int[] { area }, encapsulation);
  }
  
  RecordType(short val, int[] area, Class<? extends RichTextRecord<?>> encapsulation) {
    this.m_val = val;
    this.m_area = area;
    this.m_encapsulation = encapsulation;
  }

  public int[] getArea() {
    return this.m_area;
  }

  public short getConstant() {
    return this.m_val;
  }

  /**
   * @return the interface that represents the encapsulating type for this record,
   *         or
   *         {@code null} if none has been specified
   * @since 1.0.2
   */
  public Class<? extends RichTextRecord<?>> getEncapsulation() {
    return this.m_encapsulation;
  }

}
