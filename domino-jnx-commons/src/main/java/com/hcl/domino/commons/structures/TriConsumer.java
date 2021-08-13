package com.hcl.domino.commons.structures;

@FunctionalInterface interface TriConsumer<A, B, C> {
  void accept(A a, B b, C c);
}