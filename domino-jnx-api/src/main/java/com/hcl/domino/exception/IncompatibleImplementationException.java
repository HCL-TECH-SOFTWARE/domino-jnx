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
package com.hcl.domino.exception;

import java.text.MessageFormat;

/**
 * Exception used to indicate that a JNX object of one implementation was passed
 * to an incompatible implementation object.
 *
 * @author Jesse Gallagher
 * @since 1.0.11
 */
public class IncompatibleImplementationException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  public IncompatibleImplementationException(final Object actual, final Class<?> expected) {
    super(MessageFormat.format("{0} is not a {1}", actual == null ? "null" : actual.getClass().getSimpleName(), //$NON-NLS-2$
        expected.getClass().getName()));
  }

}
