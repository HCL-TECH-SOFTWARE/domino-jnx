/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

import com.hcl.domino.data.IAdaptable;

public interface TextStyle extends IAdaptable {
  public enum Justify {
    /** flush left, ragged right */
    LEFT((short) 0),
    /** flush right, ragged left */
    RIGHT((short) 1),
    /** full block justification */
    BLOCK((short) 2),
    /** centered */
    CENTER((short) 3),
    /** no line wrapping AT ALL (except hard CRs) */
    NONE((short) 4);

    private final short m_constant;

    Justify(final short constant) {
      this.m_constant = constant;
    }

    public short getConstant() {
      return this.m_constant;
    }
  }

  Justify getAlign();

  double getFirstLineLeftMargin();

  double getLeftMargin();

  int getLineSpacing();

  String getName();

  int getParagraphSpacingAfter();

  int getParagraphSpacingBefore();

  double getRightMargin();

  short[] getTabPositions();

  int getTabsInTable();

  boolean isDisplayAsBulletList();

  boolean isDisplayAsNumberedList();

  boolean isHideReadOnly();

  boolean isHideReadWrite();

  boolean isHideWhenCopied();

  boolean isHideWhenEditedInPreview();

  boolean isHideWhenPreviewed();

  boolean isHideWhenPrinting();

  boolean isKeepTogether();

  boolean isKeepWithNext();

  boolean isPaginateBefore();

  TextStyle setAlign(Justify align);

  TextStyle setDisplayAsBulletList(boolean b);

  TextStyle setDisplayAsNumberedList(boolean b);

  TextStyle setFirstLineLeftMargin(double margin);

  TextStyle setHideReadOnly(boolean b);

  TextStyle setHideReadWrite(boolean b);

  TextStyle setHideWhenCopied(boolean b);

  TextStyle setHideWhenEditedInPreview(boolean b);

  TextStyle setHideWhenPreviewed(boolean b);

  TextStyle setHideWhenPrinting(boolean b);

  TextStyle setKeepTogether(boolean b);

  TextStyle setKeepWithNext(boolean b);

  TextStyle setLeftMargin(double margin);

  TextStyle setLineSpacing(int spacing);

  TextStyle setPaginateBefore(boolean b);

  TextStyle setParagraphSpacingAfter(int spacing);

  TextStyle setParagraphSpacingBefore(int spacing);

  TextStyle setRightMargin(double margin);

  TextStyle setTabPositions(short[] tabPos);

  TextStyle setTabsInTable(int tabs);

}
