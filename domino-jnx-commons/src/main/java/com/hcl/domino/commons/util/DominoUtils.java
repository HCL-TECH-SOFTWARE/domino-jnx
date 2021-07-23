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
package com.hcl.domino.commons.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.CAPIGarbageCollector;

public enum DominoUtils {
  ;

  /**
   * Checkes the provided boolean-type Java property and environment value,
   * returning {@code true} if either
   * is {@code "true"} or if the environment value is {@code "1"}.
   * 
   * @param propertyName the Java property name to check, or {@code null} to skip
   * @param envVarName   the environment variable to check, or {@code null} to
   *                     skip
   * @return {@code true} if the property is set; {@code false} otherwise
   * @since 1.0.18
   */
  public static boolean checkBooleanProperty(final String propertyName, final String envVarName) {
    if (StringUtil.isNotEmpty(propertyName)) {
      final boolean propVal = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean(propertyName));
      if (propVal) {
        return true;
      }
    }
    if (StringUtil.isNotEmpty(envVarName)) {
      final String envValString = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getenv(envVarName));
      final boolean envVal = "true".equalsIgnoreCase(envValString) || "1".equalsIgnoreCase(envValString); //$NON-NLS-1$ //$NON-NLS-2$
      if (envVal) {
        return true;
      }
    }
    return false;
  }

  /**
   * Calls {@link System#getenv} wrapped in an {@link AccessController} call to
   * avoid security-manager trouble.
   * 
   * @param envVar the name of the environment variable to retrieve
   * @return the value of the environment variable
   * @since 1.0.18
   */
  public static String getenv(final String envVar) {
    return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getenv(envVar));
  }

  /**
   * Calls {@link System#getProperty} wrapped in an {@link AccessController} call
   * to avoid security-manager trouble.
   * 
   * @param propertyName the name of the Java property to retrieve
   * @param defaultValue the default value when the property is not set
   * @return the value of the property, or {@code defaultValue} if it is not set
   * @since 1.0.18
   */
  public static String getJavaProperty(final String propertyName, final String defaultValue) {
    return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(propertyName, defaultValue));
  }

  /**
   * Determines whether {@link CAPIGarbageCollector} should skip its call to
   * {@link APIObjectAllocations#dispose()}.
   * 
   * @return {@code true} if disposal calls should be skipped, {@code false}
   *         otherwise
   * @since 1.0.18
   */
  public static boolean isDisableGCDispose() {
    return DominoUtils.checkBooleanProperty("jnx.nogcdispose", "JNX_NOGCDISPOSE"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Determines whether calls to {@code NotesInitExtended} should be skipped even
   * when otherwise valid.
   * 
   * @return {@code true} if {@code NotesInitExtended} calls should be skipped,
   *         {@code false} otherwise
   * @since 1.0.2
   */
  public static boolean isNoInit() {
    return DominoUtils.checkBooleanProperty("jnx.noinit", "JNX_NOINIT"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Determines whether calls to {@code NotesInitThread} and
   * {@code NotesTermThread} should be skipped even when
   * otherwise valid.
   * 
   * @return {@code true} if {@code NotesInitThread} calls should be skipped,
   *         {@code false} otherwise
   * @since 1.0.18
   */
  public static boolean isNoInitTermThread() {
    return DominoUtils.checkBooleanProperty("jnx.noinittermthread", "JNX_NOINITTERMTHREAD"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Determines whether calls to {@code NotesTerm} should be skipped even when
   * otherwise valid.
   * 
   * @return {@code true} if {@code NotesTerm} calls should be skipped,
   *         {@code false} otherwise
   * @since 1.0.2
   */
  public static boolean isNoTerm() {
    return DominoUtils.checkBooleanProperty("jnx.noterm", "JNX_NOTERM"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Determines whether the provided file path is a legal replica ID, i.e.
   * it is 16-digit hexadecimal string with an optional ':' in the center
   * position.
   * 
   * @param filePath the file path to check
   * @return whether the file path is a legal replica ID
   */
  public static boolean isReplicaId(final String filePath) {
    if (filePath == null || filePath.isEmpty()) {
      return false;
    }
    final int len = filePath.length();
    String p;
    if (len == 17) {
      if (filePath.charAt(8) != ':') {
        return false;
      }
      p = filePath.substring(0, 8) + filePath.substring(9);
    } else if (len == 16) {
      p = filePath;
    } else {
      return false;
    }

    for (int i = 0; i < 16; i++) {
      final char c = p.charAt(i);
      if (!(Character.isDigit(c) || 'a' <= c && 'f' >= c || 'A' <= c && 'F' >= c)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Determines whether the DominoProcess implementation should skip its thread
   * warning at process termination.
   * 
   * @return {@code true} if the thread warning should be skipped, {@code false}
   *         otherwise
   * @since 1.0.18
   */
  public static boolean isSkipThreadWarning() {
    return DominoUtils.checkBooleanProperty("jnx.skipthreadwarning", null); //$NON-NLS-1$
  }

  /**
   * Calls {@link System#setProperty} wrapped in an {@link AccessController} call
   * to avoid security-manager trouble.
   * 
   * @param propertyName the name of the Java property to set
   * @param value        the value to set
   * @return the previous value of the property
   * @since 1.0.18
   */
  public static String setJavaProperty(final String propertyName, final String value) {
    return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.setProperty(propertyName, value));
  }
}
