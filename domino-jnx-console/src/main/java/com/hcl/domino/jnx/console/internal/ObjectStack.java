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
package com.hcl.domino.jnx.console.internal;

/**
 * Implementation of a blocking list of objects with a max number of
 * entries.<br>
 * Use {@link #pushObject(Object)} to insert entries at the start of the list
 * and
 * {@link #popObject()} or {@link #popObjectNoWait()} to take entries from its
 * end.
 */
public class ObjectStack {
  static final int DEF_STACK_SIZE = 20;
  static int curStackSize = 20;
  private final Object[] objectStore;
  private int objectPtr = 0;
  private Object retObject;

  public ObjectStack() {
    this(20);
  }

  public ObjectStack(final int n) {
    if (n > 0) {
      ObjectStack.curStackSize = n;
    }
    this.objectStore = new Object[ObjectStack.curStackSize];
  }

  public synchronized Object popObject() throws InterruptedException {
    this.retObject = null;
    if (this.objectPtr == 0) {
      this.wait();
    }
    if (this.objectPtr >= ObjectStack.curStackSize) {
      this.notify();
    }
    this.retObject = this.objectStore[--this.objectPtr];
    this.objectStore[this.objectPtr] = null;
    return this.retObject;
  }

  public synchronized Object popObjectNoWait() throws InterruptedException {
    this.retObject = null;
    if (this.objectPtr == 0) {
      return this.retObject;
    }
    if (this.objectPtr >= ObjectStack.curStackSize) {
      this.notify();
    }
    this.retObject = this.objectStore[--this.objectPtr];
    this.objectStore[this.objectPtr] = null;
    return this.retObject;
  }

  public synchronized void pushObject(final Object object) throws InterruptedException {
    int n = 0;
    if (this.objectPtr >= ObjectStack.curStackSize) {
      this.wait();
    }
    if (this.objectPtr == 0) {
      this.notify();
    }
    for (n = this.objectPtr; n > 0; --n) {
      this.objectStore[n] = this.objectStore[n - 1];
    }
    this.objectStore[0] = object;
    ++this.objectPtr;
  }
}
