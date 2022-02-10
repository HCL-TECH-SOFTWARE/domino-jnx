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
package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code ods.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface Ods {
  short LONGRECORDLENGTH = 0x0000;
  short WORDRECORDLENGTH = (short) (0xff00 & 0xffff);
  short BYTERECORDLENGTH = 0; /* High byte contains record length */
  
  /*  Saved Query records for items of type TYPE_QUERY */

  short SIG_QUERY_HEADER = (129 | BYTERECORDLENGTH);
  short SIG_QUERY_TEXTTERM = (130 | WORDRECORDLENGTH);
  short SIG_QUERY_BYFIELD = (131 | WORDRECORDLENGTH);
  short SIG_QUERY_BYDATE = (132 | WORDRECORDLENGTH);
  short SIG_QUERY_BYAUTHOR = (133 | WORDRECORDLENGTH);
  short SIG_QUERY_FORMULA = (134 | WORDRECORDLENGTH);
  short SIG_QUERY_BYFORM = (135 | WORDRECORDLENGTH);
  short SIG_QUERY_BYFOLDER = (136 | WORDRECORDLENGTH);
  short SIG_QUERY_USESFORM = (137 | WORDRECORDLENGTH);
  short SIG_QUERY_TOPIC = (138 | WORDRECORDLENGTH);
}
