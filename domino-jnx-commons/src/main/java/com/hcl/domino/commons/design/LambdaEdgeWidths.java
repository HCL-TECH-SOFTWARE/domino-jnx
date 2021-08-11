package com.hcl.domino.commons.design;

import java.util.function.Supplier;

import com.hcl.domino.design.EdgeWidths;

/**
 * Implementation of {@link EdgeWidths} that uses generic lambda expressions
 * to determine each width dynamically.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class LambdaEdgeWidths implements EdgeWidths {
  private final Supplier<Integer> top;
  private final Supplier<Integer> left;
  private final Supplier<Integer> right;
  private final Supplier<Integer> bottom;

  public LambdaEdgeWidths(Supplier<Integer> top, Supplier<Integer> left, Supplier<Integer> right, Supplier<Integer> bottom) {
    this.top = top;
    this.left = left;
    this.right = right;
    this.bottom = bottom;
  }

  @Override
  public int getTop() {
    return this.top.get();
  }

  @Override
  public int getLeft() {
    return this.left.get();
  }

  @Override
  public int getRight() {
    return this.right.get();
  }

  @Override
  public int getBottom() {
    return this.bottom.get();
  }

}
