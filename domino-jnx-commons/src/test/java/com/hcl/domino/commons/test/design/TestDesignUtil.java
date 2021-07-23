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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import com.hcl.domino.commons.design.DesignUtil;

@SuppressWarnings("nls")
public class TestDesignUtil {

	@Test
	public void testTitleMatch() {
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("Foo")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("Foo|Bar")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("(foo)")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("Bar|Foo")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("Bar", "Foo")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("fOO")));
		assertTrue(DesignUtil.matchesTitleValues("(Foo)", asList("fOO")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("Bar|(Foo)|Baz")));
		assertTrue(DesignUtil.matchesTitleValues("Foo", asList("Bar", "(Foo)", "Baz")));
		assertFalse(DesignUtil.matchesTitleValues("Foo", asList("Food")));
		assertFalse(DesignUtil.matchesTitleValues("Foo", asList("Bar")));
		assertFalse(DesignUtil.matchesTitleValues("Foo", asList("Bar|(Food)|Baz")));
		assertFalse(DesignUtil.matchesTitleValues("Foo", asList("Bar", "(Food)", "Baz")));
	}

	@Test
	public void testToTitlesList() {
		assertEquals(Arrays.asList("foo", "bar"), DesignUtil.toTitlesList(Arrays.asList("foo", "bar")));
		assertEquals(Arrays.asList(""), DesignUtil.toTitlesList(null));
		assertEquals(Arrays.asList("foo", "bar"), DesignUtil.toTitlesList(Arrays.asList("foo|bar")));
		assertEquals(Arrays.asList("foo", "bar", "baz"), DesignUtil.toTitlesList(Arrays.asList("foo|bar", "baz")));
	}
}
