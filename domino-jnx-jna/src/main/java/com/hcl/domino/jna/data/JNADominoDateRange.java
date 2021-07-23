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
package com.hcl.domino.jna.data;

import java.text.MessageFormat;
import java.util.Objects;

import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;

public class JNADominoDateRange implements DominoDateRange, IAdaptable {
	private DominoDateTime m_start;
	private DominoDateTime m_end;
	
	public JNADominoDateRange(DominoDateTime start, DominoDateTime end) {
		m_start = start;
		m_end = end;
	}
	
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		return null;
	}

	@Override
	public DominoDateTime getStartDateTime() {
		return m_start;
	}

	@Override
	public DominoDateTime getEndDateTime() {
		return m_end;
	}

	@Override
	public String toString() {
		return MessageFormat.format("JNADominoDateRange [startdatetime()={0}, enddatetime()={1}]", getStartDateTime(), getEndDateTime()); //$NON-NLS-1$
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DominoDateRange)) {
			return false;
		}
		DominoDateRange o = (DominoDateRange)obj;
		return Objects.equals(m_start, o.getStartDateTime()) && Objects.equals(m_end, o.getEndDateTime());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_end, m_start);
	}

}
