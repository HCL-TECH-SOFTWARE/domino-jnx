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
package com.hcl.domino.commons.data;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Document.SignatureData;

/**
 * Container for note signature data
 * 
 * @author Karsten Lehmann
 */
public class SignatureDataImpl implements SignatureData {
	private DominoDateTime m_whenSigned;
	private String m_signer;
	private String m_certifier;
	
	public SignatureDataImpl(DominoDateTime whenSigned, String signer, String certifier) {
		m_whenSigned = whenSigned;
		m_signer = signer;
		m_certifier = certifier;
	}
	
	@Override
	public DominoDateTime getWhenSigned() {
		return m_whenSigned;
	}
	
	@Override
	public String getSigner() {
		return m_signer;
	}
	
	@Override
	public String getCertifier() {
		return m_certifier;
	}
}