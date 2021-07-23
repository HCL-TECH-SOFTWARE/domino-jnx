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
package com.hcl.domino.commons.dxl;

import com.hcl.domino.dxl.DxlExporterLog;

/**
 * Representation of a DXL exporter log report.
 * 
 * @author Jesse Gallagher
 */
public class DxlExporterLogImpl implements DxlExporterLog {
	public static class DXLErrorImpl implements DxlExporterLog.DxlError {
		private int id;
		private String text;
		
		@Override
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		
		@Override
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
	}
	
	private DxlError error;
	
	public DxlExporterLogImpl(DxlError error) {
		this.error = error;
	}
	
	public void setError(DxlError error) {
		this.error = error;
	}
	
	@Override
	public DxlError getError() {
		return error;
	}
}
