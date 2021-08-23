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
package com.hcl.domino.commons.structures;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StructureMap {
  final List<StructMember> members = new ArrayList<>();
  final Map<Method, StructMember> getterMap = new HashMap<>();
  final Map<Method, StructMember> setterMap = new HashMap<>();
  final Map<Method, Method> synthSetterMap = new HashMap<>();

  void add(final StructMember member, final List<Method> getters, final List<Method> setters,
      final Map<Method, Method> synthSetters) {
    this.members.add(member);
    getters.forEach(m -> this.getterMap.put(m, member));
    setters.forEach(m -> {
      this.setterMap.put(m, member);
    });
    this.synthSetterMap.putAll(synthSetters);
  }

  int size() {
    return this.members.stream()
        .mapToInt(m -> MemoryStructureUtil.sizeOf(m.type) * m.length)
        .sum();
  }
}