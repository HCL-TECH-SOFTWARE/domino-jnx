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
package com.hcl.domino.commons.gc;

public interface IGCControl {

  public enum GCAction {
    FLUSH_REFQUEUE, NOOP
  }

  GCAction objectAllocated(IAPIObject<?> parent, IAPIObject<?> obj);

  /**
   * Sets the max number of new API object allocations to flush the
   * reference queue
   * 
   * @param threshold threshold, 0 to not flush the reference queue at all
   */
  void setThreshold(int threshold);

}
