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
import java.util.ListIterator;

import com.hcl.domino.data.Attachment;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDHotspotEnd;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Conversion class that removes all file hotspot occurrences from a richtext
 * items that
 * point to the specified attachment
 *
 * @author Karsten Lehmann
 */
public class RemoveAttachmentIconConversion implements IRichTextConversion {
  private final String m_attachmentFileName;

  public RemoveAttachmentIconConversion(final Attachment att) {
    this.m_attachmentFileName = att.getFileName();
  }

  public RemoveAttachmentIconConversion(final String attachmentFileName) {
    this.m_attachmentFileName = attachmentFileName;
  }

  @Override
  public void convert(final List<RichTextRecord<?>> source, final RichTextWriter target) {
    int currPos = 0;
    if (!source.isEmpty()) {
      while (true) {
        // go through the CD stream, copying all records we find to "target", until we
        // reach
        // a BEGIN, followed by a HOTSPOTBEGIN record with the filename we are searching
        // for
        final int nextBeginBeforeHotspot = this.scanForBeginOfHotspot(source, currPos, target);
        if (nextBeginBeforeHotspot > -1) {
          final int contentAfterHotspotEnd = this.findContentAfterHotspotEnd(source, nextBeginBeforeHotspot);
          if (contentAfterHotspotEnd > -1) {
            // start here reading content at the next loop
            currPos = contentAfterHotspotEnd;
          } else {
            // we are done
            break;
          }
        } else {
          // no more hotspots found
          break;
        }
      }
    } else {
      // richtext empty
    }
  }

  private int findBeginBeforeHotspot(final List<RichTextRecord<?>> nav, final int startPos) {
    return this.scanForBeginOfHotspot(nav, startPos, null);
  }

  /**
   * Starts reading at "startPos" and searches for a record HOTSPOTEND followed by
   * END. These two records
   * mark the end of the file hotspot we want to skip.
   *
   * @param nav      navigator
   * @param startPos start position or null to start at the beginning
   * @return position of the data right after the END record or null if there is
   *         no such data
   */
  private int findContentAfterHotspotEnd(final List<RichTextRecord<?>> nav, final int startPos) {
    if (nav.isEmpty()) {
      return -1;
    }

    final ListIterator<RichTextRecord<?>> iter = nav.listIterator(startPos);
    while (iter.hasNext()) {
      RichTextRecord<?> record = iter.next();
      if (record instanceof CDHotspotEnd) {
        if (iter.hasNext()) {
          record = iter.next();
          if (record instanceof CDEnd) {
            // get the position of the data right after END
            if (iter.hasNext()) {
              return iter.nextIndex();
            } else {
              return -1;
            }
          }
        }
      }
    }

    return -1;
  }

  @Override
  public boolean isMatch(final List<RichTextRecord<?>> nav) {
    final int pos = this.findBeginBeforeHotspot(nav, 0);
    if (pos > -1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void richTextNavigationEnd() {
  }

  @Override
  public void richTextNavigationStart() {
  }

  /**
   * Traverses the CD record stream searching for records BEGIN followed by
   * HOTSPOTBEGIN with the unique
   * filename we are searching for.
   *
   * @param nav          navigator
   * @param startPos     start position for the search or null to start at the
   *                     beginning of the stream
   * @param copyToTarget if not null, we copy all records we find until the right
   *                     BEGIN record to this target
   * @return position of the BEGIN record or null if not found
   */
  private int scanForBeginOfHotspot(final List<RichTextRecord<?>> nav, final int startPos, final RichTextWriter copyToTarget) {
    if (nav.isEmpty()) {
      return -1;
    }

    final ListIterator<RichTextRecord<?>> iter = nav.listIterator(startPos);
    while (iter.hasNext()) {
      RichTextRecord<?> record = iter.next();
      if (record instanceof CDBegin) {
        if (((CDBegin) record).getSignature() == RichTextConstants.SIG_CD_V4HOTSPOTBEGIN) {
          if (iter.hasNext()) {
            record = iter.next();
            // check what is next
            if (record instanceof CDHotspotBegin) {
              if (((CDHotspotBegin) record).getHotspotType() == CDHotspotBegin.Type.FILE) {
                final String uniqueFileName = ((CDHotspotBegin) record).getUniqueFileName();
                if (uniqueFileName.equalsIgnoreCase(this.m_attachmentFileName)) {
                  return iter.previousIndex();
                }
              }
            }
          }
        }
        if (copyToTarget != null) {
          copyToTarget.addRichTextRecord(record);
        }
      } else {
        if (copyToTarget != null) {
          copyToTarget.addRichTextRecord(record);
        }
      }
    }

    return -1;
  }

}
