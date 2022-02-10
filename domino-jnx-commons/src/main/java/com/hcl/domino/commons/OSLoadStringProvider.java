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
package com.hcl.domino.commons;

import com.hcl.domino.misc.JNXServiceFinder;

/**
 * This service interface provides an implementation-neutral mechanism for
 * loading strings
 * via {@code OSLoadString}.
 *
 * @author Jesse Gallagher
 * @since 1.0.19
 */
public interface OSLoadStringProvider {
  /**
   * @return an implementation of {@code OSLoadStringProvider}
   * @throws IllegalStateException if the active API implementation does not
   *                               provide one
   */
  static OSLoadStringProvider get() {
    return JNXServiceFinder.findRequiredService(OSLoadStringProvider.class, OSLoadStringProvider.class.getClassLoader());
  }

  String loadString(int module, short status);
}
