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
package com.hcl.domino.jnx.jep454.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.ibm.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestClientBasicsJEP extends AbstractJEPRuntimeTest {
  public static final String PING_SERVER = "PING_SERVER";

  @Test
  public void testNonEmptyUsername() {
    Assertions.assertTrue(StringUtil.isNotEmpty(this.getClient().getIDUserName()));
  }
}
