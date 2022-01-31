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
package com.hcl.domino.richtext.structures;

/**
 * This is a non-structural interface describing common behavior for Composite
 * Data signature structures.
 *
 * @param <S> The number type used for the signature
 * @param <L> The number type used for the length
 * @param <T> The {@link CDSignature} implementation type
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public interface CDSignature<S extends Number, L extends Number, T extends CDSignature<S, L, T>> extends MemoryStructure {
  L getLength();

  S getSignature();

  T setLength(L length);

  T setSignature(S signature);
}
