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
