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
package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents available HTML-style events for actions and buttons.
 * 
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public enum HtmlEventId implements INumberEnum<Short> {
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
  LIBRARY(RichTextConstants.HTML_EVENT_LIBRARY);

  private final short value;

  HtmlEventId(final short value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
}