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
package com.hcl.domino;

import java.util.List;

import com.hcl.domino.data.IAdaptable;

/**
 * A {@link UserNamesList} contains name variants like wildcards (* / Company)
 * and all groups (including nested ones) for a user.
 */
public interface UserNamesList extends Iterable<String>, IAdaptable {

  /**
   * Returns the first value of the list
   *
   * @return primary name
   */
  String getPrimaryName();

  /**
   * Returns the names contained in this names list
   *
   * @return names
   */
  List<String> toList();

}
