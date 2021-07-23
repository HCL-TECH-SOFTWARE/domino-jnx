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
package com.hcl.domino.jna.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;

@SuppressWarnings("nls")
public class TestDocGetAndSet extends AbstractJNARuntimeTest {


	@Test
	public void testArbitraryUsername() throws IOException {
		DominoClient client = getClient();

		Database dbFakenames = client.openDatabase("", "log.nsf");
		Document doc = dbFakenames.createDocument();

		{
			String val = "abcäöüß";
			doc.replaceItemValue("str1", val);
			String testVal = doc.get("str1", String.class, "");
			
			assertEquals(val, testVal, "Value read/write ok");
		}
		
	}
}
