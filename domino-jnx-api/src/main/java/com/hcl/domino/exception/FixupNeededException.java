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

import com.hcl.domino.DominoException;

/**
 * Represents error code 0x03B7, "This database cannot be opened because a consistency check of it is in progress."
 * 
 * @author Jesse Gallagher
 * @since 1.0.21
 */
public class FixupNeededException extends DominoException {
	private static final long serialVersionUID = 1L;

	public FixupNeededException(int id, String message) {
		super(id, message);
	}

	public FixupNeededException(int id, String message, Throwable cause) {
		super(id, message, cause);
	}

	public FixupNeededException(String msg) {
		super(msg);
	}

	public FixupNeededException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
