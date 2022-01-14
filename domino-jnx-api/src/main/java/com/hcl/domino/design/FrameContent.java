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
package com.hcl.domino.design;

import java.util.Optional;

import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.FramesetLayout.FrameSizeUnit;

public interface FrameContent<T> extends IAdaptable {

  Optional<FramesetLayout> getParent();

  void setParent(FramesetLayout parent);
  
	/**
	 * Checks if the frame or its content has changed since loading
	 * 
	 * @return true if dirty
	 */
	boolean isLayoutDirty();

	/**
	 * Resets the dirty flag
	 */
	public void resetLayoutDirty();

  /**
   * Sets the width or height of frame content depending on the
   * parent frameset's orientation
   * 
   * @param amount amount
   * @param unit width unit
   * @return this element
   */
  T setSize(int amount, FrameSizeUnit unit);

  /**
   * Returns the width or height of frame content depending on the
   * parent frameset's orientation
   * 
   * @return size
   */
  int getSize();
  
  /**
   * Returns the size unit of frame content in its parent
   * frameset layout
   * 
   * @return unit
   */
  Optional<FrameSizeUnit> getSizeUnit();

}
