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
package com.hcl.domino.jna.internal.gc.allocations;

import java.lang.ref.ReferenceQueue;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.dxl.JNADxlExporter;
import com.hcl.domino.jna.internal.capi.NotesCAPI;

public class JNADxlExporterAllocations extends APIObjectAllocations<JNADxlExporter> {
	private boolean m_disposed;
	private int m_hDxlExporter;

	@SuppressWarnings("rawtypes")
	public JNADxlExporterAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNADxlExporter referent, ReferenceQueue<? super IAPIObject> q) {
		super(parentDominoClient, parentAllocations, referent, q);
		
	}

	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	@Override
	public void dispose() {
		if (isDisposed()) {
			return;
		}
		
		if(m_hDxlExporter != 0) {
			NotesCAPI.get().DXLDeleteExporter(m_hDxlExporter);
			m_hDxlExporter = 0;
		}
		m_disposed = true;
	}

	public void setDxlExporterHandle(int hDxlExporter) {
		this.m_hDxlExporter = hDxlExporter;
	}
	
	public int getDxlExporterHandle() {
		return this.m_hDxlExporter;
	}
}
