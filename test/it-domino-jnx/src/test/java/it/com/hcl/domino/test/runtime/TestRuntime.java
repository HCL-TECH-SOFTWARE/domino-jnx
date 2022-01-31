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
package it.com.hcl.domino.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.hcl.domino.runtime.DominoRuntime;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestRuntime extends AbstractNotesRuntimeTest {
  private DominoRuntime runtime;

  @BeforeEach
  public void initDominoRuntime() {
    this.runtime = this.getClient().getDominoRuntime();
  }

  @Test
  public void testDataDirectory() {
    final Path data = this.runtime.getDataDirectory().orElse(null);
    assertNotNull(data);
    assertTrue(Files.isDirectory(data));
  }

  @Test
  @Disabled("Causes segfaults on macOS somehow")
  public void testFerryConvertible() {
    final String prop = this.getClass().getSimpleName() + "_conv";
    final int val = Math.abs((int) System.currentTimeMillis() % 1000);
    this.runtime.setProperty(prop, val);
    try {
      assertEquals(String.valueOf(val), this.runtime.getPropertyString(prop));
    } finally {
      this.runtime.setProperty(prop, null);
    }
  }

  @Test
  @Disabled("Causes segfaults on macOS somehow")
  public void testFerryInt() {
    final String prop = this.getClass().getSimpleName() + "_int";
    final int val = Math.abs((int) System.currentTimeMillis() % 1000);
    this.runtime.setProperty(prop, val);
    try {
      assertEquals(val, this.runtime.getPropertyInt(prop));
    } finally {
      this.runtime.setProperty(prop, null);
    }
  }

  @Test
  @Disabled("Causes segfaults on macOS somehow")
  public void testFerryString() {
    final String prop = this.getClass().getSimpleName() + "_string";
    final String val = String.valueOf("time is " + System.currentTimeMillis());
    this.runtime.setProperty(prop, val);
    try {
      assertEquals(val, this.runtime.getPropertyString(prop));
    } finally {
      this.runtime.setProperty(prop, null);
    }
  }

  @Test
  public void testProgramDirectory() {
    final Path program = this.runtime.getProgramDirectory().orElse(null);
    assertNotNull(program);
    assertTrue(Files.isDirectory(program));
  }

  @Test
  public void testTempDirectory() {
    final Path temp = this.runtime.getTempDirectory().orElse(null);
    assertTrue(Files.isDirectory(temp));
  }

  @Test
  public void testViewRebuildDirectory() {
    final Path rebuild = this.runtime.getViewRebuildDirectory().orElse(null);
    // May not be set
    if (rebuild != null) {
      assertTrue(Files.isDirectory(rebuild));
    }
  }
  
  @Test
  public void testSharedDataDirectory() {
    Path data = this.runtime.getSharedDataDirectory().orElse(null);
    String iniValue = this.runtime.getPropertyString("SharedDataDirectory");
    if(StringUtil.isEmpty(iniValue)) {
      assertNull(data);
    } else {
      assertEquals(Paths.get(iniValue), data);
    }
  }
}
