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
package com.hcl.domino.jna.internal.gc.handles;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.concurrent.locks.Lock;
import com.hcl.domino.jna.internal.gc.JNAGCUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * HANDLE on 64 bit systems
 * 
 * @author Karsten Lehmann
 */
public abstract class HANDLE64 implements HANDLE {
  private boolean disposed;

  public static HANDLE64 newInstance(long hdl) {
    return AccessController.doPrivileged((PrivilegedAction<HANDLE64>) () -> new ByValue(hdl));
  }

  @Override
  public <R> R _lockHandleAccess(HandleAccess<HANDLE.ByValue, R> handleAccess) {
    return AccessController.doPrivileged((PrivilegedAction<R>) () -> {
      Lock lock = JNAGCUtil.getHandleLock(HANDLE64.this);
      lock.lock();
      try {
        checkDisposed();

        HANDLE64.ByValue newHdl = new HANDLE64.ByValue(this.getValue());
        return handleAccess.accessLockedHandle(newHdl);
      } finally {
        lock.unlock();
      }
    });
  }

  @Override
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public void setDisposed() {
    disposed = true;
  }

  @Override
  public int size() {
    return 8;
  }

  public static HANDLE64 newInstance(final Pointer peer) {
    return AccessController.doPrivileged((PrivilegedAction<HANDLE64>) () -> new ByReference(peer));
  }

  public static class ByReference extends HANDLE64 implements HANDLE.ByReference {
    Pointer value;

    /**
     * @deprecated only public to be used by JNA; use static newInstance method instead to run in
     *             AccessController.doPrivileged block
     */
    @Deprecated
    public ByReference() {
      this.value = new Memory(8);
      clear();
    }

    /**
     * @param peer memory pointer
     * @deprecated only public to be used by JNA; use static newInstance method instead to run in
     *             AccessController.doPrivileged block
     */
    @Deprecated
    public ByReference(Pointer peer) {
      this.value = peer;
    }

    @Override
    public void clear() {
      this.value.setLong(0, 0);
    }

    @Override
    public long getValue() {
      return this.value.getLong(0);
    }

    @Override
    public Pointer getPointer() {
      return this.value;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> clazz) {
      if(Pointer.class.isAssignableFrom(clazz)) {
        return (T)getPointer();
      }
      return super.getAdapter(clazz);
    }
  };
  public static class ByValue extends HANDLE64 implements HANDLE.ByValue {
    long value;

    /**
     * @deprecated only public to be used by JNA; use static newInstance method instead to run in
     *             AccessController.doPrivileged block
     */
    @Deprecated
    public ByValue() {
      this.value = 0;
    }

    @Deprecated
    public ByValue(long value) {
      this.value = value;
    }

    @Override
    public void clear() {
      this.value = 0;
    }

    @Override
    public long getValue() {
      return this.value;
    }
  };

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if (clazz == HANDLE.class || clazz == HANDLE64.class) {
      return (T) this;
    }

    return null;
  }

  @Override
  public boolean isNull() {
    return getValue() == 0;
  }

  @Override
  public String toString() {
    return MessageFormat.format("HANDLE64 [handle={0}]", getValue()); //$NON-NLS-1$
  }

}
