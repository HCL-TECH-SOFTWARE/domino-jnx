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
package com.hcl.domino.commons.richtext;

import java.nio.ByteBuffer;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.RichTextRecord;

public interface RichtextNavigator extends IAdaptable {

  public interface RichtextPosition {

    /**
     * Can be used to compare this position with another one
     * 
     * @param obj other position
     * @return true if equal
     */
    @Override
    boolean equals(Object obj);

    /**
     * Return a hashcode that can be used to hash the position
     * across items and documents
     * 
     * @return hashcode
     */
    @Override
    int hashCode();
  }

  void copyCurrentRecordTo(RichTextWriter ct);

  /**
   * Returns the current navigation position in the
   * richtext. Use {@link #restorePosition(RichtextPosition)}
   * to return to this position.
   * 
   * @return position
   */
  RichtextPosition getCurrentPosition();

  /**
   * Returns info about the currently focused richtext record
   * 
   * @return record
   */
  RichTextRecord<?> getCurrentRecord();

  /**
   * Returns a read-only buffer to access the CD record data (CD record header
   * BSIG/WSIG/LSIG is not part of
   * the returned data)
   * 
   * @return data buffer with length {@link #getCurrentRecordDataLength()}
   */
  ByteBuffer getCurrentRecordData();

  int getCurrentRecordDataLength();

  /**
   * Returns a read-only buffer to access the CD record data (CD record header
   * BSIG/WSIG/LSIG is included
   * in the returned data)
   * 
   * @return data buffer with length {@link #getCurrentRecordDataLength()}
   */
  ByteBuffer getCurrentRecordDataWithHeader();

  int getCurrentRecordHeaderLength();

  int getCurrentRecordTotalLength();

  short getCurrentRecordTypeConstant();

  String getItemName();

  Document getParentDocument();

  /**
   * Navigates to the first record in the richtext item
   * 
   * @return true if there is a first record
   */
  boolean gotoFirst();

  /**
   * Navigates to the last record
   * 
   * @return true if there is data to read
   */
  boolean gotoLast();

  /**
   * Navigates to the next record in the richtext item
   * 
   * @return true if there is a next record
   */
  boolean gotoNext();

  /**
   * Navigates to the previous record in the richtext item
   * 
   * @return true if there is a previous record
   */
  boolean gotoPrev();

  /**
   * Method to check if there is a next record
   * 
   * @return true if there is data to read
   */
  boolean hasNext();

  /**
   * Method to check if there is a previous CD record
   * 
   * @return true if there is data to read
   */
  boolean hasPrev();

  /**
   * Checks whether the navigator contains any CD records
   * 
   * @return true if empty
   */
  boolean isEmpty();

  /**
   * Restores the current position to a previous one received
   * via {@link #getCurrentPosition()}
   * 
   * @param pos new position
   */
  void restorePosition(RichtextPosition pos);

}
