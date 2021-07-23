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
package com.hcl.domino.jna;

import java.nio.file.Path;
import java.util.List;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;

public class JNADominoClientBuilder extends DominoClientBuilder {

	@Override
	public DominoClient build() {
		return new JNADominoClient(this);
	}

	@Override
	public boolean isFullAccess() {
		return super.isFullAccess();
	}
	
	@Override
	public Path getIDFilePath() {
		return super.getIDFilePath();
	}
	
	@Override
	public String getIDPassword() {
		return super.getIDPassword();
	}
	
	@Override
	public boolean isMaxInternetAccess() {
		return super.isMaxInternetAccess();
	}
	
	@Override
	public String getUserName() {
		return super.getUserName();
	}
	
	@Override
	public List<String> getUserNamesList() {
		return super.getUserNamesList();
	}

	@Override
	public boolean isAsIDUser() {
		return super.isAsIDUser();
	}
	
	@Override
	public String getCredServer() {
		return super.getCredServer();
	}
	@Override
	public String getCredUser() {
		return super.getCredUser();
	}
	@Override
	public String getCredPassword() {
		return super.getCredPassword();
	}
	@Override
	public Object getCredToken() {
		return super.getCredToken();
	}
	
}
