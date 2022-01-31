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
 * Contains constant values from the Win32 API that are used inside Notes
 * data.
 * 
 * @author Jesse Gallagher
 * @since 1.0.43
 */
public interface WindowsConstants {
  /* Mapping Modes */
  short MM_TEXT = 1;
  short MM_LOMETRIC = 2;
  short MM_HIMETRIC = 3;
  short MM_LOENGLISH = 4;
  short MM_HIENGLISH = 5;
  short MM_TWIPS = 6;
  short MM_ISOTROPIC = 7;
  short MM_ANISOTROPIC = 8;
}
