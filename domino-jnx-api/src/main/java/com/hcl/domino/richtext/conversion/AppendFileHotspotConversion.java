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
package com.hcl.domino.richtext.conversion;

import java.util.List;

import com.hcl.domino.data.Attachment;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.RichTextRecord;

public class AppendFileHotspotConversion implements IRichTextConversion {
  private final String m_attachmentProgrammaticName;
  private String m_fileNameToDisplay;

  /**
   * Creates a new instance
   *
   * @param att               attachment to add an icon for
   * @param fileNameToDisplay filename to display below the file icon, not
   *                          necessarily the same as
   *                          {@link Attachment#getFileName()}
   */
  public AppendFileHotspotConversion(final Attachment att, final String fileNameToDisplay) {
    this.m_attachmentProgrammaticName = att.getFileName();
    this.m_fileNameToDisplay = fileNameToDisplay;
  }

  /**
   * Creates a new instance
   *
   * @param attachmentProgrammaticName name returned by
   *                                   {@link Attachment#getFileName()}
   * @param fileNameToDisplay          filename to display below the file icon,
   *                                   not necessarily the same as
   *                                   {@link Attachment#getFileName()}
   */
  public AppendFileHotspotConversion(final String attachmentProgrammaticName, final String fileNameToDisplay) {
    this.m_attachmentProgrammaticName = attachmentProgrammaticName;
  }

  @Override
  public void convert(final List<RichTextRecord<?>> source, final RichTextWriter target) {
    // TODO provide another method to append file hotspots with less copy
    // operations, e.g. by modifying the last item value of the last TYPE_COMPOSITE
    // item or add another item if the hotspot would exceed the segment size
    source.forEach(target::addRichTextRecord);
    target.addAttachmentIcon(this.m_attachmentProgrammaticName, this.m_fileNameToDisplay);
  }

  @Override
  public boolean isMatch(final List<RichTextRecord<?>> nav) {
    // always append
    return true;
  }

  @Override
  public void richTextNavigationEnd() {
  }

  @Override
  public void richTextNavigationStart() {
  }

}
