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