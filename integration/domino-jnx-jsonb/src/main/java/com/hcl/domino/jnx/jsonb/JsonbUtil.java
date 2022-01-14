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
package com.hcl.domino.jnx.jsonb;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

enum JsonbUtil {
  ;

  static Set<String> toSet(final Collection<String> names) {
    if (names == null) {
      return null;
    } else {
      final Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      names.stream()
          .filter(Objects::nonNull)
          .forEach(result::add);
      return result;
    }
  }

}
