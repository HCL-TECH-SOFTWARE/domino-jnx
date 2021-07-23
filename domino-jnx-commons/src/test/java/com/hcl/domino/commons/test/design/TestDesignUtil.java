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
package com.hcl.domino.commons.test.design;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.design.DesignUtil;

@SuppressWarnings("nls")
public class TestDesignUtil {

  @Test
  public void testTitleMatch() {
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Foo")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Foo|Bar")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("(foo)")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar|Foo")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar", "Foo")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("fOO")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("(Foo)", Arrays.asList("fOO")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar|(Foo)|Baz")));
    Assertions.assertTrue(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar", "(Foo)", "Baz")));
    Assertions.assertFalse(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Food")));
    Assertions.assertFalse(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar")));
    Assertions.assertFalse(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar|(Food)|Baz")));
    Assertions.assertFalse(DesignUtil.matchesTitleValues("Foo", Arrays.asList("Bar", "(Food)", "Baz")));
  }

  @Test
  public void testToTitlesList() {
    Assertions.assertEquals(Arrays.asList("foo", "bar"), DesignUtil.toTitlesList(Arrays.asList("foo", "bar")));
    Assertions.assertEquals(Arrays.asList(""), DesignUtil.toTitlesList(null));
    Assertions.assertEquals(Arrays.asList("foo", "bar"), DesignUtil.toTitlesList(Arrays.asList("foo|bar")));
    Assertions.assertEquals(Arrays.asList("foo", "bar", "baz"), DesignUtil.toTitlesList(Arrays.asList("foo|bar", "baz")));
  }
}
