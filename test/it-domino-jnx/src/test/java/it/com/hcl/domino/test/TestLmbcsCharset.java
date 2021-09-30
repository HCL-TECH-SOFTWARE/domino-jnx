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
package it.com.hcl.domino.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.hcl.domino.commons.design.NativeDesignSupport;
import com.hcl.domino.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestLmbcsCharset extends AbstractNotesRuntimeTest {

  @Test
  public void testForNameLMBCS() {
    Charset.forName("LMBCS");
  }

  @Test
  public void testForNameLMBCSNative() {
    Charset.forName("LMBCS-native");
  }

  @ParameterizedTest
  @ValueSource(strings = { "Hello", "EkranAlıntısı1.JPG" })
  public void testRoundTrip(final String expected) {
    final Charset charset = Charset.forName("LMBCS-native");
    final byte[] encoded = expected.getBytes(charset);
    final String decoded = new String(encoded, charset);
    assertEquals(expected, decoded);
    
    final Charset charsetNullTerm = Charset.forName("LMBCS-nullterm");
    final byte[] encodedWithNull = expected.getBytes(charsetNullTerm);
    assertEquals(encoded.length+1, encodedWithNull.length);
    assertEquals(0, encodedWithNull[encodedWithNull.length-1]);
    
    for (int i=0; i<encoded.length; i++) {
      assertEquals(encoded[i], encodedWithNull[i]);
    }
  }
  
  @ParameterizedTest
  @ValueSource(strings = { /* "\n", */ "\r\n" })
  public void testReplaceNewlines(final String delim) {
    final String fileName1 = "EkranAlıntısı1.JPG";
    final String fileName2 = "EkranAlıntısı2.JPG";
    final String testString = fileName1 + delim + fileName2;
    
    final Charset charset = Charset.forName("LMBCS");

    byte[] fileName1Arr = fileName1.getBytes(charset);
    byte[] fileName2Arr = fileName2.getBytes(charset);
    {
      byte[] testStringArr = testString.getBytes(charset);

      //newline delimiter is expected to be \0
      assertEquals(fileName1Arr.length + 1 + fileName2Arr.length, testStringArr.length);
      
      for (int i=0; i<fileName1Arr.length; i++) {
        assertEquals(fileName1Arr[i], testStringArr[i]);
      }
      assertEquals(0, testStringArr[fileName1Arr.length]);
      for (int i=0; i<fileName2Arr.length; i++) {
        assertEquals(fileName2Arr[i], testStringArr[fileName1Arr.length + 1 + i]);
      }
    }
    
    final Charset charsetKeeplines = Charset.forName("LMBCS-keepnewlines");
    byte[] testStringArr_keeplines = testString.getBytes(charsetKeeplines);
    
    {
      byte[] delimArr = delim.getBytes(Charset.forName("ASCII"));
      assertEquals(fileName1Arr.length + delimArr.length + fileName2Arr.length, testStringArr_keeplines.length);
      
      for (int i=0; i<fileName1Arr.length; i++) {
        assertEquals(fileName1Arr[i], testStringArr_keeplines[i]);
      }
      for (int i=0; i<delimArr.length; i++) {
        assertEquals(delimArr[i], testStringArr_keeplines[fileName1Arr.length + i]);
      }
      for (int i=0; i<fileName2Arr.length; i++) {
        assertEquals(fileName2Arr[i], testStringArr_keeplines[fileName1Arr.length + delimArr.length + i]);
      }
    }
    
    final Charset charsetNullTermKeeplines = Charset.forName("LMBCS-nullterm-keepnewlines");
    byte[] testStringArr_nullterm_keeplines = testString.getBytes(charsetNullTermKeeplines);
    assertEquals(testStringArr_keeplines.length+1, testStringArr_nullterm_keeplines.length);
    assertEquals(0, testStringArr_nullterm_keeplines[testStringArr_nullterm_keeplines.length-1]);
    
    for (int i=0; i<testStringArr_keeplines.length; i++) {
      assertEquals(testStringArr_keeplines[i], testStringArr_nullterm_keeplines[i]);
    }
  }
  
  @Test
  public void testStringSplitWithMultibyte() {
    //Turkish dotted i: [105, 20, 3, 7]
    final String dottedI = String.valueOf('\u0130').toLowerCase();
    //Thai character Kho Khuat: [11, -93]
    final String thaiChar = String.valueOf('\u0E03');
    //Hebrew character Aleph: [3, -32]
    final String hebrewChar = String.valueOf('\u05D0');
    //Indian character Ai: [20, 9, 16]
    final String indianChar = String.valueOf('\u0910');
    
    String testString = StringUtil.repeat(dottedI + thaiChar + hebrewChar + indianChar, 40);
    
    int[] i = new int[1];
    for (i[0] = 5; i[0]<(testString.length()+30); i[0]++) {
      String concString = NativeDesignSupport.get().splitAsLMBCS(testString, false, false, i[0])
          .map((bb) -> {
            byte[] arr = new byte[bb.limit()];
            assertTrue(arr.length <= i[0], "Chunk size is <="+i[0]);
            bb.get(arr);

            return new String(arr, Charset.forName("LMBCS"));
          })
          .collect(Collectors.joining());

      assertEquals(testString, concString, "splitAsLMBCS correct for chunk size "+i[0]);
      
    }
  }
  
}
