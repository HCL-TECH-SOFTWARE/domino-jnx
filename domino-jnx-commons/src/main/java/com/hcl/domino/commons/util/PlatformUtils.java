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

/**
 * Utility functions for the Domino platform and its OS platform
 */
public class PlatformUtils {
  private static boolean m_is64Bit;
  private static boolean m_isWindows;
  private static boolean m_isMac;
  private static boolean m_isLinux;

  static {
    final String arch = DominoUtils.getJavaProperty("os.arch", null); //$NON-NLS-1$
    PlatformUtils.m_is64Bit = "xmd64".equals(arch) || "x86_64".equals(arch) || "amd64".equals(arch); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final String osName = DominoUtils.getJavaProperty("os.name", ""); //$NON-NLS-1$ //$NON-NLS-2$
    final String osNameLC = osName.toLowerCase();

    if (osNameLC.startsWith("windows")) { //$NON-NLS-1$
      PlatformUtils.m_isWindows = true;
    } else if (osNameLC.startsWith("mac")) { //$NON-NLS-1$
      PlatformUtils.m_isMac = true;
    } else if (osNameLC.startsWith("linux")) { //$NON-NLS-1$
      PlatformUtils.m_isLinux = true;
    }
  }

  /**
   * Checks if the current JVM is running in 32 bit mode
   * 
   * @return true if 32 bit
   */
  public static boolean is32Bit() {
    return !PlatformUtils.m_is64Bit;
  }

  /**
   * Checks if the current JVM is running in 64 bit mode
   * 
   * @return true if 64 bit
   */
  public static boolean is64Bit() {
    return PlatformUtils.m_is64Bit;
  }

  /**
   * Method to check if we are running in a Linux environment
   * 
   * @return true if Linux
   */
  public static boolean isLinux() {
    return PlatformUtils.m_isLinux;
  }

  /**
   * Method to check if we are running in a Mac environment
   * 
   * @return true if Mac
   */
  public static boolean isMac() {
    return PlatformUtils.m_isMac;
  }

  /**
   * Checks if we are running in a Windows 32 bit environment
   * 
   * @return true if win32
   */
  public static boolean isWin32() {
    return PlatformUtils.isWindows() && PlatformUtils.is32Bit();
  }

  /**
   * Method to check if we are running in a Windows environment
   * 
   * @return true if Windows
   */
  public static boolean isWindows() {
    return PlatformUtils.m_isWindows;
  }
}