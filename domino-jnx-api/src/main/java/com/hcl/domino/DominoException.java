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
 package com.hcl.domino;

/**
 * Exception class for Notes C API errors
 * 
 * @author Karsten Lehmann
 */
public class DominoException extends RuntimeException {
	private static final long serialVersionUID = 2712910384246785374L;
	private int m_id;
	
	/**
	 * Creates a new instance
	 * 
	 * @param id error code received from Domino
	 * @param message error message
	 */
	public DominoException(int id, String message) {
		super(message);
		m_id = id;
	}

	/**
	 * Created a new instance
	 * 
	 * @param id error code received from Domino
	 * @param message error message
	 * @param cause exception cause
	 */
	public DominoException(int id, String message, Throwable cause) {
		super(message, cause);
		m_id = id;
	}

	/**
	 * Creates a new instance
	 * 
	 * @param msg error message
	 */
	public DominoException(String msg) {
		this(0, msg);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param msg error message
	 * @param cause exception cause
	 */
	public DominoException(String msg, Throwable cause) {
		this(0, msg, cause);
	}
	
	/**
	 * Returns the C API error code
	 * 
	 * @return code
	 */
	public int getId() {
		return m_id;
	}
}
