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
package com.hcl.domino.misc;

/**
 * Helper method to control a forEach loop, e.g. to stop the loop or get the
 * current offset
 */
public class Loop {
  private boolean m_isStopped = false;
  private boolean m_isLast = false;
  private int m_index;

  /**
   * Returns the current loop index. If skip(int) is used in the forEach loop,
   * the first loop index is the skip offset value
   *
   * @return index
   */
  public int getIndex() {
    return this.m_index;
  }

  /**
   * Returns true if the current element is the first of the collection
   *
   * @return true if first
   */
  public boolean isFirst() {
    return this.m_index == 0;
  }

  /**
   * Returns true if the current element is the last of the collection
   *
   * @return true if last
   */
  public boolean isLast() {
    return this.m_isLast;
  }

  /**
   * Returns true if forEach loop should be stopped
   *
   * @return true if stopped
   */
  public boolean isStopped() {
    return this.m_isStopped;
  }

  /**
   * Internal method to change current index
   *
   * @param index new index
   */
  protected void setIndex(final int index) {
    this.m_index = index;
  }

  /**
   * Internal method to change isLast flag
   */
  protected void setIsLast() {
    this.m_isLast = true;
  }

  /**
   * Call this method to stop a forEach loop
   */
  public void stop() {
    this.m_isStopped = true;
  }
}