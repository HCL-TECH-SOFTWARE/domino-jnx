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
package it.com.hcl.domino.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.hcl.domino.runtime.DominoRuntime;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestRuntime extends AbstractNotesRuntimeTest {
	private DominoRuntime runtime;
	
	@BeforeEach
	public void initDominoRuntime() {
		this.runtime = getClient().getDominoRuntime();
	}
	
	@Test
	public void testProgramDirectory() {
		Path program = runtime.getProgramDirectory().orElse(null);
		assertNotNull(program);
		assertTrue(Files.isDirectory(program));
	}
	
	@Test
	public void testDataDirectory() {
		Path data = runtime.getDataDirectory().orElse(null);
		assertNotNull(data);
		assertTrue(Files.isDirectory(data));
	}
	
	@Test
	public void testViewRebuildDirectory() {
		Path rebuild = runtime.getViewRebuildDirectory().orElse(null);
		// May not be set
		if(rebuild != null) {
			assertTrue(Files.isDirectory(rebuild));
		}
	}
	
	@Test
	public void testTempDirectory() {
		Path temp = runtime.getTempDirectory().orElse(null);
		assertTrue(Files.isDirectory(temp));
	}
	
	@Test
	@Disabled("Causes segfaults on macOS somehow")
	public void testFerryString() {
		String prop = getClass().getSimpleName() + "_string";
		String val = String.valueOf("time is " + System.currentTimeMillis());
		runtime.setProperty(prop, val);
		try {
			assertEquals(val, runtime.getPropertyString(prop));
		} finally {
			runtime.setProperty(prop, null);
		}
	}
	
	@Test
	@Disabled("Causes segfaults on macOS somehow")
	public void testFerryInt() {
		String prop = getClass().getSimpleName() + "_int";
		int val = Math.abs((int)System.currentTimeMillis() % 1000);
		runtime.setProperty(prop, val);
		try {
			assertEquals(val, runtime.getPropertyInt(prop));
		} finally {
			runtime.setProperty(prop, null);
		}
	}
	
	@Test
	@Disabled("Causes segfaults on macOS somehow")
	public void testFerryConvertible() {
		String prop = getClass().getSimpleName() + "_conv";
		int val = Math.abs((int)System.currentTimeMillis() % 1000);
		runtime.setProperty(prop, val);
		try {
			assertEquals(String.valueOf(val), runtime.getPropertyString(prop));
		} finally {
			runtime.setProperty(prop, null);
		}
	}
}
