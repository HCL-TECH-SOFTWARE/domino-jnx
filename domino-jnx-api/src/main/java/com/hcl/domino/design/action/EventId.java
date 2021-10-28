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
package com.hcl.domino.design.action;

import static com.hcl.domino.misc.NotesConstants.DOC_ACTION_ITEM;
import static com.hcl.domino.misc.NotesConstants.VIEW_ACTION_ITEM_NAME;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents available HTML-style client events for actions, buttons, and page events.
 * 
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public enum EventId implements INumberEnum<Short> {
  ONCLICK(RichTextConstants.HTML_EVENT_ONCLICK),
  ONDBLCLICK(RichTextConstants.HTML_EVENT_ONDBLCLICK),
  ONMOUSEDOWN(RichTextConstants.HTML_EVENT_ONMOUSEDOWN),
  ONMOUSEUP(RichTextConstants.HTML_EVENT_ONMOUSEUP),
  ONMOUSEOVER(RichTextConstants.HTML_EVENT_ONMOUSEOVER),
  ONMOUSEMOVE(RichTextConstants.HTML_EVENT_ONMOUSEMOVE),
  ONMOUSEOUT(RichTextConstants.HTML_EVENT_ONMOUSEOUT),
  ONKEYPRESS(RichTextConstants.HTML_EVENT_ONKEYPRESS),
  ONKEYDOWN(RichTextConstants.HTML_EVENT_ONKEYDOWN),
  ONKEYUP(RichTextConstants.HTML_EVENT_ONKEYUP),
  ONFOCUS(RichTextConstants.HTML_EVENT_ONFOCUS),
  ONBLUR(RichTextConstants.HTML_EVENT_ONBLUR),
  ONLOAD(RichTextConstants.HTML_EVENT_ONLOAD),
  ONUNLOAD(RichTextConstants.HTML_EVENT_ONUNLOAD),
  HEADER(RichTextConstants.HTML_EVENT_HEADER),
  ONSUBMIT(RichTextConstants.HTML_EVENT_ONSUBMIT),
  ONRESET(RichTextConstants.HTML_EVENT_ONRESET),
  ONCHANGE(RichTextConstants.HTML_EVENT_ONCHANGE),
  ONERROR(RichTextConstants.HTML_EVENT_ONERROR),
  ONHELP(RichTextConstants.HTML_EVENT_ONHELP),
  ONSELECT(RichTextConstants.HTML_EVENT_ONSELECT),
  LIBRARY(RichTextConstants.HTML_EVENT_LIBRARY),
  
  CLIENT_FORM_QUERYOPEN(RichTextConstants.HTML_EVENT_CLIENT_FORM_QUERYOPEN, DOC_ACTION_ITEM + "0"), //$NON-NLS-1$
  CLIENT_FORM_QUERYMODE(RichTextConstants.HTML_EVENT_CLIENT_FORM_QUERYMODE, DOC_ACTION_ITEM + "3"), //$NON-NLS-1$
  CLIENT_FORM_POSTMODE(RichTextConstants.HTML_EVENT_CLIENT_FORM_POSTMODE, DOC_ACTION_ITEM + "4"), //$NON-NLS-1$
  CLIENT_FORM_POSTRECALC(RichTextConstants.HTML_EVENT_CLIENT_FORM_POSTRECALC, DOC_ACTION_ITEM + "5"), //$NON-NLS-1$
  CLIENT_FORM_POSTSAVE(RichTextConstants.HTML_EVENT_CLIENT_FORM_POSTSAVE, DOC_ACTION_ITEM + "7"), //$NON-NLS-1$
  CLIENT_FORM_POSTSEND(RichTextConstants.HTML_EVENT_CLIENT_FORM_POSTSEND, DOC_ACTION_ITEM + "8"), //$NON-NLS-1$
  CLIENT_FORM_QUERYRECALC(RichTextConstants.HTML_EVENT_CLIENT_FORM_QUERYRECALC, DOC_ACTION_ITEM + "9"), //$NON-NLS-1$
  CLIENT_FORM_QUERYSEND(RichTextConstants.HTML_EVENT_CLIENT_FORM_QUERYSEND, DOC_ACTION_ITEM + "A"), //$NON-NLS-1$
  CLIENT_FORM_ONSIZE(RichTextConstants.HTML_EVENT_CLIENT_FORM_ONSIZE, DOC_ACTION_ITEM + "10"), //$NON-NLS-1$
  
  // These pseudo elements are for the formula event only, and have no constants backing them
  CLIENT_FORM_QUERYSAVE(DOC_ACTION_ITEM + "6"), //$NON-NLS-1$
  CLIENT_FORM_POSTOPEN(DOC_ACTION_ITEM + "1"), //$NON-NLS-1$
  CLIENT_FORM_QUERYCLOSE(DOC_ACTION_ITEM + "2"), //$NON-NLS-1$
  
  CLIENT_VIEW_QUERYOPEN(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYOPEN, VIEW_ACTION_ITEM_NAME + "0"), //$NON-NLS-1$
  CLIENT_VIEW_POSTOPEN(RichTextConstants.HTML_EVENT_CLIENT_VIEW_POSTOPEN, VIEW_ACTION_ITEM_NAME + "1"), //$NON-NLS-1$
  CLIENT_VIEW_REGIONDBLCLK(RichTextConstants.HTML_EVENT_CLIENT_VIEW_REGIONDBLCLK, VIEW_ACTION_ITEM_NAME + "2"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYOPENDOC(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYOPENDOC, VIEW_ACTION_ITEM_NAME + "3"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYRECALC(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYRECALC, VIEW_ACTION_ITEM_NAME + "4"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYADDTOFOLDER(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYADDTOFOLDER, VIEW_ACTION_ITEM_NAME + "5"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYPASTE(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYPASTE, VIEW_ACTION_ITEM_NAME + "6"), //$NON-NLS-1$
  CLIENT_VIEW_POSTPASTE(RichTextConstants.HTML_EVENT_CLIENT_VIEW_POSTPASTE, VIEW_ACTION_ITEM_NAME + "7"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYDRAGDROP(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYDRAGDROP, VIEW_ACTION_ITEM_NAME + "8"), //$NON-NLS-1$
  CLIENT_VIEW_POSTDRAGDROP(RichTextConstants.HTML_EVENT_CLIENT_VIEW_POSTDRAGDROP, VIEW_ACTION_ITEM_NAME + "9"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYCLOSE(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYCLOSE, VIEW_ACTION_ITEM_NAME + "A"), //$NON-NLS-1$
  CLIENT_VIEW_QUERYENTRYRESIZE(RichTextConstants.HTML_EVENT_CLIENT_VIEW_QUERYENTRYRESIZE, VIEW_ACTION_ITEM_NAME + "B"), //$NON-NLS-1$
  CLIENT_VIEW_POSTENTRYRESIZE(RichTextConstants.HTML_EVENT_CLIENT_VIEW_POSTENTRYRESIZE, VIEW_ACTION_ITEM_NAME + "C"), //$NON-NLS-1$
  CLIENT_VIEW_INVIEWEDIT(RichTextConstants.HTML_EVENT_CLIENT_VIEW_INVIEWEDIT),
  CLIENT_VIEW_ONSELECT(RichTextConstants.HTML_EVENT_CLIENT_VIEW_ONSELECT, VIEW_ACTION_ITEM_NAME + "E"), //$NON-NLS-1$
  CLIENT_VIEW_ONFOCUS(RichTextConstants.HTML_EVENT_CLIENT_VIEW_ONFOCUS),
  CLIENT_VIEW_ONBLUR(RichTextConstants.HTML_EVENT_CLIENT_VIEW_ONBLUR),
  CLIENT_VIEW_ONSIZE(RichTextConstants.HTML_EVENT_CLIENT_VIEW_ONSIZE, VIEW_ACTION_ITEM_NAME + "F"), //$NON-NLS-1$
  
  CLIENT_ONOBJECTEXECUTE(RichTextConstants.HTML_EVENT_CLIENT_ONOBJECTEXECUTE),
  
  CLIENT_DB_QUERYOPEN(RichTextConstants.HTML_EVENT_CLIENT_DB_QUERYOPEN),
  CLIENT_DB_POSTOPEN(RichTextConstants.HTML_EVENT_CLIENT_DB_POSTOPEN),
  CLIENT_DB_DOCDELETE(RichTextConstants.HTML_EVENT_CLIENT_DB_DOCDELETE),
  CLIENT_DB_QUERYCLOSE(RichTextConstants.HTML_EVENT_CLIENT_DB_QUERYCLOSE),
  CLIENT_DB_QUERYDELETE(RichTextConstants.HTML_EVENT_CLIENT_DB_QUERYDELETE),
  CLIENT_DB_QUERYUNDELETE(RichTextConstants.HTML_EVENT_CLIENT_DB_QUERYUNDELETE),
  CLIENT_DB_QUERYDRAGDROP(RichTextConstants.HTML_EVENT_CLIENT_DB_QUERYDRAGDROP),
  CLIENT_DB_POSTDRAGDROP(RichTextConstants.HTML_EVENT_CLIENT_DB_POSTDRAGDROP),
  CLIENT_DB_QUERYARCHIVEDRAGDROP(RichTextConstants.HTML_EVENT_CLIENT_DB_QUERYARCHIVEDRAGDROP),
  CLIENT_DB_POSTARCHIVEDRAGDROP(RichTextConstants.HTML_EVENT_CLIENT_DB_POSTARCHIVEDRAGDROP),
  
  CLIENT_SCHED_INTERVALCHANGE(RichTextConstants.HTML_EVENT_CLIENT_SCHED_INTERVALCHANGE),
  CLIENT_SCHED_SUGGESTIONSAVAIL(RichTextConstants.HTML_EVENT_CLIENT_SCHED_SUGGESTIONSAVAIL);

  private final short value;
  private final String itemName;
  private final boolean skip;

  EventId(final short value) {
    this(value, null);
  }
  EventId(final short value, final String itemName) {
    this.value = value;
    this.itemName = itemName;
    this.skip = false;
  }
  EventId(final String itemName) {
    this.value = (short)0;
    this.itemName = itemName;
    this.skip = true;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
  
  @Override
  public boolean isSkipInLookup() {
    return skip;
  }
  
  public String getItemName() {
    return itemName;
  }
}