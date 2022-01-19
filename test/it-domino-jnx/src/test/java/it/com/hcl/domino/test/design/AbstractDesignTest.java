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
package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;

import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.RawColorValue;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public abstract class AbstractDesignTest extends AbstractNotesRuntimeTest {

  protected static void assertColorEquals(ColorValue color, int red, int green, int blue) {
    assertNotNull(color);
    
    String expected = MessageFormat.format("({0}, {1}, {2})", red, green, blue); //$NON-NLS-1$
    String actual = MessageFormat.format("({0}, {1}, {2})", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
    assertEquals(expected, actual);
  }

  protected static void assertColorEquals(RawColorValue color, int red, int green, int blue) {
    assertNotNull(color);
    
    String expected = MessageFormat.format("({0}, {1}, {2})", red, green, blue); //$NON-NLS-1$
    String actual = MessageFormat.format("({0}, {1}, {2})", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
    assertEquals(expected, actual);
  }

  protected <B extends RichTextRecord<?>, E extends RichTextRecord<?>> List<?> extract(List<?> body, int index, Class<B> begin,
      Class<E> end) {
        return extract(body, index, begin::isInstance, end::isInstance);
      }

  protected List<?> extract(List<?> body, int index, Predicate<Object> begin, Predicate<Object> end) {
    int found = 0;
    
    int oleBeginIndex = -1;
    for(oleBeginIndex = 0; oleBeginIndex < body.size(); oleBeginIndex++) {
      if(begin.test(body.get(oleBeginIndex))) {
        if(found == index) {
          break;
        }
        found++;
      }
    }
    assertTrue(oleBeginIndex > -1 && oleBeginIndex < body.size());
    int oleEndIndex = -1;
    for(oleEndIndex = oleBeginIndex; oleEndIndex < body.size(); oleEndIndex++) {
      if(end.test(body.get(oleEndIndex))) {
        break;
      }
    }
    assertTrue(oleEndIndex > -1 && oleEndIndex < body.size());
    
    return body.subList(oleBeginIndex, oleEndIndex+1);
  }

  @SuppressWarnings("unchecked")
  protected <T extends RichTextRecord<?>> T extract(List<?> body, int index, Class<T> type) {
    return (T)extract(body, index, type, type).get(0);
  }
}
