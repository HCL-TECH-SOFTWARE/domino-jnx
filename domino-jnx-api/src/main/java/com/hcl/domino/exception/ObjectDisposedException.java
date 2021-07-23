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

import com.hcl.domino.DominoException;

/**
 * Exception used to indicate an implementation-specific state of a backend
 * object having been exposed when access is attempted.
 * 
 * @author Jesse Gallagher
 *
 */
public class ObjectDisposedException extends DominoException {
	private static final long serialVersionUID = 1L;
	
	public ObjectDisposedException() {
		this((Object)null);
	}
	
	public ObjectDisposedException(Object obj) {
		this(0, MessageFormat.format("{0} is already disposed", obj == null ? "Object" : obj.getClass().getSimpleName()));
	}

	public ObjectDisposedException(int status, String message) {
		super(status, message);
	}

	public ObjectDisposedException(int status, String message, Throwable cause) {
		super(status, message, cause);
	}

	public ObjectDisposedException(String msg) {
		super(msg);
	}

	public ObjectDisposedException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
