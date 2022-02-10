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
package com.hcl.domino.commons.data;

import com.hcl.domino.data.Document.SignatureData;
import com.hcl.domino.data.DominoDateTime;

/**
 * Container for note signature data
 *
 * @author Karsten Lehmann
 */
public class SignatureDataImpl implements SignatureData {
  private final DominoDateTime m_whenSigned;
  private final String m_signer;
  private final String m_certifier;

  public SignatureDataImpl(final DominoDateTime whenSigned, final String signer, final String certifier) {
    this.m_whenSigned = whenSigned;
    this.m_signer = signer;
    this.m_certifier = certifier;
  }

  @Override
  public String getCertifier() {
    return this.m_certifier;
  }

  @Override
  public String getSigner() {
    return this.m_signer;
  }

  @Override
  public DominoDateTime getWhenSigned() {
    return this.m_whenSigned;
  }
}